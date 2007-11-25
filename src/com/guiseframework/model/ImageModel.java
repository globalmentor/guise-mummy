package com.guiseframework.model;

import java.net.URI;

import static com.garretwilson.lang.ClassUtilities.*;

/**A model for an image.
@author Garret Wilson
*/
public interface ImageModel extends Model
{
	/**The image URI bound property.*/
	public final static String IMAGE_URI_PROPERTY=getPropertyName(ImageModel.class, "imageURI");

	/**@return The image URI, which may be a resource URI, or <code>null</code> if there is no image URI.*/
	public URI getImageURI();

	/**Sets the URI of the image.
	This is a bound property.
	@param newImageURI The new URI of the image, which may be a resource URI.
	@see #IMAGE_URI_PROPERTY
	*/
	public void setImageURI(final URI newImageURI);

}
