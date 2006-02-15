package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.CardLayout;
import com.guiseframework.model.ListSelectModel;

/**An abstract panel with a card layout.
The panel's model reflects the currently selected component, if any.
@author Garret Wilson
@see CardLayout
*/
public abstract class AbstractCardPanel<C extends Box<C> & Panel<C> & CardControl<C>> extends AbstractContainerControl<C> implements Box<C>, Panel<C>, CardControl<C>
{

	/**@return The data model used by this component.*/
	@SuppressWarnings("unchecked")
	public ListSelectModel<Component<?>> getModel() {return (ListSelectModel<Component<?>>)super.getModel();}

	/**@return The layout definition for the container.*/
	public CardLayout getLayout() {return (CardLayout)super.getLayout();}

	/**Whether the state of the control represents valid user input.*/
	private boolean valid=true;

		/**@return Whether the state of the control represents valid user input.*/
		public boolean isValid() {return valid;}

		/**Sets whether the state of the control represents valid user input
		This is a bound property of type <code>Boolean</code>.
		@param newValid <code>true</code> if user input should be considered valid
		@see Control#VALID_PROPERTY
		*/
		public void setValid(final boolean newValid)
		{
			if(valid!=newValid)	//if the value is really changing
			{
				final boolean oldValid=valid;	//get the current value
				valid=newValid;	//update the value
				firePropertyChange(VALID_PROPERTY, Boolean.valueOf(oldValid), Boolean.valueOf(newValid));
			}
		}

	/**The strategy used to generate a component to represent each value in the model.*/
	private ValueRepresentationStrategy<Component<?>> valueRepresentationStrategy;

		/**@return The strategy used to generate a component to represent each value in the model.*/
		public ValueRepresentationStrategy<Component<?>> getValueRepresentationStrategy() {return valueRepresentationStrategy;}

		/**Sets the strategy used to generate a component to represent each value in the model.
		This is a bound property
		@param newValueRepresentationStrategy The new strategy to create components to represent this model's values.
		@exception NullPointerException if the provided value representation strategy is <code>null</code>.
		@see SelectControl#VALUE_REPRESENTATION_STRATEGY_PROPERTY
		*/
		public void setValueRepresentationStrategy(final ValueRepresentationStrategy<Component<?>> newValueRepresentationStrategy)
		{
			if(valueRepresentationStrategy!=newValueRepresentationStrategy)	//if the value is really changing
			{
				final ValueRepresentationStrategy<Component<?>> oldValueRepresentationStrategy=valueRepresentationStrategy;	//get the old value
				valueRepresentationStrategy=checkNull(newValueRepresentationStrategy, "Value representation strategy cannot be null.");	//actually change the value
				firePropertyChange(VALUE_REPRESENTATION_STRATEGY_PROPERTY, oldValueRepresentationStrategy, newValueRepresentationStrategy);	//indicate that the value changed
			}
		}

	/**Session, ID, and layout constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	protected AbstractCardPanel(final GuiseSession session, final String id, final CardLayout layout)
	{
		super(session, id, layout, layout.getModel());	//construct the parent class, using the card layout's value model
	}

	/**Adds a component to the container along with a label.
	This convenience method creates new card layout constraints from the given label model and adds the component.
	@param component The component to add.
	@param labelModel The label associated with an individual component.
	@exception NullPointerException if the given label is <code>null</code>.
	@exception IllegalArgumentException if the component already has a parent.
	*/
/*TODO del if not wanted
	public void add(final Component<?> component, final LabelModel labelModel)
	{
		add(component, new CardLayout.Constraints(labelModel));	//create card layout constraints for the label and add the component to the container
	}
*/

	/**Enables or disables a card.
	This convenience method looks up card layout constraints for the given component and changes the enabled status of those constraints.
	@param component The component for which the card should be enabled or disabled.
	@param newEnabled <code>true</code> if the card can be selected.
	@see CardLayout.Constraints#setEnabled(boolean)
	*/
/*TODO del if not wanted
	public void setEnabled(final Component<?> component, final boolean newEnabled)
	{
		final CardLayout.Constraints constraints=getLayout().getConstraints(component);	//get the card constraints for this component
		if(constraints!=null)	//if there are constraints for this component
		{
			constraints.setEnabled(newEnabled);	//change the enabled status of the constraints
		}
	}
*/
}
