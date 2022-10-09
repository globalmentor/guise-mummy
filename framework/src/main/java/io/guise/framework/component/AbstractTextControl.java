/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.component;

import java.beans.*;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.util.Objects.*;

import com.globalmentor.net.MediaType;
import com.globalmentor.text.Text;

import io.guise.framework.GuiseSession;
import io.guise.framework.converter.*;
import io.guise.framework.model.*;
import io.guise.framework.validator.*;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.text.Text.*;

/**
 * Control to accept text input from the user representing a particular value type. This control keeps track of literal text entered by the user, distinct from
 * the value stored in the model. The component valid status is updated before any literal text change event is fired. Default converters are available for the
 * following types:
 * <ul>
 * <li><code>char[]</code></li>
 * <li><code>java.lang.Boolean</code></li>
 * <li><code>java.lang.Float</code></li>
 * <li><code>java.lang.Integer</code></li>
 * <li><code>java.lang.String</code></li>
 * </ul>
 * This control uses a single line feed character to represent each line break.
 * @param <V> The type of value the input text is to represent.
 * @author Garret Wilson
 */
public class AbstractTextControl<V> extends AbstractEditValueControl<V> {

	/** The auto commit pattern bound property. */
	public static final String AUTO_COMMIT_PATTERN_PROPERTY = getPropertyName(AbstractTextControl.class, "autoCommitPattern");
	/** The column count bound property. */
	public static final String COLUMN_COUNT_PROPERTY = getPropertyName(AbstractTextControl.class, "columnCount");
	/** The provisional text literal bound property. */
	public static final String PROVISIONAL_TEXT_PROPERTY = getPropertyName(AbstractTextControl.class, "provisionalText");
	/** The text literal bound property. */
	public static final String TEXT_PROPERTY = getPropertyName(AbstractTextControl.class, "text");
	/** The value content type bound property. */
	public static final String VALUE_CONTENT_TYPE_PROPERTY = getPropertyName(AbstractTextControl.class, "valueContentType");

	/**
	 * The regular expression pattern that will cause the text automatically to be committed immediately, or <code>null</code> if text should not be committed
	 * during entry.
	 */
	private Pattern autoCommitPattern = null;

	/**
	 * @return The regular expression pattern that will cause the text automatically to be committed immediately, or <code>null</code> if text should not be
	 *         committed during entry.
	 */
	public Pattern getAutoCommitPattern() {
		return autoCommitPattern;
	}

	/**
	 * Sets the The regular expression pattern that will cause the text automatically to be committed immediately. This is a bound property.
	 * @param newAutoCommitPattern The regular expression pattern that will cause the text automatically to be committed immediately, or <code>null</code> if text
	 *          should not be committed during entry.
	 * @see #AUTO_COMMIT_PATTERN_PROPERTY
	 */
	public void setAutoCommitPattern(final Pattern newAutoCommitPattern) {
		if(!Objects.equals(autoCommitPattern, newAutoCommitPattern)) { //if the value is really changing (compare their values, rather than identity)
			final Pattern oldAutoCommitPattern = autoCommitPattern; //get the old value
			autoCommitPattern = newAutoCommitPattern; //actually change the value
			firePropertyChange(AUTO_COMMIT_PATTERN_PROPERTY, oldAutoCommitPattern, newAutoCommitPattern); //indicate that the value changed
		}
	}

	/** The estimated number of columns requested to be visible, or -1 if no column count is specified. */
	private int columnCount = -1;

	/** @return The estimated number of columns requested to be visible, or -1 if no column count is specified. */
	public int getColumnCount() {
		return columnCount;
	}

	/**
	 * Sets the estimated number of columns requested to be visible. This is a bound property of type <code>Integer</code>.
	 * @param newColumnCount The new requested number of visible columns, or -1 if no column count is specified.
	 * @see #COLUMN_COUNT_PROPERTY
	 */
	public void setColumnCount(final int newColumnCount) {
		if(columnCount != newColumnCount) { //if the value is really changing
			final int oldColumnCount = columnCount; //get the old value
			columnCount = newColumnCount; //actually change the value
			firePropertyChange(COLUMN_COUNT_PROPERTY, oldColumnCount, newColumnCount); //indicate that the value changed
		}
	}

