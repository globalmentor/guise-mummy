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

package com.guiseframework.model;

import java.io.IOException;

/**
 * An object that can commit come data.
 * <p>
 * Note: Although in standard English the adjective would be "committable", the spelling "commitable" is chosen for clarity and consistency. Doubling of
 * consonants is used in standard English to distinguish from verbs that lengthen a middle vowel by the use of an ending "e". The convention used here would let
 * the ending "e" remain, resulting, for example, in "hideable".
 * </p>
 * @author Garret Wilson
 */
public interface Commitable {

	/**
	 * Commits the data.
	 * @throws IOException if there is an error committing data.
	 */
	public void commit() throws IOException;
}
