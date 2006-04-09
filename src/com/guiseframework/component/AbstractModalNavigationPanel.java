package com.guiseframework.component;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.component.layout.Layout;

/**Abstract implementation of a modal navigation panel.
@param <R> The type of modal result this modal navigation panel produces.
@author Garret Wilson
*/
public abstract class AbstractModalNavigationPanel<R, C extends ModalNavigationPanel<R, C>> extends AbstractNavigationPanel<C> implements ModalNavigationPanel<R, C>
{

	/**The result of this navigation panel's modal interaction, or <code>null</code> if no result is given.*/
	private R result=null;

		/**@return The result of this navigation panel's modal interaction, or <code>null</code> if no result is given.*/
		public R getResult() {return result;}

		/**Sets the modal result.
		This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
		@param newResult The new result of this navigation panel's modal interaction, or <code>null</code> if no result is given.
		@see ModalNavigationPanel#RESULT_PROPERTY
		*/
		public void setResult(final R newResult)
		{
			if(!ObjectUtilities.equals(result, newResult))	//if the value is really changing (compare their values, rather than identity)
			{
				final R oldResult=result;	//get the old value
				result=newResult;	//actually change the value
				firePropertyChange(RESULT_PROPERTY, oldResult, newResult);	//indicate that the value changed
			}
		}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout, is <code>null</code>.
	*/
	public AbstractModalNavigationPanel(final Layout layout)
	{
		super(layout);	//construct the parent class
	}
	/**Ends this navigation panel's modal interaction and navigates either to the previous modal navigation or to this navigation panel's referring URI, if any.
	@param result The result of this navigation panel's modal interaction, or <code>null</code> if no result is given.
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
