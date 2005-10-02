package com.javaguise.event;

import com.javaguise.component.ModalNavigationPanel;

/**An object that listens for the end of a modal frame's modality.
@param <R> The type of modal result the modal frame produces.
@author Garret Wilson
*/
public interface ModalNavigationListener<R> extends GuiseEventListener<ModalNavigationPanel<R, ?>>
{

	/**Called when an a modal frame begins its modality.
	@param modalEvent The event indicating the frame beginning modality and the modal value.
	*/
	public void modalBegan(final ModalEvent<R> modalEvent);

	/**Called when an a modal frame ends its modality.
	@param modalEvent The event indicating the frame ending modality and the modal value.
	*/
	public void modalEnded(final ModalEvent<R> modalEvent);

}
