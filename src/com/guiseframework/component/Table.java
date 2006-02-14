package com.guiseframework.component;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

import javax.mail.internet.ContentType;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.TextUtilities.*;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.converter.AbstractStringLiteralConverter;
import com.guiseframework.converter.ConversionException;
import com.guiseframework.converter.Converter;
import com.guiseframework.event.AbstractGuisePropertyChangeListener;
import com.guiseframework.event.GuisePropertyChangeEvent;
import com.guiseframework.model.*;
import com.guiseframework.validator.*;

/**A table component.
@author Garret Wilson
*/
public class Table extends AbstractCompositeStateComponent<TableModel.Cell<?>, Table.CellComponentState, Table> implements LabeledComponent<Table>
{

	/**@return The data model used by this component.*/
	public TableModel getModel() {return (TableModel)super.getModel();}

	/**The label icon URI, or <code>null</code> if there is no icon URI.*/
	private URI labelIcon=null;

		/**@return The label icon URI, or <code>null</code> if there is no icon URI.*/
		public URI getLabelIcon() {return labelIcon;}

		/**Sets the URI of the label icon.
		This is a bound property of type <code>URI</code>.
		@param newLabelIcon The new URI of the label icon.
		@see #LABEL_ICON_PROPERTY
		*/
		public void setLabelIcon(final URI newLabelIcon)
		{
			if(!ObjectUtilities.equals(labelIcon, newLabelIcon))	//if the value is really changing
			{
				final URI oldLabelIcon=labelIcon;	//get the old value
				labelIcon=newLabelIcon;	//actually change the value
				firePropertyChange(LABEL_ICON_PROPERTY, oldLabelIcon, newLabelIcon);	//indicate that the value changed
			}			
		}

	/**The label icon URI resource key, or <code>null</code> if there is no icon URI resource specified.*/
	private String labelIconResourceKey=null;

		/**@return The label icon URI resource key, or <code>null</code> if there is no icon URI resource specified.*/
		public String getLabelIconResourceKey() {return labelIconResourceKey;}

		/**Sets the key identifying the URI of the label icon in the resources.
		This is a bound property.
		@param newIconResourceKey The new label icon URI resource key.
		@see #LABEL_ICON_RESOURCE_KEY_PROPERTY
		*/
		public void setLabelIconResourceKey(final String newIconResourceKey)
		{
			if(!ObjectUtilities.equals(labelIconResourceKey, newIconResourceKey))	//if the value is really changing
			{
				final String oldIconResourceKey=labelIconResourceKey;	//get the old value
				labelIconResourceKey=newIconResourceKey;	//actually change the value
				firePropertyChange(LABEL_ICON_RESOURCE_KEY_PROPERTY, oldIconResourceKey, newIconResourceKey);	//indicate that the value changed
			}
		}

	/**The label text, or <code>null</code> if there is no label text.*/
	private String labelText=null;

		/**@return The label text, or <code>null</code> if there is no label text.*/
		public String getLabelText() {return labelText;}

		/**Sets the text of the label.
		This is a bound property.
		@param newLabelText The new text of the label.
		@see #LABEL_TEXT_PROPERTY
		*/
		public void setLabelText(final String newLabelText)
		{
			if(!ObjectUtilities.equals(labelText, newLabelText))	//if the value is really changing
			{
				final String oldLabel=labelText;	//get the old value
				labelText=newLabelText;	//actually change the value
				firePropertyChange(LABEL_TEXT_PROPERTY, oldLabel, newLabelText);	//indicate that the value changed
			}			
		}

	/**The content type of the label text.*/
	private ContentType labelTextContentType=Model.PLAIN_TEXT_CONTENT_TYPE;

		/**@return The content type of the label text.*/
		public ContentType getLabelTextContentType() {return labelTextContentType;}

