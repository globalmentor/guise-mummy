/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.demo;

import java.beans.PropertyVetoException;
import java.io.*;
import java.net.URI;

import org.urframework.*;
import org.urframework.io.DefaultURFRDFXMLIO;
import org.urframework.io.DefaultURFTURFIO;

import static com.globalmentor.w3c.spec.XML.*;
import static java.nio.charset.StandardCharsets.*;
import static org.urframework.TURF.*;

import com.globalmentor.w3c.spec.HTML;

import io.guise.framework.component.*;
import io.guise.framework.component.layout.*;
import io.guise.framework.component.urf.DefaultURFResourceTreeNodeRepresentationStrategy;
import io.guise.framework.event.*;
import io.guise.framework.model.DummyTreeNodeModel;
import io.guise.framework.model.Notification;
import io.guise.framework.model.Notification.Severity;
import io.guise.framework.model.urf.URFDynamicTreeNodeModel;

/**
 * URF Process Guise demonstration panel. Copyright © 2007 GlobalMentor, Inc. Demonstrates URF processing and tree controls.
 * @author Garret Wilson
 */
public class URFProcessPanel extends LayoutPanel {

	/** Instructions for the this demo. */
	protected static final String INSTRUCTIONS = "<?xml version='1.0'?>"
			+ "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>" + "<html xmlns='http://www.w3.org/1999/xhtml'>"
			+ "<head><title>Instructions</title></head>" + "<body>"
			+ "	<p>This is a Guise\u2122 demonstration of the <a href=\"http://www.urf.name/\">Uniform Resource Framework (URF)</a>.</p>"
			+ "	<p>Input one of the following and select \"Process\":</p>" + "	<ul>" + "	  <li>TURF (beginning with \"`URF\")</li>"
			+ "	  <li>RDF/XML (beginning with \"&lt;XML\")</li>" + "	</ul>"
			+ "	<p>The resulting assertions will be listed, along with a TURF representation of the URF instance. The tree control will allow you to dynamically explore the URF graph.</p>"
			+ "</body>" + "</html>";

