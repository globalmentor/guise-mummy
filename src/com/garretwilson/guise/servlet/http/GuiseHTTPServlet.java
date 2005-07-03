package com.garretwilson.guise.servlet.http;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.garretwilson.guise.application.AbstractGuiseApplication;
import com.garretwilson.guise.application.GuiseApplication;
import com.garretwilson.guise.component.*;
import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.guise.controller.text.xml.xhtml.*;
import com.garretwilson.guise.session.GuiseSession;
import com.garretwilson.guise.validator.*;
import com.garretwilson.io.OutputStreamUtilities;
import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import com.garretwilson.net.http.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import static com.garretwilson.text.CharacterConstants.*;
import com.garretwilson.util.*;
import static com.garretwilson.util.LocaleUtilities.*;

/**The servlet that controls a Guise web applications. 
Navigation frame bindings for paths can be set in initialization parameters named <code>navigation.<var>frameID</var></code>, with values in the form <code><var>contextRelativePath</var>?class=<var>com.example.Frame</var></code>.
@author Garret Wilson
*/
public class GuiseHTTPServlet extends BasicHTTPServlet
{

	/**The init parameter, "defaultLocale", used to specify the default locale.*/
	public final static String DEFAULT_LOCALE_INIT_PARAMETER_PREFIX="defaultLocale";
	/**The init parameter, "supportedLocales", used to specify the supported locales.*/
	public final static String SUPPORTED_LOCALES_INIT_PARAMETER_PREFIX="supportedLocales";
	/**The prefix, "navigation.", used to identify navigation definitions in the web application's init parameters.*/
	public final static String NAVIGATION_INIT_PARAMETER_PREFIX="navigation.";
	/**The parameter, "class", used to identify the navigation frame class in the web application's init parameters.*/
	public final static String NAVIGATION_CLASS_PARAMETER="class";

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

	/**The Guise application controlled by this servlet.*/
	private final HTTPServletGuiseApplication guiseApplication;

		/**@return The Guise application controlled by this servlet.*/
		protected HTTPServletGuiseApplication getGuiseApplication() {return guiseApplication;}

	/**The context path of the servlet, and thereby of the Guise application.
	Because this servlet's context path is only available via the request object, we must wait until the first request to update it.
	@see HTTPServletGuiseApplication#getContextPath()
	*/
	private String guiseApplicationContextPath=null;

	/**The factory method to create a Guise application.
	This implementation creates a default Guise application and registers an XHTML controller kit.
	Subclasses can override this method to create a specialized application type. 
	@return A new Guise application object.
	*/
	protected HTTPServletGuiseApplication createGuiseApplication()
	{
		final HTTPServletGuiseApplication guiseApplication=new HTTPServletGuiseApplication();	//create a default application
		guiseApplication.installControllerKit(new XHTMLControllerKit<AbstractHTTPServletGuiseContext>());	//create and install an XHTML controller kit
		return guiseApplication;	//return the created Guise application
	}

