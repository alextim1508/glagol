package com.alextim.glagol.frontend.view;


import com.alextim.glagol.RootController;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

import static com.alextim.glagol.frontend.MainWindow.ROOT_KEY;


public abstract class NodeController implements Initializable {

    protected RootController rootController;

    protected abstract String getName();

    public void adopt(RootController rootController, String name, NodeController controller) {
        this.rootController = rootController;
        this.rootController.addChild(name, controller);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        RootController root = (RootController) resources.getObject(ROOT_KEY);
        adopt(root, getName(), this);
    }
}