package com.alextim.glagol.frontend.view.magazine;

import com.alextim.glagol.client.SomeMessage;
import com.alextim.glagol.frontend.view.NodeController;
import com.alextim.glagol.service.message.AlarmMessages.AlarmEvent;
import com.alextim.glagol.service.message.CommandMessages.AnswerMessage;
import com.alextim.glagol.service.message.CommandMessages.CommandMessage;
import com.alextim.glagol.service.message.MeasurementMessages.MeasEvent;
import com.alextim.glagol.service.message.ProcessingMessages.ProcessingErrorMessage;
import javafx.scene.layout.AnchorPane;

import java.util.ResourceBundle;


import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ScrollEvent;

import java.io.File;
import java.net.URL;

import static com.alextim.glagol.client.SomeMessage.formatDataAsHex;
import static com.alextim.glagol.context.Property.QUEUE_CAPACITY;
import static com.alextim.glagol.service.protocol.MessageCategory.*;

public class MagazineController extends NodeController {

    @FXML
    private AnchorPane pane;

    @FXML
    private TableView<SomeMessage> table;
    @FXML
    private TableColumn<SomeMessage, Integer> id;
    @FXML
    private TableColumn<SomeMessage, String> time;
    @FXML
    private TableColumn<SomeMessage, String> type;
    @FXML
    private TableColumn<SomeMessage, String> comEvAns;
    @FXML
    private TableColumn<SomeMessage, String> message;
    @FXML
    private TableColumn<SomeMessage, String> data;

    @Override
    protected String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        initTable();
        paneInit();
    }

    private void paneInit() {
        /* bug JavaFX. Other tabs of tabPane get ScrollEvent from current tab*/
        pane.addEventHandler(ScrollEvent.ANY, Event::consume);
    }

    private void initTable() {
        id.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().id));
        id.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(String.format("0x%04X", item));
                }
            }
        });

        time.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(String.valueOf(param.getValue().time)));
        id.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                if (item == null || item == 0 || empty) {
                    setText(null);
                } else {
                    setText(String.format("0x%04X", item));
                }
            }
        });

        type.setCellValueFactory(param -> {
            if (param.getValue() instanceof AlarmEvent) {
                return new ReadOnlyObjectWrapper<>(ALARM.getDescription());
            } else if (param.getValue() instanceof CommandMessage) {
                return new ReadOnlyObjectWrapper<>(CONTROL.getDescription());
            } else if (param.getValue() instanceof AnswerMessage) {
                return new ReadOnlyObjectWrapper<>(RESPONSE.getDescription());
            } else if (param.getValue() instanceof MeasEvent) {
                return new ReadOnlyObjectWrapper<>(MEASUREMENT.getDescription());
            } else if (param.getValue() instanceof ProcessingErrorMessage) {
                return new ReadOnlyObjectWrapper<>("Ошибка обработки сообщения");
            } else {
                return new ReadOnlyObjectWrapper<>("Не известное сообщение");
            }
        });

        comEvAns.setCellValueFactory(param -> {
            Object value = param.getValue();
            if (value instanceof AlarmEvent alarmEvent) {
                return new ReadOnlyObjectWrapper<>(alarmEvent.command.getDescription());

            } else if (value instanceof CommandMessage commandMessage) {
                return new ReadOnlyObjectWrapper<>(commandMessage.command.getDescription());

            } else if (value instanceof AnswerMessage answerMessage) {
                return new ReadOnlyObjectWrapper<>(answerMessage.command.getDescription() + ". " + answerMessage.commandStatus.getDescription());

            } else if (value instanceof MeasEvent measEvent) {
                return new ReadOnlyObjectWrapper<>(measEvent.type.getDescription());

            } else if (value instanceof ProcessingErrorMessage) {
                return new ReadOnlyObjectWrapper<>("Ошибка обработки сообщения");

            } else {
                return new ReadOnlyObjectWrapper<>("Не известно");
            }
        });

        message.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().toString()));

        data.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(formatDataAsHex(param.getValue().data)));

        table.setPlaceholder(new Label(""));
        table.setItems(FXCollections.observableArrayList());
    }

    public void addLog(SomeMessage msg) {
        table.getItems().add(0, msg);
        if (table.getItems().size() > QUEUE_CAPACITY) {
            table.getItems().remove(table.getItems().size() - 1);
        }
    }

    public void clearTable() {
        table.getItems().clear();
    }
}