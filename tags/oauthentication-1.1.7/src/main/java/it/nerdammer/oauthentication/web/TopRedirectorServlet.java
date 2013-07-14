package it.nerdammer.oauthentication.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TopRedirectorServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		PrintWriter out = res.getWriter();
		
		String url = req.getParameter("url");
		checkUrl(url);
		
		out.println("<!DOCTYPE HTML>");
		out.println("<html lang=\"en\">");
		out.println("<head>");
		out.println("<title>Facebook authorization</title>");
		out.println("");
		out.println("<script type=\"text/javascript\">");
		out.println("	function oAuthenticate() {");
		out.println("		window.top.location.href=\"" + encode(url) + "\";");
		out.println("	}");
		out.println("</script>");
		out.println("");
		out.println("</head>");
		out.println("<body onload=\"javascript:oAuthenticate()\">");
		out.println("");
		out.println("</body>");
		out.println("</html>");
		
	}
	
	private String encode(String source) throws UnsupportedEncodingException {
		source = source.replace("\"", "\\\"");
		return source;
	}
	
	private void checkUrl(String url) {
		// allow any url
	}
	
}
