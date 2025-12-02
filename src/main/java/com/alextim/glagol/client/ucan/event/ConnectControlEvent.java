package com.alextim.glagol.client.ucan.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ConnectControlEvent {
    USBCAN_EVENT_CONNECT((byte) 0x06, "Connect"),
    USBCAN_EVENT_DISCONNECT((byte) 0x07, "Disconnect"),
    USBCAN_EVENT_FATALDISCON((byte) 0x08, "Fatal disconnect");

    private final byte value;
    private final String description;

    public static ConnectControlEvent fromValue(int value) {
        for (ConnectControlEvent event : ConnectControlEvent.values()) {
            if (event.getValue() == value) {
                return event;
            }
        }
        throw new IllegalArgumentException("Unknown connect control event value: " + value);
    }
}