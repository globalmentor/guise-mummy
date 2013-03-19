/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.component;

import java.beans.PropertyVetoException;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.net.ContentTypeConstants.*;

import com.globalmentor.io.*;
import com.globalmentor.net.ContentType;

import static com.globalmentor.text.Text.*;
import com.guiseframework.component.transfer.*;
import com.guiseframework.converter.*;
import com.guiseframework.model.*;

/**Control to accept text input from the user representing a particular value type.
This control keeps track of literal text entered by the user, distinct from the value stored in the model.
If line wrap is not specified in the constructor, it defaults to <code>true</code>.
If multiline is not specified in the constructor, it defaults to <code>true</code> only when there is more than one row and line wrap is turned off.
Default converters are available for the following types:
<ul>
	<li><code>char[]</code></li>
	<li><code>java.lang.Boolean</code></li>
	<li><code>java.lang.Float</code></li>
	<li><code>java.lang.Integer</code></li>
	<li><code>java.lang.String</code></li>
</ul>
This component installs a default export strategy supporting export of the following content types:
<ul>
	<li><code>text/plain</code></li>
</ul>
This component installs a default import strategy supporting import of the following content types:
<ul>
	<li><code>text/*</code></li>
</ul>
This control uses a single line feed character to represent each line break.
@param <V> The type of value the input text is to represent.
@author Garret Wilson
*/
public class TextControl<V> extends AbstractTextControl<V>
{

	/**The line wrap bound property.*/
	public final static String LINE_WRAP_PROPERTY=getPropertyName(TextControl.class, "lineWrap");
	/**The masked bound property.*/
	public final static String MASKED_PROPERTY=getPropertyName(TextControl.class, "masked");
	/**The maximum length bound property.*/
	public final static String MAXIMUM_LENGTH_PROPERTY=getPropertyName(TextControl.class, "maximumLength");
	/**The multiline bound property.*/
	public final static String MULTILINE_PROPERTY=getPropertyName(TextControl.class, "multiline");
	/**The row count bound property.*/
	public final static String ROW_COUNT_PROPERTY=getPropertyName(TextControl.class, "rowCount");

	/**Whether the user input text is masked to prevent viewing of the literal entered value.*/
	private boolean masked=false;

		/**@return Whether the user input text is masked to prevent viewing of the literal entered value.*/
		public boolean isMasked() {return masked;}

		/**Sets whether the user input text is masked to prevent viewing of the literal entered value.
		This is a bound property of type <code>Boolean</code>.
		@param newMasked <code>true</code> if the user input text should be masked.
		@see #MASKED_PROPERTY
		*/
		public void setMasked(final boolean newMasked)
		{
			if(masked!=newMasked)	//if the value is really changing
			{
				final boolean oldEnabled=masked;	//get the old value
				masked=newMasked;	//actually change the value
				firePropertyChange(MASKED_PROPERTY, Boolean.valueOf(oldEnabled), Boolean.valueOf(newMasked));	//indicate that the value changed
			}
		}

	/**The maximum number of input characters to allow, or -1 if there is no maximum length.*/
	private int maximumLength=-1;

		/**@return The maximum number of input characters to allow, or -1 if there is no maximum length.*/
		public int getMaximumLength() {return maximumLength;}

		/**Sets the maximum number of input characters to allow.
		This is a bound property of type <code>Integer</code>.
		@param newMaximumLength The new maximum number of input characters to allow, or -1 if there is no maximum length.
		@see #MAXIMUM_LENGTH_PROPERTY
		*/
		public void setMaximumLength(final int newMaximumLength)
		{
			if(maximumLength!=newMaximumLength)	//if the value is really changing
			{
				final int oldMaximumLength=maximumLength;	//get the old value
				maximumLength=newMaximumLength;	//actually change the value
				firePropertyChange(MAXIMUM_LENGTH_PROPERTY, new Integer(oldMaximumLength), new Integer(newMaximumLength));	//indicate that the value changed
			}
		}

	/**Whether lines will be logically wrapped if needed in the view.*/
	private boolean lineWrap;

		/**@return Whether lines will be logically wrapped in the view if needed.*/
		public boolean isLineWrap() {return lineWrap;}

		/**Sets whether lines will be logically wrapped in the view if needed.
		This is a bound property of type <code>Boolean</code>.
		@param newLineWrap Whether lines should be logically wrapped in the view if needed.
		@see #LINE_WRAP_PROPERTY
		*/
		public void setLineWrap(final boolean newLineWrap)
		{
			if(lineWrap!=newLineWrap)	//if the value is really changing
			{
				final boolean oldLineWrap=lineWrap;	//get the old value
				lineWrap=newLineWrap;	//actually change the value
				firePropertyChange(LINE_WRAP_PROPERTY, oldLineWrap, newLineWrap);	//indicate that the value changed
			}
		}

