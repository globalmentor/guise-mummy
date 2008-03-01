package com.guiseframework.demo;

import com.globalmentor.beans.AbstractGenericPropertyChangeListener;
import com.globalmentor.beans.GenericPropertyChangeEvent;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.coupler.ActionCardCoupler;
import com.guiseframework.coupler.ListSelectCardCoupler;
import com.guiseframework.model.DefaultValueModel;
import com.guiseframework.validator.RegularExpressionStringValidator;
import com.guiseframework.validator.ValueRequiredValidator;

/**Event wizard Guise demonstration panel.
Copyright © 2006-2007 GlobalMentor, Inc.
Demonstrates sequence card panels, task card constraints, task status select links,
	action card couplers, link select card couplers, and action prototypes.
@author Garret Wilson
*/
public class EventWizardPanel extends LayoutPanel
{

	/**Default constructor.*/
	public EventWizardPanel()
	{
		super(new FlowLayout(Flow.PAGE));	//construct the parent class flowing vertically
		setLabel("Guise\u2122 Demonstration: Event Wizard");	//set the panel title	

		final Heading heading=new Heading(0);	//create a first-level heading
		heading.setLabel("Event Planner Wizard");	//set the heading text
		add(heading);	//add the heading to the panel

			//wizard
		final LayoutPanel wizardPanel=new LayoutPanel(new RegionLayout());
				//wizard cards
		final SequenceCardPanel wizardCardPanel=new SequenceCardPanel();
		wizardCardPanel.setName("wizardCardPanel");
		wizardCardPanel.setBookmarkEnabled(true);	//turn on bookmarks for the wizard
		final PersonalNamePanel personalNamePanel=new PersonalNamePanel();
		personalNamePanel.setName("personalCard1");
		personalNamePanel.setConstraints(new TaskCardConstraints());
		wizardCardPanel.add(personalNamePanel);
		final Panel personalAgePanel=new PersonalAgePanel();
		personalAgePanel.setName("personalAgePanel");
		personalAgePanel.setConstraints(new TaskCardConstraints());
		wizardCardPanel.add(personalAgePanel);
		final Panel personalEmailPanel=new PersonalEmailPanel();
		personalEmailPanel.setConstraints(new TaskCardConstraints());
		personalEmailPanel.setName("personalEmailPanel");
		wizardCardPanel.add(personalEmailPanel);		
			//listen for the age checkbox changing
		personalNamePanel.getAgeCheckControl().addPropertyChangeListener(CheckControl.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Boolean>()
				{
					public void propertyChange(GenericPropertyChangeEvent<Boolean> propertyChangeEvent)	//if the age checkbox changes
					{
						wizardCardPanel.setDisplayed(personalAgePanel, propertyChangeEvent.getNewValue());	//show or hide the age panel based upon the state of the age checkbox
					}			
				});
		final Panel businessNamePanel=new BusinessNamePanel();
		businessNamePanel.setConstraints(new TaskCardConstraints());
		businessNamePanel.setName("businessNamePane");
		wizardCardPanel.add(businessNamePanel);
		final Panel businessAddressPanel=new BusinessAddressPanel();
		businessAddressPanel.setConstraints(new TaskCardConstraints());
		businessAddressPanel.setName("businessAddressPanel");
		wizardCardPanel.add(businessAddressPanel);
		final Panel eventNamePanel=new EventNamePanel();
		eventNamePanel.setName("eventNamePanel");
		eventNamePanel.setConstraints(new TaskCardConstraints());
		wizardCardPanel.add(eventNamePanel);
		wizardPanel.add(wizardCardPanel, new RegionConstraints(Region.CENTER));
				//wizard links
		final LayoutPanel wizardLinkPanel=new LayoutPanel();
		final Link personalLink10=new Link();
		personalLink10.setLabel("1. Personal");
		wizardLinkPanel.add(personalLink10);		
		final TaskStateSelectLink personalLink11=new TaskStateSelectLink();
		personalLink11.setLabel("Name");
		wizardLinkPanel.add(personalLink11);
		final TaskStateSelectLink personalLink12=new TaskStateSelectLink();
		personalLink12.setLabel("Age");
		wizardLinkPanel.add(personalLink12);
		final TaskStateSelectLink personalLink13=new TaskStateSelectLink();
		personalLink13.setLabel("Email");
		wizardLinkPanel.add(personalLink13);
		final Link businessLink20=new Link();
		businessLink20.setLabel("2. Business");
		wizardLinkPanel.add(businessLink20);		
		final TaskStateSelectLink businessLink21=new TaskStateSelectLink();
		businessLink21.setLabel("Name");
		wizardLinkPanel.add(businessLink21);
		final TaskStateSelectLink businessLink22=new TaskStateSelectLink();
		businessLink22.setLabel("Address");
		wizardLinkPanel.add(businessLink22);
		final Link eventLink30=new Link();
		eventLink30.setLabel("3. Event");
		wizardLinkPanel.add(eventLink30);		
		final TaskStateSelectLink eventLink31=new TaskStateSelectLink();
		eventLink31.setLabel("Name");
		wizardLinkPanel.add(eventLink31);
		wizardPanel.add(wizardLinkPanel, new RegionConstraints(Region.LINE_START));
				//wizard tabs
		final TabContainerControl wizardTabContainerControl=new TabContainerControl(Flow.LINE);
		final Label personalTabLabel=new Label();
		personalTabLabel.setLabel("Personal");
		wizardTabContainerControl.add(personalTabLabel);
		final Label businessTabLabel=new Label();
		businessTabLabel.setLabel("Business");
		wizardTabContainerControl.add(businessTabLabel);
		final Label eventTabLabel=new Label();
		eventTabLabel.setLabel("Event");
		wizardTabContainerControl.add(eventTabLabel);
		wizardPanel.add(wizardTabContainerControl, new RegionConstraints(Region.PAGE_START));
				//wizard buttons
		final LayoutPanel wizardButtonPanel=new LayoutPanel(new FlowLayout(Flow.LINE));
		wizardButtonPanel.setName("wizardButtonPanel");
		final Button previousButton=new Button(wizardCardPanel.getPreviousActionPrototype());
		wizardButtonPanel.add(previousButton);
		final Button nextButton=new Button(wizardCardPanel.getNextActionPrototype());
		wizardButtonPanel.add(nextButton);
		final Button finishButton=new Button(wizardCardPanel.getFinishActionPrototype());
		wizardButtonPanel.add(finishButton);
		wizardPanel.add(wizardButtonPanel, new RegionConstraints(Region.PAGE_END));
				//wizard link couplers
		new ActionCardCoupler(personalLink10, personalNamePanel);
		new ActionCardCoupler(personalLink11, personalNamePanel);
		new ActionCardCoupler(personalLink12, personalAgePanel);
		new ActionCardCoupler(personalLink13, personalEmailPanel);
		new ActionCardCoupler(businessLink20, businessNamePanel);
		new ActionCardCoupler(businessLink21, businessNamePanel);
		new ActionCardCoupler(businessLink22, businessAddressPanel);
		new ActionCardCoupler(eventLink30, eventNamePanel);
		new ActionCardCoupler(eventLink31, eventNamePanel);
				//wizard tab couplers
		new ListSelectCardCoupler<Component>(wizardTabContainerControl, personalTabLabel, personalNamePanel, personalAgePanel, personalEmailPanel);
		new ListSelectCardCoupler<Component>(wizardTabContainerControl, businessTabLabel, businessNamePanel, businessAddressPanel);
		new ListSelectCardCoupler<Component>(wizardTabContainerControl, eventTabLabel, eventNamePanel);
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

		/**Default constructor.*/
		public PersonalNamePanel()
		{
				//heading
			final Heading heading=new Heading(1);
			heading.setLabel("Personal Information: Name");
			add(heading);
				//first name
			final TextControl<String> firstNameTextControl=new TextControl<String>(String.class);
			firstNameTextControl.setLabel("First Name");
			firstNameTextControl.setValidator(new RegularExpressionStringValidator("\\S+.*", true));
			add(firstNameTextControl);
				//last name
			final TextControl<String> lastNameTextControl=new TextControl<String>(String.class);
			lastNameTextControl.setLabel("Last Name");
			lastNameTextControl.setValidator(new RegularExpressionStringValidator("\\S+.*", true));
			add(lastNameTextControl);
				//age checkbox
			ageCheckControl=new CheckControl(new DefaultValueModel<Boolean>(Boolean.class, Boolean.TRUE));
			ageCheckControl.setLabel("I want to tell you my age");
			add(ageCheckControl);
		}
	}

