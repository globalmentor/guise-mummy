package com.guiseframework.platform.web;

import java.io.*;
import java.net.*;
import java.security.Principal;
import java.util.*;

import javax.mail.internet.ContentType;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.*;

import org.apache.commons.fileupload.*;
import org.w3c.dom.*;
import org.w3c.dom.css.CSSStyleSheet;
import org.xml.sax.SAXException;

import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;

import com.garretwilson.io.*;
import com.garretwilson.lang.ClassUtilities;
import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.model.DefaultResource;
import com.garretwilson.model.Resource;

import static com.garretwilson.io.ContentTypeUtilities.*;
import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.lang.StringBuilderUtilities.*;
import static com.garretwilson.lang.ThreadUtilities.*;

import com.garretwilson.net.URIConstants;
import com.garretwilson.net.URIUtilities;
import com.garretwilson.net.http.*;

import static com.garretwilson.net.http.HTTPConstants.*;

import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import static com.garretwilson.text.CharacterConstants.*;
import static com.garretwilson.text.xml.XMLConstants.*;
import com.garretwilson.text.xml.XMLUtilities;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

import com.garretwilson.rdf.RDFResourceIO;
import com.garretwilson.rdf.RDFUtilities;
import com.garretwilson.rdf.maqro.Activity;
import com.garretwilson.rdf.maqro.MAQROUtilities;
import com.garretwilson.security.Nonce;
import com.garretwilson.servlet.http.HttpServletUtilities;

import static com.garretwilson.text.xml.stylesheets.css.XMLCSSConstants.*;

import com.garretwilson.text.xml.xhtml.XHTMLConstants;
import com.garretwilson.text.xml.xpath.*;
import com.garretwilson.util.*;
import com.guiseframework.*;
import com.guiseframework.Bookmark.Parameter;
import com.guiseframework.component.*;
import com.guiseframework.context.GuiseContext;
import com.guiseframework.controller.*;
import com.guiseframework.controller.text.xml.xhtml.*;
import com.guiseframework.geometry.*;
import com.guiseframework.model.FileItemResourceImport;
import com.guiseframework.platform.web.css.*;
import com.guiseframework.theme.Theme;
import com.guiseframework.view.text.xml.xhtml.XHTMLApplicationFrameView;

