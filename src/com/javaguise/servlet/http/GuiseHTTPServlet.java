package com.javaguise.servlet.http;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.*;

import static java.util.Arrays.*;
import static java.util.Collections.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.javaguise.*;
import com.javaguise.component.*;
import com.javaguise.context.GuiseContext;
import com.javaguise.controller.Controller;
import com.javaguise.controller.text.xml.xhtml.*;
import com.javaguise.model.Model;
import com.javaguise.session.*;
import com.javaguise.validator.*;

import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;

import com.garretwilson.lang.ObjectUtilities;
import static com.garretwilson.lang.ObjectUtilities.*;
import com.garretwilson.net.http.*;

import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import static com.garretwilson.servlet.ServletConstants.*;
import static com.garretwilson.text.CharacterConstants.*;
import static com.garretwilson.text.xml.XMLConstants.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

import com.garretwilson.security.Nonce;
import com.garretwilson.util.*;
import com.globalmentor.webapps.globalmentor.groups.MentorGroups;

import static com.garretwilson.util.LocaleUtilities.*;

/**The servlet that controls a Guise web applications. 
Only one Guise context will be active at one one time for a single session, so any Guise contexts that are not inactive can be assured that they will be the only context accessing the data.
Navigation frame bindings for paths can be set in initialization parameters named <code>navigation.<var>frameID</var></code>, with values in the form <code><var>contextRelativePath</var>?class=<var>com.example.Frame</var></code>.
This implementation only works with Guise applications that descend from {@link AbstractGuiseApplication}.
@author Garret Wilson
*/
public class GuiseHTTPServlet extends DefaultHTTPServlet
{

	/**The init parameter, "applicationClass", used to specify the application class.*/
	public final static String APPLICATION_CLASS_INIT_PARAMETER_PREFIX="applicationClass";
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

	/**The context parameter of the data base directory.*/
	public final static String DATA_BASE_DIRECTORY_CONTEXT_PARAMETER="dataBaseDirectory";

	/**The suffic for AJAX requests for each navigation path.*/
	public final static String AJAX_URI_SUFFIX="_guise_ajax";

	/**Whether debug is turned on for Mentor Groups.*/
	protected final static boolean DEBUG=true;	//TODO load this from an init parameter

	/**The minimum level of debug reporting for Mentor Groups.*/
	protected final static Debug.ReportLevel DEBUG_LEVEL=Debug.ReportLevel.TRACE;	//TODO load this from an init parameter

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

	/**Determines the debug log file.
	@param context The servlet context from which to retrieve context parameters.
	@return The file for debug logging.
	@see com.garretwilson.servlet.ServletUtilities#getWebInfDirectory
	@see #DATA_BASE_DIRECTORY_CONTEXT_PARAMETER
	*/
	public static File getDebugLogFile(final ServletContext context)
	{
		return new File(getDataBaseDirectory(context), "debug.log");	//TODO use a constant
	}

	/**Determines the data base directory, in this order:
	<ol>
		<li>The file for the value of the context parameter <code>dataBaseDirectory</code>.</li>
		<li>The file for the real path to "/WEB-INF".</li>
	</ol>
	@param context The servlet context from which to retrieve context parameters.
	@return The data base directory.
	@see com.garretwilson.servlet.ServletUtilities#getWebInfDirectory
	@see #DATA_BASE_DIRECTORY_CONTEXT_PARAMETER
	*/
	public static File getDataBaseDirectory(final ServletContext context)
	{
		final String path=context.getInitParameter(DATA_BASE_DIRECTORY_CONTEXT_PARAMETER);	//get the context parameter
		return path!=null ? new File(path) : getWebInfDirectory(context);	//return a file for the path, or the default WEB-INF-based path
	}

	/**The Guise container that owns the applications.*/
	private HTTPServletGuiseContainer guiseContainer=null;

