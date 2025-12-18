package com.alextim.glagol.frontend.view.metrology;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.ResourceBundle;

public class MetrologyView {
    public Pane getView(ResourceBundle bundle) {
        try {
            return FXMLLoader.<AnchorPane>load(MetrologyView.class.getResource("MetrologyView.fxml"), bundle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
