package com.alextim.glagol.client;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SomeMessage {
    public int id;
    public byte[] data;
    public long time;

    @Override
    public String toString() {
        return String.format("SomeMessage{id=0x%04X, data=[%s], time=%d}", id, formatDataAsHex(data), time);
    }

    public static String formatDataAsHex(byte[] data) {
        StringBuilder dataHex = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            dataHex.append(String.format("%02X", data[i] & 0xFF));
            if (i < data.length - 1) {
                dataHex.append(" ");
            }
        }
        return dataHex.toString();
    }
}