package controllers;


import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import models.Password;

public class indexController {
	@FXML private HBox contentContainer;
	
	@FXML void initialize() throws IOException {
    	setBorderContainerCenter("operations");
	}
 
    public void showOptions(ActionEvent event) throws Exception {
    	setBorderContainerCenter("options");
    }
    
    public void showOperations(ActionEvent event) throws Exception {
    	setBorderContainerCenter("operations");
    }
    
    public void setBorderContainerCenter(String viewName) throws IOException {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/" +viewName+".fxml"));
		AnchorPane viewPane = (AnchorPane) loader.load();
		
		contentContainer.getChildren().clear();
		contentContainer.getChildren().add(viewPane);
	}

	public void generatePassword() throws SQLException, ClassNotFoundException {
		final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < 10; i++)
		{
			int randomIndex = random.nextInt(chars.length());
			sb.append(chars.charAt(randomIndex));
		}

		Password.updatePassword(sb.toString());

		ButtonType copy = new ButtonType("Copy");
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Generate Password");
		alert.setHeaderText("Attention ! This password will only be accessible and valid once");
		alert.setContentText("Password is : "+sb);
		alert.getButtonTypes().add(copy);
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == copy){
			final Clipboard clipboard = Clipboard.getSystemClipboard();
			final ClipboardContent content = new ClipboardContent();
			content.putString(sb.toString());
			clipboard.setContent(content);
		}
	}



}