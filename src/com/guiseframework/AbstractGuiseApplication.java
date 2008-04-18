package com.guiseframework;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.security.Principal;
import java.text.DateFormat;
import java.util.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import java.util.concurrent.*;

import javax.mail.*;
import javax.mail.Message;

import static com.globalmentor.io.FileConstants.*;
import static com.globalmentor.io.Files.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.java.Threads.*;
import static com.globalmentor.net.URIs.*;
import static com.globalmentor.text.CharacterEncoding.*;
import static com.globalmentor.urf.URF.*;
import static com.globalmentor.util.Calendars.*;
import static com.globalmentor.util.Locales.*;
import static com.guiseframework.Guise.*;
import static com.guiseframework.GuiseResourceConstants.*;

import com.globalmentor.beans.BoundPropertyObject;
import com.globalmentor.io.*;
import com.globalmentor.java.Objects;
import com.globalmentor.mail.MailManager;
import com.globalmentor.marmot.repository.Repository;
import com.globalmentor.net.URIPath;
import com.globalmentor.net.URIs;
import com.globalmentor.text.W3CDateFormat;
import com.globalmentor.urf.*;
import com.globalmentor.util.*;
import com.guiseframework.component.*;
import static com.guiseframework.Resources.*;
import com.guiseframework.theme.Theme;
import static com.guiseframework.theme.Theme.*;

import com.guiseframework.platform.DefaultEnvironment;
import com.guiseframework.platform.Environment;
import com.guiseframework.platform.Platform;

/**An abstract base class for a Guise application.
This implementation only works with Guise containers that descend from {@link AbstractGuiseContainer}.
@author Garret Wilson
*/
public abstract class AbstractGuiseApplication extends BoundPropertyObject implements GuiseApplication
{

	/**I/O for loading themes.*/
	private final static IO<Theme> themeIO=new TypedURFResourceTURFIO<Theme>(Theme.class, THEME_NAMESPACE_URI);	//create I/O for loading the theme

	static
	{
		((TypedURFResourceTURFIO<Theme>)themeIO).registerResourceFactory(RESOURCES_NAMESPACE_URI, new JavaURFResourceFactory(Resources.class.getPackage()));	//add support for resource declarations within a theme
	}

		/**@return I/O for loading themes.*/
		protected static IO<Theme> getThemeIO() {return themeIO;}

	/**I/O for loading resources.*/
	private final static IO<Resources> resourcesIO=new TypedURFResourceTURFIO<Resources>(Resources.class, RESOURCES_NAMESPACE_URI);

		/**@return I/O for loading resources.*/
		protected static IO<Resources> getResourcesIO() {return resourcesIO;}

	/**The application identifier URI.*/
	private URI uri;

		/**Returns the application identifier URI.
		This URI may be but is not guaranteed to be the URI at which the application can be accessed.
		@return The application identifier URI, or <code>null</code> if the identifier is not known.
		*/
		public URI getURI() {return uri;}

	/**The Guise container into which this application is installed, or <code>null</code> if the application is not yet installed.*/
	private AbstractGuiseContainer container=null;

		/**@return The Guise container into which this application is installed, or <code>null</code> if the application is not yet installed.*/
		public GuiseContainer getContainer() {return container;}

	/**The application local environment.*/
	private Environment environment;

		/**@return The application local environment.*/
		public Environment getEnvironment() {return environment;}

		/**Sets the application local environment.
		This method will not normally be called directly from applications.
		This is a bound property.
		@param newEnvironment The new application local environment.
		@exception NullPointerException if the given environment is <code>null</code>.
		@see #ENVIRONMENT_PROPERTY
		*/
		public void setEnvironment(final Environment newEnvironment)
		{
			if(!Objects.equals(environment, newEnvironment))	//if the value is really changing (compare their values, rather than identity)
			{
				final Environment oldEnvironment=environment;	//get the old value
				environment=checkInstance(newEnvironment, "Guise session environment cannot be null.");	//actually change the value
				firePropertyChange(ENVIRONMENT_PROPERTY, oldEnvironment, newEnvironment);	//indicate that the value changed
			}
		}

	/**The properties of the mail manager, or <code>null</code> if the mail properties have not been configured.*/
	private Map<?, ?> mailProperties=null;

		/**Returns the properties of the mail manager.
		This method is guaranteed to return a non-<code>null</code> value after the application is installed.
		@return The properties of the mail manager.
		@exception ConfigurationException if the application is installed into a container but the mail properties has not been configured. 
		*/
		public Map<?, ?> getMailProperties()
		{
			if(isInstalled() && mailProperties==null)	//if the application has been installed but the repositories repository has not yet been set
			{
				throw new ConfigurationException("Repositories repository has not been configured.");
			}
			return mailProperties;	//return the repository for user repositories
		}

		/**Sets properties of the mail manager.
		@param mailProperties The new properties of the mail manager
		@exception NullPointerException if the given properties is <code>null</code>.
		@exception IllegalStateException if the application has already been installed into a container. 
		*/
		public void setMailProperties(final Map<?, ?> mailProperties)
		{
			checkNotInstalled();	//make sure the application has not been installed
			this.mailProperties=unmodifiableMap(checkInstance(mailProperties, "Repository cannot be null."));
		}

	/**The mail manager, or <code>null</code> if the application is not installed or there is no mail defined for this application.*/
	private MailManager mailManager=null;

		/**Retrieves the current mail session.
		@return This application's mail session.
		@exception IllegalStateException if the application has not yet been installed into a container.
		@exception ConfigurationException if mail has not been configured for this application.
		*/
		public Session getMailSession()
		{
			checkInstalled();	//make sure the application has been installed (which will set the mail manager if configured)
			final MailManager mailManager=this.mailManager;	//get the mail manager
			if(mailManager==null)	//if we don't have a mail manager, mail hasn't been configured
			{
				throw new ConfigurationException("Mail has not been configured for the application.");
			}
			return mailManager.getSession();	//return the mail manager's session
		}

		/**Retrieves the queue used to send mail.
		Mail added to this queue will be sent use the application's configured mail protocols.
		@return The queue used for to send mail.
		@exception IllegalStateException if the application has not yet been installed into a container.
		@exception ConfigurationException if mail has not been configured for this application.
		*/
		public Queue<Message> getMailSendQueue()
		{
			checkInstalled();	//make sure the application has been installed (which will set the mail manager if configured)
			final MailManager mailManager=this.mailManager;	//get the mail manager
			if(mailManager==null)	//if we don't have a mail manager, mail hasn't been configured
			{
				throw new ConfigurationException("Mail has not been configured for the application.");
			}
			return mailManager.getSendQueue();	//return the mail manager's send queue
		}

	/**Whether the application applies themes.*/
	private boolean themed=true;

		/**@return Whether the application applies themes.*/
		public boolean isThemed() {return themed;}

		/**Sets whether the application applies themes.
		This is a bound property of type <code>Boolean</code>.
		@param newThemed <code>true</code> if the application should apply themes, else <code>false</code>.
		@see #THEMED_PROPERTY
		*/
		public void setThemed(final boolean newThemed)
		{
			if(themed!=newThemed)	//if the value is really changing
			{
				final boolean oldThemed=themed;	//get the current value
				themed=newThemed;	//update the value
				firePropertyChange(THEMED_PROPERTY, Boolean.valueOf(oldThemed), Boolean.valueOf(newThemed));
			}
		}

	/**Determines the logical navigation path based upon a requested depict path.
	This method must preserve paths beginning with {@value #GUISE_RESERVED_BASE_PATH}.
	This version returns the depict path unmodified.
	@param depictURI The plain absolute depict URI.
	@param depictPath The application-relative depict path.
	@return The application-relative logical navigation path.
	@throws NullPointerException if the given depict URI and/or depict path is <code>null</code>.
	@see #GUISE_RESERVED_BASE_PATH
	*/
	public URIPath getNavigationPath(final URI depictURI, final URIPath depictPath)
	{
		return depictPath;	//by default the navigation path and the depiction path are the same	
	}

	/**Determines the depict URI based upon a navigation URI.
	This method must preserve paths beginning with {@value #GUISE_RESERVED_BASE_PATH}.
	This version returns the navigation URI unmodified.
	@param depictURI The plain absolute depict URI.
	@param navigationURI The logical navigation URI, either absolute or relative to the application.
	@return The depict URI, either absolute or relative to the application.
	@throws NullPointerException if the given depict URI and/or logical URI is <code>null</code>.
	@see #GUISE_RESERVED_BASE_PATH
	*/
	public URI getDepictURI(final URI depictURI, final URI navigationURI)
	{
		return navigationURI;	//by default the navigation URI and the depiction URI are the same	
	}

	/**Creates a new session for the application on the given platform.
 	This version creates and returns a default session.
	@param platform The platform on which this session's objects are depicted.
	@return A new session for the application
	@exception NullPointerException if the given platform is <code>null</code>.
	*/
	public GuiseSession createSession(final Platform platform)
	{
		return new DefaultGuiseSession(this, platform);	//create a new default Guise session
	}

	/**The concurrent map of Guise session info keyed to Guise sessions.*/
	private final Map<GuiseSession, GuiseSessionInfo> guiseSessionInfoMap=new ConcurrentHashMap<GuiseSession, GuiseSessionInfo>();

	/**The concurrent map of Guise sessions keyed to UUIDs.*/
	private final Map<UUID, GuiseSession> uuidGuiseSessionMap=new ConcurrentHashMap<UUID, GuiseSession>(new HashMap<UUID, GuiseSession>());

