package com.guiseframework;

import java.beans.PropertyChangeEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.security.Principal;
import java.text.MessageFormat;

import static java.text.MessageFormat.*;
import java.util.*;
import static java.util.Collections.*;

import com.garretwilson.beans.*;
import com.garretwilson.event.PostponedEvent;
import com.garretwilson.io.BOMInputStreamReader;
import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.net.URIPath;
import com.garretwilson.urf.*;
import com.garretwilson.urf.ploop.PLOOPURFProcessor;
import com.garretwilson.util.*;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.Orientation;
import com.guiseframework.event.*;
import com.guiseframework.geometry.Extent;
import com.guiseframework.input.*;
import com.guiseframework.model.*;
import com.guiseframework.platform.Platform;
import com.guiseframework.prototype.*;
import com.guiseframework.style.*;
import com.guiseframework.theme.Theme;

import static com.garretwilson.io.FileConstants.*;
import static com.garretwilson.io.FileUtilities.*;
import static com.garretwilson.io.WriterUtilities.*;
import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.CharSequenceUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.text.CharacterConstants.*;
import static com.garretwilson.text.CharacterEncodingConstants.*;
import static com.garretwilson.text.FormatUtilities.*;
import static com.guiseframework.Resources.*;
import static com.guiseframework.theme.Theme.*;

/**An abstract implementation that keeps track of the components of a user session.
@author Garret Wilson
*/
public abstract class AbstractGuiseSession extends BoundPropertyObject implements GuiseSession
{

	/**The unique identifier of this session.*/
	private final UUID uuid;

		/**@return The unique identifier of this session.*/
		public UUID getUUID() {return uuid;}

	/**The Guise application to which this session belongs.*/
	private final GuiseApplication application;

		/**@return The Guise application to which this session belongs.*/
		public GuiseApplication getApplication() {return application;}

	/**The writer for writing to the log file.*/
	private Writer logWriter;

		/**@return The writer for writing to the log file, which may not be thread-safe.*/
		public Writer getLogWriter() {return logWriter;}

		/**Sets the log writer.
		@param logWriter The writer for writing to the log file, which may not be thread-safe.
		@exception NullPointerException if the given log writer is <code>null</code>.
		*/
		public void setLogWriter(final Writer logWriter)
		{
			this.logWriter=checkInstance(logWriter, "Log writer cannot be null.");
		}

	/**The base URI of the session.*/
	private URI baseURI;

		/**Reports the base URI of the session.
		The base URI is an absolute URI that ends with the base path of the application, which ends with a slash ('/').
		The session base URI may be different for different sessions, and may not be equal to the application base path resolved to the container's base URI.
		@return The base URI representing the Guise session.
		*/
		public URI getBaseURI() {return baseURI;}

		/**Sets the base URI of the session.
		The raw path of the base URI must be equal to the application base path.
		@param baseURI The new base URI of the session.
		@exception NullPointerException if the given base URI is <code>null</code>.
		@exception IllegalArgumentException if the raw path of the given base URI is not equal to the application base path.
		*/
		public void setBaseURI(final URI baseURI)
		{
			if(!this.baseURI.equals(checkInstance(baseURI, "Session base URI cannot be null.")))	//if a new base URI is given
			{
				if(!baseURI.getRawPath().equals(getApplication().getBasePath()))	//if the path of the base URI is not the application base path
				{
					throw new IllegalArgumentException("Session base URI path "+baseURI.getRawPath()+" does not equal application base path "+getApplication().getBasePath());				
				}
				this.baseURI=baseURI;	//save the new base URI
			}
		}

	/**The application frame, initialized during {@link #initialize()}.*/
	private ApplicationFrame applicationFrame=null;

		/**Returns the application frame, which is available after {@link #initialize()} has been called.
		This method must not be called before initialization has occurred.
		@return The application frame.
		@exception IllegalStateException if this session has not yet been initialized.
		*/
		public ApplicationFrame getApplicationFrame()
		{
			if(applicationFrame==null)	//if this session has not yet been initialized
			{
				throw new IllegalStateException("Guise session "+this+" has not yet been initialized and therefore has no application frame.");
			}
			return applicationFrame;	//return the application frame
		}

	/**The cache of components keyed to component destinations.*/
	private final Map<ComponentDestination, Component> destinationComponentMap=synchronizedMap(new HashMap<ComponentDestination, Component>());

	/**The map of preference resource descriptions keyed to classes. This is a temporary implementation that will later be replaced with a backing store based upon the current principal.*/
	private final ReadWriteLockMap<Class<?>, URFResource> classPreferencesMap=new DecoratorReadWriteLockMap<Class<?>, URFResource>(new HashMap<Class<?>, URFResource>());

		/**Retrieves the saved preference properties for a given class.
		@param objectClass The class for which preference properties should be returned.
		@return The saved preference properties for the given class.
		@exception NullPointerException if the given class is <code>null</code>.
		@exception IOException if there was an error retrieving preferences.
		*/
		public URFResource getPreferences(final Class<?> objectClass) throws IOException
		{
			URFResource preferences=classPreferencesMap.get(checkInstance(objectClass, "Class cannot be null."));	//get the preferences stored in the map
			if(preferences==null)	//if no preferences are stored in the map
			{
				preferences=new DefaultURFResource();	//create a default set of preference properties				
/*TODO del if we decide to store resource copies; change map to concurrent map
				classPreferencesMap.writeLock().lock();	//get a write lock on the preferences map
				try
				{
					preferences=classPreferencesMap.get(objectClass);	//try again to get the preferences stored in the map
					if(preferences==null)	//if preferences are still not stored in the map
					{
						preferences=new DefaultRDFResource();	//create a default set of preference properties
						classPreferencesMap.put(objectClass, preferences);	//store the preferences in the map
					}
				}
				finally
				{
					classPreferencesMap.writeLock().unlock();	//always release the write lock on the preferences map
				}
*/
			}
			return preferences;	//return the preferences we found for this class
		}

		/**Saves preference properties for a given class.
		@param objectClass The class for which preference properties should be saved.
		@param preferences The preferences to save for the given class.
		@exception NullPointerException if the given class and/or preferences is <code>null</code>.
		@exception IOException if there was an error storing preferences.
		*/
		public void setPreferences(final Class<?> objectClass, final URFResource preferences) throws IOException
		{
			classPreferencesMap.put(checkInstance(objectClass, "Class cannot be null."), new DefaultURFResource(checkInstance(preferences, "Preferences cannot be null.")));	//store a copy of the preferences in the map
		}

	/**The platform on which Guise objects are depicted.*/
	private final Platform platform;

		/**@return The platform on which Guise objects are depicted.*/
		public Platform getPlatform() {return platform;}

	/**The strategy for processing input, or <code>null</code> if this session has no input strategy.*/
	private InputStrategy inputStrategy=null;

		/**@return The strategy for processing input, or <code>null</code> if this session has no input strategy.*/
		public InputStrategy getInputStrategy() {return inputStrategy;}

		/**Sets the strategy for processing input.
		This is a bound property.
		@param newInputStrategy The new strategy for processing input, or <code>null</code> if this session is to have no input strategy.
		@see #INPUT_STRATEGY_PROPERTY
		*/
		public void setInputStrategy(final InputStrategy newInputStrategy)
		{
			if(!ObjectUtilities.equals(inputStrategy, newInputStrategy))	//if the value is really changing
			{
				final InputStrategy oldInputStrategy=inputStrategy;	//get the current value
				inputStrategy=newInputStrategy;	//update the value
				firePropertyChange(INPUT_STRATEGY_PROPERTY, oldInputStrategy, newInputStrategy);
			}
		}

	/**The current session locale.*/
	private Locale locale;

		/**@return The current session locale.*/
		public Locale getLocale() {return locale;}

		/**Sets the current session locale.
		The default orientation will be updated if needed to reflect the new locale.
		This is a bound property.
		@param newLocale The new session locale.
		@exception NullPointerException if the given locale is <code>null</code>.
		@see GuiseSession#LOCALE_PROPERTY
		@see #setOrientation(Orientation)
		*/
		public void setLocale(final Locale newLocale)
		{
			if(!ObjectUtilities.equals(locale, newLocale))	//if the value is really changing (compare their values, rather than identity)
			{
				final Locale oldLocale=locale;	//get the old value
				locale=checkInstance(newLocale, "Guise session locale cannot be null.");	//actually change the value
				releaseResourceBundle();	//release the resource bundle, as the new locale may indicate that new resources should be used
				firePropertyChange(LOCALE_PROPERTY, oldLocale, newLocale);	//indicate that the value changed
				setOrientation(Orientation.getOrientation(locale));	//update the orientation based upon the new locale
			}
		}

		/**Requests that the locale be changed to one of the given locales.
		Each of the locales in the list are examined in order, and the first one supported by the application is used.
		A requested locale is accepted if a more general locale is supported. (i.e. <code>en-US</code> is accepted if <code>en</code> is supported.)
		@param requestedLocales The locales requested, in order of preference.
		@return The accepted locale (which may be a variation of this locale), or <code>null</code> if none of the given locales are supported by the application.
		@see GuiseApplication#getSupportedLocales()
		@see #setLocale(Locale)
		*/
		public Locale requestLocale(final List<Locale> requestedLocales)
		{
			final List<Locale> supportedLocales=getApplication().getLocales();	//get the application's supported locales
//TODO del when works			final Set<Locale> supportedLocales=getApplication().getSupportedLocales();	//get the application's supported locales	TODO maybe don't expose the whole set
			for(final Locale requestedLocale:requestedLocales)	//for each requested locale
			{
				Locale acceptedLocale=null;	//we'll determine if any variations of the requested locale is supported
				if(supportedLocales.contains(requestedLocale))	//if the application supports this local 
				{
					acceptedLocale=requestedLocale;	//accept this locale as-is
				}
				if(acceptedLocale==null && requestedLocale.getVariant().length()>0)	//if the requested locale specifies a variant, see if there is a more general supported locale
				{
					final Locale languageCountryLocale=new Locale(requestedLocale.getLanguage(), requestedLocale.getCountry());	//create a more general locale with just the language and country
					if(supportedLocales.contains(languageCountryLocale))	//if the application supports this locale 
					{
						acceptedLocale=languageCountryLocale;	//accept this more general locale
					}
				}
				if(acceptedLocale==null && requestedLocale.getCountry().length()>0)	//if the requested locale specifies a country, see if there is an even more general supported locale
				{
					final Locale languageLocale=new Locale(requestedLocale.getLanguage());	//create a more general locale with just the language
					if(supportedLocales.contains(languageLocale))	//if the application supports this locale 
					{
						acceptedLocale=languageLocale;	//accept this more general locale
					}
				}
				if(acceptedLocale!=null)	//if we were able to find a supported locale
				{
					setLocale(acceptedLocale);	//change to this locale
					return acceptedLocale;	//indicate which locale was accepted
				}
			}
			return null;	//indicate that the application supports none of the requested locales and none of their more general variations
		}

