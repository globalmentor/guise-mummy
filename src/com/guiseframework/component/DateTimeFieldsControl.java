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

package com.guiseframework.component;

import java.beans.PropertyVetoException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import com.globalmentor.beans.*;
import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.model.TimeZones.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.Table.CellRepresentationStrategy;
import com.guiseframework.component.layout.*;
import com.guiseframework.converter.*;
import com.guiseframework.model.*;
import com.guiseframework.validator.*;

/**Control that allows selection of a date and/or a time, providing separate inputs for date/time fields with the option of a calendar popup.
@author Garret Wilson
*/
public class DateTimeFieldsControl extends AbstractLayoutValueControl<Date>	//TODO finish or delete class
{

	/**The visible date bound property.*/
//TODO del	public final static String DATE_PROPERTY=getPropertyName(DateTimeControl.class, "date");

	/**The list control containing the months.*/
	private final ListControl<Date> monthListControl;

		/**@return The list control containing the months.*/
		protected ListControl<Date> getMonthListControl() {return monthListControl;}

	/**The control containing the year; this control can change dynamically based upon the current model range.*/
	private ValueControl<Integer> yearControl=null;

		/**@return The control containing the year.*/
		protected ValueControl<Integer> getYearControl() {return yearControl;}

	/**The control containing the year.*/
//TODO del	private final TextControl<Integer> yearControl;

		/**@return The control containing the year.*/
	//TODO del		protected TextControl<Integer> getYearControl() {return yearControl;}

	/**The control containing the month.*/
	//TODO del	private final TextControl<Integer> monthControl;

		/**@return The control containing the month.*/
	//TODO del		protected TextControl<Integer> getMonthControl() {return monthControl;}

	/**The control containing the day.*/
	private final TextControl<Integer> dayControl;

		/**@return The control containing the day.*/
		protected TextControl<Integer> getDayControl() {return dayControl;}

	/**The control containing the hour.*/
	private final TextControl<Integer> hourControl;

		/**@return The control containing the hour.*/
		protected TextControl<Integer> getHourControl() {return hourControl;}

	/**The control containing the minutes.*/
	private final TextControl<Integer> minuteControl;

		/**@return The control containing the minutes.*/
		protected TextControl<Integer> getMinuteControl() {return minuteControl;}

	/**The control containing the seconds.*/
	private final TextControl<Integer> secondControl;

		/**@return The control containing the day.*/
		protected TextControl<Integer> getSecondControl() {return secondControl;}

	/**The control containing the milliseconds.*/
	private final TextControl<Integer> millisecondControl;

		/**@return The control containing the milliseconds.*/
		protected TextControl<Integer> getMillisecondControl() {return millisecondControl;}

	/**The date being viewed, not necessarily chosen, or <code>null</code> if no date is being viewed.*/
//TODO del	private Date date;

		/**@return The date being viewed, not necessarily chosen, or <code>null</code> if no date is being viewed.*/
//TODO del		public Date getDate() {return date!=null ? (Date)date.clone() : null;}

		/**Sets the date being viewed.
		A copy will be made of the date before it is stored.
		This is a bound property.
		@param newDate The date to be viewed, not necessarily chosen.
		@exception NullPointerException if the given date is <code>null</code>.
		@see #DATE_PROPERTY
		*/
/*TODO del
		public void setDate(final Date newDate)
		{
			if(!date.equals(checkInstance(newDate, "Date cannot be null.")))	//if the value is really changing
			{
				final Date oldDate=date;	//get the old value
				date=(Date)newDate.clone();	//clone the new date and actually change the value
				updateDateControls();	//update the date controls based upon the new value
				firePropertyChange(DATE_PROPERTY, oldDate, newDate);	//indicate that the value changed
			}
		}
*/

	/**The property change listener that updates the visible dates if the year is different than the last one.*/
	protected final GenericPropertyChangeListener<Integer> yearPropertyChangeListener;

