package com.javaguise.model;

import com.javaguise.session.GuiseSession;

/**The default implementation of a column in a table.
The table column model by default is not editable.
@param <V> The type of values contained in the table column.
@author Garret Wilson
*/
public class DefaultTableColumnModel<V> extends AbstractControlModel implements TableColumnModel<V>
{

	/**The class representing the type of values this model can hold.*/
	private final Class<V> valueClass;

		/**@return The class representing the type of values this model can hold.*/
		public Class<V> getValueClass() {return valueClass;}

	/**Whether the cells in this table column model are editable and will allow the the user to change their values.*/
	private boolean editable=false;

		/**@return Whether the cells in this table column model are editable and will allow the the user to change their values.*/
		public boolean isEditable() {return editable;}

		/**Sets whether the cells in this table column model are editable and will allow the the user to change their values.
		This is a bound property of type <code>Boolean</code>.
		@param newEditable <code>true</code> if the table column cells should allow the user to change their values.
		@see TableColumnModel#EDITABLE_PROPERTY
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

	/**Whether the column is visible.*/
	private boolean visible=true;

		/**@return Whether the column is visible.*/
		public boolean isVisible() {return visible;}

		/**Sets whether the column is visible.
		This is a bound property of type <code>Boolean</code>.
		@param newVisible <code>true</code> if the column should be visible, else <code>false</code>.
		@see TableColumnModel#VISIBLE_PROPERTY
		*/
		public void setVisible(final boolean newVisible)
		{
			if(visible!=newVisible)	//if the value is really changing
			{
				final boolean oldVisible=visible;	//get the current value
				visible=newVisible;	//update the value
				firePropertyChange(VISIBLE_PROPERTY, Boolean.valueOf(oldVisible), Boolean.valueOf(newVisible));
			}
		}

	/**Session and value class constructor.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of values held in the model.
	@exception NullPointerException if the given session, and/or value class is <code>null</code>.
	*/
	public DefaultTableColumnModel(final GuiseSession<?> session, final Class<V> valueClass)
	{
		this(session, null, valueClass);	//construct the class indicating that a default ID should be generated
	}
	
	/**Session, value class, and label constructor.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of values held in the model.
	@param label The text of the label.
	@exception NullPointerException if the given session, and/or value class is <code>null</code>.
	*/
	public DefaultTableColumnModel(final GuiseSession<?> session, final Class<V> valueClass, final String label)
	{
		this(session, null, valueClass, label);	//construct the class indicating that a default ID should be generated
	}

	/**Session, ID, and value class constructor.
	@param session The Guise session that owns this component.
	@param id The column identifier, or <code>null</code> if a default column identifier should be generated.
	@param valueClass The class indicating the type of values held in the model.
	@exception NullPointerException if the given session, and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultTableColumnModel(final GuiseSession<?> session, final String id, final Class<V> valueClass)
	{
		this(session, id, valueClass, null);	//construct the class with no label
	}
	
	/**Session, ID, value class, and label constructor.
	@param session The Guise session that owns this component.
	@param id The column identifier, or <code>null</code> if a default column identifier should be generated.
	@param valueClass The class indicating the type of values held in the model.
	@param label The text of the label.
	@exception NullPointerException if the given session, and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultTableColumnModel(final GuiseSession<?> session, final String id, final Class<V> valueClass, final String label)
	{
		super(session, label);	//construct the parent class
		this.valueClass=valueClass;	//save the value class
	}
	
}
