package com.alextim.glagol.client.ucan.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CanMode {
    UCAN_MODE_NORMAL((byte) 0x00, "normal"),
    UCAN_MODE_LISTEN_ONLY((byte)0x01, "listen only"),
    UCAN_MODE_TX_ECHO((byte)0x02, "TX echo"),
    UCAN_MODE_RX_ORDER_CH((byte)0x04, "RX order CH"),
    UCAN_MODE_HIGH_RES_TIMER((byte)0x08, "high res timer"),
    UCAN_MODE_RESERVED((byte)0x10, "reserved");

    private final byte value;
    private final String description;

    public static CanMode fromValue(byte value) {
        for (CanMode mode : CanMode.values()) {
            if (mode.getValue() == value) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown CAN mode value: " + (value & 0xFF));
    }
}