package com.javaguise.event;

import com.javaguise.component.ModalNavigationPanel;

/**An object that listens for the end of a modal frame's modality, providing default empty method definitions.
@param <S> The type of the event source.
@author Garret Wilson
*/
public class ModalNavigationAdapter<S extends ModalNavigationPanel<?, ?>> implements ModalNavigationListener<S>
{

	/**Called when an a modal frame Begins its modality.
	@param modalEvent The event indicating the frame beginning modality and the modal value.
	*/
	public void modalBegan(final ModalEvent<S> modalEvent)
	{
	}

	/**Called when an a modal frame ends its modality.
	@param modalEvent The event indicating the frame ending modality and the modal value.
	*/
	public void modalEnded(final ModalEvent<S> modalEvent)
	{
	}

}
