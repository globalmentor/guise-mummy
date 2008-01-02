package com.guiseframework.component;

import static com.globalmentor.java.ClassUtilities.*;

/**A panel that supports modal navigation.
@param <R> The type of modal result this modal navigation panel produces.
@author Garret Wilson
*/
public interface ModalNavigationPanel<R> extends Panel
{
	/**The modal state bound property.*/
	public final static String MODAL_PROPERTY=getPropertyName(ModalNavigationPanel.class, "modal");
	/**The result bound property.*/
	public final static String RESULT_PROPERTY=getPropertyName(ModalNavigationPanel.class, "result");

	/**@return The result of this navigation panel's modal interaction, or <code>null</code> if no result is given.*/
	public R getResult();

	/**Sets the modal result.
	This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
	@param newResult The new result of this navigation panel's modal interaction, or <code>null</code> if no result is given.
	@see #RESULT_PROPERTY
	*/
	public void setResult(final R newResult);

}
