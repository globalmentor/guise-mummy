package com.guiseframework.component;

import java.net.URI;
import javax.mail.internet.ContentType;

import static com.garretwilson.io.ContentTypeConstants.*;
import static com.garretwilson.io.ContentTypeUtilities.*;
import static com.garretwilson.lang.ClassUtilities.getPropertyName;
import static com.garretwilson.lang.ObjectUtilities.checkNull;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.text.TextUtilities.isText;
import static com.garretwilson.util.ArrayUtilities.*;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.transfer.*;
import com.guiseframework.model.*;

/**An image component.
This component installs a default export strategy supporting export of the following content types:
<ul>
	<li><code>text/uri-list</code></li>
	<li>The label content type.</li>
</ul>
@author Garret Wilson
*/
public class Image extends AbstractComponent<Image>
{

	/**@return The data model used by this component.*/
	public ImageModel getModel() {return (ImageModel)super.getModel();}

	/**The default export strategy for this component type.*/
	protected final static ExportStrategy<Image> DEFAULT_EXPORT_STRATEGY=new ExportStrategy<Image>()
			{
				/**Exports data from the given component.
				@param component The component from which data will be transferred.
				@return The object to be transferred, or <code>null</code> if no data can be transferred.
				*/
				public Transferable<Image> exportTransfer(final Image component)
				{
					return new DefaultTransferable(component);	//return a default transferable for this component
				}
			};

	/**The bound property of whether the component has image dragging enabled.*/
//TODO del if not needed	public final static String IMAGE_DRAG_ENABLED_PROPERTY=getPropertyName(Image.class, "imageDragEnabled");

	/**The message bound property.*/
	public final static String MESSAGE_PROPERTY=getPropertyName(Image.class, "message");
	/**The message content type bound property.*/
	public final static String MESSAGE_CONTENT_TYPE_PROPERTY=getPropertyName(Image.class, "messageContentType");
	/**The message resource key bound property.*/
	public final static String MESSAGE_RESOURCE_KEY_PROPERTY=getPropertyName(Image.class, "messageResourceKey");

	/**The message text, or <code>null</code> if there is no message text.*/
	private String message=null;

		/**@return The message text, or <code>null</code> if there is no message text.*/
		public String getMessage() {return message;}

		/**Sets the text of the message.
		This is a bound property.
		@param newMessage The new text of the message.
		@see #MESSAGE_PROPERTY
		*/
		public void setMessage(final String newMessage)
		{
			if(!ObjectUtilities.equals(message, newMessage))	//if the value is really changing
			{
				final String oldMessage=message;	//get the old value
				message=newMessage;	//actually change the value
				firePropertyChange(MESSAGE_PROPERTY, oldMessage, newMessage);	//indicate that the value changed
			}			
		}

	/**The content type of the message text.*/
	private ContentType messageContentType=Component.PLAIN_TEXT_CONTENT_TYPE;

		/**@return The content type of the message text.*/
		public ContentType getMessageContentType() {return messageContentType;}

		/**Sets the content type of the message text.
		This is a bound property.
		@param newMessageContentType The new message text content type.
		@exception NullPointerException if the given content type is <code>null</code>.
		@exception IllegalArgumentException if the given content type is not a text content type.
		@see #MESSAGE_CONTENT_TYPE_PROPERTY
		*/
		public void setMessageContentType(final ContentType newMessageContentType)
		{
			checkNull(newMessageContentType, "Content type cannot be null.");
			if(messageContentType!=newMessageContentType)	//if the value is really changing
			{
				final ContentType oldMessageContentType=messageContentType;	//get the old value
				if(!isText(newMessageContentType))	//if the new content type is not a text content type
				{
					throw new IllegalArgumentException("Content type "+newMessageContentType+" is not a text content type.");
				}
				messageContentType=newMessageContentType;	//actually change the value
				firePropertyChange(MESSAGE_CONTENT_TYPE_PROPERTY, oldMessageContentType, newMessageContentType);	//indicate that the value changed
			}			
		}

