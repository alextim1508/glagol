package com.alextim.glagol;


import com.alextim.glagol.client.MessageReceiver;
import com.alextim.glagol.client.SomeMessage;
import com.alextim.glagol.context.AppState;
import com.alextim.glagol.frontend.MainWindow;
import com.alextim.glagol.frontend.view.NodeController;
import com.alextim.glagol.frontend.view.data.DataController;
import com.alextim.glagol.frontend.view.magazine.MagazineController;
import com.alextim.glagol.frontend.view.management.ManagementController;
import com.alextim.glagol.frontend.view.metrology.MetrologyController;
import com.alextim.glagol.service.ExportService;
import com.alextim.glagol.service.MetrologyMeasService;
import com.alextim.glagol.service.StatisticMeasService;
import com.alextim.glagol.service.StatisticMeasService.StatisticMeasurement;
import com.alextim.glagol.service.message.CommandMessages.CommandMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@RequiredArgsConstructor
public class RootControllerInitializer {

    protected final MainWindow mainWindow;

    protected final MessageReceiver transfer;

    protected final StatisticMeasService statisticMeasService;

    protected final MetrologyMeasService metrologyMeasService;

    protected final ExportService exportService;

    protected final AppState appState;

    protected final ConcurrentLinkedQueue<StatisticMeasurement> statisticMsg = new ConcurrentLinkedQueue<>();

    protected final LinkedList<CommandMessage> waitingCommands = new LinkedList<>();

    protected Future<?> connectTimer;
    protected final AtomicLong lastReceivedMsgTime = new AtomicLong();

    protected final ExecutorService executorService = Executors.newCachedThreadPool();

    protected final Map<String, NodeController> children = new HashMap<>();

    public void addChild(String name, NodeController child) {
        children.put(name, child);
    }

    public NodeController getChild(String name) {
        return children.get(name);
    }

    public File showFileChooseDialog() {
        return mainWindow.showFileChooseDialog();
    }

    protected DataController getDataController() {
        return (DataController) getChild(DataController.class.getSimpleName());
    }

    protected ManagementController getManagementController() {
        return (ManagementController) getChild(ManagementController.class.getSimpleName());
    }

    protected MagazineController getMagazineController() {
        return (MagazineController) getChild(MagazineController.class.getSimpleName());
    }

    protected MetrologyController getMetrologyController() {
        return (MetrologyController) getChild(MetrologyController.class.getSimpleName());
    }
}
