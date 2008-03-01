package com.guiseframework.platform.web;

import java.net.URI;
import java.util.Map;

import static com.globalmentor.java.Enums.*;
import static com.globalmentor.java.Objects.*;


import com.globalmentor.util.*;
import com.guiseframework.audio.Audio;
import com.guiseframework.model.TaskState;
import com.guiseframework.platform.DepictEvent;
import com.guiseframework.platform.PlatformEvent;

/**A web depictor for Guise audio.
@author Garret Wilson
*/
public class WebAudioDepictor extends AbstractWebDepictor<Audio> implements Audio.Depictor<Audio>
{

	/**The web commands for controlling audio.*/
	public enum AudioCommand implements WebPlatformCommand
	{

		/**The command to start the audio.
		parameters: <code>{{@value #AUDIO_URI_PROPERTY}:"<var>uri</var>"}</code>
		*/
		AUDIO_PLAY,

		/**The command to pause the audio.*/
		AUDIO_PAUSE,

		/**The command to stop the audio.*/
		AUDIO_STOP,

		/**The command to set the position of the audio.
		parameters: <code>{{@value #POSITION_PROPERTY}:"<var>millisecondPosition</var>"}</code>
		*/
		AUDIO_POSITION;

		/**The property for specifying the URI of the audio.*/
		public final static String AUDIO_URI_PROPERTY="audioURI";
		/**The property for specifying the position of the audio.*/
		public final static String POSITION_PROPERTY="position";
	
	}

	/**Requests that the audio start.*/
	@SuppressWarnings("unchecked")
	public void play()
	{
		final Audio audio=getDepictedObject();	//get the audio depicted object
		final URI audioURI=audio.getAudioURI();	//get the audio URI
		if(audioURI!=null)	//if there is an audio URI
		{
			final URI resolvedAudioURI=getDepictedObject().getSession().resolveURI(audioURI);	//resolve the audio URI
			getPlatform().getSendMessageQueue().add(new WebCommandDepictEvent<AudioCommand>(getDepictedObject(), AudioCommand.AUDIO_PLAY,
					new NameValuePair<String, Object>(AudioCommand.AUDIO_URI_PROPERTY, resolvedAudioURI)));	//send an audio start command to the platform
		}
	}

	/**Requests that the audio pause.*/
	public void pause()
	{
		getPlatform().getSendMessageQueue().add(new WebCommandDepictEvent<AudioCommand>(getDepictedObject(), AudioCommand.AUDIO_PAUSE));	//send an audio pause command to the platform
	}

	/**Requests that the audio stop.*/
	public void stop()
	{
		getPlatform().getSendMessageQueue().add(new WebCommandDepictEvent<AudioCommand>(getDepictedObject(), AudioCommand.AUDIO_STOP));	//send an audio stop command to the platform
	}

	/**Requests a new time-based play position.
	@param newTimePosition The new play position in microseconds.
	@exception IllegalArgumentException if the given position is negative.
	*/
	@SuppressWarnings("unchecked")
	public void setTimePosition(final long newTimePosition)
	{
		if(newTimePosition<0)	//if the new time position is negative
		{
			throw new IllegalArgumentException("Time position cannot be negative: "+newTimePosition);
		}
		getPlatform().getSendMessageQueue().add(new WebCommandDepictEvent<AudioCommand>(getDepictedObject(), AudioCommand.AUDIO_POSITION,
				new NameValuePair<String, Object>(AudioCommand.POSITION_PROPERTY, Long.valueOf(newTimePosition/1000))));	//send an audio position command to the platform, converting the time to milliseconds
	}

	/**Processes an event from the platform.
	@param event The event to be processed.
	@exception IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	*/
	public void processEvent(final PlatformEvent event)
	{
		if(event instanceof WebChangeDepictEvent)	//if a property changed
		{
			final WebChangeDepictEvent webChangeEvent=(WebChangeDepictEvent)event;	//get the web change event
			final Audio audio=getDepictedObject();	//get the depicted object
			if(webChangeEvent.getDepictedObject()!=audio)	//if the event was meant for another depicted object
			{
				throw new IllegalArgumentException("Depict event "+event+" meant for depicted object "+webChangeEvent.getDepictedObject());
			}
			final Map<String, Object> properties=webChangeEvent.getProperties();	//get the new properties
			final String stateString=asInstance(properties.get("state"), String.class);	//get the new state TODO use a constant
			if(stateString!=null)	//if a state was given
			{
				audio.setState(getSerializedEnum(TaskState.class, stateString));	//update the state of the audio
			}
			final Number position=asInstance(properties.get("position"), Number.class);	//get the position, if reported TODO use a constant
			if(position!=null)	//if we have a position
			{
				final Number duration=asInstance(properties.get("duration"), Number.class);	//get the duration, if reported
				audio.updateTimeProgress(position.longValue()*1000, duration!=null ? duration.longValue()*1000 : -1);	//update the audio position, converting from milliseconds to microseconds
			}			
		}
	}

}
