module org.furniture {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.furniture to javafx.fxml;
    exports org.furniture;
}
