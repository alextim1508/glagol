package com.alextim.glagol.frontend.view.data;


import com.alextim.glagol.service.StatisticMeasService.StatisticMeasurement;
import com.alextim.glagol.service.util.ValueFormatter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.time.ZoneId;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.alextim.glagol.context.Property.*;
import static com.alextim.glagol.service.StatisticMeasService.ACCUM_MEAS_DATA_UNIT;
import static com.alextim.glagol.service.StatisticMeasService.MEAS_DATA_UNIT;


@Slf4j
public class DataController extends DataControllerInitializer {

    @Override
    protected String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        enableStartStopBtn(true);

        setGrayCircle();
    }

    public void showStatisticMeas(StatisticMeasurement meas) {
        log.info("Show statistic");
        long timestamp = meas.localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        log.info("Current dose rate: {}", meas.currentDoseRate);
        String formattedCurrentDoseRate = new ValueFormatter(
                Math.abs(meas.currentDoseRate), MEAS_DATA_UNIT, MEAS_DATA_NUMBER_SING_DIGITS)
                .toString();
        log.info("Formatted current dose rate: {}", formattedCurrentDoseRate);

        log.info("Average dose rate: {}", meas.averageDoseRate);
        String formattedAverageDoseRate = new ValueFormatter(
                Math.abs(meas.averageDoseRate), MEAS_DATA_UNIT, MEAS_DATA_NUMBER_SING_DIGITS)
                .toString();
        log.info("Formatted average dose rate: {}", formattedAverageDoseRate);

        log.info("Accumulated dose rate: {}", meas.accumulatedDoseRateInTime);
        String formattedAccumulatedDoseRateInTime = new ValueFormatter(
                Math.abs(meas.accumulatedDoseRateInTime), ACCUM_MEAS_DATA_UNIT, MEAS_DATA_NUMBER_SING_DIGITS)
                .toString();
        log.info("Formatted accumulated dose rate: {}", formattedAccumulatedDoseRateInTime);

        currentDoseRateGraph.addPoint(graphIndex, timestamp, meas.currentDoseRate,
                String.format(Locale.US, CURRENT_GRAPH_LABEL_FORMAT, formattedCurrentDoseRate,
                        meas.currentCounts[0], meas.currentCounts[1], meas.currentCounts[2], meas.currentCounts[3]));

        if (currentDoseRateGraph.size() > QUEUE_CAPACITY)
            currentDoseRateGraph.remove(0);

        averageDoseRateGraph.addPoint(graphIndex, timestamp, meas.averageDoseRate,
                String.format(Locale.US, AVERAGE_GRAPH_LABEL_FORMAT, formattedAverageDoseRate,
                        meas.averageCounts[0], meas.averageCounts[1], meas.averageCounts[2], meas.averageCounts[3]));

        if (averageDoseRateGraph.size() > QUEUE_CAPACITY)
            averageDoseRateGraph.remove(0);

        graphIndex++;

        updateTable(meas);

        setDoseRates((meas.currentDoseRate < 0 ? "-" : "") + formattedCurrentDoseRate,
                (meas.averageDoseRate < 0 ? "-" : "") + formattedAverageDoseRate,
                (meas.accumulatedDoseRateInTime < 0 ? "-" : "") + formattedAccumulatedDoseRateInTime);

        setMeasTime(meas.accumulatedInterval + " сек");

        setCurrentRage(meas.currentRange);

        setProgress(meas.progress);
    }

    @Override
    void start(long measTime, int range) {
        rootController.startMeasurement(measTime, range);
    }

    @Override
    void stop() {
        rootController.stopMeasurement();
    }

    @Override
    void save() {
        File file = rootController.showFileChooseDialog();
        if (file != null) {
            rootController.saveMeasurements(file, getFileComment());
        }
    }

    @Override
    void clear() {
        rootController.clear();
    }
}
