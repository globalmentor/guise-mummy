package com.guiseframework.model;

import java.text.DateFormat;
import java.util.*;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.IntegerUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.util.CalendarConstants.*;

import com.garretwilson.util.Debug;
import com.guiseframework.GuiseSession;
import com.guiseframework.converter.AbstractDateStringLiteralConverter;
import com.guiseframework.converter.DateStringLiteralStyle;
import com.guiseframework.event.AbstractGuisePropertyChangeListener;
import com.guiseframework.event.GuisePropertyChangeEvent;

/**A table model representing the days of a calendar month.
Each cell contains a {@link Date} value.
@author Garret Wilson
*/
public class CalendarMonthTableModel extends AbstractTableModel	//TODO set the model to read-only
{
	
//TODO switch to using calendar.getActualMaximum(calobject.DAY_OF_WEEK) or something to determine the days in the week, rather than a constant
	
	/**The column style bound property.*/
	public final static String COLUMN_LABEL_DATE_STYLE_PROPERTY=getPropertyName(CalendarMonthTableModel.class, "columnLabelStyle");
	/**The date bound property.*/
	public final static String DATE_PROPERTY=getPropertyName(CalendarMonthTableModel.class, "date");

	/**The number of days this calendar should be offset left (negative) or right (positive) so that the days align with the correct day-of-the-week column.*/
	private int dayOffset=0;

		/**@return The number of days this calendar should be offset left (negative) or right (positive) so that the days align with the correct day-of-the-week column.*/
		protected int getDayOffset() {return dayOffset;}

	/**The number of rows in this table.*/
	private int rowCount=0;	

		/**@return The number of rows in this table.*/
		public int getRowCount() {return rowCount;}

	/**The calendar representing the first day of the month.*/
	private Calendar monthCalendar;

		/**@return A clone of the calendar representing the first day of the month.*/
		protected Calendar getMonthCalendar() {return (Calendar)monthCalendar.clone();}

	/**The date this calendar represents.*/
	private Date date;

		/**@return The date this calendar represents.*/
		public Date getDate() {return (Date)date.clone();}

		/**Sets the date this calendar represents.
		A copy will be made of the date before it is stored.
		This is a bound property.
		@param newDate The date this calendar is to represent.
		@exception NullPointerException if the given date is <code>null</code>.
		@see #DATE_PROPERTY
		*/
		public void setDate(final Date newDate)
		{
			if(!date.equals(checkNull(newDate, "Date cannot be null.")))	//if the value is really changing
			{
				final Date oldDate=date;	//get the old value
				date=(Date)newDate.clone();	//clone the new date and actually change the value
				updateModel();	//update the model based upon the new value
				firePropertyChange(DATE_PROPERTY, oldDate, newDate);	//indicate that the value changed
			}
		}

	/**The style of the column label.*/
	private DateStringLiteralStyle columnLabelDateStyle=DateStringLiteralStyle.DAY_OF_WEEK;

		/**@return The style of the column label.*/
		public DateStringLiteralStyle getColumnLabelDateStyle() {return columnLabelDateStyle;}

		/**Sets the style of the column label.
		Note that this property is experimental, and may eventually be replaced with a style specification in the table component rather than the table model.
		This is a bound property.
		@param newColumnLabelStyle The style of the column label.
		@exception NullPointerException if the given label style is <code>null</code>.
		@see #COLUMN_LABEL_DATE_STYLE_PROPERTY
		*/
		public void setColumnLabelDateStyle(final DateStringLiteralStyle newColumnLabelStyle)
		{
			if(columnLabelDateStyle!=newColumnLabelStyle)	//if the value is really changing
			{
				final DateStringLiteralStyle oldColumnLabelStyle=columnLabelDateStyle;	//get the old value
				columnLabelDateStyle=checkNull(newColumnLabelStyle, "Column label style cannot be null.");	//actually change the value
				updateColumnLabelDateFormat();	//update the column label date format object based upon the new style
				firePropertyChange(COLUMN_LABEL_DATE_STYLE_PROPERTY, oldColumnLabelStyle, newColumnLabelStyle);	//indicate that the value changed
			}
		}

	/**The date format object for formatting the column labels.*/
	private DateFormat columnLabelDateFormat;

		/**@return The date format object for formatting the column labels.*/
		protected DateFormat getColumnLabelDateFormat() {return columnLabelDateFormat;}

