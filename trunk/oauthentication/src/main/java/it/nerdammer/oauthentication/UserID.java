package it.nerdammer.oauthentication;

import java.io.Serializable;

public class UserID implements Serializable {

	private static final long serialVersionUID = 6007068747332530381L;

	private OauthProvider provider;
	
	private String providerCode;
	
	public UserID(OauthProvider provider, String providerCode) {
		if(provider==null || providerCode==null) {
			throw new IllegalArgumentException("code and provider cannot be null");
		}
		
		this.provider = provider;
		this.providerCode = providerCode;
	}
	
	public OauthProvider getProvider() {
		return provider;
	}
	
	public String getProviderCode() {
		return providerCode;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ provider.hashCode();
		result = prime * result + providerCode.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UserID) {
			UserID ui = (UserID) obj;
			return this.provider.equals(ui.provider) && this.providerCode.equals(ui.providerCode);
		}
		return false;
	}

	@Override
	public String toString() {
		return this.provider + "-" + this.providerCode;
	}
	
}
