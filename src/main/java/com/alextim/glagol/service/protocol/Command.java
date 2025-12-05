package com.alextim.glagol.service.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Command {
    RESTART((byte) 0x01, "Перезапустить устройство"),
    GET_PARAM((byte) 0x04, "Выдать параметр"),
    SET_PARAM((byte) 0x05, "Задать параметр"),
    START_MEASURE((byte) 0x40, "Старт измерения"),
    STOP_MEASURE((byte) 0x41, "Стоп измерения");

    private final byte code;
    private final String description;

    public static Command fromCode(byte code) {
        for (Command cmd : Command.values()) {
            if (cmd.getCode() == code) {
                return cmd;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown command code:  0x%02X", code));
    }
}