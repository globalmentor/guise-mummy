package com.guiseframework.component;

/**Abstract implementation of a composite component that keeps track of its child components at specific indices in an array.
The array is indexed using the provided enum.
Child components should not directly call {@link #addComponent(Component)} and {@link #removeComponent(Component)}.
Each index in the array can be <code>null</code>.
Iterating over child components is thread safe.
<p>This class should be constructed using <code>Enum.values()</code> to indicate the possible enum values.</p>
@param <E> The enum to index child components. 
@author Garret Wilson
*/
public abstract class AbstractEnumCompositeComponent<E extends Enum<E>, C extends CompositeComponent<C>> extends AbstractArrayCompositeComponent<C>
{

  /**Returns the component for the given enum value.
  @param e The enum value indicating the component to return.
	@return The component associated with the given enum value.
	*/
	protected Component<?> getComponent(final E e)
	{
		return super.getComponent(e.ordinal());	//look up the component in the array
	}
		
  /**Sets the component for the given enum value.
  If the new component is the same as the old, no action is taken.
  This implementation calls {@link #addComponent(Component)} and {@link #removeComponent(Component)} as necessary.
  @param e The enum value indicating the component to set.
  @param newComponent The component to associate with the enum value.
  @return The component previously associated for the enum value.
	@see #addComponent(Component)
	@see #removeComponent(Component)
	*/
	protected Component<?> setComponent(final E e, final Component<?> newComponent)
	{
		return super.setComponent(e.ordinal(), newComponent);	//look up the component in the array
	}
		
	/**Enum values constructor.
	@param enumValues The values of enums that allow access to the underlying array.
	@exception NullPointerException if the given enum values array is <code>null</code>.
	*/
	public AbstractEnumCompositeComponent(final E[] enumValues)
	{
		super(enumValues.length);	//construct the parent class
	}

}
