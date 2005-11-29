package com.javaguise.component;

import com.javaguise.model.ActionValueModel;

/**An action control that also contains a value in its model.
@author Garret Wilson
@param <V> The type of value the action represents.
*/
public interface ActionValueControl<V, C extends ActionValueControl<V, C>> extends ActionControl<C>, ValueControl<V, C>
{
	/**@return The data model used by this component.*/
	public ActionValueModel<V> getModel();

}
