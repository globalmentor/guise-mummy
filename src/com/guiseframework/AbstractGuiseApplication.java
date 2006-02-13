package com.guiseframework;

import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.*;

import static java.util.Collections.*;

import com.garretwilson.beans.BoundPropertyObject;

import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.net.URIUtilities;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.util.LocaleUtilities.*;
import static com.guiseframework.GuiseResourceConstants.*;

import com.garretwilson.util.Debug;
import com.guiseframework.component.ApplicationFrame;
import com.guiseframework.component.Component;
import com.guiseframework.component.DefaultApplicationFrame;
import com.guiseframework.component.DefaultNavigationPanel;
import com.guiseframework.component.kit.ComponentKit;
import com.guiseframework.context.GuiseContext;
import com.guiseframework.controller.*;
import com.guiseframework.view.View;

/**An abstract base class for a Guise application.
This implementation only works with Guise containers that descend from {@link AbstractGuiseContainer}.
@author Garret Wilson
*/
public abstract class AbstractGuiseApplication extends BoundPropertyObject implements GuiseApplication
{

	/**The Guise container into which this application is installed, or <code>null</code> if the application is not yet installed.*/
	private AbstractGuiseContainer container=null;

		/**@return The Guise container into which this application is installed, or <code>null</code> if the application is not yet installed.*/
		public GuiseContainer getContainer() {return container;}

	/**Creates a new session for the application.
 	This version creates and returns a default session.
	@return A new session for the application
	*/
	public GuiseSession createSession()
	{
		return new DefaultGuiseSession(this);	//create a new default Guise session
	}

	/**Creates a frame for the application.
	This implementation returns a default application frame.
	@param session The Guise session that will own the application frame.
	@return A new frame for the application.
	*/
	public ApplicationFrame<?> createApplicationFrame(final GuiseSession session)
	{
		return new DefaultApplicationFrame(session);	//return an instance of the default application frame 
	}
		
	/**The base path of the application, or <code>null</code> if the application is not yet installed.*/
	private String basePath=null;

		/**Reports the base path of the application.
		The base path is an absolute path that ends with a slash ('/'), indicating the base path of the navigation panels.
		@return The base path representing the Guise application, or <code>null</code> if the application is not yet installed.
		*/
		public String getBasePath() {return basePath;}

	/**@return Whether this application has been installed into a container at some base path.
	@see #getContainer()
	@see #getBasePath()
	*/
	public boolean isInstalled() {return getContainer()!=null && getBasePath()!=null;}

	/**Checks to ensure that this application is installed.
	@exception IllegalStateException if the application is not installed.
	@see #isInstalled()
	*/
	public void checkInstalled()
	{
		if(!isInstalled())	//if the application is not installed
		{
			throw new IllegalStateException("Application not installed.");
		}
	}

	/**Installs the application into the given container at the given base path.
	This method is only package-visible so that it can be accessed by {@link AbstractGuiseContainer}.
	@param container The Guise container into which the application is being installed.
	@param basePath The base path at which the application is being installed.
	@exception NullPointerException if either the container or base path is <code>null</code>.
	@exception IllegalArgumentException if the context path is not absolute and does not end with a slash ('/') character.
	@exception IllegalStateException if the application is already installed.
	*/
	void install(final AbstractGuiseContainer container, final String basePath)
	{
		if(this.container!=null || this.basePath!=null)	//if we already have a container and/or a base path
		{
			throw new IllegalStateException("Application already installed.");
		}
		checkNull(container, "Container cannot be null");
		checkNull(basePath, "Application base path cannot be null");
		if(!isAbsolutePath(basePath) || !isContainerPath(basePath))	//if the path doesn't begin and end with a slash
		{
			throw new IllegalArgumentException("Application base path "+basePath+" does not begin and end with a path separator.");
		}
		this.container=container;	//store the container
		this.basePath=basePath;	//store the base path
	}

	/**Uninstalls the application from the given container.
	This method is only package-visible so that it can be accessed by {@link AbstractGuiseContainer}.
	@param container The Guise container into which the application is being installed.
	@exception IllegalStateException if the application is not installed or is installed into another container.
	*/
	void uninstall(final GuiseContainer container)
	{
		if(this.container==null)	//if we don't have a container
		{
			throw new IllegalStateException("Application not installed.");
		}
		if(this.container!=container)	//if we're installed into a different container
		{
			throw new IllegalStateException("Application installed into different container.");
		}
		this.container=null;	//release the container
		this.basePath=null;	//remove the base path
	}

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

