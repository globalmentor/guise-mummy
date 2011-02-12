/* GlobalMentor JavaScript Library
 * Copyright © 2005-2011 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

/*
 * GlobalMentor general JavaScript library.
 * Author: Garret Wilson
*/

//Array

/** An add() method for arrays, equivalent to Array.push(). */
Array.prototype.add=Array.prototype.push;

/** An enqueue() method for arrays, equivalent to Array.push(). */
Array.prototype.enqueue=Array.prototype.push;

/** A dequeue() method for arrays, equivalent to Array.shift(). */
Array.prototype.dequeue=Array.prototype.shift;

/**
 * Determines the index of the first match of a given object in the array using object.toString() if the object isn't
 * null.
 * 
 * @param regexp The regular expression of the string version of the object to find in the array.
 * @return The index of the matching object in the array, or -1 if a matching object is not in the array.
 */
Array.prototype.indexOfMatch=function(regexp)
{
	var length=this.length;	//get the length of the array
	for(var i=0; i<length; ++i)	//for each index
	{
		var object=this[i];	//get a reference to this object
		if(object!=null && object.toString().match(regexp))	//if this object isn't null and it matches the given regular expression
		{
			return i;	//return this index
		}
	}
	return -1;	//indicate that the object could not be found
};

/** Clears an array by removing every item at every index in the array. */
Array.prototype.clear=function()	//TODO del when no longer used
{
	this.splice(0, this.length);	//splice out all the elements
};

/**
 * Determines whether a match of the given regular expression is present in the array, using object.toString() if the
 * object isn't null.
 * 
 * @param regexp The regular expression of the string version of the object to find in the array.
 * @return true if a matching object is present in the array.
 */
Array.prototype.containsMatch=function(regexp)
{
	return this.indexOfMatch(regexp)>=0;	//see if a matching object is in the array
};

/**
 * Removes an item at the given index in the array.
 * 
 * @param index The index at which the element should be removed.
 * @return The element previously at the given index in the array.
 */
Array.prototype.remove=function(index)
{
	return this.splice(index, 1)[0];	//splice out the element and return it (note that this will not work on Netscape <4.06 or IE <=5.5; see http://www.samspublishing.com/articles/article.asp?p=30111&seqNum=3&rl=1)
};

var EMPTY_ARRAY=new Array();	//a shared empty array TODO create methods to make this read-only

//String

/**
 * Determines whether this string is in all lowercase.
 * 
 * @return true if the string is in all lowercase.
 */
String.prototype.isLowerCase=function()
{
	return this==this.toLowerCase();	//see if this substring matches the same string in all lowercase
};

/**
 * Determines whether this string is in all uppercase.
 * 
 * @return true if the string is in all uppercase.
 */
String.prototype.isUpperCase=function()
{
	return this==this.toUpperCase();	//see if this substring matches the same string in all uppercase
};

/**
 * Determines whether this string starts with the indicated substring.
 * 
 * @param substring The string to check to see if it is at the beginning of this string.
 * @return true if the given string is at the start of this string.
 */
String.prototype.startsWith=function(substring)
{
	return this.hasSubstring(substring, 0);	//see if this substring is at the beginning of the string
};

/**
 * Determines whether this string ends with the indicated substring.
 * 
 * @param substring The string to check to see if it is at the end of this string.
 * @return true if the given string is at the end of this string.
 */
String.prototype.endsWith=function(substring)
{
	return this.hasSubstring(substring, this.length-substring.length);	//see if this substring is at the end of the string
};

/**
 * Determines if this string has the given substring at the given index in the string.
 * 
 * @param substring The substring to compare.
 * @param index The index to compare.
 * @return true if the given substring matches the characters as the given index of this string.
 */
String.prototype.hasSubstring=function(substring, index)
{
	var length=substring.length;	//get the length of the substring
	if(index<0 || this.length<index+length)	//if the range doesn't fall within this string
	{
		return false;	//the substring can't start this string
	}
	for(var i=length-1; i>=0; --i)	//for each character in the substring
	{
		if(this.charAt(i+index)!=substring.charAt(i))	//if these characters don't match
		{
			return false;	//the substring doesn't match
		}
	}
	return true;	//show that the string matches
}

/**
 * Splits a string and returns an associative array with the contents. Each the value of each key of the associative
 * array will be set to true. Empty and null splits will be ignored.
 * 
 * @param separator The optional separator string or regular expression; if no separator is provided, the entire string
 *          is placed in the set.
 * @param limit The optional limit to the number of splits to be found.
 * @return An associative array with they keys set to the elements of the split string.
 */
String.prototype.splitSet=function(separator, limit)
{
	var splitSet=new Object();	//create an associative array
	var splits=this.split(separator, limit);	//split the string into an array
	for(var i=splits.length-0; i>=0; --i)	//for each split
	{
		var split=splits[i];	//get this split
		if(split)	//if this is a valid split
		{
			splitSet[split]=true;	//add this split to the set
		}
	}
	return splitSet;	//return the set of splits
};

//StringBuilder

/**
 * A class for concatenating string with more efficiency than using the additive operator. Inspired by Nicholas C.
 * Zakas, _Professional JavaScript for Web Developers_, Wiley, 2005, p. 97.
 * 
 * @param strings (...) Zero or more strings with which to initialize the string builder.
 */
