package it.nerdammer.oauthentication.web;

import it.nerdammer.oauthentication.OauthProvider;
import it.nerdammer.oauthentication.User;
import it.nerdammer.oauthentication.UserID;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;


class GoogleConnectionsFinder implements ConnectionsFinder {

	public Collection<UserID> findConnections(User user) throws IOException {
		try {
			String accessToken = user.getAccessToken();
			String userId = user.getUserID().getProviderCode();
			
			String url = "https://www.googleapis.com/plus/v1/people/" + URLEncoder.encode(userId, "UTF-8") + "/people/visible" +
					"?access_token=" + URLEncoder.encode(accessToken, "UTF-8");
			
			Collection<UserID> users = new HashSet<UserID>();
			retrieve(url, null, users);
			
			return users;
		} catch(UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
	
	protected void retrieve(String urlStr, String pageToken, Collection<UserID> collection) throws MalformedURLException, IOException {
		String composedURL = urlStr;
		if(pageToken!=null) {
			composedURL += "&pageToken=" + URLEncoder.encode(pageToken, "UTF-8");
		}
		URL url = new URL(composedURL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		InputStream in = conn.getInputStream();
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> friendsData = mapper.readValue(in, new TypeReference<Map<String, Object>>() {
		});
		
		conn.disconnect();
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> friendList = (List<Map<String,Object>>) friendsData.get("items");
		
		for(Map<String, Object> f : friendList) {
			String fid = (String) f.get("id");
			UserID id = new UserID(OauthProvider.GOOGLE, fid);
			collection.add(id);
		}

		// Recursively retrieve all other friends
		String nextPageToken = (String) friendsData.get("nextPageToken");
		if(nextPageToken!=null) {
			retrieve(urlStr, nextPageToken, collection);
		}
		
	}
	
}
