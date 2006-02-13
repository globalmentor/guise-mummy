package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.MissingResourceException;

import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.util.Debug;
import com.guiseframework.GuiseSession;
import com.guiseframework.converter.*;
import com.guiseframework.event.AbstractGuisePropertyChangeListener;
import com.guiseframework.event.GuisePropertyChangeEvent;
import com.guiseframework.model.*;
import com.guiseframework.validator.Validator;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.guiseframework.model.ValueModel.VALIDATOR_PROPERTY;


/**Control to accept text input from the user representing a particular value type.
This control keeps track of literal text entered by the user, distinct from the value stored in the model.
Default converters are available for the following types:
<ul>
	<li><code>char[]</code></li>
	<li><code>java.lang.Boolean</code></li>
	<li><code>java.lang.Float</code></li>
	<li><code>java.lang.Integer</code></li>
	<li><code>java.lang.String</code></li>
</ul>
@param <V> The type of value the input text is to represent.
@author Garret Wilson
*/
public class AbstractTextControl<V, C extends ValueControl<V, C>> extends AbstractValueControl<V, C>
{

	/**The column count bound property.*/
	public final static String COLUMN_COUNT_PROPERTY=getPropertyName(AbstractTextControl.class, "columnCount");
	/**The text literal bound property.*/
	public final static String TEXT_PROPERTY=getPropertyName(AbstractTextControl.class, "text");
	/**The valid bound property.*/
//TODO del	public final static String VALID_PROPERTY=getPropertyName(AbstractTextControl.class, "valid");

	/**Determines the text of the label.
	If a label is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@return The label text, or <code>null</code> if there is no label text.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getLabelResourceKey()
	*/
	public String getLabelText() throws MissingResourceException	//TODO testing
	{
		return getModel().getLabel();	//TODO fix
	}

	/**Sets the text of the label.
	This is a bound property.
	@param newLabel The new text of the label.
	@see LabelModel#LABEL_PROPERTY
	*/
	public void setLabelText(final String newLabel)	//TODO testing
	{
		getModel().setLabel(newLabel);	//TODO fix
	}

	/**The estimated number of columns requested to be visible, or -1 if no column count is specified.*/
	private int columnCount=-1;

		/**@return The estimated number of columns requested to be visible, or -1 if no column count is specified.*/
		public int getColumnCount() {return columnCount;}

		/**Sets the estimated number of columns requested to be visible.
		This is a bound property of type <code>Integer</code>.
		@param newColumnCount The new requested number of visible columns, or -1 if no column count is specified.
		@see #COLUMN_COUNT_PROPERTY 
		*/
		public void setColumnCount(final int newColumnCount)
		{
			if(columnCount!=newColumnCount)	//if the value is really changing
			{
				final int oldColumnCount=columnCount;	//get the old value
				columnCount=newColumnCount;	//actually change the value
				firePropertyChange(COLUMN_COUNT_PROPERTY, new Integer(oldColumnCount), new Integer(newColumnCount));	//indicate that the value changed
			}			
		}

	/**The converter for this component.*/
	private Converter<V, String> converter;

		/**@return The converter for this component.*/
		public Converter<V, String> getConverter() {return converter;}

		/**Sets the converter.
		This is a bound property
		@param newConverter The converter for this component.
		@exception NullPointerException if the given converter is <code>null</code>.
		@see ValueControl#CONVERTER_PROPERTY
		*/
		public void setConverter(final Converter<V, String> newConverter)
		{
			if(converter!=newConverter)	//if the value is really changing
			{
				final Converter<V, String> oldConverter=converter;	//get the old value
				converter=checkNull(newConverter, "Converter cannot be null.");	//actually change the value
				firePropertyChange(CONVERTER_PROPERTY, oldConverter, newConverter);	//indicate that the value changed
				updateText(getModel().getValue());	//update the text, now that we've installed a new converter
			}
		}

	/**The text literal value displayed in the control, or <code>null</code> if there is no literal value.*/
	private String text;

		/**@return The text literal value displayed in the control, or <code>null</code> if there is no literal value.*/
		public String getText() {return text;}

