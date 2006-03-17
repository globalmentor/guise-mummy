package com.guiseframework.demo;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.coupler.ActionCardCoupler;
import com.guiseframework.event.ActionListener;
import com.guiseframework.validator.RegularExpressionStringValidator;
import com.guiseframework.validator.ValueRequiredValidator;

/**Tabs Guise demonstration panel.
Copyright © 2006 GlobalMentor, Inc.
Demonstrates tabbed panels, tab controls, and card panels. 
@author Garret Wilson
*/
public class WizardPanel extends DefaultNavigationPanel
{

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public WizardPanel(final GuiseSession session)
	{
		super(session, new FlowLayout(session, Flow.PAGE));	//construct the parent class flowing vertically
		setLabel("Guise\u2122 Demonstration: Wizard");	//set the panel title	

		final Heading heading=new Heading(session, 0);	//create a first-level heading
		heading.setLabel("Event Planner Wizard");	//set the heading text
		add(heading);	//add the heading to the panel
		
		final LayoutPanel wizard=new LayoutPanel(session, new RegionLayout(session));
				//wizard cards
		final SequenceCardPanel wizardCardPanel=new SequenceCardPanel(session);
					//page 1
		final Panel<?> wizardPage1=new PersonalNamePanel(session);
		wizardCardPanel.add(wizardPage1, new TaskCardConstraints(session, "Page 1"));	//add the panel with a label
					//page 2
		final Panel<?> wizardPage2=new PersonalAgePanel(session);
		wizardCardPanel.add(wizardPage2, new TaskCardConstraints(session, "Page 2"));	//add the panel with a label
					//page 3
		final Panel<?> wizardPage3=new LayoutPanel(session);	//create a panel to serve as the page
		final Heading wizardPage3Heading=new Heading(session, 0);	//create a top-level heading
		wizardPage3Heading.setLabel("Wizard page 3.");	//set the text of the heading
		wizardPage3.add(wizardPage3Heading);	//add the heading to the page
		wizardCardPanel.add(wizardPage3, new TaskCardConstraints(session, "Page 3"));	//add the panel with a label
		wizard.add(wizardCardPanel, new RegionConstraints(session, Region.CENTER));
				//wizard links
		final LayoutPanel wizardLinkPanel=new LayoutPanel(session);
					//link to page 1
		final TaskStatusSelectLink wizardLink1=new TaskStatusSelectLink(session);
		wizardLink1.setLabel("Page 1");
		wizardLinkPanel.add(wizardLink1);
					//link to page 2
		final TaskStatusSelectLink wizardLink2=new TaskStatusSelectLink(session);
		wizardLink2.setLabel("Page 2");
		wizardLinkPanel.add(wizardLink2);
					//link to page 3
		final TaskStatusSelectLink wizardLink3=new TaskStatusSelectLink(session);
		wizardLink3.setLabel("Page 3");
		wizardLinkPanel.add(wizardLink3);
		wizard.add(wizardLinkPanel, new RegionConstraints(session, Region.LINE_START));
				//buttons
		final LayoutPanel wizardButtonPanel=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));
					//previous
		final Button previousButton=new Button(session);
		previousButton.setLabel("Previous");
		wizardButtonPanel.add(previousButton);
					//next
		final Button nextButton=new Button(session);
		nextButton.setLabel("Next");
		nextButton.addActionListener(new ActionListener(){
			public void actionPerformed(com.guiseframework.event.ActionEvent actionEvent)
			{
				wizardCardPanel.goNext();
			};
		});
		wizardButtonPanel.add(nextButton);
		wizard.add(wizardButtonPanel, new RegionConstraints(session, Region.PAGE_END));
		new ActionCardCoupler(session, wizardLink1, wizardPage1);
		new ActionCardCoupler(session, wizardLink2, wizardPage2);
		new ActionCardCoupler(session, wizardLink3, wizardPage3);
		wizardCardPanel.resetSequence();
		add(wizard);
	}

	/**The panel with name information.
	@author Garret Wilson
	*/
	protected static class PersonalNamePanel extends LayoutPanel
	{
		/**Session constructor.
		@param session The Guise session that owns this component.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public PersonalNamePanel(final GuiseSession session)
		{
			super(session);
				//heading
			final Heading heading=new Heading(session, 1);
			heading.setLabel("Personal Information: Name");
			add(heading);
				//first name
			final TextControl<String> firstNameTextControl=new TextControl<String>(session, String.class);
			firstNameTextControl.setLabel("First Name");
			firstNameTextControl.setValidator(new RegularExpressionStringValidator(session, ".+", true));
			add(firstNameTextControl);
				//last name
			final TextControl<String> lastNameTextControl=new TextControl<String>(session, String.class);
			lastNameTextControl.setLabel("Last Name");
			lastNameTextControl.setValidator(new RegularExpressionStringValidator(session, ".+", true));
			add(lastNameTextControl);
		}
	}

	/**The panel with age information.
	@author Garret Wilson
	*/
	protected static class PersonalAgePanel extends LayoutPanel
	{
		/**Session constructor.
		@param session The Guise session that owns this component.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public PersonalAgePanel(final GuiseSession session)
		{
			super(session);
				//heading
			final Heading heading=new Heading(session, 1);
			heading.setLabel("Personal Information: Age");
			add(heading);
				//first name
			final TextControl<Integer> ageTextControl=new TextControl<Integer>(session, Integer.class);
			ageTextControl.setLabel("Age");
			ageTextControl.setValidator(new ValueRequiredValidator<Integer>(session));
			add(ageTextControl);
		}
	}
}
