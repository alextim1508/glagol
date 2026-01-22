package com.alextim.glagol.frontend.view.metrology;

import com.alextim.glagol.service.BackgroundMeasService.BackgroundMeasurement;
import com.alextim.glagol.service.MetrologyMeasService.MetrologyMeasurement;
import javafx.application.Platform;

public class MetrologyController extends MetrologyControllerInitializer {

    @Override
    protected void startMetrology(int cycleAmount, int measAmount, float realMeasData, float background, int range) {
        rootController.startMetrology(cycleAmount, measAmount, realMeasData, background, range);
    }

    @Override
    protected void startBackground(int measTime, int range) {
        rootController.startBackground(measTime, range);
    }

    @Override
    protected void stopBackground() {
        rootController.stopBackground();
    }

    public void showMetrologyMeas(MetrologyMeasurement metrologyMeas) {
        Platform.runLater(() -> {
            setAveMeasData(metrologyMeas.aveMeasData, metrologyMeas.unit);
            updateTable(metrologyMeas.cycle, metrologyMeas.measData, metrologyMeas.unit);
            setError(metrologyMeas.error);
            setProgress(metrologyMeas.progress);
        });
    }

    public void showBackground(BackgroundMeasurement backgroundMeasurement) {
        Platform.runLater(() -> {
            setBackground(backgroundMeasurement.averageDoseRate, backgroundMeasurement.unit);
            setProgress(backgroundMeasurement.progress);
        });
    }

}
