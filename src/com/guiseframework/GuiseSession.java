package com.guiseframework;

import java.io.*;
import java.net.URI;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.*;

import com.garretwilson.beans.PropertyBindable;
import com.garretwilson.event.PostponedEvent;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.Orientation;
import com.guiseframework.context.GuiseContext;
import com.guiseframework.event.*;
import com.guiseframework.model.InformationLevel;
import com.guiseframework.model.Notification;
import com.guiseframework.prototype.ActionPrototype;
import com.guiseframework.style.*;
import com.guiseframework.theme.Theme;

import static com.garretwilson.lang.ClassUtilities.*;

/**Represents a session with a user.
A Swing-based client application may have only one session, while a web server application will likely have multiple sessions.
@author Garret Wilson
*/
public interface GuiseSession extends PropertyBindable
{

	/**The environment bound property.*/
	public final static String ENVIRONMENT_PROPERTY=getPropertyName(GuiseSession.class, "environment");
	/**The locale bound property.*/
	public final static String LOCALE_PROPERTY=getPropertyName(GuiseSession.class, "locale");
	/**The orientation bound property.*/
	public final static String ORIENTATION_PROPERTY=getPropertyName(GuiseSession.class, "orientation");
	/**The principal (e.g. user) bound property.*/
	public final static String PRINCIPAL_PROPERTY=getPropertyName(GuiseSession.class, "principal");
	/**The theme URI bound property.*/
	public final static String THEME_URI_PROPERTY=getPropertyName(GuiseSession.class, "themeURI");

	/**@return The Guise application to which this session belongs.*/
	public GuiseApplication getApplication();

	/**@return The writer for writing to the log file, which may not be thread-safe.*/
	public Writer getLogWriter();

	/**Sets the log writer.
	@param logWriter The writer for writing to the log file, which may not be thread-safe.
	@exception NullPointerException if the given log writer is <code>null</code>.
	*/
	public void setLogWriter(final Writer logWriter);

	/**Reports the base URI of the session.
	The base URI is an absolute URI that ends with the base path of the application, which ends with a slash ('/').
	The session base URI may be different for different sessions, and may not be equal to the application base path resolved to the container's base URI.
	@return The base URI representing the Guise session.
	*/
	public URI getBaseURI();

	/**Sets the base URI of the session.
	The raw path of the base URI must be equal to the application base path.
	@param baseURI The new base URI of the session.
	@exception NullPointerException if the given base URI is <code>null</code>.
	@exception IllegalArgumentException if the raw path of the given base URI is not equal to the application base path.
	*/
	public void setBaseURI(final URI baseURI);

	/**@return The application frame.*/
	public ApplicationFrame<?> getApplicationFrame();

	/**@return The user local environment.*/
	public GuiseEnvironment getEnvironment();

	/**Sets the user local environment.
	This method will not normally be called directly from applications.
	This is a bound property.
	@param newEnvironment The new user local environment.
	@exception NullPointerException if the given environment is <code>null</code>.
	@see #ENVIRONMENT_PROPERTY
	*/
	public void setEnvironment(final GuiseEnvironment newEnvironment);

	/**@return An iterator to all visible frames.*/
	public Iterator<Frame<?>> getFrameIterator();

	/**Adds a frame to the list of visible frames.
	This method should usually only be called by the frames themselves.
	@param frame The frame to add.
	*/
	public void addFrame(final Frame<?> frame);

	/**Removes a frame from the list of visible frames.
	This method should usually only be called by the frames themselves.
	@param frame The frame to remove.
	*/
	public void removeFrame(final Frame<?> frame);

	/**@return The current session locale.*/
	public Locale getLocale();

	/**Sets the current session locale.
	The default orientation will be updated if needed to reflect the new locale.
	This is a bound property.
	@param newLocale The new session locale.
	@exception NullPointerException if the given locale is <code>null</code>.
	@see #LOCALE_PROPERTY
	@see #setOrientation(Orientation)
	*/
	public void setLocale(final Locale newLocale);

