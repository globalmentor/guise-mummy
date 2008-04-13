package com.guiseframework.component;

import static com.globalmentor.java.Classes.*;

import com.guiseframework.model.ActionModel;

/**A general control with an action model.
@author Garret Wilson
*/
public interface ActionControl extends Control, ActionModel
{

	/**The bound property of the rollover state.*/
	public final static String ROLLOVER_PROPERTY=getPropertyName(ActionControl.class, "rollover");

	/**@return Whether the component is in a rollover state.*/
	public boolean isRollover();

	/**Sets whether the component is in a rollover state.
	This is a bound property of type <code>Boolean</code>.
	@param newRollover <code>true</code> if the component should be in a rollover state, else <code>false</code>.
	@see #ROLLOVER_PROPERTY
	*/
	public void setRollover(final boolean newRollover);

}
