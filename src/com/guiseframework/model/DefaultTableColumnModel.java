package com.guiseframework.model;

import static com.garretwilson.lang.ObjectUtilities.checkNull;
import static com.garretwilson.text.TextUtilities.isText;

import java.net.URI;

import javax.mail.internet.ContentType;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.Component;
import com.guiseframework.validator.Validator;

/**The default implementation of a column in a table.
The table column model by default is not editable.
@param <V> The type of values contained in the table column.
@author Garret Wilson
*/
public class DefaultTableColumnModel<V> extends AbstractModel implements TableColumnModel<V>
{

	/**The class representing the type of values this model can hold.*/
	private final Class<V> valueClass;

		/**@return The class representing the type of values this model can hold.*/
		public Class<V> getValueClass() {return valueClass;}

	/**The icon URI, or <code>null</code> if there is no icon URI.*/
	private URI icon=null;

		/**@return The icon URI, or <code>null</code> if there is no icon URI.*/
		public URI getIcon() {return icon;}

		/**Sets the URI of the icon.
		This is a bound property of type <code>URI</code>.
		@param newLabelIcon The new URI of the icon.
		@see #ICON_PROPERTY
		*/
		public void setIcon(final URI newLabelIcon)
		{
			if(!ObjectUtilities.equals(icon, newLabelIcon))	//if the value is really changing
			{
				final URI oldLabelIcon=icon;	//get the old value
				icon=newLabelIcon;	//actually change the value
				firePropertyChange(ICON_PROPERTY, oldLabelIcon, newLabelIcon);	//indicate that the value changed
			}			
		}

	/**The icon URI resource key, or <code>null</code> if there is no icon URI resource specified.*/
	private String iconResourceKey=null;

		/**@return The icon URI resource key, or <code>null</code> if there is no icon URI resource specified.*/
		public String getIconResourceKey() {return iconResourceKey;}

		/**Sets the key identifying the URI of the icon in the resources.
		This is a bound property.
		@param newIconResourceKey The new icon URI resource key.
		@see #ICON_RESOURCE_KEY_PROPERTY
		*/
		public void setIconResourceKey(final String newIconResourceKey)
		{
			if(!ObjectUtilities.equals(iconResourceKey, newIconResourceKey))	//if the value is really changing
			{
				final String oldIconResourceKey=iconResourceKey;	//get the old value
				iconResourceKey=newIconResourceKey;	//actually change the value
				firePropertyChange(ICON_RESOURCE_KEY_PROPERTY, oldIconResourceKey, newIconResourceKey);	//indicate that the value changed
			}
		}

	/**The label text, or <code>null</code> if there is no label text.*/
	private String label=null;

		/**@return The label text, or <code>null</code> if there is no label text.*/
		public String getLabel() {return label;}

		/**Sets the text of the label.
		This is a bound property.
		@param newLabelText The new text of the label.
		@see #LABEL_PROPERTY
		*/
		public void setLabel(final String newLabelText)
		{
			if(!ObjectUtilities.equals(label, newLabelText))	//if the value is really changing
			{
				final String oldLabel=label;	//get the old value
				label=newLabelText;	//actually change the value
				firePropertyChange(LABEL_PROPERTY, oldLabel, newLabelText);	//indicate that the value changed
			}			
		}

	/**The content type of the label text.*/
	private ContentType labelContentType=Component.PLAIN_TEXT_CONTENT_TYPE;

		/**@return The content type of the label text.*/
		public ContentType getLabelContentType() {return labelContentType;}

		/**Sets the content type of the label text.
		This is a bound property.
		@param newLabelTextContentType The new label text content type.
		@exception NullPointerException if the given content type is <code>null</code>.
		@exception IllegalArgumentException if the given content type is not a text content type.
		@see #LABEL_CONTENT_TYPE_PROPERTY
		*/
		public void setLabelContentType(final ContentType newLabelTextContentType)
		{
			checkNull(newLabelTextContentType, "Content type cannot be null.");
			if(labelContentType!=newLabelTextContentType)	//if the value is really changing
			{
				final ContentType oldLabelTextContentType=labelContentType;	//get the old value
				if(!isText(newLabelTextContentType))	//if the new content type is not a text content type
				{
					throw new IllegalArgumentException("Content type "+newLabelTextContentType+" is not a text content type.");
				}
				labelContentType=newLabelTextContentType;	//actually change the value
				firePropertyChange(LABEL_CONTENT_TYPE_PROPERTY, oldLabelTextContentType, newLabelTextContentType);	//indicate that the value changed
			}			
		}

