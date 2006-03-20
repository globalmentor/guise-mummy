package com.guiseframework.demo;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.coupler.ActionCardCoupler;
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

			//Personal tab
		final LayoutPanel personalTab=new LayoutPanel(session, new RegionLayout(session));
				//Personal wizard cards
		final SequenceCardPanel personalCardPanel=new SequenceCardPanel(session);
		final PersonalNamePanel personalCard1=new PersonalNamePanel(session);
		personalCard1.setConstraints(new TaskCardConstraints(session));
		personalCardPanel.add(personalCard1);
		final Panel<?> personalCard2=new PersonalAgePanel(session);
		personalCard2.setConstraints(new TaskCardConstraints(session));
		personalCardPanel.add(personalCard2);
		final Panel<?> personalCard3=new PersonalEmailPanel(session);
		personalCard3.setConstraints(new TaskCardConstraints(session));
		personalCardPanel.add(personalCard3);		
		personalTab.add(personalCardPanel, new RegionConstraints(session, Region.CENTER));
			//listen for the age checkbox changing
		personalCard1.getAgeCheckControl().addPropertyChangeListener(CheckControl.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<Boolean>()
				{
					public void propertyChange(GuisePropertyChangeEvent<Boolean> propertyChangeEvent)	//if the age checkbox changes
					{
						personalCardPanel.setDisplayed(personalCard2, propertyChangeEvent.getNewValue());	//show or hide the age panel based upon the state of the age checkbox
					}			
				});
				//Personal wizard links
		final LayoutPanel personalLinkPanel=new LayoutPanel(session);
		final Link personalLink10=new Link(session);
		personalLink10.setLabel("1. Personal");
		personalLinkPanel.add(personalLink10);		
		final TaskStatusSelectLink personalLink11=new TaskStatusSelectLink(session);
		personalLink11.setLabel("Name");
		personalLinkPanel.add(personalLink11);
		final TaskStatusSelectLink personalLink12=new TaskStatusSelectLink(session);
		personalLink12.setLabel("Age");
		personalLinkPanel.add(personalLink12);
		final TaskStatusSelectLink personalLink13=new TaskStatusSelectLink(session);
		personalLink13.setLabel("Email");
		personalLinkPanel.add(personalLink13);
		personalTab.add(personalLinkPanel, new RegionConstraints(session, Region.LINE_START));
				//Personal wizard buttons
		final LayoutPanel personalButtonPanel=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));
		final Button personalPreviousButton=new Button(session);
		personalPreviousButton.setLabel("Previous");
		personalPreviousButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(com.guiseframework.event.ActionEvent actionEvent)
					{
						personalCardPanel.goPrevious();
					};
				});
		personalButtonPanel.add(personalPreviousButton);
		final Button personalNextButton=new Button(session);
		personalNextButton.setLabel("Next");
		personalNextButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(com.guiseframework.event.ActionEvent actionEvent)
					{
						personalCardPanel.goNext();
					};
				});
		personalButtonPanel.add(personalNextButton);
		personalTab.add(personalButtonPanel, new RegionConstraints(session, Region.PAGE_END));
		new ActionCardCoupler(session, personalLink10, personalCard1);
		new ActionCardCoupler(session, personalLink11, personalCard1);
		new ActionCardCoupler(session, personalLink12, personalCard2);
		new ActionCardCoupler(session, personalLink13, personalCard3);
		
			//Business tab
		final LayoutPanel businessTab=new LayoutPanel(session, new RegionLayout(session));
				//Business wizard cards
		final SequenceCardPanel businessCardPanel=new SequenceCardPanel(session);
		final Panel<?> businessCard1=new BusinessNamePanel(session);
		businessCard1.setConstraints(new TaskCardConstraints(session));
		businessCardPanel.add(businessCard1);
		final Panel<?> businessCard2=new BusinessAddressPanel(session);
		businessCard2.setConstraints(new TaskCardConstraints(session));
		businessCardPanel.add(businessCard2);
		businessTab.add(businessCardPanel, new RegionConstraints(session, Region.CENTER));
				//Business wizard links
		final LayoutPanel businessLinkPanel=new LayoutPanel(session);
		final Link businessLink20=new Link(session);
		businessLink20.setLabel("2. Business");
		businessLinkPanel.add(businessLink20);		
		final TaskStatusSelectLink businessLink21=new TaskStatusSelectLink(session);
		businessLink21.setLabel("Name");
		businessLinkPanel.add(businessLink21);
		final TaskStatusSelectLink businessLink22=new TaskStatusSelectLink(session);
		businessLink22.setLabel("Address");
		businessLinkPanel.add(businessLink22);
		businessTab.add(businessLinkPanel, new RegionConstraints(session, Region.LINE_START));
				//Business wizard buttons
		final LayoutPanel businessButtonPanel=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));
		final Button businessPreviousButton=new Button(session);
		businessPreviousButton.setLabel("Previous");
		businessPreviousButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(com.guiseframework.event.ActionEvent actionEvent)
					{
						businessCardPanel.goPrevious();
					};
				});
		businessButtonPanel.add(businessPreviousButton);
		final Button businessNextButton=new Button(session);
		businessNextButton.setLabel("Next");
		businessNextButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(com.guiseframework.event.ActionEvent actionEvent)
					{
						businessCardPanel.goNext();
					};
				});
		businessButtonPanel.add(businessNextButton);
		businessTab.add(businessButtonPanel, new RegionConstraints(session, Region.PAGE_END));
		new ActionCardCoupler(session, businessLink20, businessCard1);
		new ActionCardCoupler(session, businessLink21, businessCard1);
		new ActionCardCoupler(session, businessLink22, businessCard2);

			//Event tab
		final LayoutPanel eventTab=new LayoutPanel(session, new RegionLayout(session));
				//Event wizard cards
		final SequenceCardPanel eventCardPanel=new SequenceCardPanel(session);
		final Panel<?> eventCard1=new EventNamePanel(session);
		eventCard1.setConstraints(new TaskCardConstraints(session));
		eventCardPanel.add(eventCard1);
		eventTab.add(eventCardPanel, new RegionConstraints(session, Region.CENTER));
				//Event wizard links
		final LayoutPanel eventLinkPanel=new LayoutPanel(session);
		final Link eventLink30=new Link(session);
		eventLink30.setLabel("3. Event");
		eventLinkPanel.add(eventLink30);		
		final TaskStatusSelectLink eventLink31=new TaskStatusSelectLink(session);
		eventLink31.setLabel("Name");
		eventLinkPanel.add(eventLink31);
		eventTab.add(eventLinkPanel, new RegionConstraints(session, Region.LINE_START));
				//Event wizard buttons
		final LayoutPanel eventButtonPanel=new LayoutPanel(session, new FlowLayout(session, Flow.LINE));
		final Button eventPreviousButton=new Button(session);
		eventPreviousButton.setLabel("Previous");
		eventPreviousButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(com.guiseframework.event.ActionEvent actionEvent)
					{
						eventCardPanel.goPrevious();
					};
				});
		eventButtonPanel.add(eventPreviousButton);
		final Button eventNextButton=new Button(session);
		eventNextButton.setLabel("Next");
		eventNextButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(com.guiseframework.event.ActionEvent actionEvent)
					{
						eventCardPanel.goNext();
					};
				});
		eventButtonPanel.add(eventNextButton);
		eventTab.add(eventButtonPanel, new RegionConstraints(session, Region.PAGE_END));
		new ActionCardCoupler(session, eventLink30, eventCard1);
		new ActionCardCoupler(session, eventLink31, eventCard1);

			//Wizard tabbed panel
		final TabbedPanel tabbedPanel=new TabbedPanel(session);
		tabbedPanel.add(personalTab, new CardConstraints(session, "Personal"));
		tabbedPanel.add(businessTab, new CardConstraints(session, "Business"));
		tabbedPanel.add(eventTab, new CardConstraints(session, "Event"));		
		add(tabbedPanel);

		personalCardPanel.resetSequence();
		businessCardPanel.resetSequence();
		eventCardPanel.resetSequence();
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
