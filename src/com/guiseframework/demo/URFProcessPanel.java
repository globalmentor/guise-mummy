package com.guiseframework.demo;

import java.beans.PropertyVetoException;
import java.io.*;
import java.net.URI;

import static com.garretwilson.text.CharacterEncodingConstants.*;
import static com.garretwilson.text.xml.XMLConstants.*;
import com.garretwilson.urf.*;

import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.component.urf.DefaultURFResourceTreeNodeRepresentationStrategy;
import com.guiseframework.event.*;
import com.guiseframework.model.DummyTreeNodeModel;
import com.guiseframework.model.urf.URFDynamicTreeNodeModel;

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
		inputTextControl.setLabel("Input TURF or RDF/XML");	//set the label of the text area
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

		final LayoutPanel controlPanel=new LayoutPanel();	//create a panel for controls
			//process button
		final Button processButton=new Button();	//create a button for processing the input
		processButton.setLabel("Process");	//set the button label
		controlPanel.add(processButton);	//add the process button to the control panel
			//tree control
		final TreeControl urfTreeControl=new TreeControl();	//create a tree control in which to place URF resources
		urfTreeControl.setTreeNodeRepresentationStrategy(URFResource.class, new DefaultURFResourceTreeNodeRepresentationStrategy());	//add a representation strategy for URF resources
		controlPanel.add(urfTreeControl);	//add the tree control to the control panel
		
		processButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(final ActionEvent actionEvent)
					{
						assertionOutputTextControl.clearValue();	//clear the assertions
						turfOutputTextControl.clearValue();	//clear the TURF
						urfTreeControl.setRootNode(new DummyTreeNodeModel());	//clear the tree control
						final String input=inputTextControl.getValue();	//get the input text
						if(input!=null)	//if there is input
						{
							try
							{
								final URI baseURI=URI.create("info:example/");	//use a default base URI
								final InputStream inputStream=new ByteArrayInputStream(input.getBytes(UTF_8));	//get an input stream to the input string
								final AbstractURFProcessor urfProcessor;	//we don't yet know which URF processor we'll use
								final URF urf;	//we'll store the resulting URF data model here
								if(input.startsWith(XML_DECL_START))	//if this is XML
								{
									final URFRDFXMLProcessor urfRDFXMLProcessor=new URFRDFXMLProcessor();	//create a new URF RDF/XML processor
									urf=new DefaultURFRDFXMLIO().readRDFXML(urfRDFXMLProcessor, inputStream, baseURI);	//read URF
									urfProcessor=urfRDFXMLProcessor;	//show which URF processor we used
								}
								else	//otherwise, assume this is TURF
								{
									final URFTURFProcessor urfTURFProcessor=new URFTURFProcessor();	//create a new TURF processor
									urf=DefaultURFTURFIO.readTURF(urfTURFProcessor, inputStream, baseURI);	//read URF
									urfProcessor=urfTURFProcessor;	//show which URF processor we used
								}
								final StringBuilder assertionStringBuilder=new StringBuilder();	//create a string builder for showiong the assertions
									//assertions
								for(final URFTURFProcessor.Assertion assertion:urfProcessor.getAssertions())	//for each assertion
								{
									assertionStringBuilder.append(assertion).append('\n');	//add this assertion
								}
								assertionOutputTextControl.setValue(assertionStringBuilder.toString());	//show the output assertions
									//TURF
								final URFTURFGenerator turfGenerator=new URFTURFGenerator();	//create a new TURF generator
								final StringWriter turfStringWriter=new StringWriter();	//create a new string writer
								turfGenerator.generateResources(turfStringWriter, urf);	//generate the URF to TURF
								turfOutputTextControl.setValue(turfStringWriter.toString());	//show the output TURF
									//resource tree
								urfTreeControl.setRootNode(new URFDynamicTreeNodeModel(urf));	//show the URF in the tree
								urfTreeControl.getRootNode().setExpanded(true);	//expand the root node of the tree
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
