package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

/**A frame that supports modal interaction.
@param <R> The type of modal result this modal frame produces.
@author Garret Wilson
*/
public interface ModalFrame<R> extends Frame
{
	/**The modal state bound property.*/
	public final static String MODAL_PROPERTY=getPropertyName(ModalFrame.class, "modal");
	/**The result bound property.*/
	public final static String RESULT_PROPERTY=getPropertyName(ModalFrame.class, "result");

	/**@return The result of this frame's modal interaction, or <code>null</code> if no result is given.*/
	public R getResult();

	/**Sets the modal result.
	This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
	@param newResult The new result of this frame's modal interaction, or <code>null</code> if no result is given.
	@see #RESULT_PROPERTY
	*/
	public void setResult(final R newResult);

}