	/**Requests that the locale be changed to one of the given locales.
	Each of the locales in the list are examined in order, and the first one supported by the application is used.
	A requested locale is accepted if a more general locale is supported. (i.e. <code>en-US</code> is accepted if <code>en</code> is supported.)
	@param requestedLocales The locales requested, in order of preference.
	@return The accepted locale (which may be a variation of this locale), or <code>null</code> if none of the given locales are supported by the application.
	@see GuiseApplication#getSupportedLocales()
	@see #setLocale(Locale)
	*/
	public Locale requestLocale(final List<Locale> requestedLocales);

	/**@return The default internationalization orientation of components for this session.*/
	public Orientation getOrientation();

	/**Sets the default orientation.
	This is a bound property
	@param newOrientation The new default internationalization orientation of components for this session.
	@exception NullPointerException if the given orientation is <code>null</code>.
	@see #ORIENTATION_PROPERTY
	*/
	public void setOrientation(final Orientation newOrientation);

	/**Initializes a component, optionally with a description in an RDF resource file.
	This method first tries to load a PLOOP RDF description of the component in an RDF file with the same name as the class file in the same directory, with an <code>.rdf</code> extension.
	That is, for the class <code>MyComponent.class</code> this method first tries to load <code>MyComponent.rdf</code> from the same directory.
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
	public void initializeComponent(final Component<?> component);
	
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
	public void initializeComponentFromResource(final Component<?> component, final String resourceKey);
	
	/**Initializes a component from the contents of an RDF description input stream.
	This method calls {@link Component#initialize()} after initializing the component from the description.
	This method synchronizes on {@link #getDocumentBuilder()}.
	@param component The component to initialize.
	@param descriptionInputStream The input stream containing an RDF description.
	@exception IllegalArgumentException if the RDF description does not provide a resource description of the same type as the specified component.
	@exception IllegalStateException if the given component has already been initialized.
	@see Component#initialize()
	*/
	public void initializeComponent(final Component<?> component, final InputStream descriptionInputStream);

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
	public ResourceBundle getResourceBundle() throws MissingResourceException;

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
	public <T> T getResource(final String resourceKey) throws MissingResourceException;

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
	public <T> T getResource(final String resourceKey, final T defaultValue) throws MissingResourceException;

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
	public String getStringResource(final String resourceKey) throws MissingResourceException;

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
	public String getStringResource(final String resourceKey, final String defaultValue) throws MissingResourceException;

	/**Retrieves a <code>Boolean</code> resource from the resource bundle.
	If the given resource is a string, it will be interpreted according to the {@link Boolean#valueOf(java.lang.String)} rules.
	This is a preferred convenience method for accessing the resources in the session's resource bundle.
	@param resourceKey The key of the resource to retrieve.
	@return The resource associated with the specified resource key.
	@exception NullPointerException if the provided resource key is <code>null</code>.
	@exception MissingResourceException if no resource could be found associated with the given key.
	@exception ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>Boolean</code> object.
	@see #getResourceBundle()
	@see #getBooleanResource(String, Boolean)
	*/
	public Boolean getBooleanResource(final String resourceKey) throws MissingResourceException;

	/**Retrieves a <code>Boolean</code> resource from the resource bundle, using a specified default if no such resource is available.
	If the given resource is a string, it will be interpreted according to the {@link Boolean#valueOf(java.lang.String)} rules.
	This is a preferred convenience method for accessing the resources in the session's resource bundle.
	@param resourceKey The key of the resource to retrieve.
	@param defaultValue The default value to use if there is no resource associated with the given key.
	@return The resource associated with the specified resource key or the default if none is available.
	@exception NullPointerException if the provided resource key is <code>null</code>.
		@exception ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>Boolean</code> object.
	@see #getResourceBundle()
	@see #getBooleanResource(String)
	*/
	public Boolean getBooleanResource(final String resourceKey, final Boolean defaultValue) throws MissingResourceException;

	/**Retrieves a {@link Color} resource from the resource bundle.
	If the given resource is a string, it will be resolved and converted to a color using {@link AbstractColor#valueOf(CharSequence)}.
	This is a preferred convenience method for accessing the resources in the session's resource bundle.
	@param resourceKey The key of the resource to retrieve.
	@return The resource associated with the specified resource key.
	@exception NullPointerException if the provided resource key is <code>null</code>.
	@exception MissingResourceException if no resource could be found associated with the given key.
	@exception ClassCastException if the resource associated with the given key is not an instance of {@link String} or {@link Color}.
	@exception IllegalArgumentException if a string is provided that is not a valid color.
	@see #getResourceBundle()
	@see #getColorResource(String, Color)
	@see AbstractColor#valueOf(CharSequence)
	*/
	public Color<?> getColorResource(final String resourceKey) throws MissingResourceException;

