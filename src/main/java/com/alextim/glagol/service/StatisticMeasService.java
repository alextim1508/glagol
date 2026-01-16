package com.alextim.glagol.service;

import com.alextim.glagol.service.message.MeasurementMessages.MeasEvent;
import com.alextim.glagol.service.message.MeasurementMessages.MeasurementCounts;
import com.alextim.glagol.service.message.MeasurementMessages.MeasurementDoseRate;
import com.alextim.glagol.service.message.MeasurementMessages.MeasurementHeader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class StatisticMeasService {

    public static String MEAS_DATA_UNIT = "Зв/ч";
    public static String ACCUM_MEAS_DATA_UNIT = "Зв";

    private int count;
    private long measAmount;

    private float averageDoseRate, accumulatedDoseRate;

    private final float[] averageCounts = new float[5];

    public final long[] accumulatedCounts = new long[5];

    @Getter
    private boolean run = false;

    private final Map<String, MeasEvent> measEvents = new HashMap<>();

    public void run(long measAmount) {
        clear();
        this.count = 0;
        this.measAmount = measAmount;
        run = true;
    }

    public void clear() {
        Arrays.fill(averageCounts, 0);
        Arrays.fill(accumulatedCounts, 0);
        measEvents.clear();

        accumulatedDoseRate = 0;
        averageDoseRate = 0;
    }

    public static class StatisticMeasurement {

        public long[] accumulatedCounts;

        public float[] averageCounts;

        public int[] currentCounts;

        public int counterCount;

        public float currentDoseRate, averageDoseRate, accumulatedDoseRateInTime;

        public long accumulatedInterval;

        public int currentRange;

        public LocalDateTime localDateTime;

        public float progress;
    }


    public Optional<StatisticMeasurement> addMeasToStatistic(MeasEvent measEvent) {
        if (!run)
            return Optional.empty();

        measEvents.put(measEvent.getClass().getSimpleName(), measEvent);

        if (measEvents.size() > 2) {
            if (    measEvents.containsKey(MeasurementHeader.class.getSimpleName()) &&
                    measEvents.containsKey(MeasurementCounts.class.getSimpleName()) &&
                    measEvents.containsKey(MeasurementDoseRate.class.getSimpleName())) {

                MeasurementHeader measurementHeader = (MeasurementHeader) measEvents.remove(MeasurementHeader.class.getSimpleName());
                MeasurementCounts measurementCounts = (MeasurementCounts) measEvents.remove(MeasurementCounts.class.getSimpleName());
                MeasurementDoseRate measurementDoseRate = (MeasurementDoseRate) measEvents.remove(MeasurementDoseRate.class.getSimpleName());


                StatisticMeasurement statMeas = new StatisticMeasurement();

                statMeas.currentRange = measurementHeader.currentRange;
                statMeas.counterCount = measurementHeader.counterCount;


                statMeas.currentCounts = new int[]{
                        measurementCounts.counts[0],
                        measurementCounts.counts[1],
                        measurementCounts.counts[2],
                        measurementCounts.counts[3],
                        0
                };
                statMeas.currentCounts[4] = statMeas.currentCounts[0] + statMeas.currentCounts[1] + statMeas.currentCounts[2] + statMeas.currentCounts[3];
                log.info("Current counts: {}", statMeas.currentCounts);


                statMeas.averageCounts = new float[]{
                        averageCounts[0] = initAverage(statMeas.currentCounts[0], averageCounts[0], count + 1),
                        averageCounts[1] = initAverage(statMeas.currentCounts[1], averageCounts[1], count + 1),
                        averageCounts[2] = initAverage(statMeas.currentCounts[2], averageCounts[2], count + 1),
                        averageCounts[3] = initAverage(statMeas.currentCounts[3], averageCounts[3], count + 1),
                        0
                };
                statMeas.averageCounts[4] = statMeas.averageCounts[0] + statMeas.averageCounts[1] + statMeas.averageCounts[2] + statMeas.averageCounts[3];
                log.info("Average counts: {}", statMeas.averageCounts);


                statMeas.accumulatedCounts = new long[]{
                        accumulatedCounts[0] = initAccumulated(measurementCounts.counts[0], accumulatedCounts[0]),
                        accumulatedCounts[1] = initAccumulated(measurementCounts.counts[1], accumulatedCounts[1]),
                        accumulatedCounts[2] = initAccumulated(measurementCounts.counts[2], accumulatedCounts[2]),
                        accumulatedCounts[3] = initAccumulated(measurementCounts.counts[3], accumulatedCounts[3]),
                        0
                };
                statMeas.accumulatedCounts[4] = statMeas.accumulatedCounts[0] + statMeas.accumulatedCounts[1] + statMeas.accumulatedCounts[2] + statMeas.accumulatedCounts[3];
                log.info("Accumulated counts: {}", statMeas.accumulatedCounts);


                log.info("Raw current dose rate: {}", measurementDoseRate.doseRate);
                statMeas.currentDoseRate = fromMicros(measurementDoseRate.doseRate);
                log.info("Current dose rate: {}", statMeas.currentDoseRate);


                averageDoseRate = initAverage(measurementDoseRate.doseRate, averageDoseRate, count + 1);

                statMeas.averageDoseRate = fromMicros(averageDoseRate);
                log.info("Average dose rate: {}", statMeas.averageDoseRate);

                final int secondsInHour = 3600;
                accumulatedDoseRate += statMeas.currentDoseRate / secondsInHour;
                statMeas.accumulatedDoseRateInTime = accumulatedDoseRate;
                log.info("Accumulated dose rate: {}", statMeas.accumulatedDoseRateInTime);

                statMeas.progress = 1.0f * (count + 1) / measAmount;

                statMeas.accumulatedInterval = count + 1;
                statMeas.localDateTime = LocalDateTime.now();

                if (count == measAmount - 1) {
                    run = false;
                }

                count++;

                return Optional.of(statMeas);
            }
        }
        return Optional.empty();
    }

    private float fromMicros(float value) {
        return value / 1_000_000;
    }

    private long initAccumulated(int cur, long accumulated) {
        return cur + accumulated;
    }

    private float initAverage(float cur, float average, int count) {
        log.debug("initAverage: Current value: {} Average value {} Count: {}", cur, average, count);

        if (count == 0)
            throw new IllegalArgumentException("Count can not be 0");

        float coff = 1 - 1.0f / count;
        log.debug("Coef: {}", coff);

        average = coff * average + (1 - coff) * cur;
        log.debug("Calculated average: {}", average);

        return average;
    }
}
