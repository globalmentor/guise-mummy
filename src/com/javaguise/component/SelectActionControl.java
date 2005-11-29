package com.javaguise.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.javaguise.model.ActionValueModel;

/**An action control that keeps track of its selected state, represented by <code>true</code> and <code>false</code>, as the boolean value of its model.
If the control is set to be toggled, when the action is initiated the selected state alternates between <code>true</code> and <code>false</code>.
Otherwise, an action sets the selected state to <code>true</code>.
@author Garret Wilson
*/
public interface SelectActionControl<C extends SelectActionControl<C>> extends ActionValueControl<Boolean, C>
{
	/**The toggle bound property.*/
	public final static String TOGGLE_PROPERTY=getPropertyName(SelectActionControl.class, "toggle");
	
	/**@return The data model used by this component.*/
	public ActionValueModel<Boolean> getModel();

	/**@return Whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to <code>true</code>.*/
	public boolean isToggle();

	/**Sets whether this control acts as a toggle, switching its value between <code>true</code> and <code>false</code>, or whether the action always sets the value to <code>true</code>.
	This is a bound property of type <code>Boolean</code>.
	@param newToggle <code>true</code> if the component should act as a toggle, else <code>false</code> if the action should unconditionally set the value to <code>true</code>.
	@see #TOGGLE_PROPERTY
	*/
	public void setToggle(final boolean newToggle);

}
