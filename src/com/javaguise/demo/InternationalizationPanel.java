package com.javaguise.demo;

import java.text.DateFormat;
import java.util.*;

import com.garretwilson.beans.AbstractPropertyValueChangeListener;
import com.garretwilson.beans.PropertyValueChangeEvent;
import com.garretwilson.beans.PropertyValueChangeListener;
import com.javaguise.component.*;
import com.javaguise.component.layout.Flow;
import com.javaguise.component.layout.RegionLayout;
import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.ValidationException;

/**Internationalization Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates locale label value models, date label models,
	application default locale, application supported locales, menus,
	component IDs, localized resource bundle resources, and localized resource files.
@author Garret Wilson
*/
public class InternationalizationPanel extends DefaultNavigationPanel
{

	/**The key to the UN Charter Preamble resource.*/
	protected final static String UN_CHARTER_PREAMBLE_RESOURCE_KEY="uncharterpreamble.html";

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public InternationalizationPanel(final GuiseSession session)
	{
		super(session, new RegionLayout(session));	//construct the parent class, using a region layout
		getModel().setLabel("Guise\u2122 Demonstration: Internationalization");	//set the panel title

			//create a value change listener to listen for language selection changes
		final PropertyValueChangeListener<Boolean> languageChangeListener=new AbstractPropertyValueChangeListener<Boolean>()
			{
				public void propertyValueChange(final PropertyValueChangeEvent<Boolean> propertyValueChangeEvent)	//when a language boolean model changes
				{
					if(Boolean.TRUE.equals(propertyValueChangeEvent.getNewValue()))	//if this language is being set
					{
						final Locale locale=((LocaleLabelValueModel<?>)propertyValueChangeEvent.getSource()).getLocale();	//get the selected locale
						session.setLocale(locale);	//change to the session selected locale
					}
				}
			};

		final Locale defaultLocale=session.getApplication().getDefaultLocale();	//get the default application locale (supported by the
		final Set<Locale> supportedLocales=session.getApplication().getSupportedLocales();	//get the locales supported by the application
			//create a mutual exclusion policy group to only allow one language to be selected at one time
		final ModelGroup<ValueModel<Boolean>> localeMutualExclusionPolicyModelGroup=new MutualExclusionPolicyModelGroup();
			//create a list of Boolean value models with locale labels
		final List<LocaleLabelValueModel<Boolean>> localeLabelValueModels=new ArrayList<LocaleLabelValueModel<Boolean>>(supportedLocales.size());
			//create models for each locale supported by the application (defined in the web.xml file, for example)
		for(final Locale supportedLocale:session.getApplication().getSupportedLocales())	//for each supported locale
		{
			final LocaleLabelValueModel<Boolean> localeLabelValueModel=new LocaleLabelValueModel<Boolean>(session, Boolean.class, supportedLocale);	//create a model this locale
			if(supportedLocale.equals(defaultLocale))	//if this is the default locale
			{
				try
				{
					localeLabelValueModel.setValue(Boolean.TRUE);	//select the locale value model
				}
				catch(final ValidationException validationException)	//there should be no problem selecting the model 
				{
					throw new AssertionError(validationException);
				}		
			}
			localeLabelValueModel.addPropertyChangeListener(LocaleLabelValueModel.VALUE_PROPERTY, languageChangeListener);	//listen for the language being changed
			localeLabelValueModels.add(localeLabelValueModel);	//add this model to the list
			localeMutualExclusionPolicyModelGroup.add(localeLabelValueModel);	//add this model to the mutual exclusion policy group
		}

		final DropMenu menu=new DropMenu(session, Flow.LINE);	//create a horizontal menu

			//Language
		final DropMenu languageMenu=new DropMenu(session, "languageMenu", Flow.PAGE);	//create a menu with a custom ID
		languageMenu.getModel().setLabelResourceKey("menu.language.label");	//show which resource to use for the label
		for(final LocaleLabelValueModel<Boolean> localeLabelValueModel:localeLabelValueModels)	//for each locale model
		{
				//create a check control, using the locale as the ID
			final CheckControl checkControl=new CheckControl(session, localeLabelValueModel.getLocale().toString(), localeLabelValueModel);
			checkControl.setCheckType(CheckControl.CheckType.ELLIPSE);	//show the check as an ellipse
			languageMenu.add(checkControl);	//add the check control to the language menu			
		}
		
		menu.add(languageMenu);	//add the language menu to the horizontal menu

			//Date
		final DropMenu dateMenu=new DropMenu(session, "dateMenu", Flow.PAGE);	//create a menu with a custom ID
		dateMenu.getModel().setLabelResourceKey("menu.date.label");	//show which resource to use for the label
			//Date|date
		final Label dateLabel=new Label(session, "dateLabel", new DateLabelModel(session, new Date(), DateFormat.LONG));	//create a label with the current date and a long date label model
		dateMenu.add(dateLabel);	//add the date label to the date menu
		
		menu.add(dateMenu);	//add the date menu to the horizontal menu

		add(menu, RegionLayout.PAGE_START_CONSTRAINTS);	//add the menu at the top

			//localized text
		final Text text=new Text(session);	//create a text component
		text.getModel().setTextContentType(TextModel.XHTML_CONTENT_TYPE);	//use application/xhtml+xml content
		text.getModel().setTextResourceKey(UN_CHARTER_PREAMBLE_RESOURCE_KEY);	//use the UN Charter Preamble resource, appropriately localized

		add(text, RegionLayout.CENTER_CONSTRAINTS);	//add the text in the center of the panel
	}

}
