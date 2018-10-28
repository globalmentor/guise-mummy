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

package io.guise.framework.demo;

import java.net.URI;

import com.globalmentor.beans.AbstractGenericPropertyChangeListener;
import com.globalmentor.beans.GenericPropertyChangeEvent;

import io.guise.framework.component.*;
import io.guise.framework.component.layout.*;
import io.guise.framework.converter.*;
import io.guise.framework.model.*;
import io.guise.framework.validator.DecimalRangeValidator;

/**
 * Image Opacity Guise demonstration panel. Copyright © 2005 GlobalMentor, Inc. Demonstrates images, slider controls, shared value models, range validators,
 * number/string converters, and image opacity.
 * @author Garret Wilson
 */
public class ImageOpacityPanel extends LayoutPanel {

	/** Default constructor. */
	public ImageOpacityPanel() {
		super(new FlowLayout(Flow.LINE)); //construct the parent class flowing horizontally
		setLabel("Guise\u2122 Demonstration: Image Opacity"); //set the panel title	

		//image
		final Picture image = new Picture(); //create the image control
		image.setImageURI(URI.create("cowcalf.jpg")); //reference an image in the root directory of the application
		image.setLabel("Cow and Calf");
		image.setDescription("A cow and her minutes-old calf. Use the sliders to change the opacity of the image.");
		add(image); //add the image

		//value model shared among slider controls and text control
		final ValueModel<Float> sliderModel = new DefaultValueModel<Float>(Float.class, 1.0f); //default to 1.0
		sliderModel.setValidator(new DecimalRangeValidator<Float>(0.0f, 1.0f, 0.01f)); //set a range validator for the model
		sliderModel.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Float>() { //listen for value changes

			@Override
			public void propertyChange(GenericPropertyChangeEvent<Float> propertyValueChangeEvent) { //if the opacity value changes
				final Float newValue = propertyValueChangeEvent.getNewValue(); //get the new value
				if(newValue != null) { //if there is a new value
					image.setImageOpacity(newValue.floatValue()); //update the image opacity
				}
			}

		});

		//converter for converting between a float and a percent-styled string
		final Converter<Float, String> percentConverter = new FloatStringLiteralConverter(NumberStringLiteralConverter.Style.PERCENT);

		//horizontal slider using shared value model
		final SliderControl<Float> horizontalSlider = new SliderControl<Float>(sliderModel, Flow.LINE);
		add(horizontalSlider);
		//vertical slider using shared value model
		final SliderControl<Float> verticalSlider = new SliderControl<Float>(sliderModel, Flow.PAGE);
		add(verticalSlider);
		//text control using shared value model
		final TextControl<Float> sliderInput = new TextControl<Float>(sliderModel, percentConverter);
		sliderInput.setLabel("Image Opacity"); //set the label for the model
		add(sliderInput);
	}
}
