package it.nerdammer.oauthentication.web;

import it.nerdammer.oauthentication.OauthProvider;

import java.io.Serializable;


class OauthConfig implements Serializable {

	private static final long serialVersionUID = -3851065306053838875L;

	protected OauthProvider defaultProvider;
	
	protected String loginErrorPage;

	protected String facebookAppID;

	protected String facebookAppSecret;
	
	protected String googleClientID;
	
	protected String googleClientSecret;

	public OauthProvider getDefaultProvider() {
		return defaultProvider;
	}

	public void setDefaultProvider(OauthProvider defaultProvider) {
		this.defaultProvider = defaultProvider;
	}

	public String getFacebookAppID() {
		return facebookAppID;
	}
	
	public String getLoginErrorPage() {
		return loginErrorPage;
	}
	
	public void setLoginErrorPage(String loginErrorPage) {
		this.loginErrorPage = loginErrorPage;
	}

	public void setFacebookAppID(String facebookAppID) {
		this.facebookAppID = facebookAppID;
	}

	public String getFacebookAppSecret() {
		return facebookAppSecret;
	}

	public void setFacebookAppSecret(String facebookAppSecret) {
		this.facebookAppSecret = facebookAppSecret;
	}

	public String getGoogleClientID() {
		return googleClientID;
	}
	
	public void setGoogleClientID(String googleClientID) {
		this.googleClientID = googleClientID;
	}
	
	public String getGoogleClientSecret() {
		return googleClientSecret;
	}
	
	public void setGoogleClientSecret(String googleClientSecret) {
		this.googleClientSecret = googleClientSecret;
	}
	
}
