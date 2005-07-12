package com.javaguise.component;

import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import static com.garretwilson.lang.ObjectUtilities.*;

/**Abstract implementation of a control to allow selection by the user of a value from a collection.
This implementation does not yet fully support elements that appear more than once in the model.
@param <V> The type of values to select.
@param <R> The type of component allowed to represent each value.
@author Garret Wilson
*/
public abstract class AbstractSelectControl<V, R extends Component<?>, C extends SelectControl<V, R, C>> extends AbstractControl<ListSelectModel<V>, C> implements SelectControl<V, R, C>
{

	/**The strategy used to generate a component to represent each value in the model.*/
	private ValueRepresentationStrategy<V, R> valueRepresentationStrategy;

		/**@return The strategy used to generate a component to represent each value in the model.*/
		public ValueRepresentationStrategy<V, R> getValueRepresentationStrategy() {return valueRepresentationStrategy;}

		/**Sets the strategy used to generate a component to represent each value in the model.
		This is a bound property
		@param newValueRepresentationStrategy The new strategy to create components to represent this model's values.
		@exception NullPointerException if the provided value representation strategy is <code>null</code>.
		@see SelectControl#VALUE_REPRESENTATION_STRATEGY_PROPERTY
		*/
		public void setValueRepresentationStrategy(final ValueRepresentationStrategy<V, R> newValueRepresentationStrategy)
		{
			if(valueRepresentationStrategy!=newValueRepresentationStrategy)	//if the value is really changing
			{
				final ValueRepresentationStrategy<V, R> oldValueRepresentationStrategy=valueRepresentationStrategy;	//get the old value
				valueRepresentationStrategy=checkNull(newValueRepresentationStrategy, "Value representation strategy cannot be null.");	//actually change the value
				firePropertyChange(VALUE_REPRESENTATION_STRATEGY_PROPERTY, oldValueRepresentationStrategy, newValueRepresentationStrategy);	//indicate that the value changed
			}
		}

	/**Session, ID, and model constructor.
	This method is only provided so that a concrete class may initialize the value representation strategy with an internal type, which cannot be done during super construction.
	The subclass must immediately set the value representation strategy to a valid value.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@see #setValueRepresentationStrategy(ValueRepresentationStrategy)
	*/
	protected AbstractSelectControl(final GuiseSession<?> session, final String id, final ListSelectModel<V> model)
	{
		super(session, id, model);	//construct the parent class
		valueRepresentationStrategy=null;	//the value representation strategy is now null, and must be set by the super class
	}

	/**Session, ID, model, and value representation strategy constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create controls to represent this model's values.
	@exception NullPointerException if the given session, model, and/or value representation strategy is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractSelectControl(final GuiseSession<?> session, final String id, final ListSelectModel<V> model, final ValueRepresentationStrategy<V, R> valueRepresentationStrategy)
	{
		super(session, id, model);	//construct the parent class
		this.valueRepresentationStrategy=checkNull(valueRepresentationStrategy, "Value representation strategy cannot be null.");
	}

}
