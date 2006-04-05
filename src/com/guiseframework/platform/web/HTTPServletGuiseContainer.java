package com.guiseframework.platform.web;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.servlet.ServletConstants.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import static com.garretwilson.util.regex.MatcherUtilities.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.io.InputStream;
import java.net.*;
import java.security.Principal;
import java.util.*;
import java.util.regex.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.garretwilson.util.Debug;
import com.guiseframework.*;

/**A Guise container for Guise HTTP servlets.
There will be one servlet Guise container per {@link ServletContext}, which usually corresponds to a single web application on a JVM.
@author Garret Wilson
*/
public class HTTPServletGuiseContainer extends AbstractGuiseContainer
{

	/**The absolute path, relative to the servlet context, of the resources directory.*/
	public final static String RESOURCES_DIRECTORY_PATH=ROOT_PATH+WEB_INF_DIRECTORY_NAME+PATH_SEPARATOR+"guise-application-resources"+PATH_SEPARATOR;

	/**The static, synchronized map of Guise containers keyed to servlet contexts.*/
	private final static Map<ServletContext, HTTPServletGuiseContainer> servletContextGuiseContainerMap=synchronizedMap(new HashMap<ServletContext, HTTPServletGuiseContainer>());

	/**Retrieves the Guise container associated with the given servlet context.
	Because the Java Servlet architecture does not provide the context path to the servlet context, this method can only be called after the first request, which will provide the context path.
	If no Guise container is associated with the servlet context, one is created.
	@param servletContext The servlet context with which this container is associated.
	@param contextPath The servlet context path, which is either the empty string or a path beginning but not ending with a slash ('/'), such as would be returned by {@link HttpServletRequest#getContextPath()}. 
	@return The Guise container associated with the given servlet context.
	@exception NullPointerException if the servlet contextr and/or the context path is <code>null</code>.
	*/
	public static HTTPServletGuiseContainer getGuiseContainer(final ServletContext servletContext, final String contextPath)
	{
		synchronized(servletContextGuiseContainerMap)	//don't allow the map to be used while we do the lookup
		{
			HTTPServletGuiseContainer guiseContainer=servletContextGuiseContainerMap.get(servletContext);	//get the Guise container for this servlet context
			if(guiseContainer==null)	//if there is no Guise container
			{
				guiseContainer=new HTTPServletGuiseContainer(contextPath+PATH_SEPARATOR, servletContext);	//create a new Guise container for this servlet context, specifying the base path
			}
			return guiseContainer;	//return the Guise container
		}
	}

	/**The servlet context with which this container is associated.*/
	private final ServletContext servletContext;

		/**@return The servlet context with which this container is associated.*/
		protected final ServletContext getServletContext() {return servletContext;}

	/**Servlet context and ontainer base path constructor.
	@param basePath The base path of the container, an absolute path that ends with a slash ('/'), indicating the base path of the application base paths.
	@param servletContext The servlet context with which this container is associated.
	@exception NullPointerException if the base path is <code>null</code>.
	@exception IllegalArgumentException if the base path is not absolute and does not end with a slash ('/') character.
	*/
	public HTTPServletGuiseContainer(final String basePath, final ServletContext servletContext)
	{
		super(basePath);	//construct the parent class
		this.servletContext=checkInstance(servletContext, "Servlet context cannot be null.");
	}

	/**Installs the given application at the given context path.
	This version is provided to expose the method to the servlet.
	@param contextPath The context path at which the application is being installed.
	@exception NullPointerException if either the application or context path is <code>null</code>.
	@exception IllegalArgumentException if the context path is not absolute and does not end with a slash ('/') character.
	@exception IllegalStateException if the application is already installed in some container.
	@exception IllegalStateException if there is already an application installed in this container at the given context path.
	*/
	protected void installApplication(final AbstractGuiseApplication application, final String contextPath)
	{
		super.installApplication(application, contextPath);	//delegate to the parent class
	}

	/**Uninstalls the given application.
	This version is provided to expose the method to the servlet.
	@exception NullPointerException if the application is <code>null</code>.
	@exception IllegalStateException if the application is not installed in this container.
	*/
	protected void uninstallApplication(final AbstractGuiseApplication application)
	{
		super.uninstallApplication(application);	//delegate to the parent class			
	}

