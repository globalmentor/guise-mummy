package com.javaguise.demo;

import java.io.*;
import java.util.regex.*;

import javax.mail.internet.ContentType;

import com.javaguise.GuiseSession;
import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.event.AbstractGuisePropertyChangeListener;
import com.javaguise.event.GuisePropertyChangeEvent;

import com.javaguise.model.MessageModel;
import com.javaguise.model.ResourceImport;
import com.javaguise.model.ValueModel;
import com.javaguise.validator.AbstractValidator;
import com.javaguise.validator.ResourceImportValidator;
import com.javaguise.validator.ValidationException;

/**Text Search Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates resource input (file upload) controls, file upload content type and size validation,
	manual component error specification, custom model validation, and text area controls.
This demonstration assumes text files are encoded using the system default character encoding.
@author Garret Wilson
*/
public class TextSearchPanel extends DefaultNavigationPanel
{

	/**Instructions for the this demo.*/
	protected final static String INSTRUCTIONS=
		"<?xml version='1.0'?>"+
		"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>"+
		"<html xmlns='http://www.w3.org/1999/xhtml'>"+
		"<head><title>Instructions</title></head>"+
		"<body>"+
		"	<p>This demonstration allows you to search a text file for regular expression matches. The text file is assumed to use the system default character encoding.</p>"+
		"	<p>Select an input text file, and then enter a regular expression. All regular expression matches from the text file will be presented in the output text area.</p>"+
		"	<p>No regular expression (either <code>null</code> or the empty string) will cause the original text file to be displayed.</p>"+
		"	<p>Here are some sample regular expressions to try:</p>"+
		"	<ul>"+
		"		<li><code>.*</code> (matches every line of the entire text document)</li>"+
		"		<li><code>\\s\\S+\\s</code> (matches words surrounded by whitespace)</li>"+
		"		<li><code>{}</code> (an invalid regular expression which will invoke an error via the custom validator)</li>"+
		"	</ul>"+
		"</body>"+
		"</html>";

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public TextSearchPanel(final GuiseSession session)
	{
		super(session, new FlowLayout(session, Flow.LINE));	//construct the parent class flowing horizontally
		getModel().setLabel("Guise\u2122 Demonstration: Text Search");	//set the panel title	

			//input panel
		final LayoutPanel inputPanel=new LayoutPanel(session, new FlowLayout(session, Flow.PAGE));	//create the input panel flowing vertically
			//instructions
		final Message message=new Message(session);	//create a new message
		message.getModel().setLabel("Instructions");	//give a label to the message
		message.getModel().setMessageContentType(MessageModel.XHTML_CONTENT_TYPE);	//indicate that the message will be of the "application/xhtml+xml" content type
		message.getModel().setMessage(INSTRUCTIONS);	//set the instructions
		inputPanel.add(message);	//add the message to the input panel
			//search regular expression input
		final TextControl<String> searchRegExControl=new TextControl<String>(session, String.class);	//create a text input control
		searchRegExControl.getModel().setLabel("Search Regular Expression");	//set the label for the regex control
		searchRegExControl.getModel().setValidator(new PatternSyntaxValidator(session));	//install our custom regular expression pattern syntax validator
		inputPanel.add(searchRegExControl);	//add the search text input to the input panel		
			//file upload control
		final ResourceImportControl resourceImportControl=new ResourceImportControl(session);	//create the file upload control
		resourceImportControl.getModel().setLabel("Input Text File");	//give the file upload control a label	
				//create a validator only allowing text files (files of type text/*) not greater than 64K to be uploaded, and require a value
		final ResourceImportValidator textImportValidator=new ResourceImportValidator(session, new ContentType("text", "*", null), 1024*64, true);
		resourceImportControl.getModel().setValidator(textImportValidator);	//assign the validator to the the file upload control model		
		inputPanel.add(resourceImportControl);	//add the file upload control to the input panel
			//search button
		final Button searchButton=new Button(session);	//create a button for initiating the upload and search
		searchButton.getModel().setLabel("Search");	//set the button label
		inputPanel.add(searchButton);	//add the search button to the input panel

			//text area
		final TextAreaControl textAreaControl=new TextAreaControl(session, 25, 40);	//create a text area control 25 rows by 40 columns
		textAreaControl.getModel().setLabel("Results");	//set the label of the text area
		textAreaControl.getModel().setEditable(false);	//don't allow the text area control to be edited

		//listen for the value of the resource import changing
		resourceImportControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<ValueModel<ResourceImport>, ResourceImport>()
				{
					public void propertyChange(GuisePropertyChangeEvent<ValueModel<ResourceImport>, ResourceImport> propertyChangeEvent)
					{
						try
						{
							final ResourceImport resourceImport=propertyChangeEvent.getNewValue();	//get the new resource import
							final String searchResults;	//we'll store the search results here
							if(resourceImport!=null && searchRegExControl.getModel().isValid())	//if we have a new resource to import and the search expression is valid
							{
								final Reader reader=new InputStreamReader(resourceImport.getInputStream());	//get an input stream to the resource, assuming the text file uses the default character encoding
								try
								{
									final char[] buffer=new char[1024];	//create a 1K buffer
									final StringBuilder inputStringBuilder=new StringBuilder();	//create a string builder to hold the input
									int readCount;	//keep track of the number of characters read
									while((readCount=reader.read(buffer))>=0)	//keep reading buffers until we reach the end of the reader
									{
										inputStringBuilder.append(buffer, 0, readCount);	//append the read characters to the input string builder
									}
									final StringBuilder outputStringBuilder=new StringBuilder();	//create a string builder to hold the matches
									final String regularExpression=searchRegExControl.getModel().getValue();	//get the entered regular expression (which we already validated in the validator)
										//the regular expression may still be null or the empty string (the latter of which is a valid regular expression)
									if(regularExpression!=null && regularExpression.length()>0)	//if there is a regular expression
									{
										final Pattern pattern=Pattern.compile(regularExpression);	//compile the regular expression
										final Matcher matcher=pattern.matcher(inputStringBuilder);	//create a matcher to search the input
										while(matcher.find())	//while we find more matches
										{
											outputStringBuilder.append(matcher.group()).append(' ').append('(').append(matcher.start()).append(')').append('\n');	//append the match on a line by itself
										}
										searchResults=outputStringBuilder.toString();	//use the string we constructed to store in the text area
									}
									else	//if there was no regular expression
									{
										searchResults=inputStringBuilder.toString();	//use the input string unmodified										
									}
								}
								finally
								{
									reader.close();	//always close the reader to the resource
								}
							}
							else	//if we don't have a new resource import
							{
								searchResults=null;	//show that we don't have any search results
							}
							try
							{
								textAreaControl.getModel().setValue(searchResults);	//show the search results in the text area
							}
							catch(final ValidationException validationException)	//we don't have a text area validator installed, so we never expect to get validation errors
							{
								throw new AssertionError(validationException);
							}
						}
						catch(final IOException ioException)	//if there is an I/O error
						{
							resourceImportControl.addError(ioException);	//add it to the resource import control for display to the user
						}
					}
				});

