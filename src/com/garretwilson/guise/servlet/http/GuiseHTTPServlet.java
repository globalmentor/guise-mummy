package com.garretwilson.guise.servlet.http;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.garretwilson.guise.*;
import com.garretwilson.guise.component.*;
import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.guise.controller.text.xml.xhtml.*;
import com.garretwilson.guise.session.GuiseSession;
import com.garretwilson.guise.session.ModalNavigation;
import com.garretwilson.guise.session.Navigation;
import com.garretwilson.guise.validator.*;

import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;

import com.garretwilson.net.http.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import static com.garretwilson.servlet.ServletConstants.*;
import static com.garretwilson.text.CharacterConstants.*;
import com.garretwilson.util.*;
import static com.garretwilson.util.LocaleUtilities.*;

/**The servlet that controls a Guise web applications. 
Navigation frame bindings for paths can be set in initialization parameters named <code>navigation.<var>frameID</var></code>, with values in the form <code><var>contextRelativePath</var>?class=<var>com.example.Frame</var></code>.
This implementation only works with Guise applications that descend from {@link AbstractGuiseApplication}.
@author Garret Wilson
*/
public class GuiseHTTPServlet extends DefaultHTTPServlet
{

	/**The init parameter, "defaultLocale", used to specify the default locale.*/
	public final static String DEFAULT_LOCALE_INIT_PARAMETER_PREFIX="defaultLocale";
	/**The init parameter, "supportedLocales", used to specify the supported locales.*/
	public final static String SUPPORTED_LOCALES_INIT_PARAMETER_PREFIX="supportedLocales";
	/**The prefix, "navigation.", used to identify navigation definitions in the web application's init parameters.*/
	public final static String NAVIGATION_INIT_PARAMETER_PREFIX="navigation.";
	/**The parameter, "class", used to identify the navigation frame class in the web application's init parameters.*/
	public final static String NAVIGATION_CLASS_PARAMETER="class";

	/**The absolute path, relative to the servlet context, of the resources directory.*/
	public final static String RESOURCES_DIRECTORY_PATH=ROOT_PATH+WEB_INF_DIRECTORY_NAME+PATH_SEPARATOR+"guise-application-resources"+PATH_SEPARATOR;
	
	/**@return The global HTTP servlet Guise instance.
	This is a convenience method.
	@see HTTPServletGuise#getInstance()
	*/
/*TODO fix
	protected HTTPServletGuise getGuise()
	{
		return HTTPServletGuise.getInstance();	//return the global HTTP servlet Guise instance
	}
*/

	/**The Guise container that owns the applications.*/
	private HTTPServletGuiseContainer guiseContainer=null;

		/**Determines the Guise container. If one does not exist, one will be created.
		This method must not be called before a request is processed.
		@return The Guise container that owns the applications.
		@exception IllegalStateException if this method is called before any requests have been processed.
		*/
		protected HTTPServletGuiseContainer getGuiseContainer()
		{
			if(guiseContainer==null)	//if no container exists
			{
				final String guiseContainerBasePath=getContextPath()+PATH_SEPARATOR;	//construct the Guise container base path from the servlet request, which is the web application path with an ending slash
				guiseContainer=new HTTPServletGuiseContainer(guiseContainerBasePath);	//create a new container
			}
			return guiseContainer;	//return the Guise container
		}

	/**The Guise application controlled by this servlet.*/
	private final AbstractGuiseApplication guiseApplication;

		/**@return The Guise application controlled by this servlet.*/
		protected AbstractGuiseApplication getGuiseApplication() {return guiseApplication;}

	/**The factory method to create a Guise application.
	This implementation creates a default Guise application and registers an XHTML controller kit.
	Subclasses can override this method to create a specialized application type. 
	@return A new Guise application object.
	*/
	protected AbstractGuiseApplication createGuiseApplication()
	{
		final AbstractGuiseApplication guiseApplication=new DefaultGuiseApplication();	//create a default application
		guiseApplication.installControllerKit(new XHTMLControllerKit());	//create and install an XHTML controller kit
		return guiseApplication;	//return the created Guise application
	}

	/**Default constructor.
	Creates a single Guise application.
	@see #createGuiseApplication()
	*/
	public GuiseHTTPServlet()
	{
//TODO del		guiseContainer=new HTTPServletGuiseContainer();	//create the Guise container
		guiseApplication=createGuiseApplication();	//create a store a Guise application
	}
		
