package com.guiseframework.audio;

import java.net.URI;

import static com.globalmentor.java.ClassUtilities.*;

import com.globalmentor.java.Objects;
import com.guiseframework.event.EventListenerManager;
import com.guiseframework.event.ProgressEvent;
import com.guiseframework.event.ProgressListenable;
import com.guiseframework.event.ProgressListener;
import com.guiseframework.model.TaskState;
import com.guiseframework.platform.AbstractDepictedObject;

/**Audio that can be played.
The installed depictor must be of the specialized type {@link Depictor}.
@author Garret Wilson
*/
public class Audio extends AbstractDepictedObject implements ProgressListenable<Long>
{

	/**The bound property of the audio URI.*/
	public final static String AUDIO_URI_PROPERTY=getPropertyName(Audio.class, "audioURI");
	/**The bound property of the state.*/
	public final static String STATE_PROPERTY=getPropertyName(Audio.class, "state");
	/**The bound property of the play duration using microseconds.*/
	public final static String TIME_LENGTH_PROPERTY=getPropertyName(Audio.class, "timeLength");
	/**The bound property of the play position in time using microseconds.*/
	public final static String TIME_POSITION_PROPERTY=getPropertyName(Audio.class, "timePosition");

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
			if(!Objects.equals(audioURI, newAudioURI))	//if the value is really changing
			{
				stop();	//make sure the current audio is stopped
				final URI oldAudio=audioURI;	//get the old value
				audioURI=newAudioURI;	//actually change the value
				firePropertyChange(AUDIO_URI_PROPERTY, oldAudio, newAudioURI);	//indicate that the value changed
				updateTimePosition(0);	//show that we're at the beginning of this audio
				updateTimeLength(-1);	//show that we don't know how long this audio is
			}			
		}

	/**The state of the audio, or <code>null</code> if the audio has not been started.*/
	private TaskState state=null;

		/**@return The state of the audio, or <code>null</code> if the audio has not been started.*/
		public TaskState getState() {return state;}

		/**Updates the state of the audio.
		This method is called by the associated depictor and should normally not be called directly by applications.
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
				if(newState==TaskState.STOPPED)	//if the audio was just stopped
				{
					updateTimeProgress(0, -1);	//show that we'll be at the beginning of this audio if/when we start again
				}
			}
		}

	/**The current play position in microseconds.*/
	private long timePosition=0;

		/**@return The current play position in microseconds.*/
		public long getTimePosition() {return timePosition;}

		/**Updates the time-based play position.
		This is a bound property.
		@param newTimePosition The new play position in microseconds.
		@exception IllegalArgumentException if the given position is negative.
		@see #TIME_POSITION_PROPERTY
		*/
		protected void updateTimePosition(final long newTimePosition)
		{
			if(timePosition!=newTimePosition)	//if the value is really changing
			{
				if(newTimePosition<0)	//if the new time position is negative
				{
					throw new IllegalArgumentException("Time position cannot be negative: "+newTimePosition);
				}
				final long oldTimePosition=timePosition;	//get the old value
				timePosition=newTimePosition;	//actually change the value
				firePropertyChange(TIME_POSITION_PROPERTY, oldTimePosition, newTimePosition);	//indicate that the value changed
			}
		}

		/**Requests a new time-based play position.
		This is an asynchronous bound property.
		@param newTimePosition The new play position in microseconds.
		@exception IllegalArgumentException if the given position is negative.
		@see #TIME_POSITION_PROPERTY
		*/
		public void setTimePosition(final long newTimePosition)
		{
			getDepictor().setTimePosition(newTimePosition);	//tell the depictor to request the time position			
		}

	/**The current duration or estimated duration in microseconds, or -1 if not known.*/
	private long timeLength=-1;

		/**@return The current duration or estimated duration in microseconds, or -1 if not known.*/
		public long getTimeLength() {return timeLength;}

		/**Updates the duration of the audio.
		This is a bound property.
		@param newTimeLength The new duration in microseconds.
		@see #TIME_LENGTH_PROPERTY
		*/
		protected void updateTimeLength(final long newTimeLength)
		{
			if(timeLength!=newTimeLength)	//if the value is really changing
			{
				final long oldTimeLength=timeLength;	//get the old value
				timeLength=newTimeLength;	//actually change the value
				firePropertyChange(TIME_LENGTH_PROPERTY, oldTimeLength, newTimeLength);	//indicate that the value changed
			}
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

	/**Requests that the audio start playing.
	If the audio is currently starting to play or already playing, no action occurs.
	*/
	public void play()
	{
		final TaskState state=getState();	//get the current audio state
		if(state!=TaskState.INITIALIZE && state!=TaskState.INCOMPLETE)	//if the audio is not yet started
		{
			setState(TaskState.INITIALIZE);	//show that we're initializing the audio
			getDepictor().play();	//tell the depictor to start
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

	/**Updates the progress of the audio, firing the appropriate progress event.
	This method is called by the associated depictor and should normally not be called directly by applications.
	@param timePosition The current play position in microseconds, or <code>-1</code> if not known.
	@param timeLength The duration or estimated duration of the audio in microseconds, or <code>-1</code> if not known.
	*/
	public void updateTimeProgress(final long timePosition, final long timeLength)
	{
		if(timePosition>=0 || timeLength>=0)	//if one of the values is being updated
		{
			if(timePosition>=0)	//if the position is known
			{
				updateTimePosition(timePosition);	//update the position
			}
			if(timeLength>=0)	//if the duration is known
			{
				updateTimeLength(timeLength);	//update the duration
			}
			fireProgressed(getTimePosition(), getTimeLength());	//fire a new progress event, using the latest times that we know about (or an old value if it wasn't updated just now)
		}
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

	/**Fires a progress event to all registered progress listeners.
	This method delegates to {@link #fireProgessed(ProgressEvent)}.
	@param timePosition The current position in microseconds, or <code>-1</code> if not known.
	@param timeDuration The length or estimated length of the audio in microseconds, or <code>-1</code> if not known.
	@see ProgressListener
	@see ProgressEvent
	*/
	protected void fireProgressed(final long timePosition, final long timeDuration)
	{
		final EventListenerManager eventListenerManager=getEventListenerManager();	//get event listener support
		if(eventListenerManager.hasListeners(ProgressListener.class))	//if there are progress listeners registered
		{
			final URI audioURI=getAudioURI();	//get the audio URI
			fireProgressed(new ProgressEvent<Long>(this, audioURI!=null ? audioURI.toString() : null, getState(), timePosition>=0 ? Long.valueOf(timePosition) : null, timeDuration>=0 ? Long.valueOf(timeDuration) : null));	//create and fire a new progress event
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

	/**The custom depictor type for audio.
	@author Garret Wilson
	@param <A> The type of audio to be depicted.
	*/
	public interface Depictor<A extends Audio> extends com.guiseframework.platform.Depictor<A>
	{

		/**Requests that the audio start playing.*/
		public void play();

		/**Requests that the audio pause.*/
		public void pause();

		/**Requests that the audio stop.*/
		public void stop();

		/**Requests a new time-based play position.
		@param newTimePosition The new play position in microseconds.
		@exception IllegalArgumentException if the given position is negative.
		*/
		public void setTimePosition(final long newTimePosition);

	}
}