	/**The synchronized map of Guise sessions keyed to HTTP sessions.*/
	private final Map<HttpSession, GuiseSession> guiseSessionMap=synchronizedMap(new HashMap<HttpSession, GuiseSession>());

	/**Retrieves a Guise session for the given HTTP session.
	A Guise session will be created if none is currently associated with the given HTTP session.
	When a Guise session is first created, its locale will be updated to match the language, if any, accepted by the HTTP request.
	This method can only be accessed by classes in the same package.
	This method should only be called by HTTP Guise session manager.
	@param guiseApplication The Guise application that will own the Guise session.
	@param httpRequest The HTTP request with which the Guise session is associated. 
	@param httpSession The HTTP session for which a Guise session should be retrieved. 
	@return The Guise session associated with the provided HTTP session.
	@see HTTPGuiseSessionManager
	*/
	protected GuiseSession getGuiseSession(final GuiseApplication guiseApplication, final HttpServletRequest httpRequest, final HttpSession httpSession)
	{
		synchronized(guiseSessionMap)	//don't allow anyone to modify the map of sessions while we access it
		{
			GuiseSession guiseSession=guiseSessionMap.get(httpSession);	//get the Guise session associated with the HTTP session
			if(guiseSession==null)	//if no Guise session is associated with the given HTTP session
			{
Debug.trace("+++creating Guise session", httpSession.getId());
				guiseSession=guiseApplication.createSession();	//create a new Guise session
				final GuiseEnvironment environment=guiseSession.getEnvironment();	//get the new session's environment
				final Cookie[] cookies=httpRequest.getCookies();	//get the cookies in the request
				if(cookies!=null)	//if a cookie array was returned
				{
					for(final Cookie cookie:cookies)	//for each cookie in the request
					{
						final String cookieName=cookie.getName();	//get the name of this cookie
//					TODO del Debug.trace("Looking at cookie", cookieName, "with value", cookie.getValue());
						if(!"jsessionid".equalsIgnoreCase(cookieName))	//ignore the session ID TODO use a constant; testing
						{
							environment.setProperty(cookieName, decode(cookie.getValue()));	//put this cookie's decoded value into the session's environment
						}
					}
				}
				initializeUserAgentEnvironment(environment, httpRequest);	//initialize the user agent information
				guiseSession.initialize();	//let the Guise session know it's being initializes so that it can listen to the application
				final Locale[] clientAcceptedLanguages=getAcceptedLanguages(httpRequest);	//get all languages accepted by the client
				guiseSession.requestLocale(asList(clientAcceptedLanguages));	//ask the Guise session to change to one of the accepted locales, if the application supports one
				guiseSessionMap.put(httpSession, guiseSession);	//associate the Guise session with the HTTP session
			}
			return guiseSession;	//return the Guise session
		}
	}

	/**The pattern for matching the Firefox user agent. The entire version number is the first matching group.*/
	private final Pattern FIREFOX_PATTERN=Pattern.compile("Firefox/("+PRODUCT_VERSION_REGEX+")");

	/**The pattern for matching the Opera user agent. The entire version number is the first matching group.
	This pattern recognizes, for example, both "Opera/7.54" and "Opera 7.54".
	*/
	private final Pattern OPERA_PATTERN=Pattern.compile("Opera[/ ]("+PRODUCT_VERSION_REGEX+")");

	/**The pattern for matching the MSIE user agent. The entire version number is the first matching group.
	Microsoft recommended regular expression: "MSIE ([0-9]{1,}[\\.0-9]{0,})"
	@see http://msdn.microsoft.com/workshop/author/dhtml/overview/browserdetection.asp
	*/
	private final Pattern MSIE_PATTERN=Pattern.compile("MSIE ("+PRODUCT_VERSION_REGEX+")");