import static com.garretwilson.text.CharacterEncodingConstants.*;
import static com.garretwilson.util.LocaleUtilities.*;
import static com.guiseframework.Guise.*;
import static com.guiseframework.platform.web.WebPlatformConstants.*;

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
	/**The parameter, "style", used to identify the navigation style in the web application's init parameters.*/
	public final static String NAVIGATION_STYLE_PARAMETER="style";
	/**The init parameter, "style", used to specify the style definition URIs.*/
	public final static String STYLE_INIT_PARAMETER="style";
	/**The init parameter, "theme", used to specify the theme URIs*/
	public final static String THEME_INIT_PARAMETER="theme";

	/**The context parameter of the data base directory.*/
	public final static String DATA_BASE_DIRECTORY_CONTEXT_PARAMETER="dataBaseDirectory";

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

		/**Returns the Guise container.
		This method must not be called before a request is processed.
		@return The Guise container that owns the applications.
		@exception IllegalStateException if this method is called before any requests have been processed.
		*/
		protected HTTPServletGuiseContainer getGuiseContainer()
		{
			if(guiseContainer==null)	//if no container exists
			{
				throw new IllegalStateException("Cannot access Guise container before first HTTP request, due to the Java Servlet architecture.");
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
		final String styleString=servletConfig.getInitParameter(STYLE_INIT_PARAMETER);	//get the style init parameter, if any
		if(styleString!=null)	//if a style is specified
		{
			guiseApplication.setStyle(URI.create(styleString));	//create a locale from the default locale string and store it in the application (trimming whitespace just to be extra helpful)
		}

			//initialize the theme
		final String themeString=servletConfig.getInitParameter(THEME_INIT_PARAMETER);	//get the theme init parameter, if any
		if(themeString!=null)	//if a theme is specified
		{
			guiseApplication.setTheme(new Theme(URI.create(themeString)));
/*TODO del when works
			try
			{
Debug.trace("creating theme URI from string:", themeString);
				final URI themeURI=guiseApplication.resolveURI(URI.create(themeString));	//resolve the theme URI against the Guise application
	Debug.trace("created theme URI:", themeURI);
	Debug.trace("creating theme IO");
				final IO<Theme> themeIO=new RDFResourceIO<Theme>(Theme.class, GUISE_NAMESPACE_URI);	//create I/O for loading the theme
	Debug.trace("creating theme input stream");
				final InputStream themeInputStream=new BufferedInputStream(new HTTPResource(themeURI).getInputStream());	//get a buffered input stream to the theme TODO maybe use some general loader
				try
				{
//TODO fix					guiseApplication.setStyle(URI.create(styleString));	//create a locale from the default locale string and store it in the application (trimming whitespace just to be extra helpful)
Debug.trace("loading theme");
					final Theme theme=themeIO.read(themeInputStream, themeURI);
Debug.trace(RDFUtilities.toString(theme));
				}
				finally
				{
					themeInputStream.close();	//always close the theme input stream
				}
				
			}
			catch(final IOException ioException)	//if there is an I/O error
			{
				throw new ServletException(ioException);
			}
*/
		}

			//initialize destinations
		final Enumeration initParameterNames=servletConfig.getInitParameterNames();	//get the names of all init parameters
		while(initParameterNames.hasMoreElements())	//while there are more initialization parameters
		{
			final String initParameterName=(String)initParameterNames.nextElement();	//get the next initialization parameter name
			if(initParameterName.startsWith(NAVIGATION_INIT_PARAMETER_PREFIX))	//if this is a path/panel binding
			{
				final String path;
				final Class<? extends NavigationPanel> navigationPanelClass;
				final URI styleURI;
				final String initParameterValue=servletConfig.getInitParameter(initParameterName);	//get this init parameter value
				try
				{
					final URI pathPanelBindingURI=new URI(initParameterValue);	//create a URI from the panel binding expression
					path=pathPanelBindingURI.getRawPath();	//extract the path from the URI
					if(path!=null)	//if a path was specified
					{
						final ListMap<String, String> parameterListMap=getParameterMap(pathPanelBindingURI);	//get the URI parameters
						final String className=parameterListMap.getItem(NAVIGATION_CLASS_PARAMETER);	//get the class parameter
						if(className!=null)	//if a class name was specified
						{
							try
							{
								final Class<?> specifiedClass=Class.forName(className);	//load the class for the specified name
								navigationPanelClass=specifiedClass.asSubclass(NavigationPanel.class);	//cast the specified class to a navigation panel class just to make sure it's the correct type
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
						final String style=parameterListMap.getItem(NAVIGATION_STYLE_PARAMETER);	//get the styleparameter
						if(style!=null)	//if a style was specified
						{
							try
							{
								styleURI=new URI(style);	//convert the style to a URI
							}
							catch(final URISyntaxException uriSyntaxException)	//if the style URI was not in the correct format
							{
								throw new IllegalArgumentException("Invalid style URI "+style+" for "+initParameterName, uriSyntaxException);
							}
						}
						else	//if no style was specified
						{
							styleURI=null;	//show that there is no style
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
				final Destination destination=new DefaultDestination(path, navigationPanelClass, styleURI);	//create a new destination
				guiseApplication.setDestination(path, destination);	//set the destination for this path				
			}
		}
		return guiseApplication;	//return the created Guise application
	}

	/**Initializes the servlet upon receipt of the first request.
	This version initializes the reference to the Guise container.
	This version installs the application into the container.
	@param request The servlet request.
	@exception IllegalStateException if this servlet has already been initialized from a request.
	@exception ServletException if there is a problem initializing.
	*/
	public void init(final HttpServletRequest request) throws ServletException
	{
		super.init(request);	//do the default initialization
		if(guiseContainer==null)	//if no container exists
		{
			Debug.trace("context path", getContextPath());
			final URI requestURI=URI.create(request.getRequestURL().toString());	//get the URI of the current request
Debug.trace("requestURI", requestURI);
			final URI containerBaseURI=changePath(requestURI, getContextPath()+PATH_SEPARATOR);	//determine the container base URI
Debug.trace("containerURI", containerBaseURI);

			guiseContainer=HTTPServletGuiseContainer.getGuiseContainer(getServletContext(), containerBaseURI);	//get a reference to the Guise container, creating it if needed
		}
		Debug.trace("initializing; container base URI:", guiseContainer.getBaseURI(), "container base path:", guiseContainer.getBasePath());
		

		
		final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container (which we just created if needed)
		final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
		if(guiseApplication.getContainer()==null)	//if this application has not yet been installed (note that there is a race condition here if multiple HTTP requests attempt to access the application simultaneously, but the losing thread will simply throw an exception and not otherwise disturb the application functionality)
		{
			final String guiseApplicationContextPath=request.getContextPath()+request.getServletPath()+PATH_SEPARATOR;	//construct the Guise application context path from the servlet request, which is the concatenation of the web application path and the servlet's path with an ending slash
Debug.trace("applicationContextPath", guiseApplicationContextPath);
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
		assert isAbsolutePath(rawPathInfo) : "Expected absolute path info, received "+rawPathInfo;	//the Java servlet specification says that the path info will start with a '/'
		final String navigationPath=rawPathInfo.substring(1);	//remove the beginning slash to get the navigation path from the path info
/*TODO del
final Enumeration headerNames=request.getHeaderNames();	//TODO del
while(headerNames.hasMoreElements())
{
	final String headerName=(String)headerNames.nextElement();
	Debug.info("request header:", headerName, request.getHeader(headerName));
}
*/
		if(rawPathInfo.startsWith(GUISE_PUBLIC_RESOURCE_BASE_PATH))	//if this is a request for a public resource
		{
			super.doGet(request, response);	//go ahead and retrieve the resource immediately
			return;	//don't try to see if there is a navigation path for this path
		}
		final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container
		final GuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
/*TODO del
		
				//TODO double-test AJAX
Debug.trace("does navigation path exist?", navigationPath);
  	if(!guiseApplication.hasDestination(navigationPath))	//if the URI doesn't represent a valid navigation path
  	{
Debug.trace("navigation path doesn't exist; doing normal get", navigationPath);
			super.doGet(request, response);	//go ahead and retrieve the resource immediately
			return;	//don't try to see if there is a navigation path for this path
		}
*/
		
		final GuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieves the Guise session for this container and request
/*TODO del
Debug.info("session ID", guiseSession.getHTTPSession().getId());	//TODO del
Debug.info("content length:", request.getContentLength());
Debug.info("content type:", request.getContentType());
*/
//TODO del Debug.info("supports Flash: ", guiseSession.getEnvironment().getProperty(GuiseEnvironment.CONTENT_APPLICATION_SHOCKWAVE_FLASH_ACCEPTED_PROPERTY));
//TODO del Debug.trace("creating thread group");
		final GuiseSessionThreadGroup guiseSessionThreadGroup=Guise.getInstance().getThreadGroup(guiseSession);	//get the thread group for this session
//	TODO del Debug.trace("creating runnable");
		final GuiseSessionRunnable guiseSessionRunnable=new GuiseSessionRunnable(request, response, guiseContainer, guiseApplication, guiseSession);	//create a runnable instance to service the Guise request
//TODO del Debug.trace("calling runnable");
		call(guiseSessionThreadGroup, guiseSessionRunnable);	//call the runnable in a new thread inside the thread group
//TODO del Debug.trace("done with the call");
		if(guiseSessionRunnable.servletException!=null)
		{
//TODO del			Debug.trace("callling runnable");
			throw guiseSessionRunnable.servletException;
		}
		if(guiseSessionRunnable.ioException!=null)
		{
			throw guiseSessionRunnable.ioException;
		}
		if(!guiseSessionRunnable.isGuiseRequest)
		{
			super.doGet(request, response);	//let the default functionality take over
		}
	}

	/**The runnable class that services an HTTP request.
	@author Garret Wilson
	*/
	protected class GuiseSessionRunnable implements Runnable
	{
		private final HttpServletRequest request;
		private final HttpServletResponse response;
		private final HTTPServletGuiseContainer guiseContainer;
		private final GuiseApplication guiseApplication;
		private final GuiseSession guiseSession;

		public ServletException servletException=null;
		public IOException ioException=null;
		public boolean isGuiseRequest=false;
		
		/**Constructor.
	  @param request The HTTP request.
	  @param response The HTTP response.
	  @param guiseContainer The Guise container.
	  @param guiseApplication The Guise application.
	  @param guiseSession The Guise session.
		*/
		public GuiseSessionRunnable(final HttpServletRequest request, final HttpServletResponse response, final HTTPServletGuiseContainer guiseContainer, final GuiseApplication guiseApplication, final GuiseSession guiseSession)
		{
			this.request=request;
			this.response=response;
			this.guiseContainer=guiseContainer;
			this.guiseApplication=guiseApplication;
			this.guiseSession=guiseSession;
		}

		/**Actually services the Guise request.
		@see GuiseHTTPServlet#serviceGuiseRequest(HttpServletRequest, HttpServletResponse, HTTPServletGuiseContainer, GuiseApplication, GuiseSession)
		*/
		public void run()
		{
			try
			{
/*TODO del				
				
Debug.trace("ready to service Guise request with session", guiseSession);
Debug.trace("Guise thinks we're in session", Guise.getInstance().getGuiseSession());
Debug.trace("are the sessions equal?", guiseSession.equals(Guise.getInstance().getGuiseSession()));

*/
				isGuiseRequest=serviceGuiseRequest(request, response, guiseContainer, guiseApplication, guiseSession);
			}
			catch(final ServletException servletException)
			{
				this.servletException=servletException;
			}
			catch(final IOException ioException)
			{
				this.ioException=ioException;
			}
		}
	}
	
	/**Services a Guise request.
  If this is a request for a Guise navigation path, a Guise context will be assigned to the Guise session while the request is processed.
  @param request The HTTP request.
  @param response The HTTP response.
  @param guiseContainer The Guise container.
  @param guiseApplication The Guise application.
  @param guiseSession The Guise session.
  @return <code>true</code> if this request was for Guise components, else <code>false</code> if no navigation panel was found at the given location.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	private boolean serviceGuiseRequest(final HttpServletRequest request, final HttpServletResponse response, final HTTPServletGuiseContainer guiseContainer, final GuiseApplication guiseApplication, final GuiseSession guiseSession) throws ServletException, IOException
	{
//TODO del Debug.trace("servicing Guise request");
		final URI requestURI=URI.create(request.getRequestURL().toString());	//get the URI of the current request		
		final String contentTypeString=request.getContentType();	//get the request content type
		final ContentType contentType=contentTypeString!=null ? createContentType(contentTypeString) : null;	//create a content type object from the request content type, if there is one
		final boolean isAJAX=contentType!=null && GUISE_AJAX_REQUEST_CONTENT_TYPE.match(contentType);	//see if this is a Guise AJAX request
			//TODO verify; does this work with file uploads?
			//this is a non-AJAX Guise POST if there is an XHTML action input ID field TODO add a better field; stop using a view
		final boolean isGuisePOST=POST_METHOD.equals(request.getMethod()) && request.getParameter(XHTMLApplicationFrameView.getActionInputID(guiseSession.getApplicationFrame()))!=null;

		final String rawPathInfo=getRawPathInfo(request);	//get the raw path info
		assert isAbsolutePath(rawPathInfo) : "Expected absolute path info, received "+rawPathInfo;	//the Java servlet specification says that the path info will start with a '/'
		final String navigationPath=rawPathInfo.substring(1);	//remove the beginning slash to get the navigation path from the path info
		final Destination destination=guiseApplication.getDestination(navigationPath);	//get the destination, if any, associated with the requested path
		if(destination!=null)	//if we have a destination associated with the requested path
		{
//		TODO del Debug.trace("have destination; creating context");
			final HTTPServletGuiseContext guiseContext=new HTTPServletGuiseContext(guiseSession, destination, request, response);	//create a new Guise context
//		TODO del Debug.trace("got context");
			synchronized(guiseSession)	//don't allow other session contexts to be active at the same time
			{

//TODO del Debug.trace("setting context");
				guiseSession.setContext(guiseContext);	//set the context for this session
				try
				{
					if(!isAJAX)	//if this is not an AJAX request, see if we need to enforce modal navigation (only do this after we find a navigation panel, as this request might be for a stylesheet or some other non-panel resource, which shouldn't be redirected)
					{
						final ModalNavigation modalNavigation=guiseSession.getModalNavigation();	//see if we are currently doing modal navigation
						if(modalNavigation!=null)	//if we are currently in the middle of modal navigation, make sure the correct panel was requested
						{
							final URI modalNavigationURI=modalNavigation.getNewNavigationURI();	//get the modal navigation URI
							if(!requestURI.getRawPath().equals(modalNavigationURI.getRawPath()))		//if this request was for a different path than our current modal navigation path (we wouldn't be here if the domain, application, etc. weren't equivalent)
							{
								throw new HTTPMovedTemporarilyException(modalNavigationURI);	//redirect to the modal navigation location				
							}
						}
					}
					final Bookmark navigationBookmark=getBookmark(request);	//get the bookmark from this request
		//TODO fix to recognize navigation, bookmark, and principal changes when the navigation panel is created		final Bookmark bookmark=getBookmark(request);	//get the bookmark from this request
					final Bookmark oldBookmark=isAJAX ? guiseSession.getBookmark() : navigationBookmark;	//get the original bookmark, which will be the one requested in navigation (which we'll soon set) if this is a normal HTTP GET/POST
					final Principal oldPrincipal=guiseSession.getPrincipal();	//get the old principal
					final NavigationPanel navigationPanel=guiseSession.getNavigationPanel(navigationPath);	//get the panel bound to the requested path
					assert navigationPanel!=null : "No navigation panel found, even though we found a valid destination.";
					final ApplicationFrame<?> applicationFrame=guiseSession.getApplicationFrame();	//get the application frame
//TODO del Debug.trace("ready to get control events");
					final List<ControlEvent> controlEvents=getControlEvents(request, guiseSession);	//get all control events from the request
Debug.trace("got control events");
					if(isAJAX)	//if this is an AJAX request
					{
/*TODO tidy when stringbuilder context works
						guiseContext.setOutputContentType(XML_CONTENT_TYPE);	//switch to the "text/xml" content type
						guiseContext.writeElementBegin(null, "response");	//<response>	//TODO use a constant, decide on a namespace
*/
					}
					else	//if this is not an AJAX request
					{
//TODO del Debug.trace("this is not AJAX, with method:", request.getMethod(), "content type", contentType, "guise POST?", isGuisePOST);
						applicationFrame.setContent(navigationPanel);	//place the navigation panel in the application frame
						setNoCache(response);	//TODO testing; fix; update method				
						final String referrer=getReferer(request);	//get the request referrer, if any
						final URI referrerURI=referrer!=null ? getPlainURI(URI.create(referrer)) : null;	//get a plain URI version of the referrer, if there is a referrer
							//see if there is non-Guise HTTP POST data, and if so, set that bookmark navigation temporarily
							//a non-Guise form HTTP POST, get the servlet parameters (which will include the URL query information)
						if(POST_METHOD.equals(request.getMethod()) && contentType!=null && APPLICATION_X_WWW_FORM_URLENCODED_CONTENT_TYPE.match(contentType) && !isGuisePOST)
						{
//TODO del Debug.trace("using servlet parameter methods");
							final List<Bookmark.Parameter> bookmarkParameterList=new ArrayList<Bookmark.Parameter>();	//create a new list of bookmark parameters
							final Iterator parameterEntryIterator=request.getParameterMap().entrySet().iterator();	//get an iterator to the parameter entries
							while(parameterEntryIterator.hasNext())	//while there are more parameter entries
							{
								final Map.Entry parameterEntry=(Map.Entry)parameterEntryIterator.next();	//get the next parameter entry
								final String parameterKey=(String)parameterEntry.getKey();	//get the parameter key
								final String[] parameterValues=(String[])parameterEntry.getValue();	//get the parameter values
								for(final String parameterValue:parameterValues)	//for each parameter value
								{
//TODO del Debug.trace("adding parameter bookmark:", parameterKey, parameterValue);
									bookmarkParameterList.add(new Bookmark.Parameter(parameterKey, parameterValue));	//create a corresponding bookmark parameter
								}
							}
							if(!bookmarkParameterList.isEmpty())	//if there are bookmark parameters
							{
								final Bookmark.Parameter[] bookmarkParameters=bookmarkParameterList.toArray(new Bookmark.Parameter[bookmarkParameterList.size()]);	//get an array of bookmark parameters
								final Bookmark postBookmark=new Bookmark(bookmarkParameters);	//create a new bookmark to represent the POST information
								guiseSession.setNavigation(navigationPath, postBookmark, referrerURI);	//set the session navigation to the POST bookmark information
							}
						}
						guiseSession.setNavigation(navigationPath, navigationBookmark, referrerURI);	//set the session navigation with the navigation bookmark, firing any navigation events if appropriate
					}
					final Set<Frame<?>> removedFrames=new HashSet<Frame<?>>();	//create a set of frames so that we can know which ones were removed TODO testing
					CollectionUtilities.addAll(removedFrames, guiseSession.getFrameIterator());	//get all the current frames; we'll determine which ones were removed, later TODO improve all this
					boolean isNavigating=false;	//we'll check this later to see if we're navigating so we won't have to update all the components
					for(final ControlEvent controlEvent:controlEvents)	//for each control event
					{
						final Set<Component<?>> requestedComponents=new HashSet<Component<?>>();	//create a set of component that were identified in the request
						try
						{
							if(controlEvent instanceof FormControlEvent)	//if this is a form submission
							{
								final FormControlEvent formControlEvent=(FormControlEvent)controlEvent;	//get the form control event
								if(formControlEvent.isExhaustive())	//if this is an exhaustive form submission (such as a POST submission)
								{
									if(formControlEvent.getParameterListMap().size()>0)	//only process the event if there were submitted values---especially important for radio buttons and checkboxes, which must assume a value of false if nothing is submitted for them, thereby updating the model
									{
										requestedComponents.add(navigationPanel);	//we'll give the event to the entire navigation panel
									}
								}
								else	//if this is only a partial form submission
								{
									final ListMap<String, Object> parameterListMap=formControlEvent.getParameterListMap();	//get the request parameter map
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
											getControlsByName(guiseSession, parameterName, requestedComponents);	//TODO comment; tidy
				//TODO del; test new method; tidy; comment							getControlsByName(guiseContext, navigationPanel, parameterName, requestedComponents);	//get all components identified by this name
										}
									}
								}
							}
							else if(controlEvent instanceof ComponentControlEvent)	//if this event is bound for a specific component
							{
			//TODO del Debug.trace("this is a control event; looking for component with ID", ((ComponentControlEvent)controlEvent).getComponentID());
								final Component<?> component=getComponentByID(guiseSession, ((ComponentControlEvent)controlEvent).getComponentID());	//get the target component from its ID
								if(component!=null)	//if there is a target component
								{
			//TODO del Debug.trace("got component", component);
									requestedComponents.add(component);	//add the component to the set of requested components
								}
							}
							if(!requestedComponents.isEmpty())	//if components were requested
							{
								guiseContext.setState(GuiseContext.State.PROCESS_EVENT);	//update the context state for processing an event
								for(final Component<?> component:requestedComponents)	//for each requested component
								{
			Debug.trace("ready to process event", controlEvent, "for component", component);
									component.processEvent(controlEvent);		//tell the component to process the event
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
							final Bookmark newBookmark=guiseSession.getBookmark();	//see if the bookmark has changed
Debug.trace("navigation bookmark:", navigationBookmark);
Debug.trace("new bookmark:", newBookmark);
							final Navigation requestedNavigation=guiseSession.getRequestedNavigation();	//get the requested navigation
							if(requestedNavigation!=null || !ObjectUtilities.equals(navigationBookmark, newBookmark))	//if navigation is requested or the bookmark has changed, redirect the browser
							{
								final String redirectURIString;	//we'll determine where to direct to
								if(requestedNavigation!=null)	//if navigation is requested
								{
									final URI requestedNavigationURI=requestedNavigation.getNewNavigationURI();
				//TODO del Debug.trace("navigation requested to", requestedNavigationURI);
									guiseSession.clearRequestedNavigation();	//remove any navigation requests
									if(requestedNavigation instanceof ModalNavigation)	//if modal navigation was requested
									{
										beginModalNavigation(guiseApplication, guiseSession, (ModalNavigation)requestedNavigation);	//begin the modal navigation
									}
									redirectURIString=requestedNavigationURI.toString();	//we already have the destination URI
								}
								else	//if navigation is not requested, request a navigation to the new bookmark location
								{
									redirectURIString=request.getRequestURL().append(newBookmark).toString();	//save the string form of the constructed bookmark URI
								}
								if(isAJAX)	//if this is an AJAX request
								{
									guiseContext.clearText();	//clear all the response data (which at this point should only be navigation information, anyway)
									guiseContext.writeElementBegin(null, "navigate");	//<navigate>	//TODO use a constant
									guiseContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString());	//xmlns:guise="http://guiseframework.com/id/ml#"
									if(requestedNavigation!=null)	//if navigation was requested (i.e. this isn't just a bookmark registration)
									{
										final String viewportID=requestedNavigation.getViewportID();	//get the requested viewport ID
										if(viewportID!=null)	//if a viewport was requested
										{
											guiseContext.writeAttribute(null, "viewportID", viewportID);	//specify the viewport ID TODO use a constant
										}
									}
									guiseContext.write(redirectURIString);	//write the navigation URI
									guiseContext.writeElementEnd(null, "navigate");	//</navigate>
									isNavigating=true;	//show that we're going to navigate; process the other events to make sure the data model is up-to-date (and in case the navigation gets overridden)
								}
								else	//if this is not an AJAX request
								{
									throw new HTTPMovedTemporarilyException(URI.create(redirectURIString));	//redirect to the new navigation location TODO fix to work with other viewports						
								}
			//TODO if !AJAX						throw new HTTPMovedTemporarilyException(requestedNavigationURI);	//redirect to the new navigation location
								//TODO store a flag or something---if we're navigating, we probably should flush the other queued events
							}
			
							
							
							
							if(!isNavigating && !ObjectUtilities.equals(oldPrincipal, guiseSession.getPrincipal()))	//if the principal has changed after updating the model (if we're navigating there's no need to reload)
							{
								if(isAJAX)	//if this is an AJAX request
								{
									guiseContext.clearText();	//clear all the response data (which at this point should only be navigation information, anyway)
									guiseContext.writeElementBegin(null, "reload", true);	//<reload>	//TODO use a constant
									guiseContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString());	//xmlns:guise="http://guiseframework.com/id/ml#"
									guiseContext.writeElementEnd(null, "reload");	//</reload>
									isNavigating=true;	//show that we're navigating, so there's no need to update views
								}
								else	//if this is not an AJAX request
								{
									throw new HTTPMovedTemporarilyException(guiseContext.getNavigationURI());	//redirect to the same page, which will generate a new request with no POST parameters, which would likely change the principal again)
								}
							}
						}
						catch(final RuntimeException runtimeException)	//if we run into any errors processing events
						{
							if(isAJAX)	//if this is an AJAX request
							{
								Debug.error(runtimeException);	//log the error
								//TODO send back the error
							}
							else	//if this is ano an AJAX request
							{
								throw runtimeException;	//pass the error back to the servlet TODO improve; pass to Guise
							}
							
						}

						if(isAJAX && !isNavigating && controlEvent instanceof InitControlEvent)	//if this is an AJAX initialization event (if we're navigating, there's no need to initialize this page) TODO maybe just dirty all the frames so this happens automatically
						{
							guiseContext.setState(GuiseContext.State.UPDATE_VIEW);	//update the context state for updating the view
								//close all the flyover frames to get rid of stuck flyover frames, such as those left from refreshing the page during flyover TODO fix; this is a workaround to keep refreshing the page from leaving stuck flyover frames; maybe do something better
							final Iterator<Frame<?>> flyoverFrameIterator=guiseSession.getFrameIterator();	//get an iterator to all the frames
							while(flyoverFrameIterator.hasNext())	//while there are more frames
							{
								final Frame<?> frame=flyoverFrameIterator.next();	//get the next frame
								if(frame instanceof FlyoverFrame)	//if this is a flyover frame
								{
									frame.close();	//close all flyover frames
								}
							}
								//send back any open frames
							final Iterator<Frame<?>> frameIterator=guiseSession.getFrameIterator();	//get an iterator to all the frames
							while(frameIterator.hasNext())	//while there are more frames
							{
								final Frame<?> frame=frameIterator.next();	//get the next frame
								if(frame!=guiseSession.getApplicationFrame())	//don't send back the application frame
								{
									guiseContext.writeElementBegin(XHTML_NAMESPACE_URI, "patch");	//<xhtml:patch>	//TODO use a constant TODO don't use the XHTML namespace if we can help it
		//							TODO fix							else	//if the component is not visible, remove the component's elements
									guiseContext.writeAttribute(null, ATTRIBUTE_XMLNS, XHTML_NAMESPACE_URI.toString());	//xmlns="http://www.w3.org/1999/xhtml"
									guiseContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString());	//xmlns:guise="http://guiseframework.com/id/ml#"
									frame.updateView(guiseContext);		//tell the component to update its view
									guiseContext.writeElementEnd(XHTML_NAMESPACE_URI, "patch");	//</xhtml:patch>
								}
							}
						}
						else	//if we don't need to update any views for these control events
						{
							guiseContext.setState(GuiseContext.State.INACTIVE);	//deactivate the context so that any model update events will be generated								
						}
					}

					
					
						//TODO move this to the bottom of the processing, as cookies only need to be updated before they go back
					synchronizeCookies(request, response, guiseSession);	//synchronize the cookies going out in the response; do this before anything is written back to the client
					
					if(!isNavigating)	//we'll only update the views if we're not navigating (if we're navigating, we're changing pages, anyway)
					{
						guiseContext.setState(GuiseContext.State.UPDATE_VIEW);	//update the context state for updating the view
						if(isAJAX)	//if this is an AJAX request
						{
							final Collection<Component<?>> dirtyComponents=getDirtyComponents(guiseSession);	//get all dirty components in all the session frames
			
							CollectionUtilities.removeAll(removedFrames, guiseSession.getFrameIterator());	//remove all the ending frames, leaving us the frames that were removed TODO improve all this
			//TODO fix					dirtyComponents.addAll(frames);	//add all the frames that were removed
							
							Debug.trace("we now have dirty components:", dirtyComponents.size());
							for(final Component<?> affectedComponent:dirtyComponents)
							{
								Debug.trace("affected component:", affectedComponent);
							}
							if(dirtyComponents.contains(applicationFrame))	//if the application frame itself was affected, we might as well reload the page
							{
								guiseContext.writeElementBegin(null, "reload", true);	//<reload>	//TODO use a constant
								guiseContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString());	//xmlns:guise="http://guiseframework.com/id/ml#"
								guiseContext.writeElementEnd(null, "reload");	//</reload>
							}
							else	//if the application frame wasn't affected
							{
								for(final Component<?> dirtyComponent:dirtyComponents)	//for each component affected by this update cycle
								{
			//TODO fix							if(dirtyComponent.isVisible())	//if the component is visible
									guiseContext.writeElementBegin(XHTML_NAMESPACE_URI, "patch");	//<xhtml:patch>	//TODO use a constant TODO don't use the XHTML namespace if we can help it
			//TODO fix							else	//if the component is not visible, remove the component's elements
									guiseContext.writeAttribute(null, ATTRIBUTE_XMLNS, XHTML_NAMESPACE_URI.toString());	//xmlns="http://www.w3.org/1999/xhtml"
									guiseContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString());	//xmlns:guise="http://guiseframework.com/id/ml#"
									dirtyComponent.updateView(guiseContext);		//tell the component to update its view
									guiseContext.writeElementEnd(XHTML_NAMESPACE_URI, "patch");	//</xhtml:patch>
								}
								for(final Frame<?> frame:removedFrames)	//for each removed frame
								{
									guiseContext.writeElementBegin(XHTML_NAMESPACE_URI, "remove");	//<xhtml:remove>	//TODO use a constant TODO don't use the XHTML namespace if we can help it								
									guiseContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString());	//xmlns:guise="http://guiseframework.com/id/ml#"
									guiseContext.writeAttribute(null, "id", frame.getID());	//TODO fix
			//TODO del Debug.trace("Sending message to remove frame:", frame.getID());
									guiseContext.writeElementEnd(XHTML_NAMESPACE_URI, "remove");	//</xhtml:remove>							
								}
							}
						}
						else	//if this is not an AJAX request
						{
							applicationFrame.updateView(guiseContext);		//tell the application frame to update its view						
						}
					}
					
					String text=guiseContext.getText();	//get the text to output
					if(isAJAX)	//if this is an AJAX request
					{
						guiseContext.setOutputContentType(XML_CONTENT_TYPE);	//switch to the "text/xml" content type TODO verify UTF-8 in a consistent, elegant way
						text="<response>"+text+"</response>";	//wrap the text in a response element
					}
