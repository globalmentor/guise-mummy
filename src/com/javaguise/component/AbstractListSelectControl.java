package com.javaguise.component;

import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import static com.garretwilson.lang.ObjectUtilities.*;

/**Abstract implementation of a control to allow selection by the user of a value from a list.
This implementation does not yet fully support elements that appear more than once in the model.
@param <V> The type of values to select.
@param <M> The type of select model used.
@author Garret Wilson
*/
public abstract class AbstractListSelectControl<V, M extends ListSelectModel<V>, C extends ListSelectControl<V, M, C>> extends AbstractControl<M, C> implements ListSelectControl<V, M, C>
{

	/**The strategy used to generate a component to represent each value in the model.*/
	private ValueRepresentationStrategy<V> valueRepresentationStrategy;

		/**@return The strategy used to generate a component to represent each value in the model.*/
		public ValueRepresentationStrategy<V> getValueRepresentationStrategy() {return valueRepresentationStrategy;}

		/**Sets the strategy used to generate a component to represent each value in the model.
		This is a bound property
		@param newValueRepresentationStrategy The new strategy to create components to represent this model's values.
		@exception NullPointerException if the provided value representation strategy is <code>null</code>.
		@see SelectControl#VALUE_REPRESENTATION_STRATEGY_PROPERTY
		*/
		public void setValueRepresentationStrategy(final ValueRepresentationStrategy<V> newValueRepresentationStrategy)
		{
			if(valueRepresentationStrategy!=newValueRepresentationStrategy)	//if the value is really changing
			{
				final ValueRepresentationStrategy<V> oldValueRepresentationStrategy=valueRepresentationStrategy;	//get the old value
				valueRepresentationStrategy=checkNull(newValueRepresentationStrategy, "Value representation strategy cannot be null.");	//actually change the value
				firePropertyChange(VALUE_REPRESENTATION_STRATEGY_PROPERTY, oldValueRepresentationStrategy, newValueRepresentationStrategy);	//indicate that the value changed
			}
		}

	/**Session, ID, model, and value representation strategy constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create controls to represent this model's values.
	@exception NullPointerException if the given session, model, and/or value representation strategy is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractListSelectControl(final GuiseSession<?> session, final String id, final M model, final ValueRepresentationStrategy<V> valueRepresentationStrategy)
	{
		super(session, id, model);	//construct the parent class
		this.valueRepresentationStrategy=checkNull(valueRepresentationStrategy, "Value representation strategy cannot be null.");
	}

}
