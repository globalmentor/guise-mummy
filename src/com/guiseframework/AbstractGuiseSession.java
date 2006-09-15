package com.guiseframework;

import java.beans.PropertyChangeEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.parsers.*;

import static java.util.Collections.*;

import com.garretwilson.beans.*;
import com.garretwilson.event.PostponedEvent;
import com.garretwilson.io.BOMInputStreamReader;
import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.rdf.*;
import com.garretwilson.rdf.ploop.PLOOPProcessor;
import com.garretwilson.text.FormatUtilities;
import com.garretwilson.util.CollectionUtilities;
import com.garretwilson.util.Debug;
import com.garretwilson.util.ResourceBundleUtilities;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.Orientation;
import com.guiseframework.context.GuiseContext;
import com.guiseframework.event.*;
import com.guiseframework.model.InformationLevel;
import com.guiseframework.model.Notification;
import com.guiseframework.style.*;
import com.guiseframework.theme.Theme;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static com.garretwilson.io.FileConstants.*;
import static com.garretwilson.io.FileUtilities.*;
import static com.garretwilson.io.WriterUtilities.*;
import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.text.CharacterConstants.*;
import static com.garretwilson.text.CharacterEncodingConstants.*;
import static com.garretwilson.text.FormatUtilities.*;
import static com.garretwilson.text.TextUtilities.*;
import static com.garretwilson.text.xml.XMLUtilities.*;
import static com.guiseframework.GuiseResourceConstants.*;

/**An abstract implementation that keeps track of the components of a user session.
@author Garret Wilson
*/
public abstract class AbstractGuiseSession extends BoundPropertyObject implements GuiseSession
{

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
	private ApplicationFrame<?> applicationFrame=null;

		/**Returns the application frame, which is available after {@link #initialize()} has been called.
		This method must not be called before initialization has occurred.
		@return The application frame.
		@exception IllegalStateException if this session has not yet been initialized.
		*/
		public ApplicationFrame<?> getApplicationFrame()
		{
			if(applicationFrame==null)	//if this session has not yet been initialized
			{
				throw new IllegalStateException("Guise session "+this+" has not yet been initialized and therefore has no application frame.");
			}
			return applicationFrame;	//return the application frame
		}

	/**The non-thread-safe document builder that parses XML documents for input to RDF.*/
	private final DocumentBuilder documentBuilder;

		/**@return The non-thread-safe document builder that parses XML documents for input to RDF.*/
		private DocumentBuilder getDocumentBuilder() {return documentBuilder;}

	/**The cache of components keyed to component destinations.*/
	private final Map<ComponentDestination, Component> destinationComponentMap=synchronizedMap(new HashMap<ComponentDestination, Component>());

	/**The user local environment.*/
	private GuiseEnvironment environment;

		/**@return The user local environment.*/
		public GuiseEnvironment getEnvironment() {return environment;}

		/**Sets the user local environment.
		This method will not normally be called directly from applications.
		This is a bound property.
		@param newEnvironment The new user local environment.
		@exception NullPointerException if the given environment is <code>null</code>.
		@see GuiseSession#ENVIRONMENT_PROPERTY
		*/
		public void setEnvironment(final GuiseEnvironment newEnvironment)
		{
			if(!ObjectUtilities.equals(environment, newEnvironment))	//if the value is really changing (compare their values, rather than identity)
			{
				final GuiseEnvironment oldEnvironment=environment;	//get the old value
				environment=checkInstance(newEnvironment, "Guise session environment cannot be null.");	//actually change the value
				firePropertyChange(ENVIRONMENT_PROPERTY, oldEnvironment, newEnvironment);	//indicate that the value changed
			}
		}
	
	/**The list of visible frames according to z-order.*/
	private final List<Frame<?>> frameList=new CopyOnWriteArrayList<Frame<?>>();

		/**@return An iterator to all visible frames.*/
		public Iterator<Frame<?>> getFrameIterator() {return frameList.iterator();}
	
		/**Adds a frame to the list of visible frames.
		This method should usually only be called by the frames themselves.
		@param frame The frame to add.
		*/
		public void addFrame(final Frame<?> frame)
		{
			frameList.add(frame);	//add this frame to the list
		}