	/**Updates the model based upon the current calendar. The column label date format is also updated.
	@see #updateColumnLabelDateFormat()
	*/
	protected void updateModel()
	{
		monthCalendar=Calendar.getInstance(getSession().getLocale());	//create a month calendar for the current locale
//TODO del when works		monthCalendar=(Calendar)calendar.clone();	//make a copy of the calendar
		monthCalendar.setTime(getDate());	//set the calendar to the correct time
		monthCalendar.set(Calendar.DAY_OF_MONTH, 1);	//set the month calendar to the first day of the month
		monthCalendar.set(Calendar.HOUR_OF_DAY, 0);	//set the hour to midnight
		monthCalendar.set(Calendar.MINUTE, 0);	//set the minute to zero
		monthCalendar.set(Calendar.SECOND, 0);	//set the second to zero
		monthCalendar.set(Calendar.MILLISECOND, 0);	//set the millisecond to zero
//TODO fix Debug.trace("updating calendar model in locale", getSession().getLocale());
//	TODO fix Debug.trace("ready to update model with calendar", monthCalendar);
		final int firstDayOfWeek=monthCalendar.getFirstDayOfWeek();	//get the first day of the week for this calendar
//TODO del Debug.trace("first day of week", firstDayOfWeek);
		final int dayOfWeek=monthCalendar.get(Calendar.DAY_OF_WEEK);	//get the day of the week of the first day of the month
//TODO del Debug.trace("day of week", dayOfWeek);
		dayOffset=firstDayOfWeek-dayOfWeek;	//calculate the offset for the first day of the month, and all days
		if(dayOfWeek<firstDayOfWeek)	//if the day of the week is before the first day of the week
		{
			dayOffset-=WEEK_DAY_COUNT;		//keep from going backwards too far TODO there should be a better way to do this using modulus
		}
//TODO del Debug.trace("day offset", dayOffset);
		rowCount=(int)Math.ceil((monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)-dayOffset)/(double)WEEK_DAY_COUNT);	//find out how many partial rows are used, taking into account the day offset
		Debug.trace("row count", rowCount);
		updateColumnLabelDateFormat();	//update the date format object for formatting column labels
	}

	/**Updates the column label date format based upon the column label date style and current locale.
	@see #getColumnLabelDateStyle()
	*/
	protected void updateColumnLabelDateFormat()
	{
		columnLabelDateFormat=AbstractDateStringLiteralConverter.createDateFormat(getColumnLabelDateStyle(), null, getSession().getLocale());	//create a new date format based upon the style and locale		
	}

	/**Session constructor for current month using the session locale.
	@param session The Guise session that owns this model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public CalendarMonthTableModel(final GuiseSession session)
	{
		this(session, new Date());	//construct the class using the current date
	}

	/**Session and date constructor.
	@param session The Guise session that owns this model.
	@param date The date this calendar is to represent.
	@exception NullPointerException if the given session and/or date is <code>null</code>.
	*/
	public CalendarMonthTableModel(final GuiseSession session, final Date date)	//TODO decide if we want to allow a calendar with another locale to be set, because right now we change calendars automatically
	{
		super(session);	//construct the parent class
		for(int i=0; i<WEEK_DAY_COUNT; ++i)	//for each week day index (the indices are constant, regardless of with which day of the week the locale starts)
		{
			addColumn(new WeekDayTableColumnModel(session, i));	//add a new week day table column
		}
		this.date=checkNull(date, "Date cannot be null");
		updateModel();	//update the model to match the initial month calendar
			//TODO important: this is a memory leak---make sure we uninstall the listener when the session goes away
		session.addPropertyChangeListener(GuiseSession.LOCALE_PROPERTY, new AbstractGuisePropertyChangeListener<Locale>()	//listen for the session locale changing
				{
					public void propertyChange(GuisePropertyChangeEvent<Locale> propertyChangeEvent)	//if the locale changes
					{
						updateModel();	//update the model based upon the new locale
					}			
				});
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
		return cast(column.getValueClass(), cellCalendar.getTime());	//return the calendar time representing the date of the cell
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
	public class WeekDayTableColumnModel extends DefaultTableColumnModel<Date>
	{
	
		/**The physical index of the day of the week relative to the first day of the week.*/
		private final int index;

			/**@return The physical index of the day of the week relative to the first day of the week.*/
			public int getIndex() {return index;}

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
				label=getColumnLabelDateFormat().format(columnCalendar.getTime());	//format the day of the week for the label
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
			super(session, Date.class);	//construct the parent class
			this.index=checkIndexBounds(index, 0, WEEK_DAY_COUNT);	//make sure the index is within bounds
		}		
	}
	
}
