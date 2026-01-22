package com.alextim.glagol;

import com.alextim.glagol.client.SomeMessage;
import com.alextim.glagol.client.MessageReceiver;
import com.alextim.glagol.context.AppState;
import com.alextim.glagol.frontend.MainWindow;
import com.alextim.glagol.frontend.dialog.progress.ProgressDialog;
import com.alextim.glagol.frontend.view.data.DataController;
import com.alextim.glagol.frontend.view.magazine.MagazineController;
import com.alextim.glagol.frontend.view.management.ManagementController;
import com.alextim.glagol.frontend.view.metrology.MetrologyController;
import com.alextim.glagol.service.BackgroundMeasService;
import com.alextim.glagol.service.BackgroundMeasService.BackgroundMeasurement;
import com.alextim.glagol.service.ExportService;
import com.alextim.glagol.service.MetrologyMeasService;
import com.alextim.glagol.service.MetrologyMeasService.MetrologyMeasurement;
import com.alextim.glagol.service.StatisticMeasService;
import com.alextim.glagol.service.StatisticMeasService.StatisticMeasurement;
import com.alextim.glagol.service.message.CommandMessages.*;
import com.alextim.glagol.service.message.MeasurementMessages.MeasEvent;
import com.alextim.glagol.service.message.MeasurementMessages.MeasurementDoseRate;
import com.alextim.glagol.service.protocol.Parameter;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.alextim.glagol.context.Property.QUEUE_CAPACITY;
import static com.alextim.glagol.service.MessageParser.parse;
import static com.alextim.glagol.service.protocol.Parameter.*;

@Slf4j
public class RootController extends RootControllerInitializer {

