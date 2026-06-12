package com.ondra;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.application.Application;            
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;    
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

class ErrorHandler
{
    static public void showError(Exception e) {
        try {
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdir();
            }

            File logFile = new File(logDir, "log.txt");
            try (FileWriter fw = new FileWriter(logFile, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                pw.println("=========================================");
                pw.println("Čas chyby: " + dtf.format(LocalDateTime.now()));
                pw.println("Zpráva: " + e.getMessage());
                pw.println("-----------------------------------------");

                e.printStackTrace(pw);
                pw.println("=========================================\n");
            }
        } catch (Exception logEx) {
            System.err.println("Nepodařilo se zapsat do logovacího souboru!");
            logEx.printStackTrace();
        }

        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Chyba aplikace");
        alert.setHeaderText("Došlo k neočekávané chybě");
        alert.setContentText("Aplikace narazila na problém a akci nebylo možné dokončit.\n\n" +
                             "Detailní informace byly uloženy do souboru: logs/log.txt");
        
        alert.showAndWait();
    }
}

public class App extends Application
{
    private Connection connection;
    
    private HBox createTableMetadataHeader(String tableName) {
        HBox headerBox = new HBox();
        headerBox.setStyle("-fx-padding: 15; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0; -fx-alignment: CENTER_LEFT;");
        headerBox.setSpacing(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, null);
            ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, tableName);

            Label tableNameLabel = new Label("Table: " + tableName);
            tableNameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: white;");

            StringBuilder pkInfo = new StringBuilder("Primary Key(s): ");
            while (primaryKeys.next()) {
                pkInfo.append(primaryKeys.getString("COLUMN_NAME")).append(", ");
            }
            if (pkInfo.toString().endsWith(", ")) {
                pkInfo.setLength(pkInfo.length() - 2);
            }
            Label pkLabel = new Label(pkInfo.toString());
            pkLabel.setStyle("-fx-font-size: 12; -fx-text-fill: white;");

            StringBuilder columnsInfo = new StringBuilder("Columns: ");
            int columnCount = 0;
            columns = metaData.getColumns(null, null, tableName, null);
            while (columns.next()) {
                columnCount++;
                String colName = columns.getString("COLUMN_NAME");
                String colType = columns.getString("TYPE_NAME");
                columnsInfo.append(colName).append(" (").append(colType).append("), ");
            }
            if (columnsInfo.toString().endsWith(", ")) {
                columnsInfo.setLength(columnsInfo.length() - 2);
            }
            Label columnsLabel = new Label(columnsInfo.toString());
            columnsLabel.setStyle("-fx-font-size: 12; -fx-wrap-text: true; -fx-text-fill: white;");
            columnsLabel.setWrapText(true);

