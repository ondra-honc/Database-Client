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
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;   
import javafx.scene.control.TableColumn;  
import javafx.scene.control.SplitPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;    

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;      

import javafx.beans.property.SimpleStringProperty; 

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class error
{
    static public void showError(SQLException e) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setHeaderText("Error");
                                
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
                                
        alert.setContentText(stackTrace);

        alert.show();
    }
}

public class App extends Application
{
    private Connection connection;
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
            Button create = new Button("Create");
            TextField fileLocation = new TextField();
            TextField dbName = new TextField();
            Label labelName = new Label("Database Name:");
            Label labelLocation = new Label("Database Location:");    
            Stage createDbStage = new Stage();
            BorderPane createDbLayout = new BorderPane();
            Scene scene = new Scene(createDbLayout);
            HBox createButtonRow = new HBox(create);
            
            directoryChooser.setOnAction(e -> {
                javafx.stage.DirectoryChooser dirChooser = new javafx.stage.DirectoryChooser();
                dirChooser.setTitle("Select Database Folder");

                java.io.File selectedDirectory = dirChooser.showDialog(createDbStage);

                if (selectedDirectory != null) 
                {
                    fileLocation.setText(selectedDirectory.getAbsolutePath());
                } else 
                {

                }
            });

            create.setOnAction(evnt -> {
                try 
                {
                    File database = new File(fileLocation.getText() + "/" + dbName.getText() + ".db");
                    if (database.createNewFile()) 
                    {
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText("Database created successfully");

                        alert.show();
                        createDbStage.close();
                    } else
                    {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("File already exists");

                        alert.show();
                    }
                } catch (IOException e)
                {
                    System.out.println("An error occurred.");
                    e.printStackTrace(); 
                }
            });

            hbox.getChildren().addAll(fileLocation, directoryChooser);

            createButtonRow.setAlignment(javafx.geometry.Pos.CENTER);
            vbox.getChildren().addAll(labelName, dbName, labelLocation, hbox, createButtonRow);

            createDbLayout.setCenter(vbox);
            
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            
            createDbLayout.getStyleClass().add("create-db-window");
            vbox.getStyleClass().add("vbox");
            hbox.getStyleClass().add("hbox");
            create.getStyleClass().add("create");

            createDbStage.initOwner(primaryStage);
            createDbStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            
            createDbStage.getIcons().add(icon);
            createDbStage.setTitle("Create Database");
            createDbStage.centerOnScreen();
            createDbStage.setHeight(300);
            createDbStage.setWidth(512);
            createDbStage.setScene(scene);
            createDbStage.showAndWait();
        });

        openDb.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open SQLite Database File");

            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SQLite Database", "*.db", "*.sqlite", "*.sqlite3")
            );

            java.io.File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) 
            {
                try
                {
                    if (connection != null) connection.close();
        
                    connection = DriverManager.getConnection("jdbc:sqlite:" + selectedFile.getAbsolutePath());
                    Statement statement = connection.createStatement();
                    
                    SplitPane splitPane = new SplitPane();
                    HBox headerBox = new HBox();
                    VBox leftPanel = new VBox(); 
                    Label tablesLabel = new Label("Tables");
                    Button createTable = new Button("+"); 
                    ListView<String> tableList = new ListView<>();
                    TableView<String> dataTable = new TableView<>();
    
                    createTable.setOnAction(ev -> {
                        VBox vbox = new VBox();
                        BorderPane createTableLayout = new BorderPane();
                        Scene scene = new Scene(createTableLayout);
                        Stage createTableStage = new Stage();
                        TextField tableName = new TextField();
                        Label tableLabelName = new Label("Table Name:");
                        Button create = new Button("Create");
                        HBox createButtonRow = new HBox(create);

                        create.setOnAction(eve -> {
                            try 
                            {
                                String name = tableName.getText().trim();
                                if (name.isEmpty()) {
                                    throw new SQLException("Table name cannot be empty");
                                }

                                String sql = "CREATE TABLE " + name + " (id INTEGER PRIMARY KEY)";
                                
                                statement.executeUpdate(sql);

                                tableList.getItems().add(name);
                                
                                ((Stage) create.getScene().getWindow()).close();
                            } catch (SQLException e) {
                                error.showError(e);
                            }
                        });

                        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                        
                        create.getStyleClass().add("create");
                        createButtonRow.setAlignment(javafx.geometry.Pos.CENTER);

                        vbox.getChildren().addAll(tableLabelName, tableName, createButtonRow);
                        vbox.getStyleClass().add("vbox");
                        javafx.scene.layout.VBox.setMargin(createButtonRow, new javafx.geometry.Insets(20, 0, 0, 0));

                        createTableLayout.getStyleClass().add("create-db-window");
                        createTableLayout.setCenter(vbox);
                        
                        
                        createTableStage.initOwner(primaryStage);
                        createTableStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
                        
                        createTableStage.getIcons().add(icon);
                        createTableStage.setTitle("Create new table");
                        createTableStage.centerOnScreen();
                        createTableStage.setHeight(250);
                        createTableStage.setWidth(512);
                        createTableStage.setScene(scene);
                        createTableStage.showAndWait();
                    });
                    
                    splitPane.getStyleClass().add("split-pane");
                    headerBox.getStyleClass().add("header-box");
                    leftPanel.getStyleClass().add("left-panel");
                    tablesLabel.getStyleClass().add("tables-header");
                    createTable.getStyleClass().add("create-table");
                    dataTable.getStyleClass().add("table-view");
    
                    headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    headerBox.setSpacing(10);
    
                    tablesLabel.setMaxWidth(Double.MAX_VALUE);
                    javafx.scene.layout.HBox.setHgrow(tablesLabel, javafx.scene.layout.Priority.ALWAYS); 
    
                    headerBox.getChildren().addAll(tablesLabel, createTable);
                    
                    javafx.scene.layout.VBox.setVgrow(tableList, javafx.scene.layout.Priority.ALWAYS);
    
                    leftPanel.getChildren().addAll(headerBox, tableList);
    
                    java.sql.DatabaseMetaData metaData = connection.getMetaData();
                    java.sql.ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
                    
                    tableList.getItems().clear(); 
                    
                    while (tables.next()) {
                        tableList.getItems().add(tables.getString("TABLE_NAME"));
                    }

                    dataTable.setPlaceholder(new Label("Choose a table from the left panel to view data"));
    
                    splitPane.getItems().addAll(leftPanel, dataTable);
                    splitPane.setDividerPositions(0.25f);
    
                    mainLayout.setCenter(splitPane);
                } catch (SQLException e)
                {
                    error.showError(e);
                }   
            } else 
            {

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
