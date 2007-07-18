package com.guiseframework.platform;

import java.util.*;
import static java.util.Collections.*;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.net.URIUtilities.*;

import com.guiseframework.Bookmark;
import com.guiseframework.event.*;
import com.guiseframework.model.TaskState;

/**A task to upload a series of files from the platform.
The files are uploaded sequentially.
This task fires progress events indicating the overall bytes transferred out of the total bytes to transfer.
The task state indicates the state of the overall transfer, not each individual transfer.
@author Garret Wilson
*/
public class PlatformFileUploadTask extends GuiseBoundPropertyObject
{

	/**The bound property of the state.*/
	public final static String STATE_PROPERTY=getPropertyName(PlatformFileUploadTask.class, "state");	//TODO move to task interface

	/**The object managing event listeners.*/
	private final EventListenerManager eventListenerManager=new EventListenerManager();

		/**@return The object managing event listeners.*/
		protected EventListenerManager getEventListenerManager() {return eventListenerManager;}

	/**The platform files to upload.*/
	private final List<PlatformFile> platformFiles;

		/**@return The platform files to upload.*/
		public List<PlatformFile> getPlatformFiles() {return platformFiles;}

	/**The destination path of the upload relative to the application context path, or <code>null</code> if the destination path has not yet been set.*/
	private final String destinationPath;

		/**@return The destination path of the upload relative to the application context path, or <code>null</code> if the destination path has not yet been set.*/
		public String getDestinationPath() {return destinationPath;}

	/**The bookmark to be used in sending resources to the destination path, or <code>null</code> if there is no bookmark specified.*/
	private final Bookmark destinationBookmark;

		/**@return The bookmark to be used in sending resources to the destination path, or <code>null</code> if there is no bookmark specified.*/
		public Bookmark getDestinationBookmark() {return destinationBookmark;}	

	/**The state of the task, or <code>null</code> if the task has not been started.*/
	private TaskState state=null;

		/**@return The state of the task, or <code>null</code> if the task has not been started.*/
		public TaskState getState() {return state;}

		/**Updates the state of the task.
		This is a bound property.
		@param newState The new state of the task, or <code>null</code> if the task has not been started.
		@see #STATE_PROPERTY
		*/
		protected void setState(final TaskState newState)
		{
			if(state!=newState)	//if the value is really changing
			{
				final TaskState oldState=state;	//get the old value
				state=newState;	//actually change the value
				firePropertyChange(STATE_PROPERTY, oldState, newState);	//indicate that the value changed
			}
		}

	/**The last number of bytes transferred before the current transfer.*/
	private long lastProgress=0;

	/**The number of bytes transferred for the current platform file.*/
	private long currentProgress=0;

		/**@return The total progress.*/
		public long getProgress()
		{
			return lastProgress+currentProgress;	//calculate the current progress
		}
	
	/**The total number of bytes to transfer.*/
	private final long completion;

		/**@return The total number of bytes to transfer.*/
		public final long getCompletion() {return completion;}

	/**The index of the platform file currently being uploaded, -1 if no upload has been started, or the size of the platform file list if all uploads have been completed.*/
	private int platformFileIndex=-1;

		/**@return The platform file currently being uploaded, or <code>null</code> if no platform file is currently being uploaded.*/
		protected PlatformFile getPlatformFile()
		{
			final List<PlatformFile> platformFiles=getPlatformFiles();	//get the platform files
			final int platformFileIndex=this.platformFileIndex;	//get a local copy of the current platform file index so that it can't be changed while we examine it
			return platformFileIndex>=0 && platformFileIndex<platformFiles.size() ? platformFiles.get(platformFileIndex) : null;	//return the platform file if this index is valid
		}

	/**The listener assigned to each platform file.
	While a platform file is being transferred, local totals are updated and the overall transfer amount is fired to any listeners.
	Once a platform file completes a transfer, the next sequential platform file will be started.
	Once all platform files are transferred, a transfer is canceled, or there is an error, the task state will be set accordingly and fired to all listeners.
	*/
	private final ProgressListener<Long> platformFileProgressListener=new ProgressListener<Long>()
			{
				public void progressed(final ProgressEvent<Long> progressEvent)	//when progress is made
				{
					final TaskState overallState=getState();	//get the overall transfer state
					final Long progress=progressEvent.getProgress();	//get the current progress
					if(progress!=null)	//if we know the current progress
					{
						currentProgress=progress;	//save the current progress
					}
					switch(progressEvent.getTaskState())	//check the progress state
					{
						case INCOMPLETE:	//if transfer is occurring
							if(overallState!=TaskState.CANCELED)	//if we didn't haven't already canceled the overall transfer
							{
								setState(TaskState.INCOMPLETE);	//the overall state is incomplete as well
							}
							fireProgressed();	//fire our own progress event
							break;
						case COMPLETE:	//if the transfer is complete
							uninitializeUpload();	//uninitialize the current platform file
							++platformFileIndex;	//go to the next platform file
							if(overallState!=TaskState.CANCELED)	//if we didn't haven't already canceled the overall transfer
							{
								if(platformFileIndex<getPlatformFiles().size())	//if we're not out of platform files
								{
									setState(TaskState.INCOMPLETE);	//the overall state is still incomplete
									fireProgressed();	//fire our own progress event
									initializeUpload();	//initialize the platform file
								}
								else	//if we're out of platform files
								{
									setState(TaskState.COMPLETE);	//change the overall state is also complete
									fireProgressed();	//fire our own progress event
								}
							}
							else	//if we've already canceled the overall transfer
							{
								fireProgressed();	//fire a progress event
							}
							break;
						case CANCELED:	//if the transfer is canceled
							uninitializeUpload();	//uninitialize the current platform file
							setState(TaskState.CANCELED);	//change the state to canceled
							fireProgressed();	//fire our own progress event
							break;
						case ERROR:	//if the transfer has an error
							uninitializeUpload();	//uninitialize the current platform file
							setState(TaskState.ERROR);	//change the state to error
							fireProgressed();	//fire our own progress event
							break;
						default:	//none of the other states should be used
							throw new IllegalStateException("Unrecognized platform file upload state "+progressEvent.getTaskState());
					}
				}
			};
		
