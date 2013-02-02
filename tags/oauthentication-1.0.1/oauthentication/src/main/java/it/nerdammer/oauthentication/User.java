package it.nerdammer.oauthentication;

import java.io.Serializable;

public class User implements Serializable {

	private static final long serialVersionUID = 2123225850658467364L;

	private UserID userID;

	private String nickName;
	
	private String middleName;

	private String firstName;

	private String lastName;
	
	private String email;

	private Gender gender;

	private String locale;
	
	private String pictureUrl;
	
	private String accessToken;
	
	private Long accessTokenExpiration;

	public UserID getUserID() {
		return userID;
	}

	public void setUserID(UserID userID) {
		this.userID = userID;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getMiddleName() {
		return middleName;
	}
	
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getCompleteName() {
		StringBuilder bui = new StringBuilder();
		if(this.firstName!=null) {
			bui.append(this.firstName);
		}
		if(this.middleName!=null) {
			bui.append(" ");
			bui.append(this.middleName);
		}
		if(this.lastName!=null) {
			bui.append(" ");
			bui.append(this.lastName);
		}
		String cn = bui.toString().trim();
		if(cn.length()==0) {
			return null;
		}
		return cn;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	public String getPictureUrl() {
		return pictureUrl;
	}
	
	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public Long getAccessTokenExpiration() {
		return accessTokenExpiration;
	}
	
	public void setAccessTokenExpiration(Long accessTokenExpiration) {
		this.accessTokenExpiration = accessTokenExpiration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userID == null) ? 0 : userID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (userID == null) {
			if (other.userID != null)
				return false;
		} else if (!userID.equals(other.userID))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return this.userID + "(" + this.firstName + " " + this.lastName + ")";
	}

}
