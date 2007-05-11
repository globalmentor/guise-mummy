package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.net.URIUtilities.*;

import java.util.*;
import static java.util.Collections.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.Bookmark;
import com.guiseframework.ResourceWriteDestination;
import com.guiseframework.event.*;
import com.guiseframework.model.*;

/**Control that allows resources to be collected and writes them to a given path. 
The destination path should reference a registered {@link ResourceWriteDestination} of the application.
@author Garret Wilson
*/
public class ResourceCollectControl extends AbstractControl<ResourceCollectControl>
{

	/**The bound property of the paths of the collected resources.*/
	public final static String RESOURCE_PATHS_PROPERTY=getPropertyName(ResourceCollectControl.class, "resourcePaths");
	/**The bound property of the state of sending resources.*/
	public final static String SEND_STATE_PROPERTY=getPropertyName(ResourceCollectControl.class, "sendState");

	/**The state of sending resources, or <code>null</code> if sending has not been initiated.*/
	private TaskState sendState=null;

		/**@return The state of sending resources, or <code>null</code> if sending has not been initiated..*/
		public TaskState getSendState() {return sendState;}

		/**Sets the state of sending resources.
		This method is called by the framework and normally this should not be called directly from applications. 
		This is a bound property.
		@param newSendState The new state of sending resources, or <code>null</code> if sending has not been initiated.
		@see #SEND_STATE_PROPERTY
		*/
		public void setSendState(final TaskState newSendState)
		{
			if(sendState!=newSendState)	//if the value is really changing
			{
				final TaskState oldSendState=sendState;	//get the old value
				sendState=newSendState;	//actually change the value
				firePropertyChange(SEND_STATE_PROPERTY, oldSendState, newSendState);	//indicate that the value changed
			}			
		}

	/**The paths of the curently collected resources.*/
	private List<String> resourcePaths=new CopyOnWriteArrayList<String>();

		/**Returns the paths of the currently collected resources.
		These paths are for identification only, and are not guaranteed to represent any location accessible from the application.
		@return The the paths of the currently collected resources.
		*/
		public List<String> getResourcePaths() {return unmodifiableList(resourcePaths);}

		/**Adds a new resource path.
		This method changes a bound property of type {@link List} holding type {@link String}.
		This method is called by the framework and normally this should not be called directly from applications. 
		Manually adding a new resource path, depending on the platform, may not actually result in another resource being collected absent user intervention.
		@param resourcePath The resource path to add.
		@exception NullPointerException if the given resource path is <code>null</code>.
		@see #RESOURCE_PATHS_PROPERTY
		*/
		public void addResourcePath(final String resourcePath)
		{
			resourcePaths.add(checkInstance(resourcePath, "Resource path cannot be null."));
			final List<String> newList=unmodifiableList(new ArrayList<String>(resourcePaths));	//create an unmodifiable copy of the resource paths
			firePropertyChange(RESOURCE_PATHS_PROPERTY, null, newList);	//indicate that the value changed			
		}

	/**The destination path relative to the application context path, of <code>null</code> if no resources are currently being sent.*/
	private String destinationPath=null;

		/**Indicates the destination path relative to the application context path.
		@return The path representing the destination of the collected resources, or <code>null</code> if no resources are currently being sent.
		*/
		public String getDestinationPath() {return destinationPath;}

	/**The bookmark being used in sending the resources to the destination path, or <code>null</code> if there is no bookmark specified and/or no resources are currently being sent.*/
	private Bookmark destinationBookmark=null;

		/**@return The bookmark being used in sending the resources to the destination path, or <code>null</code> if there is no bookmark specified and/or no resources are currently being sent.*/
		public Bookmark getDestinationBookmark() {return destinationBookmark;}	
	
	/**Default constructor with a default models.*/
	public ResourceCollectControl()
	{
		this(new DefaultLabelModel(), new DefaultEnableable());	//construct the class with default models
	}

	/**Label model and enableable object constructor.
	@param labelModel The component label model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model and/or enableable object is <code>null</code>.
	*/
	public ResourceCollectControl(final LabelModel labelModel, final Enableable enableable)
	{
		super(labelModel, enableable);	//construct the parent class
	}

	/**Sends collected resources to the given destination path with no bookmark.
	@param destinationPath The path representing the destination of the collected resources, or <code>null</code> if no resources are currently being sent.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	*/
	public void sendResources(final String destinationPath)
	{
		sendResources(destinationPath, null);	//send the resources with no bookmark
	}

	/**Sends collected resources to the given destination path using the given bookmark.
	If successful, the send state will be changed to {@link TaskState#INITIALIZE}.
	@param destinationPath The path representing the destination of the collected resources, or <code>null</code> if no resources are currently being sent.
	@param destinationBookmark The bookmark to be used in sending the resources to the destination path, or <code>null</code> if no bookmark should be used.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	@exception IllegalStateException if the current send state is not <code>null</code>.
	@see #getSendState()
	*/
	public void sendResources(final String destinationPath, final Bookmark destinationBookmark)
	{
		final TaskState sendState=getSendState();	//get the current send state
		if(sendState!=null)	//if the current send state is not null
		{
			throw new IllegalArgumentException("Cannot send when send state is "+sendState);
		}
		this.destinationPath=checkRelativePath(destinationPath);	//save the path
		this.destinationBookmark=destinationBookmark;	//save the bookmark
		setSendState(TaskState.INITIALIZE);	//initiate sending
	}

	/**Adds a progress listener.
	@param progressListener The progress listener to add.
	*/
	public void addProgressListener(final ProgressListener progressListener)
	{
		getEventListenerManager().add(ProgressListener.class, progressListener);	//add the listener
	}

	/**Removes an progress listener.
	@param progressListener The progress listener to remove.
	*/
	public void removeProgressListener(final ProgressListener progressListener)
	{
		getEventListenerManager().remove(ProgressListener.class, progressListener);	//remove the listener
	}

	/**Fires a progress event to all registered progress listeners.
	This method delegates to {@link #fireProgessed(ProgressEvent)}.
	@param task The task being performed, or <code>null</code> if not indicated.
	@param taskState The state of the task.
	@param value The current progress, or <code>-1</code> if not known.
	@param maximumValue The goal, or <code>-1</code> if not known.
	@exception NullPointerException if the given task state is <code>null</code>.
	@see ProgressListener
	@see ProgressEvent
	*/
	public void fireProgressed(final String task, final TaskState taskState, final long value, final long maximumValue)
	{
		final EventListenerManager eventListenerManager=getEventListenerManager();	//get event listener support
		if(eventListenerManager.hasListeners(ProgressListener.class))	//if there are progress listeners registered
		{
			fireProgressed(new ProgressEvent(this, task, taskState, value, maximumValue));	//create and fire a new progress event
		}
	}

	/**Fires a given progress event to all registered progress listeners.
	@param progressEvent The progress event to fire.
	*/
	protected void fireProgressed(final ProgressEvent progressEvent)
	{
		for(final ProgressListener progressListener:getEventListenerManager().getListeners(ProgressListener.class))	//for each progress listener
		{
			progressListener.progressed(progressEvent);	//dispatch the progress event to the listener
		}
	}
}
