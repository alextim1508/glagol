package com.alextim.glagol.service.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AddressInfo {

    private final MessageCategory category;
    private final boolean isBD;
    private final DeviceType deviceType;
    private final int deviceNumber;

    @Override
    public String toString() {
        return String.format("AddressInfo{category=%s, isBD=%s, deviceType=%s, deviceNumber=%d}",
                category.getDescription(), isBD, deviceType.getDescription(), deviceNumber);
    }

    public static int createId(AddressInfo addressInfo) {
        return ((addressInfo.getCategory().getCode() & 0x3) << 9) |
                ((addressInfo.isBD() ? 1 : 0) << 8) |
                ((addressInfo.getDeviceType().getCode() & 0x7) << 5) |
                (addressInfo.getDeviceNumber() & 0x1F);
    }

    public static AddressInfo parseAddress(int canId) {
        MessageCategory messageCategory = MessageCategory.fromCode((byte) ((canId >> 9) & 0x3));
        boolean isBD = ((canId >> 8) & 0x1) == 1;
        DeviceType deviceType = DeviceType.fromCode((canId >> 5) & 0x7);
        int deviceNumber = canId & 0x1F;
        return new AddressInfo(messageCategory, isBD, deviceType, deviceNumber);
    }
}
