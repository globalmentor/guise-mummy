package com.javaguise.converter;

import java.util.Currency;

/**Indicates an object that can convert a number from and to a string.
This converter supports different representations, including number, currency, percent, and integer.
If the currency style is chosen, care should be taken to indicate a specific constant currency unless it is desired that the currency type change whenever the locale changes.
@see http://support.worldpay.com/kb/integration_guides/pro/help/spig12300.html 
@param <V> The value type this converter supports.
@author Garret Wilson
*/
public interface NumberStringLiteralConverter<V extends Number> extends Converter<V, String>
{

	/**The style of the number in its literal form.*/
	public enum Style
	{
		/**General number formatting.*/
		NUMBER,
		/**Money formatting.*/
		CURRENCY,
		/**Percentage representation.*/
		PERCENT,
		/**Scientific notation.*/
//TODO bring back when supported by Java		SCIENTIFIC,
		/**Integer formatting.*/
		INTEGER;
	}

	/**United States Dollar currency.*/
	public final Currency USD_CURRENCY=Currency.getInstance("USD");	//see http://developers.sun.com/dev/gadc/technicalpublications/articles/java1.4currency.html TODO use a constant
	/**Euro currency.*/
	public final Currency EUR_CURRENCY=Currency.getInstance("EUR");	//see http://developers.sun.com/dev/gadc/technicalpublications/articles/java1.4currency.html TODO use a constant

}
