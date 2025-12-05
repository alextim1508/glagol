package com.alextim.glagol.service.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DeviceType {
    ALL_DEVICES((byte) 0x00, "Все устройства (и БД в том числе)"),
    BOI((byte) 0x01, "БОИ (Блок Отображения Информации)"),
    PC((byte) 0x02, "ПЭВМ (Персональный Электронно-Вычислительная Машина)"),
    RESERVED_03((byte) 0x03, "Зарезервировано"),
    VB((byte) 0x04, "ВБ (Вспомогательный блок)"),
    IGNORE_DEVICE((byte) 0x05, "Игнорируемое устройство"),
    RESERVED_06((byte) 0x06, "Зарезервировано"),
    RESERVED_07((byte) 0x07, "Зарезервировано");

    private final byte code;
    private final String description;

    public static DeviceType fromCode(int code) {
        for (DeviceType type : DeviceType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }

        if (code >= 0x1000 && code <= 0x17FF) {
            throw new IllegalArgumentException("Code " + String.format("0x%04X", code) + " belongs to the range of Base Device (BD) types, not a standard DeviceType.");
        }
        throw new IllegalArgumentException("Unknown device type code: " + String.format("0x%02X", code));
    }
}