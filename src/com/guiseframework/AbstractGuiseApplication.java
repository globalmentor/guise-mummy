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

import com.garretwilson.beans.BoundPropertyObject;
import com.garretwilson.io.*;
import static com.garretwilson.io.FileConstants.*;
import com.garretwilson.lang.ObjectUtilities;
import static com.garretwilson.lang.ThreadUtilities.*;
import com.garretwilson.net.URIUtilities;
import com.garretwilson.net.http.HTTPNotFoundException;
import com.garretwilson.net.http.HTTPResource;
import com.garretwilson.rdf.RDFResourceIO;
import com.garretwilson.text.W3CDateFormat;
import com.garretwilson.util.*;

import static com.garretwilson.io.FileUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.text.CharacterEncodingConstants.*;
import static com.garretwilson.util.CalendarUtilities.*;
import static com.garretwilson.util.LocaleUtilities.*;
import static com.guiseframework.Guise.*;
import static com.guiseframework.GuiseResourceConstants.*;

import com.guiseframework.component.*;
import com.guiseframework.component.kit.ComponentKit;
import com.guiseframework.context.GuiseContext;
import com.guiseframework.controller.*;
import com.guiseframework.theme.Theme;
import com.guiseframework.view.View;

/**An abstract base class for a Guise application.
This implementation only works with Guise containers that descend from {@link AbstractGuiseContainer}.
@author Garret Wilson
*/
public abstract class AbstractGuiseApplication extends BoundPropertyObject implements GuiseApplication
{

	/**I/O for loading resources.*/
	private final static IO<Resources> resourcesIO=new RDFResourceIO<Resources>(Resources.class, GUISE_NAMESPACE_URI);

		/**@return I/O for loading resources.*/
		protected static IO<Resources> getResourcesIO() {return resourcesIO;}

	/**The Guise container into which this application is installed, or <code>null</code> if the application is not yet installed.*/
	private AbstractGuiseContainer container=null;

		/**@return The Guise container into which this application is installed, or <code>null</code> if the application is not yet installed.*/
		public GuiseContainer getContainer() {return container;}

	/**Creates a new session for the application.
 	This version creates and returns a default session.
	@return A new session for the application
	*/
	public GuiseSession createSession()
	{
		return new DefaultGuiseSession(this);	//create a new default Guise session
	}

	/**Creates a frame for the application.
	This implementation returns a default application frame.
	@return A new frame for the application.
	*/
	public ApplicationFrame<?> createApplicationFrame()
	{
		return new DefaultApplicationFrame();	//return an instance of the default application frame 
	}

	/**The base path of the application, or <code>null</code> if the application is not yet installed.*/
	private String basePath=null;

		/**Reports the base path of the application.
		The base path is an absolute path that ends with a slash ('/'), indicating the base path of the navigation panels.
		@return The base path representing the Guise application, or <code>null</code> if the application is not yet installed.
		*/
		public String getBasePath() {return basePath;}
	
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

	/**Installs the application into the given container at the given base path.
	This method is only package-visible so that it can be accessed by {@link AbstractGuiseContainer}.
	@param container The Guise container into which the application is being installed.
	@param basePath The base path at which the application is being installed.
	@param homeDirectory The home directory of the application.
	@param logDirectory The log directory of the application.
	@param tempDirectory The temporary directory of the application.
	@exception NullPointerException if the container, base path, home directory, log directory, and/or temporary directory is <code>null</code>.
	@exception IllegalArgumentException if the context path is not absolute and does not end with a slash ('/') character.
	@exception IllegalStateException if the application is already installed.
	*/
	void install(final AbstractGuiseContainer container, final String basePath, final File homeDirectory, final File logDirectory, final File tempDirectory)
	{
		if(this.container!=null || this.basePath!=null)	//if we already have a container and/or a base path
		{
			throw new IllegalStateException("Application already installed.");
		}
		checkInstance(container, "Container cannot be null");
		checkInstance(basePath, "Application base path cannot be null");
		if(!isAbsolutePath(basePath) || !isContainerPath(basePath))	//if the path doesn't begin and end with a slash
		{
			throw new IllegalArgumentException("Application base path "+basePath+" does not begin and end with a path separator.");
		}
		this.container=container;	//store the container
		this.basePath=basePath;	//store the base path
		this.homeDirectory=checkInstance(homeDirectory, "Home directory cannot be null.");
		this.logDirectory=checkInstance(logDirectory, "Log directory cannot be null.");
		this.tempDirectory=checkInstance(tempDirectory, "Temporary directory cannot be null.");
//TODO del if not needed		hashCode=ObjectUtilities.hashCode(container.getBaseURI(), basePath);	//create a hash code based upon the base URI of the container and the base path of the application
	}

