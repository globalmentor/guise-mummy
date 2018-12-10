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

import com.globalmentor.beans.AbstractGenericPropertyChangeListener;
import com.globalmentor.beans.GenericPropertyChangeEvent;

import io.guise.framework.component.*;
import io.guise.framework.component.layout.*;
import io.guise.framework.event.*;
import io.guise.framework.model.*;

/**
 * Menus Guise demonstration panel. Copyright © 2005 GlobalMentor, Inc. Demonstrates drop menus, accordion menus, menu rollovers, and menu actions.
 * @author Garret Wilson
 */
public class MenusPanel extends LayoutPanel {

	/** Default constructor. */
	public MenusPanel() {
		super(new RegionLayout()); //construct the parent class, using a region layout
		setLabel("Guise\u2122 Demonstration: Menus"); //set the panel title
		//drop-down menu
		final DropMenu dropMenu = new DropMenu(Flow.LINE); //create a drop menu flowing along the line
		//file menu
		final DropMenu fileMenu = new DropMenu(Flow.PAGE); //the submenu flows along the page
		fileMenu.setLabel("File");
		final Link openMenuLink = new Link(); //file|open link
		openMenuLink.setLabel("Open");
		fileMenu.add(openMenuLink);
		final Link closeMenuLink = new Link(); //the submenu flows along the page
		closeMenuLink.setLabel("Close"); //file|close link
		fileMenu.add(closeMenuLink);
		dropMenu.add(fileMenu);
		//edit menu
		final DropMenu editMenu = new DropMenu(Flow.PAGE); //the submenu flows along the page
		editMenu.setLabel("Edit");
		final Link copyMenuLink = new Link(); //edit|copy link
		copyMenuLink.setLabel("Copy");
		editMenu.add(copyMenuLink);
		final Link cutMenuLink = new Link(); //edit|cut link
		cutMenuLink.setLabel("Cut");
		editMenu.add(cutMenuLink);
		final Link pasteMenuLink = new Link(); //edit|paste link
		pasteMenuLink.setLabel("Paste");
		editMenu.add(pasteMenuLink);
		dropMenu.add(editMenu);
		//window menu
		final DropMenu windowMenu = new DropMenu(Flow.PAGE); //the submenu flows along the page
		windowMenu.setLabel("Window");
		//window|arrange menu
		final DropMenu arrangeMenu = new DropMenu(Flow.PAGE); //the sub-submenu flows along the page
		arrangeMenu.setLabel("Arrange");
		final Link tileMenuLink = new Link(); //window|arrange|tile link
		tileMenuLink.setLabel("Tile");
		arrangeMenu.add(tileMenuLink);
		final Link cascadeMenuLink = new Link(); //window|arrange|cascade
		cascadeMenuLink.setLabel("Cascade");
		arrangeMenu.add(cascadeMenuLink);
		windowMenu.add(arrangeMenu);
		dropMenu.add(windowMenu);
		add(dropMenu, new RegionConstraints(Region.PAGE_START)); //add the drop-down menu at the top

		//accordion menu
		final AccordionMenu accordionMenu = new AccordionMenu(Flow.PAGE); //create an accordion menu flowing along the page
		//continents menu
		final AccordionMenu continentsMenu = new AccordionMenu(Flow.PAGE); //the accordion submenu also flows along the page
		continentsMenu.setLabel("Continents");
		final String[] continents = new String[] {"Africa", "Asia", "Antarctica", "Australia", "Europe", "North America", "South America"};
		for(final String continent : continents) { //for each continent
			final Link continentLink = new Link(); //continents:continent link
			continentLink.setLabel(continent);
			continentsMenu.add(continentLink);
		}
		accordionMenu.add(continentsMenu);
		//text menu
		final AccordionMenu messageMenu = new AccordionMenu(Flow.PAGE); //the accordion submenu also flows along the page
		messageMenu.setLabel("Message");
		final Message message = new Message();
		message.setMessage("Because geography is defined by local convention, there are several conceptions as to which landmasses qualify as continents. "
				+ "There are names for six, but America is often divided, and Europe is often united with Asia. "
				+ "Ignoring cases where Antarctica is omitted, there are half a dozen lists. " + "(Wikipedia)");
		messageMenu.add(message);
		accordionMenu.add(messageMenu);
		add(accordionMenu, new RegionConstraints(Region.LINE_START)); //add the accordion menu at the side

		//center panel
		final LayoutPanel centerPanel = new LayoutPanel(new FlowLayout(Flow.PAGE)); //create the center panel flowing vertically
		//accordion menu settings
		final GroupPanel accordionMenuPanel = new GroupPanel(new FlowLayout(Flow.PAGE)); //create a group panel flowing vertically
		accordionMenuPanel.setLabel("Accordion Menu");
		final CheckControl rolloverOpenCheckbox = new CheckControl(); //accordion rollover open enabled
		rolloverOpenCheckbox.setLabel("Enable accordion menu rollover open.");
		rolloverOpenCheckbox.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Boolean>() {

			public void propertyChange(final GenericPropertyChangeEvent<Boolean> propertyChangeEvent) {
				continentsMenu.setRolloverOpenEnabled(propertyChangeEvent.getNewValue().booleanValue()); //update the accordion submenu rollover open enabled option
				messageMenu.setRolloverOpenEnabled(propertyChangeEvent.getNewValue().booleanValue()); //update the accordion submenu rollover open enabled option
			}
		});
		accordionMenuPanel.add(rolloverOpenCheckbox);
		//create a text input control to indicate the number of continents clicks, and set its model's default to zero
		final TextControl<Integer> continentsClickCountControl = new TextControl<Integer>(new DefaultValueModel<Integer>(Integer.class, new Integer(0)));
		continentsClickCountControl.setLabel("Number of clicks on the \"Continents\" accordion menu."); //add a label to the input control
		continentsClickCountControl.setEditable(false); //don't allow the control to be edited
		accordionMenuPanel.add(continentsClickCountControl);
		centerPanel.add(accordionMenuPanel);

		continentsMenu.addActionListener(new ActionListener() { //listen for accordion menu action

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				final int oldClickCount = continentsClickCountControl.getValue().intValue(); //get the old number of clicks
				try {
					continentsClickCountControl.setValue(new Integer(oldClickCount + 1)); //update the number of clicks
				} catch(final PropertyVetoException propertyVetoException) { //if the change was vetoed, ignore the exception
				}
			}

		});

		add(centerPanel, new RegionConstraints(Region.CENTER)); //add the center panel in the center
	}

}