	/**The base name of the resource bundle to use for this application, or <code>null</code> if no custom resource bundle is specified for this application.*/
	private String resourceBundleBaseName=null;

		/**@return The base name of the resource bundle to use for this application, or <code>null</code> if no custom resource bundle is specified for this application..*/
		public String getResourceBundleBaseName() {return resourceBundleBaseName;}

		/**Changes the resource bundle base name.
		This is a bound property.
		@param newResourceBundleBaseName The new base name of the resource bundle, or <code>null</code> if no custom resource bundle is specified for this application.
		@see GuiseApplication#RESOURCE_BUNDLE_BASE_NAME_PROPERTY
		*/
		public void setResourceBundleBaseName(final String newResourceBundleBaseName)
		{
			if(!ObjectUtilities.equals(resourceBundleBaseName, newResourceBundleBaseName))	//if the value is really changing
			{
				final String oldResourceBundleBaseName=resourceBundleBaseName;	//get the old value
				resourceBundleBaseName=newResourceBundleBaseName;	//actually change the value
				firePropertyChange(RESOURCE_BUNDLE_BASE_NAME_PROPERTY, oldResourceBundleBaseName, newResourceBundleBaseName);	//indicate that the value changed
			}			
		}

	/**The absolute or application-relative URI of the application style, or <code>null</code> if the default style should be used.*/
	private URI style;

		/**@return The absolute or application-relative URI of the application style, or <code>null</code> if the default style should be used.*/
		public URI getStyle() {return style;}

		/**Sets the URI of the style of the application.
		This is a bound property.
		@param newStyle The URI of the application style, or <code>null</code> if the default style should be used.
		@see GuiseApplication#STYLE_PROPERTY
		*/
		public void setStyle(final URI newStyle)
		{
			if(!ObjectUtilities.equals(style, newStyle))	//if the value is really changing (compare their values, rather than identity)
			{
				final URI oldStyle=style;	//get the old value
				style=newStyle;	//actually change the value
				firePropertyChange(STYLE_PROPERTY, oldStyle, newStyle);	//indicate that the value changed
			}
		}

	/**Default constructor.
	This implementation sets the locale to the JVM default.
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

	/**The thread-safe list of installed component kits, with later registrations taking precedence*/
	private final List<ComponentKit> componentKitList=new CopyOnWriteArrayList<ComponentKit>();

	/**Installs a component kit.
	Later component kits take precedence over earlier-installed component kits.
	If the component kit is already installed, no action occurs.
	@param componentKit The component kit to install.
	*/
	public void installComponentKit(final ComponentKit componentKit)
	{
		synchronized(componentKitList)	//don't allow anyone to access the list of component kits while we access it
		{
			if(!componentKitList.contains(componentKit))	//if the component kit is not already installed
			{
				componentKitList.add(0, componentKit);	//add the component kit to our list at the front of the list, giving it earlier priority
			}
		}
	}

	/**Uninstalls a component kit.
	If the component kit is not installed, no action occurs.
	@param componentKit The component kit to uninstall.
	*/
	public void uninstallComponentKit(final ComponentKit componentKit)
	{
		componentKitList.remove(componentKit);	//remove the installed component kit
	}

	/**Determines the controller class registered for the given component class.
	This request is delegated to each component kit, with later-installed component kits taking precedence. 
	@param componentClass The class of component that may be registered.
	@return A class of controller registered to render component of the specific class, or <code>null</code> if no controller is registered.
	*/
	protected Class<? extends Controller> getRegisteredControllerClass(final Class<? extends Component> componentClass)
	{
		for(final ComponentKit componentKit:componentKitList)	//for each component kit in our list
		{
			final Class<? extends Controller> controllerClass=componentKit.getRegisteredControllerClass(componentClass);	//ask the component kit for a registered controller class for this component
			if(controllerClass!=null)	//if this component kit gave us a controller class
			{
				return controllerClass;	//return the class
			}
		}
		return null;	//indicate that none of our installed component kits had a controller class registered for the specified component class
	}

