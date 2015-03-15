# oauthentication
oAuthentication is a Java EE module that provides a configurable API to handle authentication using the oAuth protocol.
It can be easily configured using the web.xml file.

It requires Java EE 6 or Java EE 6 Web Profile (JBoss 6, Tomcat 7 and many others).

It currently supports the following oAuth providers:

* Facebook
* Google

## Enable oAuth
You need to activate the oAuth authentication and authorization services in order to use this library. 
The procedure is different, depending on the provider you want to use.

### oAuth on Facebook
First, subscribe to the [Facebook developers website](https://developers.facebook.com), then create a new application and remember to specify the real URL of your web application, 
for example: [http://mydomain.org/](http://mydomain.org/), or [http://localhost:8080/](http://localhost:8080/).


Take note of the following values:

* App ID/API Key
* Application secret key

You need to put these values in the oAuthentication configuration (web.xml).

### Google
Access the Google [APIs console](https://code.google.com/apis/console?hl=it#access) and, under API Access, create a new Client ID.
When prompted, you need to specify the complete redirect URL for the oAuth protocol. It must be:

* http{s}://{your-domain-and-path}/oauthentication/google_login_callback

You can specify more than one redirect URLs.

Take note of the following values:

* Client ID
* Client Secret

You need to put these values in the oAuthentication configuration (web.xml).

To complete the configuration, go to the "services" tab and enable "Google+ API".

## Configuration
Include the oAuthentication jar into your web application using maven:

```
<dependency>
	<groupId>it.nerdammer</groupId>
	<artifactId>oauthentication</artifactId>
	<version>#FIND LATEST VERSION FROM MAVEN SEARCH#</version>
</dependency>
```


Configure your web.xml specifying the pages you want to protect:

```
<filter>
	<filter-name>oAuthenticationFilter</filter-name>
	<filter-class>it.nerdammer.oauthentication.web.AuthenticationFilter</filter-class>
	<init-param>
		<param-name>DEFAULT_PROVIDER</param-name>
		<param-value>google</param-value>
	</init-param>
	<init-param>
		<param-name>LOGIN_ERROR_PAGE</param-name>
		<param-value>/myErrorPage</param-value>
	</init-param>
	<init-param>
		<param-name>FACEBOOK_APP_ID</param-name>
		<param-value>--get an app id from facebook--</param-value>
	</init-param>
	<init-param>
		<param-name>FACEBOOK_APP_SECRET</param-name>
		<param-value>--the app secret from facebook--</param-value>
	</init-param>
	<init-param>
		<param-name>GOOGLE_CLIENT_ID</param-name>
		<param-value>--get a client id from google app--</param-value>
	</init-param>
	<init-param>
		<param-name>GOOGLE_CLIENT_SECRET</param-name>
		<param-value>--the client secret from google app--</param-value>
	</init-param>
</filter>

<filter-mapping>
	<filter-name>oAuthenticationFilter</filter-name>
	<url-pattern>/web/private/*</url-pattern>
</filter-mapping>
```

You can specify all the providers or just one. The default provider and error page properties are mandatory.

## Usage
Whenever the user requests a protected page (specified in the filter-mapping tag of web.xml), this filter redirects the browser to the default oAuth provider authorization page.

To access the user information you can simply use `OauthManager.getCurrentUser()`.

Automatically exported from code.google.com/p/oauthentication
