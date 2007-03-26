package com.guiseframework.component.text.directory.vcard;

import java.beans.PropertyVetoException;
import java.util.Locale;

import static com.garretwilson.text.FormatUtilities.*;
import com.garretwilson.text.directory.vcard.*;
import static com.garretwilson.text.directory.vcard.VCardConstants.*;
import static com.garretwilson.util.ArrayUtilities.*;

import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import static com.guiseframework.Resources.*;

/**A panel allowing entry of the {@value VCardConstants#N_TYPE} type of a vCard <code>text/directory</code>
	profile as defined in <a href="http://www.ietf.org/rfc/rfc2426.txt">RFC 2426</a>, "vCard MIME Directory Profile".
@author Garret Wilson
*/
public class NamePanel extends AbstractPanel<NamePanel>
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

	/**The honorific prefix text field.*/
	private final TextControl<String> honorificPrefixControl;

		/**@return The honorific prefix text field.*/
		public TextControl<String> getHonorificPrefixControl() {return honorificPrefixControl;}

	/**The honorific suffix text field.*/
	private final TextControl<String> honorificSuffixControl;

		/**@return The honorific suffix text field.*/
		public TextControl<String> getHonorificSuffixControl() {return honorificSuffixControl;}

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
			honorificPrefixControl.setValue(formatList(new StringBuilder(), VALUE_SEPARATOR_CHAR, name.getHonorificPrefixes()).toString());
			honorificSuffixControl.setValue(formatList(new StringBuilder(), VALUE_SEPARATOR_CHAR, name.getHonorificSuffixes()).toString());
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
		final String familyNameValue=familyNameControl.getValue();
		final String givenNameValue=givenNameControl.getValue();
		final String additionalNameValue=additionalNameControl.getValue();
		final String honorificPrefixValue=honorificPrefixControl.getValue();
		final String honorificSuffixValue=honorificSuffixControl.getValue();
			//get the values from the components
		final String[] familyNames=familyNameValue!=null ? familyNameValue.trim().split(String.valueOf(VALUE_SEPARATOR_CHAR)) : EMPTY_STRING_ARRAY;
		final String[] givenNames=givenNameValue!=null ? givenNameValue.trim().split(String.valueOf(VALUE_SEPARATOR_CHAR)) : EMPTY_STRING_ARRAY;
		final String[] additionalNames=additionalNameValue!=null ? additionalNameValue.trim().split(String.valueOf(VALUE_SEPARATOR_CHAR)) : EMPTY_STRING_ARRAY;
		final String[] honorificPrefixes=honorificPrefixValue!=null ? honorificPrefixValue.trim().split(String.valueOf(VALUE_SEPARATOR_CHAR)) : EMPTY_STRING_ARRAY;
		final String[] honorificSuffixes=honorificSuffixValue!=null ? honorificSuffixValue.trim().split(String.valueOf(VALUE_SEPARATOR_CHAR)) : EMPTY_STRING_ARRAY;
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
		honorificPrefixControl=new TextControl<String>(String.class);
		honorificPrefixControl.setLabel(createStringResourceReference("text.directory.vcard.honorific.prefix.label"));
		honorificPrefixControl.setInfo(createStringResourceReference("text.directory.vcard.honorific.prefix.info"));
		honorificPrefixControl.setColumnCount(4);
		add(honorificPrefixControl);

			//family name
		givenNameControl=new TextControl<String>(String.class);
		givenNameControl.setLabel(createStringResourceReference("text.directory.vcard.given.name.label"));
		givenNameControl.setInfo(createStringResourceReference("text.directory.vcard.given.name.info"));
		givenNameControl.setColumnCount(8);
		add(givenNameControl);

			//additional name
		additionalNameControl=new TextControl<String>(String.class);
		additionalNameControl.setLabel(createStringResourceReference("text.directory.vcard.additional.name.label"));
		additionalNameControl.setInfo(createStringResourceReference("text.directory.vcard.additional.name.info"));
		additionalNameControl.setColumnCount(8);
		add(additionalNameControl);
	
			//family name
		familyNameControl=new TextControl<String>(String.class);
		familyNameControl.setLabel(createStringResourceReference("text.directory.vcard.family.name.label"));
		familyNameControl.setInfo(createStringResourceReference("text.directory.vcard.family.name.info"));
		familyNameControl.setColumnCount(8);
		add(familyNameControl);
	
			//honorific suffix
		honorificSuffixControl=new TextControl<String>(String.class);
		honorificSuffixControl.setLabel(createStringResourceReference("text.directory.vcard.honorific.suffix.label"));
		honorificSuffixControl.setInfo(createStringResourceReference("text.directory.vcard.honorific.suffix.info"));
		honorificSuffixControl.setColumnCount(4);
		add(honorificSuffixControl);
	
	
	}
}
