package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.guiseframework.converter.*;
import com.guiseframework.model.*;

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
public class TextControl<V> extends AbstractTextControl<V, TextControl<V>>
{

	/**The line wrap bound property.*/
	public final static String LINE_WRAP_PROPERTY=getPropertyName(TextControl.class, "lineWrap");
	/**The masked bound property.*/
	public final static String MASKED_PROPERTY=getPropertyName(TextControl.class, "masked");
	/**The maximum length bound property.*/
	public final static String MAXIMUM_LENGTH_PROPERTY=getPropertyName(TextControl.class, "maximumLength");
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

	/**Whether lines will be wrapped if needed in the view.*/
	private boolean lineWrap;

		/**@return Whether lines will be wrapped in the view if needed.*/
		public boolean isLineWrap() {return lineWrap;}

		/**Sets whether lines will be wrapped in the view if needed.
		This is a bound property of type <code>Boolean</code>.
		@param newLineWrap Whether lines should be wrapped in the view if needed.
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
		super(valueModel, converter);	//construct the parent class
		this.rowCount=rowCount;
		setColumnCount(columnCount);
		this.lineWrap=lineWrap;
	}

}