		/**Removes a frame from the list of visible frames.
		This method should usually only be called by the frames themselves.
		@param frame The frame to remove.
		*/
		public void removeFrame(final Frame<?> frame)
		{
			frameList.remove(frame);	//remove this frame from the list
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
		If this session does not yet have a resource bundle, one will be created based upon the current locale.
		The returned resource bundle should only be used temporarily and should not be saved,
		as the resource bundle may change if the session locale or the application resource bundle base name changes.
		The resource bundle retrieved will allow hierarchical resolution in the following priority:
		<ol>
			<li>Any resource defined by the application.</li>
			<li>Any resource defined by the theme.</li>
			<li>Any resource defined by default by Guise.</li>
		</ol>
		@return The resource bundle containing the resources for this session, based upon the locale.
		@exception MissingResourceException if no resource bundle for the application's specified base name can be found.
		@see GuiseApplication#getResourceBundle(Locale)
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
		public ResourceBundle getResourceBundle()
		{
			if(resourceBundle==null)	//if the resource bundle has not yet been loaded
			{
				final Locale locale=getLocale();	//get the current locale
				resourceBundle=getApplication().getResourceBundle(locale);	//ask the application for the resource bundle based upon the locale
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
		If the given resource is a string, it will be converted to an {@link RGBColor}.
		This is a preferred convenience method for accessing the resources in the session's resource bundle.
		@param resourceKey The key of the resource to retrieve.
		@return The resource associated with the specified resource key.
		@exception NullPointerException if the provided resource key is <code>null</code>.
		@exception MissingResourceException if no resource could be found associated with the given key.
		@exception ClassCastException if the resource associated with the given key is not an instance of {@link String} or {@link Color}.
		@exception IllegalArgumentException if a string is provided that is not a valid color.
		@see #getResourceBundle()
		@see #getColorResource(String, Color)
		*/
		public Color<?> getColorResource(final String resourceKey) throws MissingResourceException
		{
			final Object resource=getResource(resourceKey);	//retrieve a resource from the resource bundle
			if(resource instanceof String)	//if the resource is a string
			{
				return RGBColor.valueOf(resolveString((String)resource));	//create a color from the resource string
			}
			else	//if the resource is not a string, assume it is a color
			{
				return (Color<?>)resource;	//return the resource as a color object, throwing a ClassCastException if it isn't an instance of Color
			}
		}

		/**Retrieves a {@link Color} resource from the resource bundle, using a specified default if no such resource is available.
		If the given resource is a string, it will be converted to an {@link RGBColor}.
		This is a preferred convenience method for accessing the resources in the session's resource bundle.
		@param resourceKey The key of the resource to retrieve.
		@param defaultValue The default value to use if there is no resource associated with the given key.
		@return The resource associated with the specified resource key or the default if none is available.
		@exception NullPointerException if the provided resource key is <code>null</code>.
		@exception ClassCastException if the resource associated with the given key is not an instance of {@link String} or {@link Color}.
		@see #getResourceBundle()
		@see #getColorResource(String)
		*/
		public Color<?> getColorResource(final String resourceKey, final Color<?> defaultValue) throws MissingResourceException
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

	/**The current session theme.*/
	private Theme theme=new Theme();	//create a default theme URI

		/**@return The current session theme.*/
		public Theme getTheme() {return theme;}

		/**Sets the current session theme.
		This is a bound property.
		@param newTheme The new session theme.
		@exception NullPointerException if the given theme is <code>null</code>.
		@see #THEME_PROPERTY
		*/
		public void setTheme(final Theme newTheme)
		{
			if(!ObjectUtilities.equals(theme, newTheme))	//if the value is really changing
			{
				final Theme oldTheme=theme;	//get the old value
				theme=checkInstance(newTheme, "Guise session theme cannot be null.");	//actually change the value
				firePropertyChange(THEME_PROPERTY, oldTheme, newTheme);	//indicate that the value changed
			}
		}

	/**Guise application constructor.
	The session local will initially be set to the locale of the associated Guise application.
	No operation must be performed inside the constructor that would require the presence of the Guise session within this thread group.
	@param application The Guise application to which this session belongs.
	*/
	public AbstractGuiseSession(final GuiseApplication application)
	{
		this.application=application;	//save the Guise instance
		this.baseURI=application.getContainer().getBaseURI().resolve(application.getBasePath());	//default to a base URI calculated from the application base path resolved to the container's base URI
		try
		{
			documentBuilder=createDocumentBuilder(true);	//create a new namespace-aware document builder
		}
		catch(final ParserConfigurationException parserConfigurationException)	//if we can't find an XML parser
		{
			throw new AssertionError(parserConfigurationException);
		}	
		this.environment=new DefaultGuiseEnvironment();	//create a default environment
		this.locale=application.getLocales().get(0);	//default to the first application locale
//TODO del when works		this.locale=application.getDefaultLocale();	//default to the application locale
		this.orientation=Orientation.getOrientation(locale);	//set the orientation default based upon the locale
		logWriter=new OutputStreamWriter(System.err);	//default to logging to the error output; this will be replaced after the session is created
	}

	/**Determines if there is a panel bound to the given appplication context-relative path.
	This class synchronizes on {@link #navigationPathPanelMap}.
	@param path The appplication context-relative path within the Guise container context.
	@return <code>true</code> if there is a panel bound to the given path, or <code>false</code> if no panel is bound to the given path.
	@exception NullPointerException if the path is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
/*TODO del if not needed
	public boolean hasNavigationPanel(final String path)
	{
		if(isAbsolutePath(checkInstance(path, "Path cannot be null")))	//if the path is absolute
		{
			throw new IllegalArgumentException("Navigation path cannot be absolute: "+path);
		}
			//see if we have a cached panel
		synchronized(navigationPathPanelMap)	//don't allow the map to be modified while we access it
		{
			if(navigationPathPanelMap.get(path)!=null)	//if we have a panel cached at this navigation path
			{
				return true;	//we have a navigation panel at that path
			}
		}
		return getApplication().getNavigationPanelClass(path)!=null;	//see if we know the class to use for creating an application panel at this path
	}
*/

	/**Retrieves the component bound to the given destination.
	If a component has already been created and cached, it will be be returned; otherwise, one will be created and cached. 
	This method synchronizes on {@link #destinationComponentMap}.
	@param destination The destination for which a component should be returned.
	@return The component bound to the given destination.
	@exception NullPointerException if the destination is <code>null</code>.
	@exception IllegalStateException if the component class bound to the destination does not provide appropriate constructors, is an interface, is abstract, or throws an exception during instantiation.
	*/
	public Component<?> getDestinationComponent(final ComponentDestination destination)
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
	public Component<?> releaseDestinationComponent(final ComponentDestination destination)
	{
		return destinationComponentMap.remove(destination);	//uncache the component
	}

	/**Retrieves the component bound to the given appplication context-relative path.
	This is a convenience method that retrieves the component associated with the component destination for the given navigation path.
	This method calls {@link GuiseApplication#getDestination(String)}.
	This method calls {@link #getDestinationComponent(ComponentDestination)}.
	@param path The appplication context-relative path within the Guise container context.
	@return The component bound to the given path. 
	@exception NullPointerException if the path is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	@exception IllegalArgumentException if no component is appropriate to associated the given navigation path (i.e. the given navigation path is not associated with a component destination).
	@exception IllegalStateException if the component class bound to the path does not provide appropriate constructors, is an interface, is abstract, or throws an exception during instantiation.
	@see ComponentDestination
	*/
	public NavigationPanel<?> getNavigationPanel(final String path)	//TODO rename to getNavigationComponent()
	{
		final Destination destination=getApplication().getDestination(path);	//get the destination associated with the given path
		if(!(destination instanceof ComponentDestination))	//if the destination is not a component destination
		{
			throw new IllegalArgumentException("Navigation path "+path+" does not designate a component destination.");
		}
		return (NavigationPanel<?>)getDestinationComponent((ComponentDestination)destination);	//return the component TODO remove the cast once a navigation panel is no longer assumed
	}


	/**Creates the component for the given class.
	@param componentClass The class representing the component to create.
	@return The created component.
	@exception IllegalStateException if the component class does not provide a default constructor, is an interface, is abstract, or throws an exception during instantiation.
	*/
	protected Component<?> createComponent(final Class<? extends Component> componentClass)
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
		initializeComponent(component);	//initialize the component from an RDF description, if possible
		return component;	//return the component
	}
	
	/**Initializes a component, optionally with a description in an RDF resource file.
	This method first tries to load a PLOOP RDF description of the component in an RDF file in the classpath in the same directory with the same name as the class file, with an <code>.rdf</code> extension.
	That is, for the class <code>MyComponent.class</code> this method first tries to load <code>MyComponent.rdf</code> from the same directory in the classpath.
	If this is successful, the component is initialized from this RDF description.
	This implementation calls {@link #initializeComponent(Component, InputStream)}.
	The component's {@link Component#initialize()} is called whether there is an RDF description.
	This method synchronizes on {@link #getDocumentBuilder()}.
	@param component The component to initialize.
	@exception MissingResourceException if no resource could be found associated with the given key.
	@exception IllegalArgumentException if the RDF description does not provide a resource description of the same type as the specified component.
	@exception IllegalStateException if the given component has already been initialized.
	@see Component#initialize()
	@see <a href="http://www.ploop.org/">PLOOP</a>
	*/
	public void initializeComponent(final Component<?> component)
	{
		final Class<?> componentClass=component.getClass();	//get the class of the component
		final String descriptionFilename=addExtension(getLocalName(componentClass), RDF_EXTENSION);	//create a name in the form ClassName.rdf
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
				throw new AssertionError(ioException);	//TODO fix better
			}		
		}
		else	//if there is no description file
		{
			component.initialize();	//call the initialize() method manually
		}
	}

