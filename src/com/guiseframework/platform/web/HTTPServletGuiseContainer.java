package com.guiseframework.platform.web;

import static com.garretwilson.io.FileUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.servlet.ServletConstants.*;
import static com.garretwilson.servlet.http.HttpServletConstants.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import static com.garretwilson.text.CharacterEncodingConstants.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.io.*;
import java.net.*;
import java.security.Principal;
import java.text.DateFormat;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.garretwilson.text.W3CDateFormat;
import com.garretwilson.util.Debug;
import com.guiseframework.*;
import com.guiseframework.model.InformationLevel;

/**A Guise container for Guise HTTP servlets.
There will be one servlet Guise container per {@link ServletContext}, which usually corresponds to a single web application on a JVM.
@author Garret Wilson
*/
public class HTTPServletGuiseContainer extends AbstractGuiseContainer
{

	/**The absolute path, relative to the servlet context, of the resources directory.*/
	public final static String RESOURCES_DIRECTORY_PATH=ROOT_PATH+WEB_INF_DIRECTORY_NAME+PATH_SEPARATOR+"guise"+PATH_SEPARATOR+"resources"+PATH_SEPARATOR;	//TODO use constants; combine with other similar designations for the same path

	/**The static, synchronized map of Guise containers keyed to servlet contexts.*/
	private final static Map<ServletContext, HTTPServletGuiseContainer> servletContextGuiseContainerMap=synchronizedMap(new HashMap<ServletContext, HTTPServletGuiseContainer>());

	/**Retrieves the Guise container associated with the given servlet context.
	Because the Java Servlet architecture does not provide the context path to the servlet context, this method can only be called after the first request, which will provide the context path.
	If no Guise container is associated with the servlet context, one is created.
	@param servletContext The servlet context with which this container is associated.
	@param baseURI The base URI of the container, an absolute URI that ends with the base path, which ends with a slash ('/'), indicating the base path of the application base paths.
	@return The Guise container associated with the given servlet context.
	@exception NullPointerException if the servlet context and/or base URI is <code>null</code>.
	@exception IllegalArgumentException if the base URI is not absolute or does not end with a slash ('/') character.
	*/
	public static HTTPServletGuiseContainer getGuiseContainer(final ServletContext servletContext, final URI baseURI)
	{
		synchronized(servletContextGuiseContainerMap)	//don't allow the map to be used while we do the lookup
		{
			HTTPServletGuiseContainer guiseContainer=servletContextGuiseContainerMap.get(servletContext);	//get the Guise container for this servlet context
			if(guiseContainer==null)	//if there is no Guise container
			{
				guiseContainer=new HTTPServletGuiseContainer(baseURI, servletContext);	//create a new Guise container for this servlet context, specifying the base URI
			}
			return guiseContainer;	//return the Guise container
		}
	}

	/**The servlet context with which this container is associated.*/
	private final ServletContext servletContext;

		/**@return The servlet context with which this container is associated.*/
		protected final ServletContext getServletContext() {return servletContext;}

