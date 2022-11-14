package models;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

public class Woocommerce {
	
	private static String store_url = "https://just-save-it.com";
	private static String consumer_key = "ck_2c39e027bd7cab74c67549c80ca3893849d67950";
	private static String consumer_secret = "cs_948f94a7e2977cabe6a2cf3b3a3380ef3cebc362";
	
	public static void UpdateOrderStatus(String commande_id) {

		try {
		
			URL url = new URL(store_url+"/wp-json/wc/v3/orders/"+commande_id);
			HttpsURLConnection http = (HttpsURLConnection)url.openConnection();
			http.setRequestMethod("PUT");
			http.setDoOutput(true);
			String auth = consumer_key + ":" + consumer_secret;
			byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
			String authHeaderValue = "Basic " + new String(encodedAuth);
			http.setRequestProperty("Content-Type", "application/json");
			http.setRequestProperty("Authorization", authHeaderValue);
	
			String data = "{\n  \"status\": \"completed\"\n}";
	
			byte[] out = data.getBytes(StandardCharsets.UTF_8);
	
			OutputStream stream = http.getOutputStream();
			stream.write(out);
	
			if (http.getResponseCode() == 200) {
				Dialog.setMessage("Order completed");			
				Dialog.showAlertDialog();
			}
			else {
				System.out.println(http.getResponseCode() + " " + http.getResponseMessage());				
			}
			
			http.disconnect();
			
		} catch (Exception e) {
			System.out.println(e);
		}



	}
}