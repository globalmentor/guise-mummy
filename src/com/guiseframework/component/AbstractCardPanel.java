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

import com.globalmentor.beans.AbstractGenericPropertyChangeListener;
import com.globalmentor.beans.GenericPropertyChangeEvent;

import com.guiseframework.Bookmark;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.CardLayout;
import com.guiseframework.event.NavigationEvent;
import com.guiseframework.event.NavigationListener;

/**An abstract panel with a card layout.
The component valid status is updated before a change in the {@link #VALUE_PROPERTY} or the {@link #VALIDATOR_PROPERTY} is fired. 
If bookmarks are enabled, this component supports bookmarks using this component's name and the name of any selectected cards.
@author Garret Wilson
@see CardLayout
*/
public abstract class AbstractCardPanel extends AbstractListSelectContainerControl implements Panel, CardControl, NavigationListener
{

		//TODO make sure we listen for enabled status changing on the layout and send an index enabled property change, maybe

	/**@return The layout definition for the container.*/
	@SuppressWarnings("unchecked")
	public CardLayout getLayout() {return (CardLayout)super.getLayout();}

	/**Whether the component has bookmarks enabled.*/
	private boolean bookmarkEnabled=false;

		/**@return Whether the component has bookmarks enabled.*/
		public boolean isBookmarkEnabled() {return bookmarkEnabled;}

		/**Sets whether the component is has bookmarks enabled.
		This is a bound property of type <code>Boolean</code>.
		@param newBookmarkEnabled <code>true</code> if the component should support bookmarking, else <code>false</code>.
		@see #BOOKMARK_ENABLED
		*/
		public void setBookmarkEnabled(final boolean newBookmarkEnabled)
		{
			if(bookmarkEnabled!=newBookmarkEnabled)	//if the value is really changing
			{
				final boolean oldBookmarkEnabled=bookmarkEnabled;	//get the current value
				bookmarkEnabled=newBookmarkEnabled;	//update the value
				firePropertyChange(BOOKMARK_ENABLED_PROPERTY, Boolean.valueOf(oldBookmarkEnabled), Boolean.valueOf(newBookmarkEnabled));
				if(newBookmarkEnabled)	//if bookmarks have just been enabled
				{
					updateBookmark();	//update the bookmark, now that that bookmarks are enabled					
				}
			}
		}
	
	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	protected AbstractCardPanel(final CardLayout layout)
	{
		super(layout);	//construct the parent class
		addPropertyChangeListener(VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Component>()	//listen for the value changing and set bookmarks in response
				{
					public void propertyChange(final GenericPropertyChangeEvent<Component> propertyChangeEvent)	//if the value changes
					{
//TODO del Log.trace("value changed to ", propertyChangeEvent.getNewValue(), "and ready to update bookmark");
						updateBookmark();	//update the bookmark, now that the property has changed
					}
				});
	}

	/**Updates the bookmark if bookmarks are enabled.
	@see #isBookmarkEnabled()
	*/
	protected void updateBookmark()
	{
//TODO del Log.trace("ready to update bookmarks, bookmarks enabled", isBookmarkEnabled(), "name", getName());
		if(isBookmarkEnabled())	//if bookmarks are enabled
		{
			final String name=getName();	//get the component name
			if(name!=null)	//if this component has a name
			{
				final GuiseSession session=getSession();	//get the current session
				final Bookmark oldBookmark=session.getBookmark();	//get the current bookmark
				final Bookmark bookmark=oldBookmark!=null ? oldBookmark : new Bookmark();	//create a new bookmark if there is no bookmark
				final Component selectedCard=getValue();	//get the current selected card
				final String newBookmarkValue=selectedCard!=null ? selectedCard.getName() : "";	//the bookmark value is the name of the new card, or "" if no value is selected
				final Bookmark newBookmark;	//we'll determine the new bookmark to set
				if(newBookmarkValue!=null)	//if we know the new bookmark value
				{
					newBookmark=bookmark.setParameter(name, newBookmarkValue);	//set the parameter to match the new card
				}
				else	//if we don't know a new value to use
				{
					newBookmark=bookmark.removeParameter(name);	//remove the parameter altogether
				}
//TODO del Log.trace("ready to set new bookmark:", newBookmark);
				session.setBookmark(newBookmark);	//update the bookmark
			}
		}		
	}

