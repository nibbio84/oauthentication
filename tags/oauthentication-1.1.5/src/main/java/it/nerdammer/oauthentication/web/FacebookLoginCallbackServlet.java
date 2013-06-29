package it.nerdammer.oauthentication.web;

import it.nerdammer.oauthentication.OauthProvider;
import it.nerdammer.oauthentication.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;



public class FacebookLoginCallbackServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		String code = req.getParameter("code");
		String state = req.getParameter("state");
		
		Logger.getAnonymousLogger().finer("Code: " + code);
		Logger.getAnonymousLogger().finer("State: " + state);
		
		HttpSession session = req.getSession(false);
		if(session==null) {
			throw new SecurityException("No session active");
		}
		
		String facebookStateKey = "it.nerdammer.oauthentication.FACEBOOK_STATE";
		String sessionState = (String) session.getAttribute(facebookStateKey);
		Logger.getAnonymousLogger().finer("Session state: " + sessionState);
		if(sessionState==null || !sessionState.equals(state)) {
			throw new SecurityException("CSRF attack prevented");
		}
		
		OauthConfig config = CommonUtils.getConfig();
		
		// If code is null the authentication failed
		if(code==null) {
			String errorPage = config.getLoginErrorPage();
			String errorUrl = CommonUtils.buildCompleteUrl(req, errorPage);
			res.sendRedirect(errorUrl);
			return;
		}
		
		
		String appId = config.getFacebookAppID();
		String appSecret = config.getFacebookAppSecret();
		
		// Build login callback
		String loginCallback = CommonUtils.buildCompleteUrl(req, "/oauthentication/facebook_login_callback");
		
		if(appId==null || appSecret==null)
			throw new IllegalStateException("Facebook config not set");
		
		String urlString = "https://graph.facebook.com/oauth/access_token?" +
				"client_id=" + URLEncoder.encode(appId, "UTF-8") +
				"&redirect_uri=" + URLEncoder.encode(loginCallback, "UTF-8") +
				"&client_secret=" + URLEncoder.encode(appSecret, "UTF-8") +
				"&code=" + code;
		
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		String contentEncoding = conn.getContentEncoding();
		if(contentEncoding==null) {
			contentEncoding = "UTF-8";
		}
		
		InputStream in = conn.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in, contentEncoding));
		
		String response = br.readLine();
		
		conn.disconnect();
		
		Logger.getAnonymousLogger().finer("Response from Facebook: " + response);
		
		Map<String, String> params = CommonUtils.parseQueryString(response);
		String accessToken = params.get("access_token");
		String expires = params.get("expires");
		Long expiration = null;
		if(expires!=null) {
			Long expiresLong = Long.parseLong(expires);
			expiration = System.currentTimeMillis() + expiresLong;
			Logger.getAnonymousLogger().info("Token expiration " + new Date(expiration));
		}
		
		String meUrlString = "https://graph.facebook.com/me?access_token=" + URLEncoder.encode(accessToken, "UTF-8");
		
		URL meUrl = new URL(meUrlString);
		HttpURLConnection connUrl = (HttpURLConnection) meUrl.openConnection();
		
		InputStream inUrl = connUrl.getInputStream();
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> profile = mapper.readValue(inUrl, new TypeReference<Map<String, Object>>() {
		});
		
		connUrl.disconnect();
		
		Logger.getAnonymousLogger().finer("Facebook profile: " + profile);
		
		User user = CommonUtils.mapFacebookUser(profile, accessToken, expiration);
		
		CommonUtils.putUserInSession(session, user);
		// set the provider cookie 
		CommonUtils.putProviderAsCookie(req, res, OauthProvider.FACEBOOK);
				
		
		String requestedUrl = CommonUtils.getRequestedUrlFromSession(session);
		res.sendRedirect(requestedUrl);
	}
	
}
