package com.alextim.glagol.frontend.view.data;

import com.alextim.glagol.frontend.view.NodeController;
import com.alextim.glagol.frontend.widget.GraphWidget;
import com.alextim.glagol.frontend.widget.graphs.SimpleGraph;
import com.alextim.glagol.service.StatisticMeasService;
import com.alextim.glagol.service.protocol.Range;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.alextim.glagol.context.Property.AVERAGE_COUNTER_NUMBER_FORMAT;
import static com.alextim.glagol.frontend.MainWindow.PROGRESS_BAR_COLOR;

@Slf4j
public abstract class DataControllerInitializer extends NodeController {

    @FXML
    private SplitPane splitPane;
    @FXML
    private AnchorPane graphPane;

    @FXML
    private TextField measTime;

    @FXML
    private Label currentDoseRate, averageDoseRate, accumulatedDoseRate;
    @FXML
    private Label currentMeasTime;

    @FXML
    private Label currentRangeLabel;

    @FXML
    private Label progressLabel;
    @FXML
    private ProgressBar progressBar;

    @FXML
    private TextField fileComment;

    @FXML
    private ComboBox<Range> inputRange;

    @FXML
    private TableView<TableRow> table;
    @FXML
    private TableColumn<TableRow, String> countTitle;
    @FXML
    private TableColumn<TableRow, Long> accumulatedCount;
    @FXML
    private TableColumn<TableRow, Float> averageCount;
    @FXML
    private TableColumn<TableRow, Integer> currentCount;

    @FXML
    private Button startBtn, stopBtn;

    @FXML
    private ImageView imageView;
    @FXML
    private Label imageViewLabel;

    private Image greenCircleImage, grayCircleImage, redCircleImage;

    protected GraphWidget graphWidget;
    protected SimpleGraph currentDoseRateGraph;
    protected SimpleGraph averageDoseRateGraph;

    abstract void start(long measTime, int range);

    abstract void stop();

    abstract void save();

    abstract void clear();

    private final String MEAS_TIME_STATE_APP_PARAM = "data.measTime";
    private final String COMMENT_STATE_APP_PARAM = "data.comment";

    protected final String CURRENT_GRAPH_LABEL_FORMAT = "%s. Счета: %d %d %d %d";
    protected final String AVERAGE_GRAPH_LABEL_FORMAT = "%s. Счета: " + AVERAGE_COUNTER_NUMBER_FORMAT + " " +
                                                                        AVERAGE_COUNTER_NUMBER_FORMAT + " " +
                                                                        AVERAGE_COUNTER_NUMBER_FORMAT + " " +
                                                                        AVERAGE_COUNTER_NUMBER_FORMAT;

    int graphIndex;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        initPane();
        initChart();

        tableInitialize();

        fullTable();
        measTimeInit();
        commentInit();
        initProgressBar();
        inputRangeInit();

        addGraph();

