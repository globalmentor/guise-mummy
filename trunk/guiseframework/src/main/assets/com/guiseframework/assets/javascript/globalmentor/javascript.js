/* GlobalMentor JavaScript Library
 * Copyright © 2005-2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
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
/**
 * Returns an array representing the contents of the given object. This implementation recognizes other arrays and the
 * arguments of a function; along with anything else that is iterable by virtue of having a length property and a []
 * access method.
 * 
 * @param object The non-null object the contents of which to return as an array.
 * @returns An array containing the contents of the given object.
 * @see <a href="http://www.prototypejs.org/api/array/from">Prototype Array.from</a>
 */
Array.from = function(object)
{
	if(object instanceof Array) //if the object is an array
	{
		return object;
	}
	else
	//otherwise, try to iterate using length and []
	{
		var array = new Array(); //create a new array
		for( var i = 0, length = object.length; i < length; ++i) //for each element
		{
			array.add(object[i]); //add this element to our array
		}
		return array; //return the new array we created
	}
};

/** An add() method for arrays, equivalent to Array.push(). */
Array.prototype.add = Array.prototype.push;

/** An enqueue() method for arrays, equivalent to Array.push(). */
Array.prototype.enqueue = Array.prototype.push;

/** A dequeue() method for arrays, equivalent to Array.shift(). */
Array.prototype.dequeue = Array.prototype.shift;

if(typeof Array.prototype.indexOf == "undefined") //defined in JavaScript 1.6
{
	/**
	 * Determines the index of the first occurrence of a given object in the array.
	 * 
	 * @param object The object to find in the array.
	 * @returns The index of the object in the array, or -1 if the object is not in the array.
	 */
	Array.prototype.indexOf = function(object)
	{
		var length = this.length; //get the length of the array
		for( var i = 0; i < length; ++i) //for each index
		{
			if(this[i] == object) //if this object is the requested object
			{
				return i; //return this index
			}
		}
		return -1; //indicate that the object could not be found
	};
}

/**
 * Determines the index of the first match of a given object in the array using object.toString() if the object isn't
 * null.
 * 
 * @param regexp The regular expression of the string version of the object to find in the array.
 * @returns The index of the matching object in the array, or -1 if a matching object is not in the array.
 */
Array.prototype.indexOfMatch = function(regexp)
{
	var length = this.length; //get the length of the array
	for( var i = 0; i < length; ++i) //for each index
	{
		var object = this[i]; //get a reference to this object
		if(object != null && object.toString().match(regexp)) //if this object isn't null and it matches the given regular expression
		{
			return i; //return this index
		}
	}
	return -1; //indicate that the object could not be found
};

/** Clears an array by removing every item at every index in the array. */
Array.prototype.clear = function() //TODO del when no longer used
{
	this.splice(0, this.length); //splice out all the elements
};

/**
 * Determines whether the given object is present in the array.
 * 
 * @param object The object for which to check.
 * @returns true if the object is present in the array.
 */
Array.prototype.contains = function(object)
{
	return this.indexOf(object) >= 0; //see if the object is in the array
};

/**
 * Determines whether a match of the given regular expression is present in the array, using object.toString() if the
 * object isn't null.
 * 
 * @param regexp The regular expression of the string version of the object to find in the array.
 * @returns true if a matching object is present in the array.
 */
Array.prototype.containsMatch = function(regexp)
{
	return this.indexOfMatch(regexp) >= 0; //see if a matching object is in the array
};

/**
 * Removes an item at the given index in the array.
 * 
 * @param index The index at which the element should be removed.
 * @returns The element previously at the given index in the array.
 */
Array.prototype.remove = function(index)
{
	return this.splice(index, 1)[0]; //splice out the element and return it (note that this will not work on Netscape <4.06 or IE <=5.5; see http://www.samspublishing.com/articles/article.asp?p=30111&seqNum=3&rl=1)
};