	/**Default constructor.
	Creates a single Guise application.
	@see #createGuiseApplication()
	*/
	public GuiseHTTPServlet()
	{
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
	@see com.garretwilson.guise.component.NavigationFrame
	*/
	protected void initGuiseApplication(final HTTPServletGuiseApplication guiseApplication, final ServletConfig servletConfig) throws ServletException
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
								final Class<? extends NavigationFrame> navigationFrameClass=specifiedClass.asSubclass(NavigationFrame.class);	//cast the specified class to a frame class just to make sure it's the correct type
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

	/**Services the POST method.
	This version delegates to <code>doGet()</code>.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  @see #doGet(HttpServletRequest, HttpServletResponse)
  */
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);	//delegate to the GET method servicing
	}

	/**Services the GET method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		if(guiseApplicationContextPath==null)	//if we haven't updated our context path for this servlet instance (the context path should always be the same, but it's not available until the first request arrives)
		{
			guiseApplicationContextPath=request.getContextPath()+request.getServletPath()+PATH_SEPARATOR;	//set the application context path from the servlet request, which is the concatenation of the web application path and the servlet's path with an ending slash
		}
		assert guiseApplicationContextPath.equals(request.getContextPath()+request.getServletPath()) : "Guise HTTP servlet context path changed unexpectedly.";

		final HTTPServletGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
		final HTTPServletGuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseApplication, request);	//retrieves the Guise session for this application and request
		final HTTPServletGuiseContext guiseContext=new HTTPServletGuiseContext(guiseSession, request, response);	//create a new Guise context
		guiseSession.addContext(guiseContext);	//add this context to the session
		final String rawPathInfo=getRawPathInfo(request);	//get the raw path info
Debug.trace("raw path info: ", rawPathInfo);
		if(rawPathInfo.endsWith(".css"))	//TODO fix
		{
			response.setContentType("text/css");
			final File file=new File(getServletContext().getRealPath(rawPathInfo));	//TODO fix all this; temporary hack
			final InputStream inputStream=new BufferedInputStream(new FileInputStream(file));
			try
			{
				OutputStreamUtilities.write(inputStream, response.getOutputStream());	//TODO fix
			}
			finally
			{
				inputStream.close();
			}
			return;
		}
		Debug.trace("raw path info", rawPathInfo);
		try
		{
			if(!rawPathInfo.startsWith(ROOT_PATH))	//the Java servlet specification says that the path info will start with a '/'
			{
				throw new IllegalArgumentException("Expected absolute path info, received "+rawPathInfo);
			}
			final String navigationPath=rawPathInfo.substring(1);	//remove the beginning slash to get the navigation path from the path info
			final NavigationFrame navigationFrame=guiseSession.getBoundNavigationFrame(navigationPath);	//get the frame bound to the requested path
			if(navigationFrame!=null)	//if we found a frame class for this address
			{
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
				final URI requestedNavigationURI=guiseSession.getRequestedNavigation();	//get the requested navigation URI
				if(requestedNavigationURI!=null)	//if navigation is requested
				{
					guiseSession.clearRequestedNavigation();	//remove any navigation requests
					throw new HTTPMovedTemporarilyException(requestedNavigationURI);	//redirect to the new navigation location
				}
				guiseContext.setState(GuiseContext.State.UPDATE_VIEW);	//update the context state for updating the view
				navigationFrame.updateView(guiseContext);		//tell the frame to update its view
			}
			else	//if we have no frame type for this address
			{
				throw new HTTPNotFoundException("Not found: "+request.getRequestURL());
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

	/**A Guise application for Guise HTTP servlets.
	@author Garret Wilson
	*/
	protected class HTTPServletGuiseApplication extends AbstractGuiseApplication<AbstractHTTPServletGuiseContext>
	{
		/**The synchronized map of Guise sessions keyed to HTTP sessions.*/
		private final Map<HttpSession, HTTPServletGuiseSession> guiseSessionMap=synchronizedMap(new HashMap<HttpSession, HTTPServletGuiseSession>());

		/**Retrieves a Guise session for the given HTTP session.
		A Guise session will be created if none is currently associated with the given HTTP session.
		When a Guise session is first created, its locale will be updated to match the language, if any, accepted by the HTTP request.
		This method can only be accessed by classes in the same package.
		This method should only be called by HTTP Guise session manager.
		@param httpRequest The HTTP request with which the Guise session is associated. 
		@param httpSession The HTTP session for which a Guise session should be retrieved. 
		@return The Guise session associated with the provided HTTP session.
		@see HTTPGuiseSessionManager
		*/
		protected HTTPServletGuiseSession getGuiseSession(final HttpServletRequest httpRequest, final HttpSession httpSession)
		{
			synchronized(guiseSessionMap)	//don't allow anyone to modify the map of sessions while we access it
			{
				HTTPServletGuiseSession guiseSession=guiseSessionMap.get(httpSession);	//get the Guise session associated with the HTTP session
				if(guiseSession==null)	//if no Guise session is associated with the given HTTP session
				{
					guiseSession=createGuiseSession(httpSession);	//create a new Guise session
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
				guiseSession.onDestroy();	//let the Guise session know it's being destroyed so that it can clean up and release references to the application
			}
			return guiseSession;	//return the associated Guise session
		}

		/**Factory method to create a Guise session from an HTTP session.
		@param httpSession The HTTP session for which a Guise session should be created.
		@return A new Guise session corresponding to the given HTTP session.
		*/ 
		protected HTTPServletGuiseSession createGuiseSession(final HttpSession httpSession)
		{
			return new HTTPServletGuiseSession(this, httpSession);	//create a default HTTP guise session
		}

		/**Reports the context path of the application.
		The context path is an absolute path that ends with a slash ('/'), indicating the application's context relative to its navigation frames.
		@return The path representing the context of the Guise application.
		@exception IllegalStateException if this method is called before this Guise servlet services its first request.
		*/
		public String getContextPath()
		{
			if(guiseApplicationContextPath==null)	//if there is no context path
			{
				throw new IllegalStateException("The Guise HTTP servlet's Guise application getContextPath() method cannot be called before the servlet services its first request.");
			}
			return guiseApplicationContextPath;	//return the context path, always updated by the 
		}

	}

	/**An implementation of an HTTP Guise session that gives special access to to the servlet.
	@author Garret Wilson
	*/
	protected class HTTPServletGuiseSession extends AbstractHTTPGuiseSession
	{
		/**Guise and HTTP session constructor.
		@param application The Guise application to which this session belongs.
		@param httpSession The HTTP session with which this Guise session is associated.
		*/
		public HTTPServletGuiseSession(final GuiseApplication<AbstractHTTPServletGuiseContext> application, final HttpSession httpSession)
		{
			super(application, httpSession);	//construct the parent class
		}

		/**Changes the navigation path of the session so that user interaction can change to another frame.
		This method is provided so that the Guise HTTP servlet can update the navigation path when new requests come in.
		If the given navigation path is the same as the current navigation path, no action occurs.
		@param navigationPath The navigation path relative to the application context path.
		@exception IllegalArgumentException if the provided path is absolute.
		@exception IllegalArgumentException if the navigation path is not recognized (e.g. there is no frame bound to the navigation path).
		*/
		protected void setNavigationPath(final String navigationPath)
		{
			super.setNavigationPath(navigationPath);	//change the navigation path, delegating to the parent version
		}

		/**@return The requested navigation URI---usually either a relative or absolute path, or an absolute URI---or <code>null</code> if no navigation has been requested.
		This method is provided so that the Guise HTTP servlet can retrieve the requested navigation URI when determining whether to redirect.
		*/
		protected URI getRequestedNavigation()
		{
			return super.getRequestedNavigation();	//delegate to the parent version
		}

		/**Removes any requests for navigation.
		This method is provided so that the Guise HTTP servlet can clear the requested navigation URI.
		*/
		protected void clearRequestedNavigation()
		{
			super.clearRequestedNavigation();	//delegate to the parent version
		}

		/**Adds a context to this session and registers a listener for context state changes.
		This implementation exposes the method to the servlet.
		@param context The context to add to this session.
		*/
		protected void addContext(final HTTPServletGuiseContext context)
		{
			super.addContext(context);	//add this context normally
		}
	
		/**Removes a context from this session and unregisters the listener for context state changes.
		This implementation exposes the method to the servlet.
		@param context The context to remove from this session.
		*/
		protected void removeContext(final HTTPServletGuiseContext context)
		{
			super.removeContext(context);	//remove this context normally
		}

		/**Called when the session is destroyed.
		This implementation exposes the method to the servlet.
		*/
		protected void onDestroy()
		{
			super.onDestroy();	//do the default destroying
		}
	}

	/**An implementation of an HTTP servlet Guise context that allows the servlet to update the state of the context.
	@author Garret Wilson
	*/
	protected class HTTPServletGuiseContext extends AbstractHTTPServletGuiseContext
	{
		/**Constructor.
		@param session The Guise user session of which this context is a part.
		@param request The HTTP servlet request.
		@param response The HTTP servlet response.
		@exception NullPointerException if the session, request or response is <code>null</code>.
		*/
		public HTTPServletGuiseContext(final GuiseSession<AbstractHTTPServletGuiseContext> session, final HttpServletRequest request, final HttpServletResponse response)
		{
			super(session, request, response);
		}

		/**Sets the current view interaction state of this context.
		This is a bound property.
		@param newState The new context state.
		@see GuiseContext#STATE_PROPERTY
		*/
		protected void setState(final State newState)
		{
			super.setState(newState);
		}

	}
}
