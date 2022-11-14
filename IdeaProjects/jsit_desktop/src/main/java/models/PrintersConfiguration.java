package models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class PrintersConfiguration {
	
	
	private static Path getConfigPath() {
        return Paths.get(System.getProperty("user.home"), ".just-save-it", "config.json");
    }
	
	public File getConfigFile() {
		File configFile = new File(getConfigPath().toString());
		if (!configFile.exists()) {
			System.out.println("file not found");
			File configFolder = configFile.getParentFile();
			if (!configFolder.exists()) {
				System.out.println("folder not found");
				if (!configFolder.mkdirs()) {
                    throw new IllegalStateException("Couldn't create dir: " + configFolder);
				} else {
					System.out.println("folder created");
				}
			}
			try {
				if (configFile.createNewFile()) {
					System.out.println("file created");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		return configFile;
	}
	
	public ArrayList getConfigFileData() {
		File configFile = getConfigFile();
        String configFileData = null;
		try {
			configFileData = new String(Files.readAllBytes(configFile.toPath()));
		} catch (IOException e) {
			System.out.println("couldn't read file");
		}
		
		return new Gson().fromJson(configFileData, new ArrayList<Map<String, String>>().getClass());
	}

	public String getDownloadFolderPath(){
		ArrayList<LinkedTreeMap<String, String>> configFileData = getConfigFileData();
		if (configFileData != null){
			return configFileData.get(configFileData.size()-1).get("download_folder_path");
		}
		return null;
	}


	public void updateConfigFileData(ObservableList<ObservableMap<String, String>> config) {

		if (config != null) {
			String jsonConfig = new GsonBuilder().setPrettyPrinting().create().toJson(config);
			try {
				Files.write(getConfigPath(), jsonConfig.getBytes(Charset.forName("UTF-8")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("config added");
		}
	}

	
	
}