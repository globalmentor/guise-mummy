package com.guiseframework.demo;

import java.io.IOException;
import java.util.*;

import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.model.*;
import com.guiseframework.platform.DepictContext;
import com.guiseframework.validator.RegularExpressionStringValidator;

/**Authorize Users Guise demonstration panel.
Copyright © 2005-2007 GlobalMentor, Inc.
Demonstrates custom table models, editable string table cells, editable boolean table cells,
	table column validators, tables, and the session notification mechanism.
@author Garret Wilson
*/
public class AuthorizeUsersPanel extends LayoutPanel
{

		//the table columns
	private final TableColumnModel<String> idColumn;
	private final TableColumnModel<String> lastNameColumn;
	private final TableColumnModel<String> firstNameColumn;
	private final TableColumnModel<String> emailColumn;
	private final TableColumnModel<Boolean> authorizedColumn;
		//the table/list select model
	private final UserAuthorizationTableModel userAuthorizationModel;

	/**Default constructor.*/
	public AuthorizeUsersPanel()
	{
		super(new FlowLayout(Flow.PAGE));	//construct the parent class flowing vertically
		setLabel("Guise\u2122 Demonstration: Authorize Users");	//set the panel title	

			//create the table columns
		idColumn=new DefaultTableColumnModel<String>(String.class, "ID");	//ID
		lastNameColumn=new DefaultTableColumnModel<String>(String.class, "Last Name");	//last name
		firstNameColumn=new DefaultTableColumnModel<String>(String.class, "First Name");	//first name
		emailColumn=new DefaultTableColumnModel<String>(String.class, "Email");	//email
		emailColumn.setEditable(true);	//allow the email column to be edited
		emailColumn.setValidator(new RegularExpressionStringValidator(".+@.+\\.[a-z]+", true));	//require an email in the correct format
		authorizedColumn=new DefaultTableColumnModel<Boolean>(Boolean.class, "Authorized");	//authorized
		authorizedColumn.setEditable(true);	//allow the authorized column to be edited
			//create and initialize the table model
		userAuthorizationModel=new UserAuthorizationTableModel(idColumn, lastNameColumn, firstNameColumn, emailColumn, authorizedColumn);	//create the table model
			//create the table
		final Table userAuthorizationTable=new Table(userAuthorizationModel);	//create the table component
		userAuthorizationTable.setLabel("User Authorizations");	//give the table a label
		add(userAuthorizationTable);	//add the user authorization table to the panel
			//apply button
		final Button applyButton=new Button();	//create a button for applying the values
		applyButton.setLabel("Apply");	//set the button label
		applyButton.addActionListener(new ActionListener()	//listen for the apply button
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						final Notification notification=new Notification(
								"The use of AJAX in Guise makes the \"Apply\" button unnecessary, because changes take place live. "+
								"This button would only be useful if AJAX were disabled.");	//create a new notification
						getSession().notify(notification);	//tell the session to notify the user
					}
				});
		add(applyButton);	//add the apply button to the panel
	}

	/**Updates the view of this component.
	This versions makes sure the user list is updated from the application.
	@param context Guise context information.
	@exception IOException if there is an error updating the view.
	@see DepictContext.State#UPDATE_VIEW
	*/
	public <GC extends DepictContext> void updateView(final GC context) throws IOException	//TODO change to depict()
	{
		userAuthorizationModel.clear();	//clear all the users we currently have
		final List<DemoUser> applicationUserList=((DemoApplication)getSession().getApplication()).getUsers();	//get the application's list of users
		synchronized(applicationUserList)	//don't allow others to modify the application user list while we iterate over it
		{
			userAuthorizationModel.addAll(applicationUserList);	//add all the users from the application
		}
		synchronized(userAuthorizationModel)	//don't allow the model of users to be changed by another thread while we sort it
		{
			Collections.sort(userAuthorizationModel);	//sort the user list model (each user implements Comparable)
		}
		super.depict();	//do the default model querying for the panel
	}

	/**A table model based upon a list of users, each column representing a property of the user.
	@author Garret Wilson
	*/
	protected class UserAuthorizationTableModel extends AbstractListSelectTableModel<DemoUser>
	{
		/**Constructs a demo user list select table model indicating the type of values it can hold, using a default multiple selection strategy.
		@param columns The models representing the table columns.
		*/
		public UserAuthorizationTableModel(final TableColumnModel<?>... columns)
		{
			super(DemoUser.class, columns);	//construct the parent class with the class representing users
		}

		/**Returns the value's property for the given column.
		@param <C> The type of cell values in the given column.
		@param user The value in this list select model.
		@param rowIndex The zero-based row index of the value.
		@param column The column for which a value should be returned.
		@return The value in the cell at the given row and column, or <code>null</code> if there is no value in that cell.
		@exception IndexOutOfBoundsException if the given row index represents an invalid location for the table.
		@exception IllegalArgumentException if the given column is not one of this table's columns.
		*/
		protected <C> C getCellValue(final DemoUser user, final int rowIndex, final TableColumnModel<C> column)
		{
			if(column==idColumn)	//ID
			{
				return column.getValueClass().cast(user.getID());	//cast and return the value	
			}
			else if(column==lastNameColumn)	//last name
			{
				return column.getValueClass().cast(user.getLastName());	//cast and return the value	
			}
			else if(column==firstNameColumn)	//first name
			{
				return column.getValueClass().cast(user.getFirstName());	//cast and return the value	
			}
			else if(column==emailColumn)	//email
			{
				return column.getValueClass().cast(user.getEmail());	//cast and return the value	
			}
			else if(column==authorizedColumn)	//authorized
			{
				return column.getValueClass().cast(Boolean.valueOf(user.isAuthorized()));	//cast and return the value	
			}
			else	//if we don't recognize the column
			{
				throw new AssertionError("Unrecognized column: "+column);
			}
		}

		/**Sets the value's property for the given column.
		@param <C> The type of cell values in the given column.
		@param user The value in this list select model.
		@param rowIndex The zero-based row index of the value.
		@param column The column for which a value should be returned.
		@param newCellValue The value to place in the cell at the given row and column, or <code>null</code> if there should be no value in that cell.
		@exception IndexOutOfBoundsException if the given row index represents an invalid location for the table.
		@exception IllegalArgumentException if the given column is not one of this table's columns.
		*/
		protected <C> void setCellValue(final DemoUser user, final int rowIndex, final TableColumnModel<C> column, final C newCellValue)
		{
			if(column==emailColumn)	//email
			{
				user.setEmail((String)newCellValue);	//cast and set the value
			}
			else if(column==authorizedColumn)	//authorized
			{
				user.setAuthorized(((Boolean)newCellValue).booleanValue());	//cast and set the value
			}
			else	//nothing else should be editable
			{
				throw new AssertionError("Unexpected editable column: "+column);
			}			
		}

	}
}
