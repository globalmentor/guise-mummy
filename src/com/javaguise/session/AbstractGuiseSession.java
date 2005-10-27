package com.javaguise.session;

import java.beans.PropertyChangeEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.*;

import com.garretwilson.beans.*;
import com.garretwilson.event.PostponedEvent;
import com.javaguise.GuiseApplication;
import com.javaguise.component.*;
import com.javaguise.component.layout.Orientation;
import com.javaguise.context.GuiseContext;
import com.javaguise.event.ModalEvent;
import com.javaguise.event.ModalNavigationListener;
import com.garretwilson.io.BOMInputStreamReader;
import static com.garretwilson.io.WriterUtilities.*;
import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.util.CollectionUtilities;
import com.garretwilson.util.Debug;
import com.garretwilson.util.ResourceBundleUtilities;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.text.CharacterEncodingConstants.UTF_8;
import static com.garretwilson.text.xml.XMLUtilities.*;
import static com.garretwilson.util.ResourceBundleUtilities.*;
import static com.garretwilson.util.SetUtilities.*;
import static com.javaguise.GuiseResourceConstants.*;

/**An abstract implementation that keeps track of the components of a user session.
@author Garret Wilson
*/
public abstract class AbstractGuiseSession extends BoundPropertyObject implements GuiseSession
{

	/**The Guise application to which this session belongs.*/
	private final GuiseApplication application;

		/**@return The Guise application to which this session belongs.*/
		public GuiseApplication getApplication() {return application;}

	/**The application frame.*/
	private final ApplicationFrame<?> applicationFrame;

		/**@return The application frame.*/
		public ApplicationFrame<?> getApplicationFrame() {return applicationFrame;}

	/**The cache of navigation panel types keyed to appplication context-relative paths.*/
	private final Map<String, NavigationPanel> navigationPathPanelMap=synchronizedMap(new HashMap<String, NavigationPanel>());

		/**Binds a frame to a particular appplication context-relative path.
		Any existing binding for the given context-relative path is replaced.
		@param path The appplication context-relative path to which the frame should be bound.
		@param frame The frame to bind to this particular context-relative path.
		@return The frame previously bound to the given context-relative path, or <code>null</code> if no frame was previously bound to the path.
		@exception NullPointerException if the path and/or the frame is null.
		@exception IllegalArgumentException if the provided path is absolute.
		*/
/*TODO del if not needed
		protected Frame bindNavigationFrame(final String path, final Frame frame)
		{
			if(isAbsolutePath(path))	//if the path is absolute
			{
				throw new IllegalArgumentException("Bound navigation path cannot be absolute: "+path);
			}
			return navigationPathFrameBindingMap.put(checkNull(path, "Path cannot be null."), checkNull(frame, "Bound frame cannot be null."));	//store the binding
		}
*/

