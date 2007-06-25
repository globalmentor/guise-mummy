package com.guiseframework.audio;

import java.net.URI;

import static com.garretwilson.lang.ClassUtilities.*;
import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.event.EventListenerManager;
import com.guiseframework.event.TaskProgressEvent;
import com.guiseframework.event.TaskProgressListener;
import com.guiseframework.model.TaskState;
import com.guiseframework.platform.AbstractDepictedObject;

/**Audio that can be played.
The installed depictor must be of the specialized type {@link Depictor}.
@author Garret Wilson
*/
public class Audio extends AbstractDepictedObject
{

	/**The bound property of the audio URI.*/
	public final static String AUDIO_URI_PROPERTY=getPropertyName(Audio.class, "audioURI");
	/**The bound property of the state.*/
	public final static String STATE_PROPERTY=getPropertyName(Audio.class, "state");

	/**@return The depictor for this object.*/
	@SuppressWarnings("unchecked")
	public Depictor<? extends Audio> getDepictor() {return (Depictor<? extends Audio>)super.getDepictor();} 

	/**The audio URI, which may be a resource URI, or <code>null</code> if there is no audio URI.*/
	private URI audioURI=null;

		/**@return The audio URI, which may be a resource URI, or <code>null</code> if there is no audio URI.*/
		public URI getAudioURI() {return audioURI;}

		/**Sets the URI of the audio.
		This is a bound property of type <code>URI</code>.
		@param newAudioURI The new URI of the audio, which may be a resource URI, or <code>null</code> if there is no audio URI.
		@see #AUDIO_URI_PROPERTY
		*/
		public void setAudioURI(final URI newAudioURI)
		{
			if(!ObjectUtilities.equals(audioURI, newAudioURI))	//if the value is really changing
			{
				stop();	//make sure the current audio is stopped
				final URI oldAudio=audioURI;	//get the old value
				audioURI=newAudioURI;	//actually change the value
				firePropertyChange(AUDIO_URI_PROPERTY, oldAudio, newAudioURI);	//indicate that the value changed
			}			
		}

	/**The state of the audio, or <code>null</code> if the audio has not been started.*/
	private TaskState state=null;

		/**@return The state of the audio, or <code>null</code> if the audio has not been started.*/
		public TaskState getState() {return state;}

		/**Sets the state of the audio.
		This method is called by the associated depictor to update the audio state and should normally not be called directly by applications.
		This is a bound property.
		@param newState The new state of the audio, or <code>null</code> if the audio has not been started.
		@see #STATE_PROPERTY
		*/
		public void setState(final TaskState newState)
		{
			if(state!=newState)	//if the value is really changing
			{
				final TaskState oldState=state;	//get the old value
				state=newState;	//actually change the value
				firePropertyChange(STATE_PROPERTY, oldState, newState);	//indicate that the value changed
			}			
		}

	/**Requests that the audio start.
	If the audio is currently starting to play or already playing, no action occurs.
	*/
	public void start()
	{
		final TaskState state=getState();	//get the current audio state
		if(state!=TaskState.INITIALIZE && state!=TaskState.INCOMPLETE)	//if the audio is not yet started
		{
			setState(TaskState.INITIALIZE);	//show that we're initializing the audio
			getDepictor().start();	//tell the depictor to start
		}
	}

	/**Requests that the audio pause.
	If the audio is not playing, no action occurs.
	*/
	public void pause()
	{
		if(getState()==TaskState.INCOMPLETE)	//if the audio is playing
		{
			getDepictor().pause();	//tell the depictor to pause
		}
	}


	/**Requests that the audio stop.
	If the audio is not initializing, playing, or paused, no action occurs.
	*/
	public void stop()
	{
		final TaskState state=getState();	//get the current audio state
		if(state==TaskState.INITIALIZE || state==TaskState.INCOMPLETE || state==TaskState.PAUSED)	//if the audio is not initializing, playing, or paused
		{
			getDepictor().stop();	//tell the depictor to stop
		}
	}

	/**Adds a progress listener.
	@param progressListener The progress listener to add.
	*/
	public void addProgressListener(final TaskProgressListener progressListener)
	{
		getEventListenerManager().add(TaskProgressListener.class, progressListener);	//add the listener
	}

	/**Removes an progress listener.
	@param progressListener The progress listener to remove.
	*/
	public void removeProgressListener(final TaskProgressListener progressListener)
	{
		getEventListenerManager().remove(TaskProgressListener.class, progressListener);	//remove the listener
	}

	/**Fires a progress event to all registered progress listeners.
	This method delegates to {@link #fireProgessed(TaskProgressEvent)}.
	@param position The current position, specified in milliseconds, or <code>-1</code> if not known.
	@param duration The duration or estimated duration of the audio, specified in milliseconds, or <code>-1</code> if not known.
	@see TaskProgressListener
	@see TaskProgressEvent
	*/
	public void fireProgressed(final long position, final long duration)
	{
		final EventListenerManager eventListenerManager=getEventListenerManager();	//get event listener support
		if(eventListenerManager.hasListeners(TaskProgressListener.class))	//if there are progress listeners registered
		{
			final URI audioURI=getAudioURI();	//get the audio URI
			fireProgressed(new TaskProgressEvent(this, audioURI!=null ? audioURI.toString() : null, getState(), position, duration));	//create and fire a new progress event
		}
	}

	/**Fires a given progress event to all registered progress listeners.
	@param progressEvent The progress event to fire.
	*/
	protected void fireProgressed(final TaskProgressEvent progressEvent)
	{
		for(final TaskProgressListener progressListener:getEventListenerManager().getListeners(TaskProgressListener.class))	//for each progress listener
		{
			progressListener.taskProgressed(progressEvent);	//dispatch the progress event to the listener
		}
	}

	/**The custom depictor type for audio.
	@author Garret Wilson
	@param <O> The type of audio to be depicted.
	*/
	public interface Depictor<A extends Audio> extends com.guiseframework.platform.Depictor<A>
	{

		/**Requests that the audio start.*/
		public void start();

		/**Requests that the audio pause.*/
		public void pause();

		/**Requests that the audio stop.*/
		public void stop();

	}

	/**Default constructor.*/
	public Audio()
	{
		this(null);	//construct the audio with no audio URI
	}

	/**Audio URI constructor.
	@param audioURI The new URI of the audio, which may be a resource URI, or <code>null</code> if there is no audio URI.
	*/
	public Audio(final URI audioURI)
	{
		this.audioURI=audioURI;
	}

	/**Prepares the object for garbage collection.
	This implementation makes sure the audio is stopped.
	*/
	protected void finalize() throws Throwable
	{
		try
		{
			stop();	//stop the audio
    }
		finally	//always do the default finalization
		{
			super.finalize();
		}
	}
}
