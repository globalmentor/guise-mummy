package com.guiseframework.component;

import java.net.URI;

import static com.globalmentor.java.ClassUtilities.*;

import com.guiseframework.model.PendingImageModel;

/**A component that displays an image and is able to indicate when an image is pending.
@author Garret Wilson
*/
public interface PendingImageComponent extends ImageComponent, PendingImageModel
{

	/**The pending image URI bound property.*/
	public final static String PENDING_IMAGE_URI_PROPERTY=getPropertyName(PendingImageComponent.class, "pendingImageURI");

	/**@return The pending image URI, which may be a resource URI, or <code>null</code> if there is no image URI.*/
	public URI getPendingImageURI();

	/**Sets the URI of the pending image.
	This is a bound property.
	@param newPendingImageURI The new URI of the pending image, which may be a resource URI.
	@see #PENDING_IMAGE_URI_PROPERTY
	*/
	public void setPendingImageURI(final URI newPendingImageURI);

}
