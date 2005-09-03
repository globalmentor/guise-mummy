package com.javaguise.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;

/**An image component.
@author Garret Wilson
*/
public class Image extends AbstractModelComponent<ImageModel, Image>
{

	/**The bound property of whether the component has image dragging enabled.*/
//TODO del if not needed	public final static String IMAGE_DRAG_ENABLED_PROPERTY=getPropertyName(Image.class, "imageDragEnabled");

	/**Session constructor with a default model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Image(final GuiseSession<?> session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Image(final GuiseSession<?> session, final String id)
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
	public Image(final GuiseSession<?> session, final String id, final ImageModel model)
	{
		super(session, id, model);	//construct the parent class
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

}
