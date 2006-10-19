package com.guiseframework;

import java.io.*;
import java.net.URI;
import java.util.*;

import com.garretwilson.beans.PropertyBindable;
import com.guiseframework.component.*;
import com.guiseframework.component.kit.ComponentKit;
import com.guiseframework.context.GuiseContext;
import com.guiseframework.controller.*;
import com.guiseframework.theme.Theme;
import com.guiseframework.view.View;

import static com.garretwilson.lang.ClassUtilities.*;

/**An application running Guise.
@author Garret Wilson
*/
public interface GuiseApplication extends PropertyBindable
{

	/**The default locale bound property.*/
//TODO del	public final static String DEFAULT_LOCALE_PROPERTY=getPropertyName(GuiseApplication.class, "defaultLocale");
	/**The locales bound property.*/
	public final static String LOCALES_PROPERTY=getPropertyName(GuiseApplication.class, "locales");
	/**The resource bundle base name bound property.*/
	public final static String RESOURCE_BUNDLE_BASE_NAME_PROPERTY=getPropertyName(GuiseApplication.class, "resourceBundleBaseName");
	/**The style bound property.*/
	public final static String STYLE_PROPERTY=getPropertyName(GuiseApplication.class, "style");
	/**The theme bound property.*/
	public final static String THEME_PROPERTY=getPropertyName(GuiseApplication.class, "theme");

	/**@return The application locale used by default if a new session cannot determine the users's preferred locale.*/
//TODO del	public Locale getDefaultLocale();

	/**Sets the application locale used by default if a new session cannot determine the users's preferred locale.
	This is a bound property.
	@param newDefaultLocale The new default application locale.
	@see #DEFAULT_LOCALE_PROPERTY
	*/
//TODO del	public void setDefaultLocale(final Locale newDefaultLocale);

	/**@return The thread-safe set of locales supported by this application.*/
//TODO del	public Set<Locale> getSupportedLocales();

	/**@return The read-only non-empty list of locales supported by the application, with the first locale the default used if a new session cannot determine the users's preferred locale.*/
	public List<Locale> getLocales();

	/**Sets the list of supported locales.
	This is a bound property.
	@param newLocales The new supported application locales.
	@exception NullPointerException if the given list of locales is <code>null</code>.
	@exception IllegalArgumentException if the given list of locales is empty.
	@see #LOCALES_PROPERTY
	*/
	public void setLocales(final List<Locale> newLocales);

	/**@return The base name of the resource bundle to use for this application, or <code>null</code> if no custom resource bundle is specified for this application..*/
	public String getResourceBundleBaseName();

	/**Changes the resource bundle base name.
	This is a bound property.
	@param newResourceBundleBaseName The new base name of the resource bundle, or <code>null</code> if no custom resource bundle is specified for this application.
	@see #RESOURCE_BUNDLE_BASE_NAME_PROPERTY
	*/
	public void setResourceBundleBaseName(final String newResourceBundleBaseName);

	/**@return The absolute or application-relative URI of the application style, or <code>null</code> if the default style should be used.*/
	public URI getStyle();

	/**Sets the URI of the style of the application.
	This is a bound property.
	@param newStyle The URI of the application style, or <code>null</code> if the default style should be used.
	@see #STYLE_PROPERTY
	*/
	public void setStyle(final URI newStyle);

	/**@return The application theme.*/
	public Theme getTheme();

	/**Sets the theme of the application.
	This is a bound property.
	@param newTheme The new application theme.
	@see #THEME_PROPERTY
	*/
	public void setTheme(final Theme newTheme);

	/**Installs a component kit.
	Later component kits take precedence over earlier-installed component kits.
	If the component kit is already installed, no action occurs.
	@param componentKit The component kit to install.
	*/
	public void installComponentKit(final ComponentKit componentKit);

	/**Uninstalls a component kit.
	If the component kit is not installed, no action occurs.
	@param componentKit The component kit to uninstall.
	*/
	public void uninstallComponentKit(final ComponentKit componentKit);

	/**@return The identifier for logging to a Data Collection System such as WebTrends, or <code>null</code> if no DCS ID is known.*/
	public String getDCSID();

	/**Sets the Data Collection Server log identifier.
	@param dcsID The identifier for logging to a Data Collection System such as WebTrends, or <code>null</code> if no DCS ID is known.
	*/
	public void setDCSID(final String dcsID);

	/**Determines the controller appropriate for the given component.
	A controller class is located by individually looking up the component class hiearchy for registered render strategies, at each checking all installed component kits.
	@param <GC> The type of Guise context being used.
	@param <C> The type of component for which a controller is requested.
	@param component The component for which a controller should be returned.
	@return A controller to render the given component, or <code>null</code> if no controller is registered.
	*/
	public <C extends Component<?>> Controller<? extends GuiseContext, ? super C> getController(final C component);

	/**Determines the view appropriate for the given component.
	A view class is located by individually looking up the component class hiearchy for registered render strategies, at each checking all installed component kits.
	@param <GC> The type of Guise context being used.
	@param <C> The type of component for which a view is requested.
	@param component The component for which a view should be returned.
	@return A view to render the given component, or <code>null</code> if no view is registered.
	*/
	public <C extends Component<?>> View<? extends GuiseContext, ? super C> getView(final C component);

