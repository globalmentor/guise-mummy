package com.garretwilson.guise.demo;

import com.garretwilson.guise.component.*;
import com.garretwilson.guise.component.layout.*;
import com.garretwilson.guise.event.*;
import com.garretwilson.guise.model.*;
import com.garretwilson.guise.session.GuiseSession;
import com.garretwilson.guise.validator.*;

/**Edit User Guise demonstration frame.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates float input controls, float input validation, radio button controls,
	required value validation, disabled controls, and style IDs.
@author Garret Wilson
*/
public class EditUserFrame extends DefaultModalFrame<User>
{

	private final TextControl<String> idControl; 
	private final TextControl<String> firstNameControl; 
	private final TextControl<String> middleNameControl; 
	private final TextControl<String> lastNameControl; 
	private final TextControl<String> emailControl; 

	/**Guise session constructor.
	@param session The Guise session that owns this frame.
	*/
	public EditUserFrame(final GuiseSession<?> session)
	{
		super(session);	//construct the parent class
		getModel().setLabel("Edit User Guise\u2122 Demonstration");	//set the frame title	

		final Panel idPanel=new Panel(session, new FlowLayout(Axis.X));	//create the ID panel flowing horizontally
			//ID
		idControl=new TextControl<String>(session, String.class);	//create the ID input control
		idControl.getModel().setLabel("ID *");	//set the ID control label
		idControl.getModel().setValidator(new RegularExpressionStringValidator(session, "[a-zA-z][\\w]*", true));	//require an ID beginning with a letter and followed by any number of letters, numbers, or underscores
		idPanel.add(idControl);	//add the ID control to the ID panel
		
		final Panel namePanel=new Panel(session, new FlowLayout(Axis.X));	//create the name panel flowing horizontally
			//first name
		firstNameControl=new TextControl<String>(session, String.class);	//create the first name input control
		firstNameControl.getModel().setLabel("First Name *");	//set the name control label
		firstNameControl.getModel().setValidator(new RegularExpressionStringValidator(session, "\\S+.*", true));	//require at least a single non-whitespace character followed by any other characters
		namePanel.add(firstNameControl);	//add the name control to the name panel
			//middle name
		middleNameControl=new TextControl<String>(session, String.class);	//create the first name input control, but don't require any validation
		middleNameControl.getModel().setLabel("Middle Name");	//set the name control label
		namePanel.add(middleNameControl);	//add the name control to the name panel
			//last name
		lastNameControl=new TextControl<String>(session, String.class);	//create the last name input control
		lastNameControl.getModel().setLabel("Last Name *");	//set the name control label
		lastNameControl.getModel().setValidator(new RegularExpressionStringValidator(session, "\\S+.*", true));	//require at least a single non-whitespace character followed by any other characters
		namePanel.add(lastNameControl);	//add the name control to the name panel

		final Panel emailPanel=new Panel(session, new FlowLayout(Axis.X));	//create the email panel flowing horizontally
			//email
		emailControl=new TextControl<String>(session, String.class);	//create the email input control
		emailControl.getModel().setLabel("Email *");	//set the email control label
		emailControl.getModel().setValidator(new RegularExpressionStringValidator(session, ".+@.+\\.[a-z]+", true));	//require an email in the correct format
		emailPanel.add(emailControl);	//add the email control to the email panel

		final Panel buttonPanel=new Panel(session, new FlowLayout(Axis.X));	//create the button panel flowing horizontally
		final Button okButton=new Button(session);	//create the OK button
		okButton.getModel().setLabel("OK");	//set the text of the OK button
		okButton.getModel().addActionListener(new ActionListener<ActionModel>()	//if the OK button was pressed
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{
						if(isValid())	//if the model values of all form components are valid
						{
							endModal(getUser());	//end the frame modality with the edited user
						}
					}
				});
		buttonPanel.add(okButton);	//add the button to the button panel
		final Button cancelButton=new Button(session);	//create the cancel button
		cancelButton.getModel().setLabel("Cancel");	//set the text of the cancel button
		cancelButton.getModel().addActionListener(new ActionListener<ActionModel>()	//if the cancel button was pressed
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{
						endModal(null);	//end the frame modality with no user
					}
				});
		buttonPanel.add(cancelButton);	//add the button to the button panel

		final Panel editUserPanel=new Panel(session, new FlowLayout(Axis.Y));	//create the entire user panel flowing vertically
		editUserPanel.getModel().setLabel("Edit User");	//set the label of the edit user panel 
		editUserPanel.add(idPanel);	//add the ID panel
		editUserPanel.add(namePanel);	//add the name panel
		editUserPanel.add(emailPanel);	//add the email panel
		editUserPanel.add(buttonPanel);	//add the buttonpanel

		setContent(editUserPanel);	//set the edit user panel as the frame's content
	}

	/**Initializes the frame with user information.
	@param user The user with which to initialize the frame.
	@exception NullPointerException if the provided user is <code>null</code>.
	@exception ValidationException if any user information is invalid.
	*/
	public void setUser(final User user) throws ValidationException
	{
		idControl.getModel().setValue(user.getID());	//update the values
		firstNameControl.getModel().setValue(user.getFirstName());
		middleNameControl.getModel().setValue(user.getMiddleName());
		lastNameControl.getModel().setValue(user.getLastName());
		emailControl.getModel().setValue(user.getEmail());
	}

	/**@return A user representing the information entered in the frame.*/
	public User getUser()
	{
		return new User(	//create and return a new user
				idControl.getModel().getValue(),
				firstNameControl.getModel().getValue(),
				middleNameControl.getModel().getValue(),
				firstNameControl.getModel().getValue(),
				emailControl.getModel().getValue());
	}
}