	/**Whether the user is allowed to enter multiple physical lines if the control has multiple rows.*/
	private boolean multiline;

		/**@return Whether the user is allowed to enter multiple physical lines if the control has multiple rows.*/
		public boolean isMultiline() {return multiline;}

		/**Sets whether the user is allowed to enter multiple physical lines if the control has multiple rows.
		This is a bound property of type <code>Boolean</code>.
		@param newMultiline Whether the user should be allowed to enter multiple physical lines if the control has multiple rows.
		@see #MULTILINE_PROPERTY
		*/
		public void setMultiline(final boolean newMultiline)
		{
			if(multiline!=newMultiline)	//if the value is really changing
			{
				final boolean oldMultiline=multiline;	//get the old value
				multiline=newMultiline;	//actually change the value
				firePropertyChange(MULTILINE_PROPERTY, oldMultiline, newMultiline);	//indicate that the value changed
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

	/**The default export strategy for this component type.*/
	protected final static ExportStrategy<TextControl<?>> DEFAULT_EXPORT_STRATEGY=new ExportStrategy<TextControl<?>>()
			{
				/**Exports data from the given component.
				@param component The component from which data will be transferred.
				@return The object to be transferred, or <code>null</code> if no data can be transferred.
				*/
				public Transferable<TextControl<?>> exportTransfer(final TextControl<?> component)
				{
					return new DefaultTransferable(component);	//return a default transferable for this component
				}
			};

	/**The default import strategy for this component type.*/
	protected final static ImportStrategy<TextControl<?>> DEFAULT_IMPORT_STRATEGY=new ImportStrategy<TextControl<?>>()	//add a new import strategy for this component
			{
				/**Determines whether this strategy can import the given transferable object.
				This implementation accepts all transferables providing <code>text/*</code> data.
				@param component The component into which the object will be transferred.
				@param transferable The object to be transferred.
				@return <code>true</code> if the given object can be imported.
				*/
				public boolean canImportTransfer(final TextControl<?> component, final Transferable<?> transferable)
				{
					return transferable.canTransfer(ContentType.create(ContentType.TEXT_PRIMARY_TYPE, ContentType.WILDCARD_SUBTYPE));	//we can import any text
				}

				/**Imports the given data into the given component.
				This implementation imports the first available <code>text/*</code> data.
				@param component The component into which the object will be transferred.
				@param transferable The object to be transferred.
				@return <code>true</code> if the given object was be imported.
				*/
				public boolean importTransfer(final TextControl<?> component, final Transferable<?> transferable)
				{
					boolean imported=false;	//we'll assume we didn't import anything
					Object data=null;	//we'll store here any data we retrieve
					if(transferable.canTransfer(PLAIN_CONTENT_TYPE))	//text/plain is our favorite type; if we can import it
					{
						data=transferable.transfer(PLAIN_CONTENT_TYPE);	//transfer the data
						imported=true;	//indicate that we transported data
					}
					else	//otherwise, check for text/* types
					{
						for(final ContentType contentType:transferable.getContentTypes())	//for each available content type
						{
							if(contentType.match(ContentType.TEXT_PRIMARY_TYPE, ContentType.WILDCARD_SUBTYPE))	//if this is a text content type
							{
								data=transferable.transfer(contentType);	//transfer the data
								imported=true;	//indicate that we transported data
								break;	//stop looking for a match
							}
						}
					}
					if(imported && data!=null)	//if we transferred data
					{
						final String oldText=component.getText();	//get the current text
						final StringBuilder newTextStringBuilder=new StringBuilder();	//create a string builder to collect our new information
						if(oldText!=null)	//if there is content already
						{
							newTextStringBuilder.append(oldText);	//add the old content
						}
						newTextStringBuilder.append(data);	//append the data
						final String newText=newTextStringBuilder.toString();	//get the new text
						try
						{
							component.setTextValue(newText);	//update the literal text of the component, which will in turn update the provisional text of the component, and then update the value
						}
						catch(final ConversionException conversionException)	//if there is a conversion error
						{
							component.setNotification(new Notification(conversionException));	//add this error to the component
							imported=false;	//transfer was unsuccessful
						}
						catch(final PropertyVetoException propertyVetoException)	//if there is a veto
						{
							final Throwable cause=propertyVetoException.getCause();	//get the cause of the veto, if any
							component.setNotification(new Notification(cause!=null ? cause : propertyVetoException));	//add notification of the error to the component
							imported=false;	//transfer was unsuccessful
						}
					}
					return imported;	//indicate whether we were able to find any information to transfer
				}

				/**Imports the given text into the given component.
				@param component The component into which the data will be transferred.
				@param data The data to be transferred.
				*/
				protected void importTransfer(final TextControl<?> component, final Object data)
				{
					if(data!=null)	//if we transferred data
					{
						final String oldText=component.getText();	//get the current text
						final StringBuilder newTextStringBuilder=new StringBuilder();	//create a string builder to collect our new information
						if(oldText!=null)	//if there is content already
						{
							newTextStringBuilder.append(oldText);	//add the old content
						}
						newTextStringBuilder.append(data);	//append the data
						final String newText=newTextStringBuilder.toString();	//get the new text
						try
						{
							component.setTextValue(newText);	//update the literal text of the component, which will in turn update the provisional text of the component, and then update the value
						}
						catch(final ConversionException conversionException)	//if there is a conversion error
						{
							component.setNotification(new Notification(conversionException));	//add this error to the component
						}
						catch(final PropertyVetoException propertyVetoException)	//if there is a veto
						{
							final Throwable cause=propertyVetoException.getCause();	//get the cause of the veto, if any
							component.setNotification(new Notification(cause!=null ? cause : propertyVetoException));	//add notification of the error to the component
						}
					}
				}
			};

	/**Value class constructor with a default data model to represent a given type and a default converter.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public TextControl(final Class<V> valueClass)
	{
		this(valueClass, null);	//construct the class with a null default value
	}

	/**Value class and default value constructor with a default data model to represent a given type and a default converter.
	@param valueClass The class indicating the type of value held in the model.
	@param defaultValue The default value, which will not be validated.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public TextControl(final Class<V> valueClass, final V defaultValue)
	{
		this(new DefaultValueModel<V>(valueClass, defaultValue));	//construct the class with a default model
	}

	/**Value class and column count constructor with one row and a default converter.
	@param valueClass The class indicating the type of value held in the model.
	@param columnCount The requested number of visible columns, or -1 if no column count is specified.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public TextControl(final Class<V> valueClass, final int columnCount)
	{
		this(valueClass, 1, columnCount);	//construct the class with one row
	}

	/**Value class, row count, and column count constructor with a default converter.
	@param valueClass The class indicating the type of value held in the model.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@param columnCount The requested number of visible columns, or -1 if no column count is specified.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public TextControl(final Class<V> valueClass, final int rowCount, final int columnCount)
	{
		this(valueClass, null, rowCount, columnCount);	//construct the class with a null default value
	}

	/**Value class, defaultValue, and column count constructor with one row a default converter.
	@param valueClass The class indicating the type of value held in the model.
	@param defaultValue The default value, which will not be validated.
	@param columnCount The requested number of visible columns, or -1 if no column count is specified.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public TextControl(final Class<V> valueClass, final V defaultValue, final int columnCount)
	{
		this(valueClass, defaultValue, 1, columnCount);	//construct the class with one row
	}

	/**Value class, defaultValue, row count, and column count constructor with a default converter.
	@param valueClass The class indicating the type of value held in the model.
	@param defaultValue The default value, which will not be validated.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@param columnCount The requested number of visible columns, or -1 if no column count is specified.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public TextControl(final Class<V> valueClass, final V defaultValue, final int rowCount, final int columnCount)
	{
		this(valueClass, defaultValue, rowCount, columnCount, true);	//default to line-wrapping
	}

	/**Value class, row count, column count, and line wrap constructor with a default converter.
	@param valueClass The class indicating the type of value held in the model.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@param columnCount The requested number of visible columns, or -1 if no column count is specified.
	@param lineWrap Whether lines should be wrapped in the view if needed.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public TextControl(final Class<V> valueClass, final int rowCount, final int columnCount, final boolean lineWrap)
	{
		this(valueClass, null, rowCount, columnCount, lineWrap);	//construct the class with a null default value
	}

	/**Value class, default value, row count, column count, and line wrap constructor with a default converter.
	@param valueClass The class indicating the type of value held in the model.
	@param defaultValue The default value, which will not be validated.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@param columnCount The requested number of visible columns, or -1 if no column count is specified.
	@param lineWrap Whether lines should be wrapped in the view if needed.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public TextControl(final Class<V> valueClass, final V defaultValue, final int rowCount, final int columnCount, final boolean lineWrap)
	{
		this(new DefaultValueModel<V>(valueClass, defaultValue), rowCount, columnCount, lineWrap);	//construct the class with a default model
	}

	/**Value model, row count, and column count constructor with a default converter.
	@param valueModel The component value model.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@param columnCount The requested number of visible columns, or -1 if no column count is specified.
	@exception NullPointerException if the given value model is <code>null</code>.
	*/
	public TextControl(final ValueModel<V> valueModel, final int rowCount, final int columnCount)
	{
		this(valueModel, rowCount, columnCount, true);	//default to line-wrapping
	}

	/**Value model, row count, column count, and line wrap constructor with a default converter.
	@param valueModel The component value model.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@param columnCount The requested number of visible columns, or -1 if no column count is specified.
	@param lineWrap Whether lines should be wrapped in the view if needed.
	@exception NullPointerException if the given value model is <code>null</code>.
	*/
	public TextControl(final ValueModel<V> valueModel, final int rowCount, final int columnCount, final boolean lineWrap)
	{
		this(valueModel, AbstractStringLiteralConverter.getInstance(valueModel.getValueClass()), rowCount, columnCount, lineWrap);	//construct the class with a default converter
	}

	/**Value model constructor with a default converter.
	@param valueModel The component value model.
	@exception NullPointerException if the given value model is <code>null</code>.
	*/
	public TextControl(final ValueModel<V> valueModel)
	{
		this(valueModel, AbstractStringLiteralConverter.getInstance(valueModel.getValueClass()));	//construct the class with a default converter
	}

	/**Value model and converter constructor.
	@param valueModel The component value model.
	@param converter The converter for this component.
	@exception NullPointerException if the given value model and/or converter is <code>null</code>.
	*/
	public TextControl(final ValueModel<V> valueModel, final Converter<V, String> converter)
	{
		this(valueModel, converter, 1, -1, true);	//construct the class with one row, defaulting to line wrap
	}

	/**Value model, converter, row count, column count, and line wrap constructor.
	@param valueModel The component value model.
	@param converter The converter for this component.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@param columnCount The requested number of visible columns, or -1 if no column count is specified.
	@param lineWrap Whether lines should be wrapped in the view if needed.
	@exception NullPointerException if the given value model and/or converter is <code>null</code>.
	*/
	public TextControl(final ValueModel<V> valueModel, final Converter<V, String> converter, final int rowCount, final int columnCount, final boolean lineWrap)
	{
		this(valueModel, converter, rowCount, columnCount, lineWrap, rowCount>1 && !lineWrap);	//only turn on multiline initially if there are multiple rows and line wrap is off
	}

	/**Value model, converter, row count, column count, and line wrap constructor.
	@param valueModel The component value model.
	@param converter The converter for this component.
	@param rowCount The requested number of visible rows, or -1 if no row count is specified.
	@param columnCount The requested number of visible columns, or -1 if no column count is specified.
	@param lineWrap Whether lines should be wrapped in the view if needed.
	@param multiline Whether the user should be allowed to enter multiple physical lines if the control has multiple rows.
	@exception NullPointerException if the given value model and/or converter is <code>null</code>.
	*/
	public TextControl(final ValueModel<V> valueModel, final Converter<V, String> converter, final int rowCount, final int columnCount, final boolean lineWrap, final boolean multiline)
	{
		super(valueModel, converter);	//construct the parent class
		this.rowCount=rowCount;
		setColumnCount(columnCount);
		this.lineWrap=lineWrap;
		this.multiline=multiline;
		addExportStrategy(DEFAULT_EXPORT_STRATEGY);	//install a default export strategy
		addImportStrategy(DEFAULT_IMPORT_STRATEGY);	//install a default import strategy
	}

	/**The default transferable object for a text control.
	@author Garret Wilson
	*/
	protected static class DefaultTransferable extends AbstractTransferable<TextControl<?>>
	{
		/**Source constructor.
		@param source The source of the transferable data.
		@exception NullPointerException if the provided source is <code>null</code>.
		*/
		public DefaultTransferable(final TextControl<?> source)
		{
			super(source);	//construct the parent class
		}

		/**Determines the content types available for this transfer.
		This implementation returns the <code>text/plain</code> content type.
		@return The content types available for this transfer.
		*/
		public ContentType[] getContentTypes() {return new ContentType[]{PLAIN_CONTENT_TYPE};}

		/**Transfers data using the given content type.
		@param contentType The type of data expected.
		@return The transferred data, which may be <code>null</code>.
		@exception IllegalArgumentException if the given content type is not supported.
		*/
		public Object transfer(final ContentType contentType)
		{
			if(contentType.hasBaseType(PLAIN_CONTENT_TYPE))	//if they request the supported content type
			{
				return getSource().getText();	//return the current text
			}
			else	//if we don't support this content type
			{
				throw new IllegalArgumentException("Content type not supported: "+contentType);
			}
		}
	}

}
