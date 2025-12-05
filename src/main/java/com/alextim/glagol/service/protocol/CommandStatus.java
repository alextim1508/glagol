package com.alextim.glagol.service.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommandStatus {
    SUCCESS((byte) 0x00, "Выполнена успешно"),
    ACCEPTED((byte) 0x01, "Принята к исполнению"),
    IN_PROGRESS((byte) 0x03, "Выполняется"),
    WITH_ERRORS((byte) 0x04, "Выполнена с ошибками");

    private final byte code;
    private final String description;

    public static CommandStatus fromCode(byte code) {
        for (CommandStatus status : CommandStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown command status code:  0x%02X", code));
    }
}