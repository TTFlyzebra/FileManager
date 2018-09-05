package com.ppfuns.filemanager.module;

import com.ppfuns.filemanager.utils.FileUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.StringTokenizer;

import fi.iki.elonen.NanoHTTPD;
import jcifs.smb.SmbFile;

/**
 * Created by 李冰锋 on 2016/10/14 15:24.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.module
 */
public class SmbHttpServer extends NanoHTTPD {
    public final static String TAG = SmbHttpServer.class.getSimpleName();

    public SmbHttpServer(int port) {
        super(port);
    }

//    @Override
//    public Response serve(IHTTPSession session) {
//        Log.d(TAG, "serve: " + session.getHeaders());
//        Response response = null;
//        try {
//            String uri = session.getUri();
//            String path = SmbContentHelper.getPath(uri);
//            SmbFile smbFile = new SmbFile(path);
//            InputStream inputStream = smbFile.getInputStream();
//            response = newFixedLengthResponse(Response.Status.OK, "video/mp4", inputStream, smbFile.getContentLength());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return response;
//    }

    @Override
    public Response serve(IHTTPSession session) {
        Response res = null;

        String uri = session.getUri();
        // Remove URL arguments
        uri = uri.trim().replace(File.separatorChar, '/');
        if (uri.indexOf('?') >= 0)
            uri = uri.substring(0, uri.indexOf('?'));

        // Prohibit getting out of current directory
        if (uri.startsWith("..") || uri.endsWith("..") || uri.indexOf("../") >= 0)
            res = newFixedLengthResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT,
                    "FORBIDDEN: Won't serve ../ for security reasons.");

//        File f = new File(homeDir, uri);
        String path = SmbContentHelper.getPath(uri);
        SmbFileWrapper f;

        try {
            f = new SmbFileWrapper(new SmbFile(path));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return res;
        }


        if (res == null && !f.exists())
            res = newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                    "Error 404, file not found.");

        // List the directory, if necessary
        if (res == null && f.isDirectory()) {
            // Browsers get confused without '/' after the
            // directory, send a redirect.
            if (!uri.endsWith("/")) {
                uri += "/";
                res = newFixedLengthResponse(Response.Status.REDIRECT, MIME_HTML,
                        "<html><body>Redirected: <a href=\"" + uri + "\">" +
                                uri + "</a></body></html>");
                res.addHeader("Location", uri);
            }

            if (res == null) {
                // First try index.html and index.htm
                String[] files = f.list();
                String msg = "<html><body><h1>Directory " + uri + "</h1><br/>";

                if (uri.length() > 1) {
                    String u = uri.substring(0, uri.length() - 1);
                    int slash = u.lastIndexOf('/');
                    if (slash >= 0 && slash < u.length())
                        msg += "<b><a href=\"" + uri.substring(0, slash + 1) + "\">..</a></b><br/>";
                }

                if (files != null) {
                    for (int i = 0; i < files.length; ++i) {
                        File curFile = new File(f, files[i]);
                        boolean dir = curFile.isDirectory();
                        if (dir) {
                            msg += "<b>";
                            files[i] += "/";
                        }

                        msg += "<a href=\"" + encodeUri(uri + files[i]) + "\">" +
                                files[i] + "</a>";

                        // Show file size
                        if (curFile.isFile()) {
                            long len = curFile.length();
                            msg += " &nbsp;<font size=2>(";
                            if (len < 1024)
                                msg += len + " bytes";
                            else if (len < 1024 * 1024)
                                msg += len / 1024 + "." + (len % 1024 / 10 % 100) + " KB";
                            else
                                msg += len / (1024 * 1024) + "." + len % (1024 * 1024) / 10 % 100 + " MB";

                            msg += ")</font>";
                        }
                        msg += "<br/>";
                        if (dir) msg += "</b>";
                    }
                }
                msg += "</body></html>";
                res = newFixedLengthResponse(Response.Status.OK, MIME_HTML, msg);
            }
        }

        try {
            if (res == null) {
                // Get MIME type from file name extension, if possible
                String mime = null;
                int dot = f.getCanonicalPath().lastIndexOf('.');
                if (dot >= 0)
                    mime = FileUtil.getMimeType(f.getCanonicalPath());
                if (mime == null)
                    mime = "application/octet-stream";

                // Support (simple) skipping:
                long startFrom = 0;
                long endAt = -1;
                String range = session.getHeaders().get("range");
                if (range != null) {
                    if (range.startsWith("bytes=")) {
                        range = range.substring("bytes=".length());
                        int minus = range.indexOf('-');
                        try {
                            if (minus > 0) {
                                startFrom = Long.parseLong(range.substring(0, minus));
                                endAt = Long.parseLong(range.substring(minus + 1));
                            }
                        } catch (NumberFormatException ignored) {
                            ignored.printStackTrace();
                        }
                    }
                }

                // Change return code and add Content-Range header when skipping is requested
                long fileLen = f.length();
                if (range != null && startFrom >= 0) {
                    if (startFrom >= fileLen) {
                        res = newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, MIME_PLAINTEXT, "");
                        res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    } else {
                        if (endAt < 0)
                            endAt = fileLen - 1;
                        long newLen = endAt - startFrom + 1;
                        if (newLen < 0) newLen = 0;

                        final long dataLen = newLen;
                        InputStream fis = new BufferedInputStream(f.get().getInputStream()) {
                            @Override
                            public synchronized int available() throws IOException {
                                return (int) dataLen;
                            }
                        };

                        fis.skip(startFrom);

                        res = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mime, fis, fileLen);
                        res.addHeader("Content-Length", "" + dataLen);
                        res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    }
                } else {
                    res = newFixedLengthResponse(Response.Status.OK, mime, new BufferedInputStream(f.get().getInputStream()), fileLen);
                    res.addHeader("Content-Length", "" + fileLen);
                }
            }
        } catch (IOException ioe) {
            res = newFixedLengthResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }

        res.addHeader("Accept-Ranges", "bytes"); // Announce that the file server accepts partial content requestes
        return res;
    }

    /**
     * URL-encodes everything between "/"-characters.
     * Encodes spaces as '%20' instead of '+'.
     */
    private String encodeUri(String uri) {
        String newUri = "";
        StringTokenizer st = new StringTokenizer(uri, "/ ", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("/"))
                newUri += "/";
            else if (tok.equals(" "))
                newUri += "%20";
            else {
                newUri += URLEncoder.encode(tok);
                // For Java 1.4 you'll want to use this instead:
                // try { newUri += URLEncoder.encode( tok, "UTF-8" ); } catch ( java.io.UnsupportedEncodingException uee ) {}
            }
        }
        return newUri;
    }
}
