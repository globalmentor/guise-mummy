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

import com.globalmentor.beans.*;
import static com.globalmentor.java.Characters.*;

import static com.globalmentor.util.Calendars.*;
import com.globalmentor.util.Debug;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.*;
import com.guiseframework.converter.*;
import com.guiseframework.event.ActionEvent;
import com.guiseframework.event.ActionListener;
import com.guiseframework.model.*;

import static com.guiseframework.theme.Theme.*;

/**Control that allows selection of a date and/or a time, providing separate inputs for date and time with the option of a calendar popup.
This implementation always represents the date and time in terms of the current session's time zone.
This implementation does not allow smaller than one-second precision.
@author Garret Wilson
*/
public class DateTimeControl extends AbstractLayoutValueControl<Date>	//TODO refactor this and code from this and RepositoryResourceURIControl into abstract functionality for decorated controls
{

	/**The control containing the date.*/
	private final TextControl<Date> dateControl;

		/**@return The control containing the date.*/
		protected TextControl<Date> getDateControl() {return dateControl;}

	/**The button allowing selection of the date.*/
	private final ToolButton calendarButton;

		/**The button allowing selection of the date.*/
		protected ToolButton getCalendarButton() {return calendarButton;}

	/**The control containing the date.*/
	private final TextControl<Date> timeControl;

		/**@return The control containing the date.*/
	public TextControl<Date> getTimeControl() {return timeControl;}

	/**@return Whether the current value represented in the control has a specified time component.*/
	public boolean hasTime()
	{
		return getTimeControl().getValue()!=null;	//if there is a value in the time control, the represented value has a time component
	}

	/**The property change listener that updates the date controls when a property changes.*/
	protected final GenericPropertyChangeListener<?> updateDateControlsPropertyChangeListener;

	/**The property change listener that updates the value when a property changes.*/
	protected final GenericPropertyChangeListener<?> updateValuePropertyChangeListener;

	/**Default constructor with a default data model.*/
	public DateTimeControl()
	{
		this(new DefaultValueModel<Date>(Date.class));	//construct the class with a default value model
	}

