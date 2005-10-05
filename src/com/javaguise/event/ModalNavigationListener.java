package com.javaguise.event;

import com.javaguise.component.ModalNavigationPanel;

/**An object that listens for a change in modal navigation modality.
@param <S> The type of the event source.
@author Garret Wilson
*/
public interface ModalNavigationListener<S extends ModalNavigationPanel<?, ?>> extends GuiseEventListener<S>
{

	/**Called when an a modal frame begins its modality.
	@param modalEvent The event indicating the frame beginning modality and the modal value.
	*/
	public void modalBegan(final ModalEvent<S> modalEvent);

	/**Called when an a modal frame ends its modality.
	@param modalEvent The event indicating the frame ending modality and the modal value.
	*/
	public void modalEnded(final ModalEvent<S> modalEvent);

}
