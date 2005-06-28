package com.garretwilson.guise.demo;

import com.garretwilson.beans.*;
import com.garretwilson.guise.component.*;
import com.garretwilson.guise.component.layout.*;
import com.garretwilson.guise.model.ValueModel;
import com.garretwilson.guise.session.GuiseSession;

/**Hello User Guise demonstration frame.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates hidden components, input controls, buttons, and listeners.
@author Garret Wilson
*/
public class HelloUserFrame extends NavigationFrame
{

	/**Guise session constructor.
	@param session The Guise session that owns this frame.
	*/
	public HelloUserFrame(final GuiseSession<?> session)
	{
		super(session, new FlowLayout(Axis.Y));	//construct the parent class, flowing vertically
		getModel().setLabel("Hello User Guise Demonstration");	//set the frame title	

		final Label helloUserLabel=new Label(session);	//create a label
		helloUserLabel.setVisible(false);	//don't show the label initially
		add(helloUserLabel);	//add the label to the frame
		
		final ValueControl<String> userInput=new ValueControl<String>(session, String.class);	//create a text input control
		userInput.getModel().setLabel("What's your name?");	//add a label to the text input control
		userInput.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractPropertyValueChangeListener<String>()
				{
					public void propertyValueChange(PropertyValueChangeEvent<String> propertyValueChangeEvent)
					{
						final String user=propertyValueChangeEvent.getNewValue();	//get the name the user entered
						helloUserLabel.getModel().setLabel("Hello, "+user+"!");	//update the label
						helloUserLabel.setVisible(true);	//make the label visible
					}
				});
		add(userInput);	//add the user input control to the form
	}

}
