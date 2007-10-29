package com.guiseframework.component;

import java.math.BigDecimal;
import java.util.Collection;

import com.garretwilson.itu.SIUnit;

import static com.garretwilson.lang.ClassUtilities.*;
import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.net.URIPath;

import static com.garretwilson.text.CharacterConstants.*;

import com.guiseframework.Bookmark;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.geometry.Extent;
import com.guiseframework.geometry.Unit;
import com.guiseframework.model.TaskState;
import com.guiseframework.platform.*;
import com.guiseframework.prototype.ActionPrototype;

import static com.guiseframework.theme.Theme.*;

/**Panel to browse platform files and upload them to the specified destination.
Progress events are sent both for individual platform file transfer progress and for overall progress.
For the former, the source of the event will be the relevant {@link PlatformFile};
for the latter, the source of the event will be a {@link PlatformFileUploadTask}.
@author Garret Wilson
*/
public class PlatformFileUploadPanel extends AbstractPanel implements ProgressListenable<Long>
{

	/**The bound property of the destination path.*/
	public final static String DESTINATION_PATH_PROPERTY=getPropertyName(PlatformFileUploadPanel.class, "destinationPath");

	/**The bound property of the destination bookmark.*/
	public final static String DESTINATION_BOOKMARK_PROPERTY=getPropertyName(PlatformFileUploadPanel.class, "destinationBookmark");

	/**The number of platform files to display at the same time.*/
	private final static int PLATFORM_FILE_DISPLAY_COUNT=16;

	/**The panel containing controls such as buttons.*/
	private final Panel controlPanel;

		/**@return The panel containing controls such as buttons.*/
		public Panel getControlPanel() {return controlPanel;}

	/**The destination path of the upload relative to the application context path, or <code>null</code> if the destination path has not yet been set.*/
	private URIPath destinationPath=null;

		/**@return The destination path of the upload relative to the application context path, or <code>null</code> if the destination path has not yet been set.*/
		public URIPath getDestinationPath() {return destinationPath;}

