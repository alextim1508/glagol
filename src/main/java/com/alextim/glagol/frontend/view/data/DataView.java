package com.alextim.glagol.frontend.view.data;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.ResourceBundle;

public class DataView {
    public Pane getView(ResourceBundle bundle) {
        try {
            return FXMLLoader.<AnchorPane>load(DataView.class.getResource("DataView.fxml"), bundle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