	/**Called when navigation occurs.
	This implementation changes the card based upon the bookmark if bookmarks are enabled for this component.
	A bookmarked component is not selected if its constraints indicate disabled and/or not displayed.
	@param navigationEvent The event indicating navigation details.
	@see #getComponent(Bookmark)
	*/
	public void navigated(final NavigationEvent navigationEvent)
	{
		if(isBookmarkEnabled())	//if bookmarks are enabled
		{
			final Bookmark bookmark=navigationEvent.getBookmark();	//get the navigation bookmark, if any
//TODO del Log.trace("card panel ready to change to bookmark:", bookmark, "old component", getValue(), "old index", getSelectedIndex());
			final Component component=getComponent(bookmark);	//get the component from the bookmark
//TODO del Log.trace("new component:", component, "new index", indexOf(component));
			if(component!=null && isEnabled(component) && isDisplayed(component))	//if an enabled, displayed component is specified
			{
				try
				{
					setValue(component);	//change to that component
					
					//TODO fix a way to force the bookmark if a null bookmark was sent
					
//TODO fix; doesn't work					updateBookmark();	//make sure our bookmark is correct, in case another bookmark sent us here---i.e. canonicize the bookmark
				}
				catch(final PropertyVetoException propertyVetoException)
				{
					//TODO fix errors
				}
/*TODO del when works
				final String parameterName=getName();	//use this component's name as the bookmark parameter name
				if(parameterName!=null)	//if we have a bookmark parameter name
				{
					final String parameterValue=bookmark.getParameterValue(parameterName);	//get the parameter value
					if(parameterValue!=null)	//if a parameter for this component was specified
					{
						final Component component=getComponentByBookmarkName(parameterValue);	//get the component from the bookmark name
						if(component!=null && isEnabled(component) && isDisplayed(component))	//if an enabled, displayed component is specified
						{
							try
							{
								setValue(component);	//change to that component
							}
							catch(final ValidationException validationException)
							{
								//TODO fix errors
							}
						}
					}
				}
*/
			}
		}
	}

	/**Determines the component for navigation based upon the given bookmark.
	A bookmark parameter with a value of the empty string is considered to indicate the <code>null</code> value.
	Child components may override this method to select a different bookmark based upon whether components are enabled, for example.
	@param bookmark The bookmark for which a component should be returned, or <code>null</code> if no bookmark is available.
	@return The child component indicated by the given bookmark parameter value, or <code>null</code> if the given bookmark represents the <code>null</code> component value.
	*/
	protected Component getComponent(final Bookmark bookmark)
	{
		if(bookmark!=null)	//if a bookmark is specified
		{
			final String parameterName=getName();	//use this component's name as the bookmark parameter name
			if(parameterName!=null)	//if we have a bookmark parameter name
			{
				final String parameterValue=bookmark.getParameterValue(parameterName);	//get the parameter value
				if(parameterValue!=null)	//if a parameter for this component was specified
				{
					if(parameterValue.length()>0)	//if the bookmark name is not the empty string
					{
						for(final Component childComponent:this)	//for each child component
						{
							if(parameterValue.equals(childComponent.getName()))	//if this component has the correct name
							{
								return childComponent;	//we found the correct child component
							}
						}
					}
					else	//if the bookmark name was the empty string
					{
						return null;	//the null component was specified
					}
				}
			}
		}
//TODO del Log.trace("keeping component we already have");
		return getValue();	//if no bookmark was specified for this component, keep the same component we current have
	}

	/**Determines the component for navigation based upon the given bookmark parameter value.
	A bookmark parameter with a value of the empty string is considered to indicate the <code>null</code> value.
	Child components may override this method to select a different bookmark based upon whether components are enabled, for example.
	@param bookmarkName The bookmark parameter value for which a component should be returned.
	@return The child component indicated by the given bookmark parameter value, or <code>null</code> if the given bookmark name represents the <code>null</code> component value.
	@exception NullPointerException if the given bookmark name is <code>null</code>.
	*/
/*TODO del when works
	protected Component getComponentByBookmarkName(final String bookmarkName)
	{
		if(bookmarkName.length()>0)	//if the bookmark name is not the empty string
		{
			for(final Component childComponent:this)	//for each child component
			{
				if(bookmarkName.equals(childComponent.getName()))	//if this component has the correct name
				{
					return childComponent;	//we found the correct child component
				}
			}
		}
		return null;	//no child had a matching name (including the condition that the bookmark specifically requested no component) 
	}
*/
}