	/**Determines the controller class appropriate for the given component class.
	A controller class is located by individually looking up the component class hiearchy for registered controllers.
	@param componentClass The class of component for which a controller should be returned.
	@return A class of controller to control the given component class, or <code>null</code> if no controller is registered.
	*/
	@SuppressWarnings("unchecked")	//we programmatically check the super classes and implemented interfaces to make sure they are component classes before casts
	protected Class<? extends Controller> getControllerClass(final Class<? extends Component> componentClass)
	{
		Class<? extends Controller> controllerClass=getRegisteredControllerClass(componentClass);	//see if there is a controller class registered for this component type
		if(controllerClass==null)	//if we couldn't find a controller for this class, check the immediate interfaces
		{
			for(final Class<?> classInterface:componentClass.getInterfaces())	//look at each implemented interface
			{
				if(Component.class.isAssignableFrom(classInterface))	//if the class interface is a component
				{
					controllerClass=getRegisteredControllerClass((Class<? extends Component>)classInterface);	//check the immediate interface
					if(controllerClass!=null)	//if we found a controller class
					{
						break;	//stop looking at the interfaces
					}
				}					
			}
		}
		if(controllerClass==null)	//if we still didn't find a controller for this class, check up the class hierarchy
		{
			final Class<?> superClass=componentClass.getSuperclass();	//get the super class of the component
			if(superClass!=null && Component.class.isAssignableFrom(superClass))	//if the super class is a component
			{
				controllerClass=getControllerClass((Class<? extends Component>)superClass);	//check the super class
			}
		}
		if(controllerClass==null)	//if we couldn't find a controller for this class, check the up the interfaces hierarchy
		{
			for(final Class<?> classInterface:componentClass.getInterfaces())	//look at each implemented interface; this results in duplicated checking of immediate interfaces, but the algorithm is more straightforward and this will only happen once for each controller installation
			{
				if(Component.class.isAssignableFrom(classInterface))	//if the class interface is a component
				{
					controllerClass=getControllerClass((Class<? extends Component>)classInterface);	//check the interface
					if(controllerClass!=null)	//if we found a controller class
					{
						break;	//stop looking at the interfaces
					}
				}					
			}
		}
		return controllerClass;	//show which if any controller class we found
	}

