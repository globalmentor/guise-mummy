package com.guiseframework.model;

import java.beans.PropertyVetoException;

/**An object that contains a value.
@param <V> The type of value contained in the object.
@author Garret Wilson
*/
public interface Valued<V>
{

	/**@return The current value, or <code>null</code> if there is no value.*/
	public V getValue();

	/**Sets the new value.
	@param newValue The new value.
	@exception PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	*/
	public void setValue(final V newValue) throws PropertyVetoException;

}