		/**Sets the text literal value displayed in the control.
		This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
		@param newText The text literal value displayed in the control.
		@see #TEXT_PROPERTY
		*/
		public void setText(final String newText)
		{
			if(!ObjectUtilities.equals(text, newText))	//if the value is really changing (compare their values, rather than identity)
			{
				final String oldText=text;	//get the old value
				text=newText;	//actually change the value
				firePropertyChange(TEXT_PROPERTY, oldText, newText);	//indicate that the value changed
				
//TODO fix				updateValid();	//TODO testing; comment
			}			
		}

	/**Whether the text literal value represents a valid value for the model.*/
//TODO del	private boolean valid=true;

		/**@return Whether the text literal value represents a valid value for the model.*/
//TODO del		public boolean isValid() {return valid;}

		/**Sets whether the text literal value represents a valid value for the value model.
		This is a bound property of type <code>Boolean</code>.
		@param newValid <code>true</code> if the text literal and model value should be considered valid.
		@see #VALID_PROPERTY
		*/
/*TODO del
		public void setValid(final boolean newValid)
		{
			if(valid!=newValid)	//if the value is really changing
			{
				final boolean oldValid=valid;	//get the current value
				valid=newValid;	//update the value
				firePropertyChange(VALID_PROPERTY, Boolean.valueOf(oldValid), Boolean.valueOf(newValid));
			}
		}
*/

	/**@return The validator for the model, or <code>null</code> if no validator is installed.*/
	public Validator<V> getValidator()
	{
		return getModel().getValidator();	//TODO testing
	}

	/**Sets the validator.
	This is a bound property
	@param newValidator The validator for the model, or <code>null</code> if no validator should be used.
	@see #VALIDATOR_PROPERTY
	*/
	public void setValidator(final Validator<V> newValidator)
	{
		getModel().setValidator(newValidator);	//TODO testing
	}

	/**Returns the model value class.
	@return The class representing the type of value the model can hold.
	@see ValueModel#getValueClass()
	*/
	public Class<V> getValueClass()
	{
		return getModel().getValueClass();	//return the value class of the model
	}

	/**The property change listener that updates the text in response to a property changing.*/
	private final PropertyChangeListener updateTextPropertyChangeListener;

	/**Session constructor with a default data model to represent a given type and a default converter.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	*/
	public AbstractTextControl(final GuiseSession session, final Class<V> valueClass)
	{
		this(session, null, valueClass);	//construct the component, indicating that a default ID should be used
	}

	/**Session, and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public AbstractTextControl(final GuiseSession session, final ValueModel<V> model)
	{
		this(session, null, model);	//construct the component, indicating that a default ID should be used				
	}

	/**Session, model, and converter constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param converter The converter for this component.
	@exception NullPointerException if the given session, model, and/or converter is <code>null</code>.
	*/
	public AbstractTextControl(final GuiseSession session, final ValueModel<V> model, final Converter<V, String> converter)
	{
		this(session, null, model, converter);	//construct the component, indicating that a default ID should be used		
	}

