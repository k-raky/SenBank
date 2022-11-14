package controllers;


import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import models.PrintersConfiguration;

public class operationController {
	
	@FXML private ListView<String> listNumCommandes;
	PrintersConfiguration pc = new PrintersConfiguration();
	private File downloadFolder = new File(pc.getDownloadFolderPath()==null ? Paths.get(System.getProperty("user.home"), "Downloads").toString() : pc.getDownloadFolderPath());


	@FXML void initialize() {
    	
    	listNumCommandes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    	listNumCommandes.setPlaceholder(new Label("Aucune Commande"));

    	FilenameFilter filefilter = new FilenameFilter() {
            @Override
            public boolean accept(File downloadFolder, String name) {
                return name.matches("^\\d+\\_.+\\.(pdf|zip)$");
            }
        };
    	String[] listFiles= downloadFolder.list(filefilter);
    	
    	ObservableSet<String> listCommandes = FXCollections.observableSet();
		if (listFiles != null) {
			for (String string : listFiles) {
				string = string.substring(0, string.indexOf("_"));
				listCommandes.add(string);
			}
		}
    	listNumCommandes.getItems().addAll(listCommandes);
 
    }
    
    
	public void showPrintFiles(MouseEvent event) throws Exception {
	    	
		String numCommande = listNumCommandes.getSelectionModel().getSelectedItem();
		Stage primaryStage = new Stage();
    	
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/print.fxml"));
		Parent root = loader.load();
		primaryStage.setTitle("Imprimer une commande");
		primaryStage.setScene(new Scene(root));
		primaryStage.setUserData(numCommande);
		primaryStage.show();
		
    }
    
    
    
    
}