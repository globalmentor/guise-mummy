package com.guiseframework.demo;

import java.beans.PropertyVetoException;
import java.io.*;

import com.garretwilson.urf.*;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;

/**URF Process Guise demonstration panel.
Copyright © 2005-2007 GlobalMentor, Inc.
Demonstrates URF processing.
@author Garret Wilson
*/
public class URFProcessPanel extends DefaultNavigationPanel
{

	/**Default constructor.*/
	public URFProcessPanel()
	{
		super(new RegionLayout());	//construct the parent class with a region layout
		setLabel("Guise\u2122 Demonstration: URF Processing");	//set the panel title	

		final LayoutPanel mainPanel=new LayoutPanel();	//create a main panel for the input/output
			//input text
		final TextControl<String> inputTextControl=new TextControl<String>(String.class, 15, 80, false);	//create a text area with no line wrap for input
		inputTextControl.setLabel("Input TURF");	//set the label of the text area
		mainPanel.add(inputTextControl);
			//assertion output text
		final TextControl<String> assertionOutputTextControl=new TextControl<String>(String.class, 10, 80, false);	//create a text area with no line wrap for output of assertions
		assertionOutputTextControl.setLabel("Resulting Assertions");	//set the label of the text area
		assertionOutputTextControl.setEditable(false);	//don't allow the assertion output to be edited
		mainPanel.add(assertionOutputTextControl);
			//TURF output text
		final TextControl<String> turfOutputTextControl=new TextControl<String>(String.class, 15, 80, false);	//create a text area with no line wrap for TURF output
		turfOutputTextControl.setLabel("Resulting TURF");	//set the label of the text area
		turfOutputTextControl.setEditable(false);	//don't allow the TURF output to be edited
		mainPanel.add(turfOutputTextControl);

		final LayoutPanel controlPanel=new LayoutPanel();	//create a panel for URF input/output
			//process button
		final Button processButton=new Button();	//create a button for processing the input
		processButton.setLabel("Process");	//set the button label
		controlPanel.add(processButton);	//add the process button to the control panel
		
		processButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(final ActionEvent actionEvent)
					{
						assertionOutputTextControl.clearValue();	//clear the assertions
						turfOutputTextControl.clearValue();	//clear the TURF
						final String input=inputTextControl.getValue();	//get the input text
						if(input!=null)	//if there is input
						{
							try
							{
								final URFTURFProcessor turfProcessor=new URFTURFProcessor();	//create a new TURF processor
								final URF urf=turfProcessor.process(new LineNumberReader(new StringReader(input)), null);	//process the TURF
								final StringBuilder assertionStringBuilder=new StringBuilder();	//create a string builder for showiong the assertions
									//assertions
								for(final URFTURFProcessor.Assertion assertion:turfProcessor.getAssertions())	//for each assertion
								{
									assertionStringBuilder.append(assertion).append('\n');	//add this assertion
								}
								assertionOutputTextControl.setValue(assertionStringBuilder.toString());	//show the output assertions
									//TURF
								final URFTURFGenerator turfGenerator=new URFTURFGenerator();	//create a new TURF generator
								final StringWriter turfStringWriter=new StringWriter();	//create a new string writer
								turfGenerator.generateResources(turfStringWriter, urf);	//generate the URF to TURF
								turfOutputTextControl.setValue(turfStringWriter.toString());	//show the output TURF
							}
							catch(final IOException ioException)	//if an I/O error occurs
							{
								getSession().notify(ioException);	//notify the user
							}
							catch(final PropertyVetoException propertyVetoException)	//there should never be an error updating the output
							{
								throw new AssertionError(propertyVetoException);
							}
						}						
					}
				});

		add(mainPanel, new RegionConstraints(Region.CENTER));	//add the main panel to the center
		add(controlPanel, new RegionConstraints(Region.LINE_END));	//add the control panel to the end of the line
	}
}
