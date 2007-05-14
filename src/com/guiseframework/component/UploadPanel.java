package com.guiseframework.component;

import java.util.List;

import static com.garretwilson.io.FileUtilities.getFilename;
import static com.garretwilson.lang.ClassUtilities.*;

import com.garretwilson.beans.AbstractGenericPropertyChangeListener;
import com.garretwilson.beans.GenericPropertyChangeEvent;
import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.util.Debug;

import static com.garretwilson.net.URIUtilities.*;
import com.guiseframework.Bookmark;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.geometry.Extent;
import com.guiseframework.model.TaskState;
import com.guiseframework.prototype.ActionPrototype;
import static com.guiseframework.theme.Theme.*;

/**Panel to collect resources and send them to the specified destination.
The destination path must be set before upload is initiated, otherwise an {@link IllegalStateException} will be thrown.
@author Garret Wilson
*/
public class UploadPanel extends AbstractPanel<UploadPanel>
{

	/**The bound property of the destination path.*/
	public final static String DESTINATION_PATH_PROPERTY=getPropertyName(UploadPanel.class, "destinationPath");

	/**The bound property of the destination bookmark.*/
	public final static String DESTINATION_BOOKMARK_PROPERTY=getPropertyName(UploadPanel.class, "destinationBookmark");

	/**The number of resource paths to display at the same time.*/
	private final static int RESOURCE_PATH_DISPLAY_COUNT=8;
	
	/**The destination path of the upload relative to the application context path, or <code>null</code> if the destination path has not yet been set.*/
	private String destinationPath=null;

		/**@return The destination path of the upload relative to the application context path, or <code>null</code> if the destination path has not yet been set.*/
		public String getDestinationPath() {return destinationPath;}

		/**Sets the destination path of the upload.
		This is a bound property.
		@param newDestinationPath The path relative to the application representing the destination of the collected resources.
		@exception NullPointerException if the given path is <code>null</code>.
		@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
		@exception IllegalArgumentException if the provided path is absolute.
		@see #DESTINATION_PATH_PROPERTY
		*/
		public void setDestinationPath(final String newDestinationPath)
		{
			if(!ObjectUtilities.equals(destinationPath, checkRelativePath(newDestinationPath)))	//if the value is really changing
			{
				final String oldDestinationPath=destinationPath;	//get the old value
				destinationPath=newDestinationPath;	//actually change the value
				firePropertyChange(DESTINATION_PATH_PROPERTY, oldDestinationPath, newDestinationPath);	//indicate that the value changed
			}
		}

	/**The bookmark to be used in sending resources to the destination path, or <code>null</code> if there is no bookmark specified.*/
	private Bookmark destinationBookmark=null;

		/**@return The bookmark to be used in sending resources to the destination path, or <code>null</code> if there is no bookmark specified.*/
		public Bookmark getDestinationBookmark() {return destinationBookmark;}	

		/**Sets the destination bookmark of the upload.
		This is a bound property.
		@param newDestinationBookmark The bookmark to be used in sending resources to the destination path, or <code>null</code> if there is no bookmark specified.
		@see #DESTINATION_BOOKMARK_PROPERTY
		*/
		public void setDestinationBookmark(final Bookmark newDestinationBookmark)
		{
			if(!ObjectUtilities.equals(destinationBookmark, newDestinationBookmark))	//if the value is really changing
			{
				final Bookmark oldDestinationBookmark=destinationBookmark;	//get the old value
				destinationBookmark=newDestinationBookmark;	//actually change the value
				firePropertyChange(DESTINATION_BOOKMARK_PROPERTY, oldDestinationBookmark, newDestinationBookmark);	//indicate that the value changed
			}
		}

	/**The resource path list control.*/
	private final ListControl<String> resourcePathList;

	/**The label containing the current status.*/
	private final Label currentStatusLabel;
	
	/**The resource collect control.*/
	private final ResourceCollectControl resourceCollectControl;

	/**The action prototype for uploading.*/
	private final ActionPrototype uploadActionPrototype;

		/**@return The action prototype for uploading.*/
		public ActionPrototype getUploadActionPrototype() {return uploadActionPrototype;}

