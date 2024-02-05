package com.awen.energy.protocol;

import lombok.Getter;

@Getter
public enum StatusType {

    //操作标识
    HEARTBEAT("hbt"), LOGIN("log"), OPEN("opn"),
    CW_HEARTBEAT("CSQ"), CW_LOGIN("CheckIn"), CW_OPEN("=GoodsNotice");

    private final String type;

    StatusType(String type) {
        this.type = type;
    }

    public static <T extends StatusType> T getByType(String type, Class<T> enumClass) {
        for (T each : enumClass.getEnumConstants()) {
            if (type.equals(each.getType())) {
                return each;
            }
        }
        return null;
    }
}
