package com.alextim.glagol.frontend.view.coefs;

import com.alextim.glagol.frontend.view.NodeController;
import com.alextim.glagol.service.CoefService.CoefsMeasurement;
import com.alextim.glagol.service.protocol.Parameter;
import com.alextim.glagol.service.protocol.Range;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.alextim.glagol.context.Property.AVERAGE_COUNTER_NUMBER_FORMAT;
import static com.alextim.glagol.frontend.MainWindow.PROGRESS_BAR_COLOR;
import static com.alextim.glagol.frontend.view.management.ManagementControllerInitializer.ERROR_PARSE_TITLE;
import static com.alextim.glagol.service.util.ValueFormatter.parsingValuePrefix;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.WARNING;

@Slf4j
public abstract class CoefsControllerInitializer extends NodeController {

    public static final String HEADER = "Работа с параметрами БД";
    public static final String ARE_YOU_SURE = "Вы уверены, что хотите задать Параметр %s в БД ?";
    public static final String ERROR_PARSE_FILED = "Ошибка обработки поля %s";

    private final String RANGE_STATE_APP_PARAM = "coefs.range";
    private final String MEAS_TIME_STATE_APP_PARAM = "coefs.measTime";
    private final String REAL_MEAS_DATA_STATE_APP_PARAM = "coefs.realMeasData";
    private final String REAL_MEAS_DATA_UNIT_STATE_APP_PARAM = "coefs.realMeasDataUnit";


    @FXML
    private AnchorPane pane;

    @FXML
    private ComboBox<Range> inputRange;

    @FXML
    private TextField realMeasData;
    @FXML
    private ComboBox<String> realMeasDataUnit;
    @FXML
    private TextField measTime;

    @FXML
    private TableView<TableRow> table;
    @FXML
    private TableColumn<TableRow, String> countTitle;
    @FXML
    private TableColumn<TableRow, Float> calculatedCount;
    @FXML
    private TableColumn<TableRow, Float> averageCount;
    @FXML
    private TableColumn<TableRow, Integer> currentCount;

    @FXML
    private Button startCoefCalc, stopCoefCalc;
    @FXML
    private Label progressLabel;
    @FXML
    private ProgressBar progressBar;

