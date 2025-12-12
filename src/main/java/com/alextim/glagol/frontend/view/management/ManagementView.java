package com.alextim.glagol.frontend.view.management;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.ResourceBundle;

public class ManagementView {
    public Pane getView(ResourceBundle bundle) {
        try {
            return FXMLLoader.<AnchorPane>load(ManagementView.class.getResource("ManagementView.fxml"), bundle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
