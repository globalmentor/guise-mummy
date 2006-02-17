package com.guiseframework.component.layout;

import static java.text.MessageFormat.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.*;

import javax.mail.internet.ContentType;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.Component;
import com.guiseframework.component.Container;
import com.guiseframework.component.Control;
import com.guiseframework.event.ListListener;
import com.guiseframework.event.ListSelectionListener;
import com.guiseframework.model.*;
import com.guiseframework.validator.*;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.TextUtilities.*;
import static com.guiseframework.GuiseResourceConstants.*;

/**A layout that manages child components as an ordered stack of cards.
Only one child component is visible at a time.
The card layout maintains its own value model that maintains the current selected card.
@author Garret Wilson
*/
public class CardLayout extends AbstractLayout<CardLayout.Constraints> implements ValueModel<Component<?>>
{

	/**The value model used by this component.*/
	private final ValueModel<Component<?>> valueModel;

		/**@return The value model used by this component.*/
		protected ValueModel<Component<?>> getValueModel() {return valueModel;}

	/**@return Whether the contents of this model are valid.*/
	public boolean isValid() {return true;}	//TODO del if not needed

	/**The lazily-created listener of constraint property changes.*/
//TODO del if not needed	private CardConstraintsPropertyChangeListener cardConstraintsPropertyChangeListener=null;