    @Override
    protected String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        paneInit();
        inputRangeInit();
        getInputRange();
        initUnitsInit();
        tableInitialize();
        fullTable();
        initProgressBar();
        initCoefsFields();
    }

    private void paneInit() {
        /* Bug JavaFX. Other tabs of tabPane get ScrollEvent from current tab */
        pane.addEventHandler(ScrollEvent.ANY, Event::consume);
    }

    private void inputRangeInit() {
        inputRange.setItems(FXCollections.observableArrayList(Range.RANGE_1, Range.RANGE_2, Range.RANGE_3));
        inputRange.getSelectionModel().select(Range.RANGE_1);
        inputRange.setConverter(new StringConverter<>() {
            @Override
            public String toString(Range object) {
                if (object != null)
                    return object.getDescription();
                return "";
            }

            @Override
            public Range fromString(String string) {
                return inputRange.getItems().stream().filter(r ->
                        r.getDescription().equals(string)).findFirst().orElse(null);
            }
        });
    }

    protected Range getInputRange() {
        return inputRange.getSelectionModel().getSelectedItem();
    }

    private void initProgressBar() {
        progressBar.setStyle("-fx-accent: " + PROGRESS_BAR_COLOR);
    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
        progressLabel.setText(String.format(Locale.US, " %.1f%%", 100 * progress));
    }

    private void initUnitsInit() {
        ObservableList<String> units = FXCollections.observableArrayList(
                "Зв/ч",
                "мЗв/ч",
                "мкЗв/ч",
                "нЗв/ч"
        );

        realMeasDataUnit.setItems(units);
        realMeasDataUnit.getSelectionModel().select(2);
    }

    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("#.######",
            DecimalFormatSymbols.getInstance(Locale.US));

    private void tableInitialize() {
        countTitle.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().countTitle));

        currentCount.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().currentCount));

        averageCount.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().averageCount));
        averageCount.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TableRow, Float> call(TableColumn<TableRow, Float> tableRowDoubleTableColumn) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Float item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            if (item != null) {
                                setText(String.format(Locale.US, AVERAGE_COUNTER_NUMBER_FORMAT, item));
                            }
                        }
                    }
                };
            }
        });

        calculatedCount.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().calculatedCount));
        calculatedCount.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TableRow, Float> call(TableColumn<TableRow, Float> tableRowDoubleTableColumn) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Float item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            if (item != null) {
                                setText(DECIMAL_FORMATTER.format(item));
                            }
                        }
                    }
                };
            }
        });

        table.setItems(FXCollections.observableArrayList());
    }

    @AllArgsConstructor
    private static class TableRow {
        public String countTitle;
        public int currentCount;
        public float averageCount;
        public float calculatedCount;
    }

    private void fullTable() {
        table.getItems().addAll(
                new TableRow("Счетчик 1", 0, 0, 0),
                new TableRow("Счетчик 2", 0, 0, 0),
                new TableRow("Счетчик 3", 0, 0, 0),
                new TableRow("Счетчик 4", 0, 0, 0)
        );
    }

    public void updateTable(CoefsMeasurement meas) {
        ObservableList<TableRow> items = table.getItems();

        items.get(0).calculatedCount = meas.calculatedCounts[0];
        items.get(0).averageCount = meas.averageCounts[0];
        items.get(0).currentCount = meas.currentCounts[0];

        items.get(1).calculatedCount = meas.calculatedCounts[1];
        items.get(1).averageCount = meas.averageCounts[1];
        items.get(1).currentCount = meas.currentCounts[1];

        items.get(2).calculatedCount = meas.calculatedCounts[2];
        items.get(2).averageCount = meas.averageCounts[2];
        items.get(2).currentCount = meas.currentCounts[2];

        items.get(3).calculatedCount = meas.calculatedCounts[3];
        items.get(3).averageCount = meas.averageCounts[3];
        items.get(3).currentCount = meas.currentCounts[3];

        table.refresh();
    }

    public void clearTable() {
        ObservableList<TableRow> items = table.getItems();
        for (TableRow item : items) {
            item.calculatedCount = 0;
            item.averageCount = 0;
            item.currentCount = 0;
        }
        table.refresh();
    }

    private void initCoefsFields() {
        String param = rootController.getAppState().getParam(REAL_MEAS_DATA_STATE_APP_PARAM);
        if (param != null) {
            realMeasData.setText(param);
        }
        param = rootController.getAppState().getParam(REAL_MEAS_DATA_UNIT_STATE_APP_PARAM);
        if (param != null) {
            realMeasDataUnit.getSelectionModel().select(Integer.parseInt(param));
        }
        param = rootController.getAppState().getParam(RANGE_STATE_APP_PARAM);
        if (param != null) {
            inputRange.getSelectionModel().select(Integer.parseInt(param));
        }
        param = rootController.getAppState().getParam(MEAS_TIME_STATE_APP_PARAM);
        if (param != null) {
            measTime.setText(param);
        }
    }

    @FXML
    void startCoefCalcOn(ActionEvent event) {
        int measTime;
        try {
            measTime = Integer.parseInt(this.measTime.getText());
            log.info("measTime: {}", measTime);
        } catch (Exception e) {
            log.error("startMetrology measTime parsing", e);
            showParsingErrorDialog("Экспозиция");
            return;
        }

        String selectedItem = realMeasDataUnit.getSelectionModel().getSelectedItem();
        double coef = parsingValuePrefix(selectedItem);

        float realMeasData;
        try {
            realMeasData = Float.parseFloat(this.realMeasData.getText());
        } catch (Exception e) {
            log.error("startMetrology measTime parsing", e);
            showParsingErrorDialog("Действительное значение");
            return;
        }
        log.info(String.format("realMeasData: %f", realMeasData));

        float realMeasDataInBaseUnit = (float) (realMeasData * coef);
        log.info(String.format("realMeasDataInBaseUnit: %f", realMeasDataInBaseUnit));

        clearTable();
        enableStartStopBtn(false);
        startCoefCalculated(measTime, realMeasDataInBaseUnit, getInputRange().getCode());
    }

    protected abstract void startCoefCalculated(int measTime, float realMeasData, int range);

    @FXML
    void stopCoefCalcOn(ActionEvent event) {
        stopCoefCalculated();
    }

    protected abstract void stopCoefCalculated();

    @FXML
    void setCoefOn(ActionEvent event) {
        ObservableList<TableRow> items = table.getItems();

        setCoef(getInputRange().getCode(), new float[] {
                items.get(0).calculatedCount,
                items.get(1).calculatedCount,
                items.get(2).calculatedCount,
                items.get(3).calculatedCount
        });
    }

    protected abstract void setCoef( int range, float[] coefs);

    public void enableStartStopBtn(boolean isStartBtnEnabled) {
        startCoefCalc.setDisable(!isStartBtnEnabled);
        stopCoefCalc.setDisable(isStartBtnEnabled);
    }

    protected boolean areYouSure(Parameter parameter) {
        return rootController.getMainWindow().showDialog(WARNING, "Внимание",
                HEADER,
                String.format(ARE_YOU_SURE, parameter.getDescription()));
    }

    protected void showParsingErrorDialog(String field) {
        rootController.getMainWindow().showDialog(ERROR, "Ошибка",
                String.format(ERROR_PARSE_FILED, field),
                ERROR_PARSE_TITLE);
    }

    public void putStateParam() {
        rootController.getAppState().putParam(MEAS_TIME_STATE_APP_PARAM, measTime.getText());
        rootController.getAppState().putParam(REAL_MEAS_DATA_STATE_APP_PARAM, realMeasData.getText());
        rootController.getAppState().putParam(REAL_MEAS_DATA_UNIT_STATE_APP_PARAM, String.valueOf(realMeasDataUnit.getSelectionModel().getSelectedIndex()));
        rootController.getAppState().putParam(RANGE_STATE_APP_PARAM, String.valueOf(inputRange.getSelectionModel().getSelectedIndex()));
    }
}