	/**Session and ID constructor with a default data model to represent a given type and a default converter.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractTextControl(final GuiseSession session, final String id, final Class<V> valueClass)
	{
		this(session, id, new DefaultValueModel<V>(session, valueClass));	//construct the class with a default model
	}

	/**Session, ID, and model constructor with a default converter.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractTextControl(final GuiseSession session, final String id, final ValueModel<V> model)
	{
		this(session, id, model, AbstractStringLiteralConverter.getInstance(session, model.getValueClass()));	//construct the class with a default converter
	}

	/**Session, ID, model, and converter constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param converter The converter for this component.
	@exception NullPointerException if the given session, model, and/or converter is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractTextControl(final GuiseSession session, final String id, final ValueModel<V> model, final Converter<V, String> converter)
	{
		super(session, id, model);	//construct the parent class
		this.converter=checkNull(converter, "Converter cannot be null");	//save the converter
		updateText(model.getValue());	//initialize the text with the literal form of the initial model value
		updateTextPropertyChangeListener=new PropertyChangeListener()	//create a listener to update the text in response to a property changing
				{
					public void propertyChange(final PropertyChangeEvent propertyChangeEvent)	//if the property changes
					{
						updateText(model.getValue());	//update the text with the value from the model
					}
				};
		model.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, updateTextPropertyChangeListener);	//listen for the model changing value, and update the text in response
		session.addPropertyChangeListener(GuiseSession.LOCALE_PROPERTY, updateTextPropertyChangeListener);	//listen for the session locale changing, in case the converter is locale-dependent TODO allow for unregistration to prevent memory leaks
	}

	/**Updates the component text with literal form of the given value.
	@param value The value to convert and store in the literal text property.
	@see Converter#convertValue(Object)
	@see #setText(String)
	*/
	protected void updateText(final V value)	//TODO remove the value parameter and just get it from the model
	{
		final Converter<V, String> converter=getConverter();	//get the current converter
		try
		{
			final String newText=converter.convertValue(value);	//convert the value to text
			setText(newText);	//convert the value to text
		}
		catch(final ConversionException conversionException)	//TODO fix better; decide what to do if there is an error here
		{
			throw new AssertionError(conversionException);
		}									
	}

	/**Determines whether the models of this component and all of its child components are valid.
	This version in addition to default functionality checks to make sure the literal text representation matches the value.
	@return Whether the models of this component and all of its child components are valid.
	*/
	public boolean isValid()
	{
//TODO also use the converter to make sure the converted text is valid
		if(!super.isValid())	//if the super class is valid, check the validity of the text
		{
			return false;	//this class isn't valid
		}
		try
		{
			final V value=getConverter().convertLiteral(getText());	//see if the literal text can correctly be converted
			final Validator<V> validator=getModel().getValidator();	//see if there is a validator installed
			if(validator!=null)	//if there is a validator installed
			{
				validator.validate(value);	//validate the value represented by the literal text
			}
		}
		catch(final ComponentException componentException)	//if there is a component error
		{
			return false;	//either the literal isn't valid or its converted value is not valid
		}
		return true;	//the values passed all validity checks
//TODO del when works, but update comment		return super.isValid() && getConverter().isValidLiteral(getText());	//if this component is valid, make sure the literal value is valid, too (due to rounding, the displayed text may not represent the exact value as the literal)
	}

/*TODO fix
	protected boolean determineValidity()
	{
		try
		{
			final V value=getConverter().convertLiteral(getText());	//see if the literal text can correctly be converted
			final Validator<V> validator=getModel().getValidator();	//see if there is a validator installed
			if(validator!=null)	//if there is a validator installed
			{
				validator.validate(value);	//validate the value represented by the literal text
			}
		}
		catch(final ComponentException componentException)	//if there is a component error
		{
			return false;	//either the literal isn't valid or its converted value is not valid
		}
		return true;	//the values passed all validity checks
	}
*/
	
	/**Updates whether the control is valid based upon its current UI state.
	This version checks to see if the current literal text can be converted to a valid value.
	@see #getText()
	@see #isValid()
	@see #setValid(boolean)
	*/
/*TODO fix
	protected void updateValid()
	{
		setValid(determineValidity());
	}
*/

	/**Validates the model of this component and all child components.
	The component will be updated with error information.
	This version also validates the literal text.
	@exception ComponentExceptions if there was one or more validation error.
	*/
	public void validate() throws ComponentExceptions
	{
		super.validate();	//validate the super class TODO accumulate errors
		try
		{
			final V value=getConverter().convertLiteral(getText());	//see if the literal text can correctly be converted
			final Validator<V> validator=getModel().getValidator();	//see if there is a validator installed
			if(validator!=null)	//if there is a validator installed
			{
				validator.validate(value);	//validate the value represented by the literal text
			}
		}
		catch(final ComponentException componentException)	//if there is a component error
		{
			componentException.setComponent(this);	//make sure the exception knows to which component it relates
			addError(componentException);	//add this error to the component
			throw new ComponentExceptions(componentException);	//throw a new component exception list exception
		}
	}
}
