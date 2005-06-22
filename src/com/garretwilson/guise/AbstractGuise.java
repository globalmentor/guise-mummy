package com.garretwilson.guise;

import java.util.*;

import com.garretwilson.guise.component.Component;
import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.guise.controller.RenderStrategy;

/**An abstract base class for a Guise instance.
@author Garret Wilson
*/
public abstract class AbstractGuise<GC extends GuiseContext> implements Guise<GC>
{

	/**The map of render strategies for component types.*/
	private final Map<Class<? extends Component>, Class<? extends RenderStrategy>> renderStrategyMap=new HashMap<Class<? extends Component>, Class<? extends RenderStrategy>>(); 

	/**Registers a render strategy to render a component of the given class (and by default subclasses).
	@param componentClass The class of the component for which the render strategy should be registered.
	@param renderStrategyClass The class of render strategy to use for rendering the components. 
	@return The render strategy class previously registered with the given component class, or <code>null</code> if there was no previous registration.
	*/
	@SuppressWarnings("unchecked")
	public <C extends Component> Class<? extends RenderStrategy<GC, C>> registerRenderStrategy(Class<C> componentClass, Class<? extends RenderStrategy> renderStrategyClass)
	{
		return (Class<? extends RenderStrategy<GC, C>>)renderStrategyMap.put(componentClass, renderStrategyClass);	//register the render strategy and return the old registration, if any
//TODO fix		return (Class<? extends RenderStrategy<? super GC, ? extends C>>)renderStrategyMap.put(componentClass, renderStrategyClass);	//register the render strategy and return the old registration, if any
	}

	/**Determines the render strategy class registered for the given component class.
	@param componentClass The class of component that may be registered.
	@return A class of render strategy registered to render component of the specific class, or <code>null</code> if no render strategy is registered.
	*/
	@SuppressWarnings("unchecked")
	public <C extends Component> Class<? extends RenderStrategy<GC, C>> getRegisteredRenderStrategyClass(final Class<C> componentClass)
	{
		return (Class<? extends RenderStrategy<GC, C>>)renderStrategyMap.get(componentClass);	//return any registration; we know that the render strategy is for the correct type, because that's the only type we'll allow to be registered
//TODO fix		return (Class<? extends RenderStrategy<? super GC, ? extends C>>)renderStrategyMap.get(componentClass);	//return any registration; we know that the render strategy is for the correct type, because that's the only type we'll allow to be registered
	}

}
