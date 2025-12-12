package com.alextim.glagol.frontend.view.management;

import com.alextim.glagol.frontend.view.NodeController;
import com.alextim.glagol.service.protocol.Parameter;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.alextim.glagol.service.protocol.Parameter.*;
import static javafx.scene.control.Alert.AlertType.*;

@Slf4j
public abstract class ManagementControllerInitializer extends NodeController {

    public static final String HEADER = "Работа с параметрами БД";
    public static final String ARE_YOU_SURE = "Вы уверены, что хотите задать Параметр %s в БД ?";
    public static final String ERROR_PARSE_FILED = "Ошибка обработки поля %s";
    public static final String ERROR_PARSE_TITLE = "Ошибка преобразования текста в число";
    public static final String PARAM_IS_SET = "Параметр %s задан в БД";
    public static final String PARAM_IS_GOT = "Параметр %s прочитан из БД";
    public static final String ERROR_ANSWER = "Параметр %s не задан. Команда завершилась с ошибкой: %s";
    public static final String ARE_YOU_SURE_TO_RESTART = "Вы уверены, что хотите перезапустить БД ?";
    public static final String DETECTOR_IS_NORMALLY_RESTARTED = "БД успешно перезапущен";
    public static final String DETECTOR_IS_EMERGENCY_RESTARTED = "БД аварийно перезапущен";

    @FXML
    protected AnchorPane pane;
    @FXML
    protected TextField deadTime1, deadTime2, deadTime3;
    @FXML
    protected TextField counterCoef11, counterCoef12, counterCoef13, counterCoef14;
    @FXML
    protected TextField counterCoef21, counterCoef22;
    @FXML
    protected TextField counterCoef31, counterCoef32;
    @FXML
    protected Label softwareVersion, dateBuild;

    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("#.######",
            DecimalFormatSymbols.getInstance(Locale.US));

    @Override
    protected String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        paneInit();
    }

    private void paneInit() {
        /* Bug JavaFX. Other tabs of tabPane get ScrollEvent from current tab */
        pane.addEventHandler(ScrollEvent.ANY, Event::consume);
    }


    public void showDialogParamIsSet(Parameter parameter) {
        Platform.runLater(() -> {
            rootController.getMainWindow().showDialog(INFORMATION, "Информация",
                    HEADER,
                    String.format(PARAM_IS_SET, parameter.getDescription()));
        });
    }

    public void showDialogParamIsGot(Parameter parameter) {
        Platform.runLater(() -> {
            rootController.getMainWindow().showDialog(INFORMATION, "Информация",
                    HEADER,
                    String.format(PARAM_IS_GOT, parameter.getDescription()));
        });
    }

    protected boolean areYouSure(Parameter parameter) {
        return rootController.getMainWindow().showDialog(WARNING, "Внимание",
                HEADER,
                String.format(ARE_YOU_SURE, parameter.getDescription()));
    }

    protected void showParsingErrorDialog(Parameter parameter) {
        rootController.getMainWindow().showDialog(ERROR, "Ошибка",
                String.format(ERROR_PARSE_FILED, parameter.getDescription()),
                ERROR_PARSE_TITLE);
    }

    protected boolean areYouSureDetectorRestart() {
        return rootController.getMainWindow().showDialog(WARNING, "Внимание",
                HEADER,
                ARE_YOU_SURE_TO_RESTART);
    }

    public void showDialogDetectorIsRestarted() {
        Platform.runLater(() -> {
            rootController.getMainWindow().showDialog(INFORMATION, "Информация",
                    HEADER,
                    DETECTOR_IS_NORMALLY_RESTARTED);
        });
    }

    public void setParam(Parameter parameter, float value) {
        Platform.runLater(() -> {
            log.debug("Setting parameter {} to value {}", parameter, value);
            String formattedValue = DECIMAL_FORMATTER.format(value); // Форматируем значение с помощью DecimalFormat
            log.debug("Formatted parameter {} value to: {}", parameter, formattedValue);

            if (parameter == BD_BG_SI29G_COEFF_1) {
                this.counterCoef11.setText(formattedValue);
            } else if (parameter == BD_BG_SI29G_COEFF_2) {
                this.counterCoef12.setText(formattedValue);
            } else if (parameter == BD_BG_SI29G_COEFF_3) {
                this.counterCoef13.setText(formattedValue);
            } else if (parameter == BD_BG_SI29G_COEFF_4) {
                this.counterCoef14.setText(formattedValue);

            } else if (parameter == BD_BG_SBM21_COEFF_1) {
                this.counterCoef21.setText(formattedValue);
            } else if (parameter == BD_BG_SBM21_COEFF_2) {
                this.counterCoef22.setText(formattedValue);

            } else if (parameter == BD_BG_SI38G_COEFF_1) {
                this.counterCoef31.setText(formattedValue);
            } else if (parameter == BD_BG_SI38G_COEFF_2) {
                this.counterCoef32.setText(formattedValue);

            } else if (parameter == BD_BG_RANGE1_DEAD_TIME) {
                this.deadTime1.setText(formattedValue); // Используем форматированное значение
            } else if (parameter == BD_BG_RANGE2_DEAD_TIME) {
                this.deadTime2.setText(formattedValue);
            } else if (parameter == BD_BG_RANGE3_DEAD_TIME) {
                this.deadTime3.setText(formattedValue);
            } else {
                log.warn("Unknown parameter for setParam: {}", parameter);
            }
        });
    }

    public void setSoftwareVersion(String version, String date) {
        Platform.runLater(() -> {
            softwareVersion.setText(version);
            dateBuild.setText(date);
        });
    }
}
