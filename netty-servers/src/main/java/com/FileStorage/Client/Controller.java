package com.FileStorage.Client;

import com.FileStorage.Common.CmdID;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.*;
import java.util.ResourceBundle;
import java.util.Comparator;

import com.FileStorage.Client.ClientCmd.*;
import com.FileStorage.Common.CmdManager;

public class Controller implements Initializable {

    private String foldName;
    public SocketChannel Skt;
    public  CmdManager Mgr  = new CmdManager();
    public String [] CmdParams = new String[5];
    private Path CurrClientFolder;
    private String CurrServFolder;
    public final RefreshCmd rf = new RefreshCmd();
    private ImageView imageViewS = new ImageView();
    final Image fielImg = new Image("file.png");
    final Image folderImg = new Image("folder.png");
    final Image arrowImg = new Image("back.png");

    @FXML
    TableView<PathLine> clientPaths;

    @FXML
    TableView<PathLine> serverPaths;

    private TableView.TableViewSelectionModel<PathLine> selectionModelc;
    private TableView.TableViewSelectionModel<PathLine> selectionModels;
    Alert alert = new Alert(Alert.AlertType.INFORMATION);

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            Skt = SocketChannel.open(new InetSocketAddress("localhost", 8189));
        } catch (UnknownHostException e){
            System.out.println("Selected host is unavaliable");
        } catch (IOException e){
            System.out.println(e.toString());
        }

        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            LoginController lc = (LoginController) loader.getController();
            lc.setContext(Skt,this);
            stage.setTitle("SCD Autorization");
            stage.setScene(new Scene(root, 400, 200));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Mgr.addCmd(new SendCmd());
        Mgr.addCmd(new GetCmd());
        Mgr.addCmd(new DelCmd());
        Mgr.addCmd(new RnmCmd());
        Mgr.addCmd(new MoveCmd());
        Mgr.addCmd(new ShrCmd());

        initClientPaths();
        initServerPaths();

        selectionModelc = clientPaths.getSelectionModel();
        selectionModels = serverPaths.getSelectionModel();

    }

    public void btnExit(ActionEvent actionEvent) {

        try {
            if(Skt != null && Skt.isConnected()) Skt.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void initClientPaths(){

        Path init = Paths.get("LaunchDir.txt");

        String initPath = "";
        try(SeekableByteChannel bChan = Files.newByteChannel(init)){
            ByteBuffer tmpBuff = ByteBuffer.allocate(256);
            bChan.read(tmpBuff);
            byte [] bytes = new byte[tmpBuff.position()];
            tmpBuff.flip();
            tmpBuff.get(bytes);
            initPath = new String(bytes);
        }catch (IOException e){
            return;
        }


        Path fPath = Paths.get(initPath);
        CurrClientFolder = fPath;
        ObservableList<PathLine> pathsList = FXCollections.observableArrayList();

        Comparator<PathLine> comparator = new Comparator<PathLine>(){
            public int compare(PathLine o1, PathLine o2) {
                return o1.getImp().compareTo(o2.getImp());
            }
        };

        pathsList.sort(comparator);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(fPath)) {
            pathsList.add(new PathLine("...",0,true,0));
            for (Path file: stream) {
                pathsList.add(new PathLine(file.getFileName().toString(),Files.size(file),Files.isDirectory(file),Files.isDirectory(file) ? 1 : 2));
            }
        } catch (IOException | DirectoryIteratorException x) {

        }

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
                    imageViewC.setImage(item == "A" ? arrowImg : item == "D" ? folderImg : fielImg);

            }
        }
    });

        TableColumn<PathLine, String> tcName = new TableColumn<>("Name");
        tcName.setCellValueFactory(new PropertyValueFactory<PathLine, String>("name"));

        TableColumn<PathLine, Long> tcSize = new TableColumn<>("Size");
        tcSize.setCellValueFactory(new PropertyValueFactory<PathLine, Long>("size"));

        clientPaths.getColumns().addAll(tcType,tcName, tcSize);
        clientPaths.setItems(pathsList);

        clientPaths.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    if (clientPaths.getSelectionModel().getSelectedItem().getName() == "..."){

                        updCPaths(CurrClientFolder.getParent());

                    }else if (clientPaths.getSelectionModel().getSelectedItem().isFolder()){

                        updCPaths(Paths.get(CurrClientFolder.toString()+getSep(CurrClientFolder.toString())+clientPaths.getSelectionModel().getSelectedItem().getName()));

                    }

                }
            }
        });

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
                    imageViewC.setImage(item == "A" ? arrowImg : item == "D" ? folderImg : fielImg);

                }
            }
        });

        TableColumn<PathLine, String> tcName = new TableColumn<>("Name");
        tcName.setCellValueFactory(new PropertyValueFactory<PathLine, String>("name"));

        TableColumn<PathLine, Long> tcSize = new TableColumn<>("Size");
        tcSize.setCellValueFactory(new PropertyValueFactory<PathLine, Long>("size"));

        serverPaths.getColumns().addAll(tcType,tcName, tcSize);
        updSPaths(CurrServFolder,(byte)0);

        serverPaths.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    if (serverPaths.getSelectionModel().getSelectedItem().getName() == "..."){

                        updSPaths(CurrServFolder,(byte)1);

                    }else if (serverPaths.getSelectionModel().getSelectedItem().isFolder()){


                        updSPaths(CurrServFolder+getSep(CurrServFolder)+serverPaths.getSelectionModel().getSelectedItem().getName(),(byte)0);

                    }

                }
            }
        });

    }

    public void updCPaths(Path nPath){

        CurrClientFolder = nPath;
        ObservableList<PathLine> pathsList = FXCollections.observableArrayList();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(nPath)) {
            pathsList.add(new PathLine("...",0,true,0));
            for (Path file: stream) {
                pathsList.add(new PathLine(file.getFileName().toString(),Files.size(file),Files.isDirectory(file),Files.isDirectory(file) ? 1 : 2));
            }
        } catch (IOException | DirectoryIteratorException x) {

        }

        Comparator<PathLine> comparator = new Comparator<PathLine>(){
            public int compare(PathLine o1, PathLine o2) {
                return o1.getImp().compareTo(o2.getImp());
            }
        };

        pathsList.sort(comparator);

        clientPaths.setItems(pathsList);

    }

    public void updSPaths(String strPath,byte mode){

        ObservableList<PathLine> pathsList = rf.Execute(Skt,strPath,mode);

        CurrServFolder = pathsList.get(pathsList.size()-1).getName();
        pathsList.remove(pathsList.size()-1);

        Comparator<PathLine> comparator = new Comparator<PathLine>(){
            public int compare(PathLine o1, PathLine o2) {
                return o1.getImp().compareTo(o2.getImp());
            }
        };

        pathsList.sort(comparator);

        serverPaths.setItems(pathsList);

    }

    public void btnSend(ActionEvent actionEvent) {
        if(!selectionModelc.selectedItemProperty().isNull().get()){
        PathLine cval = selectionModelc.selectedItemProperty().getValue();

        CmdParams[0] = CurrClientFolder.toString() +getSep(CurrClientFolder.toString())+cval.getName();
        CmdParams[1] = CurrServFolder +getSep(CurrServFolder)+ cval.getName();

        runCmd(CmdID.ToSrvID);
        updSPaths(CurrServFolder,(byte)0);

    }}

    public void btnDel(ActionEvent actionEvent) {
        if(!selectionModels.selectedItemProperty().isNull().get()){

        PathLine sval = selectionModels.selectedItemProperty().getValue();
        CmdParams[0] = CurrServFolder +getSep(CurrServFolder)+ sval.getName();
        runCmd(CmdID.Delete);
        updSPaths(CurrServFolder,(byte)0);

    }}

    public void btnRnm(ActionEvent actionEvent) {

        if(!selectionModels.selectedItemProperty().isNull().get()){
        PathLine sval = selectionModels.selectedItemProperty().getValue();
        String NewName = "";

        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Rename.fxml"));
            Parent root = loader.load();
            RnmController lc = (RnmController) loader.getController();
            stage.setTitle("Rename");
            stage.setScene(new Scene(root, 400, 200));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            NewName = CurrServFolder + getSep(CurrServFolder) + lc.getNewName();

        } catch (IOException e) {
            e.printStackTrace();
        }

        CmdParams[0] = CurrServFolder +getSep(CurrServFolder)+ sval.getName();
        CmdParams[1] = NewName;

        runCmd(CmdID.Rename);
        updSPaths(CurrServFolder,(byte)0);

    }}

    public void btnShare(ActionEvent actionEvent) {
        String UsrName = "";

            try {
                Stage stage = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Share.fxml"));
                Parent root = loader.load();
                ShareController lc = (ShareController) loader.getController();
                stage.setTitle("Share With...");
                stage.setScene(new Scene(root, 400, 200));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
                UsrName = lc.getUsrName();

            } catch (IOException e) {
                e.printStackTrace();
            }

            CmdParams[0] = UsrName;

            runCmd(CmdID.Share);
            updSPaths(CurrServFolder,(byte)0);

        }

    public void btnGet(ActionEvent actionEvent) {

        if(!selectionModels.selectedItemProperty().isNull().get()){
        PathLine cval = selectionModelc.selectedItemProperty().getValue();
        PathLine sval = selectionModels.selectedItemProperty().getValue();

        CmdParams[0] = CurrClientFolder.toString() +getSep(CurrClientFolder.toString())+sval.getName();
        CmdParams[1] = CurrServFolder +getSep(CurrServFolder)+ sval.getName();

        runCmd(CmdID.FromSrvID);

        updCPaths(CurrClientFolder);

    }}

    public void btnMov(ActionEvent actionEvent) {

        if(!selectionModels.selectedItemProperty().isNull().get()){
            PathLine sval = selectionModels.selectedItemProperty().getValue();

            try {
                Stage stage = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Move.fxml"));
                Parent root = loader.load();
                MoveController lc = (MoveController) loader.getController();
                lc.setContext(CurrServFolder,CurrServFolder +getSep(CurrServFolder)+ sval.getName(),this);
                stage.setTitle("Replace");
                stage.setScene(new Scene(root, 400, 200));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }

            updSPaths(CurrServFolder,(byte)0);

        }}

    void runCmd (String cmID){

        boolean success = Mgr.setCmd(cmID);

        if (success){
            byte ercode = Mgr.activeCmd.Execute(Skt,CmdParams);
            String err = Mgr.activeCmd.getError(ercode);

            if (!err.equals("")) {
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText(err);
                alert.showAndWait();
            }
        }

    }

    public void setServerFolder(String srvFolder){
        this.CurrServFolder = srvFolder;
    }

    public String getSep(String folder){
        String sep;

        if (folder.endsWith("\\")){
            sep = "";
        }else{
            sep = "\\";
        }

        return sep;
    }

}