        loadIcons();
    }

    @SneakyThrows
    private void loadIcons() {
        @Cleanup
        InputStream greenCircleAsStream = DataControllerInitializer.class.getResourceAsStream("circle-green.png");
        greenCircleImage = new Image(Objects.requireNonNull(greenCircleAsStream));

        @Cleanup
        InputStream grayCircleAsStream = DataControllerInitializer.class.getResourceAsStream("circle-gray.png");
        grayCircleImage = new Image(Objects.requireNonNull(grayCircleAsStream));

        @Cleanup
        InputStream redCircleAsStream = DataControllerInitializer.class.getResourceAsStream("circle-red.png");
        redCircleImage = new Image(Objects.requireNonNull(redCircleAsStream));
    }

    private void initPane() {
        splitPane.setDividerPositions(0.7f);
    }

    private void initChart() {
        graphWidget = new GraphWidget("Значение");
        AnchorPane spectrumPane = graphWidget.getPane();
        graphPane.getChildren().add(spectrumPane);
        AnchorPane.setTopAnchor(spectrumPane, 5.0);
        AnchorPane.setLeftAnchor(spectrumPane, 5.0);
        AnchorPane.setRightAnchor(spectrumPane, 60.0);
        AnchorPane.setBottomAnchor(spectrumPane, 5.0);
    }

    private void addGraph() {
        currentDoseRateGraph = new SimpleGraph(new SimpleStringProperty("Текущая МАЭД"), null);
        averageDoseRateGraph = new SimpleGraph(new SimpleStringProperty("Усредненная за время экспозиции МАЭД"), null);

        graphWidget.addGraph(currentDoseRateGraph);
        graphWidget.addGraph(averageDoseRateGraph);
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

    private void tableInitialize() {
        countTitle.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().countTitle));

        accumulatedCount.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().accumulatedCount));

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

        table.setItems(FXCollections.observableArrayList());
    }

    @AllArgsConstructor
    private static class TableRow {
        public String countTitle;
        public long accumulatedCount;
        public float averageCount;
        public int currentCount;
    }

    private void fullTable() {
        table.getItems().addAll(
                new TableRow("Счетчик 1", 0, 0, 0),
                new TableRow("Счетчик 2", 0, 0, 0),
                new TableRow("Счетчик 3", 0, 0, 0),
                new TableRow("Счетчик 4", 0, 0, 0),
                new TableRow("Всего", 0, 0, 0)
        );
    }

    public void updateTable(StatisticMeasService.StatisticMeasurement meas) {
        ObservableList<TableRow> items = table.getItems();

        items.get(0).accumulatedCount = meas.accumulatedCounts[0];
        items.get(0).averageCount = meas.averageCounts[0];
        items.get(0).currentCount = meas.currentCounts[0];

        items.get(1).accumulatedCount = meas.accumulatedCounts[1];
        items.get(1).averageCount = meas.averageCounts[1];
        items.get(1).currentCount = meas.currentCounts[1];

        items.get(2).accumulatedCount = meas.accumulatedCounts[2];
        items.get(2).averageCount = meas.averageCounts[2];
        items.get(2).currentCount = meas.currentCounts[2];

        items.get(3).accumulatedCount = meas.accumulatedCounts[3];
        items.get(3).averageCount = meas.averageCounts[3];
        items.get(3).currentCount = meas.currentCounts[3];


        items.get(4).accumulatedCount = meas.accumulatedCounts[4];
        items.get(4).averageCount = meas.averageCounts[4];
        items.get(4).currentCount = meas.currentCounts[4];

        table.refresh();
    }

    public void clearTable() {
        ObservableList<TableRow> items = table.getItems();
        for (TableRow item : items) {
            item.accumulatedCount = 0;
            item.averageCount = 0;
            item.currentCount = 0;
        }
        table.refresh();
    }

    private void initProgressBar() {
        progressBar.setStyle("-fx-accent: " + PROGRESS_BAR_COLOR);
    }

    public void setProgress(double progress) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            progressLabel.setText(String.format(Locale.US, " %.1f%%", 100 * progress));
        });
    }

    public void setCurrentRage(int range) {
        Platform.runLater(() -> {
            currentRangeLabel.setText(String.valueOf(range));
        });
    }

    protected Range getInputRange() {
        return inputRange.getSelectionModel().getSelectedItem();
    }

    private void measTimeInit() {
        String param = rootController.getAppState().getParam(MEAS_TIME_STATE_APP_PARAM);
        measTime.setText(Objects.requireNonNullElseGet(param, () -> String.valueOf(60)));
    }

    protected int getMeasTime() {
        return Integer.parseInt(measTime.getText());
    }

    private void commentInit() {
        String param = rootController.getAppState().getParam(COMMENT_STATE_APP_PARAM);
        if (param != null) {
            fileComment.setText(param);
        }
    }

    public String getFileComment() {
        return fileComment.getText();
    }

    public void setDoseRates(String currentDoseRate, String averageDoseRate, String accumulatedDoseRate) {
        log.info("Dose rates show. CurrentDoseRate: {} AverageDoseRate: {} AccumulatedDoseRate: {}",
                currentDoseRate, averageDoseRate, accumulatedDoseRate);
        Platform.runLater(() -> {
            this.currentDoseRate.setText(currentDoseRate);
            this.averageDoseRate.setText(averageDoseRate);
            this.accumulatedDoseRate.setText(accumulatedDoseRate);
        });
    }

    public void setMeasTime(String text) {
        Platform.runLater(() -> currentMeasTime.setText(text));
    }

    public void setRedCircle() {
        Platform.runLater(() -> imageView.setImage(redCircleImage));
    }

    public void setGreenCircle() {
        Platform.runLater(() -> imageView.setImage(greenCircleImage));
    }

    public void setGrayCircle() {
        Platform.runLater(() -> imageView.setImage(grayCircleImage));
    }

    public void setEmptyCircle() {
        Platform.runLater(() -> imageView.setImage(null));
    }

    public void clearGraphAndTableData() {
        graphIndex = 0;

        setProgress(0);

        currentDoseRateGraph.clear();
        averageDoseRateGraph.clear();
        clearTable();
    }

    public void enableStartStopBtn(boolean isStartBtnEnabled) {
        startBtn.setDisable(!isStartBtnEnabled);
        stopBtn.setDisable(isStartBtnEnabled);
    }

    @FXML
    void onConnectToDetector(ActionEvent event) {
        start(getMeasTime(), getInputRange().getCode());
    }

    @FXML
    void onDisconnectFromDetector(ActionEvent event) {
        stop();
    }

    @FXML
    void onSave(ActionEvent event) {
        save();
    }

    @FXML
    void onClear(ActionEvent event) {
        clear();
    }

    public void putStateParam() {
        rootController.getAppState().putParam(MEAS_TIME_STATE_APP_PARAM, measTime.getText());
        rootController.getAppState().putParam(COMMENT_STATE_APP_PARAM, fileComment.getText());
    }
}