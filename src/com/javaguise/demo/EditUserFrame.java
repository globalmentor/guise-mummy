package com.javaguise.demo;

import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.event.*;
import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.*;

/**Edit User Guise demonstration frame.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates modal frames, component validity checks, ending modality, and modal cancel functionality.
@author Garret Wilson
*/
public class EditUserFrame extends DefaultModalFrame<DemoUser>
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
		super(session, new FlowLayout(Orientation.Flow.PAGE));	//construct the parent class flowing vertically
		getModel().setLabel("Guise\u2122 Demonstration: Edit User");	//set the frame title	

		final Panel idPanel=new Panel(session, new FlowLayout(Orientation.Flow.LINE));	//create the ID panel flowing horizontally
			//ID
		idControl=new TextControl<String>(session, String.class);	//create the ID input control
		idControl.getModel().setLabel("ID");	//set the ID control label
		idControl.getModel().setEditable(false);	//don't allow the ID to be edited
		idPanel.add(idControl);	//add the ID control to the ID panel
		
		final Panel namePanel=new Panel(session, new FlowLayout(Orientation.Flow.LINE));	//create the name panel flowing horizontally
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

		final Panel emailPanel=new Panel(session, new FlowLayout(Orientation.Flow.LINE));	//create the email panel flowing horizontally
			//email
		emailControl=new TextControl<String>(session, String.class);	//create the email input control
		emailControl.getModel().setLabel("Email *");	//set the email control label
		emailControl.getModel().setValidator(new RegularExpressionStringValidator(session, ".+@.+\\.[a-z]+", true));	//require an email in the correct format
		emailPanel.add(emailControl);	//add the email control to the email panel

		final Panel buttonPanel=new Panel(session, new FlowLayout(Orientation.Flow.LINE));	//create the button panel flowing horizontally
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

		add(idPanel);	//add the ID panel to the frame
		add(namePanel);	//add the name panel to the frame
		add(emailPanel);	//add the email panel to the frame
		add(buttonPanel);	//add the buttonpanel to the frame
	}

	/**Initializes the frame with information for a new user.
	@param id The ID of the new user to be edited.
	@exception NullPointerException if the provided user is <code>null</code>.
	@exception IllegalArgumentException if any user information is invalid.
	*/
	public void setNewUser(final String id)
	{
		try
		{
			idControl.getModel().setValue(id);	//set the user ID
		}
		catch(final ValidationException validationException)	//if the user information is invalid
		{
			throw new IllegalArgumentException(validationException);
		}		
	}

	/**Initializes the frame with user information.
	@param user The user with which to initialize the frame.
	@exception NullPointerException if the provided user is <code>null</code>.
	@exception IllegalArgumentException if any user information is invalid.
	*/
	public void setUser(final DemoUser user)
	{
		try
		{
			idControl.getModel().setValue(user.getID());	//update the values
			firstNameControl.getModel().setValue(user.getFirstName());
			middleNameControl.getModel().setValue(user.getMiddleName());
			lastNameControl.getModel().setValue(user.getLastName());
			emailControl.getModel().setValue(user.getEmail());
		}
		catch(final ValidationException validationException)	//if the user information is invalid
		{
			throw new IllegalArgumentException(validationException);
		}
	}

	/**@return A user representing the information entered in the frame.*/
	public DemoUser getUser()
	{
		return new DemoUser(	//create and return a new user
				idControl.getModel().getValue(),
				firstNameControl.getModel().getValue(),
				middleNameControl.getModel().getValue(),
				lastNameControl.getModel().getValue(),
				emailControl.getModel().getValue());
	}
}
