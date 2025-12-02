package com.alextim.glagol.client.ucan;

import com.alextim.glagol.client.ucan.callback.ConnectControlCallbackEx;
import com.alextim.glagol.client.ucan.callback.EventCallbackEx;
import com.alextim.glagol.client.ucan.structure.CanMsg;
import com.alextim.glagol.client.ucan.structure.UcanInitCanParam;
import com.alextim.glagol.client.ucan.structure.UcanMsgCountInfo;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;


public interface UsbCanLibrary extends Library {

    UsbCanLibrary INSTANCE = Native.load("USBCAN64.dll", UsbCanLibrary.class, W32APIOptions.DEFAULT_OPTIONS);

    NativeLong UcanInitHwConnectControlEx(ConnectControlCallbackEx pfnConnectControlEx_p, Pointer pCallbackArg_p);
    NativeLong UcanDeinitHwConnectControl();

    NativeLong UcanInitHardwareEx(IntByReference pUcanHandle_p, byte bDeviceNr_p, EventCallbackEx pfnEventCallbackEx_p, Pointer pCallbackArg_p);
    NativeLong UcanDeinitHardware(NativeLong UcanHandle_p);

    NativeLong UcanInitCanEx(NativeLong UcanHandle_p, UcanInitCanParam pInitCanParam_p);
    NativeLong UcanResetCan(NativeLong UcanHandle_p);
    NativeLong UcanDeinitCan(NativeLong UcanHandle_p);

    NativeLong UcanReadCanMsg(NativeLong UcanHandle_p, CanMsg pCanMsg_p);
    NativeLong UcanWriteCanMsg(NativeLong UcanHandle_p, CanMsg pCanMsg_p);
    NativeLong UcanGetMsgCountInfo(NativeLong UcanHandle_p, UcanMsgCountInfo pMsgCountInfo_p);
}
