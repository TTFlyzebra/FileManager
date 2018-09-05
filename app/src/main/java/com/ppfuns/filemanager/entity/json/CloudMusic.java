package com.ppfuns.filemanager.entity.json;

import java.util.List;

/**
 * Created by 李冰锋 on 2017/1/12 18:07.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.entity.json
 */
public class CloudMusic {


    /**
     * result : {"songCount":11,"songs":[{"id":343065,"name":"遇","artists":[{"id":11000,"name":"ai.mini","picUrl":null}],"album":{"id":33820,"name":"爱100 春天的味道","artist":{"id":0,"name":"","picUrl":null},"picUrl":"http://p1.music.126.net/uINwghY8KgS5kg63S50OSg==/123145302324071.jpg"},"audio":"http://m2.music.126.net/HTNsQs_vFe6m-CJ7i0x71Q==/1129198441734093.mp3","djProgramId":0}]}
     * code : 200
     */

    public ResultBean result;
    public int code;

    @Override
    public String toString() {
        return "CloudMusic{" +
                "result=" + result +
                ", code=" + code +
                '}';
    }

    public static class ResultBean {
        /**
         * songCount : 11
         * songs : [{"id":343065,"name":"遇","artists":[{"id":11000,"name":"ai.mini","picUrl":null}],"album":{"id":33820,"name":"爱100 春天的味道","artist":{"id":0,"name":"","picUrl":null},"picUrl":"http://p1.music.126.net/uINwghY8KgS5kg63S50OSg==/123145302324071.jpg"},"audio":"http://m2.music.126.net/HTNsQs_vFe6m-CJ7i0x71Q==/1129198441734093.mp3","djProgramId":0}]
         */

        public int songCount;
        public List<SongsBean> songs;

        @Override
        public String toString() {
            return "ResultBean{" +
                    "songCount=" + songCount +
                    ", songs=" + songs +
                    '}';
        }

        public static class SongsBean {
            /**
             * id : 343065
             * name : 遇
             * artists : [{"id":11000,"name":"ai.mini","picUrl":null}]
             * album : {"id":33820,"name":"爱100 春天的味道","artist":{"id":0,"name":"","picUrl":null},"picUrl":"http://p1.music.126.net/uINwghY8KgS5kg63S50OSg==/123145302324071.jpg"}
             * audio : http://m2.music.126.net/HTNsQs_vFe6m-CJ7i0x71Q==/1129198441734093.mp3
             * djProgramId : 0
             */

            public int id;
            public String name;
            public AlbumBean album;
            public String audio;
            public int djProgramId;
            public List<ArtistsBean> artists;

            @Override
            public String toString() {
                return "SongsBean{" +
                        "id=" + id +
                        ", name='" + name + '\'' +
                        ", album=" + album +
                        ", audio='" + audio + '\'' +
                        ", djProgramId=" + djProgramId +
                        ", artists=" + artists +
                        '}';
            }

            public static class AlbumBean {
                /**
                 * id : 33820
                 * name : 爱100 春天的味道
                 * artist : {"id":0,"name":"","picUrl":null}
                 * picUrl : http://p1.music.126.net/uINwghY8KgS5kg63S50OSg==/123145302324071.jpg
                 */

                public int id;
                public String name;
                public ArtistBean artist;
                public String picUrl;

                @Override
                public String toString() {
                    return "AlbumBean{" +
                            "id=" + id +
                            ", name='" + name + '\'' +
                            ", artist=" + artist +
                            ", picUrl='" + picUrl + '\'' +
                            '}';
                }

                public static class ArtistBean {
                    /**
                     * id : 0
                     * name :
                     * picUrl : null
                     */

                    public int id;
                    public String name;
                    public Object picUrl;

                    @Override
                    public String toString() {
                        return "ArtistBean{" +
                                "id=" + id +
                                ", name='" + name + '\'' +
                                ", picUrl=" + picUrl +
                                '}';
                    }
                }
            }

            public static class ArtistsBean {
                /**
                 * id : 11000
                 * name : ai.mini
                 * picUrl : null
                 */

                public int id;
                public String name;
                public Object picUrl;

                @Override
                public String toString() {
                    return "ArtistsBean{" +
                            "id=" + id +
                            ", name='" + name + '\'' +
                            ", picUrl=" + picUrl +
                            '}';
                }
            }
        }
    }
}