	/** The converter for this component. */
	private Converter<V, String> converter;

	/** @return The converter for this component. */
	public Converter<V, String> getConverter() {
		return converter;
	}

	/**
	 * Sets the converter. This is a bound property
	 * @param newConverter The converter for this component.
	 * @throws NullPointerException if the given converter is <code>null</code>.
	 * @see ValueControl#CONVERTER_PROPERTY
	 */
	public void setConverter(final Converter<V, String> newConverter) {
		if(converter != newConverter) { //if the value is really changing
			final Converter<V, String> oldConverter = converter; //get the old value
			converter = requireNonNull(newConverter, "Converter cannot be null."); //actually change the value
			firePropertyChange(CONVERTER_PROPERTY, oldConverter, newConverter); //indicate that the value changed
			updateText(); //update the text, now that we've installed a new converter
		}
	}

	/** The provisional text literal value, or <code>null</code> if there is no provisional literal value. */
	private String provisionalText;

	/** @return The provisional text literal value, or <code>null</code> if there is no provisional literal value. */
	public String getProvisionalText() {
		return provisionalText;
	}

	/**
	 * Sets the provisional text literal value. This method updates the valid status before firing a change event. This is a bound property.
	 * @param newProvisionalText The provisional text literal value.
	 * @see #PROVISIONAL_TEXT_PROPERTY
	 */
	public void setProvisionalText(final String newProvisionalText) {
		if(!Objects.equals(provisionalText, newProvisionalText)) { //if the value is really changing (compare their values, rather than identity)
			final String oldProvisionalText = provisionalText; //get the old value
			provisionalText = newProvisionalText; //actually change the value
			updateValid(); //update the valid status before firing the text change property so that any listeners will know the valid status
			firePropertyChange(PROVISIONAL_TEXT_PROPERTY, oldProvisionalText, newProvisionalText); //indicate that the value changed
		}
	}

	/** The text literal value displayed in the control, or <code>null</code> if there is no literal value. */
	private String text;

	/** @return The text literal value displayed in the control, or <code>null</code> if there is no literal value. */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text literal value displayed in the control. This method updates the provisional text to match and updates the valid status if needed. This is a
	 * bound property.
	 * @param newText The text literal value displayed in the control.
	 * @see #TEXT_PROPERTY
	 */
	public void setText(final String newText) {
		if(!Objects.equals(text, newText)) { //if the value is really changing (compare their values, rather than identity)
			final String oldText = text; //get the old value
			text = newText; //actually change the value
			final String oldProvisionalText = getProvisionalText(); //get the current provisional text
			final boolean provisionalTextAlreadyMatched = Objects.equals(text, getProvisionalText()); //see if the provisional text already matches our new text value
			setProvisionalText(text); //update the provisional text before firing a text change property so that the valid state will be updated 
			if(provisionalTextAlreadyMatched) { //if the provisional text already matched the new text, it didn't update validity, so we need to do that here
				updateValid(); //update the valid status before firing the text change property so that any listeners will know the valid status; this will also update the status, which is important because the text and provisional text values are now the same					
			}
			firePropertyChange(TEXT_PROPERTY, oldText, newText); //indicate that the value changed
		}
	}

	/**
	 * Sets the text literal value displayed in the control, and then converts the text to an appropriate value and stores it. This is a convenience method.
	 * @param newText The new text literal value to display in the control and then convert and store as a value.
	 * @see #setText(String)
	 * @see #getConverter()
	 * @see Converter#convertLiteral(Object)
	 * @see #setValue(Object)
	 * @throws ConversionException if the literal value cannot be converted.
	 * @throws PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	 */
	public void setTextValue(final String newText) throws ConversionException, PropertyVetoException {
		setText(newText); //update the literal text of the component, which will in turn update the provisional text of the component
		final Converter<V, String> converter = getConverter(); //get the component's converter
		final V value = converter.convertLiteral(newText); //convert the literal text value, throwing an exception if the value cannot be converted
		setValue(value); //store the value in the model, throwing an exception if the value is invalid
	}

	/** The content type of the value. */
	private MediaType valueContentType;

	/** @return The content type of the value. */
	public MediaType getValueContentType() {
		return valueContentType;
	}

