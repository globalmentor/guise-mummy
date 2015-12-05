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
import java.util.Date;
import java.util.Locale;

import com.globalmentor.beans.AbstractGenericPropertyChangeListener;
import com.globalmentor.beans.GenericPropertyChangeEvent;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.model.*;
import com.guiseframework.validator.*;

/**
 * Calendars Guise demonstration panel. Copyright © 2005 GlobalMentor, Inc. Demonstrates calendar month table models, calendar controls, calendar dialog frames,
 * and text controls with date value models.
 * @author Garret Wilson
 */
public class CalendarsPanel extends LayoutPanel {

	/** Default constructor. */
	public CalendarsPanel() {
		super(new RegionLayout()); //construct the parent class, using a region layout
		setLabel("Guise\u2122 Demonstration: Calendars"); //set the panel title

		//center panel
		final LayoutPanel centerPanel = new LayoutPanel(new FlowLayout(Flow.PAGE)); //create the center panel flowing vertically
		//CalendarMonthTableModel demonstration
		final GroupPanel calendarMonthTableModelPanel = new GroupPanel(new FlowLayout(Flow.PAGE)); //create a group panel flowing vertically
		calendarMonthTableModelPanel.setLabel("Calendar Month Table Model");
		final Table calendarMonthTable = new Table(new CalendarMonthTableModel()); //create a normal table with a calendar month table model
		calendarMonthTable.setLabel("Normal Table using Default CalendarMonthTableModel");
		calendarMonthTableModelPanel.add(calendarMonthTable);
		centerPanel.add(calendarMonthTableModelPanel);

		add(centerPanel, new RegionConstraints(Region.CENTER)); //add the center panel in the center

		//side panel
		final LayoutPanel sidePanel = new LayoutPanel(new FlowLayout(Flow.PAGE)); //create the side panel flowing vertically
		//locale panel
		final GroupPanel localePanel = new GroupPanel(new FlowLayout(Flow.PAGE)); //create a group panel flowing vertically
		localePanel.setLabel("Specify Session Locale");
		final ListControl<Locale> localeListControl = new ListControl<Locale>(Locale.class, new SingleListSelectionPolicy<Locale>()); //create a list control allowing only single selections of locales
		localeListControl.setLabel("Locale"); //set the list control label
		localeListControl.setValidator(new ValueRequiredValidator<Locale>()); //require a locale to be selected in the list control
		localeListControl.setRowCount(1); //make this a drop-down list
		localeListControl.add(getSession().getLocale()); //add the current locale
		localeListControl.add(Locale.FRANCE);
		localeListControl.add(Locale.CHINA);
		localeListControl.add(new Locale("ar"));
		localeListControl.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Locale>() { //listen for selection changes

					public void propertyChange(final GenericPropertyChangeEvent<Locale> propertyChangeEvent) { //if the locale selection changes
						final Locale newLocale = propertyChangeEvent.getNewValue(); //get the new locale selected
						if(newLocale != null) { //if a new locale was selected
							getSession().setLocale(newLocale); //change to the session locale							
						}
					}
				});
		try {
			localeListControl.setSelectedValues(getSession().getLocale()); //show the session locale selected
		} catch(final PropertyVetoException propertyVetoException) { //if the change was vetoed, ignore the exception
		}
		localePanel.add(localeListControl);
		sidePanel.add(localePanel);
		//CalendarControl demonstration
		final GroupPanel calendarControlPanel = new GroupPanel(new FlowLayout(Flow.PAGE)); //create a group panel flowing vertically
		calendarControlPanel.setLabel("Calendar Control");
		final CalendarControl calendarControl = new CalendarControl(); //create a default calendar control
		calendarControlPanel.add(calendarControl);
		final TextControl<Date> embeddedDateTextControl = new TextControl<Date>(Date.class); //create a text control to display the date
		embeddedDateTextControl.setLabel("Selected Date:");
		embeddedDateTextControl.setEditable(false);
		calendarControlPanel.add(embeddedDateTextControl);
		calendarControl.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Date>() { //listen for the calendar control value changing

					public void propertyChange(final GenericPropertyChangeEvent<Date> propertyChangeEvent) { //if the calendar control value changed
						final Date newDate = propertyChangeEvent.getNewValue(); //get the new date
						if(newDate != null) { //if a new date was selected
							try {
								embeddedDateTextControl.setValue(newDate); //show the date in the text control
							} catch(final PropertyVetoException propertyVetoException) { //if the change was vetoed, ignore the exception
							}
						}
					}
				});
		sidePanel.add(calendarControlPanel);
		//Popup CalendarControl demonstration
		final GroupPanel popupCalendarControlPanel = new GroupPanel(new FlowLayout(Flow.PAGE)); //create a group panel flowing horizontally
		popupCalendarControlPanel.setLabel("Popup Calendar Control");
		final Button calendarButton = new Button(); //create a button
		calendarButton.setLabel("Select Date"); //set the button label
		popupCalendarControlPanel.add(calendarButton);
		final TextControl<Date> popupDateTextControl = new TextControl<Date>(Date.class); //create a text control to display the date
		popupDateTextControl.setLabel("Selected Date:");
		popupDateTextControl.setEditable(false);
		popupCalendarControlPanel.add(popupDateTextControl);
		calendarButton.addActionListener(new ActionListener() { //listen for the calendar button being pressed

					public void actionPerformed(final ActionEvent actionEvent) { //if the calendar button is pressed
						final CalendarDialogFrame calendarDialogFrame = new CalendarDialogFrame(); //create a new calendar popup
						calendarDialogFrame.setLabel("Select a date");
						calendarDialogFrame.setRelatedComponent(calendarButton); //associate the popup with the button
						calendarDialogFrame.open(); //show the calendar popup
						calendarDialogFrame.open(new AbstractGenericPropertyChangeListener<Frame.Mode>() { //ask for the date to be selected		

									public void propertyChange(final GenericPropertyChangeEvent<Frame.Mode> propertyChangeEvent) { //when the modal dialog mode changes
										final Date newDate = calendarDialogFrame.getValue(); //get the value of the frame's model
										if(newDate != null) { //if a new date was selected (i.e. the calendar dialog frame was not closed without a selection)
											try {
												popupDateTextControl.setValue(newDate); //show the date in the text control
											} catch(final PropertyVetoException propertyVetoException) { //if the change was vetoed, ignore the exception
											}
										}
									}
								});
					}
				});
		sidePanel.add(popupCalendarControlPanel);

		add(sidePanel, new RegionConstraints(Region.LINE_END)); //add the side panel on the right
	}

}
