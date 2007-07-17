import flash.external.ExternalInterface;
import flash.net.FileReference;
import flash.net.FileReferenceList;

/**Guise ActionScript for support functionality based in Flash.
Copyright (c) 2007 GlobalMentor, Inc.
@author Garret Wilson
<p>Sounds are given the following additional variables:
var id:String;	//the ID of the sound
var state:TaskState.X;	//the current playing state of the sound
var startPosition:Number;	//the position of the sound to use when playing is commenced
</p>
<p>File references are given the following additional variables:
var id:String;	//the ID of the file reference
var fileReferenceListID:String;	//the ID of the Guise depicted object that manages the file reference
</p>
*/
class com.guiseframework.platform.web.as.Guise
{

	/**The singleton instance of the Guise class.*/
	private static var guise:Guise;

	/**The name of the Guise variable in JavaScript.*/
	private static var GUISE_JAVSCRIPT_VARIABLE_NAME:String="guise";

	/**The delimiter for forming compound IDs.*/
	public static var ID_SEGMENT_DELIMITER:String=".";

	/**The counter used for creating unique IDs.*/
	private var nextIDNumber:Number=1;

		/**Generates an ID for an object.
		The ID is guaranteed to be unique within this instance of Guise Flash.
		*/ 
		private function generateID():String
		{
			return "id"+(nextIDNumber++).toString(16);	//return an ID including the hex representation of the counter and increment the counter TODO use Base64 when possible
		}

	/**The associate array of Sounds keyed to sound IDs.*/
	private var soundMap:Object={};

	/**The associate array of FileReferences (for single file selections) and FileReferenceLists keyed to IDs.*/
	private var fileReferenceMap:Object={};

	/**The state of a task.
	@see com.guiseframework.model.TaskState
	*/
	private var TaskState={INITIALIZE:"initialize", INCOMPLETE:"incomplete", ERROR:"error", PAUSED:"paused", STOPPED:"stopped", CANCELED:"canceled", COMPLETE:"complete"};
	
	/**The ID of the current timeout to fire a position event, or null if there is no current timeout.*/
	private var positionTimeoutID=null;

	/**Constructor.*/
	public function Guise()
	{
			//add callback methods for JavaScript to call
		ExternalInterface.addCallback("browseFiles", this, browseFiles);
		ExternalInterface.addCallback("pauseSound", this, pauseSound); 
		ExternalInterface.addCallback("playSound", this, playSound); 
		ExternalInterface.addCallback("setSoundPosition", this, setSoundPosition); 
		ExternalInterface.addCallback("stopSound", this, stopSound);
		ExternalInterface.addCallback("uploadFile", this, uploadFile);
	}

	/**Sets the state of a sound.
	The old and new states of the sound will be send back to the Guise JavaScript class.
	@param newState The new state of the sound.
	@see guise.onSoundStateChange()
	*/
	private function setSoundState(sound:Sound, newState:String)
	{
		var oldState:String=sound["state"];	//get the old state
		sound["state"]=newState;	//update the state of the sound
		ExternalInterface.call(GUISE_JAVSCRIPT_VARIABLE_NAME+"._onSoundStateChange", sound["id"], oldState, newState);	//send the sound's state back to Guise
	}

	/**Pauses sound with the given ID.
	If no such sound exists, or the sound is not playing, no action will occur.
	Sound.state will be set to TaskState.PAUSED.
	@param soundID The ID of the sound to pause.
	*/
	public function pauseSound(soundID:String):Void
	{
		var sound:Sound=soundMap[soundID];	//get the sound by its ID
		if(sound && sound["state"]==TaskState.INCOMPLETE)	//if there is such a sound and it is playing
		{
		  sound["startPosition"]=sound.position;	//save the sound position for when the sound is next started
			sound.stop();	//stop the sound
			setSoundState(sound, TaskState.PAUSED);	//indicate that the sound is paused
		}
	}

