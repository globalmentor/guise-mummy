package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.net.URI;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.model.*;
import com.guiseframework.prototype.ValuePrototype;

/**Image control that can be selected and generates actions, storing a Boolean value in its model representing the selected state.
@author Garret Wilson
*/
public class ImageBooleanSelectActionControl extends AbstractBooleanSelectActionControl implements ImageComponent		//TODO fix the inconsistency of this component implementing LabelDisplayableComponent
{

	/**The image bound property.*/
	public final static String IMAGE_PROPERTY=getPropertyName(ImageBooleanSelectActionControl.class, "image");
	/**The rollover image bound property.*/
	public final static String ROLLOVER_IMAGE_PROPERTY=getPropertyName(ImageBooleanSelectActionControl.class, "rolloverImage");
	/**The selected image bound property.*/
	public final static String SELECTED_IMAGE_PROPERTY=getPropertyName(ImageBooleanSelectActionControl.class, "selectedImage");

	/**The image URI, which may be a resource URI, or <code>null</code> if there is no image URI.*/
	private URI image=null;

		/**@return The image URI, which may be a resource URI, or <code>null</code> if there is no image URI.*/
		public URI getImageURI() {return image;}

		/**Sets the URI of the image.
		This is a bound property of type <code>URI</code>.
		@param newImage The new URI of the image, which may be a resource URI.
		@see ImageModel#IMAGE_URI_PROPERTY
		*/
		public void setImageURI(final URI newImage)
		{
			if(!ObjectUtilities.equals(image, newImage))	//if the value is really changing
			{
				final URI oldImage=image;	//get the old value
				image=newImage;	//actually change the value
				firePropertyChange(IMAGE_PROPERTY, oldImage, newImage);	//indicate that the value changed
			}			
		}

	/**The rollover image URI, which may be a resource URI, or <code>null</code> if there is no rollover image URI.*/
	private URI rolloverImage=null;

		/**@return The rollover image URI, which may be a resource URI, or <code>null</code> if there is no rollover image URI.*/
		public URI getRolloverImage() {return rolloverImage;}

		/**Sets the URI of the rollover image.
		This is a bound property of type <code>URI</code>.
		@param newRolloverImage The new URI of the rollover image, which may be a resource URI.
		@see #ROLLOVER_IMAGE_PROPERTY
		*/
		public void setRolloverImage(final URI newRolloverImage)
		{
			if(!ObjectUtilities.equals(rolloverImage, newRolloverImage))	//if the value is really changing
			{
				final URI oldRolloverImage=image;	//get the old value
				rolloverImage=newRolloverImage;	//actually change the value
				firePropertyChange(ROLLOVER_IMAGE_PROPERTY, oldRolloverImage, newRolloverImage);	//indicate that the value changed
			}			
		}

	/**The selected image URI, which may be a resource URI, or <code>null</code> if there is no selected image URI.*/
	private URI selectedImage=null;

		/**@return The selected image URI, which may be a resource URI, or <code>null</code> if there is no selected image URI.*/
		public URI getSelectedImage() {return selectedImage;}

		/**Sets the URI of the selected image.
		This is a bound property of type <code>URI</code>.
		@param newSelectedImage The new URI of the selected image, which may be a resource URI.
		@see #SELECTED_IMAGE_PROPERTY
		*/
		public void setSelectedImage(final URI newSelectedImage)
		{
			if(!ObjectUtilities.equals(selectedImage, newSelectedImage))	//if the value is really changing
			{
				final URI oldSelectedImage=image;	//get the old value
				selectedImage=newSelectedImage;	//actually change the value
				firePropertyChange(SELECTED_IMAGE_PROPERTY, oldSelectedImage, newSelectedImage);	//indicate that the value changed
			}			
		}

	/**Default constructor.*/
	public ImageBooleanSelectActionControl()
	{
		this(new DefaultLabelModel(), new DefaultActionModel(), new DefaultValueModel<Boolean>(Boolean.class, Boolean.FALSE), new DefaultEnableable());	//construct the class with default models
	}

	/**Label model, action model, value model, and enableable object constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param valueModel The component value model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, action model, and/or enableable object is <code>null</code>.
	*/
	public ImageBooleanSelectActionControl(final LabelModel labelModel, final ActionModel actionModel, final ValueModel<Boolean> valueModel, final Enableable enableable)
	{
		super(labelModel, actionModel, valueModel, enableable);	//construct the parent class		
	}

	/**Prototype constructor.
	@param valuePrototype The prototype on which this component should be based.
	*/
	public ImageBooleanSelectActionControl(final ValuePrototype<Boolean> valuePrototype)
	{
		this(valuePrototype, new DefaultActionModel(), valuePrototype, valuePrototype);	//use the value prototype as every needed model except for the action model
	}
}