	/**Retrieves a {@link Color} resource from the resource bundle, using a specified default if no such resource is available.
	If the given resource is a string, it will be resolved and converted to a color using {@link AbstractColor#valueOf(CharSequence)}.
	This is a preferred convenience method for accessing the resources in the session's resource bundle.
	@param resourceKey The key of the resource to retrieve.
	@param defaultValue The default value to use if there is no resource associated with the given key.
	@return The resource associated with the specified resource key or the default if none is available.
	@exception NullPointerException if the provided resource key is <code>null</code>.
	@exception ClassCastException if the resource associated with the given key is not an instance of {@link String} or {@link Color}.
	@see #getResourceBundle()
	@see #getColorResource(String)
	@see AbstractColor#valueOf(CharSequence)
	*/
	public Color<?> getColorResource(final String resourceKey, final Color<?> defaultValue) throws MissingResourceException;

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
	public Integer getIntegerResource(final String resourceKey) throws MissingResourceException;

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
	public Integer getIntegerResource(final String resourceKey, final Integer defaultValue) throws MissingResourceException;

	/**Retrieves a <code>URI</code> resource from the resource bundle.
	If the given resource is a string, it will be converted to a URI.
	This is a preferred convenience method for accessing the resources in the session's resource bundle.
	@param resourceKey The key of the resource to retrieve.
	@return The resource associated with the specified resource key.
	@exception NullPointerException if the provided resource key is <code>null</code>.
	@exception MissingResourceException if no resource could be found associated with the given key.
	@exception ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>URI</code> object.
	@exception IllegalArgumentException if a string is provided that is not a valid URI.
	@see #getResourceBundle()
	@see #getURIResource(String, URI)
	*/
	public URI getURIResource(final String resourceKey) throws MissingResourceException;

	/**Retrieves a <code>URI</code> resource from the resource bundle, using a specified default if no such resource is available.
	If the given resource is a string, it will be converted to a URI.
	This is a preferred convenience method for accessing the resources in the session's resource bundle.
	@param resourceKey The key of the resource to retrieve.
	@param defaultValue The default value to use if there is no resource associated with the given key.
	@return The resource associated with the specified resource key or the default if none is available.
	@exception NullPointerException if the provided resource key is <code>null</code>.
	@exception ClassCastException if the resource associated with the given key is not an instance of <code>String</code> or <code>URI</code> object.
	@see #getResourceBundle()
	@see #getURIResource(String)
	*/
	public URI getURIResource(final String resourceKey, final URI defaultValue) throws MissingResourceException;

	/**@return The current principal (e.g. logged-in user), or <code>null</code> if there is no principal authenticated for this session.*/
	public Principal getPrincipal();

	/**Sets the current principal (e.g. logged-in user).
	This is a bound property.
	@param newPrincipal The new principal, or <code>null</code> if there should be no associated principal (e.g. the user should be logged off).
	@see #PRINCIPAL_PROPERTY
	*/
	public void setPrincipal(final Principal newPrincipal);

	/**Returns the current session theme.
	If this session's theme has not yet been loaded, this method loads the theme.
	@return The current session theme.
	@exception IOException if there is an error loading the theme.
	@see #getThemeURI()
	*/
	public Theme getTheme() throws IOException;

	/**@return The URI of the session theme, to be resolved against the application base path.*/
	public URI getThemeURI();

	/**Sets the URI of the session theme.
	The current theme, if any, will be released and loaded the next time {@link #getTheme()} is called.
	This is a bound property.
	@param newThemeURI The URI of the new session theme.
	@exception NullPointerException if the given theme URI is <code>null</code>.
	@see #THEME_URI_PROPERTY
	@see #getTheme()
	*/
	public void setThemeURI(final URI newThemeURI);

