package com.alextim.glagol.client.ucan.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UsbCanError {
    USBCAN_SUCCESSFUL(0x00, "Success"),
    USBCAN_ERR_RESOURCE(0x01, "Resource error"),
    USBCAN_ERR_MAXMODULES(0x02, "Too many modules"),
    USBCAN_ERR_HWINUSE(0x03, "Hardware in use"),
    USBCAN_ERR_ILLVERSION(0x04, "Version mismatch"),
    USBCAN_ERR_ILLHW(0x05, "Hardware not found"),
    USBCAN_ERR_ILLHANDLE(0x06, "Invalid handle"),
    USBCAN_ERR_ILLPARAM(0x07, "Invalid parameter"),
    USBCAN_ERR_BUSY(0x08, "Busy"),
    USBCAN_ERR_TIMEOUT(0x09, "Timeout"),
    USBCAN_ERR_IOFAILED(0x0A, "I/O failed"),
    USBCAN_ERR_DLL_TXFULL(0x0B, "TX buffer full"),
    USBCAN_ERR_MAXINSTANCES(0x0C, "Too many instances"),
    USBCAN_ERR_CANNOTINIT(0x0D, "Cannot initialize"),
    USBCAN_ERR_DISCONNECT(0x0E, "Disconnected"),
    USBCAN_ERR_ILLCHANNEL(0x10, "Invalid channel"),
    USBCAN_ERR_ILLHWTYPE(0x12, "Invalid hardware type"),
    USBCAN_ERRCMD_NOTEQU(0x40, "Command mismatch"),
    USBCAN_ERRCMD_REGTST(0x41, "Register test failed"),
    USBCAN_ERRCMD_ILLCMD(0x42, "Invalid command"),
    USBCAN_ERRCMD_EEPROM(0x43, "EEPROM error"),
    USBCAN_ERRCMD_ILLBDR(0x47, "Invalid baud rate"),
    USBCAN_ERRCMD_NOTINIT(0x48, "Not initialized"),
    USBCAN_ERRCMD_ALREADYINIT(0x49, "Already initialized"),
    USBCAN_ERRCMD_ILLSUBCMD(0x4A, "Invalid sub-command"),
    USBCAN_ERRCMD_ILLIDX(0x4B, "Invalid index"),
    USBCAN_ERRCMD_RUNNING(0x4C, "Already running"),
    USBCAN_WARN_NODATA(0x80, "No data"),
    USBCAN_WARN_SYS_RXOVERRUN(0x81, "System RX overrun"),
    USBCAN_WARN_DLL_RXOVERRUN(0x82, "DLL RX overrun"),
    USBCAN_WARN_FW_TXOVERRUN(0x85, "FW TX overrun"),
    USBCAN_WARN_FW_RXOVERRUN(0x86, "FW RX overrun"),
    USBCAN_WARN_NULL_PTR(0x90, "Null pointer"),
    USBCAN_WARN_TXLIMIT(0x91, "TX limit");

    private final int value;

    private final String description;

    public static UsbCanError fromValue(int value) {
        for (UsbCanError error : UsbCanError.values()) {
            if (error.getValue() == value) return error;
        }
        throw new IllegalArgumentException("Unknown USB-CAN error code: " + value);
    }

    @Override
    public String toString() {
        return name() + "(value=0x" + Integer.toHexString(value) + ", description=" + description + ")";
    }

    public boolean isSuccess() { return this == USBCAN_SUCCESSFUL; }

    public boolean isWarning() { return value >= 0x80 && value < 0x100; }

    public boolean isError() { return value != 0x00 && value < 0x80; }
}