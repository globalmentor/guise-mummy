/*
 * Copyright © 2005-2011 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.security.Principal;
import java.text.DateFormat;
import java.util.*;

import static java.nio.charset.StandardCharsets.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.Objects.*;

import java.util.concurrent.*;

import javax.mail.Message;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;

import org.slf4j.event.Level;

import static com.globalmentor.io.Filenames.*;
import static com.globalmentor.io.Files.*;
import static com.globalmentor.java.Threads.*;
import static com.globalmentor.model.Locales.*;
import static com.globalmentor.net.URIs.*;
import static com.globalmentor.time.Calendars.*;
import static io.guise.framework.Guise.*;
import static io.guise.framework.Resources.*;
import static io.guise.framework.theme.Theme.*;

import com.globalmentor.beans.BoundPropertyObject;
import com.globalmentor.collections.DecoratorReadWriteLockMap;
import com.globalmentor.collections.PurgeOnWriteSoftValueHashMap;
import com.globalmentor.io.*;
import com.globalmentor.java.Objects;
import com.globalmentor.mail.MailManager;
import com.globalmentor.model.ConfigurationException;
import com.globalmentor.net.URIPath;
import com.globalmentor.text.W3CDateFormat;
import com.globalmentor.util.*;
import com.globalmentor.xml.spec.XML;

import io.clogr.Clogged;
import io.clogr.Clogr;
import io.clogr.LoggingConcern;
import io.csar.*;
import io.guise.framework.component.*;
import io.guise.framework.platform.*;
import io.guise.framework.theme.Theme;
import io.urf.model.SimpleGraphUrfProcessor;
import io.urf.model.UrfObject;
import io.urf.turf.TurfParser;

/**
 * An abstract base class for a Guise application. This implementation only works with Guise containers that descend from {@link AbstractGuiseContainer}.
 * @author Garret Wilson
 */
public abstract class AbstractGuiseApplication extends BoundPropertyObject implements GuiseApplication, Clogged {

	/** Whether this application is in debug mode. */
	private boolean debug = false;

	@Override
	public boolean isDebug() {
		return debug;
	}

	@Override
	public void setDebug(final boolean debug) {
		this.debug = debug;
	}

	/**
	 * I/O for loading resources.
	 * @implSpec This temporary implementation loads a map stored as the root resource in a TURF file.
	 * @implSpec This implementation does not support saving TURF resources.
	 */
	private static final IO<Map<Object, Object>> resourcesIO = new IO<>() {
		@SuppressWarnings("unchecked")
		@Override
		public Map<Object, Object> read(final InputStream inputStream, final URI baseURI) throws IOException {
			return new TurfParser<>(new SimpleGraphUrfProcessor()).parseDocument(inputStream).stream().findFirst().filter(Map.class::isInstance).map(Map.class::cast)
					.orElseThrow(() -> new IOException("TURF resources did not contain map at root."));
		}

		@Override
		public void write(final OutputStream outputStream, final URI baseURI, final Map<Object, Object> object) throws IOException {
			throw new UnsupportedOperationException();
		}
	};

	/** @return I/O for loading resources. */
	protected IO<Map<Object, Object>> getResourcesIO() {
		return resourcesIO;
	}

	/**
	 * I/O for loading themes.
	 * @implSpec This temporary implementation loads a legacy Guise theme and stores it as the theme description.
	 * @implSpec This implementation does not support saving themes resources.
	 */
	private static final IO<Theme> themeIO = new IO<>() {
		@SuppressWarnings("unchecked")
		@Override
		public Theme read(final InputStream inputStream, final URI baseURI) throws IOException {
			final UrfObject themeDescription = new TurfParser<>(new SimpleGraphUrfProcessor()).parseDocument(inputStream).stream().findFirst()
					.filter(UrfObject.class::isInstance).map(UrfObject.class::cast).orElseThrow(() -> new IOException("Theme missing main theme description."));
			return new Theme(baseURI, themeDescription);
		}

		@Override
		public void write(final OutputStream outputStream, final URI baseURI, final Theme object) throws IOException {
			throw new UnsupportedOperationException();
		}
	};

	/** @return I/O for loading themes. */
	public IO<Theme> getThemeIO() {
		return themeIO;
	}

	/** The manager of configurations for this session. */
	private final ConcernRegistry configurationManager = new DefaultConcernRegistry();

	/**
	 * Sets the given configurations, associating them with their respective classes.
	 * @param configurations The configurations to set.
	 */
	protected void setConfigurations(final Concern... configurations) {
		configurationManager.registerConcerns(configurations);
	}

	/**
	 * Sets the given configuration, associating it with its class.
	 * @param <C> The type of configuration being set.
	 * @param configuration The configuration to set.
	 * @return The configuration previously associated with the same class, or <code>null</code> if there was no previous configuration for that class.
	 * @throws NullPointerException if the given configuration is <code>null</code>.
	 */
	protected <C extends Concern> Optional<C> setConfiguration(final C configuration) {
		return configurationManager.registerConcern(configuration);
	}

	/**
	 * Sets the given configuration.
	 * @param <C> The type of configuration being set.
	 * @param configurationClass The class with which to associate the configuration.
	 * @param configuration The configuration to set.
	 * @return The configuration previously associated with the given class, or <code>null</code> if there was no previous configuration for that class.
	 */
	protected <C extends Concern> Optional<C> setConfiguration(final Class<C> configurationClass, final C configuration) {
		return configurationManager.registerConcern(configurationClass, configuration);
	}

	@Override
	public <C extends Concern> Optional<C> findConcern(final Class<C> configurationClass) {
		return configurationManager.findConcern(configurationClass);
	}

	/**
	 * Removes a configuration of the given type. If no configuration is associated with the specified type, no action occurs.
	 * @param <C> The type of configuration being removed.
	 * @param configurationClass The class with which the configuration is associated.
	 * @return The configuration previously associated with the given class, or <code>null</code> if there was no previous configuration for that class.
	 */
	protected <C extends Concern> Optional<C> removeConfiguration(final Class<C> configurationClass) {
		return configurationManager.unregisterConcern(configurationClass);
	}

	/** The application identifier URI. */
	private URI uri;

	/**
	 * {@inheritDoc}
	 * <p>
	 * This URI may be but is not guaranteed to be the URI at which the application can be accessed.
	 * </p>
	 */
	@Override
	public URI getURI() {
		return uri;
	}

	/** The Guise container into which this application is installed, or <code>null</code> if the application is not yet installed. */
	private AbstractGuiseContainer container = null;

	@Override
	public GuiseContainer getContainer() {
		return container;
	}

	/** The application local environment. */
	private Environment environment;

	@Override
	public Environment getEnvironment() {
		return environment;
	}

	@Override
	public void setEnvironment(final Environment newEnvironment) {
		if(!Objects.equals(environment, newEnvironment)) { //if the value is really changing (compare their values, rather than identity)
			final Environment oldEnvironment = environment; //get the old value
			environment = requireNonNull(newEnvironment, "Guise session environment cannot be null."); //actually change the value
			firePropertyChange(ENVIRONMENT_PROPERTY, oldEnvironment, newEnvironment); //indicate that the value changed
		}
	}

	/** The properties of the mail manager, or <code>null</code> if the mail properties have not been configured. */
	private Map<?, ?> mailProperties = null;

	@Override
	public Map<?, ?> getMailProperties() {
		if(isInstalled() && mailProperties == null) { //if the application has been installed but the repositories repository has not yet been set
			throw new ConfigurationException("Repositories repository has not been configured.");
		}
		return mailProperties; //return the repository for user repositories
	}

	@Override
	public void setMailProperties(final Map<?, ?> mailProperties) {
		checkNotInstalled(); //make sure the application has not been installed
		this.mailProperties = unmodifiableMap(requireNonNull(mailProperties, "Repository cannot be null."));
	}

	/** The mail manager, or <code>null</code> if the application is not installed or there is no mail defined for this application. */
	private MailManager mailManager = null;

	@Override
	public Session getMailSession() {
		checkInstalled(); //make sure the application has been installed (which will set the mail manager if configured)
		final MailManager mailManager = this.mailManager; //get the mail manager
		if(mailManager == null) { //if we don't have a mail manager, mail hasn't been configured
			throw new ConfigurationException("Mail has not been configured for the application.");
		}
		return mailManager.getSession(); //return the mail manager's session
	}

