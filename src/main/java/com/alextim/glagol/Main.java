package com.alextim.glagol;

import com.alextim.glagol.client.ucan.CanTransfer;
import com.alextim.glagol.context.AppState;
import com.alextim.glagol.frontend.MainWindow;
import com.alextim.glagol.service.StatisticMeasService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

import static com.alextim.glagol.context.Property.TITLE_APP;


@Slf4j
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        log.info("start");
        System.setProperty("file.encoding", "UTF-8");

        MainWindow mainWindow = new MainWindow(stage);

        AppState appState = new AppState(new File(System.getProperty("user.dir") + "/AppParams.txt"));

        CanTransfer canTransfer = new CanTransfer();

        StatisticMeasService statisticMeasService = new StatisticMeasService();

        RootController rootController = new RootController(
                mainWindow,
                canTransfer,
                statisticMeasService,
                appState);

        Thread thread = new Thread(() -> {
            Platform.runLater(() -> {
                AnchorPane mainWindowPane = mainWindow.createMainWindow(rootController);

                initStage(stage,
                        mainWindowPane,
                        mainWindow.getIconImage(),
                        rootController);
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void initStage(Stage stage,
                           AnchorPane rootPane,
                           Image icon,
                           RootController rootController) {
        stage.hide();
        stage.setMaximized(true);

        Scene scene = new Scene(rootPane);
        scene.setOnKeyPressed(rootController::onKeyEvent);

        stage.setScene(scene);
        stage.setTitle(TITLE_APP);
        stage.getIcons().add(icon);

        stage.setOnShowing(event -> {
            log.info("showing callback");
            rootController.listenDetectorClient();
        });

        stage.setOnCloseRequest(handler -> {
            log.info("shutdown callback");

            rootController.close();
            log.info("Root controller is closed");

            log.info("call Platform.exit");
            Platform.exit();

            log.info("call System.exit 0");
            System.exit(0);
        });

        stage.show();
    }
}