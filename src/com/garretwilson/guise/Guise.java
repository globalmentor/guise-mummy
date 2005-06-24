package com.garretwilson.guise;

import com.garretwilson.guise.component.Component;
import com.garretwilson.guise.component.Frame;
import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.guise.controller.Controller;

/**An interface for a Guise instance.
@author Garret Wilson
*/
public interface Guise<GC extends GuiseContext>
{

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

	/**Registers a controller to render a component of the given class (and by default subclasses).
	@param componentClass The class of the component for which the render strategy should be registered.
	@param renderStrategyClass The class of render strategy to use for rendering the components. 
	@return The render strategy class previously registered with the given component class, or <code>null</code> if there was no previous registration.
	*/
	public <C extends Component> Class<? extends Controller<? extends GC, C>> registerRenderStrategy(final Class<C> componentClass, Class<? extends Controller> renderStrategyClass);
//TODO last to work	public <C extends Component> Class<? extends Controller<GC, C>> registerRenderStrategy(Class<C> componentClass, Class<? extends Controller> renderStrategyClass);
//TODO fix; seems correct	public <C extends Component> Class<? extends RenderStrategy<? super GC, ? extends C>> registerRenderStrategy(Class<C> componentClass, Class<? extends RenderStrategy> renderStrategyClass);
//TODO del; works	public void registerRenderStrategy(Class<? extends RenderStrategy> renderStrategyClass);
//TODO fix	public <C extends Component> Class<? extends RenderStrategy<? super GC, ? extends C>> registerRenderStrategy(Class<C> componentClass, Class<? extends RenderStrategy<? super GC, ? extends C>> renderStrategyClass);

	/**Determines the controller class registered for the given component class.
	@param context The Guise context interested in retrieving a controller. 
	@param componentClass The class of component that may be registered.
	@return A class of render strategy registered to render component of the specific class, or <code>null</code> if no render strategy is registered.
	*/
	public <C extends Component> Class<? extends Controller<GC, C>> getRegisteredRenderStrategyClass(final Class<C> componentClass);
//TODO last to work	public <C extends Component> Class<? extends Controller<GC, C>> getRegisteredRenderStrategyClass(final Class<C> componentClass);
//TODO fix	public <C extends Component> Class<? extends RenderStrategy<? super GC, ? extends C>> getRegisteredRenderStrategyClass(final Class<C> componentClass);

}
