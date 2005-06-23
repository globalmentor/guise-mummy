package com.garretwilson.guise.component;

import com.garretwilson.guise.component.layout.Layout;

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

	/**Determines whether this container contains the given component.
	@param component The component to check.
	@return <code>true</code> if this container contains the given component.
	*/
	public boolean contains(final Component component);

	/**@return The layout definition for the container.*/
	public Layout getLayout();

}
