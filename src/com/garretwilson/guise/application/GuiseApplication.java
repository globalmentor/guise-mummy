package com.garretwilson.guise.application;

import com.garretwilson.guise.component.Component;
import com.garretwilson.guise.component.Frame;
import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.guise.controller.Controller;
import com.garretwilson.guise.controller.ControllerKit;

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

	/**Binds a frame type to a particular appplication context-relative absolute path.
	Any existing binding for the given context-relative path is replaced.
	@param path The appplication context-relative absolute path to which the frame should be bound.
	@param frameClass The class of frame to render for this particular appplication context-relative absolute path.
	@return The frame previously bound to the given appplication context-relative absolute path, or <code>null</code> if no frame was previously bound to the path.
	@exception NullPointerException if the path and/or the frame is null.
	*/
	public Class<? extends Frame> bindFrame(final String path, final Class<? extends Frame> frameClass);

	/**Determines the class of frame bound to the given appplication context-relative absolute path.
	@param path The address for which a frame should be retrieved.
	@return The type of frame bound to the given path, or <code>null</code> if no frame is bound to the path. 
	*/
	public Class<? extends Frame> getBoundFrameClass(final String path);

	/**Reports the context path of the application.
	The context path is either the empty string (""), or a path beginning with a slash ('/') indicating the application's context relative to its frames.
	The context path does not end with a slash ('/').
	@return The path representing the context of the Guise application.
	*/
	public String getContextPath();

}