	/**@return The action prototype for presenting application information.*/
	public ActionPrototype getAboutApplicationActionPrototype();

	/**@return The current context for this session, or <code>null</code> if there currently is no context.*/
	public GuiseContext getContext();

	/**Sets the current context.
	This method should not normally be called by application code.
	@param context The current context for this session, or <code>null</code> if there currently is no context.
	*/
	public void setContext(final GuiseContext context);

	/**Queues a postponed event to be fired after the context has finished processing events.
	If a Guise context is currently processing events, the event will be queued for later.
	If no Guise context is currently processing events, the event will be fired immediately.
	@param postponedEvent The event to fire at a later time.
	@see GuiseContext.State#PROCESS_EVENT
	*/
	public void queueEvent(final PostponedEvent<?> postponedEvent);

	/**Retrieves the component bound to the given destination.
	If a component has already been created and cached, it will be be returned; otherwise, one will be created and cached. 
	@param destination The destination for which a component should be returned.
	@return The component bound to the given destination.
	@exception NullPointerException if the destination is <code>null</code>.
	@exception IllegalStateException if the component class bound to the destination does not provide appropriate constructors, is an interface, is abstract, or throws an exception during instantiation.
	*/
	public Component<?> getDestinationComponent(final ComponentDestination destination);

	/**Releases the component bound to the given destination.
	@param destination The destination for which any bound component should be released.
	@return The component previously bound to the given destination, or <code>null</code> if no component was bound to the given destination.
	@exception NullPointerException if the destination is <code>null</code>.
	*/
	public Component<?> releaseDestinationComponent(final ComponentDestination destination);

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
	public Component<?> getNavigationComponent(final String path);

	/**@return Whether the session is in a modal navigation state.*/
	public boolean isModalNavigation();

	/**@return The current modal navigation state, or <code>null</code> if there are no modal navigations.*/
	public ModalNavigation getModalNavigation();

	/**Begins modal interaction for a particular modal panel.
	The modal navigation is pushed onto the stack, and an event is fired to the modal listener of the modal navigation.
	@param modalNavigationPanel The panel for which modal navigation state should begin.
	@param modalNavigation The state of modal navigation.
	*/
	public void beginModalNavigation(final ModalNavigationPanel<?, ?> modalNavigationPanel, final ModalNavigation modalNavigation);

	/**Ends modal interaction for a particular modal panel.
	The panel is released from the cache so that new navigation will create a new modal panel.
	This method is called by modal panels and should seldom if ever be called directly.
	If the current modal state corresponds to the current navigation state, the current modal state is removed, the modal state's event is fired, and modal state is handed to the previous modal state, if any.
	Otherwise, navigation is transferred to the modal panel's referring URI, if any.
	If the given modal panel is not the panel at the current navigation path, the modal state is not changed, although navigation and releasal will still occur.
	@param modalNavigationPanel The panel for which modal navigation state should be ended.
	@return true if modality actually ended for the given panel.
	@see Frame#getReferrerURI()
	@see #releaseDestinationComponent(String)
	*/
	public boolean endModalNavigation(final ModalNavigationPanel<?, ?> modalNavigationPanel);

	/**Reports the navigation path relative to the application context path.
	@return The path representing the current navigation location of the Guise application.
	@exception IllegalStateException if this message has been called before the navigation path has been initialized.
	*/
	public String getNavigationPath();

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
	public void setNavigationPath(final String navigationPath);

	/**Reports the current bookmark relative to the current navigation path.
	@return The bookmark relative to the current navigation path, or <code>null</code> if there is no bookmark specified.
	*/
	public Bookmark getBookmark();

	/**Changes the bookmark of the current navigation path.
	This method does not necessarily cause navigation to occur, but instead "publishes" the bookmark to indicate that it is representative of the current state of the current navigation.
	@param bookmark The bookmark relative to the current navigation path, or <code>null</code> if there should be no bookmark.
	*/
	public void setBookmark(final Bookmark bookmark);