	/**The label text resource key, or <code>null</code> if there is no label text resource specified.*/
	private String labelResourceKey=null;
	
		/**@return The label text resource key, or <code>null</code> if there is no label text resource specified.*/
		public String getLabelResourceKey() {return labelResourceKey;}
	
		/**Sets the key identifying the text of the label in the resources.
		This is a bound property.
		@param newLabelTextResourceKey The new label text resource key.
		@see #LABEL_RESOURCE_KEY_PROPERTY
		*/
		public void setLabelResourceKey(final String newLabelTextResourceKey)
		{
			if(!ObjectUtilities.equals(labelResourceKey, newLabelTextResourceKey))	//if the value is really changing
			{
				final String oldLabelTextResourceKey=labelResourceKey;	//get the old value
				labelResourceKey=newLabelTextResourceKey;	//actually change the value
				firePropertyChange(LABEL_RESOURCE_KEY_PROPERTY, oldLabelTextResourceKey, newLabelTextResourceKey);	//indicate that the value changed
			}
		}

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
//TODO important: fix				firePropertyChange(EDITABLE_PROPERTY, Boolean.valueOf(oldEditable), Boolean.valueOf(newEditable));	//indicate that the value changed
			}			
		}

		/**The style identifier, or <code>null</code> if there is no style ID.*/
		private String styleID=null;

			/**@return The style identifier, or <code>null</code> if there is no style ID.*/
			public String getStyleID() {return styleID;}

			/**Identifies the style for the column.
			This is a bound property.
			@param newStyleID The style identifier, or <code>null</code> if there is no style ID.
			@see TableColumnModel#STYLE_ID_PROPERTY
			*/
			public void setStyleID(final String newStyleID)
			{
				if(styleID!=newStyleID)	//if the value is really changing
				{
					final String oldStyleID=styleID;	//get the current value
					styleID=newStyleID;	//update the value
					firePropertyChange(STYLE_ID_PROPERTY, oldStyleID, newStyleID);
				}
			}

	/**The validator for cells in this column, or <code>null</code> if no validator is installed.*/
	private Validator<V> validator;

		/**@return The validator for cells in this column, or <code>null</code> if no validator is installed.*/
		public Validator<V> getValidator() {return validator;}

		/**Sets the validator.
		This is a bound property
		@param newValidator The validator for cells in this column, or <code>null</code> if no validator should be used.
		@see TableColumnModel#VALIDATOR_PROPERTY
		*/
		public void setValidator(final Validator<V> newValidator)
		{
			if(validator!=newValidator)	//if the value is really changing
			{
				final Validator<V> oldValidator=validator;	//get the old value
				validator=newValidator;	//actually change the value
				firePropertyChange(ValueModel.VALIDATOR_PROPERTY, oldValidator, newValidator);	//indicate that the value changed
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
	@param session The Guise session that owns this column.
	@param valueClass The class indicating the type of values held in the model.
	@exception NullPointerException if the given session, and/or value class is <code>null</code>.
	*/
/*TODO del; the ID apparently is not used
	public DefaultTableColumnModel(final GuiseSession session, final Class<V> valueClass)
	{
		this(session, null, valueClass);	//construct the class indicating that a default ID should be generated
	}
*/
	
	/**Session, value class, and label constructor.
	@param session The Guise session that owns this column.
	@param valueClass The class indicating the type of values held in the model.
	@param labelText The text of the label.
	@exception NullPointerException if the given session, and/or value class is <code>null</code>.
	*/
	public DefaultTableColumnModel(final GuiseSession session, final Class<V> valueClass, final String label)
	{
		this(session, null, valueClass, label);	//construct the class indicating that a default ID should be generated
	}

	/**Session, ID, and value class constructor.
	@param session The Guise session that owns this column.
	@param valueClass The class indicating the type of values held in the model.
	@exception NullPointerException if the given session, and/or value class is <code>null</code>.
	*/
	public DefaultTableColumnModel(final GuiseSession session, final Class<V> valueClass)
	{
		this(session, valueClass, null);	//construct the class with no label
	}
	
	/**Session, ID, value class, and label constructor.
	@param session The Guise session that owns this column.
	@param id The column identifier, or <code>null</code> if a default column identifier should be generated.
	@param valueClass The class indicating the type of values held in the model.
	@param labelText The text of the label.
	@exception NullPointerException if the given session, and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultTableColumnModel(final GuiseSession session, final String id, final Class<V> valueClass, final String labelText)
	{
		super(session);	//construct the parent class
		this.valueClass=valueClass;	//save the value class
		this.label=labelText;	//save the label text
	}
	
}
