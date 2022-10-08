/*
 * Copyright © 2005-2012 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.platform.web;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.*;
import java.nio.charset.Charset;
import java.security.Principal;
import java.time.Instant;
import java.util.*;

import static java.nio.charset.StandardCharsets.*;
import static java.util.Collections.*;
import static java.util.Objects.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.*;

import com.globalmentor.collections.CollectionMap;
import com.globalmentor.collections.Collections;
import com.globalmentor.event.ProgressEvent;
import com.globalmentor.event.ProgressListener;
import com.globalmentor.io.*;
import com.globalmentor.java.Objects;
import com.globalmentor.javascript.JSON;
import com.globalmentor.model.NameValuePair;
import com.globalmentor.model.ObjectHolder;
import com.globalmentor.model.TaskState;
import com.globalmentor.net.*;
import com.globalmentor.net.http.*;
import com.globalmentor.net.mime.ContentDispositionType;
import com.globalmentor.security.Nonce;
import com.globalmentor.servlet.Servlets;
import com.globalmentor.servlet.http.*;
import com.globalmentor.text.elff.*;
import com.globalmentor.xml.spec.XML;
import com.globalmentor.xml.xpath.*;

import io.clogr.Clogged;
import io.guise.framework.*;
import io.guise.framework.component.*;
import io.guise.framework.event.*;
import io.guise.framework.geometry.*;
import io.guise.framework.input.Key;
import io.guise.framework.model.FileItemResourceImport;
import io.guise.framework.platform.*;
import io.guise.framework.platform.web.WebPlatform.PollCommand;
import io.guise.framework.platform.web.css.*;
import io.urf.model.UrfObject;
import io.urf.model.UrfResourceDescription;
import io.urf.vocab.content.Content;

import static com.globalmentor.html.spec.HTML.*;
import static com.globalmentor.io.Filenames.*;
import static com.globalmentor.io.Files.*;
import static com.globalmentor.java.Enums.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.java.Threads.*;
import static com.globalmentor.net.URIs.*;
import static com.globalmentor.net.HTTP.*;
import static com.globalmentor.servlet.Servlets.*;
import static com.globalmentor.servlet.http.HTTPServlets.*;
import static com.globalmentor.text.elff.WebTrendsConstants.*;
import static com.globalmentor.time.TimeZones.*;
import static com.globalmentor.xml.spec.XML.*;
import static io.guise.framework.platform.web.WebPlatform.*;
import static io.guise.framework.platform.web.WebUserAgentProduct.Brand.*;
import static io.guise.framework.platform.web.adobe.Flash.*;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.event.Level;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * The servlet that controls a Guise web applications. Each Guise session's platform will be locked during normal web page generation context will be active at
 * one one time. This implementation only works with Guise applications that descend from {@link AbstractGuiseApplication}.
 * <p>
 * For all {@link ResourceReadDestination}s, this servlet recognizes a query parameter named {@value #GUISE_CONTENT_DISPOSITION_URI_QUERY_PARAMETER} specifying
 * the content disposition of the content to return; the value is the serialize version of a {@link ContentDispositionType} value.
 * </p>
 * This servlet supports the following initialization parameters in addition to those in {@link BaseHTTPServlet}:
 * <dl>
 * <dt>{@link Servlets#DATA_DIRECTORY_INIT_PARAMETER}</dt>
 * <dd>The directory for storing data.</dd>
 * <dt>{@link Servlets#LOG_DIRECTORY_INIT_PARAMETER}</dt>
 * <dd>The directory for storing logs.</dd>
 * <dt>{@link #DEBUG_INIT_PARAMETER}</dt>
 * <dd>Whether the servlet is in debug mode; should be "true" or "false"; sets the log level to debug if not explicitly set.</dd>
 * <dt>{@link #LOG_LEVEL_INIT_PARAMETER}</dt>
 * <dd>The level of logging for the JVM of type {@link Level}. If multiple servlets specify this value, the last one initialized will have precedence.</dd>
 * <dt>{@link #LOG_HTTP_INIT_PARAMETER}</dt>
 * <dd>Whether HTTP communication is logged.</dd>
 * <dt>{@link #PROFILE_INIT_PARAMETER}</dt>
 * <dd>Whether profiling should occur; should be "true" or "false".</dd>
 * </dl>
 * <p>
 * For example, the following Guise servlet context might define a data directory:
 * </p>
 * <blockquote>{@code <Context ...><Parameter name="dataDirectory" value="D:\data"/></Context>}</blockquote>
 * @author Garret Wilson
 */
public class GuiseHTTPServlet extends DefaultHTTPServlet implements Clogged {

	/** The init parameter, "applicationClass", used to specify the Guise application to create. */
	public static final String APPLICATION_CLASS_INIT_PARAMETER = "applicationClass";

	/** The init parameter prefix, "guise-environment:", used to indicate a Guise environment property. */
	public static final String GUISE_ENVIRONMENT_INIT_PARAMETER_PREFIX = "guise-environment:";

	/** The init parameter suffix, ".uri", used to indicate that a Guise environment property should be processed as a URI. */
	public static final String GUISE_ENVIRONMENT_URI_INIT_PARAMETER_SUFFIX = ".uri";

	/**
	 * The URI query parameter indicating that the content disposition of the content of a {@link ResourceReadDestination}. The value will be the serialize
	 * version of a {@link ContentDispositionType} value.
	 */
	public static final String GUISE_CONTENT_DISPOSITION_URI_QUERY_PARAMETER = "guiseContentDisposition";

	/** The ID of the viewport to use for sending resources. */
	public static final String SEND_RESOURCE_VIEWPORT_ID = "guiseDownload";

	/** The Guise container that owns the applications. */
	private HTTPServletGuiseContainer guiseContainer = null;

	/**
	 * Returns the Guise container. This method must not be called before a request is processed.
	 * @return The Guise container that owns the applications.
	 * @throws IllegalStateException if this method is called before any requests have been processed.
	 */
	protected HTTPServletGuiseContainer getGuiseContainer() {
		if(guiseContainer == null) { //if no container exists
			throw new IllegalStateException("Cannot access Guise container before first HTTP request, due to the Java Servlet architecture.");
		}
		return guiseContainer; //return the Guise container
	}

	/** The Guise application controlled by this servlet. */
	private AbstractGuiseApplication guiseApplication;

	/** @return The Guise application controlled by this servlet. */
	protected AbstractGuiseApplication getGuiseApplication() {
		return guiseApplication;
	}

	/** The ELFF logger for this Guise application. */
	private final ELFF elff;

	/** @return The ELFF logger for this application. */
	public ELFF getELFF() {
		return elff;
	}

	/**
	 * The initializer for initializing ELFF writers. This implementation writes the default directives along with the <code>Start-Date</code> directive.
	 */
	protected final IOOperation<Writer> elffWriterInitializer = new IOOperation<Writer>() {

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation writes ELFF directives to the ELFF writer along with the <code>Start-Date</code> directive.
		 * </p>
		 */
		@SuppressWarnings("unchecked")
		@Override
		//we use a generic NameValuePair as a vararg
		public void perform(final Writer writer) throws IOException {
			writer.write(getELFF().serializeDirectives()); //write the directives to the ELFF writer
			writer.write(getELFF().serializeDirective(ELFF.START_DATE_DIRECTIVE, ELFF.createDateTimeFormat().format(new Date()))); //add the Start-Date directive with the current time
			writer.flush(); //flush the directives to the writer
		}
	};

	/**
	 * The uninitializer for uninitializing ELFF writers. This implementation writes the <code>End-Date</code> directive.
	 */
	protected final IOOperation<Writer> elffWriterUninitializer = new IOOperation<Writer>() {

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation writes the <code>End-Date</code> directive to the ELFF writer.
		 * </p>
		 */
		@Override
		public void perform(final Writer writer) throws IOException {
			writer.write(getELFF().serializeDirective(ELFF.END_DATE_DIRECTIVE, ELFF.createDateTimeFormat().format(new Date()))); //add the End-Date directive with the current time
			writer.flush(); //flush the directive to the writer
		}
	};