	/**The default internationalization orientation of components for this session.*/
	private Orientation orientation=Orientation.LEFT_TO_RIGHT_TOP_TO_BOTTOM;
		
		/**@return The default internationalization orientation of components for this session.*/
		public Orientation getOrientation() {return orientation;}

		/**Sets the default orientation.
		This is a bound property
		@param newOrientation The new default internationalization orientation of components for this session.
		@exception NullPointerException if the given orientation is <code>null</code>.
		@see GuiseSession#ORIENTATION_PROPERTY
		*/
		public void setOrientation(final Orientation newOrientation)
		{
			if(!ObjectUtilities.equals(orientation, newOrientation))	//if the value is really changing
			{
				final Orientation oldOrientation=checkInstance(orientation, "Orientation cannot be null");	//get the old value
				orientation=newOrientation;	//actually change the value
				firePropertyChange(ORIENTATION_PROPERTY, oldOrientation, newOrientation);	//indicate that the value changed
			}
		}

	/**The lazily-created resource bundle used by this session.*/
	private ResourceBundle resourceBundle=null;

		/**Retrieves a resource bundle to be used by this session.
		One of the <code>getXXXResource()</code> should be used in preference to using this method directly.
		If this session does not yet have a resource bundle, one will be created based upon the current theme and locale.
		The returned resource bundle should only be used temporarily and should not be saved,
		as the resource bundle may change if the session locale or the application resource bundle base name changes.
		The resource bundle retrieved will allow hierarchical resolution in the following priority:
		<ol>
			<li>Any resource defined by the application.</li>
			<li>Any resource defined by the theme.</li>
			<li>Any resource defined by default by Guise.</li>
		</ol>
		@return The resource bundle containing the resources for this session, based upon the locale.
		@exception MissingResourceException if no resource bundle for the application's specified base name can be found or there was an error loading a resource bundle.
		@see GuiseApplication#loadResourceBundle(Theme, Locale)
		@see #getTheme()
		@see #getLocale()
		@see #getStringResource(String)
		@see #getStringResource(String, String)
		@see #getBooleanResource(String)
		@see #getBooleanResource(String, Boolean)
		@see #getIntegerResource(String)
		@see #getIntegerResource(String, Integer)
		@see #getURIResource(String)
		@see #getURIResource(String, URI)
		*/
		public ResourceBundle getResourceBundle() throws MissingResourceException
		{
			if(resourceBundle==null)	//if the resource bundle has not yet been loaded
			{
				final Locale locale=getLocale();	//get the current locale
				try
				{
					resourceBundle=getApplication().loadResourceBundle(getTheme(), locale);	//ask the application for the resource bundle based upon the locale
				}
				catch(final IOException ioException)	//if there is an I/O error, convert it to a missing resource exception
				{
					throw (MissingResourceException)new MissingResourceException(ioException.getMessage(), null, null).initCause(ioException);	//TODO check to see if null is OK for arguments here
				}
			}
			return resourceBundle;	//return the resource bundle
		}

		/**Unloads the current resource bundle so that the next call to {@link #getResourceBundle()} will load the resource bundle anew.*/
		protected void releaseResourceBundle()
		{
			resourceBundle=null;	//release our reference to the resource bundle
		}

		/**The property value change listener that, in response to a change in value, releases the resource bundle.
		@see #releaseResourceBundle()
		*/
		private final GenericPropertyChangeListener<String> resourceBundleReleasePropertyValueChangeListener=new AbstractGenericPropertyChangeListener<String>()
			{
				/**Called when a bound property is changed.
				@param propertyChangeEvent An event object describing the event source, the property that has changed, and its old and new values.
				*/
				public void propertyChange(final GenericPropertyChangeEvent<String> propertyChangeEvent)
				{
					releaseResourceBundle();	//release the resource bundle, as the new locale may indicate that new resources should be used					
				}
			};

		/**Retrieves an object resource from the resource bundle.
		Every resource access method should eventually call this method.
		This is a preferred convenience method for accessing the resources in the session's resource bundle.
		This method involves an implicit cast that will throw a class cast exception after the method ends if the resource is not of the expected type.
		@param resourceKey The key of the resource to retrieve.
		@return The resource associated with the specified resource key.
		@exception NullPointerException if the provided resource key is <code>null</code>.
		@exception MissingResourceException if no resource could be found associated with the given key.
		@see #getResourceBundle()
		@see #getResource(String, Object)
		*/
		@SuppressWarnings("unchecked")
		public <T> T getResource(final String resourceKey) throws MissingResourceException
		{
			return (T)getResourceBundle().getObject(resourceKey);	//retrieve an object from the resource bundle
		}

		/**Retrieves an object resource from the resource bundle, using a specified default if no such resource is available.
		This is a preferred convenience method for accessing the resources in the session's resource bundle.
		This method involves an implicit cast that will throw a class cast exception after the method ends if the resource is not of the expected type.
		@param resourceKey The key of the resource to retrieve.
		@param defaultValue The default value to use if there is no resource associated with the given key.
		@return The resource associated with the specified resource key or the default if none is available.
		@exception NullPointerException if the provided resource key is <code>null</code>.
		@see #getResourceBundle()
		@see #getResource(String)
		*/
		@SuppressWarnings("unchecked")
		public <T> T getResource(final String resourceKey, final T defaultValue) throws MissingResourceException
		{
			try
			{
				return (T)getResource(resourceKey);	//try to load the string from the resources
			}
			catch(final MissingResourceException missingResourceException)	//if no such resource is available
			{
				return defaultValue;	//return the specified default value
			}
		}

		/**Retrieves a string resource from the resource bundle.
		If the resource cannot be found in the resource bundle, it will be loaded from the application's resources, if possible,
		treating the resource key as a locale-sensitive resource path in the application resource area.
		This is a preferred convenience method for accessing the resources in the session's resource bundle.
		@param resourceKey The key of the resource to retrieve, or a relative path to the resource in the application's resource area.
		@return The resource associated with the specified resource key.
		@exception NullPointerException if the provided resource key is <code>null</code>.
		@exception MissingResourceException if no resource could be found associated with the given key.
		@exception ClassCastException if the resource associated with the given key is not an instance of <code>String</code>.
		@see #getResourceBundle()
		@see #getStringResource(String, String)
		*/
		public String getStringResource(final String resourceKey) throws MissingResourceException	//TODO convert the resource to a string using toString()
		{
			try
			{
				return getResource(resourceKey);	//retrieve a string from the resource bundle
			}
			catch(final MissingResourceException missingResourceException)	//if the resource does not exist
			{
				if(isPath(resourceKey) && !isAbsolutePath(resourceKey))	//if the resource key is a relative path
				{
					final String applicationResourcePath=getApplication().getLocaleResourcePath(resourceKey, getLocale());	//try to get a locale-sensitive path to the resource
					if(applicationResourcePath!=null)	//if there is a path to the resource
					{
						final InputStream inputStream=getApplication().getResourceInputStream(applicationResourcePath);	//get a stream to the resource
						if(inputStream!=null)	//if we got a stream to the resource (we always should, as we already checked to see which path represents an existing resource)
						{
							try
							{
								try
								{
									final StringWriter stringWriter=new StringWriter();	//create a new string writer to receive the resource contents
										//TODO do better checking of XML encoding type by prereading
									final Reader resourceReader=new BOMInputStreamReader(new BufferedInputStream(inputStream), UTF_8);	//get an input reader to the file, defaulting to UTF-8 if we don't know its encoding
									write(resourceReader, stringWriter);	//copy the resource to the string
									return stringWriter.toString();	//return the string read from the resource
								}
								finally
								{
									inputStream.close();	//always close the input stream
								}
							}
							catch(final IOException ioException)	//if there is an I/O error, convert it to a missing resource exception
							{
								throw (MissingResourceException)new MissingResourceException(ioException.getMessage(), missingResourceException.getClassName(), missingResourceException.getKey()).initCause(ioException);
							}
						}
					}
				}
				throw missingResourceException;	//if we couldn't find an application resource, throw the original missing resource exception
			}			
		}

		/**Retrieves a string resource from the resource bundle, using a specified default if no such resource is available.
		If the resource cannot be found in the resource bundle, it will be loaded from the application's resources, if possible,
		treating the resource key as a locale-sensitive resource path in the application resource area.
		This is a preferred convenience method for accessing the resources in the session's resource bundle.
		@param resourceKey The key of the resource to retrieve, or a relative path to the resource in the application's resource area.
		@param defaultValue The default value to use if there is no resource associated with the given key.
		@return The resource associated with the specified resource key or the default if none is available.
		@exception NullPointerException if the provided resource key is <code>null</code>.
		@exception ClassCastException if the resource associated with the given key is not an instance of <code>String</code>.
		@see #getResourceBundle()
		@see #getStringResource(String)
		*/
		public String getStringResource(final String resourceKey, final String defaultValue) throws MissingResourceException
		{
			try
			{
				return getStringResource(resourceKey);	//try to load the string from the resources
			}
			catch(final MissingResourceException missingResourceException)	//if no such resource is available
			{
				return defaultValue;	//return the specified default value
			}
		}

		/**Retrieves a <code>Boolean</code> resource from the resource bundle.
		If the given resource is a string, it will be interpreted according to the {@link Boolean#valueOf(java.lang.String)} rules.
		This is a preferred convenience method for accessing the resources in the session's resource bundle.
		@param resourceKey The key of the resource to retrieve.
		@return The resource associated with the specified resource key.
		@exception NullPointerException if the provided resource key is <code>null</code>.
		@exception MissingResourceException if no resource could be found associated with the given key.
		@exception ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>Boolean</code>.
		@see #getResourceBundle()
		@see #getBooleanResource(String, Boolean)
		*/
		public Boolean getBooleanResource(final String resourceKey) throws MissingResourceException
		{
			final Object resource=getResource(resourceKey);	//retrieve a resource from the resource bundle
			if(resource instanceof String)	//if the resource is a string
			{
				return Boolean.valueOf(resolveString((String)resource));	//get the Boolean value of the resource string
			}
			else	//if the resource is not a string, assume it is a Boolean
			{
				return (Boolean)resource;	//return the resource as a Boolean object, throwing a ClassCastException if it isn't an instance of Boolean
			}
		}

