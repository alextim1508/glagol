package com.alextim.glagol.service.message;

import com.alextim.glagol.client.SomeMessage;
import com.alextim.glagol.service.protocol.MeasurementMessageType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.alextim.glagol.service.protocol.MeasurementMessageType.*;

public class MeasurementMessages {

    @Slf4j
    public static class MeasurementHeader extends MeasEvent {

        public final int workOption;
        public final int currentRange;
        public final int accumTimeDeciSec;
        public final int bytesPerCounter;
        public final int counterCount;

        public MeasurementHeader(SomeMessage baseMsg) {
            super(baseMsg.id, baseMsg.data, baseMsg.time, HEADER);

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
            return String.format("%s. Диапазон=%d, Время накопления=%d, Количество счетчиков=%d",
                    HEADER.getDescription(), currentRange, accumTimeDeciSec, counterCount);
        }
    }

    @Slf4j
    public static class MeasurementCounts extends MeasEvent {

        public final int[] counts;

        public MeasurementCounts(SomeMessage baseMsg) {
            super(baseMsg.id, baseMsg.data, baseMsg.time, COUNTS);

            this.counts = new int[4];
            for (int i = 0; i < 4; i++) {
                counts[i] = (data[2 * i] & 0xFF) | ((data[2 * i + 1] & 0xFF) << 8);
            }
            log.debug("Counts: {}", counts);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Счета=[");
            for (int i = 0; i < counts.length; i++) {
                sb.append(counts[i]);
                if (i < counts.length - 1) sb.append(", ");
            }
            sb.append("]");
            return COUNTS.getDescription() + " " + sb;
        }
    }

    @Slf4j
    public static class MeasurementDoseRate extends MeasEvent {

        public final float doseRate;

        public MeasurementDoseRate(SomeMessage baseMsg) {
            super(baseMsg.id, baseMsg.data, baseMsg.time, DOSE_RATE);

            byte[] valueBytes = new byte[4];
            System.arraycopy(baseMsg.data, 0, valueBytes, 0, 4);

            ByteBuffer bb = ByteBuffer.wrap(valueBytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            this.doseRate = bb.getFloat();

            log.debug("Dose rate: {}", doseRate);
        }

        @Override
        public String toString() {
            return String.format("%s Мощность дозы=%.6f", DOSE_RATE.getDescription(), doseRate);
        }
    }

    public static class MeasEvent extends SomeMessage {

        public final MeasurementMessageType type;

        public MeasEvent(int id, byte[] data, long time, MeasurementMessageType type) {
            super(id, data, time);
            this.type = type;
        }
    }
}
