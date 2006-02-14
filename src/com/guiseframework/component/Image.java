package com.guiseframework.component;

import java.net.URI;
import javax.mail.internet.ContentType;

import static com.garretwilson.io.ContentTypeConstants.*;
import static com.garretwilson.io.ContentTypeUtilities.*;
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
public class Image extends AbstractComponent<Image> implements LabeledComponent<Image>
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
		public ContentType[] getContentTypes() {return createArray(new ContentType(TEXT, URI_LIST_SUBTYPE, null), getSource().getLabelTextContentType());}

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
			else if(contentType.match(image.getLabelTextContentType()))	//if the label has the content type requested
			{
				return image.getSession().determineString(image.getLabelText(), image.getLabelTextResourceKey());	//return the label text
			}
			else	//if we don't support this content type
			{
				throw new IllegalArgumentException("Content type not supported: "+contentType);
			}
		}
	}

}
