package it.nerdammer.oauthentication.web;

import it.nerdammer.oauthentication.OauthProvider;
import it.nerdammer.oauthentication.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;



public class FacebookLoginCanvasServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		this.doGet(req, res);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		Logger logger = Logger.getAnonymousLogger();
		if(logger.isLoggable(Level.FINER)) {
			logger.finer("Facebook-Oauth-Canvas. Received canvas request. Printing parameters");
			Map<String, String[]> pars = req.getParameterMap();
			for(String p : pars.keySet()) {
				logger.finer(p + ": " + req.getParameter(p));
			}
		}
		
		String signedRequest = req.getParameter("signed_request");
		
		Logger.getAnonymousLogger().finer("Signed Request: " + signedRequest);
		
		String[] requestParts = signedRequest.split("\\.");
		
		String signature = requestParts[0];
		String payload = requestParts[1];
		byte[] signatureByte = Base64.decodeBase64(signature);
		byte[] payloadByte = Base64.decodeBase64(payload);
		
		OauthConfig config = CommonUtils.getConfig();
		String appSecret = config.getFacebookAppSecret();
		
		byte[] expectedSignature;
		try {
			Mac sha = Mac.getInstance("HmacSHA256");
			byte[] secretBytes = appSecret.getBytes("UTF-8");
			SecretKeySpec key = new javax.crypto.spec.SecretKeySpec(secretBytes, "HmacSHA256");
			sha.init(key);
			expectedSignature = sha.doFinal(payload.getBytes("UTF-8"));
		} catch(Exception e) {
			Logger.getAnonymousLogger().warning("Error while verifying the signature");
			Logger.getAnonymousLogger().throwing(this.getClass().getCanonicalName(), "doGet", e);
			String errorPage = config.getLoginErrorPage();
			String errorUrl = CommonUtils.buildCompleteUrl(req, errorPage);
			res.sendRedirect(errorUrl);
			return;
		}
		
		if(!Arrays.equals(expectedSignature, signatureByte)) {
			res.sendError(403, "Signature does not match");
			return;
		}
		
		ObjectMapper messageMapper = new ObjectMapper();
		Map<String, Object> message = messageMapper.readValue(payloadByte, new TypeReference<Map<String, Object>>() {
		});
		
		String accessToken = (String)message.get("oauth_token");
		
		if(accessToken==null) {
			logger.info("Access token not found, redirect to login");
			String reqUrl = CommonUtils.getRequestedUrlFromSession(req.getSession(true));
			String topRedirector = CommonUtils.buildCompleteUrl(req, "/oauthentication/top_redirector?url=" + URLEncoder.encode(reqUrl, "UTF-8"));
			res.sendRedirect(topRedirector);
			return;
		}
		
		Number expires = (Number)message.get("expires");
		Long expiration = null;
		if(expires!=null) {
			Long expiresLong = expires.longValue();
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
		
		HttpSession session = req.getSession(true);
		
		CommonUtils.putUserInSession(session, user);
		// set the provider cookie 
		CommonUtils.putProviderAsCookie(req, res, OauthProvider.FACEBOOK);
				
		String gotoPage = CommonUtils.getRequestedUrlFromSession(session);
		
		res.sendRedirect(gotoPage);
	}
	
}