	/**The panel with personal age information.
	@author Garret Wilson
	*/
	protected static class PersonalAgePanel extends LayoutPanel
	{
		/**Default constructor.*/
		public PersonalAgePanel()
		{
				//heading
			final Heading heading=new Heading(1);
			heading.setLabel("Personal Information: Age");
			add(heading);
				//age
			final TextControl<Integer> ageTextControl=new TextControl<Integer>(Integer.class);
			ageTextControl.setLabel("Age");
			ageTextControl.setValidator(new ValueRequiredValidator<Integer>());
			add(ageTextControl);
		}
	}

	/**The panel with personal email information.
	@author Garret Wilson
	*/
	protected static class PersonalEmailPanel extends LayoutPanel
	{
		/**Default constructor.*/
		public PersonalEmailPanel()
		{
				//heading
			final Heading heading=new Heading(1);
			heading.setLabel("Personal Information: Email");
			add(heading);
				//email
			final TextControl<String> emailTextControl=new TextControl<String>(String.class);
			emailTextControl.setLabel("Email");
			emailTextControl.setValidator(new RegularExpressionStringValidator(".+@.+\\.[a-z]+", true));
			add(emailTextControl);
		}
	}

	/**The panel with business name information.
	@author Garret Wilson
	*/
	protected static class BusinessNamePanel extends LayoutPanel
	{
		/**Default constructor.*/
		public BusinessNamePanel()
		{
				//heading
			final Heading heading=new Heading(1);
			heading.setLabel("Business Information: Name");
			add(heading);
				//business name
			final TextControl<String> nameTextControl=new TextControl<String>(String.class);
			nameTextControl.setLabel("Business Name");
			nameTextControl.setValidator(new RegularExpressionStringValidator("\\S+.*", true));
			add(nameTextControl);
		}
	}

	/**The panel with business address information.
	@author Garret Wilson
	*/
	protected static class BusinessAddressPanel extends LayoutPanel
	{
		/**Default constructor.*/
		public BusinessAddressPanel()
		{
				//heading
			final Heading heading=new Heading(1);
			heading.setLabel("Business Information: Address");
			add(heading);
				//business address
			final TextControl<String> addressTextControl=new TextControl<String>(String.class, 4, 40);
			addressTextControl.setLabel("Business Address");
			addressTextControl.setValidator(new RegularExpressionStringValidator("\\S+.*", true));
			add(addressTextControl);
		}
	}

	/**The panel with event name information.
	@author Garret Wilson
	*/
	protected static class EventNamePanel extends LayoutPanel
	{
		/**Default constructor.*/
		public EventNamePanel()
		{
				//heading
			final Heading heading=new Heading(1);
			heading.setLabel("Event Information: Name");
			add(heading);
				//event name
			final TextControl<String> nameTextControl=new TextControl<String>(String.class);
			nameTextControl.setLabel("Event Name");
			nameTextControl.setValidator(new RegularExpressionStringValidator("\\S+.*", true));
			add(nameTextControl);
		}
	}

}
