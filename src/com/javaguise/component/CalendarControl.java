package com.javaguise.component;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.util.Debug;

import static com.garretwilson.lang.ClassUtilities.getPropertyName;
import static com.garretwilson.lang.ObjectUtilities.*;

import com.javaguise.GuiseSession;
import com.javaguise.component.Table.CellRepresentationStrategy;
import com.javaguise.component.Table.DefaultCellMessageModel;
import com.javaguise.component.Table.DefaultCellValueModel;
import com.javaguise.component.layout.Flow;
import com.javaguise.component.layout.FlowLayout;
import com.javaguise.converter.Converter;
import com.javaguise.converter.DateStringLiteralConverter;
import com.javaguise.converter.DateStringLiteralStyle;
import com.javaguise.event.AbstractGuisePropertyChangeListener;
import com.javaguise.event.ActionEvent;
import com.javaguise.event.ActionListener;
import com.javaguise.event.GuisePropertyChangeEvent;
import com.javaguise.event.GuisePropertyChangeListener;
import com.javaguise.model.ActionModel;
import com.javaguise.model.CalendarMonthTableModel;
import com.javaguise.model.DefaultValueModel;
import com.javaguise.model.ListSelectModel;
import com.javaguise.model.SingleListSelectionPolicy;
import com.javaguise.model.TableColumnModel;
import com.javaguise.model.TableModel;
import com.javaguise.model.ValueModel;
import com.javaguise.validator.ValidationException;
import com.javaguise.validator.ValueRequiredValidator;

/**Control that allows selection of a date.
@author Garret Wilson
*/
public class CalendarControl extends AbstractContainer<CalendarControl> implements ValueControl<Date, CalendarControl>
{

	/**The visible date bound property.*/
	public final static String DATE_PROPERTY=getPropertyName(CalendarControl.class, "date");

	/**Whether the state of the control represents valid user input.*/
	private boolean valid=true;

		/**@return Whether the state of the control represents valid user input.*/
		public boolean isValid() {return valid;}

		/**Sets whether the state of the control represents valid user input
		This is a bound property of type <code>Boolean</code>.
		@param newValid <code>true</code> if user input should be considered valid
		@see Control#VALID_PROPERTY
		*/
		public void setValid(final boolean newValid)
		{
			if(valid!=newValid)	//if the value is really changing
			{
				final boolean oldValid=valid;	//get the current value
				valid=newValid;	//update the value
				firePropertyChange(VALID_PROPERTY, Boolean.valueOf(oldValid), Boolean.valueOf(newValid));
			}
		}

	private int getMonthCount() {return 1;}	//TODO update to allow modification

	/**@return The data model used by this component.*/
	@SuppressWarnings("unchecked")
	public ValueModel<Date> getModel() {return (ValueModel<Date>)super.getModel();}

	/**The container containing the controls.*/
	private Container<?> controlContainer;
	
		/**The container containing the controls.*/
		public Container<?> getControlContainer() {return controlContainer;}

	/**The container containing the calendars.*/
	private Container<?> calendarContainer;
	
		/**The container containing the calendars.*/
		public Container<?> getCalendarContainer() {return calendarContainer;}

	/**The list control containing the months.*/
	private final ListControl<Date> monthListControl;

