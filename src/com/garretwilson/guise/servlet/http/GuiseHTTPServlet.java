package com.garretwilson.guise.servlet.http;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import static java.util.Collections.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.garretwilson.guise.application.AbstractGuiseApplication;
import com.garretwilson.guise.component.*;
import com.garretwilson.guise.controller.text.xml.xhtml.*;
import com.garretwilson.guise.validator.ValidationException;
import com.garretwilson.io.OutputStreamUtilities;
import static com.garretwilson.net.URIConstants.*;
import com.garretwilson.net.http.*;
import com.garretwilson.util.Debug;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;

/**The servlet that controls a Guise web applications. 
Navigation frame bindings for paths can be set in initialization parameters named <code>navigationPathFrameClass.<var>frameID</var></code>, with values in the form <code>/<var>contextRelativePath</var>?<var>com.example.Frame</var></code>.
@author Garret Wilson
*/
public class GuiseHTTPServlet extends BasicHTTPServlet
{

	/**The prefix, "navigationPathFrameClass.", used to identify path/frame bindings in the web application's init parameters.*/
	public final static String NAVIGATION_PATH_FRAME_CLASS_INIT_PARAMETER_PREFIX="navigationPathFrameClass.";

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

	/**@return The Guise application controlled by this servlet.*/
	private final HTTPServletGuiseApplication guiseApplication;

		/**The Guise application controlled by this servlet.*/
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
		guiseApplication.installControllerKit(new XHTMLControllerKit<HTTPServletGuiseContext>());	//create and install an XHTML controller kit
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
		initNavigationFrameBindings(servletConfig);	//initialize the frame bindings
	}

	/**Initializes bindings between paths and associated navigation frame classes.
	This implementation reads the initialization parameters named <code>navigationPathFrameClass.<var>frameID</var></code>, expecting values in the form <code><var>contextRelativePath</var>?<var>com.example.Frame</var></code>.
	@param servletConfig The servlet configuration. 
	@exception IllegalArgumentException if the one of the frame bindings is not expressed in correct format.
	@exception IllegalArgumentException if the one of the classes specified as a frame binding could not be found.
	@exception ClassCastException if the one of the classes specified as a frame binding does not represent a subclass of a frame component.
	@exception ServletException if there is a problem initializing the frame bindings.
	@see com.garretwilson.guise.component.NavigationFrame
	*/
	protected void initNavigationFrameBindings(final ServletConfig servletConfig) throws ServletException
	{
		final Enumeration initParameterNames=servletConfig.getInitParameterNames();	//get the names of all init parameters
		while(initParameterNames.hasMoreElements())	//while there are more initialization parameters
		{
			final String initParameterName=(String)initParameterNames.nextElement();	//get the next initialization parameter name
			if(initParameterName.startsWith(NAVIGATION_PATH_FRAME_CLASS_INIT_PARAMETER_PREFIX))	//if this is a path/frame binding
			{
				final String initParameterValue=servletConfig.getInitParameter(initParameterName);	//get this init parameter value
				try
				{
					final URI pathFrameBindingURI=new URI(initParameterValue);	//create a URI from the frame binding expression
					final String path=pathFrameBindingURI.getRawPath();	//extract the path from the URI
					if(path!=null)	//if a path was specified
					{
						final String className=pathFrameBindingURI.getQuery();	//get the decoded query (using the decoded form will allow users to express values that might be legal class names but not legal URI characters)
						if(className!=null)	//if a class name was specified
						{
							try
							{
								final Class<?> specifiedClass=Class.forName(className);	//load the class for the specified name
								final Class<? extends NavigationFrame> navigationFrameClass=specifiedClass.asSubclass(NavigationFrame.class);	//cast the specified class to a frame class just to make sure it's the correct type
								getGuiseApplication().bindNavigationFrame(path, navigationFrameClass);	//cast the class to a frame class and bind it to the path in the Guise application
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
		final HTTPGuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseApplication, request);	//retrieves the Guise session for this application and request
		final HTTPServletGuiseContext guiseContext=new HTTPServletGuiseContext(guiseSession, request, response);	//create a new Guise context
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
			if(!rawPathInfo.startsWith(ROOT_PATH))	//the Java servlet specification says that the path into will start with a '/'
			{
				throw new IllegalArgumentException("Expected absolute path info, received "+rawPathInfo);
			}
			final String navigationPath=rawPathInfo.substring(1);	//remove the beginning slash to get the navigation path from the path info
			final NavigationFrame navigationFrame=guiseSession.getBoundNavigationFrame(navigationPath);	//get the frame bound to the requested path
			if(navigationFrame!=null)	//if we found a frame class for this address
			{
				guiseSession.updateNavigationPath(navigationPath);	//make sure the Guise session has the correct navigation path
				try
				{
					navigationFrame.validateView(guiseContext);		//tell the frame to validate its view
					navigationFrame.updateModel(guiseContext);	//tell the frame to update its model
				}
				catch(final ValidationException validationException)	//if there were any validation errors
				{
					navigationFrame.setError(validationException);	//store the validation error(s) so that the frame can report them to the user
				}
				final URI requestedNavigationURI=guiseSession.getRequestedNavigationURI();	//get the requested navigation URI
				if(requestedNavigationURI!=null)	//if navigation is requested
				{
					guiseSession.clearRequestedNavigationURI();	//remove any navigation requests
					throw new HTTPMovedTemporarilyException(requestedNavigationURI);	//redirect to the new navigation location
				}
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
	}

	/**A Guise application for Guise HTTP servlets.
	@author Garret Wilson
	*/
	protected class HTTPServletGuiseApplication extends AbstractGuiseApplication<HTTPServletGuiseContext>
	{
		/**The synchronized map of Guise sessions keyed to HTTP sessions.*/
		private final Map<HttpSession, HTTPGuiseSession> guiseSessionMap=synchronizedMap(new HashMap<HttpSession, HTTPGuiseSession>());

		/**Retrieves a Guise session for the given HTTP session.
		A Guise session will be created if none is currently associated with the given HTTP session.
		This method can only be accessed by classes in the same package.
		This method should only be called by HTTP Guise session manager.
		@param httpSession The HTTP session for which a Guise session should be retrieved. 
		@return The Guise session associated with the provided HTTP session.
		@see HTTPGuiseSessionManager
		*/
		HTTPGuiseSession getGuiseSession(final HttpSession httpSession)
		{
			synchronized(guiseSessionMap)	//don't allow anyone to modify the map of sessions while we access it
			{
				HTTPGuiseSession guiseSession=guiseSessionMap.get(httpSession);	//get the Guise session associated with the HTTP session
				if(guiseSession==null)	//if no Guise session is associated with the given HTTP session
				{
					guiseSession=createGuiseSession(httpSession);	//create a new Guise session
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
		HTTPGuiseSession removeGuiseSession(final HttpSession httpSession)
		{
			return guiseSessionMap.remove(httpSession);	//remove the HTTP session and Guise session association
		}

		/**Factory method to create a Guise session from an HTTP session.
		@param httpSession The HTTP session for which a Guise session should be created.
		@return A new Guise session corresponding to the given HTTP session.
		*/ 
		protected HTTPGuiseSession createGuiseSession(final HttpSession httpSession)
		{
			return new HTTPGuiseSession(this, httpSession);	//create a default HTTP guise session
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
}
