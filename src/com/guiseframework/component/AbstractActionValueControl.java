package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.*;
import com.guiseframework.validator.*;

/**Abstract implementation of an action control containing a value.
@author Garret Wilson
@param <V> The type of value the control represents.
*/
public abstract class AbstractActionValueControl<V, C extends ActionValueControl<V, C>> extends AbstractActionControl<C> implements ActionValueControl<V, C>
{

	/**The value model used by this component.*/
	private final ValueModel<V> valueModel;

		/**@return The value model used by this component.*/
		protected ValueModel<V> getValueModel() {return valueModel;}

	/**Whether the value is editable and the control will allow the the user to change the value.*/
	private boolean editable=true;

		/**@return Whether the value is editable and the control will allow the the user to change the value.*/
		public boolean isEditable() {return editable;}

		/**Sets whether the value is editable and the control will allow the the user to change the value.
		This is a bound property of type <code>Boolean</code>.
		@param newEditable <code>true</code> if the control should allow the user to change the value.
		@see #EDITABLE_PROPERTY
		*/
		public void setEditable(final boolean newEditable)
		{
			if(editable!=newEditable)	//if the value is really changing
			{
				final boolean oldEditable=editable;	//get the old value
				editable=newEditable;	//actually change the value
				firePropertyChange(EDITABLE_PROPERTY, Boolean.valueOf(oldEditable), Boolean.valueOf(newEditable));	//indicate that the value changed
			}			
		}

	/**The map of icons keyed to values.*/
	private final Map<V, URI> valueIconMap=new HashMap<V, URI>();
		
		/**Retrieves the icon associated with a given value.
		@param value The value for which an associated icon should be returned, or <code>null</code> to retrieve the icon associated with the <code>null</code> value.
		@return The value icon URI, or <code>null</code> if the value has no associated icon URI.
		*/
		public URI getValueIcon(final V value) {return valueIconMap.get(value);}

		/**Sets the URI of the icon associated with a value.
		This method fires a property change event for the changed icon if its value changes.
		@param value The value with which the icon should be associated, or <code>null</code> if the icon should be associated with the <code>null</code> value.
		@param newValueIcon The new URI of the value icon.
		@see #VALUE_ICON_PROPERTY
		*/
		public void setValueIcon(final V value, final URI newValueIcon)
		{
			final URI oldValueIcon=valueIconMap.put(value, newValueIcon);	//store the new value
			firePropertyChange(VALUE_ICON_PROPERTY, oldValueIcon, newValueIcon);	//indicate that the value changed (which will only fire the event if the value actually changed)
		}

	/**The map of icon resource keys keyed to values.*/
	private final Map<V, String> valueIconResourceKeyMap=new HashMap<V, String>();
		
		/**Retrieves the icon resource key associated with a given value.
		@param value The value for which an associated icon resource key should be returned, or <code>null</code> to retrieve the icon resource key associated with the <code>null</code> value.
		@return The value icon resource key, or <code>null</code> if the value has no associated icon resource.
		*/
		public String getValueIconResourceKey(final V value) {return valueIconResourceKeyMap.get(value);}

		/**Sets the resource key of the icon associated with a value.
		This method fires a property change event for the changed icon resource key if its value changes.
		@param value The value with which the icon resource key should be associated, or <code>null</code> if the icon resource key should be associated with the <code>null</code> value.
		@param newValueIconResourceKey The new value icon resource key.
		@see #VALUE_ICON_RESOURCE_KEY_PROPERTY
		*/
		public void setValueIconResourceKey(final V value, final String newValueIconResourceKey)
		{
			final String oldValueIconResourceKey=valueIconResourceKeyMap.put(value, newValueIconResourceKey);	//store the new value
			firePropertyChange(VALUE_ICON_RESOURCE_KEY_PROPERTY, oldValueIconResourceKey, newValueIconResourceKey);	//indicate that the value changed (which will only fire the event if the value actually changed)
		}
		
	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueModel The component value model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractActionValueControl(final GuiseSession session, final String id, final ValueModel<V> valueModel)
	{
		super(session, id, new DefaultActionModel(session));	//construct the parent class with a default action model TODO add an action model parameter
		this.valueModel=checkNull(valueModel, "Value model cannot be null.");	//save the table model
		this.valueModel.addPropertyChangeListener(getRepeaterPropertyChangeListener());	//listen an repeat all property changes of the value model
	}

	/**Validates the model of this component and all child components.
	The component will be updated with error information.
	This version validates the associated model.
	@exception ComponentExceptions if there was one or more validation error.
	*/
	public void validate() throws ComponentExceptions
	{
		super.validate();	//validate the parent class
		try
		{
			getValueModel().validateValue();	//validate the value model
		}
		catch(final ComponentException componentException)	//if there is a component error
		{
			componentException.setComponent(this);	//make sure the exception knows to which component it relates
			addError(componentException);	//add this error to the component
			throw new ComponentExceptions(componentException);	//throw a new component exception list exception
		}
	}

	/**@return The default value.*/
	public V getDefaultValue() {return getValueModel().getDefaultValue();}

	/**@return The input value, or <code>null</code> if there is no input value.*/
	public V getValue() {return getValueModel().getValue();}

	/**Sets the input value.
	This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
	If a validator is installed, the value will first be validated before the current value is changed.
	Validation always occurs if a validator is installed, even if the value is not changing.
	@param newValue The input value of the model.
	@exception ValidationException if the provided value is not valid.
	@see #getValidator()
	@see #VALUE_PROPERTY
	*/
	public void setValue(final V newValue) throws ValidationException {getValueModel().setValue(newValue);}

	/**Clears the value by setting the value to <code>null</code>, which may be invalid according to any installed validators.
	No validation occurs.
	@see ValueModel#VALUE_PROPERTY
	*/
	public void clearValue() {getValueModel().clearValue();}

	/**Resets the value to a default value, which may be invalid according to any installed validators.
	No validation occurs.
	@see #VALUE_PROPERTY
	*/
	public void resetValue() {getValueModel().resetValue();}

	/**@return The validator for this model, or <code>null</code> if no validator is installed.*/
	public Validator<V> getValidator() {return getValueModel().getValidator();}

	/**Sets the validator.
	This is a bound property
	@param newValidator The validator for this model, or <code>null</code> if no validator should be used.
	@see #VALIDATOR_PROPERTY
	*/
	public void setValidator(final Validator<V> newValidator) {getValueModel().setValidator(newValidator);}

	/**Validates the value of this model, throwing an exception if the model is not valid.
	@exception ValidationException if the value of this model is not valid.	
	*/
	public void validateValue() throws ValidationException {getValueModel().validateValue();}

	/**@return The class representing the type of value this model can hold.*/
	public Class<V> getValueClass() {return getValueModel().getValueClass();}
}
