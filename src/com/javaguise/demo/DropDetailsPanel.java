package com.javaguise.demo;

import java.net.URI;

import javax.mail.internet.ContentType;

import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.component.transfer.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.ValidationException;

/**Drop Details Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates images, drag enabling, drop enabling,
	default drop support, and custom import strategies. 
@author Garret Wilson
*/
public class DropDetailsPanel extends DefaultNavigationPanel
{

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public DropDetailsPanel(final GuiseSession session)
	{
		super(session, new FlowLayout(session, Flow.PAGE));	//construct the parent class flowing vertically
		getModel().setLabel("Guise\u2122 Demonstration: Drop Details");	//set the panel title

			//flag panel
		final GroupPanel flagPanel=new GroupPanel(session, new FlowLayout(session, Flow.LINE));	//create the flag panel flowing horizontally
		flagPanel.getModel().setLabel("Drag a Flag");	//set the flag panel label				
		final Image usFlag=new Image(session);	//US flag
		usFlag.getModel().setImage(URI.create("http://www.cia.gov/cia/publications/factbook/flags/us-flag.gif"));	//set the URI
		usFlag.getModel().setLabel("USA");	//set the label
		usFlag.setDragEnabled(true);	//enable dragging for the image
		flagPanel.add(usFlag);	//add the image to the flag panel
		final Image frFlag=new Image(session);	//France flag
		frFlag.getModel().setImage(URI.create("http://www.cia.gov/cia/publications/factbook/flags/fr-flag.gif"));	//set the URI
		frFlag.getModel().setLabel("France");	//set the label
		frFlag.setDragEnabled(true);	//enable dragging for the image
		flagPanel.add(frFlag);	//add the image to the flag panel
		final Image inFlag=new Image(session);	//India flag
		inFlag.getModel().setImage(URI.create("http://www.cia.gov/cia/publications/factbook/flags/in-flag.gif"));	//set the URI
		inFlag.getModel().setLabel("India");	//set the label
		inFlag.setDragEnabled(true);	//enable dragging for the image
		flagPanel.add(inFlag);	//add the image to the flag panel
		final Image irFlag=new Image(session);	//Iran flag
		irFlag.getModel().setImage(URI.create("http://www.cia.gov/cia/publications/factbook/flags/ir-flag.gif"));	//set the URI
		irFlag.getModel().setLabel("Iran");	//set the label
		irFlag.setDragEnabled(true);	//enable dragging for the image
		flagPanel.add(irFlag);	//add the image to the flag panel
		add(flagPanel);	//add the flag panel to the panel

			//label panel
		final GroupPanel labelPanel=new GroupPanel(session, new FlowLayout(session, Flow.LINE));	//create the label panel flowing horizontally
		labelPanel.getModel().setLabel("Drag a Label");	//set the label panel label
		final String[] partsOfSpeech=new String[]{"Noun", "Verb", "Ajective", "Adverb", "Pronoun", "Preposition", "Conjunction", "Interjection"};
		for(final String partOfSpeech:partsOfSpeech)	//for each label string
		{
			final Label label=new Label(session);	//create a label label
			label.getModel().setLabel(partOfSpeech);	//set the label text
			label.setDragEnabled(true);	//enable dragging for the label
			labelPanel.add(label);	//add the label to the label panel			
		}
		add(labelPanel);	//add the label panel to the panel

			//default text area
		final TextAreaControl defaultTextArea=new TextAreaControl(session, 5, 80);	//create a text area control
		defaultTextArea.getModel().setLabel("Drop Here for Built-In Drop Functionality");	//set the label of the text area
		defaultTextArea.getModel().setEditable(false);	//don't allow the text area control to be edited
		defaultTextArea.setDropEnabled(true);	//allow dropping on the text area
		add(defaultTextArea);	//add the default drop text area control to the panel

			//details text area
		final TextAreaControl detailsTextArea=new TextAreaControl(session, 15, 80);	//create a text area control
		detailsTextArea.getModel().setLabel("Drop Here for Drop Details");	//set the label of the text area
		detailsTextArea.getModel().setEditable(false);	//don't allow the text area control to be edited
		detailsTextArea.setDropEnabled(true);	//allow dropping on the text area
		detailsTextArea.addImportStrategy(new ImportStrategy<TextAreaControl>()	//add a new import strategy for this component
				{		
					public boolean canImportTransfer(final TextAreaControl component, final Transferable<Component<?>> transferable)
					{
						return true;	//accept all import types
					}
					public boolean importTransfer(final TextAreaControl component, final Transferable<Component<?>> transferable)
					{
						final String oldContent=component.getModel().getValue();	//get the old text area control content
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
							component.getModel().setValue(newContent.toString());	//update the text area contents
						}
						catch(final ValidationException validationException)	//we don't have a validator installed, so we don't expect validation exceptions
						{
							throw new AssertionError(validationException);
						}
						return true;	//indicate that we imported the information
					}
				});
		add(detailsTextArea);	//add the drop details text area control to the panel
	}

}