	/**
	 * Sets the content type of the value. This is a bound property.
	 * @param newValueContentType The new value content type.
	 * @throws NullPointerException if the given content type is <code>null</code>.
	 * @throws IllegalArgumentException if the given content type is not a text content type.
	 * @see #VALUE_CONTENT_TYPE_PROPERTY
	 */
	public void setValueContentType(final MediaType newValueContentType) {
		requireNonNull(newValueContentType, "Content type cannot be null.");
		if(valueContentType != newValueContentType) { //if the value is really changing
			final MediaType oldValueContentType = valueContentType; //get the old value
			if(!isText(newValueContentType)) { //if the new content type is not a text content type
				throw new IllegalArgumentException("Content type " + newValueContentType + " is not a text content type.");
			}
			valueContentType = newValueContentType; //actually change the value
			firePropertyChange(VALUE_CONTENT_TYPE_PROPERTY, oldValueContentType, newValueContentType); //indicate that the value changed
		}
	}

	/** The property change listener that updates the text in response to a property changing. */
	private final PropertyChangeListener updateTextPropertyChangeListener = new PropertyChangeListener() { //create a listener to update the text in response to a property changing

		@Override
		public void propertyChange(final PropertyChangeEvent propertyChangeEvent) { //if the property changes
			updateText(); //update the text with the new value from the model
		}

	};

	/**
	 * Value class constructor with a default data model to represent a given type and a default converter.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public AbstractTextControl(final Class<V> valueClass) {
		this(new DefaultValueModel<V>(valueClass)); //construct the class with a default model
	}

	/**
	 * Value model constructor with a default converter.
	 * @param valueModel The component data model.
	 * @throws NullPointerException if the given value model is <code>null</code>.
	 */
	public AbstractTextControl(final ValueModel<V> valueModel) {
		this(valueModel, AbstractStringLiteralConverter.getInstance(valueModel.getValueClass())); //construct the class with a default converter
	}

	/**
	 * Value model and converter constructor.
	 * @param valueModel The component value model.
	 * @param converter The converter for this component.
	 * @throws NullPointerException if the given value model and/or converter is <code>null</code>.
	 */
	public AbstractTextControl(final ValueModel<V> valueModel, final Converter<V, String> converter) {
		super(new DefaultInfoModel(), valueModel, new DefaultEnableable()); //construct the parent class
		this.valueContentType = Text.PLAIN_MEDIA_TYPE;
		this.converter = requireNonNull(converter, "Converter cannot be null"); //save the converter
		updateText(); //initialize the text with the literal form of the initial model value
		addPropertyChangeListener(VALUE_PROPERTY, updateTextPropertyChangeListener); //listen for the value changing, and update the text in response
		getSession().addPropertyChangeListener(GuiseSession.LOCALE_PROPERTY, updateTextPropertyChangeListener); //listen for the session locale changing, in case the converter is locale-dependent TODO allow for unregistration to prevent memory leaks
	}

	//TODO important; remove this hack and make work with models, compensating for delayed listeners somehow; right now this results in the text being updated twice; once immediately when the value changes, and another when the value property change listener is fired (with delayed events)
	/*TODO fix; temporarily removing delayed events
		public void setValue(final V newValue) throws PropertyVetoException
		{
			super.setValue(newValue);
			updateText();	//update the text with the new value from the model
		}
	*/

