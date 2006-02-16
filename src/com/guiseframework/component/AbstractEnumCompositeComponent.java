package com.guiseframework.component;

import com.guiseframework.GuiseSession;

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
		
	/**Session, ID, and enum class constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param enumValues The values of enums that allow access to the underlying array.
	@exception NullPointerException if the given session, and/or layout is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractEnumCompositeComponent(final GuiseSession session, final String id, final E[] enumValues)
	{
		super(session, id, enumValues.length);	//construct the parent class
	}

}