/**
 * Removes an item from the array. If the item is not contained in the array, no action is taken.
 * 
 * @param item The item to be removed.
 * @returns The removed item.
 */
Array.prototype.removeItem = function(item)
{
	var index = this.indexOf(item); //get the index of the item
	if(index >= 0) //if the item is contained in the array
	{
		return this.remove(index); //remove the item at the index
	}
};

var EMPTY_ARRAY = new Array(); //a shared empty array TODO create methods to make this read-only

//Function

/**
 * Creates a new function that functions exactly as does the original function, except that it provides the given
 * variable to appear as "this" to the new function. Any other given arguments will be inserted before the actual
 * arguments when the function is invoked.
 * 
 * @param newThis The variable to appear as "this" when the function is called.
 * @param extraArguments The new arguments, if any, to appear at the first of the arguments when the new function is
 *          called.
 * @returns A new function bound to the given this.
 * @see <a href="http://www.prototypejs.org/api/function/bind">Prototype Function.bind</a>
 */
Function.prototype.bind = function()
{
	var originalFunction = this; //save a reference to this function instance to allow calling this via closure
	var extraArguments = Array.from(arguments); //get the provided arguments
	var newThis = extraArguments.shift(); //get the first argument, which provides the new this when calling the function, and leaving the remaining arguments to be passed to the function
	return function() //create and send back a new function
	{
		originalFunction.apply(newThis, extraArguments.length != 0 ? extraArguments.concat(Array.from(arguments)) : arguments); //the new function will call the original function with the new arguments followed by whatever arguments are given, but using the given this instead of whatever this is passed when the function is called
	};
};

/**
 * Creates a new function that functions exactly as does the original function, except that it provides the given
 * variable to appear as "this" to the new function. The original this present when the function is invoked will be
 * inserted as the first argument. Any other given arguments will be inserted before the actual arguments when the
 * function is invoked.
 * 
 * @param newThis The variable to appear as "this" when the function is called.
 * @param extraArguments The new arguments, if any, to appear at the first of the arguments when the new function is
 *          called.
 * @returns A new function bound to the given this.
 * @see <a href="http://www.prototypejs.org/api/function/bind">Prototype Function.bind</a>
 */
Function.prototype.bindOldThis = function()
{
	var originalFunction = this; //save a reference to this function instance to allow calling this via closure
	var extraArguments = Array.from(arguments); //get the provided arguments
	var newThis = extraArguments.shift(); //get the first argument, which provides the new this when calling the function, and leaving the remaining arguments to be passed to the function
	return function() //create and send back a new function
	{
		var actualArguments = Array.from(arguments); //get the actual arguments as an array
		var newArguments = extraArguments.length != 0 ? extraArguments.concat(actualArguments) : actualArguments; //if extra arguments were supplied, use them at the front of the array
		newArguments.unshift(this); //insert the old this at the beginning of the arguments
		originalFunction.apply(newThis, newArguments); //the new function will call the original function with the new arguments followed by the new arguments, but using the given this instead of whatever this is passed when the function is called
	};
};

//String

/**
 * Determines whether the given substring is present in the string.
 * 
 * @param substring The substring for which to check.
 * @returns true if the substring is present in the string.
 */
String.prototype.contains = function(substring)
{
	return this.indexOf(substring) >= 0; //see if the substring is in the string
};

/**
 * Determines whether this string is in all lowercase.
 * 
 * @returns true if the string is in all lowercase.
 */
String.prototype.isLowerCase = function()
{
	return this == this.toLowerCase(); //see if this substring matches the same string in all lowercase
};

/**
 * Determines whether this string is in all uppercase.
 * 
 * @returns true if the string is in all uppercase.
 */
String.prototype.isUpperCase = function()
{
	return this == this.toUpperCase(); //see if this substring matches the same string in all uppercase
};

/**
 * Determines whether this string starts with the indicated substring.
 * 
 * @param substring The string to check to see if it is at the beginning of this string.
 * @returns true if the given string is at the start of this string.
 */