	/**
	 * Default constructor. Creates a single Guise application.
	 */
	public GuiseHTTPServlet() {
		elff = new ELFF(
				//create an ELFF log
				Field.DATE_FIELD, Field.TIME_FIELD, Field.CLIENT_IP_FIELD, Field.CLIENT_SERVER_USERNAME_FIELD, Field.CLIENT_SERVER_HOST_FIELD,
				Field.CLIENT_SERVER_METHOD_FIELD, Field.CLIENT_SERVER_URI_STEM_FIELD, Field.CLIENT_SERVER_URI_QUERY_FIELD, Field.SERVER_CLIENT_STATUS_FIELD,
				Field.CLIENT_SERVER_BYTES_FIELD, Field.CLIENT_SERVER_VERSION_FIELD, Field.CLIENT_SERVER_USER_AGENT_HEADER_FIELD,
				Field.CLIENT_SERVER_COOKIE_HEADER_FIELD, Field.CLIENT_SERVER_REFERER_HEADER_FIELD, Field.DCS_ID_FIELD);
		elff.setDirective(ELFF.SOFTWARE_DIRECTIVE, Guise.GUISE_NAME + ' ' + Guise.getVersion()); //set the software directive of the ELFF log
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version initializes the Guise application.
	 * </p>
	 */
	@Override
	public void initialize(ServletConfig servletConfig) throws ServletException, IllegalArgumentException, IllegalStateException {
		super.initialize(servletConfig); //do the default initialization
		getLogger().info("Initializing {} {} {}", Guise.GUISE_NAME, Guise.getVersion(), Guise.getBuildDate());
		setReadOnly(true); //make this servlet read-only
		//TODO turn off directory listings, and/or fix them
		try {
			guiseApplication = initGuiseApplication(servletConfig); //initialize the application and frame bindings
		} catch(final ServletException servletException) {
			getLogger().error("", servletException);
			throw servletException;
		} catch(final Exception exception) { //if there is any problem initializing the Guise application
			getLogger().error("", exception);
			throw new ServletException("Error initializing Guise application: " + exception.getMessage(), exception);
		}
	}

	/**
	 * Initializes bindings between paths and associated navigation frame classes.
	 * @param servletConfig The servlet configuration.
	 * @return The new initialized application.
	 * @throws IllegalArgumentException if the one of the frame bindings is not expressed in correct format.
	 * @throws IllegalArgumentException if the one of the classes specified as a frame binding could not be found.
	 * @throws IllegalArgumentException if the one of the classes specified as a frame binding could not be found.
	 * @throws ClassCastException if the one of the classes specified as a frame binding does not represent a subclass of a frame component.
	 * @throws ServletException if there is a problem initializing the application or frame bindings.
	 * @see Frame
	 */
	protected AbstractGuiseApplication initGuiseApplication(final ServletConfig servletConfig) throws ServletException {
		final ServletContext servletContext = servletConfig.getServletContext(); //get the servlet context
		final AbstractGuiseApplication guiseApplication; //create the application and store it here TODO why do we require a particular abstract implementation base? 
		final String guiseApplicationClassInitParameter = servletConfig.getInitParameter(APPLICATION_CLASS_INIT_PARAMETER); //get name of the Guise application class
		if(guiseApplicationClassInitParameter != null) {
			try {
				guiseApplication = AbstractGuiseApplication.class.cast(Class.forName(guiseApplicationClassInitParameter).getDeclaredConstructor().newInstance());
			} catch(final ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException
					| ClassCastException exception) { //TODO these need to be logged
				throw new ServletException(exception);
			}
		} else { //if no application description is specified, indicate an error TODO allow Guise to support overlays in the future with default Guise applications
			throw new ServletException("web.xml missing Guise application class init parameter \"" + APPLICATION_CLASS_INIT_PARAMETER + "\".");
		}
		//TODO del when WebPlatform works		guiseApplication.installComponentKit(new XHTMLComponentKit());	//create and install an XHTML controller kit
		//install configured environment properties
		final Environment environment = guiseApplication.getEnvironment(); //get the application environment
		final Enumeration<String> initParameterNames = (Enumeration<String>)servletContext.getInitParameterNames(); //get all the init parameter names from the servlet context, allowing all init parameters to be retrieved, even those stored externally
		while(initParameterNames.hasMoreElements()) { //while there are more init parameters
			final String initParameterName = initParameterNames.nextElement(); //get the next init parameter
			if(initParameterName.startsWith(GUISE_ENVIRONMENT_INIT_PARAMETER_PREFIX)) { //if this is a Guise parameter specification
				final String initParameterValue = servletContext.getInitParameter(initParameterName); //get the value of the init parameter
				if(initParameterValue != null) { //if there is a value recorded (just in case the deployment description somehow managed on some platform to store a null value)
					final String environmentPropertyName = initParameterName.substring(GUISE_ENVIRONMENT_INIT_PARAMETER_PREFIX.length()); //determine the name of the environment property
					Object environmentPropertyValue = initParameterValue; //we'll see if we need to change the value type
					if(initParameterName.endsWith(GUISE_ENVIRONMENT_URI_INIT_PARAMETER_SUFFIX)) { //if the init parameter name ends with ".uri", try to process it as a URI
						try {
							environmentPropertyValue = new URI(initParameterValue); //convert the string to a URI, if we can
						} catch(final URISyntaxException uriSyntaxException) { //if we couldn't parse the value as a URI
							getLogger().warn("Unable to process Guise environment property {} {} value {} as a URI.", environmentPropertyName, environmentPropertyValue);
						}
					}
					environment.setProperty(environmentPropertyName, environmentPropertyValue); //store the Guise environment property in the environment
				}
			}
		}
		return guiseApplication; //return the created Guise application
	}

	/** The mutex that prevents two threads from trying to initialize the Guise container simultaneously. */
	private Object guiseContainerMutex = new Object();

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version installs the application into the container.
	 * </p>
	 */
	@Override
	public void initialize(final HttpServletRequest request) throws ServletException {
		//TODO del Log.trace("initializing servlet from request");
		super.initialize(request); //do the default initialization
		synchronized(guiseContainerMutex) { //if more than one request are coming in simultaneously, only look up the container for the first one (although multiple lookups should still retrieve the same container)
			//TODO del	Log.trace("checking container");
			if(guiseContainer == null) { //if no container exists
				//TODO del				Log.trace("context path", getContextPath());
				final URI requestURI = URI.create(request.getRequestURL().toString()); //get the URI of the current request
				//			TODO del	Log.trace("requestURI", requestURI);
				final URIPath containerBasePath = URIPath.of(getContextPath() + PATH_SEPARATOR); //determine the base path of the container TODO important: determine if getContextPath() returns the raw path, as we want; otherwise, this will not work correctly for context paths with encoded path characters
				final URI containerBaseURI = changePath(requestURI, containerBasePath); //determine the container base URI
				//			TODO del	Log.trace("containerURI", containerBaseURI);

				final ServletContext servletContext = getServletContext(); //get the servlet context
				guiseContainer = HTTPServletGuiseContainer.getGuiseContainer(servletContext, containerBaseURI); //get a reference to the Guise container, creating it if needed
				//			TODO del	Log.trace("guise container: ", guiseContainer, "for servlet context", getServletContext());
				//TODO del Log.trace("installing application into guise container: ", guiseContainer, "for servlet context", getServletContext());
				//install the application into the container
				final AbstractGuiseApplication guiseApplication = getGuiseApplication(); //get the Guise application
				/*TODO del when works
										//"/contextPath" or "", "/servletPath" or ""
								final URIPath guiseApplicationBasePath=URIPath.of(request.getContextPath()+request.getServletPath()+PATH_SEPARATOR);	//construct the Guise application base path from the servlet request, which is the concatenation of the web application path and the servlet's path with an ending slash
								final URIPath guiseApplicationRelativePath=containerBasePath.relativize(guiseApplicationBasePath);	//get the application path relative to the container path
				*/

				//TODO del Log.trace("context path", request.getContextPath(), "servlet path", request.getServletPath(), "container base path", containerBasePath, "application base path", guiseApplicationBasePath, "application relative path", guiseApplicationRelativePath);
				try {
					final File dataDirectory = getDataDirectory(servletContext); //get the data directory
					if(dataDirectory == null) { //if there is no data directory
						throw new ServletException(
								"No data directory available; either deploy servlet as in a file system or define the init parameter " + DATA_DIRECTORY_INIT_PARAMETER + ".");
					}
					URI applicationBaseURI = guiseApplication.getBaseURI(); //see if the application already has a preferred base URI
					if(applicationBaseURI == null) { //if the application has no preferred base URI
						applicationBaseURI = resolve(containerBaseURI, request.getContextPath() + request.getServletPath() + PATH_SEPARATOR); //get the default Guise application base path from the servlet request, which is the concatenation of the web application path and the servlet's path with an ending slash, and resolve it to the container base path
					}
					final URIPath guiseApplicationRelativePath = URIPath.EMPTY_URI_PATH; //TODO fix, now that we are using URIs for each application; perhaps use a name for each application, as this ID must remain the same even when deployed in different locations
					final File guiseApplicationHomeDirectory = getDataDirectory(servletContext, DATA_DIRECTORY_INIT_PARAMETER,
							"guise/home/" + guiseApplicationRelativePath); //get the explicitly defined data directory; if there is no data directory defined, use the default data directory with a subpath of "guise/home" plus the application relative path TODO use a constant
					final File guiseApplicationLogDirectory = getDataDirectory(servletContext, LOG_DIRECTORY_INIT_PARAMETER,
							"guise/logs/" + guiseApplicationRelativePath); //get the explicitly defined data directory; if there is no data directory defined, use the default data directory with a subpath of "guise/home" plus the application relative path TODO use a constant
					final File guiseApplicationTempDirectory = getDataDirectory(servletContext, TEMP_DIRECTORY_INIT_PARAMETER,
							"guise/temp/" + guiseApplicationRelativePath); //get the explicitly defined data directory; if there is no data directory defined, use the default data directory with a subpath of "guise/home" plus the application relative path TODO use a constant
					//			TODO delLog.trace("ready to install application into container with context path", guiseApplicationContextPath);
					guiseContainer.installApplication(guiseApplication, applicationBaseURI, guiseApplicationHomeDirectory, guiseApplicationLogDirectory,
							guiseApplicationTempDirectory); //install the application
				} catch(final IOException ioException) { //if there is an I/O exception installing the application
					throw new ServletException(ioException);
				}
			}
		}
		//	TODO del		Log.trace("initializing; container base URI:", guiseContainer.getBaseURI(), "container base path:", guiseContainer.getBasePath());
		/*TODO del when works; now application is installed when container is retrieved
				final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container (which we just created if needed)
				final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
				if(guiseApplication.getContainer()==null) {	//if this application has not yet been installed (note that there is a race condition here if multiple HTTP requests attempt to access the application simultaneously, but the losing thread will simply throw an exception and not otherwise disturb the application functionality)
					final String guiseApplicationContextPath=request.getContextPath()+request.getServletPath()+PATH_SEPARATOR;	//construct the Guise application context path from the servlet request, which is the concatenation of the web application path and the servlet's path with an ending slash
		//		TODO delLog.trace("ready to install application into container with context path", guiseApplicationContextPath);
					guiseContainer.installApplication(guiseApplication, guiseApplicationContextPath);	//install the application
				}
		*/
	}

	//TODO fix HEAD method servicing, probably by overriding serveResource()

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final HTTPServletGuiseContainer guiseContainer = getGuiseContainer(); //get the Guise container
		final GuiseApplication guiseApplication = getGuiseApplication(); //get the Guise application
		final HTTPServletGuiseRequest guiseRequest = new HTTPServletGuiseRequest(request, /*TODO del response, */guiseContainer, guiseApplication); //get Guise request information
		if(guiseRequest.isRequestPathReserved()) { //if this is a request for a Guise reserved path (e.g. a public resource or a temporary resource)
			super.doGet(request, response); //go ahead and retrieve the resource immediately
			return; //don't try to see if there is a navigation path for this path
		}

		final Destination destination = guiseApplication.getDestination(guiseRequest.getNavigationPath()).orElse(null); //try to get a destination associated with the requested path TODO improve use of Optional
		if(destination != null) { //if we have a destination associated with the requested path
			getLogger().trace("found destination: {}", destination);
			final GuiseSession guiseSession = HTTPServletGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request); //retrieve the Guise session for this container and request
			//make sure the environment has the WebTrends ID
			final Environment environment = guiseSession.getPlatform().getEnvironment(); //get the session's environment
			if(!environment.hasProperty(WEBTRENDS_ID_COOKIE_NAME)) { //if the environment doesn't have a WebTrends ID
				final StringBuilder webtrendsIDStringBuilder = new StringBuilder(); //create a string builder for creating a WebTrends ID
				webtrendsIDStringBuilder.append(request.getRemoteAddr()); //IP address
				webtrendsIDStringBuilder.append('-'); //-
				webtrendsIDStringBuilder.append(System.currentTimeMillis()); //current time in milliseconds
				//TODO fix nanonseconds if needed, but Java doesn't even offer this information
				environment.setProperty(WEBTRENDS_ID_COOKIE_NAME, webtrendsIDStringBuilder.toString()); //store the WebTrends ID in the environment, which will be stored in the cookies eventually
			}
			final String httpMethod = request.getMethod(); //get the current HTTP method being used
			getLogger().trace("method {}", httpMethod);
			getLogger().trace("destination {} is resource read destination? {}", destination, destination instanceof ResourceReadDestination);
			getLogger().trace("is GET with resource destination?", (destination instanceof ResourceReadDestination && GET_METHOD.equals(httpMethod)));
			if(destination instanceof ResourceReadDestination && GET_METHOD.equals(httpMethod)) { //if this is a resource read destination (but only if this is a GET request; the ResourceReadDestination may also be a ResourceWriteDestination)
				final URIPath path = guiseRequest.getNavigationPath(); //get the path
				final Bookmark bookmark = guiseRequest.getBookmark(); //get the bookmark, if any
				final URI referrerURI = guiseRequest.getReferrerURI(); //get the referrer URI, if any
				final URIPath newPath = destination.getPath(guiseSession, path, bookmark, referrerURI); //see if we should use another path
				if(!newPath.equals(path)) { //if we should use another path
					redirect(guiseRequest, guiseApplication, newPath.toURI(), bookmark, true); //redirect the user agent to the preferred path
				}
				getLogger().trace("ready to delegate to super");
				super.doGet(request, response); //let the default functionality take over, which will take care of accessing the resource destination by creating a specialized access resource
				return; //don't service the Guise request normally
			}

			final GuiseSessionThreadGroup guiseSessionThreadGroup = Guise.getInstance().getThreadGroup(guiseSession); //get the thread group for this session
			try {
				call(guiseSessionThreadGroup, new Runnable() { //call the method in a new thread inside the thread group

					@Override
					public void run() {
						try {
							if(guiseApplication.isDebug()) {
								//TODO fix										Probe.startStackProbe(); //TODO testing
							}
							try {
								serviceGuiseRequest(guiseRequest, response, guiseContainer, guiseApplication, guiseSession, destination); //service the Guise request to the given destination
							} finally {
								if(guiseApplication.isDebug()) {
									//TODO fix											Probe.stopStackProbe(); //TODO testing
								}
							}
						} catch(final IOException ioException) { //if an exception is thrown
							throw new UndeclaredThrowableException(ioException); //let it pass to the calling thread
						}
					}

				});
			} catch(final UndeclaredThrowableException undeclaredThrowableException) { //if an exception was thrown
				final Throwable cause = undeclaredThrowableException.getCause(); //see what exception was thrown
				if(cause instanceof ResourceNotFoundException) { //if a ResourceNotFoundException was thrown
					HTTPException.createHTTPException((ResourceIOException)cause); //pass back an equivalent HTTP exception
				} else if(cause instanceof IOException) { //if an IOException was thrown
					throw ((IOException)cause); //pass it on
				} else { //we don't expect any other types of exceptions
					throw new AssertionError(cause);
				}
			}
		} else { //if there is no Guise destination for the requested path
			super.doGet(request, response); //let the default functionality take over
		}
	}

	@Override
	public void doPut(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final HTTPServletGuiseContainer guiseContainer = getGuiseContainer(); //get the Guise container
		final GuiseApplication guiseApplication = getGuiseApplication(); //get the Guise application
		final HTTPServletGuiseRequest guiseRequest = new HTTPServletGuiseRequest(request, /*TODO del response, */guiseContainer, guiseApplication); //get Guise request information
		if(guiseRequest.isRequestPathReserved()) { //if this is a request for a Guise reserved path (e.g. a public resource or a temporary resource)
			throw new HTTPForbiddenException("Uploading content to " + guiseRequest.getNavigationPath() + " is not allowed.");
		}
		final Destination destination = guiseApplication.getDestination(guiseRequest.getNavigationPath()).orElse(null); //try to get a destination associated with the requested path TODO improve use of Optional
		if(!(destination instanceof ResourceWriteDestination)) { //if we don't have a write destination associated with the requested path
			throw new HTTPForbiddenException("Uploading content to " + guiseRequest.getNavigationPath() + " is not supported.");
		}
		getLogger().trace("found resource write destination: {}", destination);
		final GuiseSession guiseSession = HTTPServletGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request); //retrieve the Guise session for this container and request
		final GuiseSessionThreadGroup guiseSessionThreadGroup = Guise.getInstance().getThreadGroup(guiseSession); //get the thread group for this session
		try {
			call(guiseSessionThreadGroup, new Runnable() { //call the method in a new thread inside the thread group

				@Override
				public void run() {
					try {
						serviceGuiseResourceWriteDestinationRequest(guiseRequest, response, guiseContainer, guiseApplication, guiseSession,
								(ResourceWriteDestination)destination, request.getInputStream(), null);
					} catch(final IOException ioException) { //if an exception is thrown
						throw new UndeclaredThrowableException(ioException); //let it pass to the calling thread
					}
				}

			});
		} catch(final UndeclaredThrowableException undeclaredThrowableException) { //if an exception was thrown
			final Throwable cause = undeclaredThrowableException.getCause(); //see what exception was thrown
			if(cause instanceof ResourceNotFoundException) { //if a ResourceNotFoundException was thrown
				HTTPException.createHTTPException((ResourceIOException)cause); //pass back an equivalent HTTP exception
			} else if(cause instanceof IOException) { //if an IOException was thrown
				throw ((IOException)cause); //pass it on
			} else { //we don't expect any other types of exceptions
				throw new AssertionError(cause);
			}
		}
	}

	/**
	 * Services a Guise request. If this is a request for a Guise component destination, a Guise context will be assigned to the Guise session while the request
	 * is processed.
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 * @param guiseContainer The Guise container.
	 * @param guiseApplication The Guise application.
	 * @param guiseSession The Guise session.
	 * @param destination The Guise session destination being accessed.
	 * @throws IOException if there is an error reading or writing data.
	 */
	private void serviceGuiseRequest(final HTTPServletGuiseRequest guiseRequest, final HttpServletResponse response,
			final HTTPServletGuiseContainer guiseContainer, final GuiseApplication guiseApplication, final GuiseSession guiseSession, final Destination destination)
			throws IOException {
		getLogger().trace("servicing Guise request with request {}", guiseRequest);
		final WebPlatform guisePlatform = (WebPlatform)guiseSession.getPlatform(); //get the web platform
		final URI requestURI = guiseRequest.getDepictURI(); //get the request URI
		final MediaType contentType = guiseRequest.getRequestContentType(); //get the request content type
		final URIPath path = guiseRequest.getNavigationPath(); //get the path
		final Bookmark bookmark = guiseRequest.getBookmark(); //get the bookmark, if any
		final URI referrerURI = guiseRequest.getReferrerURI(); //get the referrer URI, if any
		//TODO del		final boolean isAJAX=contentType!=null && GUISE_AJAX_REQUEST_CONTENT_TYPE.match(contentType);	//see if this is a Guise AJAX request
		final String requestMethod = guiseRequest.getHTTPServletRequest().getMethod(); //get the request method
		//TODO verify; does this work with file uploads?
		//this is a non-AJAX Guise POST if there is an XHTML action input ID field TODO add a better field; stop using a view
		final boolean isGuisePOST = POST_METHOD.equals(requestMethod)
				&& guiseRequest.getHTTPServletRequest().getParameter(WebApplicationFrameDepictor.getActionInputID(guiseSession.getApplicationFrame())) != null;
		/*TODO del
				final String rawPathInfo=getRawPathInfo(request);	//get the raw path info
				assert isAbsolutePath(rawPathInfo) : "Expected absolute path info, received "+rawPathInfo;	//the Java servlet specification says that the path info will start with a '/'
				URIPath navigationPath=URIPath.of(rawPathInfo.substring(1));	//remove the beginning slash to get the navigation path from the path info
		*/
		if(!guiseRequest.isAJAX()) { //if this is not an AJAX request, verify existence and permissions
			if(GET_METHOD.equals(requestMethod) || !(destination instanceof ResourceWriteDestination)) { //if this is not an AJAX request, verify that the destination exists (doing this with AJAX requests would be too costly; we can assume that AJAX requests are for existing destinations) (but don't check if this is a POST to a ResourceWriteDestination, which probably won't exist; TODO clarify exist() semantics for ResourceWriteDestinations)
				final URIPath newPath = destination.getPath(guiseSession, path, bookmark, referrerURI); //see if we should use another path
				if(!newPath.equals(path)) { //if we should use another path
					redirect(guiseRequest, guiseApplication, newPath.toURI(), bookmark, true); //redirect the user agent to the preferred path
				}
				if(!destination.exists(guiseSession, path, bookmark, referrerURI)) { //if this destination doesn't exist
					throw new HTTPNotFoundException("Path does not exist at Guise destination: " + path);
				}
			}
			if(!destination.isAuthorized(guiseSession, path, bookmark, referrerURI)) { //if this destination isn't authorized
				throw new HTTPForbiddenException("Path is not authorized at Guise destination: " + path);
			}
		}
		if(destination instanceof ComponentDestination) { //if we have a component destination associated with the requested path
			serviceGuiseComponentDestinationRequest(guiseRequest, response, guiseContainer, guiseApplication, guiseSession, (ComponentDestination)destination); //service the request for the component destination TODO eventually maybe create an HTTPServletComponentDestination and pass everything there
		} else if(destination instanceof ResourceWriteDestination) { //if we should be writing to this destination TODO refactor this to use serviceGuiseResourceWriteDestinationRequest()
			if(ServletFileUpload.isMultipartContent(guiseRequest.getHTTPServletRequest())) { //if the request is multipart content, as we expect
				final ResourceWriteDestination resourceWriteDestination = (ResourceWriteDestination)destination; //get the destination for writing the resource
				/*TODO del
								final Bookmark bookmark=getBookmark(request);	//get the bookmark from this request
								final String referrer=getReferer(request);	//get the request referrer, if any
								final URI referrerURI=referrer!=null ? getPlainURI(URI.create(referrer)) : null;	//get a plain URI version of the referrer, if there is a referrer
				*/
				//TODO del and tidy
				//TODO del if not needed final HTTPServletWebDepictContext depictContext = new HTTPServletWebDepictContext(guiseRequest, response, guiseSession, resourceWriteDestination); //create a new Guise context
				try {
					final ServletFileUpload servletFileUpload = new ServletFileUpload(); //create a new servlet file upload object
					final Set<Component> progressComponents = new HashSet<Component>(); //keep track of which components need to know about progress
					final FileItemIterator itemIterator = servletFileUpload.getItemIterator(guiseRequest.getHTTPServletRequest()); //get an iterator to the file items
					while(itemIterator.hasNext()) { //while there are more items
						final FileItemStream fileItemStream = itemIterator.next(); //get the current file item
						//TODO del if not needed						final String name=item.getFieldName();	//get
						if(!fileItemStream.isFormField()) { //if this isn't a form field item, it's a file upload item for us to process
							final String itemName = fileItemStream.getName(); //get the item's name, if any
							if(itemName != null && itemName.length() > 0) { //if a non-empty-string name is specified
								final String fieldName = fileItemStream.getFieldName(); //get the field name for this item
								final Component progressComponent; //there may be a component that will want to know progress
								if(FILEDATA_FORM_FIELD_NAME.equals(fieldName)) { //if this is a Flash upload
									progressComponent = null; //there is no progress component; Flash will send its own progress update separately
								} else { //if this is not a Flash upload
									progressComponent = asInstance(guisePlatform.getDepictedObject(guisePlatform.getDepictID(fieldName)), Component.class).orElse(null); //get the component by its ID
								}
								if(progressComponent != null && !progressComponents.contains(progressComponent)) { //if there is a transfer component and this is the first transfer for this component
									progressComponents.add(progressComponent); //add this progress component to our set of progress components so we can send finish events to them later
									//Log.trace("sending progress with no task for starting");
									synchronized(guiseSession) { //don't allow other session contexts to be active while we dispatch the event
										progressComponent.processEvent(new WebProgressDepictEvent(progressComponent, null, TaskState.INCOMPLETE, 0)); //indicate to the component that progress is starting for all transfers
									}
								}
								final UrfObject resourceDescription = new UrfObject(); //create a new resource description
								final String itemContentTypeString = fileItemStream.getContentType(); //get the item content type, if any
								if(itemContentTypeString != null) { //if we know the item's content type
									final MediaType itemContentType = MediaType.parse(itemContentTypeString);
									if(!MediaType.APPLICATION_OCTET_STREAM_MEDIA_TYPE.hasBaseType(itemContentType)) { //if the content type is not just a generic "bunch of bytes" content type
										resourceDescription.setPropertyValue(Content.TYPE_PROPERTY_TAG, itemContentType); //set the resource's content type
									}
								}
								final String name = URIs.getName(itemName); //removing any extraneous path information a browser such as IE or Opera might have given TODO verify that the correct slashes are being checked
								resourceDescription.setPropertyValueByHandle("info-name", name); //specify the name provided to us TODO determine official approach for specifying URF name

								try {
									try (final InputStream inputStream = new BufferedInputStream(fileItemStream.openStream())) { //get an input stream to the item
										final ProgressListener progressListener = new ProgressListener() { //create a progress listener for listening for progress

											@Override
											public void progressed(ProgressEvent progressEvent) { //when progress has been made
												//Log.trace("delta: ", progressEvent.getDelta(), "progress:", progressEvent.getValue());
												synchronized(guiseSession) { //don't allow other session contexts to be active while we dispatch the event
													if(progressComponent != null) { //if there is a progress component
														progressComponent.processEvent(new WebProgressDepictEvent(progressComponent, name, TaskState.INCOMPLETE, progressEvent.getValue())); //indicate to the component that progress is starting for this file
													}
												}
											}

										};
										try (final ProgressOutputStream progressOutputStream = new ProgressOutputStream(
												resourceWriteDestination.getOutputStream(resourceDescription, guiseSession, path, bookmark, referrerURI))) { //get an output stream to the destination; don't buffer the output stream (our copy method essentially does this) so that progress events will be accurate
											if(progressComponent != null) { //if we know the component that wants to know progress
												synchronized(guiseSession) { //don't allow other session contexts to be active while we dispatch the event
													progressComponent.processEvent(new WebProgressDepictEvent(progressComponent, name, TaskState.INCOMPLETE, 0)); //indicate to the component that progress is starting for this file
												}
											}
											progressOutputStream.addProgressListener(progressListener); //start listening for progress events from the output stream
											IOStreams.copy(inputStream, progressOutputStream); //copy the uploaded file to the destination
											progressOutputStream.removeProgressListener(progressListener); //stop listening for progress events from the output stream
											//TODO catch and send errors here
										}
										if(progressComponent != null) { //if we know the component that wants to know progress (send the progress event after the output stream is closed, because the output stream may buffer contents)
											synchronized(guiseSession) { //don't allow other session contexts to be active while we dispatch the event
												progressComponent.processEvent(new WebProgressDepictEvent(progressComponent, name, TaskState.COMPLETE, 0)); //indicate to the component that progress is finished for this file
											}
										}
									}
								} finally {
									servletFileUpload.setProgressListener(null); //always stop listening for progress
								}
							}
						}
					}
					for(final Component progressComponent : progressComponents) { //for each component that was notified of progress
						synchronized(guiseSession) { //don't allow other session contexts to be active while we dispatch the event
							progressComponent.processEvent(new WebProgressDepictEvent(progressComponent, null, TaskState.COMPLETE, 0)); //indicate to the component that progress is finished for all transfers
						}
					}
				} catch(final FileUploadException fileUploadException) { //if there was an upload exception
					//TODO do something interesting with the error so that the ResourceCollectControl will learn of it
					throw (IOException)new IOException(fileUploadException.getMessage()).initCause(fileUploadException);
				}
				//TODO del if not needed				response.getOutputStream().write("testupload posted\n".getBytes());	//TODO del
			}
		} else if(destination instanceof RedirectDestination) { //if we have a component destination associated with the requested path
			redirect(guiseRequest, guiseApplication, (RedirectDestination)destination, bookmark); //perform the redirect; this should never return
			throw new AssertionError("Redirect not expected to allow processing to continue.");
		} else { //if we don't recognize the destination type
			throw new AssertionError("Unrecognized destination type: " + destination.getClass());
		}
		/*TODO del when works
				else {	//if there was no navigation panel at the given path
					return false;	//indicate that this was not a Guise component-related request
				}
		*/
	}

	/**
	 * Services a Guise upload request intended for a write destination.
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 * @param guiseContainer The Guise container.
	 * @param guiseApplication The Guise application.
	 * @param guiseSession The Guise session.
	 * @param resourceWriteDestination The Guise session destination being accessed.
	 * @param inputStream The input stream containing the resource content to be written; this stream will not be closed in this method.
	 * @param progressComponent The component the wants to know progress, or <code>null</code> if no component wants to know progress.
	 * @throws IOException if there is an error reading or writing data.
	 */
	private void serviceGuiseResourceWriteDestinationRequest(final HTTPServletGuiseRequest guiseRequest, final HttpServletResponse response,
			final HTTPServletGuiseContainer guiseContainer, final GuiseApplication guiseApplication, final GuiseSession guiseSession,
			final ResourceWriteDestination resourceWriteDestination, final InputStream inputStream, final Component progressComponent) throws IOException {
		getLogger().trace("servicing Guise request with request {}", guiseRequest);
		final WebPlatform guisePlatform = (WebPlatform)guiseSession.getPlatform(); //get the web platform
		final URI requestURI = guiseRequest.getDepictURI(); //get the request URI
		final MediaType contentType = guiseRequest.getRequestContentType(); //get the request content type
		final URIPath path = guiseRequest.getNavigationPath(); //get the path
		final Bookmark bookmark = guiseRequest.getBookmark(); //get the bookmark, if any
		final URI referrerURI = guiseRequest.getReferrerURI(); //get the referrer URI, if any
		//TODO del		final boolean isAJAX=contentType!=null && GUISE_AJAX_REQUEST_CONTENT_TYPE.match(contentType);	//see if this is a Guise AJAX request
		final String requestMethod = guiseRequest.getHTTPServletRequest().getMethod(); //get the request method
		//TODO del if not needed				final HTTPServletWebDepictContext depictContext=new HTTPServletWebDepictContext(guiseRequest, response, guiseSession, resourceWriteDestination);	//create a new Guise context
		final UrfResourceDescription resourceDescription = new UrfObject(); //create a new resource description
		/*TODO del if not needed
					final String itemContentTypeString=fileItemStream.getContentType();	//get the item content type, if any
					if(itemContentTypeString!=null) {	//if we know the item's content type
						final ContentType itemContentType=ContentType.getInstance(itemContentTypeString);
						if(!ContentType.APPLICATION_OCTET_STREAM_CONTENT_TYPE.match(itemContentType)) {	//if the content type is not just a generic "bunch of bytes" content type
							setContentType(resourceDescription, itemContentType);	//set the resource's content type
						}
					}
		*/
		final String name = URIs.findName(requestURI).orElse(null); //determine a name to use for informational purposes TODO create URIPath.getName() and use on the navigation path
		final ProgressListener progressListener = new ProgressListener() { //create a progress listener for listening for progress

			@Override
			public void progressed(ProgressEvent progressEvent) { //when progress has been made
				//Log.trace("delta: ", progressEvent.getDelta(), "progress:", progressEvent.getValue());
				synchronized(guiseSession) { //don't allow other session contexts to be active while we dispatch the event
					if(progressComponent != null) { //if there is a progress component
						progressComponent.processEvent(new WebProgressDepictEvent(progressComponent, name, TaskState.INCOMPLETE, progressEvent.getValue())); //indicate to the component that progress is starting for this file
					}
				}
			}

		};
		try (
				//get an output stream to the destination; don't buffer the output stream (our copy method essentially does this) so that progress events will be accurate
				final OutputStream resourceOutputStream = resourceWriteDestination.getOutputStream(resourceDescription, guiseSession, path, bookmark, referrerURI);
				//if we know the component that wants to know progress, create a wrapper that will give us progress so that we can send it back manually
				final OutputStream outputStream = progressComponent != null ? new ProgressOutputStream(resourceOutputStream) : resourceOutputStream) {
			if(progressComponent != null) { //if we know the component that wants to know progress
				synchronized(guiseSession) { //don't allow other session contexts to be active while we dispatch the event
					progressComponent.processEvent(new WebProgressDepictEvent(progressComponent, name, TaskState.INCOMPLETE, 0)); //indicate to the component that progress is starting for this file
				}
			}
			if(outputStream instanceof ProgressOutputStream) {
				((ProgressOutputStream)outputStream).addProgressListener(progressListener); //start listening for progress events from the output stream
			}
			IOStreams.copy(inputStream, outputStream); //copy the uploaded file to the destination
			if(outputStream instanceof ProgressOutputStream) {
				((ProgressOutputStream)outputStream).removeProgressListener(progressListener); //stop listening for progress events from the output stream
			}
			//TODO catch and send errors here
		}
		if(progressComponent != null) { //if we know the component that wants to know progress (send the progress event after the output stream is closed, because the output stream may buffer contents)
			synchronized(guiseSession) { //don't allow other session contexts to be active while we dispatch the event
				progressComponent.processEvent(new WebProgressDepictEvent(progressComponent, name, TaskState.COMPLETE, 0)); //indicate to the component that progress is finished for this file
			}
		}
	}

	/**
	 * Services a Guise request meant for a component destination. A Guise context is assigned to the Guise session while the request is processed.
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 * @param guiseContainer The Guise container.
	 * @param guiseApplication The Guise application.
	 * @param guiseSession The Guise session.
	 * @param componentDestination The Guise component destination being accessed.
	 * @param requestURI The URI requested.
	 * @param navigationPath The navigation path relative to the application base path.
	 * @throws IOException if there is an error reading or writing data.
	 */
	private void serviceGuiseComponentDestinationRequest(/*TODO del final HttpServletRequest request, */final HTTPServletGuiseRequest guiseRequest,
			final HttpServletResponse response, final HTTPServletGuiseContainer guiseContainer, final GuiseApplication guiseApplication,
			final GuiseSession guiseSession, final ComponentDestination componentDestination/*TODO del , final URI requestURI, final URIPath navigationPath*/)
			throws IOException {
		final HTTPServletWebPlatform guisePlatform = (HTTPServletWebPlatform)guiseSession.getPlatform(); //get the web platform
		/*TODO del
				final String contentTypeString=request.getContentType();	//get the request content type
				final ContentType contentType=contentTypeString!=null ? createContentType(contentTypeString) : null;	//create a content type object from the request content type, if there is one
		*/
		final boolean isAJAX = guiseRequest.isAJAX(); //see if this is a Guise AJAX request
		//this is a non-AJAX Guise POST if there is an XHTML action input ID field TODO add a better field; stop using a view
		final boolean isGuisePOST = POST_METHOD.equals(guiseRequest.getHTTPServletRequest().getMethod())
				&& guiseRequest.getHTTPServletRequest().getParameter(WebApplicationFrameDepictor.getActionInputID(guiseSession.getApplicationFrame())) != null;
		final HTTPServletWebDepictContext depictContext = new HTTPServletWebDepictContext(guiseRequest, response, guiseSession, componentDestination); //create a new Guise context
		depictContext.registerDataAttributeNamespaceURI(GUISE_ML_NAMESPACE_URI); //use HTML5 data attributes for the Guise namespace
		//Log.trace("setting context");
		guisePlatform.getDepictLock().lock(); //get the platform depict lock TODO surely reconsider this
		guisePlatform.setDepictContext(depictContext); //set the depict context for this platform
		try {
			/*TODO del modal navigation
						if(!guiseRequest.isAJAX()) {	//if this is not an AJAX request, see if we need to enforce modal navigation (only do this after we find a navigation panel, as this request might be for a stylesheet or some other non-panel resource, which shouldn't be redirected)
							final ModalNavigation modalNavigation=guiseSession.getModalNavigation();	//see if we are currently doing modal navigation
							if(modalNavigation!=null) {	//if we are currently in the middle of modal navigation, make sure the correct panel was requested
								final URI modalNavigationURI=modalNavigation.getNewNavigationURI();	//get the modal navigation URI
								if(!requestURI.getRawPath().equals(modalNavigationURI.getRawPath())) {	//if this request was for a different path than our current modal navigation path (we wouldn't be here if the domain, application, etc. weren't equivalent)
									throw new HTTPMovedTemporarilyException(modalNavigationURI);	//redirect to the modal navigation location
								}
							}
						}
			*/
			final URIPath navigationPath = guiseRequest.getNavigationPath(); //get the logical path for this request
			final Bookmark navigationBookmark = guiseRequest.getBookmark(); //get the bookmark from this request
			//TODO fix to recognize navigation, bookmark, and principal changes when the navigation panel is created		final Bookmark bookmark=getBookmark(request);	//get the bookmark from this request
			final Bookmark oldBookmark = isAJAX ? guiseSession.getBookmark() : navigationBookmark; //get the original bookmark, which will be the one requested in navigation (which we'll soon set) if this is a normal HTTP GET/POST
			//TODO del Log.trace("navigation bookmark:", navigationBookmark, "old bookmark:", oldBookmark, "session bookmark:", guiseSession.getBookmark(), "is AJAX:", isAJAX);
			final Principal oldPrincipal = guiseSession.getPrincipal(); //get the old principal
			final Component destinationComponent = guiseSession.getDestinationComponent(componentDestination); //get the component bound to the requested destination
			assert destinationComponent != null : "No component found, even though we found a valid destination.";
			final ApplicationFrame applicationFrame = guiseSession.getApplicationFrame(); //get the application frame
			//TODO del Log.trace("ready to get request events");
			final List<GuiseEvent> requestEvents = getRequestEvents(guiseRequest, guiseSession, depictContext); //get all events from the request
			getLogger().trace("got control events");
			if(isAJAX) { //if this is an AJAX request
				/*TODO tidy when stringbuilder context works
										guiseContext.setOutputContentType(XML_CONTENT_TYPE);	//switch to the "text/xml" content type
										guiseContext.writeElementBegin(null, "response");	//<response>	//TODO use a constant, decide on a namespace
				*/
			} else { //if this is not an AJAX request
				//TODO del Log.trace("this is not AJAX, with method:", request.getMethod(), "content type", contentType, "guise POST?", isGuisePOST);
				applicationFrame.setContent(destinationComponent); //place the component in the application frame
				setNoCache(guiseRequest.getHTTPServletRequest(), response); //make sure the response is not cached TODO should we do this for AJAX responses as well?
				/*TODO del
								final String referrer=getReferer(request);	//get the request referrer, if any
								final URI referrerURI=referrer!=null ? getPlainURI(URI.create(referrer)) : null;	//get a plain URI version of the referrer, if there is a referrer
				*/
				final URI referrerURI = guiseRequest.getReferrerURI(); //get the referrer URI, if any
				final MediaType contentType = guiseRequest.getRequestContentType(); //get the request content type
				//see if there is non-Guise HTTP POST data, and if so, set that bookmark navigation temporarily
				//a non-Guise form HTTP POST, get the servlet parameters (which will include the URL query information)
				if(POST_METHOD.equals(guiseRequest.getHTTPServletRequest().getMethod()) && contentType != null
						&& APPLICATION_X_WWW_FORM_URLENCODED_MEDIA_TYPE.hasBaseType(contentType) && !isGuisePOST) {
					//TODO del Log.trace("using servlet parameter methods");
					final List<Bookmark.Parameter> bookmarkParameterList = new ArrayList<Bookmark.Parameter>(); //create a new list of bookmark parameters
					final Iterator<Map.Entry<String, String[]>> parameterEntryIterator = (Iterator<Map.Entry<String, String[]>>)guiseRequest.getHTTPServletRequest()
							.getParameterMap().entrySet().iterator(); //get an iterator to the parameter entries
					while(parameterEntryIterator.hasNext()) { //while there are more parameter entries
						final Map.Entry<String, String[]> parameterEntry = parameterEntryIterator.next(); //get the next parameter entry
						final String parameterKey = parameterEntry.getKey(); //get the parameter key
						final String[] parameterValues = parameterEntry.getValue(); //get the parameter values
						for(final String parameterValue : parameterValues) { //for each parameter value
							//TODO del Log.trace("adding parameter bookmark:", parameterKey, parameterValue);
							bookmarkParameterList.add(new Bookmark.Parameter(parameterKey, parameterValue)); //create a corresponding bookmark parameter
						}
					}
					if(!bookmarkParameterList.isEmpty()) { //if there are bookmark parameters
						final Bookmark.Parameter[] bookmarkParameters = bookmarkParameterList.toArray(new Bookmark.Parameter[bookmarkParameterList.size()]); //get an array of bookmark parameters
						final Bookmark postBookmark = new Bookmark(bookmarkParameters); //create a new bookmark to represent the POST information
						guiseSession.setNavigation(navigationPath, postBookmark, referrerURI); //set the session navigation to the POST bookmark information
					}
				}
				//TODO del Log.trace("ready to set navigation with new navigation path:", navigationPath, "navigation bookmark:", navigationBookmark, "referrerURI:", referrerURI);
				guiseSession.setNavigation(navigationPath, navigationBookmark, referrerURI); //set the session navigation with the navigation bookmark, firing any navigation events if appropriate
			}
			final Set<Frame> removedFrames = new HashSet<Frame>(); //create a set of frames so that we can know which ones were removed TODO testing
			Collections.addAll(removedFrames, guiseSession.getApplicationFrame().getChildFrames().iterator()); //get all the current frames; we'll determine which ones were removed, later TODO improve all this
			boolean isNavigating = false; //we'll check this later to see if we're navigating so we won't have to update all the components
			for(final GuiseEvent requestEvent : requestEvents) { //for each request event
				final Set<Component> requestedComponents = new HashSet<Component>(); //create a set of component that were identified in the request
				try {
					if(requestEvent instanceof DepictEvent) { //if this is an event for a depicted object
						DepictEvent depictEvent = (DepictEvent)requestEvent; //get the depict event TODO maybe make sure the the component is in the current hierarchy, if this is a component depict event
						depictEvent.getDepictedObject().getDepictor().processEvent(depictEvent); //tell the object's depictor to process the depict event TODO maybe eventually pass these events through the platform, and let the platform dispatch the event
					} else if(requestEvent instanceof WebPlatformEvent) { //if this is a web platform event
						//TODO fix to submit form event to entire hierarchy
						final WebPlatformEvent controlEvent = (WebPlatformEvent)requestEvent; //get the request event as a control event
						if(controlEvent instanceof WebFormEvent) { //if this is a form submission
							final WebFormEvent formControlEvent = (WebFormEvent)controlEvent; //get the form control event
							if(formControlEvent.isExhaustive()) { //if this is an exhaustive form submission (such as a POST submission)
								if(formControlEvent.getParameterListMap().size() > 0) { //only process the event if there were submitted values---especially important for radio buttons and checkboxes, which must assume a value of false if nothing is submitted for them, thereby updating the model
									requestedComponents.add(destinationComponent); //we'll give the event to the entire destination component
								}
							} else { //if this is only a partial form submission
								final CollectionMap<String, Object, List<Object>> parameterListMap = formControlEvent.getParameterListMap(); //get the request parameter map
								for(final Map.Entry<String, List<Object>> parameterListMapEntry : parameterListMap.entrySet()) { //for each entry in the map of parameter lists
									final String parameterName = parameterListMapEntry.getKey(); //get the parameter name

									if(parameterName.equals(WebApplicationFrameDepictor.getActionInputID(applicationFrame)) && parameterListMapEntry.getValue().size() > 0) { //if this parameter is for an action
										final Component actionComponent = AbstractComponent.getComponentByID(applicationFrame,
												guisePlatform.getDepictID(parameterListMapEntry.getValue().get(0).toString())); //get an action component
										if(actionComponent != null) { //if we found an action component
											requestedComponents.add(actionComponent); //add it to the list of requested components
										}
									} else { //if this parameter is not a special action parameter
										//TODO don't re-update nested components (less important for controls, which don't have nested components)
										//TODO del Log.trace("looking for component with name", parameterName);
										getComponentsByDepictName(applicationFrame, parameterName, requestedComponents); //get all components with depictions using the given name
										//TODO del; test new method; tidy; comment							getControlsByName(guiseContext, navigationPanel, parameterName, requestedComponents);	//get all components identified by this name
									}
								}
							}
						} else if(controlEvent instanceof WebInitializeEvent) { //if this is an initialization event
							final WebInitializeEvent initControlEvent = (WebInitializeEvent)controlEvent; //get the init control event
							final HttpServletRequest request = guiseRequest.getHTTPServletRequest(); //get the HTTP servlet request
							final String javascriptVersion = initControlEvent.getJavaScriptVersion(); //get the JavaScript version reported
							if(javascriptVersion != null) { //if JavaScript is supported
								guisePlatform.setJavaScriptProduct(new DefaultProduct(null, "JavaScript", javascriptVersion, Double.parseDouble(javascriptVersion))); //set the JavaScript product for the platform TODO use a constant
							}
							//TODO del if not needed							TimeZone timeZone=null;	//we'll try to determine a time zone from the initialization information
							final int utcOffset = initControlEvent.getUTCOffset(); //get the current UTC offset in milliseconds
							final TimeZone timeZone = guiseSession.getTimeZone(); //get the session time zone
							final Date now = new Date();
							if(timeZone.getOffset(now.getTime()) != utcOffset) { //if the session time zone doesn't match the client UTC offset

								guiseSession.setTimeZone(getTimeZone(now, utcOffset)); //get a time zone for this offset and update the sesson's time zone TODO improve to check DST

							}
							/*TODO fix or del
														final String[] timeZoneIDs=TimeZone.getAvailableIDs(utcOffset);	//get the available time zone IDs for the UTC offset
														if(timeZoneIDs.length>0) {	//if there are time zone IDs that match the given offset
															for(final String timeZoneID:timeZoneIDs) {	//look at each available time zone ID
																timeZone=TimeZone.getTimeZone(timeZoneID);	//try this time zone
																	//TODO finish
															}
														}
														else
														{
															timeZone=TimeZone.getTimeZone(GMT_ID);	//default to GMT
														}
							*/
							//perform ELFF logging
							final Entry entry = new Entry();
							entry.setFieldValue(Field.DATE_FIELD, now);
							entry.setFieldValue(Field.TIME_FIELD, now);
							entry.setFieldValue(Field.CLIENT_IP_FIELD, request.getRemoteAddr());
							entry.setFieldValue(Field.CLIENT_SERVER_USERNAME_FIELD, request.getRemoteUser());
							//							TODO fix				entry.setFieldValue(Field.CLIENT_SERVER_HOST_FIELD, request.get
							entry.setFieldValue(Field.CLIENT_SERVER_METHOD_FIELD, GET_METHOD); //log the GET method always for WebTrends
							//TODO del								entry.setFieldValue(Field.CLIENT_SERVER_METHOD_FIELD, request.getMethod());
							entry.setFieldValue(Field.CLIENT_SERVER_URI_STEM_FIELD, getRawPathInfo(request));
							//TODO del								final List<NameValuePair<String, String>> queryParameters=new ArrayList<NameValuePair<String, String>>();	//create an array of parameters
							final StringBuilder queryParametersStringBuilder = new StringBuilder(); //create a new string builder for adding the query parameters
							final String queryString = request.getQueryString(); //get the current query string
							if(queryString != null) { //if there is a query string
								queryParametersStringBuilder.append(queryString); //start with the current query string
							}
							if(queryParametersStringBuilder.length() > 0) { //if we have an existing query string
								queryParametersStringBuilder.append(QUERY_NAME_VALUE_PAIR_DELIMITER); //append '&' to separate the old parameters from the new ones
							}
							//WT.bh
							ELFF.appendURIQueryParameter(queryParametersStringBuilder, BROWSING_HOUR_QUERY_ATTRIBUTE_NAME, Integer.toString(initControlEvent.getHour()))
									.append(QUERY_NAME_VALUE_PAIR_DELIMITER); //add WT.bh as a query parameter
							//TODO add WT.co to indicate whether cookies are enabled
							//TODO add WT.co_d to provide cookie data (only on the first time---is this a WebTrends hack that isn't needed here, or is it used for something?)
							//WT.sr
							ELFF.appendURIQueryParameter(queryParametersStringBuilder, BROWSER_SIZE_QUERY_ATTRIBUTE_NAME,
									Integer.toString(initControlEvent.getBrowserWidth()) + "x" + Integer.toString(initControlEvent.getBrowserHeight()))
									.append(QUERY_NAME_VALUE_PAIR_DELIMITER); //add WT.bs as a query parameter
							//WT.cd
							ELFF.appendURIQueryParameter(queryParametersStringBuilder, COLOR_DEPTH_QUERY_ATTRIBUTE_NAME, Integer.toString(initControlEvent.getColorDepth()))
									.append(QUERY_NAME_VALUE_PAIR_DELIMITER); //add WT.cd as a query parameter
							//TODO add WT.fi (ActiveX)
							//TODO add WT.fv (ActiveX version?)
							//WT.jo
							ELFF.appendURIQueryParameter(queryParametersStringBuilder, JAVA_ENABLED_QUERY_ATTRIBUTE_NAME,
									WebTrendsYesNo.asYesNo(initControlEvent.isJavaEnabled()).toString()).append(QUERY_NAME_VALUE_PAIR_DELIMITER); //add WT.jo as a query parameter
							//WT.js
							final Product jsProduct = guisePlatform.getJavaScriptProduct(); //get the JavaScript product
							//WT.jv
							ELFF.appendURIQueryParameter(queryParametersStringBuilder, JAVASCRIPT_QUERY_ATTRIBUTE_NAME, WebTrendsYesNo.asYesNo(jsProduct != null).toString())
									.append(QUERY_NAME_VALUE_PAIR_DELIMITER); //add WT.js as a query parameter
							if(jsProduct != null) { //if JavaScript is supported
								final String jsVersion = jsProduct.getVersion(); //get the JavaScript version, if any
								if(jsVersion != null) { //if we know the JavaScript version
									ELFF.appendURIQueryParameter(queryParametersStringBuilder, JAVASCRIPT_VERSION_QUERY_ATTRIBUTE_NAME, jsVersion)
											.append(QUERY_NAME_VALUE_PAIR_DELIMITER); //add WT.jv as a query parameter
								}
							}
							//TODO add WT.sp, if needed
							//WT.sr
							ELFF.appendURIQueryParameter(queryParametersStringBuilder, SCREEN_RESOLUTION_QUERY_ATTRIBUTE_NAME,
									Integer.toString(initControlEvent.getScreenWidth()) + "x" + Integer.toString(initControlEvent.getScreenHeight()))
									.append(QUERY_NAME_VALUE_PAIR_DELIMITER); //add WT.sr as a query parameter
							//WT.ti
							final String title = destinationComponent.getLabel(); //get the title of the page, if there is a title
							if(title != null) { //if there is a title
								ELFF.appendURIQueryParameter(queryParametersStringBuilder, TITLE_QUERY_ATTRIBUTE_NAME, guiseSession.dereferenceString(title))
										.append(QUERY_NAME_VALUE_PAIR_DELIMITER); //add WT.ti as a query parameter
							}
							//WT.tz
							ELFF.appendURIQueryParameter(queryParametersStringBuilder, TIMEZONE_QUERY_ATTRIBUTE_NAME,
									Integer.toString(initControlEvent.getUTCOffset() / (60 * 60 * 1000))).append(QUERY_NAME_VALUE_PAIR_DELIMITER); //add WT.tz as a query parameter
							//WT.ul
							ELFF.appendURIQueryParameter(queryParametersStringBuilder, USER_LANGUAGE_QUERY_ATTRIBUTE_NAME, initControlEvent.getLanguage())
									.append(QUERY_NAME_VALUE_PAIR_DELIMITER); //add WT.ul as a query parameter
							//content groups and subgroups
							final List<String> destinationCategoryIDs = new ArrayList<String>(); //we'll look for all the categories available
							final List<String> destinationSubcategoryIDs = new ArrayList<String>(); //we'll look for all the subcategories available, in whatever category (because WebTrends doesn't distinguish among categories for subcategories)
							for(final Category category : componentDestination.getCategories()) { //look at each category
								//TODO del									Log.trace("destination has category", category.getID());
								final String categoryID = category.getID(); //get this category's ID
								if(!destinationCategoryIDs.contains(categoryID)) { //if this category hasn't yet been added TODO use an array set
									destinationCategoryIDs.add(categoryID); //note this category's ID
								}
								for(final Category subcategory : category.getCategories()) { //look at each subcategory
									//TODO del										Log.trace("category has subcategory", subcategory.getID());
									final String subcategoryID = subcategory.getID(); //get this subcategory's ID
									if(!destinationSubcategoryIDs.contains(subcategoryID)) { //if this subcategory hasn't yet been added TODO use an array set
										destinationSubcategoryIDs.add(subcategoryID); //note this subcategory's ID (ignore all sub-subcategories, as WebTrends doesn't support them)
									}
								}
							}
							//WT.cg_n
							if(!destinationCategoryIDs.isEmpty()) { //if there are destination categories
								/*TODO fix
								TODO: find out why sometimes ELFF can't be loaded because the application isn't installed into the container
								*/
								ELFF.appendURIQueryParameter(queryParametersStringBuilder, CONTENT_GROUP_NAME_QUERY_ATTRIBUTE_NAME,
										destinationCategoryIDs.toArray(new String[destinationCategoryIDs.size()])).append(QUERY_NAME_VALUE_PAIR_DELIMITER); //add WT.cg_n as a query parameter
								//WT.cg_s
								if(!destinationSubcategoryIDs.isEmpty()) { //if there are destination subcategories (there cannot be subcategories without categories, and moreover WebTrends documentation does not indicate that subcategories are allowed without categories)
									ELFF.appendURIQueryParameter(queryParametersStringBuilder, CONTENT_SUBGROUP_NAME_QUERY_ATTRIBUTE_NAME,
											destinationSubcategoryIDs.toArray(new String[destinationSubcategoryIDs.size()])).append(QUERY_NAME_VALUE_PAIR_DELIMITER); //add WT.cg_s as a query parameter
								}
							}
							queryParametersStringBuilder.delete(queryParametersStringBuilder.length() - 1, queryParametersStringBuilder.length()); //remove the last parameter delimiter
							//TODO del Log.trace("ready to log query:", queryParametersStringBuilder);
							//TODO del when works								final NameValuePair<String, String>[] queryParameterArray=(NameValuePair<String, String>[])queryParameters.toArray(new NameValuePair[queryParameters.size()]);	//put the query parameters into an array
							//TODO del when works								entry.setFieldValue(Field.CLIENT_SERVER_URI_QUERY_FIELD, appendQueryParameters(request.getQueryString(), queryParameterArray));	//append the new parameters and set the log field
							entry.setFieldValue(Field.CLIENT_SERVER_URI_QUERY_FIELD, queryParametersStringBuilder.toString()); //set the log field to be the parameters we determined
							//TODO del entry.setFieldValue(Field.CLIENT_SERVER_URI_QUERY_FIELD, request.getQueryString());

							//							TODO fix				entry.setFieldValue(Field.CLIENT_SERVER_URI_QUERY_FIELD, request.getQueryString());
							entry.setFieldValue(Field.SERVER_CLIENT_STATUS_FIELD, 200); //TODO fix with real HTTP status
							//							TODO fix cs-status
							//							TODO fix cs-bytes
							//							TODO fix cs-version
							entry.setFieldValue(Field.CLIENT_SERVER_USER_AGENT_HEADER_FIELD, getUserAgent(request));
							final String webTrendsID = asInstance(guisePlatform.getEnvironment().getProperty(WEBTRENDS_ID_COOKIE_NAME), String.class).orElse(null); //get the WebTrends ID
							entry.setFieldValue(Field.CLIENT_SERVER_COOKIE_HEADER_FIELD, webTrendsID != null ? WEBTRENDS_ID_COOKIE_NAME + "=" + webTrendsID : null); //store the WebTrends ID cookie as the cookie TODO decide if we want to get general cookies instead of just the WebTrends cookie
							final URI referrerURI = initControlEvent.getReferrerURI(); //get the initialization referrer URI
							entry.setFieldValue(Field.CLIENT_SERVER_REFERER_HEADER_FIELD, referrerURI != null ? referrerURI.toString() : null); //store the referrer URI, if any
							entry.setFieldValue(Field.DCS_ID_FIELD, guiseApplication.getDCSID()); //get the DCS ID from the application, if there is a DCS ID
							//log this page
							final Writer elffWriter = guiseApplication.getLogWriter("elff.log", elffWriterInitializer, elffWriterUninitializer); //get the ELFF log writer for this application TODO use a constant
							elffWriter.write(getELFF().serializeEntry(entry)); //serialize the ELFF entry to the ELFF writer
							elffWriter.flush(); //flush the ELFF writer
							final WebPlatform platform = (WebPlatform)guiseSession.getPlatform(); //get the current platform
							final int pollInterval = platform.getPollInterval(); //get the current polling interval
							final Queue<WebPlatformMessage> sendMessageQueue = platform.getSendMessageQueue(); //get the queue for sending messages
							sendMessageQueue.add(new WebCommandMessage<PollCommand>(PollCommand.POLL_INTERVAL,
									new NameValuePair<String, Object>(PollCommand.INTERVAL_PROPERTY, Integer.valueOf(pollInterval)))); //send a poll command to the platform with the new interval
						}
						if(!requestedComponents.isEmpty()) { //if components were requested
							for(final Component component : requestedComponents) { //for each requested component
								getLogger().trace("ready to process event {} for component {}", controlEvent, component);
								component.processEvent(controlEvent); //tell the component to process the event
							}
						}
					} else if(requestEvent instanceof InputEvent) { //if this is an input event
						applicationFrame.dispatchInputEvent((InputEvent)requestEvent); //tell the application frame to dispatch the input event
					}
					/*TODO del
					Log.trace("we now have affected components:", affectedComponents.size());
					for(final Component<?> affectedComponent:affectedComponents)
					{
						Log.trace("affected component:", affectedComponent);
					}
					*/
					//send the resource if needed
					final URI sendResourceURI = guisePlatform.getSendResourceURI(); //see if there is a resource to send back
					if(sendResourceURI != null) { //if there is a resource to send back
						depictContext.writeElementBegin(null, "navigate"); //<navigate>	//TODO use a constant
						depictContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString()); //xmlns:guise="https://guise.io/framework/id/ml#"
						depictContext.writeAttribute(null, "viewportID", SEND_RESOURCE_VIEWPORT_ID); //specify the viewport ID for sending resources
						//append the "guiseContentDisposition=attachment" query parameter to the URI
						final URI sendResourceAttachmentURI = appendQueryParameter(sendResourceURI, GUISE_CONTENT_DISPOSITION_URI_QUERY_PARAMETER,
								getSerializationName(ContentDispositionType.ATTACHMENT));
						depictContext.write(depictContext.getDepictionURI(sendResourceAttachmentURI).toString()); //write the depict URI of the resource to send
						depictContext.writeElementEnd(null, "navigate"); //</navigate>
						guisePlatform.clearSendResourceURI(); //clear the address of the resource to send so that we won't send it again
					}
					final URI requestDepictionURI = guiseRequest.getDepictURI(); //get the request URI
					final Bookmark newBookmark = guiseSession.getBookmark(); //see if the bookmark has changed
					//TODO del Log.trace("navigation bookmark:", navigationBookmark, "new bookmark", newBookmark);
					final Navigation requestedNavigation = guiseSession.getRequestedNavigation(); //get the requested navigation
					//Log.trace("requested navigation:", requestedNavigation);
					if(requestedNavigation != null || !Objects.equals(navigationBookmark, newBookmark)) { //if navigation is requested or the bookmark has changed, redirect the browser
						final URI redirectNavigationURI; //we'll determine where to direct to in navigation terms; this may not be an absolute URI
						if(requestedNavigation != null) { //if navigation is requested
							final URI requestedNavigationURI = requestedNavigation.getNewNavigationURI();
							//Log.trace("navigation requested to", requestedNavigationURI);
							guiseSession.clearRequestedNavigation(); //remove any navigation requests
							/*TODO remove modal navigation
														if(requestedNavigation instanceof ModalNavigation) {	//if modal navigation was requested
															beginModalNavigation(guiseApplication, guiseSession, (ModalNavigation)requestedNavigation);	//begin the modal navigation
														}
							*/
							redirectNavigationURI = requestedNavigationURI; //we already have the destination URI
						} else { //if navigation is not requested, request a navigation to the new bookmark location
							redirectNavigationURI = appendRawQuery(navigationPath.toURI(), newBookmark.toString()); //save the constructed bookmark URI	TODO fix the confusion about whether there is a query on the URIs
						}
						final URI redirectDepictionURI = resolve(requestDepictionURI,
								guiseApplication.getDepictionURI(getPlainURI(resolve(requestDepictionURI, ROOT_PATH)), redirectNavigationURI)); //get the absolute redirect URI in depiction terms
						//Log.trace("depict version of requested navigation:", redirectDepictURI);
						if(!requestDepictionURI.equals(redirectDepictionURI)) { //if the navigation is really changing (i.e. they didn't request to go to where they already were)
							if(isAJAX) { //if this is an AJAX request
								isNavigating = true; //show that we're going to navigate; process the other events to make sure the data model is up-to-date (and in case the navigation gets overridden)
								depictContext.clearDepictText(); //clear all the response data (which at this point should only be navigation information, anyway) TODO improve; this will discard any resources to send
								depictContext.writeElementBegin(null, "navigate"); //<navigate>	//TODO use a constant
								depictContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString()); //xmlns:guise="https://guise.io/framework/id/ml#"
								if(requestedNavigation != null) { //if navigation was requested (i.e. this isn't just a bookmark registration)
									final String viewportID = requestedNavigation.getViewportID(); //get the requested viewport ID
									if(viewportID != null) { //if a viewport was requested
										depictContext.writeAttribute(null, "viewportID", viewportID); //specify the viewport ID TODO use a constant
										isNavigating = false; //don't consider a viewport-specific navigation to be true navigation, as we still want the main page to be updated (e.g. closed frames still need to be removed)
									}
								}
								//Log.trace("telling AJAX to redirect to:", redirectDepictURI);
								depictContext.write(redirectDepictionURI.toString()); //write the redirect URI
								depictContext.writeElementEnd(null, "navigate"); //</navigate>
							} else { //if this is not an AJAX request
								//Log.trace("HTTP redirecting to:", redirectDepictURI);
								throw new HTTPMovedTemporarilyException(redirectDepictionURI); //redirect to the new location TODO fix to work with other viewports
							}
							//TODO if !AJAX						throw new HTTPMovedTemporarilyException(requestedNavigationURI);	//redirect to the new navigation location
							//TODO store a flag or something---if we're navigating, we probably should flush the other queued events
						}
					}
					if(!isNavigating && !Objects.equals(oldPrincipal, guiseSession.getPrincipal())) { //if the principal has changed after updating the model (if we're navigating there's no need to reload)
						if(!isNavigating) { //if we're not navigating to a new location, fire a navigation event anyway to indicate that the principal has changed
							guiseSession.fireNavigated(navigationPath.toURI()); //tell the session that navigation has essentially occurred again from the same URI so that it can update things based upon the new principal TODO decide whether this is in depict or logical space
						}
						if(isAJAX) { //if this is an AJAX request
							depictContext.clearDepictText(); //clear all the response data (which at this point should only be navigation information, anyway)
							//Log.traceStack("ready to reload");
							depictContext.writeElementBegin(null, "reload", true); //<reload>	//TODO use a constant
							depictContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString()); //xmlns:guise="https://guise.io/framework/id/ml#"
							depictContext.writeElementEnd(null, "reload"); //</reload>
							isNavigating = true; //show that we're navigating, so there's no need to update views
						} else { //if this is not an AJAX request
							throw new HTTPMovedTemporarilyException(depictContext.getDepictionURI()); //redirect to the same page with the same query, which will generate a new request with no POST parameters, which would likely change the principal again)
						}
					}
				} catch(final RuntimeException runtimeException) { //if we run into any errors processing events
					if(isAJAX) { //if this is an AJAX request
						getLogger().error("AJAX error", runtimeException); //log the error
						//TODO send back the error
					} else { //if this is an AJAX request
						throw runtimeException; //pass the error back to the servlet TODO improve; pass to Guise
					}
				}

				if(isAJAX && !isNavigating && requestEvent instanceof WebInitializeEvent) { //if this is an AJAX initialization event (if we're navigating, there's no need to initialize this page) TODO maybe just dirty all the frames so this happens automatically
					//close all the flyover frames to get rid of stuck flyover frames, such as those left from refreshing the page during flyover TODO fix; this is a workaround to keep refreshing the page from leaving stuck flyover frames; maybe do something better
					final Iterator<Frame> flyoverFrameIterator = guiseSession.getApplicationFrame().getChildFrames().iterator(); //get an iterator to all the frames
					while(flyoverFrameIterator.hasNext()) { //while there are more frames
						final Frame frame = flyoverFrameIterator.next(); //get the next frame
						if(frame instanceof FlyoverFrame) { //if this is a flyover frame
							frame.close(); //close all flyover frames
						}
					}
					//send back any open frames
					final Iterator<Frame> frameIterator = guiseSession.getApplicationFrame().getChildFrames().iterator(); //get an iterator to all the frames
					if(frameIterator.hasNext()) { //if there are open frames
						depictContext.writeElementBegin(XHTML_NAMESPACE_URI, "patch"); //<xhtml:patch>	//TODO use a constant TODO don't use the XHTML namespace if we can help it
						depictContext.writeAttribute(null, ATTRIBUTE_XMLNS.getLocalName(), XHTML_NAMESPACE_URI.toString()); //xmlns="http://www.w3.org/1999/xhtml"; note that we pass the wrong namespace to get the effectively correct prefix
						depictContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString()); //xmlns:guise="https://guise.io/framework/id/ml#"
						do {
							final Frame frame = frameIterator.next(); //get the next frame
							if(frame != guiseSession.getApplicationFrame()) { //don't send back the application frame
								//							TODO fix							else	//if the component is not visible, remove the component's elements
								frame.updateTheme(); //make sure a theme has been applied to this frame
								frame.depict(); //tell the component to update its view
							}
						} while(frameIterator.hasNext()); //keep sending back frames as long as there are more frames
						depictContext.writeElementEnd(XHTML_NAMESPACE_URI, "patch"); //</xhtml:patch>
					}
				}
			}

			//TODO move this to the bottom of the processing, as cookies only need to be updated before they go back
			synchronizeCookies(guiseRequest.getHTTPServletRequest(), response, guiseSession); //synchronize the cookies going out in the response; do this before anything is written back to the client

			if(!isNavigating) { //we'll only update the views if we're not navigating (if we're navigating, we're changing pages, anyway)
				if(isAJAX) { //if this is an AJAX request
					final Collection<Component> dirtyComponents = AbstractComponent.getDirtyComponents(guiseSession.getApplicationFrame()); //get all dirty components

					Collections.removeAll(removedFrames, guiseSession.getApplicationFrame().getChildFrames().iterator()); //remove all the ending frames, leaving us the frames that were removed TODO improve all this
					//TODO fix					dirtyComponents.addAll(frames);	//add all the frames that were removed

					getLogger().trace("we now have dirty components: {}", dirtyComponents.size());
					for(final Component affectedComponent : dirtyComponents) {
						getLogger().trace("affected component: {}", affectedComponent);
					}
					if(dirtyComponents.contains(applicationFrame)) { //if the application frame itself was affected, we might as well reload the page
						//TODO del Log.trace("dirty because:", CollectionUtilities.toString(((AbstractDepictor)applicationFrame.getDepictor()).getModifiedProperties(), ','));
						//Log.traceStack("ready to reload");
						depictContext.writeElementBegin(null, "reload", true); //<reload>	//TODO use a constant
						depictContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString()); //xmlns:guise="https://guise.io/framework/id/ml#"
						depictContext.writeElementEnd(null, "reload"); //</reload>
					} else { //if the application frame wasn't affected
						if(!dirtyComponents.isEmpty()) { //if components were affected by this update cycle
							depictContext.writeElementBegin(XHTML_NAMESPACE_URI, "patch"); //<xhtml:patch>	//TODO use a constant TODO don't use the XHTML namespace if we can help it
							depictContext.writeAttribute(null, ATTRIBUTE_XMLNS.getLocalName(), XHTML_NAMESPACE_URI.toString()); //xmlns="http://www.w3.org/1999/xhtml"; note that we pass the wrong namespace to get the effectively correct prefix
							depictContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString()); //xmlns:guise="https://guise.io/framework/id/ml#"
							for(final Component dirtyComponent : dirtyComponents) { //for each component affected by this update cycle
								//TODO fix							if(dirtyComponent.isVisible())	//if the component is visible
								//TODO fix							else	//if the component is not visible, remove the component's elements
								dirtyComponent.updateTheme(); //make sure a theme has been applied to this component
								dirtyComponent.depict(); //tell the component to update its view
							}
							depictContext.writeElementEnd(XHTML_NAMESPACE_URI, "patch"); //</xhtml:patch>
						}
						for(final Frame frame : removedFrames) { //for each removed frame
							depictContext.writeElementBegin(XHTML_NAMESPACE_URI, "remove"); //<xhtml:remove>	//TODO use a constant TODO don't use the XHTML namespace if we can help it
							depictContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString()); //xmlns:guise="https://guise.io/framework/id/ml#"
							depictContext.writeAttribute(null, "id", guisePlatform.getDepictIDString(frame.getDepictID())); //TODO fix
							depictContext.writeElementEnd(XHTML_NAMESPACE_URI, "remove"); //</xhtml:remove>
						}
					}
					//send any platform events
					final WebPlatform platform = (WebPlatform)guiseSession.getPlatform(); //get the current platform
					final Queue<WebPlatformMessage> sendMessageQueue = platform.getSendMessageQueue(); //get the queue for sending messages
					WebPlatformMessage webPlatformMessage = sendMessageQueue.poll(); //get any message to send to the platform
					while(webPlatformMessage != null) { //while there are messages to send to the platform
						if(webPlatformMessage instanceof WebPlatformCommandMessage) { //if this is a web command message
							final WebPlatformCommandMessage<?> webCommandMessage = (WebPlatformCommandMessage<?>)webPlatformMessage; //get the web command
							depictContext.writeElementBegin(XHTML_NAMESPACE_URI, "command"); //<xhtml:command>	//TODO use a constant TODO don't use the XHTML namespace if we can help it
							depictContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString()); //xmlns:guise="https://guise.io/framework/id/ml#"
							if(webCommandMessage instanceof WebDepictEvent) { //if this is a depict message
								final WebDepictEvent webDepictEvent = (WebDepictEvent)webCommandMessage; //get the depict event
								depictContext.writeAttribute(null, "objectID", platform.getDepictIDString(webDepictEvent.getDepictedObject().getDepictID())); //objectID="depictedObjectID" TODO use a constant
							}
							depictContext.writeAttribute(null, "command", getSerializationName(webCommandMessage.getCommand())); //command="webCommand" TODO use a constant
							/*TODO del if not needed
														final Map<String, Object> depictParameters=new HashMap<String, Object>(webCommandMessage.getParameters().size());	//create a new map of depiction parameters
														for(final Map.Entry<String, Object> parameterEntry:webCommandMessage.getParameters().entrySet()) {	//look at all the parameters
															Object value=parameterEntry.getValue();	//get the parameter value
															if(value instanceof URI) {	//if this is a URI
																value=depictContext.getDepictURI((URI)value);	//get the depict URI
															}
															depictParameters.put(parameterEntry.getKey(), value);	//store the parameter in the depict parameter map
														}
														depictContext.write(JSON.serialize(depictParameters));	//{parameters...}
							*/
							depictContext.write(JSON.serialize(webCommandMessage.getParameters())); //{parameters...}
							depictContext.writeElementEnd(XHTML_NAMESPACE_URI, "command"); //</xhtml:command>
						}
						webPlatformMessage = sendMessageQueue.poll(); //get the next event to send to the platform
					}
				} else { //if this is not an AJAX request
					applicationFrame.updateTheme(); //make sure a theme has been applied to the application frame
					applicationFrame.depict(); //tell the application frame to update its view
				}
			}

			String text = depictContext.getDepictText(); //get the text to output
			if(isAJAX) { //if this is an AJAX request
				depictContext.setOutputContentType(XML.MEDIA_TYPE); //switch to the "text/xml" content type TODO verify UTF-8 in a consistent, elegant way
				text = "<response>" + text + "</response>"; //wrap the text in a response element
			}
			//			Log.debug("response length:", text.length());
			//			Log.debug("response text:", text);
			final byte[] bytes = text.getBytes(UTF_8); //write the content we collected in the context as series of bytes encoded in UTF-8
			final OutputStream outputStream = getCompressedOutputStream(guiseRequest.getHTTPServletRequest(), response); //get a compressed output stream, if possible
			outputStream.write(bytes); //write the bytes
			outputStream.close(); //close the output stream, finishing writing the compressed contents (don't put this in a finally block, as it will attempt to write more data and raise another exception)
		} finally {
			guisePlatform.setDepictContext(null); //remove the depict context from this platform
			guisePlatform.getDepictLock().unlock(); //always release the platform depict lock
		}
	}

	/**
	 * Processes a redirect from a redirect destination. This method will unconditionally throw an exception. Under normal circumstances, an
	 * {@link HTTPRedirectException} will be thrown.
	 * @param requestURI The requested URI.
	 * @param bookmark The requested bookmark, or <code>null</code> if no bookmark is requested
	 * @param guiseApplication The Guise application.
	 * @param redirectDestination The destination indicating how and to where redirection should occur.
	 * @throws IllegalArgumentException if the referenced destination does not specify a path (instead specifying a path pattern, for example).
	 * @throws HTTPRedirectException unconditionally to indicate how and to where redirection should occur.
	 */
	/*TODO del when works
		protected void redirect(final URI requestURI, final Bookmark bookmark, final GuiseApplication guiseApplication, final RedirectDestination redirectDestination) throws HTTPRedirectException
		{
			final URIPath redirectPath;	//the path to which direction should occur
			if(redirectDestination instanceof ReferenceDestination) {	//if the destination references another destination
				redirectPath=((ReferenceDestination)redirectDestination).getDestination().getPath();	//get the path of the referenced destination TODO what if the referenced destination is itself a redirect? should we support that, too? probably
				if(redirectPath==null) {	//if there is no redirect path
					throw new IllegalArgumentException("Redirect destination "+redirectDestination+" does not have a valid path.");
				}
			}
			else {	//we don't yet support non-reference redirects
				throw new AssertionError("Unsupported redirect destination type "+redirectDestination.getClass().getName());
			}
			if(redirectDestination instanceof TemporaryRedirectDestination) {	//if this is a temporary redirect
				redirect(requestURI, bookmark, redirectPath, guiseApplication, false);	//redirect temporarily
			}
			else if(redirectDestination instanceof PermanentRedirectDestination) {	//if this is a permanent redirect
				redirect(requestURI, bookmark, redirectPath, guiseApplication, true);	//redirect permanently
			}
			else {	//if we don't recognize the type of redirect
				throw new AssertionError("Unsupported redirect destination type "+redirectDestination.getClass().getName());
			}
		}
	*/

	/**
	 * Processes a redirect from a redirect destination. This method will unconditionally throw an exception. Under normal circumstances, an
	 * {@link HTTPRedirectException} will be thrown.
	 * @param guiseRequest The Guise request information.
	 * @param guiseApplication The Guise application.
	 * @param redirectDestination The destination indicating how and to where redirection should occur.
	 * @param bookmark The requested bookmark, or <code>null</code> if no bookmark is requested
	 * @throws IllegalArgumentException if the referenced destination does not specify a path (instead specifying a path pattern, for example).
	 * @throws HTTPRedirectException unconditionally to indicate how and to where redirection should occur.
	 */
	protected void redirect(final HTTPServletGuiseRequest guiseRequest, final GuiseApplication guiseApplication, final RedirectDestination redirectDestination,
			final Bookmark bookmark) throws HTTPRedirectException {
		final URIPath redirectPath; //the path to which direction should occur
		if(redirectDestination instanceof ReferenceDestination) { //if the destination references another destination
			redirectPath = ((ReferenceDestination)redirectDestination).getDestination().getPath(); //get the path of the referenced destination TODO what if the referenced destination is itself a redirect? should we support that, too? probably
			if(redirectPath == null) { //if there is no redirect path
				throw new IllegalArgumentException("Redirect destination " + redirectDestination + " does not have a valid path.");
			}
		} else { //we don't yet support non-reference redirects
			throw new AssertionError("Unsupported redirect destination type " + redirectDestination.getClass().getName());
		}
		if(redirectDestination instanceof TemporaryRedirectDestination) { //if this is a temporary redirect
			redirect(guiseRequest, guiseApplication, redirectPath.toURI(), bookmark, false); //redirect temporarily
		} else if(redirectDestination instanceof PermanentRedirectDestination) { //if this is a permanent redirect
			redirect(guiseRequest, guiseApplication, redirectPath.toURI(), bookmark, true); //redirect permanently
		} else { //if we don't recognize the type of redirect
			throw new AssertionError("Unsupported redirect destination type " + redirectDestination.getClass().getName());
		}
	}

	/**
	 * Redirects to the given navigation path, preserving the given bookmark. This method will unconditionally throw an exception. Under normal circumstances, an
	 * {@link HTTPRedirectException} will be thrown.
	 * @param requestURI The requested URI.
	 * @param redirectPath The application-relative path to which redirection should occur.
	 * @param bookmark The requested bookmark, or <code>null</code> if no bookmark is requested
	 * @param guiseApplication The Guise application.
	 * @param permanent <code>true</code> if the redirect should be permanent.
	 * @throws HTTPRedirectException unconditionally to indicate how and to where redirection should occur.
	 */
	/*TODO del when works
		protected void redirect(final URI requestURI, final Bookmark bookmark, final URIPath redirectPath, final GuiseApplication guiseApplication, final boolean permanent) throws HTTPRedirectException
		{
			URI redirectURI=resolve(requestURI, guiseApplication.resolvePath(redirectPath).toURI());	//resolve the path to the application and resolve that against the request URI
			if(bookmark!=null) {	//if a bookmark was given
				redirectURI=appendRawQuery(redirectURI, bookmark.toString().substring(1));	//append the bookmark to the redirect URI TODO use a better way of extracting the bookmark query information
			}
			if(permanent) {	//if this is a permanent redirect
				throw new HTTPMovedPermanentlyException(redirectURI);	//redirect permanently
			}
			else {	//if this is a temporary redirect
				throw new HTTPMovedTemporarilyException(redirectURI);	//redirect temporarily
			}
		}
	*/

	/**
	 * Redirects to the given navigation path, preserving the given bookmark. This method will unconditionally throw an exception. Under normal circumstances, an
	 * {@link HTTPRedirectException} will be thrown.
	 * @param guiseRequest The Guise request information.
	 * @param guiseApplication The Guise application.
	 * @param redirectNavigationURI The absolute or application-relative URI to which redirection should occur in navigation space.
	 * @param bookmark The requested bookmark, or <code>null</code> if no bookmark is requested
	 * @param permanent <code>true</code> if the redirect should be permanent.
	 * @throws HTTPRedirectException unconditionally to indicate how and to where redirection should occur.
	 */
	protected void redirect(final HTTPServletGuiseRequest guiseRequest, final GuiseApplication guiseApplication, final URI redirectNavigationURI,
			final Bookmark bookmark, final boolean permanent) throws HTTPRedirectException {
		final URI requestDepictionURI = guiseRequest.getDepictURI(); //get the request depict URI
		URI redirectDepictionURI = resolve(requestDepictionURI,
				guiseApplication.getDepictionURI(getPlainURI(resolve(requestDepictionURI, ROOT_PATH)), redirectNavigationURI)); //convert the redirect URI to a depict URI, resolve it to the application, and resolve it to the original depict URI
		if(bookmark != null) { //if a bookmark was given
			redirectDepictionURI = appendRawQuery(redirectDepictionURI, bookmark.toString().substring(1)); //append the bookmark to the redirect URI TODO use a better way of extracting the bookmark query information
		}
		if(permanent) { //if this is a permanent redirect
			throw new HTTPMovedPermanentlyException(redirectDepictionURI); //redirect permanently
		} else { //if this is a temporary redirect
			throw new HTTPMovedTemporarilyException(redirectDepictionURI); //redirect temporarily
		}
	}

	/**
	 * Synchronizes the cookies in a request with the environment properties in a Guise session. Any cookies missing from the request will be added from the
	 * environment to the response.
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 * @param guiseSession The Guise session.
	 */
	protected void synchronizeCookies(final HttpServletRequest request, final HttpServletResponse response, final GuiseSession guiseSession) {
		//remove unneeded cookies from the request
		final URIPath applicationBasePath = guiseSession.getApplication().getBasePath(); //get the application's base path
		assert applicationBasePath != null : "Application not yet installed during cookie synchronization.";
		final String applicationBasePathString = applicationBasePath.toString(); //we'll need the string version of the base path for later
		final Environment environment = guiseSession.getPlatform().getEnvironment(); //get the platform's environment
		final Cookie[] cookies = request.getCookies(); //get the cookies in the request
		final Map<String, Cookie> cookieMap = new HashMap<String, Cookie>(cookies != null ? cookies.length : 0); //create a map to hold the cookies for quick lookup
		if(cookies != null) { //if a cookie array was returned
			for(final Cookie cookie : cookies) { //for each cookie in the request
				final String cookieName = cookie.getName(); //get the name of this cookie
				if(!SESSION_ID_COOKIE_NAME.equals(cookieName)) { //ignore the session ID
					asInstance(environment.getProperty(cookieName), String.class).ifPresentOrElse(environmentPropertyValue -> { //see if there is a string environment property value for this cookie's name
						if(!Objects.equals(cookie.getValue(), encode(environmentPropertyValue))) { //if the cookie's value doesn't match the encoded environment property value
							cookie.setValue(encode(environmentPropertyValue)); //update the cookie's value, making sure the value is encoded
							response.addCookie(cookie); //add the cookie to the response to change its value
						}
					}, () -> { //if there is no such environment property, remove the cookie
						cookie.setValue(null); //remove the value now
						cookie.setPath(applicationBasePathString); //set the cookie path to the application base path, because we'll need the same base path as the one that was set
						cookie.setMaxAge(0); //tell the cookie to expire immediately
						response.addCookie(cookie); //add the cookie to the response to delete it
					});
					cookieMap.put(cookieName, cookie); //store the cookie in the map to show that there's no need to copy over the environment variable
				}
			}
		}
		//add new cookies from the environment to the response
		for(final Map.Entry<String, Object> environmentPropertyEntry : environment.getProperties().entrySet()) { //iterate the environment properties so that new cookies can be added as needed
			final String environmentPropertyName = environmentPropertyEntry.getKey(); //get the name of the environment property value
			if(!cookieMap.containsKey(environmentPropertyName)) { //if no cookie contains this environment variable
				asInstance(environmentPropertyEntry.getValue(), String.class).ifPresent(environmentPropertyValue -> { //get the environment property value as a string; if there is a property value
					final Cookie cookie = new Cookie(environmentPropertyName, encode(environmentPropertyValue)); //create a new cookie with the encoded property value
					cookie.setPath(applicationBasePathString); //set the cookie path to the application base path
					cookie.setMaxAge(Integer.MAX_VALUE); //don't allow the cookie to expire for a very long time
					response.addCookie(cookie); //add the cookie to the response
				});
			}
		}
	}

	@Override
	protected void serveResource(final HttpServletRequest request, final HttpServletResponse response, final HTTPServletResource resource,
			final boolean serveContent) throws ServletException, IOException {
		if(resource instanceof DestinationResource) { //if the resource is a destination we're reading from
			//Log.trace("ready to serve destination resource", request.getRequestURI(), "with query", request.getQueryString());
			final DestinationResource destinationResource = (DestinationResource)resource; //get the desetination resource
			//check for a content-disposition indication
			final String queryString = request.getQueryString(); //get the query string from the request
			if(queryString != null && queryString.length() > 0) { //if there is a query string (Tomcat 5.5.16 returns an empty string for no query, even though the Java Servlet specification 2.4 says that it should return null; this is fixed in Tomcat 6)
				final NameValuePair<String, String>[] parameters = getParameters(queryString); //get the parameters from the query
				for(final NameValuePair<String, String> parameter : parameters) { //look at each parameter
					if(GUISE_CONTENT_DISPOSITION_URI_QUERY_PARAMETER.equals(parameter.getName())) { //if this is a content-disposition parameter
						final ContentDispositionType contentDispositionType = getSerializedEnum(ContentDispositionType.class, parameter.getValue()); //get the content disposition type
						if(contentDispositionType == ContentDispositionType.ATTACHMENT) { //if the content should be sent back as an attachment
							//try to turn off caching; if caching is not turned off, Firefox on subsequent requests to the same URL will not send a request to the server
							//but if caching is turned on for IE, the browser will try to download the HTML page instead; this seems to still happen on IE 7.0.5730.11; see http://support.microsoft.com/kb/279667
							final Map<String, Object> userAgentProperties = getUserAgentProperties(request); //get the user agent properties for this request
							if(!USER_AGENT_NAME_MSIE.equals(userAgentProperties.get(USER_AGENT_NAME_PROPERTY))) { //if this is not IE, set Cache-Control: no-cache
								setNoCache(request, response); //turn off caching for downloads
							}
						}
						//TODO why was this originally destinationResource.getResourceDescription().getURI()? was that a mistake, confusing the description with the resource
						final URI referenceURI = destinationResource.getURI(); //get the reference URI of the description, if any
						//Log.trace("setting content disposition, reference URI", referenceURI);
						setContentDisposition(response, contentDispositionType, referenceURI != null ? findName(referenceURI).orElse(null) : null); //set the response content disposition, suggesting the resource's decoded name if we can
					}
				}
			}
			/*TODO fix for DCMI description and/or URF description
			final String description = getDescription(destinationResource.getResourceDescription()); //get the dc.description
			if(description != null) { //if this resource provides a description
				setContentDescription(response, description); //resport the description back to the client
			}
			*/
		}
		super.serveResource(request, response, resource, serveContent); //serve the resource normally
	}

	/**
	 * Retrieves all descendant components, including the given component, that have a given depict name.
	 * @param component The component to check, along with all descendants, for components with the given depict name.
	 * @param depictName The name for which to check.
	 * @param componentSet The set of components collecting the components with the given depict name.
	 * @throws NullPointerException if the given depict name is <code>null</code>.
	 * @see WebComponentDepictor#getDepictName()
	 */
	protected void getComponentsByDepictName(final Component component, final String depictName, final Set<Component> componentSet) {
		if(depictName.equals(((WebComponentDepictor<?>)component.getDepictor()).getDepictName())) { //if the depictor for this component indicates the given depict name
			componentSet.add(component); //collect this component
		}
		if(component instanceof CompositeComponent) { //if this is a composite component
			for(final Component childComponent : ((CompositeComponent)component).getChildComponents()) { //for each child
				getComponentsByDepictName(childComponent, depictName, componentSet); //collect components from this child branch
			}
		}
	}

	private static final PathExpression AJAX_REQUEST_EVENTS_WILDCARD_XPATH_EXPRESSION = new PathExpression("request", "events", "*"); //TODO use constants; comment
	private static final PathExpression AJAX_REQUEST_CONTROL_XPATH_EXPRESSION = new PathExpression("control"); //TODO use constants; comment
	//TODO del	private final PathExpression AJAX_REQUEST_CONTROL_NAME_XPATH_EXPRESSION=new PathExpression("control", "name");	//TODO use constants; comment
	//TODO del	private final PathExpression AJAX_REQUEST_CONTROL_VALUE_XPATH_EXPRESSION=new PathExpression("control", "value");	//TODO use constants; comment
	private static final PathExpression AJAX_REQUEST_SOURCE_XPATH_EXPRESSION = new PathExpression("source"); //TODO use constants; comment
	private static final PathExpression AJAX_REQUEST_TARGET_XPATH_EXPRESSION = new PathExpression("target"); //TODO use constants; comment
	private static final PathExpression AJAX_REQUEST_COMPONENT_XPATH_EXPRESSION = new PathExpression("component"); //TODO use constants; comment
	private static final PathExpression AJAX_REQUEST_MOUSE_XPATH_EXPRESSION = new PathExpression("mouse"); //TODO use constants; comment
	private static final PathExpression AJAX_REQUEST_VIEWPORT_XPATH_EXPRESSION = new PathExpression("viewport"); //TODO use constants; comment

	/**
	 * Retrieves events from the HTTP request.
	 * @param guiseRequest The HTTP request.
	 * @param guiseSession The Guise session object.
	 * @param depictContext The platform depict object.
	 * @throws IOException if there is an error reading or writing data.
	 * @return The events from the HTTP request.
	 */
	protected List<GuiseEvent> getRequestEvents(/*TODO del final HttpServletRequest request,*/final HTTPServletGuiseRequest guiseRequest,
			final GuiseSession guiseSession, final DepictContext depictContext) throws IOException {
		getLogger().trace("getting request events");
		final WebPlatform platform = (WebPlatform)guiseSession.getPlatform(); //get the web platform
		final List<GuiseEvent> requestEventList = new ArrayList<GuiseEvent>(); //create a new list for storing request events
		/*TODO del
				final String contentTypeString=request.getContentType();	//get the request content type
				final ContentType contentType=contentTypeString!=null ? createContentType(contentTypeString) : null;	//create a content type object from the request content type, if there is one
		*/
		if(guiseRequest.isAJAX()) { //if this is a Guise AJAX request
			//		TODO del Log.trace("Guise AJAX request");
			try {
				final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance(); //create a document builder factory TODO create a shared document builder factory, maybe---but make sure it is used by only one thread
				final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder(); //create a new document builder
				final Document document = documentBuilder.parse(guiseRequest.getHTTPServletRequest().getInputStream()); //read the document from the request
				//Log.trace("request XML:", XMLUtilities.toString(document));
				@SuppressWarnings("unchecked")
				final List<Node> eventNodes = (List<Node>)XPathDom.evaluatePathExpression(document, AJAX_REQUEST_EVENTS_WILDCARD_XPATH_EXPRESSION); //get all the events
				for(final Node eventNode : eventNodes) { //for each event node
					if(eventNode.getNodeType() == Node.ELEMENT_NODE) { //if this is an event element
						final Element eventElement = (Element)eventNode; //cast the node to an element
						final String eventName = eventNode.getNodeName(); //get the event name
						final WebPlatformEventType eventType = getSerializedEnum(WebPlatformEventType.class, eventName); //get this event type, throwing an IllegalArgumentException if the event type is not recognized
						if(eventType != WebPlatformEventType.LOG) { //if this is not a log event (there's no use logging a log even)
							getLogger().debug("AJAX event: {}", eventType);
						}
						switch(eventType) { //see which type of event this is
							case ACTION:
								{
									final String depictedObjectID = eventElement.getAttribute("objectID"); //get the ID of the depicted object TODO use a constant
									if(depictedObjectID.length() > 0) { //if there is an object TODO add better event handling, to throw an error and send back that error
										final DepictedObject depictedObject = platform.getDepictedObject(platform.getDepictID(depictedObjectID)); //look up the depicted object
										if(depictedObject != null) { //if we know the depicted object
											final String targetID = eventElement.getAttribute("targetID"); //get the ID of the target element TODO use a constant
											final String actionID = eventElement.getAttribute("actionID"); //get the action identifier TODO use a constant
											final int option = Integer.parseInt(eventElement.getAttribute("option")); //TODO tidy; improve; check for errors; comment
											requestEventList.add(new WebActionDepictEvent(depictedObject, targetID, actionID, option)); //create and add the event to the list
										}
									}
								}
								break;
							case CHANGE:
								{
									final String depictedObjectID = eventElement.getAttribute("objectID"); //get the ID of the depicted object TODO use a constant
									if(depictedObjectID.length() > 0) { //if there is an object TODO add better event handling, to throw an error and send back that error
										final DepictedObject depictedObject = platform.getDepictedObject(platform.getDepictID(depictedObjectID)); //look up the depicted object
										if(depictedObject != null) { //if we know the depicted object
											final Map<String, Object> properties = new HashMap<String, Object>(); //create a map of properties
											final NodeList propertyElementList = eventElement.getElementsByTagName("property"); //get a list of property elements
											for(int propertyIndex = propertyElementList.getLength() - 1; propertyIndex >= 0; --propertyIndex) { //for each property element
												final Element propertyElement = (Element)propertyElementList.item(propertyIndex); //get this property element
												final String propertyName = propertyElement.getAttribute("name"); //get the name of the property TODO use a constant
												final Object propertyValue = JSON.parseValue(propertyElement.getTextContent()); //get the value of the property
												properties.put(propertyName, propertyValue); //add this property name and value to the event
											}
											requestEventList.add(new WebChangeDepictEvent(depictedObject, properties)); //create and add a change event to the list
										}
									}
								}
								break;
							case DROP:
								{
									final String dropTargetID = eventElement.getAttribute("objectID"); //get the ID of the depicted object TODO use a constant
									final String dragSourceID = eventElement.getAttribute("dragSourceID"); //get the ID of the drag source TODO use a constant
									if(dropTargetID.length() > 0 && dragSourceID.length() > 0) { //if there is a drag source and a drop target TODO add better event handling, to throw an error and send back that error
										final DepictedObject dragSource = platform.getDepictedObject(platform.getDepictID(dragSourceID)); //look up the drag srouce
										final DepictedObject dropTarget = platform.getDepictedObject(platform.getDepictID(dropTargetID)); //look up the drop target
										if(dragSource != null && dropTarget != null) { //if we know the drag source and the drop target
											requestEventList.add(new PlatformDropEvent(dragSource, dropTarget)); //create and add a drop event to the list
										}
									}
								}
								break;
							case FOCUS:
								{
									final String depictedObjectID = eventElement.getAttribute("objectID"); //get the ID of the depicted object TODO use a constant
									if(depictedObjectID.length() > 0) { //if there is an object TODO add better event handling, to throw an error and send back that error
										final DepictedObject depictedObject = platform.getDepictedObject(platform.getDepictID(depictedObjectID)); //look up the depicted object
										if(depictedObject != null) { //if we know the depicted object
											requestEventList.add(new PlatformFocusEvent(depictedObject)); //create and add a focus event to the list
										}
									}
								}
								break;
							case INIT:
								{
									final String hour = eventElement.getAttribute("hour");
									final String utcOffset = eventElement.getAttribute("utcOffset");
									final String utcOffset01 = eventElement.getAttribute("utcOffset01");
									final String utcOffset06 = eventElement.getAttribute("utcOffset06");
									final String timezone = eventElement.getAttribute("timezone");
									final String language = eventElement.getAttribute("language");
									final String colorDepth = eventElement.getAttribute("colorDepth");
									final String screenWidth = eventElement.getAttribute("screenWidth");
									final String screenHeight = eventElement.getAttribute("screenHeight");
									final String browserWidth = eventElement.getAttribute("browserWidth");
									final String browserHeight = eventElement.getAttribute("browserHeight");
									final String javascriptVersion = eventElement.getAttribute("javascriptVersion"); //get the JavaScript version TODO use a constant
									final String javaEnabled = eventElement.getAttribute("javaEnabled");
									final String referrer = eventElement.getAttribute("referrer");
									URI referrerURI = null; //assume we can't get a referrer URI
									if(referrer.length() > 0) { //if there is a referrer
										try {
											referrerURI = new URI(referrer); //create a URI object from the referrer string
										} catch(final URISyntaxException uriSyntaxException) { //if there is a problem with the URI syntax
											getLogger().warn("Invalid referrer URI syntax: {}" + referrer);
										}
									}
									final WebInitializeEvent initEvent = new WebInitializeEvent(depictContext,
											hour.length() > 0 ? Integer.parseInt(hour) : 0, /*TODO del timezone.length()>0 ? Integer.parseInt(timezone) : 0,*/
											utcOffset.length() > 0 ? Integer.parseInt(utcOffset) : 0, utcOffset.length() > 0 ? Integer.parseInt(utcOffset01) : 0,
											utcOffset06.length() > 0 ? Integer.parseInt(utcOffset06) : 0, language.length() > 0 ? language : "en-US",
											colorDepth.length() > 0 ? Integer.parseInt(colorDepth) : 24, screenWidth.length() > 0 ? Integer.parseInt(screenWidth) : 1024,
											screenHeight.length() > 0 ? Integer.parseInt(screenHeight) : 768, browserWidth.length() > 0 ? Integer.parseInt(browserWidth) : 1024,
											browserHeight.length() > 0 ? Integer.parseInt(browserHeight) : 768, javascriptVersion.length() > 0 ? javascriptVersion : null,
											javaEnabled.length() > 0 ? Boolean.valueOf(javaEnabled) : false, referrerURI); //create a new initialization event TODO check for NumberFormatException
									requestEventList.add(initEvent); //add the event to the list
								}
								break;
							case KEYPRESS:
							case KEYRELEASE:
								{
									final int code = Integer.parseInt(eventElement.getAttribute("code")); //get the key code TODO use a constant
									final Set<Key> keys = EnumSet.noneOf(Key.class); //we'll find any keys that were pressed
									if(Boolean.valueOf(eventElement.getAttribute("altKey")).booleanValue()) { //if Alt was pressed TODO use a constant
										keys.add(Key.ALT_LEFT); //note the Alt key
									}
									if(Boolean.valueOf(eventElement.getAttribute("controlKey")).booleanValue()) { //if Control was pressed TODO use a constant
										keys.add(Key.CONTROL_LEFT); //note the Control key
									}
									if(Boolean.valueOf(eventElement.getAttribute("shiftKey")).booleanValue()) { //if Shiftwas pressed TODO use a constant
										keys.add(Key.SHIFT_LEFT); //note the Shift key
									}
									final KeyboardEvent keyEvent;
									switch(eventType) { //see which type of keypress this is
										case KEYPRESS:
											keyEvent = new KeyPressEvent(platform, KeyCode.valueOf(code).getKey(), keys.toArray(new Key[keys.size()])); //create a new key press event
											break;
										case KEYRELEASE:
											keyEvent = new KeyReleaseEvent(platform, KeyCode.valueOf(code).getKey(), keys.toArray(new Key[keys.size()])); //create a new key release event
											break;
										default:
											throw new AssertionError("Unrecognized key event type: " + eventType);
									}
									requestEventList.add(keyEvent); //add the event to the list
								}
								break;
							case LOG:
								{
									final Level logLevel = getSerializedEnum(Level.class, eventElement.getAttribute("level")); //get the log level
									final String text = eventElement.getTextContent(); //get the log text
									getLogger().atLevel(logLevel).log("Guise AJAX: {}", text); //log this information
								}
								break;
							case MOUSECLICK:
							case MOUSEENTER:
							case MOUSEEXIT:
								{
									final Node componentNode = XPathDom.getNode(eventNode, AJAX_REQUEST_COMPONENT_XPATH_EXPRESSION); //get the component node
									final String componentID = ((Element)componentNode).getAttribute("id"); //TODO tidy; improve; comment
									final int componentX = Integer.parseInt(((Element)componentNode).getAttribute("x")); //TODO tidy; improve; check for errors; comment
									final int componentY = Integer.parseInt(((Element)componentNode).getAttribute("y")); //TODO tidy; improve; check for errors; comment
									final int componentWidth = Integer.parseInt(((Element)componentNode).getAttribute("width")); //TODO tidy; improve; check for errors; comment
									final int componentHeight = Integer.parseInt(((Element)componentNode).getAttribute("height")); //TODO tidy; improve; check for errors; comment

									final Node targetNode = XPathDom.getNode(eventNode, AJAX_REQUEST_TARGET_XPATH_EXPRESSION); //get the target node
									final String targetID = ((Element)targetNode).getAttribute("id"); //TODO tidy; improve; comment
									final int targetX = Integer.parseInt(((Element)targetNode).getAttribute("x")); //TODO tidy; improve; check for errors; comment
									final int targetY = Integer.parseInt(((Element)targetNode).getAttribute("y")); //TODO tidy; improve; check for errors; comment
									final int targetWidth = Integer.parseInt(((Element)targetNode).getAttribute("width")); //TODO tidy; improve; check for errors; comment
									final int targetHeight = Integer.parseInt(((Element)targetNode).getAttribute("height")); //TODO tidy; improve; check for errors; comment

									final Node viewportNode = XPathDom.getNode(eventNode, AJAX_REQUEST_VIEWPORT_XPATH_EXPRESSION); //get the viewport node
									final int viewportX = Integer.parseInt(((Element)viewportNode).getAttribute("x")); //TODO tidy; improve; check for errors; comment
									final int viewportY = Integer.parseInt(((Element)viewportNode).getAttribute("y")); //TODO tidy; improve; check for errors; comment
									final int viewportWidth = Integer.parseInt(((Element)viewportNode).getAttribute("width")); //TODO tidy; improve; check for errors; comment
									final int viewportHeight = Integer.parseInt(((Element)viewportNode).getAttribute("height")); //TODO tidy; improve; check for errors; comment

									final Node mouseNode = XPathDom.getNode(eventNode, AJAX_REQUEST_MOUSE_XPATH_EXPRESSION); //get the mouse node
									final int mouseX = Integer.parseInt(((Element)mouseNode).getAttribute("x")); //TODO tidy; improve; check for errors; comment
									final int mouseY = Integer.parseInt(((Element)mouseNode).getAttribute("y")); //TODO tidy; improve; check for errors; comment

									final Set<Key> keys = EnumSet.noneOf(Key.class); //we'll find any keys that were pressed
									if(Boolean.valueOf(eventElement.getAttribute("altKey")).booleanValue()) { //if Alt was pressed TODO use a constant
										keys.add(Key.ALT_LEFT); //note the Alt key
									}
									if(Boolean.valueOf(eventElement.getAttribute("controlKey")).booleanValue()) { //if Control was pressed TODO use a constant
										keys.add(Key.CONTROL_LEFT); //note the Control key
									}
									if(Boolean.valueOf(eventElement.getAttribute("shiftKey")).booleanValue()) { //if Shiftwas pressed TODO use a constant
										keys.add(Key.SHIFT_LEFT); //note the Shift key
									}
									if(componentID != null) { //if there is a component ID TODO add better event handling, to throw an error and send back that error
										final Component component = asInstance(platform.getDepictedObject(platform.getDepictID(componentID)), Component.class).orElse(null); //get the component by its ID
										if(component != null && AbstractComponent.hasAncestor(component, guiseSession.getApplicationFrame())) { //if there is a target component in our current hierarchy
											final MouseEvent mouseEvent;
											switch(eventType) { //see which type of event this is
												case MOUSECLICK:
													{
														final int buttonCode = Integer.parseInt(eventElement.getAttribute("button")); //get the button code TODO use a constant
														final int clickCount = Integer.parseInt(eventElement.getAttribute("clickCount")); //get the click count TODO use a constant
														mouseEvent = new MouseClickEvent(platform, component, new Rectangle(componentX, componentY, componentWidth, componentHeight),
																new Rectangle(viewportX, viewportY, viewportWidth, viewportHeight), new Point(mouseX, mouseY),
																Button.valueOf(buttonCode).getMouseButton(), clickCount, keys.toArray(new Key[keys.size()])); //create a new mouse enter event
													}
													break;
												case MOUSEENTER:
													mouseEvent = new MouseEnterEvent(platform, component, new Rectangle(componentX, componentY, componentWidth, componentHeight),
															new Rectangle(viewportX, viewportY, viewportWidth, viewportHeight), new Point(mouseX, mouseY),
															keys.toArray(new Key[keys.size()])); //create a new mouse enter event
													break;
												case MOUSEEXIT:
													mouseEvent = new MouseExitEvent(platform, component, new Rectangle(componentX, componentY, componentWidth, componentHeight),
															new Rectangle(viewportX, viewportY, viewportWidth, viewportHeight), new Point(mouseX, mouseY),
															keys.toArray(new Key[keys.size()])); //create a new mouse exit event
													break;
												default:
													throw new AssertionError("Unrecognized mouse event type: " + eventType);
											}
											//Log.trace("mouse event bound for component", ((Component<?>)mouseEvent.getTarget()).getID());
											requestEventList.add(mouseEvent); //add the event to the list
											//Log.trace("mouse event; targetXY:", targetX, targetY, "viewportXY:", viewportX, viewportY, "mouseXY:", mouseX, mouseY);
										}
									}
								}
								break;
							case POLL:
								{
									final WebPollEvent pollEvent = new WebPollEvent(platform); //create a new poll event
									requestEventList.add(pollEvent); //add the event to the list
								}
								break;
							default:
								throw new IllegalArgumentException("Unrecognized event type: " + eventType);
						}
					}
				}
			} catch(final ParserConfigurationException parserConfigurationException) { //we don't expect parser configuration errors
				throw new AssertionError(parserConfigurationException);
			} catch(final SAXException saxException) { //we don't expect parsing errors
				throw new AssertionError(saxException); //TODO maybe change to throwing an IOException
			} catch(final IOException ioException) { //if there is an I/O exception
				throw new AssertionError(ioException); //TODO fix better
			}
		} else { //if this is not a Guise AJAX request
			final HttpServletRequest request = guiseRequest.getHTTPServletRequest(); //get the HTTP servlet request
			//populate our parameter map
			if(ServletFileUpload.isMultipartContent(request)) { //if this is multipart/form-data content
				final WebFormEvent webFormEvent = new WebFormEvent(platform); //create a new form submission event
				final CollectionMap<String, Object, List<Object>> parameterListMap = webFormEvent.getParameterListMap(); //get the map of parameter lists
				final DiskFileItemFactory fileItemFactory = new DiskFileItemFactory(); //create a disk-based file item factory
				fileItemFactory.setRepository(guiseSession.getApplication().getTempDirectory()); //store the temporary files in the session temporary directory
				final ServletFileUpload servletFileUpload = new ServletFileUpload(fileItemFactory); //create a new servlet file upload handler
				servletFileUpload.setFileSizeMax(-1); //don't reject anything
				try { //try to parse the file items submitted in the request
					final List<FileItem> fileItems = (List<FileItem>)servletFileUpload.parseRequest(request); //parse the request
					for(final FileItem fileItem : fileItems) { //look at each file item
						final String parameterKey = fileItem.getFieldName(); //the parameter key will always be the field name
						final Object parameterValue = fileItem.isFormField() ? fileItem.getString() : new FileItemResourceImport(fileItem); //if this is a form field, store it normally; otherwise, create a file item resource import object
						parameterListMap.addItem(parameterKey, parameterValue); //store the value in the parameters
					}
				} catch(final FileUploadException fileUploadException) { //if there was an error parsing the files
					throw (IOException)new IOException(fileUploadException.getMessage()).initCause(fileUploadException);
				}
				requestEventList.add(webFormEvent); //add the event to the list
			} else { //if this is normal application/x-www-form-urlencoded data
				final boolean exhaustive = POST_METHOD.equals(request.getMethod()); //if this is an HTTP post, the form event is exhaustive of all controls on the form
				if(!exhaustive || request.getParameter(WebApplicationFrameDepictor.getActionInputID(guiseSession.getApplicationFrame())) != null) { //if this is a POST, only use the data if it is a Guise POST
					final WebFormEvent formSubmitEvent = new WebFormEvent(platform, exhaustive); //create a new form submission event
					final CollectionMap<String, Object, List<Object>> parameterListMap = formSubmitEvent.getParameterListMap(); //get the map of parameter lists
					final Iterator<Map.Entry<String, String[]>> parameterEntryIterator = ((Map<String, String[]>)request.getParameterMap()).entrySet().iterator(); //get an iterator to the parameter entries
					while(parameterEntryIterator.hasNext()) { //while there are more parameter entries
						final Map.Entry<String, String[]> parameterEntry = parameterEntryIterator.next(); //get the next parameter entry
						final String parameterKey = parameterEntry.getKey(); //get the parameter key
						final String[] parameterValues = parameterEntry.getValue(); //get the parameter values
						final List<Object> parameterValueList = new ArrayList<Object>(parameterValues.length); //create a list to hold the parameters
						addAll(parameterValueList, parameterValues); //add all the parameter values to our list
						parameterListMap.put(parameterKey, parameterValueList); //store the the array of values as a list, keyed to the value
					}
					requestEventList.add(formSubmitEvent); //add the event to the list
				}
			}
		}
		if(requestEventList.size() > 0) { //indicate the parameters
			getLogger().debug("Received Request Events:");
			for(final GuiseEvent requestEvent : requestEventList) { //for each control event
				getLogger().debug("  Event: {} {}", requestEvent.getClass(), requestEvent);
			}
		}

		/*TODO del
		Log.trace("parameter names:", request.getParameterNames());	//TODO del when finished with dual mulipart+encoded content
		Log.trace("number of parameter names:", request.getParameterNames());
		Log.trace("***********number of distinct parameter keys", parameterListMap.size());
		*/
		return requestEventList; //return the list of control events
	}

	/**
	 * Begins modal navigation based upon modal navigation information.
	 * @param guiseApplication The Guise application.
	 * @param guiseSession The Guise session.
	 * @param modalNavigation The modal navigation information
	 */
	protected void beginModalNavigation(final GuiseApplication guiseApplication, final GuiseSession guiseSession, final ModalNavigation modalNavigation) {
		final URIPath navigationPath = guiseApplication.relativizeURI(modalNavigation.getNewNavigationURI()); //get the navigation path for the modal navigation
		final Destination destination = guiseApplication.getDestination(navigationPath).orElse(null); //get the destination for this path TODO maybe add a GuiseSession.getDestination() TODO improve use of Optional
		if(destination instanceof ComponentDestination) { //if the destination is a component destination
			final ComponentDestination componentDestination = (ComponentDestination)destination; //get the destination as a component destination
			final Component destinationComponent = guiseSession.getDestinationComponent(componentDestination); //get the component for this destination
			if(destinationComponent instanceof ModalNavigationPanel) { //if the component is a modal navigatoin panel, as we expect
				final ModalNavigationPanel<?> modalPanel = (ModalNavigationPanel<?>)destinationComponent; //get the destination component as a modal panel
				guiseSession.beginModalNavigation(modalPanel, modalNavigation); //begin modal navigation with the panel
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version uninstalls the Guise application from the Guise container.
	 * </p>
	 */
	@Override
	public void destroy() {
		final AbstractGuiseApplication guiseApplication = getGuiseApplication(); //get the Guise application
		if(guiseApplication.getContainer() != null) { //if the Guise application is installed
			getGuiseContainer().uninstallApplication(guiseApplication); //uninstall the application
		}
		super.destroy(); //do the default destroying
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version delegates to the Guise container.
	 * </p>
	 */
	@Override
	protected Principal getPrincipal(final String id) throws HTTPInternalServerErrorException {
		return getGuiseContainer().getPrincipal(getGuiseApplication(), id); //delegate to the container
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version delegates to the Guise container.
	 * </p>
	 */
	@Override
	protected char[] getPassword(final Principal principal) throws HTTPInternalServerErrorException {
		return getGuiseContainer().getPassword(getGuiseApplication(), principal); //delegate to the container
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version delegates to the container.
	 * </p>
	 */
	@Override
	protected String getRealm(final URI resourceURI) throws HTTPInternalServerErrorException {
		return getGuiseContainer().getRealm(getGuiseApplication(), resourceURI); //delegate to the container
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version delegates to the Guise container, using the principal of the current Guise session instead of the given principal. This technique allows
	 * browser-based authentication to function normally (as successful authentication will have already updated the session's principal), and also allows
	 * browser-based authentication to work with session-based authentication in the even that the session has already authenticated a principal unknown to the
	 * browser.
	 * </p>
	 */
	@Override
	protected boolean isAuthorized(final HttpServletRequest request, final URI resourceURI, final String method, final Principal principal, final String realm)
			throws HTTPInternalServerErrorException {
		final HTTPServletGuiseContainer guiseContainer = getGuiseContainer(); //get the Guise container
		final AbstractGuiseApplication guiseApplication = getGuiseApplication(); //get the Guise application
		//TODO del; we don't want to force a session here, in case this is a non-Guise resource		final GuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieve the Guise session for this container and request
		return getGuiseContainer().isAuthorized(guiseApplication, resourceURI, principal, realm); //delegate to the container, using the current Guise session
	}

	/**
	 * Determines if the given nonce is valid. This version counts a nonce as invalid if it was associated with a different principal than the current Guise
	 * session principal (i.e. the Guise principal was logged out).
	 * @param request The HTTP request.
	 * @param nonce The nonce to check for validity.
	 * @return <code>true</code> if the nonce is not valid.
	 */
	protected boolean isValid(final HttpServletRequest request, final Nonce nonce) { //TODO check to see if we want to force a session
		//	TODO del Log.trace("ready to check validity of nonce; default validity", nonce);
		if(!super.isValid(request, nonce)) { //if the nonce doesn't pass the normal validity checks
			//		TODO del Log.trace("doesn't pass the basic checks");
			return false; //the nonce isn't valid
		}
		final HTTPServletGuiseContainer guiseContainer = getGuiseContainer(); //get the Guise container
		final AbstractGuiseApplication guiseApplication = getGuiseApplication(); //get the Guise application
		final GuiseSession guiseSession = HTTPServletGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request); //retrieve the Guise session for this container and request
		final Principal guiseSessionPrincipal = guiseSession.getPrincipal(); //get the current principal of the Guise session
		final String guiseSessionPrincipalID = guiseSessionPrincipal != null ? guiseSessionPrincipal.getName() : null; //get the current guise session principal ID
		//	TODO del Log.trace("checking to see if nonce principal ID", getNoncePrincipalID(nonce), "matches Guise session principal ID", guiseSessionPrincipalID);
		if(!Objects.equals(getNoncePrincipalID(nonce), guiseSessionPrincipalID)) { //if this nonce was for a different principal
			return false; //the user must have logged out or have been changed
		}
		//	TODO del Log.trace("nonce is valid");
		return true; //indicate that the nonce passed all the tests
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version stores the authenticated principal in the current Guise session if authentication was successful for valid credentials.
	 * </p>
	 */
	@Override
	protected void authenticated(final HttpServletRequest request, final URI resourceURI, final String method, final String requestURI, final Principal principal,
			final String realm, final AuthenticateCredentials credentials, final boolean authenticated) {
		if(authenticated && credentials != null) { //if authentication was successful with credentials (don't change the session principal for no credentials, because this might remove a principal set by the session itself with no knowledge of the browser)
			final HTTPServletGuiseContainer guiseContainer = getGuiseContainer(); //get the Guise container
			final AbstractGuiseApplication guiseApplication = getGuiseApplication(); //get the Guise application
			final GuiseSession guiseSession = HTTPServletGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request); //retrieve the Guise session for this container and request
			guiseSession.setPrincipal(principal); //set the new principal in the Guise session
		}
	}

	/**
	 * Extracts the bookmark contained in the given request URL.
	 * @param request The HTTP request object.
	 * @return The bookmark represented by the request, or <code>null</code> if no bookmark is contained in the request.
	 */
	/*TODO del if not needed
		protected static Bookmark getBookmark(final HttpServletRequest request)
		{
			final String queryString=request.getQueryString();	//get the query string from the request
			if(queryString!=null && queryString.length()>0) {	//if there is a query string (Tomcat 5.5.16 returns an empty string for no query, even though the Java Servlet specification 2.4 says that it should return null; this is fixed in Tomcat 6)
	//TODO del Log.trace("just got query string from request, length", queryString.length(), "content", queryString);
				return new Bookmark(String.valueOf(QUERY_SEPARATOR)+queryString);	//construct a new bookmark, preceding the string with a query indicator
			}
			else {	//if there is no query string, there is no bookmark
	//TODO del Log.trace("just got null query string from request");
				return null;	//indicate that there is no bookmark information
			}
		}
	*/

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version prevents redirects from a registered Guise destination.
	 * </p>
	 */
	@Override
	protected boolean canSubstitute(final HttpServletRequest request, final URI requestedResourceURI, final URI substituteResourceURI) throws IOException {
		final GuiseApplication guiseApplication = getGuiseApplication(); //get the Guise application
		final URIPath path = guiseApplication.getNavigationPath(requestedResourceURI); //get the application-relative navigation path
		if(guiseApplication.hasDestination(path)) { //if the application has a registered destination at the requested URI
			return false; //don't allow URI substitutions for any registered destination
		}
		return super.canSubstitute(request, requestedResourceURI, substituteResourceURI); //for all other cases, delegate to the parent version
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version adds checks to see if the URI represents a valid application navigation path. This version adds support for Guise public resources.
	 * </p>
	 */
	@Override
	protected boolean exists(final HttpServletRequest request, final URI resourceURI) throws IOException {
		getLogger().trace("checking exists for {}", resourceURI);
		final GuiseApplication guiseApplication = getGuiseApplication(); //get the Guise application
		final HTTPServletGuiseRequest guiseRequest = new HTTPServletGuiseRequest(request, /*TODO del response, */guiseContainer, guiseApplication); //get Guise request information
		///TODO del when works  	final URIPath path=guiseRequest.getNavigationPath();	//get the application-relative logical path
		final URIPath path = guiseApplication.getNavigationPath(resourceURI); //get the application-relative navigation path for the given resource URI
		if(guiseApplication.hasAsset(path)) { //if the path represents a valid application asset
			return true; //the resource exists
		}
		final Destination destination = guiseApplication.getDestination(path).orElse(null); //get the destination for the given path TODO improve use of Optional
		if(destination != null) { //if the URI represents a valid navigation path
			getLogger().trace("this is a destination");
			final HTTPServletGuiseContainer guiseContainer = getGuiseContainer(); //get the Guise container
			final GuiseSession guiseSession = HTTPServletGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request); //retrieve the Guise session for this container and request
			final Bookmark bookmark = guiseRequest.getBookmark(); //get the bookmark, if any
			final URI referrerURI = guiseRequest.getReferrerURI(); //get the referrer URI, if any
			/*TODO del
			 			final Bookmark bookmark=getBookmark(request);	//get the bookmark from this request
						final String referrer=getReferer(request);	//get the request referrer, if any
						final URI referrerURI=referrer!=null ? getPlainURI(URI.create(referrer)) : null;	//get a plain URI version of the referrer, if there is a referrer
			*/
			final ObjectHolder<Boolean> resourceExistsHolder = new ObjectHolder<Boolean>(); //create an object holder to receive the existence result
			final GuiseSessionThreadGroup guiseSessionThreadGroup = Guise.getInstance().getThreadGroup(guiseSession); //get the thread group for this session
			try {
				call(guiseSessionThreadGroup, new Runnable() { //call the method in a new thread inside the thread group

					@Override
					public void run() {
						try {
							resourceExistsHolder.setObject(Boolean.valueOf(destination.exists(guiseSession, path, bookmark, referrerURI))); //ask the resource destination if the resource exists
						} catch(final IOException ioException) { //if an exception is thrown
							throw new UndeclaredThrowableException(ioException); //let it pass to the calling thread
						}
					}

				});
			} catch(final UndeclaredThrowableException undeclaredThrowableException) { //if an exception was thrown
				final Throwable cause = undeclaredThrowableException.getCause(); //see what exception was thrown
				if(cause instanceof IOException) { //if an IOException was thrown
					throw ((IOException)cause); //pass it on
				} else { //we don't expect any other types of exceptions
					throw new AssertionError(cause);
				}
			}
			assert resourceExistsHolder.getObject() != null : "Return value from thread unexpectedly missing.";
			return resourceExistsHolder.getObject().booleanValue(); //return whether the resource at the resource destination exists
		}
		return super.exists(request, resourceURI); //see if a physical resource exists at the location, if we can't find a virtual resource (a Guise public resource or a navigation path component)
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version adds support for Guise public and temporary resources; and destination resources.
	 * </p>
	 */
	@Override
	protected HTTPServletResource getResource(final HttpServletRequest request, final URI resourceURI) throws IllegalArgumentException, IOException {
		//TODO del Log.trace("getting resource for URI: ", resourceURI);
		final GuiseApplication guiseApplication = getGuiseApplication(); //get the Guise application
		final HTTPServletGuiseRequest guiseRequest = new HTTPServletGuiseRequest(request, /*TODO del response, */guiseContainer, guiseApplication); //get Guise request information
		final URIPath path = guiseRequest.getNavigationPath(); //get the application-relative logical path
		final HTTPServletResource resource; //we'll create a resource for this resource URI
		if(guiseApplication.hasAsset(path)) { //if the path represents an application asset
			final GuiseSession guiseSession = HTTPServletGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request, false); //retrieve the Guise session, if any, for this container and request, but don't create one if there isn't one already
			try {
				final URL assetURL = guiseApplication.getAssetURL(path, guiseSession); //get the URL of the asset
				if(assetURL == null) { //if there is no such asset (it could have been removed after we checked)
					throw new HTTPNotFoundException("No such Guise asset: " + resourceURI);
				}
				resource = new DefaultHTTPServletResource(resourceURI, assetURL); //create a new default resource with a URL to the asset
			} catch(final IllegalStateException illegalStateException) { //if we cannot access the asset from the current session
				throw new HTTPForbiddenException(illegalStateException.getMessage(), illegalStateException); //forbid the user from accessing the resource
			}
			//		TODO del Log.trace("constructed a resource with length:", resource.getContentLength(), "and last modified", resource.getLastModified());
		} else { //if this is not a Guise asset, see if it is a Guise resource destination
			final Destination destination = guiseApplication.getDestination(path).orElse(null); //get the destination for the given path TODO improve use of Optional
			if(destination instanceof ResourceReadDestination) { //if this is a request for a resource destination
				final ResourceReadDestination resourceDestination = (ResourceReadDestination)destination; //get the resource destination
				final HTTPServletGuiseContainer guiseContainer = getGuiseContainer(); //get the Guise container
				final GuiseSession guiseSession = HTTPServletGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request); //retrieve the Guise session for this container and request
				final Bookmark bookmark = guiseRequest.getBookmark(); //get the bookmark, if any
				final URI referrerURI = guiseRequest.getReferrerURI(); //get the referrer URI, if any
				/*TODO del
					 			final Bookmark bookmark=getBookmark(request);	//get the bookmark from this request
								final String referrer=getReferer(request);	//get the request referrer, if any
								final URI referrerURI=referrer!=null ? getPlainURI(URI.create(referrer)) : null;	//get a plain URI version of the referrer, if there is a referrer
				*/
				final ObjectHolder<UrfResourceDescription> destinationResourceDescriptionHolder = new ObjectHolder<>(); //create an object holder to receive the result of asking for the resource description
				final GuiseSessionThreadGroup guiseSessionThreadGroup = Guise.getInstance().getThreadGroup(guiseSession); //get the thread group for this session
				try {
					call(guiseSessionThreadGroup, new Runnable() { //call the method in a new thread inside the thread group

						@Override
						public void run() {
							try {
								destinationResourceDescriptionHolder.setObject(resourceDestination.getResourceDescription(guiseSession, path, bookmark, referrerURI)); //ask the resource destination for the resource description
							} catch(final ResourceIOException resourceIOException) { //if an exception is thrown
								throw new UndeclaredThrowableException(resourceIOException); //let it pass to the calling thread
							}
						}

					});
				} catch(final UndeclaredThrowableException undeclaredThrowableException) { //if an exception was thrown
					final Throwable cause = undeclaredThrowableException.getCause(); //see what exception was thrown
					if(cause instanceof ResourceIOException) { //if a ResourceIOException was thrown
						throw HTTPException.createHTTPException((ResourceIOException)cause); //pass back an equivalent HTTP exception
					} else { //we don't expect any other types of exceptions
						throw new AssertionError(cause);
					}
				}
				assert destinationResourceDescriptionHolder.getObject() != null : "Return value from thread unexpectedly missing.";
				resource = new DestinationResource(resourceURI, destinationResourceDescriptionHolder.getObject(), guiseContainer, guiseApplication, guiseSession,
						resourceDestination, path, bookmark, referrerURI); //create an object to access the resource at the given resource destination
			} else { //if this is not a Guise asset or a resource destination, access the resource normally
				resource = super.getResource(request, resourceURI); //return a default resource
			}
		}
		return resource; //return the resource without extra processing
	}

	/**
	 * A resource that has retrieves its properties, if possible, from a given RDF description. Recognized properties are:
	 * <ul>
	 * <li>{@link Content#TYPE_PROPERTY_TAG} TODO implement</li>
	 * <li>{@link Content#LENGTH_PROPERTY_TAG}</li>
	 * <li>{@link Content#MODIFIED_AT_PROPERTY_TAG}</li>
	 * </ul>
	 * @implNote This class originated in the default HTTP servlet class in the GlobalMentor servlet library.
	 * @author Garret Wilson
	 */
	protected abstract static class AbstractDescriptionResource implements HTTPServletResource {

		private final URI uri;

		@Override
		public URI getURI() {
			return uri;
		}

		/** The description of the resource. */
		private final UrfResourceDescription resourceDescription;

		/** @return The description of the resource. */
		public UrfResourceDescription getResourceDescription() {
			return resourceDescription;
		}

		/**
		 * Returns the full content type of the resource, including any parameters.
		 * @param request The HTTP request in response to which the content type is being retrieved.
		 * @return The full content type of the resource with any parameters, or <code>null</code> if the content type could not be determined.
		 * @throws IOException if there is an error getting the type of the resource.
		 */
		public MediaType getContentType(final HttpServletRequest request) throws IOException {
			//TODO move to new URF utility methods
			final MediaType contentType = MediaType.class.cast(getResourceDescription().findPropertyValue(Content.TYPE_PROPERTY_TAG).orElse(null)); //get the content type, if any
			if(contentType != null) { //if a content type was indicated
				final Charset charset = Charset.class.cast(getResourceDescription().findPropertyValue(Content.CHARSET_PROPERTY_TAG).orElse(null)); //get the charset, if any
				if(charset != null) { //if a charset was specified
					contentType.withCharset(charset); //add the charset as the value of the charset parameter
				}
			}
			return contentType; //return the full content type, if any, we constructed
		}

		/**
		 * Returns the content length of the resource.
		 * @param request The HTTP request in response to which the content length is being retrieved.
		 * @return The content length of the resource, or <code>-1</code> if the content length could not be determined.
		 * @throws IOException if there is an error getting the length of the resource.
		 */
		public long getContentLength(final HttpServletRequest request) throws IOException {
			//TODO move to new URF utility methods
			final Number contentLength = Number.class.cast(getResourceDescription().findPropertyValue(Content.LENGTH_PROPERTY_TAG).orElse(null));
			return contentLength != null ? contentLength.longValue() : -1; //return the content length from the description, if possible
		}

		/**
		 * Returns the last modification time of the resource.
		 * @param request The HTTP request in response to which the last modified time is being retrieved.
		 * @return The time of last modification as the number of milliseconds since January 1, 1970 GMT, or <code>-1</code> if the last modified date could not be
		 *         determined.
		 * @throws IOException if there is an error getting the last modified time.
		 */
		public long getLastModified(final HttpServletRequest request) throws IOException {
			//TODO move to new URF utility methods
			final Instant lastModified = Instant.class.cast(getResourceDescription().findPropertyValue(Content.MODIFIED_AT_PROPERTY_TAG).orElse(null)); //get the last modified date time from the description, if that property exists
			return lastModified != null ? lastModified.getEpochSecond() : -1; //return the milliseconds of the time, if the time is available
		}

		/**
		 * Constructs a resource with a reference URI and resource description.
		 * @param referenceURI The reference URI for the new resource.
		 * @param resourceDescription The description of the resource.
		 * @throws NullPointerException if the reference URI and/or resource description is <code>null</code>.
		 */
		public AbstractDescriptionResource(final URI referenceURI, final UrfResourceDescription resourceDescription) {
			this.uri = requireNonNull(referenceURI, "HTTP resource reference URI cannot be null.");
			this.resourceDescription = requireNonNull(resourceDescription, "Resource description cannot be null."); //save the description
		}

	}

	/**
	 * A resource that is accessed through a Guise session's resource destination.
	 * @author Garret Wilson
	 * @see ResourceReadDestination
	 */
	protected class DestinationResource extends AbstractDescriptionResource {

		final GuiseContainer guiseContainer;
		final GuiseApplication guiseApplication;
		final GuiseSession guiseSession;
		final ResourceReadDestination resourceDestination;
		final URIPath navigationPath;
		final Bookmark bookmark;
		final URI referrerURI;

		@Override
		public InputStream getInputStream(final HttpServletRequest request) throws IOException {
			final ObjectHolder<InputStream> inputStreamHolder = new ObjectHolder<InputStream>(); //create an object holder to receive the result of asking for the input stream
			final GuiseSessionThreadGroup guiseSessionThreadGroup = Guise.getInstance().getThreadGroup(guiseSession); //get the thread group for this session
			try {
				call(guiseSessionThreadGroup, new Runnable() { //call the method in a new thread inside the thread group

					@Override
					public void run() {
						try {
							inputStreamHolder.setObject(resourceDestination.getInputStream(guiseSession, navigationPath, bookmark, referrerURI)); //ask the resource destination for an input stream to the resource
						} catch(final ResourceIOException resourceIOException) { //if an exception is thrown
							throw new UndeclaredThrowableException(resourceIOException); //let it pass to the calling thread
						}
					}

				});
			} catch(final UndeclaredThrowableException undeclaredThrowableException) { //if an exception was thrown
				final Throwable cause = undeclaredThrowableException.getCause(); //see what exception was thrown
				if(cause instanceof ResourceIOException) { //if a ResourceIOException was thrown
					throw HTTPException.createHTTPException((ResourceIOException)cause); //pass back an equivalent HTTP exception
				} else { //we don't expect any other types of exceptions
					throw new AssertionError(cause);
				}
			}
			assert inputStreamHolder.getObject() != null : "Return value from thread unexpectedly missing.";
			return inputStreamHolder.getObject(); //return the input stream we received from the resource destination
		}

		/**
		 * Constructs a resource with a reference URI and resource description, along with the Guise container, application, session, and resource destination.
		 * @param referenceURI The reference URI for the new resource.
		 * @param resourceDescription The description of the resource.
		 * @param guiseContainer The Guise container.
		 * @param guiseApplication The Guise application.
		 * @param guiseSession The Guise session.
		 * @param resourceDestination The Guise session resource destination being accessed.
		 * @param navigationPath The navigation path relative to the application context path.
		 * @param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in
		 *          navigation.
		 * @param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is
		 *          known.
		 * @throws NullPointerException if the reference URI, resource description, Guise container, Guise application, Guise session, resource destination,
		 *           navigation path, and/or bookmark is <code>null</code>.
		 */
		public DestinationResource(final URI referenceURI, final UrfResourceDescription resourceDescription, final HTTPServletGuiseContainer guiseContainer,
				final GuiseApplication guiseApplication, final GuiseSession guiseSession, final ResourceReadDestination resourceDestination,
				final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI) {
			super(referenceURI, resourceDescription); //construct the parent class
			this.guiseContainer = requireNonNull(guiseContainer, "Guise container cannot be null.");
			this.guiseApplication = requireNonNull(guiseApplication, "Guise application cannot be null.");
			this.guiseSession = requireNonNull(guiseSession, "Guise session cannot be null.");
			this.resourceDestination = requireNonNull(resourceDestination, "Resource destination cannot be null.");
			this.navigationPath = requireNonNull(navigationPath, "Navigation path cannot be null.");
			this.bookmark = bookmark;
			this.referrerURI = referrerURI;
		}

	}

}
