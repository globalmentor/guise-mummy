package com.guiseframework.demo;

import java.beans.PropertyVetoException;
import java.util.*;

import com.garretwilson.beans.AbstractGenericPropertyChangeListener;
import com.garretwilson.beans.GenericPropertyChangeEvent;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.converter.Converter;
import com.guiseframework.converter.DateStringLiteralConverter;
import com.guiseframework.converter.DateStringLiteralStyle;
import com.guiseframework.model.*;

/**Internationalization Guise demonstration panel.
Copyright © 2005 GlobalMentor, Inc.
Demonstrates locale label models, date label models,
	application default locale, application supported locales, menus,
	localized resource bundle resources, and localized resource files.
@author Garret Wilson
*/
public class InternationalizationPanel extends DefaultNavigationPanel
{

	/**The key to the UN Charter Preamble resource.*/
	protected final static String UN_CHARTER_PREAMBLE_RESOURCE_KEY="uncharterpreamble.html";

	/**Default constructor.*/
	public InternationalizationPanel()
	{
		super(new RegionLayout());	//construct the parent class, using a region layout
		setLabel("Guise\u2122 Demonstration: Internationalization");	//set the panel title

		final Locale defaultLocale=getSession().getApplication().getDefaultLocale();	//get the default application locale supported by the application

		final Set<Locale> supportedLocales=getSession().getApplication().getSupportedLocales();	//get the locales supported by the application
			//create a mutual exclusion policy group to only allow one language to be selected at one time
		final ModelGroup<ValueModel<Boolean>> localeMutualExclusionPolicyModelGroup=new MutualExclusionPolicyModelGroup();
		final DropMenu menu=new DropMenu(Flow.LINE);	//create a horizontal menu

			//Language
		final DropMenu languageMenu=new DropMenu(Flow.PAGE);	//create a menu with a custom ID
		languageMenu.setLabel(getSession().createStringResourceReference("menu.language.label"));	//show which resource to use for the label
			//create check controls for each locale supported by the application (defined in the web.xml file, for example)
		for(final Locale supportedLocale:supportedLocales)	//for each supported locale
		{
			final LabelModel localeLabelModel=new LocaleLabelModel(supportedLocale);	//create a label model to represent the locale
				//create a check control, using the locale label model
			final CheckControl checkControl=new CheckControl(localeLabelModel);
			checkControl.setCheckType(CheckControl.CheckType.ELLIPSE);	//show the check as an ellipse
			if(supportedLocale.equals(defaultLocale))	//if this is the default locale
			{
				try
				{
					checkControl.setValue(Boolean.TRUE);	//select this check control
				}
				catch(final PropertyVetoException propertyVetoException)	//if the change was vetoed, ignore the exception
				{
				}		
			}
				//install a value change listener to listen for language selection
			checkControl.addPropertyChangeListener(CheckControl.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Boolean>()
					{
						public void propertyChange(final GenericPropertyChangeEvent<Boolean> propertyChangeEvent)	//when the language check changes
						{
							if(Boolean.TRUE.equals(propertyChangeEvent.getNewValue()))	//if this language is being set
							{
								getSession().setLocale(supportedLocale);	//change the session locale
							}
						}
					});
			localeMutualExclusionPolicyModelGroup.add(checkControl);	//add this check control to the mutual exclusion policy group
			languageMenu.add(checkControl);	//add the check control to the language menu			
		}
		
		menu.add(languageMenu);	//add the language menu to the horizontal menu

			//Date
		final DropMenu dateMenu=new DropMenu(Flow.PAGE);	//create a menu with a custom ID
		dateMenu.setLabel(getSession().createStringResourceReference("menu.date.label"));	//show which resource to use for the label
			//Date|date
				//create a converter to convert the date to a string in long format using the current locale
		final Converter<Date, String> dateConverter=new DateStringLiteralConverter(DateStringLiteralStyle.LONG);
			//create a label with the current date using the converter we created to show the date in the label
		final Label dateLabel=new Label(new ValueConverterLabelModel<Date>(new Date(), dateConverter));
		dateMenu.add(dateLabel);	//add the date label to the date menu
		
		menu.add(dateMenu);	//add the date menu to the horizontal menu

		add(menu, new RegionConstraints(Region.PAGE_START));	//add the menu at the top

			//localized text
		final Text text=new Text();	//create a text component
		text.setTextContentType(XHTML_CONTENT_TYPE);	//use application/xhtml+xml content
		text.setText(getSession().createStringResourceReference(UN_CHARTER_PREAMBLE_RESOURCE_KEY));	//use the UN Charter Preamble resource, appropriately localized

		add(text, new RegionConstraints(Region.CENTER));	//add the text in the center of the panel
	}

}
