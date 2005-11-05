package com.javaguise.session;

import java.net.URI;
import java.security.Principal;
import java.util.*;

import com.garretwilson.beans.PropertyBindable;
import com.garretwilson.event.PostponedEvent;
import com.javaguise.GuiseApplication;
import com.javaguise.GuiseEnvironment;
import com.javaguise.component.*;
import com.javaguise.component.layout.Orientation;
import com.javaguise.context.GuiseContext;
import com.javaguise.event.ModalEvent;
import com.javaguise.event.ModalNavigationListener;

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

	/**Begins modal interaction for a particular modal panel.
	The modal navigation is pushed onto the stack, and an event is fired to the modal listener of the modal navigation.
	@param <P> The type of navigation panel beginning navigation.
	@param modalNavigationPanel The panel for which modal navigation state should begin.
	@param modalNavigation The state of modal navigation.
	*/
	public <P extends ModalNavigationPanel<?, ?>> void beginModalNavigation(final P modalNavigationPanel, final ModalNavigation<P> modalNavigation);

	/**Ends modal interaction for a particular modal panel.
	The panel is released from the cache so that new navigation will create a new modal panel.
	This method is called by modal panels and should seldom if ever be called directly.
	If the current modal state corresponds to the current navigation state, the current modal state is removed, the modal state's event is fired, and modal state is handed to the previous modal state, if any.
	Otherwise, navigation is transferred to the modal panel's referring URI, if any.
	If the given modal panel is not the panel at the current navigation path, the modal state is not changed, although navigation and releasal will still occur.
	@param <P> The type of navigation panel ending navigation.
	@param modalNavigationPanel The panel for which modal navigation state should be ended.
	@return true if modality actually ended for the given panel.
	@see Frame#getReferrerURI()
	@see #releaseNavigationPanel(String)
	*/
	public <P extends ModalNavigationPanel<?, ?>> boolean endModalNavigation(final P modalNavigationPanel);

	/**Reports the navigation path relative to the application context path.
	@return The path representing the current navigation location of the Guise application.
	@exception IllegalStateException if this message has been called before the navigation path has been initialized.
	*/
	public String getNavigationPath();

	/**Requests navigation to the specified path.
	The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
	Later requested navigation before navigation occurs will override this request.
	@param path A path that is either relative to the application context path or is absolute.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #navigate(URI)} should be used instead).
	@see #navigate(URI)
	*/
	public void navigate(final String path);

	/**Requests navigation to the specified URI.
	The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
	Later requested navigation before navigation occurs will override this request.
	@param uri Either a relative or absolute path, or an absolute URI.
	@exception NullPointerException if the given URI is <code>null</code>.
	*/
	public void navigate(final URI uri);

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
	public <P extends ModalNavigationPanel<?, ?>> void navigateModal(final String path, final ModalNavigationListener<P> modalListener);

	/**Requests modal navigation to the specified URI.
	The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
	Later requested navigation before navigation occurs will override this request.
	@param <P> The type of navigation panel for modal navigation.
	@param uri Either a relative or absolute path, or an absolute URI.
	@param modalListener The listener to respond to the end of modal interaction.
	@exception NullPointerException if the given URI is <code>null</code>.
	*/
	public <P extends ModalNavigationPanel<?, ?>> void navigateModal(final URI uri, final ModalNavigationListener<P> modalListener);

	/**@return A new component ID appropriate for using with a new component.*/
	public String generateComponentID();

}
