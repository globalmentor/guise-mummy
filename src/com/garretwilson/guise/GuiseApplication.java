package com.garretwilson.guise;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

import com.garretwilson.beans.PropertyBindable;
import com.garretwilson.guise.component.*;
import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.guise.controller.*;

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
	public <GC extends GuiseContext<?>, C extends Component<?>> Controller<? super GC, ? super C> getController(final GC context, final C component);

	/**Binds a frame type to a particular application context-relative path.
	Any existing binding for the given context-relative path is replaced.
	@param path The appplication context-relative path to which the frame should be bound.
	@param navigationFrameClass The class of frame to render for this particular appplication context-relative path.
	@return The frame previously bound to the given appplication context-relative path, or <code>null</code> if no frame was previously bound to the path.
	@exception NullPointerException if the path and/or the frame is null.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public Class<? extends NavigationFrame> bindNavigationFrame(final String path, final Class<? extends NavigationFrame> navigationFrameClass);

	/**Determines the class of frame bound to the given application context-relative path.
	@param path The address for which a frame should be retrieved.
	@return The type of frame bound to the given path, or <code>null</code> if no frame is bound to the path. 
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public Class<? extends NavigationFrame> getBoundNavigationFrameClass(final String path);

	/**@return The Guise container into which this application is installed, or <code>null</code> if the application is not yet installed.*/
	public GuiseContainer getContainer();

	/**Reports the context path of the application.
	The context path is an absolute path that ends with a slash ('/'), indicating the application's context relative to its navigation frames.
	@return The path representing the context of the Guise application, or <code>null</code> if the application is not yet installed.
	*/
	public String getContextPath();

	/**Resolves a relative or absolute path against the application context path.
	Relative paths will be resolved relative to the application context path. Absolute paths will be be considered already resolved.
	For an application path "/path/to/application/", resolving "relative/path" will yield "/path/to/application/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path".
	@param path The path to be resolved.
	@return The path resolved against the application context path.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #resolveURI(URI)} should be used instead).
	@see #resolveURI(URI)
	*/
	public String resolvePath(final String path);

	/**Resolves URI against the application context path.
	Relative paths will be resolved relative to the application context path. Absolute paths will be considered already resolved, as will absolute URIs.
	For an application path "/path/to/application/", resolving "relative/path" will yield "/path/to/application/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path". Resolving "http://example.com/path" will yield "http://example.com/path".
	@param uri The URI to be resolved.
	@return The uri resolved against the application context path.
	@exception NullPointerException if the given URI is <code>null</code>.
	@see #getContextPath()
	*/
	public URI resolveURI(final URI uri);

}
