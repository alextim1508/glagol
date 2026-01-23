package com.alextim.glagol.frontend.view.coefs;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.ResourceBundle;

public class CoefsView {
    public Pane getView(ResourceBundle bundle) {
        try {
            return FXMLLoader.<AnchorPane>load(CoefsView.class.getResource("Coefs.fxml"), bundle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}