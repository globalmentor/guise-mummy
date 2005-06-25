package com.garretwilson.guise.component;

import com.garretwilson.guise.component.layout.Layout;

/**Component that can contain other components.
A container is iterable, and can be used in short <code>for(:)</code> form. 
@author Garret Wilson
*/
public interface Container extends Component<Container>, Iterable<Component<?>>
{

	/**Adds a component to the container
	@param component The component to add.
	*/
	public void add(final Component<?> component);

	/**Determines whether this container contains the given component.
	@param component The component to check.
	@return <code>true</code> if this container contains the given component.
	*/
	public boolean contains(final Component<?> component);

	/**@return The layout definition for the container.*/
	public Layout getLayout();

	/**@return The character used by this container when building absolute IDs.*/
	public char getAbsoluteIDSegmentDelimiter();

	/**Determines the unique ID of the provided child component within this container.
	This method is typically called by child components when determining their own unique IDs.
	@param childComponent A component within this container.
	@return An identifier of the given component unique within this container.
	@exception IllegalArgumentException if the given component is not a child of this container.
	*/
	public String getUniqueID(final Component<?> childComponent);

	/**Determines the absolute unique ID of the provided child component up the component's hierarchy.
	This method is typically called by child components when determining their own absolute unique IDs.
	@param childComponent A component within this container.
	@return An absolute identifier of the given component unique up the component's hierarchy.
	@exception IllegalArgumentException if the given component is not a child of this container.
	*/
	public String getAbsoluteUniqueID(final Component<?> childComponent);

}
