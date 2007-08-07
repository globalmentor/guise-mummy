package com.guiseframework.component;

import java.beans.PropertyVetoException;

import com.guiseframework.model.Valued;

/**A component that can be initialized with a value and allows a value to be retrieved.
This component is useful for editing complex values (such as contact information).
@param <V> The type of value displayed within the component.
@author Garret Wilson
*/
public interface ValuedComponent<V> extends Component, Valued<V>
{

	/**@return The current value edited in the component, or <code>null</code> if there is no value.*/
	public V getValue();

	/**Sets the new value to be edited in the component.
	@param newValue The new value.
	@exception PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	*/
	public void setValue(final V newValue) throws PropertyVetoException;

}
