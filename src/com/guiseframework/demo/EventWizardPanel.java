package com.guiseframework.demo;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.coupler.ActionCardCoupler;
import com.guiseframework.coupler.ListSelectCardCoupler;
import com.guiseframework.event.AbstractGuisePropertyChangeListener;
import com.guiseframework.event.ActionListener;
import com.guiseframework.event.GuisePropertyChangeEvent;
import com.guiseframework.model.DefaultValueModel;
import com.guiseframework.validator.RegularExpressionStringValidator;
import com.guiseframework.validator.ValueRequiredValidator;

/**Event wizard Guise demonstration panel.
Copyright © 2006 GlobalMentor, Inc.
Demonstrates tabbed panels, tab controls, and card panels. 
@author Garret Wilson
*/
public class EventWizardPanel extends DefaultNavigationPanel
{

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public EventWizardPanel(final GuiseSession session)
	{
		super(session, new FlowLayout(session, Flow.PAGE));	//construct the parent class flowing vertically
		setLabel("Guise\u2122 Demonstration: Event Wizard");	//set the panel title	

		final Heading heading=new Heading(session, 0);	//create a first-level heading
		heading.setLabel("Event Planner Wizard");	//set the heading text
		add(heading);	//add the heading to the panel

			//wizard
		final LayoutPanel wizardPanel=new LayoutPanel(session, new RegionLayout(session));
				//wizard cards
		final SequenceCardPanel wizardCardPanel=new SequenceCardPanel(session);
		wizardCardPanel.setName("wizardCardPanel");
		wizardCardPanel.setBookmarkEnabled(true);	//turn on bookmarks for the wizard
		final PersonalNamePanel personalCard1=new PersonalNamePanel(session);
		personalCard1.setName("personalCard1");
		personalCard1.setConstraints(new TaskCardConstraints(session));
		wizardCardPanel.add(personalCard1);
		final Panel<?> personalCard2=new PersonalAgePanel(session);
		personalCard2.setName("personalCard2");
		personalCard2.setConstraints(new TaskCardConstraints(session));
		wizardCardPanel.add(personalCard2);
		final Panel<?> personalCard3=new PersonalEmailPanel(session);
		personalCard3.setConstraints(new TaskCardConstraints(session));
		personalCard3.setName("personalCard3");
		wizardCardPanel.add(personalCard3);		
			//listen for the age checkbox changing
		personalCard1.getAgeCheckControl().addPropertyChangeListener(CheckControl.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<Boolean>()
				{
					public void propertyChange(GuisePropertyChangeEvent<Boolean> propertyChangeEvent)	//if the age checkbox changes
					{
						wizardCardPanel.setDisplayed(personalCard2, propertyChangeEvent.getNewValue());	//show or hide the age panel based upon the state of the age checkbox
					}			
				});
		final Panel<?> businessCard1=new BusinessNamePanel(session);
		businessCard1.setConstraints(new TaskCardConstraints(session));
		businessCard1.setName("businessCard1");
		wizardCardPanel.add(businessCard1);
		final Panel<?> businessCard2=new BusinessAddressPanel(session);
		businessCard2.setConstraints(new TaskCardConstraints(session));
		businessCard2.setName("businessCard2");
		wizardCardPanel.add(businessCard2);
		final Panel<?> eventCard1=new EventNamePanel(session);
		eventCard1.setName("eventCard1");
		eventCard1.setConstraints(new TaskCardConstraints(session));
		wizardCardPanel.add(eventCard1);
		wizardPanel.add(wizardCardPanel, new RegionConstraints(session, Region.CENTER));
				//wizard links
		final LayoutPanel wizardLinkPanel=new LayoutPanel(session);
		final Link personalLink10=new Link(session);
		personalLink10.setLabel("1. Personal");
		wizardLinkPanel.add(personalLink10);		
		final TaskStatusSelectLink personalLink11=new TaskStatusSelectLink(session);
		personalLink11.setLabel("Name");
		wizardLinkPanel.add(personalLink11);
		final TaskStatusSelectLink personalLink12=new TaskStatusSelectLink(session);
		personalLink12.setLabel("Age");
		wizardLinkPanel.add(personalLink12);
		final TaskStatusSelectLink personalLink13=new TaskStatusSelectLink(session);
		personalLink13.setLabel("Email");
		wizardLinkPanel.add(personalLink13);
		final Link businessLink20=new Link(session);
		businessLink20.setLabel("2. Business");
		wizardLinkPanel.add(businessLink20);		
		final TaskStatusSelectLink businessLink21=new TaskStatusSelectLink(session);
		businessLink21.setLabel("Name");
		wizardLinkPanel.add(businessLink21);
		final TaskStatusSelectLink businessLink22=new TaskStatusSelectLink(session);
		businessLink22.setLabel("Address");
		wizardLinkPanel.add(businessLink22);
		final Link eventLink30=new Link(session);
		eventLink30.setLabel("3. Event");
		wizardLinkPanel.add(eventLink30);		
		final TaskStatusSelectLink eventLink31=new TaskStatusSelectLink(session);
		eventLink31.setLabel("Name");
		wizardLinkPanel.add(eventLink31);
		wizardPanel.add(wizardLinkPanel, new RegionConstraints(session, Region.LINE_START));
				//wizard tabs
		final TabContainerControl wizardTabContainerControl=new TabContainerControl(session, Flow.LINE);
		final Label personalTabLabel=new Label(session);
		personalTabLabel.setLabel("Personal");
		wizardTabContainerControl.add(personalTabLabel);
		final Label businessTabLabel=new Label(session);
		businessTabLabel.setLabel("Business");
		wizardTabContainerControl.add(businessTabLabel);
		final Label eventTabLabel=new Label(session);
		eventTabLabel.setLabel("Event");
		wizardTabContainerControl.add(eventTabLabel);
		wizardPanel.add(wizardTabContainerControl, new RegionConstraints(session, Region.PAGE_START));
				//wizard buttons
		final LayoutPanel wizardButtonPanel=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));
		final Button personalPreviousButton=new Button(session);
		personalPreviousButton.setLabel("Previous");
		personalPreviousButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(com.guiseframework.event.ActionEvent actionEvent)
					{
						wizardCardPanel.goPrevious();
					};
				});
		wizardButtonPanel.add(personalPreviousButton);
		final Button personalNextButton=new Button(session);
		personalNextButton.setLabel("Next");
		personalNextButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(com.guiseframework.event.ActionEvent actionEvent)
					{
						wizardCardPanel.goNext();
					};
				});
		wizardButtonPanel.add(personalNextButton);
		wizardPanel.add(wizardButtonPanel, new RegionConstraints(session, Region.PAGE_END));
				//wizard link couplers
		new ActionCardCoupler(session, personalLink10, personalCard1);
		new ActionCardCoupler(session, personalLink11, personalCard1);
		new ActionCardCoupler(session, personalLink12, personalCard2);
		new ActionCardCoupler(session, personalLink13, personalCard3);
		new ActionCardCoupler(session, businessLink20, businessCard1);
		new ActionCardCoupler(session, businessLink21, businessCard1);
		new ActionCardCoupler(session, businessLink22, businessCard2);
		new ActionCardCoupler(session, eventLink30, eventCard1);
		new ActionCardCoupler(session, eventLink31, eventCard1);
				//wizard tab couplers
		new ListSelectCardCoupler<Component<?>>(session, wizardTabContainerControl, personalTabLabel, personalCard1, personalCard2, personalCard3);
		new ListSelectCardCoupler<Component<?>>(session, wizardTabContainerControl, businessTabLabel, businessCard1, businessCard2);
		new ListSelectCardCoupler<Component<?>>(session, wizardTabContainerControl, eventTabLabel, eventCard1);
		add(wizardPanel);

		wizardCardPanel.resetSequence();
	}

	/**The panel with personal name information.
	@author Garret Wilson
	*/
	protected static class PersonalNamePanel extends LayoutPanel
	{

		/**The check control for providing age.*/
		private final CheckControl ageCheckControl;

			/**@return The check control for providing age.*/
			public CheckControl getAgeCheckControl() {return ageCheckControl;}

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
			firstNameTextControl.setValidator(new RegularExpressionStringValidator(session, "\\S+.*", true));
			add(firstNameTextControl);
				//last name
			final TextControl<String> lastNameTextControl=new TextControl<String>(session, String.class);
			lastNameTextControl.setLabel("Last Name");
			lastNameTextControl.setValidator(new RegularExpressionStringValidator(session, "\\S+.*", true));
			add(lastNameTextControl);
				//age checkbox
			ageCheckControl=new CheckControl(session, new DefaultValueModel<Boolean>(session, Boolean.class, Boolean.TRUE));
			ageCheckControl.setLabel("I want to tell you my age");
			add(ageCheckControl);
		}
	}

	/**The panel with personal age information.
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
				//age
			final TextControl<Integer> ageTextControl=new TextControl<Integer>(session, Integer.class);
			ageTextControl.setLabel("Age");
			ageTextControl.setValidator(new ValueRequiredValidator<Integer>(session));
			add(ageTextControl);
		}
	}

	/**The panel with personal email information.
	@author Garret Wilson
	*/
	protected static class PersonalEmailPanel extends LayoutPanel
	{
		/**Session constructor.
		@param session The Guise session that owns this component.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public PersonalEmailPanel(final GuiseSession session)
		{
			super(session);
				//heading
			final Heading heading=new Heading(session, 1);
			heading.setLabel("Personal Information: Email");
			add(heading);
				//email
			final TextControl<String> ageTextControl=new TextControl<String>(session, String.class);
			ageTextControl.setLabel("Email");
			ageTextControl.setValidator(new RegularExpressionStringValidator(session, ".+@.+\\.[a-z]+", true));
			add(ageTextControl);
		}
	}

	/**The panel with business name information.
	@author Garret Wilson
	*/
	protected static class BusinessNamePanel extends LayoutPanel
	{
		/**Session constructor.
		@param session The Guise session that owns this component.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public BusinessNamePanel(final GuiseSession session)
		{
			super(session);
				//heading
			final Heading heading=new Heading(session, 1);
			heading.setLabel("Business Information: Name");
			add(heading);
				//business name
			final TextControl<String> nameTextControl=new TextControl<String>(session, String.class);
			nameTextControl.setLabel("Business Name");
			nameTextControl.setValidator(new RegularExpressionStringValidator(session, "\\S+.*", true));
			add(nameTextControl);
		}
	}

	/**The panel with business address information.
	@author Garret Wilson
	*/
	protected static class BusinessAddressPanel extends LayoutPanel
	{
		/**Session constructor.
		@param session The Guise session that owns this component.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public BusinessAddressPanel(final GuiseSession session)
		{
			super(session);
				//heading
			final Heading heading=new Heading(session, 1);
			heading.setLabel("Business Information: Address");
			add(heading);
				//business name
			final TextAreaControl addressTextArea=new TextAreaControl(session, 4, 40);
			addressTextArea.setLabel("Business Address");
			addressTextArea.setValidator(new RegularExpressionStringValidator(session, "\\S+.*", true));
			add(addressTextArea);
		}
	}

	/**The panel with event name information.
	@author Garret Wilson
	*/
	protected static class EventNamePanel extends LayoutPanel
	{
		/**Session constructor.
		@param session The Guise session that owns this component.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public EventNamePanel(final GuiseSession session)
		{
			super(session);
				//heading
			final Heading heading=new Heading(session, 1);
			heading.setLabel("Event Information: Name");
			add(heading);
				//business name
			final TextControl<String> nameTextControl=new TextControl<String>(session, String.class);
			nameTextControl.setLabel("Event Name");
			nameTextControl.setValidator(new RegularExpressionStringValidator(session, "\\S+.*", true));
			add(nameTextControl);
		}
	}

}
