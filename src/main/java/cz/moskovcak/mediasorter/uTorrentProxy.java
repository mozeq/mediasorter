package cz.moskovcak.mediasorter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class uTorrentProxy {
	String url = null;
	String authToken = null;
	List<String> cookies = null;

	uTorrentProxy(String uTorrentURL) {
		url = uTorrentURL.endsWith("/") ? uTorrentURL : (uTorrentURL + "/");

		if (!initAuthToken()) {
			System.err.println("Can't get the auth token");
		}
	}

	private HttpURLConnection createConnection(String requestURL) throws MalformedURLException, IOException {
		//System.out.println("Connecting to: '" + requestURL + "'");

		HttpURLConnection connection = (HttpURLConnection) new URL(requestURL).openConnection();

		//connection.setRequestMethod("GET");
		//Base64 encoded name+password
		connection.setRequestProperty("Authorization", "Basic YWRtaW46a29rb3RpY2U=");

		if (cookies != null){
			for (String cookie: cookies) {
				connection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
			}
		}

		return connection;
	}

	boolean initAuthToken() {
		String r = url + "gui/token.html";
		HttpURLConnection con = null;
		try {
			con = createConnection(r);
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		this.cookies = con.getHeaderFields().get("Set-Cookie");

		//for (String s: cookies) {
		//	System.out.println("Cookie: " + s);
		//}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		}
		catch (Exception ex){
			System.out.println("Can't get input stream\n" + ex +"\n" + r);
		}

		String html = null;
		String line = null;
		try {
			while((line = reader.readLine()) != null)
				html += line;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//l<html><div id='token' style='display:none;'>TVKcqI_Sjdsv3cPLYAK-EVXoxhIrR8LuN-0-ABSGqNSDqGFMBxScDNagx1AAAAAA</div></html>
		try {

            //Document doc = Jsoup.connect(url + "gui/token.html").get();
            Document doc = Jsoup.parse(html);

            Element token = doc.getElementById("token");
            authToken = token.text();
            //System.out.println("using auth token: " + authToken);
            return true;
        }
        catch (Exception e) {
        	System.out.println(e);
        }

		return false;
	}

	//http://[IP]:[PORT]/gui/?action=remove&hash=[TORRENT HASH]
	boolean removeTorrent(String torrentHash) throws MalformedURLException, IOException {
		boolean retval = false;
		String request = url + "gui/?token="+ this.authToken +"&action=remove&hash=" + torrentHash;
		HttpURLConnection con = createConnection(request);

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		}
		catch (Exception ex){
			System.out.println("Can't get input stream\n" + ex);
			return retval;
		}

		String jsonString = "";
		String line = null;
		//System.out.println("Response from the server: >>>>");
		try {
			while ((line = reader.readLine()) != null) {
				jsonString += line;
			}
			//System.out.println(">> '" + jsonString + "'");
			jsonString = jsonString.trim().replace("]", "").replace("[", "");
			if (jsonString.length() > 2) {
				retval = true;
				//response = new JSONObject(jsonString);
				//System.out.println("<>" + jsonString);
			}
		}
		catch (IOException ex) {
			System.out.println("error while reading from stream: " + ex);
			retval = false;
		}

		return retval;
	}
}