		add(inputPanel);	//add the input panel to the panel
		add(textAreaControl);	//add the text area control to the panel
	}

	/**A pattern validator that can validate whether a regular expression has valid syntax, allowing <code>null</code> values.
	@author Garret Wilson
	*/
	public static class PatternSyntaxValidator extends AbstractValidator<String>
	{
		/**Session constructor that allows <code>null</code> values.
		@param session The Guise session that owns this validator.
		*/
		public PatternSyntaxValidator(final GuiseSession session)
		{
			super(session, false);	//don't require non-null values
		}

		/**Determines whether a given regular expression value is syntactically correct.
		@param value The value to validate.
		@return <code>true</code> if the string is a valid regular expression, else <code>false</code>.
		*/
		public boolean isValid(final String value)
		{
			if(!super.isValid(value))	//if this value doesn't pass the default checks (including checking for null if need be)
			{
				return false;	//report that the value isn't valid				
			}
			if(value!=null)	//if a non-null value is given
			{
				try
				{
					Pattern.compile(value);	//try to compile the regular expression
				}
				catch(final PatternSyntaxException patternSyntaxException)	//if there was a syntax error
				{
					return false;	//the value is invalid
				}
			}
			return true;	//indicate that the regular expression is either null or syntactically correct
		}

	}
}