String.prototype.startsWith = function(substring)
{
	return this.hasSubstring(substring, 0); //see if this substring is at the beginning of the string
};

/**
 * Determines whether this string ends with the indicated substring.
 * 
 * @param substring The string to check to see if it is at the end of this string.
 * @returns true if the given string is at the end of this string.
 */
String.prototype.endsWith = function(substring)
{
	return this.hasSubstring(substring, this.length - substring.length); //see if this substring is at the end of the string
};

/**
 * Determines if this string has the given substring at the given index in the string.
 * 
 * @param substring The substring to compare.
 * @param index The index to compare.
 * @returns true if the given substring matches the characters as the given index of this string.
 */
String.prototype.hasSubstring = function(substring, index)
{
	var length = substring.length; //get the length of the substring
	if(index < 0 || this.length < index + length) //if the range doesn't fall within this string
	{
		return false; //the substring can't start this string
	}
	for( var i = length - 1; i >= 0; --i) //for each character in the substring
	{
		if(this.charAt(i + index) != substring.charAt(i)) //if these characters don't match
		{
			return false; //the substring doesn't match
		}
	}
	return true; //show that the string matches
};

/**
 * Splits a string and returns an associative array with the contents. Each the value of each key of the associative
 * array will be set to true. Empty and null splits will be ignored.
 * 
 * @param separator The optional separator string or regular expression; if no separator is provided, the entire string
 *          is placed in the set.
 * @param limit The optional limit to the number of splits to be found.
 * @returns An associative array with they keys set to the elements of the split string.
 */
String.prototype.splitSet = function(separator, limit)
{
	var splitSet = new Object(); //create an associative array
	var splits = this.split(separator, limit); //split the string into an array
	for( var i = splits.length - 0; i >= 0; --i) //for each split
	{
		var split = splits[i]; //get this split
		if(split) //if this is a valid split
		{
			splitSet[split] = true; //add this split to the set
		}
	}
	return splitSet; //return the set of splits
};

/**
 * Trims the given string of whitespace.
 * 
 * @see <a
 *      href="https://lists.latech.edu/pipermail/javascript/2004-May/007570.html">https://lists.latech.edu/pipermail/javascript/2004-May/007570.html</a>
 */
String.prototype.trim = function()
{
	return this.replace(/^\s+|\s+$/g, ""); //replace beginning and ending whitespace with nothing
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
	this._strings = new Array(); //create an array of strings
	if(!StringBuilder.prototype._initialized)
	{
		StringBuilder.prototype._initialized = true;

		/**
		 * Appends a string to the string builder.
		 * 
		 * @param string The string to append.
		 * @returns A reference to the string builder.
		 */
		StringBuilder.prototype.append = function(string)
		{
			this._strings.add(string); //add this string to the array
			return this;
		};

		/**
		 * Removes the last string appended to the string builder.
		 * 
		 * @returns The string removed from the string builder.
		 */
		StringBuilder.prototype.unpend = function(string)
		{
			return this._strings.pop(); //pop the last element from the array
		};

		/** @returns A single string containing the contents of the string builder. */
		StringBuilder.prototype.toString = function()
		{
			return this._strings.join(""); //join with no separator and return the strings
		};
	}
	var argumentCount = arguments.length; //find out how many arguments there are
	for( var i = 0; i < argumentCount; ++i) //for each argument
	{
		this.append(arguments[i]); //append this string
	}
}

//JSON

/**
 * A set of utilities for working with JSON.
 * 
 * @see <a href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>
 */
