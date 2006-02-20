package com.guiseframework;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.Principal;
import java.util.*;

import com.garretwilson.beans.PropertyBindable;
import com.garretwilson.event.PostponedEvent;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.Orientation;
import com.guiseframework.context.GuiseContext;
import com.guiseframework.event.ModalNavigationListener;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.text.CharacterEncodingConstants.UTF_8;

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

	/**@return The Guise application to which this session belongs.*/
	public GuiseApplication getApplication();

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
	public ResourceBundle getResourceBundle();

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

	/**Determines a string value either explicitly set or stored in the resources.
	If a value is explicitly specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@param value The value explicitly set, which will override any resource.
	@param resourceKey The key for looking up a resource if no value is explicitly set.
	@return The string value, or <code>null</code> if there is no value available, neither explicitly set nor in the resources.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getStringResource(String)
	*/
	public String determineString(final String value, final String resourceKey) throws MissingResourceException;

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
	@exception ClassCastException if the resource associated with the given key is not an instance of <code>URI</code>.
	@see #getResourceBundle()
	@see #getURIResource(String)
	*/
	public URI getURIResource(final String resourceKey, final URI defaultValue) throws MissingResourceException;

	/**Determines a URI value either explicitly set or stored in the resources.
	If a value is explicitly specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@param value The value explicitly set, which will override any resource.
	@param resourceKey The key for looking up a resource if no value is explicitly set.
	@return The URI value, or <code>null</code> if there is no value available, neither explicitly set nor in the resources.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getURIResource(String)
	*/
	public URI determineURI(final URI value, final String resourceKey) throws MissingResourceException;

	/**@return The current principal (e.g. logged-in user), or <code>null</code> if there is no principal authenticated for this session.*/
	public Principal getPrincipal();

	/**Sets the current principal (e.g. logged-in user).
	This is a bound property.
	@param newPrincipal The new principal, or <code>null</code> if there should be no associated principal (e.g. the user should be logged off).
	@see #PRINCIPAL_PROPERTY
	*/
	public void setPrincipal(final Principal newPrincipal);

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

	/**Retrieves the panel bound to the given appplication context-relative path.
	If a panel has already been created and cached, it will be be returned; otherwise, one will be created and cached. 
	The panel will be given an ID of a modified form of the path.
	@param path The appplication context-relative path within the Guise container context.
	@return The panel bound to the given path, or <code>null</code> if no panel is bound to the given path.
	@exception NullPointerException if the path is null.
	@exception IllegalArgumentException if the provided path is absolute.
	@exception IllegalStateException if the panel class bound to the path does not provide appropriate constructors, is an interface, is abstract, or throws an exception during instantiation.
	*/
	public NavigationPanel<?> getNavigationPanel(final String path);

	/**Releases the panel bound to the given appplication context-relative path.
	@param path The appplication context-relative path within the Guise container context.
	@return The panel previously bound to the given path, or <code>null</code> if no panel was bound to the given path.
	@exception NullPointerException if the path is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public NavigationPanel<?> releaseNavigationPanel(final String path);

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
	@see #releaseNavigationPanel(String)
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
	@exception IllegalArgumentException if the provided path is absolute.
	@exception IllegalArgumentException if the navigation path is not recognized (e.g. there is no panel bound to the navigation path).
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

	/**@return A new component ID appropriate for using with a new component.*/
	public String generateComponentID();

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

}
