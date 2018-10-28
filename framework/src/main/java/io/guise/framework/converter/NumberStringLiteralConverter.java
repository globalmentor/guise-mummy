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

package io.guise.framework.converter;

import java.util.Currency;

/**
 * Indicates an object that can convert a number from and to a string. This converter supports different representations, including number, currency, percent,
 * and integer. If the currency style is chosen, care should be taken to indicate a specific constant currency unless it is desired that the currency type
 * change whenever the locale changes.
 * @see <a href="http://support.worldpay.com/kb/integration_guides/pro/help/spig12300.html">WorldPay - Integration Guides (spig12300)</a>
 * @param <V> The value type this converter supports.
 * @author Garret Wilson
 */
public interface NumberStringLiteralConverter<V extends Number> extends Converter<V, String> {

	/** The style of the number in its literal form. */
	public enum Style {
		/** General number formatting. */
		NUMBER,
		/** Money formatting. */
		CURRENCY,
		/** Percentage representation. */
		PERCENT,
		/** Scientific notation. */
		//TODO bring back when supported by Java		SCIENTIFIC,
		/** Integer formatting. */
		INTEGER;
	}

	/** United States Dollar currency. */
	public final Currency USD_CURRENCY = Currency.getInstance("USD"); //see http://developers.sun.com/dev/gadc/technicalpublications/articles/java1.4currency.html TODO use a constant
	/** Euro currency. */
	public final Currency EUR_CURRENCY = Currency.getInstance("EUR"); //see http://developers.sun.com/dev/gadc/technicalpublications/articles/java1.4currency.html TODO use a constant

}
