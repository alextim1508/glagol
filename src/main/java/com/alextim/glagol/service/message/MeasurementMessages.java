package com.alextim.glagol.service.message;

import com.alextim.glagol.client.SomeMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MeasurementMessages {

    @Slf4j
    @Getter
    public static class MeasurementHeader extends MeasEvent {

        public final int workOption;
        public final int currentRange;
        public final int accumTimeDeciSec;
        public final int bytesPerCounter;
        public final int counterCount;

        public MeasurementHeader(SomeMessage baseMsg) {
            super(baseMsg.id, baseMsg.data, baseMsg.time);

            this.workOption = baseMsg.data[0] & 0xFF;
            log.debug("Work option: {}", workOption);

            this.currentRange = baseMsg.data[1] & 0xFF;
            log.debug("Current range: {}", currentRange);

            this.accumTimeDeciSec = (baseMsg.data[2] & 0xFF) | ((baseMsg.data[3] & 0xFF) << 8);
            log.debug("Accumulation time DeciSec: {}", accumTimeDeciSec);

            this.bytesPerCounter = baseMsg.data[4] & 0xFF;

            this.counterCount = baseMsg.data[5] & 0xFF;
            log.debug("Counter count: {}", counterCount);
        }

        @Override
        public String toString() {
            return String.format("Заголовок измерительного сообщения. Диапазон=%d, Время накопления=%d, Количество счетчиков=%d",
                    currentRange, accumTimeDeciSec, counterCount);
        }
    }

    @Slf4j
    @Getter
    public static class MeasurementCounts extends MeasEvent {

        public final int[] counts;

        public MeasurementCounts(SomeMessage baseMsg) {
            super(baseMsg.id, baseMsg.data, baseMsg.time);

            this.counts = new int[4];
            for (int i = 0; i < 4; i++) {
                counts[i] = (data[2 * i] & 0xFF) | ((data[2 * i + 1] & 0xFF) << 8);
            }
            log.debug("Counts: {}", counts);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Измерительное сообщение. Счета=[");
            for (int i = 0; i < counts.length; i++) {
                sb.append(counts[i]);
                if (i < counts.length - 1) sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    @Slf4j
    @Getter
    public static class MeasurementDoseRate extends MeasEvent {

        public final float doseRate;

        public MeasurementDoseRate(SomeMessage baseMsg) {
            super(baseMsg.id, baseMsg.data, baseMsg.time);

            byte[] valueBytes = new byte[4];
            System.arraycopy(baseMsg.data, 0, valueBytes, 0, 4);

            ByteBuffer bb = ByteBuffer.wrap(valueBytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            this.doseRate = bb.getFloat();

            log.debug("Dose rate: {}", doseRate);
        }

        @Override
        public String toString() {
            return String.format("Измерительное сообщение. Мощность дозы=%.6f", doseRate);
        }
    }

    public static class MeasEvent extends SomeMessage {
        public MeasEvent(int id, byte[] data, long time) {
            super(id, data, time);
        }
    }
}