	/**Servlet contains and container base URI constructor.
	@param baseURI The base URI of the container, an absolute URI that ends with the base path, which ends with a slash ('/'), indicating the base path of the application base paths.
	@param servletContext The servlet context with which this container is associated.
	@exception NullPointerException if the base URI and/or servlet context is <code>null</code>.
	@exception IllegalArgumentException if the base URI is not absolute or does not end with a slash ('/') character.
	*/
	public HTTPServletGuiseContainer(final URI baseURI, final ServletContext servletContext)
	{
		super(baseURI);	//construct the parent class
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
/*TODO del
final Enumeration headerNames=httpRequest.getHeaderNames();	//TODO del
while(headerNames.hasMoreElements())
{
	final String headerName=(String)headerNames.nextElement();
	Debug.info("request header:", headerName, httpRequest.getHeader(headerName));
}
*/
				final URI requestURI=URI.create(httpRequest.getRequestURL().toString());	//get the URI of the current request
				final URI sessionBaseURI=requestURI.resolve(guiseApplication.getBasePath());	//resolve the application base path to the request URI
				guiseSession=guiseApplication.createSession();	//ask the application to create a new Guise session
//TODO del Debug.trace("default session base URI:", guiseSession.getBaseURI());
				guiseSession.setBaseURI(sessionBaseURI);	//update the base URI to the one specified by the request, in case we can create a session from different URLs
//TODO del Debug.trace("new session base URI:", guiseSession.getBaseURI());
				final String relativeApplicationPath=relativizePath(getBasePath(), guiseApplication.getBasePath());	//get the application path relative to the container path
/*TODO bring back logging after testing log out-of-memory error
				try
				{
					final File baseLogDirectory=GuiseHTTPServlet.getLogDirectory(getServletContext());	//get the base log directory
					final File logDirectory=new File(baseLogDirectory, relativeApplicationPath);	//get a subdirectory if needed; the File class allows a relative application path of ""
					ensureDirectoryExists(logDirectory);	//make sure the log directory exists
					final DateFormat logFilenameDateFormat=new W3CDateFormat(W3CDateFormat.Style.DATE);	//create a formatter for the log filename
					final String logFilename=logFilenameDateFormat.format(new Date())+" session "+httpSession.getId()+".csv";	//create a filename in the form "date session sessionID.log" TODO use a constant
					final File logFile=new File(logDirectory, logFilename);	//create a log file object
					final boolean isNewLogFile=!logFile.exists();	//see if we're creating a new log file
					final Writer logWriter=new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(logFile, true)), UTF_8);	//create a buffered UTF-8 log writer, appending if the file already exists
					if(isNewLogFile)	//if this is a new log file
					{
						Log.logHeaders(logWriter);	//log the headers to the log writer
					}
					guiseSession.setLogWriter(logWriter);	//tell the session to use the new log writer
					final Map<String, Object> logParameters=new HashMap<String, Object>();	//create a map for our log parameters
					logParameters.put("id", httpSession.getId());	//session ID
					logParameters.put("creationTime", new Date(httpSession.getCreationTime()));	//session creation time
					logParameters.put("isNew", Boolean.valueOf(httpSession.isNew()));	//session is new
					logParameters.put("lastAccessedTime", new Date(httpSession.getLastAccessedTime()));	//session last accessed time
					logParameters.put("maxInactiveInterval", new Integer(httpSession.getMaxInactiveInterval()));	//session max inactive interval
					Log.log(logWriter, InformationLevel.LOG, null, null, "guise-session-create", null, logParameters, null);	//TODO improve; use a constant
					logWriter.flush();	//flush the log information
				}
				catch(final UnsupportedEncodingException unsupportedEncodingException)	//we should always support UTF-8
				{
					throw new AssertionError(unsupportedEncodingException);					
				}
				catch(final IOException ioException)	//we have a problem if we can't create a file TODO improve; we would go on now, but we would have a problem when we try to close the standard error stream; what needs to be done is to create a better default stream, and then recover from this error
				{
					throw new AssertionError(ioException);
				}
*/
				final GuiseEnvironment environment=guiseSession.getEnvironment();	//get the new session's environment
				final Cookie[] cookies=httpRequest.getCookies();	//get the cookies in the request
				if(cookies!=null)	//if a cookie array was returned
				{
					for(final Cookie cookie:cookies)	//for each cookie in the request
					{
						final String cookieName=cookie.getName();	//get the name of this cookie
//					TODO del Debug.trace("Looking at cookie", cookieName, "with value", cookie.getValue());
						if(!SESSION_ID_COOKIE_NAME.equals(cookieName))	//ignore the session ID
						{
							environment.setProperty(cookieName, decode(cookie.getValue()));	//put this cookie's decoded value into the session's environment
						}
					}
				}
				environment.setProperties(getUserAgentProperties(httpRequest));	//initialize the Guise environment user agent information
/*TODO del if can't be salvaged for Firefox
					//The "Accept: application/x-shockwave-flash" header is only sent in the first request from IE.
					//Unfortunately Firefox 1.5.0.3 doesn't send it at all.
					//see http://www.sitepoint.com/article/techniques-unearthed
					//see http://www.adobe.com/support/flash/releasenotes/player/rn_6.html
				environment.setProperty(CONTENT_APPLICATION_SHOCKWAVE_FLASH_ACCEPTED_PROPERTY,	//content.application.shockwave.flash.accepted
						Boolean.valueOf(isAcceptedContentType(httpRequest, APPLICATION_SHOCKWAVE_FLASH_CONTENT_TYPE, false)));	//see if Flash is installed
*/
/*TODO bring back logging after testing log out-of-memory error
					//log the environment variables
				try
				{
					Log.log(guiseSession.getLogWriter(), InformationLevel.LOG, null, "guise-session-environment", null, null, environment.getProperties(), null);	//TODO improve; use a constant
					guiseSession.getLogWriter().flush();	//flush the log information
				}
				catch(final IOException ioException)	//we have a problem if we can't write to the file TODO improve; we would go on now, but we would have a problem when we try to close the standard error stream; what needs to be done is to create a better default stream, and then recover from this error
				{
					throw new AssertionError(ioException);
				}
*/
				addGuiseSession(guiseSession);	//add and initialize the Guise session
				final Locale[] clientAcceptedLanguages=getAcceptedLanguages(httpRequest);	//get all languages accepted by the client
				guiseSession.requestLocale(asList(clientAcceptedLanguages));	//ask the Guise session to change to one of the accepted locales, if the application supports one
				guiseSessionMap.put(httpSession, guiseSession);	//associate the Guise session with the HTTP session
			}
			return guiseSession;	//return the Guise session
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
			try
			{
/*TODO bring back logging after testing log out-of-memory error
					//TODO refactor and consolidate; prevent code duplication
				final Map<String, Object> logParameters=new HashMap<String, Object>();	//create a map for our log parameters
				logParameters.put("id", httpSession.getId());	//session ID
				logParameters.put("creationTime", new Date(httpSession.getCreationTime()));	//session creation time
				logParameters.put("isNew", Boolean.valueOf(httpSession.isNew()));	//session is new
				logParameters.put("lastAccessedTime", new Date(httpSession.getLastAccessedTime()));	//session last accessed time
				logParameters.put("maxInactiveInterval", new Integer(httpSession.getMaxInactiveInterval()));	//session max inactive interval
				try
				{
					Log.log(guiseSession.getLogWriter(), InformationLevel.LOG, null, null, "guise-session-destroy", null, logParameters, null);	//TODO improve; use a constant
					guiseSession.getLogWriter().flush();	//flush the log information
				}
				catch(final IOException ioException)	//we have a problem if we can't write to the file TODO improve; we would go on now, but we would have a problem when we try to close the standard error stream; what needs to be done is to create a better default stream, and then recover from this error
				{
					throw new AssertionError(ioException);
				}
*/
				removeGuiseSession(guiseSession);	//remove the Guise session
			}
			finally
			{
/*TODO bring back logging after testing log out-of-memory error
				try
				{
					guiseSession.getLogWriter().close();	//always close the log writer
				}
				catch(final IOException ioException)	//if there is an I/O error
				{
					Debug.error(ioException);	//log the error
				}
*/
			}
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

