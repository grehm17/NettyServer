package com.FileStorage.Client;

import com.FileStorage.Common.CmdID;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class MoveController {

    @FXML
    TableView<PathLine> serverPaths;

    @FXML
    VBox globParent;

    private TableView.TableViewSelectionModel<PathLine> selectionModels;

    private Controller parentCntr;
    private String CurrServFolder;
    private String source;

    public void setContext(String ServFolder,String source, Controller cntr){

        this.parentCntr = cntr;
        this.CurrServFolder = ServFolder;
        this.source = source;
        init();

    }

    public void init() {

        initServerPaths();

        selectionModels = serverPaths.getSelectionModel();

    }


    public void initServerPaths(){

        TableColumn<PathLine, String> tcType = new TableColumn<>("Type");
        tcType.setCellValueFactory(new PropertyValueFactory<PathLine, String>("type"));

        tcType.setCellFactory(column -> new TableCell<PathLine, String>() {
            private ImageView imageViewC = new ImageView();

            {
                // initialize ImageView + set as graphic
                imageViewC.setFitWidth(20);
                imageViewC.setFitHeight(20);
                setGraphic(imageViewC);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    // no image for empty cells
                    imageViewC.setImage(null);
                } else {
                    // set image for non-empty cell
                    imageViewC.setImage(item == "A" ? parentCntr.arrowImg : item == "D" ? parentCntr.folderImg : parentCntr.fielImg);

                }
            }
        });

        TableColumn<PathLine, String> tcName = new TableColumn<>("Name");
        tcName.setCellValueFactory(new PropertyValueFactory<PathLine, String>("name"));

        TableColumn<PathLine, Long> tcSize = new TableColumn<>("Size");
        tcSize.setCellValueFactory(new PropertyValueFactory<PathLine, Long>("size"));

        serverPaths.getColumns().addAll(tcType,tcName, tcSize);
        updSPaths(CurrServFolder,(byte)3);

        serverPaths.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    if (serverPaths.getSelectionModel().getSelectedItem().getName() == "..."){

                        updSPaths(CurrServFolder,(byte)4);

                    }else if (serverPaths.getSelectionModel().getSelectedItem().isFolder()){


                        updSPaths(CurrServFolder+parentCntr.getSep(CurrServFolder)+serverPaths.getSelectionModel().getSelectedItem().getName(),(byte)3);

                    }

                }
            }
        });

    }

    public void updSPaths(String strPath,byte mode){

        ObservableList<PathLine> pathsList = parentCntr.rf.Execute(parentCntr.Skt,strPath,mode);

        CurrServFolder = pathsList.get(pathsList.size()-1).getName();
        pathsList.remove(pathsList.size()-1);

        serverPaths.setItems(pathsList);

    }

    public void btnMov(ActionEvent actionEvent) {

            PathLine sval = selectionModels.selectedItemProperty().getValue();

            parentCntr.CmdParams[0] = source;
            parentCntr.CmdParams[1] = CurrServFolder;

            boolean success = parentCntr.Mgr.setCmd(CmdID.Move);

            if (success){
                parentCntr.Mgr.activeCmd.Execute(parentCntr.Skt,parentCntr.CmdParams);
            }

        globParent.getScene().getWindow().hide();
        }

}
