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
import java.util.regex.*;

import com.globalmentor.beans.*;
import com.globalmentor.html.spec.HTML;
import com.globalmentor.net.ContentType;

import io.guise.framework.component.*;
import io.guise.framework.component.layout.*;
import io.guise.framework.model.*;
import io.guise.framework.validator.*;

/**
 * Text Search Guise demonstration panel. Copyright © 2005-2007 GlobalMentor, Inc. Demonstrates resource input (file upload) controls, file upload content type
 * and size validation, manual component error specification, custom model validation, and text area controls. This demonstration assumes text files are encoded
 * using the system default character encoding.
 * @author Garret Wilson
 */
public class TextSearchPanel extends LayoutPanel {

	/** Instructions for the this demo. */
	protected static final String INSTRUCTIONS = "<?xml version='1.0'?>"
			+ "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>" + "<html xmlns='http://www.w3.org/1999/xhtml'>"
			+ "<head><title>Instructions</title></head>" + "<body>"
			+ "	<p>This demonstration allows you to search a text file for regular expression matches. The text file is assumed to use the system default character encoding.</p>"
			+ "	<p>Select an input text file, and then enter a regular expression. All regular expression matches from the text file will be presented in the output text area.</p>"
			+ "	<p>No regular expression (either <code>null</code> or the empty string) will cause the original text file to be displayed.</p>"
			+ "	<p>Here are some sample regular expressions to try:</p>" + "	<ul>" + "		<li><code>.*</code> (matches every line of the entire text document)</li>"
			+ "		<li><code>\\s\\S+\\s</code> (matches words surrounded by whitespace)</li>"
			+ "		<li><code>{}</code> (an invalid regular expression which will invoke an error via the custom validator)</li>" + "	</ul>" + "</body>" + "</html>";

	/** Default constructor. */
	public TextSearchPanel() {
		super(new FlowLayout(Flow.LINE)); //construct the parent class flowing horizontally
		setLabel("Guise\u2122 Demonstration: Text Search"); //set the panel title	

		//input panel
		final LayoutPanel inputPanel = new LayoutPanel(new FlowLayout(Flow.PAGE)); //create the input panel flowing vertically
		//instructions
		final Message message = new Message(); //create a new message
		message.setLabel("Instructions"); //give a label to the message
		message.setMessageContentType(HTML.XHTML_CONTENT_TYPE); //indicate that the message will be of the "application/xhtml+xml" content type
		message.setMessage(INSTRUCTIONS); //set the instructions
		inputPanel.add(message); //add the message to the input panel
		//search regular expression input
		final TextControl<String> searchRegExControl = new TextControl<String>(String.class); //create a text input control
		searchRegExControl.setLabel("Search Regular Expression"); //set the label for the regex control
		searchRegExControl.setValidator(new PatternSyntaxValidator()); //install our custom regular expression pattern syntax validator
		inputPanel.add(searchRegExControl); //add the search text input to the input panel		
		//file upload control
		final ResourceImportControl resourceImportControl = new ResourceImportControl(); //create the file upload control
		resourceImportControl.setLabel("Input Text File"); //give the file upload control a label	
		//create a validator only allowing text files (files of type text/*) not greater than 64K to be uploaded, and require a value
		final ResourceImportValidator textImportValidator = new ResourceImportValidator(ContentType.create("text", "*"), 1024 * 64, true);
		resourceImportControl.setValidator(textImportValidator); //assign the validator to the the file upload control model		
		inputPanel.add(resourceImportControl); //add the file upload control to the input panel
		//search button
		final Button searchButton = new Button(); //create a button for initiating the upload and search
		searchButton.setLabel("Search"); //set the button label
		inputPanel.add(searchButton); //add the search button to the input panel

		//text
		final TextControl<String> textControl = new TextControl<String>(String.class, 25, 40); //create a text area 25 rows by 40 columns
		textControl.setLabel("Results"); //set the label of the text area
		textControl.setEditable(false); //don't allow the text control to be edited

		//listen for the value of the resource import changing
		resourceImportControl.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<ResourceImport>() {

			@Override
			public void propertyChange(GenericPropertyChangeEvent<ResourceImport> propertyChangeEvent) {
				try {
					final ResourceImport resourceImport = propertyChangeEvent.getNewValue(); //get the new resource import
					final String searchResults; //we'll store the search results here
					if(resourceImport != null && searchRegExControl.isValid()) { //if we have a new resource to import and the search expression is valid
						final Reader reader = new InputStreamReader(resourceImport.getInputStream()); //get an input stream to the resource, assuming the text file uses the default character encoding
						try {
							final char[] buffer = new char[1024]; //create a 1K buffer
							final StringBuilder inputStringBuilder = new StringBuilder(); //create a string builder to hold the input
							int readCount; //keep track of the number of characters read
							while((readCount = reader.read(buffer)) >= 0) { //keep reading buffers until we reach the end of the reader
								inputStringBuilder.append(buffer, 0, readCount); //append the read characters to the input string builder
							}
							final StringBuilder outputStringBuilder = new StringBuilder(); //create a string builder to hold the matches
							final String regularExpression = searchRegExControl.getValue(); //get the entered regular expression (which we already validated in the validator)
							//the regular expression may still be null or the empty string (the latter of which is a valid regular expression)
							if(regularExpression != null && regularExpression.length() > 0) { //if there is a regular expression
								final Pattern pattern = Pattern.compile(regularExpression); //compile the regular expression
								final Matcher matcher = pattern.matcher(inputStringBuilder); //create a matcher to search the input
								while(matcher.find()) { //while we find more matches
									outputStringBuilder.append(matcher.group()).append(' ').append('(').append(matcher.start()).append(')').append('\n'); //append the match on a line by itself
								}
								searchResults = outputStringBuilder.toString(); //use the string we constructed to store in the text area
							} else { //if there was no regular expression
								searchResults = inputStringBuilder.toString(); //use the input string unmodified										
							}
						} finally {
							reader.close(); //always close the reader to the resource
						}
					} else { //if we don't have a new resource import
						searchResults = null; //show that we don't have any search results
					}
					try {
						textControl.setValue(searchResults); //show the search results in the text area
					} catch(final PropertyVetoException propertyVetoException) { //the change should never be vetoed
						throw new AssertionError(propertyVetoException);
					}
				} catch(final IOException ioException) { //if there is an I/O error
					resourceImportControl.setNotification(new Notification(ioException)); //add it to the resource import control for display to the user
				}
			}

		});

		add(inputPanel); //add the input panel to the panel
		add(textControl); //add the text control to the panel
	}

	/**
	 * A pattern validator that can validate whether a regular expression has valid syntax, allowing <code>null</code> values.
	 * @author Garret Wilson
	 */
	public static class PatternSyntaxValidator extends AbstractValidator<String> {

		/** Default constructor that allows <code>null</code> values. */
		public PatternSyntaxValidator() {
			super(false); //don't require non-null values
		}

		@Override
		public void validate(final String value) throws ValidationException {
			super.validate(value); //perform the default validation, checking for null and throwing an exception if a value was required
			if(value != null) { //if a non-null value is given
				try {
					Pattern.compile(value); //try to compile the regular expression
				} catch(final PatternSyntaxException patternSyntaxException) { //if there was a syntax error
					throwInvalidValueValidationException(value); //indicate that the value was invalid
				}
			}
		}
	}
}