		/**Removes the binding a frame from a particular appplication context-relative path.
		If no frame is bound to the given navigation path, no action occurs.
		@param path The appplication context-relative path to which the frame should be bound.
		@return The frame previously bound to the given context-relative path, or <code>null</code> if no frame was previously bound to the path.
		@exception NullPointerException if the path is null.
		@exception IllegalArgumentException if the provided path is absolute.
		*/
/*TODO del if not needed
		protected Frame unbindNavigationFrame(final String path)
		{
			if(isAbsolutePath(path))	//if the path is absolute
			{
				throw new IllegalArgumentException("Bound navigation path cannot be absolute: "+path);
			}
			return navigationPathFrameBindingMap.remove(checkNull(path, "Path cannot be null."));	//remove the binding
		}
*/

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
				locale=checkNull(newLocale, "Guise session locale cannot be null.");	//actually change the value
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
			final Set<Locale> supportedLocales=getApplication().getSupportedLocales();	//get the application's supported locales	TODO maybe don't expose the whole set
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
				final Orientation oldOrientation=checkNull(orientation, "Orientation cannot be null");	//get the old value
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
		@return The resource bundle containing the resources for this session, based upon the locale.
		@exception MissingResourceException if no resource bundle for the application's specified base name can be found.
		@see GuiseApplication#getResourceBundleBaseName()
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
				final ClassLoader loader=getClass().getClassLoader();	//get our class loader
				final ResourceBundle defaultResourceBundle=ResourceBundleUtilities.getResourceBundle(DEFAULT_RESOURCE_BUNDLE_BASE_NAME, locale, loader, null);	//load the default resource bundle
				final String resourceBundleBaseName=getApplication().getResourceBundleBaseName();	//get the specified resource bundle base name
				if(resourceBundleBaseName!=null && !resourceBundleBaseName.equals(DEFAULT_RESOURCE_BUNDLE_BASE_NAME))	//if a distinct resource bundle base name was specified
				{
					resourceBundle=ResourceBundleUtilities.getResourceBundle(resourceBundleBaseName, locale, loader, defaultResourceBundle);	//load the new resource bundle, specifying the default resource bundle as the parent					
				}
				else	//if no custom resource bundle was specified
				{
					resourceBundle=defaultResourceBundle;	//just use the default resource bundle
				}
//TODO del when works				resourceBundle=ResourceBundle.getBundle(getApplication().getResourceBundleBaseName(), getLocale());	//load a resource bundle appropriate for the locale
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
		private final PropertyValueChangeListener<String> resourceBundleReleasePropertyValueChangeListener=new AbstractPropertyValueChangeListener<String>()
			{
				/**Called when a bound property is changed.
				@param propertyValueChangeEvent An event object describing the event source, the property that has changed, and its old and new values.
				*/
				public void propertyValueChange(final PropertyValueChangeEvent<String> propertyValueChangeEvent)
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
		@see #getResource(String, T)
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
		public String getStringResource(final String resourceKey) throws MissingResourceException
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
						final InputStream inputStream=getApplication().getResourceAsStream(applicationResourcePath);	//get a stream to the resource
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
				return Boolean.valueOf((String)resource);	//get the Boolean value of the resource string
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
				return Integer.valueOf((String)resource);	//get the Integer value of the resource string
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
				return URI.create((String)resource);	//create a URI from the resource string
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
		@exception ClassCastException if the resource associated with the given key is not an instance of <code>URI</code>.
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

	/**Guise application constructor.
	The session local will initially be set to the locale of the associated Guise application.
	@param application The Guise application to which this session belongs.
	*/
	public AbstractGuiseSession(final GuiseApplication application)
	{
		this.application=application;	//save the Guise instance
		this.locale=application.getDefaultLocale();	//default to the application locale
		this.orientation=Orientation.getOrientation(locale);	//set the orientation default based upon the locale
		this.applicationFrame=application.createApplicationFrame(this);	//create the application frame
		this.applicationFrame.open();	//open the application frame
	}

	/**Retrieves the panel bound to the given appplication context-relative path.
	If a panel has already been created and cached, it will be be returned; otherwise, one will be created and cached. 
	The panel will be given an ID of a modified form of the path.
	@param path The appplication context-relative path within the Guise container context.
	@return The panel bound to the given path, or <code>null</code> if no panel is bound to the given path.
	@exception NullPointerException if the path is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	@exception IllegalStateException if the panel class bound to the path does not provide appropriate constructors, is an interface, is abstract, or throws an exception during instantiation.
	*/
	public NavigationPanel<?> getNavigationPanel(final String path)
	{
		if(isAbsolutePath(checkNull(path, "Path cannot be null")))	//if the path is absolute
		{
			throw new IllegalArgumentException("Navigation path cannot be absolute: "+path);
		}
		NavigationPanel panel;	//we'll store the panel here, either a cached panel or a created panel
		synchronized(navigationPathPanelMap)	//don't allow the map to be modified while we access it
		{
			panel=navigationPathPanelMap.get(path);	//get the bound panel type, if any
			if(panel==null)	//if no panel is cached
			{
				final Class<? extends NavigationPanel> panelClass=getApplication().getNavigationPanelClass(path);	//see which panel we should show for this path
				if(panelClass!=null)	//if we found a panel class for this path
				{
					try
					{
						try
						{
							final String panelID=createName(path);	//convert the path to a valid ID TODO use a Guise-specific routine or, better yet, bind an ID with the panel
							panel=panelClass.getConstructor(GuiseSession.class, String.class).newInstance(this, panelID);	//find the Guise session and ID constructor and create an instance of the class
						}
						catch(final NoSuchMethodException noSuchMethodException)	//if there was no Guise session and string ID constructor
						{
							panel=panelClass.getConstructor(GuiseSession.class).newInstance(this);	//use the Guise session constructor if there is one					
						}
						navigationPathPanelMap.put(path, panel);	//bind the panel to the path, caching it for next time
					}
					catch(final NoSuchMethodException noSuchMethodException)	//if the constructor could not be found
					{
						throw new IllegalStateException(noSuchMethodException);
					}
					catch(final IllegalAccessException illegalAccessException)	//if the constructor is not visible
					{
						throw new IllegalStateException(illegalAccessException);
					}
					catch(final InstantiationException instantiationException)	//if the class is an interface or is abstract
					{
						throw new IllegalStateException(instantiationException);
					}
					catch(final InvocationTargetException invocationTargetException)	//if the constructor throws an exception
					{
						throw new IllegalStateException(invocationTargetException);
					}
				}
			}
		}
		return panel;	//return the panel, or null if we couldn't find a panel
	}

	/**Releases the panel bound to the given appplication context-relative path.
	@param path The appplication context-relative path within the Guise container context.
	@return The panel previously bound to the given path, or <code>null</code> if no panel was bound to the given path.
	@exception NullPointerException if the path is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public NavigationPanel<?> releaseNavigationPanel(final String path)
	{
		if(isAbsolutePath(checkNull(path, "Path cannot be null")))	//if the path is absolute
		{
			throw new IllegalArgumentException("Bound navigation path cannot be absolute: "+path);
		}
		return navigationPathPanelMap.remove(path);	//uncache the panel
	}

	/**The stack of modal navigation points.*/
	private final List<ModalNavigation> modalNavigationStack=synchronizedList(new ArrayList<ModalNavigation>());

		/**Pushes the given model navigation onto the top of the stack.
		@param modalNavigation The modal navigation to add.
		@exception NullPointerException if the given modal navigation is <code>null</code>.
		*/
		protected void pushModalNavigation(final ModalNavigation modalNavigation)
		{
			modalNavigationStack.add(checkNull(modalNavigation, "Modal navigation cannot be null."));	//push the modal navigation onto the top of the stack (the end of the list)
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

		/**Begins modal interaction for a particular modal panel.
		The modal navigation is pushed onto the stack, and an event is fired to the modal listener of the modal navigation.
		@param <N> The type of navigation panel beginning navigation.
		@param modalNavigationPanel The panel for which modal navigation state should begin.
		@param modalNavigation The state of modal navigation.
		@see #pushModalNavigation(ModalNavigation)
		*/
		public <N extends ModalNavigationPanel<?, ?>> void beginModalNavigation(final N modalNavigationPanel, final ModalNavigation<N> modalNavigation)
		{
			//TODO release the navigation panel, maybe, just in case
			pushModalNavigation(modalNavigation);	//push the modal navigation onto the top of the modal navigation stack
			modalNavigation.getModalListener().modalBegan(new ModalEvent<N>(this, modalNavigationPanel));
		}

		/**Ends modal interaction for a particular modal panel.
		The panel is released from the cache so that new navigation will create a new modal panel.
		This method is called by modal panels and should seldom if ever be called directly.
		If the current modal state corresponds to the current navigation state, the current modal state is removed, the modal state's event is fired, and modal state is handed to the previous modal state, if any.
		Otherwise, navigation is transferred to the modal panel's referring URI, if any.
		If the given modal panel is not the panel at the current navigation path, the modal state is not changed, although navigation and releasal will still occur.
		@param <P> The type of navigation panel ending navigation.
		@param modalNavigationPanel The panel for which modal navigation state should be ended.
		@return true if modality actually ended for the given panel.
		@see #popModalNavigation()
		@see Frame#getReferrerURI()
		@see #releaseNavigationPanel(String)
		*/
		@SuppressWarnings("unchecked")	//we check to see that the given panel is the same one at this navigation path, and that this navigation path is in a modal state, implying that the current navigation state's listener has same generic result type as does the modal panel
		public <P extends ModalNavigationPanel<?, ?>> boolean endModalNavigation(final P modalNavigationPanel)
		{
			final String navigationPath=getNavigationPath();	//get our current navigation path
			ModalNavigation modalNavigation=null;	//if we actually end modal navigation, we'll store the information here
//TODO fix			URI navigationURI=modalPanel.getReferrerURI();	//in the worse case scenario, we'll want to go back to where the modal panel came from, if that's available
			URI navigationURI=null;	//TODO fix
			if(navigationPathPanelMap.get(navigationPath)==modalNavigationPanel)	//before we try to actually ending modality, make sure this panel is actually the one at our current navigation path
			{
				synchronized(modalNavigationStack)	//don't allow anyone to to access the modal navigation stack while we access it
				{
					final ModalNavigation currentModalNavigation=peekModalNavigation();	//see which model navigation is on the top of the stack
					if(currentModalNavigation!=null)	//if there is a modal navigation currently in use
					{
						if(getApplication().resolvePath(navigationPath).equals(currentModalNavigation.getNewNavigationURI().getPath()))	//if we're navigating where we expect to be (if we somehow got to here at something other than the modal navigation path, we wouldn't want to remove the current navigation path)
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
			releaseNavigationPanel(navigationPath);	//release the panel associated with this navigation path
			if(modalNavigation!=null)	//if we if we ended modality for the panel
			{
				((ModalNavigationListener<P>)modalNavigation.getModalListener()).modalEnded(new ModalEvent<P>(this, modalNavigationPanel));	//send an event to the modal listener
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

		/**Changes the navigation path of the session so that user interaction can change to another panel.
		If the given navigation path is the same as the current navigation path, no action occurs.
		@param navigationPath The navigation path relative to the application context path.
		@exception IllegalArgumentException if the provided path is absolute.
		@exception IllegalArgumentException if the navigation path is not recognized (e.g. there is no panel bound to the navigation path).
		*/
		protected void setNavigationPath(final String navigationPath)
		{
			if(!ObjectUtilities.equals(this.navigationPath, navigationPath))	//if the navigation path is really changing
			{
				if(getApplication().getNavigationPanelClass(navigationPath)==null)	//if no panel is bound to the given navigation path
				{
					throw new IllegalArgumentException("Unknown navigation path: "+navigationPath);
				}
				this.navigationPath=navigationPath;	//change the navigation path TODO fire an event
			}
		}

	/**The requested navigation, or <code>null</code> if no navigation has been requested.*/
	private Navigation requestedNavigation=null;

		/**@return The requested navigation, or <code>null</code> if no navigation has been requested.*/
		protected Navigation getRequestedNavigation() {return requestedNavigation;}

		/**Removes any requests for navigation.*/
		protected void clearRequestedNavigation() {requestedNavigation=null;}

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
			navigate(createPathURI(path));	//navigate to the requested URI, converting the path to a URI and verifying that it is only a path
		}
	
		/**Requests navigation to the specified URI.
		The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
		Later requested navigation before navigation occurs will override this request.
		@param uri Either a relative or absolute path, or an absolute URI.
		@exception NullPointerException if the given URI is <code>null</code>.
		*/
		public void navigate(final URI uri)
		{
			requestedNavigation=new Navigation(getApplication().resolveURI(createPathURI(getNavigationPath())), getApplication().resolveURI(checkNull(uri, "URI cannot be null.")));	//resolve the URI against the application context path
		}

		/**Requests modal navigation to the specified path.
		The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
		Later requested navigation before navigation occurs will override this request.
		@param <P> The type of navigation panel for modal navigation.
		@param path A path that is either relative to the application context path or is absolute.
		@param modalListener The listener to respond to the end of modal interaction.
		@exception NullPointerException if the given path is <code>null</code>.
		@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #navigate(URI)} should be used instead).
		@see #navigateModal(URI, ModalNavigationListener)
		*/
		public <P extends ModalNavigationPanel<?, ?>> void navigateModal(final String path, final ModalNavigationListener<P> modalListener)
		{
			navigateModal(createPathURI(path), modalListener);	//navigate to the requested URI, converting the path to a URI and verifying that it is only a path
		}

		/**Requests modal navigation to the specified URI.
		The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
		Later requested navigation before navigation occurs will override this request.
		@param <P> The type of navigation panel for modal navigation.
		@param uri Either a relative or absolute path, or an absolute URI.
		@param modalListener The listener to respond to the end of modal interaction.
		@exception NullPointerException if the given URI is <code>null</code>.
		*/
		public <P extends ModalNavigationPanel<?, ?>> void navigateModal(final URI uri, final ModalNavigationListener<P> modalListener)
		{
			requestedNavigation=new ModalNavigation<P>(getApplication().resolveURI(createPathURI(getNavigationPath())), getApplication().resolveURI(checkNull(uri, "URI cannot be null.")), modalListener);	//resolve the URI against the application context path
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
		@param context The current context for this session, or <code>null</code> if there currently is no context.
		*/
		protected synchronized void setContext(final GuiseContext context)
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
			if(context!=null && (context.getState()==GuiseContext.State.PROCESS_EVENT))	//if the context is processing an event
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
	@see #destroy()
	*/
	protected void initialize()
	{
		getApplication().addPropertyChangeListener(GuiseApplication.RESOURCE_BUNDLE_BASE_NAME_PROPERTY, resourceBundleReleasePropertyValueChangeListener);	//when the application changes its resource bundle base name, release the resource bundle		
	}

	/**Called when the session is destroyed.
	@see #initialize()
	*/
	protected void destroy()
	{
		getApplication().removePropertyChangeListener(GuiseApplication.RESOURCE_BUNDLE_BASE_NAME_PROPERTY, resourceBundleReleasePropertyValueChangeListener);	//stop listening for the application to change its resource bundle base name				
	}

	/**The variable used to generate unique component IDs.*/
	private long componentIDCounter=0;

		/**@return A new component ID appropriate for using with a new component.*/
		public String generateComponentID()
		{
			final long counter;
			synchronized(this)	//don't allow other threads to modify the counter while we're modifying it
			{
				counter=++componentIDCounter;	//increment the component ID counter and retrieve the resulting value
			}
			return "id"+Long.toHexString(counter);	//create an ID from the counter
		}	

	/**Reports that a bound property has changed. This method can be called	when a bound property has changed and it will send the appropriate property change event to any registered property change listeners.
	This version fires a property change event even if no listeners are attached, so that the Guise session can be notified of the event.
	No event is fired if old and new are both <code>null</code> or are both non-<code>null</code> and equal according to the {@link Object#equals(java.lang.Object)} method.
	This method delegates actual firing of the event to {@link #firePropertyChange(PropertyChangeEvent)}.
	@param propertyName The name of the property being changed.
	@param oldValue The old property value.
	@param newValue The new property value.
	@see #firePropertyChange(PropertyChangeEvent)
	@see #hasListeners(String)
	@see PropertyValueChangeEvent
	@see PropertyValueChangeListener
	*/
	protected <V> void firePropertyChange(final String propertyName, V oldValue, final V newValue)
	{
		if(!ObjectUtilities.equals(oldValue, newValue))	//if the values are different
		{					
			firePropertyChange(new PropertyValueChangeEvent<V>(this, propertyName, oldValue, newValue));	//create and fire a genericized subclass of a property change event
		}
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
	protected class ContextStateListener extends AbstractPropertyValueChangeListener<GuiseContext.State>
	{
		/**Called when a bound property is changed.
		@param propertyValueChangeEvent An event object describing the event source, the property that has changed, and its old and new values.
		*/
		public void propertyValueChange(final PropertyValueChangeEvent<GuiseContext.State> propertyValueChangeEvent)
		{
			updateContextStates();	//update the context states when a context state changes
		}
	}
}