		/**@return The lazily-created listener of card constraint property changes.*/
/*TODO del if not needed
		protected CardConstraintsPropertyChangeListener getConstraintsPropertyChangeListener()
		{
			if(cardConstraintsPropertyChangeListener==null)	//if we haven't yet created a property change listener for constraints
			{
				cardConstraintsPropertyChangeListener=new CardConstraintsPropertyChangeListener();	//create a new constraints property change listener
			}
			return cardConstraintsPropertyChangeListener;	//return the listener of constraints properties
		}
*/
		
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
			final Component<?> component=newIndex>=0 ? container.get(newIndex) : null;	//get the component at the given index, if a valid index was given
			if(newIndex!=getSelectedIndex() && component!=getValue())	//if we're really changing either the selected index or the component
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
		return new Constraints();	//create constraints with a default label model
	}

	/**Session constructor.
	@param session The Guise session that owns this layout.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public CardLayout(final GuiseSession session)
	{
		super(session);	//construct the parent class
		this.valueModel=(ValueModel<Component<?>>)new DefaultValueModel<Component>(session, Component.class);	//create a new value model
		this.valueModel.addPropertyChangeListener(new PropertyChangeListener()	//create a listener to listen for the value model changing a property value
				{
					public void propertyChange(final PropertyChangeEvent propertyChangeEvent)	//if the value model changes a property value
					{
						firePropertyChange(propertyChangeEvent.getPropertyName(), propertyChangeEvent.getOldValue(), propertyChangeEvent.getNewValue());	//forward the property change event, indicating this component as the event source
					}			
				});
	}

	/**@return The default value.*/
	public Component<?> getDefaultValue() {return getValueModel().getDefaultValue();}

	/**@return The input value, or <code>null</code> if there is no input value.*/
	public Component<?> getValue() {return getValueModel().getValue();}

	/**Sets the input value.
	This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
	If a validator is installed, the value will first be validated before the current value is changed.
	Validation always occurs if a validator is installed, even if the value is not changing.
	This version makes sure that the given component is contained in the container, and resets the cached selected index so that it can be recalculated.
	@param newValue The input value of the model.
	@exception ValidationException if the provided value is not valid.
	@see #getValidator()
	@see #VALUE_PROPERTY
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
			if(newValue!=null && !container.contains(newValue))	//if there is a new component that isn't contained in the container
			{
				throw new ValidationException(format(getSession().getStringResource(VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE), newValue.toString()), newValue);						
			}
			selectedIndex=-1;	//uncache the selected index
			getValueModel().setValue(newValue);	//set the new value normally
		}
	}

	/**Clears the value by setting the value to <code>null</code>, which may be invalid according to any installed validators.
	No validation occurs.
	This version resets the cached selected index so that it can be recalculated.
	@see #VALUE_PROPERTY
	*/
	public void clearValue()
	{
		selectedIndex=-1;	//uncache the selected index
		getValueModel().clearValue();
	}

	/**Resets the value to a default value, which may be invalid according to any installed validators.
	No validation occurs.
	This version resets the cached selected index so that it can be recalculated.
	@see #VALUE_PROPERTY
	*/
	public void resetValue()
	{
		selectedIndex=-1;	//uncache the selected index
		getValueModel().resetValue();
	}

	/**@return The validator for this model, or <code>null</code> if no validator is installed.*/
	public Validator<Component<?>> getValidator() {return getValueModel().getValidator();}

	/**Sets the validator.
	This is a bound property
	@param newValidator The validator for this model, or <code>null</code> if no validator should be used.
	@see #VALIDATOR_PROPERTY
	*/
	public void setValidator(final Validator<Component<?>> newValidator) {getValueModel().setValidator(newValidator);}

	/**Validates the value of this model, throwing an exception if the model is not valid.
	@exception ValidationException if the value of this model is not valid.	
	*/
	public void validateValue() throws ValidationException {getValueModel().validateValue();}

	/**@return The class representing the type of value this model can hold.*/
	public Class<Component<?>> getValueClass() {return getValueModel().getValueClass();}

	/**Metadata about individual component layout.
	@author Garret Wilson
	*/
	public static class Constraints extends AbstractLayout.AbstractConstraints implements LabelModel	//TODO fix hack; no session is supported for these constraints; refactor all constraints
	{

		/**@return The Guise session that owns this model.*/
		public GuiseSession getSession() {throw new UnsupportedOperationException("Constraints does not currently support Guise session.");}

		/**@return Whether the contents of this model are valid.*/
		public boolean isValid() {throw new UnsupportedOperationException("Constraints does not currently support isValid().");}	//TODO del

		/**The icon URI, or <code>null</code> if there is no icon URI.*/
		private URI labelIcon=null;

			/**@return The icon URI, or <code>null</code> if there is no icon URI.*/
			public URI getIcon() {return labelIcon;}

			/**Sets the URI of the icon.
			This is a bound property of type <code>URI</code>.
			@param newLabelIcon The new URI of the icon.
			@see #ICON_PROPERTY
			*/
			public void setIcon(final URI newLabelIcon)
			{
				if(!ObjectUtilities.equals(labelIcon, newLabelIcon))	//if the value is really changing
				{
					final URI oldLabelIcon=labelIcon;	//get the old value
					labelIcon=newLabelIcon;	//actually change the value
					firePropertyChange(LabelModel.ICON_PROPERTY, oldLabelIcon, newLabelIcon);	//indicate that the value changed
				}			
			}

		/**The icon URI resource key, or <code>null</code> if there is no icon URI resource specified.*/
		private String labelIconResourceKey=null;

			/**@return The icon URI resource key, or <code>null</code> if there is no icon URI resource specified.*/
			public String getIconResourceKey() {return labelIconResourceKey;}

			/**Sets the key identifying the URI of the icon in the resources.
			This is a bound property.
			@param newIconResourceKey The new icon URI resource key.
			@see #LABEL_RESOURCE_KEY_PROPERTY
			*/
			public void setIconResourceKey(final String newIconResourceKey)
			{
				if(!ObjectUtilities.equals(labelIconResourceKey, newIconResourceKey))	//if the value is really changing
				{
					final String oldIconResourceKey=labelIconResourceKey;	//get the old value
					labelIconResourceKey=newIconResourceKey;	//actually change the value
					firePropertyChange(LabelModel.ICON_RESOURCE_KEY_PROPERTY, oldIconResourceKey, newIconResourceKey);	//indicate that the value changed
				}
			}

		/**The label text, or <code>null</code> if there is no label text.*/
		private String labelText=null;

			/**@return The label text, or <code>null</code> if there is no label text.*/
			public String getLabel() {return labelText;}

			/**Sets the text of the label.
			This is a bound property.
			@param newLabelText The new text of the label.
			@see #LABEL_PROPERTY
			*/
			public void setLabel(final String newLabelText)
			{
				if(!ObjectUtilities.equals(labelText, newLabelText))	//if the value is really changing
				{
					final String oldLabel=labelText;	//get the old value
					labelText=newLabelText;	//actually change the value
					firePropertyChange(LabelModel.LABEL_PROPERTY, oldLabel, newLabelText);	//indicate that the value changed
				}			
			}

		/**The content type of the label text.*/
		private ContentType labelTextContentType=Component.PLAIN_TEXT_CONTENT_TYPE;

			/**@return The content type of the label text.*/
			public ContentType getLabelContentType() {return labelTextContentType;}

			/**Sets the content type of the label text.
			This is a bound property.
			@param newLabelTextContentType The new label text content type.
			@exception NullPointerException if the given content type is <code>null</code>.
			@exception IllegalArgumentException if the given content type is not a text content type.
			@see #LABEL_CONTENT_TYPE_PROPERTY
			*/
			public void setLabelContentType(final ContentType newLabelTextContentType)
			{
				checkNull(newLabelTextContentType, "Content type cannot be null.");
				if(labelTextContentType!=newLabelTextContentType)	//if the value is really changing
				{
					final ContentType oldLabelTextContentType=labelTextContentType;	//get the old value
					if(!isText(newLabelTextContentType))	//if the new content type is not a text content type
					{
						throw new IllegalArgumentException("Content type "+newLabelTextContentType+" is not a text content type.");
					}
					labelTextContentType=newLabelTextContentType;	//actually change the value
					firePropertyChange(LabelModel.LABEL_CONTENT_TYPE_PROPERTY, oldLabelTextContentType, newLabelTextContentType);	//indicate that the value changed
				}			
			}

		/**The label text resource key, or <code>null</code> if there is no label text resource specified.*/
		private String labelTextResourceKey=null;
		
			/**@return The label text resource key, or <code>null</code> if there is no label text resource specified.*/
			public String getLabelResourceKey() {return labelTextResourceKey;}
		
			/**Sets the key identifying the text of the label in the resources.
			This is a bound property.
			@param newLabelTextResourceKey The new label text resource key.
			@see #LABEL_RESOURCE_KEY_PROPERTY
			*/
			public void setLabelResourceKey(final String newLabelTextResourceKey)
			{
				if(!ObjectUtilities.equals(labelTextResourceKey, newLabelTextResourceKey))	//if the value is really changing
				{
					final String oldLabelTextResourceKey=labelTextResourceKey;	//get the old value
					labelTextResourceKey=newLabelTextResourceKey;	//actually change the value
					firePropertyChange(LabelModel.LABEL_RESOURCE_KEY_PROPERTY, oldLabelTextResourceKey, newLabelTextResourceKey);	//indicate that the value changed
				}
			}

		/**Whether the card is enabled for selection.*/
		private boolean enabled=true;

			/**@return Whether the card is enabled for selection.*/
			public boolean isEnabled() {return enabled;}

			/**Sets whether the the card is enabled for selection.
			This is a bound property of type <code>Boolean</code>.
			@param newEnabled <code>true</code> if the corresponding card can be selected.
			@see Control#ENABLED_PROPERTY
			*/
			public void setEnabled(final boolean newEnabled)
			{
				if(enabled!=newEnabled)	//if the value is really changing
				{
					final boolean oldEnabled=enabled;	//get the old value
					enabled=newEnabled;	//actually change the value
					firePropertyChange(Control.ENABLED_PROPERTY, Boolean.valueOf(oldEnabled), Boolean.valueOf(newEnabled));	//indicate that the value changed
				}			
			}

		/**Default constructor.*/
		public Constraints()
		{
			this(true);	//default to enabling the card
		}

		/**Enabled constructor.
		@param enabled Whether the card is enabled.
		*/
		public Constraints(final boolean enabled)
		{
			this.enabled=enabled;
		}

		/**Label constructor.
		@param labelText The text of the label.
		*/
		public Constraints(final String labelText)
		{
			this(true);	//enable the card
			this.labelText=labelText;	//save the label text
		}

		/**Label model constructor.
		@param labelModel The label model providing label information.
		*/
		public Constraints(final LabelModel labelModel)	//TODO improve entire class to obviate this constructor; right now the label model is only copied rather than used in delegation
		{
			this(true);	//enable the card
			setIcon(labelModel.getIcon());	//initialize the constraints
			setIconResourceKey(labelModel.getIconResourceKey());
			setLabel(labelModel.getLabel());
			setLabelContentType(labelModel.getLabelContentType());
			setLabelResourceKey(labelModel.getLabelResourceKey());
		}
	}

	/**A property change listener that listens for changes in a constraint object's properties and fires a layout constraints property change event in response.
	This version also fires model {@link ValuePropertyChangeEvent}s if appropriate.
	A {@link LayoutConstraintsPropertyChangeEvent} will be fired for each component associated with the constraints for which a property changed
	@author Garret Wilson
	@see ValuePropertyChangeEvent
	*/
/*TODO decide if we need this
	protected class CardConstraintsPropertyChangeListener extends ConstraintsPropertyChangeListener
	{
*/
		/**Refires a constraint property change event for the layout in the form of a {@link LayoutConstraintsPropertyChangeEvent}.
		This version also fires a model {@link ValuePropertyChangeEvent} if appropriate to satisfy the list select model contract for value state changes.
		@param component The component for which a constraint value changed.
		@param constraints The constraints for which a value changed.
		@param propertyName The name of the property being changed.
		@param oldValue The old property value.
		@param newValue The new property value.
		*/
/*TODO decide if we need this
		protected <V> void refirePropertyChange(final Component<?> component, final Constraints constraints, final String propertyName, final V oldValue, final V newValue)
		{
			super.refirePropertyChange(component, constraints, propertyName, oldValue, newValue);	//refire the event normally
			if(Constraints.ENABLED_PROPERTY.equals(propertyName))	//if the enabled constraint changed
			{
				listSelectModel.fireValuePropertyChange(component, propertyName, oldValue, newValue);	//tell the model to fire its own event to satisfy the model's contract
			}			
		}
	}
*/

}