	/**Registers a session with this application.
	The Guise session has not yet been initialized when this method is called.
	@param guiseSession The Guise session to register with this Guise application.
	@exception IllegalStateException if the given session has alreaady been registered with this application.
	*/
	public void registerSession(final GuiseSession guiseSession)
	{
		if(guiseSessionInfoMap.containsKey(guiseSession))	//if we already have info for this session (there is a race condition here that would allow a session to be registered twice, but that would only prevent error-checking for conditions that logically shouldn't happen anyway, so it's not worth the synchronization overhead to prevent)
		{
			throw new IllegalStateException("Guise session "+guiseSession+" already registered with Guise application "+this);
		}
		uuidGuiseSessionMap.put(guiseSession.getUUID(), guiseSession);	//associate the Guise session with its UUID
		guiseSessionInfoMap.put(guiseSession, new GuiseSessionInfo(guiseSession));	//add new Guise session information
	}

	/**Unregisters a session from this application.
	The Guise session has already been uninitialized when this method is called. 
	@param guiseSession The Guise session to unregister from this Guise application.
	@exception IllegalStateException if the given session is not registered with this application.
	*/
	public void unregisterSession(final GuiseSession guiseSession)
	{
		final GuiseSessionInfo guiseSessionInfo=guiseSessionInfoMap.remove(guiseSession);	//remove the info for this Guise session
		if(guiseSessionInfo==null)	//if there was no Guise session registered
		{
			throw new IllegalStateException("Guise session "+guiseSession+" not registered with Guise application "+this);
		}
		uuidGuiseSessionMap.remove(guiseSession.getUUID());	//remove the Guise session from the UUID map
		final List<TempFileInfo> tempFileInfos=guiseSessionInfo.getTempFileInfos();	//get the temp files registered with this application
		synchronized(tempFileInfos)	//synchronize on the temp file infos for completeness (although no other threads should be accessing the list at this point
		{
			for(final TempFileInfo tempFileInfo:tempFileInfos)	//for each temporary file
			{
				final File tempFile=tempFileInfo.getTempFile();	//get the temporary file
				if(tempFile.exists())	//if this file still exists
				{
					if(!tempFile.delete())	//delete the temporary file; if the file could not be deleted
					{
						Debug.warn("Could not delete temporary file "+tempFile+" associated with Guise session "+guiseSession);
					}
				}
			}
		}		
	}

	/**Retrieves a Guise session for the given UUID.
	@param uuid The UUID of the Guise session to retrieve. 
	@return The Guise session associated with the given UUID, or <code>null</code> if no Guise session is associated with the given UUID.
	@exception NullPointerException if the given UUID is <code>null</code>.
	*/
	public GuiseSession getSession(final UUID uuid)
	{
		return uuidGuiseSessionMap.get(checkInstance(uuid, "UUID cannot be null."));	//return the Guise session, if any, associted with the given UUID
	}

	/**Creates a frame for the application.
	This implementation returns a default application frame.
	@return A new frame for the application.
	*/
	public ApplicationFrame createApplicationFrame()
	{
		return new DefaultApplicationFrame();	//return an instance of the default application frame 
	}

	/**The base path of the application, or <code>null</code> if the application is not yet installed.*/
	private URIPath basePath=null;

		/**Reports the base path of the application.
		The base path is an absolute path that ends with a slash ('/'), indicating the base path of the navigation panels.
		@return The base path representing the Guise application, or <code>null</code> if the application is not yet installed.
		*/
		public URIPath getBasePath() {return basePath;}
	
	/**The home directory shared by all sessions of this application.*/	
	private File homeDirectory=null;

		/**Returns the home directory shared by all sessions of this application.
		This value is not available before the application is installed.
		@return The home directory of the application.
		@exception IllegalStateException if the application has not yet been installed into a container. 
		*/
		public File getHomeDirectory()
		{
			checkInstalled();	//make sure the application has been installed (which will set the home directory)
			assert homeDirectory!=null : "Home directory is null even though application is installed.";
			return homeDirectory;	//return the home directory;
		}

	/**The log directory shared by all sessions of this application.*/	
	private File logDirectory=null;

		/**Returns the log directory shared by all sessions of this application.
		This value is not available before the application is installed.
		@return The log directory of the application.
		@exception IllegalStateException if the application has not yet been installed into a container. 
		*/
		public File getLogDirectory()
		{
			checkInstalled();	//make sure the application has been installed (which will set the log directory)
			assert logDirectory!=null : "Log directory is null even though application is installed.";
			return logDirectory;	//return the log directory;
		}

	/**The temprary directory shared by all sessions of this application.*/	
	private File tempDirectory=null;

		/**Returns the temprary directory shared by all sessions of this application.
		This value is not available before the application is installed.
		@return The temporary directory of the application.
		@exception IllegalStateException if the application has not yet been installed into a container. 
		*/
		public File getTempDirectory()
		{
			checkInstalled();	//make sure the application has been installed (which will set the temporary directory)
			assert tempDirectory!=null : "Temporary directory is null even though application is installed.";
			return tempDirectory;	//return the temporary directory;
		}

	/**The synchronized map of log writer infos keyed to log base filenames.*/
	private final Map<String, LogWriterInfo> baseNameLogWriterInfoMap=synchronizedMap(new HashMap<String, LogWriterInfo>());

	/**Retrieves a writer suitable for recording log information for the application.
	This implementation returns an asynchronous writer that does not block for information to be written when receiving information.
	The given base filename is appended with a representation of the current date.
	If a log writer for the same date is available, it is returned; otherwise, a new log writer is created.
	If the current date is a different day than that used for the current log writer for a given base filename, a new writer is created for the current date.
	@param baseFilename The base filename (e.g. "base.log") that will be used in generating a log file for the current date (e.g. "base 2003-02-01.log").
	@param initializer The encapsulation of any initialization that should be performed on any new writer, or <code>null</code> if no initialization is requested.
	@param uninitializer The encapsulation of any uninitialization that should be performed on any new writer, or <code>null</code> if no uninitialization is requested.
	@see GuiseApplication#getLogDirectory()
	*/	
	public Writer getLogWriter(final String baseFilename, /*TODO fix final CalendarResolution calendarResolution, */final IOOperation<Writer> initializer, final IOOperation<Writer> uninitializer) throws IOException
	{
		synchronized(baseNameLogWriterInfoMap)	//don't allow the map to be used while we look up a writer
		{
			LogWriterInfo logWriterInfo=baseNameLogWriterInfoMap.get(baseFilename);	//get the log writer info
			if(logWriterInfo==null || System.currentTimeMillis()>=logWriterInfo.getExpireTime())	//if there is no log writer information, or this log writer has expired
			{
				if(logWriterInfo!=null && uninitializer!=null)	//if we have an old log writer and something to uninitialize it with
				{
					uninitializer.perform(logWriterInfo.getWriter());	//uninitialize the old log writer
				}				
				final File logDirectory=getLogDirectory();	//get the application log directory
				final DateFormat logFilenameDateFormat=new W3CDateFormat(W3CDateFormat.Style.DATE);	//create a formatter for the log filename
//TODO del; testing				final String logFilename=appendFilename(baseFilename, " "+logFilenameDateFormat.format(new Date())+" "+System.currentTimeMillis());	//create a filename in the form "baseFilename date.ext"
				final String logFilename=appendFilename(baseFilename, " "+logFilenameDateFormat.format(new Date()));	//create a filename in the form "baseFilename date.ext"
				final File logFile=new File(logDirectory, logFilename);	//create a log file object
//TODO add a way to let the initializer know if this is a new log file or just a new writer				final boolean isNewLogFile=!logFile.exists();	//see if this is a new log file
				try
				{
					final Writer writer=new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(logFile, true)), UTF_8);	//create a buffered UTF-8 log writer, appending if the file already exists
					final ThreadGroup guiseSessionThreadGroup=Guise.getInstance().getGuiseSessionThreadGroup(Thread.currentThread());	//get the Guise session thread group
					assert guiseSessionThreadGroup!=null : "Expected to be inside a Guise session thread group when application log writer was requested.";
					final AsynchronousWriterRunnable asynchronousWriterRunnable=new AsynchronousWriterRunnable(writer);	//create a runnable for creating the new asynchronous writer
					call(guiseSessionThreadGroup.getParent(), asynchronousWriterRunnable);	//create an asynchronous writer in the thread group above the Guise session thread group, because the asynchronous writer's thread will live past this session's thread group
					final Writer logWriter=asynchronousWriterRunnable.getWriter();	//get the asynchronous writer that was created
					assert logWriter!=null : "Asynchronous writer runnable did not create asynchronous writer as expected.";
//TODO del when works					final Writer logWriter=new AsynchronousWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(logFile, true)), UTF_8));	//create an asynchronous, buffered UTF-8 log writer, appending if the file already exists
					final Calendar calendar=Calendar.getInstance();	//create a new default calendar for the current date and time
					calendar.add(Calendar.DAY_OF_YEAR, 1);	//go to the next day to find out when this writer should expire
					clearTime(calendar);	//clear the calendar's time, consentrating on just the date (i.e. set the writer to expire at midnight)					
//TODO del; testing					logWriterInfo=new LogWriterInfo(logWriter, System.currentTimeMillis()+10000);					
					logWriterInfo=new LogWriterInfo(logWriter, calendar.getTimeInMillis());	//encapsulate the writer and its expiration time
					baseNameLogWriterInfoMap.put(baseFilename, logWriterInfo);	//replace the old writer with our new writer; the old writer will eventually be garbage collected and, under normal conditions, will close when it is finalized
					if(initializer!=null)	//if we have something to initialize the new log writer with
					{
						initializer.perform(logWriter);	//initialize the new log writer
					}
				}
				catch(final UnsupportedEncodingException unsupportedEncodingException)	//we should always support UTF-8
				{
					throw new AssertionError(unsupportedEncodingException);					
				}
			}
			return logWriterInfo.getWriter();	//return the writer
		}
	}

	/**The runnable whose sole function is to create an asynchronous writer.
	@author Garret Wilson
	*/
	private static class AsynchronousWriterRunnable implements Runnable	//create a runnable for creating the new asynchronous writer
	{
		/**The writer to be decorated.*/
		private final Writer decoratedWriter;
		
		/**The writer that was created, or <code>null</code> if the writer has not yet been created.*/
		private Writer writer=null;

			/**@return The writer that was created, or <code>null</code> if the writer has not yet been created.*/
			public Writer getWriter() {return writer;}

		/**Constructs the class with a writer to decorate.
		@param decoratedWriter The writer to decorate with an asynchronous writer.
		@exception NullPointerException if the given writer is <code>null</code>.
		*/
		public AsynchronousWriterRunnable(final Writer decoratedWriter)
		{
			this.decoratedWriter=checkInstance(decoratedWriter, "Decorated writer cannot be null.");
		}

		/**Creates the writer.*/
		public void run()
		{
			writer=new AsynchronousWriter(decoratedWriter);	//create an asynchronous writer based upon the decorated writer
		}
	};

	/**The hash code, which we'll update after installation. The value is only used after installation, so the initial value is irrelevant.*/
