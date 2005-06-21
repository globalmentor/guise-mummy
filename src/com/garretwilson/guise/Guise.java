package com.garretwilson.guise;

import com.garretwilson.guise.component.Component;
import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.guise.render.RenderStrategy;

/**An interface for a Guise instance.
@author Garret Wilson
*/
public interface Guise<GC extends GuiseContext>
{

	/**Registers a render strategy to render a component of the given class (and by default subclasses).
	@param componentClass The class of the component for which the render strategy should be registered.
	@param renderStrategyClass The class of render strategy to use for rendering the components. 
	@return The render strategy class previously registered with the given component class, or <code>null</code> if there was no previous registration.
	*/
	public <C extends Component> Class<? extends RenderStrategy<GC, C>> registerRenderStrategy(Class<C> componentClass, Class<? extends RenderStrategy> renderStrategyClass);
//TODO fix; seems correct	public <C extends Component> Class<? extends RenderStrategy<? super GC, ? extends C>> registerRenderStrategy(Class<C> componentClass, Class<? extends RenderStrategy> renderStrategyClass);
//TODO del; works	public void registerRenderStrategy(Class<? extends RenderStrategy> renderStrategyClass);
//TODO fix	public <C extends Component> Class<? extends RenderStrategy<? super GC, ? extends C>> registerRenderStrategy(Class<C> componentClass, Class<? extends RenderStrategy<? super GC, ? extends C>> renderStrategyClass);

	/**Determines the render strategy class registered for the given component class.
	@param componentClass The class of component that may be registered.
	@return A class of render strategy registered to render component of the specific class, or <code>null</code> if no render strategy is registered.
	*/
	public <C extends Component> Class<? extends RenderStrategy<GC, C>> getRegisteredRenderStrategyClass(final Class<C> componentClass);
//TODO fix	public <C extends Component> Class<? extends RenderStrategy<? super GC, ? extends C>> getRegisteredRenderStrategyClass(final Class<C> componentClass);

}