	/**Initializes the servlet.
	@param servletConfig The servlet configuration. 
	@exception ServletException if there is a problem initializing.
	*/
	public void init(final ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);	//do the default initialization
		Debug.setDebug(true);	//turn on debug
		Debug.setMinimumReportLevel(Debug.ReportLevel.TRACE);	//set the level of reporting
		setReadOnly(true);	//make this servlet read-only
		//TODO turn off directory listings, and/or fix them
		try
		{
			initGuiseApplication(getGuiseApplication(), servletConfig);	//initialize the frame bindings
		}
		catch(final Exception exception)	//if there is any problem initializing the Guise application
		{
			throw new ServletException("Error initializing Guise application: "+exception.getMessage(), exception);
		}
	}

	/**Initializes bindings between paths and associated navigation frame classes.
	This implementation reads the initialization parameters named <code>navigation.<var>frameID</var></code>, expecting values in the form <code><var>contextRelativePath</var>?class=<var>com.example.Frame</var></code>.
	@param guiseApplication The Guise application to initialize.
	@param servletConfig The servlet configuration. 
	@exception IllegalArgumentException if the one of the frame bindings is not expressed in correct format.
	@exception IllegalArgumentException if the one of the classes specified as a frame binding could not be found.
	@exception ClassCastException if the one of the classes specified as a frame binding does not represent a subclass of a frame component.
	@exception ServletException if there is a problem initializing the frame bindings.
	@see Frame
	*/
	protected void initGuiseApplication(final AbstractGuiseApplication guiseApplication, final ServletConfig servletConfig) throws ServletException
	{
			//initialize the supported locales
		final String supportedLocalesString=servletConfig.getInitParameter(SUPPORTED_LOCALES_INIT_PARAMETER_PREFIX);	//get the supported locales init parameter
		if(supportedLocalesString!=null)	//if supported locales are specified
		{
			final String[] supportedLocaleStrings=supportedLocalesString.split(String.valueOf(COMMA_CHAR));	//split the string into separate locale strings
			final Locale[] supportedLocales=new Locale[supportedLocaleStrings.length];	//create an array to hold the locales
			for(int i=supportedLocaleStrings.length-1; i>=0; --i)	//for each supported locale string
			{
				supportedLocales[i]=createLocale(supportedLocaleStrings[i].trim());	//create a locale for this specified locale (trimming whitespace just to be extra helpful)
			}
			guiseApplication.getSupportedLocales().clear();	//remove the application's currently supported locales
			addAll(guiseApplication.getSupportedLocales(), supportedLocales);	//add all supported locales to the application 
		}
			//initialize the default locale
		final String defaultLocaleString=servletConfig.getInitParameter(DEFAULT_LOCALE_INIT_PARAMETER_PREFIX);	//get the default locale init parameter
		if(defaultLocaleString!=null)	//if a default locale is specified
		{
			guiseApplication.setDefaultLocale(createLocale(defaultLocaleString.trim()));	//create a locale from the default locale string and store it in the application (trimming whitespace just to be extra helpful)
		}
			//initialize navigation path/frame bindings
		final Enumeration initParameterNames=servletConfig.getInitParameterNames();	//get the names of all init parameters
		while(initParameterNames.hasMoreElements())	//while there are more initialization parameters
		{
			final String initParameterName=(String)initParameterNames.nextElement();	//get the next initialization parameter name
			if(initParameterName.startsWith(NAVIGATION_INIT_PARAMETER_PREFIX))	//if this is a path/frame binding
			{
				final String initParameterValue=servletConfig.getInitParameter(initParameterName);	//get this init parameter value
				try
				{
					final URI pathFrameBindingURI=new URI(initParameterValue);	//create a URI from the frame binding expression
					final String path=pathFrameBindingURI.getRawPath();	//extract the path from the URI
					if(path!=null)	//if a path was specified
					{
						final ListMap<String, String> parameterListMap=getParameters(pathFrameBindingURI);	//get the URI parameters
						final String className=parameterListMap.getItem(NAVIGATION_CLASS_PARAMETER);	//get the class parameter
						if(className!=null)	//if a class name was specified
						{
							try
							{
								final Class<?> specifiedClass=Class.forName(className);	//load the class for the specified name
								final Class<? extends Frame> navigationFrameClass=specifiedClass.asSubclass(Frame.class);	//cast the specified class to a frame class just to make sure it's the correct type
								guiseApplication.bindNavigationFrame(path, navigationFrameClass);	//cast the class to a frame class and bind it to the path in the Guise application
							}
							catch(final ClassNotFoundException classNotFoundException)
							{
								throw new IllegalArgumentException("The initialization parameter specified class "+className+" for path "+path+" could not be found.", classNotFoundException);						
							}
						}
						else	//if no class name was specified
						{
							throw new IllegalArgumentException("The initialization parameter path/frame binding "+pathFrameBindingURI+" for "+initParameterName+" did not specify a class name.");												
						}
					}
					else	//if no path was specified
					{
						throw new IllegalArgumentException("The initialization parameter path/frame binding "+pathFrameBindingURI+" for "+initParameterName+" did not specify a path.");												
					}
				}
				catch(final URISyntaxException uriSyntaxException)	//if the parameter value was not in the correct format
				{
					throw new IllegalArgumentException("Incorrect initialization parameter path/frame class binding URI "+initParameterValue+" for "+initParameterName, uriSyntaxException);
				}
			}
		}
	}

