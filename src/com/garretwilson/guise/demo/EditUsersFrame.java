package com.garretwilson.guise.demo;

import java.util.*;

import com.garretwilson.guise.component.*;
import com.garretwilson.guise.component.layout.*;
import com.garretwilson.guise.event.*;
import com.garretwilson.guise.model.*;
import com.garretwilson.guise.session.GuiseSession;

/**Edit Users Guise demonstration frame.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates list controls with default representation, thread-safe select model access,
	sorting list control models, listening for select model list changes,
	retrieving navigation frames, invoking modal frames, retrieving modal frame results,
	and action model confirmation messages.
@author Garret Wilson
*/
public class EditUsersFrame extends DefaultFrame	//TODO add a way to keep user IDs from being duplicated
{

	/**The application-relative navigation path to the edit user modal frame.*/
	protected final static String EDIT_USER_FRAME_NAVIGATION_PATH="edituser";

	/**Guise session constructor.
	@param session The Guise session that owns this frame.
	*/
	public EditUsersFrame(final GuiseSession<?> session)
	{
		super(session);	//construct the parent class
		getModel().setLabel("Guise\u2122 Demonstration: Edit Users");	//set the frame title	

		final ListControl<DemoUser> userListControl=new ListControl<DemoUser>(session, DemoUser.class, new SingleSelectionStrategy<DemoUser>());	//create a list control allowing only single selections
		userListControl.getModel().setLabel("Users");	//set the list control label
		userListControl.setRowCount(8);	//request eight visible rows in the list

		userListControl.getModel().add(new DemoUser("user1", "Jane", null, "Smith", "janesmith@example.com"));	//add example users
		userListControl.getModel().add(new DemoUser("user2", "John", null, "Smith", "johnsmith@example.com"));
		userListControl.getModel().add(new DemoUser("user3", "Jill", null, "Jones", "jilljones@example.com"));

		synchronized(userListControl.getModel())	//don't allow the user select model to be changed by another thread while we sort it
		{
			Collections.sort(userListControl.getModel());	//sort the user list model (each user implements Comparable)
		}
		
		final Panel buttonPanel=new Panel(session, new FlowLayout(Axis.X));	//create the button panel flowing horizontally
			//add button
		final Button addButton=new Button(session);	//create the add button
		addButton.getModel().setLabel("Add User");	//set the text of the add button
		addButton.getModel().addActionListener(new ActionListener<ActionModel>()	//if the add button was pressed
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{
						session.releaseNavigationFrame(EDIT_USER_FRAME_NAVIGATION_PATH);	//make sure we'll be going to a blank slate
						session.navigateModal(EDIT_USER_FRAME_NAVIGATION_PATH, new ModalListener<DemoUser>()	//navigate modally to the edit user frame
								{
									public void modalEnded(final ModalEvent<DemoUser> modalEvent)	//when modal editing is finished
									{
										final DemoUser newUser=modalEvent.getResult();	//get the modal result
										if(newUser!=null)	//if a new user was created
										{
											userListControl.getModel().add(newUser);	//add the new user to the list
											synchronized(userListControl.getModel())	//don't allow the user select model to be changed by another thread while we sort it
											{
												Collections.sort(userListControl.getModel());	//sort the user list model (each user implements Comparable)
											}
											userListControl.getModel().setSelectedValues(newUser);	//select the new user
										}
									}
								});
					}
				});
		buttonPanel.add(addButton);	//add the button to the button panel
			//edit button	
		final Button editButton=new Button(session);	//create the edit button
		editButton.getModel().setLabel("Edit");	//set the text of the edit button
		editButton.getModel().addActionListener(new ActionListener<ActionModel>()	//if the edit button was pressed
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{
						final DemoUser user=userListControl.getModel().getSelectedValue();	//get the selected user
						if(user!=null)	//if a user is selected
						{
							final EditUserFrame editUserFrame=(EditUserFrame)session.getNavigationFrame(EDIT_USER_FRAME_NAVIGATION_PATH);	//get the edit user frame
							editUserFrame.setUser(user);	//initialize the frame with this user
							session.navigateModal(EDIT_USER_FRAME_NAVIGATION_PATH, new ModalListener<DemoUser>()	//navigate modally to the edit user frame
									{
										public void modalEnded(final ModalEvent<DemoUser> modalEvent)	//when modal editing is finished
										{
											final DemoUser newUser=modalEvent.getResult();	//get the modal result
											if(newUser!=null)	//if a new user was created
											{
												userListControl.getModel().replace(user, newUser);	//replace the user with the new user
												synchronized(userListControl.getModel())	//don't allow the user select model to be changed by another thread while we sort it
												{
													Collections.sort(userListControl.getModel());	//sort the user list model (each user implements Comparable)
												}
												userListControl.getModel().setSelectedValues(newUser);	//select the edited user
											}											
										}
									});
						}
				}
				});
		buttonPanel.add(editButton);	//add the button to the button panel
			//remove button	
		final Button removeButton=new Button(session);	//create the remove button
		removeButton.getModel().setLabel("Remove");	//set the text of the remove button
		removeButton.getModel().setConfirmationMessage(new DefaultMessageModel(session, "Are you sure you want to remove this user?"));	//add a confirmation message to the button's action model
		removeButton.getModel().addActionListener(new ActionListener<ActionModel>()	//if the remove button was pressed
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{
						final int selectedIndex=userListControl.getModel().getSelectedIndex();	//get the selected index
						if(selectedIndex>=0)	//if an index is selected
						{
							userListControl.getModel().remove(selectedIndex);	//remove the user at the given index
						}
					}
				});
		buttonPanel.add(removeButton);	//add the button to the button panel
		
		final Panel editUsersPanel=new Panel(session, new FlowLayout(Axis.X));	//create the entire users panel flowing horizontally
		editUsersPanel.getModel().setLabel("Edit Users");	//set the label of the edit users panel 
		editUsersPanel.add(userListControl);	//add the list control
		editUsersPanel.add(buttonPanel);	//add the button panel

			//disable the add and remove buttons whenever there are no users 
		userListControl.getModel().addListListener(new ListListener<SelectModel<DemoUser>, DemoUser>()	//listen for the list being modified
				{
					public void listModified(final ListEvent<SelectModel<DemoUser>, DemoUser> listEvent)	//if the list is modified
					{
						final boolean listEmpty=listEvent.getSource().isEmpty();	//see if the list is empty
						editButton.getModel().setEnabled(!listEmpty);	//only enable the edit button if there are users to edit
						removeButton.getModel().setEnabled(!listEmpty);	//only enable the remove button if there are users to remove
					}
				});	
		
		setContent(editUsersPanel);	//set the edit users panel as the frame's content
	}

}