		/**Sets the destination path of the upload.
		This is a bound property.
		@param newDestinationPath The path relative to the application representing the destination of the collected resources.
		@exception NullPointerException if the given path is <code>null</code>.
		@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
		@exception IllegalArgumentException if the provided path is absolute.
		@see #DESTINATION_PATH_PROPERTY
		*/
		public void setDestinationPath(final URIPath newDestinationPath)
		{
			if(!ObjectUtilities.equals(destinationPath, newDestinationPath.checkRelative()))	//if the value is really changing
			{
				final URIPath oldDestinationPath=destinationPath;	//get the old value
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

	/**The platform file list control.*/
	private final ListControl<PlatformFile> platformFileListControl;

	/**The label containing the status of the current platform file.*/
	private final Label platformFileStatusLabel;

	/**The label containing the overall status.*/
	private final Label overallStatusLabel;

	/**The resource collect control.*/
//TODO del	private final ResourceCollectControl resourceCollectControl;

		/**@return The resource collect control.*/
//TODO del		public ResourceCollectControl getResourceCollectControl() {return resourceCollectControl;}

	/**The task for performing a file upload, or <code>null</code> if a file upload is not occuring.*/
	private PlatformFileUploadTask platformFileUploadTask=null;

	/**The action prototype for browsing the platform file system.*/
	private final ActionPrototype browseActionPrototype;

		/**@return The action prototype for browsing the platforom file system.*/
		public ActionPrototype getBrowseActionPrototype() {return browseActionPrototype;}

	/**The action prototype for uploading.*/
	private final ActionPrototype uploadActionPrototype;

		/**@return The action prototype for uploading.*/
		public ActionPrototype getUploadActionPrototype() {return uploadActionPrototype;}

	/**The action prototype for canceling.*/
	private final ActionPrototype cancelActionPrototype;

		/**@return The action prototype for canceling.*/
		public ActionPrototype getCancelActionPrototype() {return cancelActionPrototype;}

	/**The progress listener that updates the platform file status label in response to individual platform file transfers.*/
	private final ProgressListener<Long> platformFileProgressListener=new ProgressListener<Long>()
			{
				public void progressed(final ProgressEvent<Long> progressEvent)	//when progress occurs
				{
					if(progressEvent.getTaskState()==TaskState.COMPLETE)	//if this file completes the upload
					{
						platformFileListControl.remove(progressEvent.getSource());	//remove this platform file from the list
					}
					updatePlatformFileStatusLabel((PlatformFile)progressEvent.getSource(), progressEvent.getTaskState(), progressEvent.getProgress(), progressEvent.getCompletion());	//update the individual platform file status label with the progress
					fireProgressed(progressEvent);	//pass along the progress event unmodified
				}
			};

		/**The progress listener that updates the overall status label in response to the overall platform file transfer task.*/
		private final ProgressListener<Long> overallProgressListener=new ProgressListener<Long>()
				{
					public void progressed(final ProgressEvent<Long> progressEvent)	//when progress occurs
					{
						final TaskState state=progressEvent.getTaskState();	//get the current overall transfer status
						if(state==TaskState.COMPLETE || state==TaskState.CANCELED || state==TaskState.ERROR)	//if the overall transfer ends
						{
							platformFileUploadTask.removeProgressListener(this);	//stop listening for the overall progress
							platformFileUploadTask=null;	//remove the upload task
						}
						updateOverallStatusLabel(state, progressEvent.getProgress(), progressEvent.getCompletion());	//update the overall status label with the progress
						updateComponents();	//update the components							
						fireProgressed(progressEvent);	//pass along the progress event unmodified
					}
				};

	/**Destination path constructor.
	@param destinationPath The path relative to the application representing the destination of the collected resources.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public PlatformFileUploadPanel(final URIPath destinationPath)
	{
		this(destinationPath, null);	//construct the panel with no bookmark
	}

	/**Destination path and destination bookmark constructor.
	@param destinationPath The path relative to the application representing the destination of the collected resources.
	@param destinationBookmark The bookmark to be used in sending resources to the destination path, or <code>null</code> if there is no bookmark specified.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public PlatformFileUploadPanel(final URIPath destinationPath, final Bookmark destinationBookmark)
	{
		this();	//construct the default panel
		setDestinationPath(destinationPath);	//set the destination path
		setDestinationBookmark(destinationBookmark);	//set the destionation bookmark
	}

	/**Default constructor with a default vertical flow layout.*/
	public PlatformFileUploadPanel()
	{
		super(new FlowLayout(Flow.PAGE));	//construct the parent class
		platformFileListControl=new ListControl<PlatformFile>(PlatformFile.class, PLATFORM_FILE_DISPLAY_COUNT);	//create a list in which to show the platform files
//TODO del if not needed; bring back if we make the list control editable		platformFileListControl.setEditable(false);	//don't allow the list to be edited
		platformFileListControl.setLineExtent(new Extent(30, Unit.EM));
		add(platformFileListControl);

		platformFileStatusLabel=new Label();	//current status label
		add(platformFileStatusLabel);
		overallStatusLabel=new Label();	//overall status label
		add(overallStatusLabel);
		
			//the horizontal panel of controls
		controlPanel=new LayoutPanel(new FlowLayout(Flow.LINE));
//TODO del		resourceCollectControl=new ResourceCollectControl();	//resource collector
//TODO del		controlPanel.add(resourceCollectControl);

		browseActionPrototype=new ActionPrototype(LABEL_BROWSE+HORIZONTAL_ELLIPSIS_CHAR, GLYPH_BROWSE);	//browse
		browseActionPrototype.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						getSession().getPlatform().selectPlatformFiles(true, new ValueSelectListener<Collection<PlatformFile>>()	//select platform files, listening for the selection to occur
								{
									public void valueSelected(final ValueEvent<Collection<PlatformFile>> valueEvent)	//when files are selected
									{
										final Collection<PlatformFile> platformFiles=valueEvent.getValue();	//get the new platform files
										platformFileListControl.clear();	//remove the currently displayed platform files
										platformFileListControl.addAll(platformFiles);	//add all the new platform files to the list
										for(final PlatformFile platformFile:platformFiles)	//for each platform file
										{
											platformFile.removeProgressListener(platformFileProgressListener);	//make sure we're not already listening for progress on this platform file
											platformFile.addProgressListener(platformFileProgressListener);	//start listening for progress on this platform file
										}
										updateComponents();	//update the components
									}
								});
					}
				});
		controlPanel.add(browseActionPrototype);
		uploadActionPrototype=new ActionPrototype(LABEL_UPLOAD, GLYPH_UPLOAD);	//resource upload
		uploadActionPrototype.setEnabled(false);	//initially disable upload
		uploadActionPrototype.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						platformFileUploadTask=new PlatformFileUploadTask(platformFileListControl, getDestinationPath(), getDestinationBookmark());	//create a new platform file upload task
						platformFileUploadTask.addProgressListener(overallProgressListener);	//listen for progress of the platform file upload task
						platformFileUploadTask.start();	//start the file uploads
						updateComponents();	//update the components to show the new state
					}
				});
		controlPanel.add(uploadActionPrototype);
		cancelActionPrototype=new ActionPrototype(LABEL_CANCEL, GLYPH_CANCEL);	//upload cancel
		cancelActionPrototype.setEnabled(false);	//initially disable canceling
		cancelActionPrototype.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						final PlatformFileUploadTask platformFileUploadTask=PlatformFileUploadPanel.this.platformFileUploadTask;	//get the file platform upload task
						if(platformFileUploadTask!=null)	//if there is an upload task
						{
							platformFileUploadTask.cancel();	//cancel the upload task
							updateComponents();	//update the components to show the new state
						}
					}
				});
		controlPanel.add(cancelActionPrototype);
	
			//listen for the resource collection control changing its list of collected resource paths
