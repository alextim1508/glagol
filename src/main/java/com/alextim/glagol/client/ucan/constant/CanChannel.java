package com.alextim.glagol.client.ucan.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CanChannel {
    USBCAN_CHANNEL_CH0((byte) 0x00, "CAN channel 0"),
    USBCAN_CHANNEL_CH1((byte) 0x01, "CAN channel 1"),
    USBCAN_CHANNEL_ANY((byte) 0xFF, "CAN channel 0 or 1");

    private final byte value;
    private final String description;


    public static CanChannel fromValue(byte value) {
        for (CanChannel channel : CanChannel.values()) {
            if (channel.getValue() == value) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unknown CAN channel value: " + (value & 0xFF));
    }
}