		/**Retrieves a <code>Boolean</code> resource from the resource bundle, using a specified default if no such resource is available.
		If the given resource is a string, it will be interpreted according to the {@link Boolean#valueOf(java.lang.String)} rules.
		This is a preferred convenience method for accessing the resources in the session's resource bundle.
		@param resourceKey The key of the resource to retrieve.
		@param defaultValue The default value to use if there is no resource associated with the given key.
		@return The resource associated with the specified resource key or the default if none is available.
		@exception NullPointerException if the provided resource key is <code>null</code>.
		@exception ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>Boolean</code>.
		@see #getResourceBundle()
		@see #getBooleanResource(String)
		*/
		public Boolean getBooleanResource(final String resourceKey, final Boolean defaultValue) throws MissingResourceException
		{
			try
			{
				return getBooleanResource(resourceKey);	//try to load the Boolean from the resources
			}
			catch(final MissingResourceException missingResourceException)	//if no such resource is available
			{
				return defaultValue;	//return the specified default value
			}
		}
		
		/**Retrieves a {@link Color} resource from the resource bundle.
		If the given resource is a string, it will be resolved and converted to a color using {@link AbstractModeledColor#valueOf(CharSequence)}.
		This is a preferred convenience method for accessing the resources in the session's resource bundle.
		@param resourceKey The key of the resource to retrieve.
		@return The resource associated with the specified resource key.
		@exception NullPointerException if the provided resource key is <code>null</code>.
		@exception MissingResourceException if no resource could be found associated with the given key.
		@exception ClassCastException if the resource associated with the given key is not an instance of {@link String} or {@link Color}.
		@exception IllegalArgumentException if a string is provided that is not a valid color.
		@see #getResourceBundle()
		@see #getColorResource(String, Color)
		@see AbstractModeledColor#valueOf(CharSequence)
		*/
		public Color getColorResource(final String resourceKey) throws MissingResourceException
		{
			final Object resource=getResource(resourceKey);	//retrieve a resource from the resource bundle
			if(resource instanceof String)	//if the resource is a string
			{
				return AbstractModeledColor.valueOf(resolveString((String)resource));	//create a color from the resolved string
			}
			else	//if the resource is not a string, assume it is a color
			{
				return (Color)resource;	//return the resource as a color object, throwing a ClassCastException if it isn't an instance of Color
			}
		}

		/**Retrieves a {@link Color} resource from the resource bundle, using a specified default if no such resource is available.
		If the given resource is a string, it will be resolved and converted to a color using {@link AbstractModeledColor#valueOf(CharSequence)}.
		This is a preferred convenience method for accessing the resources in the session's resource bundle.
		@param resourceKey The key of the resource to retrieve.
		@param defaultValue The default value to use if there is no resource associated with the given key.
		@return The resource associated with the specified resource key or the default if none is available.
		@exception NullPointerException if the provided resource key is <code>null</code>.
		@exception ClassCastException if the resource associated with the given key is not an instance of {@link String} or {@link Color}.
		@see #getResourceBundle()
		@see #getColorResource(String)
		@see AbstractModeledColor#valueOf(CharSequence)
		*/
		public Color getColorResource(final String resourceKey, final Color defaultValue) throws MissingResourceException
		{
			try
			{
				return getColorResource(resourceKey);	//try to load the color from the resources
			}
			catch(final MissingResourceException missingResourceException)	//if no such resource is available
			{
				return defaultValue;	//return the specified default value
			}
		}

		/**Retrieves an <code>Integer</code> resource from the resource bundle.
		If the given resource is a string, it will be interpreted according to the {@link Integer#valueOf(java.lang.String)} rules.
		This is a preferred convenience method for accessing the resources in the session's resource bundle.
		@param resourceKey The key of the resource to retrieve.
		@return The resource associated with the specified resource key.
		@exception NullPointerException if the provided resource key is <code>null</code>.
		@exception MissingResourceException if no resource could be found associated with the given key.
		@exception ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>Integer</code>.
		@exception NumberFormatException if the resource key identifies a string that is not a valid integer.
		@see #getResourceBundle()
		@see #getIntegerResource(String, Integer)
		*/
		public Integer getIntegerResource(final String resourceKey) throws MissingResourceException
		{
			final Object resource=getResource(resourceKey);	//retrieve a resource from the resource bundle
			if(resource instanceof String)	//if the resource is a string
			{
				return Integer.valueOf(resolveString((String)resource));	//get the Integer value of the resource string
			}
			else	//if the resource is not a string, assume it is an Integer
			{
				return (Integer)resource;	//return the resource as an Integer object, throwing a ClassCastException if it isn't an instance of Integer
			}
		}

		/**Retrieves an <code>Integer</code> resource from the resource bundle, using a specified default if no such resource is available.
		If the given resource is a string, it will be interpreted according to the {@link Integer#valueOf(java.lang.String)} rules.
		This is a preferred convenience method for accessing the resources in the session's resource bundle.
		@param resourceKey The key of the resource to retrieve.
		@param defaultValue The default value to use if there is no resource associated with the given key.
		@return The resource associated with the specified resource key or the default if none is available.
		@exception NullPointerException if the provided resource key is <code>null</code>.
		@exception ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>Integer</code>.
		@see #getResourceBundle()
		@see #getIntegerResource(String)
		*/
		public Integer getIntegerResource(final String resourceKey, final Integer defaultValue) throws MissingResourceException
		{
			try
			{
				return getIntegerResource(resourceKey);	//try to load the Integer from the resources
			}
			catch(final MissingResourceException missingResourceException)	//if no such resource is available
			{
				return defaultValue;	//return the specified default value
			}
		}

		/**Retrieves a <code>URI</code> resource from the resource bundle.
		If the given resource is a string, it will be converted to a URI.
		This is a preferred convenience method for accessing the resources in the session's resource bundle.
		@param resourceKey The key of the resource to retrieve.
		@return The resource associated with the specified resource key.
		@exception NullPointerException if the provided resource key is <code>null</code>.
		@exception MissingResourceException if no resource could be found associated with the given key.
		@exception ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>URI</code>.
		@exception IllegalArgumentException if a string is provided that is not a valid URI.
		@see #getResourceBundle()
		@see #getURIResource(String, URI)
		*/
		public URI getURIResource(final String resourceKey) throws MissingResourceException
		{
			final Object resource=getResource(resourceKey);	//retrieve a resource from the resource bundle
			if(resource instanceof String)	//if the resource is a string
			{
				return URI.create(resolveString((String)resource));	//create a URI from the resource string
			}
			else	//if the resource is not a string, assume it is a URI
			{
Debug.trace("ready to cast object to a URI", resource);
				return (URI)resource;	//return the resource as a URI object, throwing a ClassCastException if it isn't an instance of URI
			}
		}

		/**Retrieves a <code>URI</code> resource from the resource bundle, using a specified default if no such resource is available.
		If the given resource is a string, it will be converted to a URI.
		This is a preferred convenience method for accessing the resources in the session's resource bundle.
		@param resourceKey The key of the resource to retrieve.
		@param defaultValue The default value to use if there is no resource associated with the given key.
		@return The resource associated with the specified resource key or the default if none is available.
		@exception NullPointerException if the provided resource key is <code>null</code>.
		@exception ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>URI</code>.
		@see #getResourceBundle()
		@see #getURIResource(String)
		*/
		public URI getURIResource(final String resourceKey, final URI defaultValue) throws MissingResourceException
		{
			try
			{
				return getURIResource(resourceKey);	//try to load the URI from the resources
			}
			catch(final MissingResourceException missingResourceException)	//if no such resource is available
			{
				return defaultValue;	//return the specified default value
			}
		}

	/**The current principal (e.g. logged-in user), or <code>null</code> if there is no principal authenticated for this session.*/
	private Principal principal;

		/**@return The current principal (e.g. logged-in user), or <code>null</code> if there is no principal authenticated for this session.*/
		public Principal getPrincipal() {return principal;}

		/**Sets the current principal (e.g. logged-in user).
		This is a bound property.
		@param newPrincipal The new principal, or <code>null</code> if there should be no associated principal (e.g. the user should be logged off).
		@see GuiseSession#PRINCIPAL_PROPERTY
		*/
		public void setPrincipal(final Principal newPrincipal)
		{
			if(!ObjectUtilities.equals(principal, newPrincipal))	//if the value is really changing (compare their values, rather than identity)
			{
				final Principal oldPrincipal=principal;	//get the old value
				principal=newPrincipal;	//actually change the value
				firePropertyChange(PRINCIPAL_PROPERTY, oldPrincipal, newPrincipal);	//indicate that the value changed
			}
		}

	/**The current session theme, or <code>null</code> if the theme has not been loaded.*/
	private Theme theme=null;

		/**Returns the current session theme.
		If this session's theme has not yet been loaded, this method loads the theme.
		@return The current session theme.
		@exception IOException if there is an error loading the theme.
		@see #getThemeURI()
		*/
		public Theme getTheme() throws IOException
		{
			if(theme==null)	//if there is no theme (this race condition is more or less benign, and will only result in the theme being loaded more than once initially)
			{
				theme=getApplication().loadTheme(getThemeURI());	//ask the application to load the theme
			}
			return theme;
		}

	/**The URI of the session theme, to be resolved against the application base path.*/
	private URI themeURI;

		/**@return The URI of the session theme, to be resolved against the application base path.*/
		public URI getThemeURI() {return themeURI;}

		/**Sets the URI of the session theme.
		The current theme, if any, will be released and loaded the next time {@link #getTheme()} is called.
		This is a bound property.
		@param newThemeURI The URI of the new session theme.
		@exception NullPointerException if the given theme URI is <code>null</code>.
		@see #THEME_URI_PROPERTY
		@see #getTheme()
		*/
		public void setThemeURI(final URI newThemeURI)
		{
			if(!ObjectUtilities.equals(themeURI, newThemeURI))	//if the value is really changing
			{
				final URI oldThemeURI=themeURI;	//get the old value
				themeURI=checkInstance(newThemeURI, "Theme URI cannot be null.");	//actually change the value
				theme=null;	//release our reference to the current theme
				firePropertyChange(THEME_URI_PROPERTY, oldThemeURI, newThemeURI);	//indicate that the value changed
			}
		}

	/**The action prototype for presenting application information.*/
	private final ActionPrototype aboutApplicationActionPrototype;

		/**@return The action prototype for presenting application information.*/
		public ActionPrototype getAboutApplicationActionPrototype() {return aboutApplicationActionPrototype;}

