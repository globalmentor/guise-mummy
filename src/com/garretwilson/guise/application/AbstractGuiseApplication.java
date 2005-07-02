package com.garretwilson.guise.application;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.util.Collections.*;

import com.garretwilson.beans.BoundPropertyObject;
import static com.garretwilson.guise.GuiseResourceConstants.*;
import com.garretwilson.guise.component.Component;
import com.garretwilson.guise.component.NavigationFrame;
import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.guise.controller.*;
import com.garretwilson.lang.ObjectUtilities;

import static com.garretwilson.net.URIUtilities.*;
import com.garretwilson.util.Debug;

import static com.garretwilson.lang.ObjectUtilities.*;

/**An abstract base class for a Guise application.
@author Garret Wilson
*/
public abstract class AbstractGuiseApplication<GC extends GuiseContext>	extends BoundPropertyObject implements GuiseApplication<GC>
{

	/**The application locale used by default if a new session cannot determine the users's preferred locale.*/
	private Locale defaultLocale;

		/**@return The application locale used by default if a new session cannot determine the users's preferred locale.*/
		public Locale getDefaultLocale() {return defaultLocale;}

		/**Sets the application locale used by default if a new session cannot determine the users's preferred locale.
		This is a bound property.
		@param newDefaultLocale The new default application locale.
		@exception NullPointerException if the given locale is <code>null</code>.
		@see GuiseApplication#DEFAULT_LOCALE_PROPERTY
		*/
		public void setDefaultLocale(final Locale newDefaultLocale)
		{
			if(!ObjectUtilities.equals(defaultLocale, newDefaultLocale))	//if the value is really changing (compare their values, rather than identity)
			{
				final Locale oldLocale=defaultLocale;	//get the old value
				defaultLocale=checkNull(newDefaultLocale, "Guise application default locale cannot be null.");	//actually change the value
				firePropertyChange(DEFAULT_LOCALE_PROPERTY, oldLocale, newDefaultLocale);	//indicate that the value changed
			}
		}

	/**The thread-safe set of locales supported by this application.*/
	private final Set<Locale> supportedLocales=new CopyOnWriteArraySet<Locale>();

		/**@return The thread-safe set of locales supported by this application.*/
		public Set<Locale> getSupportedLocales() {return supportedLocales;}

	/**The base name of the resource bundle to use for this application.*/
	private String resourceBundleBaseName=DEFAULT_RESOURCE_BUNDLE_BASE_NAME;

		/**@return The base name of the resource bundle to use for this application.*/
		public String getResourceBundleBaseName() {return resourceBundleBaseName;}

		/**Changes the resource bundle base name.
		This is a bound property.
		@param newResourceBundleBaseName The new base name of the resource bundle.
		@see GuiseApplication#RESOURCE_BUNDLE_BASE_NAME_PROPERTY
		*/
		public void setResourceBundleBaseName(final String newResourceBundleBaseName)
		{
			if(!ObjectUtilities.equals(resourceBundleBaseName, newResourceBundleBaseName))	//if the value is really changing
			{
				final String oldResourceBundleBaseName=resourceBundleBaseName;	//get the old value
				resourceBundleBaseName=checkNull(newResourceBundleBaseName, "Resource bundle base name cannot be null.");	//actually change the value
				firePropertyChange(RESOURCE_BUNDLE_BASE_NAME_PROPERTY, oldResourceBundleBaseName, newResourceBundleBaseName);	//indicate that the value changed
			}			
		}

	/**Default constructor.
	This implemetation sets the locale to the JVM default.
	*/
	public AbstractGuiseApplication()
	{
		this(Locale.getDefault());	//construct the class with the JVM default locale
	}