function StringBuilder(strings)
{
	this._strings=new Array();	//create an array of strings
	if(!StringBuilder.prototype._initialized)
	{
		StringBuilder.prototype._initialized=true;

		/**
		 * Appends a string to the string builder.
		 * 
		 * @param string The string to append.
		 * @return A reference to the string builder.
		 */
		StringBuilder.prototype.append=function(string)
		{
			this._strings.add(string);	//add this string to the array
			return this;
		};

		/**
		 * Removes the last string appended to the string builder.
		 * 
		 * @return The string removed from the string builder.
		 */
		StringBuilder.prototype.unpend=function(string)
		{
			return this._strings.pop();	//pop the last element from the array
		};

		/** @return A single string containing the contents of the string builder. */
		StringBuilder.prototype.toString=function()
		{
			return this._strings.join("");	//join with no separator and return the strings
		};
	}
	var argumentCount=arguments.length;	//find out how many arguments there are
	for(var i=0; i<argumentCount; ++i)	//for each argument
	{
		this.append(arguments[i]);	//append this string
	}
}

//Map

/**
 * A class encapsulating keys and values. This is a convenience class for constructing an Object with a given set of
 * keys and values, as the JavaScript shorthand notation does not allow non-literal key names. Values may be accessed in
 * normal object[key]==value syntax. The constructor allows any number of key/value pairs as arguments.
 * 
 * @param key A key with which to associate a value.
 * @param value The value associated with the preceding key.
 */
function Map(key, value)
{
	var argumentCount=arguments.length;	//find out how many arguments there are
	for(var i=0; i+1<argumentCount; i+=2)	//for each key/value combination (counting by twos)
	{
		this[arguments[i]]=arguments[i+1];	//store the value keyed to the key
	}
}

//Set

/**
 * A class encapsulating keys mapped to the value true. This is a convenience class for constructing an Object with a
 * given set of keys, as the JavaScript shorthand notation does not allow non-literal key names. Values may be tested in
 * normal if(object[item]) syntax. The constructor allows any number of items as arguments.
 * 
 * @param items The items to store in the set.
 */
function Set(items)
{
	for(var i=arguments.length-1; i>=0; --i)	//for each item (order doesn't matter in a set)
	{
		this[arguments[i]]=true;	//store the item as a key with a value of true
	}
}

//Point

/**
 * A class encapsulating a point.
 * 
 * @param x The X coordinate, stored under this.x;
 * @param y The Y coordinate, stored under this.y;
 */
function Point(x, y) {this.x=x; this.y=y;}

//Rectangle

/**
 * A class encapsulating a rectangle.
 * 
 * @param coordinates The position of the top left corner of the rectangle, stored under this.coordinates.
 * @param size The size of the rectangle, stored under this.size.
 * @var x, y The coordinates of the upper-left corner of the rectangle.
 * @var width, height The dimensions of the rectangle.
 */
function Rectangle(coordinates, size)
{
	this.coordinates=coordinates;
	this.x=coordinates.x;
	this.y=coordinates.y;
	this.size=size;
	this.width=size.width;
	this.height=size.height;
}

//Size

/**
 * A class encapsulating a size.
 * 
 * @param width The width, stored under this.width;
 * @param height The height coordinate, stored under this.height;
 */
function Size(width, height) {this.width=width; this.height=height;}

//URI

/**
 * A class for parsing and encapsulting a URI according to RFC 2396, "Uniform Resource Identifiers (URI): Generic
 * Syntax".
 * 
 * @param uriString The string form of the URI.
 * @see http://www.ietf.org/rfc/rfc2396.txt
 * @var scheme The scheme of the URI.
 * @var authority The authority of the URI.
 * @var path The path of the URI.
 * @var query The query of the URI.
 * @var fragment The fragment of the URI.
 * @var parameters An associative array of parameter name/value combinations.
 */
function URI(uriString)
{
	if(!URI.prototype._initialized)
	{
		URI.prototype._initialized=true;

		URI.prototype.URI_REGEXP=/^(([^:\/?#]+):)?(\/\/([^\/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?/;	//the regular expression for parsing URIs, from http://www.ietf.org/rfc/rfc2396.txt
	}
	this.URI_REGEXP.test(uriString);	//split out the components of the URI using a regular expression
	this.scheme=RegExp.$2;	//save the URI components
	this.authority=RegExp.$4;
	this.path=RegExp.$5;
	this.query=RegExp.$7;
	this.parameters=new Object();	//create a new associative array to hold parameters
	if(this.query)	//if a query is given
	{
		var queryComponents=this.query.split("&");	//split up the query components
		var parameterCount=queryComponents.length;	//find out how many parameters there are
		for(var i=0; i<parameterCount; ++i)	//for each parameter
		{
			var parameterComponents=queryComponents[i].split("=");	//split out the parameter components
			var parameterName=decodeURIComponent(parameterComponents[0]);	//get and decode the parameter name
			var parameterValue=parameterComponents.length>1 ? decodeURIComponent(parameterComponents[1]) : null;	//get and decode the parameter value
			this.parameters[parameterName]=parameterValue;	//store this parameter name/value combination
		}
	}
	this.fragment=RegExp.$9;
}

//Console

/*
 * Prevents logging errors if the browser has no log support.
 * @see <a href="http://stackoverflow.com/questions/217957/how-to-print-debug-messages-in-the-google-chrome-javascript-console/2757552#2757552">How to print debug messages in the Google Chrome Javascript Console</a>
 */
if(!window.console)
{
	window.console = {};
}
window.console.log = console.log || function(){};
window.console.warn = console.warn || function(){};
window.console.error = console.error || function(){};
window.console.info = console.info || function(){};
