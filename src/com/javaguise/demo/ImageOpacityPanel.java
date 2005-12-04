package com.javaguise.demo;

import java.net.URI;

import com.javaguise.GuiseSession;
import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.converter.*;
import com.javaguise.event.*;
import com.javaguise.model.*;
import com.javaguise.validator.DecimalRangeValidator;

/**Image Opacity Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates images, slider controls, shared value models, range validators, number/string converters, and image opacity.
@author Garret Wilson
*/
public class ImageOpacityPanel extends DefaultNavigationPanel
{

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public ImageOpacityPanel(final GuiseSession session)
	{
		super(session, new FlowLayout(session, Flow.LINE));	//construct the parent class flowing horizontally
		getModel().setLabel("Guise\u2122 Demonstration: Image Opacity");	//set the panel title	

			//image
		final Image image=new Image(session);	//create the image control
		image.getModel().setImage(URI.create("cowcalf.jpg"));	//reference an image in the root directory of the application
		image.getModel().setLabel("Cow and Calf");
		image.getModel().setMessage("A cow and her minutes-old calf. Use the sliders to change the opacity of the image.");
		add(image);	//add the image

			//value model shared among slider controls and text control
		final ValueModel<Float> sliderModel=new DefaultValueModel<Float>(session, Float.class, 1.0f);	//default to 1.0
		sliderModel.setValidator(new DecimalRangeValidator<Float>(session, 0.0f, 1.0f, 0.01f));	//set a range validator for the model
		sliderModel.setLabel("Image Opacity");	//set the label for the model
		sliderModel.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<ValueModel<Float>, Float>()	//listen for value changes
				{
					public void propertyChange(GuisePropertyChangeEvent<ValueModel<Float>, Float> propertyValueChangeEvent)	//if the opacity value changes
					{
						final Float newValue=propertyValueChangeEvent.getNewValue();	//get the new value
						if(newValue!=null)	//if there is a new value
						{
							image.getModel().setOpacity(newValue.floatValue());	//update the image opacity
						}
					}
				});

			//converter for converting between a float and a percent-styled string
		final Converter<Float, String> percentConverter=new FloatStringLiteralConverter(session, NumberStringLiteralConverter.Style.PERCENT);
		
			//horizontal slider using shared value model
		final SliderControl<Float> horizontalSlider=new SliderControl<Float>(session, sliderModel, Flow.LINE);
		add(horizontalSlider);
			//vertical slider using shared value model
		final SliderControl<Float> verticalSlider=new SliderControl<Float>(session, sliderModel, Flow.PAGE);
		add(verticalSlider);
			//text control using shared value model
		final TextControl<Float> sliderInput=new TextControl<Float>(session, sliderModel, percentConverter);
		add(sliderInput);
	}
}
