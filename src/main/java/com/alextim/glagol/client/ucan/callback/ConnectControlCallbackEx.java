package com.alextim.glagol.client.ucan.callback;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface ConnectControlCallbackEx extends Callback {
    void callback(int event, int param, Pointer pArg);
}