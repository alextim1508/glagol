package com.alextim.glagol.service.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MessageCategory {
    ALARM((byte) 0x00, "Аварийное"),
    CONTROL((byte) 0x01, "Управляющее"),
    RESPONSE((byte) 0x02, "Ответное"),
    MEASUREMENT((byte) 0x03, "Измерительная информация");

    private final byte code;
    private final String description;

    public static MessageCategory fromCode(byte code) {
        for (MessageCategory category : MessageCategory.values()) {
            if (category.getCode() == code) {
                return category;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown message category code:  0x%02X", code));
    }
}