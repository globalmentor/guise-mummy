/* Guise™ ActionScript Library
 * Copyright © 2007-2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.platform.web
{

import flash.display.Sprite; 
import flash.external.ExternalInterface;
import flash.events.Event;
import flash.media.*;
import flash.net.*;
import flash.utils.*;
import org.as3wavsound.WavSound;  
import org.as3wavsound.WavSoundChannel;

/**
 * Guise ActionScript for support functionality based in Flash 10.
 * <p>
 * <code>audio/wav</code> support is added using AS3WavSound, with supported sample rates of 44100Hz, 22050Hz, or
 * 11025Hz, and bitrates of 8 or 16, are supported. Sounds are given the following additional properties:
 * </p>
 * <dl>
 * <dt>id:String</dt>
 * <dd>The ID of the sound.</dd>
 * <dt>state:TaskState</dt>
 * <dd>The current playing state of the sound.</dd>
 * <dt>startPosition:Number</dt>
 * <dd>The position of the sound to use when playing is commenced.</dd>
 * </dl>
 * <p>
 * This class requires the Guise.js runtime on the host browser, with the following callback methods defined:
 * </p>
 * <ul>
 * <li><code>guise._onFlashInitialize()</code></li>
 * <li><code>guise._onSoundStateChange()</code></li>
 * <li><code>guise._onSoundPositionChange()</code></li>
 * </ul>
 * <p>
 * Dependencies
 * </p>
 * <ul>
 * <li>AS3WavSound </li>
 * </ul>
 * @author Garret Wilson
 * @see <a href="code.google.com/p/as3wavsound/">AS3WavSound</a>
 */
public final class Guise extends Sprite 
{

	/** The associate array of SoundChannelInfos keyed to sound IDs. */
	private var soundInfoMap:Object={};

	/**
	 * The state of a task.
	 * @see com.globalmentor.model.TaskState
	 */
	private var TaskState:Object={INITIALIZE:"initialize", INCOMPLETE:"incomplete", ERROR:"error", PAUSED:"paused", STOPPED:"stopped", CANCELED:"canceled", COMPLETE:"complete"};
	
	/** The ID of the current timeout to fire a position event, or null if there is no current timeout. */
	private var positionTimeoutID:String=null;

	/** Constructor. */
	public function Guise()
	{
			//add callback methods for JavaScript to call
    ExternalInterface.marshallExceptions = true;		
		ExternalInterface.addCallback("pauseSound", pauseSound); 
		ExternalInterface.addCallback("playSound", playSound); 
		ExternalInterface.addCallback("setSoundPosition", setSoundPosition); 
		ExternalInterface.addCallback("stopSound", stopSound);
		ExternalInterface.call("guise._onFlashInitialize");	//indicate back to Guise that Flash support is initialized
	}

	/**
	 * Plays sound with the given ID loaded from the given URI. If the sound has not been created, it will be created and
	 * given the indicated ID. Sound.state will be set to TaskState.INCOMPLETE.
	 * <p>
	 * If no ID is provided, the sound is played with no playback control and discarded.
	 * </p>
	 * <p>
	 * Playback control of WAV files is currently not supported and any ID will be ignored.
	 * </p>
	 * @param soundURI The URI of the sound to play.
	 * @param soundID The ID of the sound to play, or <code>null</code> if the sound should be played with no playback
	 *          control and discarded.
	 */
	public function playSound(soundURI:String, soundID:String):void
	{
		var sound:Sound;
		var urlRequest:URLRequest = new URLRequest(soundURI);	//create a means of accessing the sound
		if(/\.wav$/.test(soundURI))	//perform special handling for WAV files
		{
			var urlLoader:URLLoader = new URLLoader();	//create a new loader  
			urlLoader.dataFormat = "binary";	//we want to load binary data
			urlLoader.addEventListener(Event.COMPLETE, onWAVLoaded);	//listen for the data being loaded
			urlLoader.load(urlRequest);	//load the data
			return;	//don't play the sound natively
		}
		if(soundID)	//if a sound ID was given
		{
			var soundInfo:SoundInfo=soundInfoMap[soundID];	//get the existing sound, if any
			if(!soundInfo)	//if there is no sound for that ID
			{
				sound=new Sound();	//create a new sound
			  var context:SoundLoaderContext = new SoundLoaderContext(8000);	//stream the file
			  sound.load(urlRequest, context);
				soundInfo=new SoundInfo(sound, soundID);	//create info for the sound state
				soundInfoMap[soundID]=soundInfo;	//store the sound info in the map, keyed to the given ID
			}
			if(soundInfo.state!=TaskState.INCOMPLETE)	//if the sound is not already playing
			{
				soundInfo.play();	//tell the sound to start or continue playing
				scheduleSoundPosition();	//schedule a position update
			}
		}
		else	//if no sound ID was given
		{
			sound=new Sound();	//create a new sound
			sound.load(urlRequest);	//load the sound
			sound.play();	//play the sound
		}
	}

