package com.garretwilson.guise;

import java.util.*;

import com.garretwilson.guise.component.Component;
import com.garretwilson.guise.component.Frame;
import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.guise.controller.Controller;
import com.garretwilson.guise.session.GuiseSession;
import static com.garretwilson.lang.ObjectUtilities.*;

/**An abstract base class for a Guise instance.
@author Garret Wilson
*/
public abstract class AbstractGuise<GC extends GuiseContext>	implements Guise<GC>
{

//TODO how do we keep the general public from changing the frame bindings?

	/**The map binding frame types to context-relative paths.*/
	private final Map<String, Class<? extends Frame>> pathFrameBindingMap=new HashMap<String, Class<? extends Frame>>();

		/**Binds a frame type to a particular context-relative path.
		Any existing binding for the given context-relative path is replaced.
		@param path The context-relative path to which the frame should be bound.
		@param frameClass The class of frame to render for this particular context-relative path.
		@return The frame previously bound to the given context-relative path, or <code>null</code> if no frame was previously bound to the path.
		@exception NullPointerException if the path and/or the frame is null.
		*/
		public Class<? extends Frame> bindFrame(final String path, final Class<? extends Frame> frameClass)
		{
			return pathFrameBindingMap.put(checkNull(path, "Path cannot be null."), checkNull(frameClass, "Type cannot be null."));	//store the binding
		}

		/**Determines the class of frame bound to the given context-relative path.
		@param path The address for which a frame should be retrieved.
		@return The type of frame bound to the given address. 
		*/
		public Class<? extends Frame> getBoundFrameClass(final String path)
		{
			return pathFrameBindingMap.get(path);	//return the bound frame type, if any
		}

	/**The map of render strategies for component types.*/
	private final Map<Class<? extends Component>, Class<? extends Controller>> renderStrategyMap=new HashMap<Class<? extends Component>, Class<? extends Controller>>(); 

		/**Registers a controller to render a component of the given class (and by default subclasses).
		@param componentClass The class of the component for which the render strategy should be registered.
		@param renderStrategyClass The class of render strategy to use for rendering the components. 
		@return The render strategy class previously registered with the given component class, or <code>null</code> if there was no previous registration.
		*/
//TODO fix		@SuppressWarnings("unchecked")
		public <C extends Component> Class<? extends Controller<? extends GC, C>> registerRenderStrategy(final Class<C> componentClass, Class<? extends Controller> renderStrategyClass)
		{
			return renderStrategyMap.put(componentClass, renderStrategyClass);	//register the render strategy and return the old registration, if any
		}
	
		/**Determines the controller class registered for the given component class.
		@param context The Guise context interested in retrieving a controller. 
		@param componentClass The class of component that may be registered.
		@return A class of render strategy registered to render component of the specific class, or <code>null</code> if no render strategy is registered.
		*/
//TODO fix		@SuppressWarnings("unchecked")
		public <C extends Component> Class<? extends Controller<GC, C>> getRegisteredRenderStrategyClass(final Class<C> componentClass)
		{
			return renderStrategyMap.get(componentClass);	//return any registration; we know that the render strategy is for the correct type, because that's the only type we'll allow to be registered
		}
}
