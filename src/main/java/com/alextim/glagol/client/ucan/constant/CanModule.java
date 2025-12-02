package com.alextim.glagol.client.ucan.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CanModule {
    USBCAN_ANY_MODULE((byte) 255);

    private final byte value;
}
