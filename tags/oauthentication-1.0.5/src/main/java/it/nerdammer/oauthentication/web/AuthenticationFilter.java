package it.nerdammer.oauthentication.web;

import it.nerdammer.oauthentication.OauthProvider;
import it.nerdammer.oauthentication.User;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


public class AuthenticationFilter implements Filter {

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		
		HttpSession session = ((HttpServletRequest)req).getSession(true);
		
		User user = CommonUtils.getUserFromSession(session);
		
		if(user!=null) {
			chain.doFilter(req, res);
			return;
		}
		
		// Get the requested resource
		String url = ((HttpServletRequest) req).getRequestURL().toString();
		String query = ((HttpServletRequest) req).getQueryString();
		if(query!=null) {
			url += "?" + query;
		}
		CommonUtils.putRequestedUrlInSession(session, url);
		
		// User not authenticated
		req.getRequestDispatcher("/oauthentication/login").forward(req, res);
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		
		if(CommonUtils.getConfig()!=null) {
			Logger.getAnonymousLogger().finer("OauthConfig present, ignoring filter configuration");
		}
		
		String defaultProviderStr = getParameter("DEFAULT_PROVIDER", filterConfig, true);
		// Get provider or throw IllegalArgumentException
		OauthProvider defaultProvider = OauthProvider.valueOf(defaultProviderStr.toUpperCase());
		
		String loginErrorPage = getParameter("LOGIN_ERROR_PAGE", filterConfig, true);
		
		String facebookAppID = getParameter("FACEBOOK_APP_ID", filterConfig, false);
		String facebookAppSecret = getParameter("FACEBOOK_APP_SECRET", filterConfig, false);
		boolean facebookConfigPresent = facebookAppID!=null || facebookAppSecret!=null;
		boolean facebookConfigComplete = facebookAppID!=null && facebookAppSecret!=null;
		if(facebookConfigPresent && !facebookConfigComplete) {
			throw new IllegalStateException("facebook config is not complete, parameters: FACEBOOK_APP_ID, FACEBOOK_APP_SECRET");
		}
		if(defaultProvider.equals(OauthProvider.FACEBOOK) && !facebookConfigPresent) {
			throw new IllegalStateException("default provider configuration is not present: facebook");
		}
		
		String googleClientID = getParameter("GOOGLE_CLIENT_ID", filterConfig, false);
		String googleClientSecret = getParameter("GOOGLE_CLIENT_SECRET", filterConfig, false);
		boolean googleConfigPresent = googleClientID!=null || googleClientSecret!=null;
		boolean googleConfigComplete = googleClientID!=null && googleClientID!=null;
		if(googleConfigPresent && !googleConfigComplete) {
			throw new IllegalStateException("google config is not complete, parameters: GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET");
		}
		if(defaultProvider.equals(OauthProvider.GOOGLE) && !googleConfigPresent) {
			throw new IllegalStateException("default provider configuration is not present: google");
		}
		
		
		OauthConfig config = new OauthConfig();
		config.setDefaultProvider(defaultProvider);
		config.setLoginErrorPage(loginErrorPage);
		
		config.setFacebookAppID(facebookAppID);
		config.setFacebookAppSecret(facebookAppSecret);
		
		config.setGoogleClientID(googleClientID);
		config.setGoogleClientSecret(googleClientSecret);
		
		CommonUtils.setConfig(config);
		Logger.getAnonymousLogger().info("oAuthentication configuration initialized");
	}
	
	protected static String getParameter(String param, FilterConfig config, boolean mandatory) {
		String val = config.getInitParameter(param);
		if(mandatory && val==null) {
			throw new IllegalStateException("Missing mandatory parameter " + param);
		}
		
		return val;
	}
	
	public void destroy() {
	}
	
	
}