	/**The message text resource key, or <code>null</code> if there is no message text resource specified.*/
	private String messageResourceKey=null;

		/**@return The message text resource key, or <code>null</code> if there is no message text resource specified.*/
		public String getMessageResourceKey() {return messageResourceKey;}

		/**Sets the key identifying the text of the message in the resources.
		This is a bound property.
		@param newMessageResourceKey The new message text resource key.
		@see #MESSAGE_RESOURCE_KEY_PROPERTY
		*/
		public void setMessageResourceKey(final String newMessageResourceKey)
		{
			if(!ObjectUtilities.equals(messageResourceKey, newMessageResourceKey))	//if the value is really changing
			{
				final String oldMessageResourceKey=messageResourceKey;	//get the old value
				messageResourceKey=newMessageResourceKey;	//actually change the value
				firePropertyChange(MESSAGE_RESOURCE_KEY_PROPERTY, oldMessageResourceKey, newMessageResourceKey);	//indicate that the value changed
			}
		}

	/**Session constructor with a default model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Image(final GuiseSession session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Image(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultImageModel(session));	//construct the class with a default model
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Image(final GuiseSession session, final String id, final ImageModel model)
	{
		super(session, id, model);	//construct the parent class
		addExportStrategy(DEFAULT_EXPORT_STRATEGY);	//install a default export strategy 
	}

	/**Whether the component has image dragging enabled.*/
//TODO del if not needed	private boolean imageDragEnabled=false;

		/**@return Whether the component has image dragging enabled.*/
//TODO del if not needed		public boolean isImageDragEnabled() {return imageDragEnabled;}

		/**Sets whether the component has image dragging enabled.
		This is a bound property of type <code>Boolean</code>.
		@param newImageDragEnabled <code>true</code> if the component should allow image dragging, else false, else <code>false</code>.
		@see #IMAGE_DRAG_ENABLED_PROPERTY
		*/
/*TODO del if not needed
		public void setImageDragEnabled(final boolean newImageDragEnabled)
		{
			if(imageDragEnabled!=newImageDragEnabled)	//if the value is really changing
			{
				final boolean oldImageDragEnabled=imageDragEnabled;	//get the current value
				imageDragEnabled=newImageDragEnabled;	//update the value
				firePropertyChange(IMAGE_DRAG_ENABLED_PROPERTY, Boolean.valueOf(oldImageDragEnabled), Boolean.valueOf(newImageDragEnabled));
			}
		}
*/

	/**The default transferable object for an image.
	@author Garret Wilson
	*/
	protected static class DefaultTransferable extends AbstractTransferable<Image>
	{
		/**Source constructor.
		@param source The source of the transferable data.
		@exception NullPointerException if the provided source is <code>null</code>.
		*/
		public DefaultTransferable(final Image source)
		{
			super(source);	//construct the parent class
		}

		/**Determines the content types available for this transfer.
		This implementation returns a URI-list content type and the content type of the label.
		@return The content types available for this transfer.
		*/
		public ContentType[] getContentTypes() {return createArray(new ContentType(TEXT, URI_LIST_SUBTYPE, null), getSource().getLabelContentType());}

		/**Transfers data using the given content type.
		@param contentType The type of data expected.
		@return The transferred data, which may be <code>null</code>.
		@exception IllegalArgumentException if the given content type is not supported.
		*/
		public Object transfer(final ContentType contentType)
		{
			final Image image=getSource();	//get the image
			final ImageModel imageModel=image.getModel();	//get the model
			if(match(contentType, TEXT, URI_LIST_SUBTYPE))	//if this is a text/uri-list type
			{
				final URI imageURI=imageModel.getImage();	//get the image URI
				return imageURI!=null ? createURIList(imageURI) : null;	//return the image URI, if there is one
			}
			else if(contentType.match(image.getLabelContentType()))	//if the label has the content type requested
			{
				return image.getSession().determineString(image.getLabel(), image.getLabelResourceKey());	//return the label text
			}
			else	//if we don't support this content type
			{
				throw new IllegalArgumentException("Content type not supported: "+contentType);
			}
		}
	}

}
