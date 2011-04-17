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

package com.guiseframework.component.text.directory.vcard;

import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Locale;

import com.globalmentor.text.directory.vcard.*;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;

import static com.globalmentor.collections.Arrays.*;
import static com.globalmentor.java.Strings.NO_STRINGS;
import static com.globalmentor.text.TextFormatter.*;
import static com.globalmentor.text.directory.vcard.VCard.*;
import static com.guiseframework.Resources.*;

/**A panel allowing entry of the {@value VCard#N_TYPE} type of a vCard <code>text/directory</code>
	profile as defined in <a href="http://www.ietf.org/rfc/rfc2426.txt">RFC 2426</a>, "vCard MIME Directory Profile".
@author Garret Wilson
*/
public class NamePanel extends AbstractPanel
{

	/**The family name text field.*/
	private final TextControl<String> familyNameControl;

		/**@return The family name text field.*/
		public TextControl<String> getFamilyNameControl() {return familyNameControl;}
	
	/**The given name text field.*/
	private final TextControl<String> givenNameControl;

		/**@return The given name text field.*/
		public TextControl<String> getGivenNameControl() {return givenNameControl;}

	/**The additional name text field.*/
	private final TextControl<String> additionalNameControl;

		/**@return The additional name text field.*/
		public TextControl<String> getAdditionalNameControl() {return additionalNameControl;}

	/**The honorific prefix list control.*/
	private final ListControl<String> honorificPrefixControl;

		/**@return The honorific prefix list control.*/
		public ListControl<String> getHonorificPrefixControl() {return honorificPrefixControl;}

	/**The honorific suffix list control.*/
	private final ListControl<String> honorificSuffixControl;

		/**@return The honorific suffix list control.*/
		public ListControl<String> getHonorificSuffixControl() {return honorificSuffixControl;}

	/**The action for selecting the language of the name.*/
//TODO fix	private final SelectLanguageAction selectLanguageAction;

		/**@return The action for selecting the language of the name.*/
//TODO fix		public SelectLanguageAction getSelectLanguageAction() {return selectLanguageAction;}

	/**Places the name information into the various fields.
	@param name The name to place in the fields, or <code>null</code> if no information should be displayed.
	@exception PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	*/
	public void setVCardName(final Name name) throws PropertyVetoException
	{
		if(name!=null)	//if there is a name
		{
			familyNameControl.setValue(formatList(new StringBuilder(), VALUE_SEPARATOR_CHAR, name.getFamilyNames()).toString());
			givenNameControl.setValue(formatList(new StringBuilder(), VALUE_SEPARATOR_CHAR, name.getGivenNames()).toString());
			additionalNameControl.setValue(formatList(new StringBuilder(), VALUE_SEPARATOR_CHAR, name.getAdditionalNames()).toString());
			final String[] honorificPrefixes=name.getHonorificPrefixes();	//get the existing values
			for(final String honorifixPrefix:honorificPrefixes)	//for each existing value
			{
				if(!honorificPrefixControl.contains(honorifixPrefix))	//if the list doesn't contain this value
				{
					honorificPrefixControl.add(honorifixPrefix);	//add the value so that it can be selected
				}
			}
			honorificPrefixControl.setSelectedValues(honorificPrefixes);	//select the correct values
			final String[] honorificSuffixes=name.getHonorificSuffixes();	//get the existing values
			for(final String honorifixSuffix:honorificSuffixes)	//for each existing value
			{
				if(!honorificPrefixControl.contains(honorifixSuffix))	//if the list doesn't contain this value
				{
					honorificPrefixControl.add(honorifixSuffix);	//add the value so that it can be selected
				}
			}
			honorificSuffixControl.setSelectedValues(honorificSuffixes);	//select the correct values
		//TODO fix		selectLanguageAction.setLocale(name.getLocale());
		}
		else	//if there is no name, clear the fields
		{
			familyNameControl.clearValue();
			givenNameControl.clearValue();
			additionalNameControl.clearValue();
			honorificPrefixControl.clearValue();
			honorificSuffixControl.clearValue();
		//TODO fix		selectLanguageAction.setLocale(name.getLocale());
		}
	}
	
