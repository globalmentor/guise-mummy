package com.guiseframework.component;

import com.garretwilson.lang.Objects;

/**Abstract implementation of a modal frame.
@param <R> The type of modal result this modal frame produces.
@author Garret Wilson
*/
public abstract class AbstractModalFrame<R> extends AbstractFrame implements ModalFrame<R>
{

	/**The result of this frame's modal interaction, or <code>null</code> if no result is given.*/
	private R result=null;

		/**@return The result of this frame's modal interaction, or <code>null</code> if no result is given.*/
		public R getResult() {return result;}

		/**Sets the modal result.
		This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
		@param newResult The new result of this frame's modal interaction, or <code>null</code> if no result is given.
		@see ModalFrame#RESULT_PROPERTY
		*/
		public void setResult(final R newResult)
		{
			if(!Objects.equals(result, newResult))	//if the value is really changing (compare their values, rather than identity)
			{
				final R oldResult=result;	//get the old value
				result=newResult;	//actually change the value
				firePropertyChange(RESULT_PROPERTY, oldResult, newResult);	//indicate that the value changed
			}
		}

	/**Component constructor.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	*/
	public AbstractModalFrame(final Component component)
	{
		super(component);	//construct the parent class
	}

	/**Ends this frame's modal interaction and navigates either to the previous modal navigation or to this frame's referring URI, if any.
	@param result The result of this frame's modal interaction, or <code>null</code> if no result is given.
	@see #setResult(R)
	@see GuiseSession#endModalNavigation(ModalPanel)
	*/
	public void endModal(final R result)
	{
/*TODO fix
		setResult(result);	//update the result
		getSession().endModalNavigation(this);	//end modal navigation for this modal frame
*/
	}
}
