package it.nerdammer.oauthentication.web;

import it.nerdammer.oauthentication.Gender;
import it.nerdammer.oauthentication.OauthProvider;
import it.nerdammer.oauthentication.User;
import it.nerdammer.oauthentication.UserID;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


class CommonUtils {
	
	private static final String USER_SESSION_KEY = "it.nerdammer.oauthentication.LOGGED_USER";
	private static final String REQUESTED_URL_SESSION_KEY = "it.nerdammer.oauthentication.REQUESTED_URL";
	private static final String PROVIDER_PARAMETER_KEY = "oauthProvider";
	private static final String PROVIDER_COOKIE_KEY = "it.nerdammer.oauthentication.OAUTH_PROVIDER";
	
	private static AtomicReference<OauthConfig> configReference = new AtomicReference<OauthConfig>();
	
	public static void setConfig(OauthConfig config) {
		CommonUtils.configReference.set(config);
	}
	
	public static OauthConfig getConfig() {
		return configReference.get();
	}
	
	public static void putRequestedUrlInSession(HttpSession session, String url) {
		session.setAttribute(REQUESTED_URL_SESSION_KEY, url);
	}
	
	public static String getRequestedUrlFromSession(HttpSession session) {
		return (String) session.getAttribute(REQUESTED_URL_SESSION_KEY);
	}
	
	public static void putUserInSession(HttpSession session, User user) {
		session.setAttribute(USER_SESSION_KEY, user);
	}
	
	public static User getUserFromSession(HttpSession session) {
		return (User) session.getAttribute(USER_SESSION_KEY);
	}
	
	public static void removeUserFromSession(HttpSession session) {
		if(session==null) {
			return;
		}
		session.removeAttribute(USER_SESSION_KEY);
	}
	
	public static OauthProvider getProviderFromRequest(HttpServletRequest request) {
		String providerStr = request.getParameter(PROVIDER_PARAMETER_KEY);
		if(providerStr!=null) {
			return OauthProvider.valueOf(providerStr.toUpperCase());
		}
		return null;
	}
	
	public static void putProviderAsCookie(HttpServletRequest request, HttpServletResponse response, OauthProvider provider) {
		Cookie c = new Cookie(PROVIDER_COOKIE_KEY, provider.name());
		c.setMaxAge(-1);
		c.setPath(request.getContextPath());
		response.addCookie(c);
	}
	
	public static void removeProviderCookie(HttpServletRequest request, HttpServletResponse response) {
		String currentCookie = null;
		OauthProvider currentProvider = getProviderFromCookies(request);
		if(currentProvider!=null) {
			currentCookie = currentProvider.name();
		}
		
		Cookie c = new Cookie(PROVIDER_COOKIE_KEY, currentCookie);
		c.setMaxAge(0);
		c.setPath(request.getContextPath());
		response.addCookie(c);
	}
	
	public static OauthProvider getProviderFromCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if(cookies!=null) {
			for(Cookie c : cookies) {
				if(c.getName()!=null && c.getName().equals(PROVIDER_COOKIE_KEY)) {
					String providerStr = c.getValue();
					return OauthProvider.valueOf(providerStr);
				}
			}
		}
		return null;
	}
	
	public static String buildCompleteUrl(HttpServletRequest req, String servlet) {
		int serverPort = req.getServerPort();
		String scheme = req.getScheme();
		String xScheme = req.getHeader("X-Forwarded-Proto");
		if(xScheme!=null) {
			if(xScheme.equalsIgnoreCase("http")) {
				serverPort = 80;
			} else if(xScheme.equalsIgnoreCase("https")) {
				serverPort = 443;
			}
			scheme = xScheme;
		}
		if(scheme==null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
			throw new IllegalStateException("Unsupported protocol: " + scheme);
		}
		
		String serverName = req.getServerName();
		String xHost = req.getHeader("X-Forwarded-Host");
		if(xHost!=null) {
			serverName = xHost;
		}
		
		
		
		String contextPath = req.getContextPath();
		
		StringBuilder bui = new StringBuilder();
		bui.append(scheme);
		bui.append("://");
		bui.append(serverName);
		bui.append(":");
		bui.append(serverPort);
		bui.append(contextPath);
		bui.append(servlet);
		
		return bui.toString();
	}
	
	public static Map<String, String> parseQueryString(String query) {
		Map<String, String> params = new TreeMap<String, String>();
		
		String[] pieces = query.split("&");
		for(String piece : pieces) {
			String[] parKV = piece.split("=");
			String key = parKV[0];
			String value = parKV[1];
			params.put(key, value);
		}
		
		return params;
	}
	
	public static User mapFacebookUser(Map<String, Object> profile, String accessToken, Long expiration) throws UnsupportedEncodingException {
		String id = (String) profile.get("id");
		String firstName = (String) profile.get("first_name");
		String middleName = (String) profile.get("middle_name");
		String lastName = (String) profile.get("last_name");
		String nickName = (String) profile.get("username");
		
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
		
		String pictureUrl = "https://graph.facebook.com/" + URLEncoder.encode(id, "UTF-8") + "/picture";
		
		// Composizione
		UserID userId = new UserID(OauthProvider.FACEBOOK, id);
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
		
		return user;
	}
	
	public static boolean isAjaxRequest(HttpServletRequest request) {
		return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
	}
	
}
