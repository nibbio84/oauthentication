package it.nerdammer.oauthentication.web;

import it.nerdammer.oauthentication.Gender;
import it.nerdammer.oauthentication.OauthProvider;
import it.nerdammer.oauthentication.User;
import it.nerdammer.oauthentication.UserID;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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



public class GoogleLoginCallbackServlet extends HttpServlet {

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
		
		String googleStateKey = "it.nerdammer.oauthentication.GOOGLE_STATE";
		String sessionState = (String) session.getAttribute(googleStateKey);
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
		
		String clientId = config.getGoogleClientID();
		String clientSecret = config.getGoogleClientSecret();
		
		// Build login callback
		String loginCallback = CommonUtils.buildCompleteUrl(req, "/oauthentication/google_login_callback");
		
		if(clientId==null || clientSecret==null)
			throw new IllegalStateException("Google config not set");
		
		String urlString = "https://accounts.google.com/o/oauth2/token";
		String urlQuery = "code=" + URLEncoder.encode(code, "UTF-8") +
				"&client_id=" + URLEncoder.encode(clientId, "UTF-8") +
				"&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8") +
				"&redirect_uri=" + URLEncoder.encode(loginCallback, "UTF-8") +
				"&grant_type=authorization_code";
		
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
		pw.print(urlQuery);
		pw.flush();
		pw.close();
		
		String contentEncoding = conn.getContentEncoding();
		if(contentEncoding==null) {
			contentEncoding = "UTF-8";
		}
		
		InputStream in = conn.getInputStream();
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> auth = mapper.readValue(in, new TypeReference<Map<String, Object>>() {
		});
		
		conn.disconnect();
		
		Logger.getAnonymousLogger().finer("Response from Google: " + auth);
		
		String accessToken = (String) auth.get("access_token");
		Integer expiresSeconds = (Integer) auth.get("expires_in");
		Long expiration = (expiresSeconds == null) ? null : System.currentTimeMillis() + (expiresSeconds * 1000L);
		if(expiration!=null) {
			Logger.getAnonymousLogger().info("Token expiration " + new Date(expiration));
		}
		
		
		String meUrlString = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + URLEncoder.encode(accessToken, "UTF-8");
		
		URL meUrl = new URL(meUrlString);
		HttpURLConnection connUrl = (HttpURLConnection) meUrl.openConnection();
		
		InputStream inUrl = connUrl.getInputStream();
		
		ObjectMapper mapper2 = new ObjectMapper();
		Map<String, Object> profile = mapper2.readValue(inUrl, new TypeReference<Map<String, Object>>() {
		});
		
		connUrl.disconnect();
		
		Logger.getAnonymousLogger().finer("Google profile: " + profile);
		
		String id = (String) profile.get("id");
		String firstName = (String) profile.get("given_name");
		String middleName = null;
		String lastName = (String) profile.get("family_name");
		String nickName = (String) profile.get("name");
		
		String genderStr = (String) profile.get("gender");
		Gender gender = null;
		for(Gender g : Gender.values()) {
			if(g.name().equalsIgnoreCase(genderStr)) {
				gender = g;
				break;
			}
		}
		
		String locale = (String) profile.get("locale");
		String email = (String) profile.get("email");
		
		String pictureUrl = (String) profile.get("picture");
		
		// Composizione
		UserID userId = new UserID(OauthProvider.GOOGLE, id);
		User user = new User();
		user.setUserID(userId);
		user.setFirstName(firstName);
		user.setMiddleName(middleName);
		user.setLastName(lastName);
		user.setNickName(nickName);
		user.setGender(gender);
		user.setEmail(email);
		user.setLocale(locale);
		user.setPictureUrl(pictureUrl);
		user.setAccessToken(accessToken);
		user.setAccessTokenExpiration(expiration);
		
		CommonUtils.putUserInSession(session, user);
		// set the provider cookie 
		CommonUtils.putProviderAsCookie(req, res, OauthProvider.GOOGLE);
		
		String requestedUrl = CommonUtils.getRequestedUrlFromSession(session);
		res.sendRedirect(requestedUrl);
		
	}
	
}