	/**Sets the new navigation path and bookmark, firing a navigation event if appropriate.
	If the navigation path and/or bookmark has changed, this method fires an event to all {@link NavigationListener}s in the component hierarchy, with the session as the source of the {@link NavigationEvent}.
	This method calls {@link #setNavigationPath(String)} and {@link #setBookmark(Bookmark)}.  
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	@param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@exception NullPointerException if the given navigation path is <code>null</code>.
	@see #setNavigationPath(String)
	@see #setBookmark(Bookmark)
	@see #getApplicationFrame()
	*/
	public void setNavigation(final String navigationPath, final Bookmark bookmark, final URI referrerURI);

	/**Fires a {@link NavigationEvent} to all {@link NavigationListener}s in the session application frame hierarchy.
	@param referrerURI The URI of the referring navigation panel or other entity with no query or fragment, or <code>null</code> if no referring URI is known.
	@see #getNavigationPath()
	@see #getBookmark()
	@see #getApplicationFrame()
	@see NavigationListener
	@see NavigationEvent 
	*/
	public void fireNavigated(final URI referrerURI);

	/**@return The requested navigation, or <code>null</code> if no navigation has been requested.*/
	public Navigation getRequestedNavigation();

	/**Removes any requests for navigation.*/
	public void clearRequestedNavigation();

	/**Requests navigation to the specified path.
	The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
	Later requested navigation before navigation occurs will override this request.
	@param path A path that is either relative to the application context path or is absolute.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #navigate(URI)} should be used instead).
	@see #navigate(URI)
	*/
	public void navigate(final String path);

	/**Requests navigation to the specified path in an identified viewport.
	The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
	Later requested navigation before navigation occurs will override this request.
	@param path A path that is either relative to the application context path or is absolute.
	@param viewportID The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #navigate(URI)} should be used instead).
	@see #navigate(URI, String)
	*/
	public void navigate(final String path, final String viewportID);

	/**Requests navigation to the specified path and bookmark.
	The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
	Later requested navigation before navigation occurs will override this request.
	@param path A path that is either relative to the application context path or is absolute.
	@param bookmark The bookmark at the given path, or <code>null</code> if no bookmark should be included in the navigation.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #navigate(URI)} should be used instead).
	@see #navigate(URI)
	*/
	public void navigate(final String path, final Bookmark bookmark);

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
	public void navigate(final String path, final Bookmark bookmark, final String viewportID);

	/**Requests navigation to the specified URI.
	The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
	Later requested navigation before navigation occurs will override this request.
	@param uri Either a relative or absolute path, or an absolute URI.
	@exception NullPointerException if the given URI is <code>null</code>.
	*/
	public void navigate(final URI uri);

	/**Requests navigation to the specified URI in an identified viewport.
	The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
	Later requested navigation before navigation occurs will override this request.
	@param uri Either a relative or absolute path, or an absolute URI.
	@param viewportID The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.
	@exception NullPointerException if the given URI is <code>null</code>.
	*/
	public void navigate(final URI uri, final String viewportID);

	/**Requests modal navigation to the specified path.
	The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
	Later requested navigation before navigation occurs will override this request.
	@param path A path that is either relative to the application context path or is absolute.
	@param modalListener The listener to respond to the end of modal interaction.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #navigate(URI)} should be used instead).
	@see #navigateModal(URI, ModalNavigationListener)
	*/
	public void navigateModal(final String path, final ModalNavigationListener modalListener);

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
	public void navigateModal(final String path, final Bookmark bookmark, final ModalNavigationListener modalListener);

	/**Requests modal navigation to the specified URI.
	The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
	Later requested navigation before navigation occurs will override this request.
	@param uri Either a relative or absolute path, or an absolute URI.
	@param modalListener The listener to respond to the end of modal interaction.
	@exception NullPointerException if the given URI is <code>null</code>.
	*/
	public void navigateModal(final URI uri, final ModalNavigationListener modalListener);

	/**Generates a new ID string unique to this session.
	This ID is appropriate for being used in a new component, for example.
	The ID will begin with a letter and be composed only of letters and numbers.
	@return A new ID unique to this session.
	*/
	public String generateID();

	/**Called when the session is initialized.
	@exception IllegalStateException if the session is already initialized.
	@see #destroy()
	*/
	public void initialize();

	/**Called when the session is destroyed.
	@exception IllegalStateException if the session has not yet been initialized or has already been destroyed.
	@see #initialize()
	*/
	public void destroy();

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
	public String createTempPublicResource(final String baseName, final String extension) throws IOException;

