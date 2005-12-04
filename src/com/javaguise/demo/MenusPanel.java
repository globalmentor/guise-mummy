package com.javaguise.demo;

import com.javaguise.GuiseSession;
import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.event.*;

import com.javaguise.model.*;
import com.javaguise.validator.ValidationException;

/**Menus Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates drop menus, accordion menus, menu rollovers, and menu actions. 
@author Garret Wilson
*/
public class MenusPanel extends DefaultNavigationPanel
{

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public MenusPanel(final GuiseSession session)
	{
		super(session, new RegionLayout(session));	//construct the parent class, using a region layout
		getModel().setLabel("Guise\u2122 Demonstration: Menus");	//set the panel title
			//drop-down menu
		final DropMenu dropMenu=new DropMenu(session, Flow.LINE);	//create a drop menu flowing along the line
			//file menu
		final DropMenu fileMenu=new DropMenu(session, Flow.PAGE);	//the submenu flows along the page
		fileMenu.getModel().setLabel("File");
		final Link openMenuLink=new Link(session);	//file|open link
		openMenuLink.getModel().setLabel("Open");
		fileMenu.add(openMenuLink);
		final Link closeMenuLink=new Link(session);	//the submenu flows along the page
		closeMenuLink.getModel().setLabel("Close");	//file|close link
		fileMenu.add(closeMenuLink);
		dropMenu.add(fileMenu);
			//edit menu
		final DropMenu editMenu=new DropMenu(session, Flow.PAGE);	//the submenu flows along the page
		editMenu.getModel().setLabel("Edit");
		final Link copyMenuLink=new Link(session);	//edit|copy link
		copyMenuLink.getModel().setLabel("Copy");
		editMenu.add(copyMenuLink);
		final Link cutMenuLink=new Link(session);	//edit|cut link
		cutMenuLink.getModel().setLabel("Cut");
		editMenu.add(cutMenuLink);
		final Link pasteMenuLink=new Link(session);	//edit|paste link
		pasteMenuLink.getModel().setLabel("Paste");
		editMenu.add(pasteMenuLink);
		dropMenu.add(editMenu);
			//window menu
		final DropMenu windowMenu=new DropMenu(session, Flow.PAGE);	//the submenu flows along the page
		windowMenu.getModel().setLabel("Window");
			//window|arrange menu
		final DropMenu arrangeMenu=new DropMenu(session, Flow.PAGE);	//the sub-submenu flows along the page
		arrangeMenu.getModel().setLabel("Arrange");
		final Link tileMenuLink=new Link(session);	//window|arrange|tile link
		tileMenuLink.getModel().setLabel("Tile");
		arrangeMenu.add(tileMenuLink);
		final Link cascadeMenuLink=new Link(session);	//window|arrange|cascade
		cascadeMenuLink.getModel().setLabel("Cascade");
		arrangeMenu.add(cascadeMenuLink);
		windowMenu.add(arrangeMenu);
		dropMenu.add(windowMenu);		
		add(dropMenu, RegionLayout.PAGE_START_CONSTRAINTS);	//add the drop-down menu at the top

			//accordion menu
		final AccordionMenu accordionMenu=new AccordionMenu(session, Flow.PAGE);	//create an accordion menu flowing along the page
			//continents menu
		final AccordionMenu continentsMenu=new AccordionMenu(session, Flow.PAGE);	//the accordion submenu also flows along the page
		continentsMenu.getModel().setLabel("Continents");
		final String[] continents=new String[]{"Africa", "Asia", "Antarctica", "Australia", "Europe", "North America", "South America"};
		for(final String continent:continents)	//for each continent
		{
			final Link continentLink=new Link(session);	//continents:continent link
			continentLink.getModel().setLabel(continent);
			continentsMenu.add(continentLink);			
		}
		accordionMenu.add(continentsMenu);
			//text menu
		final AccordionMenu messageMenu=new AccordionMenu(session, Flow.PAGE);	//the accordion submenu also flows along the page
		messageMenu.getModel().setLabel("Message");
		final Message message=new Message(session, new DefaultMessageModel(session));
		message.getModel().setMessage("Because geography is defined by local convention, there are several conceptions as to which landmasses qualify as continents. "+
				"There are names for six, but America is often divided, and Europe is often united with Asia. "+
				"Ignoring cases where Antarctica is omitted, there are half a dozen lists. "+
				"(Wikipedia)");
		messageMenu.add(message);
		accordionMenu.add(messageMenu);
		add(accordionMenu, RegionLayout.LINE_START_CONSTRAINTS);	//add the accordion menu at the side

			//center panel
		final LayoutPanel centerPanel=new LayoutPanel(session, new FlowLayout(session, Flow.PAGE)); //create the center panel flowing vertically
			//accordion menu settings
		final GroupPanel accordionMenuPanel=new GroupPanel(session, new FlowLayout(session, Flow.PAGE));	//create a group panel flowing vertically
		accordionMenuPanel.getModel().setLabel("Accordion Menu");
		final CheckControl rolloverOpenCheckbox=new CheckControl(session);	//accordion rollover open enabled
		rolloverOpenCheckbox.getModel().setLabel("Enable accordion menu rollover open.");
		rolloverOpenCheckbox.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<ValueModel<Boolean>, Boolean>()
				{
					public void propertyChange(final GuisePropertyChangeEvent<ValueModel<Boolean>, Boolean> propertyChangeEvent)
					{
						continentsMenu.setRolloverOpenEnabled(propertyChangeEvent.getNewValue().booleanValue());	//update the accordion submenu rollover open enabled option
						messageMenu.setRolloverOpenEnabled(propertyChangeEvent.getNewValue().booleanValue());	//update the accordion submenu rollover open enabled option
					}
				});
		accordionMenuPanel.add(rolloverOpenCheckbox);
				//create a text input control to indicate the number of continents clicks, and set its model's default to zero
		final TextControl<Integer> continentsClickCountControl=new TextControl<Integer>(session, new DefaultValueModel<Integer>(session, Integer.class, new Integer(0)));
		continentsClickCountControl.getModel().setLabel("Number of clicks on the \"Continents\" accordion menu.");	//add a label to the input control
		continentsClickCountControl.getModel().setEditable(false);	//don't allow the control to be edited
		accordionMenuPanel.add(continentsClickCountControl);		
		centerPanel.add(accordionMenuPanel);
		
		continentsMenu.getModel().addActionListener(new ActionListener<ActionModel>()	//listen for accordion menu action
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)
					{
						final int oldClickCount=continentsClickCountControl.getModel().getValue().intValue();	//get the old number of clicks
						try
						{
							continentsClickCountControl.getModel().setValue(new Integer(oldClickCount+1));	//update the number of clicks
						}
						catch(final ValidationException validationException)	//we don't have validators installed, so we don't expect validation problems
						{
							throw new AssertionError(validationException);
						}
					}
				});

		add(centerPanel, RegionLayout.CENTER_CONSTRAINTS);	//add the center panel in the center
	}

}
