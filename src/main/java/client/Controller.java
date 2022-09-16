package client;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.SneakyThrows;
import common.DeleteFileMessage;
import common.FileMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.ResourceBundle;

@Getter
public class Controller implements Initializable {

    @FXML
    public ListView<String> serverView;
    public ListView<String> clientView;
    public TextArea serverText;
    public TextArea clientText;
    public TextArea clientCurrentFolder;
    public TextArea serverCurrentFolder;
    public MenuItem reg;
    public MenuItem exit;

    private String clientFiles = "clientFiles";
    private String HOST = "localhost";
    private int PORT = 45003;

    public static Network network;
    public String userName;
    public String password;
    public String userId;

    private FileChooser fileChooser;
    private FileMessage fileMessage;

    private File currentDirectoryPath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser = new FileChooser();

        network = new Network(
                (FileMessage fm) -> {
                    Files.copy(fm.getFile().toPath(), new File(currentDirectoryPath + "\\" + fm.getName()).toPath());
                    Platform.runLater(this::refresh);
                },

                (String[] list) -> Platform.runLater(() -> {
                    if (list[0].equals("/registration") || list[0].equals("/login")) {
                        userName = list[1];
                        password = list[2];
                        userId = list[3];
                        refresh();
                    } else if (list[0].equals("/refresh")) {
                        getServerView().getItems().clear();
                        getServerView().getItems().addAll(Arrays.copyOfRange(list, 1, list.length));
                    }
                }));

        File temp = new File(clientFiles);
        currentDirectoryPath = new File(temp.getAbsolutePath());
        if (!currentDirectoryPath.exists()) currentDirectoryPath.mkdir();

        addViewListener(serverView, serverText);
        addViewListener(clientView, clientText);
        addDialogActionListener();
        setDoubleClickListener();

    }

    private void addViewListener(ListView<String> lv, TextArea ta) {
        lv.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends String> ov, String old_val, String new_val) -> {
                    try {
                        ta.clear();
                        ta.appendText(lv.getSelectionModel().getSelectedItem());
                    } catch (NullPointerException ignored) {
                    }
                });
    }

    public void download(ActionEvent event) {
        if (!serverText.getText().equals("")) {
            network.sendMsg("/download " + serverText.getText() + " " + userId);
        }
        refresh();
    }

    public void send(ActionEvent actionEvent) {
        if (fileMessage != null && fileMessage.getFile() != null) {
            network.send(fileMessage);
        } else if (fileMessage == null && !clientText.getText().equals("")) {
            fileMessage = new FileMessage();
            File toSend = new File(currentDirectoryPath.getAbsolutePath() + "\\" + clientText.getText());
            fileMessage.setFile(toSend);
            fileMessage.setName(toSend.getName());
            fileMessage.setSize(toSend.length());
            fileMessage.setFileOwner(userId);
            network.send(fileMessage);
            fileMessage = null;
        }
        refresh();
    }

    public void deleteFileFromClient(ActionEvent actionEvent) {
        if (!clientText.getText().equals("")) {
            File delete = new File(clientCurrentFolder.getText() + "\\" + clientText.getText());
            delete.delete();
        }
        refresh();
    }

    public void deleteFileFromServer(ActionEvent actionEvent) {
        if (!serverText.getText().equals("")) {
            network.delete(new DeleteFileMessage(serverText.getText()));
        }
        refresh();
    }

    public void chose(ActionEvent event) {
        fileMessage = new FileMessage();
        fileChooser.setTitle("Chose file");
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("All files", "*.html", "*.jpg",
                        "*.jfif", "*.png", "*.txt", "*.mpeg4", "*.mp3", "*.wav", "*.docx", "*.xlsx", "*.xls");
        fileChooser.getExtensionFilters().add(extFilter);

        fileMessage.setFile(
                new File(String.valueOf(
                        fileChooser.showOpenDialog(clientText.getScene().getWindow()))));
        fileMessage.setName(fileMessage.getFile().getName());
        fileMessage.setSize(fileMessage.getFile().length());
        fileMessage.setFileOwner(userId);
        setClientText(fileMessage.getName());
    }

    private void setClientText(String text) {
        clientText.clear();
        clientText.setText(text);
    }

    public void refresh() {
        network.sendMsg("/refresh " + userId);
        refreshClient();
    }

    private void refreshClient() {
        clientView.getItems().clear();
        clientView.getItems().addAll(currentDirectoryPath.list());

        clientCurrentFolder.clear();
        clientCurrentFolder.appendText(currentDirectoryPath.getAbsolutePath());
    }

    public void dirRight(ActionEvent event) {
        if (Paths.get(currentDirectoryPath.getAbsolutePath() +
                "\\" + clientText.getText()).toFile().isDirectory()) {

            currentDirectoryPath = Paths.get(currentDirectoryPath + "\\" + clientText.getText()).toFile();
            refreshClient();
        }
    }

    public void dirLeft(ActionEvent event) {
        if (currentDirectoryPath.getParent() != null) {
            currentDirectoryPath = new File(currentDirectoryPath.getParent());
        } else {
            currentDirectoryPath = new File(System.getProperty("user.home"));
        }
        refreshClient();
    }

    public void enterPath(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            try {
                currentDirectoryPath = new File(clientCurrentFolder.getText());
                refreshClient();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addDialogActionListener() {
        reg.setOnAction(
                new EventHandler<ActionEvent>() {
                    @SneakyThrows
                    @Override
                    public void handle(ActionEvent event) {
                        final Stage dialog = new Stage();
                        dialog.initModality(Modality.APPLICATION_MODAL);
                        dialog.initOwner(App.ps);
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("popup.fxml"));
                        Parent parent = loader.load();
                        dialog.setScene(new Scene(parent));
                        dialog.show();
                    }
                });
    }

    public void registration(ActionEvent event) {
        addDialogActionListener();
    }

    public void exit(ActionEvent event) {
        String propsPath = System.getProperty("user.home") + "/cloud-storage/prop.properties";
        File props = new File(propsPath);

        Properties prop = new Properties();

        prop.setProperty("login", "");
        prop.setProperty("password", "");
        try {
            FileOutputStream fos = new FileOutputStream(props);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        userName = "";
        password = "";
        userId = "0";

        refresh();
    }

    public void setDoubleClickListener() {
        clientView.setOnMouseClicked(event -> {
            if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                if (new File(
                        clientCurrentFolder.getText() +
                                "\\" +
                                clientView.getSelectionModel().getSelectedItem()).isDirectory()) {
                    dirRight(new ActionEvent());
                }
            }
        });
    }
}
