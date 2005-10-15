package com.javaguise.servlet.http;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.*;

import static java.util.Arrays.*;
import static java.util.Collections.*;

import javax.mail.internet.ContentType;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.fileupload.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.javaguise.*;
import com.javaguise.component.*;
import com.javaguise.component.transfer.*;
import com.javaguise.context.GuiseContext;
import com.javaguise.context.text.xml.XMLGuiseContext;
import com.javaguise.controller.*;
import com.javaguise.controller.text.xml.xhtml.*;
import com.javaguise.model.FileItemResourceImport;
import com.javaguise.model.Model;
import com.javaguise.platform.web.*;
import static com.javaguise.platform.web.WebPlatformConstants.*;
import com.javaguise.session.*;
import com.javaguise.validator.*;
import com.javaguise.view.text.xml.xhtml.XHTMLApplicationFrameView;
import com.javaguise.view.text.xml.xhtml.XHTMLNavigationPanelView;

import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;

import com.garretwilson.io.ContentTypeConstants;
import com.garretwilson.io.FileUtilities;
import com.garretwilson.lang.ObjectUtilities;

import static com.garretwilson.io.ContentTypeConstants.*;
import static com.garretwilson.io.ContentTypeUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import com.garretwilson.net.http.*;
import static com.garretwilson.net.http.HTTPConstants.*;

import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import static com.garretwilson.servlet.ServletConstants.*;
import static com.garretwilson.text.CharacterConstants.*;
import static com.garretwilson.text.CharacterEncodingConstants.UTF_8;
import static com.garretwilson.text.xml.XMLConstants.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

