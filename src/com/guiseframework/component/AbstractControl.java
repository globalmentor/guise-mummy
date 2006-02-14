package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.TextUtilities.*;

import java.net.URI;

import javax.mail.internet.ContentType;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.model.ControlModel;
import com.guiseframework.model.Model;

/**An abstract implementation of a model component that allows user interaction to modify the model.
@author Garret Wilson
*/
public abstract class AbstractControl<C extends Control<C>> extends AbstractComponent<C> implements Control<C>
{

	/**Whether the state of the control represents valid user input.*/
//TODO fix	private boolean valid=true;

		/**@return Whether the state of the control represents valid user input.*/
//TODO fix		public boolean isValid() {return valid;}

		/**Sets whether the state of the control represents valid user input
		This is a bound property of type <code>Boolean</code>.
		@param newValid <code>true</code> if user input should be considered valid
		@see Control#VALID_PROPERTY
		*/
/*TODO fix
		public void setValid(final boolean newValid)
		{
			if(valid!=newValid)	//if the value is really changing
			{
				final boolean oldValid=valid;	//get the current value
				valid=newValid;	//update the value
				firePropertyChange(VALID_PROPERTY, Boolean.valueOf(oldValid), Boolean.valueOf(newValid));
			}
		}
*/

	/**@return The data model used by this component.*/
	@SuppressWarnings("unchecked")
	public ControlModel getModel() {return (ControlModel)super.getModel();}

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
	
	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractControl(final GuiseSession session, final String id, final Model model)
	{
		super(session, id, model);	//construct the parent class
	}

}
