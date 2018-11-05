package com.FileStorage.Client;

import com.FileStorage.Common.CmdID;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class LoginController {
    @FXML
    TextField login;

    @FXML
    PasswordField password;

    @FXML
    VBox globParent;

    private SocketChannel Skt;
    private Controller parentCntr;
    Alert alert = new Alert(Alert.AlertType.INFORMATION);

    public void setContext(SocketChannel Skt, Controller cntr){

        this.Skt = Skt;
        this.parentCntr = cntr;

    }

    public void reg(ActionEvent actionEvent) throws IOException {

        String log = login.getText();
        String pwd = password.getText();

        ByteBuffer msgBuff = ByteBuffer.allocate(1024);

        msgBuff.put(CmdID.Register.getBytes());
        msgBuff.putInt(log.length());
        msgBuff.put(log.getBytes());
        msgBuff.putInt(pwd.length());
        msgBuff.put(pwd.getBytes());
        msgBuff.flip();
        Skt.write(msgBuff);

        msgBuff.clear();

        Skt.read(msgBuff);

        msgBuff.flip();

        byte answ = msgBuff.get();

        String title = "";
        String message = "";

        if (answ == 1){
            title = "Success";
            message = "User registered successfully";
        }else if(answ == 3){
            title = "Error";
            message = "User with this name is already exist. Please enter unique name";
        }else if(answ == 4){
            title = "Error";
            message = "Unexpected data base error. Please try to reconnect";
        }

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();

    }

    public void auth(ActionEvent actionEvent) throws IOException {

        String log = login.getText();
        String pwd = password.getText();

        ByteBuffer msgBuff = ByteBuffer.allocate(1024);

        msgBuff.put(CmdID.Auth.getBytes());
        msgBuff.putInt(log.length());
        msgBuff.put(log.getBytes());
        msgBuff.putInt(pwd.length());
        msgBuff.put(pwd.getBytes());
        msgBuff.flip();
        Skt.write(msgBuff);

        msgBuff.clear();

        Skt.read(msgBuff);
        msgBuff.flip();
        int msgSize = msgBuff.getInt();
        byte[] msg = new byte[msgSize];
        msgBuff.get(msg);
        String StrMsg = new String(msg);

        if (StrMsg.equals("Denied")){

            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Invalid login or password");

            alert.showAndWait();

        }else{

            parentCntr.setServerFolder(StrMsg);
            globParent.getScene().getWindow().hide();

        }

    }
}