		/**Determines the Guise container. If one does not exist, one will be created.
		This method must not be called before a request is processed.
		@return The Guise container that owns the applications.
		@exception IllegalStateException if this method is called before any requests have been processed.
		*/
		protected HTTPServletGuiseContainer getGuiseContainer()	//TODO probably move this logic to init(HttpServletRequest)
		{
			if(guiseContainer==null)	//if no container exists
			{
				final String guiseContainerBasePath=getContextPath()+PATH_SEPARATOR;	//construct the Guise container base path from the servlet request, which is the web application path with an ending slash
				guiseContainer=new HTTPServletGuiseContainer(guiseContainerBasePath);	//create a new container
			}
			return guiseContainer;	//return the Guise container
		}

	/**The Guise application controlled by this servlet.*/
	private AbstractGuiseApplication guiseApplication;

		/**@return The Guise application controlled by this servlet.*/
		protected AbstractGuiseApplication getGuiseApplication() {return guiseApplication;}

	/**Default constructor.
	Creates a single Guise application.
	*/
	public GuiseHTTPServlet()
	{
		Debug.setDebug(DEBUG);	//turn on debug if needed
	}
		
	/**Initializes the servlet.
	@param servletConfig The servlet configuration. 
	@exception ServletException if there is a problem initializing.
	*/
	public void init(final ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);	//do the default initialization
		Debug.setMinimumReportLevel(DEBUG_LEVEL);	//set the level of reporting
		try
		{
			Debug.setOutput(MentorGroups.getDebugLogFile(getServletContext()));	//set the log file
		}
		catch(final FileNotFoundException fileNotFoundException)	//if we can't find the debug file
		{
			throw new ServletException(fileNotFoundException);
		}
		
		
		
