package com.javaguise.demo;

import java.util.Date;
import java.util.Locale;

import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.event.*;

import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.*;

/**Calendars Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates calendar month table models, calendar controls,
	calendar dialog frames, and text controls with date value models. 
@author Garret Wilson
*/
public class CalendarsPanel extends DefaultNavigationPanel
{

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public CalendarsPanel(final GuiseSession session)
	{
		super(session, new RegionLayout(session));	//construct the parent class, using a region layout
		getModel().setLabel("Guise\u2122 Demonstration: Calendars");	//set the panel title

			//center panel
		final LayoutPanel centerPanel=new LayoutPanel(session, new FlowLayout(session, Flow.PAGE)); //create the center panel flowing vertically
				//CalendarMonthTableModel demonstration
		final GroupPanel calendarMonthTableModelPanel=new GroupPanel(session, new FlowLayout(session, Flow.PAGE));	//create a group panel flowing vertically
		calendarMonthTableModelPanel.getModel().setLabel("Calendar Month Table Model");
		final Table calendarMonthTable=new Table(session, new CalendarMonthTableModel(session));	//create a normal table with a calendar month table model
		calendarMonthTable.getModel().setLabel("Normal Table using Default CalendarMonthTableModel");
		calendarMonthTableModelPanel.add(calendarMonthTable);
		centerPanel.add(calendarMonthTableModelPanel);
		
		add(centerPanel, RegionLayout.CENTER_CONSTRAINTS);	//add the center panel in the center
		
			//side panel
		final LayoutPanel sidePanel=new LayoutPanel(session, new FlowLayout(session, Flow.PAGE)); //create the side panel flowing vertically
				//locale panel
		final GroupPanel localePanel=new GroupPanel(session, new FlowLayout(session, Flow.PAGE));	//create a group panel flowing vertically
		localePanel.getModel().setLabel("Specify Session Locale");
		final ListControl<Locale> localeListControl=new ListControl<Locale>(session, Locale.class, new SingleListSelectionPolicy<Locale>());	//create a list control allowing only single selections of locales
		localeListControl.getModel().setLabel("Locale");	//set the list control label
		localeListControl.setValueRepresentationStrategy(new AbstractListSelectControl.LocaleRepresentationStrategy(session));	//represent the locales in the correct locale
		localeListControl.getModel().setValidator(new ValueRequiredValidator<Locale>(session));	//require a locale to be selected in the list control
		localeListControl.setRowCount(1);	//make this a drop-down list
		localeListControl.getModel().add(session.getLocale());	//add the current locale
		localeListControl.getModel().add(Locale.FRANCE);
		localeListControl.getModel().add(Locale.CHINA);
		localeListControl.getModel().add(new Locale("ar"));
		localeListControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<ListSelectModel<Locale>, Locale>()	//listen for selection changes
				{
					public void propertyChange(final GuisePropertyChangeEvent<ListSelectModel<Locale>, Locale> propertyChangeEvent)	//if the locale selection changes
					{
						final Locale newLocale=propertyChangeEvent.getNewValue();	//get the new locale selected
						if(newLocale!=null)	//if a new locale was selected
						{
							session.setLocale(newLocale);	//change to the session locale							
						}
					}
				});
		try
		{
			localeListControl.getModel().setSelectedValues(session.getLocale());	//show the session locale selected
		}
		catch(final ValidationException validationException)	//any of the values can be selected, so we don't expect any errors
		{
			throw new AssertionError(validationException);
		}
		localePanel.add(localeListControl);
		sidePanel.add(localePanel);
				//CalendarControl demonstration
		final GroupPanel calendarControlPanel=new GroupPanel(session, new FlowLayout(session, Flow.PAGE));	//create a group panel flowing vertically
		calendarControlPanel.getModel().setLabel("Calendar Control");
		final CalendarControl calendarControl=new CalendarControl(session);	//create a default calendar control
		calendarControlPanel.add(calendarControl);
		final TextControl<Date> embeddedDateTextControl=new TextControl<Date>(session, Date.class);	//create a text control to display the date
		embeddedDateTextControl.getModel().setLabel("Selected Date:");
		embeddedDateTextControl.getModel().setEditable(false);
		calendarControlPanel.add(embeddedDateTextControl);
		calendarControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<CalendarMonthTableModel, Date>()	//listen for the calendar control value changing
				{
					public void propertyChange(final GuisePropertyChangeEvent<CalendarMonthTableModel, Date> propertyChangeEvent)	//if the calendar control value changed
					{
						final Date newDate=propertyChangeEvent.getNewValue();	//get the new date
						if(newDate!=null)	//if a new date was selected
						{
							try
							{
								embeddedDateTextControl.getModel().setValue(newDate);	//show the date in the text control
							}
							catch(final ValidationException validationException)	//no text control validator is installed, so there should be no validation errors
							{
								throw new AssertionError(validationException);
							}
						}
					}
				});
		sidePanel.add(calendarControlPanel);
			//Popup CalendarControl demonstration
		final GroupPanel popupCalendarControlPanel=new GroupPanel(session, new FlowLayout(session, Flow.PAGE));	//create a group panel flowing horizontally
		popupCalendarControlPanel.getModel().setLabel("Popup Calendar Control");
		final Button calendarButton=new Button(session);	//create a button
		calendarButton.getModel().setLabel("Select Date");	//set the button label
		popupCalendarControlPanel.add(calendarButton);
		final TextControl<Date> popupDateTextControl=new TextControl<Date>(session, Date.class);	//create a text control to display the date
		popupDateTextControl.getModel().setLabel("Selected Date:");
		popupDateTextControl.getModel().setEditable(false);
		popupCalendarControlPanel.add(popupDateTextControl);
		calendarButton.getModel().addActionListener(new ActionListener<ActionModel>()	//listen for the calendar button being pressed
				{
					public void actionPerformed(final ActionEvent<ActionModel> actionEvent)	//if the calendar button is pressed
					{
						final CalendarDialogFrame calendarDialogFrame=new CalendarDialogFrame(session);	//create a new calendar popup
						calendarDialogFrame.getModel().setLabel("Select a date");
						calendarDialogFrame.setRelatedComponent(calendarButton);	//associate the popup with the button
						calendarDialogFrame.open();	//show the calendar popup
						calendarDialogFrame.open(new AbstractGuisePropertyChangeListener<CalendarDialogFrame, Mode>()	//ask for the date to be selected
								{		
									public void propertyChange(final GuisePropertyChangeEvent<CalendarDialogFrame, Mode> propertyChangeEvent)	//when the modal dialog mode changes
									{
										final Date newDate=propertyChangeEvent.getSource().getModel().getValue();	//get the value of the frame's model
										if(newDate!=null)	//if a new date was selected (i.e. the calendar dialog frame was not closed without a selection)
										{
											try
											{
												popupDateTextControl.getModel().setValue(newDate);	//show the date in the text control
											}
											catch(final ValidationException validationException)	//no text control validator is installed, so there should be no validation errors
											{
												throw new AssertionError(validationException);
											}
										}
									}
								});
					}
				});
		sidePanel.add(popupCalendarControlPanel);

		add(sidePanel, RegionLayout.LINE_END_CONSTRAINTS);	//add the side panel on the right
	}

}
