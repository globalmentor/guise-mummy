package com.garretwilson.guise.demo;

import javax.mail.internet.ContentType;

import com.garretwilson.guise.component.*;
import com.garretwilson.guise.component.layout.*;

import com.garretwilson.guise.session.GuiseSession;
import com.garretwilson.guise.validator.ResourceImportValidator;

/**Temperature Conversion Guise demonstration frame.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates resource input (file upload) controls, file upload content type and size validation,
@author Garret Wilson
*/
public class GrepFrame extends DefaultFrame
{

	/**Guise session constructor.
	@param session The Guise session that owns this frame.
	*/
	public GrepFrame(final GuiseSession<?> session)
	{
		super(session);	//construct the parent class
		getModel().setLabel("Guise\u2122 Demonstration: Grep");	//set the frame title	

		final Panel grepPanel=new Panel(session, new FlowLayout(Axis.X));	//create the root panel flowing horizontally

			//input panel
		final Panel inputPanel=new Panel(session, new FlowLayout(Axis.Y));	//create the input panel flowing vertically
			//file upload control
		final ResourceImportControl resourceImportControl=new ResourceImportControl(session);	//create the file upload control
		resourceImportControl.getModel().setLabel("Input Text File");	//give the file upload control a label
				//create a validator only allowing text files (files of type text/*) not greater than 64K to be uploaded, and require a value
		final ResourceImportValidator textImportValidator=new ResourceImportValidator(session, new ContentType("text", "*", null), 1024*64, true);
		resourceImportControl.getModel().setValidator(textImportValidator);	//assign the validator to the the file upload control model		
		inputPanel.add(resourceImportControl);	//add the file upload control to the input panel

			//grep button
		final Button grepButton=new Button(session);	//create a button for initiating the upload and search
		grepButton.getModel().setLabel("Grep");	//set the button label
		inputPanel.add(grepButton);	//add the grep button to the input panel

		grepPanel.add(inputPanel);	//add the input panel to the main panel

		setContent(grepPanel);	//set the entire grep panel as the navigation frame's content
	}

}
