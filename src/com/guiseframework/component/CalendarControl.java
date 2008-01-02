package com.guiseframework.component;

import java.beans.PropertyVetoException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;

import com.garretwilson.beans.*;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.Table.CellRepresentationStrategy;
import com.guiseframework.component.layout.Flow;
import com.guiseframework.component.layout.FlowLayout;
import com.guiseframework.converter.*;
import com.guiseframework.event.*;
import com.guiseframework.model.*;
import com.guiseframework.validator.*;

/**Control that allows selection of a date.
If the model used by the calendar control uses a {@link RangeValidator} with a date range of less than 100 years, a drop-down list will be used for the year control.
Otherwise, a text input will be used for year selection.
@author Garret Wilson
*/
public class CalendarControl extends AbstractContainerValueControl<Date>
{

	/**The visible date bound property.*/
	public final static String DATE_PROPERTY=getPropertyName(CalendarControl.class, "date");

	/**Whether the value is editable and the control will allow the the user to change the value.*/
//TODO del	private boolean editable=true;	//TODO fix

		/**@return Whether the value is editable and the control will allow the the user to change the value.*/
//TODO del		public boolean isEditable() {return editable;}

		/**Sets whether the value is editable and the control will allow the the user to change the value.
		This is a bound property of type <code>Boolean</code>.
		@param newEditable <code>true</code> if the control should allow the user to change the value.
		@see #EDITABLE_PROPERTY
		*/
/*TODO del
		public void setEditable(final boolean newEditable)
		{
			if(editable!=newEditable)	//if the value is really changing
			{
				final boolean oldEditable=editable;	//get the old value
				editable=newEditable;	//actually change the value
				firePropertyChange(EDITABLE_PROPERTY, Boolean.valueOf(oldEditable), Boolean.valueOf(newEditable));	//indicate that the value changed
			}			
		}
*/

	private int getMonthCount() {return 1;}	//TODO update to allow modification

	/**The container containing the controls.*/
	private Container controlContainer;
	
		/**The container containing the controls.*/
		public Container getControlContainer() {return controlContainer;}

	/**The container containing the calendars.*/
	private Container calendarContainer;
	
		/**The container containing the calendars.*/
		public Container getCalendarContainer() {return calendarContainer;}

	/**The list control containing the months.*/
	private final ListControl<Date> monthListControl;

		/**@return The list control containing the months.*/
		protected ListControl<Date> getMonthListControl() {return monthListControl;}

	/**The control containing the year; this control can change dynamically based upon the current model range.*/
	private ValueControl<Integer> yearControl=null;

		/**@return The control containing the year.*/
		protected ValueControl<Integer> getYearControl() {return yearControl;}

	/**The list of calendar table components.*/
	private final List<Table> calendarTables=new CopyOnWriteArrayList<Table>();
	
		/**@return An iterator to the calendar table components.*/
		protected Iterator<Table> getCalendarTables() {return calendarTables.iterator();}

	/**The date being viewed, not necessarily chosen.*/
	private Date date;

		/**@return The date being viewed, not necessarily chosen.*/
		public Date getDate() {return (Date)date.clone();}

		/**Sets the date being viewed.
		A copy will be made of the date before it is stored.
		This is a bound property.
		@param newDate The date to be viewed, not necessarily chosen.
		@exception NullPointerException if the given date is <code>null</code>.
		@see #DATE_PROPERTY
		*/
		public void setDate(final Date newDate)
		{
			if(!date.equals(checkInstance(newDate, "Date cannot be null.")))	//if the value is really changing
			{
				final Date oldDate=date;	//get the old value
				date=(Date)newDate.clone();	//clone the new date and actually change the value
				updateCalendars();	//update the calendars based upon the new value
				firePropertyChange(DATE_PROPERTY, oldDate, newDate);	//indicate that the value changed
			}
		}

	/**Default constructor with a default data model.*/
	public CalendarControl()
	{
		this(new DefaultValueModel<Date>(Date.class));	//construct the class with a default value model
	}

	/**The property change listener that updates the calendars when a property changes.*/
	protected final GenericPropertyChangeListener<?> updateModelPropertyChangeListener;

	/**The property change listener that updates the visible dates if the year is different than the last one.*/
	protected final GenericPropertyChangeListener<Integer> yearPropertyChangeListener;

