package com.alextim.glagol.client.ucan.structure;

import com.alextim.glagol.client.SomeMessage;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import static com.alextim.glagol.client.ucan.constant.CanFrameFormat.USBCAN_MSG_FF_STD;

@Structure.FieldOrder({
        "m_dwID",
        "m_bFF",
        "m_bDLC",
        "m_bData",
        "m_dwTime"
})
public class CanMsg extends Structure {
    public int m_dwID;
    public byte m_bFF;
    public byte m_bDLC;
    public byte[] m_bData = new byte[8];
    public int m_dwTime;

    public CanMsg() {
        super();
    }

    public CanMsg(Pointer p) {
        super(p);
    }

    public CanMsg(SomeMessage someMessage) {
        m_dwID = someMessage.id;
        for (int i = 0; i < 8 && i < someMessage.data.length; i++) {
            m_bData[i] = someMessage.data[i];
        }
        m_bDLC = (byte) someMessage.data.length;
        m_bFF = USBCAN_MSG_FF_STD.getValue();
    }

    @Override
    public String toString() {
        StringBuilder dataHex = new StringBuilder();
        for (int i = 0; i < (m_bDLC & 0xFF); i++) {
            dataHex.append(String.format("%02X", m_bData[i] & 0xFF));
            if (i < (m_bDLC & 0xFF) - 1) {
                dataHex.append(" ");
            }
        }
        return String.format("CanMsg{ID=0x%04X, FF=0x%02X, DLC=%d, Data=[%s], Time=%d}",
                m_dwID, m_bFF & 0xFF, m_bDLC & 0xFF, dataHex, m_dwTime);
    }
}