	/**Initializes a Guise environment with user agent information.
	For known browsers, the user agent string will be parsed with specific knowledge of the evolution of the user of the user agent string.
	@param environment The Guise environment.
	@param httpRequest The HTTP request representing the Guise environment. 
	*/
	protected void initializeUserAgentEnvironment(final GuiseEnvironment environment, final HttpServletRequest httpRequest)
	{
		final String userAgent=getUserAgent(httpRequest);	//get the user agent string
		if(userAgent!=null)	//if there is a user agent string
		{
			//e.g. Opera: "Opera/7.54 (Windows NT 5.1; U)"
			//e.g. Opera: "Mozilla/5.0 (Windows NT 5.1; U) Opera 7.54"
			//e.g. Opera: "Mozilla/4.78 (Windows NT 5.1; U) Opera 7.54"
			//e.g. Opera: "Mozilla/3.0 (Windows NT 5.1; U) Opera 7.54"
			//e.g. Opera: "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1) Opera 7.54"
			//e.g. Firefox 1.5: "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8) Gecko/20051111 Firefox/1.5"
			//e.g. IE 6.0: "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"
			//e.g. Safari 1.3.2: "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/312.8 (KHTML, like Gecko) Safari/312.5"
Debug.trace("user agent:", userAgent);
			String userAgentName=null;	//we'll determine the user agent name
			String userAgentVersion=null;	//we'll determine the user agent version string
			int[] userAgentVersionNumbers=null;	//we'll determine the version numbers
			final Matcher operaMatcher=OPERA_PATTERN.matcher(userAgent);	//first match for Opera, which can masquerade as other browsers
			if(operaMatcher.find())	//if the user agent string finds an Opera match
			{
				userAgentName=GuiseEnvironment.USER_AGENT_NAME_OPERA;	//show that this is Opera
				userAgentVersion=operaMatcher.group(1);	//the first group is the entire version number
				userAgentVersionNumbers=getIntGroups(operaMatcher, 2);	//parse out the version, skipping the entire version string group
			}
			else	//if this is not Opera
			{
				final Matcher ieMatcher=MSIE_PATTERN.matcher(userAgent);	//match for IE
				if(ieMatcher.find())	//if the user agent string finds an IE match
				{
					userAgentName=GuiseEnvironment.USER_AGENT_NAME_MSIE;	//show that this is MSIE
					userAgentVersion=ieMatcher.group(1);	//the first group is the entire version number
					userAgentVersionNumbers=getIntGroups(ieMatcher, 2);	//parse out the version, skipping the entire version string group
				}
				else	//if this is not IE
				{
					final Matcher firefoxMatcher=FIREFOX_PATTERN.matcher(userAgent);	//match for Firefox
					if(firefoxMatcher.find())	//if the user agent string finds a Firefox match
					{
						userAgentName=GuiseEnvironment.USER_AGENT_NAME_FIREFOX;	//show that this is Firefox
						userAgentVersion=firefoxMatcher.group(1);	//the first group is the entire version number
						userAgentVersionNumbers=getIntGroups(firefoxMatcher, 2);	//parse out the version, skipping the entire version string group
					}
				}
			}
Debug.trace("user agent name:", userAgentName, "with version", userAgentVersion, "with version numbers", userAgentVersionNumbers!=null ? Arrays.toString(userAgentVersionNumbers) : null);
			if(userAgentName!=null)	//if we determined a user agent name
			{
				environment.setProperty(GuiseEnvironment.USER_AGENT_NAME_PROPERTY, userAgentName);	//store the user agent name
			}
			if(userAgentVersion!=null)	//if we determined a user agent version
			{
				environment.setProperty(GuiseEnvironment.USER_AGENT_VERSION_PROPERTY, userAgentVersion);	//store the user agent version
			}
			if(userAgentVersionNumbers!=null)	//if we determined a user agent version numbers
			{
				environment.setProperty(GuiseEnvironment.USER_AGENT_VERSION_NUMBERS_PROPERTY, userAgentVersionNumbers);	//store the user agent version numbers
			}
		}			
	}

	/**Removes the Guise session for the given HTTP session.
	This method can only be accessed by classes in the same package.
	This method should only be called by HTTP Guise session manager.
	@param httpSession The HTTP session which should be removed along with its corresponding Guise session. 
	@return The Guise session previously associated with the provided HTTP session, or <code>null</code> if no Guise session was associated with the given HTTP session.
	@see HTTPGuiseSessionManager
	*/
	protected GuiseSession removeGuiseSession(final HttpSession httpSession)
	{
Debug.trace("+++removing Guise session", httpSession.getId());
		GuiseSession guiseSession=guiseSessionMap.remove(httpSession);	//remove the HTTP session and Guise session association
		if(guiseSession!=null)	//if there is a Guise session associated with the HTTP session
		{
			guiseSession.destroy();	//let the Guise session know it's being destroyed so that it can clean up and release references to the application
		}
		return guiseSession;	//return the associated Guise session
	}

