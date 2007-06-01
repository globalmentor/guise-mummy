package com.guiseframework.demo;

import java.beans.PropertyVetoException;
import java.net.URI;

import javax.mail.internet.ContentType;

import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.component.transfer.*;

/**Drop Details Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates images, drag enabling, drop enabling,
	default drop support, and custom import strategies. 
@author Garret Wilson
*/
public class DropDetailsPanel extends DefaultNavigationPanel
{

	/**Default constructor.*/
	public DropDetailsPanel()
	{
		super(new FlowLayout(Flow.PAGE));	//construct the parent class flowing vertically
		setLabel("Guise\u2122 Demonstration: Drop Details");	//set the panel title

			//flag panel
		final GroupPanel flagPanel=new GroupPanel(new FlowLayout(Flow.LINE));	//create the flag panel flowing horizontally
		flagPanel.setLabel("Drag a Flag");	//set the flag panel label				
		final Picture usFlag=new Picture();	//US flag
		usFlag.setImage(URI.create("http://www.cia.gov/cia/publications/factbook/flags/us-flag.gif"));	//set the URI
		usFlag.setLabel("USA");	//set the label
		usFlag.setDragEnabled(true);	//enable dragging for the image
		flagPanel.add(usFlag);	//add the image to the flag panel
		final Picture frFlag=new Picture();	//France flag
		frFlag.setImage(URI.create("http://www.cia.gov/cia/publications/factbook/flags/fr-flag.gif"));	//set the URI
		frFlag.setLabel("France");	//set the label
		frFlag.setDragEnabled(true);	//enable dragging for the image
		flagPanel.add(frFlag);	//add the image to the flag panel
		final Picture inFlag=new Picture();	//India flag
		inFlag.setImage(URI.create("http://www.cia.gov/cia/publications/factbook/flags/in-flag.gif"));	//set the URI
		inFlag.setLabel("India");	//set the label
		inFlag.setDragEnabled(true);	//enable dragging for the image
		flagPanel.add(inFlag);	//add the image to the flag panel
		final Picture irFlag=new Picture();	//Iran flag
		irFlag.setImage(URI.create("http://www.cia.gov/cia/publications/factbook/flags/ir-flag.gif"));	//set the URI
		irFlag.setLabel("Iran");	//set the label
		irFlag.setDragEnabled(true);	//enable dragging for the image
		flagPanel.add(irFlag);	//add the image to the flag panel
		add(flagPanel);	//add the flag panel to the panel

			//label panel
		final GroupPanel labelPanel=new GroupPanel(new FlowLayout(Flow.LINE));	//create the label panel flowing horizontally
		labelPanel.setLabel("Drag a Label");	//set the label panel label
		final String[] partsOfSpeech=new String[]{"Noun", "Verb", "Ajective", "Adverb", "Pronoun", "Preposition", "Conjunction", "Interjection"};
		for(final String partOfSpeech:partsOfSpeech)	//for each label string
		{
			final Label label=new Label();	//create a label label
			label.setLabel(partOfSpeech);	//set the label text
			label.setDragEnabled(true);	//enable dragging for the label
			labelPanel.add(label);	//add the label to the label panel			
		}
		add(labelPanel);	//add the label panel to the panel

			//default text area
		final TextAreaControl defaultTextArea=new TextAreaControl(5, 80);	//create a text area control
		defaultTextArea.setLabel("Drop Here for Built-In Drop Functionality");	//set the label of the text area
		defaultTextArea.setEditable(false);	//don't allow the text area control to be edited
		defaultTextArea.setDropEnabled(true);	//allow dropping on the text area
		add(defaultTextArea);	//add the default drop text area control to the panel

			//details text area
		final TextAreaControl detailsTextArea=new TextAreaControl(15, 80);	//create a text area control
		detailsTextArea.setLabel("Drop Here for Drop Details");	//set the label of the text area
		detailsTextArea.setEditable(false);	//don't allow the text area control to be edited
		detailsTextArea.setDropEnabled(true);	//allow dropping on the text area
		detailsTextArea.addImportStrategy(new ImportStrategy<TextAreaControl>()	//add a new import strategy for this component
				{		
					public boolean canImportTransfer(final TextAreaControl component, final Transferable<?> transferable)
					{
						return true;	//accept all import types
					}
					public boolean importTransfer(final TextAreaControl component, final Transferable<?> transferable)
					{
						final String oldContent=component.getValue();	//get the old text area control content
						final StringBuilder newContent=new StringBuilder();	//create a string builder to collect our new information
						if(oldContent!=null)	//if there is content already
						{
							newContent.append(oldContent);	//add the old content
						}
						newContent.append("Drop Source: ").append(transferable.getSource().getClass().getName()).append('\n');
						for(final ContentType contentType:transferable.getContentTypes())	//for each content type
						{
							newContent.append("* Drop Content Type: ").append(contentType).append('\n');
							newContent.append("  Drop Data: ").append(transferable.transfer(contentType)).append('\n');	//actually transfer the data
						}
						newContent.append('\n');
						try
						{
							component.setValue(newContent.toString());	//update the text area contents
						}
						catch(final PropertyVetoException propertyVetoException)	//if the change was vetoed, ignore the exception
						{
						}
						return true;	//indicate that we imported the information
					}
				});
		add(detailsTextArea);	//add the drop details text area control to the panel
	}

}