	@Override
	public Queue<Message> getMailSendQueue() {
		checkInstalled(); //make sure the application has been installed (which will set the mail manager if configured)
		final MailManager mailManager = this.mailManager; //get the mail manager
		if(mailManager == null) { //if we don't have a mail manager, mail hasn't been configured
			throw new ConfigurationException("Mail has not been configured for the application.");
		}
		return mailManager.getSendQueue(); //return the mail manager's send queue
	}

	/** Whether the application applies themes. */
	private boolean themed = true;

	@Override
	public boolean isThemed() {
		return themed;
	}

	@Override
	public void setThemed(final boolean newThemed) {
		if(themed != newThemed) { //if the value is really changing
			final boolean oldThemed = themed; //get the current value
			themed = newThemed; //update the value
			firePropertyChange(THEMED_PROPERTY, Boolean.valueOf(oldThemed), Boolean.valueOf(newThemed));
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version returns the relative path to the application unmodified.
	 * </p>
	 */
	@Override
	public URIPath getNavigationPath(final URI depictionURI) {
		return relativizeURI(depictionURI); //by default the navigation path and the depiction path are the same	
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation delegates to {@link #getDepictionURI(URI, URI)}.
	 * </p>
	 */
	@Override
	public final URI getDepictionURI(final URI depictionRootURI, final URIPath navigationPath) {
		return getDepictionURI(depictionRootURI, navigationPath.toURI());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version resolves the navigation URI to the base path, but otherwise returns the navigation URI unmodified.
	 * </p>
	 */
	@Override
	public URI getDepictionURI(final URI depictionRootURI, final URI navigationURI) {
		return getBasePath().resolve(navigationURI); //by default the navigation URI and the depiction URI are the same, except that the depiction URI is resolved to the application base path	
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version creates and returns a default session.
	 * </p>
	 */
	@Override
	public GuiseSession createSession(final Platform platform) {
		return new DefaultGuiseSession(this, platform); //create a new default Guise session
	}

	/** The concurrent map of Guise session info keyed to Guise sessions. */
	private final Map<GuiseSession, GuiseSessionInfo> guiseSessionInfoMap = new ConcurrentHashMap<GuiseSession, GuiseSessionInfo>();

	/** The concurrent map of Guise sessions keyed to UUIDs. */
	private final Map<UUID, GuiseSession> uuidGuiseSessionMap = new ConcurrentHashMap<UUID, GuiseSession>(new HashMap<UUID, GuiseSession>());

	@Override
	public void registerSession(final GuiseSession guiseSession) {
		if(guiseSessionInfoMap.containsKey(guiseSession)) { //if we already have info for this session (there is a race condition here that would allow a session to be registered twice, but that would only prevent error-checking for conditions that logically shouldn't happen anyway, so it's not worth the synchronization overhead to prevent)
			throw new IllegalStateException("Guise session " + guiseSession + " already registered with Guise application " + this);
		}
		uuidGuiseSessionMap.put(guiseSession.getUUID(), guiseSession); //associate the Guise session with its UUID
		guiseSessionInfoMap.put(guiseSession, new GuiseSessionInfo(guiseSession)); //add new Guise session information
	}

	@Override
	public void unregisterSession(final GuiseSession guiseSession) {
		final GuiseSessionInfo guiseSessionInfo = guiseSessionInfoMap.remove(guiseSession); //remove the info for this Guise session
		if(guiseSessionInfo == null) { //if there was no Guise session registered
			throw new IllegalStateException("Guise session " + guiseSession + " not registered with Guise application " + this);
		}
		uuidGuiseSessionMap.remove(guiseSession.getUUID()); //remove the Guise session from the UUID map
		final List<TempFileInfo> tempFileInfos = guiseSessionInfo.getTempFileInfos(); //get the temp files registered with this application
		synchronized(tempFileInfos) { //synchronize on the temp file infos for completeness (although no other threads should be accessing the list at this point
			for(final TempFileInfo tempFileInfo : tempFileInfos) { //for each temporary file
				final File tempFile = tempFileInfo.getTempFile(); //get the temporary file
				if(tempFile.exists()) { //if this file still exists
					if(!tempFile.delete()) { //delete the temporary file; if the file could not be deleted
						getLogger().warn("Could not delete temporary file {} associated with Guise session {}", tempFile, guiseSession);
					}
				}
			}
		}
	}

	@Override
	public GuiseSession getSession(final UUID uuid) {
		return uuidGuiseSessionMap.get(requireNonNull(uuid, "UUID cannot be null.")); //return the Guise session, if any, associted with the given UUID
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns a default application frame.
	 * </p>
	 */
	public ApplicationFrame createApplicationFrame() {
		return new DefaultApplicationFrame(); //return an instance of the default application frame 
	}

	/**
	 * The base URI where the application is installed, or <code>null</code> if no application base URI has been specified and the application is not yet
	 * installed.
	 */
	private URI baseURI = null;

	/**
	 * Reports the base URI where the application is installed. The base URI is an absolute URI that ends with the base path, which ends with a slash ('/').
	 * @return The base URI representing the Guise application, or <code>null</code> if no application base URI has been specified and the application is not yet
	 *         installed.
	 * @see #getBasePath()
	 */
	public URI getBaseURI() {
		return baseURI;
	}

	/**
	 * Sets the base URI of the application. The base path is also set.
	 * @param baseURI The base URI where the application is installed, which must be an absolute URI with an absolute collection path (e.g.
	 *          <code>http://www.example.com/path/</code>).
	 * @throws NullPointerException if the given base URI is <code>null</code>.
	 * @throws IllegalArgumentException if the given URI is not absolute or the path of which is not absolute or not a collection.
	 * @throws IllegalStateException if the application is already installed.
	 * @see #getBasePath()
	 */
	public void setBaseURI(final URI baseURI) {
		checkNotInstalled();
		this.basePath = findURIPath(checkAbsolute(baseURI)).map(URIPath::checkAbsolute).map(URIPath::checkCollection)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Base URI <%s> has no path.", baseURI)));
		this.baseURI = baseURI;
	}

	/** The base path of the application, or <code>null</code> if no application base URI has been specified and the application is not yet installed. */
	private URIPath basePath = null;

	@Override
	public URIPath getBasePath() {
		return basePath;
	}

	/** The home directory shared by all sessions of this application. */
	private File homeDirectory = null;

	@Override
	public File getHomeDirectory() {
		checkInstalled(); //make sure the application has been installed (which will set the home directory)
		assert homeDirectory != null : "Home directory is null even though application is installed.";
		return homeDirectory; //return the home directory;
	}

	/** The log directory shared by all sessions of this application. */
	private File logDirectory = null;

	@Override
	public File getLogDirectory() {
		checkInstalled(); //make sure the application has been installed (which will set the log directory)
		assert logDirectory != null : "Log directory is null even though application is installed.";
		return logDirectory; //return the log directory;
	}

	/** The temporary directory shared by all sessions of this application. */
	private File tempDirectory = null;

	@Override
	public File getTempDirectory() {
		checkInstalled(); //make sure the application has been installed (which will set the temporary directory)
		assert tempDirectory != null : "Temporary directory is null even though application is installed.";
		return tempDirectory; //return the temporary directory;
	}

	/** The synchronized map of log writer infos keyed to log base filenames. */
	private final Map<String, LogWriterInfo> baseNameLogWriterInfoMap = synchronizedMap(new HashMap<String, LogWriterInfo>());

	/**
	 * Retrieves a writer suitable for recording log information for the application. This implementation returns an asynchronous writer that does not block for
	 * information to be written when receiving information. The given base filename is appended with a representation of the current date. If a log writer for
	 * the same date is available, it is returned; otherwise, a new log writer is created. If the current date is a different day than that used for the current
	 * log writer for a given base filename, a new writer is created for the current date.
	 * @param baseFilename The base filename (e.g. "base.log") that will be used in generating a log file for the current date (e.g. "base 2003-02-01.log").
	 * @param initializer The encapsulation of any initialization that should be performed on any new writer, or <code>null</code> if no initialization is
	 *          requested.
	 * @param uninitializer The encapsulation of any uninitialization that should be performed on any new writer, or <code>null</code> if no uninitialization is
	 *          requested.
	 * @see GuiseApplication#getLogDirectory()
	 */
	public Writer getLogWriter(final String baseFilename, /*TODO fix final CalendarResolution calendarResolution, */final IOOperation<Writer> initializer,
			final IOOperation<Writer> uninitializer) throws IOException {
		synchronized(baseNameLogWriterInfoMap) { //don't allow the map to be used while we look up a writer
			LogWriterInfo logWriterInfo = baseNameLogWriterInfoMap.get(baseFilename); //get the log writer info
			if(logWriterInfo == null || System.currentTimeMillis() >= logWriterInfo.getExpireTime()) { //if there is no log writer information, or this log writer has expired
				if(logWriterInfo != null && uninitializer != null) { //if we have an old log writer and something to uninitialize it with
					uninitializer.perform(logWriterInfo.getWriter()); //uninitialize the old log writer
				}
				final File logDirectory = getLogDirectory(); //get the application log directory
				final DateFormat logFilenameDateFormat = new W3CDateFormat(W3CDateFormat.Style.DATE); //create a formatter for the log filename
				final String logFilename = appendBase(baseFilename, "-" + logFilenameDateFormat.format(new Date())); //create a filename in the form "baseFilename-date.ext"
				final File logFile = new File(logDirectory, logFilename); //create a log file object
				//TODO add a way to let the initializer know if this is a new log file or just a new writer				final boolean isNewLogFile=!logFile.exists();	//see if this is a new log file
				try {
					final Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(logFile, true)), UTF_8); //create a buffered UTF-8 log writer, appending if the file already exists
					final ThreadGroup guiseSessionThreadGroup = Guise.getInstance().getGuiseSessionThreadGroup(Thread.currentThread()); //get the Guise session thread group
					assert guiseSessionThreadGroup != null : "Expected to be inside a Guise session thread group when application log writer was requested.";
					final AsynchronousWriterRunnable asynchronousWriterRunnable = new AsynchronousWriterRunnable(writer); //create a runnable for creating the new asynchronous writer
					call(guiseSessionThreadGroup.getParent(), asynchronousWriterRunnable); //create an asynchronous writer in the thread group above the Guise session thread group, because the asynchronous writer's thread will live past this session's thread group
					final Writer logWriter = asynchronousWriterRunnable.getWriter(); //get the asynchronous writer that was created
					assert logWriter != null : "Asynchronous writer runnable did not create asynchronous writer as expected.";
					//TODO del when works					final Writer logWriter=new AsynchronousWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(logFile, true)), UTF_8));	//create an asynchronous, buffered UTF-8 log writer, appending if the file already exists
					final Calendar calendar = Calendar.getInstance(); //create a new default calendar for the current date and time
					calendar.add(Calendar.DAY_OF_YEAR, 1); //go to the next day to find out when this writer should expire
					clearTime(calendar); //clear the calendar's time, consentrating on just the date (i.e. set the writer to expire at midnight)					
					//TODO del; testing					logWriterInfo=new LogWriterInfo(logWriter, System.currentTimeMillis()+10000);					
					logWriterInfo = new LogWriterInfo(logWriter, calendar.getTimeInMillis()); //encapsulate the writer and its expiration time
					baseNameLogWriterInfoMap.put(baseFilename, logWriterInfo); //replace the old writer with our new writer; the old writer will eventually be garbage collected and, under normal conditions, will close when it is finalized
					if(initializer != null) { //if we have something to initialize the new log writer with
						initializer.perform(logWriter); //initialize the new log writer
					}
				} catch(final UnsupportedEncodingException unsupportedEncodingException) { //we should always support UTF-8
					throw new AssertionError(unsupportedEncodingException);
				}
			}
			return logWriterInfo.getWriter(); //return the writer
		}
	}

	/**
	 * The runnable whose sole function is to create an asynchronous writer.
	 * @author Garret Wilson
	 */
	private static class AsynchronousWriterRunnable implements Runnable //create a runnable for creating the new asynchronous writer
	{

		/** The writer to be decorated. */
		private final Writer decoratedWriter;

		/** The writer that was created, or <code>null</code> if the writer has not yet been created. */
		private Writer writer = null;

		/** @return The writer that was created, or <code>null</code> if the writer has not yet been created. */
		public Writer getWriter() {
			return writer;
		}

		/**
		 * Constructs the class with a writer to decorate.
		 * @param decoratedWriter The writer to decorate with an asynchronous writer.
		 * @throws NullPointerException if the given writer is <code>null</code>.
		 */
		public AsynchronousWriterRunnable(final Writer decoratedWriter) {
			this.decoratedWriter = requireNonNull(decoratedWriter, "Decorated writer cannot be null.");
		}

		@Override
		public void run() {
			writer = new AsynchronousWriter(decoratedWriter); //create an asynchronous writer based upon the decorated writer
		}
	};

	/** The hash code, which we'll update after installation. The value is only used after installation, so the initial value is irrelevant. */
	//TODO del if not needed	private int hashCode=-1;

	@Override
	public boolean isInstalled() {
		return getContainer() != null && getBasePath() != null;
	}

	@Override
	public void checkInstalled() {
		if(!isInstalled()) { //if the application is not installed
			throw new IllegalStateException("Application not installed.");
		}
	}

	/**
	 * Checks to ensure that this application is not installed.
	 * @throws IllegalStateException if the application is installed.
	 * @see #isInstalled()
	 */
	public void checkNotInstalled() {
		if(isInstalled()) { //if the application is installed
			throw new IllegalStateException("Application already installed.");
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation configures logging. Mail is enabled if mail properties have been configured using {@link #setMailProperties(Map)}.
	 * </p>
	 */
	@Override
	public void install(final AbstractGuiseContainer container, final URI baseURI, final File homeDirectory, final File logDirectory, final File tempDirectory) {
		requireNonNull(container, "Container cannot be null");
		checkNotInstalled();
		getLogger().info("Installing application {} at URI {}", this, baseURI);
		setBaseURI(baseURI); //set the base URI
		this.container = container; //store the container
		this.homeDirectory = requireNonNull(homeDirectory, "Home directory cannot be null.");
		this.logDirectory = requireNonNull(logDirectory, "Log directory cannot be null.");
		this.tempDirectory = requireNonNull(tempDirectory, "Temporary directory cannot be null.");
		final DateFormat logFilenameDateFormat = new W3CDateFormat(W3CDateFormat.Style.DATE); //create a formatter for the log filename
		final String logFilename = addExtension("application-" + logFilenameDateFormat.format(new Date()), "log"); //create a filename in the form "application-YYYY-MM-DD.log" TODO use constant once it is added to com.globalmentor.text.Text
		final File logFile = new File(logDirectory, logFilename); //determine the log file for this application TODO create a custom log configuration that will use rolling log files
		/* TODO configure logging concern appropriately, to bring back feature parity with legacy log library
		loggingConcern.setFile(logFile); //set the log file for our log configuration
		loggingConcern.setStandardOutput(isDebug()); //if we are debugging, turn on logging to the standard output
		*/
		setConfiguration(LoggingConcern.class, loggingConcern); //configure logging for this application
		loggingConcern.getLogger(getClass()).info("Installing application {}", this); //inform the application-specific log
		if(mailProperties != null) { //if mail properties have been configured
			try {
				mailManager = new MailManager(mailProperties); //create a new mail manager from the properties
			} catch(final NoSuchProviderException noSuchProviderException) { //if a provider couldn't be found
				throw new ConfigurationException(noSuchProviderException); //indicate that there was a problem configuring the application TODO use a better error; create a ConfigurationStateException
			}
		} else { //if there are no mail properties
			getLogger().warn("Mail properties not configured."); //warn that mail isn't configured
		}
	}

	@Override
	public void uninstall(final GuiseContainer container) {
		getLogger().info("Uninstalling application {} from URI {}", this, baseURI);
		loggingConcern.getLogger(getClass()).info("Uninstalling application {}", this); //inform the application-specific log
		if(this.container == null) { //if we don't have a container
			throw new IllegalStateException("Application not installed.");
		}
		if(this.container != container) { //if we're installed into a different container
			throw new IllegalStateException("Application installed into different container.");
		}
		mailManager.getSendThread().interrupt(); //interrupt the mail manager's send thread
		mailManager = null; //release the mail manager
		synchronized(baseNameLogWriterInfoMap) { //don't allow the map to be used while we look up a writer
			for(final LogWriterInfo logWriterInfo : baseNameLogWriterInfoMap.values()) { //for each log writer info
				try {
					logWriterInfo.getWriter().close(); //close this writer
				} catch(final IOException ioException) { //if there is an error closing the writer
					getLogger().warn("", ioException); //log the warning and continue
				}
			}
			baseNameLogWriterInfoMap.clear(); //remove all log writer information
		}
		/* TODO close logging concern if needed after bringing back back feature parity with legacy log library
		try {
			loggingConcern.close(); //close our main application log configuration, closing files as necessary
		} catch(final IOException ioException) {
			System.err.println("Error closing log writer; " + ioException.getMessage());
			ioException.printStackTrace();
		}
		*/

		this.container = null; //release the container
		this.basePath = null; //remove the base path
	}

	/** The identifier for logging to a Data Collection System such as WebTrends, or <code>null</code> if no DCS ID is known. */
	private String dcsID = null;

	@Override
	public String getDCSID() {
		return dcsID;
	}

	@Override
	public void setDCSID(final String dcsID) {
		this.dcsID = dcsID;
	}

	/**
	 * The read-only non-empty list of locales supported by the application, with the first locale the default used if a new session cannot determine the users's
	 * preferred locale.
	 */
	private List<Locale> locales;

	@Override
	public List<Locale> getLocales() {
		return locales;
	}

	@Override
	public void setLocales(final List<Locale> newLocales) {
		requireNonNull(newLocales, "Guise application locales cannot be null."); //make sure the list is not null
		if(newLocales.isEmpty()) { //if there are no locales given
			throw new IllegalArgumentException("Guise application must support at least one locale.");
		}
		if(!locales.equals(newLocales)) { //if the value is really changing
			final List<Locale> oldLocales = locales; //get the old value
			locales = unmodifiableList(new ArrayList<Locale>(newLocales)); //create an unmodifiable copy of the locales
			firePropertyChange(LOCALES_PROPERTY, oldLocales, locales); //indicate that the value changed
		}
	}

	/** The thread-safe set of locales supported by this application. */
	private final Set<Locale> supportedLocales = new CopyOnWriteArraySet<Locale>();

	/** @return The thread-safe set of locales supported by this application. */
	public Set<Locale> getSupportedLocales() {
		return supportedLocales;
	}

	/** The base name of the resource bundle to use for this application, or <code>null</code> if no custom resource bundle is specified for this application. */
	private String resourceBundleBaseName = null;

	@Override
	public String getResourceBundleBaseName() {
		return resourceBundleBaseName;
	}

	@Override
	public void setResourceBundleBaseName(final String newResourceBundleBaseName) {
		if(!Objects.equals(resourceBundleBaseName, newResourceBundleBaseName)) { //if the value is really changing
			final String oldResourceBundleBaseName = resourceBundleBaseName; //get the old value
			resourceBundleBaseName = newResourceBundleBaseName; //actually change the value
			firePropertyChange(RESOURCE_BUNDLE_BASE_NAME_PROPERTY, oldResourceBundleBaseName, newResourceBundleBaseName); //indicate that the value changed
		}
	}

	/** The absolute or application-relative URI of the application style, or <code>null</code> if the default style should be used. */
	private URI styleURI;

	/** @return The absolute or application-relative URI of the application style, or <code>null</code> if the default style should be used. */
	public URI getStyleURI() {
		return styleURI;
	}

	@Override
	public void setStyleURI(final URI newStyle) {
		if(!Objects.equals(styleURI, newStyle)) { //if the value is really changing (compare their values, rather than identity)
			final URI oldStyle = styleURI; //get the old value
			styleURI = newStyle; //actually change the value
			firePropertyChange(STYLE_URI_PROPERTY, oldStyle, newStyle); //indicate that the value changed
		}
	}

	/** The URI of the application theme, to be resolved against the application base path. */
	private URI themeURI = GUISE_BASIC_THEME_PATH.toURI();

	/** @return The URI of the application theme, to be resolved against the application base path. */
	public URI getThemeURI() {
		return themeURI;
	}

	@Override
	public void setThemeURI(final URI newThemeURI) {
		if(!Objects.equals(themeURI, newThemeURI)) { //if the value is really changing
			final URI oldThemeURI = themeURI; //get the old value
			themeURI = requireNonNull(newThemeURI, "Theme URI cannot be null."); //actually change the value
			firePropertyChange(THEME_URI_PROPERTY, oldThemeURI, newThemeURI); //indicate that the value changed
		}
	}

	/** The log configuration for this application. */
	private final LoggingConcern loggingConcern;

	@Override
	public void setLogLevel(final Level level) {
		loggingConcern.setLogLevel(level);
	}

	/**
	 * URI constructor. The URI identifier may or may not be the URI at which the application can be accessed This implementation sets the locale to the JVM
	 * default.
	 * @param uri The URI for the application, or <code>null</code> if there is no identifier is not known.
	 */
	public AbstractGuiseApplication(final URI uri) {
		this.uri = uri; //set the URI
		locales = unmodifiableList(asList(Locale.getDefault())); //create an unmodifiable list of locales including only the default locale of the JVM
		this.environment = new DefaultEnvironment(); //create a default environment
		this.loggingConcern = Clogr.getLoggingConcern();
		/* TODO wrap default logging concern and configure appropriately, to bring back feature parity with legacy log library
		this.loggingConcern = new DefaultLogConfiguration(); //create a default log configuration, which we'll initialize with a log file later
		this.loggingConcern.setStandardOutput(false); //turn off logging to the standard output; we may turn it on later
		*/
	}

	/** The concurrent list of destinations which have path patterns specified. */
	private final List<Destination> pathPatternDestinations = new CopyOnWriteArrayList<Destination>();

	/** The concurrent map of destinations associated with application context-relative paths. */
	private final Map<URIPath, Destination> pathDestinationMap = new ConcurrentHashMap<URIPath, Destination>();

	@Override
	public void addDestination(final Destination destination) {
		addDestination(destination, false); //add this destination with subordinate priority
	}

	@Override
	public void addDestination(final Destination destination, final boolean priority) {
		final URIPath path = destination.getPath(); //get the destination's path, if there is one
		if(path != null) { //if this destination has a path
			pathDestinationMap.put(path, destination); //associate the destination with the path
		} else { //if the destination has no path
			assert destination.getPathPattern() != null : "Destination should have had either a path or a path pattern.";
			if(priority) { //if this destination has priority
				pathPatternDestinations.add(0, destination); //add this destination to the beginning of the list of destinations with path patterns
			} else { //if this destination should not have priority
				pathPatternDestinations.add(destination); //add this destination to the list of destinations with path patterns
			}
		}
	}

	@Override
	public void setDestinations(final List<Destination> destinations) {
		pathDestinationMap.clear(); //clear the map of path/destination associations
		pathPatternDestinations.clear(); //clear the list of path pattern destinations
		for(final Destination destination : destinations) { //for each destination
			addDestination(destination); //add this destination
		}
	}

	@Override
	public Optional<Destination> getDestination(final URIPath path) {
		path.checkRelative(); //make sure the path is relative
		Destination destination = pathDestinationMap.get(path); //get the destination associated with this path, if any
		if(destination == null) { //if there is no destination for this exact path
			for(final Destination pathPatternDestination : pathPatternDestinations) { //look at all the destinations with path patterns
				if(pathPatternDestination.getPathPattern().matcher(path.toString()).matches()) { //if this destination's pattern matches the given path
					destination = pathPatternDestination; //use this destination
					break; //stop looking at destinations with path patterns
				}
			}
		}
		return Optional.ofNullable(destination); //return the destination we found, if any
	}

	@Override
	public Iterable<Destination> getDestinations() {
		final List<Destination> destinations = new ArrayList<Destination>(pathDestinationMap.size() + pathPatternDestinations.size()); //create a list large enough to hold all the path-mapped destinations and all the destinations with path patterns
		destinations.addAll(pathDestinationMap.values()); //add the path destinations
		destinations.addAll(pathPatternDestinations); //add the path pattern destinations
		return destinations; //return our constructed list of all available destinations
	}

	@Override
	public boolean hasDestination(final URIPath path) {
		path.checkRelative(); //make sure the path is relative
		if(pathDestinationMap.containsKey(path)) { //see if there is a destination associated with this navigation path
			return true; //show that we found an exact match
		}
		for(final Destination pathPatternDestination : pathPatternDestinations) { //look at all the destinations with path patterns
			if(pathPatternDestination.getPathPattern().matcher(path.toString()).matches()) { //if this destination's pattern matches the given path
				return true; //show that we found a pattern match
			}
		}
		return false; //indicate we couldn't find an exact match or a pattern match
	}

	@Override
	public URIPath resolvePath(final URIPath path) {
		return getBasePath().resolve(requireNonNull(path, "Path cannot be null.")); //resolve the given path against the base path
	}

	@Override
	public URI resolveURI(URI uri) {
		if(PATH_SCHEME.equals(uri.getScheme())) { //if this ia a path: URI
			uri = getPathURIPath(uri).toURI(); //get the URI form of the raw path of the path URI
		}
		return getBasePath().resolve(requireNonNull(uri, "URI cannot be null.")); //resolve the given URI against the base path
	}

	@Override
	public URIPath relativizePath(final URIPath path) {
		return getBasePath().relativizeChildPath(path); //get the path relative to the application path 
	}

	@Override
	public URIPath relativizeURI(final URI uri) {
		return relativizePath(URIPath.of(uri.getRawPath())); //relativize the path of the URI TODO make sure the URI is from the correct domain
	}

	@Override
	public String getLocaleResourcePath(final String resourceBasePath, final Locale locale) {
		/*TODO refactor into common method
				final String relativeApplicationPath=relativizePath(getContainer().getBasePath(), getBasePath());	//get the application path relative to the container path
				final String contextRelativeResourcebasePath=relativeApplicationPath+resourceBasePath;	//get the base path relative to the container
		*/
		for(int depth = 3; depth >= 0; --depth) { //try different locales, starting with the most specific
			final String resourceCandidatePath = getLocaleCandidatePath(resourceBasePath, locale, depth); //get a candidate path for the resource at this locale depth
			if(resourceCandidatePath != null && hasResource(resourceCandidatePath)) { //if we can generate a candidate path for the locale at this depth, and we have that resource
				return resourceCandidatePath; //return this candidate path
			}
		}
		return null; //indicate that we were unable to find a resource path for the given locale
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation uses package access to delegate to {@link AbstractGuiseContainer#hasResource(String)}.
	 * </p>
	 */
	@Override
	public boolean hasResource(final String resourcePath) {
		checkInstalled(); //make sure we're installed
		final URIPath relativeApplicationPath = getContainer().getBasePath().relativizeChildPath(getBasePath()); //get the application path relative to the container path 
		return container.hasResource(relativeApplicationPath.toString() + resourcePath); //delegate to the container
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation uses package access to delegate to {@link AbstractGuiseContainer#getResourceInputStream(String)}.
	 * </p>
	 */
	@Override
	public InputStream getResourceInputStream(final String resourcePath) {
		checkInstalled(); //make sure we're installed
		final URIPath relativeApplicationPath = getContainer().getBasePath().relativizeChildPath(getBasePath()); //get the application path relative to the container path 
		return container.getResourceInputStream(relativeApplicationPath.toString() + resourcePath); //delegate to the container
	}

	@Override
	public InputStream getInputStream(final URI uri) throws IOException { //TODO check for the resource: URI scheme
		/*TODO decide if we really want this
				If this is a <code>resource:</code> URI representing a private resource, this method delegates to {@link #getResourceInputStream(String)}.
				if(RESOURCE_SCHEME.equals(uri.getScheme())) {	//if this is a resource reference URI
					return getResourceInputStream(uri.get)
				}
		*/
		//TODO del getLogger().trace("getting input stream to URI {}", uri);
		final GuiseContainer container = getContainer(); //get the container
		final URI resolvedURI = resolveURI(uri); //resolve the URI to the application
		//TODO del getLogger().trace("resolved URI: {}", resolvedURI);
		final URI absoluteResolvedURI = resolve(container.getBaseURI(), resolvedURI); //resolve the URI against the container base URI
		//TODO del getLogger().trace("absolute resolved URI: {}", absoluteResolvedURI);
		//check for Guise public resources
		final URI publicResourcesBaseURI = resolve(container.getBaseURI(), getBasePath().resolve(GUISE_ASSETS_BASE_PATH).toURI()); //get the base URI of Guise public resources
		//	TODO del getLogger().trace("publicResourcesBaseURI: {}", publicResourcesBaseURI);
		final URI publicResourceRelativeURI = publicResourcesBaseURI.relativize(absoluteResolvedURI); //see if the absolute URI is in the application public path
		//	TODO del getLogger().trace("resourceURI: {}", resourceURI);		
		if(!publicResourceRelativeURI.isAbsolute()) { //if the URI is relative to the application's public resources
			return Guise.getInstance().getAssetInputStream(GUISE_ASSETS_BASE_KEY + publicResourceRelativeURI.getPath()); //return an input stream to the resource directly, rather than going through the server
		}
		//check for Guise public temp resources
		final URI publicTempBaseURI = resolve(container.getBaseURI(), getBasePath().resolve(GUISE_ASSETS_TEMP_BASE_PATH).toURI()); //get the base URI of Guise public temporary resources
		//	TODO del getLogger().trace("publicResourcesBaseURI: {}", publicResourcesBaseURI);
		final URI publicTempRelativeURI = publicTempBaseURI.relativize(absoluteResolvedURI); //see if the absolute URI is in the application public temporary path
		//	TODO del getLogger().trace("resourceURI: {}", resourceURI);		
		if(!publicTempRelativeURI.isAbsolute()) { //if the URI is relative to the application's public temp resources
			final String filename = publicTempRelativeURI.getRawPath(); //get the filename of the temp file
			final TempFileInfo tempFileInfo = filenameTempFileInfoMap.get(filename); //get the info for this temp file
			if(tempFileInfo != null) { //if we found the temporary file
				final File tempFile = tempFileInfo.getTempFile(); //get the temp file
				if(tempFile.exists()) { //if the temp file exists
					final GuiseSession restrictionSession = tempFileInfo.getRestrictionSession(); //get the restriction session, if any
					if(restrictionSession != null) { //if this file is restricted to a Guise session
						if(!restrictionSession.equals(Guise.getInstance().getGuiseSession())) { //compare the restricted session with the current Guise session, throwing an exception if there is Guise session
							throw new IllegalStateException("Guise public temporary resource " + uri + " cannot be accessed from the current Guise session.");
						}
					}
					return new FileInputStream(tempFileInfo.getTempFile()); //create an input stream to the temp file
				}
			}
			return null; //if there is no such temp file info, or the temp file does not exist, indicate that the temporary file does not exist
		}
		return container.getInputStream(resolvedURI); //resolve the URI to the application and delegate to the container
	}

	@Override
	public InputStream getInputStream(final URIPath path) throws IOException {
		return getInputStream(path.toURI()); //create a URI, verifying that it is a path, and return an input stream to the URI		
		/*TODO fix; reverse delegation
				final URIPath basePath=getBasePath();	//get the application base path
				final URIPath resolvedPath=basePath.resolve(path);	//resolve the path against the application base URI
				final URIPath publicTempBasePath=basePath.resolve(GUISE_TEMP_BASE_PATH);	//get the base path of the Guise temp resources
				final URIPath publicTempRelativePath=publicTempBasePath.relativize(resolvedPath);	//see if the resolved path is in the application public path
				if(!publicTempRelativePath.isAbsolute()) {	//if the path is relative to the application's temp resources
					final String filename=publicTempRelativePath.toString();	//get the filename of the temp file; all temporary files are stored in the same directory, so if this is not a plain filename no file will be found
					final TempFileInfo tempFileInfo=filenameTempFileInfoMap.get(filename);	//get the info for this temp file
					if(tempFileInfo!=null) {	//if we found the temporary file
						final GuiseSession restrictionSession=tempFileInfo.getRestrictionSession();	//get the restriction session, if any
						if(restrictionSession!=null) {	//if this file is restricted to a Guise session
							if(!restrictionSession.equals(Guise.getInstance().getGuiseSession())) {	//compare the restricted session with the current Guise session, throwing an exception if there is Guise session
								throw new IllegalStateException("Guise public temporary resource "+path+" cannot be accessed from the current Guise session.");
							}
						}
						return new FileOutputStream(tempFileInfo.getTempFile());	//create an output stream to the temp file
					}
					else {	//if there is no such temp file
						throw new FileNotFoundException("No such Guise public temp file: "+filename);
					}
				}
				else {	//if the URI is not an application-relative public temporary resource URI
					throw new UnsupportedOperationException("Access to non-temporary resource URI "+path+" is unsupported.");	//TODO fix
				}
		*/
	}

	@Override
	public OutputStream getOutputStream(final URI uri) throws IOException {
		//TODO del getLogger().trace("getting input stream to URI {}", uri);
		final GuiseContainer container = getContainer(); //get the container
		final URI resolvedURI = resolveURI(uri); //resolve the URI to the application
		//	TODO del getLogger().trace("resolved URI: {}", resolvedURI);
		final URI absoluteResolvedURI = resolve(container.getBaseURI(), resolvedURI); //resolve the URI against the container base URI
		//	TODO del getLogger().trace("absolute resolved URI: {}", absoluteResolvedURI);
		final URI publicTempBaseURI = resolve(container.getBaseURI(), getBasePath().resolve(GUISE_ASSETS_TEMP_BASE_PATH).toURI()); //get the base URI of the Guise temp resources
		//	TODO del getLogger().trace("publicResourcesBaseURI: {}", publicResourcesBaseURI);
		final URI publicTempRelativeURI = publicTempBaseURI.relativize(absoluteResolvedURI); //see if the absolute URI is in the application public path
		//	TODO del getLogger().trace("resourceURI: {}", resourceURI);		
		if(!publicTempRelativeURI.isAbsolute()) { //if the URI is relative to the application's temp resources
			final String filename = publicTempRelativeURI.getRawPath(); //get the filename of the temp file
			final TempFileInfo tempFileInfo = filenameTempFileInfoMap.get(filename); //get the info for this temp file
			if(tempFileInfo != null) { //if we found the temporary file
				final GuiseSession restrictionSession = tempFileInfo.getRestrictionSession(); //get the restriction session, if any
				if(restrictionSession != null) { //if this file is restricted to a Guise session
					if(!restrictionSession.equals(Guise.getInstance().getGuiseSession())) { //compare the restricted session with the current Guise session, throwing an exception if there is Guise session
						throw new IllegalStateException("Guise public temporary resource " + uri + " cannot be accessed from the current Guise session.");
					}
				}
				return new FileOutputStream(tempFileInfo.getTempFile()); //create an output stream to the temp file
			} else { //if there is no such temp file
				throw new FileNotFoundException("No such Guise public temp file: " + filename);
			}
		} else { //if the URI is not an application-relative public temporary resource URI
			throw new UnsupportedOperationException("Access to non-temporary resource URI " + uri + " is unsupported."); //TODO fix
		}
	}

	@Override
	public OutputStream getOutputStream(final URIPath path) throws IOException {
		return getOutputStream(path.toURI()); //create a URI, verifying that it is a path, and return an output stream to the URI
	}

	/**
	 * The map of temp file info objects keyed to temporary filenames (not paths). Because all temporary files are created in the same directory, there should be
	 * no filename conflicts.
	 */
	private final Map<String, TempFileInfo> filenameTempFileInfoMap = new ConcurrentHashMap<String, TempFileInfo>();

	@Override
	public URIPath createTempAsset(String baseName, final String extension, final GuiseSession restrictionSession) throws IOException {
		final File tempFile = createTempFile(baseName, requireNonNull(extension, "Extension cannot be null."), getTempDirectory(), true); //create a temporary file in the application's temporary directory, specifying that it should be deleted on JVM exit
		final String filename = tempFile.getName(); //get the name of the file
		assert filename.length() > 0 : "Name of generated temporary file is missing.";
		final TempFileInfo tempFileInfo = new TempFileInfo(tempFile, restrictionSession); //create an object to keep track of the file
		filenameTempFileInfoMap.put(filename, tempFileInfo); //map the filename to the temp file info
		if(restrictionSession != null) { //if file access should be restricted to a session
			final GuiseSessionInfo guiseSessionInfo = guiseSessionInfoMap.get(restrictionSession); //get info for this session
			if(guiseSessionInfo == null) { //if this Guise session isn't registered with this application
				throw new IllegalStateException("Guise restriction session " + restrictionSession + " not registered with Guise application " + this);
			}
			guiseSessionInfo.getTempFileInfos().add(tempFileInfo); //indicate that this temp file is associated with the given session
		}
		return GUISE_ASSETS_TEMP_BASE_PATH.resolve(filename); //create and return a path for the temp asset under the Guise temp path
	}

	//TODO rename all these to XXXAsset() and check both for normal and temp assets, delegating to the Guise class for non-temp assets

	/** The string form of the assets base path. */
	private static final String GUISE_ASSETS_BASE_PATH_STRING = GUISE_ASSETS_BASE_PATH.toString();

	/** The string form of the temp assets base path. */
	private static final String GUISE_ASSETS_TEMP_BASE_PATH_STRING = GUISE_ASSETS_TEMP_BASE_PATH.toString();

	@Override
	public boolean hasAsset(final URIPath path) throws IOException {
		final String pathString = path.normalize().toString(); //get the string form of the normalized path
		if(pathString.startsWith(GUISE_ASSETS_BASE_PATH_STRING)) { //if the path is in the Guise asset tree
			if(pathString.startsWith(GUISE_ASSETS_TEMP_BASE_PATH_STRING)) { //if the path is in the Guise temporary asset tree
				final String filename = pathString.substring(GUISE_ASSETS_TEMP_BASE_PATH_STRING.length()); //determine the filename
				final TempFileInfo tempFileInfo = filenameTempFileInfoMap.get(filename); //get the info for this temp file
				return tempFileInfo != null && tempFileInfo.getTempFile().exists(); //return whether there is a temporary file that exists
			} else { //if the path is for a normal Guise asset
				final String guiseAssetKey = GUISE_ASSETS_BASE_KEY + pathString.substring(GUISE_ASSETS_BASE_PATH_STRING.length()); //determine the Guise asset key
				return Guise.getInstance().hasAsset(guiseAssetKey); //see whether the Guise asset exists
			}
		}
		//TODO add support for normal application assets
		return false; //indicate we couldn't find an asset at the given path
	}

	@Override
	public URL getAssetURL(final URIPath path, final GuiseSession guiseSession) throws IOException {
		final String pathString = path.normalize().toString(); //get the string form of the normalized path
		if(pathString.startsWith(GUISE_ASSETS_BASE_PATH_STRING)) { //if the path is in the Guise asset tree
			if(pathString.startsWith(GUISE_ASSETS_TEMP_BASE_PATH_STRING)) { //if the path is in the Guise temporary asset tree
				final String filename = pathString.substring(GUISE_ASSETS_TEMP_BASE_PATH_STRING.length()); //determine the filename
				final TempFileInfo tempFileInfo = filenameTempFileInfoMap.get(filename); //get the info for this temp file
				if(tempFileInfo == null) { //if there is no temporary file
					return null; //there is no temporary asset
				}
				final GuiseSession restrictionSession = tempFileInfo.getRestrictionSession(); //get the restriction session, if any
				if(restrictionSession != null) { //if this file is restricted to a Guise session
					if(!restrictionSession.equals(guiseSession)) { //compare the restricted session with the given Guise session
						throw new IllegalStateException("Guise temporary asset " + path + " cannot be accessed from the current Guise session.");
					}
				}
				return tempFileInfo.getTempFile().toURI().toURL(); //return a URL to the given temporary asset
			} else { //if the path is for a normal Guise asset
				final String guiseAssetKey = GUISE_ASSETS_BASE_KEY + pathString.substring(GUISE_ASSETS_BASE_PATH_STRING.length()); //determine the Guise asset key
				return Guise.getInstance().getAssetURL(guiseAssetKey); //return a URL to the Guise asset
			}
		}
		//TODO add support for normal application assets
		return null; //indicate we couldn't find an asset at the given path
	}

	@Override
	public ResourceBundle loadResourceBundle(final Theme theme, final Locale locale) throws IOException {
		final ClassLoader loader = getClass().getClassLoader(); //get our class loader
		//default resources
		ResourceBundle resourceBundle = ResourceBundles.getResourceBundle(DEFAULT_RESOURCE_BUNDLE_BASE_NAME, locale, loader, null, resourcesIO); //load the default resource bundle
		//theme resources
		resourceBundle = loadResourceBundle(theme, locale, resourceBundle); //load any resources for this theme and resolving parents
		//application resources
		final String resourceBundleBaseName = getResourceBundleBaseName(); //get the specified resource bundle base name
		//TODO del getLogger().trace("ready to load application resources; resource bundle base name: {}", resourceBundleBaseName);
		if(resourceBundleBaseName != null && !resourceBundleBaseName.equals(DEFAULT_RESOURCE_BUNDLE_BASE_NAME)) { //if a distinct resource bundle base name was specified
			resourceBundle = ResourceBundles.getResourceBundle(resourceBundleBaseName, locale, loader, resourceBundle, resourcesIO); //load the new resource bundle, specifying the current resource bundle as the parent					
		}
		return resourceBundle; //return the resource bundle
	}

	/**
	 * Retrieves a resource bundle from this theme and its resolving parents, if any. If multiple resource bundles are specified in this theme, they will be
	 * chained in no particular order. For each resource that provides both a reference URI and local definitions, the resources at the reference URI will be used
	 * as the resolving parent of the local definitions. If the theme does not specify a resource bundle, the given parent resource bundle will be returned.
	 * @param theme The theme for which to load resources.
	 * @param locale The locale for which resources should be retrieved.
	 * @param parentResourceBundle The resource bundle to serve as the parent, or <code>null</code> if there is no parent resource bundle.
	 * @return The resource bundle for the theme, with parent resource bundles loaded, or the parent resource bundle if the theme specifies no resources.
	 * @throws IOException if there was an error loading a resource bundle.
	 */
	protected ResourceBundle loadResourceBundle(final Theme theme, final Locale locale, final ResourceBundle parentResourceBundle) throws IOException {
		ResourceBundle resourceBundle = parentResourceBundle; //at the end of the chain will be the parent resource bundle
		final Theme parentTheme = theme.getParent(); //get the parent theme
		if(parentTheme != null) { //if there is a parent theme
			resourceBundle = loadResourceBundle(parentTheme, locale, parentResourceBundle); //get the parent resource bundle first and use that as the parent
		}
		for(final Object resources : theme.getResourceResources(locale)) { //for each resources object in the theme
			if(resources instanceof UrfObject && ((UrfObject)resources).getTag().isPresent()) {
				final URIPath resourcesURIPath = URIPath.asPathURIPath(((UrfObject)resources).getTag().orElseThrow(AssertionError::new));
				assert resourcesURIPath != null : "Transitional themes must use path URIs until URF support relative references.";
				final URI resourcesURI = theme.getURI().resolve(resourcesURIPath.toURI().getRawPath());
				resourceBundle = loadResourceBundle(resourcesURI, resourceBundle); //load the resources and insert it into the chain
			} else if(resources instanceof Map) { //if this is embedded resources
				@SuppressWarnings("unchecked")
				final Map<Object, Object> resourceMap = (Map<Object, Object>)resources; //TODO cache this if possible
				if(!resourceMap.isEmpty()) { //if any resources are defined locally
					resourceBundle = new HashMapResourceBundle(resourceMap, resourceBundle); //create a new hash map resource bundle with resources and the given parent and insert it into the chain				
				}
			}
		}
		return resourceBundle; //return the end of the resource bundle chain
	}

	/** A thread-safe cache of softly-referenced resource maps keyed to resource bundle URIs. */
	private static final Map<URI, Map<Object, Object>> cachedResourceMapMap = new DecoratorReadWriteLockMap<URI, Map<Object, Object>>( //TODO restrict this to string keys again
			new PurgeOnWriteSoftValueHashMap<URI, Map<Object, Object>>());

	/**
	 * Loads a resource bundle from the given URI.
	 * @param resourceBundleURI The URI of the resource bundle to load.
	 * @param parentResourceBundle The resource bundle to serve as the parent, or <code>null</code> if there is no parent resource bundle.
	 * @return The loaded resource bundle.
	 * @throws IOException if there was an error loading the resource bundle.
	 */
	protected ResourceBundle loadResourceBundle(final URI resourceBundleURI, ResourceBundle parentResourceBundle) throws IOException {
		Map<Object, Object> resourceMap = cachedResourceMapMap.get(resourceBundleURI); //see if we already have a map representing the resources in the bundle TODO first check to see if the file has changed
		if(resourceMap == null) { //if there is no cached resource map; don't worry about the benign race condition, which at worst will cause the resource bundle to be loaded more than once; blocking would be less efficient
			//TODO del Debug.info("resource bundle cache miss for", resourceBundleURI);
			//TODO make sure this is a TURF file; if not, load the properties from the properties file
			try (final InputStream resourcesInputStream = new BufferedInputStream(getInputStream(resourceBundleURI))) { //get a buffered input stream to the resources
				resourceMap = getResourcesIO().read(resourcesInputStream, resourceBundleURI); //load the resources
				cachedResourceMapMap.put(resourceBundleURI, resourceMap); //cache the map for later
			} catch(final IOException ioException) { //if there was an error loading the resource bundle
				throw new IOException("Error loading resource bundle (" + resourceBundleURI + "): " + ioException.getMessage(), ioException);
			}
		}
		/*TODO del
				else {	//TODO del
					Debug.info("resource bundle cache hit for", resourceBundleURI);			
				}
		*/
		return new HashMapResourceBundle(resourceMap, parentResourceBundle); //create a new hash map resource bundle with resources and the given parent and return it
	}

	@Override
	public Theme loadTheme(final URI themeURI) throws IOException {
		final URI resolvedThemeURI = resolveURI(themeURI); //resolve the theme URI against the application path; getInputStream() will do this to, but we will need this resolved URI later in this method
		final InputStream themeInputStream = getInputStream(resolvedThemeURI); //ask the application to get the input stream, so that the resource can be loaded directly if possible
		if(themeInputStream == null) { //if there is no such theme
			throw new FileNotFoundException("Missing theme resource: " + resolvedThemeURI); //indicate that the theme cannot be found
		}
		final Theme theme;
		try (final InputStream bufferedThemeInputStream = new BufferedInputStream(themeInputStream)) { //get a buffered input stream to the theme
			theme = getThemeIO().read(bufferedThemeInputStream, resolvedThemeURI); //read this theme
		} catch(final IOException ioException) { //if there was an error loading the theme
			throw new IOException("Error loading theme (" + resolvedThemeURI + "): " + ioException.getMessage(), ioException);
		}
		final URI rootThemeURI = GUISE_ROOT_THEME_PATH.toURI(); //get the application-relative URI to the root theme
		final URI resolvedRootThemeURI = resolveURI(rootThemeURI); //get the resolved path URI to the root theme
		if(!resolvedThemeURI.equals(resolvedRootThemeURI)) { //if this is not the root theme, load the parent theme
			URI parentURI = theme.getParentURI(); //get the parent designation, if any TODO detect circular references
			if(parentURI == null) { //if no parent was designated
				parentURI = rootThemeURI; //use the root theme for the parent theme
			}
			final Theme parentTheme = loadTheme(parentURI); //load the parent theme
			theme.setParent(parentTheme); //set the parent theme
		}
		try {
			theme.updateRules(); //update the theme rules
		} catch(final ClassNotFoundException classNotFoundException) { //if a class specified by a rule selector cannot be found
			throw new IOException("Error loading theme (" + resolvedThemeURI + "): " + classNotFoundException.getMessage(), classNotFoundException);
		}
		return theme; //return the theme
	}

	@Override
	public Properties loadProperties(final String propertiesPath) throws IOException {
		final File propertiesFile = new File(getHomeDirectory(), requireNonNull(propertiesPath, "Properties path cannot be null.")); //create the properties file object
		final String extension = findNameExtension(propertiesFile).orElse(null); //get the extension of the properties file
		final boolean isXML; //see if this is an XML file
		if(XML.FILENAME_EXTENSION.equals(extension)) { //if this is an XML file
			isXML = true; //indicate that we should load XML
		} else if(PropertiesFiles.FILENAME_EXTENSION.equals(extension)) { //if this is a properties file
			isXML = false; //indicate that we should load normal properties
		} else { //if this is neither an XML file nor a traditional properties file
			throw new IllegalArgumentException("Unrecognized properties file type: " + propertiesPath);
		}
		final Properties properties = new Properties(); //create a properties file
		try (final InputStream inputStream = new BufferedInputStream(new FileInputStream(propertiesFile))) { //get an input stream to the file
			if(isXML) { //if we're loading XML
				properties.loadFromXML(inputStream); //load the properties file from the XML				
			} else { //if we're loading a traditional properties file
				properties.load(inputStream); //load the traditional properties file				
			}
			return properties; //return the properties we loaded
		}
	}

	/**
	 * Looks up a principal from the given ID. This version returns <code>null</code>.
	 * @param id The ID of the principal.
	 * @return The principal corresponding to the given ID, or <code>null</code> if no principal could be determined.
	 */
	protected Principal getPrincipal(final String id) {
		return null; //the abstract Guise application doesn't know any principals
	}

	/**
	 * Looks up the corresponding password for the given principal. This version returns <code>null</code>.
	 * @param principal The principal for which a password should be returned.
	 * @return The password associated with the given principal, or <code>null</code> if no password is associated with the given principal.
	 */
	protected char[] getPassword(final Principal principal) {
		return null; //the abstract Guise application doesn't know any passwords
	}

	/**
	 * Determines the realm applicable for the resource indicated by the given application path. This version returns the application base path as the realm for
	 * all application paths.
	 * @param applicationPath The relative path of the resource requested.
	 * @return The realm appropriate for the resource, or <code>null</code> if the given resource is not in a known realm.
	 */
	protected String getRealm(final URIPath applicationPath) {
		return getBasePath().toString(); //return the application base path as the realm for all resources
	}

	/**
	 * Checks whether the given principal is authorized to access the resource at the given application path. This version authorized any principal accessing any
	 * application path.
	 * @param applicationPath The relative path of the resource requested.
	 * @param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	 * @param realm The realm with which the resource is associated, or <code>null</code> if the realm is not known.
	 * @return <code>true</code> if the given principal is authorized to access the resource represented by the given application path.
	 */
	protected boolean isAuthorized(final URIPath applicationPath, final Principal principal, final String realm) {
		return true; //default to authorizing access
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version returns an empty set.
	 * </p>
	 */
	@Override
	public Set<String> getFacebookAdminIDs() {
		return emptySet(); //TODO fix to allow storage in the application
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version delegates to {@link #getFacebookAdminIDs()}.
	 * </p>
	 */
	@Override
	public Set<String> getFacebookAdminIDs(final URIPath navigationPath) {
		requireNonNull(navigationPath);
		return getFacebookAdminIDs();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version returns <code>null</code>.
	 * </p>
	 */
	@Override
	public String getFacebookAppID() {
		return null; //TODO fix to allow storage in the application
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version delegates to {@link #getFacebookAppID()}.
	 * </p>
	 */
	@Override
	public String getFacebookAppID(final URIPath navigationPath) {
		requireNonNull(navigationPath);
		return getFacebookAppID();
	}

	/**
	 * Information about a log writer.
	 * @author Garret Wilson
	 */
	private static class LogWriterInfo {

		/** The writer. */
		private final Writer writer;

		/** @return The writer. */
		public Writer getWriter() {
			return writer;
		}

		/** The time at which the writer expires. */
		private long expireTime;

		/** @return The time at which the writer expires. */
		public long getExpireTime() {
			return expireTime;
		}

		/**
		 * Writer and expire time constructor.
		 * @param writer The writer.
		 * @param expireTime The time at which the writer expires.
		 * @throws NullPointerException if the given writer is <code>null</code>.
		 */
		public LogWriterInfo(final Writer writer, final long expireTime) {
			this.writer = requireNonNull(writer, "Writer cannot be null.");
			this.expireTime = expireTime;
		}
	}

	/**
	 * Information about a temporary file.
	 * @author Garret Wilson
	 */
	private static class TempFileInfo {

		/** The file object representing the actual temporary file in the file system. */
		private final File tempFile;

		/** @return The file object representing the actual temporary file in the file system. */
		public File getTempFile() {
			return tempFile;
		}

		/** The session to which this temporary file is restricted, or <code>null</code> if this temporary file is not restricted to a session. */
		private final GuiseSession restrictionSession;

		/** @return The session to which this temporary file is restricted, or <code>null</code> if this temporary file is not restricted to a session. */
		public GuiseSession getRestrictionSession() {
			return restrictionSession;
		}

		/**
		 * Temporary file and restriction session constructor.
		 * @param tempFile The file object representing the actual temporary file in the file system.
		 * @param restrictionSession The session to which this temporary file is restricted, or <code>null</code> if this temporary file is not restricted to a
		 *          session.
		 * @throws NullPointerException if the given temporary file is <code>null</code>.
		 */
		public TempFileInfo(final File tempFile, final GuiseSession restrictionSession) {
			this.tempFile = requireNonNull(tempFile, "Temporary file object cannot be null.");
			this.restrictionSession = restrictionSession; //save the session, if there is one
		}

		@Override
		public int hashCode() {
			return getTempFile().hashCode(); //return the file's hash code
		}

		/** {@inheritDoc} @return <code>true</code> if the other object is another info object for the same temporary file */
		@Override
		public boolean equals(final Object object) {
			return object instanceof TempFileInfo && getTempFile().equals(((TempFileInfo)object).getTempFile()); //see if the other object is a temp file info object for the same file
		}

		@Override
		public String toString() {
			return getTempFile().toString();
		}
	}

	/**
	 * Application-related information about a Guise session associated with this application.
	 * @author Garret Wilson
	 */
	private static class GuiseSessionInfo {

		/** The Guise session to which the information relates. */
		private final GuiseSession guiseSession;

		/** @return The Guise session to which the information relates. */
		public GuiseSession getGuiseSession() {
			return guiseSession;
		}

		/** The synchronized list of information for all files associated with this session. */
		private final List<TempFileInfo> tempFileInfos = synchronizedList(new ArrayList<TempFileInfo>());

		/** @return The synchronized list of information for all files associated with this session. */
		public List<TempFileInfo> getTempFileInfos() {
			return tempFileInfos;
		}

		/**
		 * Guise session constructor.
		 * @param guiseSession The Guise session to which the information relates.
		 * @throws NullPointerException if the given Guise session is <code>null</code>.
		 */
		public GuiseSessionInfo(final GuiseSession guiseSession) {
			this.guiseSession = requireNonNull(guiseSession, "Guise session cannot be null.");
		}

		@Override
		public int hashCode() {
			return getGuiseSession().hashCode(); //return the session's hash code
		}

		/**
		 * {inheritDoc}
		 * <p>
		 * This version considers two object equal if they are both {@link GuiseSessionInfo} objects with the same session.
		 * <p>
		 * @see GuiseSessionInfo#getGuiseSession()
		 */
		@Override
		public boolean equals(final Object object) {
			return object instanceof GuiseSessionInfo && getGuiseSession().equals(((GuiseSessionInfo)object).getGuiseSession()); //see if the other object is a Guise session info object for the same Guise session
		}

		@Override
		public String toString() {
			return "Guise Session info: " + getGuiseSession().toString();
		}
	}
}
