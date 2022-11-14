module com.example.jsit_desktop {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires java.desktop;
    requires gson;
    requires pdfbox.app;

    opens com.application.jsit_desktop to javafx.fxml;
    exports com.application.jsit_desktop;
    opens controllers to javafx.fxml;
    exports controllers;
    opens models to javafx.fxml;
    exports models;

}