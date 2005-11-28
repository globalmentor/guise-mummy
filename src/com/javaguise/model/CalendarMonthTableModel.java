package com.javaguise.model;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.IntegerUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.util.ArrayUtilities.*;
import static com.garretwilson.util.CalendarConstants.*;

import com.garretwilson.util.ArrayUtilities;
import com.garretwilson.util.CollectionUtilities;
import com.garretwilson.util.Debug;
import com.garretwilson.util.SynchronizedListDecorator;

import com.javaguise.session.GuiseSession;
import com.javaguise.validator.Validator;

/**A table model representing the days of a calendar month.
Each cell contains a {@link Calendar} value.
@author Garret Wilson
*/
public class CalendarMonthTableModel extends AbstractTableModel	//TODO set the model to read-only
{

	/**The month calendar bound property.*/
	public final static String MONTH_CALENDAR_PROPERTY=getPropertyName(CalendarMonthTableModel.class, "monthCalendar");

	/**The number of days this calendar should be offset left (negative) or right (positive) so that the days align with the correct day-of-the-week column.*/
	private int dayOffset=0;

		/**@return The number of days this calendar should be offset left (negative) or right (positive) so that the days align with the correct day-of-the-week column.*/
		protected int getDayOffset() {return dayOffset;}

	/**The number of rows in this table.*/
	private int rowCount=0;	

		/**@return The number of rows in this table.*/
		public int getRowCount() {return rowCount;}

	/**Updates the model based upon the current calendar.*/
	protected void updateModel()
	{
		monthCalendar=(Calendar)calendar.clone();	//make a copy of the calendar
		monthCalendar.set(Calendar.DAY_OF_MONTH, 1);	//set the month calendar to the first day of the month
		monthCalendar.set(Calendar.HOUR_OF_DAY, 0);	//set the hour to midnight
		monthCalendar.set(Calendar.MINUTE, 0);	//set the minute to zero
		monthCalendar.set(Calendar.SECOND, 0);	//set the second to zero
		monthCalendar.set(Calendar.MILLISECOND, 0);	//set the millisecond to zero
Debug.trace("ready to update model with calendar", monthCalendar);
		final int firstDayOfWeek=monthCalendar.getFirstDayOfWeek();	//get the first day of the week for this calendar
Debug.trace("first day of week", firstDayOfWeek);
		final int dayOfWeek=monthCalendar.get(Calendar.DAY_OF_WEEK);	//get the day of the week of the first day of the month
		Debug.trace("day of week", dayOfWeek);
		dayOffset=firstDayOfWeek-dayOfWeek;	//calculate the offset for the first day of the month, and all days
		Debug.trace("day offset", dayOffset);
		rowCount=(int)Math.ceil((calendar.getMaximum(Calendar.DAY_OF_MONTH)-dayOffset)/(double)WEEK_DAY_COUNT);	//find out how many partial rows are used, taking into account the day offset
		Debug.trace("row count", rowCount);
	}

	/**The calendar representing the first day of the month.*/
	private Calendar monthCalendar;

		/**@return A clone of the calendar representing the first day of the month.*/
		protected Calendar getMonthCalendar() {return (Calendar)monthCalendar.clone();}

	/**The calendar representing the date.*/
	private Calendar calendar;

		/**@return The calendar representing the date.*/
		public Calendar getCalendar() {return (Calendar)calendar.clone();}

		/**Sets the calendar representing the date.
		A copy will be made of the calendar before it is stored.
		This is a bound property.
		@param newCalendar The calendar representing the date.
		@exception NullPointerException if the given calendar is <code>null</code>.
		@see #MONTH_CALENDAR_PROPERTY
		*/
		public void setCalendar(final Calendar newCalendar)
		{
			if(!calendar.equals(checkNull(newCalendar, "Calendar cannot be null.")))	//if the value is really changing
			{
				final Calendar oldCalendar=calendar;	//get the old value
				calendar=(Calendar)newCalendar.clone();	//clone the new month calendar and actually change the value
				updateModel();	//update the model based upon the new value
				firePropertyChange(MONTH_CALENDAR_PROPERTY, oldCalendar, newCalendar);	//indicate that the value changed
			}
		}

