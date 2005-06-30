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
Copyright © 2005 GlobalMentor, Inc.
Demonstrates integer input controls, integer input validation, radio button controls, required value validation, style IDs, validation.
@author Garret Wilson
*/
public class TemperatureConversionFrame extends NavigationFrame
{

	/**Guise session constructor.
	@param session The Guise session that owns this frame.
	*/
	public TemperatureConversionFrame(final GuiseSession<?> session)
	{
		super(session, new FlowLayout(Axis.Y));	//construct the parent class, flowing vertically
		getModel().setLabel("Temperature Conversion Guise Demonstration");	//set the frame title	

		final Panel temperaturePanel=new Panel(session, new FlowLayout(Axis.X));	//create the root panel flowing horizontally
				
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
		temperaturePanel.add(scalePanel);	//add the scale panel to the temperature panel
		
		final Panel conversionPanel=new Panel(session, new FlowLayout(Axis.Y));	//create the right-hand panel flowing vertically
		
		final Panel inputPanel=new Panel(session, new FlowLayout(Axis.X));	//create the input panel flowing horizontally
		final TextControl<Integer> temperatureInput=new TextControl<Integer>(session, "temperatureInput", Integer.class);	//create a text input control to receive an integer
		temperatureInput.getModel().setLabel("Input Temperature");	//add a label to the text input control
		temperatureInput.getModel().setValidator(new ValueRequiredValidator<Integer>());	//install a validator requiring a value
		inputPanel.add(temperatureInput);	//add the input control to the input panel
		final ActionControl convertButton=new ActionControl(session);	//create a button for initiating the conversion
		convertButton.getModel().setLabel("Convert");	//set the button label
		inputPanel.add(convertButton);	//add the button to the input panel
		conversionPanel.add(inputPanel);	//add the input panel to the conversion panel

		final TextControl<Integer> temperatureOutput=new TextControl<Integer>(session, "temperatureOutput", Integer.class);	//create a text input control to display the result
		temperatureOutput.getModel().setLabel("Output Temperature");	//add a label to the text output control
		conversionPanel.add(temperatureOutput);	//add the output control to the conversion panel
		
		temperaturePanel.add(conversionPanel);	//add the conversion panel to the temperature panel
		
		add(temperaturePanel);	//add the entire temperature panel to the navigation frame

		convertButton.getModel().addActionListener(new ActionListener<ActionModel>()	//when the convert button is pressed
				{
					public void onAction(ActionEvent<ActionModel> actionEvent)
					{
						final int convertedValue;	//we'll convert the value and store it here
						if(celsiusCheckControl.getModel().getValue())	//if the Celsius radio button is selected
						{
							convertedValue=(temperatureInput.getModel().getValue().intValue()*9)/5+32;	//convert: (9c/5)+32
						}
						else if(farenheitCheckControl.getModel().getValue())	//if the Farenheit radio button is selected
						{
							convertedValue=((temperatureInput.getModel().getValue().intValue()-32)*5)/9;	//convert: 5(f-32)/9							
						}
						else	//if neither check control is selected (which should never happen, because we set one to begin with and they are both using a mutual exclusion model group)
						{
							throw new AssertionError("Expected one of the scale radio buttons to be selected.");
						}
						try
						{
							temperatureOutput.getModel().setValue(new Integer(convertedValue));	//store the conversion result in the temperature output
						}
						catch(final ValidationException validationException)	//we have no validator installed in the temperature output text control, so we don't expect changing its value ever to cause any problems
						{
							throw new AssertionError(validationException);
						}						
					}
				});
	}

}
