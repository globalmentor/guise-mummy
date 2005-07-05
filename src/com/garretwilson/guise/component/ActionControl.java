package com.garretwilson.guise.component;

import com.garretwilson.guise.model.ActionModel;

/**A control with an action model.
@author Garret Wilson
*/
public interface ActionControl<C extends ActionControl<C>> extends Control<ActionModel, C>
{
}