	/**Determines if the container has a resource available stored at the given resource path.
	The provided path is first normalized.
	@param resourcePath A container-relative path to a resource in the resource storage area.
	@return <code>true</code> if a resource exists at the given resource path.
	@exception IllegalArgumentException if the given resource path is absolute.
	@exception IllegalArgumentException if the given path is not a valid path.
	*/
	protected boolean hasResource(final String resourcePath)
	{
		try
		{
			return getServletContext().getResource(getContextAbsoluteResourcePath(resourcePath))!=null;	//determine whether we can get a URL to that resource
		}
		catch(final MalformedURLException malformedURLException)	//if the path is malformed
		{
			throw new IllegalArgumentException(malformedURLException);
		}
	}

	/**Retrieves and input stream to the resource at the given path.
	The provided path is first normalized.
	@param resourcePath A container-relative path to a resource in the resource storage area.
	@return An input stream to the resource at the given resource path, or <code>null</code> if no resource exists at the given resource path.
	@exception IllegalArgumentException if the given resource path is absolute.
	*/
	protected InputStream getResourceAsStream(final String resourcePath)
	{
		return getServletContext().getResourceAsStream(getContextAbsoluteResourcePath(resourcePath));	//try to get an input stream to the resource
	}

	/**Determines the servlet context-relative absolute path of the given container-relative path.
	The provided path is first normalized.
	@param containerRelativeResourcePath A container-relative path to a resource in the resource storage area.
	@return The absolute path to the resource relative to the servlet context.
	@exception IllegalArgumentException if the given resource path is absolute.
	*/
	protected String getContextAbsoluteResourcePath(final String containerRelativeResourcePath)
	{
		final String normalizedPath=normalizePath(containerRelativeResourcePath);	//normalize the path
		if(isAbsolutePath(normalizedPath))	//if the given path is absolute
		{
			throw new IllegalArgumentException("Resource path "+normalizedPath+" is not a relative path.");
		}
		return RESOURCES_DIRECTORY_PATH+normalizedPath;	//construct the absolute context-relative path to the resource
	}

	/**Looks up an application principal from the given ID.
	This version is provided to allow package access.
	@param application The application for which a principal should be returned for the given ID.
	@param id The ID of the principal.
	@return The principal corresponding to the given ID, or <code>null</code> if no principal could be determined.
	*/
	protected Principal getPrincipal(final AbstractGuiseApplication application, final String id)
	{
		return super.getPrincipal(application, id);	//delegate to the parent class
	}

	/**Looks up the corresponding password for the given principal.
	This version is provided to allow package access.
	@param application The application for which a password should e retrieved for the given principal.
	@param principal The principal for which a password should be returned.
	@return The password associated with the given principal, or <code>null</code> if no password is associated with the given principal.
	*/
	protected char[] getPassword(final AbstractGuiseApplication application, final Principal principal)
	{
		return super.getPassword(application, principal);	//delegate to the parent class			
	}

	/**Determines the realm applicable for the resource indicated by the given URI.
	This version is provided to allow package access.
	@param application The application for a realm should be returned for the given resouce URI.
	@param resourceURI The URI of the resource requested.
	@return The realm appropriate for the resource, or <code>null</code> if the given resource is not in a known realm.
	@see GuiseApplication#relativizeURI(URI)
	*/
	protected String getRealm(final AbstractGuiseApplication application, final URI resourceURI)
	{
		return super.getRealm(application, resourceURI);	//delegate to the parent class
	}

	/**Checks whether the given principal is authorized to access the resouce at the given application path.
	This version is provided to allow package access.
	@param application The application for which a principal should be authorized for a given resouce URI.
	@param resourceURI The URI of the resource requested.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param realm The realm with which the resource is associated, or <code>null</code> if the realm is not known.
	@return <code>true</code> if the given principal is authorized to access the resource represented by the given resource URI.
	*/
	protected boolean isAuthorized(final AbstractGuiseApplication application, final URI resourceURI, final Principal principal, final String realm)
	{
		return super.isAuthorized(application, resourceURI, principal, realm);	//delegate to the parent class
	}

}
