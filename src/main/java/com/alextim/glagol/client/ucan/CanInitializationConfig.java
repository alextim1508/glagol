package com.alextim.glagol.client.ucan;

import com.alextim.glagol.client.ucan.structure.UcanInitCanParam;

import static com.alextim.glagol.client.ucan.constant.CanMode.UCAN_MODE_NORMAL;

public class CanInitializationConfig {

    public static UcanInitCanParam createInitParam(short BTR) {
        UcanInitCanParam initParam = new UcanInitCanParam();
        initParam.m_dwSize = initParam.size();
        initParam.m_bMode = UCAN_MODE_NORMAL.getValue();
        initParam.m_bBTR0 = (byte) ((BTR >> 8) & 0xFF);     // HIBYTE
        initParam.m_bBTR1 = (byte) (BTR & 0xFF);            // LOBYTE
        initParam.m_bOCR = (byte) 0x1A;                     // standard output
        initParam.m_dwAMR = 0xffffffff;                     // USBCAN_AMR_ALL
        initParam.m_dwACR = 0x00000000;                     // USBCAN_ACR_ALL
        initParam.m_dwBaudrate = 0x00000000;                // USBCAN_BAUDEX_USE_BTR01
        initParam.m_wNrOfRxBufferEntries = (short) 4096;    // USBCAN_DEFAULT_BUFFER_ENTRIES
        initParam.m_wNrOfTxBufferEntries = (short) 4096;    // USBCAN_DEFAULT_BUFFER_ENTRIES
        return initParam;
    }
}
