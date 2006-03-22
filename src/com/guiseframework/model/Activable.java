package com.guiseframework.model;

import static com.garretwilson.lang.ClassUtilities.*;

/**An object that can be set active or inactive.
@author Garret Wilson
*/
public interface Activable
{

	/**The active bound property.*/
	public final static String ACTIVE_PROPERTY=getPropertyName(Activable.class, "active");

	/**@return Whether the object is active.*/
	public boolean isActive();

	/**Sets whether the object is active.
	This is a bound property of type <code>Boolean</code>.
	@param newActive <code>true</code> if the object should be active.
	@see #ACTIVE_PROPERTY
	*/
	public void setActive(final boolean newActive);
}