	/**Default constructor with a default data model.*/
	public DateTimeFieldsControl()
	{
		this(new DefaultValueModel<Date>(Date.class));	//construct the class with a default value model
	}

	/**The property change listener that updates the date controls when a property changes.*/
	protected final GenericPropertyChangeListener<?> updateDateControlsPropertyChangeListener;

	/**The property change listener that updates the visible dates if the year is different than the last one.*/
//TODO fix or del	protected final GenericPropertyChangeListener<Integer> yearPropertyChangeListener;

	/**Value model constructor.
	@param valueModel The component value model.
	@exception NullPointerException if the given value model is <code>null</code>.
	*/
	public DateTimeFieldsControl(final ValueModel<Date> valueModel)
	{
		super(new FlowLayout(Flow.LINE), valueModel);	//construct the parent class flowing along the line
		final Date date=valueModel.getValue();	//get the selected date
//TODO del		date=selectedDate!=null ? selectedDate : new Date();	//set the currently visible date to the selected date, or the current date if no date is selected
//TODO del		date=selectedDate;	//set the currently visible date to the selected date
/*TODO del
			//YYYY
		yearControl=new TextControl<Integer>(Integer.class);	//create the year control
		yearControl.setColumnCount(4);	//provide for sufficient characters for the year
		addComponent(yearControl);	//add the year control
			//MM
		monthControl=new TextControl<Integer>(Integer.class);	//create the month control
		//TODO set the range
		monthControl.setColumnCount(2);	//provide for sufficient characters for the month
		addComponent(monthControl);	//add the month control
*/
		addComponent(new Label("-"));
		monthListControl=new ListControl<Date>(Date.class, new SingleListSelectionPolicy<Date>());	//create a list control allowing only single selections of a month
//TODO fix		monthListControl.setLabel("Month");	//set the month control label TODO get from resources
//TODO fix		monthListControl.setValidator(new ValueRequiredValidator<Date>());	//require a locale to be selected in the list control
		monthListControl.setRowCount(1);	//make this a drop-down list
		final Converter<Date, String> monthConverter=new DateStringLiteralConverter(DateStringLiteralStyle.MONTH_OF_YEAR);	//get a converter to display the month of the year
		monthListControl.setValueRepresentationStrategy(new ListControl.DefaultValueRepresentationStrategy<Date>(monthConverter));	//install a month representation strategy
		addComponent(monthListControl);	//add the month control
		addComponent(new Label("-"));
			//DD
		dayControl=new TextControl<Integer>(Integer.class);	//create the day control
		//TODO set the range
		dayControl.setColumnCount(2);	//provide for sufficient characters for the day
		addComponent(dayControl);	//add the day control
			//HH
		hourControl=new TextControl<Integer>(Integer.class);	//create the hour control
		//TODO set the range
		hourControl.setColumnCount(2);	//provide for sufficient characters for the hours
		addComponent(hourControl);	//add the day control
		addComponent(new Label(":"));
			//MM
		minuteControl=new TextControl<Integer>(Integer.class);	//create the minute control
		//TODO set the range
		minuteControl.setColumnCount(2);	//provide for sufficient characters for the minutes
		addComponent(minuteControl);	//add the minutes control
		addComponent(new Label(":"));
			//SS
		secondControl=new TextControl<Integer>(Integer.class);	//create the second control
		//TODO set the range
		secondControl.setColumnCount(2);	//provide for sufficient characters for the seconds
		addComponent(secondControl);	//add the second control
		addComponent(new Label("."));
			//sss
		millisecondControl=new TextControl<Integer>(Integer.class);	//create the millisecond control
		//TODO set the range
		millisecondControl.setColumnCount(3);	//provide for sufficient characters for the milliseconds
		addComponent(millisecondControl);	//add the millisecond control

			//TODO fix; default date for testing purposes only
/*TODO del
		dateControl=new TextControl<Date>(Date.class, new Date());	//create a list control allowing only single selections of a month
		dateControl.setLabel("Date");	//set the date control label TODO get from resources
		final Converter<Date, String> dateConverter=new DateStringLiteralConverter(DateStringLiteralStyle.SHORT, TimeStringLiteralStyle.SHORT);	//get a converter to display the date in a numeric representation
		dateControl.setConverter(dateConverter);	//
		dateControl.setValidator(new ValueRequiredValidator<Date>());	//require a date
		dateControl.setColumnCount(10);	//provide for sufficient characters for the most common date format
*/
//TODO del		addComponent(dateControl);	//add the date control
			//create a year property change listener before we update the year control
		yearPropertyChangeListener=new AbstractGenericPropertyChangeListener<Integer>()	//create a property change listener to listen for the year changing
				{
					public void propertyChange(final GenericPropertyChangeEvent<Integer> propertyChangeEvent)	//if the selected year changed
					{
/*TODO fix
						final Integer newYear=propertyChangeEvent.getNewValue();	//get the new selected year
						if(newYear!=null)	//if a new year was selected (a null value can be sent when the model is cleared)
						{
							final Calendar calendar=Calendar.getInstance(getSession().getLocale());	//create a new calendar
							calendar.setTime(getDate());	//set the calendar date to our currently displayed date
							if(calendar.get(Calendar.YEAR)!=newYear)	//if the currently visible date is in another year
							{
								calendar.set(Calendar.YEAR, newYear);	//change to the given year
								setDate(calendar.getTime());	//change the date to the given month, which will update the calenders TODO make sure that going from a 31-day month, for example, to a 28-day month will be OK, if the day is day 31
							}
						}
*/
					}
				};
		updateYearControl();	//create and install an appropriate year control
		updateDateControls();	//update the date controls
		updateDateControlsPropertyChangeListener=new AbstractGenericPropertyChangeListener<Object>()	//create a property change listener to update the calendars
		{
			public void propertyChange(final GenericPropertyChangeEvent<Object> propertyChangeEvent)	//if the model value value changed
			{
				updateDateControls();	//update the date controls based upon the new selected date
			}
		};
		addPropertyChangeListener(VALUE_PROPERTY, updateDateControlsPropertyChangeListener);	//update the calendars if the selected date changes
		addPropertyChangeListener(VALIDATOR_PROPERTY, new AbstractGenericPropertyChangeListener<Validator<Date>>()	//create a property change listener to listen for our validator changing, so that we can update the date control if needed
				{
					public void propertyChange(final GenericPropertyChangeEvent<Validator<Date>> propertyChangeEvent)	//if the model's validator changed
					{
						updateYearControl();	//update the year control (e.g. a drop-down list) to match the new validator (e.g. a range validator), if any
					}
				});
	}

