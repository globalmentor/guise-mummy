package com.guiseframework.model;

import static com.garretwilson.lang.ClassUtilities.*;

/**A model for an image that is pending.
@author Garret Wilson
*/
public interface PendingImageModel extends ImageModel
{
	/**The image pending bound property.*/
	public final static String IMAGE_PENDING_PROPERTY=getPropertyName(PendingImageModel.class, "imagePending");

	/**@return Whether the current image is in the process of transitioning to some other value.*/
	public boolean isImagePending();

}
