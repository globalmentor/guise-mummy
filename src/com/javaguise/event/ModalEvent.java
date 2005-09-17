package com.javaguise.event;

import com.javaguise.component.ModalFrame;
import com.javaguise.session.GuiseSession;

/**An event indicating that a modal frame ended its modalitay.
@param <R> The type of modal result the modal frame produces.
author Garret Wilson
*/
public class ModalEvent<R> extends GuiseEvent<ModalFrame<R, ?>>
{

	/**The result of this frame's modal interaction, or <code>null</code> if no result is given.*/
	private final R result;

		/**@return The result of this frame's modal interaction, or <code>null</code> if no result is given.*/
		public R getResult() {return result;}

	/**Session and source constructor.
	@param session The Guise session in which this event was generated.
	@param source The modal frame ending modality.
	@param result The result of this frame's modal interaction, or <code>null</code> if no result is given.
	@exception NullPointerException if the given session and/or source is <code>null</code>.
	*/
	public ModalEvent(final GuiseSession session, final ModalFrame<R, ?> source, final R result)
	{
		super(session, source);	//construct the parent class
		this.result=result;	//save the result
	}

}
