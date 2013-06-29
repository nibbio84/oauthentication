package it.nerdammer.oauthentication.web;

import it.nerdammer.oauthentication.User;
import it.nerdammer.oauthentication.UserID;

import java.io.IOException;
import java.util.Collection;


interface ConnectionsFinder {

	public Collection<UserID> findConnections(User user) throws IOException;
	
}