	/**
	 * Internal callback method indicating that a WAV file was loaded.
	 * @param event The callback event.
	 */
	private final function onWAVLoaded(event:Event):void  
	{  
		var wavSound:WavSound = new WavSound(event.target.data as ByteArray);	//create a WAV sound from the binary data  
		wavSound.play();	//play the sound
	}	
	
	/**
	 * Pauses sound with the given ID. If no such sound exists, or the sound is not playing, no action will occur.
	 * Sound.state will be set to TaskState.PAUSED.
	 * @param soundID The ID of the sound to pause.
	 */
	public function pauseSound(soundID:String):void
	{
		var soundInfo:SoundInfo=soundInfoMap[soundID];	//get the sound by its ID
		if(soundInfo)	//if there is such a sound
		{
			soundInfo.pause();	//pause the sound
		}
	}
	
	/**
	 * Sets the playback position of the given sound in milliseconds. If no such sound exists, no action will occur.
	 * @param soundID The ID of the sound the position of which to set.
	 * @param position The new position in milliseconds.
	 */
	public function setSoundPosition(soundID:String, position:Number):void
	{
		var soundInfo:SoundInfo=soundInfoMap[soundID];	//get the sound by its ID
		if(soundInfo)	//if there is such a sound
		{
			soundInfo.position=position;	//update the sound's position
			scheduleSoundPosition(true);	//schedule a position immediately
		}
	}

	/**
	 * Stops the sound with the given ID. If no such sound exists, or the sound is not playing or paused, no action will
	 * occur. Sound.state will be set to TaskState.STOPPED.
	 * @param soundID The ID of the sound to stop.
	 */
	public function stopSound(soundID:String):void
	{
		var soundInfo:SoundInfo=soundInfoMap[soundID];	//get the sound by its ID
		if(soundInfo)	//if there is such a sound
		{
			soundInfo.stop();	//update the sound's position
		}
	}

	/**
	 * Schedules a sound position to be fired for all playing sounds. If a sound position event is already scheduled, no
	 * action occurs unless the update is to be scheduled immediately.
	 * @param immediately Optional parameter indicating the event should be scheduled immediately
	 * @see positionTimeoutID
	 */
	private function scheduleSoundPosition(immediately:Boolean=false):void
	{
		if(immediately || !positionTimeoutID)	//if there is currently no sound position event scheduled
		{
			if(positionTimeoutID)	//if we have a timeout running
			{
				clearTimeout(positionTimeoutID);	//stop that timeout
				positionTimeoutID=null;	//we'll set this again soon; clearing it here is just for semantic completeness
			}
			var delay:Number=immediately ? 1 : 1000;	//if we aren't scheduling immediately, wait a second
			positionTimeoutID=setTimeout(fireSoundPosition, delay);	//schedule firing of sound positions in the future
		}
	}

	/**
	 * Fires the position and duration of every playing sound.
	 * @see guise._onSoundPositionChange()
	 */
	private function fireSoundPosition():void
	{
		clearTimeout(positionTimeoutID);	//make sure the timeout is cleared
		positionTimeoutID=null;	//indicate that no updates are scheduled now
		var soundPlaying:Boolean=false;	//keep track of whether we found a sound playing
		for(var soundID:String in soundInfoMap)	//for each sound info ID
		{
			var soundInfo:SoundInfo=soundInfoMap[soundID];	//get this sound info
			if(soundInfo.state==TaskState.INCOMPLETE)		//if the sound is playing
			{
				soundPlaying=true;	//indicate that we found a sound playing
				ExternalInterface.call("guise._onSoundPositionChange", soundID, soundInfo.position, soundInfo.duration);	//send the sound's position and duration back to Guise
			}
		}
		if(soundPlaying)	//if at least one sound was playing
		{
			scheduleSoundPosition();	//schedule another event
		}
	}
}

}

import flash.events.*;
import flash.external.ExternalInterface;
import flash.media.*;

/**
 * Information on a sound, including its ID, and state.
 * @author Garret Wilson
 */
class SoundInfo
{
	
	/** The sound being played. */
	private var _sound:Sound;
	
	/** The current sound channel, or <code>null</code> if the sound isn't playing. */
	private var _channel:SoundChannel;

	/** The ID of the sound to identify the sound in the outside world. */
	private var _id:String;
	
	/** The current state of the sound. */
	private var _state:String;

	/** The position at which to start the sound, or -1 if the sound has never been played. */
	private var _startPosition:Number;

	/**
	 * Constructor.
	 * @param sound The sound to play.
	 * @param id The ID to identify the sound in the outside world.
	 */
	function SoundInfo(sound:Sound, id:String)
	{
		_sound=sound;
		_channel=null;	//we haven't started playing the file, yet
		_id=id;
		_state="stopped";	//TODO use constant
		_startPosition=-1;
	}

  public function get id():String
  { 
      return _id; 
  } 