            headerBox.getChildren().addAll(tableNameLabel, pkLabel, columnsLabel);

        } catch (SQLException e) {
            ErrorHandler.showError(e);
        }

        return headerBox;
    }

    private VBox loadTableData(String tableName) {
        VBox container = new VBox();
        
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            TableView<ObservableList<String>> tableView = new TableView<>();
            tableView.getColumns().clear();
            tableView.getItems().clear();

            for (int i = 1; i <= columnCount; i++) {
                final int columnIndex = i - 1;
                String columnName = metaData.getColumnName(i);
                
                TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnName);
                column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(columnIndex)));
                column.setPrefWidth(100);
                
                tableView.getColumns().add(column);
            }

            while (resultSet.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= columnCount; i++) {
                    String value = resultSet.getString(i);
                    row.add(value != null ? value : "");
                }
                tableView.getItems().add(row);
            }

            resultSet.close();
            statement.close();

            VBox.setVgrow(tableView, Priority.ALWAYS);
            container.getChildren().addAll(
                createTableMetadataHeader(tableName),
                tableView
            );

        } catch (Exception e) {
            ErrorHandler.showError(e);
        }

        return container;
    }
    public static void main(String[] args) 
    {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) 
    {     
        Image icon = new Image(getClass().getResourceAsStream("/database.png"));
        BorderPane mainLayout = new BorderPane();
        
        VBox welcomePage = new VBox();
        welcomePage.getStyleClass().add("welcome-page");
        welcomePage.setAlignment(Pos.CENTER);
        welcomePage.setSpacing(25);

        ImageView welcomeIcon = new ImageView(icon);
        welcomeIcon.setFitWidth(64);
        welcomeIcon.setFitHeight(64);
        welcomeIcon.setOpacity(0.6);
        Label welcomeTitle = new Label("Database Client");
        welcomeTitle.getStyleClass().add("welcome-title");

        Label welcomeSubtitle = new Label("Create a new SQLite database file or open an existing one to manage your tables and data.");
        welcomeSubtitle.getStyleClass().add("welcome-subtitle");

        HBox actionRow = new HBox();
        actionRow.setAlignment(Pos.CENTER);
        actionRow.setSpacing(15);

        Button quickOpen = new Button("Open Database");
        quickOpen.getStyleClass().add("welcome-btn");

        Button quickCreate = new Button("Create Database");
        quickCreate.getStyleClass().add("welcome-btn");
        quickCreate.getStyleClass().add("welcome-btn-primary");

        actionRow.getChildren().addAll(quickOpen, quickCreate);
        welcomePage.getChildren().addAll(welcomeIcon, welcomeTitle, welcomeSubtitle, actionRow);
        
        mainLayout.setCenter(welcomePage);

        MenuItem createDb = new MenuItem("Create Database");
        MenuItem openDb = new MenuItem("Open Database");

        quickOpen.setOnAction(event -> openDb.fire());
        quickCreate.setOnAction(event -> createDb.fire());
        
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("menuBar"); 

        Menu fileMenu = new Menu("Database");

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
                DirectoryChooser dirChooser = new DirectoryChooser();
                dirChooser.setTitle("Select Database Folder");

                File selectedDirectory = dirChooser.showDialog(createDbStage);

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
                    ErrorHandler.showError(e);
                }
            });

            hbox.getChildren().addAll(fileLocation, directoryChooser);

            createButtonRow.setAlignment(Pos.CENTER);
            vbox.getChildren().addAll(labelName, dbName, labelLocation, hbox, createButtonRow);

            createDbLayout.setCenter(vbox);
            
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            
            createDbLayout.getStyleClass().add("create-db-window");
            vbox.getStyleClass().add("vbox");
            hbox.getStyleClass().add("hbox");
            create.getStyleClass().add("create");

            createDbStage.initOwner(primaryStage);
            createDbStage.initModality(Modality.WINDOW_MODAL);
            
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

            File selectedFile = fileChooser.showOpenDialog(primaryStage);

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
                    Label tablesLabel = new Label(selectedFile.getName() + " tables");
                    Button createTable = new Button("+"); 
                    Button deleteTable = new Button("-");
                    ListView<String> tableList = new ListView<>();
                    TableView<ObservableList<String>> dataTable = new TableView<>();
    
                    deleteTable.setOnAction(evnt -> {
                        String selectedTable = tableList.getSelectionModel().getSelectedItem();

                        if (selectedTable == null) return;

                        Alert alert = new Alert(AlertType.CONFIRMATION);
                        alert.setTitle("Delete table");
                        alert.setHeaderText("Delete table: " + selectedTable + "?");
                        alert.setContentText("This action cannot be undone.");

                        alert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                try {
                                    statement.executeUpdate("DROP TABLE " + selectedTable);
                                    tableList.getItems().remove(selectedTable);
                                } catch(SQLException e)
                                {
                                    ErrorHandler.showError(e);
                                }
                            }
                        });
                    });
                    
                    createTable.setOnAction(evn -> {
                        VBox vbox = new VBox();
                        HBox hbox = new HBox();
                        BorderPane createTableLayout = new BorderPane();
                        Scene scene = new Scene(createTableLayout);
                        Stage createTableStage = new Stage();
                        TextField tableName = new TextField();
                        Label tableLabelName = new Label("Table Name:");
                        TextField PKName = new TextField();
                        Label PKLabelName = new Label("Primary key Name:");
                        Button create = new Button("Create");
                        HBox createButtonRow = new HBox(create);
                        ComboBox<String> typeComboBox = new ComboBox<>();
                        
                        typeComboBox.getItems().addAll("INTEGER", "TEXT", "BIGINT");
                        typeComboBox.setValue("INTEGER");
                        typeComboBox.getStyleClass().add("combo-box");

                        CheckBox autoIncrementCheck = new CheckBox("AI");
                        Tooltip tooltip = new Tooltip("Autoincrement");
                        tooltip.setShowDelay(Duration.millis(100));
                        autoIncrementCheck.getStyleClass().add("checkbox"); 
                        autoIncrementCheck.setTooltip(tooltip);

                        typeComboBox.setOnAction(e -> {
                            if (!typeComboBox.getValue().equals("INTEGER")) {
                                autoIncrementCheck.setSelected(false);
                                autoIncrementCheck.setDisable(true); 
                            } else {
                                autoIncrementCheck.setDisable(false);
                            }
                        });

                        
                        
                        create.setOnAction(ev -> {
                            try 
                            {
                                
                                String name = tableName.getText().trim();
                                String pk = PKName.getText().trim();
                                String type = typeComboBox.getValue(); 
                                
                                if (name.isEmpty() ||pk.isEmpty()) {
                                    Alert alert = new Alert(AlertType.WARNING);
                                    alert.setTitle("Chybějící údaje");
                                    alert.setHeaderText("Formulář není kompletní");
                                    alert.setContentText("Název tabulky i název primárního klíče musí být vyplněny.");
                                    alert.show();
                                    return;
                                }

                                if (tableList.getItems().contains(name)) {
                                    Alert alert = new Alert(AlertType.WARNING);
                                    alert.setTitle("Duplicitní tabulka");
                                    alert.setHeaderText("Tabulka již existuje");
                                    alert.setContentText("Tabulka s názvem '" + name + "' už v databázi je. Zvol jiný název.");
                                    alert.show();
                                    return;
                                }

                                String sql = "CREATE TABLE " + name + " (" + pk + " " + type + " PRIMARY KEY" + (autoIncrementCheck.isSelected() ? " AUTOINCREMENT" : "") + ")";
                                
                                statement.executeUpdate(sql);

                                tableList.getItems().add(name);
                                
                                ((Stage) create.getScene().getWindow()).close();
                            } catch (SQLException e) {
                                ErrorHandler.showError(e);
                            }
                        });
                        
                        
                        hbox.getChildren().addAll(PKName, typeComboBox, autoIncrementCheck);
                        hbox.setAlignment(Pos.CENTER_LEFT);
                        HBox.setMargin(typeComboBox, new Insets(0, 0, 0, 10));
                        HBox.setMargin(autoIncrementCheck, new Insets(0, 0, 0, 10));
                        HBox.setHgrow(PKName, Priority.ALWAYS);
                        
                        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                        
                        create.getStyleClass().add("create");
                        createButtonRow.setAlignment(Pos.CENTER);

                        vbox.getChildren().addAll(tableLabelName, tableName, PKLabelName, hbox, createButtonRow);
                        vbox.getStyleClass().add("vbox");
                        VBox.setMargin(createButtonRow, new Insets(20, 0, 0, 0));
                        
                        createTableLayout.getStyleClass().add("create-db-window");
                        createTableLayout.setCenter(vbox);
                        
                        
                        createTableStage.initOwner(primaryStage);
                        createTableStage.initModality(Modality.WINDOW_MODAL);
                        
                        createTableStage.getIcons().add(icon);
                        createTableStage.setTitle("Create new table");
                        createTableStage.centerOnScreen();
                        createTableStage.setHeight(350);
                        createTableStage.setWidth(512);
                        createTableStage.setScene(scene);
                        createTableStage.showAndWait();
                    });
                    
                    splitPane.getStyleClass().add("split-pane");
                    headerBox.getStyleClass().add("header-box");
                    leftPanel.getStyleClass().add("left-panel");
                    tablesLabel.getStyleClass().add("tables-header");
                    createTable.getStyleClass().add("create-table");
                    deleteTable.getStyleClass().add("create-table");
                    dataTable.getStyleClass().add("table-view");
                    
                    headerBox.setAlignment(Pos.CENTER_LEFT);
                    headerBox.setSpacing(10);
                    
                    tablesLabel.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(tablesLabel, Priority.ALWAYS); 
                    
                    headerBox.getChildren().addAll(tablesLabel, createTable, deleteTable);
                    
                    VBox.setVgrow(tableList, Priority.ALWAYS);
                    
                    leftPanel.getChildren().addAll(headerBox, tableList);
                    
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
                    
                    tableList.getItems().clear(); 
                    
                    while (tables.next()) {
                        tableList.getItems().add(tables.getString("TABLE_NAME"));
                    }

                    VBox emptyContainer = new VBox();
                    emptyContainer.setAlignment(Pos.CENTER);
                    emptyContainer.getChildren().add(new Label("Choose a table from the left panel to view data")); 
                    
                    splitPane.getItems().addAll(leftPanel, dataTable);
                    splitPane.setDividerPositions(0.25f);

                    tableList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            VBox tableContainer = loadTableData(newVal);
                            splitPane.getItems().set(1, tableContainer);
                        }
                    });
    
                    mainLayout.setCenter(splitPane);
                } catch (SQLException e)
                {
                    ErrorHandler.showError(e);
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
