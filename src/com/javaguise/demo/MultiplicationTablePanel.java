package com.javaguise.demo;

import com.javaguise.component.*;
import com.javaguise.component.layout.CardLayout;
import com.javaguise.component.layout.Flow;
import com.javaguise.component.layout.FlowLayout;
import com.javaguise.component.layout.RegionLayout;
import com.javaguise.event.ActionEvent;
import com.javaguise.event.ActionListener;

import com.javaguise.model.ActionModel;
import com.javaguise.model.DefaultLabelModel;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.IntegerRangeValidator;

/**Multiplication Table Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates dynamic tabbed panels, model info/tooltips, integer range validators,
	and table components with default table models and columns.
@author Garret Wilson
*/
public class MultiplicationTablePanel extends DefaultNavigationPanel
{

	/**The maximum number of tabs.*/
	protected final static int MAX_TAB_COUNT=5;

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public MultiplicationTablePanel(final GuiseSession session)
	{
		super(session, new FlowLayout(session, Flow.LINE));	//construct the parent class flowing horizontally
		getModel().setLabel("Guise\u2122 Demonstration: Multiplication Table");	//set the panel title	

		final TabbedPanel tabbedPanel=new TabbedPanel(session);	//create a tabbed panel
		add(tabbedPanel);	//add the tabbed panel to this panel

		final Component<?> defaultTab=createMultiplicationTableTab(9);	//create a default multiplication table with a maximum factor of 9
		tabbedPanel.add(defaultTab, new CardLayout.Constraints(new DefaultLabelModel(session, "Default")));	//add the default tab
			//create the maximum factor control
		final LayoutPanel controlPanel=new LayoutPanel(getSession(), new FlowLayout(session, Flow.PAGE));	//create a new panel for the controls
		final TextControl<Integer> maxFactorControl=new TextControl<Integer>(session, Integer.class);	//create a text input control to receive an integer
		maxFactorControl.getModel().setLabel("Maximum Factor (1-25)");	//add a label to the text input control
		maxFactorControl.getModel().setValidator(new IntegerRangeValidator(session, 1, 25, 1, true));	//require a value from 1-25
		maxFactorControl.getModel().setInfo("Enter the maximum factor (1-25) for the new multiplication table.");	//set the button information
		controlPanel.add(maxFactorControl);	//add the count control to the control panel
			//create the add button
		final DefaultButton addButton=new DefaultButton(session);	//create the button for adding a tab
		addButton.getModel().setLabel("Add Tab");	//set the button label
		addButton.getModel().setInfo("Add a tab containing a multiplication table of the specified factor.");	//set the button information
		addButton.getModel().addActionListener(new ActionListener<ActionModel>()	//listen for the button being pressed
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)	//if the button is pressed
					{
						if(tabbedPanel.size()<MAX_TAB_COUNT)	//if we don't have too many tabs
						{
							try
							{
								maxFactorControl.validate();	//validate the count control to make sure the value is correct
								final int maxFactor=maxFactorControl.getModel().getValue().intValue();	//find out how many factors to display
								final Component<?> tab=createMultiplicationTableTab(maxFactor);	//create a multiplication table with the desired maximum factor
								tabbedPanel.add(tab, new CardLayout.Constraints(new DefaultLabelModel(session, String.valueOf(maxFactor))));	//add the tab to the tabbed panel
								if(tabbedPanel.size()==MAX_TAB_COUNT)	//if we've reached the maximum tab count
								{
									maxFactorControl.getModel().setEnabled(false);	//disable the count control
									addButton.getModel().setEnabled(false);	//disable the add button
								}
							}
							catch (ComponentExceptions e)
							{
							}
						}
					}
				});
		controlPanel.add(addButton);	//add the add button to this panel
			//create the clear button
		final DefaultButton clearButton=new DefaultButton(session);	//create the button for clearing the tabs
		clearButton.getModel().setLabel("Clear Tabs");	//set the button label
		clearButton.getModel().setInfo("Remove all tabs except the default tab.");	//set the button information
		clearButton.getModel().addActionListener(new ActionListener<ActionModel>()	//listen for the button being pressed
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)	//if the button is pressed
					{
						for(final Component<?> tab:tabbedPanel)	//for each tab in the tabbed panel
						{
							if(tab!=defaultTab)	//if this is not the default tab
							{
								tabbedPanel.remove(tab);	//remove the tab
							}
						}
						maxFactorControl.getModel().setEnabled(true);	//make sure the count control is enabled
						addButton.getModel().setEnabled(true);	//make sure the add button is enabled 
					}
				});
		controlPanel.add(clearButton);	//add the clear button to this panel

		add(controlPanel);	//add the control panel to this panel
	}

	/**Creates a new tab component containing a multiplication table from zero to the given factor.
	@param maxFactor The maximum factor to contain in the multiplication table.
	@return The new tab component to be placed on the tabbed panel.
	*/
	protected Component<?> createMultiplicationTableTab(final int maxFactor)
	{
		
		final LayoutPanel tab=new LayoutPanel(getSession(), new RegionLayout(getSession()));	//create a new panel for the tab

		final Integer[][] multiplicationTableData=new Integer[maxFactor+1][maxFactor+1];	//create the table data array
		for(int rowIndex=maxFactor; rowIndex>=0; --rowIndex)	//for each row
		{
			for(int columnIndex=maxFactor; columnIndex>=0; --columnIndex)	//for each column
			{
				multiplicationTableData[rowIndex][columnIndex]=new Integer(rowIndex*columnIndex);	//fill this cell with data
			}
		}
		final String[] columnNames=new String[maxFactor+1];	//create the array of column names
		for(int columnIndex=maxFactor; columnIndex>=0; --columnIndex)	//for each column
		{
			columnNames[columnIndex]=Integer.toString(columnIndex);	//generate the column name
		}
		final Table multiplicationTable=new Table(getSession(), Integer.class, multiplicationTableData, columnNames);	//create the table component
		multiplicationTable.getModel().setLabel("Multiplication Table up to "+maxFactor);	//give the table a label
		
		tab.add(multiplicationTable);	//add the multiplication table to the tab
		return tab;	//return the tab component we created
	}

}