//TODO del if not needed	private int hashCode=-1;

	/**@return Whether this application has been installed into a container at some base path.
	@see #getContainer()
	@see #getBasePath()
	*/
	public boolean isInstalled() {return getContainer()!=null && getBasePath()!=null;}

	/**Checks to ensure that this application is installed.
	@exception IllegalStateException if the application is not installed.
	@see #isInstalled()
	*/
	public void checkInstalled()
	{
		if(!isInstalled())	//if the application is not installed
		{
			throw new IllegalStateException("Application not installed.");
		}
	}

	/**Checks to ensure that this application is not installed.
	@exception IllegalStateException if the application is installed.
	@see #isInstalled()
	*/
	public void checkNotInstalled()
	{
		if(isInstalled())	//if the application is installed
		{
			throw new IllegalStateException("Application already installed.");
		}
	}

	/**Installs the application into the given container at the given base path.
	This method is called by {@link GuiseContainer} and should not be called directly by applications.
	Mail is enabled if mail properties have been configured using {@link #setMailProperties(Map)}.
	@param container The Guise container into which the application is being installed.
	@param basePath The base path at which the application is being installed.
	@param homeDirectory The home directory of the application.
	@param logDirectory The log directory of the application.
	@param tempDirectory The temporary directory of the application.
	@exception NullPointerException if the container, base path, home directory, log directory, and/or temporary directory is <code>null</code>.
	@exception IllegalArgumentException if the context path is not absolute and does not end with a slash ('/') character.
	@exception IllegalStateException if the application is already installed.
	*/
	public void install(final AbstractGuiseContainer container, final URIPath basePath, final File homeDirectory, final File logDirectory, final File tempDirectory)
	{
		if(this.container!=null || this.basePath!=null)	//if we already have a container and/or a base path
		{
			throw new IllegalStateException("Application already installed.");
		}
		checkInstance(container, "Container cannot be null");
		checkInstance(basePath, "Application base path cannot be null");
		if(!basePath.isAbsolute() || !basePath.isCollection())	//if the path doesn't begin and end with a slash
		{
			throw new IllegalArgumentException("Application base path "+basePath+" does not begin and end with a path separator.");
		}
		this.container=container;	//store the container
		this.basePath=basePath;	//store the base path
		this.homeDirectory=checkInstance(homeDirectory, "Home directory cannot be null.");
		this.logDirectory=checkInstance(logDirectory, "Log directory cannot be null.");
		this.tempDirectory=checkInstance(tempDirectory, "Temporary directory cannot be null.");
		if(mailProperties!=null)	//if mail properties have been configured
		{
			try
			{
				mailManager=new MailManager(mailProperties);	//create a new mail manager from the properties
			}
			catch(final NoSuchProviderException noSuchProviderException)	//if a provider couldn't be found
			{
				throw new ConfigurationException(noSuchProviderException);	//indicate that there was a problem configuring the application TODO use a better error; create a ConfigurationStateException
			}
		}
		else	//if there are no mail properties
		{
			Debug.warn("Mail properties not configured.");	//warn that mail isn't configured
		}
	}

	/**Uninstalls the application from the given container.
	All log writers are closed.
	This method is called by {@link GuiseContainer} and should not be called directly by applications.
	@param container The Guise container into which the application is being installed.
	@exception IllegalStateException if the application is not installed or is installed into another container.
	*/
	public void uninstall(final GuiseContainer container)
	{
		if(this.container==null)	//if we don't have a container
		{
			throw new IllegalStateException("Application not installed.");
		}
		if(this.container!=container)	//if we're installed into a different container
		{
			throw new IllegalStateException("Application installed into different container.");
		}
		mailManager.getSendThread().interrupt();	//interrupt the mail manager's send thread
		mailManager=null;	//release the mail manager
		synchronized(baseNameLogWriterInfoMap)	//don't allow the map to be used while we look up a writer
		{
			for(final LogWriterInfo logWriterInfo:baseNameLogWriterInfoMap.values())	//for each log writer info
			{
				try
				{
					logWriterInfo.getWriter().close();	//close this writer
				}
				catch(final IOException ioException)	//if there is an error closing the writer
				{
					Debug.warn(ioException);	//log the warning and continue
				}
			}
			baseNameLogWriterInfoMap.clear();	//remove all log writer information
		}
		this.container=null;	//release the container
		this.basePath=null;	//remove the base path
	}

	/**The identifier for logging to a Data Collection System such as WebTrends, or <code>null</code> if no DCS ID is known.*/
	private String dcsID=null;

		/**@return The identifier for logging to a Data Collection System such as WebTrends, or <code>null</code> if no DCS ID is known.*/
		public String getDCSID() {return dcsID;}

		/**Sets the Data Collection Server log identifier.
		@param dcsID The identifier for logging to a Data Collection System such as WebTrends, or <code>null</code> if no DCS ID is known.
		*/
		public void setDCSID(final String dcsID) {this.dcsID=dcsID;}

	/**The read-only non-empty list of locales supported by the application, with the first locale the default used if a new session cannot determine the users's preferred locale.*/
	private List<Locale> locales;

		/**@return The read-only non-empty list of locales supported by the application, with the first locale the default used if a new session cannot determine the users's preferred locale.*/
		public List<Locale> getLocales() {return locales;}
	
		/**Sets the list of supported locales.
		This is a bound property.
		@param newLocales The new supported application locales.
		@exception NullPointerException if the given list of locales is <code>null</code>.
		@exception IllegalArgumentException if the given list of locales is empty.
		@see #LOCALES_PROPERTY
		*/
		public void setLocales(final List<Locale> newLocales)
		{
			checkInstance(newLocales, "Guise application locales cannot be null.");	//make sure the list is not null
			if(newLocales.isEmpty())	//if there are no locales given
			{
				throw new IllegalArgumentException("Guise application must support at least one locale.");
			}
			if(!locales.equals(newLocales))	//if the value is really changing
			{
				final List<Locale> oldLocales=locales;	//get the old value
				locales=unmodifiableList(new ArrayList<Locale>(newLocales));	//create an unmodifiable copy of the locales
				firePropertyChange(LOCALES_PROPERTY, oldLocales, locales);	//indicate that the value changed
			}
		}
	
	/**The application locale used by default if a new session cannot determine the users's preferred locale.*/
//TODO del	private Locale defaultLocale;

		/**@return The application locale used by default if a new session cannot determine the users's preferred locale.*/
//TODO del		public Locale getDefaultLocale() {return defaultLocale;}

		/**Sets the application locale used by default if a new session cannot determine the users's preferred locale.
		This is a bound property.
		@param newDefaultLocale The new default application locale.
		@exception NullPointerException if the given locale is <code>null</code>.
		@see GuiseApplication#DEFAULT_LOCALE_PROPERTY
		*/
