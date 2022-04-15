import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    HBox msgPanel, loginPanel;

    @FXML
    TextField msgField, usernameField;

    @FXML
    TextArea textArea;

    @FXML
    ListView<String> clientsList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public void login() {
        if(socket == null || socket.isClosed()) {
            connect();
        }

        if(usernameField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Имя пользователя не может быть пустым", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        try {
            out.writeUTF("/login " + usernameField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setName(String username) {
        this.username = username;
        if(username != null) {
            loginPanel.setVisible(false);
            loginPanel.setManaged(false);
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
            clientsList.setVisible(true);
            clientsList.setManaged(true);
        } else {
            loginPanel.setVisible(true);
            loginPanel.setManaged(true);
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
            clientsList.setVisible(false);
            clientsList.setManaged(false);
        }
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread t1 = new Thread(() -> {

                try {
                    while (true) {
                        String msg = in.readUTF();
                        if(msg.startsWith("/login_ok ")) {
                            setName(msg.split("\\s")[1]);
                            break;
                        }
                        if(msg.startsWith("/login_failed ")) {

                            String cause = msg.split("\\s", 2)[1];
                            textArea.appendText(cause + "\n");
                        }
                    }
                    while (true) {
                        String msg = in.readUTF();
                        if(msg.startsWith("/")){
                            if(msg.startsWith("/clients_list ")){
                                String[] tokens = msg.split("\\s");
                                Platform.runLater(()->{
                                    clientsList.getItems().clear();
                                    for (int i =1; i < tokens.length; i++){
                                        clientsList.getItems().add(tokens[i]);
                                    }
                                });
                            }
                            continue;
                        }
                        textArea.appendText(msg + "\n");
                    }
                }catch (IOException e){
                    e.printStackTrace();
                } finally {
                    disconnect();
                }
            });
            t1.start();

        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to server 8189");
        }
    }



    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgPanel.requestFocus();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to send msg", ButtonType.OK);
            alert.showAndWait();
            System.out.println();
        }
    }

    public void disconnect() {
        setName(null);
        try {
            if (socket != null) {
                socket.close();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }




    @Override
    public void initialize(URL location, ResourceBundle resources) {setName(null);}

    }