var JSON =
{

	/** The regular expression for testing JSON expressions. */
	_TEST_REGEXP : /[^,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t]/,

	/** The regular expression for testing JSON expressions. */
	_REPLACE_REGEXP : /\"(\\.|[^\"\\])*\"/g,

	/**
	 * Evaluates a JSON expression, returning the result of the expression.
	 * 
	 * @param json The JSON expression to evaluate.
	 * @returns The result of the JSON evaluation
	 * @exception If the JSON expression contains more than the valid JSON subset or is otherwise invalid.
	 */
	evaluate : function(json)
	{
		if(this._TEST_REGEXP.test(json.replace(this._REPLACE_REGEXP, ""))) //if the JSON expression is invalid
		{
			throw "Invalid JSON expression: " + json;
		}
		return eval("(" + json + ")"); //evaluate and return the JSON expression
	},

	/**
	 * Serializes an object using JSON. This implementation does not yet correctly escape characters.
	 * 
	 * @param object The object to serialize.
	 */
	serialize : function(object)
	{
		if(object != null) //if the object is not null
		{
			switch(typeof object)
			//see what type this object is
			{
				case "string":
					return "\"" + object.replace(/["\\\b\f\n\r\t]/g, this.escapeStringChar) + "\""; //return the string surrounded by quotes
				case "boolean":
				case "number":
					return object.toString(); //return the string version of the
				default:
					if(object instanceof Array)
					{
						var length = object.length; //get the length of the array
						if(length == 0) //if the array is empty
						{
							return "[]"; //return an empty array serialization
						}
						var serializedObjectArray = new Array(); //create a new array for serializating the objects
						for( var i = 0; i < length; ++i) //for each array element
						{
							serializedObjectArray[i] = this.serialize(object[i]); //place the serialization of the array element into the new array
						}
						return "[" + serializedObjectArray.join(",") + "]"; //join the object serializations, separated by commas, and surround the list with the array delimiters
					}
					else
					//if this is a general object
					{
						var stringBuilder = new StringBuilder("{"); //create a new string builder with the beginning object delimiter
						var propertyCount = 0; //keep track of the properties
						for( var property in object) //for each object property
						{
							stringBuilder.append(this.serialize(property)).append(":").append(this.serialize(object[property])).append(","); //"property":value,
							++propertyCount; //show that we serialized another property
						}
						if(propertyCount > 0) //if there were properties
						{
							stringBuilder.unpend(); //remove the last value separator
						}
						stringBuilder.append("}"); //append the ending object delimiter
						return stringBuilder.toString(); //return the serialized object 
					}
			}
		}
		else
		//if the object is null
		{
			return "null";
		}
	},

	/**
	 * Escapes a character of a JSON string.
	 * 
	 * @param restricted The restricted character as a string.
	 * @returns The escaped form of the given character string.
	 */
	escapeStringChar : function(restricted)
	{
		switch(restricted)
		{
			case "\b":
				return "\\b";
			case "\f":
				return "\\f";
			case "\n":
				return "\\n";
			case "\r":
				return "\\r";
			case "\t":
				return "\\t";
			default: //for all other characters
				return "\\" + restricted; //escape the character with a backslash
		}
	}
};

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
	var argumentCount = arguments.length; //find out how many arguments there are
	for( var i = 0; i + 1 < argumentCount; i += 2) //for each key/value combination (counting by twos)
	{
		this[arguments[i]] = arguments[i + 1]; //store the value keyed to the key
	}
}

/**
 * Reverses an associative array by associating the keys with the given values. If multiple keys have the same value, it
 * is undefined which of the keys will be retained as a value.
 * @param object The associative array to reverse; if a Map is provided, a Map will be returned.
 * @return A new associative array (a Map, if a Map was provided) with they keys associated with the values.
 */
Map.reverse = function(object)
{
	var map = object instanceof Map ? new Map() : {}; //create a new object
	for( var key in object) //for each key property
	{
		map[object[key]] = key; //associate the key with the value
	}
	return map; //return the new map
};

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
	for( var i = arguments.length - 1; i >= 0; --i) //for each item (order doesn't matter in a set)
	{
		this[arguments[i]] = true; //store the item as a key with a value of true
	}
}

//Point

