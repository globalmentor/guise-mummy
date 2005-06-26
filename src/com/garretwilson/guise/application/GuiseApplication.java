package com.garretwilson.guise.application;

import java.net.URI;

import com.garretwilson.guise.component.*;
import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.guise.controller.*;

/**An application running Guise.
@author Garret Wilson
*/
public interface GuiseApplication<GC extends GuiseContext>
{

	/**Installs a controller kit.
	Later controller kits take precedence over earlier-installed controller kits.
	If the controller kit is already installed, no action occurs.
	@param controllerKit The controller kit to install.
	*/
	public void installControllerKit(final ControllerKit<GC> controllerKit);

	/**Uninstalls a controller kit.
	If the controller kit is not installed, no action occurs.
	@param controllerKit The controller kit to uninstall.
	*/
	public void uninstallControllerKit(final ControllerKit<GC> controllerKit);

	/**Determines the controller appropriate for the given component.
	A controller class is located by individually looking up the component class hiearchy for registered render strategies, at each checking all installed controller kits.
	@param component The component for which a controller should be returned.
	@return A controller to render the given component, or <code>null</code> if no controller is registered.
	*/
	public <C extends Component> Controller<GC, C> getController(final C component);

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

	/**Reports the context path of the application.
	The context path is an absolute path that ends with a slash ('/'), indicating the application's context relative to its navigation frames.
	@return The path representing the context of the Guise application.
	*/
	public String getContextPath();

	/**Resolves a relative or absolute path against the application context path.
	Relative paths will be resolved relative to the application context path. Absolute paths will be be considered already resolved.
	For an application path "/path/to/application/", resolving "relative/path" will yield "/path/to/application/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path".
	@param path The path to be resolved.
	@return The path resolved against the application context path.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case <code>resolveURI()</code> should be used instead).
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
