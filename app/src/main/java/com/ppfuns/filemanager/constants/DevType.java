package com.ppfuns.filemanager.constants;

/**
 * Created by 李冰锋 on 2016/7/29 16:31.
 * E-mail:libf@ppfuns.com
 * Package: com.ppfuns.filemanager.constants
 */
public enum DevType {
    USB_DEV(StrConst.DESC_USB, ItemPriority.DevItemPriority.PRI_USB),
    DLNA_DEV(StrConst.DESC_DLNA, ItemPriority.DevItemPriority.PRI_DLNA),
    SD_CARD_DEV(StrConst.DESC_SD_CARD, ItemPriority.DevItemPriority.PRI_SD_CARD),
    SAMBA_DEV(StrConst.DESC_SAMBA, ItemPriority.DevItemPriority.PRI_SAMBA),
    LOCAL_DEV(StrConst.DESC_LOCAL, ItemPriority.DevItemPriority.PRI_LOCAL),;

    private String desc;
    private int priority;
    private int order;

    DevType(String desc, int priority) {
        this.desc = desc;
        this.priority = priority;
        this.order = this.ordinal();
    }

    public static DevType getTypeByOrdinal(int order) {
        if (order == USB_DEV.order) {
            return USB_DEV;
        } else if (order == DLNA_DEV.order) {
            return DLNA_DEV;
        } else if (order == SD_CARD_DEV.order) {
            return SD_CARD_DEV;
        } else if (order == LOCAL_DEV.order) {
            return LOCAL_DEV;
        } else {
            return null;
        }
    }

    public int getOrder() {
        return order;
    }

    public String getDesc() {
        return desc;
    }

    public int getPriority() {
        return priority;
    }
}
