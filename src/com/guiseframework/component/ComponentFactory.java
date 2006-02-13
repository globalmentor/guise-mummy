package com.guiseframework.component;

/**Indicates a class that can provide a component to represent some value.
@param <V> The type of value for which components should be created.
@param <C> The type of component created for the value.
@author Garret Wilson
*/
public interface ComponentFactory<V, C extends Component<?>>
{

	/**Creates a component for the given value.
	@param value The value for which a component should be created.
	@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
	*/
	public C createComponent(final V value);

}