	/** Default constructor. */
	public URFProcessPanel() {
		super(new RegionLayout()); //construct the parent class with a region layout
		setLabel("Guise\u2122 Demonstration: URF Processing"); //set the panel title

		final LayoutPanel mainPanel = new LayoutPanel(); //create a main panel for the input/output
		//input text
		final TextControl<String> inputTextControl = new TextControl<String>(String.class, 15, 80, false); //create a text area with no line wrap for input
		inputTextControl.setLabel("Input TURF (beginning with \"`URF\") or RDF/XML (beginning with \"<XML\")"); //set the label of the text area
		mainPanel.add(inputTextControl);
		//assertion output text
		final TextControl<String> assertionOutputTextControl = new TextControl<String>(String.class, 10, 80, false); //create a text area with no line wrap for output of assertions
		assertionOutputTextControl.setLabel("Resulting Assertions"); //set the label of the text area
		assertionOutputTextControl.setEditable(false); //don't allow the assertion output to be edited
		mainPanel.add(assertionOutputTextControl);
		//TURF output text
		final TextControl<String> turfOutputTextControl = new TextControl<String>(String.class, 15, 80, false); //create a text area with no line wrap for TURF output
		turfOutputTextControl.setLabel("Resulting TURF"); //set the label of the text area
		turfOutputTextControl.setEditable(false); //don't allow the TURF output to be edited
		mainPanel.add(turfOutputTextControl);

		final LayoutPanel controlPanel = new LayoutPanel(); //create a panel for controls
		final Heading heading = new Heading(0); //create a top-level heading
		heading.setLabel("Guise\u2122 URF Processing Demo"); //set the heading text
		controlPanel.add(heading); //add the heading to the control panel
		//instructions
		final TextBox text = new TextBox(); //create new text
		text.setLabel("Instructions"); //give a label to the text
		text.setTextContentType(HTML.XHTML_CONTENT_TYPE); //indicate that the message will be of the "application/xhtml+xml" content type
		text.setText(INSTRUCTIONS); //set the instructions
		controlPanel.add(text); //add the text message to the control panel
		//process button
		final Button processButton = new Button(); //create a button for processing the input
		processButton.setLabel("Process"); //set the button label
		controlPanel.add(processButton); //add the process button to the control panel
		//tree control
		final TreeControl urfTreeControl = new TreeControl(); //create a tree control in which to place URF resources
		urfTreeControl.setLabel("URF Resource Tree"); //set the tree control label
		urfTreeControl.setTreeNodeRepresentationStrategy(URFResource.class, new DefaultURFResourceTreeNodeRepresentationStrategy()); //add a representation strategy for URF resources
		controlPanel.add(urfTreeControl); //add the tree control to the control panel

		processButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent actionEvent) {
				assertionOutputTextControl.clearValue(); //clear the assertions
				turfOutputTextControl.clearValue(); //clear the TURF
				urfTreeControl.setRootNode(new DummyTreeNodeModel()); //clear the tree control
				final String input = inputTextControl.getValue(); //get the input text
				if(input != null) { //if there is input
					try {
						final URI baseURI = URI.create("info:example/"); //use a default base URI
						final InputStream inputStream = new ByteArrayInputStream(input.getBytes(UTF_8)); //get an input stream to the input string
						final AbstractURFProcessor urfProcessor; //we don't yet know which URF processor we'll use
						final URF urf; //we'll store the resulting URF data model here
						if(input.startsWith(SIGNATURE)) { //if this is TURF
							final URFTURFProcessor urfTURFProcessor = new URFTURFProcessor(); //create a new TURF processor
							urf = DefaultURFTURFIO.readTURF(urfTURFProcessor, inputStream, baseURI); //read URF
							urfProcessor = urfTURFProcessor; //show which URF processor we used
						} else if(input.startsWith(XML_DECL_START)) { //if this is XML
							final URFRDFXMLProcessor urfRDFXMLProcessor = new URFRDFXMLProcessor(); //create a new URF RDF/XML processor
							urf = new DefaultURFRDFXMLIO().readRDFXML(urfRDFXMLProcessor, inputStream, baseURI); //read URF
							urfProcessor = urfRDFXMLProcessor; //show which URF processor we used
						} else { //if this is unrecognized content
							getSession().notify(new Notification("Input must start with " + SIGNATURE + " or " + XML_DECL_START + ".", Severity.ERROR)); //notify the user
							return; //don't process the input further
						}
						final StringBuilder assertionStringBuilder = new StringBuilder(); //create a string builder for showiong the assertions
						//assertions
						for(final URFTURFProcessor.Assertion assertion : urfProcessor.getAssertions()) { //for each assertion
							assertionStringBuilder.append(assertion).append('\n'); //add this assertion
						}
						assertionOutputTextControl.setValue(assertionStringBuilder.toString()); //show the output assertions
						//TURF
						final URFTURFGenerator turfGenerator = new URFTURFGenerator(); //create a new TURF generator
						final StringBuilder turfStringBuilder = new StringBuilder(); //create a new string builder
						turfGenerator.generateResources(turfStringBuilder, urf); //generate the URF to TURF
						turfOutputTextControl.setValue(turfStringBuilder.toString()); //show the output TURF
						//resource tree
						urfTreeControl.setRootNode(new URFDynamicTreeNodeModel(urf)); //show the URF in the tree
						urfTreeControl.getRootNode().setExpanded(true); //expand the root node of the tree
					} catch(final IOException ioException) { //if an I/O error occurs
						getSession().notify(ioException); //notify the user
					} catch(final PropertyVetoException propertyVetoException) { //there should never be an error updating the output
						throw new AssertionError(propertyVetoException);
					}
				} else { //if there is no input
					getSession().notify(new Notification("There is no input to process.")); //notify the user of the lack of input							
				}
			}

		});

		add(mainPanel, new RegionConstraints(Region.CENTER)); //add the main panel to the center
		add(controlPanel, new RegionConstraints(Region.LINE_END)); //add the control panel to the end of the line
	}
}