	/**Associates a destination with a particular application context-relative path.
	Any existing destination for the given context-relative path is replaced.
	@param path The appplication context-relative path to which the destination should be associated.
	@param destination The description of the destination at the appplication context-relative path.
	@return The destination previously assiciated with the given appplication context-relative path, or <code>null</code> if no destination was previously associated with the path.
	@exception NullPointerException if the path and/or the destination is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public Destination setDestination(final String path, final Destination destination);

	/**Associates multiple destinations with application context-relative paths.
	Any existing destinations for the given context-relative path are replaced.
	@param destinations The destinations to set.
	@exception IllegalArgumentException if a provided path is absolute.
	@see #setDestination(String, Destination)
	*/
	public void setDestinations(final List<Destination> destinations);

	/**Determines the destination associated with the given application context-relative path.
	@param path The address for which a destination should be retrieved.
	@return The destination associated with the given path, or <code>null</code> if no destination is associated with the path. 
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public Destination getDestination(final String path);

	/**Returns an iterable of destinations.
	Any changes to the iterable will not necessarily be reflected in the destinations available to the application.
	@return An iterable to the application's destinations.
	*/
	public Iterable<Destination> getDestinations();

	/**Determines if there is a destination associated with the given appplication context-relative path.
	@param path The appplication context-relative path.
	@return <code>true</code> if there is destination associated with the given path, or <code>false</code> if no destination is associated with the given path.
	@exception NullPointerException if the path is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public boolean hasDestination(final String path);

	/**@return The Guise container into which this application is installed, or <code>null</code> if the application is not yet installed.*/
	public GuiseContainer getContainer();

	/**Creates a new session for the application.
	@return A new session for the application
	*/
	public GuiseSession createSession();

	/**Creates a frame for the application.
	@return A new frame for the application.
	*/
	public ApplicationFrame<?> createApplicationFrame();

	/**Reports the base path of the application.
	The base path is an absolute path that ends with a slash ('/'), indicating the base path of the navigation panels.
	@return The base path representing the Guise application, or <code>null</code> if the application is not yet installed.
	*/
	public String getBasePath();

	/**@return Whether this application has been installed into a container at some base path.
	@see #getContainer()
	@see #getBasePath()
	*/
	public boolean isInstalled();

	/**Checks to ensure that this application is installed.
	@exception IllegalStateException if the application is not installed.
	@see #isInstalled()
	*/
	public void checkInstalled();

	/**Resolves a relative or absolute path against the application base path.
	Relative paths will be resolved relative to the application base path. Absolute paths will be be considered already resolved.
	For an application path "/path/to/application/", resolving "relative/path" will yield "/path/to/application/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path".
	@param path The path to be resolved.
	@return The path resolved against the application base path.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #resolveURI(URI)} should be used instead).
	@see #getBasePath()
	@see #resolveURI(URI)
	*/
	public String resolvePath(final String path);

	/**Resolves a URI against the application base path.
	Relative paths will be resolved relative to the application base path. Absolute paths will be considered already resolved, as will absolute URIs.
	For an application path "/path/to/application/", resolving "relative/path" will yield "/path/to/application/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path". Resolving "http://example.com/path" will yield "http://example.com/path".
	@param uri The URI to be resolved.
	@return The uri resolved against the application base path.
	@exception NullPointerException if the given URI is <code>null</code>.
	@see #getBasePath()
	@see #resolvePath(String)
	*/
	public URI resolveURI(final URI uri);

	/**Changes an absolute path to an application-relative path.
	For an application base path "/path/to/application/", relativizing "/path/to/application/relative/path" will yield "relative/path"
	@param path The path to be relativized.
	@return The path relativized to the application base path.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #resolveURI(URI)} should be used instead).
	@see #getBasePath()
	@see #relativizeURI(URI)
	*/
	public String relativizePath(final String path);

	/**Changes a URI to an application-relative path.
	For an application base path "/path/to/application/", relativizing "http://www.example.com/path/to/application/relative/path" will yield "relative/path"
	@param uri The URI to be relativized.
	@return The URI path relativized to the application base path.
	@exception NullPointerException if the given URI is <code>null</code>.
	@see #getBasePath()
	@see #relativizePath(String)
	*/
	public String relativizeURI(final URI uri);

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
	public String getLocaleResourcePath(final String resourceBasePath, final Locale locale);

	/**Determines if the application has a resource available stored at the given resource path.
	The provided path is first normalized.
	@param resourcePath An application-relative path to a resource in the application resource storage area.
	@return <code>true</code> if a resource exists at the given resource path.
	@exception IllegalArgumentException if the given resource path is absolute.
	@exception IllegalArgumentException if the given path is not a valid path.
	*/
	public boolean hasResource(final String resourcePath);

	/**Retrieves an input stream to the resource at the given path.
	The provided path is first normalized.
	@param resourcePath An application-relative path to a resource in the application resource storage area.
	@return An input stream to the resource at the given resource path, or <code>null</code> if no resource exists at the given resource path.
	@exception IllegalArgumentException if the given resource path is absolute.
	@exception IllegalArgumentException if the given path is not a valid path.
	*/
	public InputStream getResourceInputStream(final String resourcePath);

	/**Retrieves an input stream to the entity at the given URI.
	The URI is first resolved to the application base path.
	If this is a <code>resource:</code> URI representing a private resource, this method delegates to {@link #getResourceInputStream(String)}.
	@param uri A URI to the entity; either absolute or relative to the application.
	@return An input stream to the entity at the given resource URI, or <code>null</code> if no entity exists at the given resource path..
	@exception NullPointerException if the given URI is <code>null</code>.
	@exception IOException if there was an error connecting to the entity at the given URI.
	@see #resolveURI(URI)
	*/
	public InputStream getInputStream(final URI uri) throws IOException;

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
	public ResourceBundle getResourceBundle(final Locale locale);

}