	/**Plays sound with the given ID loaded from the given URI.
	If the sound has not been created, it will be created and given the indicated ID.
	Sound.state will be set to TaskState.INCOMPLETE.
	@param soundID The ID of the sound to play.
	@param soundURI The URI of the sound to play.
	*/
	public function playSound(soundID:String, soundURI:String):Void
	{
		var sound:Sound=soundMap[soundID];	//get the existing sound, if any
		if(!sound)	//if there is no sound for that ID
		{
			sound=new Sound();	//create a new sound
			sound["id"]=soundID;	//save the sound ID
			sound["state"]=TaskState.STOPPED;	//indicate that the sound is not playing as an initialization value
		  sound["startPosition"]=0;	//indicate that the sound should begin playing at the beginning
		  sound.onSoundComplete=function(){this.setSoundState(sound, this.TaskState.STOPPED);}.bind(this);	//when the sound is complete, set the state to stopped
			sound.loadSound(soundURI, true);	//load the sound using streaming
			soundMap[soundID]=sound;	//store the sound in the map, keyed to the given ID
		}
		if(sound["state"]!=TaskState.INCOMPLETE)	//if the sound is not already playing
		{
			setSoundState(sound, TaskState.INCOMPLETE);	//indicate that the sound is playing
			sound.start(sound["startPosition"]/1000);	//start the sound where we left off
			scheduleSoundPosition();	//schedule a position update
		}
	}

	/**Sets the playback position of the given sound in milliseconds.
	If no such sound exists, no action will occur.
	@param soundID The ID of the sound the position of which to set.
	@param position The new position in milliseconds.
	*/
	public function setSoundPosition(soundID:String, position:Number):Void
	{
		var sound:Sound=soundMap[soundID];	//get the sound by its ID
		if(sound)	//if there is such a sound
		{
			switch(sound["state"])	//check the sound state
			{
				case TaskState.INCOMPLETE:	//if the sound is playing
					sound.start(position/1000);	//start the sound wherever it is TODO test
					break;
				case TaskState.PAUSED:	//if the sound is not playing
				case TaskState.STOPPED:	//if the sound is not playing
				  sound["startPosition"]=position;	//update the sound position for the next time we start it
				  break;
			}
		}
	}

	/**Stops sound with the given ID.
	If no such sound exists, or the sound is not playing or paused, no action will occur.
	Sound.state will be set to TaskState.STOPPED.
	@param soundID The ID of the sound to stop.
	*/
	public function stopSound(soundID:String):Void
	{
		var sound:Sound=soundMap[soundID];	//get the sound by its ID
		if(sound)	//if there is such a sound
		{
			switch(sound["state"])	//check the sound state
			{
				case TaskState.INCOMPLETE:	//if the sound is playing
					sound.stop();	//stop the sound
				case TaskState.PAUSED:	//if the sound is not playing
					setSoundState(sound, TaskState.STOPPED);	//indicate that we've stopped the sound
				  sound["startPosition"]=0;	//indicate that the sound should begin playing at the beginning if it is started again
				  break;
			}
		}
	}

	/**Schedules a sound position to be fired for all playing sounds.
	If a sound position event is already scheduled, no action occurs unless the event should be scheduled unconditionally.
	@param unconditionally Optional parameter indicating the event should be scheduled regardless of whether a timeout is currently scheduled.
	@see positionTimeoutID
	*/
	private function scheduleSoundPosition(unconditionally:Boolean):Void
	{
		if(unconditionally || positionTimeoutID==null)	//if there is currently no sound position event scheduled
		{ 
			positionTimeoutID=setTimeout(fireSoundPosition.bind(this), 1000);	//schedule firing of sound positions in the future
		}
	}

