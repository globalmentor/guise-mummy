package com.javaguise.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.net.URI;
import java.util.MissingResourceException;
import com.garretwilson.lang.ObjectUtilities;
import com.javaguise.component.layout.Orientation;
import com.javaguise.model.ActionModel;
import com.javaguise.model.DefaultActionModel;
import com.javaguise.model.ImageModel;
import com.javaguise.session.GuiseSession;

/**Control with an action model rendered as a button.
If an image is specified, it will be used instead of the button label, if possible.
@author Garret Wilson
*/
public class Button extends AbstractActionControl<Button>
{

	/**The image bound property.*/
	public final static String IMAGE_PROPERTY=getPropertyName(Button.class, "image");
	/**The image resource key bound property.*/
	public final static String IMAGE_RESOURCE_KEY_PROPERTY=getPropertyName(Button.class, "imageResourceKey");

	/**The image URI, or <code>null</code> if there is no image URI.*/
	private URI image=null;

		/**Determines the URI of the image.
		If an image is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
		@return The image URI, or <code>null</code> if there is no image URI.
		@exception MissingResourceException if there was an error loading the value from the resources.
		@see #getImageResourceKey()
		*/
		public URI getImage() throws MissingResourceException
		{
			return getURI(image, getImageResourceKey(), (Orientation.Flow)null);	//get the value or the resource, if available
		}

		/**Sets the URI of the image.
		This is a bound property of type <code>URI</code>.
		@param newImage The new URI of the image.
		@see ImageModel#IMAGE_PROPERTY
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
		@see ImageModel#IMAGE_RESOURCE_KEY_PROPERTY
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

	/**Session constructor with a default data model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Button(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Button(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultActionModel(session));	//construct the class with a default model
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public Button(final GuiseSession session, final ActionModel model)
	{
		this(session, null, model);	//construct the component, indicating that a default ID should be used
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Button(final GuiseSession session, final String id, final ActionModel model)
	{
		super(session, id, model);	//construct the parent class
	}

}
