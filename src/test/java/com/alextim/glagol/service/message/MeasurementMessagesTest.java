package com.alextim.glagol.service.message;

import com.alextim.glagol.client.SomeMessage;
import com.alextim.glagol.service.message.MeasurementMessages.MeasurementCounts;
import com.alextim.glagol.service.message.MeasurementMessages.MeasurementDoseRate;
import com.alextim.glagol.service.message.MeasurementMessages.MeasurementHeader;
import com.alextim.glagol.service.protocol.MeasurementMessageType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MeasurementMessagesTest {

    private static final int TEST_ID = 0x123;
    private static final long TEST_TIME = System.currentTimeMillis();

    @Test
    public void testMeasurementHeader() {
        byte[] testData = new byte[8];
        testData[0] = (byte) 0x01; // workOption
        testData[1] = (byte) 0x02; // currentRange
        testData[2] = (byte) 0x34; // accumTimeDeciSec LSB
        testData[3] = (byte) 0x12; // accumTimeDeciSec MSB 4660 -> 0x1234
        testData[4] = (byte) 0x02; // bytesPerCounter
        testData[5] = (byte) 0x04; // counterCount
        // testData[6], testData[7] - не используются

        SomeMessage baseMsg = new SomeMessage(TEST_ID, testData, TEST_TIME);

        MeasurementHeader header = new MeasurementHeader(baseMsg);

        assertEquals(1, header.workOption);
        assertEquals(2, header.currentRange);
        assertEquals(4660, header.accumTimeDeciSec);
        assertEquals(2, header.bytesPerCounter);
        assertEquals(4, header.counterCount);
        assertEquals(MeasurementMessageType.HEADER, header.type);
        assertEquals(TEST_ID, header.id);
        assertEquals(TEST_TIME, header.time);
    }

    @Test
    public void testMeasurementCounts() {
        byte[] testData = new byte[8];
        // Счетчик 0: 1000 -> 0x03E8
        testData[0] = (byte) 0xE8;
        testData[1] = (byte) 0x03;
        // Счетчик 1: 2000 -> 0x07D0
        testData[2] = (byte) 0xD0;
        testData[3] = (byte) 0x07;
        // Счетчик 2: 3000 -> 0x0BB8
        testData[4] = (byte) 0xB8;
        testData[5] = (byte) 0x0B;
        // Счетчик 3: 4000 -> 0x0FA0
        testData[6] = (byte) 0xA0;
        testData[7] = (byte) 0x0F;

        SomeMessage baseMsg = new SomeMessage(TEST_ID, testData, TEST_TIME);

        MeasurementCounts counts = new MeasurementCounts(baseMsg);

        assertArrayEquals(new int[]{1000, 2000, 3000, 4000}, counts.counts);
        assertEquals(MeasurementMessageType.COUNTS, counts.type);
        assertEquals(TEST_ID, counts.id);
        assertEquals(TEST_TIME, counts.time);
    }

    @Test
    public void testMeasurementDoseRate() {
        float expectedDoseRate = 12.34f;
        byte[] floatBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(expectedDoseRate).array();

        byte[] testData = new byte[8];
        System.arraycopy(floatBytes, 0, testData, 0, 4);


        SomeMessage baseMsg = new SomeMessage(TEST_ID, testData, TEST_TIME);

        MeasurementDoseRate doseRateMsg = new MeasurementDoseRate(baseMsg);

        assertEquals(expectedDoseRate, doseRateMsg.doseRate, 0.0001f);
        assertEquals(MeasurementMessageType.DOSE_RATE, doseRateMsg.type);
        assertEquals(TEST_ID, doseRateMsg.id);
        assertEquals(TEST_TIME, doseRateMsg.time);
    }

    @Test
    public void testMeasurementHeaderToString() {
        byte[] testData = new byte[8];
        testData[0] = (byte) 0x01;
        testData[1] = (byte) 0x02;
        testData[2] = (byte) 0x34;
        testData[3] = (byte) 0x12;
        testData[4] = (byte) 0x02;
        testData[5] = (byte) 0x04;

        SomeMessage baseMsg = new SomeMessage(TEST_ID, testData, TEST_TIME);
        MeasurementHeader header = new MeasurementHeader(baseMsg);

        String expectedString = "Диапазон: 2, Время накопления: 4660, Количество счетчиков: 4";
        assertEquals(expectedString, header.toString());
    }

    @Test
    public void testMeasurementCountsToString() {
        byte[] testData = new byte[8];
        testData[0] = (byte) 0xE8; testData[1] = (byte) 0x03; // 1000
        testData[2] = (byte) 0xD0; testData[3] = (byte) 0x07; // 2000
        testData[4] = (byte) 0xB8; testData[5] = (byte) 0x0B; // 3000
        testData[6] = (byte) 0xA0; testData[7] = (byte) 0x0F; // 4000

        SomeMessage baseMsg = new SomeMessage(TEST_ID, testData, TEST_TIME);
        MeasurementCounts counts = new MeasurementCounts(baseMsg);

        String expectedString = "Счета: [1000, 2000, 3000, 4000]";
        assertEquals(expectedString, counts.toString());
    }

    @Test
    public void testMeasurementDoseRateToString() {
        float doseRateValue = 12.34f;
        byte[] floatBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(doseRateValue).array();

        byte[] testData = new byte[8];
        System.arraycopy(floatBytes, 0, testData, 0, 4);

        SomeMessage baseMsg = new SomeMessage(TEST_ID, testData, TEST_TIME);
        MeasurementDoseRate doseRateMsg = new MeasurementDoseRate(baseMsg);

        String expectedString = String.format("Мощность дозы: %f", doseRateValue);
        assertEquals(expectedString, doseRateMsg.toString());
    }
}