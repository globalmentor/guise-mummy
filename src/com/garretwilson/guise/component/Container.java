package com.garretwilson.guise.component;

/**Component that can contain other components.
A container is iterable, and can be used in short <code>for(:)</code> form. 
@author Garret Wilson
*/
public interface Container extends Component, Iterable<Component>
{

	/**Adds a component to the container
	@param component The component to add.
	*/
	public void add(final Component component);
}
