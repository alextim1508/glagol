package com.alextim.glagol.service;

import com.alextim.glagol.service.MetrologyMeasService.MetrologyMeasurement;
import com.alextim.glagol.service.message.MeasurementMessages.MeasurementDoseRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class MetrologyMeasServiceTest {

    private MetrologyMeasService service;

    @BeforeEach
    void setUp() {
        service = new MetrologyMeasService();
    }

    @Test
    void testRun() {
        service.run(3, 2, 10.0f);

        assertTrue(service.isRun());
        assertEquals(0, service.count);
        assertEquals(3, service.cycleAmount);
        assertEquals(2, service.measAmount);
        assertEquals(10.0f, service.realMeasData, 0.01f);
        assertEquals(0, service.aveMeasDataList.size());
    }

    @Test
    void testAddMeasToMetrology_NotRunning() {
        service.run = false;

        MeasurementDoseRate msg = createMeasurementDoseRate(1000000.0f); // 1.0 Зв/ч
        Optional<MetrologyMeasurement> result = service.addMeasToMetrology(msg);

        assertTrue(result.isEmpty());
        assertEquals(0, service.count);
    }

    @Test
    void testAddMeasToMetrology_SingleMeasurement() {
        service.run(1, 1, 5.0f);

        MeasurementDoseRate msg = createMeasurementDoseRate(5000000.0f); // 5.0 Зв/ч
        Optional<MetrologyMeasurement> result = service.addMeasToMetrology(msg);

        assertTrue(result.isPresent());
        MetrologyMeasurement meas = result.get();

        assertEquals(1, meas.cycle); // Первый цикл
        assertEquals(5.0f, meas.measData, 0.01f); // Средняя доза после 1 измерения
        assertEquals(5.0f, meas.aveMeasData, 0.01f); // Среднее по итогам цикла после 1 измерения в 1 цикле
        assertEquals("Зв/ч", meas.unit);
        assertEquals(1.0f, meas.progress, 0.01f); // 1/1
        assertEquals(0.0f, meas.error, 0.01f); // (|5.0 - 5.0| / 5.0) * 100

        assertEquals(1, service.count);
        assertFalse(service.isRun()); // Должен остановиться после достижения общего количества (1 * 1)
        assertEquals(1, service.aveMeasDataList.size()); // Добавлено среднее значение первого цикла
        assertEquals(5.0f, service.aveMeasDataList.get(0), 0.01f);
    }

    @Test
    void testAddMeasToMetrology_MultipleMeasurementsSingleCycle() {
        service.run(1, 3, 15.0f); // 1 цикл, 3 измерения на цикл, эталон = 15.0

        // Отправляем 3 измерения, каждое 10.0 Зв/ч
        MeasurementDoseRate msg1 = createMeasurementDoseRate(10000000.0f); // 10.0 Зв/ч
        Optional<MetrologyMeasurement> result1 = service.addMeasToMetrology(msg1);
        assertTrue(result1.isPresent());
        assertEquals(1, result1.get().cycle);
        // После 1-го: среднее = 10.0
        assertEquals(10.0f, result1.get().measData, 0.01f); // Средняя доза
        assertEquals(0.0f, result1.get().aveMeasData, 0.01f); // Среднее по циклам на 1-м измерении = 0, так как список пуст


        MeasurementDoseRate msg2 = createMeasurementDoseRate(10000000.0f); // 10.0 Зв/ч
        Optional<MetrologyMeasurement> result2 = service.addMeasToMetrology(msg2);
        assertTrue(result2.isPresent());
        assertEquals(1, result2.get().cycle);
        assertEquals(10.0f, result2.get().measData, 0.01f); // Средняя доза после 2-го измерения по-прежнему 10.0
        assertEquals(0.0f, result2.get().aveMeasData, 0.01f); // Среднее по циклам на 2-м измерении = 0

        MeasurementDoseRate msg3 = createMeasurementDoseRate(10000000.0f); // 10.0 Зв/ч
        Optional<MetrologyMeasurement> result3 = service.addMeasToMetrology(msg3);
        assertTrue(result3.isPresent());
        MetrologyMeasurement meas3 = result3.get();

        assertEquals(1, meas3.cycle);
        assertEquals(10.0f, meas3.measData, 0.01f); // Средняя доза после 3 измерений в цикле по-прежнему 10.0
        assertEquals(10.0f, meas3.aveMeasData, 0.01f); // Среднее по циклам после завершения первого цикла (список имеет одно значение: 10.0)
        assertEquals(1.0f, meas3.progress, 0.01f); // 3/3 = 1.0 или 100%
        assertEquals((Math.abs(15.0f - 10.0f) / 15.0f) * 100, meas3.error, 0.01f); // 33.33%

        assertEquals(3, service.count);
        assertFalse(service.isRun()); // Должен остановиться после завершения единственного цикла (1 * 3 измерения)
        assertEquals(1, service.aveMeasDataList.size()); // Добавлено одно среднее значение цикла
        assertEquals(10.0f, service.aveMeasDataList.get(0), 0.01f);
    }

    @Test
    void testAddMeasToMetrology_MultipleCycles() {
        service.run(2, 2, 10.0f); // 2 цикла, 2 измерения на цикл, эталон = 10.0

        // Цикл 1, Изм 1: 9.0 Зв/ч
        Optional<MetrologyMeasurement> r1 = service.addMeasToMetrology(createMeasurementDoseRate(9000000.0f));
        assertTrue(r1.isPresent());
        assertEquals(1, r1.get().cycle);
        assertEquals(9.0f, r1.get().measData, 0.01f); // Средняя доза после 1-го измерения
        assertEquals(0.0f, r1.get().aveMeasData, 0.01f); // Среднее по циклам = 0, так как список пуст
        assertEquals(0.25f, r1.get().progress, 0.01f); // 1/4

        // Цикл 1, Изм 2: 11.0 Зв/ч -> Среднее для цикла 1 = (9+11)/2 = 10.0
        Optional<MetrologyMeasurement> r2 = service.addMeasToMetrology(createMeasurementDoseRate(11000000.0f));
        assertTrue(r2.isPresent());
        assertEquals(1, r2.get().cycle);
        // averageDoseRate после 2-го измерения в цикле 1: (9.0 * 1 + 11.0) / 2 = 10.0
        assertEquals(10.0f, r2.get().measData, 0.01f); // Средняя доза после 2-го измерения = 10.0
        // В этот момент список получает [10.0] (среднее цикла 1), затем вычисляется среднее по циклам: 10.0 / 1 = 10.0
        assertEquals(10.0f, r2.get().aveMeasData, 0.01f); // Среднее по циклам после завершения цикла 1
        assertEquals(0.50f, r2.get().progress, 0.01f); // 2/4
        // Ошибка основана на среднем по циклам (10.0) против эталона (10.0) = 0%
        assertEquals(0.0f, r2.get().error, 0.01f);

        // Цикл 2, Изм 1: 8.0 Зв/ч
        Optional<MetrologyMeasurement> r3 = service.addMeasToMetrology(createMeasurementDoseRate(8000000.0f));
        assertTrue(r3.isPresent());
        assertEquals(2, r3.get().cycle);
        // averageDoseRate после 3-го измерения: (10.0 * 2 + 8.0) / 3 = 28/3 = 9.333
        assertEquals(28.0f / 3, r3.get().measData, 0.01f); // Общее среднее после 3 измерений
        assertEquals(10.0f, r3.get().aveMeasData, 0.01f); // Среднее из цикла 1
        assertEquals(0.75f, r3.get().progress, 0.01f); // 3/4

        // Цикл 2, Изм 2: 12.0 Зв/ч -> Среднее для цикла 2 = (8+12)/2 = 10.0
        Optional<MetrologyMeasurement> r4 = service.addMeasToMetrology(createMeasurementDoseRate(12000000.0f));
        assertTrue(r4.isPresent());
        MetrologyMeasurement meas4 = r4.get();
        assertEquals(2, meas4.cycle);
        // averageDoseRate после 4-го измерения: (28.0/3 * 3 + 12.0) / 4 = (28 + 12) / 4 = 40/4 = 10.0
        assertEquals(10.0f, meas4.measData, 0.01f); // Общее среднее после 4 измерений = 10.0
        // (count+1)%measAmount = 4%2 = 0 -> ДА, добавляем averageDoseRate (10.0) в список -> список=[10.0, 10.0]
        // calcAveTotalMeasData -> сумма=10+10=20, размер=2 -> среднее = 20/2 = 10.0
        assertEquals(10.0f, meas4.aveMeasData, 0.01f); // Среднее арифметическое средних циклов (10+10)/2
        assertEquals(1.0f, meas4.progress, 0.01f); // 4/4
        // Ошибка основана на среднем по циклам (10.0) против эталона (10.0) = 0%
        assertEquals(0.0f, meas4.error, 0.01f);

        assertEquals(4, service.count);
        assertFalse(service.isRun()); // Должен остановиться после завершения 2 циклов (2 * 2 измерения)
        assertEquals(2, service.aveMeasDataList.size()); // Добавлены два средних значения циклов
        assertEquals(10.0f, service.aveMeasDataList.get(0), 0.01f); // Среднее цикла 1
        assertEquals(10.0f, service.aveMeasDataList.get(1), 0.01f); // Среднее цикла 2
    }

    private MeasurementDoseRate createMeasurementDoseRate(float rawValue) {
        byte[] floatBytes = ByteBuffer.allocate(4).order(java.nio.ByteOrder.LITTLE_ENDIAN).putFloat(rawValue).array();
        byte[] fullData = new byte[8];
        System.arraycopy(floatBytes, 0, fullData, 0, 4);

        com.alextim.glagol.client.SomeMessage dummyMsg = new SomeMessageStub(0x123, fullData, System.currentTimeMillis());
        return new MeasurementDoseRate(dummyMsg);
    }

    private static class SomeMessageStub extends com.alextim.glagol.service.message.MeasurementMessages.MeasEvent {
        public SomeMessageStub(int id, byte[] data, long time) {
            super(id, data, time, null);
        }
    }
}