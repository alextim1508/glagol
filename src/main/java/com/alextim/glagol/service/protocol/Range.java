package com.alextim.glagol.service.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Range {
    AUTO(0, "Авто"),
    RANGE_1(1, "1"),
    RANGE_2(2, "2"),
    RANGE_3(3, "3");

    private final int code;
    private final String description;

    public static Range fromCode(int code) {
        for (Range range : Range.values()) {
            if (range.getCode() == code) {
                return range;
            }
        }
        throw new IllegalArgumentException("Unknown range code: " + code);
    }
}