/**
 * A class encapsulating a point.
 * 
 * @param x The X coordinate, stored under this.x;
 * @param y The Y coordinate, stored under this.y;
 */
function Point(x, y)
{
	this.x = x;
	this.y = y;
}

//Rectangle

/**
 * A class encapsulating a rectangle.
 * 
 * @param coordinates The position of the top left corner of the rectangle, stored under this.coordinates.
 * @param size The size of the rectangle, stored under this.size.
 * @property x The coordinates of the upper-left corner of the rectangle.
 * @property y The coordinates of the upper-left corner of the rectangle.
 * @property width The dimensions of the rectangle.
 * @property height The dimensions of the rectangle.
 */
function Rectangle(coordinates, size)
{
	this.coordinates = coordinates;
	this.x = coordinates.x;
	this.y = coordinates.y;
	this.size = size;
	this.width = size.width;
	this.height = size.height;
}

//Size

/**
 * A class encapsulating a size.
 * 
 * @param width The width, stored under this.width;
 * @param height The height coordinate, stored under this.height;
 */
function Size(width, height)
{
	this.width = width;
	this.height = height;
}

//MIME Content Type

/**
 * A class for parsing and encapsulating a MIME content type according to RFC 2045, "Multipurpose Internet Mail
 * Extensions - (MIME) Part One: Format of Internet Message Bodies".
 * 
 * @param string The string form of the MIME content type.
 * @property type The top-level type of the content type.
 * @property subType The sub-type of the content type.
 * @property parameters An associative array of parameter name/value combinations.
 * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 - Multipurpose Internet Mail Extensions - (MIME) Part
 *      One: Format of Internet Message Bodies</a>
 */
function ContentType(string, _skipTest)
{
	this._string = string;
	if(!ContentType.prototype._initialized)
	{
		ContentType.prototype._initialized = true;

		/**
		 * Determines if this content matches the given type and subtype.
		 * @property type The top-level type of the content type.
		 * @property subType The sub-type of the content type, or <code>null</code> if only the type should be compared.
		 * @return <code>true</code> if this content type has the same type and optionally subtype.
		 */
		ContentType.prototype.match = function(type, subType)
		{
			return this.type == type && (!subType || this.subType == subType);
		};

		/** @return <code>true</code> if this content type represents some sort of audio content. */
		ContentType.prototype.isAudio = function()
		{
			if(this.type == "audio") //audio/* is always audio
			{
				return true;
			}
			if(this.match("application", "ogg")) //application/ogg is also audio
			{
				return true;
			}
			return false;
		};

		/** @returns The string version of the content type. */
		ContentType.prototype.toString = function()
		{
			return this._string; //return the string
		};
	}
	if(!_skipTest) //if we shouldn't skip testing
	{
		if(!ContentType.REGEXP.test(string)) //split out the components of the content type using a regular expression; if the string is not a content type
		{
			throw "Invalid MIME content type: " + string;
		}
	}
	this.type = RegExp.$1; //save the content type components
	this.subType = RegExp.$2;
	var param = RegExp.$4;
	this.parameters = new Object(); //create a new associative array to hold parameters
	if(param) //if there are parameters
	{
		var parameterComponents = param.split("\s*&\s*"); //split up the parameter components
		var parameterCount = parameterComponents.length; //find out how many parameters there are
		for( var i = 0; i < parameterCount; ++i) //for each parameter
		{
			var parameterComponents = parameterComponents[i].split("="); //split out the parameter components
			var parameterName = parameterComponents[0]; //get the parameter name
			var parameterValue = parameterComponents.length > 1 ? parameterComponents[1] : null; //get the parameter value
			this.parameters[parameterName] = parameterValue; //store this parameter name/value combination
		}
	}
}

/** The regular expression for parsing URIs, adapted from http://www.ietf.org/rfc/rfc2045.txt . */
ContentType.REGEXP = /^(\w+)\/(\w+)(;\s*(\S*)\s*)?/;

