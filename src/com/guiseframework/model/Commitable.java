package com.guiseframework.model;

import java.io.IOException;

/**An object that can commit come data.
<p>Note: Although in standard English the adjective would be "committable", the spelling "commitable" is chosen for clarity and consistency.
Doubling of consonants is used in standard English to distinguish from verbs that lengthen a middle vowel by the use of an ending "e".
The convention used here would let the ending "e" remain, resulting, for example, in "hideable".</p>
@author Garret Wilson
*/
public interface Commitable
{

	/**Commits the data.
	@throws IOException if there is an error committing data.
	*/
	public void commit() throws IOException;
}
