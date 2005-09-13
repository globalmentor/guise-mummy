package com.javaguise.demo;

import java.util.*;

import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.event.*;
import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;

/**Edit Users Guise demonstration frame.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates list controls with default representation, thread-safe select model access,
	sorting list control models, listening for select model list changes,
	retrieving navigation frames, invoking modal frames, retrieving modal frame results,
	disabled control models, action model confirmation messages, and
	accessing a custom Guise application.
@author Garret Wilson
*/
public class EditUsersFrame extends DefaultFrame
{

	/**Guise session constructor.
	@param session The Guise session that owns this frame.
	*/
	public EditUsersFrame(final GuiseSession<?> session)
	{
		super(session, new FlowLayout(session, Orientation.Flow.LINE));	//construct the parent class flowing horizontally
		getModel().setLabel("Guise\u2122 Demonstration: Edit Users");	//set the frame title	

		final ListControl<DemoUser> userListControl=new ListControl<DemoUser>(session, DemoUser.class, new SingleListSelectionStrategy<DemoUser>());	//create a list control allowing only single selections
		userListControl.getModel().setLabel("Users");	//set the list control label
		userListControl.setRowCount(8);	//request eight visible rows in the list
		final List<DemoUser> applicationUserList=((DemoApplication)getSession().getApplication()).getUsers();	//get the application's list of users
		synchronized(applicationUserList)	//don't allow others to modify the application user list while we iterate over it
		{
			userListControl.getModel().addAll(applicationUserList);	//add all the users from the application
		}

		synchronized(userListControl.getModel())	//don't allow the user select model to be changed by another thread while we sort it
		{
			Collections.sort(userListControl.getModel());	//sort the user list model (each user implements Comparable)
		}
		
		final LayoutPanel buttonPanel=new LayoutPanel(session, new FlowLayout(session, Orientation.Flow.LINE));	//create the button panel flowing horizontally
			//add button
		final Button addButton=new Button(session);	//create the add button
		addButton.getModel().setLabel("Add User");	//set the text of the add button
		addButton.getModel().addActionListener(new ActionListener<ActionModel>()	//if the add button was pressed
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{
						session.navigateModal(DemoApplication.EDIT_USER_FRAME_NAVIGATION_PATH, new ModalListener<DemoUser>()	//navigate modally to the edit user frame
								{
									public void modalBegan(final ModalEvent<DemoUser> modalEvent)	//when modal editing begins
									{
										final String newUserID=((DemoApplication)getSession().getApplication()).generateUserID();	//ask the application to generate a new user ID
										final EditUserFrame editUserFrame=((EditUserFrame)(Object)modalEvent.getSource());	//TODO add better workaround for Java 1.5.0_04 cast requirement 
										editUserFrame.setNewUser(newUserID);	//initialize the frame for a new user
									}
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
							session.navigateModal(DemoApplication.EDIT_USER_FRAME_NAVIGATION_PATH, new ModalListener<DemoUser>()	//navigate modally to the edit user frame
									{
										public void modalBegan(final ModalEvent<DemoUser> modalEvent)	//when modal editing begins
										{
											final EditUserFrame editUserFrame=((EditUserFrame)(Object)modalEvent.getSource());	//TODO add better workaround for Java 1.5.0_04 cast requirement 
											editUserFrame.setUser(user);	//initialize the frame with this user
										}
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
		
		add(userListControl);	//add the list control to the frame
		add(buttonPanel);	//add the button panel to the frame

			//disable the add and remove buttons whenever there are no users 
		userListControl.getModel().addListListener(new ListListener<ListSelectModel<DemoUser>, DemoUser>()	//listen for the list being modified
				{
					public void listModified(final ListEvent<ListSelectModel<DemoUser>, DemoUser> listEvent)	//if the list is modified
					{
						final boolean listEmpty=listEvent.getSource().isEmpty();	//see if the list is empty
						editButton.getModel().setEnabled(!listEmpty);	//only enable the edit button if there are users to edit
						removeButton.getModel().setEnabled(!listEmpty);	//only enable the remove button if there are users to remove
						final List<DemoUser> applicationUserList=((DemoApplication)getSession().getApplication()).getUsers();	//get the application's list of users
						synchronized(applicationUserList)	//don't allow others to modify the application user list while we modify it
						{
							applicationUserList.clear();	//clear all the application users
							applicationUserList.addAll(userListControl.getModel());	//update the application users with the ones we are editing						
						}
					}
				});	
	}

}
