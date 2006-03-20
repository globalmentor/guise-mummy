package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.converter.*;
import com.guiseframework.model.*;
import com.guiseframework.validator.Validator;

import static com.garretwilson.lang.ObjectUtilities.*;

/**Control to accept text input from the user representing a particular value type.
This control keeps track of literal text entered by the user, distinct from the value stored in the model.
The component valid status is updated before any literal text change event is fired. 
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
	/**The provisional text literal bound property.*/
	public final static String PROVISIONAL_TEXT_PROPERTY=getPropertyName(AbstractTextControl.class, "provisionalText");
	/**The text literal bound property.*/
	public final static String TEXT_PROPERTY=getPropertyName(AbstractTextControl.class, "text");

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
				updateText(getValue());	//update the text, now that we've installed a new converter
			}
		}

	/**The provisional text literal value, or <code>null</code> if there is no provisional literal value.*/
	private String provisionalText;

		/**@return The provisional text literal value, or <code>null</code> if there is no provisional literal value.*/
		public String getProvisionalText() {return provisionalText;}

		/**Sets the provisional text literal value.
		This method updates the valid status before firing a change event.
		This is a bound property.
		@param newProvisionalText The provisional text literal value.
		@see #PROVISIONAL_TEXT_PROPERTY
		*/
		public void setProvisionalText(final String newProvisionalText)
		{
			if(!ObjectUtilities.equals(provisionalText, newProvisionalText))	//if the value is really changing (compare their values, rather than identity)
			{
				final String oldProvisionalText=provisionalText;	//get the old value
				provisionalText=newProvisionalText;	//actually change the value
				updateValid();	//update the valid status before firing the text change property so that any listeners will know the valid status
				firePropertyChange(PROVISIONAL_TEXT_PROPERTY, oldProvisionalText, newProvisionalText);	//indicate that the value changed
			}			
		}

	/**The text literal value displayed in the control, or <code>null</code> if there is no literal value.*/
	private String text;

		/**@return The text literal value displayed in the control, or <code>null</code> if there is no literal value.*/
		public String getText() {return text;}

		/**Sets the text literal value displayed in the control.
		This method updates the provisional text to match and updates the valid status if needed.
		This is a bound property.
		@param newText The text literal value displayed in the control.
		@see #TEXT_PROPERTY
		*/
		public void setText(final String newText)
		{
			if(!ObjectUtilities.equals(text, newText))	//if the value is really changing (compare their values, rather than identity)
			{
				final String oldText=text;	//get the old value
				text=newText;	//actually change the value
				final String oldProvisionalText=getProvisionalText();	//get the current provisional text
				final boolean provisionalTextAlreadyMatched=ObjectUtilities.equals(text, getProvisionalText());	//see if the provisional text already matches our new text value
				setProvisionalText(text);	//update the provisional text before firing a text change property so that the valid state will be updated 
				if(provisionalTextAlreadyMatched)	//if the provisional text already matched the new text, it didn't update validity, so we need to do that here
				{
					updateValid();	//update the valid status before firing the text change property so that any listeners will know the valid status; this will also update the status, which is important because the text and provisional text values are now the same					
				}
				firePropertyChange(TEXT_PROPERTY, oldText, newText);	//indicate that the value changed
			}			
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

	/**Checks the state of the component for validity.
	This version in addition to default functionality checks to make sure the literal text can be converted to a valid value.
	The provisional text is checked for validity, because it represents the latest available input from the user.
	@return <code>true</code> if the component and all children passes all validity tests, else <code>false</code>.
	@see #getProvisionalText()
	*/ 
	protected boolean determineValid()
	{
		if(!super.determineValid())	//if we don't pass the default validity checks
		{
			return false;	//the component isn't valid
		}
		try
		{
			final V value=getConverter().convertLiteral(getProvisionalText());	//see if the provisional literal text can correctly be converted
			final Validator<V> validator=getValidator();	//see if there is a validator installed
			if(validator!=null)	//if there is a validator installed
			{
				if(!validator.isValid(value))	//if the value represented by the literal text is not valid
				{
					return false;	//the converted value isn't valid
				}
			}
		}
		catch(final ConversionException conversionException)	//if we can't convert the literal text to a value
		{
			return false;	//the literal isn't valid
		}
		return true;	//the values passed all validity checks
	}

	/**Checks the user input status of the control.
	This version checks to see if the provisional literal text can be converted to a valid value.
	If the provisional literal text cannot be converted, the status is determined to be {@link Status#ERROR}.
	If the provisional literal text can be converted but the converted value is invalid,
		the status is determined to be {@link Status#WARNING} unless the provisional text is the same as the literal text,
		in which case the status is determined to be {@link Status#ERROR}.
	The default value, even if invalid, is considered valid.
	@return The current user input status of the control.
	@see #getProvisionalText()
	*/ 
	protected Status determineStatus()
	{
		Status status=super.determineStatus();	//do the defualt status checks
		if(status==null)	//if no status is reported
		{
			try
			{
				final String provisionalText=getProvisionalText();	//get the provisional literal text
				final V value=getConverter().convertLiteral(provisionalText);	//see if the provisional literal text can correctly be converted
				if(!ObjectUtilities.equals(value, getDefaultValue()))	//don't count the value as invalid if it is equal to the default value
				{
					final Validator<V> validator=getValidator();	//see if there is a validator installed
					if(validator!=null)	//if there is a validator installed
					{
						if(!validator.isValid(value))	//if the value represented by the provisional literal text is not valid
						{
							if(ObjectUtilities.equals(provisionalText, getText()))	//if the invalid provisional literal text is equal to the current literal test
							{
								status=Status.ERROR;	//the invalid value has already been committed, so mark it as an error
							}
							else	//if the invalid valud isn't equal to the current value
							{
								status=Status.WARNING;	//the invalid value hasn't been committed; mark it as a warning
							}
						}
					}
				}
			}
			catch(final ConversionException conversionException)	//if we can't convert the provisional literal text to a value
			{
				status=Status.ERROR;	//conversion problems are errors
			}
		}
		return status;	//return the determined status
	}
	
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
			final Validator<V> validator=getValidator();	//see if there is a validator installed
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
