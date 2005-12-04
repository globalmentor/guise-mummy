package com.javaguise.model;

import java.util.MissingResourceException;

import com.javaguise.GuiseSession;
import com.garretwilson.lang.ObjectUtilities;

/**A default implementation of a model for an identifier such as text and/or an icon.
@author Garret Wilson
*/
public class DefaultLabelModel extends AbstractLabelModel
{

	/**The label text, or <code>null</code> if there is no label text.*/
	private String label=null;

		/**Determines the text of the label.
		If a label is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
		@return The label text, or <code>null</code> if there is no label text.
		@exception MissingResourceException if there was an error loading the value from the resources.
		@see #getLabelResourceKey()
		*/
		public String getLabel() throws MissingResourceException
		{
			return getString(label, getLabelResourceKey());	//get the value or the resource, if available
		}

		/**Sets the text of the label.
		This is a bound property.
		@param newLabel The new text of the label.
		@see LabelModel#LABEL_PROPERTY
		*/
		public void setLabel(final String newLabel)
		{
			if(!ObjectUtilities.equals(label, newLabel))	//if the value is really changing
			{
				final String oldLabel=label;	//get the old value
				label=newLabel;	//actually change the value
				firePropertyChange(LABEL_PROPERTY, oldLabel, newLabel);	//indicate that the value changed
			}			
		}

	/**Session constructor.
	@param session The Guise session that owns this model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultLabelModel(final GuiseSession session)
	{
		this(session, null);	//construct the class with no label
	}

	/**Session and label constructor.
	@param session The Guise session that owns this model.
	@param label The text of the label.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultLabelModel(final GuiseSession session, final String label)
	{
		super(session);	//construct the parent class
		this.label=label;	//save the label
	}

	/**@return <code>true</code> if this model has label information, such as an icon or a label string.*/
	public boolean hasLabel()
	{
		return label!=null || getLabelResourceKey()!=null || getIcon()!=null;	//see if there is label text, a label resource key, or an icon
	}

}