/*TODO del
		public void setDefaultLocale(final Locale newDefaultLocale)
		{
			if(!ObjectUtilities.equals(defaultLocale, newDefaultLocale))	//if the value is really changing (compare their values, rather than identity)
			{
				final Locale oldLocale=defaultLocale;	//get the old value
				defaultLocale=checkInstance(newDefaultLocale, "Guise application default locale cannot be null.");	//actually change the value
				firePropertyChange(DEFAULT_LOCALE_PROPERTY, oldLocale, newDefaultLocale);	//indicate that the value changed
			}
		}
*/

	/**The thread-safe set of locales supported by this application.*/
	private final Set<Locale> supportedLocales=new CopyOnWriteArraySet<Locale>();

		/**@return The thread-safe set of locales supported by this application.*/
		public Set<Locale> getSupportedLocales() {return supportedLocales;}

	/**The base name of the resource bundle to use for this application, or <code>null</code> if no custom resource bundle is specified for this application.*/
	private String resourceBundleBaseName=null;

		/**@return The base name of the resource bundle to use for this application, or <code>null</code> if no custom resource bundle is specified for this application..*/
		public String getResourceBundleBaseName() {return resourceBundleBaseName;}

		/**Changes the resource bundle base name.
		This is a bound property.
		@param newResourceBundleBaseName The new base name of the resource bundle, or <code>null</code> if no custom resource bundle is specified for this application.
		@see GuiseApplication#RESOURCE_BUNDLE_BASE_NAME_PROPERTY
		*/
		public void setResourceBundleBaseName(final String newResourceBundleBaseName)
		{
			if(!Objects.equals(resourceBundleBaseName, newResourceBundleBaseName))	//if the value is really changing
			{
				final String oldResourceBundleBaseName=resourceBundleBaseName;	//get the old value
				resourceBundleBaseName=newResourceBundleBaseName;	//actually change the value
				firePropertyChange(RESOURCE_BUNDLE_BASE_NAME_PROPERTY, oldResourceBundleBaseName, newResourceBundleBaseName);	//indicate that the value changed
			}			
		}

	/**The absolute or application-relative URI of the application style, or <code>null</code> if the default style should be used.*/
	private URI styleURI;

		/**@return The absolute or application-relative URI of the application style, or <code>null</code> if the default style should be used.*/
		public URI getStyleURI() {return styleURI;}

		/**Sets the URI of the style of the application.
		This is a bound property.
		@param newStyle The URI of the application style, or <code>null</code> if the default style should be used.
		@see GuiseApplication#STYLE_URI_PROPERTY
		*/
		public void setStyleURI(final URI newStyle)
		{
			if(!Objects.equals(styleURI, newStyle))	//if the value is really changing (compare their values, rather than identity)
			{
				final URI oldStyle=styleURI;	//get the old value
				styleURI=newStyle;	//actually change the value
				firePropertyChange(STYLE_URI_PROPERTY, oldStyle, newStyle);	//indicate that the value changed
			}
		}

	/**The URI of the application theme, to be resolved against the application base path.*/
	private URI themeURI=GUISE_BASIC_THEME_PATH.toURI();

		/**@return The URI of the application theme, to be resolved against the application base path.*/
		public URI getThemeURI() {return themeURI;}

		/**Sets the URI of the application theme.
		This is a bound property.
		@param newThemeURI The URI of the new application theme.
		@exception NullPointerException if the given theme URI is <code>null</code>.
		@see #THEME_URI_PROPERTY
		*/
		public void setThemeURI(final URI newThemeURI)
		{
			if(!Objects.equals(themeURI, newThemeURI))	//if the value is really changing
			{
				final URI oldThemeURI=themeURI;	//get the old value
				themeURI=checkInstance(newThemeURI, "Theme URI cannot be null.");	//actually change the value
				firePropertyChange(THEME_URI_PROPERTY, oldThemeURI, newThemeURI);	//indicate that the value changed
			}
		}

	/**URI constructor.
	This implementation sets the locale to the JVM default.
	@param uri The URI for the application, which may or may not be the URI at which the application can be accessed.
	@throws NullPointerException if the given URI is <code>null</code>.
	*/
	public AbstractGuiseApplication(final URI uri)
	{
		this.uri=checkInstance(uri, "Application URI cannot be null."); //set the URI
		locales=unmodifiableList(asList(Locale.getDefault()));	//create an unmodifiable list of locales including only the default locale of the JVM
		this.environment=new DefaultEnvironment();	//create a default environment
	}

	/**The concurrent list of destinations which have path patterns specified.*/
	private final List<Destination> pathPatternDestinations=new CopyOnWriteArrayList<Destination>();	

	/**The concurrent map of destinations associated with application context-relative paths.*/
	private final Map<URIPath, Destination> pathDestinationMap=new ConcurrentHashMap<URIPath, Destination>();

		/**Registers a destination so that it can be matched against one or more paths.
		Any existing destinations for the path or path pattern is replaced.
		Existing destinations will take priority if a path matches multiple destination path patterns.
		@param destination The description of the destination at the appplication context-relative path or path pattern.
		@exception NullPointerException if the destination is <code>null</code>.
		*/
		public void addDestination(final Destination destination)
		{
			addDestination(destination, false);	//add this destination with subordinate priority
		}

		/**Registers a destination so that it can be matched against one or more paths.
		Any existing destinations for the path or path pattern is replaced.
		@param destination The description of the destination at the appplication context-relative path or path pattern.
		@param priority Whether this destination takes priority over other destinations when there are multiple matches;
			if this destination has no path pattern, this parameter is ignored.
		@exception NullPointerException if the destination is <code>null</code>.
		*/
		public void addDestination(final Destination destination, final boolean priority)
		{
			final URIPath path=destination.getPath();	//get the destination's path, if there is one
			if(path!=null)	//if this destination has a path
			{
				pathDestinationMap.put(path, destination);	//associate the destination with the path
			}
			else	//if the destination has no path
			{
				assert destination.getPathPattern()!=null : "Destination should have had either a path or a path pattern.";
				if(priority)	//if this destination has priority
				{
					pathPatternDestinations.add(0, destination);	//add this destination to the beginning of the list of destinations with path patterns
				}
				else	//if this destination should not have priority
				{
					pathPatternDestinations.add(destination);	//add this destination to the list of destinations with path patterns
				}
			}
		}

		/**Associates multiple destinations with application context-relative paths or path patterns.
		All destinations are first cleared.
		Any existing destinations for the given context-relative paths are replaced.
		@param destinations The destinations to set.
		*/
		public void setDestinations(final List<Destination> destinations)
		{
			pathDestinationMap.clear();	//clear the map of path/destination associations
			pathPatternDestinations.clear();	//clear the list of path pattern destinations
			for(final Destination destination:destinations)	//for each destination
			{
				addDestination(destination);	//add this destination
			}
		}

		/**Determines the destination associated with the given application context-relative path.
		This method first checks for a destination that matches the exact path as given;
		if no matching path is found, all destinations with path patterns are searched for a match.
		@param path The address for which a destination should be retrieved.
		@return The destination associated with the given path, or <code>null</code> if no destination is associated with the path. 
		@exception IllegalArgumentException if the provided path is absolute.
		*/
		public Destination getDestination(final URIPath path)
		{
			path.checkRelative();	//make sure the path is relative
			Destination destination=pathDestinationMap.get(path);	//get the destination associated with this path, if any
			if(destination==null)	//if there is no destination for this exact path
			{
				for(final Destination pathPatternDestination:pathPatternDestinations)	//look at all the destinations with path patterns
				{
					if(pathPatternDestination.getPathPattern().matcher(path.toString()).matches())	//if this destination's pattern matches the given path
					{
						destination=pathPatternDestination;	//use this destination
						break;	//stop looking at destinations with path patterns
					}
				}
			}
			return destination;	//return the destination we found, if any
		}

		/**Returns an iterable of destinations.
		Any changes to the iterable will not necessarily be reflected in the destinations available to the application.
		@return An iterable to the application's destinations.
		*/
		public Iterable<Destination> getDestinations()
		{
			final List<Destination> destinations=new ArrayList<Destination>(pathDestinationMap.size()+pathPatternDestinations.size());	//create a list large enough to hold all the path-mapped destinations and all the destinations with path patterns
			destinations.addAll(pathDestinationMap.values());	//add the path destinations
			destinations.addAll(pathPatternDestinations);	//add the path pattern destinations
			return destinations;	//return our constructed list of all available destinations
		}

		/**Determines if there is a destination associated with the given appplication context-relative path.
		This method first checks for a destination that matches the exact path as given;
		if no matching path is found, all destinations with path patterns are searched for a match.
		@param path The appplication context-relative path.
		@return <code>true</code> if there is destination associated with the given path, or <code>false</code> if no destination is associated with the given path.
		@exception NullPointerException if the path is <code>null</code>.
		@exception IllegalArgumentException if the provided path is absolute.
		*/
		public boolean hasDestination(final URIPath path)
		{
			path.checkRelative();	//make sure the path is relative
			if(pathDestinationMap.containsKey(path))	//see if there is a destination associated with this navigation path
			{
				return true;	//show that we found an exact match
			}
			for(final Destination pathPatternDestination:pathPatternDestinations)	//look at all the destinations with path patterns
			{
				if(pathPatternDestination.getPathPattern().matcher(path.toString()).matches())	//if this destination's pattern matches the given path
				{
					return true;	//show that we found a pattern match
				}
			}
			return false;	//indicate we couldn't find an exact match or a pattern match
		}	

	/**Resolves a relative or absolute path against the application base path.
	Relative paths will be resolved relative to the application base path. Absolute paths will be be considered already resolved.
	For an application base path "/path/to/application/", resolving "relative/path" will yield "/path/to/application/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path".
	@param path The path to be resolved.
	@return The path resolved against the application base path.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #resolveURI(URI)} should be used instead).
	@see #getBasePath()
	@see #resolveURI(URI)
	*/
	public URIPath resolvePath(final URIPath path)
	{
		return getBasePath().resolve(checkInstance(path, "Path cannot be null."));	//resolve the given path against the base path
	}

	/**Resolves a URI against the application base path.
	Relative paths and {@value URIs#PATH_SCHEME} scheme URIs with relative paths will be resolved relative to the application base path.
	Absolute paths will be considered already resolved, as will absolute URIs.
	For an application base path "/path/to/application/", resolving "path:relative/path" or "relative/path" will yield "/path/to/application/relative/path",
	while resolving "path:/absolute/path" or "/absolute/path" will yield "/absolute/path". Resolving "http://example.com/path" will yield "http://example.com/path".
	@param uri The URI to be resolved.
	@return The URI resolved against the application base path.
	@exception NullPointerException if the given URI is <code>null</code>.
	@see #getBasePath()
	@see #resolvePath(URIPath)
	*/
	public URI resolveURI(URI uri)
	{
		if(PATH_SCHEME.equals(uri.getScheme()))	//if this ia a path: URI
		{
			uri=getPathURIPath(uri).toURI();	//get the URI form of the raw path of the path URI
		}
		return getBasePath().resolve(checkInstance(uri, "URI cannot be null."));	//resolve the given URI against the base path
	}

	/**Changes an absolute path to an application-relative path.
	For an application base path "/path/to/application/", relativizing "/path/to/application/relative/path" will yield "relative/path"
	@param path The path to be relativized.
	@return The path relativized to the application base path.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #resolveURI(URI)} should be used instead).
	@see #getBasePath()
	@see #relativizeURI(URI)
	*/
	public URIPath relativizePath(final URIPath path)
	{
		return getBasePath().relativize(path);	//get the path relative to the application path 
	}

	/**Changes a URI to an application-relative path.
	For an application base path "/path/to/application/", relativizing "http://www.example.com/path/to/application/relative/path" will yield "relative/path"
	@param uri The URI to be relativized.
	@return The URI path relativized to the application base path.
	@exception NullPointerException if the given URI is <code>null</code>.
	@see #getBasePath()
	@see #relativizePath(String)
	*/
	public URIPath relativizeURI(final URI uri)
	{
		return relativizePath(new URIPath(uri.getRawPath()));	//relativize the path of the URI TODO make sure the URI is from the correct domain
	}

	/**Determines the locale-sensitive path of the given resource path.
	Based upon the provided locale, candidate resource paths are checked in the following order:
	<ol>
		<li> <var>resourceBasePath</var> + "_" + <var>language</var> + "_" + <var>country</var> + "_" + <var>variant</var> </li>
		<li> <var>resourceBasePath</var> + "_" + <var>language</var> + "_" + <var>country</var> </li>
		<li> <var>resourceBasePath</var> + "_" + <var>language</var> </li>
	</ol>	 
	@param resourceBasePath An application-relative base path to a resource in the application resource storage area.
	@param locale The locale to use in generating candidate resource names.
	@return The locale-sensitive path to an existing resource based upon the given locale, or <code>null</code> if no resource exists at the given resource base path or any of its locale candidates.
	@exception NullPointerException if the given resource base path and/or locale is <code>null</code>.
	@exception IllegalArgumentException if the given resource path is absolute.
	@exception IllegalArgumentException if the given path is not a valid path.
	@see #hasResource(String)
	*/
	public String getLocaleResourcePath(final String resourceBasePath, final Locale locale)
	{
/*TODO refactor into common method
		final String relativeApplicationPath=relativizePath(getContainer().getBasePath(), getBasePath());	//get the application path relative to the container path
		final String contextRelativeResourcebasePath=relativeApplicationPath+resourceBasePath;	//get the base path relative to the container
*/
		for(int depth=3; depth>=0; --depth)	//try different locales, starting with the most specific
		{
			final String resourceCandidatePath=getLocaleCandidatePath(resourceBasePath, locale, depth);	//get a candidate path for the resource at this locale depth
			if(resourceCandidatePath!=null && hasResource(resourceCandidatePath))	//if we can generate a candidate path for the locale at this depth, and we have that resource
			{
				return resourceCandidatePath;	//return this candidate path
			}
		}
		return null;	//indicate that we were unable to find a resource path for the given locale
	}

	/**Determines if the application has a resource available stored at the given resource path.
	The provided path is first normalized.
	This implementation uses package access to delegate to {@link AbstractGuiseContainer#hasResource(String)}.
	@param resourcePath An application-relative path to a resource in the application resource storage area.
	@return <code>true</code> if a resource exists at the given resource path.
	@exception IllegalArgumentException if the given resource path is absolute.
	@exception IllegalArgumentException if the given path is not a valid path.
	*/
	public boolean hasResource(final String resourcePath)
	{
		checkInstalled();	//make sure we're installed
		final URIPath relativeApplicationPath=getContainer().getBasePath().relativize(getBasePath());	//get the application path relative to the container path 
		return container.hasResource(relativeApplicationPath.toString()+resourcePath);	//delegate to the container
	}

	/**Retrieves an input stream to the resource at the given path.
	The provided path is first normalized.
	This implementation uses package access to delegate to {@link AbstractGuiseContainer#getResourceInputStream(String)}.
	@param resourcePath An application-relative path to a resource in the application resource storage area.
	@return An input stream to the resource at the given resource path, or <code>null</code> if no resource exists at the given resource path.
	@exception IllegalArgumentException if the given resource path is absolute.
	@exception IllegalArgumentException if the given path is not a valid path.
	*/
	public InputStream getResourceInputStream(final String resourcePath)
	{
		checkInstalled();	//make sure we're installed
		final URIPath relativeApplicationPath=getContainer().getBasePath().relativize(getBasePath());	//get the application path relative to the container path 
		return container.getResourceInputStream(relativeApplicationPath.toString()+resourcePath);	//delegate to the container
	}

	/**Retrieves an input stream to the entity at the given URI.
	The URI is first resolved to the application base path.
	If the URI represents one of this application's public resources, this implementation will return an input stream directly from that resource if possible rather than issuing a separate server request.
	This method supports read access to temporary public resources.
	@param uri A URI to the entity; either absolute or relative to the application.
	@return An input stream to the entity at the given resource URI, or <code>null</code> if no entity exists at the given resource path.
	@exception NullPointerException if the given URI is <code>null</code>.
	@exception IllegalStateException if a Guise public temporary resource was requested that requires a particular Guise session, and the request was not made from the required session.
	@exception IOException if there was an error connecting to the entity at the given URI.
	@see #resolveURI(URI)
	*/
	public InputStream getInputStream(final URI uri) throws IOException	//TODO check for the resource: URI scheme
	{
/*TODO decide if we really want this
		If this is a <code>resource:</code> URI representing a private resource, this method delegates to {@link #getResourceInputStream(String)}.
		if(RESOURCE_SCHEME.equals(uri.getScheme()))	//if this is a resource reference URI
		{
			return getResourceInputStream(uri.get)
		}
*/
//TODO del Debug.trace("getting input stream to URI", uri);
		final GuiseContainer container=getContainer();	//get the container
		final URI resolvedURI=resolveURI(uri);	//resolve the URI to the application
//TODO del Debug.trace("resolved URI:", resolvedURI);
		final URI absoluteResolvedURI=container.getBaseURI().resolve(resolvedURI);	//resolve the URI against the container base URI
//TODO del Debug.trace("absolute resolved URI:", absoluteResolvedURI);
			//check for Guise public resources
		final URI publicResourcesBaseURI=container.getBaseURI().resolve(getBasePath().resolve(GUISE_ASSETS_BASE_PATH).toURI());	//get the base URI of Guise public resources
//	TODO del Debug.trace("publicResourcesBaseURI:", publicResourcesBaseURI);
		final URI publicResourceRelativeURI=publicResourcesBaseURI.relativize(absoluteResolvedURI);	//see if the absolute URI is in the application public path
//	TODO del Debug.trace("resourceURI:", resourceURI);		
		if(!publicResourceRelativeURI.isAbsolute())	//if the URI is relative to the application's public resources
		{
			return Guise.getInstance().getAssetInputStream(GUISE_ASSETS_BASE_KEY+publicResourceRelativeURI.getPath());	//return an input stream to the resource directly, rather than going through the server
		}
			//check for Guise public temp resources
		final URI publicTempBaseURI=container.getBaseURI().resolve(getBasePath().resolve(GUISE_ASSETS_TEMP_BASE_PATH).toURI());	//get the base URI of Guise public temporary resources
//	TODO del Debug.trace("publicResourcesBaseURI:", publicResourcesBaseURI);
		final URI publicTempRelativeURI=publicTempBaseURI.relativize(absoluteResolvedURI);	//see if the absolute URI is in the application public temporary path
//	TODO del Debug.trace("resourceURI:", resourceURI);		
		if(!publicTempRelativeURI.isAbsolute())	//if the URI is relative to the application's public temp resources
		{
			final String filename=publicTempRelativeURI.getRawPath();	//get the filename of the temp file
			final TempFileInfo tempFileInfo=filenameTempFileInfoMap.get(filename);	//get the info for this temp file
			if(tempFileInfo!=null)	//if we found the temporary file
			{
				final File tempFile=tempFileInfo.getTempFile();	//get the temp file
				if(tempFile.exists())	//if the temp file exists
				{
					final GuiseSession restrictionSession=tempFileInfo.getRestrictionSession();	//get the restriction session, if any
					if(restrictionSession!=null)	//if this file is restricted to a Guise session
					{
						if(!restrictionSession.equals(Guise.getInstance().getGuiseSession()))	//compare the restricted session with the current Guise session, throwing an exception if there is Guise session
						{
							throw new IllegalStateException("Guise public temporary resource "+uri+" cannot be accessed from the current Guise session.");
						}
					}
					return new FileInputStream(tempFileInfo.getTempFile());	//create an input stream to the temp file
				}
			}
			return null;	//if there is no such temp file info, or the temp file does not exist, indicate that the temporary file does not exist
		}
		return container.getInputStream(resolvedURI);	//resolve the URI to the application and delegate to the container
	}

	/**Retrieves an input stream to the entity at the given path.
	If the URI represents one of this application's public resources, this implementation will return an input stream directly from that resource if possible rather than issuing a separate server request.
	This method supports read access to temporary public resources.
	@param path A path that is either relative to the application context path or is absolute.
	@return An input stream to the entity at the given resource path, or <code>null</code> if no entity exists at the given resource path.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #getInputStream(URI)} should be used instead).
	@exception IllegalStateException if a Guise public temporary resource was requested that requires a particular Guise session, and the request was not made from the required session.
	@exception IOException if there was an error connecting to the entity at the given path.
	@see #getInputStream(URI)
	*/
	public InputStream getInputStream(final URIPath path) throws IOException
	{
		return getInputStream(path.toURI());	//create a URI, verifying that it is a path, and return an input stream to the URI		
/*TODO fix; reverse delegation
		final URIPath basePath=getBasePath();	//get the application base path
		final URIPath resolvedPath=basePath.resolve(path);	//resolve the path against the application base URI
		final URIPath publicTempBasePath=basePath.resolve(GUISE_TEMP_BASE_PATH);	//get the base path of the Guise temp resources
		final URIPath publicTempRelativePath=publicTempBasePath.relativize(resolvedPath);	//see if the resolved path is in the application public path
		if(!publicTempRelativePath.isAbsolute())	//if the path is relative to the application's temp resources
		{
			final String filename=publicTempRelativePath.toString();	//get the filename of the temp file; all temporary files are stored in the same directory, so if this is not a plain filename no file will be found
			final TempFileInfo tempFileInfo=filenameTempFileInfoMap.get(filename);	//get the info for this temp file
			if(tempFileInfo!=null)	//if we found the temporary file
			{
				final GuiseSession restrictionSession=tempFileInfo.getRestrictionSession();	//get the restriction session, if any
				if(restrictionSession!=null)	//if this file is restricted to a Guise session
				{
					if(!restrictionSession.equals(Guise.getInstance().getGuiseSession()))	//compare the restricted session with the current Guise session, throwing an exception if there is Guise session
					{
						throw new IllegalStateException("Guise public temporary resource "+path+" cannot be accessed from the current Guise session.");
					}
				}
				return new FileOutputStream(tempFileInfo.getTempFile());	//create an output stream to the temp file
			}
			else	//if there is no such temp file
			{
				throw new FileNotFoundException("No such Guise public temp file: "+filename);
			}
		}
		else	//if the URI is not an application-relative public temporary resource URI
		{
			throw new UnsupportedOperationException("Access to non-temporary resource URI "+path+" is unsupported.");	//TODO fix
		}
*/
	}

	/**Retrieves an output stream to the entity at the given URI.
	The URI is first resolved to the application base path.
	This method supports write access to temporary public resources.
	Write access to resources other than Guise public temporary files is currently unsupported. 
	@param uri A URI to the entity; either absolute or relative to the application.
	@return An output stream to the entity at the given resource URI.
	@exception NullPointerException if the given URI is <code>null</code>.
	@exception IllegalStateException if a Guise public temporary resource was requested that requires a particular Guise session, and the request was not made from the required session.
	@exception FileNotFoundException if a URI to a temporary file was passed before the file was created using {@link #createTempAsset(String, String, boolean)}.
	@exception IOException if there was an error connecting to the entity at the given URI.
	@see #resolveURI(URI)
	@see #createTempAsset(String, String, boolean)
	*/
	public OutputStream getOutputStream(final URI uri) throws IOException
	{
//TODO del Debug.trace("getting input stream to URI", uri);
		final GuiseContainer container=getContainer();	//get the container
		final URI resolvedURI=resolveURI(uri);	//resolve the URI to the application
//	TODO del Debug.trace("resolved URI:", resolvedURI);
		final URI absoluteResolvedURI=container.getBaseURI().resolve(resolvedURI);	//resolve the URI against the container base URI
//	TODO del Debug.trace("absolute resolved URI:", absoluteResolvedURI);
		final URI publicTempBaseURI=container.getBaseURI().resolve(getBasePath().resolve(GUISE_ASSETS_TEMP_BASE_PATH).toURI());	//get the base URI of the Guise temp resources
//	TODO del Debug.trace("publicResourcesBaseURI:", publicResourcesBaseURI);
		final URI publicTempRelativeURI=publicTempBaseURI.relativize(absoluteResolvedURI);	//see if the absolute URI is in the application public path
//	TODO del Debug.trace("resourceURI:", resourceURI);		
		if(!publicTempRelativeURI.isAbsolute())	//if the URI is relative to the application's temp resources
		{
			final String filename=publicTempRelativeURI.getRawPath();	//get the filename of the temp file
			final TempFileInfo tempFileInfo=filenameTempFileInfoMap.get(filename);	//get the info for this temp file
			if(tempFileInfo!=null)	//if we found the temporary file
			{
				final GuiseSession restrictionSession=tempFileInfo.getRestrictionSession();	//get the restriction session, if any
				if(restrictionSession!=null)	//if this file is restricted to a Guise session
				{
					if(!restrictionSession.equals(Guise.getInstance().getGuiseSession()))	//compare the restricted session with the current Guise session, throwing an exception if there is Guise session
					{
						throw new IllegalStateException("Guise public temporary resource "+uri+" cannot be accessed from the current Guise session.");
					}
				}
				return new FileOutputStream(tempFileInfo.getTempFile());	//create an output stream to the temp file
			}
			else	//if there is no such temp file
			{
				throw new FileNotFoundException("No such Guise public temp file: "+filename);
			}
		}
		else	//if the URI is not an application-relative public temporary resource URI
		{
			throw new UnsupportedOperationException("Access to non-temporary resource URI "+uri+" is unsupported.");	//TODO fix
		}
	}

	/**Retrieves an output stream to the entity at the given path.
	This method supports write access to temporary public resources.
	Write access to resources other than Guise public temporary files is currently unsupported. 
	@param path A path that is either relative to the application context path or is absolute.
	@return An output stream to the entity at the given resource path.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalStateException if a Guise public temporary resource was requested that requires a particular Guise session, and the request was not made from the required session.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #getOutputStream(URI)} should be used instead).
	@exception FileNotFoundException if a path to a temporary file was passed before the file was created using {@link #createTempAsset(String, String, boolean)}.
	@exception IOException if there was an error connecting to the entity at the given URI.
	@see #getOutputStream(URI)
	@see #createTempAsset(String, String, boolean)
	*/
	public OutputStream getOutputStream(final URIPath path) throws IOException
	{
		return getOutputStream(path.toURI());	//create a URI, verifying that it is a path, and return an output stream to the URI
	}

	/**The map of temp file info objects keyed to temporary filenames (not paths).
	Because all temporary files are created in the same directory, there should be no filename conflicts.
	*/
	private final Map<String, TempFileInfo> filenameTempFileInfoMap=new ConcurrentHashMap<String, TempFileInfo>();