/**
 * Tests the given string to see if it meets the requirements for a MIME content type, returning a content type
 * instance.
 * @param string The string form of the content type.
 * @return A new content type instance from the parsed string, or <code>null</code> if the given string is not a valid
 *         content type.
 */
ContentType.test = function(string)
{
	if(!ContentType.REGEXP.test(string)) //split out the components of the content type using a regular expression; if the string is not a content type
	{
		return false;
	}
	return new ContentType(string, true); //create a new content type; don't test twice 
};

//URI
/**
 * A class for parsing and encapsulating a URI according to RFC 2396, "Uniform Resource Identifiers (URI): Generic
 * Syntax".
 * 
 * @param string The string form of the URI.
 * @param _skipTest Whether the URI regular expression has already been tested and parsing should continue with the
 *          latest regular expression results; for internal use only.
 * @throws if the given string is not a valid URI (if _skipText is <code>false</code>).
 * @property scheme The scheme of the URI.
 * @property authority The authority of the URI.
 * @property path The path of the URI.
 * @property query The query of the URI.
 * @property fragment The fragment of the URI.
 * @property parameters An associative array of parameter name/value combinations.
 * @see <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396 - Uniform Resource Identifiers (URI): Generic Syntax</a>
 */
function URI(string, _skipTest)
{
	this._string = string
	if(!URI.prototype._initialized)
	{
		URI.prototype._initialized = true;

		/** @returns The string version of the URI. */
		URI.prototype.toString = function()
		{
			return this._string; //return the string
		};
	}
	if(!_skipTest) //if we shouldn't skip testing
	{
		if(URI.REGEXP.test(string)) //split out the components of the URI using a regular expression; if the string is not a URI
		{
			throw "Invalid URI: " + string;
		}
	}
	this.scheme = RegExp.$2; //save the URI components
	this.authority = RegExp.$4;
	this.path = RegExp.$5;
	this.query = RegExp.$7;
	this.parameters = new Object(); //create a new associative array to hold parameters
	if(this.query) //if a query is given
	{
		var queryComponents = this.query.split("&"); //split up the query components
		var parameterCount = queryComponents.length; //find out how many parameters there are
		for( var i = 0; i < parameterCount; ++i) //for each parameter
		{
			var parameterComponents = queryComponents[i].split("="); //split out the parameter components
			var parameterName = decodeURIComponent(parameterComponents[0]); //get and decode the parameter name
			var parameterValue = parameterComponents.length > 1 ? decodeURIComponent(parameterComponents[1]) : null; //get and decode the parameter value
			this.parameters[parameterName] = parameterValue; //store this parameter name/value combination
		}
	}
	this.fragment = RegExp.$9;
}

/** The regular expression for parsing URIs, from http://www.ietf.org/rfc/rfc2396.txt . */
URI.REGEXP = /^(([^:\/?#]+):)?(\/\/([^\/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?/;

/**
 * Tests the given string to see if it meets the requirements for a URI, returning a URI instance.
 * @param string The string form of the URI.
 * @return A new URI instance from the parsed string, or <code>null</code> if the given string is not a valid URI.
 */
URI.test = function(string)
{
	if(!URI.REGEXP.test(string)) //split out the components of the URI using a regular expression; if the string is not a URI
	{
		return false;
	}
	return new URI(string, true); //create a new URI; don't test twice 
};

//Console

/**
 * Prevents logging errors if the browser has no log support.
 * 
 * @see <a
 *      href="http://stackoverflow.com/questions/217957/how-to-print-debug-messages-in-the-google-chrome-javascript-console/2757552#2757552">How
 *      to print debug messages in the Google Chrome Javascript Console</a>
 */
if(!window.console)
{
	window.console = {};
}
window.console.log = console.log || function()
{};
window.console.warn = console.warn || function()
{};
window.console.error = console.error || function()
{};
window.console.info = console.info || function()
{};
