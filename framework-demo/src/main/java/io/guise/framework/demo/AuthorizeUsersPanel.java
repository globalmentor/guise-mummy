/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.demo;

import java.io.IOException;
import java.util.*;

import io.guise.framework.component.*;
import io.guise.framework.component.layout.*;
import io.guise.framework.event.*;
import io.guise.framework.model.*;
import io.guise.framework.platform.DepictContext;
import io.guise.framework.validator.RegularExpressionStringValidator;

/**
 * Authorize Users Guise demonstration panel. Copyright © 2005-2007 GlobalMentor, Inc. Demonstrates custom table models, editable string table cells, editable
 * boolean table cells, table column validators, tables, and the session notification mechanism.
 * @author Garret Wilson
 */
public class AuthorizeUsersPanel extends LayoutPanel {

	//the table columns
	private final TableColumnModel<String> idColumn;
	private final TableColumnModel<String> lastNameColumn;
	private final TableColumnModel<String> firstNameColumn;
	private final TableColumnModel<String> emailColumn;
	private final TableColumnModel<Boolean> authorizedColumn;
	//the table/list select model
	private final UserAuthorizationTableModel userAuthorizationModel;

	/** Default constructor. */
	public AuthorizeUsersPanel() {
		super(new FlowLayout(Flow.PAGE)); //construct the parent class flowing vertically
		setLabel("Guise\u2122 Demonstration: Authorize Users"); //set the panel title	

		//create the table columns
		idColumn = new DefaultTableColumnModel<String>(String.class, "ID"); //ID
		lastNameColumn = new DefaultTableColumnModel<String>(String.class, "Last Name"); //last name
		firstNameColumn = new DefaultTableColumnModel<String>(String.class, "First Name"); //first name
		emailColumn = new DefaultTableColumnModel<String>(String.class, "Email"); //email
		emailColumn.setEditable(true); //allow the email column to be edited
		emailColumn.setValidator(new RegularExpressionStringValidator(".+@.+\\.[a-z]+", true)); //require an email in the correct format
		authorizedColumn = new DefaultTableColumnModel<Boolean>(Boolean.class, "Authorized"); //authorized
		authorizedColumn.setEditable(true); //allow the authorized column to be edited
		//create and initialize the table model
		userAuthorizationModel = new UserAuthorizationTableModel(idColumn, lastNameColumn, firstNameColumn, emailColumn, authorizedColumn); //create the table model
		//create the table
		final Table userAuthorizationTable = new Table(userAuthorizationModel); //create the table component
		userAuthorizationTable.setLabel("User Authorizations"); //give the table a label
		add(userAuthorizationTable); //add the user authorization table to the panel
		//apply button
		final Button applyButton = new Button(); //create a button for applying the values
		applyButton.setLabel("Apply"); //set the button label
		applyButton.addActionListener(new ActionListener() { //listen for the apply button

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				final Notification notification = new Notification("The use of AJAX in Guise makes the \"Apply\" button unnecessary, because changes take place live. "
						+ "This button would only be useful if AJAX were disabled."); //create a new notification
				getSession().notify(notification); //tell the session to notify the user
			}

		});
		add(applyButton); //add the apply button to the panel
	}

	/**
	 * Updates the view of this component. This versions makes sure the user list is updated from the application.
	 * @param <GC> The type of the context.
	 * @param context Guise context information.
	 * @throws IOException if there is an error updating the view.
	 */
	public <GC extends DepictContext> void updateView(final GC context) throws IOException { //TODO change to depict()
		userAuthorizationModel.clear(); //clear all the users we currently have
		final List<DemoUser> applicationUserList = ((DemoApplication)getSession().getApplication()).getUsers(); //get the application's list of users
		synchronized(applicationUserList) { //don't allow others to modify the application user list while we iterate over it
			userAuthorizationModel.addAll(applicationUserList); //add all the users from the application
		}
		synchronized(userAuthorizationModel) { //don't allow the model of users to be changed by another thread while we sort it
			Collections.sort(userAuthorizationModel); //sort the user list model (each user implements Comparable)
		}
		super.depict(); //do the default model querying for the panel
	}

	/**
	 * A table model based upon a list of users, each column representing a property of the user.
	 * @author Garret Wilson
	 */
	protected class UserAuthorizationTableModel extends AbstractListSelectTableModel<DemoUser> {

		/**
		 * Constructs a demo user list select table model indicating the type of values it can hold, using a default multiple selection strategy.
		 * @param columns The models representing the table columns.
		 */
		public UserAuthorizationTableModel(final TableColumnModel<?>... columns) {
			super(DemoUser.class, columns); //construct the parent class with the class representing users
		}

		@Override
		protected <C> C getCellValue(final DemoUser user, final int rowIndex, final TableColumnModel<C> column) {
			if(column == idColumn) { //ID
				return column.getValueClass().cast(user.getID()); //cast and return the value	
			} else if(column == lastNameColumn) { //last name
				return column.getValueClass().cast(user.getLastName()); //cast and return the value	
			} else if(column == firstNameColumn) { //first name
				return column.getValueClass().cast(user.getFirstName()); //cast and return the value	
			} else if(column == emailColumn) { //email
				return column.getValueClass().cast(user.getEmail()); //cast and return the value	
			} else if(column == authorizedColumn) { //authorized
				return column.getValueClass().cast(Boolean.valueOf(user.isAuthorized())); //cast and return the value	
			} else { //if we don't recognize the column
				throw new AssertionError("Unrecognized column: " + column);
			}
		}

		@Override
		protected <C> void setCellValue(final DemoUser user, final int rowIndex, final TableColumnModel<C> column, final C newCellValue) {
			if(column == emailColumn) { //email
				user.setEmail((String)newCellValue); //cast and set the value
			} else if(column == authorizedColumn) { //authorized
				user.setAuthorized(((Boolean)newCellValue).booleanValue()); //cast and set the value
			} else { //nothing else should be editable
				throw new AssertionError("Unexpected editable column: " + column);
			}
		}

	}
}
