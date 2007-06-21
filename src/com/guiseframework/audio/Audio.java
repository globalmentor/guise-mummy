package com.guiseframework.audio;

import com.guiseframework.platform.AbstractDepictedObject;
import com.guiseframework.platform.DepictedObject;

/**Audio that can be played.
@author Garret Wilson
*/
public class Audio extends AbstractDepictedObject
{

	/**@return The depictor for this object.*/
	@SuppressWarnings("unchecked")
	public Depictor<? extends Audio> getDepictor() {return (Depictor<? extends Audio>)super.getDepictor();} 

	/**Requests that the audio start.
	If the audio is already started, no action occurs.
	*/
	public void start()
	{
		
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
//TODO fix		public void start();

		/**Requests that the audio pause.*/
//TODO fix		public void pause();

		/**Requests that the audio stop.*/
//TODO fix		public void stop();

	}
}
