package com.alextim.glagol.context;

import com.alextim.glagol.RootController;
import com.alextim.glagol.client.ucan.CanTransfer;
import com.alextim.glagol.frontend.MainWindow;
import com.alextim.glagol.service.BackgroundMeasService;
import com.alextim.glagol.service.ExportService;
import com.alextim.glagol.service.MetrologyMeasService;
import com.alextim.glagol.service.StatisticMeasService;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static com.alextim.glagol.context.Property.*;

@Slf4j
public class Context {

    @Getter
    private RootController rootController;

    private CanTransfer canTransfer;
    private StatisticMeasService statisticMeasService;
    private MetrologyMeasService metrologyMeasService;
    private BackgroundMeasService backgroundMeasService;
    private ExportService exportService;
    private AppState appState;

    public Context(MainWindow mainWindow) {
        readAppProperty();

        createBeans(mainWindow);
    }

    @SneakyThrows
    private void readAppProperty() {
        Properties properties = new Properties();
        try {
            String file = System.getProperty("user.dir") + "./application.properties";
            log.info("properties file: {}", file);

            @Cleanup Reader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));

            properties.load(reader);

        } catch (Exception e) {
            log.info("There are default properties!");

            @Cleanup InputStream resourceAsStream = Context.class.getClassLoader()
                    .getResourceAsStream("application.properties");

            @Cleanup Reader resourceReader = new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8);

            properties.load(resourceReader);
        }

        initAppProperties(properties);
        initNumberFormatProperties(properties);
    }

    private void initAppProperties(Properties properties) {
        TITLE_APP = (String) properties.get("app.title");
        log.info("TITLE_APP: {}", TITLE_APP);

        QUEUE_CAPACITY =Integer.parseInt((String) properties.get("app.queue-capacity"));
        log.info("QUEUE_CAPACITY: {}", QUEUE_CAPACITY);
    }

    private void initNumberFormatProperties(Properties properties) {
        AVERAGE_COUNTER_NUMBER_FORMAT = (String) properties.get("app.view.average-counter-number-format");
        log.info("AVERAGE_COUNTER_NUMBER_FORMAT: {}", AVERAGE_COUNTER_NUMBER_FORMAT);

        MEAS_DATA_NUMBER_SING_DIGITS = Integer.parseInt((String) properties.get("app.view.meas-data-number-sign-digits"));
        log.info("MEAS_DATA_NUMBER_SING_DIGITS: {}", MEAS_DATA_NUMBER_SING_DIGITS);

        METROLOGY_ERROR_NUMBER_SING_DIGITS = Integer.parseInt((String) properties.get("app.view.metrology-error-number-sign-digits"));
        log.info("METROLOGY_ERROR_NUMBER_SING_DIGITS: {}", METROLOGY_ERROR_NUMBER_SING_DIGITS);
    }

    void createBeans(MainWindow mainWindow) {
        createStateApp();
        createServices();
        createRootController(mainWindow);
    }

    private void createStateApp() {
        appState = new AppState(new File(System.getProperty("user.dir") + "/AppParams.txt"));
        try {
            appState.readParam();
        } catch (Exception e) {
            log.error("ReadParam error", e);
        }
    }

    private void createServices() {
        canTransfer = new CanTransfer();

        statisticMeasService = new StatisticMeasService();

        metrologyMeasService = new MetrologyMeasService();

        backgroundMeasService = new BackgroundMeasService();

        exportService = new ExportService();
    }

    private void createRootController(MainWindow mainWindow) {
        log.info("Creating root controller");

        rootController =  new RootController(
                mainWindow,
                canTransfer,
                statisticMeasService,
                metrologyMeasService,
                backgroundMeasService,
                exportService,
                appState);
    }
}
