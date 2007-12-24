package com.guiseframework.component;

import java.net.URI;

import javax.mail.internet.ContentType;

import static com.garretwilson.io.ContentTypeConstants.*;
import static com.garretwilson.io.ContentTypeUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIs.*;
import static com.garretwilson.util.ArrayUtilities.*;

import com.guiseframework.component.transfer.*;
import com.guiseframework.model.*;

/**An abstract implementation of an image component.
This component installs a default export strategy supporting export of the following content types:
<ul>
	<li><code>text/uri-list</code></li>
	<li>The label content type.</li>
</ul>
@author Garret Wilson
*/
public abstract class AbstractImageComponent extends AbstractComponent implements ImageComponent
{

	/**The default export strategy for this component type.*/
	protected final static ExportStrategy<ImageComponent> DEFAULT_EXPORT_STRATEGY=new ExportStrategy<ImageComponent>()
			{
				/**Exports data from the given component.
				@param component The component from which data will be transferred.
				@return The object to be transferred, or <code>null</code> if no data can be transferred.
				*/
				public Transferable<ImageComponent> exportTransfer(final ImageComponent component)
				{
					return new DefaultTransferable(component);	//return a default transferable for this component
				}
			};

	/**The image model used by this component.*/
	private final ImageModel imageModel;

		/**@return The image model used by this component.*/
		protected ImageModel getImageModel() {return imageModel;}

	/**@return The image URI, which may be a resource URI, or <code>null</code> if there is no image URI.*/
	public URI getImageURI() {return getImageModel().getImageURI();}

	/**Sets the URI of the image.
	This is a bound property of type <code>URI</code>.
	@param newImageURI The new URI of the image, which may be a resource URI.
	@see #IMAGE_URI_PROPERTY
	*/
	public void setImageURI(final URI newImageURI) {getImageModel().setImageURI(newImageURI);}

	/**Label model and image model constructor.
	@param labelModel The component label model.
	@param imageModel The component image model.
	@exception NullPointerException if the given label model and/or iamge model is <code>null</code>.
	*/
	public AbstractImageComponent(final LabelModel labelModel, final ImageModel imageModel)
	{
		super(labelModel);	//construct the parent class
		this.imageModel=checkInstance(imageModel, "Image model cannot be null.");	//save the image model
		if(imageModel!=labelModel)	//if the models are different (we'll already be listening to the label model)
		{
			this.imageModel.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the image model
			this.imageModel.addVetoableChangeListener(getRepeatVetoableChangeListener());	//listen and repeat all vetoable changes of the image model
		}
	}
	
	/**The default transferable object for an image.
	@author Garret Wilson
	*/
	protected static class DefaultTransferable extends AbstractTransferable<ImageComponent>
	{
		/**Source constructor.
		@param source The source of the transferable data.
		@exception NullPointerException if the provided source is <code>null</code>.
		*/
		public DefaultTransferable(final ImageComponent source)
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
			final ImageComponent image=getSource();	//get the image
			if(match(contentType, TEXT, URI_LIST_SUBTYPE))	//if this is a text/uri-list type
			{
				final URI imageURI=image.getImageURI();	//get the image URI
				return imageURI!=null ? createURIList(image.getSession().resolveURI(imageURI)) : null;	//return the image URI, if there is one
			}
			else if(contentType.match(image.getLabelContentType()))	//if the label has the content type requested
			{
				final String label=image.getLabel();	//get the image label, if any
				return label!=null ? image.getSession().resolveString(image.getLabel()) : null;	//return the resolved label text, if any
			}
			else	//if we don't support this content type
			{
				throw new IllegalArgumentException("Content type not supported: "+contentType);
			}
		}
	}

}
