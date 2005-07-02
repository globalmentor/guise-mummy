package com.garretwilson.guise.session;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.*;

import com.garretwilson.beans.PropertyBindable;
import com.garretwilson.event.PostponedEvent;
import com.garretwilson.guise.application.GuiseApplication;
import com.garretwilson.guise.component.NavigationFrame;
import com.garretwilson.guise.context.GuiseContext;
import static com.garretwilson.lang.ClassUtilities.*;

/**Represents a session with a user.
A client application may only have one session, while a web server application will likely have multiple sessions.
@author Garret Wilson
*/
public interface GuiseSession<GC extends GuiseContext<GC>> extends PropertyBindable
{

	/**The locale bound property.*/
	public final static String LOCALE_PROPERTY=getPropertyName(GuiseSession.class, "locale");

	/**@return The Guise application to which this session belongs.*/
	public GuiseApplication<GC> getApplication();

	/**@return The current session locale.*/
	public Locale getLocale();

	/**Sets the current session locale.
	This is a bound property.
	@param newLocale The new session locale.
	@exception NullPointerException if the given locale is <code>null</code>.
	@see #LOCALE_PROPERTY
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
	*/
	public ResourceBundle getResourceBundle();

	/**Retrieves a string resource from the resource bundle.
	This is a preferred convenience method for accessing the resources in the session's resource bundle.
	@param resourceKey The key of the resource to retrieve.
	@return The resource associated with the specified resource key.
	@exception NullPointerException if the provided resource key is <code>null</code>.
	@exception MissingResourceException if no resource could be found associated with the given key.
	@exception ClassCastException if the resource associated with the given key is not an instance of <code>String</code>.
	@see #getResourceBundle()
	@see #getStringResource(String, String)
	*/
	public String getStringResource(final String resourceKey);

	/**Retrieves a string resource from the resource bundle, using a specified default if no such resource is available.
	This is a preferred convenience method for accessing the resources in the session's resource bundle.
	@param resourceKey The key of the resource to retrieve.
	@param defaultValue The default value to use if there is no resource associated with the given key.
	@return The resource associated with the specified resource key or the default if none is available.
	@exception NullPointerException if the provided resource key is <code>null</code>.
	@exception ClassCastException if the resource associated with the given key is not an instance of <code>String</code>.
	@see #getResourceBundle()
	@see #getStringResource(String)
	*/
	public String getStringResource(final String resourceKey, final String defaultValue);

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
	public Boolean getBooleanResource(final String resourceKey);

	/**Retrieves a <code>Boolean</codeL resource from the resource bundle, using a specified default if no such resource is available.
	If the given resource is a string, it will be interpreted according to the {@link Boolean#valueOf(java.lang.String)} rules.
	This is a preferred convenience method for accessing the resources in the session's resource bundle.
	@param resourceKey The key of the resource to retrieve.
	@param defaultValue The default value to use if there is no resource associated with the given key.
	@return The resource associated with the specified resource key or the default if none is available.
	@exception NullPointerException if the provided resource key is <code>null</code>.
	@exception ClassCastException if the resource associated with the given key is not an instance of <code>Boolean</code>.
	@see #getResourceBundle()
	@see #getBooleanResource(String)
	*/
	public Boolean getBooleanResource(final String resourceKey, final Boolean defaultValue);

	/**@return The unmodifiable set of all states of available Guise contexts.*/
	public Set<GuiseContext.State> getContextStates();

	/**Queues a postponed model event to be fired after all contexts have finished updating the model.
	If a Guise context is currently updating the model, the event will be queued for later.
	If no Guise context is currently updating the model, the event will be fired immediately.
	@param postponedModelEvent The event to fire at a later time.
	@see GuiseContext.State#UPDATE_MODEL
	*/
	public void queueModelEvent(final PostponedEvent<?> postponedModelEvent);

	/**Retrieves the frame bound to the given appplication context-relateive path.
	If a frame has already been created and cached, it will be be returned; otherwise, one will be created and cached. 
	The frame will be given an ID of a modified form of the path.
	@param path The appplication context-relative path within the Guise container context.
	@return The frame bound to the given path, or <code>null</code> if no frame is bound to the given path
	@exception IllegalArgumentException if the provided path is absolute.
	@exception NoSuchMethodException if the frame bound to the path does not provide Guise session constructor; or a Guise session and ID string constructor.
	@exception IllegalAccessException if the bound frame enforces Java language access control and the underlying constructor is inaccessible.
	@exception InstantiationException if the bound frame is an abstract class.
	@exception InvocationTargetException if the bound frame's underlying constructor throws an exception.
	*/
	public NavigationFrame getBoundNavigationFrame(final String path) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException;

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

}
