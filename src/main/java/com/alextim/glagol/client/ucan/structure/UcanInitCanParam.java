package com.alextim.glagol.client.ucan.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

@Structure.FieldOrder({
        "m_dwSize",
        "m_bMode",
        "m_bBTR0",
        "m_bBTR1",
        "m_bOCR",
        "m_dwAMR",
        "m_dwACR",
        "m_dwBaudrate",
        "m_wNrOfRxBufferEntries",
        "m_wNrOfTxBufferEntries"
})
public class UcanInitCanParam extends Structure {
    public int m_dwSize;
    public byte m_bMode;
    public byte m_bBTR0;
    public byte m_bBTR1;
    public byte m_bOCR;
    public int m_dwAMR;
    public int m_dwACR;
    public int m_dwBaudrate;
    public short m_wNrOfRxBufferEntries;
    public short m_wNrOfTxBufferEntries;

    public UcanInitCanParam() {
        super();
    }

    public UcanInitCanParam(Pointer p) {
        super(p);
    }
}