	/**Uninstalls the application from the given container.
	This method is only package-visible so that it can be accessed by {@link AbstractGuiseContainer}.
	All log writers are closed.
	@param container The Guise container into which the application is being installed.
	@exception IllegalStateException if the application is not installed or is installed into another container.
	*/
	void uninstall(final GuiseContainer container)
	{
		if(this.container==null)	//if we don't have a container
		{
			throw new IllegalStateException("Application not installed.");
		}
		if(this.container!=container)	//if we're installed into a different container
		{
			throw new IllegalStateException("Application installed into different container.");
		}
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
			if(!ObjectUtilities.equals(resourceBundleBaseName, newResourceBundleBaseName))	//if the value is really changing
			{
				final String oldResourceBundleBaseName=resourceBundleBaseName;	//get the old value
				resourceBundleBaseName=newResourceBundleBaseName;	//actually change the value
				firePropertyChange(RESOURCE_BUNDLE_BASE_NAME_PROPERTY, oldResourceBundleBaseName, newResourceBundleBaseName);	//indicate that the value changed
			}			
		}

	/**The absolute or application-relative URI of the application style, or <code>null</code> if the default style should be used.*/
	private URI style;

		/**@return The absolute or application-relative URI of the application style, or <code>null</code> if the default style should be used.*/
		public URI getStyle() {return style;}

		/**Sets the URI of the style of the application.
		This is a bound property.
		@param newStyle The URI of the application style, or <code>null</code> if the default style should be used.
		@see GuiseApplication#STYLE_PROPERTY
		*/
		public void setStyle(final URI newStyle)
		{
			if(!ObjectUtilities.equals(style, newStyle))	//if the value is really changing (compare their values, rather than identity)
			{
				final URI oldStyle=style;	//get the old value
				style=newStyle;	//actually change the value
				firePropertyChange(STYLE_PROPERTY, oldStyle, newStyle);	//indicate that the value changed
			}
		}

	/**The application theme.*/
	private Theme theme;

		/**@return The application theme.*/
		public Theme getTheme() {return theme;}

		/**Sets the theme of the application.
		This is a bound property.
		@param newTheme The new application theme.
		@see #THEME_PROPERTY
		*/
		public void setTheme(final Theme newTheme)
		{
			if(!ObjectUtilities.equals(theme, newTheme))	//if the value is really changing
			{
				final Theme oldTheme=theme;	//get the old value
				theme=newTheme;	//actually change the value
				firePropertyChange(THEME_PROPERTY, oldTheme, newTheme);	//indicate that the value changed
			}
		}

	/**Default constructor.
	This implementation sets the locale to the JVM default.
	*/
	public AbstractGuiseApplication()
	{
		this(Locale.getDefault());	//construct the class with the JVM default locale
	}

	/**Locale constructor.
	@param locale The default application locale.
	*/
	public AbstractGuiseApplication(final Locale locale)
	{
//TODO del		this.defaultLocale=locale;	//set the default locale
		locales=unmodifiableList(asList(locale));	//create an unmodifiable list of locales including only the default locale
	}

	/**The thread-safe list of installed component kits, with later registrations taking precedence*/
	private final List<ComponentKit> componentKitList=new CopyOnWriteArrayList<ComponentKit>();

	/**Installs a component kit.
	Later component kits take precedence over earlier-installed component kits.
	If the component kit is already installed, no action occurs.
	@param componentKit The component kit to install.
	*/
	public void installComponentKit(final ComponentKit componentKit)
	{
		synchronized(componentKitList)	//don't allow anyone to access the list of component kits while we access it
		{
			if(!componentKitList.contains(componentKit))	//if the component kit is not already installed
			{
				componentKitList.add(0, componentKit);	//add the component kit to our list at the front of the list, giving it earlier priority
			}
		}
	}

	/**Uninstalls a component kit.
	If the component kit is not installed, no action occurs.
	@param componentKit The component kit to uninstall.
	*/
	public void uninstallComponentKit(final ComponentKit componentKit)
	{
		componentKitList.remove(componentKit);	//remove the installed component kit
	}

	/**Determines the controller class registered for the given component class.
	This request is delegated to each component kit, with later-installed component kits taking precedence. 
	@param componentClass The class of component that may be registered.
	@return A class of controller registered to render component of the specific class, or <code>null</code> if no controller is registered.
	*/
	protected Class<? extends Controller> getRegisteredControllerClass(final Class<? extends Component> componentClass)
	{
		for(final ComponentKit componentKit:componentKitList)	//for each component kit in our list
		{
			final Class<? extends Controller> controllerClass=componentKit.getRegisteredControllerClass(componentClass);	//ask the component kit for a registered controller class for this component
			if(controllerClass!=null)	//if this component kit gave us a controller class
			{
				return controllerClass;	//return the class
			}
		}
		return null;	//indicate that none of our installed component kits had a controller class registered for the specified component class
	}

