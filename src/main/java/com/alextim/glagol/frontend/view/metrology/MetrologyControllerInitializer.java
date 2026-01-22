package com.alextim.glagol.frontend.view.metrology;

import com.alextim.glagol.frontend.view.NodeController;
import com.alextim.glagol.service.protocol.Range;
import com.alextim.glagol.service.util.ValueFormatter;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.alextim.glagol.context.Property.METROLOGY_ERROR_NUMBER_SING_DIGITS;
import static com.alextim.glagol.context.Property.MEAS_DATA_NUMBER_SING_DIGITS;
import static com.alextim.glagol.frontend.MainWindow.PROGRESS_BAR_COLOR;
import static com.alextim.glagol.frontend.view.management.ManagementControllerInitializer.ERROR_PARSE_FILED;
import static com.alextim.glagol.frontend.view.management.ManagementControllerInitializer.ERROR_PARSE_TITLE;
import static com.alextim.glagol.service.util.ValueFormatter.*;
import static javafx.scene.control.Alert.AlertType.ERROR;

@Slf4j
public abstract class MetrologyControllerInitializer extends NodeController {

    private final String CYCLE_AMOUNT_STATE_APP_PARAM = "metrology.cycleAmount";
    private final String MEAS_AMOUNT_STATE_APP_PARAM = "metrology.measAmount";
    private final String RANGE_STATE_APP_PARAM = "metrology.range";
    private final String REAL_MEAS_DATA_STATE_APP_PARAM = "metrology.realMeasData";
    private final String REAL_MEAS_DATA_UNIT_STATE_APP_PARAM = "metrology.realMeasDataUnit";
    private final String BACKGROUND_MEAS_DATA_STATE_APP_PARAM = "metrology.backgroundMeasData";
    private final String BACKGROUND_MEAS_DATA_UNIT_STATE_APP_PARAM = "metrology.backgroundMeasDataUnit";
    private final String BACKGROUND_MEAS_TIME_STATE_APP_PARAM = "metrology.backgroundMeasTime";

    @FXML
    private AnchorPane pane;


    @FXML
    private TextField cycleAmount;
    @FXML
    private TextField measAmount;

    @FXML
    private ComboBox<Range> inputRange;

    @FXML
    private TextField realMeasData;
    @FXML
    private ComboBox<String> realMeasDataUnit;

    @FXML
    private TextField backgroundMeasData;
    @FXML
    private ComboBox<String> backgroundMeasDataUnit;

    @FXML
    private TextField error;

    @FXML
    private TextField measTime;

    @FXML
    private Label progressLabel;
    @FXML
    private ProgressBar progressBar;

    @FXML
    private TableView<TableRow> table;
    @FXML
    private TableColumn<TableRow, Integer> numberCycleColumn;
    @FXML
    private TableColumn<TableRow, String> aveMeasDataColumn;

    @FXML
    private TextField aveMeasData;

    @FXML
    private Button startBtn, startBackgroundBtn, stopBackgroundBtn;

    @AllArgsConstructor
    public static class TableRow {
        public int numberCycle;
        public float aveMeasData;
        public String unit;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        paneInit();
        inputRangeInit();
        initUnitsInit();
        initTable();
        initProgressBar();
        initMetrologyFields();
    }

    private void paneInit() {
        /* Bug JavaFX. Other tabs of tabPane get ScrollEvent from current tab */
        pane.addEventHandler(ScrollEvent.ANY, Event::consume);
    }

    private void inputRangeInit() {
        inputRange.setItems(FXCollections.observableArrayList(Range.values()));
        inputRange.getSelectionModel().select(Range.AUTO);
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

    private void initUnitsInit() {
        ObservableList<String> units = FXCollections.observableArrayList(
                "Зв/ч",
                "мЗв/ч",
                "мкЗв/ч",
                "нЗв/ч"
        );

        realMeasDataUnit.setItems(units);
        realMeasDataUnit.getSelectionModel().select(2);
        backgroundMeasDataUnit.setItems(units);
        backgroundMeasDataUnit.getSelectionModel().select(2);
    }

    private void initTable() {
        numberCycleColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().numberCycle));
        aveMeasDataColumn.setCellValueFactory(param -> {
            if (Float.isNaN(param.getValue().aveMeasData) || Float.isInfinite(param.getValue().aveMeasData)) {
                return new ReadOnlyObjectWrapper<>("-");
            }

            String s = new ValueFormatter(
                    param.getValue().aveMeasData,
                    param.getValue().unit,
                    MEAS_DATA_NUMBER_SING_DIGITS).toString();

            return new ReadOnlyObjectWrapper<>(s);
        });

