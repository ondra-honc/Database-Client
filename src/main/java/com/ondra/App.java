package com.ondra;

import javafx.application.Application; 
import javafx.stage.Stage;            
import javafx.scene.Scene;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;       
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;   
import javafx.scene.control.Label;     
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.ListView; 
import javafx.stage.FileChooser;      

import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import javafx.scene.control.TableView;   
import javafx.scene.control.TableColumn;  
import javafx.beans.property.SimpleStringProperty; 

import javafx.scene.image.Image;    
import javafx.scene.image.ImageView;

public class App extends Application
{
    public static void main(String[] args) 
    {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) 
    {     
        Image icon = new Image(getClass().getResourceAsStream("/database.png"));
        BorderPane mainLayout = new BorderPane();
        
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("menuBar"); 

        Menu fileMenu = new Menu("Database");

        MenuItem createDb = new MenuItem("Create Database");
        MenuItem openDb = new MenuItem("Open Database");
        fileMenu.getItems().addAll(createDb, openDb);

        createDb.setOnAction(event -> {
            VBox vbox = new VBox();
            HBox hbox = new HBox();    
            Button directoryChooser = new Button("Browse...");
            TextField fileLocation = new TextField();
            TextField dbName = new TextField();
            Label labelName = new Label("Database Name:");
            Label labelLocation = new Label("Database Location:");            

            hbox.getChildren().addAll(fileLocation, directoryChooser);

            vbox.getChildren().addAll(labelName, dbName, labelLocation, hbox);

            BorderPane createDbLayout = new BorderPane();
            createDbLayout.setCenter(vbox);

            Stage createDbStage = new Stage();
            
            Scene scene = new Scene(createDbLayout);
            
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            
            createDbLayout.getStyleClass().add("create-db-window");
            vbox.getStyleClass().add("vbox");
            hbox.getStyleClass().add("hbox");

            createDbStage.getIcons().add(icon);
            createDbStage.setTitle("Create Database");
            createDbStage.centerOnScreen();
            createDbStage.setHeight(325);
            createDbStage.setWidth(512);
            createDbStage.setScene(scene);
            createDbStage.show();
        });

        openDb.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open SQLite Database File");

            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SQLite Database", "*.db", "*.sqlite", "*.sqlite3")
            );

            java.io.File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {

            } 
            else {

            }

        });

        menuBar.getMenus().addAll(fileMenu);

        mainLayout.setTop(menuBar);

        Scene scene = new Scene(mainLayout);
        
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("Database Client");
        primaryStage.centerOnScreen();
        primaryStage.setHeight(650);
        primaryStage.setWidth(1024);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
