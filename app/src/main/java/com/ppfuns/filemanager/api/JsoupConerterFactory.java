package com.ppfuns.filemanager.api;

import android.util.Log;

import com.ppfuns.filemanager.entity.DangBeiAppEntity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by 李冰锋 on 2016/12/19 10:28.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.api
 */
public class JsoupConerterFactory extends Converter.Factory {
    public final static String TAG = JsoupConerterFactory.class.getSimpleName();

    public JsoupConerterFactory() {
        super();
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new JsoupResponseBodyConverter();
    }

    private class JsoupResponseBodyConverter implements Converter<ResponseBody, List<DangBeiAppEntity>> {

        @Override
        public List<DangBeiAppEntity> convert(ResponseBody value) throws IOException {
            List<DangBeiAppEntity> list = new ArrayList<>();
            Log.d(TAG, "ResponseBody is null: " + (value == null));

            try {
                Document document = Jsoup.parse(value.string());
                Element softList = document.getElementById("softList");
                Elements lis = softList.getElementsByTag("li");
                for (Element li : lis) {
                    DangBeiAppEntity dangBeiAppEntity = new DangBeiAppEntity();
                    list.add(dangBeiAppEntity);

                    Element a0 = li.getElementsByTag("a")
                            .get(0);

                    /**
                     * id
                     */
                    Log.d(TAG, "convert: " + a0.attr("href"));
                    String href1 = a0.attr("href").split("\\.")[0];
                    int id = Integer.parseInt(
                            href1.substring(href1.lastIndexOf("/") + 1)
                    );
                    dangBeiAppEntity.setId(id);

                    /**
                     * 图标
                     */
                    String icon = a0.getElementsByTag("img").get(0)
                            .attr("src");
                    dangBeiAppEntity.setIcon(icon);

                    /**
                     * title
                     */
                    Element title = li.getElementsByClass("title").get(0);
                    dangBeiAppEntity.setTitle(title.text());

                    /**
                     * star
                     */
                    String startCount = title.getElementsByClass("star").get(0)
                            .attr("number");
                    dangBeiAppEntity.setStar(Integer.parseInt(startCount));

                    /**
                     * date/size
                     */
                    Elements infoVal = li.getElementsByClass("softInfo").get(0)
                            .getElementsByClass("infoVal");
                    dangBeiAppEntity.setSize(infoVal.get(0).text());
                    dangBeiAppEntity.setDate(infoVal.get(1).text());
                }
            } catch (Exception pE) {
                pE.printStackTrace();
                throw new IOException(pE);
            }


            return list;
        }

    }


}