	/**Fires the position and duration of every playing sound.
	@see guise.onSoundPositionChange()
	*/
	private function fireSoundPosition():Void
	{
		var soundPlaying:Boolean=false;	//keep track of whether we found a sound playing
		for(var soundID in soundMap)	//for each sound ID
		{
			var sound:Sound=soundMap[soundID];	//get this sound
			if(sound["state"]==TaskState.INCOMPLETE)		//if the sound is playing
			{
				soundPlaying=true;	//indicate that we found a sound playing
				ExternalInterface.call(GUISE_JAVSCRIPT_VARIABLE_NAME+"._onSoundPositionChange", soundID, sound.position, sound.duration);	//send the sound's position and duration back to Guise
			}
		}
		if(soundPlaying)	//if at least one sound was playing
		{
			scheduleSoundPosition(true);	//schedule another event
		}
		else	//if no more sounds are playing
		{
			positionTimeoutID=null;	//indicate that no updates are scheduled TODO improve the race condition here if we can
		}
	}

	/**Browses for files.
	If the file reference or file reference list has not been created, it will be created and given the indicated ID.
	@param fileReferenceListID The ID of the file reference or file reference list.
	@param multiple Whether the user is allowed to select multiple files.
	*/
	public function browseFiles(fileReferenceListID:String, multiple:Boolean):Void
	{
		var object:Object=fileReferenceMap[fileReferenceListID];	//get the existing file reference or file reference list, if any
		if(!object)	//if there is no file reference for that ID
		{
			if(multiple)	//if we should allow multiple files to be selected
			{
				var fileReferenceList:FileReferenceList=new FileReferenceList();	//create the new file reference list
				fileReferenceList["id"]=fileReferenceListID;	//save the file reference ID
				var listener:Object=new Object();	//create a new listener
				listener.onSelect=onFileReferenceListSelect.bind(this);	//set the listener method for files selected
				fileReferenceList.addListener(listener);	//add the listener object
				fileReferenceMap[fileReferenceListID]=fileReferenceList;	//store the file reference list in the map, keyed to the given ID
				object=fileReferenceList;	//save the file reference list				
			}
			else	//if we should only allow one file to be selected
			{
				//TODO finish
			}
		}
		if(object instanceof FileReferenceList)	//if this is a file reference list
		{
			var fileReferenceList:FileReferenceList=FileReferenceList(object);	//get the file reference list
			fileReferenceList.browse();	//tell the file reference list to browse
		}
		//TODO fix for FileReference
	}

	/**Called when a file reference list successfully selects files from user input.
	This method creates a list of file information objects, assigning them unique IDs, and sends them back to Guise. 
	@param fileReferenceList The file reference list that selected files.
	*/ 
	private function onFileReferenceListSelect(fileReferenceList:FileReferenceList)
	{
		var fileList:Array=fileReferenceList.fileList;	//get the list of file references from the file reference list
		var fileMap:Object=new Object();	//create an associated array for quickly looking up file references in the future
		var fileReferenceCount:Number=fileList.length;	//find out how many file references there are
		var fileReferenceInfos=new Array(fileReferenceCount);	//create a new array to store only the file references info we want to send back
		for(var i:Number=0; i<fileReferenceCount; ++i)	//for each file reference
		{
			var fileReference:FileReference=fileList[i];	//get this file reference
			var fileReferenceID:String=generateID();	//generate an ID for this file reference
			fileReference["id"]=fileReferenceID;	//assign the ID to this file reference so that we can recognize it later
			fileReference["fileReferenceListID"]=fileReferenceList["id"];	//save a reference to the file reference list ID
			var listener:Object=new Object();	//create a new listener
			listener.onProgress=onFileReferenceProgress.bind(this);	//set the listener method for file progress
			fileReference.addListener(listener);	//add the listener object
			fileMap[fileReferenceID]=fileReference;	//store the file reference in the map for quick lookup
			fileReferenceInfos[i]={id:fileReferenceID, name:fileReference.name, size:fileReference.size};	//create our own information about this file reference
		}
		fileReferenceList["fileMap"]=fileMap;	//associate the file map with the file reference list, replacing any map of files from previous selections, if any
		ExternalInterface.call(GUISE_JAVSCRIPT_VARIABLE_NAME+"._onFilesSelected", fileReferenceList["id"], fileReferenceInfos);	//send information on the selected files back to Guise
	}