	/**Updates the year control by removing any old year control from the component and adding a new year control.
	If the model used by the calendar control uses a {@link RangeValidator} with a date range of less than 100 years, a drop-down list will be used for the year control.
	Otherwise, a text input will be used for year selection.
	*/
	protected void updateYearControl()
	{
		final ValueControl<Integer> oldYearControl=yearControl;	//get the old year control, which we'll replace
		if(oldYearControl!=null)	//if there is a year control already in use
		{
			removeComponent(yearControl);	//remove our year control TODO later use controlContainer.replace() when that method is available
			oldYearControl.removePropertyChangeListener(ValueModel.VALUE_PROPERTY, yearPropertyChangeListener);	//stop listening for the year changing
			yearControl=null;	//for completeness, indicate that we don't currently have a year control
		}
		final Locale locale=getSession().getLocale();	//get the current locale
			//see if there is a minimum and maximum date specified; this will determine what sort of control to use for the date input
		int minYear=-1;	//we'll determine if there is a minimum and/or maximum year restriction
		int maxYear=-1;
		final Validator<Date> validator=getValidator();	//get our validator
		if(validator instanceof RangeValidator)	//if there is a range validator installed
		{
			final RangeValidator<Date> rangeValidator=(RangeValidator<Date>)validator;	//get the validator as a range validator
			final Calendar calendar=Calendar.getInstance(locale);	//create a new calendar for determining the year of the restricted dates
			final Date minDate=rangeValidator.getMinimum();	//get the minimum date
			if(minDate!=null)	//if there is a minimum date specified
			{
				calendar.setTime(minDate);	//set the calendar date to the minimum date
				minYear=calendar.get(Calendar.YEAR);	//get the minimum year to use
			}
			final Date maxDate=rangeValidator.getMaximum();	//get the maximum date
			if(maxDate!=null)	//if there is a maximum date specified
			{
				calendar.setTime(maxDate);	//set the calendar date to the maximum date
				maxYear=calendar.get(Calendar.YEAR);	//get the maximum year to use
			}
		}
		if(minYear>=0 && maxYear>=0 && maxYear-minYear<100)	//if there is a minimum year and maximum year specified, use a drop-down control
		{
			final ListControl<Integer> yearListControl=new ListControl<Integer>(Integer.class, new SingleListSelectionPolicy<Integer>());	//create a list control allowing only single selections
			yearListControl.setRowCount(1);	//make the list control a drop-down list
			for(int year=minYear; year<=maxYear; ++year)	//for each valid year
			{
				yearListControl.add(Integer.valueOf(year));	//add this year to the choices
			}
//TODO del			yearListControl.setValidator(new ValueRequiredValidator<Integer>());	//require a value in the year drop-down
			yearControl=yearListControl;	//use the year list control for the year control
		}
		else	//if minimum and maximum years are not specified, use a standard text control TODO update to use a spinner control as well, and auto-update the value once four characters are entered 
		{
			final TextControl<Integer> yearTextControl=new TextControl<Integer>(Integer.class);	//create a text control to select the year
			yearTextControl.setMaximumLength(4);	//TODO testing
			yearTextControl.setColumnCount(4);	//TODO testing
//TODO fix or del			yearTextControl.setValidator(new IntegerRangeValidator(new Integer(1800), new Integer(2100), new Integer(1), true));	//restrict the range of the year TODO improve; don't arbitrarily restrict the range
			yearTextControl.setValidator(new IntegerRangeValidator(new Integer(1800), new Integer(2100), new Integer(1)));	//restrict the range of the year TODO improve; don't arbitrarily restrict the range
			yearTextControl.setAutoCommitPattern(Pattern.compile("\\d{4}"));	//automatically commit the year when four digits are entered
			yearControl=yearTextControl;	//use the year text control for the year control
		}
		assert yearControl!=null : "Failed to create a year control";
//TODO fix if needed		yearControl.setStyleID("year");	//TODO use a constant
//TODO del if not wanted		yearControl.setLabel("Year");	//set the year control label TODO get from resources

		yearControl.setLabel(getLabel());	//TODO improve
		yearControl.setLabelContentType(getLabelContentType());	//TODO improve

//TODO del when works		final Date date=getDate();	//get the current date
		final Date date=getValue();	//get the current date
		if(date!=null)	//if there is a date currently displayed
		{
			final Calendar calendar=Calendar.getInstance(locale);	//create a new calendar for setting the year
			calendar.setTime(date);	//set the calendar date to our displayed date
			final int year=calendar.get(Calendar.YEAR);	//get the current year
			try
			{
				yearControl.setValue(Integer.valueOf(year));	//show the selected year in the text box
			}
			catch(final PropertyVetoException propertyVetoException)	//we should never have a problem selecting a year or a month
			{
				throw new AssertionError(propertyVetoException);
			}
		}
		else	//if there is no date displayed
		{
			yearControl.clearValue();	//clear the year value
		}
		yearControl.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, yearPropertyChangeListener);	//listen for the year changing
		addComponent(0, yearControl);	//add the year text control TODO use replaceComponent when available
	}

	/**The locale used the last time the date controls were updated, or <code>null</code> if no locale was known.*/
	private Locale oldLocale=null;

	/**The month calendar used the last time the calendars were updated, or <code>null</code> if no calendar was known.*/