	/**Value model constructor.
	@param valueModel The component value model.
	@exception NullPointerException if the given value model is <code>null</code>.
	*/
	public DateTimeControl(final ValueModel<Date> valueModel)
	{
		super(new FlowLayout(Flow.LINE), valueModel);	//construct the parent class flowing along the line
			//date
		dateControl=new TextControl<Date>(Date.class);	//create a control for the date
		dateControl.setLabel(LABEL_DATE);	//set the date control label
		final Converter<Date, String> dateConverter=new DateStringLiteralConverter(DateStringLiteralStyle.SHORT, null);	//get a converter to display the date in a numeric representation
		dateControl.setConverter(dateConverter);	//set the date converter
//TODO del		dateControl.setValidator(new ValueRequiredValidator<Date>());	//require a date
		dateControl.setColumnCount(10);	//provide for sufficient characters for the most common date format
		addComponent(dateControl);	//add the date control
		calendarButton=new ToolButton(LABEL_CALENDAR+HORIZONTAL_ELLIPSIS_CHAR, GLYPH_CALENDAR);	//create a tool button for the calendar
		calendarButton.setLabelDisplayed(false);
		calendarButton.addActionListener(new ActionListener() 
				{
					public void actionPerformed(final ActionEvent actionEvent)	//if the calendar button is clicked
					{
						final Date date=dateControl.getValue();	//get the current date value, if any
						final CalendarDialogFrame calendarDialogFrame=new CalendarDialogFrame(date);	//create a new calendar popup for the current date
//TODO fix						calendarDialogFrame.setLabel("Select a date");
						calendarDialogFrame.setRelatedComponent(calendarButton);	//associate the popup with the button
						calendarDialogFrame.open();	//show the calendar popup
						calendarDialogFrame.open(new AbstractGenericPropertyChangeListener<Frame.Mode>()	//ask for the date to be selected
								{		
									public void propertyChange(final GenericPropertyChangeEvent<Frame.Mode> propertyChangeEvent)	//when the modal dialog mode changes
									{
										final Date newDate=calendarDialogFrame.getValue();	//get the value of the frame's model
										if(newDate!=null)	//if a new date was selected (i.e. the calendar dialog frame was not closed without a selection)
										{
											try
											{
//Debug.trace("ready to put new date in control:", newDate);
												dateControl.setValue(newDate);	//show the date in the date control
											}
											catch(final PropertyVetoException propertyVetoException)	//we should never have a problem selecting a date
											{
												throw new AssertionError(propertyVetoException);
											}
										}
									}
								});
					}
				});
		addComponent(calendarButton);	//add the calendar button
			//time
		timeControl=new TextControl<Date>(Date.class);	//create a control for the time
		timeControl.setLabel(LABEL_TIME);	//set the date control label
		final Converter<Date, String> timeConverter=new DateStringLiteralConverter(null, TimeStringLiteralStyle.SHORT);	//get a converter to display the time in a numeric representation
		timeControl.setConverter(timeConverter);	//set the time converter
//TODO del		dateControl.setValidator(new ValueRequiredValidator<Date>());	//require a date
		timeControl.setColumnCount(8);	//provide for sufficient characters for the time
		addComponent(timeControl);	//add the time control
//TODO del		updateYearControl();	//create and install an appropriate year control
		updateDateControlsPropertyChangeListener=new AbstractGenericPropertyChangeListener<Object>()	//create a property change listener to update the calendars
		{
			public void propertyChange(final GenericPropertyChangeEvent<Object> propertyChangeEvent)	//if the model value value changed
			{
				updateDateControls();	//update the date controls based upon the new selected date
			}
		};
		updateValuePropertyChangeListener=new AbstractGenericPropertyChangeListener<Object>()	//create a property change listener to update the value
		{
			public void propertyChange(final GenericPropertyChangeEvent<Object> propertyChangeEvent)	//if the model value changed
			{
				if(!updatingDateControls)	//if we're not manually updating the controls
				{
					updatingDateControls=true;	//show that we're updating the calendars
					try
					{
						Date date=dateControl.getValue();	//get the date value, if there is one
						if(date!=null)	//if there is a date value
						{
//Debug.trace("got date", date, "milliseconds", date.getTime());
							final GuiseSession session=getSession();	//get the current session
							final Locale locale=session.getLocale();	//get the current locale
							final TimeZone timeZone=session.getTimeZone();	//get the current time zone
							final Calendar dateCalendar=Calendar.getInstance(timeZone, locale);	//get a calendar to manipulate the date
							dateCalendar.setTime(date);	//set the date in the calendar
							final Date time=timeControl.getValue();	//get the time date
							if(time!=null)	//if there is a time, we'll need to update our date
							{
//Debug.trace("got time", time, "milliseconds", time.getTime());
								final Calendar timeCalendar=Calendar.getInstance(timeZone, locale);	//get a calendar to manipulate the time
								timeCalendar.setTime(time);	//set the time in the calendar
								setTime(dateCalendar, timeCalendar);	//set the time of the date calendar
							}
							else	//if there is no time
							{
								clearTime(dateCalendar);	//remove the time from the date calendar
							}
							date=dateCalendar.getTime();	//update the date to include or not include the time
//Debug.trace("using date", date, "milliseconds", date.getTime());
						}
						setValue(date);	//update our value with the date
					}
					catch(final PropertyVetoException propertyVetoException)	//the control might have a validator, but currently we can't do much about it at this point
					{
						Debug.warn(propertyVetoException);
					}
					finally
					{
						updatingDateControls=false;	//show that we're no longer updating controls
					}
				}
			}
		};
		addPropertyChangeListener(VALUE_PROPERTY, updateDateControlsPropertyChangeListener);	//update the controls if the selected date changes
		updateDateControls();	//update the date controls
		dateControl.addPropertyChangeListener(VALUE_PROPERTY, updateValuePropertyChangeListener);	//update the value if the date control changes
		timeControl.addPropertyChangeListener(VALUE_PROPERTY, updateValuePropertyChangeListener);	//update the value if the time control changes
	}

	/**Called when the enabled property changes.
	This version updates the enabled status of the child controls.
	@param oldValue The old value of the property.
	@param newValue The new value of the property.
	*/
	protected void enabledPropertyChange(final boolean oldValue, final boolean newValue)
	{
		dateControl.setEnabled(newValue);
		calendarButton.setEnabled(newValue);
		timeControl.setEnabled(newValue);
		super.enabledPropertyChange(oldValue, newValue);	//always perform the default functionality
	}

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
				final Date date=getValue();
				if(date!=null)	//if there is a date
				{
//Debug.trace("updating controls with date", date, "milliseconds", date.getTime());
					final GuiseSession session=getSession();	//get the current session
					final Locale locale=session.getLocale();	//get the current locale
					final TimeZone timeZone=session.getTimeZone();	//get the current time zone
					final Calendar calendar=Calendar.getInstance(timeZone, locale);	//get a calendar to manipulate the date
					calendar.setTime(date);	//set the calendar's date
					clearTime(calendar);	//clear the time from the calendar
					dateControl.setValue(new Date(calendar.getTimeInMillis()));	//set the date control, creating a new Date because the API doesn't specify that the JVM will do so
					calendar.setTime(date);	//set the calendar's time
					clearDate(calendar);	//clear the date from the calendar
					timeControl.setValue(new Date(calendar.getTimeInMillis()));	//set the time control, creating a new Date because the API doesn't specify that the JVM will do so
				}
				else	//if there is no date
				{
					dateControl.clearValue();	//clear the controls
					timeControl.clearValue();
				}
			}
			catch(final PropertyVetoException propertyVetoException)	//we should never have a problem selecting a date
			{
				throw new AssertionError(propertyVetoException);
			}
			finally
			{
				updatingDateControls=false;	//show that we're no longer updating controls
			}
		}
	}

}