	/**Application and platform constructor.
	The session local will initially be set to the locale of the associated Guise application.
	No operation must be performed inside the constructor that would require the presence of the Guise session within this thread group.
	@param application The Guise application to which this session belongs.
	@param platform The platform on which this session's objects are depicted.
	@exception NullPointerException if the given application and/or platform is <code>null</code>.
	*/
	public AbstractGuiseSession(final GuiseApplication application, final Platform platform)
	{
		this.uuid=UUID.randomUUID();	//create a UUID for the session
		this.application=checkInstance(application, "Application cannot be null.");	//save the application
		this.baseURI=application.getContainer().getBaseURI().resolve(application.getBasePath().toURI());	//default to a base URI calculated from the application base path resolved to the container's base URI
/*TODO del
		try
		{
			documentBuilder=createDocumentBuilder(true);	//create a new namespace-aware document builder
		}
		catch(final ParserConfigurationException parserConfigurationException)	//if we can't find an XML parser
		{
			throw new AssertionError(parserConfigurationException);
		}
*/
		this.platform=checkInstance(platform, "Platform cannot be null.");	//save the platform
		this.themeURI=application.getThemeURI();	//default to the application theme
		this.locale=application.getLocales().get(0);	//default to the first application locale
//TODO del when works		this.locale=application.getDefaultLocale();	//default to the application locale
		this.orientation=Orientation.getOrientation(locale);	//set the orientation default based upon the locale
		logWriter=new OutputStreamWriter(System.err);	//default to logging to the error output; this will be replaced after the session is created
			//about action prototype
		aboutApplicationActionPrototype=new ActionPrototype();
		aboutApplicationActionPrototype.setLabel(LABEL_ABOUT_X+createStringValueReference(APPLICATION_NAME));
		aboutApplicationActionPrototype.setGlyphURI(GLYPH_ABOUT);
		aboutApplicationActionPrototype.addActionListener(new ActionListener()
				{
					public void actionPerformed(final ActionEvent actionEvent)
					{
						final AboutPanel aboutPanel=new AboutPanel();	//create a new about panel
						aboutPanel.setNameLabel(APPLICATION_NAME);
						aboutPanel.setVersionLabel(LABEL_VERSION+' '+APPLICATION_VERSION);
						aboutPanel.setCopyrightLabel(APPLICATION_COPYRIGHT);
						final Frame aboutFrame=new NotificationOptionDialogFrame(aboutPanel, Notification.Option.OK);	//create an about frame
						aboutFrame.setLabel(LABEL_ABOUT+' '+APPLICATION_NAME);	//set the title
						aboutFrame.open(true);	//show the about dialog
					}
				});
	}

	/**Retrieves the component bound to the given destination.
	If a component has already been created and cached, it will be be returned; otherwise, one will be created and cached. 
	This method synchronizes on {@link #destinationComponentMap}.
	@param destination The destination for which a component should be returned.
	@return The component bound to the given destination.
	@exception NullPointerException if the destination is <code>null</code>.
	@exception IllegalStateException if the component class bound to the destination does not provide appropriate constructors, is an interface, is abstract, or throws an exception during instantiation.
	*/
	public Component getDestinationComponent(final ComponentDestination destination)
	{
		Component component;	//we'll store the component here, either a cached component or a created component
		synchronized(destinationComponentMap)	//don't allow the map to be modified while we access it
		{
			component=destinationComponentMap.get(destination);	//get cached component, if any
			if(component==null)	//if no component is cached
			{
//TODO maybe verify that this destination is actually associated with the navigation path for this application				final Destination destination=getApplication().getDestination(path);	//get the destination for this path
				component=createComponent(destination.getComponentClass());	//create the component
				destinationComponentMap.put(destination, component);	//bind the component to the path, caching it for next time
			}
		}
		return component;	//return the panel, or null if we couldn't find a panel
	}

	/**Releases the component bound to the given destination.
	@param destination The destination for which any bound component should be released.
	@return The component previously bound to the given destination, or <code>null</code> if no component was bound to the given destination.
	@exception NullPointerException if the destination is <code>null</code>.
	*/
	public Component releaseDestinationComponent(final ComponentDestination destination)
	{
		return destinationComponentMap.remove(destination);	//uncache the component
	}

	/**Retrieves the component bound to the given application context-relative path.
	This is a convenience method that retrieves the component associated with the component destination for the given navigation path.
	This method calls {@link GuiseApplication#getDestination(String)}.
	This method calls {@link #getDestinationComponent(ComponentDestination)}.
	@param path The application context-relative path within the Guise container context.
	@return The component bound to the given path. 
	@exception NullPointerException if the path is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	@exception IllegalArgumentException if no component is appropriate to associated the given navigation path (i.e. the given navigation path is not associated with a component destination).
	@exception IllegalStateException if the component class bound to the path does not provide appropriate constructors, is an interface, is abstract, or throws an exception during instantiation.
	@see ComponentDestination
	*/
	public Component getNavigationComponent(final URIPath path)
	{
		final Destination destination=getApplication().getDestination(path);	//get the destination associated with the given path
		if(!(destination instanceof ComponentDestination))	//if the destination is not a component destination
		{
			throw new IllegalArgumentException("Navigation path "+path+" does not designate a component destination.");
		}
		return getDestinationComponent((ComponentDestination)destination);	//return the component
	}

	/**Creates the component for the given class.
	@param componentClass The class representing the component to create.
	@return The created component.
	@exception IllegalStateException if the component class does not provide a default constructor, is an interface, is abstract, or throws an exception during instantiation.
	*/
	protected Component createComponent(final Class<? extends Component> componentClass)
	{
		Component component;	//we'll store the component here
		try
		{
//TODO del			Debug.trace("***ready to create component for class", componentClass);
			component=componentClass.newInstance();	//create a new instance of the component
		}
		catch(final IllegalAccessException illegalAccessException)	//if the constructor is not visible
		{
			throw new IllegalStateException(illegalAccessException);
		}
		catch(final InstantiationException instantiationException)	//if the class is an interface or is abstract
		{
			throw new IllegalStateException(instantiationException);
		}
		initializeComponent(component);	//initialize the component from a TURF description, if possible
		return component;	//return the component
	}
	
	/**Initializes a component, optionally with a description in a TURF resource file.
	This method first tries to load a PLOOP URF description of the component in a TURF file in the classpath in the same directory with the same name as the class file, with an <code>.turf</code> extension.
	That is, for the class <code>MyComponent.class</code> this method first tries to load <code>MyComponent.turf</code> from the same directory in the classpath.
	If this is successful, the component is initialized from this URF description.
	This implementation calls {@link #initializeComponent(Component, InputStream)}.
	The component's {@link Component#initialize()} is called whether there is an URF description.
	This method synchronizes on {@link #getDocumentBuilder()}.
	@param component The component to initialize.
	@exception MissingResourceException if no resource could be found associated with the given key.
	@exception IllegalArgumentException if the URF description does not provide a resource description of the same type as the specified component.
	@exception IllegalStateException if the given component has already been initialized, or there was some other problem initializing the component.
	@see Component#initialize()
	@see <a href="http://www.ploop.org/">PLOOP</a>
	*/
	public void initializeComponent(final Component component)
	{
		final Class<?> componentClass=component.getClass();	//get the class of the component
		final String descriptionFilename=addExtension(getLocalName(componentClass), TURF_EXTENSION);	//create a name in the form ClassName.turf
		//TODO del Debug.trace("Trying to load description file:", descriptionFilename);
		final InputStream descriptionInputStream=componentClass.getResourceAsStream(descriptionFilename);	//get an input stream to the description file
		if(descriptionInputStream!=null)	//if we have a description file
		{
			try
			{
				try
				{
					initializeComponent(component, descriptionInputStream);	//initialize the component from the description, calling the initialize() method in the process
				}
				finally
				{
					descriptionInputStream.close();	//always close the description input stream for good measure
				}
			}
			catch(final IOException ioException)	//if there is an I/O exception
			{
				throw new IllegalStateException(ioException);	//TODO fix better
			}		
			catch(final DataException dataException)
			{
				throw new IllegalStateException(dataException);	//TODO fix better
			}		
			catch(final InvocationTargetException invocationTargetException)
			{
				throw new IllegalStateException(invocationTargetException);	//TODO fix better
			}		
		}
		else	//if there is no description file
		{
			component.initialize();	//call the initialize() method manually
		}
	}

	/**Initializes a component with a description in an TURF resource file.
	This method calls {@link Component#initialize()} after initializing the component from the description.
	This implementation calls {@link #initializeComponent(Component, InputStream)}.
	This method synchronizes on {@link #getDocumentBuilder()}.
	@param component The component to initialize.
	@param resourceKey The key to a TURF description resource file.
	@exception MissingResourceException if no resource could be found associated with the given key.
	@exception IllegalArgumentException if the URF description does not provide a resource description of the same type as the specified component.
	@exception IllegalStateException if the given component has already been initialized.
	@exception DataException if the data was incorrect for component initialization.
	@exception InvocationTargetException if a given resource indicates a Java class the constructor of which throws an exception.
	@see Component#initialize()
	*/
	public void initializeComponentFromResource(final Component component, final String resourceKey) throws DataException, InvocationTargetException
	{
		final String descriptionResource=getStringResource(resourceKey);	//get the description resource
		try
		{
			final InputStream descriptionInputStream=new ByteArrayInputStream(descriptionResource.getBytes(UTF_8));	//convert the string to bytes and create an input string to the array of bytes TODO verify the encoding somehow
			initializeComponent(component, descriptionInputStream);	//initialize the component from the description resource
		}
		catch(final UnsupportedEncodingException unsupportedEncodingException)	//UTF-8 should always be supported
		{
			throw new AssertionError(unsupportedEncodingException);
		}
		catch(final IOException ioException)	//we should never have an I/O exception reading from a byte array input stream
		{
			throw new AssertionError(ioException);
		}
	}
	
	/**Initializes a component from the contents of an URF description input stream.
	This method calls {@link Component#initialize()} after initializing the component from the description.
	This method synchronizes on {@link #getDocumentBuilder()}.
	@param component The component to initialize.
	@param descriptionInputStream The input stream containing an URF description.
	@exception IllegalArgumentException if the URF description does not provide a resource description of the same type as the specified component.
	@exception IllegalStateException if the given component has already been initialized.
	@exception IOException if there is an error reading from the input stream.
	@exception DataException if the data was incorrect for component initialization.
	@exception InvocationTargetException if a given resource indicates a Java class the constructor of which throws an exception.
	@see Component#initialize()
	*/
	public void initializeComponent(final Component component, final InputStream descriptionInputStream) throws IOException, DataException, InvocationTargetException
	{
		final URI BASE_URI=URI.create("guise:/");	//TODO fix
		final URF urf=AbstractTURFIO.readTURF(new URF(), descriptionInputStream, baseURI);	//read TURF from the input stream
		final URI componentResourceTypeURI=createInfoJavaURI(component.getClass());	//create a new URI that indicates the type of the resource description we expect
		final URFResource componentResource=urf.getResourceByTypeURI(componentResourceTypeURI);	//try to locate the description of the given component
		if(componentResource!=null)	//if there is a resource description of a matching type
		{
//TODO del					final PLOOPProcessor ploopProcessor=new PLOOPProcessor(this);	//create a new PLOOP processor, passing the Guise session to use as a default constructor argument
			final PLOOPURFProcessor ploopProcessor=new PLOOPURFProcessor();	//create a new PLOOP processor
			ploopProcessor.setObjectProperties(component, componentResource);	//initialize the component from this resource
			component.initialize();	//initialize the component
			final List<Object> objects=ploopProcessor.getObjects(urf);	//make sure all described Java objects in the URF instance have been created
		}
		else	//if there is no resource of the appropriate type
		{
			throw new IllegalArgumentException("No resource description found of type "+componentResourceTypeURI);
		}
	}

