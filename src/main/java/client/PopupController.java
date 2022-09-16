package client;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class PopupController implements Initializable {

    public PasswordField password;
    public TextField login;
    public CheckBox checkBox;

    private Properties prop;
    private File file;
    public File dir;
    private String filePath;


    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void register(ActionEvent event) {
        if (login.getText().length() >= 5 && password.getText().length() >= 5) {
            Controller.network.sendMsg("/registration " + login.getText() + " " + password.getText());
            stayInSystem();
        } else {
            showAlert();
        }
    }

    public void login(ActionEvent event) {
        if (login.getText().length() >= 5 && password.getText().length() >= 5) {
            Controller.network.sendMsg("/login " + login.getText() + " " + password.getText());
            stayInSystem();
        } else {
            showAlert();
        }
    }

    private void stayInSystem() {
        if (checkBox.isSelected()) {
            prop = new Properties();
            dir = new File(System.getProperty("user.home") + "/" + "cloud-storage");
            filePath = System.getProperty("user.home") + "/" + dir.getName() + "/prop.properties";
            if (!dir.exists()) {
                dir.mkdir();
            }
            try {
                file = new File(filePath);
                if (!file.exists()) {
                    file.createNewFile();
                }
                prop.put("login", login.getText());
                prop.put("password", password.getText());
                storeProp();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void storeProp() {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            prop.store(fos, "");
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");
        alert.setHeaderText("Password or login error");
        alert.setContentText("Password and login could not be less then 5 characters!");
        alert.showAndWait();
    }
}
