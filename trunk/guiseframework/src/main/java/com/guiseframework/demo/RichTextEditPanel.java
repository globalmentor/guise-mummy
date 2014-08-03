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

package com.guiseframework.demo;

import java.beans.PropertyVetoException;

import com.guiseframework.component.*;

/**
 * Rich Text Edit Guise demonstration panel. Copyright © 2008 GlobalMentor, Inc. Demonstrates XHTML fragment rich text editing and text control model sharing.
 * @author Garret Wilson
 */
public class RichTextEditPanel extends LayoutPanel {

	/** Sample text for the this demo. */
	protected final static String SAMPLE_TEXT = "<h1>Sample Text</h2>" + "<p>This is <em>really</em> rich text.</p>";

	/** Default constructor. */
	public RichTextEditPanel() {
		setLabel("Guise\u2122 Demonstration: Rich Text Edit"); //set the panel title	

		final TextControl<String> xhtmlTextControl = new TextControl<String>(String.class, 10, 80); //create a text input control
		xhtmlTextControl.setLabel("XHTML"); //set the label for the text control
		try {
			xhtmlTextControl.setValue(SAMPLE_TEXT); //set the sample text in the rich text control
			xhtmlTextControl.setValueContentType(XHTML_FRAGMENT_CONTENT_TYPE); //indicate that the text is an XHTML fragment
		} catch(final PropertyVetoException propertyVetoException) { //we don't have a validator installed, so there should be no problems setting the value 
			throw new AssertionError(propertyVetoException);
		}
		add(xhtmlTextControl); //add the rich text control

		//create a text input control to show the HTML source;
		//the source text control will use the HTML text control as its text model, but will use a different content type
		final TextControl<String> sourceTextControl = new TextControl<String>(xhtmlTextControl, 10, 80);
		sourceTextControl.setLabel("Source"); //set the label for the text control
		sourceTextControl.setValueContentType(PLAIN_TEXT_CONTENT_TYPE); //indicate that source text is just plain text
		sourceTextControl.setEditable(false); //don't allow the source to be edited
		add(sourceTextControl); //add the source text control
	}
}
