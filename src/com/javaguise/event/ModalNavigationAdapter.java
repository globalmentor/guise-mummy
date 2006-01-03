package com.javaguise.event;


/**An object that listens for the end of a modal frame's modality, providing default empty method definitions.
@author Garret Wilson
*/
public class ModalNavigationAdapter implements ModalNavigationListener
{

	/**Called when an a modal frame Begins its modality.
	@param modalEvent The event indicating the frame beginning modality and the modal value.
	*/
	public void modalBegan(final ModalEvent modalEvent)
	{
	}

	/**Called when an a modal frame ends its modality.
	@param modalEvent The event indicating the frame ending modality and the modal value.
	*/
	public void modalEnded(final ModalEvent modalEvent)
	{
	}

}