	/**Session constructor for current month using the session locale.
	@param session The Guise session that owns this model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public CalendarMonthTableModel(final GuiseSession session)
	{
		this(session, Calendar.getInstance(session.getLocale()));	//construct the class using the current month in the session locale
	}

	/**Session and month calendar constructor.
	@param session The Guise session that owns this model.
	@param calendar The calendar representing the date.
	@exception NullPointerException if the given session and/or month calendar is <code>null</code>.
	*/
	public CalendarMonthTableModel(final GuiseSession session, final Calendar calendar)
	{
		super(session);	//construct the parent class
		for(int i=0; i<WEEK_DAY_COUNT; ++i)	//for each week day index
		{
			addColumn(new WeekDayTableColumnModel(session, i));	//add a new week day table column
		}
		this.calendar=checkNull(calendar, "Calendar cannot be null");
		updateModel();	//update the model to match the initial month calendar
	}

	/**Returns the cell value at the given row and column.
	@param <C> The type of cell values in the given column.
	@param rowIndex The zero-based row index.
	@param column The column for which a value should be returned.
	@return The value in the cell at the given row and column, or <code>null</code> if there is no value in that cell.
	@exception IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	@exception IllegalArgumentException if the given column is not one of this table's columns.
	*/
	public <C> C getCellValue(final int rowIndex, final TableColumnModel<C> column)
	{
		final int columnIndex=getColumnIndex(column);	//get the index of this column
		if(columnIndex<0)	//if this column isn't in this table
		{
			throw new IllegalArgumentException("Table column "+column+" not in table.");
		}
		final int offset=rowIndex*WEEK_DAY_COUNT+columnIndex+getDayOffset();	//find the absolute offset from the beginning, and then compensate for the first day of the month
		final Calendar cellCalendar=getMonthCalendar();	//get a clone of the month calendar
		cellCalendar.add(Calendar.DAY_OF_MONTH, offset);	//move the calendar day to the requested cell
		return cast(column.getValueClass(), cellCalendar);	//return the calendar representing the date of the cell
	}

	/**Sets the cell value at the given row and column.
	@param <C> The type of cell values in the given column.
	@param rowIndex The zero-based row index.
	@param column The column for which a value should be returned.
	@param newCellValue The value to place in the cell at the given row and column, or <code>null</code> if there should be no value in that cell.
	@exception IndexOutOfBoundsException if the given row index represents an invalid location for the table.
	@exception IllegalArgumentException if the given column is not one of this table's columns.
	*/
	public <C> void setCellValue(final int rowIndex, final TableColumnModel<C> column, final C newCellValue)
	{
		throw new UnsupportedOperationException("Calendar days are read-only.");
	}

	/**A day-of-week column in a calendar month table.
	Each cell contains a {@link Date} value.
	@author Garret Wilson
	*/
	public class WeekDayTableColumnModel extends DefaultTableColumnModel<Calendar>
	{
	
		/**The physical index of the day of the week relative to the first day of the week.*/
		private final int index;

			/**@return The physical index of the day of the week relative to the first day of the week.*/
			public int getIndex() {return index;}

		/**@return <code>true</code> if this model has label information, such as an icon or a label string.*/
		public boolean hasLabel()
		{
			return true;	//a week day table column always has a label
		}

		/**Determines the text of the label.
		This version returns a representation of the day of the week if no label is specified.
		@return The label text, or <code>null</code> if there is no label text.
		@exception MissingResourceException if there was an error loading the value from the resources.
		@see #getLabelResourceKey()
		*/
		public String getLabel() throws MissingResourceException
		{
			String label=super.getLabel();	//get the specified label
			if(label==null)	//if no label is specified
			{
				final Calendar columnCalendar=getMonthCalendar();	//get a clone of the month calendar
				final int dayOfWeek=((columnCalendar.getFirstDayOfWeek()+getIndex()-1)%WEEK_DAY_COUNT)+1;	//find out which day of the week this column represents
				columnCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);	//set the calendar to the correct day of the week, without caring the actual date
				label=new SimpleDateFormat("E", getSession().getLocale()).format(columnCalendar.getTime());	//format the day of the week for the label
			}
			return label;	//return the label for this column
		}

		/**Session constructor.
		@param session The Guise session that owns this column.
		@param index The physical index of the day of the week relative to the first day of the week.
		@exception NullPointerException if the given session is <code>null</code>.
		@exception IllegalArgumentException if the given index is less than zero, or greater than or equal to the number of days in a week.
		*/
		public WeekDayTableColumnModel(final GuiseSession session, final int index)
		{
			super(session, Calendar.class);	//construct the parent class
			this.index=checkIndexBounds(index, 0, WEEK_DAY_COUNT);	//make sure the index is within bounds
		}
		
	}
	
}