//TODO fix HEAD method servicing, probably by overriding serveResource()

	/**Services the GET method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final URI requestURI=URI.create(request.getRequestURL().toString());	//get the URI of the current request
		final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container
		final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
		if(guiseApplication.getContainer()==null)	//if this application has not yet been installed (note that there is a race condition here if multiple HTTP requests attempt to access the application simultaneously, but the losing thread will simply throw an exception and not otherwise disturb the application functionality)
		{
			final String guiseApplicationContextPath=request.getContextPath()+request.getServletPath()+PATH_SEPARATOR;	//construct the Guise application context path from the servlet request, which is the concatenation of the web application path and the servlet's path with an ending slash
			guiseContainer.installApplication(guiseApplication, guiseApplicationContextPath);	//install the application			
		}
		final HTTPServletGuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieves the Guise session for this container and request
		final HTTPServletGuiseContext guiseContext=new HTTPServletGuiseContext(guiseSession, request, response);	//create a new Guise context
		guiseSession.addContext(guiseContext);	//add this context to the session
		final String rawPathInfo=getRawPathInfo(request);	//get the raw path info
Debug.trace("raw path info", rawPathInfo);
Debug.trace("Referrer:", getReferer(request));
		assert isAbsolutePath(rawPathInfo) : "Expected absolute path info, received "+rawPathInfo;	//the Java servlet specification says that the path info will start with a '/'
		try
		{
			final String navigationPath=rawPathInfo.substring(1);	//remove the beginning slash to get the navigation path from the path info
			final Frame<?> navigationFrame=guiseSession.getBoundNavigationFrame(navigationPath);	//get the frame bound to the requested path
			if(navigationFrame!=null)	//if we found a frame class for this address
			{
				setNoCache(response);	//TODO testing; fix; update method
				
					//before actually changing the navigation path, check to see if we're in the middle of modal navigation (only do this after we find a navigation frame, as this request might be for a stylesheet or some other non-frame resource, which shouldn't be redirected)
				final ModalNavigation modalNavigation=guiseSession.peekModalNavigation();	//see if we are currently doing modal navigation
				if(modalNavigation!=null)	//if we are currently in the middle of modal navigation, make sure the correct frame was requested
				{
					final URI modalNavigationURI=modalNavigation.getNavigationURI();	//get the modal navigation URI
					if(!requestURI.getRawPath().equals(modalNavigationURI.getRawPath()))		//if this request was for a different path than our current modal navigation path (we wouldn't be here if the domain, application, etc. weren't equivalent)
					{
						throw new HTTPMovedTemporarilyException(modalNavigationURI);	//redirect to the modal navigation location				
					}
				}
					//update the frame's referrer
				final String referrer=getReferer(request);	//see if the request has a referrer
				if(referrer!=null)	//if the request indicates a referrer
				{
					final URI plainReferrerURI=getPlainURI(URI.create(referrer));	//get a plain URI version of the referrer
					if(!plainReferrerURI.equals(getPlainURI(requestURI)))	//if we aren't being referred from ourselves
					{
						navigationFrame.setReferrerURI(plainReferrerURI);	//update the frame's referrer URI
					}
				}
				guiseSession.setNavigationPath(navigationPath);	//make sure the Guise session has the correct navigation path
				try
				{
					guiseContext.setState(GuiseContext.State.QUERY_VIEW);	//update the context state for querying the view
					navigationFrame.queryView(guiseContext);		//tell the frame to query its view
					guiseContext.setState(GuiseContext.State.DECODE_VIEW);	//update the context state for decoding the view
					navigationFrame.decodeView(guiseContext);		//tell the frame to decode its view
					guiseContext.setState(GuiseContext.State.VALIDATE_VIEW);	//update the context state for validating the view
					navigationFrame.validateView(guiseContext);		//tell the frame to validate its view
					guiseContext.setState(GuiseContext.State.UPDATE_MODEL);	//update the context state for updating the model
					navigationFrame.updateModel(guiseContext);	//tell the frame to update its model
					guiseContext.setState(GuiseContext.State.QUERY_MODEL);	//update the context state for querying the model
					navigationFrame.queryModel(guiseContext);		//tell the frame to query its model
					guiseContext.setState(GuiseContext.State.ENCODE_MODEL);	//update the context state for encoding the model
					navigationFrame.encodeModel(guiseContext);		//tell the frame to encode its model
				}
				catch(final ValidationsException validationsException)	//if there were any validation errors during validation
				{
					navigationFrame.addErrors(validationsException);	//store the validation error(s) so that the frame can report them to the user
				}
				catch(final ValidationException validationException)	//if there were any validation errors while updating the model
				{
					navigationFrame.addError(validationException);	//store the validation error so that the frame can report it to the user
				}
Debug.trace("looking for requested navigation");
				final Navigation requestedNavigation=guiseSession.getRequestedNavigation();	//get the requested navigation
				if(requestedNavigation!=null)	//if navigation is requested
				{
Debug.trace("got requested navigation");
					final URI requestedNavigationURI=requestedNavigation.getNavigationURI();
					guiseSession.clearRequestedNavigation();	//remove any navigation requests
					if(requestedNavigation instanceof ModalNavigation)	//if modal navigation was requested
					{
Debug.trace("this is a modal navigation; pushing it on the stack");
						guiseSession.pushModalNavigation((ModalNavigation)requestedNavigation);	//push the modal navigation onto the top of the modal navigation stack
					}
					throw new HTTPMovedTemporarilyException(requestedNavigationURI);	//redirect to the new navigation location
				}
				guiseContext.setState(GuiseContext.State.UPDATE_VIEW);	//update the context state for updating the view
				navigationFrame.updateView(guiseContext);		//tell the frame to update its view
			}
			else	//if we have no frame type for this address
			{
				super.doGet(request, response);	//let the default functionality take over
			}
		}
		catch(final NoSuchMethodException noSuchMethodException)	//catch and rethrow instantiation errors from retrieving/creating the frame
		{
			throw new ServletException(noSuchMethodException);
		}
		catch(final InvocationTargetException invocationTargetException)
		{
			throw new ServletException(invocationTargetException);
		}
		catch(final InstantiationException instantiationException)
		{
			throw new ServletException(instantiationException);
		}
		catch(final IllegalAccessException illegalAccessException)
		{
			throw new ServletException(illegalAccessException);
		}
		finally
		{
			guiseContext.setState(GuiseContext.State.INACTIVE);	//always deactivate the context			
			guiseSession.removeContext(guiseContext);	//remove this context from the session
		}
	}

	/**Called by the servlet container to indicate to a servlet that the servlet is being taken out of service.
	This version uninstalls the Guise application from the Guise container.
	*/
	public void destroy()
	{
		final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
		if(guiseApplication.getContainer()!=null)	//if the Guise application is installed
		{
			getGuiseContainer().uninstallApplication(guiseApplication);	//uninstall the application
		}
		super.destroy();	//do the default destroying
	}

	/**A Guise container for Guise HTTP servlets.
	@author Garret Wilson
	*/
	protected class HTTPServletGuiseContainer extends AbstractGuiseContainer	//TODO eventually make this static and share it among servlets
	{
		/**Container base path constructor.
		@param basePath The base path of the container.
		@exception NullPointerException if the base path is <code>null</code>.
		@exception IllegalArgumentException if the base path is not absolute and does not end with a slash ('/') character.
		*/
		public HTTPServletGuiseContainer(final String basePath)
		{
			super(basePath);	//construct the parent class
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
		private final Map<HttpSession, HTTPServletGuiseSession> guiseSessionMap=synchronizedMap(new HashMap<HttpSession, HTTPServletGuiseSession>());

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
		protected HTTPServletGuiseSession getGuiseSession(final GuiseApplication guiseApplication, final HttpServletRequest httpRequest, final HttpSession httpSession)
		{
			synchronized(guiseSessionMap)	//don't allow anyone to modify the map of sessions while we access it
			{
				HTTPServletGuiseSession guiseSession=guiseSessionMap.get(httpSession);	//get the Guise session associated with the HTTP session
				if(guiseSession==null)	//if no Guise session is associated with the given HTTP session
				{
					guiseSession=createGuiseSession(guiseApplication, httpSession);	//create a new Guise session
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
		protected HTTPServletGuiseSession removeGuiseSession(final HttpSession httpSession)
		{
			HTTPServletGuiseSession guiseSession=guiseSessionMap.remove(httpSession);	//remove the HTTP session and Guise session association
			if(guiseSession!=null)	//if there is a Guise session associated with the HTTP session
			{
				guiseSession.destroy();	//let the Guise session know it's being destroyed so that it can clean up and release references to the application
			}
			return guiseSession;	//return the associated Guise session
		}

		/**Factory method to create a Guise session from an HTTP session.
		@param guiseApplication The Guise application that will own the created Guise session.
		@param httpSession The HTTP session for which a Guise session should be created.
		@return A new Guise session corresponding to the given HTTP session.
		*/
		protected HTTPServletGuiseSession createGuiseSession(final GuiseApplication guiseApplication, final HttpSession httpSession)
		{
			return new HTTPServletGuiseSession(guiseApplication, httpSession);	//create a default HTTP guise session
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
	}

}