	/**Initializes all components in a hierarchy in depth-first order.
	@param component The parent of the component tree to initialize.
	*/
/*TODO fix
	protected static void initializeComponents(final Component component)
	{
		
	}
*/
	
	/**The stack of modal navigation points.*/
	private final List<ModalNavigation> modalNavigationStack=synchronizedList(new ArrayList<ModalNavigation>());

		/**Pushes the given model navigation onto the top of the stack.
		@param modalNavigation The modal navigation to add.
		@exception NullPointerException if the given modal navigation is <code>null</code>.
		*/
		protected void pushModalNavigation(final ModalNavigation modalNavigation)
		{
			modalNavigationStack.add(checkInstance(modalNavigation, "Modal navigation cannot be null."));	//push the modal navigation onto the top of the stack (the end of the list)
		}

		/**@return The modal navigation on the top of the stack, or <code>null</code> if there are no modal navigations.*/
		protected ModalNavigation peekModalNavigation()
		{
			synchronized(modalNavigationStack)	//don't allow anyone to to access the modal navigation stack while we access it
			{
				return !modalNavigationStack.isEmpty() ? modalNavigationStack.get(modalNavigationStack.size()-1) : null;	//return the last (top) modal navigation in the stack
			}
		}

		/**@return The modal navigation from the top of the stack, or <code>null</code> if there are no modal navigations on the stack.*/
		protected ModalNavigation pollModalNavigation()
		{
			synchronized(modalNavigationStack)	//don't allow anyone to to access the modal navigation stack while we access it
			{
				return !modalNavigationStack.isEmpty() ? modalNavigationStack.remove(modalNavigationStack.size()-1) : null;	//return the last (top) modal navigation in the stack
			}
		}

		/**@return The modal navigation from the top of the stack.
		@exception NoSuchElementException if there are no modal navigations on the stack.
		*/
		protected ModalNavigation popModalNavigation()
		{
			final ModalNavigation modalNavigation=pollModalNavigation();	//get the modal navigation from the top of the stack, if there is one
			if(modalNavigation==null)	//if the stack was empty
			{
				throw new NoSuchElementException("No modal navigations are on the stack.");
			}
			return modalNavigation;	//return the modal navigation we popped from the top of the stack
		}

		/**@return Whether the session is in a modal navigation state.*/
		public boolean isModalNavigation()
		{
			return !modalNavigationStack.isEmpty();	//we are modally navigating if there is one or more modal navigation states on the stack
		}

		/**@return The current modal navigation state, or <code>null</code> if there are no modal navigations.*/
		public ModalNavigation getModalNavigation() {return peekModalNavigation();}

		/**Begins modal interaction for a particular modal panel.
		The modal navigation is pushed onto the stack, and an event is fired to the modal listener of the modal navigation.
		@param modalNavigationPanel The panel for which modal navigation state should begin.
		@param modalNavigation The state of modal navigation.
		@see #pushModalNavigation(ModalNavigation)
		*/
		public void beginModalNavigation(final ModalNavigationPanel<?> modalNavigationPanel, final ModalNavigation modalNavigation)
		{
			//TODO release the navigation panel, maybe, just in case
			pushModalNavigation(modalNavigation);	//push the modal navigation onto the top of the modal navigation stack
			modalNavigation.getModalListener().modalBegan(new ModalEvent(modalNavigationPanel));
		}

		/**Ends modal interaction for a particular modal panel.
		The panel is released from the cache so that new navigation will create a new modal panel.
		This method is called by modal panels and should seldom if ever be called directly.
		If the current modal state corresponds to the current navigation state, the current modal state is removed, the modal state's event is fired, and modal state is handed to the previous modal state, if any.
		Otherwise, navigation is transferred to the modal panel's referring URI, if any.
		If the given modal panel is not the panel at the current navigation path, the modal state is not changed, although navigation and releasal will still occur.
		@param modalNavigationPanel The panel for which modal navigation state should be ended.
		@return true if modality actually ended for the given panel.
		@see #popModalNavigation()
		@see Frame#getReferrerURI()
		@see #releaseDestinationComponent(String)
		*/
		public boolean endModalNavigation(final ModalNavigationPanel<?> modalNavigationPanel)
		{
			final URIPath navigationPath=getNavigationPath();	//get our current navigation path
			final GuiseApplication application=getApplication();	//get the application
			ModalNavigation modalNavigation=null;	//if we actually end modal navigation, we'll store the information here
			final Destination destination=application.getDestination(navigationPath);	//get the destination for this path TODO maybe add a GuiseSession.getDestination()
			if(destination instanceof ComponentDestination)	//if we're at a component destination
			{
				final ComponentDestination componentDestination=(ComponentDestination)destination;	//get the destination as a component destination
				URI navigationURI=null;	//TODO fix
	//TODO fix			URI navigationURI=modalPanel.getReferrerURI();	//in the worse case scenario, we'll want to go back to where the modal panel came from, if that's available
				if(destinationComponentMap.get(componentDestination)==modalNavigationPanel)	//before we try to actually ending modality, make sure this panel is actually the one at our current destination
				{
					synchronized(modalNavigationStack)	//don't allow anyone to to access the modal navigation stack while we access it
					{
						final ModalNavigation currentModalNavigation=peekModalNavigation();	//see which model navigation is on the top of the stack
						if(currentModalNavigation!=null)	//if there is a modal navigation currently in use
						{
							if(application.resolvePath(navigationPath).equals(new URIPath(currentModalNavigation.getNewNavigationURI().getRawPath())))	//if we're navigating where we expect to be (if we somehow got to here at something other than the modal navigation path, we wouldn't want to remove the current navigation path)
							{
								modalNavigation=popModalNavigation();	//end the current modal navigation
								navigationURI=modalNavigation.getOldNavigationURI();	//we'll return to where the current modal navigation came from---that's a better choice
								final ModalNavigation oldModalNavigation=peekModalNavigation();	//see which model navigation is next on the stack
								if(oldModalNavigation!=null)	//if there is another modal navigation to go to
								{
									navigationURI=oldModalNavigation.getOldNavigationURI();	//we're forced to go to the navigation URI of the old modal navigation							
								}
							}
						}
					}
				}
				if(navigationURI!=null)	//if we know where to go now that modality has ended
				{
					navigate(navigationURI);	//navigate to the new URI
				}
				releaseDestinationComponent(componentDestination);	//release the component associated with this destination
				if(modalNavigation!=null)	//if we if we ended modality for the panel
				{
					modalNavigation.getModalListener().modalEnded(new ModalEvent(modalNavigationPanel));	//send an event to the modal listener
				}
			}
			return modalNavigation!=null;	//return whether we ended modality
		}

	/**The navigation path relative to the application context path.*/
	private URIPath navigationPath=null;

		/**Reports the navigation path relative to the application context path.
		@return The path representing the current navigation location of the Guise application.
		@exception IllegalStateException if this message has been called before the navigation path has been initialized.
		*/
		public URIPath getNavigationPath()
		{
			if(navigationPath==null)	//if no navigation path has been set, yet
			{
				throw new IllegalStateException("Navigation path has not yet been initialized.");
			}
			return navigationPath;	//return the navigation path
		}

		/**Changes the navigation path of the session.
		This method does not actually cause navigation to occur.
		If the given navigation path is the same as the current navigation path, no action occurs.
		@param navigationPath The navigation path relative to the application context path.
		@exception NullPointerException if the given navigation path is <code>null</code>.		
		@exception IllegalArgumentException if the provided path is absolute.
		@exception IllegalArgumentException if the navigation path is not recognized (e.g. there is no destination associated with the navigation path).
		@see #navigate(URIPath)
		@see #navigate(URI)
		@see #navigateModal(URIPath, ModalNavigationListener)
		@see #navigateModal(URI, ModalNavigationListener)
		*/
		public void setNavigationPath(final URIPath navigationPath)
		{
			if(!ObjectUtilities.equals(this.navigationPath, checkInstance(navigationPath, "Navigation path cannot be null.")))	//if the navigation path is really changing
			{
				if(!getApplication().hasDestination(navigationPath))	//if no destination is associated with the given navigation path
				{
					throw new IllegalArgumentException("Unknown navigation path: "+navigationPath);
				}
				this.navigationPath=navigationPath;	//change the navigation path TODO fire an event, but make sure that doesn't make the page reload
			}
		}

	/**The bookmark relative to the navigation path.*/
	private Bookmark bookmark=null;

		/**Reports the current bookmark relative to the current navigation path.
		@return The bookmark relative to the current navigation path, or <code>null</code> if there is no bookmark specified.
		*/
		public Bookmark getBookmark()
		{
			return bookmark;	//return the bookmark, if there is one
		}
	
		/**Changes the bookmark of the current navigation path.
		This method does not necessarily cause navigation to occur, but instead "publishes" the bookmark to indicate that it is representative of the current state of the current navigation.
		@param bookmark The bookmark relative to the current navigation path, or <code>null</code> if there should be no bookmark.
		*/
		public void setBookmark(final Bookmark bookmark)
		{
			if(!ObjectUtilities.equals(this.bookmark, bookmark))	//if the bookmark is really changing
			{
				this.bookmark=bookmark;	//change the bookmark TODO fire an event, but make sure that doesn't make the page reload
				log(null, "guise-bookmark", bookmark!=null ? bookmark.toString() : null, null, null);	//TODO improve; use a constant
			}
		}	

