package com.garretwilson.guise.demo;

import com.garretwilson.guise.component.*;
import com.garretwilson.guise.component.layout.*;
import com.garretwilson.guise.event.ActionEvent;
import com.garretwilson.guise.event.ActionListener;

import static com.garretwilson.guise.controller.text.xml.CSSStyleConstants.*;

import com.garretwilson.guise.model.*;
import com.garretwilson.guise.session.GuiseSession;
import com.garretwilson.guise.validator.ValidationException;
import com.garretwilson.guise.validator.ValueRequiredValidator;

/**Temperature Conversion Guise demonstration frame.
Copyright � 2005 GlobalMentor, Inc.
Demonstrates float input controls, float input validation, radio button controls,
	required value validation, disabled controls, and style IDs.
@author Garret Wilson
*/
public class TemperatureConversionFrame extends NavigationFrame
{

	/**Guise session constructor.
	@param session The Guise session that owns this frame.
	*/
	public TemperatureConversionFrame(final GuiseSession<?> session)
	{
		super(session);	//construct the parent class
		getModel().setLabel("Temperature Conversion Guise\u2122 Demonstration");	//set the frame title	

		final Panel temperaturePanel=new Panel(session, new FlowLayout(Axis.X));	//create the root panel flowing horizontally

			//input panel
		final Panel inputPanel=new Panel(session, new FlowLayout(Axis.Y));	//create the input panel flowing vertically
		final TextControl<Float> temperatureInput=new TextControl<Float>(session, Float.class);	//create a text input control to receive a float
		temperatureInput.getModel().setLabel("Input Temperature");	//add a label to the text input control
		temperatureInput.getModel().setValidator(new ValueRequiredValidator<Float>(session));	//install a validator requiring a value
		inputPanel.add(temperatureInput);	//add the input control to the input panel
		final TextControl<Float> temperatureOutput=new TextControl<Float>(session, Float.class);	//create a text input control to display the result
		temperatureOutput.getModel().setLabel("Output Temperature");	//add a label to the text output control
		temperatureOutput.getModel().setEnabled(false);	//disable the text output control so that the user cannot modify it
		inputPanel.add(temperatureOutput);	//add the output control to the input panel

		temperaturePanel.add(inputPanel);	//add the input panel to the temperature panel

		final Panel conversionPanel=new Panel(session, new FlowLayout(Axis.Y));	//create the right-hand panel flowing vertically
			
			//scale panel
		final Panel scalePanel=new Panel(session, new FlowLayout(Axis.Y));	//create the scale panel flowing vertically
		scalePanel.setStyleID(GROUP_PANEL_CLASS);	//show that we want to style the scale panel as one that visually groups components
		scalePanel.getModel().setLabel("Input Scale");	//set the panel label
		final CheckControl celsiusCheckControl=new CheckControl(session, CheckControl.CheckType.ELLIPSE);	//create a check control for the Celsius scale, using an ellipse check are
		celsiusCheckControl.getModel().setLabel("Celsius");	//set the label of the check to indicate the scale
		try
		{
			celsiusCheckControl.getModel().setValue(true);	//default to converting from Celsius to Farenheit
		}
		catch(final ValidationException validationException)	//we have no validator installed in the check control model, so we don't expect changing its value ever to cause any problems
		{
			throw new AssertionError(validationException);
		}
		scalePanel.add(celsiusCheckControl);	//add the Celsius check control to the panel	
		final CheckControl farenheitCheckControl=new CheckControl(session, CheckControl.CheckType.ELLIPSE);	//create a check control for the Farenheit scale, using an ellipse check are
		farenheitCheckControl.getModel().setLabel("Farenheit");	//set the label of the check to indicate the scale
		scalePanel.add(farenheitCheckControl);	//add the Farenheit check control to the panel	
			//create a mutual exclusion group and add the Celsius and Farenheit check box boolean value models to get radio button functionality
		final ModelGroup<ValueModel<Boolean>> radioButtonModelGroup=new MutualExclusionModelGroup(celsiusCheckControl.getModel(), farenheitCheckControl.getModel());
		conversionPanel.add(scalePanel);	//add the scale panel to the conversion panel			
			//conversion button
		final Button convertButton=new Button(session);	//create a button for initiating the conversion
		convertButton.getModel().setLabel("Convert");	//set the button label
		convertButton.getModel().addActionListener(new ActionListener<ActionModel>()	//when the convert button is pressed
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)	//convert the temperature in the input field and place the result in the output field
					{
						final float inputValue=temperatureInput.getModel().getValue().floatValue();	//get the input value from the control
						final float outputValue;	//we'll convert the value and store it here
						if(celsiusCheckControl.getModel().getValue())	//if the Celsius radio button is selected
						{
							outputValue=(inputValue*9)/5+32;	//convert: (9c/5)+32
						}
						else if(farenheitCheckControl.getModel().getValue())	//if the Farenheit radio button is selected
						{
							outputValue=((inputValue-32)*5)/9;	//convert: 5(f-32)/9							
						}
						else	//if neither check control is selected (which should never happen, because we set one to begin with and they are both using a mutual exclusion model group)
						{
							throw new AssertionError("Expected one of the scale radio buttons to be selected.");
						}
						try
						{
							temperatureOutput.getModel().setValue(new Float(outputValue));	//store the conversion result in the temperature output control
						}
						catch(final ValidationException validationException)	//we have no validator installed in the temperature output text control, so we don't expect changing its value ever to cause any problems
						{
							throw new AssertionError(validationException);
						}						
					}
				});
		conversionPanel.add(convertButton);	//add the button to the conversion panel
		
		temperaturePanel.add(conversionPanel);	//add the conversion panel to the temperature panel
		
		setContent(temperaturePanel);	//set the entire temperature panel as the navigation frame's content
	}

}
