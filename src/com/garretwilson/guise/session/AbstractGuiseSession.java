package com.garretwilson.guise.session;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.*;
import static java.util.Collections.*;

import com.garretwilson.beans.*;
import com.garretwilson.event.PostponedEvent;
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
public abstract class AbstractGuiseSession<GC extends GuiseContext<GC>> implements GuiseSession<GC>
{

	/**The Guise application to which this session belongs.*/
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
		@param path A path that is either relative to the application context path or is absolute.
		@exception NullPointerException if the given path is <code>null</code>.
		@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #navigate(URI)} should be used instead).
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

	/**The object that listenes for context state changes and updates the set of context states in response.*/
	private final ContextStateListener contextStateListener=new ContextStateListener();

		/**@return The object that listenes for context state changes and updates the set of context states in response.*/
		protected ContextStateListener getContextStateListener() {return contextStateListener;}

	/**The unmodifiable set of all states of available Guise contexts.*/
	private Set<GuiseContext.State> contextStateSet=emptySet();

		/**@return The unmodifiable set of all states of available Guise contexts.*/
		public Set<GuiseContext.State> getContextStates() {return contextStateSet;}

	/**The set of registered contexts. A synchronized set is used so that updating the set of states can be based upon the very latest data when used by multiple threads.*/
	private final Set<GC> contextSet=synchronizedSet(new HashSet<GC>());

		/**Adds a context to this session and registers a listener for context state changes.
		@param context The context to add to this session.
		*/
		protected void addContext(final GC context)
		{
			contextSet.add(context);	//add this context to the set
			context.addPropertyChangeListener(GuiseContext.STATE_PROPERTY, getContextStateListener());	//listen for context state changes and update the set of context states in response
			updateContextStates();	//make sure the record of context states is up to date
		}
	
		/**Removes a context from this session and unregisters the listener for context state changes.
		@param context The context to remove from this session.
		*/
		protected void removeContext(final GC context)
		{
			context.removePropertyChangeListener(GuiseContext.STATE_PROPERTY, getContextStateListener());	//stop listening for context state changes
			contextSet.remove(context);	//remove this context from the set
			updateContextStates();	//make sure the record of context states is up to date
		}

		/**Updates the record of current states of available contexts.
		If any model change events are pending and no context is in an update model state, the model change events are processed.
		@see #fireQueuedModelEvents()
		*/
		protected void updateContextStates()
		{
			final EnumSet<GuiseContext.State> updatedContextStateSet=EnumSet.noneOf(GuiseContext.State.class);	//create an empty enum set
			synchronized(contextSet)	//don't allow anyone to add or remove context sets while we read them, and ensure we have the latest data
			{
				for(final GC context:contextSet)	//for each context
				{
					updatedContextStateSet.add(context.getState());	//add this state to our enumeration
				}
				contextStateSet=unmodifiableSet(updatedContextStateSet);	//update the set of context states
				if(!contextStateSet.contains(GuiseContext.State.UPDATE_MODEL))	//if no contexts are updating the model
				{
					fireQueuedModelEvents();	//fire any queued events
				}
			}
		}

	/**The synchronized list of postponed model events.*/
	private final List<PostponedEvent<?>> queuedModelEventList=synchronizedList(new ArrayList<PostponedEvent<?>>());

		/**Queues a postponed model event to be fired after all contexts have finished updating the model.
		If a Guise context is currently updating the model, the event will be queued for later.
		If no Guise context is currently updating the model, the event will be fired immediately.
		@param postponedModelEvent The event to fire at a later time.
		@see GuiseContext.State#UPDATE_MODEL
		*/
		public void queueModelEvent(final PostponedEvent<?> postponedModelEvent)
		{
			synchronized(contextSet)	//don't let the state of context states change while we check the states (the method updating context states synchronizes on the same value)
			{
				if(contextStateSet.contains(GuiseContext.State.UPDATE_MODEL))	//if at least one context is changing the model
				{
					queuedModelEventList.add(postponedModelEvent);	//add the postponed event to our list of postponed events					
				}
				else	//if no context is changing the model
				{
					postponedModelEvent.fireEvent();	//go ahead and fire the event immediately
				}
			}
		}

		/**Fires any postponed model events that are queued.*/
		protected void fireQueuedModelEvents()
		{
			synchronized(queuedModelEventList)	//don't allow any changes to the postponed model event list while we access it
			{
				for(final PostponedEvent<?> postponedModelEvent:queuedModelEventList)	//for each postponed model event
				{
					postponedModelEvent.fireEvent();	//fire the event
				}
				queuedModelEventList.clear();	//remove all pending model events
			}
		}

	/**The class that listens for context state changes and updates the context state set in response.
	@author Garret Wilson
	*/
	protected class ContextStateListener extends AbstractPropertyValueChangeListener<GuiseContext.State>
	{
		/**Called when a bound property is changed.
		@param propertyValueChangeEvent An event object describing the event source, the property that has changed, and its old and new values.
		*/
		public void propertyValueChange(final PropertyValueChangeEvent<GuiseContext.State> propertyValueChangeEvent)
		{
			updateContextStates();	//update the context states when a context state changes
		}
	}
}
