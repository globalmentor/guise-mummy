package com.javaguise.component;

import com.javaguise.component.layout.CardLayout;
import com.javaguise.component.layout.Flow;
import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import static com.garretwilson.lang.ObjectUtilities.*;

/**Convenience tab control that automatically controls the selected card of a card control.
@author Garret Wilson
@see CardContainer
*/
public class CardTabControl extends TabControl<Component<?>>
{

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param cardControl The card control to be controlled.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given session, model, and/or axis is <code>null</code>.
	*/
	public CardTabControl(final GuiseSession session, final CardControl<?> cardControl, final Flow axis)
	{
		this(session, null, cardControl, axis);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, model, and maximum tab count constructor.
	@param session The Guise session that owns this component.
	@param cardControl The card control to be controlled.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given session, model, and/or axis is <code>null</code>.
	*/
	public CardTabControl(final GuiseSession session, final CardControl<?> cardControl, final Flow axis, final int maxTabCount)
	{
		this(session, null, cardControl, axis, maxTabCount);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, model, and value representation strategy constructor.
	@param session The Guise session that owns this component.
	@param cardControl The card control to be controlled.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given session, model, value representation strategy, and/or axis is <code>null</code>.
	*/
	public CardTabControl(final GuiseSession session, final CardControl<?> cardControl, final ValueRepresentationStrategy<Component<?>> valueRepresentationStrategy, final Flow axis)
	{
		this(session, null, cardControl, valueRepresentationStrategy, axis);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, model, value representation strategy, and maximum count constructor.
	@param session The Guise session that owns this component.
	@param cardControl The card control to be controlled.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session, model, value representation strategy, and/or axis is <code>null</code>.
	*/
	public CardTabControl(final GuiseSession session, final CardControl<?> cardControl, final ValueRepresentationStrategy<Component<?>> valueRepresentationStrategy, final Flow axis, final int maxTabCount)
	{
		this(session, null, cardControl, valueRepresentationStrategy, axis, maxTabCount);	//construct the class, indicating that a default ID should be generated
	}
		
	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param cardControl The card control to be controlled.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given session, model and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CardTabControl(final GuiseSession session, final String id, final CardControl<?> cardControl, final Flow axis)
	{
		this(session, id, cardControl, axis, -1);	//construct the class with no maximum tab count
	}

	/**Session, ID, model, and maximumn tab count constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param cardControl The card control to be controlled.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible tabs, or -1 if no maximum tab count is specified.
	@exception NullPointerException if the given session, model, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CardTabControl(final GuiseSession session, final String id, final CardControl<?> cardControl, final Flow axis, final int maxTabCount)
	{
		this(session, id, cardControl, new CardRepresentationStrategy(cardControl.getLayout()), axis, maxTabCount);	//construct the class with a default representation strategy
	}

	/**Session, ID, model, and value representation strategy constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param cardControl The card control to be controlled.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given session, model, value representation strategy, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CardTabControl(final GuiseSession session, final String id, final CardControl<?> cardControl, final ValueRepresentationStrategy<Component<?>> valueRepresentationStrategy, final Flow axis)
	{
		this(session, id, cardControl, valueRepresentationStrategy, axis, -1);	//construct the class with no maximum tab count
	}

	/**Session, ID, model, value representation strategy, and maximum tab count constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param cardControl The card control to be controlled.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible tabs, or -1 if no maximum tab count is specified.
	@exception NullPointerException if the given session, model, value representation strategy, and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CardTabControl(final GuiseSession session, final String id, final CardControl<?> cardControl, final ValueRepresentationStrategy<Component<?>> valueRepresentationStrategy, final Flow axis, final int maxTabCount)
	{
		super(session, id, cardControl.getModel(), valueRepresentationStrategy, axis, maxTabCount);	//construct the parent class using the card container's model
	}

	/**A value representation strategy for representing cards.
	A label component will be generated based on the card layout information.
	The label's ID will be the card component's ID.
	@see Label
	@author Garret Wilson
	*/
	public static class CardRepresentationStrategy implements ValueRepresentationStrategy<Component<?>>
	{
		
		/**The card layout containing component layout information.*/
		private final CardLayout cardLayout;

			/**@return The card layout containing component layout information.*/
			public final CardLayout getCardLayout() {return cardLayout;}

		/**Card layout constructor.
		@param cardLayout The card layout containing component layout information.
		@exception NullPointerException if the given card layout is <code>null</code>.
		*/
		public CardRepresentationStrategy(final CardLayout cardLayout)
		{
			this.cardLayout=checkNull(cardLayout, "Card layout cannot be null");	//save the card layout
		}

		/**Creates a component for the given list value.
		This implementation returns a label with information from the card layout.
		The label's ID will be the card component's ID.
		@param model The model containing the value.
		@param value The value for which a component should be created.
		@param index The index of the value within the list.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		public Label createComponent(final ListSelectModel<Component<?>> model, final Component<?> value, final int index, final boolean selected, final boolean focused)
		{
			return value!=null	//if there is a value
					? new Label(getCardLayout().getSession(), getID(value), getCardLayout().getConstraints(value).getLabel())	//generate a label containing the label model from the card layout constraints
					: null;	//otherwise return null
		}

		/**Determines an identifier for the given object.
		This implementation returns the card component's ID.
		@param value The value for which an identifier should be returned.
		@return A string identifying the value, or <code>null</code> if the provided value is <code>null</code>.
		@see Component#getID()
		*/
		public String getID(final Component<?> value)
		{
			return value!=null ? value.getID() : null;	//use the card component's ID, if a card component was given
		}
	}

}
