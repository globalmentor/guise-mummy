package com.javaguise.demo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.mail.internet.ContentType;

import com.garretwilson.beans.AbstractPropertyValueChangeListener;
import com.garretwilson.beans.PropertyValueChangeEvent;
import com.garretwilson.util.Debug;
import com.javaguise.component.Button;
import com.javaguise.component.CheckControl;
import com.javaguise.component.DefaultFrame;
import com.javaguise.component.GroupPanel;
import com.javaguise.component.Image;
import com.javaguise.component.Label;
import com.javaguise.component.LayoutPanel;
import com.javaguise.component.Message;
import com.javaguise.component.ResourceImportControl;
import com.javaguise.component.TextAreaControl;
import com.javaguise.component.TextControl;
import com.javaguise.component.layout.FlowLayout;
import com.javaguise.component.layout.Orientation;
import com.javaguise.component.transfer.ImportStrategy;
import com.javaguise.component.transfer.Transferable;
import com.javaguise.model.DefaultLabelModel;
import com.javaguise.model.MessageModel;
import com.javaguise.model.ModelGroup;
import com.javaguise.model.MutualExclusionPolicyModelGroup;
import com.javaguise.model.ResourceImport;
import com.javaguise.model.ValueModel;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.AbstractValidator;
import com.javaguise.validator.ResourceImportValidator;
import com.javaguise.validator.ValidationException;

/**Drop Details Guise demonstration frame.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates drag enabling, 


This demonstration assumes text files are encoded using the system default character encoding.
@author Garret Wilson
*/
public class DropDetailsFrame extends DefaultFrame
{

	/**Guise session constructor.
	@param session The Guise session that owns this frame.
	*/
	public DropDetailsFrame(final GuiseSession<?> session)
	{
		super(session, new FlowLayout(Orientation.Flow.PAGE));	//construct the parent class flowing vertically
		getModel().setLabel("Guise\u2122 Demonstration: Drop Details");	//set the frame title

			//flag panel
		final GroupPanel flagPanel=new GroupPanel(session, new FlowLayout(Orientation.Flow.LINE));	//create the flag panel flowing horizontally
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
		add(flagPanel);	//add the flag panel to the frame

			//label panel
		final GroupPanel labelPanel=new GroupPanel(session, new FlowLayout(Orientation.Flow.LINE));	//create the label panel flowing horizontally
		labelPanel.getModel().setLabel("Drag a Label");	//set the label panel label
		final Label nounLabel=new Label(session);	//noun label
		nounLabel.getModel().setLabel("Noun");	//set the label
		nounLabel.setDragEnabled(true);	//enable dragging for the label
		labelPanel.add(nounLabel);	//add the label to the label panel
		final Label verbLabel=new Label(session);	//verb label
		verbLabel.getModel().setLabel("Verb");	//set the label
		verbLabel.setDragEnabled(true);	//enable dragging for the label
		labelPanel.add(verbLabel);	//add the label to the label panel
		add(labelPanel);	//add the label panel to the frame

			//details text area
		final TextAreaControl detailsTextArea=new TextAreaControl(session, 20, 80);	//create a text area control
		detailsTextArea.getModel().setLabel("Drop Here for Details");	//set the label of the text area
		detailsTextArea.getModel().setEditable(false);	//don't allow the text area control to be edited
		detailsTextArea.setDropEnabled(true);	//allow dropping on the text area
		detailsTextArea.addImportStrategy(new ImportStrategy<TextAreaControl>()	//add a new import strategy for this component
				{		
					public boolean canImportTransfer(final TextAreaControl component, final Transferable transferable)
					{
						return true;	//accept all import types
					}

					public boolean importTransfer(final TextAreaControl component, final Transferable transferable)
					{
						Debug.trace("we're ready to transfer!");
						return true;
					}
				});
		add(detailsTextArea);	//add the text area control to the frame
	}

}
