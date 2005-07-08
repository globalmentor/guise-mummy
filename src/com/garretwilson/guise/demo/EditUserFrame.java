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
import com.garretwilson.util.Debug;

/**Edit User Guise demonstration frame.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates float input controls, float input validation, radio button controls,
	required value validation, disabled controls, and style IDs.
@author Garret Wilson
*/
public class EditUserFrame extends DialogFrame<Object>
{

	/**Guise session constructor.
	@param session The Guise session that owns this frame.
	*/
	public EditUserFrame(final GuiseSession<?> session)
	{
		super(session);	//construct the parent class
		getModel().setLabel("Edit User Guise\u2122 Demonstration");	//set the frame title	

		final Panel buttonPanel=new Panel(session, new FlowLayout(Axis.X));	//create the button panel flowing horizontally
		final Button okButton=new Button(session);
		okButton.getModel().setLabel("OK");
		okButton.getModel().addActionListener(new ActionListener<ActionModel>()
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{
Debug.trace("user pressed OK");
						if(!hasErrors())
						{
							endModal(null);
						}
					}
				});
		buttonPanel.add(okButton);
		
		setContent(buttonPanel);	//set the entire temperature panel as the navigation frame's content
	}

}
