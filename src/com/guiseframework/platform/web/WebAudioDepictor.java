package com.guiseframework.platform.web;

import java.net.URI;
import java.util.Map;

import static com.garretwilson.lang.EnumUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.util.*;

import com.guiseframework.audio.Audio;
import com.guiseframework.model.TaskState;
import com.guiseframework.platform.DepictEvent;
import com.guiseframework.platform.PlatformEvent;

/**A web depictor for Guise audio.
This depictor uses the following web command events:
<dl>
	<dt>audio-stop<dt> <dd>/dd>
	<dt>audio-pause<dt> <dd>/dd>
	<dt>audio-position<dt> <dd>{position:"<var>millisecondPosition</var>"}</dd>
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
		AUDIO_STOP,

		/**The command to set the position of the audio.*/
		AUDIO_POSITION;
	}

	/**The property for specifying the URI of the audio.*/
	public final static String AUDIO_URI_PROPERTY="audioURI";
	/**The property for specifying the position of the audio.*/
	public final static String POSITION_PROPERTY="position";

	/**Requests that the audio start.*/
	@SuppressWarnings("unchecked")
	public void start()
	{
		final Audio audio=getDepictedObject();	//get the audio depicted object
		final URI audioURI=audio.getAudioURI();	//get the audio URI
		if(audioURI!=null)	//if there is an audio URI
		{
			final URI resolvedAudioURI=getDepictedObject().getSession().resolveURI(audioURI);	//resolve the audio URI
			getPlatform().getSendEventQueue().offer(new WebCommandEvent<AudioCommand>(getDepictedObject(), AudioCommand.AUDIO_START, new NameValuePair<String, Object>(AUDIO_URI_PROPERTY, resolvedAudioURI)));	//send an audio start command to the platform
		}
	}

	/**Requests that the audio pause.*/
	public void pause()
	{
		getPlatform().getSendEventQueue().offer(new WebCommandEvent<AudioCommand>(getDepictedObject(), AudioCommand.AUDIO_PAUSE));	//send an audio pause command to the platform
	}

	/**Requests that the audio stop.*/
	public void stop()
	{
		getPlatform().getSendEventQueue().offer(new WebCommandEvent<AudioCommand>(getDepictedObject(), AudioCommand.AUDIO_STOP));	//send an audio stop command to the platform
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
		getPlatform().getSendEventQueue().offer(new WebCommandEvent<AudioCommand>(getDepictedObject(), AudioCommand.AUDIO_POSITION, new NameValuePair<String, Object>(POSITION_PROPERTY, Long.valueOf(newTimePosition/1000))));	//send an audio positoin command to the platform, converting the time to milliseconds
	}

	/**Processes an event from the platform.
	@param event The event to be processed.
	@exception IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	*/
	public void processEvent(final PlatformEvent event)
	{
		if(event instanceof WebChangeEvent)	//if a property changed
		{
			final WebChangeEvent webChangeEvent=(WebChangeEvent)event;	//get the web change event
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
			final Number position=asInstance(properties.get("position"), Number.class);	//get the position, if reported
			if(position!=null)	//if we have a position
			{
				final Number duration=asInstance(properties.get("duration"), Number.class);	//get the duration, if reported
				audio.updateTimeProgress(position.longValue()*1000, duration!=null ? duration.longValue()*1000 : -1);	//update the audio position, converting from milliseconds to microseconds
			}			
		}
	}

}
