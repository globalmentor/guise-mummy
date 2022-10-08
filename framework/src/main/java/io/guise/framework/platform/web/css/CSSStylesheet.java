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

/**
 * A CSS stylesheet.
 * @author Garret Wilson
 */
public class CSSStylesheet {

	/** The list of stylesheet rules. */
	private final List<Rule> rules = new ArrayList<Rule>();

	/** @return The list of stylesheet rules. */
	public List<Rule> getRules() {
		return rules;
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder();
		for(final Rule rule : rules) { //for each rule
			stringBuilder.append(rule).append('\n');
		}
		return stringBuilder.toString();
	}
}
