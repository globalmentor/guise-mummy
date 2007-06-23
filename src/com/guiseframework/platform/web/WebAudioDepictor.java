package com.guiseframework.platform.web;

import java.net.URI;

import com.garretwilson.util.NameValuePair;
import com.guiseframework.audio.Audio;

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
		AUDIO_START;
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

}