import com.garretwilson.security.Nonce;
import com.garretwilson.text.xml.XMLUtilities;
import com.garretwilson.text.xml.xhtml.XHTMLConstants;
import com.garretwilson.text.xml.xpath.PathExpression;
import com.garretwilson.text.xml.xpath.XPath;
import com.garretwilson.util.*;

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
	public final static String APPLICATION_CLASS_INIT_PARAMETER="applicationClass";
	/**The init parameter, "defaultLocale", used to specify the default locale.*/
	public final static String DEFAULT_LOCALE_INIT_PARAMETER="defaultLocale";
	/**The init parameter, "supportedLocales", used to specify the supported locales.*/
	public final static String SUPPORTED_LOCALES_INIT_PARAMETER="supportedLocales";
	/**The prefix, "navigation.", used to identify navigation definitions in the web application's init parameters.*/
	public final static String NAVIGATION_INIT_PARAMETER_PREFIX="navigation.";
	/**The parameter, "class", used to identify the navigation frame class in the web application's init parameters.*/
	public final static String NAVIGATION_CLASS_PARAMETER="class";
	/**The init parameter, "style", used to specify the style definition URIs.*/
	public final static String STYLE_INIT_PARAMETER="style";

	/**The absolute path, relative to the servlet context, of the resources directory.*/
	public final static String RESOURCES_DIRECTORY_PATH=ROOT_PATH+WEB_INF_DIRECTORY_NAME+PATH_SEPARATOR+"guise-application-resources"+PATH_SEPARATOR;

	/**The context parameter of the data base directory.*/
	public final static String DATA_BASE_DIRECTORY_CONTEXT_PARAMETER="dataBaseDirectory";

	/**The suffic for AJAX requests for each navigation path.*/
	public final static String AJAX_URI_SUFFIX="_guise_ajax";

	/**The content type of a Guise AJAX request, <code>application/x-guise-ajax-request</code>.*/
	public final static ContentType GUISE_AJAX_REQUEST_CONTENT_TYPE=new ContentType(ContentTypeConstants.APPLICATION, ContentTypeConstants.EXTENSION_PREFIX+"guise-ajax-request"+ContentTypeConstants.SUBTYPE_SUFFIX_DELIMITER_CHAR+ContentTypeConstants.XML_SUBTYPE_SUFFIX, null);

	/**The content type of a Guise AJAX response, <code>application/x-guise-ajax-response</code>.*/
	public final static ContentType GUISE_AJAX_RESPONSE_CONTENT_TYPE=new ContentType(ContentTypeConstants.APPLICATION, ContentTypeConstants.EXTENSION_PREFIX+"guise-ajax-response"+ContentTypeConstants.SUBTYPE_SUFFIX_DELIMITER_CHAR+ContentTypeConstants.XML_SUBTYPE_SUFFIX, null);

	/**Whether debug is turned on for Guise.*/
	protected final static boolean DEBUG=true;	//TODO load this from an init parameter

	/**The minimum level of debug reporting for Guise.*/
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
			Debug.setOutput(getDebugLogFile(getServletContext()));	//set the log file
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
		final String guiseApplicationClassName=servletConfig.getInitParameter(APPLICATION_CLASS_INIT_PARAMETER);	//get name of the guise application class
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
		guiseApplication.installComponentKit(new XHTMLComponentKit());	//create and install an XHTML controller kit
			//initialize the supported locales
		final String supportedLocalesString=servletConfig.getInitParameter(SUPPORTED_LOCALES_INIT_PARAMETER);	//get the supported locales init parameter
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
		final String defaultLocaleString=servletConfig.getInitParameter(DEFAULT_LOCALE_INIT_PARAMETER);	//get the default locale init parameter
		if(defaultLocaleString!=null)	//if a default locale is specified
		{
			guiseApplication.setDefaultLocale(createLocale(defaultLocaleString.trim()));	//create a locale from the default locale string and store it in the application (trimming whitespace just to be extra helpful)
		}

			//initialize the style TODO allow for multiple styles
		final String styleString=servletConfig.getInitParameter(STYLE_INIT_PARAMETER);	//get the style init parameter
		if(styleString!=null)	//if a style is specified
		{
			guiseApplication.setStyle(URI.create(styleString));	//create a locale from the default locale string and store it in the application (trimming whitespace just to be extra helpful)
		}

			//initialize navigation path/panel bindings
		final Enumeration initParameterNames=servletConfig.getInitParameterNames();	//get the names of all init parameters
		while(initParameterNames.hasMoreElements())	//while there are more initialization parameters
		{
			final String initParameterName=(String)initParameterNames.nextElement();	//get the next initialization parameter name
			if(initParameterName.startsWith(NAVIGATION_INIT_PARAMETER_PREFIX))	//if this is a path/panel binding
			{
				final String initParameterValue=servletConfig.getInitParameter(initParameterName);	//get this init parameter value
				try
				{
					final URI pathPanelBindingURI=new URI(initParameterValue);	//create a URI from the panel binding expression
					final String path=pathPanelBindingURI.getRawPath();	//extract the path from the URI
					if(path!=null)	//if a path was specified
					{
						final ListMap<String, String> parameterListMap=getParameters(pathPanelBindingURI);	//get the URI parameters
						final String className=parameterListMap.getItem(NAVIGATION_CLASS_PARAMETER);	//get the class parameter
						if(className!=null)	//if a class name was specified
						{
							try
							{
								final Class<?> specifiedClass=Class.forName(className);	//load the class for the specified name
//TODO bring back for JDK 5 when backwards-compatibility isn't needed								final Class<? extends NavigationPanel> navigationFrameClass=specifiedClass.asSubclass(Frame.class);	//cast the specified class to a frame class just to make sure it's the correct type
								final Class<? extends DefaultNavigationPanel> navigationPanelClass=(Class<? extends DefaultNavigationPanel>)specifiedClass;	//cast the specified class to a panel class just to make sure it's the correct type
								guiseApplication.bindNavigationPanel(path, navigationPanelClass);	//cast the class to a panel class and bind it to the path in the Guise application
							}
							catch(final ClassNotFoundException classNotFoundException)
							{
								throw new IllegalArgumentException("The initialization parameter specified class "+className+" for path "+path+" could not be found.", classNotFoundException);						
							}
						}
						else	//if no class name was specified
						{
							throw new IllegalArgumentException("The initialization parameter path/panel binding "+pathPanelBindingURI+" for "+initParameterName+" did not specify a class name.");												
						}
					}
					else	//if no path was specified
					{
						throw new IllegalArgumentException("The initialization parameter path/panel binding "+pathPanelBindingURI+" for "+initParameterName+" did not specify a path.");												
					}
				}
				catch(final URISyntaxException uriSyntaxException)	//if the parameter value was not in the correct format
				{
					throw new IllegalArgumentException("Incorrect initialization parameter path/panel class binding URI "+initParameterValue+" for "+initParameterName, uriSyntaxException);
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
		final String rawPathInfo=getRawPathInfo(request);	//get the raw path info
Debug.trace("raw path info:", rawPathInfo);
		if(rawPathInfo.startsWith(GUISE_PUBLIC_RESOURCE_BASE_PATH))	//if this is a request for a public resource
		{
			final String resourceKey=Guise.PUBLIC_RESOURCE_BASE_PATH+rawPathInfo.substring(GUISE_PUBLIC_RESOURCE_BASE_PATH.length());	//replace the beginning web base path to the resource base path
			final byte[] resource=Guise.getInstance().getPublicResource(resourceKey);	//load the resource
			if(resource!=null)	//if the resource was found
			{
				final ContentType contentType=FileUtilities.getMediaType(rawPathInfo);	//see what content type the resource is
				if(contentType!=null)	//if we know the content type
				{
					response.setContentType(contentType.toString());	//set the response content type
				}
				response.setContentLength(resource.length);	//indicate the size of the resource
					//TODO write encoding if appropriate for content type
					//TODO write cache information
				response.getOutputStream().write(resource);	//write the resource
				return;	//don't process this request further
			}
		}		
		final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container
		final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
		final HTTPServletGuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieves the Guise session for this container and request
/*TODO del
Debug.info("session ID", guiseSession.getHTTPSession().getId());	//TODO del
Debug.info("content length:", request.getContentLength());
Debug.info("content type:", request.getContentType());
*/
		final HTTPServletGuiseContext guiseContext=new HTTPServletGuiseContext(guiseSession, request, response);	//create a new Guise context
		synchronized(guiseSession)	//don't allow other session contexts to be active at the same time
		{
			guiseSession.setContext(guiseContext);	//set the context for this session
			try
			{
//TODO del Debug.trace("before getting requested URI, application base path is:", guiseApplication.getBasePath());
				final URI requestURI=URI.create(request.getRequestURL().toString());	//get the URI of the current request		
				
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
					final NavigationPanel<?> navigationPanel=guiseSession.getNavigationPanel(navigationPath);	//get the panel bound to the requested path
					if(navigationPanel!=null)	//if we found a frame class for this address
					{
						guiseSession.getApplicationFrame().setComponent(navigationPanel);	//place the navigation panel in the application frame
						
						setNoCache(response);	//TODO testing; fix; update method

						final List<ControlEvent> controlEvents=getControlEvents(request);	//get all control events from the request
						final FormControlEvent formSubmitEvent=(FormControlEvent)controlEvents.get(0);	//get the form submit event TODO fix, combine with AJAX code

							//before actually changing the navigation path, check to see if we're in the middle of modal navigation (only do this after we find a navigation panel, as this request might be for a stylesheet or some other non-panel resource, which shouldn't be redirected)
						final ModalNavigation modalNavigation=guiseSession.peekModalNavigation();	//see if we are currently doing modal navigation TODO make public access routines
						if(modalNavigation!=null)	//if we are currently in the middle of modal navigation, make sure the correct panel was requested
						{
							final URI modalNavigationURI=modalNavigation.getNewNavigationURI();	//get the modal navigation URI
							if(!requestURI.getRawPath().equals(modalNavigationURI.getRawPath()))		//if this request was for a different path than our current modal navigation path (we wouldn't be here if the domain, application, etc. weren't equivalent)
							{
								throw new HTTPMovedTemporarilyException(modalNavigationURI);	//redirect to the modal navigation location				
							}
						}
/*TODO del if not needed
							//update the frame's referrer
						final String referrer=getReferer(request);	//see if the request has a referrer
						if(referrer!=null && navigationPanel.getReferrerURI()==null)	//if the request indicates a referrer, but the navigation frame has not yet been updated with a referrer
						{
							final URI plainReferrerURI=getPlainURI(URI.create(referrer));	//get a plain URI version of the referrer
							if(!plainReferrerURI.equals(getPlainURI(requestURI)))	//if we aren't being referred from ourselves
							{
								navigationPanel.setReferrerURI(plainReferrerURI);	//update the frame's referrer URI
							}
						}
*/
						guiseSession.setNavigationPath(navigationPath);	//make sure the Guise session has the correct navigation path
						final Principal oldPrincipal=guiseSession.getPrincipal();	//get the old principal
						if(formSubmitEvent.getParameterListMap().size()>0)	//only query the view if there were submitted values---especially important for radio buttons and checkboxes, which must assume a value of false if nothing is submitted for them, thereby updating the model
						{
							guiseContext.setState(GuiseContext.State.PROCESS_EVENT);	//update the context state for processing an event
							try
							{
								navigationPanel.processEvent(formSubmitEvent);		//tell the panel to process the event
							}
							catch(final ComponentExceptions componentExceptions)	//if there were any component errors while processing the event
							{
								navigationPanel.addErrors(componentExceptions);	//store the validation error(s) so that the panel can report them to the user
							}
						}
						guiseContext.setState(GuiseContext.State.UPDATE_VIEW);	//update the context state for updating the view; make the change now in case queued model changes want to navigate, and an error was thrown when updating the model) TODO see if this comment is relevant after new local-error-catching changes above
						if(!ObjectUtilities.equals(oldPrincipal, guiseSession.getPrincipal()))	//if the principal has changed after updating the model
						{
							throw new HTTPMovedTemporarilyException(guiseContext.getNavigationURI());	//redirect to the same page, which will generate a new request with no POST parameters, which would likely change the principal again)
						}
						final Navigation requestedNavigation=guiseSession.getRequestedNavigation();	//get the requested navigation
						if(requestedNavigation!=null)	//if navigation is requested
						{
							final URI requestedNavigationURI=requestedNavigation.getNewNavigationURI();
//TODO del Debug.trace("navigation requested to", requestedNavigationURI);
							guiseSession.clearRequestedNavigation();	//remove any navigation requests
							if(requestedNavigation instanceof ModalNavigation)	//if modal navigation was requested
							{
								beginModalNavigation(guiseApplication, guiseSession, (ModalNavigation<?>)requestedNavigation);	//begin the modal navigation
							}
							throw new HTTPMovedTemporarilyException(requestedNavigationURI);	//redirect to the new navigation location
						}
						guiseSession.getApplicationFrame().updateView(guiseContext);		//tell the application frame to update its view
					}
					else	//if we have no panel type for this address
					{
						super.doGet(request, response);	//let the default functionality take over					
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
/*TODO del when works
		if(Debug.isDebug() && Debug.getReportLevels().contains(Debug.ReportLevel.INFO))	//indicate the parameters if information tracing is turned on
		{
			Debug.info("Received AJAX parameters:");
			final ListMap<Object, Object> parameterListMap=guiseContext.getParameterListMap();	//get the request parameter map
			for(final Map.Entry<Object, List<Object>> parameterListMapEntry:parameterListMap.entrySet())	//for each entry in the map of parameter lists
			{
				Debug.info("Key:", parameterListMapEntry.getKey(), "Value:", ArrayUtilities.toString(parameterListMapEntry.getValue().toArray()));				
			}
		}
*/
//	TODO del			final String navigationPath=(String)guiseContext.getParameterListMap().getItem("navigationPath");	//TODO decode param value
		final NavigationPanel navigationPanel=guiseSession.getNavigationPanel(navigationPath);	//get the panel bound to the requested path
		if(navigationPanel!=null)	//if we found a panel class for this address
		{
			final ApplicationFrame<?> applicationFrame=guiseSession.getApplicationFrame();	//get the application frame
			final Set<Frame<?>> removedFrames=new HashSet<Frame<?>>();	//create a set of frames so that we can know which ones were removed TODO testing
			CollectionUtilities.addAll(removedFrames, guiseSession.getFrameIterator());	//get all the current frames; we'll determine which ones were removed, later TODO improve all this
			
			
			final List<ControlEvent> controlEvents=getControlEvents(request);	//get all control events from the request
			guiseContext.setOutputContentType(XML_CONTENT_TYPE);	//switch to the "text/xml" content type
			guiseContext.writeElementBegin(null, "response");	//<response>	//TODO use a constant, decide on a namespace
			for(final ControlEvent controlEvent:controlEvents)	//for each control event
			{
				final Set<Component<?>> requestedComponents=new HashSet<Component<?>>();	//create a set of component that were identified in the request
				if(controlEvent instanceof FormControlEvent)	//if this is a form submission
				{
					final FormControlEvent formSubmitEvent=(FormControlEvent)controlEvent;	//get the form submit event
					final ListMap<String, Object> parameterListMap=formSubmitEvent.getParameterListMap();	//get the request parameter map
					for(final Map.Entry<String, List<Object>> parameterListMapEntry:parameterListMap.entrySet())	//for each entry in the map of parameter lists
					{
						final String parameterName=parameterListMapEntry.getKey();	//get the parameter name

						if(parameterName.equals(XHTMLApplicationFrameView.getActionInputID(applicationFrame)) && parameterListMapEntry.getValue().size()>0)	//if this parameter is for an action
						{
							final Component<?> actionComponent=getComponentByID(guiseSession, parameterListMapEntry.getValue().get(0).toString());	//get an action component
							if(actionComponent!=null)	//if we found an action component
							{
								requestedComponents.add(actionComponent);	//add it to the list of requested components
							}
						}
						else	//if this parameter is not a special action parameter
						{
							//TODO don't re-update nested components (less important for controls, which don't have nested components) 
			//TODO del Debug.trace("looking for component with name", parameterName);
							getControlsByName(guiseContext, navigationPanel, parameterName, requestedComponents);	//get all components identified by this name
						}
					}
				}
				else if(controlEvent instanceof ComponentControlEvent)	//if this event is bound for a specific component
				{
Debug.trace("this is a control event; looking for component with ID", ((ComponentControlEvent)controlEvent).getComponentID());
					final Component<?> component=getComponentByID(guiseSession, ((ComponentControlEvent)controlEvent).getComponentID());	//get the target component from its ID
					if(component!=null)	//if there is a target component
					{
Debug.trace("got component", component);
						requestedComponents.add(component);	//add the component to the set of requested components
					}					
				}
				if(!requestedComponents.isEmpty())	//if components were requested
				{
					guiseContext.setState(GuiseContext.State.PROCESS_EVENT);	//update the context state for processing an event
					for(final Component<?> component:requestedComponents)	//for each requested component
					{
						try
						{
Debug.trace("ready to process event", controlEvent, "for component", component);
							component.processEvent(controlEvent);		//tell the component to process the event
						}
						catch(final ComponentExceptions componentExceptions)	//if there were any component errors while processing the event
						{
/*TODO fix dirtying on errors
							for(final ComponentException componentException:componentExceptions)	//for each validation exception
							{
								final Component<?> affectedComponent=componentException.getComponent();	//see if this error is for a component
								if(affectedComponent!=null)	//if this component exception was for a specific component
								{
									affectedComponents.add(affectedComponent);	//add this component to our list of affected components
								}
							}
*/
						}
					}
					guiseContext.setState(GuiseContext.State.INACTIVE);	//deactivate the context so that any model update events will be generated
				}					
/*TODO del
Debug.trace("we now have affected components:", affectedComponents.size());
for(final Component<?> affectedComponent:affectedComponents)
{
	Debug.trace("affected component:", affectedComponent);
}
*/
				final Navigation requestedNavigation=guiseSession.getRequestedNavigation();	//get the requested navigation
				if(requestedNavigation!=null)	//if navigation is requested
				{
					final URI requestedNavigationURI=requestedNavigation.getNewNavigationURI();
//TODO del Debug.trace("navigation requested to", requestedNavigationURI);
					guiseSession.clearRequestedNavigation();	//remove any navigation requests
					if(requestedNavigation instanceof ModalNavigation)	//if modal navigation was requested
					{
						beginModalNavigation(guiseApplication, guiseSession, (ModalNavigation<?>)requestedNavigation);	//begin the modal navigation
					}
					//TODO ifAJAX()
					guiseContext.writeElementBegin(null, "navigate");	//<navigate>	//TODO use a constant
					guiseContext.write(requestedNavigationURI.toString());	//write the navigation URI
					guiseContext.writeElementEnd(null, "navigate");	//</navigate>
//TODO if !AJAX						throw new HTTPMovedTemporarilyException(requestedNavigationURI);	//redirect to the new navigation location
					//TODO store a flag or something---if we're navigating, we probably should flush the other queued events
				}

				
				final Collection<Component<?>> dirtyComponents=getDirtyComponents(guiseSession);	//get all dirty components in all the session frames 

				CollectionUtilities.removeAll(removedFrames, guiseSession.getFrameIterator());	//remove all the ending frames, leaving us the frames that were removed TODO improve all this
//TODO fix					dirtyComponents.addAll(frames);	//add all the frames that were removed
/*TODO del
					if(controlEvent instanceof InitControlEvent)	//if this is an initialization event
					{
Debug.trace("found init event; currently have frame count:", frames.size());
						CollectionUtilities.addAll(frames, guiseSession.getFrameIterator());	//get all the current frames so that the page can have the most up-to-date frame information
Debug.trace("now have frames: ", frames.size());
					}
*/
				
				Debug.trace("we now have dirty components:", dirtyComponents.size());
				for(final Component<?> affectedComponent:dirtyComponents)
				{
					Debug.trace("affected component:", affectedComponent);
				}
				
				
				
				if(dirtyComponents.contains(applicationFrame))	//if the application frame itself was affected, we might as well reload the page
				{
					guiseContext.writeElementBegin(null, "reload", true);	//<reload>	//TODO use a constant
					guiseContext.writeElementEnd(null, "reload");	//</reload>						
				}
				else	//if the application frame wasn't affected
				{
					guiseContext.setState(GuiseContext.State.UPDATE_VIEW);	//update the context state for updating the view
					for(final Component<?> dirtyComponent:dirtyComponents)	//for each component affected by this update cycle
					{
//TODO fix							if(dirtyComponent.isVisible())	//if the component is visible
						guiseContext.writeElementBegin(XHTML_NAMESPACE_URI, "patch");	//<xhtml:patch>	//TODO use a constant TODO don't use the XHTML namespace if we can help it
//TODO fix							else	//if the component is not visible, remove the component's elements
						guiseContext.writeAttribute(null, ATTRIBUTE_XMLNS, XHTML_NAMESPACE_URI.toString());	//xmlns="http://www.w3.org/1999/xhtml"
						dirtyComponent.updateView(guiseContext);		//tell the component to update its view
						guiseContext.writeElementEnd(XHTML_NAMESPACE_URI, "patch");	//</xhtml:patch>
					}
					for(final Frame<?> frame:removedFrames)	//for each removed frame
					{
						guiseContext.writeElementBegin(XHTML_NAMESPACE_URI, "remove");	//<xhtml:remove>	//TODO use a constant TODO don't use the XHTML namespace if we can help it								
						guiseContext.writeAttribute(null, "id", frame.getID());	//TODO fix
						guiseContext.writeElementEnd(XHTML_NAMESPACE_URI, "remove");	//</xhtml:remove>							
					}
					if(controlEvent instanceof InitControlEvent)	//if this is an initialization event TODO maybe just dirty all the frames so this happens automatically
					{
						final Iterator<Frame<?>> frameIterator=guiseSession.getFrameIterator();	//get an iterator to all the frames
						while(frameIterator.hasNext())	//while there are more frames
						{
							final Frame<?> frame=frameIterator.next();	//get the next frame
							if(frame!=guiseSession.getApplicationFrame())	//don't send back the application frame
							{
								guiseContext.writeElementBegin(XHTML_NAMESPACE_URI, "patch");	//<xhtml:patch>	//TODO use a constant TODO don't use the XHTML namespace if we can help it
	//							TODO fix							else	//if the component is not visible, remove the component's elements
								guiseContext.writeAttribute(null, ATTRIBUTE_XMLNS, XHTML_NAMESPACE_URI.toString());	//xmlns="http://www.w3.org/1999/xhtml"
								frame.updateView(guiseContext);		//tell the component to update its view
								guiseContext.writeElementEnd(XHTML_NAMESPACE_URI, "patch");	//</xhtml:patch>
							}
						}
					}
				}
			}
			guiseContext.setState(GuiseContext.State.INACTIVE);	//deactivate the context so that any model update events will be generated			
			guiseContext.writeElementEnd(null, "response");	//</response>
		}
	}
	
	/*TODO comment
	@param context Guise context information.
	*/
	protected <T extends Component<?>> void getControlsByName(final GuiseContext context, final T component, final String name, final Set<Component<?>> componentSet)
	{
			//TODO check first that the component is a control; that should be much faster
		final Controller<? extends GuiseContext, ?> controller=component.getController();
		if(controller instanceof AbstractXHTMLControlController)
		{
			final AbstractXHTMLControlController xhtmlControlController=(AbstractXHTMLControlController)controller;
//TODO del Debug.trace("checking control with ID", xhtmlControlController.getAbsoluteUniqueID(component), "and name", xhtmlControlController.getComponentName((Control)component));
			if(name.equals(xhtmlControlController.getComponentName((Control)component)))	//TODO comment: the returned name can be null
			{
//TODO del Debug.trace("using this component");
				componentSet.add(component);
			}
		}
		else if(controller instanceof XHTMLTabbedPanelController)	//TODO fix hack; make XHTMLTabbedPanelController descend from XHTMLControlController
		{
			final XHTMLTabbedPanelController tabbedPanelController=(XHTMLTabbedPanelController)controller;
			if(name.equals(component.getID()))	//TODO fix hack
			{
//TODO del Debug.trace("using this component");
				componentSet.add(component);
			}
			
		}
		if(component instanceof CompositeComponent)
		{
			for(final Component<?> childComponent:(CompositeComponent<?>)component)
			{
				getControlsByName(context, childComponent, name, componentSet);
			}
		}
	}

	/**Retrieves all components that have views needing updated within a session.
	This method checks all frames in the session.
	If a given component is dirty, its child views will not be checked.
	@param session The Guise session to check for dirty views.
	@return The components with views needing to be updated. 
	*/
	public static Collection<Component<?>> getDirtyComponents(final GuiseSession session)
	{
		final ArrayList<Component<?>> dirtyComponents=new ArrayList<Component<?>>();	//create a new list to hold dirty components
		final Iterator<Frame<?>> frameIterator=session.getFrameIterator();	//get an iterator to session frames
		while(frameIterator.hasNext())	//while there are more frames
		{
			final Frame<?> frame=frameIterator.next();	//get the next frame
			AbstractComponent.getDirtyComponents(frame, dirtyComponents);	//gather more dirty components
		}
		return dirtyComponents;	//return the dirty components we collected
	}

	/**Retrieves a component with the given ID.
	This method searches the hierarchies of all frames in the session.
	@param session The Guise session to check for the component.
	@return The component with the given ID, or <code>null</code> if no component with the given ID could be found on any of the given frames. 
	*/
	public static Component<?> getComponentByID(final GuiseSession session, final String id)
	{
		final Iterator<Frame<?>> frameIterator=session.getFrameIterator();	//get an iterator to session frames
		while(frameIterator.hasNext())	//while there are more frames
		{
			final Frame<?> frame=frameIterator.next();	//get the next frame
			final Component<?> component=AbstractComponent.getComponentByID(frame, id);	//try to find the component within this frame
			if(component!=null)	//if a component with the given ID was found
			{
				return component;	//return the component
			}
		}
		return null;	//indicate that no such component could be found
	}

	private final PathExpression AJAX_REQUEST_EVENTS_WILDCARD_XPATH_EXPRESSION=new PathExpression("request", "events", "*");	//TODO use constants; comment 
	private final PathExpression AJAX_REQUEST_CONTROL_XPATH_EXPRESSION=new PathExpression("control");	//TODO use constants; comment 
//TODO del	private final PathExpression AJAX_REQUEST_CONTROL_NAME_XPATH_EXPRESSION=new PathExpression("control", "name");	//TODO use constants; comment 
//TODO del	private final PathExpression AJAX_REQUEST_CONTROL_VALUE_XPATH_EXPRESSION=new PathExpression("control", "value");	//TODO use constants; comment 
	private final PathExpression AJAX_REQUEST_SOURCE_XPATH_EXPRESSION=new PathExpression("source");	//TODO use constants; comment 
	private final PathExpression AJAX_REQUEST_TARGET_XPATH_EXPRESSION=new PathExpression("target");	//TODO use constants; comment 
	
	/**Retrieves control events from the HTTP request.
  @param request The HTTP request.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	protected List<ControlEvent> getControlEvents(final HttpServletRequest request) throws ServletException, IOException
	{
		final List<ControlEvent> controlEventList=new ArrayList<ControlEvent>();	//create a new list for storing control events
		final String contentTypeString=request.getContentType();	//get the request content type
		final ContentType contentType=contentTypeString!=null ? createContentType(contentTypeString) : null;	//create a content type object from the request content type, if there is one
		if(contentType!=null && GUISE_AJAX_REQUEST_CONTENT_TYPE.match(contentType))	//if this is a Guise AJAX request
		{
			try
			{
				final DocumentBuilderFactory documentBuilderFactory=DocumentBuilderFactory.newInstance();	//create a document builder factory TODO create a shared document builder factory, maybe---but make sure it is used by only one thread			
				final DocumentBuilder documentBuilder=documentBuilderFactory.newDocumentBuilder();	//create a new document builder
				final Document document=documentBuilder.parse(request.getInputStream());	//read the document from the request
				final List<Node> eventNodes=(List<Node>)XPath.evaluatePathExpression(document, AJAX_REQUEST_EVENTS_WILDCARD_XPATH_EXPRESSION);	//get all the events
				for(final Node eventNode:eventNodes)	//for each event node
				{
					if(eventNode.getNodeType()==Node.ELEMENT_NODE)//if this is an event element
					{
						final Element eventElement=(Element)eventNode;	//cast the node to an element
						if("form".equals(eventNode.getNodeName()))	//if this is a form event TODO use a constant
						{
							final FormControlEvent formSubmitEvent=new FormControlEvent(false);	//create a new form submission event TODO get the exhaustive indication from the element
							final ListMap<String, Object> parameterListMap=formSubmitEvent.getParameterListMap();	//get the map of parameter lists
							final List<Node> controlNodes=(List<Node>)XPath.evaluatePathExpression(eventNode, AJAX_REQUEST_CONTROL_XPATH_EXPRESSION);	//get all the control settings
							for(final Node controlNode:controlNodes)	//for each control node
							{
								final Element controlElement=(Element)controlNode;	//get this control element
								final String controlName=controlElement.getAttribute("name");	//get the control name TODO use a constant
								if(controlName!=null && controlName.length()>0)	//if this control has a name
								{
									final String controlValue=controlElement.getTextContent();	//get the control value
									parameterListMap.addItem(controlName, controlValue);	//store the value in the parameters
								}
							}
							controlEventList.add(formSubmitEvent);	//add the event to the list
						}
						else if("action".equals(eventNode.getNodeName()))	//if this is an action event TODO use a constant
						{
							final String componentID=eventElement.getAttribute("componentID");	//get the ID of the component TODO use a constant
							if(componentID!=null)	//if there is a component ID TODO add better event handling, to throw an error and send back that error
							{
								final String targetID=eventElement.getAttribute("targetID");	//get the ID of the target element TODO use a constant
								final String actionID=eventElement.getAttribute("actionID");	//get the action identifier TODO use a constant								
								final ActionControlEvent actionControlEvent=new ActionControlEvent(componentID, targetID, actionID);	//create a new action control event
								controlEventList.add(actionControlEvent);	//add the event to the list
							}
						}
						else if("drop".equals(eventNode.getNodeName()))	//if this is a drop event TODO use a constant
						{
							final Node sourceNode=XPath.getNode(eventNode, AJAX_REQUEST_SOURCE_XPATH_EXPRESSION);	//get the source node
							final String dragSourceID=((Element)sourceNode).getAttribute("id");	//TODO tidy; improve; comment
							final Node targetNode=XPath.getNode(eventNode, AJAX_REQUEST_TARGET_XPATH_EXPRESSION);	//get the target node
							final String dropTargetID=((Element)targetNode).getAttribute("id");	//TODO tidy; improve; comment
							final DropControlEvent dropEvent=new DropControlEvent(dragSourceID, dropTargetID);	//create a new drop event
							controlEventList.add(dropEvent);	//add the event to the list
						}
						else if("init".equals(eventNode.getNodeName()))	//if this is an initialization event TODO use a constant
						{
							final InitControlEvent initEvent=new InitControlEvent();	//create a new initialization event
							controlEventList.add(initEvent);	//add the event to the list
						}
					}
				}
			}
			catch(final ParserConfigurationException parserConfigurationException)	//we don't expect parser configuration errors
			{
				throw new AssertionError(parserConfigurationException);
			}
			catch(final SAXException saxException)	//we don't expect parsing errors
			{
				throw new AssertionError(saxException);	//TODO maybe change to throwing an IOException
			}
			catch(final IOException ioException)	//if there is an I/O exception
			{
				throw new AssertionError(ioException);	//TODO fix better
			}
		}
		else	//if this is not a Guise AJAX request
		{
				//populate our parameter map
			if(FileUpload.isMultipartContent(request))	//if this is multipart/form-data content
			{
				final FormControlEvent formSubmitEvent=new FormControlEvent(true);	//create a new form submission event, indicating that the event is exhaustive
				final ListMap<String, Object> parameterListMap=formSubmitEvent.getParameterListMap();	//get the map of parameter lists
				final DiskFileUpload diskFileUpload=new DiskFileUpload();	//create a file upload handler
				diskFileUpload.setSizeMax(-1);	//don't reject anything
				try	//try to parse the file items submitted in the request
				{
					final List fileItems=diskFileUpload.parseRequest(request);	//parse the request
					for(final Object object:fileItems)	//look at each file item
					{
						final FileItem fileItem=(FileItem)object;	//cast the object to a file item
						final String parameterKey=fileItem.getFieldName();	//the parameter key will always be the field name
						final Object parameterValue=fileItem.isFormField() ? fileItem.getString() : new FileItemResourceImport(fileItem);	//if this is a form field, store it normally; otherwise, create a file item resource import object
						parameterListMap.addItem(parameterKey, parameterValue);	//store the value in the parameters
					}
				}
				catch(final FileUploadException fileUploadException)	//if there was an error parsing the files
				{
					throw new IllegalArgumentException("Couldn't parse multipart/form-data request.");
				}
				controlEventList.add(formSubmitEvent);	//add the event to the list
			}
			else	//if this is normal application/x-www-form-urlencoded data
			{
				final boolean exhaustive=POST_METHOD.equals(request.getMethod());	//if this is an HTTP post, the form event is exhaustive of all controls on the form
				final FormControlEvent formSubmitEvent=new FormControlEvent(exhaustive);	//create a new form submission event
				final ListMap<String, Object> parameterListMap=formSubmitEvent.getParameterListMap();	//get the map of parameter lists
				final Iterator parameterEntryIterator=request.getParameterMap().entrySet().iterator();	//get an iterator to the parameter entries
				while(parameterEntryIterator.hasNext())	//while there are more parameter entries
				{
					final Map.Entry parameterEntry=(Map.Entry)parameterEntryIterator.next();	//get the next parameter entry
					final String parameterKey=(String)parameterEntry.getKey();	//get the parameter key
					final String[] parameterValues=(String[])parameterEntry.getValue();	//get the parameter values
					final List<Object> parameterValueList=new ArrayList<Object>(parameterValues.length);	//create a list to hold the parameters
					CollectionUtilities.addAll(parameterValueList, parameterValues);	//add all the parameter values to our list
					parameterListMap.put(parameterKey, parameterValueList);	//store the the array of values as a list, keyed to the value
				}
				controlEventList.add(formSubmitEvent);	//add the event to the list
			}
		}
		if(controlEventList.size()>0 && Debug.isDebug() && Debug.getReportLevels().contains(Debug.ReportLevel.INFO))	//indicate the parameters if information tracing is turned on
		{
			Debug.info("Received Control Events:");
			for(final ControlEvent controlEvent:controlEventList)	//for each control event
			{
				Debug.info("  Event:", controlEvent.getClass(), controlEvent);				
			}
		}

/*TODO del
Debug.trace("parameter names:", request.getParameterNames());	//TODO del when finished with dual mulipart+encoded content
Debug.trace("number of parameter names:", request.getParameterNames());
Debug.trace("***********number of distinct parameter keys", parameterListMap.size());
*/
		return controlEventList;	//return the list of control events
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
	@param <P> The type of navigation panel beginning navigation.
	@param guiseApplication The Guise application.
	@param guiseSession The Guise session.
	@param modalNavigation The modal navigation information
	*/
	protected <P extends ModalNavigationPanel<?, ?>> void beginModalNavigation(final GuiseApplication guiseApplication, final GuiseSession guiseSession, final ModalNavigation<P> modalNavigation)
	{
		final P modalPanel=(P)guiseSession.getNavigationPanel(guiseApplication.relativizeURI(modalNavigation.getNewNavigationURI()));	//get the modal frame for this navigation path
		if(modalPanel!=null)	//if we have a modal frame
		{
			guiseSession.beginModalNavigation(modalPanel, modalNavigation);
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