	/**Destination path constructor a default vertical flow layout.
	@param destinationPath The path relative to the application representing the destination of the collected resources.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public UploadPanel(final String destinationPath)
	{
		this(destinationPath, null);	//construct the panel with no bookmark
	}

	/**Destination path and destination bookmark constructor a default vertical flow layout.
	@param destinationPath The path relative to the application representing the destination of the collected resources.
	@param destinationBookmark The bookmark to be used in sending resources to the destination path, or <code>null</code> if there is no bookmark specified.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public UploadPanel(final String destinationPath, final Bookmark destinationBookmark)
	{
		this();	//construct the default panel
		setDestinationPath(destinationPath);	//set the destination path
		setDestinationBookmark(destinationBookmark);	//set the destionation bookmark
	}

	/**Default constructor with a default vertical flow layout.*/
	public UploadPanel()
	{
		super(new FlowLayout(Flow.PAGE));	//construct the parent class
		resourcePathList=new ListControl<String>(String.class, RESOURCE_PATH_DISPLAY_COUNT);	//create a list in which to show the resource paths
		resourcePathList.setEditable(false);	//don't allow the list to be edited
		resourcePathList.setPreferredWidth(new Extent(30, Extent.Unit.EM));
		add(resourcePathList);

		currentStatusLabel=new Label();	//current status label
		add(currentStatusLabel);
		
			//the horizontal panel of controls
		final Panel<?> controlPanel=new LayoutPanel(new FlowLayout(Flow.LINE));
		resourceCollectControl=new ResourceCollectControl();	//resource collector
		controlPanel.add(resourceCollectControl);
		uploadActionPrototype=new ActionPrototype(LABEL_UPLOAD, GLYPH_UPLOAD);	//resource upload
		uploadActionPrototype.setEnabled(false);	//initially disable upload
		uploadActionPrototype.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						final String destinationPath=getDestinationPath();	//get the destination path
						if(destinationPath==null)	//if there is no destination path
						{
							throw new IllegalStateException("Destination path not set.");
						}
						resourceCollectControl.sendResources(destinationPath, getDestinationBookmark());	//tell the resource collect control to send the resource
					}
				});
		controlPanel.add(uploadActionPrototype);
	
			//listen for the resource collection control changing its list of collected resource paths
		resourceCollectControl.addPropertyChangeListener(ResourceCollectControl.RESOURCE_PATHS_PROPERTY, new AbstractGenericPropertyChangeListener<List<String>>()
				{
					public void propertyChange(final GenericPropertyChangeEvent<List<String>> genericPropertyChangeEvent)	//if the list of resource path changes
					{
						resourcePathList.clear();	//remove the currently displayed resource paths
						resourcePathList.addAll(genericPropertyChangeEvent.getNewValue());	//add all the new resource paths to the list
						updateComponents();	//update the components in response
					}
				});
			//listen for the resource collection control changing its send state, and update the state of the components in response
		resourceCollectControl.addPropertyChangeListener(ResourceCollectControl.SEND_STATE_PROPERTY, new AbstractGenericPropertyChangeListener<TaskState>()
				{
					public void propertyChange(final GenericPropertyChangeEvent<TaskState> genericPropertyChangeEvent)	//if the send state changes
					{
						updateComponents();	//update the components in response
					}
				});
			//listen for progress from the resource collect control and update the progress labels in response
		resourceCollectControl.addProgressListener(new ProgressListener()
				{
					public void progressed(final ProgressEvent progressEvent)	//if progress occurs
					{
						final StringBuilder statusStringBuilder=new StringBuilder();	//build the status string
						final String task=progressEvent.getTask();	//get the current task
						if(task!=null)	//if there is a task
						{
							statusStringBuilder.append(task).append(':').append(' ');	//task:
							final long value=progressEvent.getValue();	//get the current value
							if(value>=0)	//if a valid value is given
							{
								statusStringBuilder.append(value);	//show the value
							}
							else	//if there is no value
							{
								statusStringBuilder.append(LABEL_UNKNOWN);	//indicate an unknown progress
							}
						}
						else	//if there is no task, just show the task status
						{
							statusStringBuilder.append(progressEvent.getTaskState().getLabel());	//show the task status label
						}
						currentStatusLabel.setLabel(statusStringBuilder.toString());	//update the status
						fireProgressed(new ProgressEvent(UploadPanel.this, progressEvent));	//refire the progress event using this panel as the source
					}
				});
		add(controlPanel);
	}

	/**Updates the state of components.*/
	protected void updateComponents()
	{
		uploadActionPrototype.setEnabled(resourceCollectControl.getSendState()==null && !resourceCollectControl.getResourcePaths().isEmpty());	//only allow upload if the control is not sending and there are collected resources
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
