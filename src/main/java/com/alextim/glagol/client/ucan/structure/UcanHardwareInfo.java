package com.alextim.glagol.client.ucan.structure;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

@Structure.FieldOrder({
        "m_bDeviceNr",
        "m_UcanHandle",
        "m_dwReserved",
        "m_bBTR0",
        "m_bBTR1",
        "m_bOCR",
        "m_dwAMR",
        "m_dwACR",
        "m_bMode",
        "m_dwSerialNr"
})
public class UcanHardwareInfo extends Structure {
    public byte m_bDeviceNr;
    public NativeLong m_UcanHandle;
    public int m_dwReserved;
    public byte m_bBTR0;
    public byte m_bBTR1;
    public byte m_bOCR;
    public int m_dwAMR;
    public int m_dwACR;
    public byte m_bMode;
    public int m_dwSerialNr;

    public UcanHardwareInfo() {
        super();
    }

    public UcanHardwareInfo(Pointer p) {
        super(p);
    }
}
