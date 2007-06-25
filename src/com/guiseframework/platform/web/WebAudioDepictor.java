package com.guiseframework.platform.web;

import java.net.URI;
import java.util.Map;

import static com.garretwilson.lang.EnumUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.util.*;

import com.guiseframework.audio.Audio;
import com.guiseframework.model.TaskState;
import com.guiseframework.platform.PlatformEvent;

/**A web depictor for Guise audio.
This depictor uses the following web command events:
<dl>
	<dt>audio-start<dt> <dd>{audioURI:"<var>uri</var>"}</dd>
</dl>
@author Garret Wilson
*/
public class WebAudioDepictor extends AbstractWebDepictor<Audio> implements Audio.Depictor<Audio>
{

	/**The web commands for controlling audio.*/
	public enum AudioCommand implements WebCommand
	{
		/**The command to start the audio.*/
		AUDIO_START,

		/**The command to pause the audio.*/
		AUDIO_PAUSE,

		/**The command to stop the audio.*/
		AUDIO_STOP;
	}

	/**The property for specifying the URI of audio.*/
	public final static String AUDIO_URI_PROPERTY="audioURI";

	/**Requests that the audio start.*/
	@SuppressWarnings("unchecked")
	public void start()
	{
		final Audio audio=getDepictedObject();	//get the audio depicted object
		final URI audioURI=audio.getAudioURI();	//get the audio URI
		if(audioURI!=null)	//if there is an audio URI
		{
			final URI resolvedAudioURI=getDepictedObject().getSession().resolveURI(audioURI);	//resolve the audio URI
			getPlatform().getSendEventQueue().offer(new WebCommandEvent<Audio, AudioCommand>(getDepictedObject(), AudioCommand.AUDIO_START, new NameValuePair<String, Object>(AUDIO_URI_PROPERTY, resolvedAudioURI)));	//send an audio start command to the platform
		}
	}

	/**Requests that the audio pause.*/
	@SuppressWarnings("unchecked")
	public void pause()
	{
		getPlatform().getSendEventQueue().offer(new WebCommandEvent<Audio, AudioCommand>(getDepictedObject(), AudioCommand.AUDIO_PAUSE));	//send an audio pause command to the platform
	}

	/**Requests that the audio stop.*/
	@SuppressWarnings("unchecked")
	public void stop()
	{
		getPlatform().getSendEventQueue().offer(new WebCommandEvent<Audio, AudioCommand>(getDepictedObject(), AudioCommand.AUDIO_STOP));	//send an audio stop command to the platform
	}

	/**Processes an event from the platform.
	@param event The event to be processed.
	*/
	public void processEvent(final PlatformEvent event)
	{
		if(event instanceof WebChangeEvent)	//if a property changed
		{
			final WebChangeEvent<Audio> changeEvent=(WebChangeEvent<Audio>)event;	//get the web change event
			final Audio audio=changeEvent.getDepictedObject();	//get the depicted object
			final Map<String, Object> properties=changeEvent.getProperties();	//get the new properties
			final String stateString=asInstance(properties.get("state"), String.class);	//get the new state TODO use a constant
			if(stateString!=null)	//if a state was given
			{
				audio.setState(getSerializedEnum(TaskState.class, stateString));	//update the state of the audio
			}
			final Number position=asInstance(properties.get("position"), Number.class);	//get the position, if reported
			if(position!=null)	//if we have a position
			{
				final Number duration=asInstance(properties.get("duration"), Number.class);	//get the duration, if reported
Debug.trace("position", position, "duration", duration);
				audio.fireProgressed(position.longValue(), duration!=null ? duration.longValue() : -1);	//fire an audio progressed event
			}
			
		}
	}

}
