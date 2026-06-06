package com.ondra;

import javafx.application.Application; 
import javafx.stage.Stage;            
import javafx.scene.Scene;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;       
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;   
import javafx.scene.control.Label;     
import javafx.scene.control.TextArea; 
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
        BorderPane mainLayout = new BorderPane();
        
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("menuBar"); 

        Menu fileMenu = new Menu("Database");

        MenuItem openDb = new MenuItem("Open Database");
        MenuItem exitApp = new MenuItem("Exit");
        fileMenu.getItems().addAll(openDb, exitApp);

        menuBar.getMenus().addAll(fileMenu);

        mainLayout.setTop(menuBar);

        Scene scene = new Scene(mainLayout);
        Image icon = new Image(getClass().getResourceAsStream("/database.png"));
        
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
