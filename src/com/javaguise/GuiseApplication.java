package com.javaguise;

import java.io.InputStream;
import java.net.URI;
import java.util.Locale;
import java.util.Set;

import com.garretwilson.beans.PropertyBindable;
import com.garretwilson.net.URIUtilities;
import com.javaguise.component.*;
import com.javaguise.context.GuiseContext;
import com.javaguise.controller.*;

import static com.garretwilson.lang.ClassUtilities.*;

/**An application running Guise.
@author Garret Wilson
*/
public interface GuiseApplication extends PropertyBindable
{

	/**The locale bound property.*/
	public final static String DEFAULT_LOCALE_PROPERTY=getPropertyName(GuiseApplication.class, "defaultLocale");
	/**The resource bundle base name bound property.*/
	public final static String RESOURCE_BUNDLE_BASE_NAME_PROPERTY=getPropertyName(GuiseApplication.class, "resourceBundleBaseName");

	/**@return The application locale used by default if a new session cannot determine the users's preferred locale.*/
	public Locale getDefaultLocale();

	/**Sets the application locale used by default if a new session cannot determine the users's preferred locale.
	This is a bound property.
	@param newDefaultLocale The new default application locale.
	@see #DEFAULT_LOCALE_PROPERTY
	*/
	public void setDefaultLocale(final Locale newDefaultLocale);

	/**@return The thread-safe set of locales supported by this application.*/
	public Set<Locale> getSupportedLocales();

	/**@return The base name of the resource bundle to use for this application.*/
	public String getResourceBundleBaseName();

	/**Changes the resource bundle base name.
	This is a bound property.
	@param newResourceBundleBaseName The new base name of the resource bundle.
	@see #RESOURCE_BUNDLE_BASE_NAME_PROPERTY
	*/
	public void setResourceBundleBaseName(final String newResourceBundleBaseName);

	/**Installs a controller kit.
	Later controller kits take precedence over earlier-installed controller kits.
	If the controller kit is already installed, no action occurs.
	@param controllerKit The controller kit to install.
	*/
	public void installControllerKit(final ControllerKit controllerKit);

	/**Uninstalls a controller kit.
	If the controller kit is not installed, no action occurs.
	@param controllerKit The controller kit to uninstall.
	*/
	public void uninstallControllerKit(final ControllerKit controllerKit);

	/**Determines the controller appropriate for the given component.
	A controller class is located by individually looking up the component class hiearchy for registered render strategies, at each checking all installed controller kits.
	@param <GC> The type of Guise context being used.
	@param <C> The type of component for which a controller is requested.
	@param context Guise context information.
	@param component The component for which a controller should be returned.
	@return A controller to render the given component, or <code>null</code> if no controller is registered.
	*/
//TODO del when works	public <GC extends GuiseContext, C extends Component<?>> Controller<? super GC, ? super C> getController(final GC context, final C component);
	public <C extends Component<?>> Controller<? extends GuiseContext, ? super C> getController(final C component);

	/**Binds a frame type to a particular application context-relative path.
	Any existing binding for the given context-relative path is replaced.
	@param path The appplication context-relative path to which the frame should be bound.
	@param frameClass The class of frame to render for this particular appplication context-relative path.
	@return The frame previously bound to the given appplication context-relative path, or <code>null</code> if no frame was previously bound to the path.
	@exception NullPointerException if the path and/or the frame is null.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public Class<? extends Frame> bindNavigationFrame(final String path, final Class<? extends Frame> frameClass);

	/**Determines the class of frame bound to the given application context-relative path.
	@param path The address for which a frame should be retrieved.
	@return The type of frame bound to the given path, or <code>null</code> if no frame is bound to the path. 
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public Class<? extends Frame> getNavigationFrameClass(final String path);

	/**Determines if there is a frame class bound to the given appplication context-relative path.
	@param path The appplication context-relative path within the Guise container context.
	@return <code>true</code> if there is a frame bound to the given path, or <code>false</code> if no frame is bound to the given path.
	@exception NullPointerException if the path is <code>null</code>.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public boolean hasNavigationPath(final String path);

	/**@return The Guise container into which this application is installed, or <code>null</code> if the application is not yet installed.*/
	public GuiseContainer getContainer();

	/**Reports the base path of the application.
	The base path is an absolute path that ends with a slash ('/'), indicating the base path of the navigation frames.
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

	/**Resolves URI against the application base path.
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

	/**Retrieves and input stream to the resource at the given path.
	The provided path is first normalized.
	@param resourcePath An application-relative path to a resource in the application resource storage area.
	@return An input stream to the resource at the given resource path, or <code>null</code> if no resource exists at the given resource path.
	@exception IllegalArgumentException if the given resource path is absolute.
	@exception IllegalArgumentException if the given path is not a valid path.
	*/
	public InputStream getResourceAsStream(final String resourcePath);

}
