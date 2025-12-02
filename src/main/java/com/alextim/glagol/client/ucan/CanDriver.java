package com.alextim.glagol.client.ucan;

import com.alextim.glagol.client.ucan.callback.ConnectControlCallbackEx;
import com.alextim.glagol.client.ucan.callback.EventCallbackEx;
import com.alextim.glagol.client.ucan.constant.UsbCanError;
import com.alextim.glagol.client.ucan.structure.CanMsg;
import com.alextim.glagol.client.ucan.structure.UcanInitCanParam;
import com.alextim.glagol.client.ucan.structure.UcanMsgCountInfo;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import static com.alextim.glagol.client.ucan.UsbCanLibrary.INSTANCE;

public class CanDriver {

    public static UsbCanError initHwConnectControl(ConnectControlCallbackEx callback, Object callbackArg) {
        Pointer pArg = callbackArg != null ? Pointer.createConstant(System.identityHashCode(callbackArg)) : null;
        NativeLong result = INSTANCE.UcanInitHwConnectControlEx(callback, pArg);
        return UsbCanError.fromValue(result.intValue());
    }

    public static UsbCanError deinitHwConnectControl() {
        NativeLong result = INSTANCE.UcanDeinitHwConnectControl();
        return UsbCanError.fromValue(result.intValue());
    }

    public static UsbCanError initHardware(IntByReference handle, byte serialNumber, EventCallbackEx callback, Object callbackArg) {
        Pointer pArg = callbackArg != null ? Pointer.createConstant(System.identityHashCode(callbackArg)) : null;
        NativeLong result = INSTANCE.UcanInitHardwareEx(handle, serialNumber, callback, pArg);
        return UsbCanError.fromValue(result.intValue());
    }

    public static UsbCanError deinitHardware(NativeLong handle) {
        NativeLong result = INSTANCE.UcanDeinitHardware(handle);
        return UsbCanError.fromValue(result.intValue());
    }

    public static UsbCanError initCan(NativeLong handle, UcanInitCanParam param) {
        NativeLong result = INSTANCE.UcanInitCanEx(handle, param);
        return UsbCanError.fromValue(result.intValue());
    }

    public static UsbCanError deinitCan(NativeLong handle) {
        NativeLong result = INSTANCE.UcanDeinitCan(handle);
        return UsbCanError.fromValue(result.intValue());
    }

    public static UsbCanError resetCan(NativeLong handle) {
        NativeLong result = INSTANCE.UcanResetCan(handle);
        return UsbCanError.fromValue(result.intValue());
    }

    public static UsbCanError getMsgCountInfo(NativeLong handle, UcanMsgCountInfo msgCountInfo) {
        NativeLong result = INSTANCE.UcanGetMsgCountInfo(handle, msgCountInfo);
        return UsbCanError.fromValue(result.intValue());
    }

    public static UsbCanError readCanMsg(NativeLong handle, CanMsg canMsg) {
        NativeLong result = INSTANCE.UcanReadCanMsg(handle, canMsg);
        return UsbCanError.fromValue(result.intValue());
    }

    public static UsbCanError writeCanMsg(NativeLong handle, CanMsg canMsg) {
        NativeLong result = INSTANCE.UcanWriteCanMsg(handle, canMsg);
        return UsbCanError.fromValue(result.intValue());
    }
}
