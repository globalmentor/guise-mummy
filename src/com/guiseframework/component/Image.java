package com.guiseframework.component;

import com.guiseframework.model.DefaultLabelModel;

/**A simple image component with no descriptory text.
@author Garret Wilson
*/
public class Image extends AbstractImageComponent<Image>
{

	/**Default constructor.*/
	public Image()
	{
		super(new DefaultLabelModel());	//construct the parent class with a default label model
	}

}
