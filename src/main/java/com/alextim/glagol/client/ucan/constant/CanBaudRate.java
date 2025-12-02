package com.alextim.glagol.client.ucan.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CanBaudRate {
    USBCAN_BAUD_10kBit((short) 0x672F, "10 kbps"),
    USBCAN_BAUD_20kBit((short) 0x532F, "20 kbps"),
    USBCAN_BAUD_50kBit((short) 0x472F, "50 kbps"),
    USBCAN_BAUD_100kBit((short) 0x432F, "100 kbps"),
    USBCAN_BAUD_125kBit((short) 0x031C, "125 kbps"),
    USBCAN_BAUD_250kBit((short) 0x011C, "250 kbps"),
    USBCAN_BAUD_500kBit((short) 0x001C, "500 kbps"),
    USBCAN_BAUD_800kBit((short) 0x0016, "800 kbps"),
    USBCAN_BAUD_1MBit((short) 0x0014, "1000 kbps");

    private final short value;
    private final String description;

    public static CanBaudRate fromValue(short value) {
        for (CanBaudRate rate : CanBaudRate.values()) {
            if (rate.getValue() == value) return rate;
        }
        throw new IllegalArgumentException("Unknown CAN baud rate value: " + value);
    }
}