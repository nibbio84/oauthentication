package it.nerdammer.oauthentication.web;

import it.nerdammer.oauthentication.OauthProvider;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		OauthConfig config = CommonUtils.getConfig();
		
		
		OauthProvider provider = CommonUtils.getProviderFromRequest(req);
		if(provider==null) {
			provider = CommonUtils.getProviderFromCookies(req);
		}
		if(provider==null) {
			provider = config.getDefaultProvider();
		}
		
		
		if(provider.equals(OauthProvider.FACEBOOK)) {
			// FACEBOOK
			String appID = config.getFacebookAppID();
			if(appID==null) {
				throw new IllegalStateException("Missing facebook App ID");
			}
			
			String state = UUID.randomUUID().toString();
			req.getSession(true).setAttribute("it.nerdammer.oauthentication.FACEBOOK_STATE", state);
		
			// Build login callback
			String loginCallback = CommonUtils.buildCompleteUrl(req, "/oauthentication/facebook_login_callback");
			
			res.sendRedirect("https://www.facebook.com/dialog/oauth?" +
				"client_id=" + URLEncoder.encode(appID, "UTF-8") +
				"&redirect_uri=" + URLEncoder.encode(loginCallback, "UTF-8") +
				"&state=" + URLEncoder.encode(state, "UTF-8") +
				"&scope=email");
			
		} else if(provider.equals(OauthProvider.GOOGLE)) {
			// GOOGLE
			String clientID = config.getGoogleClientID();
			if(clientID==null) {
				throw new IllegalStateException("Missing google Client ID");
			}
			
			String state = UUID.randomUUID().toString();
			req.getSession(true).setAttribute("it.nerdammer.oauthentication.GOOGLE_STATE", state);
		
			// Build login callback
			String loginCallback = CommonUtils.buildCompleteUrl(req, "/oauthentication/google_login_callback");
			
			res.sendRedirect("https://accounts.google.com/o/oauth2/auth" +
					"?state=" + URLEncoder.encode(state, "UTF-8") +
					"&redirect_uri=" + URLEncoder.encode(loginCallback, "UTF-8") +
					"&response_type=code" +
					"&client_id=" + URLEncoder.encode(clientID, "UTF-8") +
					//"&approval_prompt=force" +
					"&scope=" + URLEncoder.encode("https://www.googleapis.com/auth/userinfo.email" +
													" https://www.googleapis.com/auth/userinfo.profile" +
													" https://www.googleapis.com/auth/plus.login", "UTF-8"));
			
		} else {
			throw new UnsupportedOperationException("Login for provider " + provider + " not implemented yet");
		}
		
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doGet(req, res);
	}
	

}
