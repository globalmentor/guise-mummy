package com.guiseframework.platform.web.css;

import static com.globalmentor.java.Characters.GREATER_THAN_CHAR;
import static com.globalmentor.java.Characters.PLUS_SIGN_CHAR;
import static com.globalmentor.java.Characters.TILDE_CHAR;
import static com.globalmentor.text.xml.stylesheets.css.XMLCSSConstants.*;

/**A combinator separating sequences of simple selectors.
@author Garret Wilson
@see <a href="http://www.w3.org/TR/css3-selectors/">CSS 3 Selectors</a>
*/
public enum Combinator
{

	DESCENDANT(SPACE_CHAR),

	CHILD(GREATER_THAN_CHAR),

	ADJACENT_SIBLING(PLUS_SIGN_CHAR),

	GENERAL_SIBLING(TILDE_CHAR);
	
	
	/**The serialized form of the combinator.*/
	private final char serialization;

	/**Serialization constructor.
	@param serialization The serialized form of the combinator.
	*/
	private Combinator(final char serialization)
	{
		this.serialization=serialization;
	}

	/**Returns a string representation of this combinator.
	This version return the canonical representation of the combinator, using ' ' to represent a descendant combinator.
	@return A string representation of this combinator.
	*/
	public String toString()
	{
		return String.valueOf(serialization);
	}

	/**Determines the combinator represented by the given character.
	Any whitespace character will produce {@link #DESCENDANT}. 
	@param combinatorChar The character representing a combinator.
	@return The combinator the character represents.
	@exception IllegalArgumentException if the given character does not represent a combinator. 
	*/
	public static Combinator valueOf(final char combinatorChar)
	{
		
		switch(combinatorChar)
		{
			case SPACE_CHAR:
			case TAB_CHAR:
			case CR_CHAR:
			case LF_CHAR:
			case FF_CHAR:
				return DESCENDANT;
			case GREATER_THAN_CHAR:
				return CHILD;
			case PLUS_SIGN_CHAR:
				return ADJACENT_SIBLING;
			case TILDE_CHAR:
				return GENERAL_SIBLING;
			default:
				throw new IllegalArgumentException("Illegal combinator character: "+combinatorChar);
		}
	}
}