		/**@return The list control containing the months.*/
		protected ListControl<Date> getMonthListControl() {return monthListControl;}

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
			if(!date.equals(checkNull(newDate, "Date cannot be null.")))	//if the value is really changing
			{
				final Date oldDate=date;	//get the old value
				date=(Date)newDate.clone();	//clone the new date and actually change the value
				updateCalendars();	//update the calendars based upon the new value
				firePropertyChange(DATE_PROPERTY, oldDate, newDate);	//indicate that the value changed
			}
		}

	/**Session constructor with a default data model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public CalendarControl(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used		
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CalendarControl(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultValueModel<Date>(session, Date.class));	//construct the class with a default value model
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public CalendarControl(final GuiseSession session, final ValueModel<Date> model)
	{
		this(session, null, model);	//construct the component, indicating that a default ID should be used		
	}

//TODO del	protected final GuisePropertyChangeListener<CalendarMonthTableModel, Calendar> calendarChangeListener;
//TODO del	protected final ActionListener<CalendarMonthTableModel> calendarActionListener;

	/**The property change listener that updates the calendars when a property changes.*/
	protected final GuisePropertyChangeListener<?> updateModelPropertyChangeListener;

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CalendarControl(final GuiseSession session, final String id, final ValueModel<Date> model)
	{
		super(session, id, new FlowLayout(session, Flow.PAGE), model);	//construct the parent class flowing along the page
		final Date selectedDate=model.getValue();	//get the selected date
		date=selectedDate!=null ? selectedDate : new Date();	//set the currently visible date to the selected date, or the current date if no date is selected
		controlContainer=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));	//create the control panel
		add(controlContainer);	//add the control panel
		calendarContainer=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));	//create the calendar panel
		add(calendarContainer);	//add the calendar panel
		monthListControl=new ListControl<Date>(session, Date.class, new SingleListSelectionPolicy<Date>());	//create a list control allowing only single selections of a month
		monthListControl.getModel().setLabel("Month");	//set the month control label TODO get from resources
		monthListControl.getModel().setValidator(new ValueRequiredValidator<Date>(session));	//require a locale to be selected in the list control
		monthListControl.setRowCount(1);	//make this a drop-down list
		final Converter<Date, String> monthConverter=new DateStringLiteralConverter(session, DateStringLiteralStyle.MONTH_OF_YEAR);	//get a converter to display the month of the year
		monthListControl.setValueRepresentationStrategy(new ListControl.DefaultValueRepresentationStrategy<Date>(session, monthConverter));	//install a month representation strategy
		controlContainer.add(monthListControl);	//add the month list control
		updateCalendars();	//update the calendars
		updateModelPropertyChangeListener=new AbstractGuisePropertyChangeListener<Object>()	//create a property change listener to update the calendars
		{
			public void propertyChange(final GuisePropertyChangeEvent<Object> propertyChangeEvent)	//if the model value value changed
			{
				updateCalendars();	//update the calendars based upon the new selected date
			}
		};
		model.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, updateModelPropertyChangeListener);	//update the calendars if the selected date changes
			//TODO important: this is a memory leak---make sure we uninstall the listener when the session goes away
		session.addPropertyChangeListener(GuiseSession.LOCALE_PROPERTY, updateModelPropertyChangeListener);	//update the calendars if the locale changes
		monthListControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<Date>()	//create a property change listener to listen for the month changing
				{
					public void propertyChange(final GuisePropertyChangeEvent<Date> propertyChangeEvent)	//if the selected month changed
					{
						final Date newDate=propertyChangeEvent.getNewValue();	//get the new selected date
						if(newDate!=null)	//if a new month was selected (a null value can be sent when the model is cleared)
						{
							final Calendar newCalendar=Calendar.getInstance(session.getLocale());	//create a new calendar
							newCalendar.setTime(newDate);	//set the new calendar date to the newly selected month
							final int newMonth=newCalendar.get(Calendar.MONTH);	//get the new requested month
							final Calendar calendar=Calendar.getInstance(session.getLocale());	//create a new calendar
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
		Debug.trace("*** Updating calendars");
				final GuiseSession session=getSession();	//get the current session
				final Locale locale=session.getLocale();	//get the current locale
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
				final boolean yearChanged=localeChanged || oldCalendar==null || oldCalendar.get(Calendar.YEAR)!=calendar.get(Calendar.YEAR);	//the year should be updated if the locale changed, there was no calendar, or the years are different
				final int month=calendar.get(Calendar.MONTH);	//get the current month
				final boolean monthChanged=yearChanged || oldCalendar.get(Calendar.MONTH)!=month;	//the month should be updated if the the year or month changed
				if(yearChanged)	//if the year changed (different years can have different months with some calendars
				{
					final ListSelectModel<Date> monthListModel=monthListControl.getModel();	//get model of the month list control
					monthListModel.clear();	//clear the values in the month list control
					final Calendar monthNameCalendar=(Calendar)calendar.clone();	//clone the month calendar as we step through the months
					final int minMonth=monthNameCalendar.getActualMinimum(Calendar.MONTH);	//get the minimum month
					final int maxMonth=monthNameCalendar.getActualMaximum(Calendar.MONTH);	//get the maximum month
					int namedMonthIndex=-1;	//keep track of the named month index in the list
					for(int namedMonth=minMonth; namedMonth<=maxMonth; ++namedMonth)	//for each month
					{
						++namedMonthIndex;	//keep track of the list index
						monthNameCalendar.set(Calendar.MONTH, namedMonth);	//switch to the given month
						monthListModel.add(monthNameCalendar.getTime());	//add this month date
						if(namedMonth==month)	//if this is the selected month
						{
							try
							{
								monthListModel.setSelectedIndexes(namedMonthIndex);	//select this month
							}
							catch(final ValidationException validationException)	//we should never have a problem selecting a month
							{
								throw new AssertionError(validationException);
							}
						}
					}
				}
				if(monthChanged)	//if the month needs updating
				{
					final Calendar monthCalendar=(Calendar)calendar.clone();	//clone the calendar for stepping through the months
					final Container<?> calendarContainer=getCalendarContainer();	//get the calendar container
					calendarContainer.clear();	//remove all calendars from the container
					final CellRepresentationStrategy<Date> dayRepresentationStrategy=createDayRepresentationStrategy();	//create a strategy for representing the days in the month calendar cells
					for(int monthIndex=0; monthIndex<getMonthCount(); ++monthIndex)	//for each month
					{
						final CalendarMonthTableModel calendarMonthTableModel=new CalendarMonthTableModel(session, monthCalendar.getTime());	//create a table model for this month
						calendarMonthTableModel.setColumnLabelDateStyle(DateStringLiteralStyle.DAY_OF_WEEK_SHORT);	//show the short day of the week in each column
						final Table calendarMonthTable=new Table(session, calendarMonthTableModel);	//create a table to hold the calendar month
						for(final TableColumnModel<?> tableColumn:calendarMonthTable.getModel().getColumns())	//for each table column
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
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		@SuppressWarnings("unchecked")	//we check the type of the column value class, so the casts are safe
		public <C extends Date> Component<?> createComponent(final Table table, final TableModel model, final int rowIndex, final TableColumnModel<C> column, final boolean editable, final boolean selected, final boolean focused)
		{
			final GuiseSession session=getSession();	//get the session
			final Date date=model.getCellValue(rowIndex, column);	//get the date for this cell
			final long time=date.getTime();	//get the time of the cell in milliseconds
			final String id=table.createID("time"+Long.toHexString(time));	//create an ID for the new component
			final Link link=new Link(session, id);	//create a link for this cell
			final Calendar calendar=Calendar.getInstance(getSession().getLocale());	//create a calendar TODO cache the calendar and only change it if the locale has changed
			calendar.setTime(date);	//set the time of the calendar to that of the cell
			link.getModel().setLabel(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));	//set the label of the link to the day of the month
			link.getModel().addActionListener(new ActionListener()	//create a listener to listen for calendar actions
					{
						public void actionPerformed(ActionEvent actionEvent)	//when a day is selected
						{
							try
							{
								CalendarControl.this.getModel().setValue(date);	//change the control's value to the calendar for this cell
							}
							catch(final ValidationException validationException)
							{
								throw new AssertionError(validationException);	//TODO fix to store the errors or something, because a validator could very well be installed in the control
							}
						}
					});
			return link;	//return the link
		}
	}

}
