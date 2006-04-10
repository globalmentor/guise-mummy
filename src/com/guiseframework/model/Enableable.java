package com.guiseframework.model;

import static com.garretwilson.lang.ClassUtilities.*;

import com.garretwilson.beans.PropertyBindable;

/**An object that can be enabled or disabled.
@author Garret Wilson
*/
public interface Enableable extends PropertyBindable
{

	/**The enabled bound property.*/
	public final static String ENABLED_PROPERTY=getPropertyName(Enableable.class, "enabled");

	/**@return Whether the object is enabled and can receive user input.*/
	public boolean isEnabled();

	/**Sets whether the object is enabled and can receive user input.
	This is a bound property of type <code>Boolean</code>.
	@param newEnabled <code>true</code> if the object should indicate and accept user input.
	@see #ENABLED_PROPERTY
	*/
	public void setEnabled(final boolean newEnabled);
}