/*TODO del
	protected TempFileInfo getTempFileInfo(final String filename)	//TODO comment
	{
		return filenameTempFileInfoMap.get(filename);
	}
*/

	/**Creates a temporary asset available at an application navigation path.
	The file will be created in the application's temporary file directory.
	If the asset is restricted to the current Guise session, the asset will be deleted when the current Guise session ends.
	@param baseName The base filename to be used in generating the filename.
	@param extension The extension to use for the temporary file.
	@param restrictionSession The Guise session to which access access to the temporary file should be restricted, or <code>null</code> if there should be no access restriction.
	@return An application navigation path that can be used to access the asset.
	@exception NullPointerException if the given base name and/or extension is <code>null</code>.
	@exception IllegalArgumentException if the base name is the empty string.
	@exception IllegalStateException if the given restriction session is not registered with this application.
	@exception IOException if there is a problem creating the asset.
	@see #getTempDirectory()
	@see #hasAsset(URIPath)
	*/
	public URIPath createTempAsset(String baseName, final String extension, final GuiseSession restrictionSession) throws IOException
	{
		final File tempFile=createTempFile(baseName, checkInstance(extension, "Extension cannot be null."), getTempDirectory(), true);	//create a temporary file in the application's temporary directory, specifying that it should be deleted on JVM exit
		final String filename=tempFile.getName();	//get the name of the file
		assert filename.length()>0 : "Name of generated temporary file is missing.";
		final TempFileInfo tempFileInfo=new TempFileInfo(tempFile, restrictionSession);	//create an object to keep track of the file
		filenameTempFileInfoMap.put(filename, tempFileInfo);	//map the filename to the temp file info
		if(restrictionSession!=null)	//if file access should be restricted to a session
		{
			final GuiseSessionInfo guiseSessionInfo=guiseSessionInfoMap.get(restrictionSession);	//get info for this session
			if(guiseSessionInfo==null)	//if this Guise session isn't registered with this application
			{
				throw new IllegalStateException("Guise restriction session "+restrictionSession+" not registered with Guise application "+this);
			}
			guiseSessionInfo.getTempFileInfos().add(tempFileInfo);	//indicate that this temp file is associated with the given session
		}
		return GUISE_ASSETS_TEMP_BASE_PATH.resolve(filename);	//create and return a path for the temp asset under the Guise temp path
	}

	//TODO rename all these to XXXAsset() and check both for normal and temp assets, delegating to the Guise class for non-temp assets

	/**The string form of the assets base path.*/
	private final static String GUISE_ASSETS_BASE_PATH_STRING=GUISE_ASSETS_BASE_PATH.toString();

	/**The string form of the temp assets base path.*/
	private final static String GUISE_ASSETS_TEMP_BASE_PATH_STRING=GUISE_ASSETS_TEMP_BASE_PATH.toString();
	
	/**Determines whether this application has an asset at the given path.
	The path is first normalized. 
	This method supports Guise assets and temporary application assets.
	@param path The application-relative path of the asset.
	@return <code>true</code> if an asset exists at the given path.
	@exception IOException if there was an error accessing the asset.
	@see #createTempAsset(String, String, boolean)
	@see Guise#hasAsset(String)
	*/
	public boolean hasAsset(final URIPath path) throws IOException
	{
		final String pathString=path.normalize().toString();	//get the string form of the normalized path
		if(pathString.startsWith(GUISE_ASSETS_BASE_PATH_STRING))	//if the path is in the Guise asset tree
		{
			if(pathString.startsWith(GUISE_ASSETS_TEMP_BASE_PATH_STRING))	//if the path is in the Guise temporary asset tree
			{
				final String filename=pathString.substring(GUISE_ASSETS_TEMP_BASE_PATH_STRING.length());	//determine the filename
				final TempFileInfo tempFileInfo=filenameTempFileInfoMap.get(filename);	//get the info for this temp file
				return tempFileInfo!=null && tempFileInfo.getTempFile().exists();	//return whether there is a temporary file that exists
			}
			else	//if the path is for a normal Guise asset
			{
				final String guiseAssetKey=GUISE_ASSETS_BASE_KEY+pathString.substring(GUISE_ASSETS_BASE_PATH_STRING.length());	//determine the Guise asset key
				return Guise.getInstance().hasAsset(guiseAssetKey);	//see whether the Guise asset exists
			}
		}
			//TODO add support for normal application assets
		return false;	//indicate we couldn't find an asset at the given path
	}

	/**Returns a URL to the asset at the given path.
	The path is first normalized. 
	This method supports Guise assets and temporary application assets.
	The returned URL represents internal access to the asset and should normally not be presented to users. 
	@param path The application-relative path of the asset.
	@param session The Guise session requesting the asset, or <code>null</code> if there is no session associated with the request.
	@return A URL to the asset, or <code>null</code> if there is no such asset.
	@exception IllegalStateException if an asset was requested that requires a particular Guise session different from the given Guise session.
	@exception IOException if there was an error accessing the asset.
	@see #createTempAsset(String, String, boolean)
	@see Guise#getAssetURL(String)
	*/
	public URL getAssetURL(final URIPath path, final GuiseSession guiseSession) throws IOException
	{
		final String pathString=path.normalize().toString();	//get the string form of the normalized path
		if(pathString.startsWith(GUISE_ASSETS_BASE_PATH_STRING))	//if the path is in the Guise asset tree
		{
			if(pathString.startsWith(GUISE_ASSETS_TEMP_BASE_PATH_STRING))	//if the path is in the Guise temporary asset tree
			{
				final String filename=pathString.substring(GUISE_ASSETS_TEMP_BASE_PATH_STRING.length());	//determine the filename
				final TempFileInfo tempFileInfo=filenameTempFileInfoMap.get(filename);	//get the info for this temp file
				if(tempFileInfo==null)	//if there is no temporary file
				{
					return null;	//there is no temporary asset
				}
				final GuiseSession restrictionSession=tempFileInfo.getRestrictionSession();	//get the restriction session, if any
				if(restrictionSession!=null)	//if this file is restricted to a Guise session
				{
					if(!restrictionSession.equals(guiseSession))	//compare the restricted session with the given Guise session
					{
						throw new IllegalStateException("Guise temporary asset "+path+" cannot be accessed from the current Guise session.");
					}
				}
				return tempFileInfo.getTempFile().toURI().toURL();	//return a URL to the given temporary asset
			}
			else	//if the path is for a normal Guise asset
			{
				final String guiseAssetKey=GUISE_ASSETS_BASE_KEY+pathString.substring(GUISE_ASSETS_BASE_PATH_STRING.length());	//determine the Guise asset key
				return Guise.getInstance().getAssetURL(guiseAssetKey);	//return a URL to the Guise asset
			}
		}
			//TODO add support for normal application assets
		return null;	//indicate we couldn't find an asset at the given path
	}

	/**Retrieves a resource bundle for the given theme in the given locale.
	The resource bundle retrieved will allow hierarchical resolution in the following priority:
	<ol>
		<li>Any resource defined by the application.</li>
		<li>Any resource defined by the theme.</li>
		<li>Any resource defined by a parent theme, including the default theme.</li>
		<li>Any resource defined by default by Guise.</li>
	</ol>
	@param theme The current theme in effect.
	@param locale The locale for which resources should be retrieved.
	@return A resolving resource bundle based upon the locale.
	@exception IOException if there was an error loading a resource bundle.
	@see #getResourceBundleBaseName()
	*/
	public ResourceBundle loadResourceBundle(final Theme theme, final Locale locale) throws IOException
	{
		final ClassLoader loader=getClass().getClassLoader();	//get our class loader
			//default resources
		ResourceBundle resourceBundle=ResourceBundles.getResourceBundle(DEFAULT_RESOURCE_BUNDLE_BASE_NAME, locale, loader, null, resourcesIO, null, null);	//load the default resource bundle
			//theme resources
		resourceBundle=loadResourceBundle(theme, locale, resourceBundle);	//load any resources for this theme and resolving parents
			//application resources
		final String resourceBundleBaseName=getResourceBundleBaseName();	//get the specified resource bundle base name
//TODO del Debug.trace("ready to load application resources; resource bundle base name:", resourceBundleBaseName);
		if(resourceBundleBaseName!=null && !resourceBundleBaseName.equals(DEFAULT_RESOURCE_BUNDLE_BASE_NAME))	//if a distinct resource bundle base name was specified
		{
			resourceBundle=ResourceBundles.getResourceBundle(resourceBundleBaseName, locale, loader, resourceBundle, resourcesIO, null, null);	//load the new resource bundle, specifying the current resource bundle as the parent					
		}
		return resourceBundle;	//return the resource bundle
	}

	/**Retrieves a resource bundle from this theme and its resolving parents, if any.
	If multiple resource bundles are specified in this theme, they will be chained in no particular order.
	For each resource that provides both a reference URI and local definitions, the resources at the reference URI will be used as the resolving parent of the local definitions.
	If the theme does not specify a resource bundle, the given parent resource bundle will be returned.
	@param theme The theme for which to load resources.
	@param locale The locale for which resources should be retrieved.
	@param parentResourceBundle The resource bundle to serve as the parent, or <code>null</code> if there is no parent resource bundle.
	@return The resource bundle for the theme, with parent resource bundles loaded, or the parent resource bundle if the theme specifies no resources.
	@exception IOException if there was an error loading a resource bundle.
	*/
	protected ResourceBundle loadResourceBundle(final Theme theme, final Locale locale, final ResourceBundle parentResourceBundle) throws IOException
	{
		ResourceBundle resourceBundle=parentResourceBundle;	//at the end of the chain will be the parent resource bundle
		final Theme parentTheme=theme.getParent();	//get the parent theme
		if(parentTheme!=null)	//if there is a parent theme
		{
			resourceBundle=loadResourceBundle(parentTheme, locale, parentResourceBundle);	//get the parent resource bundle first and use that as the parent
		}
		for(final URFResource resourcesResource:theme.getResourceResources(locale))	//for each resources object in the theme
		{
			final URI resourcesURI=resourcesResource.getURI();	//get the resources reference URI if any
			if(resourcesURI!=null)	//if there are external resources specified
			{
				resourceBundle=loadResourceBundle(resourcesURI, resourceBundle);	//load the resources and insert it into the chain
			}
//TODO del when works			final Map<String, Object> resourceMap=ResourceBundleUtilities.toMap(resourceResource, STRING_NAMESPACE_URI);	//generate a map from the local resources TODO cache this if possible
			if(resourcesResource instanceof Resources)	//if this is a Guise reosurces object
			{
				final Map<String, Object> resourceMap=ResourceBundles.getResourceValue(resourcesResource);	//generate a map from the local resources TODO cache this if possible
				if(!resourceMap.isEmpty())	//if any resources are defined locally
				{
					resourceBundle=new HashMapResourceBundle(resourceMap, resourceBundle);	//create a new hash map resource bundle with resources and the given parent and insert it into the chain				
				}
			}
		}
		return resourceBundle;	//return the end of the resource bundle chain
	}

	/**A thread-safe cache of softly-referenced resource maps keyed to resource bundle URIs.*/
	private final static Map<URI, Map<String, Object>> cachedResourceMapMap=new DecoratorReadWriteLockMap<URI, Map<String,Object>>(new PurgeOnWriteSoftValueHashMap<URI, Map<String,Object>>());

	/**Loads a resource bundle from the given URI.
	@param resourceBundleURI The URI of the resource bundle to load.
	@param parentResourceBundle The resource bundle to serve as the parent, or <code>null</code> if there is no parent resource bundle.
	@return The loaded resource bundle.
	@exception IOException if there was an error loading the resource bundle.
	*/
	protected ResourceBundle loadResourceBundle(final URI resourceBundleURI, ResourceBundle parentResourceBundle) throws IOException
	{
		Map<String, Object> resourceMap=cachedResourceMapMap.get(resourceBundleURI);	//see if we already have a map representing the resources in the bundle TODO first check to see if the file has changed
		if(resourceMap==null)	//if there is no cached resource map; don't worry about the benign race condition, which at worst will cause the resource bundle to be loaded more than once; blocking would be less efficient
		{
//TODO del Debug.info("resource bundle cache miss for", resourceBundleURI);
				//TODO make sure this is a TURF file; if not, load the properties from the properties file
			final InputStream resourcesInputStream=new BufferedInputStream(getInputStream(resourceBundleURI));	//get a buffered input stream to the resources
			try
			{
				final Resources resources=getResourcesIO().read(resourcesInputStream, resourceBundleURI);	//load the resources
//TODO del when works				resourceMap=ResourceBundleUtilities.toMap(resources, STRING_NAMESPACE_URI);	//generate a map from the resources
				resourceMap=ResourceBundles.getResourceValue(resources);	//generate a map from the resources
				cachedResourceMapMap.put(resourceBundleURI, resourceMap);	//cache the map for later
			}
			catch(final IOException ioException)	//if there was an error loading the resource bundle
			{
				throw new IOException("Error loading resource bundle ("+resourceBundleURI+"): "+ioException.getMessage(), ioException);
			}
			finally
			{
				resourcesInputStream.close();	//always close the resources input stream
			}
		}
/*TODO del
		else	//TODO del
		{
			Debug.info("resource bundle cache hit for", resourceBundleURI);			
		}
*/
		return new HashMapResourceBundle(resourceMap, parentResourceBundle);	//create a new hash map resource bundle with resources and the given parent and return it
	}

	/**Loads a theme from the given URI.
	All relative URIs are considered relative to the application.
	If the theme specifies no parent theme, the default parent theme will be assigned unless the theme is the default theme.
	@param themeURI The URI of the theme to load.
	@return A loaded theme with resolving parents loaded as well.
	@exception NullPointerException if the given theme URI is <code>null</code>.
	@throws IOException if there is an error loading the theme or one of its parents.
	*/
	public Theme loadTheme(final URI themeURI) throws IOException
	{
		final URI resolvedThemeURI=resolveURI(themeURI);	//resolve the theme URI against the application path; getInputStream() will do this to, but we will need this resolved URI later in this method
		final InputStream themeInputStream=getInputStream(resolvedThemeURI);	//ask the application to get the input stream, so that the resource can be loaded directly if possible
		if(themeInputStream==null)	//if there is no such theme
		{
			throw new FileNotFoundException("Missing theme resource: "+resolvedThemeURI);	//indicate that the theme cannot be found
		}
		final InputStream bufferedThemeInputStream=new BufferedInputStream(themeInputStream);	//get a buffered input stream to the theme
		try
		{
			final Theme theme=getThemeIO().read(bufferedThemeInputStream, resolvedThemeURI);	//read this theme
			final URI rootThemeURI=GUISE_ROOT_THEME_PATH.toURI();	//get the application-relative URI to the root theme
			final URI resolvedRootThemeURI=resolveURI(rootThemeURI);	//get the resolved path URI to the root theme
			if(!resolvedThemeURI.equals(resolvedRootThemeURI))	//if this is not the root theme, load the parent theme
			{
				URI parentURI=theme.getParentURI();	//get the parent designation, if any TODO detect circular references
				if(parentURI==null)	//if no parent was designated
				{
					parentURI=rootThemeURI;	//use the root theme for the parent theme
				}
				final Theme parentTheme=loadTheme(parentURI);	//load the parent theme
				theme.setParent(parentTheme);	//set the parent theme
			}
			try
			{
				theme.updateRules();	//update the theme rules
			}
			catch(final ClassNotFoundException classNotFoundException)	//if a class specified by a rule selector cannot be found
			{
				throw new IOException("Error loading theme ("+resolvedThemeURI+"): "+classNotFoundException.getMessage(), classNotFoundException);
			}
			return theme;	//return the theme
		}
		catch(final IOException ioException)	//if there was an error loading the theme
		{
			throw new IOException("Error loading theme ("+resolvedThemeURI+"): "+ioException.getMessage(), ioException);
		}
		finally
		{
			bufferedThemeInputStream.close();	//always close the theme input stream
		}				
	}

	/**Loads properties from a file in the home directory.
	The properties can be stored in XML or in the traditional properties format.
	@param propertiesPath The path to the properties file, relative to the application home directory.
	@return The properties loaded from the file at the given path.
	@exception NullPointerException if the given properties path is <code>null</code>.
	@exception IllegalArgumentException if the type of properties file is not recognized.
	@exception IOException if there is an error loading the properties.
	@see #getHomeDirectory()
	*/
	public Properties loadProperties(final String propertiesPath) throws IOException
	{
		final File propertiesFile=new File(getHomeDirectory(), checkInstance(propertiesPath, "Properties path cannot be null."));	//create the properties file object
		final String extension=getExtension(propertiesFile);	//get the extension of the properties file
		final boolean isXML;	//see if this is an XML file
		if(XML_EXTENSION.equals(extension))	//if this is an XML file
		{
			isXML=true;	//indicate that we should load XML
		}
		else if(PROPERTIES_EXTENSION.equals(extension))	//if this is a properties file
		{
			isXML=false;	//indicate that we should load normal properties
		}
		else	//if this is neither an XML file nor a traditional properties file
		{
			throw new IllegalArgumentException("Unrecognized properties file type: "+propertiesPath);				
		}
		final Properties properties=new Properties();	//create a properties file
		final InputStream inputStream=new BufferedInputStream(new FileInputStream(propertiesFile));	//get an input stream to the file
		try
		{
			if(isXML)	//if we're loading XML
			{
				properties.loadFromXML(inputStream);	//load the properties file from the XML				
			}
			else	//if we're loading a traditional properties file
			{
				properties.load(inputStream);	//load the traditional properties file				
			}
			return properties;	//return the properties we loaded
		}
		finally
		{
			inputStream.close();	//always close the input stream
		}
	}

	/**Looks up a principal from the given ID.
	This version returns <code>null</code>. 
	@param id The ID of the principal.
	@return The principal corresponding to the given ID, or <code>null</code> if no principal could be determined.
	*/
	protected Principal getPrincipal(final String id)
	{
		return null;	//the abstract Guise application doesn't know any principals
	}

	/**Looks up the corresponding password for the given principal.
	This version returns <code>null</code>. 
	@param principal The principal for which a password should be returned.
	@return The password associated with the given principal, or <code>null</code> if no password is associated with the given principal.
	*/
	protected char[] getPassword(final Principal principal)
	{
		return null;	//the abstract Guise application doesn't know any passwords
	}

	/**Determines the realm applicable for the resource indicated by the given application path.
	This version returns the application base path as the realm for all application paths.
	@param applicationPath The relative path of the resource requested.
	@return The realm appropriate for the resource, or <code>null</code> if the given resource is not in a known realm.
	*/
	protected String getRealm(final URIPath applicationPath)
	{
		return getBasePath().toString();	//return the application base path as the realm for all resouces
	}

	/**Checks whether the given principal is authorized to access the resouce at the given application path.
	This version authorized any principal accessing any application path.
	@param applicationPath The relative path of the resource requested.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param realm The realm with which the resource is associated, or <code>null</code> if the realm is not known.
	@return <code>true</code> if the given principal is authorized to access the resource represented by the given application path.
	*/
	protected boolean isAuthorized(final URIPath applicationPath, final Principal principal, final String realm)
	{
		return true;	//default to authorizing access
	}

	/**Information about a log writer.
	@author Garret Wilson
	*/
	private static class LogWriterInfo
	{
		/**The writer.*/
		private final Writer writer;

			/**@return The writer.*/
			public Writer getWriter() {return writer;}

		/**The time at which the writer expires.*/
		private long expireTime;

			/**@return The time at which the writer expires.*/
			public long getExpireTime() {return expireTime;}

		/**Writer and expire time constructor.
		@param writer The writer.
		@param expireTime The time at which the writer expires.
		@exception NullPointerException if the given writer is <code>null</code>.
		*/
		public LogWriterInfo(final Writer writer, final long expireTime)
		{
			this.writer=checkInstance(writer, "Writer cannot be null.");
			this.expireTime=expireTime;
		}
	}

	/**Information about a temporary file.
	@author Garret Wilson
	*/
	private static class TempFileInfo
	{

		/**The file object representing the actual temprary file in the file system.*/
		private final File tempFile;

			/**@return The file object representing the actual temporary file in the file system.*/
			public File getTempFile() {return tempFile;}

		/**The session to which this temporary file is restricted, or <code>null</code> if this temporary file is not restricted to a session.*/
		private final GuiseSession restrictionSession;

			/**@return The session to which this temporary file is restricted, or <code>null</code> if this temporary file is not restricted to a session.*/
			public GuiseSession getRestrictionSession() {return restrictionSession;}

		/**Temporary file and restriction session constructor.
		@param tempFile The file object representing the actual temprary file in the file system.
		@param restrictionSession The session to which this temporary file is restricted, or <code>null</code> if this temporary file is not restricted to a session.
		@exception NullPointerException if the given temporary file is <code>null</code>.
		*/
		public TempFileInfo(final File tempFile, final GuiseSession restrictionSession)
		{
			this.tempFile=checkInstance(tempFile, "Temporary file object cannot be null.");
			this.restrictionSession=restrictionSession;	//save the session, if there is one
		}

		/**@return A hash code for the object.*/
		public int hashCode()
		{
			return getTempFile().hashCode();	//return the file's hash code
		}

		/**Determines whether this object is equal to another.
		@return <code>true</code> if the other object is another info object for the same temporary file.
		*/
		public boolean equals(final Object object)
		{
			return object instanceof TempFileInfo && getTempFile().equals(((TempFileInfo)object).getTempFile());	//see if the other object is a temp file info object for the same file
		}

		/**@return A string version of this object.*/
		public String toString()
		{
			return getTempFile().toString();
		}
	}

	/**Application-related information about a Guise session associated with this application.
	@author Garret Wilson
	*/
	private static class GuiseSessionInfo
	{

		/**The Guise session to which the information relates.*/
		private final GuiseSession guiseSession;

			/**@return The Guise session to which the information relates.*/
			public GuiseSession getGuiseSession() {return guiseSession;}

		/**The synchronized list of information for all files associated with this session.*/
		private final List<TempFileInfo> tempFileInfos=synchronizedList(new ArrayList<TempFileInfo>());

			/**@return The synchronized list of information for all files associated with this session.*/
			public List<TempFileInfo> getTempFileInfos() {return tempFileInfos;}

		/**Guise session constructor.
		@param guiseSession The Guise session to which the information relates.
		@exception NullPointerException if the given Guise session is <code>null</code>.
		*/
		public GuiseSessionInfo(final GuiseSession guiseSession)
		{
			this.guiseSession=checkInstance(guiseSession, "Guise session cannot be null.");
		}

		/**@return A hash code for the object.*/
		public int hashCode()
		{
			return getGuiseSession().hashCode();	//return the session's hash code
		}

		/**Determines whether this object is equal to another.
		@return <code>true</code> if the other object is another info object for the same Guise session.
		*/
		public boolean equals(final Object object)
		{
			return object instanceof GuiseSessionInfo && getGuiseSession().equals(((GuiseSessionInfo)object).getGuiseSession());	//see if the other object is a Guise session info object for the same Guise session
		}

		/**@return A string version of this object.*/
		public String toString()
		{
			return "Guise Session info: "+getGuiseSession().toString();
		}
	}
}
