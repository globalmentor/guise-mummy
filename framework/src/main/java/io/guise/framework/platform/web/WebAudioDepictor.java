/*
 * Copyright © 2005-2012 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.platform.web;

import java.net.URI;
import java.util.Map;

import static com.globalmentor.java.Enums.*;
import static com.globalmentor.java.Objects.*;

import com.globalmentor.model.NameValuePair;
import com.globalmentor.model.TaskState;

import io.guise.framework.audio.Audio;
import io.guise.framework.platform.DepictEvent;
import io.guise.framework.platform.PlatformEvent;

/**
 * A web depictor for Guise audio.
 * @author Garret Wilson
 */
public class WebAudioDepictor extends AbstractWebDepictor<Audio> implements Audio.Depictor<Audio> {

	/** The web commands for controlling audio. */
	public enum AudioCommand implements WebPlatformCommand {

		/**
		 * The command to start the audio. Parameters:
		 * <ul>
		 * <li><code>{{@value #URI_PROPERTY}:"<var>uri</var>"}</code></li>
		 * <li><code>{{@value #TYPE_PROPERTY}:"<var>type</var>"}</code></li>
		 * </ul>
		 */
		AUDIO_PLAY,

		/** The command to pause the audio. */
		AUDIO_PAUSE,

		/** The command to stop the audio. */
		AUDIO_STOP,

		/**
		 * The command to set the position of the audio. parameters: <code>{{@value #POSITION_PROPERTY}:"<var>millisecondPosition</var>"}</code>
		 */
		AUDIO_POSITION;

		/** The property for specifying the URI of the audio. */
		public static final String URI_PROPERTY = "uri";
		/** The property for specifying the content type of the audio. */
		public static final String TYPE_PROPERTY = "type";
		/** The property for specifying the position of the audio. */
		public static final String POSITION_PROPERTY = "position";

	}

	@SuppressWarnings("unchecked")
	@Override
	public void play() {
		final Audio audio = getDepictedObject(); //get the audio depicted object
		final URI audioURI = audio.getAudioURI(); //get the audio URI
		if(audioURI != null) { //if there is an audio URI
			final URI resolvedAudioURI = getDepictedObject().getSession().resolveURI(audioURI); //resolve the audio URI
			getPlatform().getSendMessageQueue().add(
					new WebCommandDepictEvent<AudioCommand>(getDepictedObject(), AudioCommand.AUDIO_PLAY, new NameValuePair<String, Object>(AudioCommand.URI_PROPERTY,
							resolvedAudioURI), new NameValuePair<String, Object>(AudioCommand.TYPE_PROPERTY, audio.getAudioContentType()))); //send an audio start command to the platform
		}
	}

	@Override
	public void pause() {
		getPlatform().getSendMessageQueue().add(new WebCommandDepictEvent<AudioCommand>(getDepictedObject(), AudioCommand.AUDIO_PAUSE)); //send an audio pause command to the platform
	}

	@Override
	public void stop() {
		getPlatform().getSendMessageQueue().add(new WebCommandDepictEvent<AudioCommand>(getDepictedObject(), AudioCommand.AUDIO_STOP)); //send an audio stop command to the platform
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setTimePosition(final long newTimePosition) {
		if(newTimePosition < 0) { //if the new time position is negative
			throw new IllegalArgumentException("Time position cannot be negative: " + newTimePosition);
		}
		getPlatform().getSendMessageQueue().add(
				new WebCommandDepictEvent<AudioCommand>(getDepictedObject(), AudioCommand.AUDIO_POSITION, new NameValuePair<String, Object>(
						AudioCommand.POSITION_PROPERTY, Long.valueOf(newTimePosition / 1000)))); //send an audio position command to the platform, converting the time to milliseconds
	}

	@Override
	public void processEvent(final PlatformEvent event) {
		if(event instanceof WebChangeDepictEvent) { //if a property changed
			final WebChangeDepictEvent webChangeEvent = (WebChangeDepictEvent)event; //get the web change event
			final Audio audio = getDepictedObject(); //get the depicted object
			if(webChangeEvent.getDepictedObject() != audio) { //if the event was meant for another depicted object
				throw new IllegalArgumentException("Depict event " + event + " meant for depicted object " + webChangeEvent.getDepictedObject());
			}
			final Map<String, Object> properties = webChangeEvent.getProperties(); //get the new properties
			asInstance(properties.get("state"), String.class).map(stateString -> getSerializedEnum(TaskState.class, stateString)).ifPresent(audio::setState); //update the state of the audio if a state was given TODO use a constant
			final Number position = asInstance(properties.get("position"), Number.class).orElse(null); //get the position, if reported TODO use a constant
			if(position != null) { //if we have a position
				final Number duration = asInstance(properties.get("duration"), Number.class).orElse(null); //get the duration, if reported
				audio.updateTimeProgress(position.longValue() * 1000, duration != null ? duration.longValue() * 1000 : -1); //update the audio position, converting from milliseconds to microseconds
			}
		}
	}

}
