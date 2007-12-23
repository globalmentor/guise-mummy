package com.guiseframework.platform.web.css;

import static com.garretwilson.lang.Strings.*;
import static com.garretwilson.text.CharacterConstants.NULL_CHAR;
import static com.garretwilson.text.xml.stylesheets.css.XMLCSSConstants.*;

import java.io.*;
import java.util.*;

import com.garretwilson.io.*;
import com.garretwilson.util.NameValuePair;

/**Class to parse CSS stylesheets in order to manipulate them within Guise.
@author Garret Wilson
@see <a href="http://www.w3.org/TR/css3-syntax/">CSS 3 Syntax</a>
*/
public class CSSProcessor
{

	/**Skips whitespace and comments without throwing an error if the end of the input was reached.
	Peeking is reset.
	@param parseReader The reader from which to retrieve characters.
	@except IOException Thrown if there is an error reading the input or the input is invalid.
	*/
	protected static void skipWhitespaceCommentsEOF(final ParseReader parseReader) throws IOException
	{
		parseReader.skipCharsEOF(WHITESPACE_CHARS);	//skip over whitespace
		while(parseReader.isPeekStringEOF(COMMENT_START))	//if we've reached a comment
		{
			parseReader.readStringUntilSkipString(COMMENT_END);	//read until the end of the comment			
			parseReader.skipCharsEOF(WHITESPACE_CHARS);	//skip over whitespace and try again
		}
		parseReader.resetPeek();	//reset peeking
	}
	
	/**Parses an input stream is expected to contain a set of declaration names and values.
	@param parseReader The reader from which to retrieve characters.
	@param rule The rule which will hold the declarations.
	@except IOException Thrown if there is an error reading the input, the input is invalid, or the end of the input was reached unexpectedly.
	@return <code>true</code> if the end-of-rule-group character was found, <code>false</code> if the end of the stream was reached.
	*/
	public static boolean parseDeclarations(final ParseReader parseReader, final Rule rule) throws IOException
	{
		while(true)	//keep reading declarations until there aren't any more
		{
			skipWhitespaceCommentsEOF(parseReader);	//skip over whitespace and comments
			if(parseReader.isEOF())	//if we've hit the end of the stream of characters
				return false;	//show that we hit the end of the stream without finding the end-of-rule-group character
			if(parseReader.peek()==RULE_GROUP_END_CHAR)  //if we're at the end of the rule group (which will happen if the rule group is empty, for instance)
				return true;  //show that we hit the end of the rule group G***this check is done at the end of this loop, too -- is there a place we can combine both checks?

			final String propertyName=parseReader.readStringUntilChar(WHITESPACE_CHARS+PROPERTY_DIVIDER_CHAR+'/');	//get the name of the property, which is followed by whitespace (or a comment) or a divider character TODO use a constant
			skipWhitespaceCommentsEOF(parseReader);	//skip over whitespace and comments, which might come before the property divider
			parseReader.readExpectedChar(PROPERTY_DIVIDER_CHAR);	//reade the divider between the property name and its value
			skipWhitespaceCommentsEOF(parseReader);	//skip over whitespace and comments
			final String propertyValue=trimEnd(parseReader.readStringUntilCharEOF(""+DECLARATION_SEPARATOR_CHAR+RULE_GROUP_END_CHAR), WHITESPACE_CHARS);	//get the trimmed value of the property, which is followed by a divider characteror the end of the group
			rule.getDeclarations().add(new NameValuePair<String, String>(propertyName, propertyValue));	//add this declaration to the rule
			switch(parseReader.peek())	//look at the next character
			{
				case RULE_GROUP_END_CHAR:	//if this is the end of the rule group
					return true;	//report that we found the end of the rule group
				case -1:	//if we reached the end of the file
					return false;	//show that we reached the end of the input
				default:	//if this is not a declaration block end or the end of the stream, there can only be a declaration separator
					parseReader.readExpectedChar(DECLARATION_SEPARATOR_CHAR);	//skip the declaration separator
					break;
			}
		}
	}
	
	/**Parses an input stream is expected to contain a block of declaration names and values, with appropriate beginning and ending delimiters.
	@param parseReader The reader from which to retrieve characters.
	@param rule The rule which will hold the declarations.
	@except IOException Thrown if there is an error reading the input, the input is invalid, or the end of the input was reached unexpectedly.
	*/
	public static void parseDeclarationBlock(final ParseReader parseReader, final Rule rule) throws IOException
	{
		parseReader.readExpectedChar(RULE_GROUP_START_CHAR);	//read the start of this group
			//parse this group of rules; if we hit the end of the stream, reading the end-of-style-rule character will throw the correct error
		parseDeclarations(parseReader, rule);	//parse the declarations inside the block
		parseReader.readExpectedChar(RULE_GROUP_END_CHAR);	//read the end of the group, throwing an EOF exception as needed
	}
	