	/**Locale constructor.
	@param locale The default application locale.
	*/
	public AbstractGuiseApplication(final Locale locale)
	{
		this.defaultLocale=locale;	//set the default locale
	}

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
	@param componentClass The class of component that may be registered.
	@return A class of controller registered to render component of the specific class, or <code>null</code> if no controller is registered.
	*/
	protected <C extends Component> Class<? extends Controller<GC, ? super C>> getRegisteredControllerClass(final Class<C> componentClass)
	{
		synchronized(controllerKitList)	//don't allow anyone to access the list of controller kits while we access it
		{
			for(final ControllerKit<GC> controllerKit:controllerKitList)	//for each controller kit in our list
			{
				final Class<? extends Controller<GC, ? super C>> controllerKitClass=controllerKit.getRegisteredControllerClass(componentClass);	//ask the controller kit for a registered controller class for this component
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
	@param componentClass The class of component for which a render strategy should be returned.
	@return A class of render strategy to render the given component class, or <code>null</code> if no render strategy is registered.
	*/
//TODO fix	@SuppressWarnings("unchecked")
	protected <C extends Component> Class<? extends Controller<GC, ? super C>> getControllerClass(final Class<C> componentClass)
	{
		Class<? extends Controller> controllerClass=getRegisteredControllerClass(componentClass);	//see if there is a controller class registered for this component type
		if(controllerClass==null)	//if we didn't find a render strategy for this class, check the super class
		{
			final Class<?> superClass=componentClass.getSuperclass();	//get the super class of the component
			if(superClass!=null && Component.class.isAssignableFrom(superClass))	//if the super class is a component
			{
				controllerClass=getControllerClass((Class<? extends C>)superClass);	//check the super class
			}
		}
		if(controllerClass==null)	//if we still couldn't find a render strategy for this class, check the interfaces
		{
			for(final Class<?> classInterface:componentClass.getInterfaces())	//look at each implemented interface
			{
				if(Component.class.isAssignableFrom(classInterface))	//if the class interface is a component
				{
					controllerClass=getControllerClass((Class<? extends C>)classInterface);	//check the interface
					if(controllerClass!=null)	//if we found a render strategy class
					{
						break;	//stop looking at the interfaces
					}
				}					
			}
		}
		return (Class<? extends Controller<GC, ? super C>>)controllerClass;	//show which if any render strategy class we found
	}

	/**Determines the controller appropriate for the given component.
	A controller class is located by individually looking up the component class hiearchy for registered render strategies, at each checking all installed controller kits.
	@param component The component for which a controller should be returned.
	@return A controller to render the given component, or <code>null</code> if no controller is registered.
	*/
//TODO fix	@SuppressWarnings("unchecked")
	public <C extends Component<?>> Controller<GC, ? super C> getController(final C component)
	{
		Class<C> componentClass=(Class<C>)component.getClass();	//get the component class
		final Class<? extends Controller<GC, ? super C>> renderStrategyClass=getControllerClass(componentClass);	//walk the hierarchy to see if there is a render strategy class registered for this component type
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
	private final Map<String, Class<? extends NavigationFrame>> navigationPathFrameBindingMap=synchronizedMap(new HashMap<String, Class<? extends NavigationFrame>>());

		/**Binds a frame type to a particular application context-relative path.
		Any existing binding for the given context-relative path is replaced.
		@param path The appplication context-relative path to which the frame should be bound.
		@param navigationFrameClass The class of frame to render for this particular appplication context-relative path.
		@return The frame previously bound to the given appplication context-relative path, or <code>null</code> if no frame was previously bound to the path.
		@exception NullPointerException if the path and/or the frame is null.
		@exception IllegalArgumentException if the provided path is absolute.
		*/
		public Class<? extends NavigationFrame> bindNavigationFrame(final String path, final Class<? extends NavigationFrame> navigationFrameClass)
		{
			if(isAbsolutePath(path))	//if the path is absolute
			{
				throw new IllegalArgumentException("Bound navigation path cannot be absolute: "+path);
			}
			return navigationPathFrameBindingMap.put(checkNull(path, "Path cannot be null."), checkNull(navigationFrameClass, "Type cannot be null."));	//store the binding
		}

		/**Determines the class of frame bound to the given application context-relative path.
		@param path The address for which a frame should be retrieved.
		@return The type of frame bound to the given path, or <code>null</code> if no frame is bound to the path. 
		@exception IllegalArgumentException if the provided path is absolute.
		*/
		public Class<? extends NavigationFrame> getBoundNavigationFrameClass(final String path)
		{
			if(isAbsolutePath(path))	//if the path is absolute
			{
				throw new IllegalArgumentException("Bound navigation path cannot be absolute: "+path);
			}
			return navigationPathFrameBindingMap.get(path);	//return the bound frame type, if any
		}

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
	public String resolvePath(final String path)
	{
		return resolveURI(createPathURI(path)).toString();	//create a URI for the given path, ensuring that the string only specifies a path, and resolve that URI
	}

	/**Resolves URI against the application context path.
	Relative paths will be resolved relative to the application context path. Absolute paths will be considered already resolved, as will absolute URIs.
	For an application path "/path/to/application/", resolving "relative/path" will yield "/path/to/application/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path". Resolving "http://example.com/path" will yield "http://example.com/path".
	@param uri The URI to be resolved.
	@return The uri resolved against the application context path.
	@exception NullPointerException if the given URI is <code>null</code>.
	@see GuiseApplication#getContextPath()
	*/
	public URI resolveURI(final URI uri)
	{
		return URI.create(getContextPath()).resolve(checkNull(uri, "URI cannot be null."));	//create a URI from the application context path and resolve the given path against it
	}

}
