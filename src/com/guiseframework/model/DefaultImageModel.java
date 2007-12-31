package com.guiseframework.model;

import java.net.URI;

import com.garretwilson.lang.Objects;

/**A default implementation of a model for an image.
@author Garret Wilson
*/
public class DefaultImageModel extends AbstractModel implements ImageModel
{

	/**The image URI, which may be a resource URI, or <code>null</code> if there is no image URI.*/
	private URI imageURI;

		/**@return The image URI, which may be a resource URI, or <code>null</code> if there is no image URI.*/
		public URI getImageURI() {return imageURI;}

		/**Sets the URI of the image.
		This is a bound property.
		@param newImageURI The new URI of the image, which may be a resource URI.
		@see #IMAGE_URI_PROPERTY
		*/
		public void setImageURI(final URI newImageURI)
		{
			if(!Objects.equals(imageURI, newImageURI))	//if the value is really changing
			{
				final URI oldImageURI=imageURI;	//get the old value
				imageURI=newImageURI;	//actually change the value
				firePropertyChange(IMAGE_URI_PROPERTY, oldImageURI, newImageURI);	//indicate that the value changed
			}
		}

	/**Default constructor.*/
	public DefaultImageModel()
	{
		this(null);	//construct the class with no image
	}

	/**Image URI constructor.
	@param imageURI The image URI, which may be a resource URI, or <code>null</code> if there is no image URI.
	*/
	public DefaultImageModel(final URI imageURI)
	{
		this.imageURI=imageURI;	//save the image URI
	}

}
