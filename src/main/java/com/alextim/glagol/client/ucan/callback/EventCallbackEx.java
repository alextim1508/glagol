package com.alextim.glagol.client.ucan.callback;

import com.sun.jna.Callback;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public interface EventCallbackEx extends Callback {
    void callback(NativeLong UcanHandle_p, byte bEvent_p, byte bChannel_p, Pointer pArg_p);
}
