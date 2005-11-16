package com.javaguise.component.layout;

import static java.text.MessageFormat.*;

import java.util.MissingResourceException;

import javax.mail.internet.ContentType;

import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.util.Debug;

import static com.garretwilson.lang.ClassUtilities.getPropertyName;
import static com.garretwilson.lang.ObjectUtilities.*;

import static com.javaguise.GuiseResourceConstants.*;
import com.javaguise.component.Component;
import com.javaguise.component.Container;
import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.*;

/**A layout that manages child components as an ordered stack of cards.
Only one child comoponent is visible at a time.
The card layout maintains its own value model that maintains the current selected card.
@author Garret Wilson
*/
public class CardLayout extends AbstractLayout<CardLayout.Constraints>
{

	/**The decorated value model maintaining the selected component.*/
	private final ValueModel<Component<?>> valueModel;

		/**@return The decorated value model maintaining the selected component.*/
		public ValueModel<Component<?>> getValueModel() {return valueModel;}

	/**The index of the selected component, or -1 if the index is not known and should be recalculated.*/
	private int selectedIndex=-1;

		/**@return The index of the selected component, or -1 if no component is selected.*/
		public int getSelectedIndex()
		{
			if(selectedIndex<0)	//if there is no valid selected index, make sure the index is up-to-date
			{
				final Component<?> selectedComponent=getValue();	//get the selected component
				if(selectedComponent!=null)	//if a component is selected, we'll need to update the selected index
				{
					selectedIndex=getContainer().indexOf(selectedComponent);	//update the selected index with the index in the container of the selected component
					assert selectedIndex>=0 : "Selected component "+selectedComponent+" is not in the container.";
				}
			}
			return selectedIndex;	//return the selected index, which we've verified is up-to-date
		}

		/**Sets the index of the selected component.
		@param newIndex The index of the selected component, or -1 if no component is selected.
		@exception IllegalStateException if this layout has not yet been installed into a container.
		@exception IndexOutOfBoundsException if the index is out of range.
		@exception ValidationException if the component at the given index is not a valid component to select.
		*/
		public void setSelectedIndex(final int newIndex) throws ValidationException
		{
			final Container<?> container=getContainer();	//get the layout's container
			if(container==null)	//if we haven't been installed into a container
			{
				throw new IllegalStateException("Layout does not have container.");
			}
			final Component<?> component=container.get(newIndex);	//get the component at the given index
			if(newIndex!=getSelectedIndex() && component!=getValue())	//if we're really changing either the selected index of the component
			{
				selectedIndex=-1;	//uncache the selected index (don't actually change it yet---we want to make sure the value model allows the value to be changed)
				setValue(component);		//update the component value, throwing a validation exception if this index can't be selected
			}
		}

	/**Associates layout metadata with a component.
	This version selects a component if none is selected.
	@param component The component for which layout metadata is being specified.
	@param constraints Layout information specifically for the component.
	@return The layout information previously associated with the component, or <code>null</code> if the component did not previously have metadata specified.
	@exception NullPointerException if the given constraints object is <code>null</code>.
	@exception IllegalStateException if this layout has not yet been installed into a container.
	*/
	public Constraints setConstraints(final Component<?> component, final Constraints constraints)
	{
		final Constraints oldConstraints=super.setConstraints(component, constraints);	//set the constraints normally
		if(getValue()==null)	//if there is no component selected
		{
			try
			{
				setSelectedIndex(0);	//select the first component
			}
			catch(final ValidationException validationException)	//if we can't select the first component, don't do anything
			{
			}
		}
		return oldConstraints;	//return the previously associated constraints, if any
	}

	/**Removes any layout metadata associated with a component.
	This implementation updates the selected component if necessary.
	@param component The component for which layout metadata is being removed.
	@return The layout information previously associated with the component, or <code>null</code> if the component did not previously have metadata specified.
	@exception IllegalStateException if this layout has not yet been installed into a container.
	*/
	public Constraints removeConstraints(final Component<?> component)
	{
		final Constraints oldConstraints=super.removeConstraints(component);	//remove the constraints normally
		if(component==getValue())	//if the selected component was removed
		{
			final Container<?> container=getContainer();	//get our container
			final int selectedIndex=container.indexOf(component);	//find the current index of the component that is being removed
			final int containerSize=container.size();	//find out how many components are in the container
			final int newSelectedComponentIndex;	//we'll determine the new selected index (that is, the index of the new selected component in this current state; it won't be the new selected index after removal)
			if(selectedIndex<containerSize-1)	//if this component wasn't the last component
			{
				newSelectedComponentIndex=selectedIndex+1;	//get the subsequent component
			}
			else	//if this was the last component tha twas removed
			{
				newSelectedComponentIndex=containerSize-2;	//get the second-to last component
			}
			try
			{
				setValue(container.get(newSelectedComponentIndex));		//update the component value, throwing a validation exception if this index can't be selected
			}				
			catch(final ValidationException validationException)	//if we can't select the next component
			{
				getValueModel().resetValue();	//reset the selected component value
			}
		}
		this.selectedIndex=-1;	//always uncache the selected index, because the index of the selected component might have changed
		return oldConstraints;	//return the previous constraints
	}

