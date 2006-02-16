package com.guiseframework.demo;

import java.util.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.model.*;
import com.guiseframework.validator.*;

/**Edit Users Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates list controls with default representation, thread-safe select model access,
	sorting list control models, listening for select model list changes,
	retrieving navigation panels, invoking modal panels, retrieving modal panel results,
	disabled control models, message dialog frames,
	and accessing a custom Guise application.
@author Garret Wilson
*/
public class EditUsersPanel extends DefaultNavigationPanel
{

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public EditUsersPanel(final GuiseSession session)
	{
		super(session, new FlowLayout(session, Flow.LINE));	//construct the parent class flowing horizontally
		setLabel("Guise\u2122 Demonstration: Edit Users");	//set the panel title	

		final ListControl<DemoUser> userListControl=new ListControl<DemoUser>(session, DemoUser.class, new SingleListSelectionPolicy<DemoUser>());	//create a list control allowing only single selections
		userListControl.setValidator(new ValueRequiredValidator<DemoUser>(session));	//require a value to be selected
		userListControl.setLabel("Users");	//set the list control label
		userListControl.setRowCount(8);	//request eight visible rows in the list
		final List<DemoUser> applicationUserList=((DemoApplication)getSession().getApplication()).getUsers();	//get the application's list of users
		synchronized(applicationUserList)	//don't allow others to modify the application user list while we iterate over it
		{
			userListControl.addAll(applicationUserList);	//add all the users from the application
		}

		synchronized(userListControl)	//don't allow the user select model to be changed by another thread while we sort it
		{
			Collections.sort(userListControl);	//sort the user list model (each user implements Comparable)
		}
		
		final LayoutPanel buttonPanel=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));	//create the button panel flowing horizontally
			//add button
		final Button addButton=new Button(session);	//create the add button
		addButton.setLabel("Add User");	//set the text of the add button
		addButton.addActionListener(new ActionListener()	//if the add button was pressed
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						session.navigateModal(DemoApplication.EDIT_USER_PANEL_NAVIGATION_PATH, new ModalNavigationListener()	//navigate modally to the edit user panel
								{
									public void modalBegan(final ModalEvent modalEvent)	//when modal editing begins
									{
										final String newUserID=((DemoApplication)getSession().getApplication()).generateUserID();	//ask the application to generate a new user ID
										((EditUserPanel)modalEvent.getSource()).setNewUser(newUserID);	//initialize the panel for a new user
									}
									public void modalEnded(final ModalEvent modalEvent)	//when modal editing is finished
									{
										final DemoUser newUser=((EditUserPanel)modalEvent.getSource()).getResult();	//get the modal result
										if(newUser!=null)	//if a new user was created
										{
											userListControl.add(newUser);	//add the new user to the list
											synchronized(userListControl)	//don't allow the user select model to be changed by another thread while we sort it
											{
												Collections.sort(userListControl);	//sort the user list model (each user implements Comparable)
											}
											try
											{
												userListControl.setSelectedValues(newUser);	//select the new user
											}
											catch(final ValidationException validationException)	//we never expect a validation exception
											{
												throw new AssertionError(validationException);
											}
										}
									}
								});
					}
				});
		buttonPanel.add(addButton);	//add the button to the button panel
			//edit button	
		final Button editButton=new Button(session);	//create the edit button
		editButton.setLabel("Edit");	//set the text of the edit button
		editButton.addActionListener(new ActionListener()	//if the edit button was pressed
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						final DemoUser user=userListControl.getSelectedValue();	//get the selected user
						if(user!=null)	//if a user is selected
						{
							session.navigateModal(DemoApplication.EDIT_USER_PANEL_NAVIGATION_PATH, new ModalNavigationListener()	//navigate modally to the edit user panel
									{
										public void modalBegan(final ModalEvent modalEvent)	//when modal editing begins
										{
											((EditUserPanel)modalEvent.getSource()).setUser(user);	//initialize the panel with this user
										}
										public void modalEnded(final ModalEvent modalEvent)	//when modal editing is finished
										{
											final DemoUser newUser=((EditUserPanel)modalEvent.getSource()).getResult();	//get the modal result
											if(newUser!=null)	//if a new user was created
											{
												userListControl.replace(user, newUser);	//replace the user with the new user
												synchronized(userListControl)	//don't allow the user select model to be changed by another thread while we sort it
												{
													Collections.sort(userListControl);	//sort the user list model (each user implements Comparable)
												}
												try
												{
													userListControl.setSelectedValues(newUser);	//select the edited user
												}
												catch(final ValidationException validationException)	//we never expect a validation exception
												{
													throw new AssertionError(validationException);
												}
											}											
										}
									});
						}
				}
				});
		buttonPanel.add(editButton);	//add the button to the button panel
			//remove button	
		final Button removeButton=new Button(session);	//create the remove button
		removeButton.setLabel("Remove");	//set the text of the remove button
		removeButton.addActionListener(new ActionListener()	//if the remove button was pressed
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						final int selectedIndex=userListControl.getSelectedIndex();	//get the selected index
						if(selectedIndex>=0)	//if an index is selected
						{
							final DemoUser user=userListControl.get(selectedIndex);	//get the selected user
								//create a confirmation dialog
							final MessageOptionDialogFrame confirmationDialog=new MessageOptionDialogFrame(session, "Are you sure you want to remove user "+user.getFirstName()+" "+user.getLastName()+"?",
									MessageOptionDialogFrame.Option.YES, MessageOptionDialogFrame.Option.NO);	//present "yes" and "no" options to the user
							confirmationDialog.open(new AbstractGuisePropertyChangeListener<Mode>()	//ask for confirmation
									{		
										public void propertyChange(final GuisePropertyChangeEvent<Mode> propertyChangeEvent)	//when the modal dialog mode changes
										{
												//if the message dialog is no longer modal and the selected option is "yes"
											if(confirmationDialog.getMode()==null && confirmationDialog.getValue()==MessageOptionDialogFrame.Option.YES)
											{
												userListControl.remove(selectedIndex);	//remove the user at the given index												
											}
										}
									});
						}
					}
				});
		buttonPanel.add(removeButton);	//add the button to the button panel
		
		add(userListControl);	//add the list control to the panel
		add(buttonPanel);	//add the button panel to the panel

			//disable the add and remove buttons whenever there are no users 
		userListControl.addListListener(new ListListener<DemoUser>()	//listen for the list being modified
				{
					public void listModified(final ListEvent<DemoUser> listEvent)	//if the list is modified
					{
						final boolean listEmpty=userListControl.isEmpty();	//see if the list is empty
						editButton.setEnabled(!listEmpty);	//only enable the edit button if there are users to edit
						removeButton.setEnabled(!listEmpty);	//only enable the remove button if there are users to remove
						final List<DemoUser> applicationUserList=((DemoApplication)getSession().getApplication()).getUsers();	//get the application's list of users
						synchronized(applicationUserList)	//don't allow others to modify the application user list while we modify it
						{
							applicationUserList.clear();	//clear all the application users
							applicationUserList.addAll(userListControl);	//update the application users with the ones we are editing						
						}
					}
				});	
	}

}
