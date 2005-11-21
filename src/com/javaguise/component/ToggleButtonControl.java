package com.javaguise.component;

import com.javaguise.model.ActionValueModel;

/**A button that can be toggled between selected and unselected states, represented by <code>true</code> and <code>false</code>, respectively.
@author Garret Wilson
*/
public interface ToggleButtonControl<C extends ToggleButtonControl<C>> extends ButtonControl<C>
{
	/**@return The data model used by this component.*/
	public ActionValueModel<Boolean> getModel();

}
