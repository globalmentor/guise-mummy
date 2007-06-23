package com.guiseframework.audio;

import static com.garretwilson.lang.ClassUtilities.getPropertyName;
import static com.garretwilson.lang.ObjectUtilities.checkInstance;

import java.net.URI;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.model.TaskState;
import com.guiseframework.platform.AbstractDepictedObject;
import com.guiseframework.platform.DepictedObject;

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
	If the audio is already started, no action occurs.
	*/
	public void start()
	{
		if(getState()==null)	//if the audio is not yet started
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
	}


	/**Requests that the audio stop.
	If the audio is not playing, no action occurs.
	*/
	public void stop()
	{

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
//TODO		public void pause();

		/**Requests that the audio stop.*/
//TODO		public void stop();

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
