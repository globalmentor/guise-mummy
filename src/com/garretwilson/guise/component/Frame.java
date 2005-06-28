package com.garretwilson.guise.component;

import com.garretwilson.guise.model.LabelModel;

/**A root-level container such as a window or an HTML page.
The title is specified by the frame model's label.
@author Garret Wilson
*/
public interface Frame<C extends Frame<C>> extends Box<C>, ModelComponent<LabelModel, C>
{
}