	/**
	 * Updates the component text with literal form of the given value.
	 * @see Converter#convertValue(Object)
	 * @see #getValue()
	 * @see #setText(String)
	 */
	protected void updateText() {
		final Converter<V, String> converter = getConverter(); //get the current converter
		try {
			final String newText = converter.convertValue(getValue()); //convert the value to text
			setText(newText); //convert the value to text
		} catch(final ConversionException conversionException) { //TODO fix better; decide what to do if there is an error here
			throw new AssertionError(conversionException);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version performs no additional checks if the control is disabled.
	 * </p>
	 */
	@Override
	protected boolean determineValid() {
		if(!super.determineValid()) { //if we don't pass the default validity checks
			return false; //the component isn't valid
		}
		if(isEnabled()) { //if the control is enabled
			try {
				final V value = getConverter().convertLiteral(getProvisionalText()); //see if the provisional literal text can correctly be converted
				final Validator<V> validator = getValidator(); //see if there is a validator installed
				if(validator != null) { //if there is a validator installed
					if(!validator.isValid(value)) { //if the value represented by the literal text is not valid
						return false; //the converted value isn't valid
					}
				}
			} catch(final ConversionException conversionException) { //if we can't convert the literal text to a value
				return false; //the literal isn't valid
			}
		}
		return true; //the values passed all validity checks
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version checks to see if the provisional literal text can be converted to a valid value. If the provisional literal text cannot be converted, the
	 * status is determined to be {@link Status#ERROR}. If the provisional literal text can be converted but the converted value is invalid, the status is
	 * determined to be {@link Status#WARNING} unless the provisional text is the same as the literal text, in which case the status is determined to be
	 * {@link Status#ERROR}. The default value, even if invalid, is considered valid. If the control is disabled no status is given.
	 * </p>
	 */
	@Override
	protected Status determineStatus() {
		Status status = super.determineStatus(); //do the defualt status checks
		if(status == null && isEnabled()) { //if no status is reported and the control is enabled, check the validity of the text
			try {
				final String provisionalText = getProvisionalText(); //get the provisional literal text
				final V value = getConverter().convertLiteral(provisionalText); //see if the provisional literal text can correctly be converted
				if(!Objects.equals(value, getDefaultValue())) { //don't count the value as invalid if it is equal to the default value
					final Validator<V> validator = getValidator(); //see if there is a validator installed
					if(validator != null) { //if there is a validator installed
						if(!validator.isValid(value)) { //if the value represented by the provisional literal text is not valid
							if(Objects.equals(provisionalText, getText())) { //if the invalid provisional literal text is equal to the current literal test
								status = Status.ERROR; //the invalid value has already been committed, so mark it as an error
							} else { //if the invalid value isn't equal to the current value
								status = Status.WARNING; //the invalid value hasn't been committed; mark it as a warning
							}
						}
					}
				}
			} catch(final ConversionException conversionException) { //if we can't convert the provisional literal text to a value
				status = Status.ERROR; //conversion problems are errors
			}
		}
		return status; //return the determined status
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version performs no additional checks if the control is disabled.
	 * </p>
	 */
	@Override
	public boolean validate() {
		V newValue = null; //we'll convert the literal value and store it here
		Notification newValueNotification = null; //if we have any problems converting the literal value, we'll store a notification here
		if(isEnabled()) { //if the control is enabled, make sure the value reflects the text value (i.e. make sure the value has been committed)
			try {
				newValue = getConverter().convertLiteral(getText()); //see if the literal text can correctly be converted
				setValue(newValue); //update the value, effectively committing the text (this will have no effect if the value isn't really changing)
			} catch(final ConversionException conversionException) { //if there is a conversion error
				newValueNotification = new Notification(conversionException); //indicate that there was a text conversion error
			} catch(final PropertyVetoException propertyVetoException) { //if there is a veto
				final Throwable cause = propertyVetoException.getCause(); //get the cause of the veto, if any
				newValueNotification = new Notification(cause != null ? cause : propertyVetoException); //indicate that there was a commit veto error
			}
		}
		super.validate(); //validate the super class
		if(isEnabled()) { //if the control is enabled
			if(newValueNotification != null) { //if we have a text error already
				setNotification(newValueNotification); //the text conversion error will override other errors (the value may have been invalid, but we want to show that the text's invalidity takes precedence)
			} else { //if we converted the text to a value with no problems, make sure its value is valid TODO is validation of the new value already taken are of above through setting the value?
				try {
					final Validator<V> validator = getValidator(); //see if there is a validator installed
					if(validator != null) { //if there is a validator installed
						validator.validate(newValue); //validate the value represented by the literal text
					}
				} catch(final ValidationException validationException) { //if there is a validation error
					setNotification(new Notification(validationException)); //add notificaiton of this error to the component
				}
			}
		}
		return isValid(); //return the current valid state
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version updates the text to match the new value.
	 * </p>
	 * @see #updateText()
	 */
	@Override
	public void reset() {
		super.reset(); //reset normally
		updateText(); //update the text to match the new value
	}
}
