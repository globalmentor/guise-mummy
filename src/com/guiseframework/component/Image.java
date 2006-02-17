package com.guiseframework.component;

import java.net.URI;

import javax.mail.internet.ContentType;

import static com.garretwilson.io.ContentTypeConstants.*;
import static com.garretwilson.io.ContentTypeUtilities.*;
import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.util.ArrayUtilities.*;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.transfer.*;

/**An image component that displays an associated label and description, if present.
This component installs a default export strategy supporting export of the following content types:
<ul>
	<li><code>text/uri-list</code></li>
	<li>The label content type.</li>
</ul>
@author Garret Wilson
*/
public class Image extends AbstractComponent<Image>
{

	/**The bound property of whether the description is visible.*/
	public final static String DESCRIPTION_VISIBLE_PROPERTY=getPropertyName(Image.class, "descriptionVisible");
	/**The image bound property.*/
	public final static String IMAGE_PROPERTY=getPropertyName(Image.class, "image");
	/**The image opacity bound property.*/
	public final static String IMAGE_OPACITY_PROPERTY=getPropertyName(Image.class, "imageOpacity");
	/**The image resource key bound property.*/
	public final static String IMAGE_RESOURCE_KEY_PROPERTY=getPropertyName(Image.class, "imageResourceKey");

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

	/**Whether the description is visible.*/
	private boolean descriptionVisible=true;

		/**@return Whether the description is visible.
		@see #isDisplayed()
		*/
		public boolean isDescriptionVisible() {return descriptionVisible;}

		/**Sets whether the description is visible.
		This is a bound property of type <code>Boolean</code>.
		@param newDescriptionVisible <code>true</code> if the description should be visible, else <code>false</code>.
		@see #DESCRIPTION_VISIBLE_PROPERTY
		*/
		public void setDescriptionVisible(final boolean newDescriptionVisible)
		{
			if(descriptionVisible!=newDescriptionVisible)	//if the value is really changing
			{
				final boolean oldDescriptionVisible=descriptionVisible;	//get the current value
				descriptionVisible=newDescriptionVisible;	//update the value
				firePropertyChange(DESCRIPTION_VISIBLE_PROPERTY, Boolean.valueOf(oldDescriptionVisible), Boolean.valueOf(newDescriptionVisible));
			}
		}

	/**The image URI, or <code>null</code> if there is no image URI.*/
	private URI image=null;

		/**@return The image URI, or <code>null</code> if there is no image URI.*/
		public URI getImage() {return image;}

		/**Sets the URI of the image.
		This is a bound property of type <code>URI</code>.
		@param newImage The new URI of the image.
		@see #IMAGE_PROPERTY
		*/
		public void setImage(final URI newImage)
		{
			if(!ObjectUtilities.equals(image, newImage))	//if the value is really changing
			{
				final URI oldImage=image;	//get the old value
				image=newImage;	//actually change the value
				firePropertyChange(IMAGE_PROPERTY, oldImage, newImage);	//indicate that the value changed
			}			
		}

	/**The image URI resource key, or <code>null</code> if there is no image URI resource specified.*/
	private String imageResourceKey=null;

		/**@return The image URI resource key, or <code>null</code> if there is no image URI resource specified.*/
		public String getImageResourceKey() {return imageResourceKey;}

		/**Sets the key identifying the URI of the image in the resources.
		This is a bound property.
		@param newImageResourceKey The new image URI resource key.
		@see #IMAGE_RESOURCE_KEY_PROPERTY
		*/
		public void setImageResourceKey(final String newImageResourceKey)
		{
			if(!ObjectUtilities.equals(imageResourceKey, newImageResourceKey))	//if the value is really changing
			{
				final String oldImageResourceKey=imageResourceKey;	//get the old value
				imageResourceKey=newImageResourceKey;	//actually change the value
				firePropertyChange(IMAGE_RESOURCE_KEY_PROPERTY, oldImageResourceKey, newImageResourceKey);	//indicate that the value changed
			}
		}

	/**The opacity of the image in the range (0.0-1.0), with a default of 1.0.*/
	private float imageOpacity=1.0f;

		/**@return The opacity of the image in the range (0.0-1.0), with a default of 1.0.*/
		public float getImageOpacity() {return imageOpacity;}

		/**Sets the opacity of the image.
		This is a bound property of type <code>Float</code>.
		@param newImageOpacity The new opacity of the image in the range (0.0-1.0).
		@exception IllegalArgumentException if the given opacity is not within the range (0.0-1.0).
		@see #IMAGE_OPACITY_PROPERTY 
		*/
		public void setImageOpacity(final float newImageOpacity)
		{
			if(newImageOpacity<0.0f || newImageOpacity>1.0f)	//if the new opacity is out of range
			{
				throw new IllegalArgumentException("Opacity "+newImageOpacity+" is not within the allowed range.");
			}
			if(imageOpacity!=newImageOpacity)	//if the value is really changing
			{
				final float oldImageOpacity=imageOpacity;	//get the old value
				imageOpacity=newImageOpacity;	//actually change the value
				firePropertyChange(IMAGE_OPACITY_PROPERTY, new Float(oldImageOpacity), new Float(newImageOpacity));	//indicate that the value changed
			}			
		}

	/**Session constructor.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Image(final GuiseSession session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Image(final GuiseSession session, final String id)
	{
		super(session, id);	//construct the parent class
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
			if(match(contentType, TEXT, URI_LIST_SUBTYPE))	//if this is a text/uri-list type
			{
				final URI imageURI=image.getImage();	//get the image URI
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
