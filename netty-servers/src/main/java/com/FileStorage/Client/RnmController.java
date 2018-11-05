package com.FileStorage.Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class RnmController {

    @FXML
    TextField name;

    @FXML
    VBox globParent;

    private String newName;

    public void rnm(ActionEvent actionEvent) throws IOException {
        String nn = name.getText();

        if(nn != "") {
            newName = nn;
            globParent.getScene().getWindow().hide();
        }


    }

    public String getNewName() {
        return newName;
    }
}
