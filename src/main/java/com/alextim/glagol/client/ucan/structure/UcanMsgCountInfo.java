package com.alextim.glagol.client.ucan.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

@Structure.FieldOrder({
        "m_wSentMsgCount",
        "m_wRecvdMsgCount"
})
public class UcanMsgCountInfo extends Structure {
    public short m_wSentMsgCount;
    public short m_wRecvdMsgCount;

    public UcanMsgCountInfo() {
        super();
    }

    public UcanMsgCountInfo(Pointer p) {
        super(p);
    }
}

