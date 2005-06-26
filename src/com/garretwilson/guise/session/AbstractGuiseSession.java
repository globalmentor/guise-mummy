package com.garretwilson.guise.session;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.*;

import com.garretwilson.guise.application.GuiseApplication;
import com.garretwilson.guise.component.NavigationFrame;
import com.garretwilson.guise.context.GuiseContext;
import com.garretwilson.lang.ObjectUtilities;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.text.xml.XMLUtilities.*;

/**An abstract implementation that keeps track of the components of a user session.
@author Garret Wilson
*/
public class AbstractGuiseSession<GC extends GuiseContext> implements GuiseSession<GC>
{

	/**@return The Guise application to which this session belongs.*/
	private final GuiseApplication<GC> application;

		/**@return The Guise application to which this session belongs.*/
		public GuiseApplication<GC> getApplication() {return application;}

	/**The map binding navigation frame types to appplication context-relative paths.*/
	private final Map<String, NavigationFrame> navigationPathFrameBindingMap=new HashMap<String, NavigationFrame>();

		/**Binds a frame to a particular appplication context-relative path.
		Any existing binding for the given context-relative path is replaced.
		@param path The appplication context-relative path to which the frame should be bound.
		@param frame The frame to render for this particular context-relative path.
		@return The frame previously bound to the given context-relative path, or <code>null</code> if no frame was previously bound to the path.
		@exception NullPointerException if the path and/or the frame is null.
		@exception IllegalArgumentException if the provided path is absolute.
		*/
		protected NavigationFrame bindNavigationFrame(final String path, final NavigationFrame frame)
		{
			if(isAbsolutePath(path))	//if the path is absolute
			{
				throw new IllegalArgumentException("Bound navigation path cannot be absolute: "+path);
			}
			return navigationPathFrameBindingMap.put(checkNull(path, "Path cannot be null."), checkNull(frame, "Bound frame cannot be null."));	//store the binding
		}
		
	/**Guise constructor.
	@param application The Guise application to which this session belongs.
	*/
	public AbstractGuiseSession(final GuiseApplication<GC> application)
	{
		this.application=application;	//save the Guise instance
	}

	/**Retrieves the frame bound to the given appplication context-relateive path.
	If a frame has already been created and cached, it will be be returned; otherwise, one will be created and cached. 
	The frame will be given an ID of a modified form of the path.
	@param path The appplication context-relative path within the Guise container context.
	@return The frame bound to the given path, or <code>null</code> if no frame is bound to the given path
	@exception IllegalArgumentException if the provided path is absolute.
	@exception NoSuchMethodException if the frame bound to the path does not provide Guise session constructor; or a Guise session and ID string constructor.
	@exception IllegalAccessException if the bound frame enforces Java language access control and the underlying constructor is inaccessible.
	@exception InstantiationException if the bound frame is an abstract class.
	@exception InvocationTargetException if the bound frame's underlying constructor throws an exception.
	*/
	public NavigationFrame getBoundNavigationFrame(final String path) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException
	{
		if(isAbsolutePath(path))	//if the path is absolute
		{
			throw new IllegalArgumentException("Bound navigation path cannot be absolute: "+path);
		}
		NavigationFrame frame=navigationPathFrameBindingMap.get(path);	//get the bound frame type, if any
		if(frame==null)	//if no frame is cached
		{
			final Class<? extends NavigationFrame> frameClass=getApplication().getBoundNavigationFrameClass(path);	//see which frame we should show for this path
			if(frameClass!=null)	//if we found a frame class for this path
			{
				try
				{
					final String frameID=createName(path);	//convert the path to a valid ID TODO use a Guise-specific routine or, better yet, bind an ID with the frame
					frame=frameClass.getConstructor(GuiseSession.class, String.class).newInstance(this, frameID);	//find the Guise session and ID constructor and create an instance of the class
				}
				catch(final NoSuchMethodException noSuchMethodException)	//if there was no Guise session and string ID constructor
				{
					frame=frameClass.getConstructor(GuiseSession.class).newInstance(this);	//use the Guise session constructor if there is one					
				}
				bindNavigationFrame(path, frame);	//bind the frame to the path, caching it for next time
			}
		}
		return frame;	//return the frame, or null if we couldn't find a frame
	}

	/**The navigation path relative to the application context path.*/
	private String navigationPath=null;

		/**Reports the navigation path relative to the application context path.
		@return The path representing the current navigation location of the Guise application.
		@exception IllegalStateException if this message has been called before the navigation path has been initialized.
		*/
		public String getNavigationPath()
		{
			if(navigationPath==null)	//if no navigation path has been set, yet
			{
				throw new IllegalStateException("Navigation path has not yet been initialized.");
			}
			return navigationPath;	//return the navigation path
		}

		/**Changes the navigation path of the session so that user interaction can change to another frame.
		If the given navigation path is the same as the current navigation path, no action occurs.
		@param navigationPath The navigation path relative to the application context path.
		@exception IllegalArgumentException if the provided path is absolute.
		@exception IllegalArgumentException if the navigation path is not recognized (e.g. there is no frame bound to the navigation path).
		*/
		protected void setNavigationPath(final String navigationPath)
		{
			if(!ObjectUtilities.equals(this.navigationPath, navigationPath))	//if the navigation path is really changing
			{
				if(getApplication().getBoundNavigationFrameClass(navigationPath)==null)	//if no frame is bound to the given navigation path
				{
					throw new IllegalArgumentException("Unknown navigation path: "+navigationPath);
				}
				this.navigationPath=navigationPath;	//change the navigation path TODO fire an event
			}
		}

	/**The requested navigation URI; usually either a relative or absolute path, or an absolute URI.*/
	private URI requestedNavigation=null;

		/**@return The requested navigation URI---usually either a relative or absolute path, or an absolute URI---or <code>null</code> if no navigation has been requested.*/
		protected URI getRequestedNavigation() {return requestedNavigation;}

		/**Removes any requests for navigation.*/
		protected void clearRequestedNavigation() {requestedNavigation=null;}

		/**Requests navigation to the specified path.
		The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
		Later requested navigation before navigation occurs will override this request.
		@param path A path that is either relative to the application context path or absolute.
		@exception NullPointerException if the given path is <code>null</code>.
		@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case <code>navigateURI()</code> should be used instead).
		@see #navigate(URI)
		*/
		public void navigate(final String path)
		{
			navigate(createPathURI(path));	//navigate to the requested URI, converting the path to a URI and verifying that it is only a path
		}
	
		/**Requests navigation to the specified URI.
		The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
		Later requested navigation before navigation occurs will override this request.
		@param uri Either a relative or absolute path, or an absolute URI.
		@exception NullPointerException if the given URI is <code>null</code>.
		*/
		public void navigate(final URI uri)
		{
			requestedNavigation=getApplication().resolveURI(checkNull(uri, "URI cannot be null."));	//resolve the URI against the application context path
		}
}