	/**Determines the controller appropriate for the given component.
	A controller class is located by individually looking up the component class hiearchy for registered render strategies, at each checking all installed component kits.
	@param <GC> The type of Guise context being used.
	@param <C> The type of component for which a controller is requested.
	@param component The component for which a controller should be returned.
	@return A controller to render the given component, or <code>null</code> if no controller is registered.
	*/
	public <C extends Component<?>> Controller<? extends GuiseContext, ? super C> getController(final C component)
	{
		Class<? extends Component> componentClass=component.getClass();	//get the component class
		final Class<? extends Controller> controllerClass=getControllerClass(componentClass);	//walk the hierarchy to see if there is a controller class registered for this component type
		if(controllerClass!=null)	//if we found a controller class
		{
			try
			{
				return (Controller<? extends GuiseContext, ? super C>)controllerClass.newInstance();	//return a new instance of the class
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
		return null;	//show that we could not find a registered controller
	}

	/**Determines the view class registered for the given component class.
	This request is delegated to each component kit, with later-installed component kits taking precedence. 
	@param componentClass The class of component that may be registered.
	@return A class of view registered to render component of the specific class, or <code>null</code> if no view is registered.
	*/
	protected Class<? extends View> getRegisteredViewClass(final Class<? extends Component> componentClass)
	{
		for(final ComponentKit componentKit:componentKitList)	//for each component kit in our list
		{
			final Class<? extends View> viewClass=componentKit.getRegisteredViewClass(componentClass);	//ask the component kit for a registered view class for this component
			if(viewClass!=null)	//if this component kit gave us a view class
			{
				return viewClass;	//return the class
			}
		}
		return null;	//indicate that none of our installed component kits had a view class registered for the specified component class
	}

	/**Determines the view class appropriate for the given component class.
	A view class is located by individually looking up the component class hiearchy for registered views.
	@param componentClass The class of component for which a view should be returned.
	@return A class of view for the given component class, or <code>null</code> if no view is registered.
	*/
	@SuppressWarnings("unchecked")	//we programmatically check the super classes and implemented interfaces to make sure they are component classes before casts
	protected Class<? extends View> getViewClass(final Class<? extends Component> componentClass)
	{
		Class<? extends View> viewClass=getRegisteredViewClass(componentClass);	//see if there is a view class registered for this component type
		if(viewClass==null)	//if we couldn't find a view for this class, check the immediate interfaces
		{
			for(final Class<?> classInterface:componentClass.getInterfaces())	//look at each implemented interface
			{
				if(Component.class.isAssignableFrom(classInterface))	//if the class interface is a component
				{
					viewClass=getRegisteredViewClass((Class<? extends Component>)classInterface);	//check the immediate interface
					if(viewClass!=null)	//if we found a view class
					{
						break;	//stop looking at the interfaces
					}
				}					
			}
		}
		if(viewClass==null)	//if we still didn't find a view for this class, check up the class hierarchy
		{
			final Class<?> superClass=componentClass.getSuperclass();	//get the super class of the component
			if(superClass!=null && Component.class.isAssignableFrom(superClass))	//if the super class is a component
			{
				viewClass=getViewClass((Class<? extends Component>)superClass);	//check the super class
			}
		}
		if(viewClass==null)	//if we couldn't find a view for this class, check up the interface hierarchy
		{
			for(final Class<?> classInterface:componentClass.getInterfaces())	//look at each implemented interface; this results in duplicated checking of immediate interfaces, but the algorithm is more straightforward and this will only happen once for each view installation
			{
				if(Component.class.isAssignableFrom(classInterface))	//if the class interface is a component
				{
					viewClass=getViewClass((Class<? extends Component>)classInterface);	//check the interface
					if(viewClass!=null)	//if we found a view class
					{
						break;	//stop looking at the interfaces
					}
				}					
			}
		}
		return viewClass;	//show which if any view class we found
	}

	/**Determines the view appropriate for the given component.
	A view class is located by individually looking up the component class hiearchy for registered render strategies, at each checking all installed component kits.
	@param <GC> The type of Guise context being used.
	@param <C> The type of component for which a view is requested.
	@param component The component for which a view should be returned.
	@return A view to render the given component, or <code>null</code> if no view is registered.
	*/
	public <C extends Component<?>> View<? extends GuiseContext, ? super C> getView(final C component)
	{
		Class<? extends Component> componentClass=component.getClass();	//get the component class
		final Class<? extends View> viewClass=getViewClass(componentClass);	//walk the hierarchy to see if there is a view class registered for this component type
		if(viewClass!=null)	//if we found a view class
		{
			try
			{
				return (View<? extends GuiseContext, ? super C>)viewClass.newInstance();	//return a new instance of the class
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
		return null;	//show that we could not find a registered view
	}


//TODO how do we keep the general public from changing the frame bindings?

	/**The synchronized map binding panel types to appplication context-relative absolute paths.*/
	private final Map<String, Class<? extends DefaultNavigationPanel>> navigationPathPanelBindingMap=synchronizedMap(new HashMap<String, Class<? extends DefaultNavigationPanel>>());

		/**Binds a panel type to a particular application context-relative path.
		Any existing binding for the given context-relative path is replaced.
		@param path The appplication context-relative path to which the panel should be bound.
		@param panelClass The class of panel to render for this particular appplication context-relative path.
		@return The panel previously bound to the given appplication context-relative path, or <code>null</code> if no panel was previously bound to the path.
		@exception NullPointerException if the path and/or the panel is <code>null</code>.
		@exception IllegalArgumentException if the provided path is absolute.
		*/
		public Class<? extends DefaultNavigationPanel> bindNavigationPanel(final String path, final Class<? extends DefaultNavigationPanel> panelClass)
		{
			if(isAbsolutePath(path))	//if the path is absolute
			{
				throw new IllegalArgumentException("Bound navigation path cannot be absolute: "+path);
			}
			return navigationPathPanelBindingMap.put(checkNull(path, "Path cannot be null."), checkNull(panelClass, "Type cannot be null."));	//store the binding
		}

		/**Determines the class of panel bound to the given application context-relative path.
		@param path The address for which a panel should be retrieved.
		@return The type of panel bound to the given path, or <code>null</code> if no panel is bound to the path. 
		@exception IllegalArgumentException if the provided path is absolute.
		*/
		public Class<? extends DefaultNavigationPanel> getNavigationPanelClass(final String path)
		{
			if(isAbsolutePath(path))	//if the path is absolute
			{
				throw new IllegalArgumentException("Bound navigation path cannot be absolute: "+path);
			}
			return navigationPathPanelBindingMap.get(path);	//return the bound panel type, if any
		}

		/**Determines if there is a panel class bound to the given appplication context-relative path.
		@param path The appplication context-relative path within the Guise container context.
		@return <code>true</code> if there is a panel bound to the given path, or <code>false</code> if no panel is bound to the given path.
		@exception NullPointerException if the path is <code>null</code>.
		@exception IllegalArgumentException if the provided path is absolute.
		*/
		public boolean hasNavigationPath(final String path)
		{
			if(isAbsolutePath(path))	//if the path is absolute
			{
				throw new IllegalArgumentException("Navigation path cannot be absolute: "+path);
			}
			return getNavigationPanelClass(path)!=null;	//see if there is a panel class bound to this navigation path
		}

	/**Resolves a relative or absolute path against the application base path.
	Relative paths will be resolved relative to the application base path. Absolute paths will be be considered already resolved.
	For an application base path "/path/to/application/", resolving "relative/path" will yield "/path/to/application/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path".
	@param path The path to be resolved.
	@return The path resolved against the application base path.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #resolveURI(URI)} should be used instead).
	@see #getBasePath()
	@see #resolveURI(URI)
	*/
	public String resolvePath(final String path)
	{
		return resolveURI(createPathURI(path)).toString();	//create a URI for the given path, ensuring that the string only specifies a path, and resolve that URI
	}

	/**Resolves URI against the application base path.
	Relative paths will be resolved relative to the application base path. Absolute paths will be considered already resolved, as will absolute URIs.
	For an application base path "/path/to/application/", resolving "relative/path" will yield "/path/to/application/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path". Resolving "http://example.com/path" will yield "http://example.com/path".
	@param uri The URI to be resolved.
	@return The uri resolved against the application base path.
	@exception NullPointerException if the given URI is <code>null</code>.
	@see #getBasePath()
	@see #resolvePath(String)
	*/
	public URI resolveURI(final URI uri)
	{
		return URI.create(getBasePath()).resolve(checkNull(uri, "URI cannot be null."));	//create a URI from the application base path and resolve the given path against it
	}

	/**Changes an absolute path to an application-relative path.
	For an application base path "/path/to/application/", relativizing "/path/to/application/relative/path" will yield "relative/path"
	@param path The path to be relativized.
	@return The path relativized to the application base path.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #resolveURI(URI)} should be used instead).
	@see #getBasePath()
	@see #relativizeURI(URI)
	*/
	public String relativizePath(final String path)
	{
		return URIUtilities.relativizePath(getBasePath(), path);	//get the path relative to the application path 
	}

	/**Changes a URI to an application-relative path.
	For an application base path "/path/to/application/", relativizing "http://www.example.com/path/to/application/relative/path" will yield "relative/path"
	@param uri The URI to be relativized.
	@return The URI path relativized to the application base path.
	@exception NullPointerException if the given URI is <code>null</code>.
	@see #getBasePath()
	@see #relativizePath(String)
	*/
	public String relativizeURI(final URI uri)
	{
		return relativizePath(uri.getRawPath());	//relativize the path of the URI TODO make sure the URI is from the correct domain
	}

	/**Determines the locale-sensitive path of the given resource path.
	Based upon the provided locale, candidate resource paths are checked in the following order:
	<ol>
		<li> <var>resourceBasePath</var> + "_" + <var>language</var> + "_" + <var>country</var> + "_" + <var>variant</var> </li>
		<li> <var>resourceBasePath</var> + "_" + <var>language</var> + "_" + <var>country</var> </li>
		<li> <var>resourceBasePath</var> + "_" + <var>language</var> </li>
	</ol>	 
	@param resourceBasePath An application-relative base path to a resource in the application resource storage area.
	@param locale The locale to use in generating candidate resource names.
	@return The locale-sensitive path to an existing resource based upon the given locale, or <code>null</code> if no resource exists at the given resource base path or any of its locale candidates.
	@exception NullPointerException if the given resource base path and/or locale is <code>null</code>.
	@exception IllegalArgumentException if the given resource path is absolute.
	@exception IllegalArgumentException if the given path is not a valid path.
	@see #hasResource(String)
	*/
	public String getLocaleResourcePath(final String resourceBasePath, final Locale locale)
	{
/*TODO refactor into common method
		final String relativeApplicationPath=relativizePath(getContainer().getBasePath(), getBasePath());	//get the application path relative to the container path
		final String contextRelativeResourcebasePath=relativeApplicationPath+resourceBasePath;	//get the base path relative to the container
*/
		for(int depth=3; depth>=0; --depth)	//try different locales, starting with the most specific
		{
			final String resourceCandidatePath=getLocaleCandidatePath(resourceBasePath, locale, depth);	//get a candidate path for the resource at this locale depth
			if(resourceCandidatePath!=null && hasResource(resourceCandidatePath))	//if we can generate a candidate path for the locale at this depth, and we have that resource
			{
				return resourceCandidatePath;	//return this candidate path
			}
		}
		return null;	//indicate that we were unable to find a resource path for the given locale
	}

	/**Determines if the application has a resource available stored at the given resource path.
	The provided path is first normalized.
	This implementation uses package access to delegate to {@link AbstractGuiseContainer#hasResource(String)}.
	@param resourcePath An application-relative path to a resource in the application resource storage area.
	@return <code>true</code> if a resource exists at the given resource path.
	@exception IllegalArgumentException if the given resource path is absolute.
	@exception IllegalArgumentException if the given path is not a valid path.
	*/
	public boolean hasResource(final String resourcePath)
	{
		checkInstalled();	//make sure we're installed
		final String relativeApplicationPath=URIUtilities.relativizePath(getContainer().getBasePath(), getBasePath());	//get the application path relative to the container path 
		return container.hasResource(relativeApplicationPath+resourcePath);	//delegate to the container
	}

	/**Retrieves and input stream to the resource at the given path.
	The provided path is first normalized.
	This implementation uses package access to delegate to {@link AbstractGuiseContainer#getResourceAsStream(String)}.
	@param resourcePath An application-relative path to a resource in the application resource storage area.
	@return An input stream to the resource at the given resource path, or <code>null</code> if no resource exists at the given resource path.
	@exception IllegalArgumentException if the given resource path is absolute.
	@exception IllegalArgumentException if the given path is not a valid path.
	*/
	public InputStream getResourceAsStream(final String resourcePath)
	{
		checkInstalled();	//make sure we're installed
		final String relativeApplicationPath=URIUtilities.relativizePath(getContainer().getBasePath(), getBasePath());	//get the application path relative to the container path 
		return container.getResourceAsStream(relativeApplicationPath+resourcePath);	//delegate to the container
	}

	/**Looks up a principal from the given ID.
	This version returns <code>null</code>. 
	@param id The ID of the principal.
	@return The principal corresponding to the given ID, or <code>null</code> if no principal could be determined.
	*/
	protected Principal getPrincipal(final String id)
	{
		return null;	//the abstract Guise application doesn't know any principals
	}

	/**Looks up the corresponding password for the given principal.
	This version returns <code>null</code>. 
	@param principal The principal for which a password should be returned.
	@return The password associated with the given principal, or <code>null</code> if no password is associated with the given principal.
	*/
	protected char[] getPassword(final Principal principal)
	{
		return null;	//the abstract Guise application doesn't know any passwords
	}

	/**Determines the realm applicable for the resource indicated by the given application path.
	This version returns the application base path as the realm for all application paths.
	@param applicationPath The relative path of the resource requested.
	@return The realm appropriate for the resource, or <code>null</code> if the given resource is not in a known realm.
	*/
	protected String getRealm(final String applicationPath)
	{
		return getBasePath();	//return the application base path as the realm for all resouces
	}

	/**Checks whether the given principal is authorized to access the resouce at the given application path.
	This version authorized any principal accessing any application path.
	@param applicationPath The relative path of the resource requested.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param realm The realm with which the resource is associated, or <code>null</code> if the realm is not known.
	@return <code>true</code> if the given principal is authorized to access the resource represented by the given application path.
	*/
	protected boolean isAuthorized(final String applicationPath, final Principal principal, final String realm)
	{
		return true;	//default to authorizing access
	}

}
