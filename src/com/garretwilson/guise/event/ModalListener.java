package com.garretwilson.guise.event;

import com.garretwilson.guise.component.ModalFrame;

/**An object that listens for the end of a modal frame's modality.
@param <R> The type of modal result the modal frame produces.
@author Garret Wilson
*/
public interface ModalListener<R> extends GuiseEventListener<ModalFrame<R, ?>>
{

	/**Called when an a modal frame ends its modality.
	@param modalEvent The event indicating the frame ending modality and the modal value.
	*/
	public void modalEnded(final ModalEvent<R> modalEvent);

}
