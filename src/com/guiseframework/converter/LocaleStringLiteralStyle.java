package com.guiseframework.converter;

/**The style of a date in its string literal form.*/
public enum LocaleStringLiteralStyle
{
	/**A name for the locale's country that is appropriate for display to the user.*/
	COUNTRY,
	/**The uppercase ISO 3166 two-letter country/region code for the locale.*/
	COUNTRY_CODE_2,
	/**The uppercase ISO 3166 three-letter country/region code for the locale.*/
	COUNTRY_CODE_3,
	/**A name for the locale's language that is appropriate for display to the user.*/
	LANGUAGE,
	/**The lowercase ISO 639 two-letter language code for the locale.*/
	LANGUAGE_CODE_2,
	/**The lowercase ISO 639-2/T three-letter language code*/
	LANGUAGE_CODE_3,
	/**The RFC 1766 language tag, such as en-US.*/
	LANGUAGE_TAG,
	/**A name for the locale that is appropriate for display to the user, such as language (country, variant).*/
	NAME,
	/**A name for the locale's variant code that is appropriate for display to the user.*/
	VARIANT,
	/**The  variant code for the locale.*/
	VARIANT_CODE;
}