  public function get state():String
  { 
      return _state;
  }
  
	/**
	 * Sets the state of a sound. The old and new states of the sound will be sent back to the Guise JavaScript class.
	 * @param newState The new state of the sound.
	 * @see guise._onSoundStateChange()
	 */
  public function set state(newState:String):void 
  {
  	var oldState:String=_state;	//get the old state
  	if(oldState!=newState)	//if the state is really changing
  	{
			_state=newState;	//update the state of the sound
  		ExternalInterface.call("guise._onSoundStateChange", _id, oldState, newState);	//send the sound's state back to Guise
  	}
  }
  
  /** Plays a file for the first time or from where it left off the last time. */
  public function play():void
  {
  	if(_startPosition>=0)	//if we've already played the file
  	{
  		if(state!="incomplete")	//if the sound is not already playing
  		{
  			state="incomplete";	//indicate that the sound is playing
  			_channel=_sound.play(_startPosition);	//play the sound where it left off
  			_channel.addEventListener(Event.SOUND_COMPLETE, onSoundComplete);
  		}
  	}
  	//if we haven't started playing the file
  	else 
  	{
			state="incomplete";	//indicate that the sound is playing
  		_startPosition=0;
  	  _channel=_sound.play();	//play the sound and gets its channel
			_channel.addEventListener(Event.SOUND_COMPLETE, onSoundComplete);
  	}
	}

  /**
	 * Internal callback to indicate that a sound has completed.
	 * @param event The event describing the ended sound.
	 */
  private function onSoundComplete(event:Event):void
  {
		_channel.removeEventListener(Event.SOUND_COMPLETE, onSoundComplete);
		_channel=null;	//release our channel
		_startPosition=0;	//indicate that the sound should begin playing at the beginning if it is started again
    state="stopped";
  }	
  
	/** Pauses sound with the given ID. Sound.state will be set to TaskState.PAUSED. */
  public function pause():void
  {
		if(state=="incomplete")	//if there is such a sound and it is playing
		{
		  _startPosition=_channel.position;	//save the sound position for when the sound is next started
			_channel.stop();	//stop the channel
			_channel.removeEventListener(Event.SOUND_COMPLETE, onSoundComplete);
			_channel=null;	//release our channel
			state="paused";	//indicate that the sound is paused
		}
  }

	/** Stops the sound. Sound.state will be set to TaskState.STOPPED. */
  public function stop():void
  {
		switch(state)	//check the sound state
		{
			case "incomplete":	//if the sound is playing
				_channel.stop();	//stop the channel
				_channel.removeEventListener(Event.SOUND_COMPLETE, onSoundComplete);
				_channel=null;	//release our channel
			//fall through and perform "paused" functionality as well
			case "paused":	//if the sound is not playing
				state="stopped";	//indicate that we've stopped the sound
				_startPosition=0;	//indicate that the sound should begin playing at the beginning if it is started again TODO consider using _startPosition<0 to indicate stopped
			  break;
		}
	}

  /**
	 * Determines the duration of the sound. This method attempts to estimate the duration if the sound is not yet fully
	 * loaded.
	 * @return The best estimate of the duration of the sound.
	 * @see <a href="http://www.stevensacks.net/2008/12/02/calculating-duration-on-mp3s-in-as3/">Calculating duration on
	 *      mp3s in AS3</a>
	 */
  public function get duration():Number
  {
  	if(_sound.bytesTotal==0 || _sound.bytesLoaded==_sound.length)	//sometimes bytesTotal never changes from zero; see http://stackoverflow.com/questions/3537038/as3-preloading-issue
  	{
  		return _sound.length;
  	}
  	else	//if the sound isn't yet loaded
  	{
  		return (_sound.bytesTotal / (_sound.bytesLoaded / _sound.length));	//calculate the duration; see http://www.stevensacks.net/2008/12/02/calculating-duration-on-mp3s-in-as3/
  	}
  }

  /**
	 * Returns the current position of the sound. If the sound is stopped, the position at which the sound will next start
	 * is returned.
	 * @return The current position of the sound.
	 */
  public function get position():Number
  {
  	if(_channel)	//if we have a channel
  	{
  		return _channel.position;	//return the channel's position
  	}
  	else	//if we have no channel
  	{
  		return _startPosition;	//return the position the sound will next start
  	}
  }

	/**
	 * Sets the playback position of the given sound in milliseconds.
	 * @param newPosition The new position in milliseconds.
	 */
  public function set position(newPosition:Number):void
  {
  	if(state=="incomplete")	//if we are in the middle of playing
  	{
			_channel.stop();	//stop the channel
			_channel=_sound.play(newPosition);	//start playing again at the new position  			
			_channel.addEventListener(Event.SOUND_COMPLETE, onSoundComplete);
  	}
  	else	//if we are paused or stopped
  	{
    	_startPosition=newPosition;	//update the starting position;
  	}
  }
  
}