package com.alextim.glagol.client.ucan.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CanFrameFormat {
    USBCAN_MSG_FF_STD((byte) 0x00, "STD"),
    USBCAN_MSG_FF_ECHO((byte)0x20, "Echo"),
    USBCAN_MSG_FF_RTR((byte)0x40, "RTR"),
    USBCAN_MSG_FF_EXT((byte)0x80, "EXT");

    private final byte value;
    private final String description;

    public static CanFrameFormat fromValue(byte value) {
        for (CanFrameFormat format : CanFrameFormat.values()) {
            if (format.getValue() == value) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unknown CAN frame format value: " + (value & 0xFF));
    }
}