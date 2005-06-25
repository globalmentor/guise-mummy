package com.garretwilson.guise.application;

import java.util.*;
import static java.util.Collections.*;

import com.garretwilson.guise.component.Component;
import com.garretwilson.guise.component.Frame;
import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.guise.controller.*;
import com.garretwilson.util.Debug;

import static com.garretwilson.lang.ObjectUtilities.*;

/**An abstract base class for a Guise application.
@author Garret Wilson
*/
public abstract class AbstractGuiseApplication<GC extends GuiseContext>	implements GuiseApplication<GC>
{

	/**The synchronized list of installed controller kits, with later registrations taking precedence*/
	private final List<ControllerKit<GC>> controllerKitList=synchronizedList(new ArrayList<ControllerKit<GC>>());

	/**Installs a controller kit.
	Later controller kits take precedence over earlier-installed controller kits.
	If the controller kit is already installed, no action occurs.
	@param controllerKit The controller kit to install.
	*/
	public void installControllerKit(final ControllerKit<GC> controllerKit)
	{
		synchronized(controllerKitList)	//don't allow anyone to access the list of controller kits while we access it
		{
			if(!controllerKitList.contains(controllerKit))	//if the controller kit is not already installed
			{
				controllerKitList.add(0, controllerKit);	//add the controller kit to our list at the front of the list, giving it earlier priority
			}
		}
	}

	/**Uninstalls a controller kit.
	If the controller kit is not installed, no action occurs.
	@param controllerKit The controller kit to uninstall.
	*/
	public void uninstallControllerKit(final ControllerKit<GC> controllerKit)
	{
		controllerKitList.remove(controllerKit);	//remove the installed controller kit
	}

	/**Determines the controller class registered for the given component class.
	This request is delegated to each controller kit, with later-installed controller kits taking precedence. 
	@param context The Guise context interested in retrieving a controller. 
	@param componentClass The class of component that may be registered.
	@return A class of controller registered to render component of the specific class, or <code>null</code> if no controller is registered.
	*/
	protected <C extends Component> Class<? extends Controller<GC, C>> getRegisteredControllerClass(final Class<C> componentClass)
	{
		synchronized(controllerKitList)	//don't allow anyone to access the list of controller kits while we access it
		{
			for(final ControllerKit<GC> controllerKit:controllerKitList)	//for each controller kit in our list
			{
				final Class<? extends Controller<GC, C>> controllerKitClass=controllerKit.getRegisteredControllerClass(componentClass);	//ask the controller kit for a registered controller class for this component
				if(controllerKitClass!=null)	//if this controller kit gave us a controller class
				{
					return controllerKitClass;	//return the class
				}
			}
		}
		return null;	//indicate that none of our installed controller kits had a controller class registered for the specified component class
	}

	/**Determines the controller class appropriate for the given component class.
	A controller class is located by individually looking up the component class hiearchy for registered controllers.
	@param context The Guise context interested in retrieving a controller for the given component.
	@param componentClass The class of component for which a render strategy should be returned.
	@return A class of render strategy to render the given component class, or <code>null</code> if no render strategy is registered.
	*/
//TODO fix	@SuppressWarnings("unchecked")
	protected <C extends Component> Class<? extends Controller<GC, C>> getControllerClass(final Class<C> componentClass)
	{
		Class<? extends Controller> renderStrategyClass=getRegisteredControllerClass(componentClass);	//see if there is a controller class registered for this component type
		if(renderStrategyClass==null)	//if we didn't find a render strategy for this class, check the super class
		{
			final Class<?> superClass=componentClass.getSuperclass();	//get the super class of the component
			if(superClass!=null && Component.class.isAssignableFrom(superClass))	//if the super class is a component
			{
				renderStrategyClass=getControllerClass((Class<? extends C>)superClass);	//check the super class
			}
		}
		if(renderStrategyClass==null)	//if we still couldn't find a render strategy for this class, check the interfaces
		{
			for(final Class<?> classInterface:componentClass.getInterfaces())	//look at each implemented interface
			{
				if(Component.class.isAssignableFrom(classInterface))	//if the class interface is a component
				{
					renderStrategyClass=getControllerClass((Class<? extends C>)classInterface);	//check the interface
					if(renderStrategyClass!=null)	//if we found a render strategy class
					{
						break;	//stop looking at the interfaces
					}
				}					
			}
		}
		return renderStrategyClass;	//show which if any render strategy class we found
	}

	/**Determines the controller appropriate for the given component.
	A controller class is located by individually looking up the component class hiearchy for registered render strategies, at each checking all installed controller kits.
	@param component The component for which a controller should be returned.
	@return A controller to render the given component, or <code>null</code> if no controller is registered.
	*/
//TODO fix	@SuppressWarnings("unchecked")
	public <C extends Component> Controller<GC, C> getController(final C component)
	{
		Class<C> componentClass=(Class<C>)component.getClass();	//get the component class
		final Class<? extends Controller<GC, C>> renderStrategyClass=getControllerClass(componentClass);	//walk the hierarchy to see if there is a render strategy class registered for this component type
		if(renderStrategyClass!=null)	//if we found a render strategy class
		{
			try
			{
				return renderStrategyClass.newInstance();	//return a new instance of the class
			}
			catch (InstantiationException e)
			{
				Debug.error(e);
				throw new AssertionError(e);	//TODO fix
			}
			catch (IllegalAccessException e)
			{
				Debug.error(e);
				throw new AssertionError(e);	//TODO fix
			}
		}
		return null;	//show that we could not find a registered render strategy
	}

//TODO how do we keep the general public from changing the frame bindings?

	/**The synchronized map binding frame types to appplication context-relative absolute paths.*/
	private final Map<String, Class<? extends Frame>> pathFrameBindingMap=synchronizedMap(new HashMap<String, Class<? extends Frame>>());

		/**Binds a frame type to a particular appplication context-relative absolute path.
		Any existing binding for the given context-relative path is replaced.
		@param path The appplication context-relative absolute path to which the frame should be bound.
		@param frameClass The class of frame to render for this particular appplication context-relative absolute path.
		@return The frame previously bound to the given appplication context-relative absolute path, or <code>null</code> if no frame was previously bound to the path.
		@exception NullPointerException if the path and/or the frame is null.
		*/
		public Class<? extends Frame> bindFrame(final String path, final Class<? extends Frame> frameClass)
		{
			return pathFrameBindingMap.put(checkNull(path, "Path cannot be null."), checkNull(frameClass, "Type cannot be null."));	//store the binding
		}

		/**Determines the class of frame bound to the given appplication context-relative absolute path.
		@param path The address for which a frame should be retrieved.
		@return The type of frame bound to the given path, or <code>null</code> if no frame is bound to the path. 
		*/
		public Class<? extends Frame> getBoundFrameClass(final String path)
		{
			return pathFrameBindingMap.get(path);	//return the bound frame type, if any
		}
}
