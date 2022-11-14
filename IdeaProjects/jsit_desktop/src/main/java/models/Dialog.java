package models;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Dialog{
	
	private static String message;
	
	public static void setMessage(String newMessage) {
		message = newMessage;
	}
	
	public static void showAlertDialog() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(null);
		alert.setHeaderText(null);
		alert.setContentText(message);
		
		alert.show();
	}
	
}