	/**@return An object representing the VCard name information entered, or <code>null</code> if no name was entered.*/
	public Name getVCardName()
	{
			//get the values from the components
		final String familyNameValue=familyNameControl.getValue();
		final String givenNameValue=givenNameControl.getValue();
		final String additionalNameValue=additionalNameControl.getValue();
		final String[] familyNames=familyNameValue!=null ? familyNameValue.trim().split(String.valueOf(VALUE_SEPARATOR_CHAR)) : NO_STRINGS;
		final String[] givenNames=givenNameValue!=null ? givenNameValue.trim().split(String.valueOf(VALUE_SEPARATOR_CHAR)) : NO_STRINGS;
		final String[] additionalNames=additionalNameValue!=null ? additionalNameValue.trim().split(String.valueOf(VALUE_SEPARATOR_CHAR)) : NO_STRINGS;
		final String[] honorificPrefixes=honorificPrefixControl.getSelectedValues();
		final String[] honorificSuffixes=honorificSuffixControl.getSelectedValues();
//TODO fix		final Locale locale=selectLanguageAction.getLocale();
		final Locale locale=null;
			//if any part(s) of the name was given 
		if(familyNames.length>0 || givenNames.length>0 || additionalNames.length>0 || honorificPrefixes.length>0 || honorificSuffixes.length>0)
		{
			return new Name(familyNames, givenNames, additionalNames, honorificPrefixes, honorificSuffixes, locale);	//create and return a name representing the entered information
		}
		else	//if no name was given
		{
			return null;	//no name was entered
		}
	}

	/**Default constructor with a default line flow layout.*/
	public NamePanel()
	{
		this(new FlowLayout(Flow.LINE));	//default to flowing horizontally
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public NamePanel(final Layout<?> layout)
	{
		super(layout);	//construct the parent class

			//honorific prefix
		honorificPrefixControl=new ListControl<String>(String.class);
		honorificPrefixControl.setRowCount(3);
		honorificPrefixControl.setLabel(createStringResourceReference("text.directory.vcard.n.honorific.prefix.label"));
		honorificPrefixControl.setInfo(createStringResourceReference("text.directory.vcard.n.honorific.prefix.info"));
		final Collection<String> honoroficPrefixChoices=getSession().getResource("text.directory.vcard.n.honorific.prefix.choices");	//get choices from the resource
		honorificPrefixControl.addAll(honoroficPrefixChoices);	//add the choices to the list control
		add(honorificPrefixControl);

			//family name
		givenNameControl=new TextControl<String>(String.class);
		givenNameControl.setLabel(createStringResourceReference("text.directory.vcard.n.given.name.label"));
		givenNameControl.setInfo(createStringResourceReference("text.directory.vcard.n.given.name.info"));
		givenNameControl.setColumnCount(8);
		add(givenNameControl);

			//additional name
		additionalNameControl=new TextControl<String>(String.class);
		additionalNameControl.setLabel(createStringResourceReference("text.directory.vcard.n.additional.name.label"));
		additionalNameControl.setInfo(createStringResourceReference("text.directory.vcard.n.additional.name.info"));
		additionalNameControl.setColumnCount(8);
		add(additionalNameControl);
	
			//family name
		familyNameControl=new TextControl<String>(String.class);
		familyNameControl.setLabel(createStringResourceReference("text.directory.vcard.n.family.name.label"));
		familyNameControl.setInfo(createStringResourceReference("text.directory.vcard.n.family.name.info"));
		familyNameControl.setColumnCount(8);
		add(familyNameControl);
	
			//honorific suffix
		honorificSuffixControl=new ListControl<String>(String.class);
		honorificSuffixControl.setRowCount(3);
		honorificSuffixControl.setLabel(createStringResourceReference("text.directory.vcard.n.honorific.suffix.label"));
		honorificSuffixControl.setInfo(createStringResourceReference("text.directory.vcard.n.honorific.suffix.info"));
		final Collection<String> honoroficSuffixChoices=getSession().getResource("text.directory.vcard.n.honorific.suffix.choices");	//get choices from the resource
		honorificSuffixControl.addAll(honoroficSuffixChoices);	//add the choices to the list control
		add(honorificSuffixControl);
	
	
	}
}