	/**Parses an input stream that is expected to begin with a CSS rule.
	@param parseReader The reader from which to retrieve characters.
	@except IOException Thrown if there is an error reading the input, the input is invalid, or the end of the input was reached unexpectedly.
	@return A new CSS rule constructed from the reader.
	*/
	protected static Rule parseRule(final ParseReader parseReader) throws IOException
	{
		final Rule rule=new Rule();	//create a new rule
		char nextDelimiter;	//we'll keep track of the next delimiter after this group
		Selector selector=new Selector();	//create a new selector
		Combinator combinator=null;	//the first combinator will be null
		do
		{
			skipWhitespaceCommentsEOF(parseReader);	//skip over whitespace and comments
				//read to the end of a simple selector chain
			final String simpleSelectorSequenceString=parseReader.readStringUntilChar(COMBINATOR_CHARS+SELECTOR_SEPARATOR_CHAR+RULE_GROUP_START_CHAR+"/");	//the end of each simple selector sequence will either be a combinator, comma, the start of the block of rules, or a comment TODO use a constnat for the start of a comment
			final List<SimpleSelector> simpleSelectorSequence=new ArrayList<SimpleSelector>();	//create a new list of simple selectors			
			final ReaderTokenizer simpleSelectorSequenceTokenizer=new ReaderTokenizer(new StringReader(simpleSelectorSequenceString), ""+CLASS_SELECTOR_DELIMITER+ID_SELECTOR_DELIMITER+PSEUDO_CLASS_DELIMITER);	//tokenize the simple selector sequence
			for(final String simpleSelectorString:simpleSelectorSequenceTokenizer)//for each simple selector string
			{
				final SimpleSelector simpleSelector;	//we'll create the appropriate simple selector
				final char simpleSelectorDelimiter=simpleSelectorSequenceTokenizer.getLastDelimiter();	//see which delimiter introduced this simple selector
				switch(simpleSelectorDelimiter)	//see which delimiter was previously encountered
				{
					case NULL_CHAR:	//if we haven't yet encountered a delimiter
						simpleSelector=new TypeSelector(simpleSelectorString);	//this is a type selector (accept the universal selector, '*', as a type selector)
						break;
					case CLASS_SELECTOR_DELIMITER:
						simpleSelector=new ClassSelector(simpleSelectorString);
						break;
					case ID_SELECTOR_DELIMITER:
						simpleSelector=new IDSelector(simpleSelectorString);
						break;
					case PSEUDO_CLASS_DELIMITER:
						simpleSelector=new PseudoClass(simpleSelectorString);
						break;
					default:
						throw new AssertionError("Unrecognized simple selector delimiter: "+simpleSelectorSequenceTokenizer.getLastDelimiter());
				}
				simpleSelectorSequence.add(simpleSelector);	//add this simple selector to the sequence
			}
			selector.add(new NameValuePair<Combinator, List<SimpleSelector>>(combinator, simpleSelectorSequence));	//add this combinator/simple selector sequence to the chain
			nextDelimiter=parseReader.peekChar();	//see what the next delimiter will be
			if(COMBINATOR_CHARS.indexOf(nextDelimiter)>=0)	//if the next character is a combinator
			{
				if(WHITESPACE_CHARS.indexOf(nextDelimiter)>=0)	//if we ran into whitespace (which can be around any combinator), see if the whitespace indicated a descendant combinator or was just a delimiter
				{
					skipWhitespaceCommentsEOF(parseReader);	//skip over whitespace and comments
					final char newDelimiter=parseReader.peekChar();	//see what the next delimiter will be
					if(COMBINATOR_CHARS.indexOf(newDelimiter)>=0)	//if the character after the whitespace is a combinator
					{
						nextDelimiter=newDelimiter;	//the whitespace wasn't a combinator, only a delimiter
						parseReader.readExpectedChar(nextDelimiter);	//skip the combinator						
					}
				}
				else	//if this is another combinator besides whitespace
				{
					parseReader.readExpectedChar(nextDelimiter);	//skip the combinator						
				}
				combinator=Combinator.valueOf(nextDelimiter);	//create a combinator from the next delimiter
			}
			if(nextDelimiter==SELECTOR_SEPARATOR_CHAR || nextDelimiter==RULE_GROUP_START_CHAR)	//if this marks the end of the selector
			{
				rule.getSelectors().add(selector);	//add this selector to the rule
				if(nextDelimiter==SELECTOR_SEPARATOR_CHAR)	//if there is another selector
				{
					parseReader.readExpectedChar(nextDelimiter);	//skip the selector separator
					selector=new Selector();	//create a new selector
					combinator=null;	//start out with no combinator
				}
			}
		}
		while(nextDelimiter!=RULE_GROUP_START_CHAR);	//keep going until we find the start of declaration block
		parseDeclarationBlock(parseReader, rule);	//parse the decaration block
		return rule;	//return the style rule we constructed
	}