		/**Sets the new navigation path and bookmark, firing a navigation event if appropriate.
		If the navigation path and/or bookmark has changed, this method fires an event to all {@link NavigationListener}s in the component hierarchy, with the session as the source of the {@link NavigationEvent}.
		This method calls {@link #setNavigationPath(URIPath)} and {@link #setBookmark(Bookmark)}.  
		This implementation logs the navigation change.
		@param navigationPath The navigation path relative to the application context path.
		@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
		@param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
		@exception NullPointerException if the given navigation path is <code>null</code>.
		@see #setNavigationPath(URIPath)
		@see #setBookmark(Bookmark)
		@see #getApplicationFrame()
		*/
		public void setNavigation(final URIPath navigationPath, final Bookmark bookmark, final URI referrerURI)
		{
//TODO del Debug.trace("setting naviation; navigation path:", navigationPath, "bookmark:", bookmark, "referrerURI:", referrerURI);
				//if the navigation path or the bookmark is changing
			if(!ObjectUtilities.equals(this.navigationPath, navigationPath)	//see if the navigation path is changing (the old navigation path will be null if this session has not yet navigated anywhere; don't call getNavigationPath(), which might throw an exception)
					|| !ObjectUtilities.equals(this.bookmark, bookmark))	//see if the bookmark is changing
			{
				setNavigationPath(navigationPath);	//make sure the Guise session has the correct navigation path
				setBookmark(bookmark);	//make sure the Guise session has the correct bookmark
//TODO del Debug.trace("changed to new bookmark:", getBookmark());
				final Map<String, Object> logParameters=new HashMap<String, Object>();	//create a map for our log parameters
				logParameters.put("bookmark", bookmark);	//bookmark TODO use a constant
				logParameters.put("referrerURI", referrerURI);	//referrer URI TODO use a constant
				log(null, "guise-navigate", null, logParameters, null);	//TODO improve; use a constant
				fireNavigated(referrerURI);	//fire a navigation event to the entire application frame hierarchy
			}
		}

		/**Fires a {@link NavigationEvent} to all {@link NavigationListener}s in the session application frame hierarchy.
		@param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
		@see #getNavigationPath()
		@see #getBookmark()
		@see #getApplicationFrame()
		@see NavigationListener
		@see NavigationEvent 
		*/
		public void fireNavigated(final URI referrerURI)
		{
			final NavigationEvent navigationEvent=new NavigationEvent(this, getNavigationPath(), getBookmark(), referrerURI);	//create a navigation event with the session as the source of the event
			fireNavigated(getApplicationFrame(), navigationEvent);	//fire a navigation event to all components in the application frame hierarchy			
		}

		/**Fires a {@link NavigationEvent} to all {@link NavigationListener}s in the given component hierarchy.
		@param component The component to which the navigation event should be fired, along with all children, if the component or any children implement {@link NavigationListener}.
		@param navigationEvent The navigation event to fire.
		@see NavigationListener
		@see NavigationEvent 
		*/
		protected void fireNavigated(final Component component, final NavigationEvent navigationEvent)
		{
			if(component instanceof NavigationListener)	//if the component is a navigation listener
			{
				((NavigationListener)component).navigated(navigationEvent);	//fire the event to the component
			}
			if(component instanceof CompositeComponent)	//if the component is a composite component
			{
				for(final Component childComponent:((CompositeComponent)component).getChildComponents())	//for every child component
				{
					fireNavigated(childComponent, navigationEvent);	//fire the event to the child component if possible
				}
			}			
		}
		
	/**The requested navigation, or <code>null</code> if no navigation has been requested.*/
	private Navigation requestedNavigation=null;

		/**@return The requested navigation, or <code>null</code> if no navigation has been requested.*/
		public Navigation getRequestedNavigation() {return requestedNavigation;}

		/**Removes any requests for navigation.*/
		public void clearRequestedNavigation() {requestedNavigation=null;}

		/**Requests navigation to the specified path.
		The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
		Later requested navigation before navigation occurs will override this request.
		@param path A path that is either relative to the application context path or is absolute.
		@exception NullPointerException if the given path is <code>null</code>.
		@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #navigate(URI)} should be used instead).
		@see #navigate(URI)
		*/
		public void navigate(final URIPath path)
		{
			navigate(path, (Bookmark)null);	//navigate to the requested path with no bookmark
		}

		/**Requests navigation to the specified path in an identified viewport.
		The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
		Later requested navigation before navigation occurs will override this request.
		@param path A path that is either relative to the application context path or is absolute.
		@param viewportID The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.
		@exception NullPointerException if the given path is <code>null</code>.
		@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #navigate(URI)} should be used instead).
		@see #navigate(URI, String)
		*/
		public void navigate(final URIPath path, final String viewportID)
		{
			navigate(path, null, viewportID);	//navigate to the requested path in the viewport with no bookmark
		}

		/**Requests navigation to the specified path and bookmark.
		The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
		Later requested navigation before navigation occurs will override this request.
		@param path A path that is either relative to the application context path or is absolute.
		@param bookmark The bookmark at the given path, or <code>null</code> if no bookmark should be included in the navigation.
		@exception NullPointerException if the given path is <code>null</code>.
		@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #navigate(URI)} should be used instead).
		@see #navigate(URI)
		*/
		public void navigate(final URIPath path, final Bookmark bookmark)
		{
			navigate(path, bookmark, null);	//navigate to the requested path with no viewport ID
		}

		/**Requests navigation to the specified path and bookmark in an identified viewport.
		The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
		Later requested navigation before navigation occurs will override this request.
		@param path A path that is either relative to the application context path or is absolute.
		@param bookmark The bookmark at the given path, or <code>null</code> if no bookmark should be included in the navigation.
		@param viewportID The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.
		@exception NullPointerException if the given path is <code>null</code>.
		@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #navigate(URI)} should be used instead).
		@see #navigate(URI, String)
		*/
		public void navigate(final URIPath path, final Bookmark bookmark, final String viewportID)
		{
			final URI uri=bookmark!=null ? URI.create(path.toString()+bookmark.toString()) : path.toURI();	//append the bookmark if needed
			navigate(uri, viewportID);	//navigate to the requested URI, converting the path to a URI and verifying that it is only a path
		}

		/**Requests navigation to the specified URI.
		The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
		Later requested navigation before navigation occurs will override this request.
		@param uri Either a relative or absolute path, or an absolute URI.
		@exception NullPointerException if the given URI is <code>null</code>.
		*/
		public void navigate(final URI uri)
		{
			navigate(uri, null);	//navigate to the given URI in the current viewport
		}

		/**Requests navigation to the specified URI in an identified viewport.
		The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
		Later requested navigation before navigation occurs will override this request.
		@param uri Either a relative or absolute path, or an absolute URI.
		@param viewportID The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.
		@exception NullPointerException if the given URI is <code>null</code>.
		*/
		public void navigate(final URI uri, final String viewportID)
		{
			requestedNavigation=new Navigation(getApplication().resolveURI(getNavigationPath().toURI()), getApplication().resolveURI(checkInstance(uri, "URI cannot be null.")), viewportID);	//resolve the URI against the application context path
		}

		/**Requests modal navigation to the specified path.
		The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
		Later requested navigation before navigation occurs will override this request.
		@param path A path that is either relative to the application context path or is absolute.
		@param modalListener The listener to respond to the end of modal interaction.
		@exception NullPointerException if the given path is <code>null</code>.
		@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #navigate(URI)} should be used instead).
		@see #navigateModal(URI, ModalNavigationListener)
		*/
		public void navigateModal(final URIPath path, final ModalNavigationListener modalListener)
		{
			navigateModal(path.toURI(), modalListener);	//navigate to the requested URI, converting the path to a URI and verifying that it is only a path
		}

		/**Requests modal navigation to the specified path and bookmark.
		The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
		Later requested navigation before navigation occurs will override this request.
		@param path A path that is either relative to the application context path or is absolute.
		@param bookmark The bookmark at the given path, or <code>null</code> if no bookmark should be included in the navigation.
		@param modalListener The listener to respond to the end of modal interaction.
		@exception NullPointerException if the given path is <code>null</code>.
		@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #navigate(URI)} should be used instead).
		@see #navigateModal(URI, ModalNavigationListener)
		*/
		public void navigateModal(final URIPath path, final Bookmark bookmark, final ModalNavigationListener modalListener)
		{
			final URI uri=bookmark!=null ? URI.create(path.toString()+bookmark.toString()) : path.toURI();	//append the bookmark if needed
			navigateModal(uri, modalListener);	//navigate to the requested URI, converting the path to a URI and verifying that it is only a path
		}

		/**Requests modal navigation to the specified URI.
		The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
		Later requested navigation before navigation occurs will override this request.
		@param uri Either a relative or absolute path, or an absolute URI.
		@param modalListener The listener to respond to the end of modal interaction.
		@exception NullPointerException if the given URI is <code>null</code>.
		*/
		public void navigateModal(final URI uri, final ModalNavigationListener modalListener)
		{
			requestedNavigation=new ModalNavigation(getApplication().resolveURI(getNavigationPath().toURI()), getApplication().resolveURI(checkInstance(uri, "URI cannot be null.")), modalListener);	//resolve the URI against the application context path
		}		

	/**The synchronized list of postponed model events.*/
	private final List<PostponedEvent<?>> queuedModelEventList=synchronizedList(new LinkedList<PostponedEvent<?>>());	//use a linked list because we'll be removing items from the front of the list as we process the events TODO fix; this doesn't seem to be the case anymore, so we can use a synchronized array list

		/**Queues a postponed event to be fired after the context has finished processing events.
		If a Guise context is currently processing events, the event will be queued for later.
		If no Guise context is currently processing events, the event will be fired immediately.
		@param postponedEvent The event to fire at a later time.
		*/
		public synchronized void queueEvent(final PostponedEvent<?> postponedEvent)
		{
//TODO fix; decide if we want to allow delayed events			if(context!=null && (context.getState()==GuiseContext.State.PROCESS_EVENT))	//if the context is processing an event
			if(false)	//TODO fix; testing
			{
				queuedModelEventList.add(postponedEvent);	//add the postponed event to our list of postponed events					
			}
			else	//if no context is changing the model
			{
				postponedEvent.fireEvent();	//go ahead and fire the event immediately
			}
		}

		/**Fires any postponed model events that are queued.*/
		protected void fireQueuedModelEvents()
		{
			synchronized(queuedModelEventList)	//don't allow any changes to the postponed model event list while we access it
			{
				final Iterator<PostponedEvent<?>> postponedModelEventIterator=queuedModelEventList.iterator();	//get an iterator to all the model events
				while(postponedModelEventIterator.hasNext())	//while there are more postponed model events
				{
					final PostponedEvent<?> postponedModelEvent=postponedModelEventIterator.next();	//get the next postponed event
					try
					{
						postponedModelEvent.fireEvent();	//fire the event
					}
					finally	//there could be an exception while we're firing the event
					{
						postponedModelEventIterator.remove();	//always remove the event we fired, even if there's an exception, so it won't be fired again if there is an exception
					}
				}
				queuedModelEventList.clear();	//remove all pending model events
			}
		}

	/**Called when the session is initialized.
	@exception IllegalStateException if the session is already initialized.
	@see #destroy()
	*/
	public void initialize()
	{
		this.applicationFrame=application.createApplicationFrame();	//create the application frame
		this.applicationFrame.open();	//open the application frame
			//TODO check active state
		getApplication().addPropertyChangeListener(GuiseApplication.RESOURCE_BUNDLE_BASE_NAME_PROPERTY, resourceBundleReleasePropertyValueChangeListener);	//when the application changes its resource bundle base name, release the resource bundle		
	}

