package com.guiseframework.component;

import java.net.URI;

import com.garretwilson.beans.*;
import com.garretwilson.lang.Objects;

import com.guiseframework.model.*;
import static com.guiseframework.theme.Theme.*;

/**A pending image component that is also an action control.
If a {@link PendingImageModel} is used, the image reflects the value of the model's {@link PendingImageModel#isImagePending()} value.
Otherwise, the pending status will reflect whether {@link #getImageURI()} is non-<code>null</code>.
@author Garret Wilson
*/
public class PendingImageActionControl extends ImageActionControl implements PendingImageComponent
{

	/**The pending image URI, which may be a resource URI, or <code>null</code> if there is no pending image URI.*/
	private URI pendingImageURI=GLYPH_BUSY;

		/**@return The pending image URI, which may be a resource URI, or <code>null</code> if there is no image URI.*/
		public URI getPendingImageURI() {return pendingImageURI;}

		/**Sets the URI of the pending image.
		This is a bound property.
		@param newPendingImageURI The new URI of the pending image, which may be a resource URI.
		@see #PENDING_IMAGE_URI_PROPERTY
		*/
		public void setPendingImageURI(final URI newPendingImageURI)
		{
			if(!Objects.equals(pendingImageURI, newPendingImageURI))	//if the value is really changing
			{
				final URI oldPendingImageURI=pendingImageURI;	//get the old value
				pendingImageURI=newPendingImageURI;	//actually change the value
				firePropertyChange(PENDING_IMAGE_URI_PROPERTY, oldPendingImageURI, newPendingImageURI);	//indicate that the value changed
			}
		}

	/**Determines whether the current image is in the process of transitioning to some other value.
	If the delegate image model is a {@link PendingImageModel} is used, this version delegates to the delegate's {@link PendingImageModel#isImagePending()} value.
	Otherwise, this version returns whether {@link #getImageURI()} is <code>null</code>.
	@return Whether the current image is in the process of transitioning to some other value.
	*/
	public boolean isImagePending()
	{
		final ImageModel imageModel=getImageModel();	//get the image model
		return imageModel instanceof PendingImageModel ? ((PendingImageModel)imageModel).isImagePending() : getImageURI()==null;
	}

	/**Default constructor.*/
	public PendingImageActionControl()
	{
		this(new DefaultLabelModel(), new DefaultImageModel(), new DefaultActionModel(), new DefaultEnableable());	//construct the class with default models
	}

	/**Image model constructor.
	@param imageModel The component image model.
	*/
	public PendingImageActionControl(final ImageModel imageModel)
	{
		this(new DefaultLabelModel(), imageModel, new DefaultActionModel(), new DefaultEnableable());	//construct the class with an image model and other default models
	}

	/**Label model, image model, action model, and enableable object constructor.
	@param labelModel The component label model.
	@param imageModel The component image model.
	@param actionModel The component action model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, image model, action model, and/or enableable object is <code>null</code>.
	*/
	public PendingImageActionControl(final LabelModel labelModel, final ImageModel imageModel, final ActionModel actionModel, final Enableable enableable)
	{
		super(labelModel, imageModel, actionModel, enableable);	//construct the parent class
		if(!(imageModel instanceof PendingImageModel))	//if the image model is not a pending image model, the presence/absence of the image model will change the image pending status
		{
			imageModel.addPropertyChangeListener(IMAGE_URI_PROPERTY, new AbstractGenericPropertyChangeListener<URI>()	//listen for the image URI changing
					{
						public void propertyChange(final GenericPropertyChangeEvent<URI> propertyChangeEvent)	//when the image URI changes
						{
							PendingImageActionControl.this.firePropertyChange(IMAGE_PENDING_PROPERTY, propertyChangeEvent.getOldValue()==null, propertyChangeEvent.getNewValue()==null);	//the image is pending if the image URI is null
						}
					});
		}
	}

}
