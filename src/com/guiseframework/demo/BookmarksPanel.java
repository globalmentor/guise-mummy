package com.guiseframework.demo;

import com.guiseframework.Bookmark;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.model.ValueModel;
import com.guiseframework.validator.ValidationException;

/**Bookmark Guise demonstration panel.
Copyright © 2005-2006 GlobalMentor, Inc.
Demonstrates saving bookmarks and updating state based upon bookmark navigation.
@author Garret Wilson
*/
public class BookmarksPanel extends DefaultNavigationPanel implements NavigationListener
{

	/**The number of tabs.*/
	private final static int TAB_COUNT=5;

	private final TabbedPanel tabbedPanel;
	
	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public BookmarksPanel(final GuiseSession session)
	{
		super(session, new FlowLayout(session, Flow.PAGE));	//construct the parent class flowing vertically
		setLabel("Guise\u2122 Demonstration: Bookmarks");	//set the panel title	

				//TabbedPanel
		tabbedPanel=new TabbedPanel(session);	//create a tabbed panel
		for(int i=0; i<TAB_COUNT; ++i)	//for each tab
		{
			final Panel<?> tab=new LayoutPanel(session);	//create a panel to serve as the page
			final Heading tabHeading=new Heading(session, 0);	//create a top-level heading
			tabHeading.setLabel("This is step "+i+".");	//set the text of the heading
			tab.add(tabHeading);	//add the heading to the tab
			tabbedPanel.add(tab, new CardConstraints(session, "Step "+i));	//add the panel with a label			
		}
			//save a new bookmark ever time the tab changes
		tabbedPanel.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<Component<?>>()	//listen for the tab changing
				{
					public void propertyChange(GuisePropertyChangeEvent<Component<?>> propertyChangeEvent)	//if the tab changes
					{
						final String bookmarkID="step"+(tabbedPanel.getSelectedIndex());	//create the bookmark ID
						final Bookmark bookmark=new Bookmark(new Bookmark.Parameter("step", Integer.toString(tabbedPanel.getSelectedIndex())));	//create a new bookmark
						session.setBookmark(bookmark);	//save this state by setting a bookmark
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
			catch(final ValidationException validationException)	//we should never encounter a validation problem selecting tabs
			{
				throw new AssertionError(validationException);
			}
		}
	}

}