//TODO del Debug.trace("response:", text);
//TODO del Debug.trace("response length:", text.length());
					final byte[] bytes=text.getBytes(UTF_8);	//write the content we collected in the context as series of bytes encoded in UTF-8
					final OutputStream outputStream=getCompressedOutputStream(request, response);	//get a compressed output stream, if possible
					outputStream.write(bytes);	//write the bytes
					outputStream.close();	//close the output stream, finishing writing the compressed contents (don't put this in a finally block, as it will attempt to write more data and raise another exception)						
				}
				finally
				{
					guiseContext.setState(GuiseContext.State.INACTIVE);	//always deactivate the context			
					guiseSession.setContext(null);	//remove this context from the session
				}
			}
			return true;	//show that we processed the Guise request
		}
		else	//if there was no navigation panel at the given path
		{
			return false;	//indicate that this was not a Guise component-related request
		}
	}
	
	/**Synchronizes the cookies in a request with the environment properties in a Guise session.
  Any cookies missing from the request will be added from the environment to the response.
  @param request The HTTP request.
  @param response The HTTP response.
  @param guiseSession The Guise session.
  */
	protected void synchronizeCookies(final HttpServletRequest request, final HttpServletResponse response, final GuiseSession guiseSession)
	{
			//remove unneeded cookies from the request						
		final String applicationBasePath=guiseSession.getApplication().getBasePath();	//get the application's base path
		assert applicationBasePath!=null : "Application not yet installed during cookie synchronization.";
		final GuiseEnvironment environment=guiseSession.getEnvironment();	//get the session's environment
		final Cookie[] cookies=request.getCookies();	//get the cookies in the request
		final Map<String, Cookie> cookieMap=new HashMap<String, Cookie>(cookies!=null ? cookies.length : 0);	//create a map to hold the cookies for quick lookup
		if(cookies!=null)	//if a cookie array was returned
		{
			for(final Cookie cookie:cookies)	//for each cookie in the request
			{
				final String cookieName=cookie.getName();	//get the name of this cookie
//TODO del Debug.trace("Looking at cookie", cookieName, "with value", cookie.getValue());
				if(!"jsessionid".equalsIgnoreCase(cookieName))	//ignore the session ID TODO use a constant
				{
//TODO del Debug.trace("Removing cookie", cookieName);
					final String environmentPropertyValue=asInstance(environment.getProperty(cookieName), String.class);	//see if there is a string environment property value for this cookie's name
					if(environmentPropertyValue!=null)	//if a value in the environment matches the cookie's name
					{
						if(!ObjectUtilities.equals(cookie.getValue(), encode(environmentPropertyValue)))	//if the cookie's value doesn't match the encoded environment property value
						{
							cookie.setValue(encode(environmentPropertyValue));	//update the cookie's value, making sure the value is encoded
						}
						cookieMap.put(cookieName, cookie);	//store the cookie in the map
					}
					else	//if there is no such environment property, remove the cookie
					{
						cookie.setValue(null);	//remove the value now
						cookie.setPath(applicationBasePath);	//set the cookie path to the application base path, because we'll need the same base path as the one that was set
						cookie.setMaxAge(0);	//tell the cookie to expire immediately
						response.addCookie(cookie);	//add the cookie to the response to delete it
					}
				}
			}
		}
			//add new cookies from the environment to the response
		for(final Map.Entry<String, Object> environmentPropertyEntry:environment.getProperties())	//iterate the environment properties so that new cookies can be added as needed
		{
			final String environmentPropertyName=environmentPropertyEntry.getKey();	//get the name of the environment property value
			if(!cookieMap.containsKey(environmentPropertyName))	//if no cookie contains this environment variable
			{
				final String environmentPropertyValue=asInstance(environmentPropertyEntry.getValue(), String.class);	//get the environment property value as a string
				if(environmentPropertyValue!=null)	//if there is a non-null environment property value
				{									
					final Cookie cookie=new Cookie(environmentPropertyName, encode(environmentPropertyValue));	//create a new cookie with the encoded property value
					cookie.setPath(applicationBasePath);	//set the cookie path to the application base path
					cookie.setMaxAge(Integer.MAX_VALUE);	//don't allow the cookie to expire for a very long time
					response.addCookie(cookie);	//add the cookie to the response
				}
			}
		}		
	}

	/*TODO comment
	@param context Guise context information.
	*/
