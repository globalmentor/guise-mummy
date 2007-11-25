package com.guiseframework.component;

import com.guiseframework.model.*;

/**A simple image component with no descriptory text.
@author Garret Wilson
*/
public class Image extends AbstractImageComponent
{

	/**Default constructor.*/
	public Image()
	{
		this(new DefaultLabelModel(), new DefaultImageModel());	//construct the parent class with default models
	}

	/**Image model constructor.
	@param imageModel The component image model.
	@exception NullPointerException if the given image model is <code>null</code>.
	*/
	public Image(final ImageModel imageModel)
	{
		this(new DefaultLabelModel(), imageModel);	//construct the parent class with a default label model
	}

	/**Label model and image model constructor.
	@param labelModel The component label model.
	@param imageModel The component image model.
	@exception NullPointerException if the given label model and/or image model is <code>null</code>.
	*/
	public Image(final LabelModel labelModel, final ImageModel imageModel)
	{
		super(labelModel, imageModel);	//construct the parent class
	}

}
