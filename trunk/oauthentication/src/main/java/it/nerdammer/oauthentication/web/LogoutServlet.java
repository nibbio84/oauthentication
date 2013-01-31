package it.nerdammer.oauthentication.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class LogoutServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		String page = req.getParameter("page");
		if(page==null) {
			throw new IllegalArgumentException("\"page\" parameter not found");
		}
		
		OauthManager.logoutCurrentUser(req, res);
		
		res.sendRedirect(page);
	}
	

}