/*TODO del when works
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
*/

	/**Retrieves all components that have views needing updated within a session.
	This method checks all frames in the session.
	If a given component is dirty, its child views will not be checked.
	@param session The Guise session to check for dirty views.
	*/
	protected void getControlsByName(final GuiseSession session, final String name, final Set<Component<?>> componentSet)	//TODO comment; tidy
	{
		final Iterator<Frame<?>> frameIterator=session.getFrameIterator();	//get an iterator to session frames
		while(frameIterator.hasNext())	//while there are more frames
		{
			final Frame<?> frame=frameIterator.next();	//get the next frame
			getControlsByName(frame, name, componentSet);			
//TODO del			AbstractComponent.getDirtyComponents(frame, dirtyComponents);	//gather more dirty components
		}
	}

	/*TODO comment
	@param context Guise context information.
	*/
	protected <T extends Component<?>> void getControlsByName(/*TODO fixfinal GuiseContext context, */final T component, final String name, final Set<Component<?>> componentSet)
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
			for(final Component<?> childComponent:((CompositeComponent<?>)component).getChildren())
			{
				getControlsByName(childComponent, name, componentSet);
//TODO fix				getControlsByName(context, childComponent, name, componentSet);
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
	private final PathExpression AJAX_REQUEST_COMPONENT_XPATH_EXPRESSION=new PathExpression("component");	//TODO use constants; comment 
	private final PathExpression AJAX_REQUEST_MOUSE_XPATH_EXPRESSION=new PathExpression("mouse");	//TODO use constants; comment 
	private final PathExpression AJAX_REQUEST_VIEWPORT_XPATH_EXPRESSION=new PathExpression("viewport");	//TODO use constants; comment 
	
	/**Retrieves control events from the HTTP request.
  @param request The HTTP request.
	@param guiseSession The Guise session object.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	protected List<ControlEvent> getControlEvents(final HttpServletRequest request, final GuiseSession guiseSession) throws ServletException, IOException
	{
Debug.trace("getting control events");
		final List<ControlEvent> controlEventList=new ArrayList<ControlEvent>();	//create a new list for storing control events
		final String contentTypeString=request.getContentType();	//get the request content type
		final ContentType contentType=contentTypeString!=null ? createContentType(contentTypeString) : null;	//create a content type object from the request content type, if there is one
		if(contentType!=null && GUISE_AJAX_REQUEST_CONTENT_TYPE.match(contentType))	//if this is a Guise AJAX request
		{
//		TODO del Debug.trace("Guise AJAX request");
			try
			{

				
				
				
				final DocumentBuilderFactory documentBuilderFactory=DocumentBuilderFactory.newInstance();	//create a document builder factory TODO create a shared document builder factory, maybe---but make sure it is used by only one thread			
				final DocumentBuilder documentBuilder=documentBuilderFactory.newDocumentBuilder();	//create a new document builder
				final Document document=documentBuilder.parse(request.getInputStream());	//read the document from the request

/*TODO del if not needed; IE6 SSL bug now worked around
final Document document=getXML(request);
//TODO del Debug.trace("got document:", XMLUtilities.toString(document));
if(document==null)	//TODO fix; del
{
	
	Debug.traceStack("error: unable to get document");
	throw new AssertionError("unable to get document");
}
*/			
				
				final List<Node> eventNodes=(List<Node>)XPath.evaluatePathExpression(document, AJAX_REQUEST_EVENTS_WILDCARD_XPATH_EXPRESSION);	//get all the events
				for(final Node eventNode:eventNodes)	//for each event node
				{
					if(eventNode.getNodeType()==Node.ELEMENT_NODE)//if this is an event element
					{
						final Element eventElement=(Element)eventNode;	//cast the node to an element
						if("form".equals(eventNode.getNodeName()))	//if this is a form event TODO use a constant
						{
							final boolean exhaustive=Boolean.valueOf(eventElement.getAttribute("exhaustive")).booleanValue();	//get the exhaustive indication TODO use a constant
							final boolean provisional=Boolean.valueOf(eventElement.getAttribute("provisional")).booleanValue();	//get the provisional indication TODO use a constant
							final FormControlEvent formSubmitEvent=new FormControlEvent(exhaustive, provisional);	//create a new form submission event
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
								final int option=Integer.parseInt(eventElement.getAttribute("option"));	//TODO tidy; improve; check for errors; comment
								final ActionControlEvent actionControlEvent=new ActionControlEvent(componentID, targetID, actionID, option);	//create a new action control event
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
						else if("mouseEnter".equals(eventNode.getNodeName()) || "mouseExit".equals(eventNode.getNodeName()))	//if this is a mouse event TODO use a constant
						{
							try
							{
								final MouseControlEvent.EventType eventType=MouseControlEvent.EventType.valueOf(eventNode.getNodeName().toUpperCase());	//get the event type from the type string

								final Node componentNode=XPath.getNode(eventNode, AJAX_REQUEST_COMPONENT_XPATH_EXPRESSION);	//get the component node
								final String componentID=((Element)componentNode).getAttribute("id");	//TODO tidy; improve; comment
								final int componentX=Integer.parseInt(((Element)componentNode).getAttribute("x"));	//TODO tidy; improve; check for errors; comment
								final int componentY=Integer.parseInt(((Element)componentNode).getAttribute("y"));	//TODO tidy; improve; check for errors; comment
								final int componentWidth=Integer.parseInt(((Element)componentNode).getAttribute("width"));	//TODO tidy; improve; check for errors; comment
								final int componentHeight=Integer.parseInt(((Element)componentNode).getAttribute("height"));	//TODO tidy; improve; check for errors; comment

								final Node targetNode=XPath.getNode(eventNode, AJAX_REQUEST_TARGET_XPATH_EXPRESSION);	//get the target node
								final String targetID=((Element)targetNode).getAttribute("id");	//TODO tidy; improve; comment
								final int targetX=Integer.parseInt(((Element)targetNode).getAttribute("x"));	//TODO tidy; improve; check for errors; comment
								final int targetY=Integer.parseInt(((Element)targetNode).getAttribute("y"));	//TODO tidy; improve; check for errors; comment
								final int targetWidth=Integer.parseInt(((Element)targetNode).getAttribute("width"));	//TODO tidy; improve; check for errors; comment
								final int targetHeight=Integer.parseInt(((Element)targetNode).getAttribute("height"));	//TODO tidy; improve; check for errors; comment
								
								final Node viewportNode=XPath.getNode(eventNode, AJAX_REQUEST_VIEWPORT_XPATH_EXPRESSION);	//get the viewport node
								final int viewportX=Integer.parseInt(((Element)viewportNode).getAttribute("x"));	//TODO tidy; improve; check for errors; comment
								final int viewportY=Integer.parseInt(((Element)viewportNode).getAttribute("y"));	//TODO tidy; improve; check for errors; comment
								final int viewportWidth=Integer.parseInt(((Element)viewportNode).getAttribute("width"));	//TODO tidy; improve; check for errors; comment
								final int viewportHeight=Integer.parseInt(((Element)viewportNode).getAttribute("height"));	//TODO tidy; improve; check for errors; comment

								final Node mouseNode=XPath.getNode(eventNode, AJAX_REQUEST_MOUSE_XPATH_EXPRESSION);	//get the mouse node
								final int mouseX=Integer.parseInt(((Element)mouseNode).getAttribute("x"));	//TODO tidy; improve; check for errors; comment
								final int mouseY=Integer.parseInt(((Element)mouseNode).getAttribute("y"));	//TODO tidy; improve; check for errors; comment

								if(componentID!=null)	//if there is a component ID TODO add better event handling, to throw an error and send back that error
								{
									final MouseControlEvent mouseEvent=new MouseControlEvent(eventType, componentID, new Rectangle(componentX, componentY, componentWidth, componentHeight),
											targetID, new Rectangle(targetX, targetY, targetWidth, targetHeight), new Rectangle(viewportX, viewportY, viewportWidth, viewportHeight),
											new Point(mouseX, mouseY));	//create a new mouse event
									controlEventList.add(mouseEvent);	//add the event to the list
//TODO del Debug.trace("mouse event; targetXY:", targetX, targetY, "viewportXY:", viewportX, viewportY, "mouseXY:", mouseX, mouseY);
								}
							}
							catch(final IllegalArgumentException illegalArgumentException)	//if we don't ignore the event type, don't create an event
							{								
							}
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
				if(!exhaustive || request.getParameter(XHTMLApplicationFrameView.getActionInputID(guiseSession.getApplicationFrame()))!=null)	//if this is a POST, only use the data if it is a Guise POST
				{				
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

	/**Begins modal navigation based upon modal navigation information.
	@param guiseApplication The Guise application.
	@param guiseSession The Guise session.
	@param modalNavigation The modal navigation information
	*/
	protected void beginModalNavigation(final GuiseApplication guiseApplication, final GuiseSession guiseSession, final ModalNavigation modalNavigation)
	{
		final ModalNavigationPanel<?, ?> modalPanel=(ModalNavigationPanel<?, ?>)guiseSession.getNavigationPanel(guiseApplication.relativizeURI(modalNavigation.getNewNavigationURI()));	//get the modal frame for this navigation path
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
		final GuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieves the Guise session for this container and request
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
		final GuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieves the Guise session for this container and request
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
	@see GuiseSession#setPrincipal(Principal)
	*/
	protected void authenticated(final HttpServletRequest request, final URI resourceURI, final String method, final String requestURI, final Principal principal, final String realm, final AuthenticateCredentials credentials, final boolean authenticated)
	{
		if(authenticated && credentials!=null)	//if authentication was successful with credentials (don't change the session principal for no credentials, because this might remove a principal set by the session itself with no knowledge of the browser)
		{
			final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container
			final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
			final GuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieves the Guise session for this container and request
			guiseSession.setPrincipal(principal);	//set the new principal in the Guise session
		}
	}

	/**Extracts the bookmark contained in the given request URL.
	@param request The HTTP request object.
	@return The bookmark represented by the request, or <code>null</code> if no bookmark is contained in the request.
	*/
	protected static Bookmark getBookmark(final HttpServletRequest request)
	{
		final String queryString=request.getQueryString();	//get the query string from the request
		if(queryString!=null && queryString.length()>0)	//if there is a query string (Tomcat 5.5.16 returns an empty string for no query, even though the Java Servlet specification 2.4 says that it should return null)
		{
//TODO del Debug.trace("just got query string from request, length", queryString.length(), "content", queryString);
			return new Bookmark(String.valueOf(QUERY_SEPARATOR)+queryString);	//construct a new bookmark, preceding the string with a query indicator
		}
		else	//if there is no query string, there is no bookmark
		{
//TODO del Debug.trace("just got null query string from request");
			return null;	//indicate that there is no bookmark information
		}
	}	

  /**Determines if the resource at a given URI exists.
  This version adds checks to see if the URI represents a valid application navigation path.
  This version adds support for Guise public resources.
  @param resourceURI The URI of the requested resource.
  @return <code>true</code> if the resource exists, else <code>false</code>.
	@exception IOException if there is an error accessing the resource.
	@see GuiseApplication#hasDestination(String)
	@see #isPublicResourceURI(URI)
  */
  protected boolean exists(final URI resourceURI) throws IOException
  {
//TODO del Debug.trace("checking exists", resourceURI);
  	if(isPublicResourceURI(resourceURI))	//if this URI represents a Guise public resource
  	{
  		final String publicResourceKey=getPublicResourceKey(resourceURI);	//get the Guise public resource key
  		return Guise.getInstance().getPublicResourceURL(publicResourceKey)!=null;	//see if there is a public resource for this key TODO add Guise.hasPublicResource()
  	}
  	final GuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
  	if(guiseApplication.hasDestination(guiseApplication.relativizeURI(resourceURI)))	//if the URI represents a valid navigation path
  	{
  		return true;	//the navigation path exists
  	}
 		return super.exists(resourceURI);	//see if a physical resource exists at the location, if we can't find a virtual resource (a Guise public resource or a navigation path component)
  }

	/**Determines the requested resource.
  This version adds support for Guise public resources.
	@param resourceURI The URI of the requested resource.
  @return An object providing an encapsulation of the requested resource, but not necessarily the contents of the resource. 
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource.
	@exception IOException if there is an error accessing the resource.
	@see #isPublicResourceURI(URI)
	@see #getPublicResourceKey(URI)
	@see Guise#getPublicResourceURL(String)
  */
	protected HTTPServletResource getResource(final URI resourceURI) throws IllegalArgumentException, IOException
	{
//TODO del Debug.trace("getting resource for URI: ", resourceURI);
		final HTTPServletResource resource;	//we'll create a resource for this resource URI
  	if(isPublicResourceURI(resourceURI))	//if this URI represents a Guise public resource
  	{
  		final String publicResourceKey=getPublicResourceKey(resourceURI);	//get the Guise public resource key
//  	TODO del Debug.trace("this is a public resource with key: ", publicResourceKey);
			final URL publicResourceURL=Guise.getInstance().getPublicResourceURL(publicResourceKey);	//get a URL to the resource
//		TODO del Debug.trace("found URL to resource: ", publicResourceURL);
			resource=new DefaultHTTPServletResource(resourceURI, publicResourceURL);	//create a new default resource with a URL to the public resource
//		TODO del Debug.trace("constructed a resource with length:", resource.getContentLength(), "and last modified", resource.getLastModified());
  	}
  	else	//if this is not a Guise public resource
  	{
  		resource=super.getResource(resourceURI);	//return a default resource
  	}
		final ContentType contentType=getContentType(resource);	//get the content type of the resource
//TODO del Debug.trace("got content type", contentType, "for resource", resource);
		if(TEXT_CSS_CONTENT_TYPE.match(contentType))	//if this is a CSS stylesheet
		{
			return new CSSResource(resource);	//return a resource that does extra CSS processing
		}
		else	//if this not a resource for extra processing
		{
			return resource;	//return the resource without extra processing
		}
	}

	/**A resource that represents a CSS file, decorating an existing resource.
	This version compresses resources of type <code>text/css</code>.
	This version processes resources of type <code>text/css</code> to work around IE6 bugs, if IE6 is the user agent.	
	@author Garret Wilson
	*/
	protected static class CSSResource extends DefaultResource implements HTTPServletResource
	{

		/**The decorated resource.*/
		private final HTTPServletResource resource;

			/**@return The decorated resource.*/
			protected HTTPServletResource getResource() {return resource;}

		/**The bytes that constitute the resource, or <code>null</code> if no transformation has yet taken place.*/
		private byte[] bytes=null;
		
		/**Returns a reference to the resource bytes.
		@param request The HTTP request in response to which the bytes are being retrieved.
		If the bytes are retrieved from the decorated resource if they haven't already been.
		@return The bytes that constitute the resource.
		@exception IOException if there is an error retrieving the bytes.
		*/
		protected byte[] getBytes(final HttpServletRequest request) throws IOException
		{
			synchronized(resource)
			{
				if(bytes==null)	//if no bytes are available
				{
					final InputStream inputStream=getResource().getInputStream(request);	//get an input stream to the resource
					try
					{
						final GuiseCSSProcessor cssProcessor=new GuiseCSSProcessor();
						final ParseReader cssReader=new ParseReader(new InputStreamReader(inputStream, UTF_8));
						final CSSStylesheet cssStylesheet=cssProcessor.process(cssReader);	//parse the stylesheet
//TODO del			Debug.trace("just parsed stylesheet:", cssStylesheet);
						final Map<String, Object> userAgentProperties=getUserAgentProperties(request);	//get the user agent properties for this request
						if(USER_AGENT_NAME_MSIE.equals(userAgentProperties.get(USER_AGENT_NAME_PROPERTY)))	//if this is IE
						{
							final Object version=userAgentProperties.get(USER_AGENT_VERSION_NUMBER_PROPERTY);	//get the version number
							if(version instanceof Float && ((Float)version).floatValue()<7.0f)	//if this is IE 6 (lower than IE 7)
							{
								cssProcessor.fixIE6Stylesheet(cssStylesheet);	//fix this stylesheet for IE6
//TODO del								Debug.trace("fixed stylesheet for IE6", cssStylesheet);
							}
						}
						bytes=cssStylesheet.toString().getBytes(UTF_8);
					}
					catch(final IOException ioException)	//TODO del when works; log the error
					{
		Debug.error(ioException);
						throw ioException;
					}
					finally
					{
						inputStream.close();	//always close the original input stream
					}
				}
				return bytes;	//return the resource bytes
			}			
		}
		
		/**Returns the content length of the resource.
		@param request The HTTP request in response to which the content length is being retrieved.
		@return The content length of the resource.
		@exception IOException if there is an error getting the length of the resource.
		*/
		public long getContentLength(final HttpServletRequest request) throws IOException
		{
			return getBytes(request).length;	//return the length of bytes
		}

		/**Determines the last modification time of the resource.
		This version delegates to the decorated resource.
		@param request The HTTP request in response to which the last modified time is being retrieved.
		@return The time of last modification as the number of milliseconds since January 1, 1970 GMT.
		@exception IOException if there is an error getting the last modified time.
		*/
		public long getLastModified(final HttpServletRequest request) throws IOException
		{
			return getResource().getLastModified(request);
		}

		/**Returns an input stream to the resource.
		@param request The HTTP request in response to which the input stream is being retrieved.
		@return The lazily-created input stream to the resource.
		@exception IOException if there is an error getting an input stream to the resource.
		*/
		public InputStream getInputStream(final HttpServletRequest request) throws IOException
		{
			return new ByteArrayInputStream(getBytes(request));	//return an input stream to the bytes
		}

		/**HTTP servlet resource constructor.
		@param resource The decorated HTTP servlet resource.
		@exception IllegalArgumentException if the given resource is <code>null</code>.
		*/
		public CSSResource(final HTTPServletResource resource)
		{
			super(checkInstance(resource, "Resource cannot be null.").getReferenceURI());	//construct the parent class
			this.resource=resource;	//save the decorated resource
		}
	}

	/**Retrieves an input stream to the given resource.
	This version compresses resources of type <code>text/css</code>.
	This version processes resources of type <code>text/css</code> to work around IE6 bugs, if IE6 is the user agent.
	@param resource The resource for which an input stream should be retrieved.
	@return An input stream to the given resource.
	@exception IOException Thrown if there is an error accessing the resource, such as a missing file or a resource that has no contents.
	*/
/*TODO del when works
	protected InputStream getInputStream(final HTTPServletResource resource) throws IOException
	{
		final InputStream inputStream=super.getInputStream(resource);	//get the default input stream to the resource
		final ContentType contentType=getContentType(resource);	//get the content type of the resource
Debug.trace("got content type", contentType, "for resource", resource);
		if(TEXT_CSS_CONTENT_TYPE.match(contentType))	//if this is a CSS stylesheet
		{
Debug.trace("this is CSS content");
			try
			{
				final XMLCSSProcessor cssProcessor=new XMLCSSProcessor();
				final Reader cssReader=new InputStreamReader(inputStream, UTF_8);
				CSSStyleSheet cssStylesheet=cssProcessor.parseStyleSheet(cssReader, resource.getReferenceURI());	//parse the stylesheet
	Debug.trace("just parsed stylesheet:", cssStylesheet);
				return new ByteArrayInputStream(cssStylesheet.toString().getBytes(UTF_8));
			}
			catch(final Exception exception)
			{
Debug.error(exception);
			}
			finally
			{
Debug.trace("closing original input stream");
				inputStream.close();	//always close the original input stream
Debug.trace("closed original input stream");
			}
		}
		return inputStream;	//return our input stream
	}
*/

	/**Determines whether the given URI references a Guise public resource.
	The URI references a public resource if the path, relative to the application base path, begins with {@value WebPlatformConstants#GUISE_PUBLIC_PATH}.
	@param uri The reference URI, which is assumed to have this servlet's domain.
	@return <code>true</code> if the given URI references a Guise public resource.
	@exception NullPointerException if the given URI is <code>null</code>.
	*/
	public boolean isPublicResourceURI(final URI uri)
	{
		final String rawPath=uri.getRawPath();	//get the raw path of the URI
		if(rawPath!=null)	//if there is a raw path
		{
//		TODO del 	Debug.trace("is public resource URI?", uri);
			final String applicationBasePath=getGuiseApplication().getBasePath();	//get the application base path
//		TODO del Debug.trace("application base path", applicationBasePath);
			final String relativePath=relativizePath(applicationBasePath, rawPath);	//relativize the raw path to the base path
//		TODO del Debug.trace("relativePath", relativePath);
			return relativePath.startsWith(GUISE_PUBLIC_PATH);	//see if the relative path starts with the Guise public resource base path
		}
		else	//if there is no raw path
		{
			return false;	//this is not a public resource URI
		}
	}

	/**Determines the public resource key for the given URI.
	The path of the given URI, relative to the application base path, must begin with {@value WebPlatformConstants#GUISE_PUBLIC_PATH}.
	This path prefix will be replaced with {@value Guise#PUBLIC_RESOURCE_BASE_PATH}.
	@param uri The URI of the public resource, which is assumed to have this servlet's domain.
	@return The path to a Guise public resource.
	@exception IllegalArgumentException if the raw path of the URI is <code>null</code> or does not start with {@value WebPlatformConstants#GUISE_PUBLIC_RESOURCE_BASE_PATH}.
	*/
	public String getPublicResourceKey(final URI uri)
	{
		final String rawPath=uri.getRawPath();	//get the raw path of the URI
		if(rawPath==null)	//if the raw path is null
		{
			throw new IllegalArgumentException("Guise public resource URI "+uri+" has no path.");
		}
		final String applicationBasePath=getGuiseApplication().getBasePath();	//get the application base path
		final String relativePath=relativizePath(applicationBasePath, rawPath);	//relativize the raw path to the base path
		if(!relativePath.startsWith(GUISE_PUBLIC_PATH))	//if this isn't a public resource URI
		{
			throw new IllegalArgumentException("URI "+uri+ " does not identify a Guise public resource.");
		}
		return PUBLIC_RESOURCE_BASE_PATH+relativePath.substring(GUISE_PUBLIC_PATH.length());	//replace the beginning of the relative path with the resource base path
	}
}
