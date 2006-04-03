package com.guiseframework.model;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.TextUtilities.*;

import java.net.URI;

import javax.mail.internet.ContentType;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;

/**A default implementation of a model for an identifier such as text and/or an icon.
@author Garret Wilson
*/
public class DefaultLabelModel extends AbstractModel implements LabelModel
{

	/**The icon URI, or <code>null</code> if there is no icon URI.*/
	private URI icon=null;

		/**@return The icon URI, or <code>null</code> if there is no icon URI.*/
		public URI getIcon() {return icon;}

		/**Sets the URI of the icon.
		This is a bound property of type <code>URI</code>.
		@param newIcon The new URI of the icon.
		@see #ICON_PROPERTY
		*/
		public void setIcon(final URI newIcon)
		{
			if(!ObjectUtilities.equals(icon, newIcon))	//if the value is really changing
			{
				final URI oldIcon=icon;	//get the old value
				icon=newIcon;	//actually change the value
				firePropertyChange(ICON_PROPERTY, oldIcon, newIcon);	//indicate that the value changed
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
	private ContentType labelContentType=PLAIN_TEXT_CONTENT_TYPE;

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
			checkInstance(newLabelTextContentType, "Content type cannot be null.");
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
}
