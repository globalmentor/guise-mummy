package com.javaguise.component;


import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.util.Debug;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.javaguise.component.Table.CellRepresentationStrategy;
import com.javaguise.component.Table.DefaultCellMessageModel;
import com.javaguise.component.Table.DefaultCellValueModel;
import com.javaguise.component.layout.Flow;
import com.javaguise.component.layout.FlowLayout;
import com.javaguise.converter.Converter;
import com.javaguise.event.AbstractGuisePropertyChangeListener;
import com.javaguise.event.ActionEvent;
import com.javaguise.event.ActionListener;
import com.javaguise.event.GuisePropertyChangeEvent;
import com.javaguise.event.GuisePropertyChangeListener;
import com.javaguise.model.ActionModel;
import com.javaguise.model.CalendarMonthTableModel;
import com.javaguise.model.DefaultValueModel;
import com.javaguise.model.TableColumnModel;
import com.javaguise.model.TableModel;
import com.javaguise.model.ValueModel;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.ValidationException;

/**Control that allows selection of a date.
@author Garret Wilson
*/
public class CalendarControl extends AbstractContainer<CalendarControl> implements ValueControl<Calendar, CalendarControl>
{
	
	private int getMonthCount() {return 2;}	//TODO update to allow modification

	/**@return The data model used by this component.*/
	@SuppressWarnings("unchecked")
	public ValueModel<Calendar> getModel() {return (ValueModel<Calendar>)super.getModel();}

	/**The container containing the controls.*/
	private Container<?> controlContainer;
	
		/**The container containing the controls.*/
		public Container<?> getControlContainer() {return controlContainer;}

	/**The container containing the calendars.*/
	private Container<?> calendarContainer;
	
		/**The container containing the calendars.*/
		public Container<?> getCalendarContainer() {return calendarContainer;}

	/**The list of calendar table components.*/
	private final List<Table> calendarTables=new CopyOnWriteArrayList<Table>();
	
		/**@return An iterator to the calendar table components.*/
		public Iterator<Table> getCalendarTables() {return calendarTables.iterator();}
	
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
		this(session, id, new DefaultValueModel<Calendar>(session, Calendar.class));	//construct the class with a default value model
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public CalendarControl(final GuiseSession session, final ValueModel<Calendar> model)
	{
		this(session, null, model);	//construct the component, indicating that a default ID should be used		
	}

//TODO del	protected final GuisePropertyChangeListener<CalendarMonthTableModel, Calendar> calendarChangeListener;
//TODO del	protected final ActionListener<CalendarMonthTableModel> calendarActionListener;

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CalendarControl(final GuiseSession session, final String id, final ValueModel<Calendar> model)
	{
		super(session, id, new FlowLayout(session, Flow.PAGE), model);	//construct the parent class flowing along the page
		controlContainer=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));	//create the control panel
		add(controlContainer);	//add the control panel
		calendarContainer=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));	//create the calendar panel
		add(calendarContainer);	//add the calendar panel
		updateCalendars();	//update the calendars
	}

	/**Updates the calendars on the calendar panel.*/
	protected void updateCalendars()
	{
		final Container<?> calendarContainer=getCalendarContainer();	//get the calendar container
		calendarContainer.clear();	//remove all calendars from the container
		final GuiseSession session=getSession();	//get the current session
		final CellRepresentationStrategy<Calendar> dayRepresentationStrategy=createDayRepresentationStrategy();	//create a strategy for representing the days in the month calendar cells
		final Calendar calendar=Calendar.getInstance(session.getLocale());	//TODO listen for a change in locale and update the calendars
		for(int monthIndex=0; monthIndex<getMonthCount(); ++monthIndex)	//for each month
		{
			final CalendarMonthTableModel calendarMonthTableModel=new CalendarMonthTableModel(session, calendar);	//create a table model for this month
			final Table calendarMonthTable=new Table(session, calendarMonthTableModel);	//create a table to hold the calendar month
			for(final TableColumnModel<?> tableColumn:calendarMonthTable.getModel().getColumns())	//for each table column
			{
				calendarMonthTable.setCellRepresentationStrategy((TableColumnModel<Calendar>)tableColumn, dayRepresentationStrategy);	//install the representation strategy for this column
			}
			calendarContainer.add(calendarMonthTable);	//add the month table to the calendar container
			calendarTables.add(calendarMonthTable);	//add this table to the list of calendar tables
			calendar.add(Calendar.MONTH, 1);	//go to the next month
		}
		
		
		
		
	}

	/**Creates a representation strategy for each cell in a calendar.
	This version returns a new instance of {@link DayRepresentationStrategy}.
	@return a representation strategy for each cell in a calendar.
	@see DayRepresentationStrategy
	*/
	protected CellRepresentationStrategy<Calendar> createDayRepresentationStrategy()
	{
		return new DayRepresentationStrategy();	//return a new day representation strategy
	}
	
	/**A cell representation strategy for calendar days.
	A link will be generated using the day of the month as its label.
	The message's ID will be in the form "<var>tableID</var>.time<var>absoluteTimeHex</var>".
	@see Link
	@author Garret Wilson
	*/
	protected class DayRepresentationStrategy implements CellRepresentationStrategy<Calendar>
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
		public <C extends Calendar> Component<?> createComponent(final Table table, final TableModel model, final int rowIndex, final TableColumnModel<C> column, final boolean editable, final boolean selected, final boolean focused)
		{
			final GuiseSession session=getSession();	//get the session
			final Calendar calendar=model.getCellValue(rowIndex, column);	//get the calendar for this cell
			final long time=calendar.getTimeInMillis();	//get the time of the cell in milliseconds
			final String id=table.createID("time"+Long.toHexString(time));	//create an ID for the new component
			final Link link=new Link(session, id);	//create a link for this cell
			link.getModel().setLabel(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));	//set the label of the link to the day of the month
			link.getModel().addActionListener(new ActionListener<ActionModel>()	//create a listener to listen for calendar actions
					{
						public void actionPerformed(ActionEvent<ActionModel> actionEvent)	//when a day is selected
						{
							try
							{
								CalendarControl.this.getModel().setValue(calendar);	//change the control's value to the calendar for this cell
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
