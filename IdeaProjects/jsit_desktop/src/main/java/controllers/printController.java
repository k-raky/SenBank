package controllers;

import com.google.gson.internal.LinkedTreeMap;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.Dialog;
import models.PrintersConfiguration;
import models.Woocommerce;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Sides;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import java.awt.print.PrinterException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.awt.print.PrinterJob;
import java.io.File;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

public class printController{
	
	@FXML private Text titre;
	@FXML private VBox listFilesContainer;
	private String[] listFiles = null;
	PrintersConfiguration pc = new PrintersConfiguration();
	private File downloadFolder = new File( pc.getDownloadFolderPath()==null || pc.getDownloadFolderPath().isEmpty() ? Paths.get(System.getProperty("user.home"), "Downloads").toString() : pc.getDownloadFolderPath());
	private Node node = new Node() {};
	private int printedFiles = 0;
	private String numCommande = null;
	
	@FXML void initialize() {
		
		Platform.runLater(()->{
		
			Stage stage = (Stage) listFilesContainer.getScene().getWindow();
			numCommande =  (String) stage.getUserData();
			  
			titre.setText("Commande "+numCommande);
			
			listFiles= getOrderFiles(numCommande);
			
			for (String string : listFiles) {		
				if (string.contains(".zip")) {
					unzip(downloadFolder.toString()+ File.separator + string, downloadFolder.toString());
					try {
						Files.delete(Paths.get(downloadFolder.toString()+ File.separator + string));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			//after unzipping the files we do a new search
			listFiles= getOrderFiles(numCommande);
			ObservableSet<String> listFileSet = FXCollections.observableSet(listFiles);

			int index=0;
			for (String string : listFileSet) {
				
				HBox fileBox = new HBox();
				fileBox.setAlignment(Pos.CENTER);
				fileBox.setSpacing(50.0);
				fileBox.setPrefHeight(50.0);
				
				Text fileName = new Text(string);
				fileName.setId("file"+index);
				fileName.prefWidth(100.0);
				fileName.applyCss();
				
		        CheckBox checkBox = new CheckBox();
		        checkBox.setSelected(true);
		        checkBox.setId("checkbox"+index);
		        checkBox.applyCss();		        
		        
		        fileBox.getChildren().add(fileName);
		        fileBox.getChildren().add(checkBox);
		        fileBox.applyCss();
		        fileBox.layout();
		        listFilesContainer.getChildren().add(fileBox);
		        index++;
			}
			
			listFilesContainer.setSpacing(20.0);
			
		});
		
	}
	
	public void imprimer(ActionEvent event) throws Exception {

		listFilesContainer.setCursor(Cursor.WAIT);

		node = (Node) event.getSource();
		node.setCursor(Cursor.WAIT);
		ObservableList<String> selectedFiles = FXCollections.observableArrayList();
	    
		for (int index = 0; index < listFiles.length; index++) {
			Text fileName =  (Text) node.getScene().lookup("#file"+index);
    		CheckBox checkBox = (CheckBox) node.getScene().lookup("#checkbox"+index);
    		
    		if (checkBox.isSelected()) {
    			selectedFiles.add(fileName.getText());				
			}
		}
		
		printSelectedFiles(selectedFiles);
	}
	
	public void printSelectedFiles(ObservableList<String> selectedFiles) throws Exception {
		
		// if filename contains bonCommande we search for activity printer with deliveryorder
		// if found redirect to that printer
		// if not found search or hierarchy = all then send to printer
		
		for (String filename : selectedFiles) {
			
			String labelType = null;
			String activity = null;
			Pattern pattern = Pattern.compile("(?<=_).[^_]+((?=\\.)|(?=_))");
			Matcher matcher = pattern.matcher(filename);
			if(matcher.find()) {
			   labelType = matcher.group(0);
			}
			 
			System.out.println(labelType);
			
			switch (labelType) {
			case "Orange":
				activity = "PRINT ORANGE";
				break;
			case "Gelb":
				activity = "PRINT YELLOW";
				break;
			case "Rosa":
				activity = "PRINT PINK";
				break;
			case "Grün":
				activity = "PRINT GREEN";
				break;
			case "enveloppeC7":
				activity = "PRINT DL/C7";
				break;
			case "enveloppeDL":
				activity = "PRINT DL/C7";
				break;
			case "bonCommande":
				activity = "PRINT DELIVERY ORDER";
				break;
			default:
				Dialog.setMessage("Activity type not found");
				Dialog.showAlertDialog();
				return;
			}
			
			String selectedPrinter = selectPrinter(activity);
			System.out.println(selectedPrinter);
			printFile(filename, selectedPrinter);
	
		}

		listFilesContainer.setCursor(Cursor.DEFAULT);
		node.setCursor(Cursor.DEFAULT);

		if (listFiles.length == printedFiles && (printedFiles==5 || printedFiles==13)) {
			Woocommerce.UpdateOrderStatus(numCommande);
		}
		if (printedFiles > 0) {
			Dialog.setMessage("Printing done");
			Dialog.showAlertDialog();
		}
		
	}
	
	public String selectPrinter(String fileActivity) {
		
		String printerName = null;
    	ArrayList<LinkedTreeMap<String, String>> configFileData = pc.getConfigFileData();
    	
    	for (LinkedTreeMap<String, String> treeMap : configFileData) {
    		if (treeMap.get("activity")!=null && treeMap.get("activity").equals(fileActivity)) {
    			printerName=treeMap.get("name");
    			return printerName;
			}
		}
    	System.out.println(configFileData);

    	if (printerName == null) {
    		for (LinkedTreeMap<String, String> treeMap : configFileData) {
        		if (treeMap.get("hierarchy")!= null && treeMap.get("hierarchy").equals("ALL")) {
        			printerName=treeMap.get("name");
        			return printerName;
    			}
    		}
		}
    	
    	return printerName;
	}

	private static PrintService findPrintService(String printerName) {
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
		if (printServices.length == 0) {
			Dialog.setMessage("No printers available");
			Dialog.showAlertDialog();
		}
		else {
			for (PrintService printService : printServices) {
				if (printService.getName().trim().equals(printerName)) {
					return printService;
				}
			}
		}
		return null;
	}

	public void printFile(String filename, String selectedPrinter) throws IOException, PrinterException {

		File fileToPrint = new File(Paths.get(downloadFolder.toString(), filename).toString());
		PDDocument document = PDDocument.load(fileToPrint);

		PrintService myPrintService = findPrintService(selectedPrinter);

		if (myPrintService == null ) {
			Dialog.setMessage("Printer "+selectedPrinter+" not found");
			Dialog.showAlertDialog();
		}
		else {
			PrinterJob job = PrinterJob.getPrinterJob();
			job.setPageable(new PDFPageable(document));
			job.setPrintService(myPrintService);
			job.print();
			printedFiles++;
		}

	}
	
//	public void printFile(String filename, String selectedPrinter) {
//
//		File fileToPrint = new File(Paths.get(downloadFolder.toString(), filename).toString());
//
//        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
//        pras.add(Sides.DUPLEX);
//        PrintService[] pss = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, pras);
//        if (pss.length == 0) {
//        	Dialog.setMessage("No printers available");
//        	Dialog.showAlertDialog();
//        }
//        else {
//	        int i = 0;
//	        for (i = 0; i < pss.length; i++) {
//				System.out.println(pss[i].getName());
//	            if (pss[i].getName().equals(selectedPrinter)) {
//	                break;
//	            }
//	        }
//	        if (i >= pss.length) {
//	        	Dialog.setMessage("Printer "+selectedPrinter+" not found");
//	        	Dialog.showAlertDialog();
//			}
//	        else {
//
//	        	PrintService ps = pss[i];
//		        DocPrintJob docPrintJob = ps.createPrintJob();
//
//		        FileInputStream fin = null;
//		        try {
//		            fin = new FileInputStream(fileToPrint);
//		        } catch (FileNotFoundException ex) {
//		            System.out.println(ex);
//		        }
//
//		        Doc doc = new SimpleDoc(fin, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
//		        try {
//		        	JobCompleteMonitor monitor = new JobCompleteMonitor();
//		        	docPrintJob.addPrintJobListener(monitor);
//		        	docPrintJob.print(doc, new HashPrintRequestAttributeSet());
//		            monitor.waitForJobCompletion();
//		            printedFiles++;
//		        } catch (PrintException ex) {
//		            System.out.println(ex);
//		        }
//
//		        try {
//		            fin.close();
//		        } catch (IOException ex) {
//		            System.out.println(ex);
//		        }
//	        }
//        }
//
//
//	}
	
	private class JobCompleteMonitor extends PrintJobAdapter {   
        private boolean completed = false;

        @Override
        public void printJobCanceled(PrintJobEvent pje) {signalCompletion();}

        @Override
        public void printJobCompleted(PrintJobEvent pje) {signalCompletion();}

        @Override
        public void printJobFailed(PrintJobEvent pje) {signalCompletion();}

        @Override
        public void printJobNoMoreEvents(PrintJobEvent pje) {signalCompletion();}

        private void signalCompletion() {
           synchronized (JobCompleteMonitor.this) { 
               completed = true;    
               JobCompleteMonitor.this.notify();    
           }
        }

        public synchronized void waitForJobCompletion() {    
            try {
                while (!completed) {
                    wait();
                }
            } catch (InterruptedException e) {}
        }
    }

	
	public String[] getOrderFiles(String numCommande) {
		
		FilenameFilter filefilter = new FilenameFilter() {
		@Override
			public boolean accept(File downloadFolder, String name) {
				return name.matches("^"+numCommande+"_.+\\.(pdf|zip)");
			}
		};
		listFiles= downloadFolder.list(filefilter);
		
		return listFiles;
	}
	
	
	public void unzip(String zipFilePath, String destDir) {
		
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
		
}