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

package com.guiseframework.platform.web;

import java.util.*;
import static java.util.Collections.*;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.servlet.http.HTTPServlets.*;

import com.globalmentor.net.http.HTTP;
import com.guiseframework.platform.BrandedProduct;
import com.guiseframework.platform.ClientProduct;

/**The identification of the user agent client, such as a browser, accessing Guise on the web platform.
@author Garret Wilson
*/
public interface WebUserAgentProduct extends ClientProduct, BrandedProduct<WebUserAgentProduct.Brand>
{

	/**The brand of the web user agent.*/
	public enum Brand implements BrandedProduct.Brand
	{
		/**Alexa user agent.*/
		ALEXA,
		/**The Baidu spider.*/
		BAIDU,
		/**The Firefox browser.*/
		FIREFOX,
		/**The Gigabot spider.*/
		GIGABOT,
		/**The Googlebot spider.*/
		GOOGLEBOT,
		/**The Googlebot Image spider.*/
		GOOGLEBOT_IMAGE,
		/**The Microsoft Internet Explorer browser.*/
		INTERNET_EXPLORER,
		/**A general Mozilla browser.*/
		MOZILLA,
		/**The MSNbot spider.*/
		MSNBOT,
		/**The Opera browser.*/
		OPERA,
		/**The Safari browser.*/
		SAFARI,
		/**The AltaVista Scooter spider.*/
		SCOOTER,
		/**The W3C Validator user agent.*/
		W3C_VALIDATOR,
		/**The WGET user agent.*/
		WGET,
		/**The Yahoo! MMCrawler spider.*/
		YAHOO_MMCRAWLER;

		/**The map of web user agent brands keyed to user agent names.*/
		private final static Map<String, Brand> nameBrandMap;
	
		static	//initialize the name brand lookup map
		{
			final Map<String, Brand> tempMap=new HashMap<String, Brand>();	//create a new map to hold the brand lookup values
			tempMap.put(USER_AGENT_NAME_ALEXA, Brand.ALEXA);
			tempMap.put(USER_AGENT_NAME_BAIDU_SPIDER, Brand.BAIDU);
			tempMap.put(USER_AGENT_NAME_FIREFOX, Brand.FIREFOX);
			tempMap.put(USER_AGENT_NAME_GIGABOT, Brand.GIGABOT);
			tempMap.put(USER_AGENT_NAME_GOOGLEBOT, Brand.GOOGLEBOT);
			tempMap.put(USER_AGENT_NAME_GOOGLEBOT_IMAGE, Brand.GOOGLEBOT_IMAGE);
			tempMap.put(USER_AGENT_NAME_MSIE, Brand.INTERNET_EXPLORER);
			tempMap.put(USER_AGENT_NAME_MOZILLA, Brand.MOZILLA);
			tempMap.put(USER_AGENT_NAME_MSNBOT, Brand.MSNBOT);
			tempMap.put(USER_AGENT_NAME_OPERA, Brand.OPERA);
	//TODO fix		tempMap.put(USER_AGENT_NAME_SAFARI, Brand.SAFARI);
			tempMap.put(USER_AGENT_NAME_SCOOTER, Brand.SCOOTER);
			tempMap.put(USER_AGENT_NAME_W3C_VALIDATOR, Brand.W3C_VALIDATOR);
			tempMap.put(USER_AGENT_NAME_WGET, Brand.WGET);
			tempMap.put(USER_AGENT_NAME_YAHOO_MMCRAWLER, Brand.YAHOO_MMCRAWLER);
			nameBrandMap=unmodifiableMap(tempMap);	//save an unmodifiable version of the map
		}

		/**Retrieves a brand from a given user agent name.
		@param userAgentName The user agent name as reported by the HTTP {@value HTTP#USER_AGENT_HEADER} header.
		@return The brand corresponding to the given user agent name, or <code>null</code> if the given user agent name was not recognized.
		@throws NullPointerException if the given user agent name is <code>null</code>.
		*/
		public static Brand getBrand(final String userAgentName)
		{
			return nameBrandMap.get(checkInstance(userAgentName, "User agent name cannot be null."));
		}
	}
}
