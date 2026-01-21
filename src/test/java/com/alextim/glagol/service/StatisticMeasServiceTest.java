package com.alextim.glagol.service;

import com.alextim.glagol.client.SomeMessage;
import com.alextim.glagol.service.StatisticMeasService.StatisticMeasurement;
import com.alextim.glagol.service.message.MeasurementMessages.MeasEvent;
import com.alextim.glagol.service.message.MeasurementMessages.MeasurementCounts;
import com.alextim.glagol.service.message.MeasurementMessages.MeasurementDoseRate;
import com.alextim.glagol.service.message.MeasurementMessages.MeasurementHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class StatisticMeasServiceTest {

    private StatisticMeasService service;

    @BeforeEach
    void setUp() {
        service = new StatisticMeasService();
    }

    @Test
    void testRun() {
        service.run(10L);

        assertTrue(service.isRun());
        assertEquals(0, service.count);
        assertEquals(10L, service.measAmount);

        assertEquals(0, service.averageDoseRate, 0.0f);
        for (int i = 0; i < 5; i++) {
            assertEquals(0, service.averageCounts[i], 0.0f);
            assertEquals(0, service.accumulatedCounts[i]);
        }
    }

    @Test
    void testClear() {
        service.run(5L);

        service.averageCounts[0] = 100.0f;
        service.accumulatedCounts[0] = 200L;
        service.averageDoseRate = 50.0f;
        service.accumulatedDoseRate = 10.0f;

        service.clear();

        for (int i = 0; i < 5; i++) {
            assertEquals(0, service.averageCounts[i], 0.0f);
            assertEquals(0, service.accumulatedCounts[i]);
        }
        assertEquals(0, service.averageDoseRate, 0.0f);
        assertEquals(0, service.accumulatedDoseRate, 0.0f);
    }

    @Test
    void testAddMeasToStatistic_NotRunning() {
        service.run = false;

        Optional<StatisticMeasurement> result = service.addMeasToStatistic(null);

        assertTrue(result.isEmpty());
        assertEquals(0, service.count);
    }

    @Test
    void testAddMeasToStatistic_CompleteGroup() {
        service.run(1L);

        // Подготовка данных для заголовка измерения
        byte[] headerData = new byte[8];
        headerData[0] = 1; // workOption
        headerData[1] = 10; // currentRange
        headerData[2] = (byte) (100 & 0xFF); headerData[3] = (byte) ((100 >> 8) & 0xFF); // accumTimeDeciSec = 100
        headerData[4] = 2; // bytesPerCounter
        headerData[5] = 4; // counterCount

        SomeMessage headerMsg = new SomeMessageStub(0x123, headerData, System.currentTimeMillis());
        MeasurementHeader header = new MeasurementHeader(headerMsg);

        // Подготовка данных для счётчиков
        byte[] countsData = new byte[8];
        // count0 = 10 -> [10, 0]
        countsData[0] = (byte) (10 & 0xFF); countsData[1] = (byte) ((10 >> 8) & 0xFF);
        // count1 = 20 -> [20, 0]
        countsData[2] = (byte) (20 & 0xFF); countsData[3] = (byte) ((20 >> 8) & 0xFF);
        // count2 = 30 -> [30, 0]
        countsData[4] = (byte) (30 & 0xFF); countsData[5] = (byte) ((30 >> 8) & 0xFF);
        // count3 = 40 -> [40, 0]
        countsData[6] = (byte) (40 & 0xFF); countsData[7] = (byte) ((40 >> 8) & 0xFF);

        SomeMessage countsMsg = new SomeMessageStub(0x124, countsData, System.currentTimeMillis());
        MeasurementCounts counts = new MeasurementCounts(countsMsg);

        // Подготовка данных для мощности дозы
        float rawDoseRate = 12345678.0f; // 12.345678 Зв/ч -> мкЗв/ч
        byte[] doseRateData = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(rawDoseRate).array();
        byte[] fullDoseRateData = new byte[8];
        System.arraycopy(doseRateData, 0, fullDoseRateData, 0, 4);
        SomeMessage doseRateMsg = new SomeMessageStub(0x125, fullDoseRateData, System.currentTimeMillis());
        MeasurementDoseRate doseRate = new MeasurementDoseRate(doseRateMsg);

        // Добавление сообщений и проверка
        Optional<StatisticMeasurement> result = service.addMeasToStatistic(header);
        assertTrue(result.isEmpty()); // Первое сообщение, не хватает данных
        assertEquals(0, service.count); // Счётчик не увеличивается, пока группа не обработана

        result = service.addMeasToStatistic(counts);
        assertTrue(result.isEmpty()); // Второе сообщение, всё ещё не хватает
        assertEquals(0, service.count); // Счётчик не увеличивается, пока группа не обработана

        result = service.addMeasToStatistic(doseRate);

        assertTrue(result.isPresent());
        StatisticMeasurement stat = result.get();

        // Проверка полей результата
        assertEquals(10, stat.currentRange);
        assertEquals(4, stat.counterCount);
        assertArrayEquals(new int[]{10, 20, 30, 40, 100}, stat.currentCounts);
        assertArrayEquals(new long[]{10, 20, 30, 40, 100}, stat.accumulatedCounts);

        assertEquals(10.0f, stat.averageCounts[0], 0.01f);
        assertEquals(20.0f, stat.averageCounts[1], 0.01f);
        assertEquals(30.0f, stat.averageCounts[2], 0.01f);
        assertEquals(40.0f, stat.averageCounts[3], 0.01f);
        assertEquals(100.0f, stat.averageCounts[4], 0.01f);
        assertEquals(12.345678f, stat.currentDoseRate, 0.000001f);
        assertEquals(12.345678f, stat.averageDoseRate, 0.000001f);
        assertEquals(12.345678f / 3600, stat.accumulatedDoseRateInTime, 0.000001f);
        assertEquals(1.0f, stat.progress, 0.01f);
        assertEquals(1, stat.accumulatedInterval);

        assertEquals(1, service.count); // Счётчик увеличивается после обработки полной группы
    }

    @Test
    void testAddMeasToStatistic_AverageCalculation() {
        service.run(2L);

        // Подготовка данных для первого измерения
        byte[] headerData1 = new byte[8];
        headerData1[0] = 1; headerData1[1] = 10;
        headerData1[2] = (byte) (100 & 0xFF); headerData1[3] = (byte) ((100 >> 8) & 0xFF);
        headerData1[4] = 2;
        headerData1[5] = 4;
        SomeMessage headerMsg1 = new SomeMessageStub(0x123, headerData1, System.currentTimeMillis());
        MeasurementHeader header1 = new MeasurementHeader(headerMsg1);

        // Данные счётчиков для первого измерения: [10, 20, 30, 40]
        byte[] countsData1 = new byte[8];
        countsData1[0] = (byte) (10 & 0xFF); countsData1[1] = (byte) ((10 >> 8) & 0xFF);
        countsData1[2] = (byte) (20 & 0xFF); countsData1[3] = (byte) ((20 >> 8) & 0xFF);
        countsData1[4] = (byte) (30 & 0xFF); countsData1[5] = (byte) ((30 >> 8) & 0xFF);
        countsData1[6] = (byte) (40 & 0xFF); countsData1[7] = (byte) ((40 >> 8) & 0xFF);
        SomeMessage countsMsg1 = new SomeMessageStub(0x124, countsData1, System.currentTimeMillis());
        MeasurementCounts counts1 = new MeasurementCounts(countsMsg1);

        // Данные мощности дозы для первого измерения: 10.0 Зв/ч = 10000000 мкЗв/ч
        float rawDoseRate1 = 10000000.0f;
        byte[] doseRateData1 = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(rawDoseRate1).array();
        byte[] fullDoseRateData1 = new byte[8];
        System.arraycopy(doseRateData1, 0, fullDoseRateData1, 0, 4);
        SomeMessage doseRateMsg1 = new SomeMessageStub(0x125, fullDoseRateData1, System.currentTimeMillis());
        MeasurementDoseRate doseRate1 = new MeasurementDoseRate(doseRateMsg1);

        // Обработка первой группы
        Optional<StatisticMeasurement> result1 = service.addMeasToStatistic(header1);
        service.addMeasToStatistic(counts1);
        result1 = service.addMeasToStatistic(doseRate1);
        assertTrue(result1.isPresent());
        assertEquals(10.0f, result1.get().currentDoseRate, 0.01f); // 10.0 Зв/ч
        assertEquals(10.0f, result1.get().averageDoseRate, 0.01f); // Среднее для первого измерения равно ему самому

        // Подготовка данных для второго измерения
        byte[] headerData2 = new byte[8];
        headerData2[0] = 2;
        headerData2[1] = 11;
        headerData2[2] = (byte) (100 & 0xFF); headerData2[3] = (byte) ((100 >> 8) & 0xFF);
        headerData2[4] = 2;
        headerData2[5] = 4;
        SomeMessage headerMsg2 = new SomeMessageStub(0x126, headerData2, System.currentTimeMillis());
        MeasurementHeader header2 = new MeasurementHeader(headerMsg2);

        // Данные счётчиков для второго измерения: [15, 25, 35, 45]
        byte[] countsData2 = new byte[8];
        countsData2[0] = (byte) (15 & 0xFF); countsData2[1] = (byte) ((15 >> 8) & 0xFF);
        countsData2[2] = (byte) (25 & 0xFF); countsData2[3] = (byte) ((25 >> 8) & 0xFF);
        countsData2[4] = (byte) (35 & 0xFF); countsData2[5] = (byte) ((35 >> 8) & 0xFF);
        countsData2[6] = (byte) (45 & 0xFF); countsData2[7] = (byte) ((45 >> 8) & 0xFF);
        SomeMessage countsMsg2 = new SomeMessageStub(0x127, countsData2, System.currentTimeMillis());
        MeasurementCounts counts2 = new MeasurementCounts(countsMsg2);

        // Данные мощности дозы для второго измерения: 20.0 Зв/ч = 20000000 мкЗв/ч
        float rawDoseRate2 = 20000000.0f;
        byte[] doseRateData2 = java.nio.ByteBuffer.allocate(4).order(java.nio.ByteOrder.LITTLE_ENDIAN).putFloat(rawDoseRate2).array();
        byte[] fullDoseRateData2 = new byte[8];
        System.arraycopy(doseRateData2, 0, fullDoseRateData2, 0, 4);
        SomeMessage doseRateMsg2 = new SomeMessageStub(0x128, fullDoseRateData2, System.currentTimeMillis());
        MeasurementDoseRate doseRate2 = new MeasurementDoseRate(doseRateMsg2);

        // Обработка второй группы
        Optional<StatisticMeasurement> result2 = service.addMeasToStatistic(header2);
        service.addMeasToStatistic(counts2);
        result2 = service.addMeasToStatistic(doseRate2);
        assertTrue(result2.isPresent());

        // Средняя мощность дозы для второго измерения должна быть (10.0 + 20.0) / 2 = 15.0
        assertEquals(20.0f, result2.get().currentDoseRate, 0.01f); // 20.0 Зв/ч
        assertEquals(15.0f, result2.get().averageDoseRate, 0.01f); // Среднее по двум измерениям

        // Проверка вычисления средних значений счётчиков
        // Среднее для первого измерения было [10, 20, 30, 40]
        // Новое среднее для счётчика 0: (10 * (2-1) + 15) / 2 = 12.5
        assertEquals(12.5f, result2.get().averageCounts[0], 0.01f);
        assertEquals(22.5f, result2.get().averageCounts[1], 0.01f);
        assertEquals(32.5f, result2.get().averageCounts[2], 0.01f);
        assertEquals(42.5f, result2.get().averageCounts[3], 0.01f);
        assertEquals(110.0f, result2.get().averageCounts[4], 0.01f); // 12.5 + 22.5 + 32.5 + 42.5

        assertEquals(2, service.count);
    }

    private static class SomeMessageStub extends MeasEvent {
        public SomeMessageStub(int id, byte[] data, long time) {
            super(id, data, time, null);
        }
    }
}