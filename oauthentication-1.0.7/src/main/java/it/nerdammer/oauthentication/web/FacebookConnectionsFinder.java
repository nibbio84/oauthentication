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


class FacebookConnectionsFinder implements ConnectionsFinder {

	public Collection<UserID> findConnections(User user) throws IOException {
		try {
			String accessToken = user.getAccessToken();
			
			String url = "https://graph.facebook.com/me/friends?" +
					"access_token=" + URLEncoder.encode(accessToken, "UTF-8");
			
			Collection<UserID> users = new HashSet<UserID>();
			retrieve(url, users);
			
			return users;
		} catch(UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
	
	protected void retrieve(String urlStr, Collection<UserID> collection) throws MalformedURLException, IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		InputStream in = conn.getInputStream();
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> friendsData = mapper.readValue(in, new TypeReference<Map<String, Object>>() {
		});
		
		conn.disconnect();
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> friendList = (List<Map<String,Object>>) friendsData.get("data");
		
		for(Map<String, Object> f : friendList) {
			String fid = (String) f.get("id");
			UserID id = new UserID(OauthProvider.FACEBOOK, fid);
			collection.add(id);
		}

		// Recursively retrieve all other friends
		@SuppressWarnings("unchecked")
		Map<String, Object> paging = (Map<String, Object>) friendsData.get("paging");
		if(paging!=null) {
			String next = (String) paging.get("next");
			if(next!=null) {
				retrieve(next, collection);
			}
		}
		
	}
	
}
