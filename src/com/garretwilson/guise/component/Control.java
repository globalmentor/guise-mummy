package com.garretwilson.guise.component;

import static com.garretwilson.lang.ClassUtilities.*;
import com.garretwilson.guise.model.Model;

/**A component that accepts user interaction to manipulate a data model.
@author Garret Wilson
*/
public interface Control<M extends Model, C extends Control<M, C>> extends ModelComponent<M, C>
{

	/**The enabled bound property.*/
	public final static String ENABLED_PROPERTY=getPropertyName(Control.class, "enabled");

	/**@return Whether the control is enabled and can receive user input.*/
	public boolean isEnabled();

	/**Sets whether the control is enabled and can receive user input.
	This is a bound property of type <code>Boolean</code>.
	@param newEnabled <code>true</code> if the component should indicate and accept user input.
	@see #ENABLED_PROPERTY
	*/
	public void setEnabled(final boolean newEnabled);

}
