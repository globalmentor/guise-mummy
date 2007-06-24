package com.guiseframework.platform.web;

import java.net.URI;

import static com.garretwilson.lang.EnumUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.util.NameValuePair;

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
		AUDIO_PAUSE;
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

	/**Processes an event from the platform.
	@param event The event to be processed.
	*/
	public void processEvent(final PlatformEvent event)
	{
		if(event instanceof WebChangeEvent)	//if a property changed
		{
			final WebChangeEvent<Audio> changeEvent=(WebChangeEvent<Audio>)event;	//get the web change event
			final String stateString=asInstance(changeEvent.getProperties().get("state"), String.class);	//get the new state TODO use a constant
			if(stateString!=null)	//if a state was given
			{
				changeEvent.getDepictedObject().setState(getSerializedEnum(TaskState.class, stateString));	//change the state of the audio
			}
		}
	}

}