    private final ScheduledExecutorService messageProcessor =
            Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "CAN-Message-Processor"));

    public RootController(MainWindow mainWindow,
                          MessageReceiver transfer,
                          StatisticMeasService statisticMeasService,
                          MetrologyMeasService metrologyMeasService,
                          BackgroundMeasService backgroundMeasService,
                          ExportService exportService,
                          AppState appState) {
        super(  mainWindow,
                transfer,
                statisticMeasService,
                metrologyMeasService,
                backgroundMeasService,
                exportService,
                appState);
    }

    public void listenDetectorClient() {
        messageProcessor.scheduleAtFixedRate(this::processQueuedMessages, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void addStatisticMsg(StatisticMeasurement msg) {
        statisticMsg.add(msg);
        if (statisticMsg.size() > QUEUE_CAPACITY)
            statisticMsg.poll();
    }

    private void processQueuedMessages() {
        SomeMessage message;

        while ((message = transfer.getNextMessage()) != null) {
            SomeMessage parsed = parse(message);
            log.info("DetectorMsg: {} : {}", parsed.getClass().getSimpleName(), parsed);

            lastReceivedMsgTime.set(System.currentTimeMillis());

            getMagazineController().addLog(parsed);

            if (parsed instanceof MeasEvent measEvent) {
                try {
                    handleMeasEvent(measEvent);
                } catch (RuntimeException e) {
                    log.error("handleCommandAnswer exception", e);
                }
            } else if (parsed instanceof AnswerMessage answerMessage) {
                try {
                    handleAnswerMessage(answerMessage);
                } catch (RuntimeException e) {
                    log.error("handleAnswerMessage exception", e);
                }
            }
        }
    }

    private void handleAnswerMessage(AnswerMessage answerMessage) {
        ManagementController managementController = getManagementController();

        if (answerMessage instanceof RestartAnswer answer) {
            managementController.showDialogDetectorIsRestarted();

        } else if (answerMessage instanceof SetParamAnswer answer) {
            if (waitingCommands.isEmpty()) {
                if (!isIgnoreToShowSettingParam(answer.param)) {
                    managementController.showDialogParamIsSet(answer.param);
                }
            }

        } else if (answerMessage instanceof GetParamAnswer answer) {
            managementController.setParam(answer.param, (Float) answer.value);
            if (waitingCommands.isEmpty()) {
                managementController.showDialogParamIsGot(answer.param);
            }
        }

        CommandMessage commandMessage = waitingCommands.pollFirst();
        if (commandMessage != null)
            sendDetectorCommand(commandMessage);
    }

    private boolean isIgnoreToShowSettingParam(Parameter param) {
        return param == BD_BG_CURRENT_RANGE;
    }

    private void handleMeasEvent(MeasEvent measEvent) {
        Optional<StatisticMeasurement> statisticMeasurement = statisticMeasService.addMeasToStatistic(measEvent);

        if (statisticMeasurement.isPresent()) {
            StatisticMeasurement meas = statisticMeasurement.get();

            DataController dataController = getDataController();
            dataController.showStatisticMeas(meas);

            addStatisticMsg(meas);

            if (!statisticMeasService.isRun()) {
                sendDetectorCommand(new StopMeasureCommand());
                dataController.setGrayCircle();
                dataController.enableStartStopBtn(true);
                connectTimer.cancel(true);
            }
        }

        if (measEvent instanceof MeasurementDoseRate measurementDoseRate) {
            Optional<MetrologyMeasurement> metrologyMeasurement = metrologyMeasService.addMeasToMetrology(measurementDoseRate);

            if (metrologyMeasurement.isPresent()) {
                MetrologyController metrologyController = getMetrologyController();
                metrologyController.showMetrologyMeas(metrologyMeasurement.get());

                if (!metrologyMeasService.isRun()) {
                    sendDetectorCommand(new StopMeasureCommand());

                    metrologyController.enableAllBtn();
                }
            }

            Optional<BackgroundMeasurement> backgroundMeasurement = backgroundMeasService.addMeasToMetrology(measurementDoseRate);
            if (backgroundMeasurement.isPresent()) {
                MetrologyController metrologyController = getMetrologyController();
                metrologyController.showBackground(backgroundMeasurement.get());

                if (!backgroundMeasService.isRun()) {
                    sendDetectorCommand(new StopMeasureCommand());

                    metrologyController.enableAllBtn();
                }
            }
        }
    }

    public void sendDetectorCommand(List<? extends CommandMessage> command) {
        waitingCommands.addAll(command);
        sendDetectorCommand(waitingCommands.pollFirst());
    }

    public void sendDetectorCommand(CommandMessage command) {
        getMagazineController().addLog(command);

        transfer.writeMsg(command);
    }

    @SneakyThrows
    public void startMeasurement(long measTime, int range) {
        DataController dataController = getDataController();
        dataController.clearGraphAndTableData();
        dataController.enableStartStopBtn(false);
        dataController.setGreenCircle();

        statisticMeasService.run(measTime);

        sendDetectorCommand(Arrays.asList(new SetParamCommand(BD_BG_CURRENT_RANGE, range),
                new StartMeasureCommand()));

        connectTimer = executorService.submit(() -> {
            lastReceivedMsgTime.set(System.currentTimeMillis());
            try {
                do {
                    Thread.sleep(1000);

                    long cur = System.currentTimeMillis();
                    if (cur - lastReceivedMsgTime.get() > 5000) {
                        dataController.setRedCircle();
                    } else {
                        dataController.setGreenCircle();
                    }
                } while (!Thread.currentThread().isInterrupted());
                log.info("timer canceled");
            } catch (InterruptedException e) {
                log.debug("connect timer sleep interrupted");
            } catch (Exception e) {
                log.error("timer connect exception", e);
            }
        });
    }

    public void stopMeasurement() {
        connectTimer.cancel(true);

        sendDetectorCommand(new StopMeasureCommand());

        DataController dataController = getDataController();
        dataController.setGrayCircle();
        dataController.enableStartStopBtn(true);
    }

    public void startMetrology(int cycleAmount, int measAmount, float realMeasData, float background, int range) {
        sendDetectorCommand(Arrays.asList(new SetParamCommand(BD_BG_CURRENT_RANGE, range),
                new StartMeasureCommand()));

        metrologyMeasService.run(cycleAmount, measAmount, realMeasData, background);
    }

    public void startBackground(int measTime, int range) {
        sendDetectorCommand(Arrays.asList(new SetParamCommand(BD_BG_CURRENT_RANGE, range),
                new StartMeasureCommand()));

        backgroundMeasService.run(measTime);
    }

    public void stopBackground() {
        sendDetectorCommand(new StopMeasureCommand());
    }

    public void clear() {
        statisticMsg.clear();
        statisticMeasService.clear();

        DataController dataController = getDataController();
        dataController.clearGraphAndTableData();
    }

    private final KeyCombination ctrlQ = new KeyCodeCombination(KeyCode.Q, KeyCodeCombination.CONTROL_DOWN);

    public void onKeyEvent(KeyEvent event) {
        if(ctrlQ.match(event)) {
            log.info("Ctrl + Q");
        }
    }

    public void close() {
        try {
            getDataController().putStateParam();
            getMetrologyController().putStateParam();
            appState.saveParam();
        } catch (Exception e) {
            log.error("SaveParams error", e);
        }

        messageProcessor.shutdown();
        try {
            if (!messageProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
                messageProcessor.shutdownNow();
            }
        } catch (InterruptedException e) {
            messageProcessor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        transfer.shutDown();
    }

    public void saveMeasurements(File file, String fileComment) {
        log.info("export to selected file {}", file);

        DoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
        StringProperty statusProperty = new SimpleStringProperty("");

        final ProgressDialog progressDialog = mainWindow.showProgressDialog(progressProperty, statusProperty);

        executorService.submit(() -> {
            try {
                exportService.exportMeasurements(statisticMsg, fileComment, file, (n, progress) ->
                        Platform.runLater(() -> {
                            progressProperty.set(progress);
                            statusProperty.set("Экспорт измерения " + n);
                        })
                );

                Platform.runLater(() -> {
                    progressDialog.forcefullyHideDialog();

                    mainWindow.showDialog(Alert.AlertType.INFORMATION,
                            "Экспорт",
                            "Измерение",
                            "Измерения экспортированы в файлы");
                });
            } catch (Exception e) {
                log.error("saveMeasurements error", e);

                Platform.runLater(() -> {
                    progressDialog.forcefullyHideDialog();

                    mainWindow.showError(Thread.currentThread(), e);
                });
            }
        });
    }

}