	/**Determines the controller class appropriate for the given component class.
	A controller class is located by individually looking up the component class hiearchy for registered controllers.
	@param componentClass The class of component for which a controller should be returned.
	@param allowDefault Whether a default controller for the component class should be accepted.
	@return A class of controller to control the given component class, or <code>null</code> if no controller is registered.
	*/
	@SuppressWarnings("unchecked")	//we programmatically check the super classes and implemented interfaces to make sure they are component classes before casts
	protected Class<? extends Controller> getControllerClass(final Class<? extends Component> componentClass, final boolean allowDefault)	//TODO create a better algorithm that finds all matches and sorts them as to interface/class and distance from Component.class
	{
		Class<? extends Controller> controllerClass=getRegisteredControllerClass(componentClass);	//see if there is a controller class registered for this component type
		if(controllerClass==null)	//if we couldn't find a controller for this class, check the immediate interfaces (except the Component interface)
		{
//TODO del Debug.trace("no luck for", componentClass);
			for(final Class<?> classInterface:componentClass.getInterfaces())	//look at each implemented interface
			{
				if(Component.class.isAssignableFrom(classInterface) && !Component.class.equals(classInterface))	//if the class interface is a component, but is not Component.class
				{
					controllerClass=getRegisteredControllerClass((Class<? extends Component>)classInterface);	//check the immediate interface
					if(controllerClass!=null)	//if we found a controller class
					{
//					TODO del 						Debug.trace("found controller class", controllerClass, "from immediate interface", classInterface);
						break;	//stop looking at the interfaces
					}
				}					
			}
		}
		else
		{
//		TODO del Debug.trace("one was registered for", componentClass, ":", controllerClass);			
		}
		if(controllerClass==null)	//if we still didn't find a controller for this class, check up the class hierarchy
		{
			final Class<?> superClass=componentClass.getSuperclass();	//get the super class of the component
			if(superClass!=null && Component.class.isAssignableFrom(superClass))	//if the super class is a component
			{
//			TODO del 				Debug.trace("checking up the controller hierarchy for", superClass);
				controllerClass=getControllerClass((Class<? extends Component>)superClass, false);	//check the super class

/*TODO del
				if(controllerClass!=null)	//TODO del
				{
					Debug.trace("found controller class", controllerClass);
				}
*/

			}
		}
		if(controllerClass==null)	//if we couldn't find a controller for this class, check the up the interfaces hierarchy
		{
			for(final Class<?> classInterface:componentClass.getInterfaces())	//look at each implemented interface; this results in duplicated checking of immediate interfaces, but the algorithm is more straightforward and this will only happen once for each controller installation
			{
				if(Component.class.isAssignableFrom(classInterface) && !Component.class.equals(classInterface))	//if the class interface is a component, but is not Component.class
				{
//				TODO del 					Debug.trace("checking up the interface hierarchy for", classInterface);
					controllerClass=getControllerClass((Class<? extends Component>)classInterface, false);	//check the interface
					if(controllerClass!=null)	//if we found a controller class
					{
//					TODO del 						Debug.trace("found controller class", controllerClass, "from super interface", classInterface);
						break;	//stop looking at the interfaces
					}
				}					
			}
		}
		if(controllerClass==null && allowDefault)	//if we couldn't find a controller for this class, as a last resort use a controller for Component.class, if there is one
		{
			controllerClass=getRegisteredControllerClass(Component.class);	//see if there is a registered controller for Component.class			
		}
		return controllerClass;	//show which if any controller class we found
	}

	/**Determines the controller appropriate for the given component.
	A controller class is located by individually looking up the component class hiearchy for registered render strategies, at each checking all installed component kits.
	@param <GC> The type of Guise context being used.
	@param <C> The type of component for which a controller is requested.
	@param component The component for which a controller should be returned.
	@return A controller to render the given component, or <code>null</code> if no controller is registered.
	*/
	public <C extends Component<?>> Controller<? extends GuiseContext, ? super C> getController(final C component)
	{
		Class<? extends Component> componentClass=component.getClass();	//get the component class
		final Class<? extends Controller> controllerClass=getControllerClass(componentClass, true);	//walk the hierarchy to see if there is a controller class registered for this component type
		if(controllerClass!=null)	//if we found a controller class
		{
			try
			{
				return (Controller<? extends GuiseContext, ? super C>)controllerClass.newInstance();	//return a new instance of the class
			}
			catch (InstantiationException e)
			{
				Debug.error(e);
				throw new AssertionError(e);	//TODO fix
			}
			catch (IllegalAccessException e)
			{
				Debug.error(e);
				throw new AssertionError(e);	//TODO fix
			}
		}
		return null;	//show that we could not find a registered controller
	}