	/**Value model constructor.
	@param valueModel The component value model.
	@exception NullPointerException if the given value model is <code>null</code>.
	*/
	public CalendarControl(final ValueModel<Date> valueModel)
	{
		super(new FlowLayout(Flow.PAGE), valueModel);	//construct the parent class flowing along the page
		final Date selectedDate=valueModel.getValue();	//get the selected date
		date=selectedDate!=null ? selectedDate : new Date();	//set the currently visible date to the selected date, or the current date if no date is selected
		controlContainer=new LayoutPanel(new FlowLayout(Flow.LINE));	//create the control panel
		add(controlContainer);	//add the control panel
		calendarContainer=new LayoutPanel(new FlowLayout(Flow.LINE));	//create the calendar panel
		add(calendarContainer);	//add the calendar panel
		monthListControl=new ListControl<Date>(Date.class, new SingleListSelectionPolicy<Date>());	//create a list control allowing only single selections of a month
//TODO fix if needed		monthListControl.setStyleID("month");	//TODO use a constant
		monthListControl.setLabel("Month");	//set the month control label TODO get from resources
		monthListControl.setValidator(new ValueRequiredValidator<Date>());	//require a locale to be selected in the list control
		monthListControl.setRowCount(1);	//make this a drop-down list
		final Converter<Date, String> monthConverter=new DateStringLiteralConverter(DateStringLiteralStyle.MONTH_OF_YEAR);	//get a converter to display the month of the year
		monthListControl.setValueRepresentationStrategy(new ListControl.DefaultValueRepresentationStrategy<Date>(monthConverter));	//install a month representation strategy
		controlContainer.add(monthListControl);	//add the month list control
			//create a year property change listener before we update the year control
		yearPropertyChangeListener=new AbstractGenericPropertyChangeListener<Integer>()	//create a property change listener to listen for the year changing
				{
					public void propertyChange(final GenericPropertyChangeEvent<Integer> propertyChangeEvent)	//if the selected year changed
					{
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
					}
				};
/*TODO del
		if(model.getValidator()==null)	//TODO del; this is a temporary default for testing the date control based upon the model validator 
		{
			final Date maxDate=new Date();	//the current date will be the maximum date
			final Date minDate=new GregorianCalendar(1940, 0, 1).getTime();	//the first day of 1940 in the Gregorian calendar will be the minimum date
//TODO del			final Calendar calendar=Calendar.getInstance(session.getLocale());	//create a new calendar for determining the minimum and maximum year
//TODO del			final Date maxDate=calendar.getTime();	//the current date will be the maximum date
//TODO del			calendar.set(1940, 0, 1);	//set the calendar to the first day of 1940
//TODO del			final Date minDate=calendar.getTime();	//the first day of 1940 will be the minimum date
			model.setValidator(new DateRangeValidator(minDate, maxDate));	//restrict the date range
		}
*/
		updateYearControl();	//create and install an appropriate year control
		updateCalendars();	//update the calendars
		updateModelPropertyChangeListener=new AbstractGenericPropertyChangeListener<Object>()	//create a property change listener to update the calendars
		{
			public void propertyChange(final GenericPropertyChangeEvent<Object> propertyChangeEvent)	//if the model value value changed
			{
				updateCalendars();	//update the calendars based upon the new selected date
			}
		};
		valueModel.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, updateModelPropertyChangeListener);	//update the calendars if the selected date changes
		valueModel.addPropertyChangeListener(ValueModel.VALIDATOR_PROPERTY, new AbstractGenericPropertyChangeListener<Validator<Date>>()	//create a property change listener to listen for our validator changing, so that we can update the date control if needed
				{
					public void propertyChange(final GenericPropertyChangeEvent<Validator<Date>> propertyChangeEvent)	//if the model's validator changed
					{
						updateYearControl();	//update the year control (e.g. a drop-down list) to match the new validator (e.g. a range validator), if any
					}
				});
			//TODO important: this is a memory leak---make sure we uninstall the listener when the session goes away
		getSession().addPropertyChangeListener(GuiseSession.LOCALE_PROPERTY, updateModelPropertyChangeListener);	//update the calendars if the locale changes
		monthListControl.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Date>()	//create a property change listener to listen for the month changing
				{
					public void propertyChange(final GenericPropertyChangeEvent<Date> propertyChangeEvent)	//if the selected month changed
					{
						final Date newDate=propertyChangeEvent.getNewValue();	//get the new selected date
						if(newDate!=null)	//if a new month was selected (a null value can be sent when the model is cleared)
						{
							final Calendar newCalendar=Calendar.getInstance(getSession().getLocale());	//create a new calendar
							newCalendar.setTime(newDate);	//set the new calendar date to the newly selected month
							final int newMonth=newCalendar.get(Calendar.MONTH);	//get the new requested month
							final Calendar calendar=Calendar.getInstance(getSession().getLocale());	//create a new calendar
							calendar.setTime(getDate());	//set the calendar date to our currently displayed date
							if(calendar.get(Calendar.MONTH)!=newMonth)	//if the currently visible date is in another month
							{
								calendar.set(Calendar.MONTH, newMonth);	//change to the given month
								setDate(calendar.getTime());	//change the date to the given month, which will update the calenders TODO make sure that going from a 31-day month, for example, to a 28-day month will be OK, if the day is day 31
							}
						}
					}
				});		
	}

	/**Updates the year control by removing any old year control from the component and adding a new year control.
	If the model used by the calendar control uses a {@link RangeValidator} with a date range of less than 100 years, a drop-down list will be used for the year control.
	Otherwise, a text input will be used for year selection.
	*/
	protected void updateYearControl()
	{
		final Locale locale=getSession().getLocale();	//get the current locale
		if(yearControl!=null)	//if there is a year control already in use
		{
			controlContainer.remove(yearControl);	//remove our year control TODO later use controlContainer.replace() when that method is available
			yearControl.removePropertyChangeListener(ValueModel.VALUE_PROPERTY, yearPropertyChangeListener);	//stop listening for the year changing
			yearControl=null;	//for completeness, indicate that we don't currently have a year control
		}
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
				yearListControl.add(new Integer(year));	//add this year to the choices
			}
			yearListControl.setValidator(new ValueRequiredValidator<Integer>());	//require a value in the year drop-down
			yearControl=yearListControl;	//use the year list control for the year control
		}
		else	//if minimum and maximum years are not specified, use a standard text control TODO update to use a spinner control as well, and auto-update the value once four characters are entered 
		{
			final TextControl<Integer> yearTextControl=new TextControl<Integer>(Integer.class);	//create a text control to select the year
			yearTextControl.setMaximumLength(4);	//TODO testing
			yearTextControl.setColumnCount(4);	//TODO testing
			yearTextControl.setValidator(new IntegerRangeValidator(new Integer(1800), new Integer(2100), new Integer(1), true));	//restrict the range of the year TODO improve; don't arbitrarily restrict the range
			yearTextControl.setAutoCommitPattern(Pattern.compile("\\d{4}"));	//automatically commit the year when four digits are entered
			yearControl=yearTextControl;	//use the year text control for the year control
		}
		assert yearControl!=null : "Failed to create a year control";