	/**Initializes a component with a description in an RDF resource file.
	This method calls {@link Component#initialize()} after initializing the component from the description.
	This implementation calls {@link #initializeComponent(Component, InputStream)}.
	This method synchronizes on {@link #getDocumentBuilder()}.
	@param component The component to initialize.
	@param resourceKey The key to an RDF description resource file.
	@exception MissingResourceException if no resource could be found associated with the given key.
	@exception IllegalArgumentException if the RDF description does not provide a resource description of the same type as the specified component.
	@exception IllegalStateException if the given component has already been initialized.
	@see Component#initialize()
	*/
	public void initializeComponentFromResource(final Component<?> component, final String resourceKey)
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
	}
	
	/**Initializes a component from the contents of an RDF description input stream.
	This method calls {@link Component#initialize()} after initializing the component from the description.
	This method synchronizes on {@link #getDocumentBuilder()}.
	@param component The component to initialize.
	@param descriptionInputStream The input stream containing an RDF description.
	@exception IllegalArgumentException if the RDF description does not provide a resource description of the same type as the specified component.
	@exception IllegalStateException if the given component has already been initialized.
	@see Component#initialize()
	*/
	public void initializeComponent(final Component<?> component, final InputStream descriptionInputStream)
	{
		try
		{
			synchronized(getDocumentBuilder())	//synchronize because the document builder is not thread-safe
			{
				final URI BASE_URI=URI.create("guise:/");	//TODO fix
				final Document document=getDocumentBuilder().parse(descriptionInputStream);	//parse the description document
		//TODO del Debug.trace("just parsed XML:", XMLUtilities.toString(document));
				final RDFXMLProcessor rdfProcessor=new RDFXMLProcessor();	//create a new RDF processor
				final RDF rdf=rdfProcessor.processRDF(document, BASE_URI);	//process the RDF from the XML
				//integrate the structure into the RDF
//TODO del					Debug.trace("just read RDF", RDFUtilities.toString(rdf));
//TODO delDebug.trace("just read RDF", RDFUtilities.toString(rdf));
				final URI componentResourceTypeURI=new URI(JAVA_SCHEME, component.getClass().getName(), null);	//create a new URI that indicates the type of the resource description we expect
				final RDFResource componentResource=RDFUtilities.getResourceByType(rdf, componentResourceTypeURI);	//try to locate the description of the given component
				if(componentResource!=null)	//if there is a resource description of a matching type
				{
					final PLOOPProcessor ploopProcessor=new PLOOPProcessor(this);	//create a new PLOOP processor, passing the Guise session to use as a default constructor argument					
					ploopProcessor.initializeObject(component, componentResource);	//initialize the component from this resource
					component.initialize();	//initialize the component
					final List<Object> objects=ploopProcessor.getObjects(rdf);	//make sure all described Java objects in the RDF instance have been created
				}
				else	//if there is no resource of the appropriate type
				{
					throw new IllegalArgumentException("No resource description found of type "+componentResourceTypeURI);
				}
			}
		}
		catch(final SAXException saxException)	//we don't expect parsing errors
		{
			throw new AssertionError(saxException);	//TODO maybe change to throwing an IOException
		}
		catch(final URISyntaxException uriSyntaxException)
		{
			throw new AssertionError(uriSyntaxException);	//TODO fix better
		}
		catch(final IOException ioException)	//if there is an I/O exception
		{
			throw new AssertionError(ioException);	//TODO fix better
		}		
		catch(final ClassNotFoundException classNotFoundTargetException)
		{
			throw new AssertionError(classNotFoundTargetException);	//TODO fix better
		}		
		catch(final InvocationTargetException invocationTargetException)
		{
			throw new AssertionError(invocationTargetException);	//TODO fix better
		}
	}

	/**Initializes all components in a hierarchy in depth-first order.
	@param component The parent of the component tree to initialize.
	*/
