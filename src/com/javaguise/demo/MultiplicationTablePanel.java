package com.javaguise.demo;

import com.javaguise.component.*;
import com.javaguise.component.layout.RegionLayout;

import com.javaguise.session.GuiseSession;

/**Multiplication Table Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates table components with default table models and columns.
@author Garret Wilson
*/
public class MultiplicationTablePanel extends DefaultNavigationPanel
{

	/**The maximum value of each factor in the multiplication table.*/
	protected final static int MAX_FACTOR=9;

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public MultiplicationTablePanel(final GuiseSession session)
	{
		super(session, new RegionLayout(session));	//construct the parent class, using a region layout
		getModel().setLabel("Guise\u2122 Demonstration: Multiplication Table");	//set the panel title	

		final Integer[][] multiplicationTableData=new Integer[MAX_FACTOR+1][MAX_FACTOR+1];	//create the table data array
		for(int rowIndex=MAX_FACTOR; rowIndex>=0; --rowIndex)	//for each row
		{
			for(int columnIndex=MAX_FACTOR; columnIndex>=0; --columnIndex)	//for each column
			{
				multiplicationTableData[rowIndex][columnIndex]=new Integer(rowIndex*columnIndex);	//fill this cell with data
			}
		}
		final String[] columnNames=new String[MAX_FACTOR+1];	//create the array of column names
		for(int columnIndex=MAX_FACTOR; columnIndex>=0; --columnIndex)	//for each column
		{
			columnNames[columnIndex]=Integer.toString(columnIndex);	//generate the column name
		}
		final Table multiplicationTable=new Table(session, Integer.class, multiplicationTableData, columnNames);	//create the table component
		multiplicationTable.getModel().setLabel("Multiplication Table");	//give the table a label
		
		add(multiplicationTable);	//add the multiplication table to the center of the panel
	}

}
