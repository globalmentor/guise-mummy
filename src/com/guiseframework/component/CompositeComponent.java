package com.guiseframework.component;

/**A component that can contain other components.
A composite component may contain other components, but only a {@link Container} allows for custom addition and removal of child components.
@author Garret Wilson
*/
public interface CompositeComponent<C extends CompositeComponent<C>> extends Component<C>
{

	/**@return Whether this component has children.*/
	public boolean hasChildren();

	/**@return An iterable to child components.*/
	public Iterable<Component<?>> getChildren();

	/**@return The number of child components in this composite component.*/
	public int size();

	/**Retrieves the child component with the given ID.
	@param id The ID of the component to return.
	@return The child component with the given ID, or <code>null</code> if there is no child component with the given ID. 
	*/
	public Component<?> getComponent(final String id);	//TODO perhaps remove; the speed may not be sufficient to outweigh the overhead; this is only a single-level search, anyway

	/**Retrieves the first component in the hierarchy with the given name.
	This method checks this component and all descendant components.
	@return The first component with the given name, or <code>null</code> if this component and all descendant components do not have the given name. 
	*/
	public Component<?> getComponentByName(final String name);

}