//TODO del	private Calendar oldCalendar=null;

	/**The month calendar used the last time the calendars were updated, or <code>null</code> if no calendar was known.*/
	private Calendar calendar=null;

	/**The last year used for updating months, or -1 if no year was known.*/
//TODO del	private int oldMonthYear=-1;

	/**Whether we're currently updating the date controls, to avoid reentry from control events.*/
	private boolean updatingDateControls=false;
	
	/**Updates the controls representing the date.
	This implementation updates the calendars on the calendar panel.
	*/
	protected synchronized void updateDateControls()
	{
		if(!updatingDateControls)	//if we're not already updating the calendars
		{
			updatingDateControls=true;	//show that we're updating the calendars
			try
			{
				
				
//TODO del		Debug.trace("*** Updating calendars");
				final GuiseSession session=getSession();	//get the Guise session
				final Integer oldMonthYearInteger=calendar!=null ? Integer.valueOf(calendar.get(Calendar.YEAR)) : null;	//get the year last used for calculating months
				final Locale locale=session.getLocale();	//get the current locale
				final boolean localeChanged=!locale.equals(oldLocale);	//see if the locale changed
				if(calendar==null || localeChanged)	//if we don't have a month calendar, or the locale changed
				{
					calendar=Calendar.getInstance(locale);	//create a new calendar
					calendar.setTimeZone(TimeZone.getTimeZone(GMT_ID));	//change to GMT; we'll do the date offset ourselves
//TODO del					monthCalendar.setTime(date!=null ? date : new Date());	//set the calendar date to our displayed date
				}
				
				
//TODO fix				final Calendar calendar;	//determine which calendar to use
				
				
				

//TODO del when works				final Date date=getDate();	//get the visible date
				final Date date=getValue();	//get the current date
/*TODO fix
				final boolean dateChanged=oldCalendar==null || (date!=null && !oldCalendar.getTime().equals(date));	//we'll have to calculate all new dates if there was no calendar before or the dates diverged		
				if(localeChanged || dateChanged)	//if the locale changed or the date changed
				{
					calendar=Calendar.getInstance(locale);	//create a new calendar
					calendar.setTimeZone(TimeZone.getTimeZone(GMT_ID));	//change to GMT; we'll do the date offset ourselves
					calendar.setTime(date!=null ? date : new Date());	//set the calendar date to our displayed date
				}
				else	//if we can keep the old calendar
				{
					calendar=oldCalendar;	//keep the calendar we had before
				}
*/
//TODO fix				final long utcOffsetMilliseconds=session.getUTCOffset()/1000;	//get the UTC offset in milliseconds
				final Date monthDate=date!=null ? date : new Date();	//get the date to use for determining months
//TODO fix				calendar.setTime(new Date(monthDate.getTime()+utcOffsetMilliseconds));	//set the calendar date to the correct date, compensating for this session's offset
				final int year=calendar.get(Calendar.YEAR);	//get the current year
				final boolean yearChanged=localeChanged || oldMonthYearInteger==null || oldMonthYearInteger.intValue()!=year;	//the year should be updated if the locale changed, there was no previous year, or the year changed
				final int month=calendar.get(Calendar.MONTH);	//get the current month
//TODO del if not needed				final boolean monthChanged=yearChanged || oldCalendar.get(Calendar.MONTH)!=month;	//the month should be updated if the the year or month changed
				try
				{
					yearControl.setValue(date!=null ? Integer.valueOf(year) : null);	//show the selected year in the text box
					if(yearChanged)	//if the year changed (different years can have different months with some calendars)
					{
						monthListControl.clear();	//clear the values in the month list control
						final Calendar monthNameCalendar=(Calendar)calendar.clone();	//clone the month calendar as we step through the months
						final int minMonth=monthNameCalendar.getActualMinimum(Calendar.MONTH);	//get the minimum month
						final int maxMonth=monthNameCalendar.getActualMaximum(Calendar.MONTH);	//get the maximum month
						int namedMonthIndex=-1;	//keep track of the named month index in the list
						for(int namedMonth=minMonth; namedMonth<=maxMonth; ++namedMonth)	//for each month
						{
							++namedMonthIndex;	//keep track of the list index
							monthNameCalendar.set(Calendar.MONTH, namedMonth);	//switch to the given month
							monthListControl.add(monthNameCalendar.getTime());	//add this month date
							if(date!=null && namedMonth==month)	//if this is the selected month
							{
								monthListControl.setSelectedIndexes(namedMonthIndex);	//select this month
							}
						}
					}
					else	//if the year didn't change, we still need to update the month
					{
						monthListControl.setSelectedIndexes(date!=null ? month-1 : -1);	//select this month
					}
					if(date!=null)	//if there is a date, set the values
					{
						final int day=calendar.get(Calendar.DAY_OF_MONTH);	//get the current day
						dayControl.setValue(day);	//set the day
						final int hour=calendar.get(Calendar.HOUR_OF_DAY);	//get the current hour
						hourControl.setValue(hour);	//set the hour
						final int minute=calendar.get(Calendar.MINUTE);	//get the current minute
						minuteControl.setValue(minute);	//set the minute
						final int second=calendar.get(Calendar.SECOND);	//get the current second
						secondControl.setValue(second);	//set the second
						final int millisecond=calendar.get(Calendar.MILLISECOND);	//get the current milliseconds
						millisecondControl.setValue(minute);	//set the millisecond
					}
					else	//if there is no date, clear the values
					{
						dayControl.clearValue();
						hourControl.clearValue();
						minuteControl.clearValue();
						secondControl.clearValue();
						millisecondControl.clearValue();
					}
				}
				catch(final PropertyVetoException propertyVetoException)	//we should never have a problem selecting a year or a month
				{
					throw new AssertionError(propertyVetoException);
				}
/*TODO del if not needed
				if(monthChanged)	//if the month needs updating
				{
					final Calendar monthCalendar=(Calendar)calendar.clone();	//clone the calendar for stepping through the months
					final Container calendarContainer=getCalendarContainer();	//get the calendar container
					calendarContainer.clear();	//remove all calendars from the container
					final CellRepresentationStrategy<Date> dayRepresentationStrategy=createDayRepresentationStrategy();	//create a strategy for representing the days in the month calendar cells
					for(int monthIndex=0; monthIndex<getMonthCount(); ++monthIndex)	//for each month
					{
						final CalendarMonthTableModel calendarMonthTableModel=new CalendarMonthTableModel(monthCalendar.getTime());	//create a table model for this month
						calendarMonthTableModel.setColumnLabelDateStyle(DateStringLiteralStyle.DAY_OF_WEEK_SHORT);	//show the short day of the week in each column
						final Table calendarMonthTable=new Table(calendarMonthTableModel);	//create a table to hold the calendar month
						calendarMonthTable.setCellRepresentationStrategy(Date.class, dayRepresentationStrategy);	//install the representation strategy for dates
						calendarContainer.add(calendarMonthTable);	//add the month table to the calendar container
						calendarTables.add(calendarMonthTable);	//add this table to the list of calendar tables
						monthCalendar.add(Calendar.MONTH, 1);	//go to the next month
					}
				}
*/
				oldLocale=locale;	//update the old locale
//TODO del				oldCalendar=calendar;	//update the old calendar
			}
			finally
			{
				updatingDateControls=false;	//show that we're no longer updating calendars
			}
		}
	}

}