		setReadOnly(true);	//make this servlet read-only
		//TODO turn off directory listings, and/or fix them
		try
		{
			guiseApplication=initGuiseApplication(servletConfig);	//initialize the application and frame bindings
		}
		catch(final Exception exception)	//if there is any problem initializing the Guise application
		{
			throw new ServletException("Error initializing Guise application: "+exception.getMessage(), exception);
		}
	}

	/**Initializes bindings between paths and associated navigation frame classes.
	This implementation reads the initialization parameters named <code>navigation.<var>frameID</var></code>, expecting values in the form <code><var>contextRelativePath</var>?class=<var>com.example.Frame</var></code>.
	@param servletConfig The servlet configuration.
	@return The new initialized application. 
	@exception IllegalArgumentException if the one of the frame bindings is not expressed in correct format.
	@exception IllegalArgumentException if the one of the classes specified as a frame binding could not be found.
	@exception IllegalArgumentException if the one of the classes specified as a frame binding could not be found.
	@exception ClassCastException if the one of the classes specified as a frame binding does not represent a subclass of a frame component.
	@exception ServletException if there is a problem initializing the application or frame bindings.
	@see Frame
	*/
	protected AbstractGuiseApplication initGuiseApplication(final ServletConfig servletConfig) throws ServletException
	{
		final AbstractGuiseApplication guiseApplication;	//create the application and store it here
		final String guiseApplicationClassName=servletConfig.getInitParameter(APPLICATION_CLASS_INIT_PARAMETER_PREFIX);	//get name of the guise application class
		if(guiseApplicationClassName!=null)	//if there is a Guise application class name specified
		{
			try
			{
				guiseApplication=(AbstractGuiseApplication)Class.forName(guiseApplicationClassName).newInstance();	//create the Guise application from the specified class
			}
			catch(final ClassNotFoundException classNotFoundException)	//if the application class cannot be found
			{
				throw new IllegalArgumentException("The initialization parameter specified application class "+guiseApplicationClassName+" could not be found.", classNotFoundException);						
			}			
			catch(final InstantiationException instantiationException)	//if the application class could not be instantiated
			{
				throw new IllegalArgumentException("The initialization parameter specified application class "+guiseApplicationClassName+" is an interface or an abstract class.", instantiationException);						
			}			
			catch(final IllegalAccessException illegalAccessException)	//if the application class constructor cannot be accessed
			{
				throw new IllegalArgumentException("The initialization parameter specified application class "+guiseApplicationClassName+" does not have an accessible constructor.", illegalAccessException);						
			}			
		}
		else	//if no Guise application class was specified
		{
			guiseApplication=new DefaultGuiseApplication();	//create a default application
		}
		guiseApplication.installControllerKit(new XHTMLControllerKit());	//create and install an XHTML controller kit
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
			CollectionUtilities.addAll(guiseApplication.getSupportedLocales(), supportedLocales);	//add all supported locales to the application 
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
//TODO bring back for JDK 5 when backwards-compatibility isn't needed								final Class<? extends Frame> navigationFrameClass=specifiedClass.asSubclass(Frame.class);	//cast the specified class to a frame class just to make sure it's the correct type
								final Class<? extends Frame> navigationFrameClass=(Class<? extends Frame>)specifiedClass;	//cast the specified class to a frame class just to make sure it's the correct type
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
		return guiseApplication;	//return the created Guise application
	}

	/**Initializes the servlet upon receipt of the first request.
	This version installs the application into the container.
	@param request The servlet request.
	@exception IllegalStateException if this servlet has already been initialized from a request.
	@exception ServletException if there is a problem initializing.
	*/
	public void init(final HttpServletRequest request) throws ServletException
	{
		super.init(request);	//do the default initialization
		final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container
		final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
		if(guiseApplication.getContainer()==null)	//if this application has not yet been installed (note that there is a race condition here if multiple HTTP requests attempt to access the application simultaneously, but the losing thread will simply throw an exception and not otherwise disturb the application functionality)
		{
			final String guiseApplicationContextPath=request.getContextPath()+request.getServletPath()+PATH_SEPARATOR;	//construct the Guise application context path from the servlet request, which is the concatenation of the web application path and the servlet's path with an ending slash
			guiseContainer.installApplication(guiseApplication, guiseApplicationContextPath);	//install the application			
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
		final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container
		final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
		final HTTPServletGuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieves the Guise session for this container and request
		final HTTPServletGuiseContext guiseContext=new HTTPServletGuiseContext(guiseSession, request, response);	//create a new Guise context
		synchronized(guiseSession)	//don't allow other session contexts to be active at the same time
		{
			guiseSession.setContext(guiseContext);	//set the context for this session
			try
			{
//TODO del Debug.trace("before getting requested URI, application base path is:", guiseApplication.getBasePath());
				final URI requestURI=URI.create(request.getRequestURL().toString());	//get the URI of the current request		
					//TODO get the raw path info from the request URI
				final String rawPathInfo=getRawPathInfo(request);	//get the raw path info
		//TODO del System.out.println("raw path info: "+rawPathInfo);
				if(rawPathInfo.endsWith(AJAX_URI_SUFFIX))	//if this is an AJAX request
				{
					serviceAJAX(request, response, guiseContainer, guiseApplication, guiseSession, guiseContext);	//service this AJAX request
				}
				else	//if this is a normal request
				{
//TODO del Debug.trace("raw path info", rawPathInfo);
//TODO del Debug.trace("Referrer:", getReferer(request));
					assert isAbsolutePath(rawPathInfo) : "Expected absolute path info, received "+rawPathInfo;	//the Java servlet specification says that the path info will start with a '/'
					final String navigationPath=rawPathInfo.substring(1);	//remove the beginning slash to get the navigation path from the path info
					final Frame<?> navigationFrame=guiseSession.getNavigationFrame(navigationPath);	//get the frame bound to the requested path
					if(navigationFrame!=null)	//if we found a frame class for this address
					{
						setNoCache(response);	//TODO testing; fix; update method
						
							//before actually changing the navigation path, check to see if we're in the middle of modal navigation (only do this after we find a navigation frame, as this request might be for a stylesheet or some other non-frame resource, which shouldn't be redirected)
						final ModalNavigation modalNavigation=guiseSession.peekModalNavigation();	//see if we are currently doing modal navigation TODO make public access routines
						if(modalNavigation!=null)	//if we are currently in the middle of modal navigation, make sure the correct frame was requested
						{
							final URI modalNavigationURI=modalNavigation.getNewNavigationURI();	//get the modal navigation URI
							if(!requestURI.getRawPath().equals(modalNavigationURI.getRawPath()))		//if this request was for a different path than our current modal navigation path (we wouldn't be here if the domain, application, etc. weren't equivalent)
							{
								throw new HTTPMovedTemporarilyException(modalNavigationURI);	//redirect to the modal navigation location				
							}
						}
							//update the frame's referrer
						final String referrer=getReferer(request);	//see if the request has a referrer
						if(referrer!=null && navigationFrame.getReferrerURI()==null)	//if the request indicates a referrer, but the navigation frame has not yet been updated with a referrer
						{
							final URI plainReferrerURI=getPlainURI(URI.create(referrer));	//get a plain URI version of the referrer
							if(!plainReferrerURI.equals(getPlainURI(requestURI)))	//if we aren't being referred from ourselves
							{
								navigationFrame.setReferrerURI(plainReferrerURI);	//update the frame's referrer URI
							}
						}
						guiseSession.setNavigationPath(navigationPath);	//make sure the Guise session has the correct navigation path
						final Principal oldPrincipal=guiseSession.getPrincipal();	//get the old principal
						if(guiseContext.getParameterListMap().size()>0)	//only query the view if there were submitted values---especially important for radio buttons and checkboxes, which must assume a value of false if nothing is submitted for them, thereby updating the model
						{
							guiseContext.setState(GuiseContext.State.QUERY_VIEW);	//update the context state for querying the view
	//TODO del Debug.trace("ready to query the navigation frame view");
							navigationFrame.queryView(guiseContext);		//tell the frame to query its view
							guiseContext.setState(GuiseContext.State.DECODE_VIEW);	//update the context state for decoding the view
							try
							{
								navigationFrame.decodeView(guiseContext);		//tell the frame to decode its view
							}
							catch(final ValidationsException validationsException)	//if there were any validation errors during validation
							{
								navigationFrame.addErrors(validationsException);	//store the validation error(s) so that the frame can report them to the user
							}
							guiseContext.setState(GuiseContext.State.UPDATE_MODEL);	//update the context state for updating the model
							try
							{
								navigationFrame.updateModel(guiseContext);	//tell the frame to update its model
							}
							catch(final ValidationsException validationsException)	//if there were any validation errors during validation
							{
								navigationFrame.addErrors(validationsException);	//store the validation error(s) so that the frame can report them to the user
							}									
						}
						guiseContext.setState(GuiseContext.State.QUERY_MODEL);	//update the context state for querying the model
						navigationFrame.queryModel(guiseContext);		//tell the frame to query its model
						guiseContext.setState(GuiseContext.State.ENCODE_MODEL);	//update the context state for encoding the model
						navigationFrame.encodeModel(guiseContext);		//tell the frame to encode its model
						guiseContext.setState(GuiseContext.State.UPDATE_VIEW);	//update the context state for updating the view; make the change now in case queued model changes want to navigate, and an error was thrown when updating the model) TODO see if this comment is relevant after new local-error-catching changes above
						if(!ObjectUtilities.equals(oldPrincipal, guiseSession.getPrincipal()))	//if the principal has changed after updating the model
						{
							throw new HTTPMovedTemporarilyException(guiseContext.getNavigationURI());	//redirect to the same page, which will generate a new request with no POST parameters, which would likely change the principal again)
						}
						final Navigation requestedNavigation=guiseSession.getRequestedNavigation();	//get the requested navigation
						if(requestedNavigation!=null)	//if navigation is requested
						{
							final URI requestedNavigationURI=requestedNavigation.getNewNavigationURI();
							guiseSession.clearRequestedNavigation();	//remove any navigation requests
							if(requestedNavigation instanceof ModalNavigation)	//if modal navigation was requested
							{
								beginModalNavigation(guiseApplication, guiseSession, (ModalNavigation<?>)requestedNavigation);	//begin the modal navigation
							}
							throw new HTTPMovedTemporarilyException(requestedNavigationURI);	//redirect to the new navigation location
						}
						navigationFrame.updateView(guiseContext);		//tell the frame to update its view
					}
					else	//if we have no frame type for this address
					{
							//TODO del the redirect code here now that this is done when we get the request URI
		//TODO del Debug.trace("could not find navigation path \""+navigationPath+"\", trying to load resource.");
		//TODO del when works				try
						{
							super.doGet(request, response);	//let the default functionality take over					
						}
		/*TODO del when works
						catch(final HTTPNotFoundException httpNotFoundException)	//if the default version couldn't find the resource
						{
							if(!isContainerPath(navigationPath))	//if the navigation path is not a container
							{
								final String containerPath=navigationPath+PATH_SEPARATOR;	//create a container path by adding a separator
								if(guiseSession.getNavigationFrame(containerPath)!=null)	//if adding a path separator would give us a navigation frame
								{
									throw new HTTPMovedPermanentlyException(URI.create(guiseApplication.resolvePath(containerPath)));	//redirect to the container path
								}
							}
							throw httpNotFoundException;	//rethrow the exception if we can't find a collection the user was trying to access
						}
		*/
					}
				}
			}
			finally
			{
				guiseContext.setState(GuiseContext.State.INACTIVE);	//always deactivate the context			
				guiseSession.setContext(null);	//remove this context from the session
			}
		}
	}

/*TODO del if not needed
	protected <T extends Component<?>> Component<?> getComponentByAbsoluteUniqueID(final T component, final String absoluteUniqueID)
	{
		final Controller<? extends GuiseContext<?>, ? super T> controller=component.getController();
		if(controller.getAbsoluteUniqueID(component).equals(absoluteUniqueID))
		{
			return component;
		}
		else
		{
			for(final Component<?> childComponent:component)
			{
				final Component<?> selectedComponent=getComponentByAbsoluteUniqueID(childComponent, absoluteUniqueID);
				if(selectedComponent!=null)
				{
					return selectedComponent;
				}
			}
		}
		return null;
	}
*/

	/**Services an AJAX request.
  @param request The HTTP request.
  @param response The HTTP response.
  @param guiseContainer The Guise container.
  @param guiseApplication The Guise application.
  @param guiseSession The Guise session.
  @param guiseContext The Guise context.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void serviceAJAX(final HttpServletRequest request, final HttpServletResponse response, final HTTPServletGuiseContainer guiseContainer, final AbstractGuiseApplication guiseApplication, final HTTPServletGuiseSession guiseSession, final HTTPServletGuiseContext guiseContext) throws ServletException, IOException
	{
		final URI requestURI=URI.create(request.getRequestURL().toString());	//get the URI of the current request		
		final String rawPathInfo=getRawPathInfo(request);	//get the raw path info
		final String navigationPath=rawPathInfo.substring(1, rawPathInfo.length()-AJAX_URI_SUFFIX.length());	//remove the beginning slash and the AJAX suffix
Debug.trace("got an AJAX request for navigation path", navigationPath);
		if(Debug.isDebug() && Debug.getReportLevels().contains(Debug.ReportLevel.INFO))	//indicate the parameters if information tracing is turned on
		{
			Debug.info("Received AJAX parameters:");
			final ListMap<Object, Object> parameterListMap=guiseContext.getParameterListMap();	//get the request parameter map
			for(final Map.Entry<Object, List<Object>> parameterListMapEntry:parameterListMap.entrySet())	//for each entry in the map of parameter lists
			{
				Debug.info("Key:", parameterListMapEntry.getKey(), "Value:", ArrayUtilities.toString(parameterListMapEntry.getValue().toArray()));				
			}
		}
//	TODO del			final String navigationPath=(String)guiseContext.getParameterListMap().getItem("navigationPath");	//TODO decode param value
		final Frame<?> navigationFrame=guiseSession.getNavigationFrame(navigationPath);	//get the frame bound to the requested path
		if(navigationFrame!=null)	//if we found a frame class for this address
		{
			final Set<Component<?>> requestedComponents=new HashSet<Component<?>>();	//create a set of component that were identified in the request
			final ListMap<Object, Object> parameterListMap=guiseContext.getParameterListMap();	//get the request parameter map
			for(final Map.Entry<Object, List<Object>> parameterListMapEntry:parameterListMap.entrySet())	//for each entry in the map of parameter lists
			{
				final String parameterName=(String)parameterListMapEntry.getKey();	//get the parameter name
/*TODO del if not needed
				final List<Object> parameterValues=parameterListMapEntry.getValue();	//get the parameter values
				final String parameterValue=parameterValues.size()>0 ? asInstance(parameterValues.get(0), String.class) : null;	//get the first parameter value
*/
				//TODO don't re-update nested components (less important for controls, which don't have nested components) 
//TODO del Debug.trace("looking for component with name", parameterName);
				getControlsByName(navigationFrame, parameterName, requestedComponents);	//get all components identified by this name
			}
			if(!requestedComponents.isEmpty())	//if components were requested
			{
				final Set<Component<?>> affectedComponents=new HashSet<Component<?>>(requestedComponents);	//we'll keep track of components that were affected by this update cycle (always include the requested components, just in case they changed their views but the model did not change---if an erroneous view value changed back to the previous, non-erroneous value, for example) 
				guiseContext.setState(GuiseContext.State.QUERY_VIEW);	//update the context state for querying the view
				for(final Component<?> component:requestedComponents)	//for each requested component
				{
//TODO del Debug.trace("AJAX component:", component);
					component.queryView(guiseContext);		//tell the component to query its view
				}
				guiseContext.setState(GuiseContext.State.DECODE_VIEW);	//update the context state for decoding the view
				for(final Component<?> component:requestedComponents)	//for each requested component
				{
					try
					{
						component.decodeView(guiseContext);		//tell the frame to decode its view
					}
					catch(final ValidationsException validationsException)	//if there were any validation errors during validation
					{
						for(final ValidationException validationException:validationsException)	//for each validation exception
						{
							final Component<?> affectedComponent=validationException.getComponent();	//see if this error is for a component
							if(affectedComponent!=null)	//if this validation exception was for a specific component
							{
								affectedComponents.add(affectedComponent);	//add this component to our list of affected components
							}
						}
					}
				}
				guiseContext.setState(GuiseContext.State.UPDATE_MODEL);	//update the context state for updating the model
				for(final Component<?> component:requestedComponents)	//for each requested component
				{
					try
					{
						component.updateModel(guiseContext);	//tell the frame to update its model
					}
					catch(final ValidationsException validationsException)	//if there were any validation errors during validation
					{
						for(final ValidationException validationException:validationsException)	//for each validation exception
						{
							final Component<?> affectedComponent=validationException.getComponent();	//see if this error is for a component
							if(affectedComponent!=null)	//if this validation exception was for a specific component
							{
								affectedComponents.add(affectedComponent);	//add this component to our list of affected components
							}
						}
					}
				}
				guiseContext.setState(GuiseContext.State.INACTIVE);	//deactivate the context so that any model update events will be generated			
//TODO del Debug.trace("we now have events:", guiseContext.getEventList().size());
				for(final EventObject contextEvent:guiseContext.getEventList())	//for each event generated in this context
				{
//				TODO del Debug.trace("context event:", contextEvent);
					final Object source=contextEvent.getSource();	//get the event source
					if(source instanceof Model)	//if this was a model change
					{
						affectedComponents.addAll(AbstractModelComponent.getModelComponents(navigationFrame, (Model)source));	//get all components that use this model
					}
				}
//			TODO del Debug.trace("we now have affected components:", affectedComponents.size());
				guiseContext.setState(GuiseContext.State.QUERY_MODEL);	//update the context state for querying the model
				for(final Component<?> affectedComponent:affectedComponents)	//for each component affected by this update cycle
				{
//				TODO del Debug.trace("affected component:", affectedComponent);
					affectedComponent.queryModel(guiseContext);		//tell the component to query its model
				}
				guiseContext.setState(GuiseContext.State.ENCODE_MODEL);	//update the context state for encoding the model
				for(final Component<?> affectedComponent:affectedComponents)	//for each component affected by this update cycle
				{
					affectedComponent.encodeModel(guiseContext);		//tell the component to encode its model
				}				
				guiseContext.setState(GuiseContext.State.UPDATE_VIEW);	//update the context state for updating the view; make the change now in case queued model changes want to navigate, and an error was thrown when updating the model)
				guiseContext.setOutputContentType(XML_CONTENT_TYPE);	//switch to the "text/xml" content type
				guiseContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_HTML);	//<xhtml:html>
				guiseContext.writeAttribute(null, ATTRIBUTE_XMLNS, XHTML_NAMESPACE_URI.toString());	//xmlns="http://www.w3.org/1999/xhtml"
				for(final Component<?> affectedComponent:affectedComponents)	//for each component affected by this update cycle
				{
					affectedComponent.updateView(guiseContext);		//tell the component to update its view
				}
				guiseContext.writeElementEnd();	//</xhtml:html>
			}
		}
	}
	
	protected <T extends Component<?>> void getControlsByName(final T component, final String name, final Set<Component<?>> componentSet)
	{
			//TODO check first that the component is a control; that should be much faster
		final Controller<? extends GuiseContext<?>, ?> controller=component.getController();
		if(controller instanceof XHTMLControlController)
		{
			final XHTMLControlController xhtmlControlController=(XHTMLControlController)controller;
Debug.trace("checking control with ID", xhtmlControlController.getAbsoluteUniqueID(component), "and name", xhtmlControlController.getComponentName((Control)component));
			if(name.equals(xhtmlControlController.getComponentName((Control)component)))	//TODO comment: the returned name can be null
			{
Debug.trace("using this component");
				componentSet.add(component);
			}
		}
		for(final Component<?> childComponent:component)
		{
			getControlsByName(childComponent, name, componentSet);
		}
	}


  /**Determines if the resource at a given URI exists.
  This version adds checks to see if the URI represents a valid application navigation path.
  @param resourceURI The URI of the requested resource.
  @return <code>true</code> if the resource exists, else <code>false</code>.
	@exception IOException if there is an error accessing the resource.
  */
  protected boolean exists(final URI resourceURI) throws IOException
  {
  	final GuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
  	if(guiseApplication.hasNavigationPath(guiseApplication.relativizeURI(resourceURI)))	//if the URI represents a valid navigation path
  	{
  		return true;	//the navigation path exists
  	}
  	else	//if there is no navigation path
  	{
  		return super.exists(resourceURI);	//see if a physical resource exists at the location
  	}
  }

	/**Begins modal navigation based upon modal navigation information.
	@param <R> The type of modal result the modal navigation involves.
	@param guiseApplication The Guise application.
	@param guiseSession The Guise session.
	@param modalNavigation The modal navigation information
	*/
	protected <R> void beginModalNavigation(final GuiseApplication guiseApplication, final GuiseSession<?> guiseSession, final ModalNavigation<R> modalNavigation)
	{
		final ModalFrame<R, ?> modalFrame=(ModalFrame<R, ?>)guiseSession.getNavigationFrame(guiseApplication.relativizeURI(modalNavigation.getNewNavigationURI()));	//get the modal frame for this navigation path
		if(modalFrame!=null)	//if we have a modal frame
		{
			guiseSession.beginModalNavigation(modalFrame, modalNavigation);
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

	/**Looks up a principal from the given ID.
	This version delegates to the Guise container.
	@param id The ID of the principal.
	@return The principal corresponding to the given ID, or <code>null</code> if no principal could be determined.
	@exception HTTPInternalServerErrorException if there is an error getting the principal.
	*/
	protected Principal getPrincipal(final String id) throws HTTPInternalServerErrorException
	{
		return getGuiseContainer().getPrincipal(getGuiseApplication(), id);	//delegate to the container
	}

	/**Looks up the corresponding password for the given principal.
	This version delegates to the Guise container.
	@param principal The principal for which a password should be returned.
	@return The password associated with the given principal, or <code>null</code> if no password is associated with the given principal.
	@exception HTTPInternalServerErrorException if there is an error getting the principal's password.
	*/
	protected char[] getPassword(final Principal principal) throws HTTPInternalServerErrorException
	{
		return getGuiseContainer().getPassword(getGuiseApplication(), principal);	//delegate to the container
	}

	/**Determines the realm applicable for the resource indicated by the given URI.
	This version delegates to the container.
	@param resourceURI The URI of the resource requested.
	@return The realm appropriate for the resource, or <code>null</code> if the given resource is not in a known realm.
	@exception HTTPInternalServerErrorException if there is an error getting the realm.
	*/
	protected String getRealm(final URI resourceURI) throws HTTPInternalServerErrorException
	{
		return getGuiseContainer().getRealm(getGuiseApplication(), resourceURI);	//delegate to the container
	}

	/**Checks whether the given principal is authorized to invoke the given method on the given resource.
	This version delegates to the Guise container, using the principal of the current Guise session instead of the given principal.
	This technique allows browser-based authentication to function normally (as successful authentication will have already updated the session's principal),
	and also allows browser-based authentication to work with session-based authentication in the even that the session has already authenticated a principal unknown to the browser.
  @param request The HTTP request.
	@param resourceURI The URI of the resource requested.
	@param method The HTTP method requested on the resource.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param realm The realm with which the resource is associated, or <code>null</code> if the realm is not known.
	@return <code>true</code> if the given principal is authorized to perform the given method on the resource represented by the given URI.
	@exception HTTPInternalServerErrorException if there is an error determining if the principal is authorized.
	*/
	protected boolean isAuthorized(final HttpServletRequest request, final URI resourceURI, final String method, final Principal principal, final String realm) throws HTTPInternalServerErrorException
	{
		final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container
		final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
		final HTTPServletGuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieves the Guise session for this container and request
		return getGuiseContainer().isAuthorized(getGuiseApplication(), resourceURI, guiseSession.getPrincipal(), realm);	//delegate to the container, using the current Guise session
	}

	/**Determines if the given nonce is valid.
	This version counts a nonce as invalid if it was associated with a different principal than the current Guise session principal (i.e. the Guise principal was logged out).
  @param request The HTTP request.
	@param nonce The nonce to check for validity.
	@return <code>true</code> if the nonce is not valid.
	*/
	protected boolean isValid(final HttpServletRequest request, final Nonce nonce)
	{
//	TODO del Debug.trace("ready to check validity of nonce; default validity", nonce);
		if(!super.isValid(request, nonce))	//if the nonce doesn't pass the normal validity checks
		{
//		TODO del Debug.trace("doesn't pass the basic checks");
			return false;	//the nonce isn't valid
		}
		final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container
		final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
		final HTTPServletGuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieves the Guise session for this container and request
		final Principal guiseSessionPrincipal=guiseSession.getPrincipal();	//get the current principal of the Guise session
		final String guiseSessionPrincipalID=guiseSessionPrincipal!=null ? guiseSessionPrincipal.getName() : null;	//get the current guise session principal ID
//	TODO del Debug.trace("checking to see if nonce principal ID", getNoncePrincipalID(nonce), "matches Guise session principal ID", guiseSessionPrincipalID); 
		if(!ObjectUtilities.equals(getNoncePrincipalID(nonce), guiseSessionPrincipalID))	//if this nonce was for a different principal
		{
			return false;	//the user must have logged out or have been changed
		}
//	TODO del Debug.trace("nonce is valid");
		return true;	//indicate that the nonce passed all the tests
	}

	/**Called when a principal has went through authentication and indicates the result of authentication.
	This version stores the authenticated principal in the current Guise session if authentication was successful for valid credentials.
  @param request The HTTP request.
	@param resourceURI The URI of the resource requested.
	@param method The HTTP method requested on the resource.
	@param requestURI The request URI as given in the HTTP request.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param realm The realm for which the principal was authenticated.
	@param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	@param authenticated <code>true</code> if the principal succeeded in authentication, else <code>false</code>.
	@see HTTPServletGuiseSession#setPrincipal(Principal)
	*/
	protected void authenticated(final HttpServletRequest request, final URI resourceURI, final String method, final String requestURI, final Principal principal, final String realm, final AuthenticateCredentials credentials, final boolean authenticated)
	{
		if(authenticated && credentials!=null)	//if authentication was successful with credentials (don't change the session principal for no credentials, because this might remove a principal set by the session itself with no knowledge of the browser)
		{
			final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container
			final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
			final HTTPServletGuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieves the Guise session for this container and request
			guiseSession.setPrincipal(principal);	//set the new principal in the Guise session
		}
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
					guiseSession.initialize();	//let the Guise session know it's being initializes so that it can listen to the application
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

}
