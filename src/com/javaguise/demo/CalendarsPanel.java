package com.javaguise.demo;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.garretwilson.util.Debug;
import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.event.*;

import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.ValidationException;
import com.javaguise.validator.ValueRequiredValidator;

/**Calendars Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates drop menus, accordion menus, menu rollovers, and menu actions. 
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
			//CalendarControl demonstration
		final GroupPanel calendarControlPanel=new GroupPanel(session, new FlowLayout(session, Flow.PAGE));	//create a group panel flowing vertically
		calendarControlPanel.getModel().setLabel("Calendar Control");
		final CalendarControl calendarControl=new CalendarControl(session);	//create a default calendar control
		calendarControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<CalendarMonthTableModel, Calendar>()	//listen for the calendar control value changing
				{
					public void propertyChange(final GuisePropertyChangeEvent<CalendarMonthTableModel, Calendar> propertyChangeEvent)	//if the calendar control value changed
					{
						final Calendar newCalendarValue=propertyChangeEvent.getNewValue();	//get the new value
						if(newCalendarValue!=null)	//if a new calendar value was selected
						{
							new MessageOptionDialogFrame(session,	//show a message dialog with the new calendar value
									"You selected date: "+
									DateFormat.getDateInstance(DateFormat.FULL, session.getLocale()).format(newCalendarValue.getTime()),
									MessageOptionDialogFrame.Option.OK).open(true);
						}
					}
				});
		calendarControlPanel.add(calendarControl);
		centerPanel.add(calendarControlPanel);
		
		add(centerPanel, RegionLayout.CENTER_CONSTRAINTS);	//add the center panel in the center
		
			//locale panel
		final GroupPanel localePanel=new GroupPanel(session, new FlowLayout(session, Flow.PAGE));	//create a group panel flowing vertically
		localePanel.getModel().setLabel("Specify Session Locale");
		final ListControl<Locale> localeListControl=new ListControl<Locale>(session, Locale.class, new SingleListSelectionPolicy<Locale>());	//create a list control allowing only single selections of locales
		localeListControl.getModel().setLabel("Locale");	//set the list control label
		localeListControl.setValueRepresentationStrategy(new AbstractListSelectControl.LocaleRepresentationStrategy(session));	//represent the locales in the correct locale
		localeListControl.getModel().setValidator(new ValueRequiredValidator<Locale>(session));	//require a locale to be selected in the list control
		localeListControl.setRowCount(1);	//make this a drop-down list
		localeListControl.getModel().add(session.getLocale());	//TODO fix
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
		add(localePanel, RegionLayout.LINE_END_CONSTRAINTS);	//add the locale panel on the right
	}

}
