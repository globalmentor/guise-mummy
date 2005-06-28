package com.garretwilson.guise.model;

import com.garretwilson.guise.session.GuiseSession;

/**A model for an identifier such as text and/or an icon.
@author Garret Wilson
*/
public class DefaultLabelModel extends AbstractModel implements LabelModel
{

	/**The label text, or <code>null</code> if there is no label text.*/
	private String label=null;

		/**@return The label text, or <code>null</code> if there is no label text.*/
		public String getLabel() {return label;}

		/**Sets the text of the label.
		This is a bound property.
		@param newLabel The new text of the label.
		@see LabelModel#LABEL_PROPERTY
		*/
		public void setLabel(final String newLabel)
		{
			if(label!=newLabel)	//if the value is really changing
			{
				final String oldText=label;	//get the old value
				label=newLabel;	//actually change the value
				firePropertyChange(LABEL_PROPERTY, oldText, newLabel);	//indicate that the value changed
			}			
		}

	/**Session constructor.
	@param session The Guise session that owns this model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultLabelModel(final GuiseSession<?> session)
	{
		super(session);	//construct the parent class
	}
}
