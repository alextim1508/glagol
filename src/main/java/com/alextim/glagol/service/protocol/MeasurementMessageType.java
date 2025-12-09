package com.alextim.glagol.service.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MeasurementMessageType {
    HEADER(1, "Заголовок измерительного сообщения"),
    COUNTS(2, "Количество импульсов"),
    DOSE_RATE(3, "Мощность дозы");

    private final int sequenceNumber;
    private final String description;

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getDescription() {
        return description;
    }

    public static MeasurementMessageType fromSequenceNumber(int number) {
        for (MeasurementMessageType type : MeasurementMessageType.values()) {
            if (type.getSequenceNumber() == number) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown measurement message sequence number: " + number);
    }
}
