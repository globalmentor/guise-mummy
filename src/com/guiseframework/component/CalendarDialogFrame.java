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
import java.util.Date;

import com.globalmentor.beans.*;
import com.guiseframework.model.*;

/**A dialog frame meant for accepting entry of a date.
The dialog is automatically closed when a date is selected.
@author Garret Wilson
*/
public class CalendarDialogFrame extends AbstractDialogFrame<Date>
{

	/**@return The single calendar control child component.*/
	public CalendarControl getContent() {return (CalendarControl)super.getContent();}

	/**Sets the single child component.
	This method throws an exception, as the content of a calendar dialog frame cannot be modified. 
	@param newContent The single child component, or <code>null</code> if this frame does not have a child component.
	@exception UnsupportedOperationException because the content cannot be changed.
	*/
	public void setContent(final Component newContent)
	{
		throw new UnsupportedOperationException("Cannot change content component of "+getClass());
	}

	/**Default constructor with no date.*/
	public CalendarDialogFrame()
	{
		this((Date)null);	//construct the class with no default date
	}

	/**Default date constructor.
	@param defaultDate The default selected date, or <code>null</code> if there is no default selected date.
	*/
	public CalendarDialogFrame(final Date defaultDate)
	{
		this(new DefaultValueModel<Date>(Date.class, defaultDate));	//use a default value model
	}

	/**Value model constructor.
	@param valueModel The component value model.
	@exception NullPointerException if the given model is <code>null</code>.
	*/
	public CalendarDialogFrame(final ValueModel<Date> valueModel)
	{
		super(valueModel, new CalendarControl());	//construct the parent class with a calendar control
		final CalendarControl calendarControl=getContent();	//get a reference to the calendar control content component
		final Date defaultDate=valueModel.getDefaultValue();	//see if there is a default date
		if(defaultDate!=null)	//if there is a default date
		{
			try
			{
				calendarControl.setValue(defaultDate);	//select the default date TODO pass this to the calendar control as a default date
			}
			catch(final PropertyVetoException propertyVetoException)
			{
//TODO fix				throw new AssertionError(validationException);	//TODO fix
			}
		}
		calendarControl.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Date>()	//listen for the calendar control value changing
				{
					public void propertyChange(final GenericPropertyChangeEvent<Date> propertyChangeEvent)	//if the calendar control value changed
					{
						try
						{
							final Date newDate=propertyChangeEvent.getNewValue();	//get the new date
							setValue(newDate);	//update our own value
							if(newDate!=null)	//if a date was selected
							{
								close();	//close the frame
							}
						}
						catch(final PropertyVetoException propertyVetoException)
						{
//TODO fix							throw new AssertionError(validationException);	//TODO fix
						}
					}
				});
	}

}