	/**Creates a component to indicate Guise busy status.
	@return A component to indicate Guise busy status.
	@see Theme#GLYPH_BUSY
	*/
	public Component<?> createBusyComponent();	//TODO maybe put this in GuiseApplication

	/**Logs the given session-related information with a default log level of {@link InformationLevel#LOG}.
	This is a convenience method that delegates to {@link #log(InformationLevel, String, String, String, Map, CharSequence)}.
	@param subject The log subject identification, or <code>null</code> if there is no related subject.
	@param predicate The log predicate identification, or <code>null</code> if there is no related predicate.
	@param object The log object identification, or <code>null</code> if there is no related object.
	@param parameters The map of log parameters, or <code>null</code> if there are no parameters.
	@param comment The log comment, or <code>null</code> if there is no log comment.
	@exception NullPointerException if the given log level is <code>null</code>.
	*/
	public void log(final String subject, final String predicate, final String object, final Map<?, ?> parameters, final CharSequence comment);

	/**Logs the given session-related information.
	@param level The log information level.
	@param subject The log subject identification, or <code>null</code> if there is no related subject.
	@param predicate The log predicate identification, or <code>null</code> if there is no related predicate.
	@param object The log object identification, or <code>null</code> if there is no related object.
	@param parameters The map of log parameters, or <code>null</code> if there are no parameters.
	@param comment The log comment, or <code>null</code> if there is no log comment.
	@exception NullPointerException if the given log level is <code>null</code>.
	*/
	public void log(final InformationLevel level, final String subject, final String predicate, final String object, final Map<?, ?> parameters, final CharSequence comment);

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
	public void notify(final Notification... notifications);

	/**Notifies the user of one or more notifications to be presented in sequence, with optional logic to be executed after all notifications have taken place.
	The notification's label and/or icon, if specified, will be used as the dialog title and icon, respectively;
	if either is not specified, a label and/or icon based upon the notification's severity will be used.
	If the selected option to any notification is fatal, the remaining notifications and the specified logic, if any, will not be performed.
	The absence of an option selection is considered fatal only if a fatal option was presented for a given notification.  
	@param notifications One or more notification informations to relay.
	@param afterNotify The code that executes after notification has taken place, or <code>null</code> if no action should be taken after notification.
	@exception NullPointerException if the given notifications is <code>null</code>.
	@exception IllegalArgumentException if no notifications are given.
	*/
	public void notify(final Runnable afterNotify, final Notification... notifications);

	/**Notifies the user of the given errors in sequence.
	If the selected option to any notification is fatal, the remaining notifications will not be performed.
	The absence of an option selection is considered fatal only if a fatal option was presented for a given notification.  
	This is a convenience method that delegates to {@link #notify(Runnable, Throwable...)}.
	@param errors The errors with which to notify the user.
	@exception NullPointerException if the given errors is <code>null</code>.
	@exception IllegalArgumentException if no errors are given.
	*/
	public void notify(final Throwable... errors);

	/**Notifies the user of the given error in sequence, with optional logic to be executed after notification takes place.
	If the selected option to any notification is fatal, the remaining notifications and the specified logic, if any, will not be performed.
	The absence of an option selection is considered fatal only if a fatal option was presented for a given notification.  
	This is a convenience method that delegates to {@link #notify(Runnable, Notification...)}.
	@param error The error with which to notify the user.
	@param afterNotify The code that executes after notification has taken place, or <code>null</code> if no action should be taken after notification.
	@exception NullPointerException if the given errors is <code>null</code>.
	@exception IllegalArgumentException if no errors are given.
	*/
	public void notify(final Runnable afterNotify, final Throwable... errors);

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
	public String resolveString(final String string) throws MissingResourceException;

	/**Resolves a URI against the application base path, looking up the URI from the resources if necessary.
	If the URI has the "resource" scheme, its scheme-specific part will be used to look up the actual URI using {@link #getURIResource(String)}.
	If suffixes are given, they will be appended to the resource key in order, separated by '.'.
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
	public URI resolveURI(final URI uri, final String... suffixes) throws MissingResourceException;
}
