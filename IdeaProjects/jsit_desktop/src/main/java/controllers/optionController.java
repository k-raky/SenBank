package controllers;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.internal.LinkedTreeMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Dialog;
import models.PrintersConfiguration;
import javafx.print.*;

public class optionController {
	
	@FXML private VBox configPrinters;
	@FXML private TextField selectedDirectory;
	private ObservableSet<Printer> printers;
	 
    @FXML void initialize() {
    	
    	PrintersConfiguration pc = new PrintersConfiguration();
    	ArrayList<LinkedTreeMap<String, String>> configFileData = pc.getConfigFileData();

    	if (configFileData == null) {
    		showNewPrinters();		
		}
    	else {
    		showConfigPrinters();
		}
    }
    
    public void saveConfigs(ActionEvent event) {
    	
    	int printersNumber = configPrinters.getChildren().size();

		System.out.println(printersNumber);
    	ObservableList<ObservableMap<String, String>> printersConfig = FXCollections.observableArrayList();
    	
    	for (int i = 0; i < printersNumber; i++) {
    		
			Text printerNameText =  (Text) configPrinters.getScene().lookup("#printer"+i);
    		ChoiceBox<String> activity = (ChoiceBox<String>) configPrinters.getScene().lookup("#activity"+i);
    		ChoiceBox<String> hierarchy = (ChoiceBox<String>) configPrinters.getScene().lookup("#hierarchy"+i);

			if (printerNameText != null && activity != null && hierarchy != null) {
				String printerName = printerNameText.getText();
				String printerActivity = activity.getSelectionModel().getSelectedItem();
				String printerHierarchy = hierarchy.getSelectionModel().getSelectedItem();

				ObservableMap<String, String> printerConfig = FXCollections.observableHashMap();
				printerConfig.put("name", printerName);
				printerConfig.put("activity", printerActivity);
				printerConfig.put("hierarchy", printerHierarchy);

				printersConfig.add(printerConfig);
			}
		}
    	
    	String downloadFolderPath = selectedDirectory.getText();
		if (!(downloadFolderPath.isEmpty())) {
			ObservableMap<String, String> downloadFolderConfig = FXCollections.observableHashMap();
			downloadFolderConfig.put("download_folder_path", downloadFolderPath);
			printersConfig.add(downloadFolderConfig);
		}

		PrintersConfiguration pc = new PrintersConfiguration();
		pc.updateConfigFileData(printersConfig);

		Dialog.setMessage("Configuration saved");
		Dialog.showAlertDialog();

	}
    
    public void showConfigPrinters() {
		
    	PrintersConfiguration pc = new PrintersConfiguration();
    	ArrayList<LinkedTreeMap<String, String>> configFileData = pc.getConfigFileData();
    	
    	int index=0;
		for (LinkedTreeMap<String, String> treeMap : configFileData) {
			if (treeMap.get("name")!=null) {
				addPrinterConfigRow(index, treeMap.get("name"), treeMap.get("activity"), treeMap.get("hierarchy"));				
			}
			if (treeMap.get("download_folder_path") != null) {
				selectedDirectory.setText(treeMap.get("download_folder_path"));
			}
			index++;
		}
	}
    
    public void showNewPrinters() {
    	
    	try {
    		configPrinters.getChildren().clear();
    		selectedDirectory.clear();
    		printers = Printer.getAllPrinters();

			System.out.println(printers);
			if (printers.isEmpty()) {
				Text text = new Text("No printers found");
				configPrinters.getChildren().add(text);
			}
			else {
				int index = 0;
				for (Printer printer : printers) {
					addPrinterConfigRow(index, printer.getName(), null, null);
					index++;
				}
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
    
    public void addPrinterConfigRow(int index, String printerName, String printerActivity, String printerHierarchy) {
    	
    	HBox printerConfigBox = new HBox();
		printerConfigBox.setAlignment(Pos.CENTER);
		printerConfigBox.setLayoutY(157.0);
		printerConfigBox.setPrefHeight(73.0);
		printerConfigBox.setPrefWidth(58.0);
		printerConfigBox.setSpacing(25.0);
		
		Text printerNameText = new Text();
		printerNameText.setText(printerName);
		printerNameText.setId("printer"+index);
		
		ChoiceBox<String> activity = new ChoiceBox<String>(FXCollections.observableArrayList(
				"PRINT DELIVERY ORDER", "PRINT DL/C7", "PRINT ORANGE", "PRINT GREEN", "PRINT PINK", "PRINT YELLOW"));
		ChoiceBox<String> hierarchy = new ChoiceBox<String>(FXCollections.observableArrayList("FOCUS", "ALL"));
		
		if (printerActivity != null) {
			activity.setValue(printerActivity);
		} 
		activity.setPrefWidth(150.0);
		activity.setId("activity"+index);
		
		if (printerHierarchy != null) {
			hierarchy.setValue(printerHierarchy);
		}
		hierarchy.setPrefWidth(150.0);
		hierarchy.setId("hierarchy"+index);
		
		printerConfigBox.getChildren().add(printerNameText);
		printerConfigBox.getChildren().add(activity);
		printerConfigBox.getChildren().add(hierarchy);
		
		configPrinters.getChildren().add(printerConfigBox);
	}
    
    public void chooseDownloadPath(ActionEvent event) {
		
    	DirectoryChooser fileChooser = new DirectoryChooser();
    	Stage optionWindow = (Stage) configPrinters.getScene().getWindow();
    	File selectedDirectoryPath= fileChooser.showDialog(optionWindow);
    	
    	if (selectedDirectoryPath != null) {
			selectedDirectory.setText(selectedDirectoryPath.toString());
		}
    	
	}

}