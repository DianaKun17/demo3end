package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    public static Stage ps;
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("prop.fxml"));
        Parent parent = loader.load();
        primaryStage.setScene(new Scene(parent));
        ps = primaryStage;
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }
}
