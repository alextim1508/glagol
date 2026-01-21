package com.alextim.glagol.service;

import com.alextim.glagol.service.message.MeasurementMessages.MeasurementDoseRate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.alextim.glagol.service.StatisticMeasService.MEAS_DATA_UNIT;

@Slf4j
public class MetrologyMeasService {

    int count;
    int measAmount;

    int cycleAmount;

    float realMeasData;
    float averageDoseRate;

    @Getter
    boolean run = false;

    final List<Float> aveMeasDataList = new ArrayList<>();

    @AllArgsConstructor
    public static class MetrologyMeasurement {
        public int cycle;
        public float measData;
        public float aveMeasData;
        public String unit;
        public float progress;
        public float error;
    }

    public void run(int cycleAmount, int measAmount, float realMeasData) {
        run = true;
        this.count = 0;
        this.cycleAmount = cycleAmount;
        this.measAmount = measAmount;
        this.realMeasData = realMeasData;
        aveMeasDataList.clear();
    }


    public Optional<MetrologyMeasurement> addMeasToMetrology(MeasurementDoseRate msg) {
        if (!run)
            return Optional.empty();

        averageDoseRate = initAverage(fromMicros(msg.doseRate), averageDoseRate, count + 1);

        /* Проверка, что следующее измерение - измерение нового цикла*/
        if ((count + 1) % measAmount == 0) {
            aveMeasDataList.add(averageDoseRate);
        }

        float aveTotalMeasData = calcAveTotalMeasData();

        float error = calcError(aveTotalMeasData);

        MetrologyMeasurement meas = new MetrologyMeasurement(
                count / measAmount + 1,
                averageDoseRate,
                aveTotalMeasData,
                MEAS_DATA_UNIT,
                1.0f * (count + 1) / (measAmount * cycleAmount),
                error
        );

        /* Проверка на самое последнее измерение*/
        if ((count + 1) / measAmount == cycleAmount) {
            run = false;
        }

        count++;

        return Optional.of(meas);
    }

    private float calcAveTotalMeasData() {
        log.info("calcAveTotalMeasData");
        if (aveMeasDataList.isEmpty()) {
            log.info("aveMeasDataList is empty, returning 0.0f");
            return 0.0f;
        }

        float aveTotalMeasData = 0;
        for (int i = 0; i < aveMeasDataList.size(); i++) {
            aveTotalMeasData += aveMeasDataList.get(i);
            log.info("{}) aveMeasData: {}", i, aveMeasDataList.get(i));
        }

        aveTotalMeasData /= aveMeasDataList.size();
        log.info("aveTotalMeasData: {}", aveTotalMeasData);

        return aveTotalMeasData;
    }

    private float calcError(float aveMeasData) {
        log.info("average meas data: {}", aveMeasData);
        log.info("real meas data: {}", realMeasData);

        float error = 100 * Math.abs(realMeasData - aveMeasData) / realMeasData;
        log.info("error: {}", error);

        return error;
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
