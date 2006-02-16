package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.converter.AbstractStringLiteralConverter;
import com.guiseframework.model.*;
import com.guiseframework.validator.ValidationException;
import com.guiseframework.validator.Validator;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.checkNull;

/**Control to allow selection of one or more values from a list.
@param <V> The type of values to select.
@author Garret Wilson
*/
public class ListControl<V> extends AbstractListSelectControl<V, ListControl<V>> implements ValueControl<V, ListControl<V>>	//TODO we probably shouldn't save both models separately
{

	/**The row count bound property.*/
	public final static String ROW_COUNT_PROPERTY=getPropertyName(ListControl.class, "rowCount");

	/**The list select model used by this component.*/
	private final ListSelectModel<V> selectModel;

		/**@return The list select model used by this component.*/
		public ListSelectModel<V> getSelectModel() {return selectModel;}

	/**The value model used by this component.*/
	private final ValueModel<V> valueModel;

		/**@return The value model used by this component.*/
		protected ValueModel<V> getValueModel() {return valueModel;}

	/**Whether the value is editable and the control will allow the the user to change the value.*/
	private boolean editable=true;	//TODO fix or del if not needed

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

	/**The estimated number of rows requested to be visible, or -1 if no row count is specified.*/
	private int rowCount;

		/**@return The estimated number of rows requested to be visible, or -1 if no row count is specified.*/
		public int getRowCount() {return rowCount;}

		/**Sets the estimated number of rows requested to be visible.
		This is a bound property of type <code>Integer</code>.
		@param newRowCount The new requested number of visible rows, or -1 if no row count is specified.
		@see #ROW_COUNT_PROPERTY
		*/
		public void setRowCount(final int newRowCount)
		{
			if(rowCount!=newRowCount)	//if the value is really changing
			{
				final int oldRowCount=rowCount;	//get the old value
				rowCount=newRowCount;	//actually change the value
				firePropertyChange(ROW_COUNT_PROPERTY, new Integer(oldRowCount), new Integer(newRowCount));	//indicate that the value changed
			}			
		}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final ListSelectModel<V> model)
	{
		this(session, null, model);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, model, and row count constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final ListSelectModel<V> model, final int rowCount)
	{
		this(session, null, model, rowCount);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, model, and value representation strategy constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@exception NullPointerException if the given session, model, and/or value representation strategy is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final ListSelectModel<V> model, final ValueRepresentationStrategy<V> valueRepresentationStrategy)
	{
		this(session, null, model, valueRepresentationStrategy);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, model, value representation strategy, and row count constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session, model, and/or value representation strategy is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final ListSelectModel<V> model, final ValueRepresentationStrategy<V> valueRepresentationStrategy, final int rowCount)
	{
		this(session, null, model, valueRepresentationStrategy, rowCount);	//construct the class, indicating that a default ID should be generated
	}
		
	/**Session constructor with a default data model to represent a given type with multiple selection.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final Class<V> valueClass)
	{
		this(session, null, valueClass);	//construct the component, indicating that a default ID should be used
	}

	/**Session and row count constructor with a default data model to represent a given type with multiple selection.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final Class<V> valueClass, final int rowCount)
	{
		this(session, null, valueClass, rowCount);	//construct the component, indicating the row count and that a default ID should be used
	}

	/**Session and selection strategy constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@exception NullPointerException if the given session, value class, and/or selection strategy is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy)
	{
		this(session, null, valueClass, selectionStrategy);	//construct the component, indicating that a default ID should be used
	}

	/**Session, selection strategy, and row count constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session, value class, and/or selection strategy is <code>null</code>.
	*/
	public ListControl(final GuiseSession session, final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final int rowCount)
	{
		this(session, null, valueClass, selectionStrategy, rowCount);	//construct the component, indicating the row count and that a default ID should be used
	}

	/**Session and ID constructor with a default data model to represent a given type with multiple selection.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final Class<V> valueClass)
	{
		this(session, id, new DefaultListSelectModel<V>(session, valueClass));	//construct the class with a default model
	}

	/**Session, ID, and row count constructor with a default data model to represent a given type with multiple selection.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final Class<V> valueClass, final int rowCount)
	{
		this(session, id, new DefaultListSelectModel<V>(session, valueClass), rowCount);	//construct the class with a default model and the row count
	}

	/**Session and ID constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@exception NullPointerException if the given session, value class, and/or selection strategy is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy)
	{
		this(session, id, new DefaultListSelectModel<V>(session, valueClass, selectionStrategy));	//construct the class with a default model
	}

	/**Session, ID, and row count constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session, value class, and/or selection strategy is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final int rowCount)
	{
		this(session, id, new DefaultListSelectModel<V>(session, valueClass, selectionStrategy), rowCount);	//construct the class with a default model and the row count
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final ListSelectModel<V> model)
	{
		this(session, id, model, -1);	//construct the class with no row count
	}

	/**Session, ID, model, and row count constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final ListSelectModel<V> model, final int rowCount)
	{
		this(session, id, model, new DefaultValueRepresentationStrategy<V>(session, AbstractStringLiteralConverter.getInstance(session, model.getValueClass())), rowCount);	//construct the class with a default representation strategy
	}

	/**Session, ID, model, and value representation strategy constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@exception NullPointerException if the given session, model, and/or value representation strategy is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final ListSelectModel<V> model, final ValueRepresentationStrategy<V> valueRepresentationStrategy)
	{
		this(session, id, model, valueRepresentationStrategy, -1);	//construct the class with no row count
	}

	/**Session, ID, model, value representation strategy, and row count constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@exception NullPointerException if the given session, model, and/or value representation strategy is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession session, final String id, final ListSelectModel<V> model, final ValueRepresentationStrategy<V> valueRepresentationStrategy, final int rowCount)
	{
		super(session, id, model, valueRepresentationStrategy);	//construct the parent class
		this.rowCount=rowCount;	//save the row count
		this.selectModel=checkNull(model, "Select model cannot be null.");	//save the select model
			//don't listen for list select model property changes specifically, because this would result in repeating events twice TODO or would it? doesn't the model keep track of double listeners?
		this.valueModel=checkNull(model, "Value model cannot be null.");	//save the value model
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
