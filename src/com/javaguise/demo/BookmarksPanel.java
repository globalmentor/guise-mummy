package com.javaguise.demo;

import com.javaguise.Bookmark;
import com.javaguise.GuiseSession;
import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.event.AbstractGuisePropertyChangeListener;
import com.javaguise.event.GuisePropertyChangeEvent;

import com.javaguise.model.DefaultLabelModel;
import com.javaguise.model.ValueModel;
import com.javaguise.validator.ValidationException;

/**Bookmark Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates saving bookmarks and updating state based upon bookmark navigation.
@author Garret Wilson
*/
public class BookmarksPanel extends DefaultNavigationPanel
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
		getModel().setLabel("Guise\u2122 Demonstration: Bookmarks");	//set the panel title	

				//TabbedPanel
		tabbedPanel=new TabbedPanel(session);	//create a tabbed panel
		for(int i=0; i<TAB_COUNT; ++i)	//for each tab
		{
			final Panel<?> tab=new LayoutPanel(session);	//create a panel to serve as the page
			final Heading tabHeading=new Heading(session, 0);	//create a top-level heading
			tabHeading.getModel().setLabel("This is step "+i+".");	//set the text of the heading
			tab.add(tabHeading);	//add the heading to the tab
			tabbedPanel.add(tab, new CardLayout.Constraints(new DefaultLabelModel(session, "Step "+i)));	//add the panel with a label			
		}
			//save a new bookmark ever time the tab changes
		tabbedPanel.getModel().addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<Component<?>>()	//listen for the tab changing
				{
					public void propertyChange(GuisePropertyChangeEvent<Component<?>> propertyChangeEvent)	//if the tab changes
					{
						final String bookmarkID="step"+(tabbedPanel.getModel().getSelectedIndex());	//create the bookmark ID
						final Bookmark bookmark=new Bookmark(new Bookmark.Parameter("step", Integer.toString(tabbedPanel.getModel().getSelectedIndex())));	//create a new bookmark
						session.setBookmark(bookmark);	//save this state by setting a bookmark
					}						
				});
		add(tabbedPanel);
	}

	/**Called when navigation occurs to the given navigation path and/or bookmark.
	@param navigationPath The navigation path relative to the application context path.
	@param bookmark The bookmark for which navigation should occur at this navigation path, or <code>null</code> if there is no bookmark involved in navigation.
	*/
	public void navigated(final String navigationPath, final Bookmark bookmark)	//update the tab in response to a new bookmark navigation
	{
		final String bookmarkedStepString=bookmark!=null ? bookmark.getParameterValue("step") : null;	//see if there is a bookmark "step" parameter
		final int stepIndex=bookmarkedStepString!=null ? Integer.valueOf(bookmarkedStepString) : 0;	//see which step to go to, depending on the bookmark (if any)
		if(stepIndex>=0 && stepIndex<TAB_COUNT)	//if the tab index is within range
		{
			try
			{
				tabbedPanel.getModel().setSelectedIndexes(stepIndex);	//select the appropriate tab
			}
			catch(final ValidationException validationException)	//we should never encounter a validation problem selecting tabs
			{
				throw new AssertionError(validationException);
			}
		}
	}

}