	/**Called when the session is destroyed.
	@exception IllegalStateException if the session has not yet been initialized or has already been destroyed.
	@see #initialize()
	*/
	public void destroy()
	{
			//TODO check active state
		getApplication().removePropertyChangeListener(GuiseApplication.RESOURCE_BUNDLE_BASE_NAME_PROPERTY, resourceBundleReleasePropertyValueChangeListener);	//stop listening for the application to change its resource bundle base name				
	}

	/**Creates a temporary resource available at a public application navigation path but with access restricted to this session.
	The file will be created in the application's temporary file directory.
	If the resource is restricted to the current Guise session, the resource will be deleted when the current Guise session ends.
	This is a convenience method that delegates to {@link GuiseApplication#createTempPublicResource(String, String, GuiseSession)}.
	@param baseName The base filename to be used in generating the filename.
	@param extension The extension to use for the temporary file.
	@return A public application navigation path that can be used to access the resource only from this session.
	@exception NullPointerException if the given base name and/or extension is <code>null</code>.
	@exception IllegalArgumentException if the base name is the empty string.
	@exception IOException if there is a problem creating the public resource.
	@see GuiseApplication#createTempPublicResource(String, String, GuiseSession)
	@see GuiseApplication#getTempDirectory()
	*/
	public URIPath createTempPublicResource(final String baseName, final String extension) throws IOException
	{
		return getApplication().createTempPublicResource(baseName, extension, this);	//delegate to the application with a reference to this session
	}
	
	/**Reports that a bound property has changed.
	This implementation delegates to the Guise session to fire or postpone the property change event.
	@param propertyChangeEvent The event to fire.
	@see GuiseSession#queueEvent(com.garretwilson.event.PostponedEvent)
	*/
	protected void firePropertyChange(final PropertyChangeEvent propertyChangeEvent)
	{
		queueEvent(createPostponedPropertyChangeEvent(propertyChangeEvent));	//create and queue a postponed property change event
	}

	/**Creates a component to indicate Guise busy status.
	@return A component to indicate Guise busy status.
	@see Theme#GLYPH_BUSY
	*/
	public Component createBusyComponent()
	{
		return new DefaultBusyPanel();	//create the default busy panel
	}

	/**Processes input such as a keystroke, a mouse click, or a command.
	A new {@link InputEvent} will be created and dispatched via the application frame.	
	If an input event is still not consumed after dispatching, its input is processed by the installed input strategy, if any.
	@param input The input to process.
	@return <code>true</code> if the input was consumed and should not be processed further.
	@exception NullPointerException if the given input is <code>null</code>.
	@exception IllegalArgumentException if input was given that this session does not know how to process.
	@see #createInputEvent(Input)
	@see GuiseSession#getApplicationFrame()
	@see Component#dispatchInputEvent(InputEvent)
	@see #getInputStrategy()
	@see InputStrategy#input(Input)
	@see InputEvent#isConsumed()
	*/
	public boolean input(final Input input)
	{
		final InputEvent inputEvent=createInputEvent(input);	//create an input event from the input
		if(!inputEvent.isConsumed())	//if the input has not been consumed (the event could be created as consumed, preventing further processing)
		{
			getApplicationFrame().dispatchInputEvent(inputEvent);	//dispatch the input event to the application frame
			if(!inputEvent.isConsumed())	//if the input has still not been consumed
			{
				final InputStrategy inputStrategy=getInputStrategy();	//get our input strategy, if any
				if(inputStrategy!=null)	//if we have an input strategy
				{
					return inputStrategy.input(input);	//send the input to the input strategy and return whether it was consumed
				}
			}
		}
		return true;	//indicate that the event was consumed
	}

	/**Creates an input event for the given input.
	@param input The input to process.
	@return An event to represent the given input.
	@exception NullPointerException if the given input is <code>null</code>.
	@exception IllegalArgumentException if an unknown input type was given.
	@see CommandInput
	@see KeystrokeInput
	@see MouseClickInput
	*/
	protected InputEvent createInputEvent(final Input input)
	{
		final InputEvent inputEvent;	//create an input event from the input
		if(input instanceof CommandInput)	//if this is command input
		{
			return new CommandEvent(this, (CommandInput)input);	//return a command event
		}
		else if(input instanceof KeystrokeInput)	//if this is keystroke input
		{
			return new KeyPressEvent(this, (KeystrokeInput)input);	//return a key press event
		}
		else if(input instanceof MouseClickInput)	//if this is mouse click input
		{
			return new MouseClickEvent(this, (MouseClickInput)input);	//return a mouse click event
		}
		else	//if we don't recognize the event
		{
			throw new IllegalArgumentException("Unrecognized input: "+input);
		}
	}

	/**Logs the given session-related information with a default log level of {@link InformationLevel#LOG}.
	This is a convenience method that delegates to {@link #log(InformationLevel, String, String, String, Map, CharSequence)}.
	@param subject The log subject identification, or <code>null</code> if there is no related subject.
	@param predicate The log predicate identification, or <code>null</code> if there is no related predicate.
	@param object The log object identification, or <code>null</code> if there is no related object.
	@param parameters The map of log parameters, or <code>null</code> if there are no parameters.
	@param comment The log comment, or <code>null</code> if there is no log comment.
	@exception NullPointerException if the given log level is <code>null</code>.
	*/
	public void log(final String subject, final String predicate, final String object, final Map<?, ?> parameters, final CharSequence comment)
	{
		log(InformationLevel.LOG, subject, predicate, object, parameters, comment);	//log the information with LOG level
	}

	/**Logs the given session-related information.
	@param level The log information level.
	@param subject The log subject identification, or <code>null</code> if there is no related subject.
	@param predicate The log predicate identification, or <code>null</code> if there is no related predicate.
	@param object The log object identification, or <code>null</code> if there is no related object.
	@param parameters The map of log parameters, or <code>null</code> if there are no parameters.
	@param comment The log comment, or <code>null</code> if there is no log comment.
	@exception NullPointerException if the given log level is <code>null</code>.
	*/
	public void log(final InformationLevel level, final String subject, final String predicate, final String object, final Map<?, ?> parameters, final CharSequence comment)
	{
/*TODO fix; bring back after testing out-of-memory error
		final Writer logWriter=getLogWriter();	//get the log writer
		try
		{
			Log.log(logWriter, level, navigationPath, subject, predicate, object, parameters, comment);	//write the log information to the file; get the navigation path directly in case it hasn't yet been initialized and it still null so that an exception won't be thrown
			logWriter.flush();	//flush the log information
		}
		catch(final IOException ioException)	//if there is a log error
		{
			Debug.error(ioException);	//log the error in the debug log
		}
*/
	}	

	/**Notifies the user of one or more notifications to be presented in sequence.
	The notification's label and/or icon, if specified, will be used as the dialog title and icon, respectively;
	if either is not specified, a label and/or icon based upon the notification's severity will be used.
	If the selected option to any notification is fatal, the remaining notifications will not be performed.
	The absence of an option selection is considered fatal only if a fatal option was presented for a given notification.  
	This is a convenience method that delegates to {@link #notify(Runnable, Notification...)}.
	@param notifications One or more notification informations to relay.
	@exception NullPointerException if the given notifications is <code>null</code>.
	@exception IllegalArgumentException if no notifications are given.
	*/
	public void notify(final Notification... notifications)
	{
		notify(null, notifications);	//perform the notifications with no ending logic
	}

	/**Notifies the user of one or more notifications to be presented in sequence, with optional logic to be executed after all notifications have taken place.
	The notification's label and/or icon, if specified, will be used as the dialog title and icon, respectively;
	if either is not specified, a label and/or icon based upon the notification's severity will be used.
	If the selected option to any notification is fatal, the remaining notifications and the specified logic, if any, will not be performed.
	The absence of an option selection is considered fatal only if a fatal option was presented for a given notification.  
	This implementation delegates to {@link #notify(Notification, Runnable)}.
	@param notifications One or more notification informations to relay.
	@param afterNotify The code that executes after notification has taken place, or <code>null</code> if no action should be taken after notification.
	@exception NullPointerException if the given notifications is <code>null</code>.
	@exception IllegalArgumentException if no notifications are given.
	*/
	public void notify(final Runnable afterNotify, final Notification... notifications)
	{
		if(checkInstance(notifications, "Notifications cannot be null.").length==0)	//if no notifications were given
		{
			throw new IllegalArgumentException("No notifications were given.");
		}
		final Runnable enumerateNotifications=new Runnable()	//create code for notifying all notifications, including the extra one we were given
		{
			private int notificationIndex=0;	//start at the first notification
			public void run()	//each time this logic is executed
			{
				if(notificationIndex<notifications.length)	//if there are more notifications
				{
					AbstractGuiseSession.this.notify(notifications[notificationIndex++], this);	//notify of the current notification (advancing to the next one), specifying that this runnable should be called again
				}
				else if(afterNotify!=null)	//if we're out of notifications and there's something we're supposed to run after all notifications are done
				{
					afterNotify.run();	//run whatever logic we're supposed to execute after notifications
				}
			}
		};
		enumerateNotifications.run();	//start enumerating the notifications
	}

	/**Notifies the user of the given notification information, with optional logic to be executed after notification takes place.
	The notification's label and/or icon, if specified, will be used as the dialog title and icon, respectively;
	if either is not specified, a label and/or icon based upon the notification's severity will be used.
	If the selected option to any notification is fatal, the remaining notifications and the specified logic, if any, will not be performed.
	The absence of an option selection is considered fatal only if a fatal option was presented for a given notification.  
	@param notification The notification information to relay.
	@param afterNotify The code that executes after notification has taken place, or <code>null</code> if no action should be taken after notification.
	*/
	protected void notify(final Notification notification, final Runnable afterNotify)
	{
		final Notification.Severity severity=notification.getSeverity();	//get the notification severity
		if(severity==Notification.Severity.ERROR)	//if this is an error notification TODO improve to work with all notifications; this will entail adding a general public debug write method and translating between log report levels and notification severities
		{
			final Throwable throwable=notification.getError();	//get the error, if any
			if(throwable!=null)	//if there is an error
			{
				Debug.error(throwable);	//produce a stack trace
			}
		}
		final NotificationOptionDialogFrame optionDialogFrame=new NotificationOptionDialogFrame(notification);	//create a dialog from the notification
		final String notificationLabel=notification.getLabel();	//get the notification's specified label, if any
		if(notificationLabel!=null)	//if the notification specified a label
		{
			optionDialogFrame.setLabel(notificationLabel);	//set the label from the notification
			optionDialogFrame.setLabelContentType(notification.getLabelContentType());	//set the label content type from the notification
		}
		else	//if the notification specified no label
		{
			optionDialogFrame.setLabel(severity.getLabel());	//set the label based upon the severity			
		}
		URI icon=notification.getGlyphURI();	//get the notification's specified icon, if any
		if(icon==null)	//if no icon was specified
		{
			icon=severity.getGlyph();	//set the icon based upon the severity
		}		
		optionDialogFrame.setGlyphURI(icon);	//set the icon
		optionDialogFrame.setLineExtent(new Extent(0.33, Extent.Unit.RELATIVE));	//set the preferred dimensions		
		optionDialogFrame.open(new AbstractGenericPropertyChangeListener<Frame.Mode>()	//show the dialog and listen for the frame closing
				{
					public void propertyChange(final GenericPropertyChangeEvent<Frame.Mode> genericPropertyChangeEvent)	//listen for the dialog mode changing
					{
						if(genericPropertyChangeEvent.getNewValue()==null && afterNotify!=null)	//if the dialog is now nonmodal and there is logic that should take place after notification
						{
							final Notification.Option selectedOption=optionDialogFrame.getValue();	//get the selected option, if any
								//we'll determine if the user selection is fatal and therefore we should not perform the given logic
							if(selectedOption!=null)	//if an option was selected
							{
								if(selectedOption.isFatal())	//if a fatal option was selected
								{
									return;	//don't perform the given logic
								}
							}
							else	//if no option was selected, determine if this should be considered fatal
							{
								for(final Notification.Option option:notification.getOptions())	//look at the given options; if there is a fatal option available, consider the absence of an option selected to be fatal
								{
									if(option.isFatal())	//if a fatal option is available
									{
										return;	//don't perform the given logic										
									}
								}
							}
							afterNotify.run();	//run the code that takes place after notification
						}
					}
				}
		);
	}

