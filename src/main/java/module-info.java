module com.alextim.glagol {
    requires static lombok;
    requires org.slf4j;

    requires javafx.controls;
    requires javafx.fxml;
    requires com.sun.jna;
    requires de.gsi.chartfx.dataset;
    requires de.gsi.chartfx.chart;

    exports com.alextim.glagol.client.ucan.structure to com.sun.jna;
    exports com.alextim.glagol.client.ucan.callback to com.sun.jna;


    opens com.alextim.glagol.frontend.view.data to javafx.fxml;
    opens com.alextim.glagol.frontend.view.management to javafx.fxml;
    opens com.alextim.glagol.frontend.view.magazine to javafx.fxml;
    opens com.alextim.glagol.frontend.view.metrology to javafx.fxml;
    opens com.alextim.glagol.frontend.dialog.progress to javafx.fxml;
    opens com.alextim.glagol.frontend.dialog.error to javafx.fxml;

    exports com.alextim.glagol;
}