/*TODO fix
	protected static void initializeComponents(final Component<?> component)
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
		public void beginModalNavigation(final ModalNavigationPanel<?, ?> modalNavigationPanel, final ModalNavigation modalNavigation)
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
		public boolean endModalNavigation(final ModalNavigationPanel<?, ?> modalNavigationPanel)
		{
			final String navigationPath=getNavigationPath();	//get our current navigation path
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
							if(application.resolvePath(navigationPath).equals(currentModalNavigation.getNewNavigationURI().getPath()))	//if we're navigating where we expect to be (if we somehow got to here at something other than the modal navigation path, we wouldn't want to remove the current navigation path)
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
	private String navigationPath=null;

		/**Reports the navigation path relative to the application context path.
		@return The path representing the current navigation location of the Guise application.
		@exception IllegalStateException if this message has been called before the navigation path has been initialized.
		*/
		public String getNavigationPath()
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
		@see #navigate(String)
		@see #navigate(URI)
		@see #navigateModal(String, ModalNavigationListener)
		@see #navigateModal(URI, ModalNavigationListener)
		*/
		public void setNavigationPath(final String navigationPath)
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
		This method calls {@link #setNavigationPath(String)} and {@link #setBookmark(Bookmark)}.  
		This implementation logs the navigation change.
		@param navigationPath The navigation path relative to the application context path.
		@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
		@param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
		@exception NullPointerException if the given navigation path is <code>null</code>.
		@see #setNavigationPath(String)
		@see #setBookmark(Bookmark)
		@see #getApplicationFrame()
		*/
		public void setNavigation(final String navigationPath, final Bookmark bookmark, final URI referrerURI)
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
				final NavigationEvent navigationEvent=new NavigationEvent(this, navigationPath, bookmark, referrerURI);	//create a navigation event with the session as the source of the event
				fireNavigated(getApplicationFrame(), navigationEvent);	//fire a navigation event to all components in the application frame hierarchy
			}			
		}

		/**Fires a {@link NavigationEvent} to all {@link NavigationListener}s in the given component hierarchy.
		@param component The component to which the navigation event should be fired, along with all children, if the component or any children implement {@link NavigationListener}.
		@param navigationEvent The navigation event to fire.
		@see NavigationListener
		@see NavigationEvent 
		*/
		protected void fireNavigated(final Component<?> component, final NavigationEvent navigationEvent)
		{
			if(component instanceof NavigationListener)	//if the component is a navigation listener
			{
				((NavigationListener)component).navigated(navigationEvent);	//fire the event to the component
			}
			if(component instanceof CompositeComponent)	//if the component is a composite component
			{
				for(final Component<?> childComponent:((CompositeComponent<?>)component).getChildren())	//for every child component
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
		public void navigate(final String path)
		{
			navigate(path, null);	//navigate to the requested path with no bookmark
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
		public void navigate(final String path, final Bookmark bookmark)
		{
			final String navigationPath=bookmark!=null ? path+bookmark.toString() : path;	//append the bookmark if needed
			navigate(createPathURI(navigationPath));	//navigate to the requested URI, converting the path to a URI and verifying that it is only a path
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
			requestedNavigation=new Navigation(getApplication().resolveURI(createPathURI(getNavigationPath())), getApplication().resolveURI(checkInstance(uri, "URI cannot be null.")), viewportID);	//resolve the URI against the application context path
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
		public void navigateModal(final String path, final ModalNavigationListener modalListener)
		{
			navigateModal(createPathURI(path), modalListener);	//navigate to the requested URI, converting the path to a URI and verifying that it is only a path
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
		public void navigateModal(final String path, final Bookmark bookmark, final ModalNavigationListener modalListener)
		{
			final String navigationPath=bookmark!=null ? path+bookmark.toString() : path;	//append the bookmark if needed
			navigateModal(createPathURI(navigationPath), modalListener);	//navigate to the requested URI, converting the path to a URI and verifying that it is only a path
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
			requestedNavigation=new ModalNavigation(getApplication().resolveURI(createPathURI(getNavigationPath())), getApplication().resolveURI(checkInstance(uri, "URI cannot be null.")), modalListener);	//resolve the URI against the application context path
		}		
		
	/**The object that listenes for context state changes and updates the set of context states in response.*/
	private final ContextStateListener contextStateListener=new ContextStateListener();

		/**@return The object that listenes for context state changes and updates the set of context states in response.*/
		protected ContextStateListener getContextStateListener() {return contextStateListener;}

	/**The unmodifiable set of all states of available Guise contexts.*/
	private Set<GuiseContext.State> contextStateSet=CollectionUtilities.emptySet();

		/**@return The unmodifiable set of all states of available Guise contexts.*/
		public Set<GuiseContext.State> getContextStates() {return contextStateSet;}

	/**The current context for this session, or <code>null</code> if there currently is no context.*/
	private GuiseContext context=null;

		/**@return The current context for this session, or <code>null</code> if there currently is no context.*/
		public synchronized GuiseContext getContext() {return context;}

		/**Sets the current context.
		This method should not normally be called by application code.
		@param context The current context for this session, or <code>null</code> if there currently is no context.
		*/
		public synchronized void setContext(final GuiseContext context)
		{
			if(this.context!=context)	//if the context is really changing
			{
				final GuiseContext oldContext=this.context;	//save the old context
				if(oldContext!=null)	//if there was a previous context
				{
					oldContext.removePropertyChangeListener(GuiseContext.STATE_PROPERTY, getContextStateListener());	//stop listening for context state changes					
				}
				this.context=context;	//set the context
				if(context!=null)	//if a new context is given
				{
					context.addPropertyChangeListener(GuiseContext.STATE_PROPERTY, getContextStateListener());	//listen for context state changes and update the set of context states in response
				}
				updateContextStates();	//make sure the record of context states is up to date
			}
		}
	
		/**Checks the state of the current context.
		If any model change events are pending and no context is processing an event, the model change events are processed.
		@see GuiseContext.State#PROCESS_EVENT
		@see #fireQueuedModelEvents()
		*/
		protected synchronized void updateContextStates()
		{
			final GuiseContext context=getContext();	//get the current context
			if(context==null || context.getState()!=GuiseContext.State.PROCESS_EVENT)	//if the context is not processing an event
			{
				fireQueuedModelEvents();	//fire any queued events				
			}
		}

	/**The synchronized list of postponed model events.*/
	private final List<PostponedEvent<?>> queuedModelEventList=synchronizedList(new LinkedList<PostponedEvent<?>>());	//use a linked list because we'll be removing items from the front of the list as we process the events TODO fix; this doesn't seem to be the case anymore, so we can use a synchronized array list

		/**Queues a postponed event to be fired after the context has finished processing events.
		If a Guise context is currently processing events, the event will be queued for later.
		If no Guise context is currently processing events, the event will be fired immediately.
		@param postponedEvent The event to fire at a later time.
		@see GuiseContext.State#PROCESS_EVENT
		*/
		public synchronized void queueEvent(final PostponedEvent<?> postponedEvent)
		{
			final GuiseContext context=getContext();	//get the current context
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

	/**The variable used to generate unique component IDs.*/
	private final AtomicLong idCounter=new AtomicLong(0);

		/**Generates a new ID string unique to this session.
		This ID is appropriate for being used in a new component, for example.
		The ID will begin with a letter and be composed only of letters and numbers.
		@return A new ID unique to this session.
		*/
		public String generateID()
		{
			final long counter=idCounter.incrementAndGet();	//atomically get the next counter value
			return "id"+Long.toHexString(counter);	//create an ID from the counter
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

	/**The class that listens for context state changes and updates the context state set in response.
	@author Garret Wilson
	*/
	protected class ContextStateListener extends AbstractGenericPropertyChangeListener<GuiseContext.State>
	{
		/**Called when a bound property is changed.
		@param propertyChangeEvent An event object describing the event source, the property that has changed, and its old and new values.
		*/
		public void propertyChange(final GenericPropertyChangeEvent<GuiseContext.State> propertyChangeEvent)
		{
			updateContextStates();	//update the context states when a context state changes
		}
	}

	/**Creates a component to indicate Guise busy status.
	@return A component to indicate Guise busy status.
	@see Theme#ICON_BUSY
	*/
	public Component<?> createBusyComponent()
	{
		return new DefaultBusyPanel();	//create the default busy panel
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
	
	/**Notifies the user of the given notification information.
	This is a convenience method that delegates to {@link #notify(Notification, Runnable)}.
	@param notification The notification information to relay.
	*/
	public void notify(final Notification notification)
	{
		notify(notification, null);	//notify the user with no post-notification action
	}

	/**Notifies the user of the given notification information, with optional logic to be executed after notification takes place.
	@param notification The notification information to relay.
	@param afterNotify The code that executes after notification has taken place, or <code>null</code> if no action should be taken after notification.
	*/
	public void notify(final Notification notification, final Runnable afterNotify)
	{
		final Text text=new Text();	//create a new text component
		text.setText(notification.getMessage());	//set the text, which may include a resource reference
		final DefaultOptionDialogFrame optionDialogFrame=new DefaultOptionDialogFrame(text, DefaultOptionDialogFrame.Option.OK);	//create a dialog with an OK button
		optionDialogFrame.setLabel(notification.getSeverity().toString());	//TODO improve title; load from resources
		optionDialogFrame.open(new AbstractGenericPropertyChangeListener<Mode>()	//show the dialog and listen for the frame closing
				{
					public void propertyChange(final GenericPropertyChangeEvent<Mode> genericPropertyChangeEvent)	//listen for the dialog mode changing
					{
						if(genericPropertyChangeEvent.getNewValue()==null && afterNotify!=null)	//if the dialog is now nonmodal and there is logic that should take place after notification
						{
							afterNotify.run();	//run the code that takes place after notification
						}
					}
				}
		);
	}

	/**Notifies the user of the given error.
	This is a convenience method that delegates to {@link #notify(Throwable, Runnable)}.
	@param error The error with which to notify the user.
	*/
	public void notify(final Throwable error)
	{
		notify(error, null);	//notify the user with no post-notification action
	}

	/**Notifies the user of the given error, with optional logic to be executed after notification takes place..
	This is a convenience method that delegates to {@link #notify(Notification, Runnable)}.
	@param error The error with which to notify the user.
	@param afterNotify The code that executes after notification has taken place, or <code>null</code> if no action should be taken after notification.
	*/
	public void notify(final Throwable error, final Runnable afterNotify)
	{
		notify(new Notification(error), afterNotify);	//notify the user with a notification of the error
	}

	/**Resolves a string by replacing any string references with a string from the resources.
	A string reference begins with the Start of String control character (U+0098) and ends with a String Terminator control character (U+009C).
	The string between these delimiters will be used to look up a string resource using {@link #getStringResource(String)}.
	Strings retrieved from resources will be recursively resolved.
	@param string The string to be resolved.
	@return The resolved string with any string references replaced with the appropriate string from the resources.
	@exception NullPointerException if the given string is <code>null</code>.
	@exception IllegalArgumentException if a string reference has no ending String Terminator control character (U+009C).
	@exception MissingResourceException if no resource could be found associated with a string reference.
	@exception ClassCastException if the resource associated with a string reference is not an instance of <code>String</code>.
	@see Resources#createStringResourceReference(String)
	@see #getStringResource(String)
	*/
	public String resolveString(final String string) throws MissingResourceException
	{
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
				final int stringEndIndex=string.indexOf(STRING_TERMINATOR_CHAR, stringStartIndex+1);	//search for the end of the string
				if(stringEndIndex<0)	//if there is no string terminator
				{
					throw new IllegalArgumentException("String reference missing String Terminator (U+009C).");
				}
				final String stringResourceKey=string.substring(stringStartIndex+1, stringEndIndex);	//get the string reference
				final String stringResource=getStringResource(stringResourceKey);	//look up the string resource
				stringBuilder.append(resolveString(stringResource));	//resolved and append the value of the string reference
				fromIndex=stringEndIndex+1;	//show the new search location
				stringStartIndex=string.indexOf(START_OF_STRING_CHAR, fromIndex);	//see if there is another string reference in the string
			}
			while(stringStartIndex>=0);	//keep building the string as long as there are more string references
			final int length=string.length();	//get the string length
			if(fromIndex<length)	//if there is remaining literal text
			{
				stringBuilder.append(string.substring(fromIndex, length));	//append the remaining				
			}
			return stringBuilder.toString();	//return the string we constructed
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