	/**Notifies the user of the given errors in sequence.
	If the selected option to any notification is fatal, the remaining notifications will not be performed.
	The absence of an option selection is considered fatal only if a fatal option was presented for a given notification.  
	This is a convenience method that delegates to {@link #notify(Runnable, Throwable...)}.
	@param errors The errors with which to notify the user.
	@exception NullPointerException if the given errors is <code>null</code>.
	@exception IllegalArgumentException if no errors are given.
	*/
	public void notify(final Throwable... errors)
	{		
		notify(null, errors);	//notify the user with no post-notification action
	}

	/**Notifies the user of the given error in sequence, with optional logic to be executed after notification takes place.
	If the selected option to any notification is fatal, the remaining notifications and the specified logic, if any, will not be performed.
	The absence of an option selection is considered fatal only if a fatal option was presented for a given notification.  
	This is a convenience method that delegates to {@link #notify(Runnable, Notification...)}.
	@param error The error with which to notify the user.
	@param afterNotify The code that executes after notification has taken place, or <code>null</code> if no action should be taken after notification.
	@exception NullPointerException if the given errors is <code>null</code>.
	@exception IllegalArgumentException if no errors are given.
	*/
	public void notify(final Runnable afterNotify, final Throwable... errors)
	{
		final int errorCount=checkInstance(errors, "Errors cannot be null").length;	//see how many errors there are (we'll let the other methods check for a non-empty array)
		final Notification[] notifications=new Notification[errorCount];	//create an array of as many notifications as are errors
		for(int i=0; i<errorCount; ++i)	//for each error
		{
			notifications[i]=new Notification(errors[i]);	//create a new notification for this error
		}
		notify(afterNotify, notifications);	//notify the user with the notifications of the errors
	}

	/**The set of string reference delimiters, <code>SOS</code> and <code>ST</code>.*/
	private final static String STRING_REFERENCE_DELIMITERS=new StringBuilder().append(START_OF_STRING_CHAR).append(STRING_TERMINATOR_CHAR).toString();
	
	/**Resolves a string by replacing any string references with a string from the resources.
	A string reference begins with the Start of String (<code>SOS</code>) control character (U+0098) and ends with a String Terminator (<code>ST</code>) control character (U+009C).
	The string between these delimiters will be used to look up a string resource using {@link #getStringResource(String)}.
	Strings retrieved from resources will be recursively resolved.
	<p>String references appearing between an <code>SOS</code>/<code>ST</code> pair that that begin with the character {@value Resources#STRING_VALUE_REFERENCE_PREFIX_CHAR}
	will be considered string values and, after they are recursively resolved, will be applied as formatting arguments to the remaining resolved text using {@link MessageFormat#format(String, Object...)}.</p>
	@param string The string to be resolved.
	@return The resolved string with any string references replaced with the appropriate string from the resources.
	@exception NullPointerException if the given string is <code>null</code>.
	@exception IllegalArgumentException if a string reference has no ending String Terminator control character (U+009C).
	@exception MissingResourceException if no resource could be found associated with a string reference.
	@exception ClassCastException if the resource associated with a string reference is not an instance of <code>String</code>.
	@see Resources#createStringResourceReference(String)
	@see Resources#createStringValueReference(String)
	@see #getStringResource(String)
	*/
	public String resolveString(final String string) throws MissingResourceException
	{
//TODO add later if we create a Guise-specific parameter feature; for now we use {}		int parameterCount=0;	//keep track of how many parameters have appeared
		List<String> argumentList=null;	//the lazily-created list of arguments
		int fromIndex=0;	//keep track of where we are in the string
		int stringStartIndex=string.indexOf(START_OF_STRING_CHAR, fromIndex);	//see if there is a string reference in the string
		if(stringStartIndex>=0)	//if there is a string reference
		{
			final StringBuilder stringBuilder=new StringBuilder();	//create a new string builder
			do
			{
				if(stringStartIndex>fromIndex)	//if there is literal text to add
				{
					stringBuilder.append(string.substring(fromIndex, stringStartIndex));	//append the literal text
				}
				int terminatorsRemaining=1;	//we currently expect to find one more string terminator
				int searchStartIndex=stringStartIndex+1;	//start searching after the SOS character
				int stringEndIndex;	//we'll store here the end of the string reference
				do
				{
					stringEndIndex=charIndexOf(string, STRING_REFERENCE_DELIMITERS, searchStartIndex);	//search for the end of the string (or the beginning of another reference)
					if(stringEndIndex<0)	//if there is no string delimiter (and therefore no string terminator)
					{
						throw new IllegalArgumentException("String reference missing String Terminator (U+009C).");
					}
					final char delimiter=string.charAt(stringEndIndex);	//get the delimiter we encountered
					switch(string.charAt(stringEndIndex))	//see if we encountered a string terminator or another start of string
					{
						case STRING_TERMINATOR_CHAR:	//if we ended the string
							--terminatorsRemaining;	//we have one less terminator left
							break;
						case START_OF_STRING_CHAR:	//if we started another string
							++terminatorsRemaining;	//we have one more terminator left to find
							break;
						default:
							throw new AssertionError("Unrecognized delimiter: "+delimiter);
					}
					searchStartIndex=stringEndIndex+1;	//if we need to search some more, we'll start searching immediately after the last delimiter
				}
				while(terminatorsRemaining>0);	//keep searching until 
				final String stringReference=string.substring(stringStartIndex+1, stringEndIndex);	//get the string reference
				if(startsWith(stringReference, STRING_VALUE_REFERENCE_PREFIX_CHAR))	//if this is a value reference
				{
					final String stringValue=resolveString(stringReference.substring(1));	//resolve the actual reference (i.e. ignore the string value reference prefix character)
					if(argumentList==null)	//if we don't yet have an argument list
					{
						argumentList=new ArrayList<String>();	//create a new argument list
					}
					argumentList.add(stringValue);	//add this string value to our argument list
				}
				else	//if this is not a value reference, it must be a resource reference
				{
					final String stringResource=getStringResource(stringReference);	//look up the string resource, using the reference as a resource key
					stringBuilder.append(resolveString(stringResource));	//resolve and append the value of the string reference
				}
				fromIndex=stringEndIndex+1;	//show the new search location
				stringStartIndex=string.indexOf(START_OF_STRING_CHAR, fromIndex);	//see if there is another string reference in the string
			}
			while(stringStartIndex>=0);	//keep building the string as long as there are more string references
			final int length=string.length();	//get the string length
			if(fromIndex<length)	//if there is remaining literal text
			{
				stringBuilder.append(string.substring(fromIndex, length));	//append the remaining text
			}
			String resolvedString=stringBuilder.toString();	//get the string we constructed
			if(argumentList!=null)	//if we have string value arguments
			{
				resolvedString=format(resolvedString, (Object[])argumentList.toArray());	//use the string as a format pattern, formatted using the collected arguments
			}
			return resolvedString;	//return the string we resolved
		}
		else	//if there is no string reference
		{
			return string;	//return the string as-is
		}
	}

	/**Resolves a URI against the application base path, looking up the URI from the resources if necessary.
	If the URI has the "resource" scheme, its scheme-specific part will be used to look up the actual URI using {@link #getURIResource(String)}.
	If suffixes are given, they will be appended to the resource key in order, separated by '.'.
	If no resource is associated with that resource key, a resource will be retrieved using the unadorned resource key.
	URIs retrieved from resources will be recursively resolved without suffixes.
	Relative paths will be resolved relative to the application base path. Absolute paths will be considered already resolved, as will absolute URIs.
	For an application base path "/path/to/application/", resolving "relative/path" will yield "/path/to/application/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path". Resolving "http://example.com/path" will yield "http://example.com/path".
	@param uri The URI to be resolved.
	@return The uri resolved against resources the application base path.
	@exception NullPointerException if the given URI is <code>null</code>.
	@exception MissingResourceException if no resource could be found associated with a string reference.
	@see Resources#createURIResourceReference(String)
	@see #getURIResource(String)
	@see GuiseApplication#resolveURI(URI)
	*/
	public URI resolveURI(final URI uri, final String... suffixes) throws MissingResourceException
	{
		if(RESOURCE_SCHEME.equals(uri.getScheme()))	//if this is a resource reference
		{
			final String resourceKey=uri.getSchemeSpecificPart();	//get the resource key from the URI
			URI resourceURI=null;	//we'll try to determine the resource URI
			if(suffixes.length>0)	//if there are suffixes
			{
				final String decoratedResourceKey=formatList(new StringBuilder(resourceKey).append('.'), '.', suffixes).toString();	//append the suffixes
				try
				{
					resourceURI=getURIResource(decoratedResourceKey);	//look up the resource using the decorated resource key
				}
				catch(final MissingResourceException missingResourceException)	//if there is no resource associated with the decorated resource key, ignore the error and try again with the base resource key
				{
				}
			}
			if(resourceURI==null)	//if we haven't found a resource URI, yet
			{
				resourceURI=getURIResource(resourceKey);	//look up the resource using the plain resource key				
			}
			return resolveURI(resourceURI);	//recursively resolve the URI
		}
		else	//if this is not a resource reference
		{
			return getApplication().resolveURI(uri);	//ask the application to resolve this URI normally
		}
	}

}
