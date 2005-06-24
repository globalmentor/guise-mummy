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

	/**Binds a frame type to a particular context-relative path.
	Any existing binding for the given context-relative path is replaced.
	@param path The context-relative path to which the frame should be bound.
	@param frameClass The class of frame to render for this particular context-relative path.
	@return The frame previously bound to the given context-relative path, or <code>null</code> if no frame was previously bound to the path.
	@exception NullPointerException if the path and/or the frame is null.
	*/
	public Class<? extends Frame> bindFrame(final String path, final Class<? extends Frame> frameClass);

	/**Determines the class of frame bound to the given context-relative path.
	@param path The address for which a frame should be retrieved.
	@return The type of frame bound to the given address. 
	*/
	public Class<? extends Frame> getBoundFrameClass(final String path);

}