	/**Creates default constraints for the container.
	@return New default constraints for the container.
	*/
	public Constraints createDefaultConstraints()
	{
		return new Constraints(new DefaultLabelModel(getSession()));	//create constraints with a default label model
	}

	/**Session constructor.
	@param session The Guise session that owns this layout.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public CardLayout(final GuiseSession session)
	{
		super(session);	//construct the parent class
		valueModel=new CardValueModel(session);	//create a new decorated value model
	}

	/**@return The current value, or <code>null</code> if there is no input value.*/
	public Component<?> getValue() {return getValueModel().getValue();}

	/**Sets the input value.
	This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
	If a validator is installed, the value will first be validated before the current value is changed.
	Validation always occurs if a validator is installed, even if the value is not changing.
	@param newValue The input value of the model.
	@exception ValidationException if the provided value is not valid.
	@see #getValidator()
	@see #VALUE_PROPERTY
	*/
	public void setValue(final Component<?> newValue) throws ValidationException {getValueModel().setValue(newValue);}

	/**Metadata about individual component layout.
	@author Garret Wilson
	*/
	public static class Constraints extends AbstractLayout.AbstractConstraints
	{

		/**The enabled bound property.*/
		public final static String ENABLED_PROPERTY=getPropertyName(ControlModel.class, "enabled");

		/**The label associated with an individual component.*/
		private final LabelModel labelModel;
		
			/**@return The label associated with an individual component.*/
			public LabelModel getLabel() {return labelModel;}

		/**Whether the card is enabled for selection.*/
		private boolean enabled=true;

			/**@return Whether the card is enabled for selection.*/
			public boolean isEnabled() {return enabled;}

			/**Sets whether the the card is enabled for selection.
			This is a bound property of type <code>Boolean</code>.
			@param newEnabled <code>true</code> if the corresponding card can be selected.
			@see ControlModel#ENABLED_PROPERTY
			*/
			public void setEnabled(final boolean newEnabled)
			{
				if(enabled!=newEnabled)	//if the value is really changing
				{
					final boolean oldEnabled=enabled;	//get the old value
					enabled=newEnabled;	//actually change the value
					firePropertyChange(ENABLED_PROPERTY, Boolean.valueOf(oldEnabled), Boolean.valueOf(newEnabled));	//indicate that the value changed
				}			
			}

		/**Label constructor.
		@param labelModel The label associated with an individual component.
		@exception NullPointerException if the given label is <code>null</code>.
		*/
		public Constraints(final LabelModel labelModel)
		{
			this(labelModel, true);	//default to enabling the card
		}

		/**Label and enabled constructor.
		@param labelModel The label associated with an individual component.
		@param enabled Whether the card is enabled.
		@exception NullPointerException if the given label is <code>null</code>.
		*/
		public Constraints(final LabelModel labelModel, final boolean enabled)
		{
			this.labelModel=checkNull(labelModel, "Label cannot be null.");
			this.enabled=enabled;
		}
	}

	/**The value model that maintains the current selected tab.
	@author Garret Wilson
	*/
	protected class CardValueModel extends DefaultValueModel<Component<?>>
	{
		/**Session constructor.
		@param session The Guise session that owns this model.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		@SuppressWarnings("unchecked")	//classes don't support generics multilevel, so we have to cast Component.class to the correct generic type
		public CardValueModel(final GuiseSession session)
		{
			super(session, (Class<Component<?>>)(Object)Component.class);	//construct the parent class TODO find out why we have to do the double-cast for JDK 1.5 to work
		}

		/**Sets the input value.
		This version makes sure that the given component is contained in the container, and resets the cached selected index so that it can be recalculated.
		@param newValue The input value of the model.
		@exception IllegalStateException if this layout has not yet been installed into a container.
		@exception ValidationException if the provided value is not valid, including if the given component is not a member of the layout container.
		*/
		public void setValue(final Component<?> newValue) throws ValidationException
		{
			if(!ObjectUtilities.equals(getValue(), newValue))	//if a new component is given
			{
				final Container<?> container=getContainer();	//get the layout's container
				if(container==null)	//if we haven't been installed into a container
				{
					throw new IllegalStateException("Layout does not have container.");
				}
				if(newValue!=null && !container.contains(newValue))	//if there is a new component that isn't contained in the contianer
				{
					throw new ValidationException(format(getSession().getStringResource(VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE), newValue.toString()), newValue);						
				}
				selectedIndex=-1;	//uncache the selected index
			}
			super.setValue(newValue);	//set the new value normally
		}

		/**Resets the value to a default value, which may be invalid according to any installed validators.
		No validation occurs.
		This version resets the cached selected index so that it can be recalculated.
		*/
		public void resetValue()
		{
			selectedIndex=-1;	//uncache the selected index
		}
	}
}