	/**Platform files, Destination path and destination bookmark constructor.
	@param platformFiles The platform files to upload.
	@param destinationPath The path relative to the application representing the destination of the collected resources.
	@param destinationBookmark The bookmark to be used in sending resources to the destination path, or <code>null</code> if there is no bookmark specified.
	@exception NullPointerException if the given list of platform files and/or destination path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public PlatformFileUploadTask(final Iterable<PlatformFile> platformFiles, final String destinationPath, final Bookmark destinationBookmark)
	{
		final List<PlatformFile> tempPlatformFiles=new ArrayList<PlatformFile>();	//create a list of platform files
		long tempCompletion=0;	//we'll calculate the total number of bytes to transfer
		for(final PlatformFile platformFile:platformFiles)	//look at each platform file
		{
			tempPlatformFiles.add(platformFile);	//add this platform file to our list
			final long size=platformFile.getSize();	//get the size of this platform file
			if(size>=0)	//if we know the size of this file
			{
				tempCompletion+=size;	//update our total size
			}
		}
		this.completion=tempCompletion;	//save the completion amount
		this.platformFiles=unmodifiableList(tempPlatformFiles);	//save an unmodifiable copy of the list
		this.destinationPath=checkRelativePath(destinationPath);	//save the destination path
		this.destinationBookmark=destinationBookmark;	//save the destination bookmark
	}

	/**Starts the task.
	If the task has already been started no action occurs.
	*/
	public void start()
	{
		if(getState()==null)	//if the task hasn't yet started
		{
			setState(TaskState.INITIALIZE);	//show that we're initializing
			platformFileIndex=0;	//start at the first platform file
			initializeUpload();	//initialize the platform file
		}
	}

	/**Cancels the task.
	If the task is not progressing, no action occurs.
	*/
	public void cancel()
	{
		if(getState()==TaskState.INCOMPLETE)	//if the task is progressing
		{
			setState(TaskState.CANCELED);	//show that we're now canceled
			final PlatformFile platformFile=getPlatformFile();	//get the current platform file being uploaded
			if(platformFile!=null)	//if there is a platform file being uploaded
			{
				platformFile.cancel();	//cancel the current upload
			}
		}
	}

	/**Initializes and begins an upload for the current platform file.
	The current progress and completion are set to zero, a listener is installed for the given platform file, and the upload is initiated.
	*/
	protected void initializeUpload()
	{
		currentProgress=0;	//indicate that we haven't transferred anything for this platform file
		final PlatformFile platformFile=getPlatformFiles().get(platformFileIndex);	//get the current platform file
		platformFile.addProgressListener(platformFileProgressListener);	//start listening to the platform file's progress
		platformFile.upload(getDestinationPath(), getDestinationBookmark());	//tell the platform file to start uploading		
	}

	/**Cleans up after an upload for the current platform file.
	The last progress is updated with the current completion amount for the current file.
	*/
	protected void uninitializeUpload()
	{
		lastProgress+=currentProgress;	//update the last progress
		final PlatformFile platformFile=getPlatformFiles().get(platformFileIndex);	//get the current platform file
		platformFile.removeProgressListener(platformFileProgressListener);	//stop listening to the platform file's progress
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

	/**Fires a progress event to all registered progress listeners with the current .
	This method delegates to {@link #fireProgessed(ProgressEvent)}.
	@see ProgressListener
	@see ProgressEvent
	*/
	protected void fireProgressed()
	{
		final EventListenerManager eventListenerManager=getEventListenerManager();	//get event listener support
		if(eventListenerManager.hasListeners(ProgressListener.class))	//if there are progress listeners registered
		{
			fireProgressed(new ProgressEvent<Long>(this, null, getState(), getProgress(), getCompletion()));	//create and fire a new progress event
		}
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
