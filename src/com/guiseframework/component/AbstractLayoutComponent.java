package com.guiseframework.component;

import com.guiseframework.component.layout.*;

import static com.garretwilson.lang.ObjectUtilities.*;

/**Abstract implementation of a layout component.
Iterating over child components is thread safe.
@author Garret Wilson
*/
public abstract class AbstractLayoutComponent<C extends LayoutComponent<C>> extends AbstractListCompositeComponent<C> implements LayoutComponent<C>
{

	/**Adds a component to the layout component with default constraints.
	@param component The component to add.
	@return <code>true</code> if the child components changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception IllegalStateException if the installed layout does not support default constraints.
	*/
	protected boolean addComponent(final Component<?> component)
	{
//TODO del when works		return add(component, null);	//add the component, indicating default constraints should be used
		if(super.addComponent(component))	//add the component normally; if the child components changed
		{
			getLayout().addComponent(component);	//add the component to the layout
	//TODO fix		fireContainerModified(indexOf(component), component, null);	//indicate the component was added at the index
			return true;	//indicate that the child components changed
		}
		else	//if the component list did not change
		{
			return false;	//indicate that the chidl components did not change
		}
	}

	/**Adds a component to the container along with constraints.
	This is a convenience method that first set the constraints of the component. 
	@param component The component to add.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@return <code>true</code> if this container changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	*/
/*TODO del if not needed
	protected boolean add(final Component<?> component, final Constraints constraints)
	{
		component.setConstraints(constraints);	//set the constraints in the component
		return add(component);	//add the component, now that its constraints have been set
	}
*/

	/**Removes a component from the layout component.
	@param component The component to remove.
	@return <code>true</code> if the child components changed as a result of the operation.
	@exception IllegalArgumentException if the component is not a member of this composite component.
	*/
	protected boolean removeComponent(final Component<?> component)
	{
		final int index=indexOf(component);	//get the index of the component
		if(component.getParent()!=this || index<0)	//if this component is not a member of this container TODO maybe make a better check than indexOf(), like hasComponent(); index may be needed eventually, though, if container events get promoted to composite componente events
		{
			throw new IllegalArgumentException("Component "+component+" is not member of composite component "+this+".");
		}
		getLayout().removeComponent(component);	//remove the component from the layout
//TODO del when works		getLayout().removeConstraints(component);	//remove the constraints for this component
		super.removeComponent(component);	//do the default removal
//TODO move to here when we promote this event type		fireContainerModified(index, null, component);	//indicate the component was removed from the index
		return true;	//indicate that the child components changed
	}

	/**Removes the child component at the specified position in this container.
	@param index The index of the component to removed.
	@return The value previously at the specified position.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
	*/
/*TODO move elsewhere
	protected Component<?> remove(final int index)
	{
		final Component<?> component=get(index);	//get the component at this index
		remove(component);	//remove the component
		return component;	//return the component that was removed
	}
*/

	/**Removes all of the components from this container.*/
/*TODO move elsewhere
	protected void clear()
	{
		for(final Component<?> component:getChildren())	//for each component in the container
		{
			remove(component);	//remove this component
		}
	}
*/

	/**The layout definition for the component.*/
	private Layout<?> layout;

		/**@return The layout definition for the component.*/
		public Layout<?> getLayout() {return layout;}

		/**Sets the layout definition for the component.
		This is a bound property.
		@param newLayout The new layout definition for the container.
		@exception NullPointerException if the given layout is <code>null</code>.
		@see #LAYOUT_PROPERTY 
		*/
		protected <T extends Constraints> void setLayout(final Layout<T> newLayout)
		{
			if(layout!=checkInstance(newLayout, "Layout cannot be null."))	//if the value is really changing
			{
				final Layout<?> oldLayout=layout;	//get the old value
				oldLayout.setOwner(null);	//tell the old layout it is no longer installed
				layout=newLayout;	//actually change the value
				layout.setOwner(this);	//tell the new layout which container owns it
				for(final Component<?> childComponent:getChildren())	//for each child component
				{
					newLayout.getConstraints(childComponent);	//make sure the constraints of all components are compatible with the layout TODO do we even need to do this? why not wait until later? but this may be OK---perhaps we can assume that if components are installed before the layout, they will be used with this layout and not another
				}
				firePropertyChange(LAYOUT_PROPERTY, oldLayout, newLayout);	//indicate that the value changed
			}			
		}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public AbstractLayoutComponent(final Layout<?> layout)
	{
		this.layout=checkInstance(layout, "Layout cannot be null.");	//save the layout
		layout.setOwner(this);	//tell the layout which container owns it
	}

}
