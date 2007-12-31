package com.guiseframework.component;

import com.guiseframework.component.layout.CardLayout;
import com.guiseframework.component.layout.Flow;
import com.guiseframework.model.*;

import static com.garretwilson.lang.Objects.*;

/**Convenience tab control that automatically controls the selected card of a card control.
@author Garret Wilson
@see CardContainer
*/
public class CardTabControl extends TabControl<Component>
{

	/**Card control and axis constructor.
	@param cardControl The card control to be controlled.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given card control and/or axis is <code>null</code>.
	*/
	public CardTabControl(final CardControl cardControl, final Flow axis)
	{
		this(cardControl, axis, -1);	//construct the class with no maximum tab count
	}

	/**Card control, axis, and maximum tab count constructor.
	@param cardControl The card control to be controlled.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible tabs, or -1 if no maximum tab count is specified.
	@exception NullPointerException if the given card control and/or axis is <code>null</code>.
	*/
	public CardTabControl(final CardControl cardControl, final Flow axis, final int maxTabCount)
	{
		this(cardControl, new CardRepresentationStrategy(cardControl.getLayout()), axis, maxTabCount);	//construct the class with a default representation strategy
	}

	/**Card control, value representation strategy, and axis  constructor.
	@param cardControl The card control to be controlled.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param axis The axis along which the tabs are oriented.
	@exception NullPointerException if the given card control, value representation strategy, and/or axis is <code>null</code>.
	*/
	public CardTabControl(final CardControl cardControl, final ValueRepresentationStrategy<Component> valueRepresentationStrategy, final Flow axis)
	{
		this(cardControl, valueRepresentationStrategy, axis, -1);	//construct the class with no maximum tab count
	}

	/**Card control, value representation strategy, axis, and maximum tab count constructor.
	@param cardControl The card control to be controlled.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param axis The axis along which the tabs are oriented.
	@param maxTabCount The requested number of visible tabs, or -1 if no maximum tab count is specified.
	@exception NullPointerException if the given card control, value representation strategy, and/or axis is <code>null</code>.
	*/
	public CardTabControl(final CardControl cardControl, final ValueRepresentationStrategy<Component> valueRepresentationStrategy, final Flow axis, final int maxTabCount)
	{
		super(cardControl, valueRepresentationStrategy, axis, maxTabCount);	//construct the parent class using the card container's model
	}

	/**A value representation strategy for representing cards.
	A label component will be generated based on the card layout information.
	The value's ID will be the card component's ID.
	@see Label
	@author Garret Wilson
	*/
	public static class CardRepresentationStrategy implements ValueRepresentationStrategy<Component>
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
			this.cardLayout=checkInstance(cardLayout, "Card layout cannot be null");	//save the card layout
		}

		/**Creates a component for the given list value.
		This implementation returns a label with information from the card layout.
		@param model The model containing the value.
		@param value The value for which a component should be created.
		@param index The index of the value within the list, or -1 if the value is not in the list (e.g. for representing no selection).
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		public Label createComponent(final ListSelectModel<Component> model, final Component value, final int index, final boolean selected, final boolean focused)
		{
			return value!=null	//if there is a value
			? new Label(getCardLayout().getConstraints(value))	//generate a label using the the card layout constraints as the label model
			: new Label();	//otherwise return an empty label
		}
	}

}