	/**Retrieves an input stream to the resource at the given path.
	The provided path is first normalized.
	@param resourcePath A container-relative path to a resource in the resource storage area.
	@return An input stream to the resource at the given resource path, or <code>null</code> if no resource exists at the given resource path.
	@exception IllegalArgumentException if the given resource path is absolute.
	*/
	protected InputStream getResourceInputStream(final String resourcePath)
	{
		return getServletContext().getResourceAsStream(getContextAbsoluteResourcePath(resourcePath));	//try to get an input stream to the resource
	}

	/**Retrieves an input stream to the entity at the given URI.
	The URI is first resolved to the container base URI.
	This version loads local resources directly through the servlet context.
	@param uri A URI to the entity; either absolute or relative to the container.
	@return An input stream to the entity at the given resource URI, or <code>null</code> if no entity exists at the given resource path..
	@exception NullPointerException if the given URI is <code>null</code>.
	@exception IOException if there was an error connecting to the entity at the given URI.
	@see #getBaseURI()
	*/
	public InputStream getInputStream(final URI uri) throws IOException
	{
//TODO del Debug.trace("getting container input stream to URI", uri);
		final URI baseURI=getBaseURI();	//get the base URI
//	TODO del Debug.trace("base URI:", baseURI);
		final URI absoluteResolvedURI=baseURI.resolve(uri);	//resolve the URI against the container base URI
//	TODO del Debug.trace("resolved URI:", absoluteResolvedURI);
		final URI relativeURI=baseURI.relativize(absoluteResolvedURI);	//see if the absolute URI is in the application public path
//	TODO del Debug.trace("relative URI:", relativeURI);		
		
//TODO allow multiple base URIs; as a short-term fix, check HTTP and HTTPS variations 
		
		if(!relativeURI.isAbsolute())	//if the URI is relative to the container base URI, we can load it directly
		{
//TODO del Debug.trace("Loading directly from servlet: "+ROOT_PATH+relativeURI);
			return getServletContext().getResourceAsStream(ROOT_PATH+relativeURI.toString());	//get an input stream through the servlet context (which may be null if there is no such resource)
		}
		return super.getInputStream(uri);	//if we can't get the resource locally, delegate to the super class
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
