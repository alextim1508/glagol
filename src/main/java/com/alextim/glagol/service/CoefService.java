package com.alextim.glagol.service;

import com.alextim.glagol.service.message.MeasurementMessages.MeasurementCounts;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class CoefService {

    int count;
    long measAmount;

    float realMeasData;

    int range;

    final float[] sensitivities = new float[]{
            1.229f, 0.316f, 0.001045f
    };

    final float[] deadTimes = new float[]{
            0.00042f, 0.0002f, 0.0002f
    };

    @Getter
    boolean run = false;

    final float[] averageCounts = new float[4];

    final float[] calculatedCounts = new float[4];

    public void run(long measTime, int range, float realMeasData) {
        clear();
        this.count = 0;
        this.measAmount = measTime;
        this.realMeasData = realMeasData;
        this.range = range;
        run = true;
    }

    public void clear() {
        Arrays.fill(averageCounts, 0);
        Arrays.fill(calculatedCounts, 0);
    }

    public static class CoefsMeasurement {

        public float[] calculatedCounts;

        public float[] averageCounts;

        public int[] currentCounts;

        public float progress;
    }


    public Optional<CoefsMeasurement> handle(MeasurementCounts measEvent) {
        if (!run)
            return Optional.empty();


        CoefsMeasurement coefsMeas = new CoefsMeasurement();

        coefsMeas.currentCounts = new int[]{
                measEvent.counts[0],
                measEvent.counts[1],
                measEvent.counts[2],
                measEvent.counts[3],
        };
        log.info("Current counts: {}", coefsMeas.currentCounts);


        coefsMeas.averageCounts = new float[]{
                averageCounts[0] = initAverage(coefsMeas.currentCounts[0], averageCounts[0], count + 1),
                averageCounts[1] = initAverage(coefsMeas.currentCounts[1], averageCounts[1], count + 1),
                averageCounts[2] = initAverage(coefsMeas.currentCounts[2], averageCounts[2], count + 1),
                averageCounts[3] = initAverage(coefsMeas.currentCounts[3], averageCounts[3], count + 1),
                0
        };
        log.info("Average counts: {}", coefsMeas.averageCounts);

        coefsMeas.calculatedCounts = new float[]{
                calculatedCounts[0] = initCalculated(averageCounts[0]),
                calculatedCounts[1] = initCalculated(averageCounts[1]),
                calculatedCounts[2] = initCalculated(averageCounts[2]),
                calculatedCounts[3] = initCalculated(averageCounts[3]),
        };
        log.info("Calculated counts: {}", coefsMeas.calculatedCounts);

        coefsMeas.progress = 1.0f * (count + 1) / measAmount;

        if (count == measAmount - 1) {
            run = false;
        }

        count++;

        return Optional.of(coefsMeas);
    }

    private float initCalculated(float averageCount) {
        log.info("initCalculated");
        log.info("RealMeasData: {}", realMeasData);
        log.info("Sensitivity: {}", sensitivities[range - 1]);
        log.info("DeadTime: {}", deadTimes[range - 1]);
        log.info("AverageCount: {}", averageCount);

        float calculatedCoef = (float) (toMicros(realMeasData) * sensitivities[range - 1] * (1.0 - averageCount * deadTimes[range - 1]) / averageCount);
        log.info("CalculatedCoef: {}", calculatedCoef);

        return calculatedCoef;
    }

    private float toMicros(float value) {
        return value * 1_000_000;
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