        table.setPlaceholder(new Label(""));
        table.setItems(FXCollections.observableArrayList());
    }

    private void initMetrologyFields() {
        String param = rootController.getAppState().getParam(CYCLE_AMOUNT_STATE_APP_PARAM);
        if (param != null) {
            cycleAmount.setText(param);
        }
        param = rootController.getAppState().getParam(MEAS_AMOUNT_STATE_APP_PARAM);
        if (param != null) {
            measAmount.setText(param);
        }
        param = rootController.getAppState().getParam(REAL_MEAS_DATA_STATE_APP_PARAM);
        if (param != null) {
            realMeasData.setText(param);
        }
        param = rootController.getAppState().getParam(REAL_MEAS_DATA_UNIT_STATE_APP_PARAM);
        if (param != null) {
            realMeasDataUnit.getSelectionModel().select(Integer.parseInt(param));
        }
        param = rootController.getAppState().getParam(BACKGROUND_MEAS_DATA_STATE_APP_PARAM);
        if (param != null) {
            backgroundMeasData.setText(param);
        }
        param = rootController.getAppState().getParam(BACKGROUND_MEAS_DATA_UNIT_STATE_APP_PARAM);
        if (param != null) {
            backgroundMeasDataUnit.getSelectionModel().select(Integer.parseInt(param));
        }
        param = rootController.getAppState().getParam(RANGE_STATE_APP_PARAM);
        if (param != null) {
            inputRange.getSelectionModel().select(Integer.parseInt(param));
        }
        param = rootController.getAppState().getParam(BACKGROUND_MEAS_TIME_STATE_APP_PARAM);
        if (param != null) {
            measTime.setText(param);
        }
    }

    @Override
    protected String getName() {
        return getClass().getSimpleName();
    }

    private void initProgressBar() {
        progressBar.setStyle("-fx-accent: " + PROGRESS_BAR_COLOR);
    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
        progressLabel.setText(String.format(Locale.US, " %.1f%%", 100 * progress));
    }

    public void setError(double error) {
        this.error.setText(
                Double.isNaN(error) || Double.isInfinite(error) ? "-" :
                        String.valueOf(sigDigRounder(error, METROLOGY_ERROR_NUMBER_SING_DIGITS)));
    }

    public void setAveMeasData(float aveMeasData, String unit) {
        if (Float.isNaN(aveMeasData) || Float.isInfinite(aveMeasData)) {
            this.aveMeasData.setText("-");
        } else {
            this.aveMeasData.setText(
                    new ValueFormatter(
                            aveMeasData,
                            unit,
                            MEAS_DATA_NUMBER_SING_DIGITS).toString());

        }
    }

    public void setBackground(float aveDoseRate, String unit) {
        if (Float.isNaN(aveDoseRate) || Float.isInfinite(aveDoseRate)) {
            backgroundMeasData.setText("-");
        } else {
            String selectedItem = backgroundMeasDataUnit.getSelectionModel().getSelectedItem();
            double coef = parsingValuePrefix(selectedItem);
            double signedBackground = sigDigRounder(aveDoseRate / coef, MEAS_DATA_NUMBER_SING_DIGITS);

            backgroundMeasData.setText(String.valueOf(signedBackground));
        }
    }

    public void updateTable(int cycle, float measData, String unit) {
        ObservableList<TableRow> items = table.getItems();
        if (cycle > items.size()) {
            table.getItems().add(new TableRow(0, 0.0f, ""));
        }
        items.get(cycle - 1).numberCycle = cycle;
        items.get(cycle - 1).aveMeasData = measData;
        items.get(cycle - 1).unit = unit;
        table.refresh();
    }

    @FXML
    void startOn(ActionEvent event) {
        log.info("start metrology");

        table.getItems().clear();

        int cycleAmount;
        try {
            cycleAmount = Integer.parseInt(this.cycleAmount.getText());
            log.info("cycleAmount: {}", cycleAmount);
        } catch (Exception e) {
            log.error("startMetrology cycleAmount parsing", e);
            showParsingErrorDialog("Количество циклов");
            return;
        }

        int measAmount;
        try {
            measAmount = Integer.parseInt(this.measAmount.getText());
            log.info("measAmount: {}", measAmount);
        } catch (Exception e) {
            log.error("startMetrology measAmount parsing", e);
            showParsingErrorDialog("Количество измерений одного цикла");
            return;
        }

        String selectedItem = realMeasDataUnit.getSelectionModel().getSelectedItem();
        double coef = parsingValuePrefix(selectedItem);

        float realMeasData;
        try {
            realMeasData = Float.parseFloat(this.realMeasData.getText());
        } catch (Exception e) {
            log.error("startMetrology measAmount parsing", e);
            showParsingErrorDialog("Действительное значение");
            return;
        }
        log.info(String.format("realMeasData: %f", realMeasData));

        float realMeasDataInBaseUnit = (float) (realMeasData * coef);
        log.info(String.format("realMeasDataInBaseUnit: %f", realMeasDataInBaseUnit));

        selectedItem = backgroundMeasDataUnit.getSelectionModel().getSelectedItem();
        coef = parsingValuePrefix(selectedItem);

        float backgroundMeasData = 0.0f;
        if (!this.backgroundMeasData.getText().isEmpty()) {
            try {
                backgroundMeasData = Float.parseFloat(this.backgroundMeasData.getText());
            } catch (Exception e) {
                log.error("startMetrology measAmount parsing", e);
                showParsingErrorDialog("Фоновое значение");
                return;
            }
        }
        log.info(String.format("backgroundMeasData: %f", backgroundMeasData));

        float backgroundMeasDataInBaseUnit = (float) (backgroundMeasData * coef);
        log.info(String.format("backgroundMeasDataInBaseUnit: %f", backgroundMeasDataInBaseUnit));

        startBackgroundBtn.setDisable(true);
        startMetrology(cycleAmount, measAmount, realMeasDataInBaseUnit, backgroundMeasDataInBaseUnit, getInputRange().getCode());
    }

    protected abstract void startMetrology(int cycleAmount, int measAmount, float realMeasData, float background, int range);

    @FXML
    void startBackgroundOn(ActionEvent event) {
        log.info("start background");

        int measTime;
        try {
            measTime = Integer.parseInt(this.measTime.getText());
            log.info("measAmount: {}", measAmount);
        } catch (Exception e) {
            log.error("startMetrology measAmount parsing", e);
            showParsingErrorDialog("Экспозиция");
            return;
        }

        startBtn.setDisable(true);
        startBackground(measTime, getInputRange().getCode());
    }

    protected abstract void startBackground(int measTime, int ramge);

    @FXML
    void stopBackgroundOn(ActionEvent event) {
        startBtn.setDisable(false);
        startBackgroundBtn.setDisable(false);

        stopBackground();
    }

    protected abstract void stopBackground();

    protected void showParsingErrorDialog(String field) {
        rootController.getMainWindow().showDialog(ERROR, "Ошибка",
                String.format(ERROR_PARSE_FILED, field),
                ERROR_PARSE_TITLE);
    }

    protected void showParsingErrorDialog(String field, String msg) {
        rootController.getMainWindow().showDialog(ERROR, "Ошибка",
                String.format(ERROR_PARSE_FILED, field),
                msg);
    }

    public void putStateParam() {
        rootController.getAppState().putParam(CYCLE_AMOUNT_STATE_APP_PARAM, cycleAmount.getText());
        rootController.getAppState().putParam(MEAS_AMOUNT_STATE_APP_PARAM, measAmount.getText());
        rootController.getAppState().putParam(REAL_MEAS_DATA_STATE_APP_PARAM, realMeasData.getText());
        rootController.getAppState().putParam(REAL_MEAS_DATA_UNIT_STATE_APP_PARAM, String.valueOf(realMeasDataUnit.getSelectionModel().getSelectedIndex()));
        rootController.getAppState().putParam(BACKGROUND_MEAS_DATA_STATE_APP_PARAM, backgroundMeasData.getText());
        rootController.getAppState().putParam(BACKGROUND_MEAS_DATA_UNIT_STATE_APP_PARAM, String.valueOf(backgroundMeasDataUnit.getSelectionModel().getSelectedIndex()));
        rootController.getAppState().putParam(RANGE_STATE_APP_PARAM, String.valueOf(inputRange.getSelectionModel().getSelectedIndex()));
        rootController.getAppState().putParam(BACKGROUND_MEAS_TIME_STATE_APP_PARAM, measTime.getText());
    }

    public void enableAllBtn() {
        startBtn.setDisable(false);
        startBackgroundBtn.setDisable(false);
    }
}
