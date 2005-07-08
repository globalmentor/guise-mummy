package com.garretwilson.guise.component;

import com.garretwilson.guise.component.layout.Layout;

/**Component that allows for addition and removal of child components.
@author Garret Wilson
*/
public interface Container<C extends Container<C>> extends Component<C>
{

	/**Adds a component to the container
	@param component The component to add.
	@exception IllegalArgumentException if the component already has a parent.
	*/
	public void add(final Component<?> component);

	/**Removes a component from the container.
	@param component The component to remove.
	@exception IllegalArgumentException if the component is not a member of the container.
	*/
	public void remove(final Component<?> component);

	/**Determines whether this container contains the given component.
	@param component The component to check.
	@return <code>true</code> if this container contains the given component.
	*/
	public boolean contains(final Component<?> component);

	/**@return The layout definition for the container.*/
	public Layout getLayout();

}
