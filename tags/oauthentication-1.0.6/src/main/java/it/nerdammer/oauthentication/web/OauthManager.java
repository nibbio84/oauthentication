package it.nerdammer.oauthentication.web;

import it.nerdammer.oauthentication.OauthProvider;
import it.nerdammer.oauthentication.User;
import it.nerdammer.oauthentication.UserID;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class OauthManager {

	public static User getCurrentUser(HttpSession session) {
		if(session==null) {
			return null;
		}
		User user = CommonUtils.getUserFromSession(session);
		return user;
	}
	
	public static User getCurrentUser(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if(session!=null) {
			return getCurrentUser(session);
		}
		return null;
	}
	
	public static void logoutCurrentUser(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		if(session!=null) {
			CommonUtils.removeUserFromSession(session);
		}
		CommonUtils.removeProviderCookie(request, response);
	}
	
	public static Collection<UserID> getProviderConnections(User user) {
		
		ConnectionsFinder finder;
		OauthProvider provider = user.getUserID().getProvider();
		if(provider.equals(OauthProvider.FACEBOOK)) {
			finder = new FacebookConnectionsFinder();
		} else {
			throw new IllegalArgumentException("Unsupported provider: " + provider);
		}
		
		String accessToken = user.getAccessToken();
		Long timeout = user.getAccessTokenExpiration();
		if(accessToken!=null && timeout!=null && timeout<System.currentTimeMillis()) {
			throw new IllegalArgumentException("Token expired");
		}
		
		// find the connections
		try {
			return finder.findConnections(user);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