	/**Determines the view class registered for the given component class.
	This request is delegated to each component kit, with later-installed component kits taking precedence. 
	@param componentClass The class of component that may be registered.
	@return A class of view registered to render component of the specific class, or <code>null</code> if no view is registered.
	*/
	protected Class<? extends View> getRegisteredViewClass(final Class<? extends Component> componentClass)
	{
		for(final ComponentKit componentKit:componentKitList)	//for each component kit in our list
		{
			final Class<? extends View> viewClass=componentKit.getRegisteredViewClass(componentClass);	//ask the component kit for a registered view class for this component
			if(viewClass!=null)	//if this component kit gave us a view class
			{
				return viewClass;	//return the class
			}
		}
		return null;	//indicate that none of our installed component kits had a view class registered for the specified component class
	}

	/**Determines the view class appropriate for the given component class.
	A view class is located by individually looking up the component class hiearchy for registered views.
	@param componentClass The class of component for which a view should be returned.
	@param allowDefault Whether a default view for the component class should be accepted.
	@return A class of view for the given component class, or <code>null</code> if no view is registered.
	*/
	@SuppressWarnings("unchecked")	//we programmatically check the super classes and implemented interfaces to make sure they are component classes before casts
	protected Class<? extends View> getViewClass(final Class<? extends Component> componentClass, final boolean allowDefault)	//TODO create a better algorithm that finds all matches and sorts them as to interface/class and distance from Component.class
	{
		Class<? extends View> viewClass=getRegisteredViewClass(componentClass);	//see if there is a view class registered for this component type
		if(viewClass==null)	//if we couldn't find a view for this class, check the immediate interfaces
		{
			for(final Class<?> classInterface:componentClass.getInterfaces())	//look at each implemented interface
			{
				if(Component.class.isAssignableFrom(classInterface) && !Component.class.equals(classInterface))	//if the class interface is a component, but is not Component.class
				{
					viewClass=getRegisteredViewClass((Class<? extends Component>)classInterface);	//check the immediate interface
					if(viewClass!=null)	//if we found a view class
					{
						break;	//stop looking at the interfaces
					}
				}					
			}
		}
		if(viewClass==null)	//if we still didn't find a view for this class, check up the class hierarchy
		{
			final Class<?> superClass=componentClass.getSuperclass();	//get the super class of the component
			if(superClass!=null && Component.class.isAssignableFrom(superClass))	//if the super class is a component
			{
				viewClass=getViewClass((Class<? extends Component>)superClass, false);	//check the super class
			}
		}
		if(viewClass==null)	//if we couldn't find a view for this class, check up the interface hierarchy
		{
			for(final Class<?> classInterface:componentClass.getInterfaces())	//look at each implemented interface; this results in duplicated checking of immediate interfaces, but the algorithm is more straightforward and this will only happen once for each view installation
			{
				if(Component.class.isAssignableFrom(classInterface) && !Component.class.equals(classInterface))	//if the class interface is a component, but is not Component.class
				{
					viewClass=getViewClass((Class<? extends Component>)classInterface, false);	//check the interface
					if(viewClass!=null)	//if we found a view class
					{
						break;	//stop looking at the interfaces
					}
				}					
			}
		}
		if(viewClass==null && allowDefault)	//if we couldn't find a view for this class, as a last resort use a view for Component.class, if there is one
		{
			viewClass=getRegisteredViewClass(Component.class);	//see if there is a registered view for Component.class			
		}
		return viewClass;	//show which if any view class we found
	}

	/**Determines the view appropriate for the given component.
	A view class is located by individually looking up the component class hiearchy for registered render strategies, at each checking all installed component kits.
	@param <GC> The type of Guise context being used.
	@param <C> The type of component for which a view is requested.
	@param component The component for which a view should be returned.
	@return A view to render the given component, or <code>null</code> if no view is registered.
	*/
	public <C extends Component<?>> View<? extends GuiseContext, ? super C> getView(final C component)
	{
		Class<? extends Component> componentClass=component.getClass();	//get the component class
		final Class<? extends View> viewClass=getViewClass(componentClass, true);	//walk the hierarchy to see if there is a view class registered for this component type
		if(viewClass!=null)	//if we found a view class
		{
			try
			{
				return (View<? extends GuiseContext, ? super C>)viewClass.newInstance();	//return a new instance of the class
			}
			catch (InstantiationException e)
			{
				Debug.error(e);
				throw new AssertionError(e);	//TODO fix
			}
			catch (IllegalAccessException e)
			{
				Debug.error(e);
				throw new AssertionError(e);	//TODO fix
			}
		}
		return null;	//show that we could not find a registered view
	}


//TODO how do we keep the general public from changing the past/destination bindings?

