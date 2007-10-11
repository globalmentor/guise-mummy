package com.guiseframework.demo;

import java.beans.PropertyVetoException;

import com.garretwilson.beans.AbstractGenericPropertyChangeListener;
import com.garretwilson.beans.GenericPropertyChangeEvent;
import com.guiseframework.Bookmark;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.model.ValueModel;

/**Bookmark Guise demonstration panel.
Copyright © 2005-2006 GlobalMentor, Inc.
Demonstrates saving bookmarks and updating state based upon bookmark navigation.
@author Garret Wilson
*/
public class BookmarksPanel extends LayoutPanel implements NavigationListener
{

	/**The number of tabs.*/
	private final static int TAB_COUNT=5;

	private final TabbedPanel tabbedPanel;
	
	/**Default constructor.*/
	public BookmarksPanel()
	{
		super(new FlowLayout(Flow.PAGE));	//construct the parent class flowing vertically
		setLabel("Guise\u2122 Demonstration: Bookmarks");	//set the panel title	

				//TabbedPanel
		tabbedPanel=new TabbedPanel();	//create a tabbed panel
		for(int i=0; i<TAB_COUNT; ++i)	//for each tab
		{
			final Panel tab=new LayoutPanel();	//create a panel to serve as the page
			final Heading tabHeading=new Heading(0);	//create a top-level heading
			tabHeading.setLabel("This is step "+i+".");	//set the text of the heading
			tab.add(tabHeading);	//add the heading to the tab
			tabbedPanel.add(tab, new CardConstraints("Step "+i));	//add the panel with a label			
		}
			//save a new bookmark ever time the tab changes
		tabbedPanel.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Component>()	//listen for the tab changing
				{
					public void propertyChange(GenericPropertyChangeEvent<Component> propertyChangeEvent)	//if the tab changes
					{
						final String bookmarkID="step"+(tabbedPanel.getSelectedIndex());	//create the bookmark ID
						final Bookmark bookmark=new Bookmark(new Bookmark.Parameter("step", Integer.toString(tabbedPanel.getSelectedIndex())));	//create a new bookmark
						getSession().setBookmark(bookmark);	//save this state by setting a bookmark
					}						
				});
		add(tabbedPanel);
	}

	/**Called when navigation occurs.
	This implementation updates the tab in response to a new bookmark navigation.
	@param navigationEvent The event indicating navigation details.
	*/
	public void navigated(final NavigationEvent navigationEvent)
	{
		final Bookmark bookmark=navigationEvent.getBookmark();	//get the bookmark, if any, from the navigation event
		final String bookmarkedStepString=bookmark!=null ? bookmark.getParameterValue("step") : null;	//see if there is a bookmark "step" parameter
		final int stepIndex=bookmarkedStepString!=null ? Integer.valueOf(bookmarkedStepString) : 0;	//see which step to go to, depending on the bookmark (if any)
		if(stepIndex>=0 && stepIndex<TAB_COUNT)	//if the tab index is within range
		{
			try
			{
				tabbedPanel.setSelectedIndexes(stepIndex);	//select the appropriate tab
			}
			catch(final PropertyVetoException propertyVetoException)	//if the change was vetoed, ignore the exception
			{
			}
		}
	}

}
