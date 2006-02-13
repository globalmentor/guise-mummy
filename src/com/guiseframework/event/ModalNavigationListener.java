package com.guiseframework.event;

/**An object that listens for a change in modal navigation modality.
@author Garret Wilson
*/
public interface ModalNavigationListener extends GuiseEventListener
{

	/**Called when an a modal frame begins its modality.
	@param modalEvent The event indicating the frame beginning modality and the modal value.
	*/
	public void modalBegan(final ModalEvent modalEvent);

	/**Called when an a modal frame ends its modality.
	@param modalEvent The event indicating the frame ending modality and the modal value.
	*/
	public void modalEnded(final ModalEvent modalEvent);

}
