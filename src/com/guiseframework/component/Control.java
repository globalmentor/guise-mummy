package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.guiseframework.model.Enableable;

/**A component that accepts user interaction to manipulate a data model.
@author Garret Wilson
*/
public interface Control<C extends Control<C>> extends Component<C>, Enableable
{

	/**The editable bound property.*/
	public final static String EDITABLE_PROPERTY=getPropertyName(Control.class, "editable");	//TODO decide if this should be moved down to ValueControl
}
