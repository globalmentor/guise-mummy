package com.javaguise.component.layout;

import static java.text.MessageFormat.*;

import java.util.MissingResourceException;

import javax.mail.internet.ContentType;

import com.garretwilson.lang.ObjectUtilities;
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
public class CardLayout extends AbstractLayout<CardLayout.Constraints> //TODO del when works implements ValueModel<Component<?>>
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
				selectedIndex=-1;	//uncache the selected index (don't actually change it yet---we want to make sure the value model allows the value to be changed
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
		int selectedIndex=getSelectedIndex();	//make sure we have the latest selected index
		final Constraints oldConstraints=super.removeConstraints(component);	//remove the constraints normally
		if(component==getValue())	//if the selected component was removed
		{
			final int size=getContainer().size();	//get the number of components in the container
			if(size>=0)	//if there are components
			{
				if(selectedIndex>=size)	//if the selected index is too high, now that a component has been removed
				{
					selectedIndex=size-1;	//select the last component
				}
			}
			else	//if there are no more components
			{
				selectedIndex=-1;	//don't select any components
			}
			try
			{
				setSelectedIndex(selectedIndex);	//update the selected component by its index
			}
			catch(final ValidationException validationException)	//if we can't select the next component
			{
				getValueModel().resetValue();	//reset the selected component value
				selectedIndex=-1;	//uncache the selected component index
			}
		}
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

	//model delegation

	/**Determines the advisory information text, such as might appear in a tooltip.
	If information is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@return The advisory information text, such as might appear in a tooltip, or <code>null</code> if there is no advisory information.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getInfoResourceKey()
	*/
//TODO del when works	public String getInfo() throws MissingResourceException {return getValueModel().getInfo();}

	/**Sets the advisory information text, such as might appear in a tooltip.
	This is a bound property.
	@param newInfo The new text of the advisory information text, such as might appear in a tooltip.
	@see Model#INFO_PROPERTY
	*/
//TODO del when works	public void setInfo(final String newInfo) {getValueModel().setInfo(newInfo);}

	/**@return The content type of the advisory information text.*/
//TODO del when works	public ContentType getInfoContentType() {return getValueModel().getInfoContentType();}

	/**Sets the content type of the advisory information text.
	This is a bound property.
	@param newInfoContentType The new advisory information text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see Model#INFO_CONTENT_TYPE_PROPERTY
	*/
//TODO del when works	public void setInfoContentType(final ContentType newInfoContentType) {getValueModel().setInfoContentType(newInfoContentType);}

	/**@return The advisory information text resource key, or <code>null</code> if there is no advisory information text resource specified.*/
//TODO del when works	public String getInfoResourceKey() {return getValueModel().getInfoResourceKey();}

	/**Sets the key identifying the text of the advisory information in the resources.
	This is a bound property.
	@param newInfoResourceKey The new advisory information text resource key.
	@see Model#INFO_RESOURCE_KEY_PROPERTY
	*/
//TODO del when works	public void setInfoResourceKey(final String newInfoResourceKey) {getValueModel().setInfoResourceKey(newInfoResourceKey);}

	//value model delegation

	/**@return Whether the contents of this model are valid.*/
//TODO del when works	public boolean isValid() {return getValueModel().isValid();}

	/**Determines the text of the label.
	If a label is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@return The label text, or <code>null</code> if there is no label text.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getLabelResourceKey()
	@see #getPlainLabel()
	*/
//TODO del when works	public String getLabel() throws MissingResourceException {return getValueModel().getLabel();}

	/**Sets the text of the label.
	This is a bound property.
	@param newLabel The new text of the label.
	@see #LABEL_PROPERTY
	*/
//TODO del when works	public void setLabel(final String newLabel) {getValueModel().setLabel(newLabel);}

	/**@return The content type of the label text.*/
//TODO del when works	public ContentType getLabelContentType() {return getValueModel().getLabelContentType();}

	/**Sets the content type of the label text.
	This is a bound property.
	@param newLabelContentType The new label text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #LABEL_CONTENT_TYPE_PROPERTY
	*/
//TODO del when works	public void setLabelContentType(final ContentType newLabelContentType) {getValueModel().setLabelContentType(newLabelContentType);}

	/**@return The label text resource key, or <code>null</code> if there is no label text resource specified.*/
//TODO del when works	public String getLabelResourceKey() {return getValueModel().getLabelResourceKey();}

	/**Sets the key identifying the text of the label in the resources.
	This is a bound property.
	@param newLabelResourceKey The new label text resource key.
	@see #LABEL_RESOURCE_KEY_PROPERTY
	*/
//TODO del when works	public void setLabelResourceKey(final String newLabelResourceKey) {getValueModel().setLabelResourceKey(newLabelResourceKey);}

	/**@return Whether the model is enabled and and the corresponding control can receive user input.*/
//TODO del when works	public boolean isEnabled() {return getValueModel().isEnabled();}

	/**Sets whether the model is enabled and and the corresponding control can receive user input..
	This is a bound property of type <code>Boolean</code>.
	@param newEnabled <code>true</code> if the corresponding control should indicate and accept user input.
	@see #ENABLED_PROPERTY
	*/
//TODO del when works	public void setEnabled(final boolean newEnabled) {getValueModel().setEnabled(newEnabled);}

	/**@return Whether the model's value is editable and the corresponding control will allow the the user to change the value.*/
//TODO del when works	public boolean isEditable() {return getValueModel().isEditable();}

	/**Sets whether the model's value is editable and the corresponding control will allow the the user to change the value.
	This is a bound property of type <code>Boolean</code>.
	@param newEditable <code>true</code> if the corresponding control should allow the user to change the value.
	@see #EDITABLE_PROPERTY
	*/
//TODO del when works	public void setEditable(final boolean newEditable) {getValueModel().setEditable(newEditable);}

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

	/**Resets the value to a default value, which may be invalid according to any installed validators.
	No validation occurs.
	@see #VALUE_PROPERTY
	*/
//TODO del when works	public void resetValue() {getValueModel().resetValue();}

	/**@return The validator for this model, or <code>null</code> if no validator is installed.*/
//TODO del when works	public Validator<Component<?>> getValidator() {return getValueModel().getValidator();}

	/**Sets the validator.
	This is a bound property
	@param newValidator The validator for this model, or <code>null</code> if no validator should be used.
	@see #VALIDATOR_PROPERTY
	*/
//TODO del when works	public void setValidator(final Validator<Component<?>> newValidator) {getValueModel().setValidator(newValidator);}

	/**@return The class representing the type of value this model can hold.*/
//TODO del when works	public Class<Component<?>> getValueClass() {return getValueModel().getValueClass();}

	/**Metadata about individual component layout.
	@author Garret Wilson
	*/
	public static class Constraints implements Layout.Constraints
	{

		/**The label associated with an individual component.*/
		private final LabelModel labelModel;
		
			/**@return The label associated with an individual component.*/
			public LabelModel getLabel() {return labelModel;}

		/**Constructor.
		@param labelModel The label associated with an individual component.
		@exception NullPointerException if the given label is <code>null</code>.
		*/
		public Constraints(final LabelModel labelModel)
		{
			this.labelModel=checkNull(labelModel, "Label cannot be null.");
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
