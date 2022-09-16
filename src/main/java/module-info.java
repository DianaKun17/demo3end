module com.example.demo3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.all;
    requires lombok;
    requires java.sql;


    opens client to javafx.fxml;
    exports client;
}