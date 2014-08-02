/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.component;

import java.net.URI;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;

import com.globalmentor.java.Objects;
import com.guiseframework.model.*;
import com.guiseframework.prototype.ValuePrototype;

/**Image control that can be selected and generates actions, storing a Boolean value in its model representing the selected state.
@author Garret Wilson
*/
public class ImageBooleanSelectActionControl extends AbstractBooleanSelectActionControl implements ImageComponent		//TODO fix the inconsistency of this component implementing LabelDisplayableComponent
{

	/**The rollover image URI bound property.*/
	public final static String ROLLOVER_IMAGE_URI_PROPERTY=getPropertyName(ImageBooleanSelectActionControl.class, "rolloverImageURI");
	/**The selected image URI bound property.*/
	public final static String SELECTED_IMAGE_URI_PROPERTY=getPropertyName(ImageBooleanSelectActionControl.class, "selectedImageURI");

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

	/**The rollover image URI, which may be a resource URI, or <code>null</code> if there is no rollover image URI.*/
	private URI rolloverImageURI=null;

		/**@return The rollover image URI, which may be a resource URI, or <code>null</code> if there is no rollover image URI.*/
		public URI getRolloverImageURI() {return rolloverImageURI;}

		/**Sets the URI of the rollover image.
		This is a bound property of type <code>URI</code>.
		@param newRolloverImageURI The new URI of the rollover image, which may be a resource URI.
		@see #ROLLOVER_IMAGE_URI_PROPERTY
		*/
		public void setRolloverImageURI(final URI newRolloverImageURI)
		{
			if(!Objects.equals(rolloverImageURI, newRolloverImageURI))	//if the value is really changing
			{
				final URI oldRolloverURIImage=rolloverImageURI;	//get the old value
				rolloverImageURI=newRolloverImageURI;	//actually change the value
				firePropertyChange(ROLLOVER_IMAGE_URI_PROPERTY, oldRolloverURIImage, newRolloverImageURI);	//indicate that the value changed
			}			
		}

	/**The selected image URI, which may be a resource URI, or <code>null</code> if there is no selected image URI.*/
	private URI selectedImageURI=null;

		/**@return The selected image URI, which may be a resource URI, or <code>null</code> if there is no selected image URI.*/
		public URI getSelectedImageURI() {return selectedImageURI;}

		/**Sets the URI of the selected image.
		This is a bound property of type <code>URI</code>.
		@param newSelectedImageURI The new URI of the selected image, which may be a resource URI.
		@see #SELECTED_IMAGE_URI_PROPERTY
		*/
		public void setSelectedImageURI(final URI newSelectedImageURI)
		{
			if(!Objects.equals(selectedImageURI, newSelectedImageURI))	//if the value is really changing
			{
				final URI oldSelectedImageURI=selectedImageURI;	//get the old value
				selectedImageURI=newSelectedImageURI;	//actually change the value
				firePropertyChange(SELECTED_IMAGE_URI_PROPERTY, oldSelectedImageURI, newSelectedImageURI);	//indicate that the value changed
			}			
		}

	/**Default constructor.*/
	public ImageBooleanSelectActionControl()
	{
		this(new DefaultInfoModel(), new DefaultImageModel(), new DefaultActionModel(), new DefaultValueModel<Boolean>(Boolean.class, Boolean.FALSE), new DefaultEnableable());	//construct the class with default models
	}

	/**Info model, image model, action model, value model, and enableable object constructor.
	@param infoModel The component info model.
	@param imageModel The component image model.
	@param actionModel The component action model.
	@param valueModel The component value model.
	@param enableable The enableable object in which to store enabled status.
	@throws NullPointerException if the given info model, image model, action model, and/or enableable object is <code>null</code>.
	*/
	public ImageBooleanSelectActionControl(final InfoModel infoModel, final ImageModel imageModel, final ActionModel actionModel, final ValueModel<Boolean> valueModel, final Enableable enableable)
	{
		super(infoModel, actionModel, valueModel, enableable);	//construct the parent class
		this.imageModel=checkInstance(imageModel, "Image model cannot be null.");	//save the image model
		if(imageModel!=infoModel && imageModel!=actionModel && imageModel!=valueModel && imageModel!=enableable)	//if the models are different (we'll already be listening to the other models)
		{
			this.imageModel.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the image model
			this.imageModel.addVetoableChangeListener(getRepeatVetoableChangeListener());	//listen and repeat all vetoable changes of the image model
		}
	}

	/**Prototype constructor.
	@param valuePrototype The prototype on which this component should be based.
	*/
	public ImageBooleanSelectActionControl(final ValuePrototype<Boolean> valuePrototype)
	{
		this(valuePrototype, new DefaultImageModel(), new DefaultActionModel(), valuePrototype, valuePrototype);	//use the value prototype as every needed model except for the image model and action model
	}
}
