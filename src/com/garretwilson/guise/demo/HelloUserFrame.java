package com.garretwilson.guise.demo;

import com.garretwilson.beans.*;
import com.garretwilson.guise.component.*;
import com.garretwilson.guise.component.layout.*;
import com.garretwilson.guise.model.ValueModel;

/**Hello User Guise demonstration frame.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates hidden components, input controls, buttons, and listeners.
@author Garret Wilson
*/
public class HelloUserFrame extends DefaultFrame
{

	/**Default constructor.*/
	public HelloUserFrame()
	{
		super(new FlowLayout(Axis.Y));	//construct the parent class, flowing vertically
		setTitle("Hello User Guise Demonstration");	//set the frame label	

		final Label helloUserLabel=new Label();	//create a label
		add(helloUserLabel);	//add the label to the frame
		
		final ValueControl<String> userInput=new ValueControl<String>(String.class);	//create a text input control
		userInput.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractPropertyValueChangeListener<String>()
				{
					public void propertyValueChange(PropertyValueChangeEvent<String> propertyValueChangeEvent)
					{
						final String user=propertyValueChangeEvent.getNewValue();	//get the name the user entered
						helloUserLabel.getModel().setText("Hello "+user);	//update the label
						helloUserLabel.setVisible(true);	//make the label visible
					}
				});
		add(userInput);	//add the user input control to the form
	}

}