/*TODO del all resource collect control references
		resourceCollectControl.addPropertyChangeListener(ResourceCollectControl.RESOURCE_PATHS_PROPERTY, new AbstractGenericPropertyChangeListener<List<String>>()
				{
					public void propertyChange(final GenericPropertyChangeEvent<List<String>> genericPropertyChangeEvent)	//if the list of resource path changes
					{
						platformFileListControl.clear();	//remove the currently displayed resource paths
						platformFileListControl.addAll(genericPropertyChangeEvent.getNewValue());	//add all the new resource paths to the list
						updateComponents();	//update the components in response
					}
				});
*/
			//listen for the resource collection control changing its send state, and update the state of the components in response
/*TODO del
		resourceCollectControl.addPropertyChangeListener(ResourceCollectControl.STATE_PROPERTY, new AbstractGenericPropertyChangeListener<TaskState>()
				{
					public void propertyChange(final GenericPropertyChangeEvent<TaskState> propertyChangeEvent)	//if the transfer state changes
					{
						updateComponents();	//update the components in response
						updateStatusLabel(null, -1, propertyChangeEvent.getNewValue());	//update the status label with the new state
					}
				});
*/
			//listen for progress from the resource collect control and update the progress labels in response
/*TODO del
		resourceCollectControl.addProgressListener(new ProgressListener()
				{
					public void progressed(final ProgressEvent progressEvent)	//if progress occurs
					{
						updateStatusLabel(progressEvent.getTask(), progressEvent.getValue(), progressEvent.getTaskState());	//update the status level with the progress
						fireProgressed(new ProgressEvent(UploadPanel.this, progressEvent));	//refire the progress event using this panel as the source
					}
				});
*/
		add(controlPanel);
	}

	/**Updates the state of components.*/
	protected void updateComponents()
	{
		uploadActionPrototype.setEnabled(platformFileUploadTask==null && !platformFileListControl.isEmpty());	//only allow upload if no platform files are being uploaded and there are platform files to upload
		cancelActionPrototype.setEnabled(platformFileUploadTask!=null && platformFileUploadTask.getState()==TaskState.INCOMPLETE);	//only allow cancel if the there is an incomplete upload task
	}

	/**Updates the status label for an individual platform file.
	@param platformFile The current platform file.
	@param state The new transfer state, or <code>null</code> if there is no state.
	@param progress The current number of bytes transferred, or <code>null</code> if the bytes transferred is not known.
	@param completion The total number of bytes to transfer, or <code>null</code> if the total is not known.
	@exception NullPointerException if the given platform file is <code>null</code>.
	*/
	protected void updatePlatformFileStatusLabel(final PlatformFile platformFile, final TaskState state, final Long progress, final Long completion)
	{
		final StringBuilder statusStringBuilder=new StringBuilder();	//build the status string
		statusStringBuilder.append(platformFile.getName()).append(':').append(' ');	//platform file:
		if(state==null || state==TaskState.INCOMPLETE)	//if we don't have a state or we have a normal progres state
		{
			if(progress!=null)	//if a valid value is given
			{
				statusStringBuilder.append(SIUnit.BYTE.format(BigDecimal.valueOf(progress.longValue()), SIUnit.Prefix.KILO));	//show the value
			}
			else	//if there is no value
			{
				statusStringBuilder.append(LABEL_UNKNOWN);	//indicate an unknown progress
			}
			if(completion!=null)	//if the total is known
			{
				statusStringBuilder.append(" / ").append(SIUnit.BYTE.format(BigDecimal.valueOf(completion.longValue()), SIUnit.Prefix.KILO));	//show the total
			}
		}
		else if(state!=null)	//if we're not transferring, just show the task state
		{
			statusStringBuilder.append(state.getLabel());	//show the task status label
		}
		platformFileStatusLabel.setLabel(statusStringBuilder.toString());	//update the status
	}

	/**Updates the status label for the overall progress.
	@param state The new transfer state, or <code>null</code> if there is no state.
	@param progress The current number of bytes transferred, or <code>null</code> if the bytes transferred is not known.
	@param completion The total number of bytes to transfer, or <code>null</code> if the total is not known.
	*/
	protected void updateOverallStatusLabel(final TaskState state, final Long progress, final Long completion)
	{
		final StringBuilder statusStringBuilder=new StringBuilder();	//build the status string TODO combine common code
		statusStringBuilder.append(LABEL_TOTAL).append(':').append(' ');	//total:
		if(state==null || state==TaskState.INCOMPLETE)	//if we don't have a state or we have a normal progres state
		{
			if(progress!=null)	//if a valid value is given
			{
				statusStringBuilder.append(SIUnit.BYTE.format(BigDecimal.valueOf(progress.longValue()), SIUnit.Prefix.KILO));	//show the value
			}
			else	//if there is no value
			{
				statusStringBuilder.append(LABEL_UNKNOWN);	//indicate an unknown progress
			}
			if(completion!=null)	//if the total is known
			{
				statusStringBuilder.append(" / ").append(SIUnit.BYTE.format(BigDecimal.valueOf(completion.longValue()), SIUnit.Prefix.KILO));	//show the total
			}
		}
		else if(state!=null)	//if we're not transferring, just show the task state
		{
			statusStringBuilder.append(state.getLabel());	//show the task status label
		}
		overallStatusLabel.setLabel(statusStringBuilder.toString());	//update the status
	}

	/**Adds a progress listener.
	@param progressListener The progress listener to add.
	*/
	public void addProgressListener(final ProgressListener<Long> progressListener)
	{
		getEventListenerManager().add(ProgressListener.class, progressListener);	//add the listener
	}

	/**Removes an progress listener.
	@param progressListener The progress listener to remove.
	*/
	public void removeProgressListener(final ProgressListener<Long> progressListener)
	{
		getEventListenerManager().remove(ProgressListener.class, progressListener);	//remove the listener
	}

	/**Fires a given progress event to all registered progress listeners.
	@param progressEvent The progress event to fire.
	*/
	protected void fireProgressed(final ProgressEvent<Long> progressEvent)
	{
		for(final ProgressListener<Long> progressListener:getEventListenerManager().getListeners(ProgressListener.class))	//for each progress listener
		{
			progressListener.progressed(progressEvent);	//dispatch the progress event to the listener
		}
	}

}
