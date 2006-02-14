package com.guiseframework.component;

import java.util.Date;

import com.guiseframework.GuiseSession;
import com.guiseframework.event.AbstractGuisePropertyChangeListener;
import com.guiseframework.event.GuisePropertyChangeEvent;
import com.guiseframework.model.DefaultValueModel;
import com.guiseframework.model.ValueModel;
import com.guiseframework.validator.ValidationException;

/**A dialog frame meant for accepting entry of a date.
The dialog is automatically closed when a date is selected.
@author Garret Wilson
*/
public class CalendarDialogFrame extends AbstractDialogFrame<Date, CalendarDialogFrame>
{

	/**@return The single calendar control child component.*/
	public CalendarControl getContent() {return (CalendarControl)super.getContent();}

	/**Sets the single child component.
	This method throws an exception, as the content of a calendar dialog frame cannot be modified. 
	@param newContent The single child component, or <code>null</code> if this frame does not have a child component.
	@exception UnsupportedOperationException because the content cannot be changed.
	*/
	public void setContent(final Component<?> newContent)
	{
		throw new UnsupportedOperationException("Cannot change content component of "+getClass());
	}

	/**Session constructor.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public CalendarDialogFrame(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and default date constructor.
	@param session The Guise session that owns this component.
	@param defaultDate The default selected date, or <code>null</code> if there is no default selected date.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public CalendarDialogFrame(final GuiseSession session, final Date defaultDate)
	{
		this(session, (String)null, defaultDate);	//construct the component, indicating that a default ID should be used
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public CalendarDialogFrame(final GuiseSession session, final ValueModel<Date> model)
	{
		this(session, (String)null, model);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CalendarDialogFrame(final GuiseSession session, final String id)
	{
		this(session, id, (Date)null);	//construct the class with no default date
	}

	/**Session, ID, and default date constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param defaultDate The default selected date, or <code>null</code> if there is no default selected date.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CalendarDialogFrame(final GuiseSession session, final String id, final Date defaultDate)
	{
		this(session, id, new DefaultValueModel<Date>(session, Date.class, defaultDate));	//use a default value model
	}

	/**Session, ID, model, and component constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public CalendarDialogFrame(final GuiseSession session, final String id, final ValueModel<Date> model)
	{
		super(session, id, model, new CalendarControl(session));	//construct the parent class with a calendar control
		final CalendarControl calendarControl=getContent();	//get a reference to the calendar control content component
		final Date defaultDate=model.getDefaultValue();	//see if there is a default date
		if(defaultDate!=null)	//if there is a default date
		{
			try
			{
				calendarControl.getModel().setValue(defaultDate);	//select the default date TODO pass this to the calendar control as a default date
			}
			catch(final ValidationException validationException)
			{
				throw new AssertionError(validationException);	//TODO fix
			}
		}
		calendarControl.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<Date>()	//listen for the calendar control value changing
				{
					public void propertyChange(final GuisePropertyChangeEvent<Date> propertyChangeEvent)	//if the calendar control value changed
					{
						try
						{
							final Date newDate=propertyChangeEvent.getNewValue();	//get the new date
							getModel().setValue(newDate);	//update our own value
							if(newDate!=null)	//if a date was selected
							{
								close();	//close the frame
							}
						}
						catch(final ValidationException validationException)
						{
							throw new AssertionError(validationException);	//TODO fix
						}
					}
				});
	}

}
