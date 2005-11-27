package com.javaguise.component;

import static com.garretwilson.lang.ObjectUtilities.checkNull;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.garretwilson.lang.ObjectUtilities;
import com.javaguise.component.layout.Flow;
import com.javaguise.component.layout.FlowLayout;
import com.javaguise.converter.Converter;
import com.javaguise.event.AbstractGuisePropertyChangeListener;
import com.javaguise.event.GuisePropertyChangeEvent;
import com.javaguise.model.CalendarMonthTableModel;
import com.javaguise.model.DefaultValueModel;
import com.javaguise.model.ValueModel;
import com.javaguise.session.GuiseSession;

/**Control that allows selection of a date.
@author Garret Wilson
*/
public class CalendarControl extends AbstractContainer<CalendarControl> implements ValueControl<Calendar, CalendarControl>
{
	
	private int getMonthCount() {return 2;}	//TODO update to allow modification

	/**@return The data model used by this component.*/
	@SuppressWarnings("unchecked")
	public ValueModel<Calendar> getModel() {return (ValueModel<Calendar>)super.getModel();}

	
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

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CalendarControl(final GuiseSession session, final String id, final ValueModel<Calendar> model)
	{
		super(session, id, new FlowLayout(session, Flow.LINE), model);	//construct the parent class

		final Calendar calendar=Calendar.getInstance(session.getLocale());	//TODO listen for a change in locale and update the calendars
		for(int i=0; i<getMonthCount(); ++i)
		{
				//TODO fix; tidy
			final CalendarMonthTableModel calendarMonthTableModel=new CalendarMonthTableModel(session, calendar);
			final Table calendarMonthTable=new Table(session, calendarMonthTableModel);
			add(calendarMonthTable);
			calendarTables.add(calendarMonthTable);
			calendar.add(Calendar.MONTH, 1);
		}
	}

}