	/**The concurrent map of destinations associated with application context-relative paths.*/
	private final Map<String, Destination> pathDestinationMap=new ConcurrentHashMap<String, Destination>();

		/**Associates a destination with a particular application context-relative path.
		Any existing destination for the given context-relative path is replaced.
		@param path The appplication context-relative path to which the destination should be associated.
		@param destination The description of the destination at the appplication context-relative path.
		@return The destination previously assiciated with the given appplication context-relative path, or <code>null</code> if no destination was previously associated with the path.
		@exception NullPointerException if the path and/or the destination is <code>null</code>.
		@exception IllegalArgumentException if the provided path is absolute.
		*/
		public Destination setDestination(final String path, final Destination destination)	//TODO check to make sure the path is the same as that indicated in the destination object
		{
			checkInstance(path, "Path cannot be null.");
			if(isAbsolutePath(path))	//if the path is absolute
			{
				throw new IllegalArgumentException("Path cannot be absolute: "+path);
			}
			return pathDestinationMap.put(path, checkInstance(destination, "Destination cannot be null."));	//store the association
		}

		/**Associates multiple destinations with application context-relative paths.
		Any existing destinations for the given context-relative path are replaced.
		@param destinations The destinations to set.
		@exception IllegalArgumentException if a provided path is absolute.
		@see #setDestination(String, Destination)
		*/
		public void setDestinations(final List<Destination> destinations)	//TODO clear existing destinations and update API
		{
			for(final Destination destination:destinations)	//for each destination
			{
				setDestination(destination.getPath(), destination);	//associate the destination with the path
			}
		}

		/**Determines the destination associated with the given application context-relative path.
		@param path The address for which a destination should be retrieved.
		@return The destination associated with the given path, or <code>null</code> if no destination is associated with the path. 
		@exception IllegalArgumentException if the provided path is absolute.
		*/
		public Destination getDestination(final String path)
		{
			if(isAbsolutePath(path))	//if the path is absolute
			{
				throw new IllegalArgumentException("Path cannot be absolute: "+path);
			}
			return pathDestinationMap.get(path);	//return the associated destination, if any
		}

		/**Returns an iterable of destinations.
		Any changes to the iterable will not necessarily be reflected in the destinations available to the application.
		@return An iterable to the application's destinations.
		*/
		public Iterable<Destination> getDestinations()
		{
			return pathDestinationMap.values();	//return
		}
		
