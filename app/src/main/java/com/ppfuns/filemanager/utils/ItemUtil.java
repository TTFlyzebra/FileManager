package com.ppfuns.filemanager.utils;

import com.ppfuns.filemanager.constants.ItemType;
import com.ppfuns.filemanager.entity.i.AbstractMediaItem;
import com.ppfuns.filemanager.entity.i.IAlbum;
import com.ppfuns.filemanager.entity.i.IArtist;
import com.ppfuns.filemanager.entity.i.IBucket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * item的工具辅助类
 * <p/>
 * Created by 李冰锋 on 2016/8/11 16:10.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.utils
 */
public class ItemUtil {
    public final static String TAG = ItemUtil.class.getSimpleName();

    public static <T extends AbstractMediaItem> Set<Long> getIdSet(List<T> list) {
        Set<Long> set = new HashSet<>();
        for (AbstractMediaItem item : list) {
            if (item instanceof IArtist) {
                set.add(((IArtist) item).getArtistId());
            } else if (item instanceof IAlbum) {
                set.add(((IAlbum) item).getAlbumId());
            } else if (item instanceof IBucket) {
                set.add(((IBucket) item).getBucketId());
            }
        }
        return set;
    }

    /**
     * @param list 传入解析的AbstractMediaItem list
     * @param <T>  继承自AbstractMediaItem的类
     * @return 返回的解析结果，key：id，List<T> 对应id的列表
     */
    public static <T extends AbstractMediaItem> Map<Long, List<T>> getListMap(List<T> list, Class<T> clz) {
        Map<Long, List<T>> map = new HashMap<>();

        Set<Long> artistIdSet = getIdSet(list);
        for (long id : artistIdSet) {
            List<T> tmp = new ArrayList<>();
            for (T item : list) {
                if (IArtist.class.equals(clz) && ((IArtist) item).getArtistId() == id) {
                    tmp.add(item);
                } else if (IAlbum.class.equals(clz) && ((IAlbum) item).getAlbumId() == id) {
                    tmp.add(item);
                } else if (IBucket.class.equals(clz) && ((IBucket) item).getBucketId() == id) {
                    tmp.add(item);
                }
            }
            map.put(id, tmp);
        }
        return map;
    }

     public static List<AbstractMediaItem> getListByType(List<AbstractMediaItem> list, ItemType... itemTypes) {
        if (list == null) {
            return null;
        }
        ArrayList<AbstractMediaItem> abstractMediaItems = new ArrayList<>();
        for (AbstractMediaItem abstractMediaItem : list) {
            for (int i = 0; i < itemTypes.length; i++) {
                if (abstractMediaItem.mItemType == itemTypes[i]) {
                    abstractMediaItems.add(abstractMediaItem);
                }
            }
        }
        return abstractMediaItems;
    }
}
