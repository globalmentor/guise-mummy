/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.platform.web.css;

import java.util.*;

import static com.globalmentor.css.spec.CSS.*;
import static java.util.Collections.*;

import com.globalmentor.model.NameValuePair;

/**
 * A CSS rule, consisting of one or more selectors and a declaration.
 * @author Garret Wilson
 */
public class Rule {

	/** The list of selectors. */
	private final List<Selector> selectors = new ArrayList<Selector>();

	/** @return The list of selectors. */
	public List<Selector> getSelectors() {
		return selectors;
	}

	/** The list of declarations. */
	private final List<NameValuePair<String, String>> declarations = new ArrayList<NameValuePair<String, String>>();

	/** @return The list of declarations. */
	public List<NameValuePair<String, String>> getDeclarations() {
		return declarations;
	}

	/**
	 * Selectors constructor.
	 * @param selectors any selectors to add to the rule.
	 */
	public Rule(final Selector... selectors) {
		addAll(this.selectors, selectors); //add all the given selectors, if any
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder();
		for(final Selector selector : selectors) { //for each selector
			if(stringBuilder.length() > 0) { //if this is not the first selector
				stringBuilder.append(SELECTOR_SEPARATOR_CHAR); //separate the selectors
			}
			stringBuilder.append('\n');
			stringBuilder.append(selector); //append the selector
		}
		stringBuilder.append('\n');
		stringBuilder.append(RULE_GROUP_START_CHAR); //start the declaration block
		stringBuilder.append('\n');
		for(final NameValuePair<String, String> declaration : getDeclarations()) { //for each declaration
			stringBuilder.append(declaration.getName()); //name
			stringBuilder.append(PROPERTY_DIVIDER_CHAR);
			stringBuilder.append(declaration.getValue()); //value
			stringBuilder.append(DECLARATION_SEPARATOR_CHAR);
			stringBuilder.append('\n');
		}
		stringBuilder.append(RULE_GROUP_END_CHAR); //end the declaration block
		return stringBuilder.toString();
	}
}
