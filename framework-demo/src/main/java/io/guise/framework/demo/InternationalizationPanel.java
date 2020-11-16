/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.demo;

import java.beans.PropertyVetoException;
import java.util.*;

import com.globalmentor.beans.AbstractGenericPropertyChangeListener;
import com.globalmentor.beans.GenericPropertyChangeEvent;
import com.globalmentor.html.spec.HTML;

import io.guise.framework.Resources;
import io.guise.framework.component.*;
import io.guise.framework.component.layout.*;
import io.guise.framework.converter.Converter;
import io.guise.framework.converter.DateStringLiteralConverter;
import io.guise.framework.converter.DateStringLiteralStyle;
import io.guise.framework.model.*;

/**
 * Internationalization Guise demonstration panel. Copyright © 2005-2006 GlobalMentor, Inc. Demonstrates locale label models, date label models, application
 * supported locales, current session locale, menus, localized resource bundle resources, and localized resource files.
 * @author Garret Wilson
 */
public class InternationalizationPanel extends LayoutPanel {

	/** The key to the UN Charter Preamble resource. */
	protected static final String UN_CHARTER_PREAMBLE_RESOURCE_KEY = "uncharterpreamble.html";

	/** Default constructor. */
	public InternationalizationPanel() {
		super(new RegionLayout()); //construct the parent class, using a region layout
		setLabel("Guise\u2122 Demonstration: Internationalization"); //set the panel title

		final List<Locale> supportedLocales = getSession().getApplication().getLocales(); //get the locales supported by the application; the first one is the default
		final Locale sessionLocale = getSession().getLocale(); //get the current session locale
		//create a mutual exclusion policy group to only allow one language to be selected at one time
		final ModelGroup<ValueModel<Boolean>> localeMutualExclusionPolicyModelGroup = new MutualExclusionPolicyModelGroup();
		final DropMenu menu = new DropMenu(Flow.LINE); //create a horizontal menu

		//Language
		final DropMenu languageMenu = new DropMenu(Flow.PAGE); //create a menu with a custom ID
		languageMenu.setLabel(Resources.createStringResourceReference("menu.language.label")); //show which resource to use for the label
		//create check controls for each locale supported by the application (defined in the web.xml file, for example)
		for(final Locale supportedLocale : supportedLocales) { //for each supported locale
			final InfoModel localeLabelModel = new LocaleInfoModel(supportedLocale); //create a label model to represent the locale
			//create a check control, using the locale label model
			final CheckControl checkControl = new CheckControl(localeLabelModel);
			checkControl.setCheckType(CheckControl.CheckType.ELLIPSE); //show the check as an ellipse
			if(supportedLocale.equals(sessionLocale)) { //if this is the session locale
				try {
					checkControl.setValue(Boolean.TRUE); //select this check control
				} catch(final PropertyVetoException propertyVetoException) { //if the change was vetoed, ignore the exception
				}
			}
			//install a value change listener to listen for language selection
			checkControl.addPropertyChangeListener(CheckControl.VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Boolean>() {

				@Override
				public void propertyChange(final GenericPropertyChangeEvent<Boolean> propertyChangeEvent) { //when the language check changes
					if(Boolean.TRUE.equals(propertyChangeEvent.getNewValue())) { //if this language is being set
						getSession().setLocale(supportedLocale); //change the session locale
					}
				}

			});
			localeMutualExclusionPolicyModelGroup.add(checkControl); //add this check control to the mutual exclusion policy group
			languageMenu.add(checkControl); //add the check control to the language menu			
		}

		menu.add(languageMenu); //add the language menu to the horizontal menu

		//Date
		final DropMenu dateMenu = new DropMenu(Flow.PAGE); //create a menu with a custom ID
		dateMenu.setLabel(Resources.createStringResourceReference("menu.date.label")); //show which resource to use for the label
		//Date|date
		//create a converter to convert the date to a string in long format using the current locale
		final Converter<Date, String> dateConverter = new DateStringLiteralConverter(DateStringLiteralStyle.LONG);
		//create a label with the current date using the converter we created to show the date in the label
		final Label dateLabel = new Label(new ValueConverterInfoModel<Date>(new Date(), dateConverter));
		dateMenu.add(dateLabel); //add the date label to the date menu

		menu.add(dateMenu); //add the date menu to the horizontal menu

		add(menu, new RegionConstraints(Region.PAGE_START)); //add the menu at the top

		//localized text
		final TextBox text = new TextBox(); //create a text component
		text.setTextContentType(HTML.XHTML_MEDIA_TYPE); //use application/xhtml+xml content
		text.setText(Resources.createStringResourceReference(UN_CHARTER_PREAMBLE_RESOURCE_KEY)); //use the UN Charter Preamble resource, appropriately localized

		add(text, new RegionConstraints(Region.CENTER)); //add the text in the center of the panel
	}

}
