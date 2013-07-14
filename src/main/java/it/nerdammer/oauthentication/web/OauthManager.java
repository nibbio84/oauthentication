package it.nerdammer.oauthentication.web;

import it.nerdammer.oauthentication.OauthProvider;
import it.nerdammer.oauthentication.User;
import it.nerdammer.oauthentication.UserID;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
		} else if(provider.equals(OauthProvider.GOOGLE)) {
			finder = new GoogleConnectionsFinder();
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
	
	public static String buildShareLink(HttpServletRequest req, String page) {
		User user = getCurrentUser(req);
		if(user==null) {
			throw new IllegalStateException("Not logged in");
		}
		
		if(!user.getUserID().getProvider().equals(OauthProvider.FACEBOOK)) {
			throw new IllegalStateException("Only Facebook is supported");
		}
		
		String pageUrl = CommonUtils.buildCompleteUrl(req, page);
		try {
			pageUrl = URLEncoder.encode(pageUrl, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
		
		String url = "https://www.facebook.com/sharer/sharer.php?u=" + pageUrl;
		return url;
	}
	
	
	
		
	public static String buildInviteFriendsLink(HttpServletRequest req, String desiredCallback, String message) {
		User user = getCurrentUser(req);
		if(user==null) {
			throw new IllegalStateException("Not logged in");
		}
		
		if(!user.getUserID().getProvider().equals(OauthProvider.FACEBOOK)) {
			throw new IllegalStateException("Only Facebook is supported");
		}
		
		String callback;
		try {
			if(CommonUtils.isInsideCanvasFromSession(req.getSession(true)) && CommonUtils.getConfig().getFacebookCanvasPage()!=null) {
				callback = CommonUtils.getConfig().getFacebookCanvasPage();
				callback = URLEncoder.encode(callback, "UTF-8");
			} else {
				callback = CommonUtils.buildCompleteUrl(req, desiredCallback);
				callback = URLEncoder.encode(callback, "UTF-8");
			}
			message = URLEncoder.encode(message, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
		
		String url = "https://www.facebook.com/dialog/apprequests?app_id=" + CommonUtils.getConfig().getFacebookAppID() + "&redirect_uri=" + callback + "&message=" + message;
		return url;
	}
	
}