		/**Determines if there is a destination associated with the given appplication context-relative path.
		@param path The appplication context-relative path.
		@return <code>true</code> if there is destination associated with the given path, or <code>false</code> if no destination is associated with the given path.
		@exception NullPointerException if the path is <code>null</code>.
		@exception IllegalArgumentException if the provided path is absolute.
		*/
		public boolean hasDestination(final String path)
		{
			if(isAbsolutePath(path))	//if the path is absolute
			{
				throw new IllegalArgumentException("Path cannot be absolute: "+path);
			}
			return pathDestinationMap.containsKey(path);	//see if there is a destination associated with this navigation path
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
	public String resolvePath(final String path)
	{
		return resolveURI(createPathURI(path)).toString();	//create a URI for the given path, ensuring that the string only specifies a path, and resolve that URI
	}

	/**Resolves a URI against the application base path.
	Relative paths will be resolved relative to the application base path. Absolute paths will be considered already resolved, as will absolute URIs.
	For an application base path "/path/to/application/", resolving "relative/path" will yield "/path/to/application/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path". Resolving "http://example.com/path" will yield "http://example.com/path".
	@param uri The URI to be resolved.
	@return The URI resolved against the application base path.
	@exception NullPointerException if the given URI is <code>null</code>.
	@see #getBasePath()
	@see #resolvePath(String)
	*/
	public URI resolveURI(final URI uri)
	{
		return URI.create(getBasePath()).resolve(checkInstance(uri, "URI cannot be null."));	//create a URI from the application base path and resolve the given path against it
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
	public String relativizePath(final String path)
	{
		return URIUtilities.relativizePath(getBasePath(), path);	//get the path relative to the application path 
	}

	/**Changes a URI to an application-relative path.
	For an application base path "/path/to/application/", relativizing "http://www.example.com/path/to/application/relative/path" will yield "relative/path"
	@param uri The URI to be relativized.
	@return The URI path relativized to the application base path.
	@exception NullPointerException if the given URI is <code>null</code>.
	@see #getBasePath()
	@see #relativizePath(String)
	*/
	public String relativizeURI(final URI uri)
	{
		return relativizePath(uri.getRawPath());	//relativize the path of the URI TODO make sure the URI is from the correct domain
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
		final String relativeApplicationPath=URIUtilities.relativizePath(getContainer().getBasePath(), getBasePath());	//get the application path relative to the container path 
		return container.hasResource(relativeApplicationPath+resourcePath);	//delegate to the container
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
		final String relativeApplicationPath=URIUtilities.relativizePath(getContainer().getBasePath(), getBasePath());	//get the application path relative to the container path 
		return container.getResourceInputStream(relativeApplicationPath+resourcePath);	//delegate to the container
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
		final URI publicResourcesBaseURI=container.getBaseURI().resolve(getBasePath()+GUISE_PUBLIC_RESOURCE_BASE_PATH);	//get the base URI of Guise public resources
//	TODO del Debug.trace("publicResourcesBaseURI:", publicResourcesBaseURI);
		final URI publicResourceRelativeURI=publicResourcesBaseURI.relativize(absoluteResolvedURI);	//see if the absolute URI is in the application public path
//	TODO del Debug.trace("resourceURI:", resourceURI);		
		if(!publicResourceRelativeURI.isAbsolute())	//if the URI is relative to the application's public resources
		{
			return Guise.getInstance().getGuisePublicResourceInputStream(GUISE_PUBLIC_RESOURCE_BASE_KEY+publicResourceRelativeURI.getPath());	//return an input stream to the resource directly, rather than going through the server
		}
			//check for Guise public temp resources
		final URI publicTempBaseURI=container.getBaseURI().resolve(getBasePath()+GUISE_PUBLIC_TEMP_BASE_PATH);	//get the base URI of Guise public temporary resources
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
	public InputStream getInputStream(final String path) throws IOException
	{
		return getInputStream(createPathURI(path));	//create a URI, verifying that it is a path, and return an input stream to the URI		
	}

	/**Retrieves an output stream to the entity at the given URI.
	The URI is first resolved to the application base path.
	This method supports write access to temporary public resources.
	Write access to resources other than Guise public temporary files is currently unsupported. 
	@param uri A URI to the entity; either absolute or relative to the application.
	@return An output stream to the entity at the given resource URI.
	@exception NullPointerException if the given URI is <code>null</code>.
	@exception IllegalStateException if a Guise public temporary resource was requested that requires a particular Guise session, and the request was not made from the required session.
	@exception FileNotFoundException if a URI to a temporary file was passed before the file was created using {@link #createTempPublicResource(String, String, boolean)}.
	@exception IOException if there was an error connecting to the entity at the given URI.
	@see #resolveURI(URI)
	@see #createTempPublicResource(String, String, boolean)
	*/
	public OutputStream getOutputStream(final URI uri) throws IOException
	{
//TODO del Debug.trace("getting input stream to URI", uri);
		final GuiseContainer container=getContainer();	//get the container
		final URI resolvedURI=resolveURI(uri);	//resolve the URI to the application
//	TODO del Debug.trace("resolved URI:", resolvedURI);
		final URI absoluteResolvedURI=container.getBaseURI().resolve(resolvedURI);	//resolve the URI against the container base URI
//	TODO del Debug.trace("absolute resolved URI:", absoluteResolvedURI);
		final URI publicTempBaseURI=container.getBaseURI().resolve(getBasePath()+GUISE_PUBLIC_TEMP_BASE_PATH);	//get the base URI of the Guise temp resources
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
	@exception FileNotFoundException if a path to a temporary file was passed before the file was created using {@link #createTempPublicResource(String, String, boolean)}.
	@exception IOException if there was an error connecting to the entity at the given URI.
	@see #getOutputStream(URI)
	@see #createTempPublicResource(String, String, boolean)
	*/
	public OutputStream getOutputStream(final String path) throws IOException
	{
		return getOutputStream(createPathURI(path));	//create a URI, verifying that it is a path, and return an output stream to the URI
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

	/**Creates a temporary resource available at a public application navigation path.
	The file will be created in the application's temporary file directory.
	If the resource is restricted to the current Guise session, the resource will be deleted when the current Guise session ends.
	@param baseName The base filename to be used in generating the filename.
	@param extension The extension to use for the temporary file.
	@param restrictionSession The Guise session to which access access to the temporary file should be restricted, or <code>null</code> if there should be no access restriction.
	@return A public application navigation path that can be used to access the resource.
	@exception NullPointerException if the given base name and/or extension is <code>null</code>.
	@exception IllegalArgumentException if the base name is the empty string.
	@exception IOException if there is a problem creating the public resource.
	@see #getTempDirectory()
	@see #hasTempPublicResource(String)
	*/
	public String createTempPublicResource(String baseName, final String extension, final GuiseSession restrictionSession) throws IOException
	{
		if(checkInstance(baseName, "Base name cannot be null.").length()==0)	//if the base name is empty)
		{
			throw new IllegalArgumentException("Base name cannot be the empty string.");
		}
		if(baseName.length()<3)	//if the base name is under three characters long (the temp file creation API requires at least three characters)
		{
			baseName=baseName+"-temp";	//pad the base name to meet the requirements of File.createTempFile()
		}
		final File tempFile=File.createTempFile(baseName, new StringBuilder().append(EXTENSION_SEPARATOR).append(extension).toString(), getTempDirectory());	//create a temporary file in the application's temporary directory
		tempFile.deleteOnExit();	//tell the file it should be deleted when the JVM exits
		final String filename=tempFile.getName();	//get the name of the file
		assert filename.length()>0 : "Name of generated temporary file is missing.";
		final TempFileInfo tempFileInfo=new TempFileInfo(tempFile, restrictionSession);	//create an object to keep track of the file
//TODO del		final String path=GUISE_TEMP_BASE_PATH+filename;	//create a path for the temp resource under the Guise temp path
		filenameTempFileInfoMap.put(filename, tempFileInfo);	//map the filename to the temp file info
		return GUISE_PUBLIC_TEMP_BASE_PATH+filename;	//create and return a path for the temp resource under the Guise temp path
	}

	/**Determines whether this application has a temporary public resource at the given path.
	@param path The application-relative path of the resource.
	@return <code>true</code> if a temporary public resource exists at the given path.
	@exception IOException if there was an error accessing the temporary public resource.
	@see #createTempPublicResource(String, String, boolean)
	*/
	public boolean hasTempPublicResource(final String path) throws IOException
	{
		if(path.startsWith(GUISE_PUBLIC_TEMP_BASE_PATH))	//if the path is in the public temporary tree
		{
			final String filename=path.substring(GUISE_PUBLIC_TEMP_BASE_PATH.length());	//get the filename TODO it would be better to resolve the path, which would fix "../..", etc.
			final TempFileInfo tempFileInfo=filenameTempFileInfoMap.get(filename);	//get the info for this temp file
			if(tempFileInfo!=null)	//if we found the temporary file
			{
				return tempFileInfo.getTempFile().exists();	//return whether the temporary file exists
			}
		}
		return false;	//indicate we couldn't find a public temporary resource at the given path
	}

	/**Returns a URL to the temporary public resource at the given path.
	The given URL represents internal access to the resource and should normally not be presented to users. 
	@param path The application-relative path of the resource.
	@param session The Guise session requesting the resource, or <code>null</code> if there is no session associated with the request.
	@return A URL to the temporary public resource, or <code>null</code> if there is no such temporary public resource.
	@exception IllegalStateException if a Guise public temporary resource was requested that requires a particular Guise session different from the given Guise session.
	@exception IOException if there was an error accessing the temporary public resource.
	@see #createTempPublicResource(String, String, boolean)
	*/
	public URL getTempPublicResourceURL(final String path, final GuiseSession guiseSession) throws IOException
	{
		if(path.startsWith(GUISE_PUBLIC_TEMP_BASE_PATH))	//if the path is in the public temporary tree
		{
			final String filename=path.substring(GUISE_PUBLIC_TEMP_BASE_PATH.length());	//get the filename TODO it would be better to resolve the path, which would fix "../..", etc.
			final TempFileInfo tempFileInfo=filenameTempFileInfoMap.get(filename);	//get the info for this temp file
			if(tempFileInfo!=null)	//if we found the temporary file
			{
				final GuiseSession restrictionSession=tempFileInfo.getRestrictionSession();	//get the restriction session, if any
				if(restrictionSession!=null)	//if this file is restricted to a Guise session
				{
					if(!restrictionSession.equals(guiseSession))	//compare the restricted session with the given Guise session
					{
						throw new IllegalStateException("Guise public temporary resource "+path+" cannot be accessed from the current Guise session.");
					}
				}
				return tempFileInfo.getTempFile().toURI().toURL();	//return a URL to the given temporary public resource
			}
		}
		return null;	//indicate we couldn't find a public temporary resource at the given path
	}

	/**Retrieves a resource bundle for the given locale.
	The resource bundle retrieved will allow hierarchical resolution in the following priority:
	<ol>
		<li>Any resource defined by the application.</li>
		<li>Any resource defined by the theme.</li>
		<li>Any resource defined by a parent theme, including the default theme.</li>
		<li>Any resource defined by default by Guise.</li>
	</ol>
	@param locale The locale for which resources should be retrieved.
	@return A resolving resource bundle based upon the locale.
	@exception MissingResourceException if a resource bundle could not be loaded.
	@see #getResourceBundleBaseName()
	*/
	public ResourceBundle getResourceBundle(final Locale locale)
	{
		final ClassLoader loader=getClass().getClassLoader();	//get our class loader
			//default resources
		ResourceBundle resourceBundle=ResourceBundleUtilities.getResourceBundle(DEFAULT_RESOURCE_BUNDLE_BASE_NAME, locale, loader, null);	//load the default resource bundle
			//theme resources
		final Theme theme=getTheme();	//get the theme, if any
		if(theme!=null)	//if there is a theme
		{
			try
			{
				resourceBundle=getResourceBundle(theme, locale, resourceBundle);	//load any resources for this theme and resolving parents
			}
			catch(final IOException ioException)	//if there is an I/O error, convert it to a missing resource exception
			{
				throw (MissingResourceException)new MissingResourceException(ioException.getMessage(), null, null).initCause(ioException);	//TODO check to see if null is OK for arguments here
			}
		}
			//application resources
		final String resourceBundleBaseName=getResourceBundleBaseName();	//get the specified resource bundle base name
//TODO del Debug.trace("ready to load application resources; resource bundle base name:", resourceBundleBaseName);
		if(resourceBundleBaseName!=null && !resourceBundleBaseName.equals(DEFAULT_RESOURCE_BUNDLE_BASE_NAME))	//if a distinct resource bundle base name was specified
		{
			resourceBundle=ResourceBundleUtilities.getResourceBundle(resourceBundleBaseName, locale, loader, resourceBundle);	//load the new resource bundle, specifying the current resource bundle as the parent					
		}
		return resourceBundle;	//return the resource bundle
	}

	/**Retrieves a resource bundle from this theme and its resolving parents, if any.
	If the theme does not specify a resource bundle, the given parent resource bundle will be returned.
	@param theme The theme for which to load resources.
	@param locale The locale for which resources should be retrieved.
	@param parentResourceBundle The resource bundle to serve as the parent, or <code>null</code> if there is no parent resource bundle.
	@return The resource bundle for the theme, with parent resource bundles loaded, or the parent resource bundle if the theme specifies no resources.
	@exception IOException if there was an error loading a resource bundle.
	*/
	protected ResourceBundle getResourceBundle(final Theme theme, final Locale locale, ResourceBundle parentResourceBundle) throws IOException
	{
		final Theme parentTheme=theme.getParent();	//get the parent theme
		if(parentTheme!=null)	//if there is a parent theme
		{
			parentResourceBundle=getResourceBundle(parentTheme, locale, parentResourceBundle);	//get the parent resource bundle first and use that as the parent
		}
		final URI resourcesURI=theme.getResourcesURI(locale);	//get the resources URI
		if(resourcesURI!=null)	//if there are resources
		{
			return loadResourceBundle(resourcesURI, parentResourceBundle);	//load the resources
		}		
		else	//if the theme specifies no resources appropriate for this locale
		{
			return parentResourceBundle;	//return the parent resource bundle
		}
	}

	/**A synchronized cache of softly-referenced resource maps keyed to resource bundle URIs.*/
	private final static Map<URI, Map<String, Object>> cachedResourceMapMap=synchronizedMap(new SoftValueHashMap<URI, Map<String, Object>>());

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
				//TODO make sure this is an RDF file; if not, load the properties from the properties file
			final InputStream resourcesInputStream=new BufferedInputStream(getInputStream(resourceBundleURI));	//get a buffered input stream to the resources
			try
			{
				final Resources resources=getResourcesIO().read(resourcesInputStream, resourceBundleURI);	//load the resources
				resourceMap=resources.toMap();	//generate a map from the resources
				cachedResourceMapMap.put(resourceBundleURI, resourceMap);	//cache the map for later
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
	protected String getRealm(final String applicationPath)
	{
		return getBasePath();	//return the application base path as the realm for all resouces
	}

	/**Checks whether the given principal is authorized to access the resouce at the given application path.
	This version authorized any principal accessing any application path.
	@param applicationPath The relative path of the resource requested.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param realm The realm with which the resource is associated, or <code>null</code> if the realm is not known.
	@return <code>true</code> if the given principal is authorized to access the resource represented by the given application path.
	*/
	protected boolean isAuthorized(final String applicationPath, final Principal principal, final String realm)
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
}