	/**Parses an input stream that contains stylesheet information.
	@param parseReader The reader from which to retrieve characters.
	@param stylesheet The stylesheet being constructed.
	@except IOException Thrown if there is an error reading the input, the input is invalid, or the end of the input was reached unexpectedly.
	*/
	protected static void parseStylesheetContent(final ParseReader parseReader, final CSSStylesheet stylesheet) throws IOException
	{
		//the stylesheet strings we expect; make sure we put the AT_RULE_START after the other at-rule constants, because it represents an unknown at-rule
		final String[] EXPECTED_STYLESHEET_STRINGS={MEDIA_RULE_SYMBOL, PAGE_RULE_SYMBOL, FONT_FACE_RULE_SYMBOL, AT_RULE_START, CDO, COMMENT_START, ""};
		//the indexes of the stylesheet strings in our array
		final int MEDIA_RULE=0, PAGE_RULE=1, FONT_FACE_RULE=2, UNKNOWN_AT_RULE=3, XML_COMMENT_START=4, COMMENT=5;
		while(true)	//we'll keep processing rulesets and such until we run out of characters looking for whitespace
		{
			parseReader.skipCharsEOF(WHITESPACE_CHARS);	//skip over whitespace
			if(parseReader.isEOF())	//if we've hit the end of the stream of characters
				return;	//we're finished processing the stylesheet
			parseReader.resetPeek();	//reset peeking so that the next character peeked will reflect the next character to be read

			switch(parseReader.peekExpectedStrings(EXPECTED_STYLESHEET_STRINGS))	//see what we have next in the stylesheet
			{
				case MEDIA_RULE:	//if this is a media rule
//G***del Debug.trace("found media rule");
				  parseReader.readStringUntilChar('}');  //G***fix parsing media rules
					parseReader.readExpectedChar('}'); //G***fix parsing media rules
					break;	//G***fix
				case PAGE_RULE:	//if this is a page rule
//G***del Debug.trace("Found page rule"); //G***del
				  parseReader.readStringUntilChar('}');  //G***fix parsing page rules
					parseReader.readExpectedChar('}'); //G***fix parsing page rules
					break;	//G***fix
				case FONT_FACE_RULE:	//if this is a font face rule
//G***del Debug.trace("found font face");
				  parseReader.readStringUntilChar('}');  //G***fix parsing font-face rules
					parseReader.readExpectedChar('}'); //G***fix parsing font-face rules
					break;	//G***fix
				case UNKNOWN_AT_RULE:	//if this is an at-rule we don't know about
//G***del Debug.trace("found unknown at rule");
				  parseReader.readStringUntilChar('}');  //G***fix parsing at rules
					parseReader.readExpectedChar('}'); //G***fix parsing at rules
					break;	//G***fix
				case COMMENT:	//if this is a CSS comment
					parseReader.readStringUntilSkipString(COMMENT_END);	//read until the end of the comment
					break;
				case XML_COMMENT_START:	//if this is the start of an XML-style comment
					parseReader.readStringUntilSkipString(CDC);	//read until the end of the XML-style comment
					break;
				default:	//if we didn't find any of the above, we'll assume this is a ruleset
					stylesheet.getRules().add(parseRule(parseReader));	//parse a rule and add it to the stylesheet's list of rules
					break;
			}
//G***del System.out.println("Outside of switch statment.");	//G***del
		}
	}	

	/**Processes a CSS stylesheet.
	@param parseReader The reader from which to retrieve characters.
	@return stylesheet The constructed stylesheet.
	@except IOException Thrown if there is an error reading the input, the input is invalid, or the end of the input was reached unexpectedly.
	*/
	public CSSStylesheet process(final ParseReader parseReader) throws IOException
	{
		final CSSStylesheet stylesheet=new CSSStylesheet();	//create a new stylesheet
		parseStylesheetContent(parseReader, stylesheet);	//parse the stylesheet content
		return stylesheet;	//return the stylesheet we constructed
	}

}
