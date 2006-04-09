package com.guiseframework.demo;

import com.guiseframework.component.*;
import com.guiseframework.event.AbstractGuisePropertyChangeListener;
import com.guiseframework.event.ActionListener;
import com.guiseframework.event.GuisePropertyChangeEvent;

/**Event wizard Guise demonstration panel using PLOOP+RDF+XML.
Copyright © 2006 GlobalMentor, Inc.
Demonstrates sequence card panels, task card constraints, task status select links,
	action card couplers, and link select card couplers.
@author Garret Wilson
*/
public class EventWizardPanel2 extends DefaultNavigationPanel
{

	/**Initializes the component after construction.*/
	public void initialize()
	{
		final SequenceCardPanel wizardCardPanel=(SequenceCardPanel)getComponentByName(this, "wizardCardPanel");
		final Panel<?> personalAgePanel=(Panel<?>)getComponentByName(this, "personalAgePanel");
		final CheckControl ageCheckControl=(CheckControl)getComponentByName(this, "ageCheckControl");
		ageCheckControl.addPropertyChangeListener(CheckControl.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<Boolean>()
				{
					public void propertyChange(GuisePropertyChangeEvent<Boolean> propertyChangeEvent)	//if the age checkbox changes
					{
						wizardCardPanel.setDisplayed(personalAgePanel, propertyChangeEvent.getNewValue());	//show or hide the age panel based upon the state of the age checkbox
					}			
				});
		final Button previousButton=(Button)getComponentByName(this, "previousButton");
		previousButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(com.guiseframework.event.ActionEvent actionEvent)
					{
						wizardCardPanel.goPrevious();
					};
				});
		final Button nextButton=(Button)getComponentByName(this, "nextButton");
		nextButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(com.guiseframework.event.ActionEvent actionEvent)
					{
						wizardCardPanel.goNext();
					};
				});
		wizardCardPanel.resetSequence();
	}

}
