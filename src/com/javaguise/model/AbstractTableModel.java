package com.javaguise.model;

import com.javaguise.session.GuiseSession;

/**An abstract implementation of a table model.
The model is thread-safe, synchronized on itself. Any iteration over values should include synchronization on the instance of this class. 
The table model is editable by default.
@author Garret Wilson
*/
public abstract class AbstractTableModel extends AbstractControlModel implements TableModel
{

	/**Whether the table is editable and the cells will allow the the user to change their values, if their respective columns are designated as editable as well.*/
	private boolean editable=true;

		/**@return Whether the table is editable and the cells will allow the the user to change their values, if their respective columns are designated as editable as well.*/
		public boolean isEditable() {return editable;}

		/**Sets whether the table is editable and the cells will allow the the user to change their values, if their respective columns are designated as editable as well.
		This is a bound property of type <code>Boolean</code>.
		@param newEditable <code>true</code> if the cells should allow the user to change their values if their respective columns are also designated as editable.
		@see TableModel#EDITABLE_PROPERTY
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

	/**Session constructor.
	@param session The Guise session that owns this model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractTableModel(final GuiseSession<?> session)
	{
		this(session, null);	//construct the class with no label
	}

	/**Session and label constructor.
	@param session The Guise session that owns this model.
	@param label The text of the label.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractTableModel(final GuiseSession<?> session, final String label)
	{
		super(session, label);	//construct the parent class
	}
}
