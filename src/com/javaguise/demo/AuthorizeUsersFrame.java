package com.javaguise.demo;

import java.io.IOException;
import java.util.*;

import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.context.GuiseContext;
import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.RegularExpressionStringValidator;

/**Authorize Users Guise demonstration frame.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates custom table models, editable string table cells, editable boolean table cells,
	table column validators, and tables and list controls sharing models.
@author Garret Wilson
*/
public class AuthorizeUsersFrame extends DefaultFrame
{

		//the table columns
	private final TableColumnModel<String> lastNameColumn;
	private final TableColumnModel<String> firstNameColumn;
	private final TableColumnModel<String> emailColumn;
	private final TableColumnModel<Boolean> authorizedColumn;
		//the table/list select model
	private final UserAuthorizationTableModel userAuthorizationModel;

	/**Guise session constructor.
	@param session The Guise session that owns this frame.
	*/
	public AuthorizeUsersFrame(final GuiseSession<?> session)
	{
		super(session, new FlowLayout(Orientation.Flow.PAGE));	//construct the parent class flowing vertically
		getModel().setLabel("Guise\u2122 Demonstration: Authorize Users");	//set the frame title	

		final Panel authorizationPanel=new Panel(session, new FlowLayout(Orientation.Flow.LINE));	//create the authorization panel flowing horizontally
			//create the table columns
		lastNameColumn=new DefaultTableColumnModel<String>(session, String.class, "Last Name");	//last name
		firstNameColumn=new DefaultTableColumnModel<String>(session, String.class, "First Name");	//first name
		emailColumn=new DefaultTableColumnModel<String>(session, String.class, "Email");	//email
		emailColumn.setEditable(true);	//allow the email column to be edited
		emailColumn.setValidator(new RegularExpressionStringValidator(session, ".+@.+\\.[a-z]+", true));	//require an email in the correct format
		authorizedColumn=new DefaultTableColumnModel<Boolean>(session, Boolean.class, "Authorized");	//authorized
		authorizedColumn.setEditable(true);	//allow the authorized column to be edited
			//create and initialize the table model
		userAuthorizationModel=new UserAuthorizationTableModel(session, lastNameColumn, firstNameColumn, emailColumn, authorizedColumn);	//create the table model
			//create the table
		final Table userAuthorizationTable=new Table(session, userAuthorizationModel);	//create the table component
		userAuthorizationTable.getModel().setLabel("User Authorizations");	//give the table a label
		authorizationPanel.add(userAuthorizationTable);	//add the user authorization table to the panel
			//create a separate list control to illustrate model sharing
		final ListControl userListControl=new ListControl<DemoUser>(session, userAuthorizationModel, 10);	//create a list control using the same model as the table
		authorizationPanel.add(userListControl);	//add the user list control to the panel
			//apply button
		final Button applyButton=new Button(session);	//create a button for applying the values
		applyButton.getModel().setLabel("Apply");	//set the button label

		add(authorizationPanel);	//add the authorization panel to the frame
		add(applyButton);	//add the apply button to the frame
	}

	/**Collects the current data from the model of this component.
	@param context Guise context information.
	@exception IOException if there is an error querying the model.
	@see GuiseContext.State#QUERY_MODEL
	*/
	public <GC extends GuiseContext<?>> void queryModel(final GC context) throws IOException
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
		super.queryModel(context);	//do the default model querying for the frame
	}

	/**A table model based upon a list of users, each column representing a property of the user.
	@author Garret Wilson
	*/
	protected class UserAuthorizationTableModel extends AbstractListSelectTableModel<DemoUser>
	{
		/**Constructs a demo user list select table model indicating the type of values it can hold, using a default multiple selection strategy.
		@param session The Guise session that owns this model.
		@param columns The models representing the table columns.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public UserAuthorizationTableModel(final GuiseSession<?> session, final TableColumnModel<?>... columns)
		{
			super(session, DemoUser.class, columns);	//construct the parent class with the class representing users
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
			if(column==lastNameColumn)	//last name
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
		protected <C> C setCellValue(final DemoUser user, final int rowIndex, final TableColumnModel<C> column, final C newCellValue)
		{
			if(column==emailColumn)	//email
			{
//TODO fix				final String oldValue=getCellValue(user, rowIndex, emailColumn);	//get the old value
				user.setEmail((String)newCellValue);	//cast and set the value
//TODO fix				return oldValue;
				return null;
			}
			else if(column==authorizedColumn)	//authorized
			{
				user.setAuthorized(((Boolean)newCellValue).booleanValue());	//cast and set the value
				return null;	//TODO fix; dangerous
			}
			else	//nothing else should be editable
			{
				throw new AssertionError("Unexpected editable column: "+column);
			}			
		}

	}
}
