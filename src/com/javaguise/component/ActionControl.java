package com.javaguise.component;

import com.javaguise.model.ActionModel;

/**A control with an action model.
@author Garret Wilson
*/
public interface ActionControl<C extends ActionControl<C>> extends Control<ActionModel, C>
{
}
