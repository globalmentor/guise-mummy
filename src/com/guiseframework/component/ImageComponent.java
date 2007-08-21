package com.guiseframework.component;

import java.net.URI;

import static com.garretwilson.lang.ClassUtilities.*;

/**A component that displays an image.
@author Garret Wilson
*/
public interface ImageComponent extends Component
{

	/**The image bound property.*/
	public final static String IMAGE_URI_PROPERTY=getPropertyName(ImageComponent.class, "imageURI");

	/**@return The image URI, which may be a resource URI, or <code>null</code> if there is no image URI.*/
	public URI getImageURI();

	/**Sets the URI of the image.
	This is a bound property of type <code>URI</code>.
	@param newImage The new URI of the image, which may be a resource URI.
	@see #IMAGE_URI_PROPERTY
	*/
	public void setImageURI(final URI newImage);

}