	/**Uploads a file
	If no such file reference or file reference list exists, or no files of a file reference list match that indicated by the given file ID, no action occurs.
	@param fileReferenceListID The ID of the file reference or file reference list.
	@param fileReferenceID The ID of the file reference within a file reference list, or null if the file reference list ID refers to a single file reference.
	@param fileURI The URI to which the file should be uploaded.
	*/
	public function uploadFile(fileReferenceListID:String, fileReferenceID:Number, fileURI:String):Void
	{
		var object:Object=fileReferenceMap[fileReferenceListID];	//get the existing file reference or file reference list, if any
		if(object instanceof FileReferenceList)	//if this is a file reference list
		{
			var fileReferenceList:FileReferenceList=FileReferenceList(object);	//get the file reference list
			var fileReference=fileReferenceList["fileMap"][fileReferenceID];	//get the requested file reference
			if(fileReference)	//if we found a file reference
			{
				fileReference.upload(fileURI);	//tell the file reference to start uploading
			}
		}
		//TODO fix for FileReference
	}

	/**Called when a file makes progress being uploaded or downloaded.
	@param fileReference The reference to the file being uploaded or downloaded.
	@param bytesLoaded The number of bytes transferred, or -1 if unknown.
	@param bytesTotal The total number of bytes to transfer, or -1 if unknown.
	*/ 
	private function onFileReferenceProgress(fileReference:FileReference, bytesLoaded:Number, bytesTotal:Number)
	{
		ExternalInterface.call(GUISE_JAVSCRIPT_VARIABLE_NAME+"._onFileProgress", fileReference["fileReferenceListID"], fileReference["id"], bytesLoaded, bytesTotal);	//send the progress to Guise
	}

	/**Main entry point.*/
	static function main(mc)
	{
		/**Returns an array representing the contents of the given object.
		This implementation recognizes other arrays and the arguments of a function;
		along with anything else that is iterable by virtue of having a length property and a [] access method.
		@param object The non-null object the contents of which to return as an array.
		@return An array containing the contents of the given object.
		@see http://www.prototypejs.org/api/array/from
		*/
		Array.from=function(object)
		{
			if(object instanceof Array)	//if the object is an array
			{
				return object;
			}
			else	//otherwise, try to iterate using length and []
			{
				var array=new Array();	//create a new array
				for(var i=0, length=object.length; i<length; ++i)	//for each element
				{
					array.add(object[i]);	//add this element to our array
				}
				return array;	//return the new array we created
			}
		};

		/**Creates a new function that functions exactly as does the original function,
		except that it provides the given variable to appear as "this" to the new function.
		Any other given arguments will be inserted before the actual arguments when the function is invoked.
		@param newThis The variable to appear as "this" when the function is called.
		@param extraArguments The new arguments, if any, to appear at the first of the arguments when the new function is called.
		@return A new function bound to the given this.
		@see http://www.prototypejs.org/api/function/bind
		*/
		Function.prototype.bind=function()
		{
			var originalFunction=this;	//save a reference to this function instance to allow calling this via closure
			var extraArguments=Array.from(arguments);	//get the provided arguments
			var newThis=extraArguments.shift();	//get the first argument, which provides the new this when calling the function, and leaving the remaining arguments to be passed to the function
			return function()	//create and send back a new function
			{
				originalFunction.apply(newThis, extraArguments.length!=0 ? extraArguments.concat(Array.from(arguments)) : arguments);	//the new function will call the original function with the new arguments followed by whatever arguments are given, but using the given this instead of whatever this is passed when the function is called
			};
		};
		guise=new Guise();	//create the single application instance
	}

}