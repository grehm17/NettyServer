package com.FileStorage.Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ShareController {

    @FXML
    TextField name;

    @FXML
    VBox globParent;

    private String usrName;

    public void share(ActionEvent actionEvent) throws IOException {
        String nn = name.getText();

        if(nn != "") {
            usrName = nn;
            globParent.getScene().getWindow().hide();
        }


    }

    public String getUsrName() {
        return usrName;
    }
}
