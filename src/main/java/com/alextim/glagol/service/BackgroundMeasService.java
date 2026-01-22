package com.alextim.glagol.service;

import com.alextim.glagol.service.message.MeasurementMessages.MeasurementDoseRate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static com.alextim.glagol.service.StatisticMeasService.MEAS_DATA_UNIT;

@Slf4j
public class BackgroundMeasService {

    int count;
    long measTime;

    float averageDoseRate;

    @Getter
    boolean run = false;

    public void run(long measTime) {
        clear();
        this.count = 0;
        this.measTime = measTime;
        run = true;
    }

    public void clear() {
        averageDoseRate = 0;
    }

    @AllArgsConstructor
    public static class BackgroundMeasurement {
        public float averageDoseRate;
        public String unit;
        public float progress;
    }

    public Optional<BackgroundMeasurement> addMeasToMetrology(MeasurementDoseRate msg) {
        if (!run)
            return Optional.empty();

        averageDoseRate = initAverage(fromMicros(msg.doseRate), averageDoseRate, count + 1);

        BackgroundMeasurement meas = new BackgroundMeasurement(
                averageDoseRate,
                MEAS_DATA_UNIT,
                1.0f * (count + 1) / measTime
        );

        if ((count + 1) == measTime) {
            run = false;
        }

        count++;

        return Optional.of(meas);
    }

    private float fromMicros(float value) {
        return value / 1_000_000;
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