		/**Sets the content type of the label text.
		This is a bound property.
		@param newLabelTextContentType The new label text content type.
		@exception NullPointerException if the given content type is <code>null</code>.
		@exception IllegalArgumentException if the given content type is not a text content type.
		@see #LABEL_TEXT_CONTENT_TYPE_PROPERTY
		*/
		public void setLabelTextContentType(final ContentType newLabelTextContentType)
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
				firePropertyChange(LABEL_TEXT_CONTENT_TYPE_PROPERTY, oldLabelTextContentType, newLabelTextContentType);	//indicate that the value changed
			}			
		}

	/**The label text resource key, or <code>null</code> if there is no label text resource specified.*/
	private String labelTextResourceKey=null;
	
		/**@return The label text resource key, or <code>null</code> if there is no label text resource specified.*/
		public String getLabelTextResourceKey() {return labelTextResourceKey;}
	
		/**Sets the key identifying the text of the label in the resources.
		This is a bound property.
		@param newLabelTextResourceKey The new label text resource key.
		@see #LABEL_TEXT_RESOURCE_KEY_PROPERTY
		*/
		public void setLabelTextResourceKey(final String newLabelTextResourceKey)
		{
			if(!ObjectUtilities.equals(labelTextResourceKey, newLabelTextResourceKey))	//if the value is really changing
			{
				final String oldLabelTextResourceKey=labelTextResourceKey;	//get the old value
				labelTextResourceKey=newLabelTextResourceKey;	//actually change the value
				firePropertyChange(LABEL_TEXT_RESOURCE_KEY_PROPERTY, oldLabelTextResourceKey, newLabelTextResourceKey);	//indicate that the value changed
			}
		}

	/**The map of cell representation strategies for columns.*/
	private final Map<TableColumnModel<?>, CellRepresentationStrategy<?>> columnCellRepresentationStrategyMap=new ConcurrentHashMap<TableColumnModel<?>, CellRepresentationStrategy<?>>();

	/**Installs the given cell representation strategy to produce representation components for the given column.
	@param <V> The type of value the column represents.
	@param column The column with which the strategy should be associated.
	@param cellRepresentationStrategy The strategy for generating components to represent values in the given column.
	@return The representation strategy previously associated with the given column.
	*/	
	@SuppressWarnings("unchecked")	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> CellRepresentationStrategy<? super V> setCellRepresentationStrategy(final TableColumnModel<V> column, CellRepresentationStrategy<V> cellRepresentationStrategy)
	{
		return (CellRepresentationStrategy<? super V>)columnCellRepresentationStrategyMap.put(column, cellRepresentationStrategy);	//associate the strategy with the column in the map
	}

	/**Returns the given cell representation strategy assigned to produce representation components for the given column.
	@param <V> The type of value the column represents.
	@param column The column with which the strategy should be associated.
	@return The strategy for generating components to represent values in the given column, or <code>null</code> if there is no associated representation strategy.
	*/	
	@SuppressWarnings("unchecked")	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> CellRepresentationStrategy<? super V> getCellRepresentationStrategy(final TableColumnModel<V> column)
	{
		return (CellRepresentationStrategy<? super V>)columnCellRepresentationStrategyMap.get(column);	//return the strategy linked to the column in the map
	}

	/**Ensures the component for a particular row and column exists.
	@param <T> The type of value contained in the cells of the column.
	@param rowIndex The zero-based cell row index.
	@param column The cell column.
	@exception IOException if there is an error updating the cell view.
	*/
	public <T> void verifyCellComponent(final int rowIndex, final TableColumnModel<T> column) throws IOException
	{
		final TableModel tableModel=getModel();	//get the table model
		final boolean editable=tableModel.isEditable() && column.isEditable();	//see if the cell is editable (a cell is only editable if both its table and column are editable)
		final TableModel.Cell<T> cell=new TableModel.Cell<T>(rowIndex, column);	//create a cell object representing this row and column
		CellComponentState cellComponentState=getComponentState(cell);	//get the component information for this cell
		if(cellComponentState==null || cellComponentState.isEditable()!=editable)	//if there is no component for this cell, or the component has a different editable status
		{
			final Component<?> valueComponent=getCellRepresentationStrategy(column).createComponent(this, tableModel, rowIndex, column, editable, false, false);	//create a new component for the cell
			valueComponent.setParent(this);	//tell this component that this table component is its parent
			cellComponentState=new CellComponentState(valueComponent, editable);	//create a new component state for the cell's component and metadata
			putComponentState(cell, cellComponentState);	//store the component state in the map for next time
		}
	}

	/**Session, value class, and column names constructor with a default ID and default data model.
	@param <C> The type of values in all the cells in the table.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of values held in the model.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public <C> Table(final GuiseSession session, final Class<C> valueClass, final String... columnNames)
	{
		this(session, null, valueClass, columnNames);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, value class, and columns constructor with a default ID and default data model.
	@param session The Guise session that owns this component.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public Table(final GuiseSession session, final TableColumnModel<?>... columns)
	{
		this(session, (String)null, columns);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, value class, table data, and column names constructor with a default ID and default data model.
	@param <C> The type of values in all the cells in the table.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of values held in the model.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	@exception ClassCastException if one of the values in a row is not compatible with the type of its column.
	*/
	public <C> Table(final GuiseSession session, final Class<C> valueClass, final C[][] rowValues, final String... columnNames)
	{
		this(session, null, valueClass, rowValues, columnNames);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, value class, table data, and columns constructor with a default ID and default data model.
	@param session The Guise session that owns this component.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	@exception ClassCastException if one of the values in a row is not compatible with the type of its column.
	*/
	public Table(final GuiseSession session, final Object[][] rowValues, final TableColumnModel<?>... columns)
	{
		this(session, null, rowValues, columns);	//construct the class, indicating that a default ID should be generated
	}

	/**Session, value class, and model constructor with a default ID.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public Table(final GuiseSession session, final TableModel model)
	{
		this(session, null, model);	//construct the class, indicating that a default ID should be generated
	}
	
	/**Session, ID, value class, and column names constructor with a default data model.
	@param <C> The type of values in all the cells in the table.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of values held in the model.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public <C> Table(final GuiseSession session, final String id, final Class<C> valueClass, final String... columnNames)
	{
		this(session, id, new DefaultTableModel(session, valueClass, null, columnNames));	//construct the class with no default data
	}

	/**Session, ID, value class, and columns constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	*/
	public Table(final GuiseSession session, final String id, final TableColumnModel<?>... columns)
	{
		this(session, id, new DefaultTableModel(session, null, columns));	//construct the class with no default data
	}

	/**Session, ID, value class, table data, and column names constructor with a default data model.
	@param <C> The type of values in all the cells in the table.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of values held in the model.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columnNames The names to serve as label headers for the columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	@exception ClassCastException if one of the values in a row is not compatible with the type of its column.
	*/
	public <C> Table(final GuiseSession session, final String id, final Class<C> valueClass, final C[][] rowValues, final String... columnNames)
	{
		this(session, id, new DefaultTableModel(session, valueClass, rowValues, columnNames));	//construct the class with a default model
	}

	/**Session, ID, value class, table data, and columns constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param rowValues The two-dimensional list of values, where the first index represents the row and the second represents the column, or <code>null</code> if no default values should be given.
	@param columns The models representing the table columns.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if the given number of columns does not equal the number of columns in any given data row.
	@exception ClassCastException if one of the values in a row is not compatible with the type of its column.
	*/
	public Table(final GuiseSession session, final String id, final Object[][] rowValues, final TableColumnModel<?>... columns)
	{
		this(session, id, new DefaultTableModel(session, rowValues, columns));	//construct the class with a default model
	}

	/**Session, ID, value class, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Table(final GuiseSession session, final String id, final TableModel model)
	{
		super(session, id, model);	//construct the parent class
		for(final TableColumnModel<?> column:model.getColumns())	//install a default cell representation strategy for each column
		{
			installDefaultCellRepresentationStrategy(column);	//create and install a default representation strategy for this column
		}
		session.addPropertyChangeListener(GuiseSession.LOCALE_PROPERTY, new AbstractGuisePropertyChangeListener<Locale>()	//listen for the session locale changing
				{
					public void propertyChange(GuisePropertyChangeEvent<Locale> propertyChangeEvent)	//if the locale changes
					{
						clearComponentStates();	//clear all the components and component states in case they are locale-related TODO probably transfer this up to the abstract composite state class
					}			
				});
	}

	/**An encapsulation of a component for a cell along with other metadata, such as whether the component was editable when created.
	@author Garret Wilson
	*/ 
	protected static class CellComponentState extends AbstractCompositeStateComponent.ComponentState
	{
		/**Whether the component is for a cell that was editable when the component was created.*/
		private final boolean editable;

			/**@return Whether the component is for a cell that was editable when the component was created.*/
			public boolean isEditable() {return editable;}

		/**Constructor
		@param component The component for a cell.
		@param editable Whether the component is for a cell that was editable when the component was created.
		@exception NullPointerException if the given component is <code>null</code>.
		*/
		public CellComponentState(final Component<?> component, final boolean editable)
		{
			super(component);	//construct the parent class
			this.editable=editable;
		}
	}

	/**Installs a default cell representation strategy for the given column.
	@param <T> The type of value contained in the column.
	@param column The table column for which a default cell representation strategy should be installed.
	*/
	private <T> void installDefaultCellRepresentationStrategy(final TableColumnModel<T> column)
	{
		setCellRepresentationStrategy(column, new DefaultCellRepresentationStrategy<T>(column.getSession(), AbstractStringLiteralConverter.getInstance(column.getSession(), column.getValueClass())));	//create a default cell representation strategy
	}

	/**A strategy for generating components to represent table cell model values.
	The component ID should reflect a unique identifier for the cell.
	@param <V> The type of value the strategy is to represent.
	@author Garret Wilson
	*/
	public interface CellRepresentationStrategy<V>
	{
		/**Creates a component to represent the given cell.
		@param <C> The type of value contained in the column.
		@param table The component containing the model.
		@param model The model containing the value.
		@param rowIndex The zero-based row index of the value.
		@param column The column of the value.
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value.
		*/
		public <C extends V> Component<?> createComponent(final Table table, final TableModel model, final int rowIndex, final TableColumnModel<C> column, final boolean editable, final boolean selected, final boolean focused);
	}

	/**A default table cell representation strategy.
	Component values will be represented as themselves.
	For non-editable cells, a message component will be generated using the cell's value as its message.
	Editable cells will be represented using a checkbox for boolean values and a text control for all other values.
	The message's ID will be in the form "<var>tableID</var>.cell-<var>rowIndex</var>-<var>columnIndex</var>".
	@param <V> The type of value the strategy is to represent.
	@see Message
	@see Converter
	@author Garret Wilson
	*/
	public static class DefaultCellRepresentationStrategy<V> implements CellRepresentationStrategy<V>
	{

		/**The Guise session that owns this representation strategy.*/
		private final GuiseSession session;

			/**@return The Guise session that owns this representation strategy.*/
			public GuiseSession getSession() {return session;}

		/**The converter to use for displaying the value as a string.*/
		private final Converter<V, String> converter;
			
			/**@return The converter to use for displaying the value as a string.*/
			public Converter<V, String> getConverter() {return converter;}

		/**Session constructor.
		@param session The Guise session that owns this representation strategy.
		@param converter The converter to use for displaying the value as a string.
		@exception NullPointerException if the given session and/or converter is <code>null</code>.
		*/
		public DefaultCellRepresentationStrategy(final GuiseSession session, final Converter<V, String> converter)
		{
			this.session=checkNull(session, "Session cannot be null.");	//save the session
			this.converter=checkNull(converter, "Converter cannot be null.");	//save the converter
		}

		/**Creates a component for the given cell.
		This implementation returns a message with string value of the given value using the object's <code>toString()</code> method.
		The label's ID is set to the hexadecimal representation of the object's hash code appended to the word "hash".
		@param <C> The type of value contained in the column.
		@param table The component containing the model.
		@param model The model containing the value.
		@param rowIndex The zero-based row index of the value.
		@param column The column of the value.
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value.
		*/
		@SuppressWarnings("unchecked")	//we check the type of the column value class, so the casts are safe
		public <C extends V> Component<?> createComponent(final Table table, final TableModel model, final int rowIndex, final TableColumnModel<C> column, final boolean editable, final boolean selected, final boolean focused)
		{
			final GuiseSession session=getSession();	//get the session
			final TableModel.Cell<C> cell=new TableModel.Cell<C>(rowIndex, column);	//create a cell to represent the row and column
			final Class<C> valueClass=column.getValueClass();	//get the value class of the column
			if(Component.class.isAssignableFrom(valueClass))	//if a component is being represented
			{
				return (Component<?>)model.getCellValue(cell);	//return the value as a component TODO find a way to update the cached component if it changes
			}
			final int columnIndex=model.getColumnIndex(column);	//get the logical index of the given column
			final String idSegment=new StringBuilder("cell-").append(rowIndex).append('-').append(columnIndex).toString();	//generate an ID for this row and column
			final String id=table.createID(idSegment);	//create an ID for the new component
			if(editable)	//if the component should be editable
			{
				final ValueModel<C> valueModel=new DefaultCellValueModel<C>(session, model, cell);	//create a new value model for the cell
				if(Boolean.class.isAssignableFrom(valueClass))	//if the value class is subclass of Boolean
				{
					return new CheckControl(session, id, (ValueModel<Boolean>)(Object)valueModel);	//create a new check control for the Boolean value model TODO find out why JDK 1.5.0_03 requires the intermediate Object cast
				}
				else	//for all other values
				{
					return new TextControl<C>(session, id, valueModel);	//generate a text input control for the value model
				}
			}
			else	//if the component should not be editable, return a message component
			{
				return new Message(session, id, new DefaultCellMessageModel<C>(session, model, cell, getConverter()));	//create a message component containing a message model representing the value's string value				
			}
		}
	}

	/**A message model that returns a default representation of the cell in a message.
	@param <C> The type of value in the cell.
	@author Garret Wilson
	*/
	public static class DefaultCellMessageModel<C> extends DefaultMessageModel
	{
		/**The table model of the cell.*/
		private final TableModel model;

			/**@return The table model of the cell.*/
			protected TableModel getModel() {return model;}

		/**The cell being represented*/
		private TableModel.Cell<C> cell;

			/**@return The cell being represented*/
			protected TableModel.Cell<C> getCell() {return cell;}

		/**The converter to use for displaying the value as a string.*/
		private final Converter<? super C, String> converter;
			
			/**@return The converter to use for displaying the value as a string.*/
			public Converter<? super C, String> getConverter() {return converter;}

		/**Constructs a default message model for a cell.
		@param session The Guise session that owns this model.
		@param model The table model of the cell.
		@param cell The cell being represented.
		@param converter The converter to use for displaying the value as a string.
		@exception NullPointerException if the given session, table model and/or cell is <code>null</code>.
		*/
		public DefaultCellMessageModel(final GuiseSession session, final TableModel model, final TableModel.Cell<C> cell, final Converter<? super C, String> converter)
		{
			super(session);	//construct the parent class
			this.model=checkNull(model, "Table model cannot be null.");
			this.cell=checkNull(cell, "Cell cannot be null.");
			this.converter=checkNull(converter, "Converter cannot be null.");
		}

		/**Determines the message text of the cell.
		This implementation returns a message with a string value of the given value using the installed converter, if no message has been explicitly set.
		@return The message text of the cell.
		@see #getConverter()
		*/
		public String getMessage()
		{
			String message=super.getMessage();	//get the message explicitly set
			if(message==null)	//if no message has been explicitly set
			{
				final TableModel.Cell<C> cell=getCell();	//get our current cell
				final C value=getModel().getCellValue(cell.getRowIndex(), cell.getColumn());	//get the value from the table model
				try
				{
					message=getConverter().convertValue(value);	//return the literal value of the value
				}
				catch(final ConversionException conversionException)	//we don't expect a value-to-string conversion to result in an error
				{
					throw new AssertionError(conversionException);
				}
			}
			return message;	//return the message
		}

	}

	/**A value model that returns and updates a the value of the cell.
	@param <C> The type of value in the cell.
	@author Garret Wilson
	*/
	public static class DefaultCellValueModel<C> extends DefaultValueModel<C>
	{
		/**The table model of the cell.*/
		private final TableModel model;

			/**@return The table model of the cell.*/
			protected TableModel getModel() {return model;}

		/**The cell being represented*/
		private TableModel.Cell<C> cell;

			/**@return The cell being represented*/
			protected TableModel.Cell<C> getCell() {return cell;}

		/**Constructs a default value model for a cell.
		@param session The Guise session that owns this model.
		@param model The table model of the cell.
		@param cell The cell being represented.
		@exception NullPointerException if the given session, table model and/or cell is <code>null</code>.
		*/
		public DefaultCellValueModel(final GuiseSession session, final TableModel model, final TableModel.Cell<C> cell)
		{
			super(session, checkNull(cell, "Cell cannot be null.").getColumn().getValueClass());	//construct the parent class
			this.model=checkNull(model, "Table model cannot be null.");
			this.cell=cell;
		}

		/**@return Whether the model's value is editable and the corresponding control will allow the the user to change the value.
		This version returns <code>true</code> if the model and column are both editable.*/
		public boolean isEditable() {return getModel().isEditable() && getCell().getColumn().isEditable();}

		/**Sets whether the model's value is editable and the corresponding control will allow the the user to change the value. This version throws an exception, as the editable status is read-only.
		@param newEditable <code>true</code> if the corresponding control should allow the user to change the value.
		*/
		public void setEditable(final boolean newEditable) {throw new UnsupportedOperationException("Editable is read-only.");}

		/**@return Whether the model is enabled and and the corresponding control can receive user input.
		This version returns <code>true</code> if the model and column are both enabled.*/
		public boolean isEnabled() {return getModel().isEnabled() && getCell().getColumn().isEnabled();}

		/**Sets whether the model is enabled and and the corresponding control can receive user input. This version throws an exception, as the enabled status is read-only.
		@param newEnabled <code>true</code> if the corresponding control should indicate and accept user input.
		*/
		public void setEnabled(final boolean newEnabled) {throw new UnsupportedOperationException("Enabled is read-only.");}

		/**@return The validator for this model, or <code>null</code> if no validator is installed.*/
		public Validator<C> getValidator() {return getCell().getColumn().getValidator();}	//return the validator from the column

		/**Sets the validator. This version throws an exception, as the validator is read-only.
		@param newValidator The validator for this model, or <code>null</code> if no validator should be used.
		*/
		public void setValidator(final Validator<C> newValidator) {throw new UnsupportedOperationException("Validator is read-only.");}

		/**@return The value from the table model cell, or <code>null</code> if there is no value in the cell.*/
		public C getValue() {return getModel().getCellValue(getCell());}	//return the value from the table model

		/**Sets the value in the cell.
		@param newValue The value of the cell.
		@exception ValidationException if the provided value is not valid.
		@see #getValidator()
		@see ValueModel#VALUE_PROPERTY
		*/
		public void setValue(final C newValue) throws ValidationException
		{
			final Validator<C> validator=getValidator();	//get the currently installed validator, if there is one
			if(validator!=null)	//if a validator is installed, always validate the value, even if it isn't changing, so that an initial value that may not be valid will throw an error when it's tried to be set to the same, but invalid, value
			{
				validator.validate(newValue);	//validate the new value, throwing an exception if anything is wrong
			}
			getModel().setCellValue(getCell(), newValue);	//set the value in the table model
		}

		/**Resets the value to a default value, which may be invalid according to any installed validators.
		No validation occurs.
		@see ValueModel#VALUE_PROPERTY
		*/
		public void resetValue()
		{
			getModel().setCellValue(getCell(), null);	//set a null value in the table model
		}

	}

}
