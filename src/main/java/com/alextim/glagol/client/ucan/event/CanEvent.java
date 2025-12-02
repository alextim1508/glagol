package com.alextim.glagol.client.ucan.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CanEvent {
    USBCAN_EVENT_INITHW((byte) 0x00, "Init HW"),
    USBCAN_EVENT_INITCAN((byte) 0x01, "Init CAN"),
    USBCAN_EVENT_RECEIVE((byte) 0x02, "Receive"),
    USBCAN_EVENT_STATUS((byte) 0x03, "Status"),
    USBCAN_EVENT_DEINITCAN((byte) 0x04, "Deinit CAN"),
    USBCAN_EVENT_DEINITHW((byte) 0x05, "Deinit HW"),
    USBCAN_EVENT_CONNECT((byte) 0x06, "Connect"),
    USBCAN_EVENT_DISCONNECT((byte) 0x07, "Disconnect"),
    USBCAN_EVENT_FATALDISCON((byte) 0x08, "Fatal disconnect"),
    USBCAN_EVENT_USBBUS_ERROR((byte) 0x10, "USB bus error"),
    USBCAN_EVENT_RECONNECT((byte) 0x11, "Reconnect");

    private final byte value;
    private final String description;

    public static CanEvent fromValue(int value) {
        for (CanEvent event : CanEvent.values()) {
            if (event.getValue() == value) {
                return event;
            }
        }
        throw new IllegalArgumentException("Unknown CAN event value: " + value);
    }
}