//TODO fix if needed		yearControl.setStyleID("year");	//TODO use a constant
		yearControl.setLabel("Year");	//set the year control label TODO get from resources
		final Calendar calendar=Calendar.getInstance(locale);	//create a new calendar for setting the year
		calendar.setTime(getDate());	//set the calendar date to our displayed date
		final int year=calendar.get(Calendar.YEAR);	//get the current year
		try
		{
			yearControl.setValue(new Integer(year));	//show the selected year in the text box
		}
		catch(final PropertyVetoException propertyVetoException)	//we should never have a problem selecting a year or a month
		{
			throw new AssertionError(propertyVetoException);
		}
		yearControl.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, yearPropertyChangeListener);	//listen for the year changing
		controlContainer.add(yearControl);	//add the year text control		
	}
	
	/**The locale used the last time the calendars were updated, or <code>null</code> if no locale was known.*/
	private Locale oldLocale=null;

	/**The month calendar used the last time the calendars were updated, or <code>null</code> if no calendar was known.*/
	private Calendar oldCalendar=null;	
	
	/**Whether we're currently updating calendars, to avoid reentry from control events.*/
	private boolean updatingCalendars=false;
	
	/**Updates the calendars on the calendar panel.*/
	protected synchronized void updateCalendars()
	{
		if(!updatingCalendars)	//if we're not already updating the calendars
		{
			updatingCalendars=true;	//show that we're updating the calendars
			try
			{
//TODO del		Debug.trace("*** Updating calendars");
				final Locale locale=getSession().getLocale();	//get the current locale
				final boolean localeChanged=!locale.equals(oldLocale);	//see if the locale changed		
				final Calendar calendar;	//determine which calendar to use
				final Date date=getDate();	//get the visible date
				final boolean dateChanged=oldCalendar==null || !oldCalendar.getTime().equals(date);	//we'll have to calculate all new dates if there was no calendar before or the dates diverged		
				if(localeChanged || dateChanged)	//if the locale changed or the date changed
				{
					calendar=Calendar.getInstance(locale);	//create a new calendar
					calendar.setTime(date);	//set the calendar date to our displayed date
				}
				else	//if we can keep the old calendar
				{
					calendar=oldCalendar;	//keep the calendar we had before
				}
				final int year=calendar.get(Calendar.YEAR);	//get the current year
				final boolean yearChanged=localeChanged || oldCalendar==null || oldCalendar.get(Calendar.YEAR)!=year;	//the year should be updated if the locale changed, there was no calendar, or the years are different
				final int month=calendar.get(Calendar.MONTH);	//get the current month
				final boolean monthChanged=yearChanged || oldCalendar.get(Calendar.MONTH)!=month;	//the month should be updated if the the year or month changed
				if(yearChanged)	//if the year changed (different years can have different months with some calendars
				{
					try
					{
						yearControl.setValue(new Integer(year));	//show the selected year in the text box
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
							if(namedMonth==month)	//if this is the selected month
							{
								monthListControl.setSelectedIndexes(namedMonthIndex);	//select this month
							}
						}
					}
					catch(final PropertyVetoException propertyVetoException)	//we should never have a problem selecting a year or a month
					{
						throw new AssertionError(propertyVetoException);
					}
				}
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
						for(final TableColumnModel<?> tableColumn:calendarMonthTable.getTableModel().getColumns())	//for each table column
						{
							calendarMonthTable.setCellRepresentationStrategy((TableColumnModel<Date>)tableColumn, dayRepresentationStrategy);	//install the representation strategy for this column
						}
						calendarContainer.add(calendarMonthTable);	//add the month table to the calendar container
						calendarTables.add(calendarMonthTable);	//add this table to the list of calendar tables
						monthCalendar.add(Calendar.MONTH, 1);	//go to the next month
					}
				}
				oldLocale=locale;	//update the old locale
				oldCalendar=calendar;	//update the old calendar
			}
			finally
			{
				updatingCalendars=false;	//show that we're no longer updating calendars
			}
		}
	}

	/**Creates a representation strategy for each cell in a calendar.
	This version returns a new instance of {@link DayRepresentationStrategy}.
	@return a representation strategy for each cell in a calendar.
	@see DayRepresentationStrategy
	*/
	protected CellRepresentationStrategy<Date> createDayRepresentationStrategy()
	{
		return new DayRepresentationStrategy();	//return a new day representation strategy
	}
	
	/**A cell representation strategy for calendar days.
	A link will be generated using the day of the month as its label.
	The message's ID will be in the form "<var>tableID</var>.time<var>absoluteTimeHex</var>".
	@see Link
	@author Garret Wilson
	*/
	protected class DayRepresentationStrategy implements CellRepresentationStrategy<Date>
	{

		/**Creates a component for the given cell.
		@param <C> The type of value contained in the column.
		@param table The component containing the model.
		@param model The model containing the value.
		@param rowIndex The zero-based row index of the value.
		@param column The column of the value.
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value.
		*/
		public <C extends Date> Component createComponent(final Table table, final TableModel model, final int rowIndex, final TableColumnModel<C> column, final boolean editable, final boolean selected, final boolean focused)
		{
			final Calendar calendar=Calendar.getInstance(getSession().getLocale());	//create a calendar TODO cache the calendar and only change it if the locale has changed
			calendar.setTime(getDate());	//set the calendar date to the date of the calendar
			final int calendarMonth=calendar.get(Calendar.MONTH);	//get the month of the calendar
			final Date date=model.getCellValue(rowIndex, column);	//get the date for this cell
			final long time=date.getTime();	//get the time of the cell in milliseconds
			calendar.setTime(date);	//set the time of the calendar to that of the cell
			if(calendar.get(Calendar.MONTH)==calendarMonth)	//if this date is within the month
			{
				final Link link=new Link();	//create a link for this cell
				final String dayOfMonthString=Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));	//create a string using the day of the month
				link.setLabel(dayOfMonthString);	//set the label of the link to the day of the month
				final Validator<Date> validator=CalendarControl.this.getValidator();	//get the calendar control model's validator
				if(validator==null || validator.isValid(date))	//if there is no validator installed, or there is a validator and this is a valid date
				{
					link.addActionListener(new ActionListener()	//create a listener to listen for calendar actions
							{
								public void actionPerformed(final ActionEvent actionEvent)	//when a day is selected
								{
									try
									{
										CalendarControl.this.setValue(date);	//change the control's value to the calendar for this cell
									}
									catch(final PropertyVetoException propertyVetoException)
									{
//TODO fix to store errors or something, because a validator could be installed										throw new AssertionError(validationException);	//TODO fix to store the errors or something, because a validator could very well be installed in the control
									}
								}
							});
				}
				else	//if there is a validator installed and this is not a valid date
				{
					link.setEnabled(false);	//disable this link
				}
				return link;	//return the link
			}
			else	//if the date is outside the month
			{
				return new Label();	//return a blank label for the cell
			}
		}
	}

}
