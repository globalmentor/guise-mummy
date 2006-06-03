/*Guise(TM) JavaScript support routines
Copyright (c) 2005-2006 GlobalMentor, Inc.

This script expects the following variables to be defined:
navigator.userAgentName The name of the user agent, such as "Firefox", "Mozilla", "MSIE", or "Opera".
navigator.userAgentVersionNumber The version of the user agent stored as a number.
*/

/*Guise AJAX Request Format, content type application/x-guise-ajax-request+xml
<request>
	<init/>	<!--initializes the page, requesting all frames to be resent-->
	<events>	<!--the list of events (zero or more)-->
		<form	<!--information resulting from form changes, analogous to that in an HTTP POST-->
			exhaustive="true|false"	<!--indicates whether the event contains values for all form controls (defaults to false)-->
			provisional="true|false"	<!--indicates whether the value is a provisional value that has not yet been accepted by the user (defaults to false)-->
		>
			<control name="">	<!--a control change (zero or more)-->
				<!--the value of the control (putting the value into an attribute could corrupt the value because of XML attribute canonicalization rules-->
			</control>
		</form>
		<drop>	<!--the end of a drag-and-drop operation-->
			<source id=""/>	<!--the element that was the source of the drag and drop operation-->
			<target id=""/>	<!--the element that was is the target of the drag and drop operation-->
			<mouse x="" y=""/>	<!--the mouse information at the time of the drop-->
		</drop>
		<action	<!--an action on a component-->
			componentID=""	<!--the ID of the component-->
			targetID=""	<!--the ID of the target element on which the action occurred-->
			actionID=""	<!--the action identifier-->
		/>
		<mouseEnter|mouseExit>	<!--a mouse event related to a component-->
			<viewport x="" y="" width="" height=""/>	<!--information on the viewport (scroll position and size)-->
			<component id="" x="" y="" width="" height=""/>	<!--information on the component that was the target of the mouse event (in absolute terms)-->
			<target id="" x="" y="" width="" height=""/>	<!--information on the element that was the target of the mouse event (in absolute terms)-->
			<mouse x="" y=""/>	<!--the mouse information at the time of the drop (in fixed terms)-->
		</mouseEnter|mouseExit>
	</events>
</request>
*/

/*Guise AJAX Response Format, content type application/x-guise-ajax-response+xml
<response>
	<patch></patch>	<!--XML elements to be patched into the existing DOM tree.-->
	<attribute id="" name="" value=""></attribute>	<!--the new name and value of an attribute of an element with the given ID to be set (or removed if the value is null)-->
	<remove id=""/>	<!--ID of the XML element to be removed from the existing DOM tree-->
	<navigate>uri</navigate>	<!--URI of another page to which to navigate-->
	<frame></frame>	<!--definition of a frame to show-->
</response>
*/

/*Guise modal windows
Guise creates a div element and places it as a layer in the z-order behind the top frame when modal frames are needed.
To work around the IE6 bug where select elements are windowed controls, Guise places a separate transparant IFrame behind the modal div element if IE6 is being used.
This blocks out IE6 select elements, and does not help non-modal frames.
See http://dotnetjunkies.com/WebLog/jking/archive/2003/07/21/488.aspx .
See http://homepage.mac.com/igstudio/design/ulsmenus/vertical-uls-iframe.html .
*/

/**Rollovers
For every component marked with the "mouseListener" class, Guise will send mouseover and mouseout events.
Guise will also automatically add and remove a "rollover" class to the component and every subelement that is part of the component
(that is, every element that has a component ID-derived ID (i.e. "componentID-XXX") before sending the mouse event.
*/

//TODO before sending a drop event, send a component update for the drop target so that its value will be updated; or otherwise make sure the value is synchronized

/**See if the browser is IE6.*/
var isUserAgentIE6=navigator.userAgentName=="MSIE" && navigator.userAgentVersionNumber<7;

var AJAX_ENABLED=true;	//TODO allow this to be configured

/**See if the browser is Safari.*/
var isSafari=navigator.userAgent.indexOf("Safari")>=0;	//TODO use a better variable; do better checks; update Guise server routines to check for Safari

/**The URI of the XHTML namespace.*/
var XHTML_NAMESPACE_URI="http://www.w3.org/1999/xhtml";

/**The URI of the GuiseML namespace.*/
//TODO use var GUISE_ML_NAMESPACE_URI="http://guiseframework.com/id/ml#";

/**The class prefix of a menu.*/
//TODO del when works var MENU_CLASS_PREFIX="menu-";

/**The class suffix for a tab.*/
var TAB_CLASS_SUFFIX="-tab";
/**The class suffix for a selected tab.*/
//TODO del var TAB_SELECTED_CLASS_SUFFIX="-tab-selected";

/**The class suffix of a decorator.*/
//TODO del var DECORATOR_CLASS_PREFIX="-decorator";

/**The prefix for Guise state-related attributes, which shouldn't be removed when elements are synchronized.
When more states are added, the GuiseAJAX.prototype.NON_REMOVABLE_ATTRIBUTE_SET should be updated.
*/
var GUISE_STATE_ATTRIBUTE_PREFIX="guiseState";

/**The state attribute for width.*/
var GUISE_STATE_WIDTH_ATTRIBUTE=GUISE_STATE_ATTRIBUTE_PREFIX+"Width";
/**The state attribute for height.*/
var GUISE_STATE_HEIGHT_ATTRIBUTE=GUISE_STATE_ATTRIBUTE_PREFIX+"Height";

/**The enumeration of recognized styles.*/
var STYLES=
{
	/**A component element that can be clicked as an action.*/
	ACTION: "action",
	AXIS_X: "axisX",
	AXIS_Y: "axisY",
	/**A general component.*/
	COMPONENT: "component",
	DRAG_SOURCE: "dragSource",
	DRAG_HANDLE: "dragHandle",
	DROP_TARGET: "dropTarget",
	OPEN_EFFECT_REGEXP: /^openEffect-.+$/,
	MOUSE_LISTENER: "mouseListener",
	ROLLOVER: "rollover",
	SLIDER_CONTROL: "sliderControl",
	SLIDER_CONTROL_THUMB: "sliderControl-thumb",
	SLIDER_CONTROL_TRACK: "sliderControl-track",
	MENU: "dropMenu",
	MENU_BODY: "dropMenu-body",
	MENU_CHILDREN: "dropMenu-children",
	FRAME_TETHER: "frame-tether"
};

//Array

/**An add() method for arrays, equivalent to Array.push().*/
Array.prototype.add=Array.prototype.push;

/**An enqueue() method for arrays, equivalent to Array.push().*/
Array.prototype.enqueue=Array.prototype.push;

/**A dequeue() method for arrays, equivalent to Array.shift().*/
Array.prototype.dequeue=Array.prototype.shift;

/**Determines the index of the first occurrence of a given object in the array.
@param object The object to find in the array.
@return The index of the object in the array, or -1 if the object is not in the array.
*/
Array.prototype.indexOf=function(object)
{
	var length=this.length;	//get the length of the array
	for(var i=0; i<length; ++i)	//for each index
	{
		if(this[i]==object)	//if this object is the requested object
		{
			return i;	//return this index
		}
	}
	return -1;	//indicate that the object could not be found
};

/**Determines the index of the first match of a given object in the array using object.toString() if the object isn't null.
@param regexp The regular expression of the string version of the object to find in the array.
@return The index of the matching object in the array, or -1 if a matching object is not in the array.
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

/**Clears an array by removing every item at every index in the array.*/
Array.prototype.clear=function()
{
	this.splice(0, this.length);	//splice out all the elements
};

/**Determines whether the given object is present in the array.
@param object The object for which to check.
@return true if the object is present in the array.
*/
Array.prototype.contains=function(object)
{
	return this.indexOf(object)>=0;	//see if the object is in the array
};

/**Determines whether a match of the given regular expression is present in the array, using object.toString() if the object isn't null.
@param regexp The regular expression of the string version of the object to find in the array.
@return true if a matching object is present in the array.
*/
Array.prototype.containsMatch=function(regexp)
{
	return this.indexOfMatch(regexp)>=0;	//see if a matching object is in the array
};

/**Removes an item at the given index in the array.
@param index The index at which the element should be removed.
@return The element previously at the given index in the array.
*/
Array.prototype.remove=function(index)
{
	return this.splice(index, 1)[0];	//splice out the element and return it (note that this will not work on Netscape <4.06 or IE <=5.5; see http://www.samspublishing.com/articles/article.asp?p=30111&seqNum=3&rl=1)
};

/**Removes an item from the array.
If the item is not contained in the array, no action is taken.
@param item The item to be removed.
@return The removed item.
*/
Array.prototype.removeItem=function(item)
{
	var index=this.indexOf(item);	//get the index of the item
	if(index>=0)	//if the item is contained in the array
	{
		return this.remove(index);	//remove the item at the index
	}
};

var EMPTY_ARRAY=new Array();	//a shared empty array TODO create methods to make this read-only

//Document

if(typeof document.createElementNS=="undefined")	//if the document does not support createElementNS(), create a substitute
{
	/**Create an element for the given namespace URI and qualified name using the document.createElement() method.
	@param namespaceURI The URI of the namespace.
	@param qname The qualified name of the element.
	@return The new created element.
	*/
	document.createElementNS=function(namespaceURI, qname)	//create an adapter function to call document.createElement()
	{
    return document.createElement(qname);	//create the element, ignoring the namespace TODO does this use closure unnecessarily?
	};
}

if(typeof document.createAttributeNS=="undefined")	//if the document does not support createAttributeNS(), create a substitute
{
	/**Create an attribute for the given namespace URI and qualified name using the document.createAttribute() method.
	@param namespaceURI The URI of the namespace.
	@param qname The qualified name of the attribute.
	@return The new created attribute.
	*/
	document.createAttributeNS=function(namespaceURI, qname)	//create an adapter function to call document.createAttribute()
	{
    return document.createAttribute(qname);	//create the attribute, ignoring the namespace TODO does this use closure unnecessarily?
	};
}

if(isSafari || (typeof document.importNode=="undefined"))	//if the document does not support document.importNode() (or this is Safari, which doesn't support importing XML into an XHTML DOM), create a substitute
{

	/**Imports a new node in the the document.
	@param node The node to import.
	@param deep Whether the entire hierarchy should be imported.
	@return A new clone of the node with the document as its owner.
	*/
/*TODO fix
	document.importNode=function(node, deep)	//create a function to manually import a node
	{
		try
		{
			var importedNode=this._importNode(node, deep);
			return importedNode;
		}
		catch(e)
		{
			alert("error "+e+" importing node "+node.nodeName);

//TODO fix			var nodeString=DOMUtilities.getNodeString(childNode);	//serialize the node
		}
	}
*/

	/**Imports a new node in the the document.
	@param node The node to import.
	@param deep Whether the entire hierarchy should be imported.
	@return A new clone of the node with the document as its owner.
	*/
	document.importNode=function(node, deep)	//create a function to manually import a node
	{
		var importedNode=null;	//we'll create a new node and store it here

		var nodeType=node.nodeType;	//get the type of the node
		if(/*TODO bring back if doesn't work in IE isSafari && */deep	//if we should do a deep import, resort immediately to using innerHTML and a dummy node because of all the IE errors---and the Safari errors that make importing from walking the tree almost impossible
				&& nodeType!=Node.TEXT_NODE)	//Safari seems to break when using innerHTML to import a text node of length 1---it's probably better to use the DOM to import text, anyway
		{
//TODO del							alert("big problem importing node: "+DOMUtilities.getNodeString(childNode));	//TODO fix importnode
			var elementName=nodeType==Node.ELEMENT_NODE ? node.nodeName.toLowerCase() : null;	//get the name of the node to be imported, if it is an element
			var dummyNode=document.createElement("div");	//create a dummy node
			var nodeString=DOMUtilities.getNodeString(node);	//convert the child node to a string
			if(elementName=="tr")	//if this is a table row
			{
				dummyNode.innerHTML="<table><tbody>"+nodeString+"</tbody></table>";	//create the tbody and put the row inside it
				importedNode=dummyNode.childNodes[0].childNodes[0].childNodes[0];	//return the tbody's first and only node, which is our new imported node; do not actually remove the node, which will cause an error on IE TODO see the failure to remove the node causes any long-term problems
			}
			else if(elementName=="th" || elementName=="td")	//if this is a table header or cell
			{
				dummyNode.innerHTML="<table><tbody><tr>"+nodeString+"</tr></tbody></table>";	//create the tbody and put the row inside it
				importedNode=dummyNode.childNodes[0].childNodes[0].childNodes[0].childNodes[0];	//return the tr's first and only node, which is our new imported node; do not actually remove the node, which will cause an error on IE TODO see the failure to remove the node causes any long-term problems
			}
			else	//if this is not a table row
			{
//TODO fix							document.documentElement.appendChild(dummyNode);	//append the dummy node to the document
				dummyNode.innerHTML=nodeString;	//assign the string version of the node to the dummy node
				if(dummyNode.childNodes.length!=1)	//we expect a single child node at the end of the operation
				{
					alert("Error importing node: "+nodeString);	//TODO assert
					alert("Imported: "+dummyNode.innerHTML);
				}
				importedNode=dummyNode.removeChild(dummyNode.childNodes[0]);	//remove the dummy node's first and only node, which is our new imported node
			}
			return importedNode;
		}
		
		switch(node.nodeType)	//see which type of child node this is
		{
			case Node.COMMENT_NODE:	//comment
				importedNode=document.createCommentNode(node.nodeValue);	//create a new comment node with appropriate text
				break;
			case Node.ELEMENT_NODE:	//element
				if(typeof node.namespaceURI!="undefined")	//if the node supports namespaces
				{
					importedNode=document.createElementNS(node.namespaceURI, node.nodeName);	//create a namespace-aware element
				}
				else	//if the node does not support namespaces
				{
					importedNode=document.createElement(node.nodeName);	//create a non-namespace-aware element
				}
/*TODO del; doesn't work
				if(node.nodeName=="button")	//if this is a button (IE fails when trying to import a button with type="button")
				{
alert("importing a button");
					var outerHTML=DOMUtilities.appendNodeString(new StringBuilder(), node).toString();	//serialize the node
alert("using HTML: "+outerHTML);
					document.body.appendChild(importedNode);	//append the element to the body just so outerHTML will work
					importedNode.outerHTML=outerHTML;	//set the element's outer HTML to the string we constructed					
					document.body.removeChild(importedNode);	//remove the element from the body
				}
				else	//for all other nodes
*/
				{
					var attributes=node.attributes;	//get the element's attributes
					var attributeCount=attributes.length;	//find out how many attributes there are
					for(var i=0; i<attributeCount; ++i)	//for each attribute
					{
						var attribute=attributes[i];	//get this attribute
						var attributeName=attribute.nodeName;	//get the attribute name
						if(attributeName=="style")	//if this is the style attribute, it must be copied differently or it will throw an error on IE
						{
//TODO fix for Safari alert("ready to copy style attributes");
//TODO fix for Safari 							DOMUtilities.copyStyleAttribute(importedNode, node);	//copy the style attribute
						}
						else	//for all other attributes
						{
	//TODO fix						try
							{
								var attributeValue=attribute.nodeValue;	//get the attribute value
								if(importedNode.setAttributeNodeNS instanceof Function && typeof attribute.namespaceURI!="undefined")	//if the attribute supports namespaces
								{
									var importedAttribute=document.createAttributeNS(attribute.namespaceURI, attributeName);	//create a namespace-aware attribute
									importedAttribute.nodeValue=attributeValue;	//set the attribute value
									importedNode.setAttributeNodeNS(importedAttribute);	//set the attribute for the element						
								}
								else	//if the attribute does not support namespaces
								{
									var importedAttribute=document.createAttribute(attributeName);	//create a non-namespace-aware element
									importedAttribute.nodeValue=attributeValue;	//set the attribute value TODO verify this works on Safari
									importedNode.setAttributeNode(importedAttribute);	//set the attribute for the element
								}
							}	//TODO check; perhaps catch an exception here and return null or throw our own exception to improve IE "type" attribute error handling
	//TODO fix						catch(exception)	//if there is an error copying the attribute (e.g. IE button type="button")
							{
	//TODO fix alert("error for "+node.nodeName+" adding attribute "+attributeName+"=\""+attributeValue+"\"");
	//TODO fix							return null;
							}
						}
					}
					if(deep)	//if we should import deep
					{
						var childNodes=node.childNodes;	//get a list of child nodes
						var childNodeCount=childNodes.length;	//find out how many child nodes there are
	/*TODO fix
						for(var i=0; i<childNodeCount; ++i)	//for all of the child nodes
						{
							var childNode=childNodes[i];	//get a reference to this child node
							try
							{
								var childImportedNode=document.importNode(childNode, deep);
							}
							catch(exception)	//if there is an error importing the node (this is just a safety precaution in case future versions of IE still throw an exception on button.type and 
							{
								var childImportedNode=null;	//set the node to null
							}
							if(childImportedNode)	//if we imported the node correctly
							{
								importedNode.appendChild(childImportedNode);	//import and append the new child node
							}
							else	//if something went wrong importing the node (IE 6 will fail to add the button.type attribute, but will not throw an exception, leaving childImportedNode undefined
							{
								var nodeString=DOMUtilities.getNodeString(childNode);	//serialize the node
								importedNode.innerHTML+=nodeString;	//append the node string to the element's inner HTML
							}
						}
	*/
						if(childNodeCount>0)	//if there are child nodes (IE will fail on importedNode.innerHTML="" for input type="text")
						{
							var innerHTMLStringBuilder=new StringBuilder();	//construct the inner HTML
							for(var i=0; i<childNodeCount; ++i)	//for all of the child nodes
							{
								DOMUtilities.appendNodeString(innerHTMLStringBuilder, childNodes[i]);	//serialize the node and append it to the string builder
							}
	//TODO del alert("ready to add inner HTML: "+innerHTMLStringBuilder.toString());
							importedNode.innerHTML=innerHTMLStringBuilder.toString();	//set the element's inner HTML to the string we constructed
						}
					}
				}
				break;
			case Node.TEXT_NODE:	//text
				importedNode=document.createTextNode(node.nodeValue);	//create a new text node with appropriate text
				break;
			default:
				alert("Unknown node type: "+node.nodeType);
				break;
			//TODO add checks for other elements, such as CDATA
		}
		return importedNode;	//return the imported node
	};
}

//Node

if(typeof Node=="undefined")	//if no Node type is defined (e.g. IE), create one to give us constant node types
{
	var Node={ELEMENT_NODE: 1, ATTRIBUTE_NODE: 2, TEXT_NODE: 3, CDATA_SECTION_NODE: 4, ENTITY_REFERENCE_NODE: 5, ENTITY_NODE: 6, PROCESSING_INSTRUCTION_NODE: 7, COMMENT_NODE: 8, DOCUMENT_NODE: 9, DOCUMENT_TYPE_NODE: 10, DOCUMENT_FRAGMENT_NODE: 11, NOTATION_NODE: 12};
}

//String

/**Determines whether this string is in all lowercase.
@return true if the string is in all lowercase.
*/
String.prototype.isLowerCase=function()
{
	return this==this.toLowerCase();	//see if this substring matches the same string in all lowercase
};

/**Determines whether this string is in all uppercase.
@return true if the string is in all uppercase.
*/
String.prototype.isUpperCase=function()
{
	return this==this.toUpperCase();	//see if this substring matches the same string in all uppercase
};

/**Determines whether this string starts with the indicated substring.
@param substring The string to check to see if it is at the beginning of this string.
@return true if the given string is at the start of this string.
*/
String.prototype.startsWith=function(substring)
{
	return this.hasSubstring(substring, 0);	//see if this substring is at the beginning of the string
};

/**Determines whether this string ends with the indicated substring.
@param substring The string to check to see if it is at the end of this string.
@return true if the given string is at the end of this string.
*/
String.prototype.endsWith=function(substring)
{
	return this.hasSubstring(substring, this.length-substring.length);	//see if this substring is at the end of the string
};

/**Determines if this string has the given substring at the given index in the string.
@param substring The substring to compare.
@param index The index to compare.
@return true if the given substring matches the characters as the given index of this string.
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

/**Splits a string and returns an associative array with the contents.
Each the value of each key of the associative array will be set to true.
Empty and null splits will be ignored.
@param separator The optional separator string or regular expression; if no separator is provided, the entire string is placed in the set.
@param limit The optional limit to the number of splits to be found.
@return An associative array with they keys set to the elements of the split string.
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

/**Trims the given string of whitespace.
@see https://lists.latech.edu/pipermail/javascript/2004-May/007570.html
*/
String.prototype.trim=function()
{
	return this.replace(/^\s+|\s+$/g, "");	//replace beginning and ending whitespace with nothing
}

//StringBuilder

/**A class for concatenating string with more efficiency than using the additive operator.
Inspired by Nicholas C. Zakas, _Professional JavaScript for Web Developers_, Wiley, 2005, p. 97.
@param strings (...) Zero or more strings with which to initialize the string builder.
*/
function StringBuilder(strings)
{
	this._strings=new Array();	//create an array of strings
	if(!StringBuilder.prototype._initialized)
	{
		StringBuilder.prototype._initialized=true;

		/**Appends a string to the string builder.
		@param string The string to append.
		@return A reference to the string builder.
		*/
		StringBuilder.prototype.append=function(string)
		{
			this._strings.add(string);	//add this string to the array
			return this;
		};

		/**@return A single string containing the contents of the string builder.*/
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

/**A class encapsulating keys and values.
This is a convenience class for constructing an Object with a given set of keys and values, as the JavaScript shorthand notation does not allow non-literal key names.
Values may be accessed in normal object[key]==value syntax. The constructor allows any number of key/value pairs as arguments.
@param key: A key with which to associate a value.
@param value: The value associated with the preceding key.
*/
function Map(key, value)
{
	var argumentCount=arguments.length;	//find out how many arguments there are
	for(var i=0; i+1<argumentCount; i+=2)	//for each key/value combination (counting by twos)
	{
		this[arguments[i]]=arguments[i+1];	//store the value keyed to the key
	}
}

//Point

/**A class encapsulating a point.
@param x: The X coordinate, stored under this.x;
@param y: The Y coordinate, stored under this.y;
*/
function Point(x, y) {this.x=x; this.y=y;}

//Rectangle

/**A class encapsulating a rectangle.
@param coordinates: The position of the top left corner of the rectangle, stored under this.coordinates.
@param size: The size of the rectangle, stored under this.size.
var x, y: The coordinates of the upper-left corner of the rectangle.
var width, height: The dimensions of the rectangle.
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

/**A class encapsulating a size.
@param width: The width, stored under this.width;
@param height: The height coordinate, stored under this.height;
*/
function Size(width, height) {this.width=width; this.height=height;}

//URI

/**A class for parsing and encapsulting a URI according to RFC 2396, "Uniform Resource Identifiers (URI): Generic Syntax".
@param uriString The string form of the URI.
@see http://www.ietf.org/rfc/rfc2396.txt
var scheme The scheme of the URI.
var authority The authority of the URI.
var path The path of the URI.
var query The query of the URI.
var fragment The fragment of the URI.
var parameters An associative array of parameter name/value combinations.
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

/**Global utilities for working with the screen.*/
var GUIUtilities=
{

	/**Centers a node on the screen.
	It is assumed that the node is already specified as absolutely positioned.
	@param node The node to center.
	*/
	centerNode:function(node)
	{
		var viewportBounds=this.getViewportBounds();	//get the bounds of the viewport so that we can center the node
		var x=viewportBounds.x+((viewportBounds.width-node.offsetWidth)/2);	//center the node horizontally
		var y=viewportBounds.y+((viewportBounds.height-node.offsetHeight)/2);	//center the node vertically
		node.style.left=x+"px";	//set the node's horizontal position
		node.style.top=y+"px";	//set the node's vertical position
	},

	/**@return The coordinates that the page has scrolled.
	@see http://www.quirksmode.org/viewport/compatibility.html 
	*/
	getScrollCoordinates:function()
	{
	/*TODO del
	alert("window.pageXOffset: "+window.pageXOffset+"\n"+
				"document.documentElement.scrollLeft: "+document.documentElement.scrollLeft+"\n"+
				"document.body.scrollLeft: "+document.body.scrollLeft+"\n");
	*/
		var x, y;
		if(typeof window.pageYOffset!="undefined") //if we know the page vertical offset (all browsers except IE)
		{
			x=window.pageXOffset;
			y=window.pageYOffset;
		}
		else if(document.documentElement && document.documentElement.scrollTop)	//if we know the document's scroll position (IE 6 strict mode)
		{
			x=document.documentElement.scrollLeft;
			y=document.documentElement.scrollTop;
		}
		else if(document.body)	//for all other IE modes
		{
			x=document.body.scrollLeft;
			y=document.body.scrollTop;
		}
		return new Point(x, y);	//return the scrolling coordinates
	},

	/**@return A Rectangle containing the coordinates and size of the viewport.*/
	getViewportBounds:function()
	{
		return new Rectangle(this.getScrollCoordinates(), this.getViewportSize());	//create a rectangle containing the coordinates and size of the viewport
	},

	/**@return The size of the viewport.
	@see http://www.quirksmode.org/viewport/compatibility.html 
	*/
	getViewportSize:function()
	{
		var width=0, height=0;	//we'll determine the width and height
		if(window.innerWidth && window.innerHeight)	//if the window knows its inner width and height
		{
			width=window.innerWidth;	//use the window's inner dimensions
			height=window.innerHeight;
		}
		else if(document.documentElement && document.documentElement.clientWidth && document.documentElement.clientHeight)	//if the document element knows its dimensions (e.g. IE 6 in strict mode)
		{
			width=document.documentElement.clientWidth;	//use the document element's client width and height
			height=document.documentElement.clientHeight;
		}
		else if(document.body)	//if there is a document body
		{
			width=document.body.clientWidth;	//use the document body's client width and height
			height=document.body.clientHeight;
		}
		return new Size(width, height);	//return the size
	}

};

/**Global utilities for working with the DOM.*/
var DOMUtilities=
{

	/**Imports all the attributes from the source element into the destination element.
	The destination element's original attributes will not be first removed.
	@param destinationElement The element into which the attributes should be imported.
	@param sourceElement The element from which the attributes should be imported.
	*/
/**TODO del when works
	importAttributes:function(destinationElement, sourceElement)
	{
		var attributes=sourceElement.attributes;	//get the element's attributes
		var attributeCount=attributes.length;	//find out how many attributes there are
		for(var i=0; i<attributeCount; ++i)	//for each attribute
		{
			var attribute=attributes[i];	//get this attribute
			if(document.createAttributeNS instanceof Function && typeof attribute.namespaceURI!="undefined")	//if the attribute supports namespaces
			{
				var importedAttribute=document.createAttributeNS(attribute.namespaceURI, attribute.nodeName);	//create a namespace-aware attribute
				importedAttribute.nodeValue=attribute.nodeValue;	//set the attribute value
				destinationElement.setAttributeNodeNS(importedAttribute);	//set the attribute for the element						
			}
			else	//if the attribute does not support namespaces
			{
			try
			{
alert("ready to create attribute: "+attribute.nodeName);
				var importedAttribute=document.createAttribute(attribute.nodeName);	//create a non-namespace-aware element
alert("ready to set value: "+attribute.nodeValue);
				importedAttribute.nodeValue=attribute.nodeValue;	//set the attribute value TODO verify this works on Safari
alert("ready set attribute node: "+attribute.nodeName);
				destinationElement.setAttributeNode(importedAttribute);	//set the attribute for the element
			}
			catch(e)
			{
alert("error: "+e+" trying to import attribute: "+attribute.nodeName+" with value: "+attribute.nodeValue);
			}
				
			}
		}
	},
*/

	/**Ensures that an IMG is loaded before calling a given function.
	@param img The img element to load.
	@param fn The function to call after the image is loaded.
	*/
	waitIMGLoaded:function(img, fn)
	{
		if(img.complete)	//if the image is loaded
		{
			fn();	//call the function directly
		}
		else	//if the image is not loaded
		{
			var onLoad=function()	//create a function to call the function after the image is loaded TODO fix this creates a race condition, because the image could finish loading before we can install our listener
					{
						eventManager.removeEvent(img, "load", onLoad, false);	//stop waiting for the img to load
						fn();	//call the function
//TODO del alert("img loaded after waiting!");	//TODO del
					};
			eventManager.addEvent(img, "load", onLoad, false);	//register an event on the img to wait for it to load
		}
	},

	/**Removes all children from the given node.
	This implementation also unregistered any events for the node and all its children.
	@param node The node the children of which to remove.
	*/
	removeChildren:function(node)
	{
		while(node.childNodes.length>0)	//while there are child nodes left (remove the last node, one at a time, because because IE can sometimes add an element back in after the last one was removed)
		{
			var childNode=node.childNodes[node.childNodes.length-1];	//get a reference to the last node
			uninitializeNode(childNode, true);	//uninitialize the node tree
			node.removeChild(childNode);	//remove the last node
		}
	},

	/**Determines the document tree depth of the given element, returning a zero-level depth for the document node.
	@param element The element for which a depth should be found.
	@return The zero-based depth of the given element in the document, with a zero-level depth for the document node.
	*/
	getElementDepth:function(element)
	{
		var depth=-1;	//this element will be at least depth zero
		do
		{
			element=element.parentNode;	//get the parent node
			++depth;	//increase the depth
		}
		while(element);	//keep getting the parent node while there are ancestors left
		return depth;	//return the depth we calculated
	},

	/**Sets the attribute value of an element, using namespaces if the DOM supports them.
	@param element The element for which an attribute should be set.
	@param namespaceURI The URI of the namespace.
	@param qname The qualified name of the attribute.
	@param value The value of the attribute.
	*/
/*TODO del; not used
	setAttributeNS:function(element, namespaceURI, qname, value)	//TODO improve by initial assignment of function
	{
		var attribute=document.createAttributeNS(namespaceURI, qname);	//create the attribute
		attribute.nodeValue=value;	//set the value
		if(element.setAttributeNodeNS instanceof Function)	//if this DOM supports setAttributeNodeNS
		{
			element.setAttributeNodeNS(attribute);	//use a namespace-aware attribute setting function
		}
		else	//if the DOM isn't namespace aware
		{
			element.setAttributeNode(attribute);	//set the attribute with no namespace information
		}
	},
*/

	/**Copies the style attribute by parsing out the individual style declarations and applying them to the destination style.
	This method is needed because the IE DOM does not allow the style attribute to be copied directly.
	@param destinationElement The destination of the style information.
	@param sourceElement The source of the style information.
	*/
/*TODO del; not used
	copyStyleAttribute:function(destinationElement, sourceElement)
	{
		var style=sourceElement.getAttribute("style");	//get the source style attribute
		if(style!=null)	//if there is a style attribute
		{
			var styles=style.split(";");	//split out the individual styles
			for(var styleIndex=styles.length-1; styleIndex>=0; --styleIndex)	//for each style
			{
				var styleComponents=styles[styleIndex].split(":");	//get a reference to this style and split out the property and value
				if(styleComponents.length==2)	//we expect there to be a property and a value
				{
					var styleProperty=styleComponents[0].trim();	//get the trimmed style property
					var styleValue=styleComponents[1].trim();	//get the trimmed style value
					destinationElement.style[styleProperty]=styleValue;	//copy this style TODO fix Safari; this causes a null value error in safari
				}
			}				
		}
	},
*/




	/**Retrieves the named ancestor element of the given node, starting at the node itself.
	@param node The node the ancestor of which to find, or null if the search should not take place.
	@param elementName The name of the element to find.
	@return The named element in which the node lies, or null if the node is not within such a named element.
	*/
	getAncestorElementByName:function(node, elementName)
	{
		while(node && (node.nodeType!=Node.ELEMENT_NODE || node.nodeName.toLowerCase()!=elementName))	//while we haven't found the named element
		{
			node=node.parentNode;	//get the parent node
		}
		return node;	//return the element we found
	},
	
	/**Retrieves the ancestor element with the given class of the given node, starting at the node itself. Multiple class names are supported.
	@param node The node the ancestor of which to find, or null if the search should not take place.
	@param className The name of the class for which to check, or a regular expression if a match should be found.
	@return The element with the given class in which the node lies, or null if the node is not within such an element.
	*/
	getAncestorElementByClassName:function(node, className)
	{
		while(node)	//while we haven't reached the top of the hierarchy
		{
			if(node.nodeType==Node.ELEMENT_NODE && this.hasClassName(node, className))	//if this is an element and this class name is one of the class names
			{
				return node;	//this node has a matching class name; we'll use it
			}
			node=node.parentNode;	//try the parent node
		}
		return node;	//return whatever node we found
	},
	
	/**Retrieves the descendant element with the given name and attributes, starting at the node itself.
	@param node The node the descendant of which to find, or null if the search should not take place.
	@param elementName The name of the element to find.
	@param parameters An associative array of name/value pairs, each representing an attribute name and value that should be present (or, if the parameter value is null, an attribute that must not be present), or null if no parameter matches are requested.
	@return The element with the given name, or null if there is no such element descendant.
	*/
	getDescendantElementByName:function(node, elementName, parameters)
	{
		if(node)	//if we have a node
		{
			if(node.nodeType==Node.ELEMENT_NODE && node.nodeName.toLowerCase()==elementName)	//if this is an element with the given name
			{
				var parametersMatch=true;	//start out assuming that the parameters match		
				if(parameters!=null)	//if parameters were provided
				{
					for(var parameterName in parameters)	//for each parameter
					{
						if(node.getAttribute(parameterName)!=parameters[parameterName])	//if this attribute doesn't exist or doesn't have the correct value
						{
							parametersMatch=false;	//indicate that the parameters do not match
							break;	//stop searching for a non-match
						}
					}
				}
				if(parametersMatch)	//if all the parameters match
				{
					return node;	//show that we found a matching element name and attribute(s)
				}
			}
			var childNodeList=node.childNodes;	//get all the child nodes
			var childNodeCount=childNodeList.length;	//find out how many children there are
			for(var i=0; i<childNodeCount; ++i)	//for each child node
			{
				var childNode=childNodeList[i];	//get this child node
				var match=this.getDescendantElementByName(childNode, elementName, parameters);	//see if we can find the node in this branch
				if(match)	//if we found a match
				{
					return match;	//return it
				}
			}
		}
		return null;	//show that we didn't find a matching element
	},
	
	/**Retrieves the descendant element with the given class of the given node, starting at the node itself. Multiple class names are supported.
	@param node The node the descendant of which to find, or null if the search should not take place.
	@param className The name of the class for which to check, or a regular expression if a match should be found.
	@return The element with the given class for which the given node is a parent or itself, or null if there is no such element descendant.
	*/
	getDescendantElementByClassName:function(node, className)
	{
		if(node)	//if we have a node
		{
			if(node.nodeType==Node.ELEMENT_NODE && this.hasClassName(node, className))	//if this is an element with the given class name
			{
				return node;	//show that we found a matching element class name
			}
			var childNodeList=node.childNodes;	//get all the child nodes
			var childNodeCount=childNodeList.length;	//find out how many children there are
			for(var i=0; i<childNodeCount; ++i)	//for each child node
			{
				var childNode=childNodeList[i];	//get this child node
				var match=this.getDescendantElementByClassName(childNode, className);	//see if we can find the node in this branch
				if(match)	//if we found a match
				{
					return match;	//return it
				}
			}
		}
		return null;	//show that we didn't find a matching element
	},

	/**Determines whether the given node has the indicated ancestor, including the node itself in the search.
	@param node The node the ancestor of which to find, or null if the search should not take place.
	@param ancestor The ancestor to find.
	@return true if the node or one of its ancestors is the given ancestor.
	*/
	hasAncestor:function(node, ancestor)
	{
		while(node)	//while we haven't ran out of nodes
		{
			if(node==ancestor)	//if this node is the ancestor
			{
				return true;	//show that there was such an ancestor
			}
			node=node.parentNode;	//go up one level
		}
		return false;	//indicate that we didn't find the given ancestor
	},
	
	/**Determines whether the given element has the given class. Multiple class names are supported.
	@param element The element that should be checked for class.
	@param className The name of the class for which to check, or a regular expression if a match should be found.
	@return true if one of the element's class names equals the given class name.
	*/
	hasClassName:function(element, className)
	{
		return this.getClassName(element, className)!=null;	//see if we can find a matching class name
	},
	
	/**Returns the given element has the given class. Multiple class names are supported.
	@param element The element that should be checked for class.
	@param className The name of the class for which to check, or a regular expression if a match should be found.
	@return The given class name, which will be the regular expression match if a regular expression is used, or null if there is no matching class name.
	*/
	getClassName:function(element, className)
	{
		var classNamesString=element.className;	//get the element's class names
		var classNames=classNamesString ? classNamesString.split(/\s/) : EMPTY_ARRAY;	//split out the class names
		var index=className instanceof RegExp ? classNames.indexOfMatch(className) : classNames.indexOf(className);	//get the index of the matching class name
		return index>=0 ? classNames[index] : null;	//return the matching class name, if there is one
	},
	
	/**Adds the given class name to the element's style class.
	@param element The element that should be given a class.
	@param className The name of the class to add.
	*/
	addClassName:function(element, className)
	{
		var classNamesString=element.className;	//get the element's class names
		element.className=classNamesString ? classNamesString+" "+className : className;	//append the class name if there is a class name already
	},
	
	/**Removes the given class name from the element's style class.
	@param element The element that should have a class removed.
	@param className The name of the class to remove.
	*/
	removeClassName:function(element, className)
	{
		var classNamesString=element.className;	//get the element's class names
		var classNames=classNamesString ? classNamesString.split(/\s/) : EMPTY_ARRAY;	//split out the class names
		for(var i=classNames.length-1; i>=0; --i)	//for each index (starting from the end so that we can remove indices at will)
		{
			if(classNames[i]==className)	//if this is a class name to remove
			{
				classNames.remove(i);	//remove this index
			}
		}
		element.className=classNames.join(" ");	//join the remaining class names back together and assign them back to the element's class name
	},

	/**Retrieves the immediate text nodes of the given node as a string.
	@param node The node from which to retrieve text.
	@return The text of the given node.
	*/ 
	getNodeText:function(node)
	{
		var childNodeList=node.childNodes;	//get all the child nodes of the node
		var childNodeCount=childNodeList.length;	//find out how many children there are
		if(childNodeCount>0)	//if there are child nodes
		{
			var stringBuilder=new StringBuilder();	//create a new string builder to construct the string
			for(var i=0; i<childNodeCount; ++i)	//for each child node
			{
				var childNode=childNodeList[i];	//get this child node
				if(childNode.nodeType==Node.TEXT_NODE)	//if this is a text node
				{
					stringBuilder.append(childNode.nodeValue);	//append this value			
				}
			}
			return stringBuilder.toString();	//return the string we constructed
		}
		else	//if there are no child nodes
		{
			return "";	//return an empty string
		}
	},

	/**Creates a string representation of the given node.
	@param node The node to serialize.
	@return A string representation of the given node.
	*/
	getNodeString:function(node)
	{
		return this.appendNodeString(new StringBuilder(), node).toString();	//serialize the node and return its string representation
	},

	/**Appends a string representation of the given node.
	@param stringBuilder The string builder to which a text representation of the node should be appended.
	@param node The node to serialize.
	@return A reference to the string builder.
	*/
	appendNodeString:function(stringBuilder, node)
	{
		switch(node.nodeType)	//see which type of node this is
		{
			case Node.ELEMENT_NODE:	//element
				var nodeName=node.nodeName.toLowerCase();	//get the name of the node
				stringBuilder.append("<").append(nodeName);	//<nodeName
				var attributes=node.attributes;	//get the element's attributes
				var attributeCount=attributes.length;	//find out how many attributes there are
				for(var i=0; i<attributeCount; ++i)	//for each attribute
				{
					var attribute=attributes[i];	//get this attribute
					var attributeValue=attribute.nodeValue;	//get the attribute value
					if(attributeValue!=null)	//if this attribute value is not null (IE6 can return null attribute values for HTML DOM attributes)
					{
						this.appendXMLAttribute(stringBuilder, attribute.nodeName, attributeValue);	//append this attribute
					}
				}
				if(node.childNodes.length>0 || this.NON_EMPTY_ELEMENT_SET[nodeName])	//if there are children, or the element cannot be serialized as an empty element (IE6, for instance, which will drop "div" and "span" from the DOM if they are empty)
				{				
					stringBuilder.append(">");	//>
					this.appendNodeContentString(stringBuilder, node);	//append this node's content
					this.appendXMLEndTag(stringBuilder, nodeName);	//append the end tag
				}
				else	//if there are no children, create an empty element (otherwise, for elements like <input></input>, IE6 will see two elements)
				{
					stringBuilder.append("/>");	///>
				}
				break;
			case Node.COMMENT_NODE:	//comment
				stringBuilder.append("<!--").append(node.nodeValue).append("-->");	//append the node's value with no changes TODO encode the sequence "--"
				break;
			case Node.TEXT_NODE:	//text
				this.appendXMLText(stringBuilder, node.nodeValue);	//append the node's text value
				break;
			//TODO add checks for other elements, such as CDATA
		}
		return stringBuilder;	//return the string builder
	},

	/**Appends a string representation of the given node's content.
	@param stringBuilder The string builder to which a text representation of the node content should be appended.
	@param node The node the content of which to serialize.
	@return A reference to the string builder.
	*/
	appendNodeContentString:function(stringBuilder, node)
	{
		var childNodes=node.childNodes;	//get a list of child nodes
		var childNodeCount=childNodes.length;	//find out how many child nodes there are
		for(var i=0; i<childNodeCount; ++i)	//for all of the child nodes
		{
			var childNode=childNodes[i];	//get a reference to this child node
			this.appendNodeString(stringBuilder, childNode);	//append this child node
		}
		return stringBuilder;	//return the string builder
	},

	/**Encodes a string so that it may be used in XML by escaping XML-specific characters.
	@param string The string to encode.
	@return The XML-encoded string.
	*/
	encodeXML:function(string)
	{
		string=string.replace(/&/g, "&amp;");	//encode '&' first
		string=string.replace(/</g, "&lt;");	//encode '<'
		string=string.replace(/>/g, "&gt;");	//encode '>'
		string=string.replace(/\"/g, "&quot;");	//encode '\"'
		return string;	//return the encoded string
	},

	/**Encodes and appends XML text to the given string builder.
	@param stringBuilder The string builder to hold the data.
	@param text The text to encode and append.
	@return A reference to the string builder.
	*/ 
	appendXMLText:function(stringBuilder, text)
	{
		return stringBuilder.append(this.encodeXML(text));	//encode and append the text, and return the string builder
	},

	/**Appends an XML start tag with the given name to the given string builder.
	If the value of a parameter is null, that parameter will not be used.
	If the value of a parameter is not a string, it will be converted to one.
	@param stringBuilder The string builder to hold the data.
	@param tagName The name of the XML tag.
	@param attributes An associative array of name/value pairs representing attribute names and values, or null if no attributes are provided.
	@return A reference to the string builder.
	*/ 
	appendXMLStartTag:function(stringBuilder, tagName, attributes)
	{
		stringBuilder.append("<").append(tagName);	//<tagName
		if(attributes!=null)	//if attributes are provided
		{
			for(var attributeName in attributes)	//for each attribute
			{
				var attributeValue=attributes[attributeName];	//get this attribute value
				if(attributeValue!=null)	//if an attribute value was given
				{
					this.appendXMLAttribute(stringBuilder, attributeName, attributeValue);	//append the attribute
				}
			}
		}
		return stringBuilder.append(">");	//>
	},

	/**Appends an XML attribute with the given name and value to the given string builder.
	If the value of an attribute is not a string, it will be converted to one.
	The attribute-value combination will be preceded by a space.
	@param stringBuilder The string builder to hold the data.
	@param attributeName The name of the XML attribute.
	@param attributeValue The value of the XML attribute, which cannot be null.
	@return A reference to the string builder.
	*/ 
	appendXMLAttribute:function(stringBuilder, attributeName, attributeValue)
	{
		stringBuilder.append(" ").append(attributeName).append("=\"").append(this.encodeXML(attributeValue.toString())).append("\"");	//name="value"
		return stringBuilder;	//return the string builder
	},

	/**Appends an XML end tag with the given name to the given string builder.
	@param stringBuilder The string builder to hold the data.
	@param tagName The name of the XML tag.
	@return A reference to the string builder.
	*/
	appendXMLEndTag:function(stringBuilder, tagName)
	{
		return stringBuilder.append("</").append(tagName).append(">");	//append the start tag
	},

	/**Appends an XML element containing text.
	@param stringBuilder The string builder to hold the data.
	@param tagName The name of the XML tag.
	@param text The text to store in the element.
	@return A reference to the string builder.
	*/
	appendXMLTextElement:function(stringBuilder, tagName, text)
	{
		this.appendXMLStartTag(stringBuilder, tagName);	//append the start tag
		this.appendXMLText(stringBuilder, text);	//append the text
		return this.appendXMLEndTag(stringBuilder, tagName);	//append the end tag
	},

	/**Determines whether the given element has the given class, using DOM methods. Multiple class names are supported.
	@param element The element that should be checked for class.
	@param className The name of the class for which to check, or a regular expression if a match should be found.
	@return true if one of the element's class names equals the given class name.
	*/
	hasClass:function(element, className)
	{
		var classNamesString=element.getAttribute("class");	//get the element's class names
		var classNames=classNamesString ? classNamesString.split(/\s/) : EMPTY_ARRAY;	//split out the class names
		return className instanceof RegExp ? classNames.containsMatch(className) : classNames.contains(className);	//return whether this class name is one of the class names
	},

	/**Map of DOM attribute names keyed to HTML attribute names.
	For example, the key "readOnly" yields "readonly".
	Attribute names that do not change are not included in the map.
	*/
	DOM_ATTRIBUTE_NAME_MAP:
	{
		"className": "class",
		"maxLength": "maxlength", //see http://www.quirksmode.org/bugreports/archives/2005/02/IE_setAttributemaxlength_5_on_input.html
		"readOnly": "readonly"
	},

	/**Map of HTML attribute names keyed to DOM attribute names.
	For example, the key "class" yields "className".
	Attribute names that do not change are not included in the map. 
	*/
	HTML_ATTRIBUTE_NAME_MAP:
	{
		"class": "className",
		"maxlength": "maxLength", //see http://www.quirksmode.org/bugreports/archives/2005/02/IE_setAttributemaxlength_5_on_input.html
		"readonly": "readOnly"
	},

	/**Map of CSS attribute names keyed to CSS style names.
	For example, the key "background-color" yields "backgroundColor".
	Style names that do not change are not included in the map.
	*/
	CSS_ATTRIBUTE_NAME_MAP:
	{
		"background-color": "backgroundColor"
	},

	/**The set of names of elements that cannot be serialized as empty elements.*/
	NON_EMPTY_ELEMENT_SET:
	{
		"div": true,
		"span": true,
		"label": true
	}
	
};

//Initialization AJAX Event

/**A class indicating an initialization AJAX request.
*/
function InitAJAXEvent()
{
}

//Form AJAX Event

/**A class encapsulating form information for an AJAX request.
@param parameters: An optional associative array of parameters with which to initialize the request.
@param provisional: Optional indication of whether the value is a provisional value that has not yet been accepted by the user (defaults to false).
var parameters: The associative array of parameters and values.
var provisional: Indicates whether the value is a provisional value that has not yet been accepted by the user.
*/
function FormAJAXEvent(parameters, provisional)
{
	this.parameters=parameters ? parameters : new Object();	//create a new associative array if not parameters were given
	if(!FormAJAXEvent.prototype._initialized)
	{
		FormAJAXEvent.prototype._initialized=true;		
	}
	this.provisional=Boolean(provisional);	//get a normal boolean version of the provisional status, assuming false
}

//Action AJAX Event

/**A class encapsulating action information for an AJAX request.
@param componentID: The ID of the source component.
@param targetID: The ID of the target element.
@param actionID: The action identifier, or null if no particular action is indicated.
@param option: The zero-based option indicating mouse buttons left, right, or middle in that order.
var componentID: The ID of the source component.
var targetID: The ID of the target element.
var actionID: The action identifier, or null if no particular action is indicated.
var option: The zero-based option indicating mouse buttons left, right, or middle in that order.
*/
function ActionAJAXEvent(componentID, targetID, actionID, option)
{
	this.componentID=componentID;
	this.targetID=targetID;
	this.actionID=actionID;
	this.option=option;
}

//Drop AJAX Event

/**A class encapsulating drop information for an AJAX request.
@param dragState: The object containing the state of the drag and drop operation.
@param dragSource: The element that was the source of the drag and drop operation.
@param dropTarget: The element that is the target of the drag and drop operation.
@param event: The W3C event object associated with the drop.
var dragSource: The element that was the source of the drag and drop operation.
var dropTarget: The element that is the target of the drag and drop operation.
var mousePosition: The position of the mouse at the drop.
*/
function DropAJAXEvent(dragState, dragSource, dropTarget, event)
{
	this.dragSource=dragSource;	//save the drag source
	this.dropTarget=dropTarget;	//save the drop target
	this.mousePosition=new Point(event.clientX, event.clientY);	//save the mouse position
}

/**A class encapsulating mouse information for an AJAX request.
@param eventType: The type of mouse event; one of MouseAJAXEvent.EventType.
@param component: The target component.
@param target: The element indicating the target of the event.
@param event: The W3C event object associated with the drop.
var eventType: The type of mouse event; one of MouseAJAXEvent.EventType.
var componentID: The ID of the target component.
var componentBounds: The rectangle of the component.
var targetID: The ID of the target element.
var targetBounds: The rectangle of the target element.
var viewportBounds: the absolute bounds of the viewport.
var mousePosition: The position of the mouse relative to the viewport.
*/
function MouseAJAXEvent(eventType, component, target, event)
{
/*TODO del if not needed
	if(!MouseAJAXEvent.prototype._initialized)
	{
		MouseAJAXEvent.prototype._initialized=true;
	}
*/
	this.eventType=eventType;	//save the event type
	this.componentID=component.id;	//save the component ID
	this.componentBounds=getElementBounds(component);	//get the component bounds
	this.targetID=target.id;	//save the target ID
	this.targetBounds=getElementBounds(component);	//get the target bounds
	this.viewportBounds=GUIUtilities.getViewportBounds();	//get the viewport bounds
	this.mousePosition=new Point(event.clientX, event.clientY);	//save the mouse position
}

/**The available types of mouse events.*/
MouseAJAXEvent.EventType={ENTER: "mouseEnter", EXIT: "mouseExit"};

//AJAX Response

/**A class encapsulating an AJAX response.
@param document: The response XML document tree.
@param size: The number of characters in the document.
var document: The response XML document tree.
var size: The number of characters in the document.
*/
function AJAXResponse(document, size)
{
	this.document=document;
	this.size=size
}

//HTTP Communicator

/**A class encapsulating HTTP communication functionality.
This class creates a shared HTTPCommunicator.prototype.xmlHTTP variable, necessary for the onreadystate() callback function.
function processHTTPResponse(): A reference to a function to call for asynchronous HTTP requests, or null if HTTP communication should be synchronous.
*/
function HTTPCommunicator()
{
	/**The reference to the current XMLHTTP request object, or null if no communication is occurring.*/
	this.xmlHTTP=null;

	/**The configured method for processing an HTTP response.*/
	this.processHTTPResponse=null;

	if(!HTTPCommunicator.prototype._initialized)
	{
		HTTPCommunicator.prototype._initialized=true;

		/**@return true if the commmunicator is in the process of communicating.*/
		HTTPCommunicator.prototype.isCommunicating=function() {return this.xmlHTTP!=null;};
	
		/**The enumeration of ready states for asynchronous XMLHTTP requests.*/
		HTTPCommunicator.prototype.READY_STATE={UNINITIALIZED: 0, LOADING: 1, LOADED: 2, INTERACTIVE: 3, COMPLETED: 4};
		
		/**The versions of the Microsoft XML HTTP ActiveX objects, in increasing order of preference.
		@see http://support.microsoft.com/?kbid=269238
		*/
		HTTPCommunicator.prototype.MSXMLHTTP_VERSIONS=["Microsoft.XMLHTTP", "MSXML2.XMLHTTP", "MSXML2.XMLHTTP.3.0", "MSXML2.XMLHTTP.4.0", "MSXML2.XMLHTTP.5.0", "MSXML2.XMLHTTP.6.0", "MSXML2.XMLHTTP.7.0"];
		
		/**Sets the callback method to use for processing an HTTP response.
		When the provided method is called, the this variable will be set to this HTTP communicator.
		@param fn The function to call when processing HTTP responses, or null if requests should be synchronous.
		*/
		HTTPCommunicator.prototype.setProcessHTTPResponse=function(fn)
		{
			this.processHTTPResponse=fn;	//save the function for processing HTTP responses
		};

		if(window.XMLHttpRequest)	//if we can create an XML HTTP request (e.g. Mozilla)
		{		
			/**@return A newly created XML HTTP request object.*/
			HTTPCommunicator.prototype._createXMLHTTP=function()
			{
				return new XMLHttpRequest();	//create a new XML HTTP request object
			};
		}
		else if(window.ActiveXObject)	//if we can create ActiveX objects
		{
			var msXMLHTTPVersion;	//we'll determine the correct version of the ActiveX to use
			for(var i=this.MSXMLHTTP_VERSIONS.length-1; i>=0; --i)	//for each available version
			{
				try
				{
					msXMLHTTPVersion=this.MSXMLHTTP_VERSIONS[i];	//get this version
					new ActiveXObject(msXMLHTTPVersion);	//try to create a new ActiveX object
					break;	//if we could create this ActiveX object, use it
				}
				catch(exception)	//ignore the errors
				{
				}
			}
			/**@return A newly created XML HTTP request object.*/
			HTTPCommunicator.prototype._createXMLHTTP=function()
			{
				return new ActiveXObject(msXMLHTTPVersion);	//create a new ActiveX object, using closure to return the version determined to work
			};
		}
		else	//if we can't create an XML HTTP request or an ActiveX object
		{
			throw new Error("XMLHTTP not available.");
		};

		/**Performs an HTTP GET request.
		@param uri The request URI.
		@param query Query information for the URI of the GET request, or null if there is no query.
		*/
		HTTPCommunicator.prototype.get=function(uri, query)
		{
			return this._performRequest("GET", uri, query);	//perform a GET request
		};
		
		/**Performs an HTTP POST request.
		@param uri The request URI.
		@param query Query information for the body of the POST request, or null if there is no query.
		@param contentType The content type of the request, or null if no content type should be specified.
		*/
		HTTPCommunicator.prototype.post=function(uri, query, contentType)
		{
			return this._performRequest("POST", uri, query, contentType);	//perform a POST request
		};
	
		/**Performs an HTTP request and returns the result.
		@param The HTTP request method.
		@param uri The request URI.
		@param query Query information for the request, or null if there is no query.
		@return The text of the response or, if the response provides an XML DOM tree, the XML document object; or null if the request is asynchronous.
		@throws Exception if an error occurs performing the request.
		@throws Number if the HTTP response code was not 200 (OK).
		*/
		HTTPCommunicator.prototype._performRequest=function(method, uri, query, contentType)
		{
				//TODO assert this.xmlHTTP does not exist
			this.xmlHTTP=this._createXMLHTTP();	//create an XML HTTP object
			var xmlHTTP=this.xmlHTTP;	//make a local copy of the XML HTTP request object
			if(method=="GET" && query)	//if there is a query for the GET method
			{
				uri=uri+"?"+query;	//add the query to the URI
			}
			var asynchronous=Boolean(this.processHTTPResponse);	//see if we should make an asynchronous request
			if(asynchronous)	//if we're making asynchronous requests
			{
				xmlHTTP.onreadystatechange=this._createOnReadyStateChangeCallback();	//create and assign a callback function for processing the response
			}
			xmlHTTP.open(method, uri, asynchronous);
			var content=null;	//we'll create content if we need to
			if(method=="POST")	//if this is the POST method
			{
//TODO del alert("posting with query: "+query);
	//TODO del alert("posting with query: "+query);
//TODO del				this.xmlHTTP.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");	//set the post content type
				if(contentType)	//if a content type was given
				{
					xmlHTTP.setRequestHeader("Content-Type", contentType);	//set the post content type
				}
				if(query)	//if there is a post query
				{
//TODO del xmlHTTP.setRequestHeader("x-content", query);	//TODO del; debugging
					content=query;	//use the query as the content
				}	
			}
			try
			{
				xmlHTTP.send(content);	//send the request
			}
			catch(e)
			{
//TODO fix---why does this occur?				alert("error loading content: "+e);
			}
			if(!asynchronous)	//if we're communicating synchronously
			{
				this._reportResponse();	//report the response immediately TODO maybe put this into an asynchrous call using setTimeout()
				return xmlHTTP;	//TODO testing synchronous
			}
		};
	
		/**Creates a method for processing XML HTTP on ready state changes.
		This method uses JavaScript closure to capture a reference to this class so that it will be present during later callback.
		*/
		HTTPCommunicator.prototype._createOnReadyStateChangeCallback=function()
		{
			var thisHTTPCommunicator=this;	//save this
			/**A new function that captures this in the form of the thisHTTPCommunicator variable.
			var thisHTTPCommunicator The captured reference to the HTTPCommunicator instance.
			*/
			return function()
			{
				if(thisHTTPCommunicator.xmlHTTP && thisHTTPCommunicator.xmlHTTP.readyState==thisHTTPCommunicator.READY_STATE.COMPLETED)	//if a transfer is completed
				{
					thisHTTPCommunicator._reportResponse();	//report the response
				}
			};
		};
	
		/**Reports the response from the XML HTTP request object by calling the processHTTPResponse() callback method.
		The reference to the XML HTTP request object is removed.
		@see #processHTTPResponse()
		*/
		HTTPCommunicator.prototype._reportResponse=function()
		{
			if(this.xmlHTTP)	//if we have an XML HTTP request object
			{
				var xmlHTTP=this.xmlHTTP;	//make a local copy of the XML HTTP request object
				this.xmlHTTP=null;	//remove the XML HTTP request object (Firefox only allows one asynchronous communication per object)
				if(this.processHTTPResponse)	//if we have a method for processing responses
				{
					this.processHTTPResponse(xmlHTTP);	//process the response
				}
			}
		};
	}
}

//GuiseAJAX

/**A class encapsulating AJAX functionality for Guise.
This class has knowledge of com.guiseframework.js.Client, which is expected to be stored in a global variable named "guise".
*/
function GuiseAJAX()
{

	/**The object for communicating with Guise via AJAX.*/
	this.httpCommunicator=new HTTPCommunicator();

	/**The queue of AJAX HTTP request information objects.*/
	this.ajaxRequests=new Array();

	/**The queue of AJAX responses of type AJAXResponse.*/
	this.ajaxResponses=new Array();

	/**Whether we are currently processing AJAX requests.*/
	this.processingAJAXRequests=false;

	/**Whether we are currently processing AJAX responses.*/
	this.processingAJAXResponses=false;

	if(!GuiseAJAX.prototype._initialized)
	{
		GuiseAJAX.prototype._initialized=true;

		/**The content type of a Guise AJAX request.*/
		GuiseAJAX.prototype.REQUEST_CONTENT_TYPE="application/x-guise-ajax-request+xml";

		/**The enumeration of the names of the request elements.*/
		GuiseAJAX.prototype.RequestElement=
				{
					REQUEST: "request", EVENTS: "events",
					FORM: "form", PROVISIONAL: "provisional", CONTROL: "control", NAME: "name", VALUE: "value",
					ACTION: "action", COMPONENT: "component", COMPONENT_ID: "componentID", TARGET_ID: "targetID", ACTION_ID: "actionID", OPTION: "option",
					DROP: "drop", SOURCE: "source", TARGET: "target", VIEWPORT: "viewport", MOUSE: "mouse", ID: "id", X: "x", Y: "y", WIDTH: "width", HEIGHT: "height",
					INIT: "init"
				};

		/**The content type of a Guise AJAX response.*/
		GuiseAJAX.prototype.RESPONSE_CONTENT_TYPE="application/x-guise-ajax-response+xml";

		/**The enumeration of the names of the response elements.*/
		GuiseAJAX.prototype.ResponseElement=
				{
					RESPONSE: "response",
					PATCH: "patch",
					ATTRIBUTE: "attribute",
					NAME: "name",
					VALUE: "value",
					REMOVE: "remove",
					NAVIGATE: "navigate",
					VIEWPORT_ID: "viewportID",
					RELOAD: "reload"
				};

		/**Immediately sends or queues an AJAX request.
		@param ajaxRequest The AJAX request to send.
		*/
		GuiseAJAX.prototype.sendAJAXRequest=function(ajaxRequest)
		{
			if(AJAX_ENABLED)	//if AJAX is enabled
			{
				this.ajaxRequests.enqueue(ajaxRequest);	//enqueue the request info
				this.processAJAXRequests();	//process any waiting requests now if we can
			}
		};
	
		/**Processes AJAX requests.
		@see GuiseAJAX#ajaxRequests
		*/
		GuiseAJAX.prototype.processAJAXRequests=function()
		{
				//see if the communicator is not busy (if it is busy, we're in asychronous mode and the end of the processing this method will be called again to check for new requests)
			if(!this.httpCommunicator.isCommunicating() && !this.processingAJAXRequests && this.ajaxRequests.length>0)	//if we aren't processing AJAX requests or communicating with the server, and there are requests queued TODO fix small race condition in determining whether processing is occurring
			{
				this.processingAJAXRequests=true;	//we are processing AJAX requests now
				try
				{
					var requestStringBuilder=new StringBuilder();	//create a string builder to hold the request string					
					DOMUtilities.appendXMLStartTag(requestStringBuilder, this.RequestElement.REQUEST);	//<request>
					DOMUtilities.appendXMLStartTag(requestStringBuilder, this.RequestElement.EVENTS);	//<event>
					while(this.ajaxRequests.length>0)	//there are more AJAX requests
					{
						var ajaxRequest=this.ajaxRequests.dequeue();	//get the next AJAX request to process
						if(ajaxRequest instanceof FormAJAXEvent)	//if this is a form event
						{
							this._appendFormAJAXEvent(requestStringBuilder, ajaxRequest);	//append the form event
						}
						else if(ajaxRequest instanceof ActionAJAXEvent)	//if this is an action event
						{
							this._appendActionAJAXEvent(requestStringBuilder, ajaxRequest);	//append the action event
						}
						else if(ajaxRequest instanceof DropAJAXEvent)	//if this is a drop event
						{
							this._appendDropAJAXEvent(requestStringBuilder, ajaxRequest);	//append the drop event
						}
						else if(ajaxRequest instanceof MouseAJAXEvent)	//if this is a mouse event
						{
							this._appendMouseAJAXEvent(requestStringBuilder, ajaxRequest);	//append the mouse event
						}
						else if(ajaxRequest instanceof InitAJAXEvent)	//if this is an initialization event
						{
							this._appendInitAJAXEvent(requestStringBuilder, ajaxRequest);	//append the init event
						}
					}
					DOMUtilities.appendXMLEndTag(requestStringBuilder, this.RequestElement.EVENTS);	//</events>
					DOMUtilities.appendXMLEndTag(requestStringBuilder, this.RequestElement.REQUEST);	//</request>
					try
					{
//TODO del alert("ready to post: "+requestStringBuilder.toString());
						this.httpCommunicator.post(window.location.href, requestStringBuilder.toString(), this.REQUEST_CONTENT_TYPE);	//post the HTTP request information back to the same URI
					}
					catch(exception)	//if a problem occurred
					{
						//TODO log a warning
//TODO fix alert(exception);
						AJAX_ENABLED=false;	//stop further AJAX communication
					}						
				}
				finally
				{
					this.processingAJAXRequests=false;	//we are no longer processing AJAX requests
				}
			}
		};

		/**Appends an AJAX form event to a string builder.
		@param stringBuilder The string builder collecting the request data.
		@param ajaxFormRequest The form request information to append.
		@return The string builder.
		*/
		GuiseAJAX.prototype._appendFormAJAXEvent=function(stringBuilder, ajaxFormRequest)
		{
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.FORM,	//<form
					new Map(this.RequestElement.PROVISIONAL, ajaxFormRequest.provisional));	//provisional="provisional">
			for(var parameterName in ajaxFormRequest.parameters)	//for each form parameter
			{
				DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.CONTROL, new Map(this.RequestElement.NAME, parameterName));	//<control name="parameterName">
				DOMUtilities.appendXMLText(stringBuilder, ajaxFormRequest.parameters[parameterName]);	//append the parameter value
				DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.CONTROL);	//</control>
			}
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.FORM);	//</form>
			return stringBuilder;	//return the string builder
		};

		/**Appends an AJAX action event to a string builder.
		@param stringBuilder The string builder collecting the request data.
		@param ajaxActionEvent The action event information to append.
		@return The string builder.
		*/
		GuiseAJAX.prototype._appendActionAJAXEvent=function(stringBuilder, ajaxActionEvent)
		{
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.ACTION,	//<action>
					new Map(this.RequestElement.COMPONENT_ID, ajaxActionEvent.componentID,	//componentID="componentID"
							this.RequestElement.TARGET_ID, ajaxActionEvent.targetID,	//targetID="targetID"
							this.RequestElement.ACTION_ID, ajaxActionEvent.actionID,	//actionID="actionID"
							this.RequestElement.OPTION, ajaxActionEvent.option));	//option="option"
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.ACTION);	//</action>
			return stringBuilder;	//return the string builder
		};

		/**Appends an AJAX drop event to a string builder.
		@param stringBuilder The string builder collecting the request data.
		@param ajaxDropEvent The drop event information to append.
		@return The string builder.
		*/
		GuiseAJAX.prototype._appendDropAJAXEvent=function(stringBuilder, ajaxDropEvent)
		{
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.DROP);	//<drop>
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.SOURCE, new Map(this.RequestElement.ID, ajaxDropEvent.dragSource.id));	//<source id="id">
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.SOURCE);	//</source>
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.TARGET, new Map(this.RequestElement.ID, ajaxDropEvent.dropTarget.id));	//<target id="id">
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.TARGET);	//</target>
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.MOUSE, new Map(this.RequestElement.X, ajaxDropEvent.mousePosition.x, this.RequestElement.Y, ajaxDropEvent.mousePosition.y));	//<mouse x="x" y="y">
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.MOUSE);	//</mouse>
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.DROP);	//</drop>
			return stringBuilder;	//return the string builder
		};

		/**Appends an AJAX mouse event to a string builder.
		@param stringBuilder The string builder collecting the request data.
		@param ajaxMouseEvent The mouse event information to append.
		@return The string builder.
		*/
		GuiseAJAX.prototype._appendMouseAJAXEvent=function(stringBuilder, ajaxMouseEvent)
		{
			DOMUtilities.appendXMLStartTag(stringBuilder, ajaxMouseEvent.eventType);	//<mouseXXX>
//TODO del alert("ready to append viewport info: "+this.RequestElement.VIEWPORT+" x: "+ajaxMouseEvent.viewportBounds.x+" y: "+ajaxMouseEvent.viewportBounds.y+" width: "+ajaxMouseEvent.viewportBounds.width+" height: "+ajaxMouseEvent.viewportBounds.height);
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.VIEWPORT,	//<viewport
					new Map(this.RequestElement.X, ajaxMouseEvent.viewportBounds.x,	//x="viewportBounds.x"
							this.RequestElement.Y, ajaxMouseEvent.viewportBounds.y,	//y="viewportBounds.y"
							this.RequestElement.WIDTH, ajaxMouseEvent.viewportBounds.width,	//width="viewportBounds.width"
							this.RequestElement.HEIGHT, ajaxMouseEvent.viewportBounds.height));	//height="viewportBounds.height">
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.VIEWPORT);	//</viewport>
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.COMPONENT,	//<component
					new Map(this.RequestElement.ID, ajaxMouseEvent.componentID,	//id="componentID"
							this.RequestElement.X, ajaxMouseEvent.componentBounds.x,	//x="componentBounds.x"
							this.RequestElement.Y, ajaxMouseEvent.componentBounds.y,	//y="componentBounds.y"
							this.RequestElement.WIDTH, ajaxMouseEvent.componentBounds.width,	//width="componentBounds.width"
							this.RequestElement.HEIGHT, ajaxMouseEvent.componentBounds.height));	//height="componentBounds.height">
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.COMPONENT);	//</component>
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.TARGET,	//<target
					new Map(this.RequestElement.ID, ajaxMouseEvent.targetID,	//id="targetID"
							this.RequestElement.X, ajaxMouseEvent.targetBounds.x,	//x="targetBounds.x"
							this.RequestElement.Y, ajaxMouseEvent.targetBounds.y,	//y="targetBounds.y"
							this.RequestElement.WIDTH, ajaxMouseEvent.targetBounds.width,	//width="targetBounds.width"
							this.RequestElement.HEIGHT, ajaxMouseEvent.targetBounds.height));	//height="targetBounds.height">
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.TARGET);	//</target>
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.MOUSE, new Map(this.RequestElement.X, ajaxMouseEvent.mousePosition.x, this.RequestElement.Y, ajaxMouseEvent.mousePosition.y));	//<mouse x="x" y="y">
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.MOUSE);	//</mouse>
			DOMUtilities.appendXMLEndTag(stringBuilder, ajaxMouseEvent.eventType);	//</mouseXXX>
			return stringBuilder;	//return the string builder
		};

		/**Appends an AJAX initialization event to a string builder.
		@param stringBuilder The string builder collecting the request data.
		@param ajaxInitEvent The init event information to append.
		@return The string builder.
		*/
		GuiseAJAX.prototype._appendInitAJAXEvent=function(stringBuilder, ajaxInitEvent)
		{
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.INIT);	//<init>
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.INIT);	//</init>
			return stringBuilder;	//return the string builder
		};
	
		/**Creates a method for processing HTTP communication.
		This method uses JavaScript closure to capture a reference to this class so that it will be present during later callback.
		*/
		GuiseAJAX.prototype._createHTTPResponseCallback=function()
		{
			var thisGuiseAJAX=this;	//save this
			/**A new function that captures this in the form of the thisGuiseAJAX variable.
			@param xmlHTTP The XML HTTP object.
			var this The HTTP communicator that calls this function.
			var thisGuiseAJAX The captured reference to the GuiseAJAX instance.
			*/ 
			return function(xmlHTTP)
			{
				try
				{
		//TODO del alert("processing asynch result");
/*TODO del
alert("we returned, at least");
	alert("ready state: "+xmlHTTP.readyState);
	alert("got status: "+xmlHTTP.status);
	alert("response text: "+xmlHTTP.responseText);
	alert("response XML: "+xmlHTTP.responseXML);
*/
					var status=0;
					try
					{
						status=xmlHTTP.status;	//get the status
					}
					catch(e)	//if there is a problem getting the status, don't do anything TODO fix this hack to get around Firefox problem when the form is submitted right before an AJAX request occurs; investigate turning off Enter-based submission, too
					{
						return;
					}
					if(status==200)	//if everything went OK
					{
						if(xmlHTTP.responseText && xmlHTTP.responseXML && xmlHTTP.responseXML.documentElement)	//if we have XML (if there is no content or there is an error, IE sends back a document has a null xmlHTTP.responseXML.documentElement)
						{
							thisGuiseAJAX.ajaxResponses.enqueue(new AJAXResponse(xmlHTTP.responseXML, xmlHTTP.responseText.length));	//enqueue the response
							thisGuiseAJAX.processAJAXResponses();	//process enqueued AJAX responses
	//TODO del						setTimeout("GuiseAJAX.prototype.processAJAXResponses();", 1);	//process the AJAX responses later		
	//TODO del						thisGuiseAJAX.processAJAXRequests();	//make sure there are no waiting AJAX requests
						}
					}
/*TODO del; XMLHTTPRequest automatically follows redirects
					else if(status>=300 && status<400)	//if this is a redirect
					{
						var location=xmlHTTP.getResponseHeader("Location");	//get the Location header
						if(location)
						{
							alert("redirect to: "+location);
						}
					}
*/
					else	//if there was an HTTP error TODO check for redirects
					{
				//TODO fix		throw xmlHTTP.status;	//throw the status code
					}
				}
				catch(exception)	//if a problem occurred
				{
					//TODO log a warning
alert(exception);
					AJAX_ENABLED=false;	//stop further AJAX communication
					throw exception;	//TODO testing
				}
			};
		};

		/**Processes responses from AJAX requests.
		This routine should be called asynchronously from an event so that the DOM tree can be successfully updated.
		Whether the busy indicator is shown depends on the the browser type and the size of the response.
		This implementation shows the busy indicator for medium communication size on IE6, and on large communication sizes on all other browsers.
		Typical response sizes include:
		500-2000: Normal communication size; acceptable delay on IE6.
		5000-10000: Medium communication size; perceptible delay on IE6.
		20000-30000: Large communication size; unacceptable delay on IE6 without indicator. 
		@see GuiseAJAX#ajaxResponses
		*/
		GuiseAJAX.prototype.processAJAXResponses=function()
		{
			if(!this.processingAJAXResponses)	//if we aren't processing AJAX responses TODO fix small race condition in determining whether processing is occurring
			{
				this.processingAJAXResponses=true;	//we are processing AJAX responses now
/*TODO del when works; the server now takes care of this
					//pre-process the responses to check for a navigation request, so that we can skip updates and immediately navigate
				for(var responseIndex=0; responseIndex<this.ajaxResponses.length; ++responseIndex)	//for each response
				{
					var childNodeList=this.ajaxResponses[responseIndex].documentElement.childNodes;	//get all the child nodes of the document element
					var childNodeCount=childNodeList.length;	//find out how many children there are
					for(var i=0; i<childNodeCount; ++i)	//for each child node
					{
						var childNode=childNodeList[i];	//get this child node
						if(childNode.nodeType==Node.ELEMENT_NODE && elementName==this.ResponseElement.NAVIGATE)	//if this is a navigation element
						{
							var navigateURI=this._processNavigate(childNode);	//navigate to the specified request
							if(navigateURI!=null)	//if a new navigation URI was requested
							{
								AJAX_ENABLED=false;	//turn off AJAX processing
								window.location.href=navigateURI;	//go to the new location
							}
						}
					}
				}
*/
				var newHRef=null;	//we'll see if a new URI was requested at any point
				try
				{
					while(this.ajaxResponses.length>0 && newHRef==null)	//while there are more AJAX responses and no redirect has been requested TODO fix small race condition on adding responses
					{
						var ajaxResponse=this.ajaxResponses.dequeue();	//get this response
						var responseDocument=ajaxResponse.document;	//get this response document
						var showBusy=isUserAgentIE6 ? ajaxResponse.size>5000 : ajaxResponse.size>20000;	//see if we should show a busy indicator
//TODO del; testirng						var showBusy=ajaxResponse.size>100;	//TODO del; testing
/*TODO salvage if needed
						if(showBusy)	//if we should show a busy indicator
						{
							guise.setBusyVisible(true);	//show a busy indicator
//TODO fix for IE6; this doesn't work when set immediately window.setTimeout(function(){guise.setBusyVisible(true);}, 1);	
						}
*/
						try
						{
							//TODO assert document element name is "response"
							var childNodeList=responseDocument.documentElement.childNodes;	//get all the child nodes of the document element
							var childNodeCount=childNodeList.length;	//find out how many children there are
							for(var i=0; i<childNodeCount; ++i)	//for each child node
							{
								var childNode=childNodeList[i];	//get this child node
								if(childNode.nodeType==Node.ELEMENT_NODE)	//if this is an element
								{
									var elementName=childNode.nodeName;	//get this element name
//TODO del alert("looking at response: "+elementName);
									switch(elementName)	//see which type of response this is
									{
										case this.ResponseElement.PATCH:	//patch
											this._processPatch(childNode);	//patch the document with this patch information
											break;
	/*TODO del when works
										case this.ResponseElement.ATTRIBUTE:	//attribute
											this._processAttribute(childNode);	//patch the document with this attribute information
											break;
	*/
										case this.ResponseElement.REMOVE:	//remove
//TODO del alert("this is a remove");
											this._processRemove(childNode);	//remove the elements from the document with this removal element
											break;
										case this.ResponseElement.NAVIGATE:	//navigate
											var navigateURI=this._processNavigate(childNode);	//navigate to the specified request
											if(navigateURI!=null)	//if a new navigation URI was requested
											{
												newHRef=navigateURI;	//request navigation to the new URI
											}
											break;
										case this.ResponseElement.RELOAD:	//reload
											window.location.reload();	//reload the page
											return;	//stop processing events
									}
								}
							}
						}
						finally
						{
/*TODO salvage if needed
							if(showBusy)	//if we are showing a busy indicator
							{
								guise.setBusyVisible(false);	//hide the busy indicator
							}
*/
						}
						this.processAJAXRequests();	//make sure there are no waiting AJAX requests
					}
					if(newHRef!=null)	//if navigation was requested
					{
						AJAX_ENABLED=false;	//turn off AJAX processing
						window.location.href=newHRef;	//go to the new location
					}
				}
				finally
				{
					this.processingAJAXResponses=false;	//we are no longer processing AJAX responses
					if(newHRef==null)	//if we're not going to a new page
					{
						for(var oldElementID in guise.oldElementIDCursors)	//for each old element ID
						{
	//TODO del alert("looking at old element ID: "+oldElementID);
							var oldCursor=guise.oldElementIDCursors[oldElementID];	//get the old cursor
	//TODO del alert("old cursor: "+oldCursor);
							var element=document.getElementById(oldElementID);	//get the old element in the document
							if(element!=null)	//if this element is still in the document TODO make sure that these two checks ensure this is really an old cursor
							{
	//TODO del alert("restoring old cursor for element: "+oldElementID+" which has cursor "+element.style.cursor);
								delete guise.oldElementIDCursors[oldElementID];	//remove this old cursor from the array
								element.style.cursor=oldCursor;	//set the cursor back to what it was
	//TODO del alert("element now has cursor: "+element.style.cursor);
							}
						}
					}
				}
			}
		};

		/**Processes the AJAX navigate response.
		If navigation is requested in a new viewport, navigation occurs; otherwise, the new navigation URI is returned.
		@param element The element representing the navigate response.
		@return The URI of the new requested navigation, or null if there is no new navigation or if the navigation occurs in a separate viewport
		*/ 
		GuiseAJAX.prototype._processNavigate=function(element)
		{
			var navigateURI=DOMUtilities.getNodeText(element);	//report the requested location
			var viewportID=element.getAttribute(this.ResponseElement.VIEWPORT_ID);	//get the viewport ID, if there is one
			if(viewportID!=null)	//if there is a viewport ID
			{
				window.open(navigateURI, viewportID);	//open the URI in the designated viewport
				return null;
			}
			else	//if no viewport ID is specified, use the default viewport---just change our current location
			{
				return navigateURI;	//report the requested location
			}
		};

		/**Processes the AJAX patch response.
		Only child elements with IDs will be processed.
		@param element The element representing patch response.
		*/ 
		GuiseAJAX.prototype._processPatch=function(element)
		{
			var childNodes=element.childNodes;	//get all the child nodes of the element
			var childNodeCount=childNodes.length;	//find out how many children there are
			for(var i=0; i<childNodeCount; ++i)	//for each child node
			{
				var childNode=childNodes[i];	//get this child node
				if(childNode.nodeType==Node.ELEMENT_NODE)	//if this is an element
				{
					var id=childNode.getAttribute("id");	//get the child node's ID, if there is one
					if(id)	//if the element has an ID
					{
						var oldElement=document.getElementById(id);	//get the old element
						if(oldElement)	//if the element currently exists in the document
						{
							this._synchronizeElement(oldElement, childNode);	//synchronize this element tree
							updateComponents(oldElement, true);	//now that we've patched the old element, update any components that rely on the old element
						}
						else if(DOMUtilities.hasClass(childNode, "frame"))	//if the element doesn't currently exist, but the patch is for a frame, create a new frame
						{
//TODO fix alert("ready to import frame node");
							oldElement=document.importNode(childNode, true);	//create an import clone of the node
//TODO del alert("ready to add frame: "+typeof oldElement);
							guise.addFrame(oldElement);	//add this frame
//TODO fix alert("frame added");
						}
					}
				}
			}
			
/*TODO testirng IE7			
			if(document.recalc)
			{
				document.recalc();
			}
*/
		};

		/**Processes the AJAX attribute response.
		@param element The element representing attribute response.
		*/
/*TODO del or salvage
		GuiseAJAX.prototype._processAttribute=function(element)
		{
			var id=element.getAttribute("id");	//get the element ID
			var name=element.getAttribute(this.ResponseElement.NAME);	//get the attribute name
			if(id && name)	//if an ID and name are given
			{
				var oldElement=document.getElementById(id);	//get the element in the document
				if(oldElement)	//if we found the old element
				{
					if(name=="style")	//if this is the style attribute
					{
						var value=element.getAttribute(this.ResponseElement.VALUE);	//get the attribute value
						this._synchronizeElementStyle(oldElement, value);	//update the element style attribute
					}
					else	//TODO fix for general attributes
					{
						alert("Support for attribute "+name+" not yet supported.");
					}			
				}
			}
		};
*/

		/**Processes the AJAX remove response.
		@param element The element representing removal response.
		*/ 
		GuiseAJAX.prototype._processRemove=function(element)
		{
			var id=element.getAttribute("id");	//get the element ID, if there is one
//TODO del alert("processing remove with ID: "+id);
			if(id)	//if the element has an ID
			{
				var oldElement=document.getElementById(id);	//get the old element
				if(oldElement!=null)	//if we found the old element
				{
//TODO del alert("we found the old element");
					if(guise.frames.contains(oldElement))	//if we're removing a frame
					{
//TODO fix alert("removing frame "+id);
						guise.removeFrame(oldElement);	//remove the frame
					}
					else	//if we're removing any other node
					{
						uninitializeNode(oldElement, true);	//uninitialize the element
						oldElement.parentNode.removeChild(oldElement);	//remove the old element from the document
					}
				}
			}
/*TODO del when works
			var childNodes=element.childNodes;	//get all the child nodes of the element
			var childNodeCount=childNodes.length;	//find out how many children there are
			for(var i=0; i<childNodeCount; ++i)	//for each child node
			{
				var childNode=childNodes[i];	//get this child node
				if(childNode.nodeType==Node.ELEMENT_NODE)	//if this is an element
				{
					var id=childNode.getAttribute("id");	//get the child node's ID, if there is one
					if(id)	//if the element has an ID
					{
						var oldElement=document.getElementById(id);	//get the old element
						if(oldElement!=null)	//if we found the old element
						{
							oldElement.parentNode.removeChild(oldElement);	//remove the old element from the document
						}
					}
				}
			}
*/
		};

		/**The set of attribute names that should not be removed when synchronizing.*/
		GuiseAJAX.prototype.NON_REMOVABLE_ATTRIBUTE_SET=
		{
			"style":true,	//don't remove local styles, because they may be used by Guise (with frames, for instance)
			"onclick":true,	//don't remove the onclick attribute, because we may be using it for Safari to prevent a default action
			"hideFocus":true,	//don't remove the IE hideFocus attribute, because we're using it to fix the IE6 lack of CSS outline: none support
			"guiseStateWidth":true,	//don't remove Guise state attributes
			"guiseStateHeight":true
		};

		/**The set of attribute names that should not be copied literally when synchronizing.*/
		GuiseAJAX.prototype.UNCOPIED_ATTRIBUTE_SET=
		{
			"style":true,	//if this is a style attribute, we have to treat it differently, because neither Mozilla nor IE provide normal DOM access to the literal style attribute value
			"guise:patchType":true	//the guise:patchType attribute is used for patching information
		};

		/**Synchronizes an element hierarchy with its patch element.
		@param oldElement The old version of the element.
		@param element The element hierarchy to patch into the existing document.
		*/ 
		GuiseAJAX.prototype._synchronizeElement=function(oldElement, element)
		{
			var elementName=element.nodeName;	//save the element name
/*TODO del or salvage
			if(elementName==this.ResponseElement.ATTRIBUTE)	//if this is really an attribute patch
			{
				this._processAttribute(element);	//patch the element with this attribute information TODO now that we're doing this in the patch tree, there may be no need for the ID attribute; double-check
				return;	//don't do synchronization patching
			}
*/
				//get the content hash attributes before we update the attributes
			var oldElementContentHash=oldElement.getAttribute("guise:contentHash");	//get the old element's content hash, if any TODO use a constant
			var newElementContentHash=element.getAttribute("guise:contentHash");	//get the new element's content hash, if any TODO use a constant
/*TODO del
			if(oldElementContentHash==newElementContentHash)	//TODO del; testing
			{
				alert("we think: "+DOMUtilities.getNodeString(oldElement));
				alert("is the same as: "+DOMUtilities.getNodeString(element));
			}
*/

			var oldElementAttributeHash=oldElement.getAttribute("guise:attributeHash");	//get the old element's attribute hash, if any TODO use a constant
			var newElementAttributeHash=element.getAttribute("guise:attributeHash");	//get the new element's attribute hash, if any TODO use a constant
			var isAttributesChanged=oldElementAttributeHash!=newElementAttributeHash;	//see if the attributes have changed (this doesn't count for the content hash attribute, which we'll check separately)
			if(isAttributesChanged)	//if the attribute hash values are different
			{
	//TODO del alert("ready to synchronize element "+oldElement.nodeName+" with ID: "+oldElement.id+" against element "+element.nodeName+" with ID: "+element.getAttribute("id"));
					//remove any attributes the old element has that are not in the new element
				var oldAttributes=oldElement.attributes;	//get the old element's attributes
				for(var i=oldAttributes.length-1; i>=0; --i)	//for each old attribute
				{
					var oldAttribute=oldAttributes[i];	//get this attribute
					var oldAttributeName=oldAttribute.nodeName;	//get the attribute name
					var oldAttributeValue=oldAttribute.nodeValue;	//get the attribute value
					var attributeName=DOMUtilities.DOM_ATTRIBUTE_NAME_MAP[oldAttributeName] || oldAttributeName;	//convert the attribute name to its standard DOM form, changing "readOnly" to "readonly", for example
	//TODO fix or del				if(attributeValue!=null && attributeValue.length>0 && !element.getAttribute(attributeName))	//if there is really an attribute value (IE provides all possible attributes, even with those with no value) and the new element doesn't have this attribute
					if(element.getAttribute(attributeName)==null && !this.NON_REMOVABLE_ATTRIBUTE_SET[attributeName])	//if the new element doesn't have this attribute, and this isn't an attribute we shouldn't remove
					{
						//TODO see if there is a way to keep from removing all the non-null but empty default IE6 attributes
	//TODO del alert("ready to remove "+oldElement.nodeName+" attribute "+oldAttributeName+" with current value "+oldAttributeValue);
						oldElement.removeAttribute(oldAttributeName);	//remove the attribute normally (apparently no action will take place if performed on IE-specific attributes such as element.start)
	//TODO fix					i=0;	//TODO fix; temporary to get out of looking at all IE's attributes
					}
				}
				if(elementName!="button" && oldElement.value && element.getAttribute("value")==null)	//if there is an old value but no value attribute present in the new element (IE 6 and Mozilla do not show "value" in the list of enumerated values) (IE6 thinks that the value of a button is content, so ignore button values) TODO fix button values for non-IE6 browsers, maybe, but current button values are unused anyway because of the IE6 bug
				{
//TODO del alert("clearing value; old value was: "+oldElement.value);
					oldElement.value="";	//set the value to the empty string (setting the value to null will result in "null" being displayed in the input control on IE)
				}
					//patch in the new and changed attributes
				var attributes=element.attributes;	//get the new element's attributes
				for(var i=attributes.length-1; i>=0; --i)	//for each attribute
				{
					var attribute=attributes[i];	//get this attribute
					var attributeNodeName=attribute.nodeName;	//get the node name
					var attributeName=DOMUtilities.HTML_ATTRIBUTE_NAME_MAP[attributeNodeName] || attributeNodeName;	//get the attribute name, compensating for special HTML attributes such as "className"
					var attributeValue=attribute.nodeValue;	//get the attribute value
					if(!this.UNCOPIED_ATTRIBUTE_SET[attributeName])	//if this is not an attribute that shouldn't be copied
					{
						if(attributeName=="value")	//if this is the value attribute
						{
							var patchType=element.getAttribute("guise:patchType");	//get the patch type TODO use a constant
							if(patchType=="novalue")	//if we should ignore the value attribute
							{
								continue;	//go to the next attribute
							}
						}
						var oldAttributeValue=oldElement[attributeName];	//get the old attribute value
						var valueChanged=oldAttributeValue!=attributeValue;	//see if the value is really changing
/*TODO del unless we want to fix external-toGuise stylesheets
						if(valueChanged)	//if the value is changing, see if we have to do fixes for IE6 (if the value hasn't changed, that means there were no fixes before and no fixes afterwards; we may want to categorically do fixes in the future if we add attribute-based selectors)
						{
							if(attributeName=="className")	//if we're changing the class name
							{
								if(typeof guiseIE6Fix!="undefined")	//if we have IE6 fix routines loaded
								{
									var fixedAttributeValue=guiseIE6Fix.getFixedElementClassName(oldElement, attributeValue);	//get the IE6 fixed form of the class name TODO make sure this is done last if we start doing CSS2 attribute-based selectors
									if(fixedAttributeValue!=null)	//if the proposed attribute changed, make sure it's now that we already have (if the proposed attribute didn't change, it's the same as it was, meaning it's different than the old attribute)
									{
										if(fixedAttributeValue!=oldAttributeValue)	//if the fixed value isn't what we already had
										{
											attributeValue=fixedAttributeValue;	//used the fixed class name
										}
										else	//if the fixed value is the same as what we already had
										{
											valueChanged=false;	//there's nothing to change; the fix put us right back where we were
										}
									}
								}
							}
						}
*/
						if(valueChanged && attributeName=="src")	//if a "src" attribute changed (e.g. img.src), make sure that the new src is not a relative URL form of the current src, which would cause IE6 to needlessly reload the image
						{
							if(attributeValue.startsWith("/") && location.protocol+"//"+location.host+attributeValue==oldAttributeValue)	//if the new value is just the relative form of the old value
							{
								valueChanged=false;	//keep the old value to prevent IE6 from reloading the image
							}
						}
						if(valueChanged)	//if the old element has a different (or no) value for this attribute (Firefox maintains different values for element.getAttribute(attributeName) and element[attributeName]) (note also that using setAttribute() IE will sometimes throw an error if button.style is changed, for instance)
						{					
//TODO del alert("updating "+element.nodeName+" attribute "+attributeName+" from value "+oldElement.getAttribute(attributeName)+" to new value "+attributeValue);
							if(attributeName.indexOf(":")>0)	//if this is a namespaced attribute, we must use the DOM, because Firefox 1.5 won't allow the indexed notation for such attributes
							{
								oldElement.setAttribute(attributeName, attributeValue);	//update the old element's attribute; only use this method when we need to, because it may be slower on Firefox and does not work with certain DOM attributes
							}
							else	//if this is a normal attribute
							{
								oldElement[attributeName]=attributeValue;	//update the old element's attribute (this format works for Firefox where oldElement.setAttribute("value", attributeValue) does not)
							}
		//TODO: fix the Firefox problem of sending an onchange event for any elements that get updated from an Ajax request, but only later when the focus blurs
		//TODO fix the focus problem if the user has focus on an element that gets changed in response to the event
						}
					}
				}
				this._synchronizeElementStyle(oldElement, element.getAttribute("style"));	//patch in the new style
					//perform special-case attribute manipulations for certain elements
				if(elementName=="input")	//input checkboxes and radio buttons do not updated the checked state correctly based upon the "checked" attribute
				{
					var inputType=element.getAttribute("type");	//get the input type
					if(inputType=="radio" || inputType=="checkbox")	//if this is a radio button or a checkbox
					{
						oldElement.checked=element.getAttribute("checked")=="checked";	//update the checked state based upon the new specified checked attribute
					}
				}
			}
/*TODO del
			if(oldElementContentHash==newElementContentHash)	//TODO del; testing
			{
				alert("we think: "+DOMUtilities.getNodeString(oldElement));
				alert("is the same as: "+DOMUtilities.getNodeString(element));
			}
*/

			if(oldElementContentHash!=newElementContentHash)	//if the content hash values are different
			{
				if(!isAttributesChanged)	//if the main attributes didn't change, we'll still need to update the content hash attribute, which isn't accounted for by the attribute hash attribute
				{
					if(newElementContentHash)	//if there is a content hash
					{
						oldElement.setAttribute("guise:contentHash", newElementContentHash);	//update the content hash attribute manually TODO use a constant
					}
					else	//if there is no longer a content hash
					{
						oldElement.removeAttribute("guise:contentHash");	//remove the content hash attribute TODO use a constant
					}
				}
					//patch in the new child element hierarchy
				if(elementName=="textarea")	//if this is a text area, do special-case value changing (restructuring won't work in IE and Mozilla) TODO check for other similar types TODO use a constant
				{
					oldElement.value=DOMUtilities.getNodeText(element);	//set the new value to be the text of the new element
				}
				else	//for other elements, restructure the DOM tree normally
				{
					var oldChildNodeList=oldElement.childNodes;	//get all the child nodes of the old element
					var oldChildNodeCount=oldChildNodeList.length;	//find out how many old children there are
					var childNodeList=element.childNodes;	//get all the child nodes of the element
					var childNodeCount=childNodeList.length;	//find out how many children there are
					var isChildrenCompatible=true;	//start by assuming children are compatible; children will be compatible as long as the exiting children are of the same types and, if they are elements, of the same name
					for(var i=0; i<oldChildNodeCount && i<childNodeCount && isChildrenCompatible; ++i)	//for each child node (as long as children are compatible)
					{
						var oldChildNode=oldChildNodeList[i];	//get the old child node
						var childNode=childNodeList[i];	//get the new child node
						if(oldChildNode.nodeType==childNode.nodeType)	//if these are the same type of nodes
						{
							if(childNode.nodeType==Node.ELEMENT_NODE)	//if this is an element, check the name and ID
							{
								
								if(oldChildNode.nodeName.toLowerCase()!=childNode.nodeName.toLowerCase())	//if these are elements with different node names
								{
		//TODO del alert("found different node names; old: "+oldChildNode.nodeName+" and new: "+childNode.nodeName);
									isChildrenCompatible=false;	//these child elements aren't compatible because they have different node name
								}
								else	//if the names are the same but the IDs are different, assume that the entire child should be replaced rather than synchronized---the event listeners would probably be different anyway
								{
									var oldChildID=oldChildNode.getAttribute("id");	//get the old child ID
									var oldChildID=oldChildNode.id ? oldChildNode.id : null;	//normalize the ID because some browsers such as IE in HTML mode might return "" for a missing attribute rather than null
									var childID=childNode.getAttribute("id");	//get the new child's ID
	//TODO del alert("comparing "+oldChildNode.nodeName+" IDs "+oldChildID+" "+typeof oldChildID+" and "+childID+" "+typeof childID);
									if(oldChildID!=childID)	//if the IDs are different
									{
	//TODO fix alert("IDs don't match: "+oldChildNode.nodeName+" IDs "+oldChildID+" "+typeof oldChildID+" and "+childID+" "+typeof childID);
										isChildrenCompatible=false;	//these child elements aren't compatible because they have different IDs
									}
								}
	/*TODO maybe add this later to prevent shifting elements from creating duplicate IDs; it would be better to simply remove the child ID attribute, though
								else	//if the names are the same
								{
									var oldChildID=oldChildNode.getAttribute("id");	//get the old child ID
									var oldChildID=oldChildNode.id ? oldChildNode.id : null;	//normalize the ID because some browsers such as IE in HTML mode might return "" for a missing attribute rather than null
									var childID=childNode.getAttribute("id");	//get the new child's ID
	//TODO del alert("comparing "+oldChildNode.nodeName+" IDs "+oldChildID+" "+typeof oldChildID+" and "+childID+" "+typeof childID);
									if(oldChildID!=childID)	//if the IDs are different
									{
	//TODO fix alert("IDs don't match: "+oldChildNode.nodeName+" IDs "+oldChildID+" "+typeof oldChildID+" and "+childID+" "+typeof childID);
										isChildrenCompatible=false;	//these child elements aren't compatible because they have different IDs
									}
								}
	*/
							}
						}
						else	//if the node types are different
						{
	//TODO del alert("node types are different; old "+oldElement.nodeName+" with ID "+oldElement.id+" child node count: "+oldChildNodeCount+" new "+element.nodeName+" "+"with ID "+element.getAttribute("id")+" child: "+i+" of "+childNodeCount+" old node type: "+oldChildNode.nodeType+" new node type: "+childNode.nodeType);
	//TODO del alert("old node structure of parent is: "+DOMUtilities.getNodeString(oldElement));
							isChildrenCompatible=false;	//these child nodes aren't compatible because they are of different types
						}
					}
					
	/*TODO fix Firefox select hack; right now, this slows things down too much for IE
					if(elementName=="select")	//TODO hack for Firefox select, which will allow the value of the selected option to be updated but not update that value shown in the drop-down list when the display changes
					{
						isChildrenCompatible=false;	//TODO testing
					}
	*/
					if(isChildrenCompatible)	//if the children are compatible
					{
							//remove superfluous old nodes
						for(var i=oldChildNodeCount-1; i>=childNodeCount; --i)	//for each old child node that is not in the new node
						{
							var oldChildNode=oldChildNodeList[i];	//get this child node
							uninitializeNode(oldChildNode, true);	//uninitialize the node tree
	//TODO del alert("removing old node: "+oldChildNodeList[i].nodeName);
							oldElement.removeChild(oldChildNode);	//remove this old child
							
						}
	//TODO del alert("children are still compatible, old child node count: "+oldElement.childNodes.length+" new child node count "+childNodeCount);
					}
					else	//if children are not compatible
					{
	//TODO del alert("children are not compatible, old "+oldElement.nodeName+" with ID "+oldElement.id+" child node count: "+oldChildNodeCount+" new "+element.nodeName+" "+"with ID "+element.getAttribute("id")+" child node count "+childNodeCount+" (verify) "+element.childNodes.length);
						DOMUtilities.removeChildren(oldElement);	//remove all the children from the old element and start from scratch
	/*TODO fix, if can improve IE6, but it probably won't help much, as most incompatible children may be single-child changes
						if(isUserAgentIE6)	//if this is IE6, it will be much faster to use innerHTML to load the children
						{
							var innerHTML=DOMUtilities.DOMUtilities.appendNodeContentString(newStringBuilder, element);	//get the inner HTML to use
						}
	*/
	//TODO del alert("incompatible old element now has children: "+oldElement.childNodes.length);
					}
					for(var i=0; i<childNodeCount; ++i)	//for each new child node
					{
						var childNode=childNodeList[i];	//get this child node
						oldChildNodeList=oldElement.childNodes;	//get the old child nodes all over again, as we may have removed some nodes before arriving here, and we may add some nodes later on
						if(i<oldChildNodeList.length)	//if we already have an old child node
						{
							var oldChildNode=oldChildNodeList[i];	//get the old child node
							switch(childNode.nodeType)	//see which type of child node this is
							{
								case Node.ELEMENT_NODE:	//element
/*TODO del
									if(oldChildNode.id=="id1a-child")	//TODO del
									{
//TODO del										alert("ready to update old element: "+DOMUtilities.getNodeString(oldChildNode));
										alert("ready to update old element: "+oldChildNode.getAttribute("guise:contentHash"));
										alert("ready to update new element: "+childNode.getAttribute("guise:contentHash"));
									}
*/
									this._synchronizeElement(oldChildNode, childNode);	//synchronize these elements
									break;
								case Node.COMMENT_NODE:	//comment
								case Node.TEXT_NODE:	//text
									oldChildNode.nodeValue=childNode.nodeValue;	//copy the text over to the old node
									break;
								//TODO add checks for other elements, such as CDATA
							}
						}
						else	//if we're out of old child nodes, create a new one
						{
	try
	{
	//TODO del alert("ready to clone node: "+DOMUtilities.getNodeString(childNode));
	//TODO del alert("ready to clone node");
							var importedNode=document.importNode(childNode, true);	//create an import clone of the node
	//TODO del alert("imported node");
								//TODO del; now inside importNode
	/*TODO del
							if(!importedNode)	//TODO check and improve big IE hack
							{
	//TODO del							alert("big problem importing node: "+DOMUtilities.getNodeString(childNode));	//TODO fix importnode
								var dummyNode=document.createElement("div");	//create a dummy node
	//TODO fix							document.documentElement.appendChild(dummyNode);	//append the dummy node to the document
								dummyNode.innerHTML=DOMUtilities.getNodeString(childNode);	//convert the child node to a string and assign it to the dummy node
								importedNode=dummyNode.removeChild(dummyNode.childNodes[0]);	//remove the dummy node's first and only node, which is our new imported node
	//TODO fix							document.documentElement.removeChild(dummyNode);	//throw away the dummy node
							}
	*/
	//TODO del alert("ready to append node");
							oldElement.appendChild(importedNode);	//append the imported node to the old element
	//TODO del alert("ready to initialize node");
							initializeNode(importedNode, true);	//initialize the new imported node, installing the correct event handlers
	}
	catch(e)
	{
		alert("error creating new child node: "+DOMUtilities.getNodeString(childNode));
	}
						}
					}
				}
			}
		};

		/**Synchronizes the literal style of an element.
		@param oldElement The old version of the element.
		@param attributeValue The new literal value of the style attribute, which may be null or the empty string.
		*/ 
		GuiseAJAX.prototype._synchronizeElementStyle=function(oldElement, attributeValue)
		{
			var removableStyles={"backgroundColor":true, "color":true, "display":true, "visibility":true};	//create a new map of styles to remove if not assigned, with the style name as the key
			if(attributeValue)	//if there is a new style
			{
//TODO fix with something else to give IE layout					oldElement["contentEditable"]=false;	//for IE 6, give the component "layout" so that things like opacity will work
				var styles=attributeValue.split(";");	//split out the individual styles
				for(var styleIndex=styles.length-1; styleIndex>=0; --styleIndex)	//for each style
				{
					var styleComponents=styles[styleIndex].split(":");	//get a reference to this style and split out the property and value
					if(styleComponents.length==2)	//we expect there to be a property and a value
					{
						var styleToken=styleComponents[0].trim();	//trim the token
						var styleProperty=DOMUtilities.CSS_ATTRIBUTE_NAME_MAP[styleToken] || styleToken;	//get the CSS DOM attribute version
						var styleValue=styleComponents[1].trim();	//get the trimmed style value
						delete removableStyles[styleProperty];	//remove this style from the map of removable styles, indicating that we shouldn't remove this style
						if(oldElement.style[styleProperty]!=styleValue)	//if the style is different	TODO check about removing a style
						{
	//TODO del alert("ready to set element style "+styleProperty+" to value "+styleValue);
							oldElement.style[styleProperty]=styleValue;	//update this style
						}
					}
				}
			}
				//remove the removable styles that weren't assigned
			for(var removableStyleName in removableStyles)	//for each removable style that needs removed
			{
//TODO del when works alert("looking at removable style "+removableStyleName+" with old value "+oldElement.style[removableStyleName]);
				if(oldElement.style[removableStyleName])	//if this style was not in the new element but it was in the old element
				{
//TODO del when works alert("trying to remove style "+removableStyleName+" with old value "+oldElement.style[removableStyleName]);
					oldElement.style[removableStyleName]="";	//remove the style from the old element
				}				
			}
		};
	}

	this.httpCommunicator.setProcessHTTPResponse(this._createHTTPResponseCallback());	//set up our callback function for processing HTTP responses

}

var com=com||{}; com.guiseframework=com.guiseframework||{}; com.guiseframework.js=com.guiseframework.js||{};	//create the com.guiseframework.js package

/**A class encapsulating JavaScript Guise client functionality for Guise.
var frames The array of frame elements.
var modalFrame The current topmost modal frame, or null if there is no modal frame.
*/
com.guiseframework.js.Client=function()
{

	/**The array of drop targets, determined when the document is loaded. The drop targets are stored in increasing order of hierarchical depth.*/
	this._dropTargets=new Array();

	/**TODO del The array of original source images, keyed to 
	this.originalImageSrcs=new Array();*/

	/**The array of frame elements.*/
	this.frames=new Array();

	/**The current topmost modal frame, or null if there is no modal frame.*/
	this.modalFrame=null;

	/**The layer that allows modality by blocking user interaction to elements below.*/
	this._modalLayer=null;
	
	/**The iframe that hides select elements in IE6; positioned right below the modal layer.*/
	this._modalIFrame=null;

	/**The map of cursors that have been temporarily changed, keyed to the ID of the element the cursor of which has been changed.
	This is a tentative implementation, as blindly resetting the cursor after AJAX processing will prevent new cursors to be changed via AJAX.
	*/
	this.oldElementIDCursors=new Object();

	if(!com.guiseframework.js.Client._initialized)
	{
		com.guiseframework.js.Client._initialized=true;

		/**Adds a frame to the array of frames.
		This implementation adds the frame to the document, initializes the frame, and updates the modal state.
		@param frame The frame to add.
		*/
		com.guiseframework.js.Client.prototype.addFrame=function(frame)
		{
		//TODO fix so that it works both on IE and Firefox							oldElement.style.position="fixed";	//TODO testing
			frame.style.position="absolute";	//change the element's position to absolute; it should already be set like this, but set it specifically so that dragging will know not to drag a copy TODO update the element's initial position
		
			frame.style.visibility="hidden";	//TODO testing
			frame.style.left="-9999px";	//TODO testing; this works; maybe even remove the visibility changing
			frame.style.top="-9999px";	//TODO testing
		
			document.body.appendChild(frame);	//add the frame element to the document; do this first, because IE doesn't allow the style to be accessed directly with imported nodes until they are added to the document
			initializeNode(frame, true);	//initialize the new imported frame, installing the correct event handlers; do this before the frame is positioned, because initialization also fixes IE6 classes, which can affect position
			this._initializeFramePosition(frame);	//initialize the frame's position
		
			var openEffectClassName=DOMUtilities.getClassName(frame, STYLES.OPEN_EFFECT_REGEXP);	//get the open effect specified for this frame
			var openEffect=null;	//we'll create an open effect if appropriate
			if(openEffectClassName)	//if there is an open effect
			{
				var effectNameMatch=/^openEffect-([\w]+)/.exec(openEffectClassName);	//search for the effect name TODO use a constant
				var effectName=effectNameMatch && effectNameMatch.length==2 ? effectNameMatch[1] : null;	//retrieve the effect name
				var delayMatch=/delay-([\d]+)/.exec(openEffectClassName);	//search for the delay amount TODO use a constant
				var delay=delayMatch && delayMatch.length==2 ? (parseInt(delayMatch[1]) || 0) : 0;	//retrieve the delay amount, compensating for parse errors and defaulting to zero
		//TODO del alert("effect: "+effectName+" delay: "+delay);	
				switch(effectName)	//see which effect name this is
				{
					case "DelayEffect":	//if this is a simple delay effect TODO use a constant
						openEffect=new DelayEffect(frame, delay);	//create a delay effect
						break;
					case "OpacityFadeEffect":	//if this is an opacity fade effect TODO use a constant
						openEffect=new OpacityFadeEffect(frame, delay);	//create an opacity fade effect
						break;
				}
			}
			if(openEffect)	//if we have an open effect
			{
				openEffect.effectBegin=function(){frame.style.visibility="visible";};	//TODO testing
				openEffect.start();
			}
			else	//if there is no open effect
			{
				frame.style.visibility="visible";	//go ahead and make the frame visible
			}
		
		//TODO del; moved to updateModal()	frame.style.zIndex=256;	//give the element an arbitrarily high z-index value so that it will appear in front of other components TODO fix
			updateComponents(frame, true);	//update all the components within the frame
			this.frames.add(frame);	//add the frame to the array
			this._updateModal();	//update the modal state
			var focusable=getFocusableDescendant(frame);	//see if this frame has a node that can be focused
			if(focusable)	//if we found a focusable node
			{
				try
				{
					focusable.focus();	//focus on the node
				}
				catch(e)	//TODO fix
				{
					alert("error trying to focus element "+focusable.nodeName+" ID: "+focusable.id+" class: "+focusable.className+" visibility: "+focusable.style.visibility+" display: "+focusable.style.display+" disabled: "+focusable.style.disabled);
		/*TODO fix
					alert("error trying to focus element "+focusable.nodeName+" ID: "+focusable.id+" class: "+focusable.className+" visibility: "+focusable.style.visibility+" display: "+focusable.style.display+" disabled: "+focusable.style.disabled);
					alert("error trying to focus element "+focusable.nodeName+" ID: "+focusable.id+" class: "+focusable.className+" current visibility: "+focusable.currentStyle.visibility+" current display: "+focusable.currentStyle.display+" current disabled: "+focusable.currentStyle.disabled);
					alert("error trying to focus element "+focusable.nodeName+" ID: "+focusable.id+" class: "+focusable.className+" runtime visibility: "+focusable.runtimeStyle.visibility+" runtime display: "+focusable.runtimeStyle.display+" runtime disabled: "+focusable.runtimeStyle.disabled);
					alert("error trying to focus element great-grandparent: "+DOMUtilities.getNodeString(focusable.parentNode.parentNode.parentNode));
		*/
				}
			}
		//TODO del	debug(DOMUtilities.getNodeString(frame));
		};

		/**Removes a frame from the array of frames.
		This implementation removes the frame to the document, uninitializes the frame, and updates the modal state.
		@param frame The frame to remove.
		*/
		com.guiseframework.js.Client.prototype.removeFrame=function(frame)
		{
			var index=this.frames.indexOf(frame);	//get the frame index
			if(index>=0)	//if we know the index of the frame
			{
				this.frames.remove(index);	//remove the frame from the array
				uninitializeNode(frame, true);	//uninitialize the frame tree
				document.body.removeChild(frame);	//remove the frame element to the document
				this._updateModal();	//update the modal state
			}
		};

		/**Initializes the position of a frame.
		@param frame The frame to position.
		*/
		com.guiseframework.js.Client.prototype._initializeFramePosition=function(frame)
		{
		//TODO del var debugString="";
		//TODO del	var framePosition=new Point();	//we'll calculate the frame position; create an object rather than using primitives so that the internal function can access its variables via closure
			var frameX, frameY;	//we'll calculate the frame position
			var relatedComponentInput=DOMUtilities.getDescendantElementByName(frame, "input", new Map("name", "relatedComponentID"));	//get the input holding the related component ID
			var relatedComponent=relatedComponentInput ? document.getElementById(relatedComponentInput.value) : null;	//get the related component, if there is one
			if(relatedComponent)	//if there is a related component
			{
		//TODO del alert("found related component: "+relatedComponentID);
				var frameBounds=getElementBounds(frame);	//get the bounds of the frame
		//TODO del debugString+="frameBounds: "+frameBounds.x+","+frameBounds.y+","+frameBounds.width+","+frameBounds.height+"\n";
				var relatedComponentBounds=getElementBounds(relatedComponent);	//get the bounds of the related component
		//TODO del debugString+="relatedComponentBounds: "+relatedComponentBounds.x+","+relatedComponentBounds.y+","+relatedComponentBounds.width+","+relatedComponentBounds.height+"\n";
				var tether=DOMUtilities.getDescendantElementByClassName(frame, STYLES.FRAME_TETHER);	//get the frame tether, if there is one
				if(tether)	//if there is a frame tether
				{
					var positionTether=function()	//create a function to position relative to the tether
							{
					//TODO del alert("found tether: "+tether.id);
								var tetherBounds=getElementBounds(tether);	//get the bounds of the tether
					//TODO del debugString+="tetherBounds: "+tetherBounds.x+","+tetherBounds.y+","+tetherBounds.width+","+tetherBounds.height+"\n";
								var tetherX, tetherY, relatedComponentX, relatedComponentY;	//get the relevant tether anchor point and the relevant component point
								if(tetherBounds.x<=frameBounds.x+8)	//if the tether is on the left side (use an arbitrary amount to account for variations in browser position calculations) TODO compare centers, which will be more accurate
								{
					//TODO del alert("tether left");
									tetherX=tetherBounds.x;	//use the left side of the tether
									relatedComponentX=relatedComponentBounds.x+relatedComponentBounds.width;	//use the right side of the component
								}
								else	//if the tether is on the right side
								{
					//TODO del alert("tether right");
									tetherX=tetherBounds.x+tetherBounds.width;	//use the right side of the tether
									relatedComponentX=relatedComponentBounds.x;	//use the left side of the component
								}
								if(tetherBounds.y<=frameBounds.y+8)	//if the tether is on the top (use an arbitrary amount to account for variations in browser position calculations)
								{
					//TODO del alert("tether top");
									tetherY=tetherBounds.y;	//use the top of the tether
									relatedComponentY=relatedComponentBounds.y+relatedComponentBounds.height;	//use the bottom of the component
								}
								else	//if the tether is on the bottom
								{
					//TODO del alert("tether bottom");
									tetherY=tetherBounds.y+tetherBounds.height;	//use the bottom of the tether
									relatedComponentY=relatedComponentBounds.y;	//use the top of the component
								}
					//TODO del alert("tetherX: "+tetherX);
					//TODO del alert("tetherY: "+tetherY);
					//TODO del alert("relatedComponentX: "+relatedComponentX);
					//TODO del alert("relatedComponentY: "+relatedComponentY);
								var tetherDeltaX=tetherX-frameBounds.x;	//find the horizontal delta of the tether from the frame
					//TODO del alert("tetherDeltaX: "+tetherDeltaX);
								var tetherDeltaY=tetherY-frameBounds.y;	//find the vertical delta of the tether from the frame
					//TODO del alert("tetherDeltaY: "+tetherDeltaY);
								frameX=relatedComponentX-tetherDeltaX;	//position the frame tether horizontally on the related component
					//TODO del alert("frameX: "+frameX);
								frameY=relatedComponentY-tetherDeltaY;	//position the frame tether vertically on the related component
					//TODO del alert("frameY: "+frameY);
					//TODO del debugString+="frame pos: "+frameX+","+frameY+"\n";
					//TODO del alert(debugString);
								frame.style.left=frameX+"px";	//set the frame's horizontal position
								frame.style.top=frameY+"px";	//set the frame's vertical position
							};
					var tetherIMG=DOMUtilities.getDescendantElementByName(tether, "img");	//see if the tether has an image TODO use a constant
					if(tetherIMG && (tetherIMG.offsetWidth<=0 || tetherIMG.offsetHeight<=0))	//if there is a tether image with an invalid width and/or height
					{
		//TODO del alert("tether image: "+tetherIMG.src+" not yet loaded; size "+tetherIMG.offsetWidth+","+tetherIMG.offsetHeight);
						DOMUtilities.waitIMGLoaded(tetherIMG, positionTether);	//make sure the image is loaded before positioning on the tether
					}
					else	//if there is no tether image, or we already know its image size
					{
						positionTether();	//position on the tether without waiting for an image
					}
				}
				else
				{
					var viewportBounds=GUIUtilities.getViewportBounds();	//get the bounds of the viewport so that we can center the frame
					if(relatedComponentBounds.x<viewportBounds.x+(viewportBounds.width/2))	//if the related component is on the left half of the screen
					{
						frameX=relatedComponentBounds.x+relatedComponentBounds.width;	//put the frame on the right side
					}
					else	//if the related component is on the right half side of the screen
					{
						frameX=relatedComponentBounds.x-frameBounds.width;	//put the frame on the left side
					}
					if(relatedComponentBounds.y<viewportBounds.y+(viewportBounds.height/2))	//if the related component is on the top half of the screen
					{
						frameY=relatedComponentBounds.y+relatedComponentBounds.height;	//put the frame on the bottom side
					}
					else	//if the related component is on the bottom half side of the screen
					{
						frameY=relatedComponentBounds.y-frameBounds.height;	//put the frame on the top side
					}
					frame.style.left=frameX+"px";	//set the frame's horizontal position
					frame.style.top=frameY+"px";	//set the frame's vertical position
				}
			}
			else	//if this frame is not related to another component, center it
			{
				var viewportBounds=GUIUtilities.getViewportBounds();	//get the bounds of the viewport so that we can center the frame
				frameX=viewportBounds.x+((viewportBounds.width-frame.offsetWidth)/2);	//center the frame horizontally
				frameY=viewportBounds.y+((viewportBounds.height-frame.offsetHeight)/2);	//center the frame vertically
				frame.style.left=frameX+"px";	//set the frame's horizontal position
				frame.style.top=frameY+"px";	//set the frame's vertical position
			}
		};

		/**Updates the modal layer and current modal frame.
		Each frame is given a z-order in the order of frames, starting with a z-order of 100 and incrementing by 100.
		The page is assumed to have a z-order of 0.
		*/
		com.guiseframework.js.Client.prototype._updateModal=function()
		{
			var frameCount=this.frames.length;	//find out how many frames there are
			this.modalFrame=null;	//start out presuming there is no modal frame
			for(var i=0; i<frameCount; ++i)	//update the z-orders
			{
				var frame=this.frames[i];	//get a reference to this frame
				frame.style.zIndex=(i+1)*100;	//give the element the appropriate z-order
				if(DOMUtilities.hasClassName(frame, "frameModal"))	//if this is a modal frame TODO use a constant
				{
					this.modalFrame=frame;	//indicate our last modal frame
				}
			}
			if(this.modalFrame!=null)	//if there is a modal frame
			{
				this.updateModalLayer();	//always update the modal layer before it is shown, as IE may not always call resize to keep the modal layer updated
				this._modalLayer.style.zIndex=this.modalFrame.style.zIndex-1;	//place the modal layer directly behind the modal frame
				this._modalLayer.style.display="block";	//make the modal layer visible
				if(this._modalIFrame)	//if we have a modal IFrame
				{
					this._modalIFrame.style.zIndex=this._modalLayer.style.zIndex-1;	//place the modal iframe directly behind the modal layer
					this._modalIFrame.style.display="block";	//make the modal IFrame visible
				}
			}
			else	//if there is no modal frame
			{
				this._modalLayer.style.display="none";	//hide the modal layer
				if(this._modalIFrame)	//if we have a modal IFrame
				{
					this._modalIFrame.style.display="none";	//hide the modal iframe
				}
			}
		};

		/**Updates the size of the modal layer, creating it if necessary.*/
		com.guiseframework.js.Client.prototype.updateModalLayer=function()
		{
			if(this._modalLayer==null)	//if the modal layer has not yet been created
			{
				this._modalLayer=document.createElementNS("http://www.w3.org/1999/xhtml", "div");	//create a div TODO use a constant for the namespace
				this._modalLayer.className="modalLayer";	//load the modal layer style
//TODO fix				oldModalLayerDisplayDisplay="none";
				this._modalLayer.style.display="none";
				this._modalLayer.style.position="absolute";
				this._modalLayer.style.top="0px";
				this._modalLayer.style.left="0px";
				document.body.appendChild(this._modalLayer);	//add the modal layer to the document
				if(isUserAgentIE6 && !this._modalIFrame)	//if we're in IE6 and we haven't found our modal IFrame, create a modal IFrame to keep select components from showing through; but don't do this in Mozilla, or it will keep the cursor from showing up for text inputs in absolutely positioned div elements above the IFrame
				{
					var form=getForm(document.documentElement);	//get the form
					if(form && form.id)	//if there is a form with an ID
					{
						var modalIFrameID=form.id.replace(".form", ".modalIFrame");	//determine the ID of the modal IFrame TODO use a constant, or get these values using a better method
						this._modalIFrame=document.getElementById(modalIFrameID);	//get the modal IFrame
/*TODO del when works
					this._modalIFrame=document.createElementNS("http://www.w3.org/1999/xhtml", "iframe");	//create an IFrame TODO use a constant for the namespace
					this._modalIFrame.src="about:blank";
			//TODO del		modalIFrame.className="modalLayer";	//load the modal layer style
			//TODO del; allows select elements to shine through		modalIFrame.allowTransparency="true";
					this._modalIFrame.frameBorder="0";
			//TODO del or fix		modalIFrame.style.backgroundColor="transparent";
					this._modalIFrame.style.display="none";
					this._modalIFrame.style.position="absolute";
					this._modalIFrame.style.top="0px";
					this._modalIFrame.style.left="0px";
					this._modalIFrame.style.filter='progid:DXImageTransform.Microsoft.Alpha(style=0,opacity=0)';	//make the frame transparent (see http://dotnetjunkies.com/WebLog/jking/archive/2003/07/21/488.aspx )
					document.body.appendChild(this._modalIFrame);	//add the modal IFrame to the document
*/
					}
				}
			}
		
			var oldModalLayerDisplay=this._modalLayer.style.display;	//get the current display status of the modal layer
			this._modalLayer.style.display="none";	//make sure the modal layer is hidden, because having it visible will interfere with the page/viewport size calculations (setting the size to 0px will not give us immediate feedback in IE during resize)
			var oldModalIFrameDisplay=null;	//get the old modal IFrame display if we need to
			if(this._modalIFrame)	//if we have a modal IFrame
			{
				oldModalIFrameDisplay=this._modalIFrame.style.display;	//get the current display status of the modal layer
				this._modalIFrame.style.display="none";	//make sure the modal layer is hidden, because having it visible will interfere with the page/viewport size calculations
			}
		
		/*TODO del; doesn't work instantanously with IE
			modalLayer.style.width="0px";	//don't let the size of the modal layer get in the way of the size calculations
			modalLayer.style.height="0px";
			if(modalIFrame)	//if we have a modal IFrame
			{
				modalIFrame.style.width="0px";	//don't let the size of the modal layer get in the way of the size calculations
				modalIFrame.style.height="0px";
			}
		*/
		
			var pageSize=getPageSize();	//get the size of the page
			var viewportSize=GUIUtilities.getViewportSize();	//get the size of the viewport
			this._modalLayer.style.width=Math.max(viewportSize.width, pageSize.width)+"px";	//update the size of the modal layer to the larger of the page and the viewport
			this._modalLayer.style.height=Math.max(viewportSize.height, pageSize.height)+"px";
		/*TODO fix
		alert("page: "+pageSize.width+","+pageSize.height+" viewport: "+viewportSize.width+","+viewportSize.height+" modalLayer: "+modalLayer.style.width+","+modalLayer.style.height
			+"\n"+"scroll: "+document.body.scrollWidth+","+document.body.scrollHeight+" offset: "+document.body.offsetWidth+","+document.body.offsetHeight);
		*/
		/*TODO fix
		alert("pageSize.width: "+pageSize.width+"\n"+
					"viewportSize.width: "+viewportSize.width+"\n"+
					"document.body.scrollWidth: "+document.body.scrollWidth+"\n"+
					"document.body.offsetWidth: "+document.body.offsetWidth+"\n"+
					"document.body.clientWidth: "+document.body.clientWidth+"\n"+
					"document.documentElement.scrollWidth: "+document.documentElement.scrollWidth+"\n"+
					"document.documentElement.offsetWidth: "+document.documentElement.offsetWidth+"\n"+
					"document.documentElement.clientWidth: "+document.documentElement.clientWidth+"\n"+
					"modalLayer.style.width: "+modalLayer.style.width);
		*/
		
			this._modalLayer.style.display=oldModalLayerDisplay;	//show the modal layer, if it was visible before
			if(this._modalIFrame)	//if we have a modal IFrame
			{
				this._modalIFrame.style.width=this._modalLayer.style.width;
				this._modalIFrame.style.height=this._modalLayer.style.height;
				this._modalIFrame.style.display=oldModalIFrameDisplay;	//show the modal IFrame, if it was visible before
			}
		};

		/*Sets the busy indicator visible or hidden.
		@param busyVisible A boolean indication of whether the busy indicator should be visible.
		*/
		com.guiseframework.js.Client.prototype.setBusyVisible=function(busyVisible)
		{
			var form=getForm(document.documentElement);	//get the form
			if(form && form.id)	//if there is a form with an ID
			{
				var busyID=form.id.replace(".form", ".busy");	//determine the ID of the busy element TODO use a constant, or get these values using a better method
				var busyElement=document.getElementById(busyID);	//get the busy element
				if(busyElement)	//if there is a busy element
				{
					busyElement.style.display=busyVisible ? "block" : "none";	//show or hide the busy information
					if(busyVisible)	//TODO testing
					{
						GUIUtilities.centerNode(busyElement);
					}
				}
			}
		};

		/**Adds an element to the list of drop targets.
		@param element The element to add to the list of drop targets.
		*/
		com.guiseframework.js.Client.prototype.addDropTarget=function(element)
		{
			this._dropTargets.add(element);	//add this element to the list of drop targets
			this._dropTargets.sort(function(element1, element2) {return DOMUtilities.getElementDepth(element1)-DOMUtilities.getElementDepth(element2);});	//sort the drop targets in increasing order of document depth
		};

		/**Determines the drop target at the given coordinates.
		@param x The horizontal test position.
		@param y The vertical test position.
		@return The drop target at the given coordinates, or null if there is no drop target at the given coordinates.
		*/
		com.guiseframework.js.Client.prototype.getDropTarget=function(x, y)
		{
			for(var i=this._dropTargets.length-1; i>=0; --i)	//for each drop target (which have been sorted by increasing element depth)
			{
				var dropTarget=this._dropTargets[i];	//get this drop target
				var dropTargetCoordinates=getElementFixedCoordinates(dropTarget);	//get the coordinates of the drop target
				if(x>=dropTargetCoordinates.x && y>=dropTargetCoordinates.y && x<dropTargetCoordinates.x+dropTarget.offsetWidth && y<dropTargetCoordinates.y+dropTarget.offsetHeight)	//if the coordinates are within the drop target area
				{
					return dropTarget;	//we've found the deepest drop target
				}
			}
		};

		/**Loads an image so that it will be present when needed.
		@param src The URL of the image to load.
		*/
		com.guiseframework.js.Client.prototype.loadImage=function(src)
		{
			var image=new Image();	//create a new image
			image.src=src;	//set the src of the image so that it will load
		};

	}
};

var guise=new com.guiseframework.js.Client();	//create a new global variable for the Guise client

/**The global object for AJAX communication with Guise.*/
var guiseAJAX=new GuiseAJAX();

/**A class maintaining event function information and optionally adapting an event to a W3C compliant version.
<ul>
	<li>charCode</li>
</ul>
A decorator will be created for the event function.
@param currentTarget The object for which a listener should be added.
@param eventType The type of event.
@param fn The function to listen for the event.
@param useCapture Whether event capture should be used.
@param createDecorator Whether a decorator should be created to wrap the event function and ensure W3DC compliancy.
var decorator The decorator created to wrap the event function and ensure W3C compliancy.
*/
function EventListener(currentTarget, eventType, fn, useCapture, createDecorator)
{
	this.currentTarget=currentTarget;
	this.eventType=eventType;
	this.fn=fn;
	this.useCapture=useCapture;
	if(!EventListener.prototype._initialized)
	{
		EventListener.prototype._initialized=true;

		/**Creates an event function decorator to appropriately set up the event to be W3C compliant, including event.currentTarget support.
		@param eventFunction The event function to be decorated.
		@param currentTarget The node on which the event listener is to be registered.
		*/
		EventListener.prototype._createDecorator=function(eventFunction, currentTarget)
		{
			var eventListener=this;	//store the event listener so that it can be referenced later via closure
			return function(event)	//create the decorator function
			{
				event=eventListener.getW3CEvent(event, currentTarget);	//make sure the event is a W3C-compliant event
				eventFunction(event);	//call the event function with our new event information
			}
		};

		/**Retrieves W3C event information in a cross-browser manner.
		@param event The event information, or null if no event information is available (e.g. on IE).
		@param currentTarget The current target (the node to which the event listener is bound), or null if the current target is not known.
		@return A W3C-compliant event object.
		*/
		EventListener.prototype.getW3CEvent=function(event, currentTarget)
		{
		/*TODO del
		alert("looking at event: "+event);
		alert("looking at event target: "+event.target);
		alert("looking at event src element: "+event.srcElement);
		*/
			if(!event)	//if no event was passed
			{
				if(window.event)	//if IE has provided an event object
				{
					event=window.event;	//switch to using the IE event
				}
				else	//if there is no IE event object
				{
					//TODO throw an assertion
				}
			}
				//fix event.target
			if(!event.target)	//if there is no target information
			{
				//TODO assert event.srcElement
				event.target=event.srcElement;	//assign a W3C target property
			}
				//fix event.currentTarget
			if(!event.currentTarget && currentTarget)	//if there is no current target information, but one was passed to us
			{
				event.currentTarget=currentTarget;	//assign a W3C current target property
			}
				//fix event.charCode
			if(!event.charCode)	//if this event has no character code
			{
				event.charCode=event.which ? event.which : event.keyCode;	//use the NN4  key indication if available; otherwise, use the key code TODO make sure this is a key event, because NN4 uses event.which for mouse events, too			
			}
				//fix event.stopPropagation
			if(!event.stopPropagation)	//if there is no method for stopping propagation TODO add workaround for Safari, which has this method but doesn't actually stop propagation
			{
				//TODO assert window.event && window.event.cancelBubble
				if(window.event && typeof window.event.cancelBubble=="boolean")	//if there is a global event with a cancel bubble property (e.g. IE)
				{
					event.stopPropagation=function()	//create a new function to stop propagation
							{
								window.event.cancelBubble=true;	//stop bubbling the IE way
							};
				}
			}
				//fix event.preventDefault
			if(!event.preventDefault)	//if there is no method for preventing the default functionality TODO add workaround for Safari, which has this method but doesn't actually prevent default functionality
			{
				//TODO assert window.event && window.event.returnValue
		//TODO find out why IE returns "undefined" for window.event.returnValue, yet enumerates it in for:in		if(window.event && typeof window.event.returnValue=="boolean")	//if there is a global event with a return value property (e.g. IE)
				if(window.event)	//if there is a global event with a return value property (e.g. IE)
				{
					event.preventDefault=function()	//create a new function to stop propagation
							{
								window.event.returnValue=false;	//prevent default functionality the IE way
							};
				}
			}
			//TODO update clientX and clientY as per _DHTML Utopia_ page 90.
			switch(event.type)	//for special types of events
			{
				case "mouseover":
					if(!event.relatedTarget && event.fromElement)	//if there is no related target information, but there is an IE fromElement property
					{
						event.relatedTarget=event.fromElement;	//use the fromElement property value
					}
					break;
				case "mouseout":
					if(!event.relatedTarget && event.toElement)	//if there is no related target information, but there is an IE toElement property
					{
						event.relatedTarget=event.toElement;	//use the toElement property value
					}
					break;
			}
			return event;	//return our event
		};
	}
	this.decorator=createDecorator ? this._createDecorator(fn, currentTarget) : null;	//create the decorator function if we were asked to do so
}


/**A class that manages events.*/
function EventManager()
{
	/**The array of event listeners.*/
	this._eventListeners=new Array();

	if(!EventManager.prototype._initialized)
	{
		EventManager.prototype._initialized=true;

		/**Adds an event listener to an object.
		@param object The object for which a listener should be added.
		@param eventType The type of event.
		@param fn The function to listen for the event.
		@param useCapture Whether event capture should be used.
		@see http://www.scottandrew.com/weblog/articles/cbs-events
		*/
		EventManager.prototype.addEvent=function(object, eventType, fn, useCapture)
		{
			var eventListener=null;	//we'll create an event listener and hold it here
			var result=true;	//we'll store the result here
			if(object.addEventListener)	//if the W3C DOM method is supported
			{
				object.addEventListener(eventType, fn, useCapture);	//add the event normally
				eventListener=new EventListener(object, eventType, fn, useCapture, false);	//create an event listener to keep track of the information
			}
			else	//if the W3C version isn't available
			{
				var eventName="on"+eventType;	//create the event name
				if(object.attachEvent)	//if we can use the IE version
				{
					eventListener=new EventListener(object, eventType, fn, useCapture, true);	//create an event listener with a decorator
					result=object.attachEvent(eventName, eventListener.decorator);	//attach the function decorator
				}
				else	//if we can't use the IE version
				{
					eventListener=new EventListener(object, eventType, fn, useCapture, true);	//create an event listener with a decorator
					object[eventName]=eventListener.decorator;	//use the object.onEvent property and our decorator
				}
			}
			this._eventListeners.add(eventListener);	//add this listener to the list
			return result;	//return the result
		};

		/**Removes an event listener from an object.
		@param object The object for which a listener should be removed.
		@param eventType The type of event.
		@param fn The function listening for the event.
		@param useCapture Whether event capture should be used.
		@param eventListener The optional event listener object containing the event decorator; if not provided it will be retrieved and removed from the list of event listeners.
		@see http://www.scottandrew.com/weblog/articles/cbs-events
		*/
		EventManager.prototype.removeEvent=function(object, eventType, fn, useCapture, eventListener)
		{
			if(!eventListener)	//if no event listener was provided
			{
				eventListener=this._removeEventListener(object, eventType, fn);	//remove and retrieve the event listener keeping information about this event			
			}
			if(object.removeEventListener)	//if the W3C DOM method is supported
			{
				object.removeEventListener(eventType, fn, useCapture);	//remove the event normally
			}
			else	//if the W3C version isn't available
			{
				var eventName="on"+eventType;	//create the event name
				if(object.detachEvent)	//if we can use the IE version
				{
					result=eventListener!=null ? object.detachEvent(eventName, eventListener.decorator) : null;	//detach the function decorator, if there is one
				}
				else	//if we can't use the IE version
				{
					object[eventName]=null;	//use the object.onEvent property
				}
		  }
		};
	
		/**Clears all registered events, optionally for a specific object.
		@param object The object for which events should be cleared, or null if events should be cleared on all objects.
		*/
		EventManager.prototype.clearEvents=function(object)
		{
			var eventListeners=this._eventListeners;	//get a reference to our event listeners
			if(object)	//if an object is specified
			{
				for(var i=eventListeners.length-1; i>=0; --i)	//for each event listener, going backwards so that removing an event listener will not disturb iteration
				{
					var eventListener=eventListeners[i];	//get the last event listener
					if(!object || eventListener.currentTarget==object)	//if this event listener was registered on this object, or if all event listeners should be removed)
					{
						eventListeners.remove(i);	//remove this event listener
						this.removeEvent(eventListener.currentTarget, eventListener.eventType, eventListener.fn, eventListener.useCapture, eventListener);	//remove this event, specifying the event listener so that it doesn't have to be looked up again
					}
				}
			}
			else	//if all event listeners should be cleared (though these loops could be combined, this is more efficient, and as this is usually called before a page unloads, we want to do this as fast as possible
			{
				for(var i=eventListeners.length-1; i>=0; --i)	//for each event listener
				{
					var eventListener=eventListeners[i];	//get the last event listener
					this.removeEvent(eventListener.currentTarget, eventListener.eventType, eventListener.fn, eventListener.useCapture, eventListener);	//remove this event, specifying the event listener so that it doesn't have to be looked up again
				}
				eventListeners.clear();	//clear all event listeners in one fell swoop
			}
		};
		
		/**Removes and returns an event listener object encapsulating information on the object, event type, and function.
		@param object The object for which a listener is listening.
		@param eventType The type of event.
		@param fn The function listening for the event.
		@return The event listener or null if no matching event listener could be found.
		*/
		EventManager.prototype._removeEventListener=function(object, eventType, fn)
		{
			for(var i=this._eventListeners.length-1; i>=0; --i)	//for each event listener
			{
				var eventListener=this._eventListeners[i];	//get this event listener
				if(eventListener.currentTarget==object && eventListener.eventType==eventType && eventListener.fn==fn)	//if this is the event listener
				{
					this._eventListeners.remove(i);	//remove this event listener
					return eventListener;	//return this event listener
				}
			}
			return null;	//indicate that we couldn't find a matching event listener
		}
	}
}		

/**Adds a function to be called when a window loads.
@param func The function to listen for window loading.
@see http://simon.incutio.com/archive/2004/05/26/addLoadEvent
*/
/*TODO transfer to EventManager and modify as fix for Safari
function addLoadListener(func)
{
	var oldonload=window.onload;	//get the old function
	if(typeof window.onload!="function")	//if there is no onload function
	{
		window.onload=func;	//use the given function
	}
	else	//if there is a window onload function
	{
		window.onload=function()	//create a new function
				{
					oldonload();	//call the old function using closure
					func();	//call the new function
				};
	}
}
*/

/**The single instance manager of events.*/
var eventManager=new EventManager();

/**A class encapsulating drag state.
By default the drag state allows dragging along both axes.
@param dragSource: The element to drag.
@param mouseX The horizontal position of the mouse.
@param mouseY The vertical position of the mouse.
var dragging true if dragging is occurring, else false.
var dragSource: The element to drag.
var element: The actual element being dragged, which may or may not be the same element as the drag souce.
var initialFixedPosition: The initial position of the drag source in fixed terms of the viewport.
var initialOffsetPosition: The initial position of the drag source relative to the offset parent.
var initialPosition: The initial position of the element in correct terms, fixed or offset; initialized when dragging starts.
var dragCopy Whether a copy of the element should be dragged, rather than the original element. Defaults to true unless the element is absolute or fixed.
var allowX: Whether dragging is allowed along the X axis (true by default).
var allowY: Whether dragging is allowed along the Y axis (true by default).
var minX: The minimum horizontal position, inclusive, in correct element terms, or null if there is no minumum horizontal position.
var maxX: The maximum horizontal position, inclusive, in correct element terms, or null if there is no maximum horizontal position.
var onBegin(element): The method called when dragging begins, or null if no additional action should be taken.
var onDrag(element, x, y): The method called when dragging occurs, or null if no additional action should be taken. The coordinates are in terms of the element's position type.
var onEnd(element): The method called when dragging ends, or null if no additional action should be taken.
*/
function DragState(dragSource, mouseX, mouseY)
{
	this.dragging=false;	//initially we are not dragging
	this.dragSource=dragSource;

	this.initialMouseFixedPosition=new Point(mouseX, mouseY);
//TODO del alert("initial mouse fixed position X: "+mouseX+" Y: "+mouseY);
	this.initialFixedPosition=getElementFixedCoordinates(dragSource);	//get the initial position of the drag source in fixed terms of the viewport
	this.initialOffsetPosition=new Point(dragSource.offsetLeft, dragSource.offsetTop);	//get the offset position of the drag source

	this.initialPosition=null;	//these will be updated when dragging is started
	this.mouseDeltaX=0;
	this.mouseDeltaY=0;

	this.minX=null;
	this.maxX=null;	
//TODO fix	this.initialPosition=new Point(dragSource.offsetLeft, dragSource.offsetTop);	//get the position of the drag source
	
//TODO fix	this.initialPosition=getElementFixedCoordinates(dragSource);	//get the position of the drag source
/*TODO fix
	this.mouseDeltaX=mouseX-this.initialPosition.x;	//calculate the mouse position relative to the drag source
	this.mouseDeltaY=mouseY-this.initialPosition.y;
*/
	if(dragSource.style)	//if the drag source has style specified
	{
		var style=dragSource.style;	//get the element style
		this.dragCopy=style.position!="absolute" && style.position!="fixed";	//see if the drag source is already fixed or absolutely positioned; if so, we won't drag a copy	
	}
	else	//if this drag source has no style specified
	{
		this.dragCopy=true;	//default to dragging a copy
	}
	this.allowX=true;	//default to allowing dragging along the X axis
	this.allowY=true;	//default to allowing dragging along the Y axis

	if(!DragState.prototype._initialized)
	{
		DragState.prototype._initialized=true;

		/**Begins the drag process.
		@param mouseX The horizontal position of the mouse.
		@param mouseY The vertical position of the mouse.
		*/
		DragState.prototype.beginDrag=function(mouseX, mouseY)
		{		
			this.element=this._getDragElement();	//create an element for dragging
/*TODO del when works
			this.width=this.element.offsetWidth;	//store the size of the element, because IE6 can sometimes reset the width and height to zero during AJAX calls for some unknown reason
			this.height=this.element.offsetHeight;
*/

			this.drag(mouseX, mouseY);	//drag the element to the current mouse position
			if(this.element!=this.dragSource)	//if we have a new element to drag
			{
				this.oldVisibility=this.dragSource.style.visibility;	//get the old visibility status				
				this.dragSource.style.visibility="hidden";	//hide the original element
				document.body.appendChild(this.element);	//add the element to the document
			}
/*TODO del after new stop default method
			document.body.ondrag=function() {return false;};	//turn off IE drag event processing; see http://www.ditchnet.org/wp/2005/06/15/ajax-freakshow-drag-n-drop-events-2/
			document.body.onselectstart=function() {return false;};
*/
//TODO del if not needed			drag(mouseX, mouseY);	//do a fake drag to make sure that the position of the element is within any ranges
			this.dragging=true;	//show that we are dragging
			eventManager.addEvent(document, "mousemove", onDrag, false);	//listen for mouse move anywhere in document (IE doesn't allow us to listen on the window), as dragging may end somewhere else besides a drop target
			if(this.onDragBegin)	//if there is a function for beginning dragging
			{
				this.onDragBegin(this.element);	//call the dragging begin method
			}
		};

		/*Drags the component to the location indicated by the mouse coordinates.
		The mouse/component deltas are taken into consideration when calculating the new component position.
		@param mouseX The horizontal position of the mouse.
		@param mouseY The vertical position of the mouse.
		*/
		DragState.prototype.drag=function(mouseX, mouseY)
		{
//TODO fix alert("dragging mouse X: "+mouseX+" mouseY: "+mouseY+" deltaX: "+dragState.mouseDeltaX+" deltaY: "+dragState.mouseDeltaY);
			var oldLeft=this.element.style.left;	//get the old left position
			var oldTop=this.element.style.top;	//get the old top position
/*TODO del when works
			var oldX=typeof oldLeft!="undefined" && oldLeft.length>0 ? parseInt(oldLeft) : this.initialPosition.x;	//find the old coordinates
			var oldY=typeof oldTop!="undefined" && oldTop.length>0 ? parseInt(oldTop) : this.initialPosition.y;
*/
			var oldX=oldLeft ? parseInt(oldLeft) : this.initialPosition.x;	//find the old coordinates
			var oldY=oldTop ? parseInt(oldTop) : this.initialPosition.y;

			var newX=oldX;	//we'll determine the new X and Y values
			var newY=oldY;
			if(this.allowX)	//if horizontal dragging is allowed
			{
				var onTrackY=this.allowY || (mouseY>=this.initialFixedPosition.y && mouseY<(this.initialFixedPosition.y+this.element.offsetHeight))	//see if the mouse is on the track vertically
				if(onTrackY)	//if the mouse is on the track
				{
					newX=mouseX-dragState.mouseDeltaX;	//calculate the new left position
				}
				else	//if the mouse is off the track
				{
					newX=this.initialPosition.x;	//reset the horizontal position
//TODO del alert("off track Y, mouse Y: "+mouseY+" deltaY: "+dragState.mouseDeltaY+" initialY: "+this.initialFixedPosition.y+" element height: "+this.element.offsetHeight);
				}
				if(this.minX!=null && newX<this.minX)	//if there is a minimum specified and the new position is below it
				{
					newX=this.minX;	//stop at the floor
				}
				else if(this.maxX!=null && newX>this.maxX)	//if there is a maximum specified and the new position is above it
				{
					newX=this.maxX;	//stop at the ceiling
				}
			}
			if(this.allowY)	//if vertical dragging is allowed
			{
				var onTrackX=this.allowX || (mouseX>=this.initialFixedPosition.x && mouseX<(this.initialFixedPosition.x+this.element.offsetWidth))	//see if the mouse is on the track horizontally
				if(onTrackX)	//if the mouse is on the track
				{
					newY=mouseY-dragState.mouseDeltaY;	//calculate the new top position
				}
				else	//if the mouse is off the track
				{
					newY=this.initialPosition.y;	//reset the vertical position
//TODO del alert("off track X, mouse X: "+mouseX+" mouse delta X: "+dragState.mouseDeltaX);
				}
				if(this.minY!=null && newY<this.minY)	//if there is a minimum specified and the new position is below it
				{
					newY=this.minY;	//stop at the floor
				}
				else if(this.maxY!=null && newY>this.maxY)	//if there is a maximum specified and the new position is above it
				{
					newY=this.maxY;	//stop at the ceiling
				}
			}
			if(newX!=oldX || newY!=oldY)	//if one of the coordinates has changed
			{
//TODO del alert("oldX: "+oldX+" oldY: "+oldY+" newX: "+newX+" newY: "+newY);
				if(newX!=oldX)	//if the horizontal position has changed
				{
					this.element.style.left=newX.toString()+"px";	//update the horizontal position of the dragged element
				}
				if(newY!=oldY)	//if the horizontal position has changed
				{
					this.element.style.top=newY.toString()+"px";	//update the vertical position of the dragged element
				}
				if(this.onDrag)	//if there is a function for dragging
				{
					this.onDrag(this.element, newX, newY);	//call the dragging method
				}
			}
		}
	
		/**Ends the drag process.*/
		DragState.prototype.endDrag=function()
		{
			eventManager.removeEvent(document, "mousemove", onDrag, false);	//stop listening for mouse moves
/*TODO del after new stop default method
			document.body.ondrag=null;	//turn IE drag event processing back on
			document.body.onselectstart=null;
*/
			if(this.element!=this.dragSource)	//if we have a different element that we're dragging
			{
				document.body.removeChild(this.element);	//remove the drag element
				this.dragSource.style.visibility=this.oldVisibility;	//reset the original element's visibility status
			}
			this.dragging=false;	//show that we are no longer dragging
			if(this.onDragEnd)	//if there is a function for ending dragging
			{
				this.onDragEnd(this.element);	//call the dragging end method
			}
//TODO del alert("ended drag, drag element offsetWidth: "+this.element.offsetWidth+" offsetHeight: "+this.element.offsetHeight);
		};

		/**@return An element appropriate for dragging, such as a clone of the original.*/
		DragState.prototype._getDragElement=function()
		{
			var element;	//we'll determine which element to use
			if(this.dragCopy)	//if we should make a copy of the element
			{
				this.initialPosition=getElementCoordinates(this.dragSource);	//get the absolute element coordinates, as we'll be positioning the element absolutely

				element=this.dragSource.cloneNode(true);	//create a clone of the original element
				this._cleanClone(element);	//clean the clone
				//TODO clean the element better, removing drag handles and such
	/*TODO add workaround to cover IE select controls, which are windowed and will appear over the dragged element
				if(document.all)	//if this is IE	TODO add better check
				{
					var shimElement=document.createElement("iframe");	//create a shim iframe that can accept z-index changes so as to cover controls; see http://dotnetjunkies.com/WebLog/jking/archive/category/139.aspx and http://dev2dev.bea.com/pub/a/2005/04/portal_menus.html
					shimElement.appendChild(element);	//place the real element inside the shim element
					element=shimElement;	//use the shim element as the drag element
				}
	*/

				element.style.left=(this.initialPosition.x).toString()+"px";	//initialize the horizontal position of the copy
				element.style.top=(this.initialPosition.y).toString()+"px";	//initialize the vertical position of the copy
				element.style.position="absolute";	//change the element's position to absolute TODO update the element's initial position
				element.style.zIndex=9001;	//give the element an arbitrarily high z-index value so that it will appear in front of other components TODO calculate the highest z-order
				//TODO make sure resizeable elements are the correct size

			}
			else	//if we should keep the same element
			{
				element=this.dragSource;	//drag the drag source itself
				if(element.style && element.style.position=="fixed")	//if this is a fixed element
				{
					this.initialPosition=this.initialFixedPosition;	//used fixed coordinates
				}
				else	//if this is not a fixed element, or we don't know
				{
					this.initialPosition=this.initialOffsetPosition;	//the initial position is the offset position TODO check for fixed position, which would also mean using fixed coordinates
				}
			}
//TODO del alert("element: "+element.nodeName+" class: "+element.className);
			this.mouseDeltaX=this.initialMouseFixedPosition.x-this.initialPosition.x;	//calculate the mouse position relative to the drag source
			this.mouseDeltaY=this.initialMouseFixedPosition.y-this.initialPosition.y;
//TODO del alert("initialXY: "+this.initialPosition.x+", "+this.initialPosition.y+" mouseXY: "+this.initialMouseFixedPosition.x+", "+this.initialMouseFixedPosition.y+" deltaXY: "+this.mouseDeltaX+", "+this.mouseDeltaY);
			return element;	//return the cloned element
		};

		/**Cleans a cloned node, removing its element IDs, for example, so that it can inserted into a document.*/
		DragState.prototype._cleanClone=function(elementClone)
		{
			if(elementClone.nodeType==Node.ELEMENT_NODE)	//if this is an element
			{
				if(elementClone.id)	//if this element has an ID
				{
					elementClone.id=null;	//remove the ID
				}
			}
			var childNodeList=elementClone.childNodes;	//get all the child nodes
			for(var i=childNodeList.length-1; i>=0; --i)	//for each child node
			{
				this._cleanClone(childNodeList[i]);	//clean this child nodethis child node
			}
		};
	}
}

/**The global drag state variable.*/
var dragState;

//Guise functionality


/*TODO del; testing

function testNode(node, deep)
{
	var x=node.nodeName;
	if(deep)
	{
			//initialize child nodes
		var childNodeList=node.childNodes;	//get all the child nodes
		var childNodeCount=childNodeList.length;	//find out how many children there are
		for(var i=0; i<childNodeCount; ++i)	//for each child node
		{
			testNode(childNodeList[i], true);	//initialize this child node
		}
	}
}
*/


/**Called when the window loads.
This implementation installs listeners.
*/
function onWindowLoad()
{
/*TODO del
		//~2500
	var time1=new Date();
	for(var loop=0; loop<100; ++loop)
	{
		testNode(document.documentElement, true);
	}
	var time2=new Date();
	alert("walking the tree: "+(time2.getTime()-time1.getTime()));
	
		//~1000
	var time3=new Date();
	for(var loop=0; loop<100; ++loop)
	{
		var all=document.all;
		var allCount=all.length;
		for(var i=0; i<allCount; ++i)
		{
			testNode(all[i]);
		}
	}
	var time4=new Date();
	alert("looking at all: "+(time4.getTime()-time3.getTime()));
*/



	guise.setBusyVisible(true);	//turn on the busy indicator
		//TODO fix; doesn't seem to work on IE6 or Firefox
	guise.oldElementIDCursors[document.body.id]=document.body.style.cursor;	//save the old document body cursor; this will get reset after we receive the response from the AJAX initialization request
	document.body.style.cursor="wait";	//TODO testing

	






	window.setTimeout(function(){
	
	
	


//TODO display a wait cursor until we initialize everything

/*TODO del unless we want to fix external-toGuise stylesheets
	if(typeof guiseIE6Fix!="undefined")	//if we have IE6 fix routines loaded
	{
		guiseIE6Fix.fixStylesheets();	//fix all IE6 stylesheets
	}
*/

	eventManager.addEvent(window, "resize", onWindowResize, false);	//add a resize listener
//TODO del	eventManager.addEvent(window, "scroll", onWindowScroll, false);	//add a scroll listener
	eventManager.addEvent(window, "unload", onWindowUnload, false);	//do the appropriate uninitialization when the window unloads
	initializeNode(document.documentElement, true, true);	//initialize the document tree, indicating that this is the first initialization
	updateComponents(document.documentElement, true);	//update all components represented by elements within the document
//TODO del when works	dropTargets.sort(function(element1, element2) {return getElementDepth(element1)-getElementDepth(element2);});	//sort the drop targets in increasing order of document depth
	eventManager.addEvent(document, "mouseup", onDragEnd, false);	//listen for mouse down anywhere in the document (IE doesn't allow listening on the window), as dragging may end somewhere else besides a drop target
	guise.updateModalLayer();	//create and update the modal layer TODO do we need or want this now? TODO put in an initialize method
	var focusable=getFocusableDescendant(document.documentElement);	//see if the document has a node that can be focused
	if(focusable)	//if we found a focusable node
	{
		focusable.focus();	//focus on the node
	}
	guiseAJAX.sendAJAXRequest(new InitAJAXEvent());	//send an initialization AJAX request	
//TODO del	alert("compatibility mode: "+document.compatMode);
	guise.setBusyVisible(false);	//turn off the busy indicator





}, 1);	//TODO testing



}

/**Called when the window unloads.
This implementation uninstalls all listeners.
@param event The object containing event information.
*/
function onWindowUnload(event)
{
	AJAX_ENABLED=false;	//turn off AJAX
//TODO fix or del	guise.setBusyVisible(true);	//turn on the busy indicator
	eventManager.clearEvents();	//unload all events
//TODO fix or del	guise.setBusyVisible(false);	//turn off the busy indicator
}

/**Called when the window resizes.
This implementation updates the modal layer.
@param event The object containing event information.
*/
function onWindowResize(event)
{
	//TODO work around IE bug that stops calling onWindowResize after a couple of maximization/minimization cycles
	window.setTimeout(function(){guise.updateModalLayer();}, 1);	//update the modal layer later, because during resize IE won't allow us to hide the modal layer and have the correct size update instantaneously; use closure to make sure the this variable is correctly passed to the function
}

/**Called when the window scrolls.
Note that Firefox 1.0.7 calls this method even when a scrollable element scrolls; this problem is fixed in Firefox 1.5.
@param event The object containing event information.
*/
/*TODO bring back if needed
function onWindowScroll(event)
{
alert("scroll");
}
*/

/**Initializes a node and optionally all its children, adding the correct listeners.
@param node The node to initialize.
@param deep true if the entire hierarchy should be initialized.
@param initialInitialization true if this is the first initialization of the entire page.
*/
function initializeNode(node, deep, initialInitialization)
{
	switch(node.nodeType)	//see which type of child node this is
	{
		case Node.ELEMENT_NODE:	//element
//TODO fix with something else to give IE layout			node["contentEditable"]=false;	//for IE 6, give the component "layout" so that things like opacity will work
//TODO bring back after giving all relevant nodes IDs			if(node.id)	//only look at element swith IDs
//TODO this may allow "layout" for IE, but only do it when we need it (otherwise it will screw up buttons and such)			node.style.zoom=1;	//TODO testing
			{
/*TODO del unless we want to fix external-toGuise stylesheets			
				if(!initialInitialization && (typeof guiseIE6Fix!="undefined"))	//if we have IE6 fix routines loaded, fix this element's class name (but don't do this for the first initialization, because we've already done this on the server)
				{
					guiseIE6Fix.fixElementClassName(node);	//fix the class name of this element
				}
*/
				var elementName=node.nodeName.toLowerCase();	//get the element name
				var elementClassName=node.className;	//get the element class name
				var elementClassNames=elementClassName ? elementClassName.split(/\s/) : EMPTY_ARRAY;	//split out the class names
				switch(elementName)	//see which element this is
				{
					case "a":
						if(isUserAgentIE6 && DOMUtilities.hasClassName(node, "imageSelectActionControl"))	//if this is IE6, which doesn't support the CSS outline: none property, create a workaround TODO use a constant; create something more general than just the image select action control
						{
							node.hideFocus="true";	//hide the focus on this element
						}
						if(elementClassNames.contains("actionControl"))	//if this is a Guise action TODO later look at *all* link clicks and do popups for certain ones
						{
							if(!node.getAttribute("target"))	//if the link has no target (the target wouldn't work if we tried to take over the events; we can't just check for null because IE will always send back at least "")
							{
								eventManager.addEvent(node, "click", onLinkClick, false);	//listen for anchor clicks
								if(isSafari)	//if this is Safari TODO fix better
								{
									node.onclick=function(){return false;};	//cancel the default action, because Safari 1.3.2 ignores event.preventDefault(); http://www.sitepoint.com/article/dhtml-utopia-modern-web-design/3
								}
							}
						}
						else if(elementClassNames.containsMatch(/-tab$/))	//if this is a tab TODO use a constant TODO is this still used?
						{
							eventManager.addEvent(node, "click", onTabClick, false);	//listen for tab clicks
							if(isSafari)	//if this is Safari TODO fix better
							{
								node.onclick=function(){return false;};	//cancel the default action, because Safari 1.3.2 ignores event.preventDefault(); http://www.sitepoint.com/article/dhtml-utopia-modern-web-design/3
							}
						}
						break;
					case "button":
						if(elementClassNames.contains("buttonControl"))	//if this is a Guise button TODO use constant
						{
							eventManager.addEvent(node, "click", onButtonClick, false);	//listen for button clicks
							if(isSafari)	//if this is Safari TODO fix better
							{
								node.onclick=function(){return false;};	//cancel the default action, because Safari 1.3.2 ignores event.preventDefault(); http://www.sitepoint.com/article/dhtml-utopia-modern-web-design/3
							}
						}
						break;
					case "div":
								//check for menu
						if(elementClassNames.contains(STYLES.MENU))	//if this is a menu
						{
							var menu=DOMUtilities.getAncestorElementByClassName(node, STYLES.MENU_BODY);	//get the menu ancestor
							if(menu)	//if there is a menu ancestor (i.e. this is not the root menu)
							{
//TODO del when works alert("for class "+className+" non-root menu: "+menu.id);
								eventManager.addEvent(node, "mouseover", onMenuMouseOver, false);
								eventManager.addEvent(node, "mouseout", onMenuMouseOut, false);
								break;
							}
//TODO del alert("found menu class: "+elementClassName);
						}
						break;
					case "img":
						var rolloverSrc=node.getAttribute("guise:rolloverSrc");	//get the image rollover, if there is one TODO use a constant
						if(rolloverSrc!=null)	//if the image has a rollover TODO use a constant
						{
							guise.loadImage(rolloverSrc);	//preload the image
							if(!DOMUtilities.hasClassName(node, STYLES.MOUSE_LISTENER))	//if this is not a mouse listener (which would get a onMouse listener registered, anyway)
							{
								eventManager.addEvent(node, "mouseover", onMouse, false);	//listen for mouse over on a mouse listener
								eventManager.addEvent(node, "mouseout", onMouse, false);	//listen for mouse out on a mouse listener							
							}
//TODO del							alert("rollover source: "+node.getAttribute("guise:rolloverSrc"));
						}
						break;
					case "input":
						switch(node.type)	//get the type of input
						{
							case "text":
							case "password":
								eventManager.addEvent(node, "change", onTextInputChange, false);
								eventManager.addEvent(node, "keypress", onTextInputKeyPress, false);
								eventManager.addEvent(node, "keyup", onTextInputKeyUp, false);
								break;
							case "checkbox":
							case "radio":
								eventManager.addEvent(node, "click", onCheckInputChange, false);
								break;
						}
						break;
					case "select":
						eventManager.addEvent(node, "change", onSelectChange, false);
/*TODO del
						var iframe=document.createElementNS("http://www.w3.org/1999/xhtml", "iframe");	//TODO testing
						iframe.src="about:blank";
						iframe.scrolling="no";
						iframe.frameborder="0";
						document.body.appendChild(iframe);
						iframe.position="absolute";
						var coordinates=getElementCoordinates(node);
						iframe.style.left=coordinates.x;
						iframe.style.top=coordinates.y;
						iframe.style.width=node.offsetWidth+"px";
						iframe.style.height=node.offsetHeight+"px";
						iframe.style.zIndex=1;
*/	
						
						break;
					case "textarea":
						eventManager.addEvent(node, "change", onTextInputChange, false);
						break;
				}
				for(var i=elementClassNames.length-1; i>=0; --i)	//for each class name
				{
					switch(elementClassNames[i])	//check out this class name
					{
/*TODO del
						case "button":	//TODO testing; del
							eventManager.addEvent(node, "click", onButtonClick, false);	//listen for button clicks
							break;
*/
						case STYLES.ACTION:
							eventManager.addEvent(node, "click", onActionClick, false);	//listen for a click on an action element
							eventManager.addEvent(node, "contextmenu", onContextMenu, false);	//listen for a right click on an action element
							//TODO see if we need to assign a default handler on Safari to prevent the default action
							break;
						case STYLES.DRAG_HANDLE:
							eventManager.addEvent(node, "mousedown", onDragBegin, false);	//listen for mouse down on a drag handle
							break;
						case STYLES.MOUSE_LISTENER:
							eventManager.addEvent(node, "mouseover", onMouse, false);	//listen for mouse over on a mouse listener
							eventManager.addEvent(node, "mouseout", onMouse, false);	//listen for mouse out on a mouse listener
							break;
						case STYLES.DROP_TARGET:
							guise.addDropTarget(node);	//add this node to the list of drop targets
							break;
						case STYLES.SLIDER_CONTROL_THUMB:
							eventManager.addEvent(node, "mousedown", onSliderThumbDragBegin, false);	//listen for mouse down on a slider thumb
							break;
					}
				}
				if(node.focus)	//if this element can receive the focus
				{
/*TODO fix
					if(node.nodeName=="a")
					{
						alert("listening for focus on a");
					}
*/
					eventManager.addEvent(node, "focus", onFocus, false);	//listen for focus events
				}
			}
			break;
	}
	if(deep)	//if we should initialize child nodes
	{
		var all=node.all;	//see if the node has an all[] array, because that will be much faster
		if(all)	//if there is an all[] array
		{
			var allCount=all.length;	//find out how many nodes there are
			for(var i=0; i<allCount; ++i)	//for each descendant node
			{
				initializeNode(all[i], false, initialInitialization);	//initialize this child node, but not its children
			}
		}
		else	//otherwise, walk the tree using the standard W3C DOM routines
		{
				//initialize child nodes
			var childNodeList=node.childNodes;	//get all the child nodes
			var childNodeCount=childNodeList.length;	//find out how many children there are
			for(var i=0; i<childNodeCount; ++i)	//for each child node
			{
				initializeNode(childNodeList[i], deep, initialInitialization);	//initialize this child subtree
			}
		}
	}
}

/**Updates the representation of any dynamic components based upon the state of the underlying element.
Components for the given node and any descendant nodes are updated.
@param node The node for which components should be updated.
@param deep true if the entire hierarchy should be initialized.
*/
function updateComponents(node, deep)
{
	switch(node.nodeType)	//see which type of child node this is
	{
		case Node.ELEMENT_NODE:	//element
//TODO bring back after giving all relevant nodes IDs			if(node.id)	//only look at element swith IDs
			{
				var elementName=node.nodeName.toLowerCase();	//get the element name
				var elementClassName=node.className;	//get the element class name
				var elementClassNames=elementClassName ? elementClassName.split(/\s/) : EMPTY_ARRAY;	//split out the class names
				switch(elementName)	//see which element this is
				{
					case "div":
								//check for slider
						if(elementClassNames.containsMatch(STYLES.SLIDER_CONTROL))	//if this is a slider control
						{
							updateSlider(node);	//update the slider
						}
						break;
				}
			}
			break;
	}
	if(deep)	//if we should update child components
	{
		var all=node.all;	//see if the node has an all[] array, because that will be much faster
		if(all)	//if there is an all[] array
		{
			var allCount=all.length;	//find out how many nodes there are
			for(var i=0; i<allCount; ++i)	//for each descendant node
			{
				updateComponents(all[i], false);	//update this component, but not its children
			}
		}
		else	//otherwise, walk the tree using the standard W3C DOM routines
		{
			var childNodeList=node.childNodes;	//get all the child nodes
			var childNodeCount=childNodeList.length;	//find out how many children there are
			for(var i=0; i<childNodeCount; ++i)	//for each child node
			{
				updateComponents(childNodeList[i], deep);	//update the components for this child subtree
			}
		}
	}
}

/**Uninitializes a node and optionally all its children, removing all added listeners.
@param node The node to uninitialize.
@param deep true if the entire hierarchy should be uninitialized.
*/
function uninitializeNode(node, deep)	//TODO remove the node from the sorted list of drop targets
{
	eventManager.clearEvents(node);	//clear events for this node
	if(deep)	//if we should uninitialize child nodes
	{
		var all=node.all;	//see if the node has an all[] array, because that will be much faster
		if(all)	//if there is an all[] array
		{
			var allCount=all.length;	//find out how many nodes there are
			for(var i=0; i<allCount; ++i)	//for each descendant node
			{
				uninitializeNode(all[i], false);	//uninitialize this child node, but not its children
			}
		}
		else	//otherwise, walk the tree using the standard W3C DOM routines
		{
				//uninitialize child nodes
			var childNodeList=node.childNodes;	//get all the child nodes
			var childNodeCount=childNodeList.length;	//find out how many children there are
			for(var i=0; i<childNodeCount; ++i)	//for each child node
			{
				uninitializeNode(childNodeList[i], deep);	//initialize this child subtree
			}
		}
	}
}

var lastFocusedNode=null;

/**Called when an element receives a focus event.
@param event The object containing event information.
*/
function onFocus(event)
{
	var currentTarget=event.currentTarget;	//get the control receiving the focus
	if(guise.modalFrame!=null)	//if there is a modal frame
	{
	
//TODO del var dummy=currentTarget.nodeName+" "+currentTarget.id;

		if(!DOMUtilities.hasAncestor(currentTarget, guise.modalFrame))	//if focus is trying to go to something outside the modal frame
		{
//TODO fix alert("focus outside of frame");
			if(DOMUtilities.hasAncestor(lastFocusedNode, guise.modalFrame))	//if we know the last focused node, and it was in the modal frame
			{

//TODO del dummy+=" changing to last focused "+lastFocusedNode.nodeName+" id "+lastFocusedNode.id;

				lastFocusedNode.focus();	//focus back on the last focused node


			}
			else	//if we don't know the last focused node
			{
				var focusable=getFocusableDescendant(guise.modalFrame);	//see if the modal frame has a node that can be focused
				if(focusable)	//if we found a focusable node
				{
//TODO del dummy+=" changing to first focusable "+focusable.nodeName+" id "+focusable.id;
					try
					{
						focusable.focus();	//focus on the node
					}						
					catch(e)	//TODO fix
					{
/*TODO fix
						alert("error trying to focus element "+focusable.nodeName+" ID: "+focusable.id+" style: "+focusable.style+" class: "+focusable.className+" visibility: "+focusable.style.visibility+" display: "+focusable.style.display+" disabled: "+focusable.style.disabled);
			alert("error trying to focus element "+focusable.nodeName+" ID: "+focusable.id+" class: "+focusable.className+" current visibility: "+focusable.currentStyle.visibility+" current display: "+focusable.currentStyle.display+" current disabled: "+focusable.currentStyle.disabled);
			alert("error trying to focus element "+focusable.nodeName+" ID: "+focusable.id+" class: "+focusable.className+" runtime visibility: "+focusable.runtimeStyle.visibility+" runtime display: "+focusable.runtimeStyle.display+" runtime disabled: "+focusable.runtimeStyle.disabled);
			alert("error trying to focus element great-grandparent: "+DOMUtilities.getNodeString(focusable.parentNode.parentNode.parentNode));
*/
					}
				}
				else	//if we can't find a focusable node on the modal frame
				{
//TODO fix for IE					currentTarget.blur();	//don't allow the element to get the focus, even though we don't know what to focus
				}
			}
/*TODO del 
test.add(dummy);

	if(test.length==8)
	{
		test.add("done");
		modalFrame=null;
		for(var i=0; i<test.length; ++i)
		{
			alert(test[i]);
		}
	}
*/
			return;	//don't process the focus event any further
		}
/*TODO del

test.add(dummy);

	if(test.length==8)
	{
		test.add("done");
		modalFrame=null;
		for(var i=0; i<test.length; ++i)
		{
			alert(test[i]);
		}
	}
*/
	}
	lastFocusedNode=currentTarget;	//see what was last focused
/*TODO fix
	if(modalFrame==null && test.length>10)
	{
		test.add("done");
		for(var i=0; i<test.length; ++i)
		{
			alert(test[i]);
		}
	}
*/
}

/**Called when a key is pressed in a text input.
This implementation checks to see if the Enter/Return key was pressed, and if so commits the input by sending it to the server.
If Enter/Return was not pressed, send the current value as a provisional value.
@param event The object describing the event.
*/
function onTextInputKeyPress(event)
{
	var charCode=event.charCode;	//get the code of the entered character
	if(charCode==13)	//if Enter/Return was pressed TODO use a constant
	{
		if(AJAX_ENABLED)	//if AJAX is enabled
		{
		//TODO del alert("an input changed! "+textInput.id);
			var textInput=event.currentTarget;	//get the control in which text changed
			var ajaxRequest=new FormAJAXEvent(new Map(textInput.name, textInput.value));	//create a new form request with the control name and value
			guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request
			event.stopPropagation();	//tell the event to stop bubbling
			event.preventDefault();	//prevent the default functionality from occurring
		}
		else	//TODO submit the form
		{
		}
	}
}

/**Called when a key is raised in a text input.
This implementation sends the current text input value as a provisional value.
@param event The object describing the event.
*/
function onTextInputKeyUp(event)
{
	var charCode=event.charCode;	//get the code of the entered character
	if(AJAX_ENABLED)	//if AJAX is enabled
	{
	//TODO del alert("an input changed! "+textInput.id);
		var textInput=event.currentTarget;	//get the control in which text changed
		var ajaxRequest=new FormAJAXEvent(new Map(textInput.name, textInput.value), true);	//create a new provisional form request with the control name and value
		guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request, but allow this event to be processed normally
	}
}

/**Called when the contents of a text input or a text area changes.
@param event The object describing the event.
*/
function onTextInputChange(event)
{
	if(AJAX_ENABLED)	//if AJAX is enabled
	{
		var textInput=event.currentTarget;	//get the control in which text changed
	//TODO del alert("an input changed! "+textInput.id);
		var ajaxRequest=new FormAJAXEvent(new Map(textInput.name, textInput.value));	//create a new form request with the control name and value
		guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request
		event.stopPropagation();	//tell the event to stop bubbling
	}
}

/**Called when a button is clicked.
@param event The object describing the event.
*/
function onButtonClick(event)
{
	var element=event.currentTarget;	//get the element on which the event was registered
	var form=getForm(element);	//get the form
	if(form && DOMUtilities.getDescendantElementByName(form, "input", new Map("type", "file")))	//if there is a file input element, we'll have to submit the entire page rather than using AJAX
	{
		if(element.id)	//if the button has an ID
		{
			if(form.id)	//if the form has an ID
			{
				var actionInputID=form.id.replace(".form", ".input");	//determine the ID of the hidden action input TODO use a constant, or get these values using a better method
				var actionInput=document.getElementById(actionInputID);	//get the action input
				if(actionInput)	//if there is an action input
				{
					actionInput.value=element.id;	//indicate which action was activated
				}
				form.submit();	//submit the form
				if(actionInput)	//if there is an action input
				{
					actionInput.value=null;	//remove the indication of which action was activated
				}
				event.stopPropagation();	//tell the event to stop bubbling
				event.preventDefault();	//prevent the default functionality from occurring
			}
		}
	}
	else	//if there is no file input element, we can submit the action via AJAX normally
	{
		onAction(event);	//process an action for the button
	}
}

/**Called when an anchor is clicked.
@param event The object describing the event.
*/
function onLinkClick(event)
{
	onAction(event);	//process an action for the anchor
}

/**Called when an action should be processed for an element
@param event The object describing the event.
@param element The element representing the action.
*/
function onAction(event)
{
	var target=event.currentTarget;	//get the element on which the event was registered
//TODO del alert("action on: "+element.nodeName);
	var component=DOMUtilities.getAncestorElementByClassName(target, STYLES.COMPONENT);	//get the component element TODO improve all this
	if(component)	//if there is a component
	{
		var componentID=component.id;	//get the component ID
		if(componentID)	//if there is a component ID
		{
			var form=getForm(component);	//get the form
			if(form && form.id)	//if there is a form with an ID
			{
				var actionInputID=form.id.replace(".form", ".input");	//determine the ID of the hidden action input TODO use a constant, or get these values using a better method
				if(AJAX_ENABLED)	//if AJAX is enabled
				{

//TODO fix					target.parentNode.style.cursor="inherit";	//TODO testing
//TODO fix					target.style.cursor="inherit";	//TODO testing
//TODO fix					document.body.style.cursor="wait";	//TODO testing
//TODO fix				alert("old cursor: "+target.style.cursor);
					guise.oldElementIDCursors[componentID]=target.style.cursor;	//save the old cursor
					component.style.cursor="wait";	//TODO testing

					var ajaxRequest=new FormAJAXEvent(new Map(actionInputID, componentID));	//create a new form request with form's hidden action control and the action element ID
					guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request			
				}
				else	//if AJAX is not enabled, do a POST
				{
					var actionInput=document.getElementById(actionInputID);	//get the action input
					if(actionInput)	//if there is an action input
					{
						actionInput.value=componentID;	//indicate which action was activated
					}
					form.submit();	//submit the form
					if(actionInput)	//if there is an action input
					{
						actionInput.value=null;	//remove the indication of which action was activated
					}
				}
				event.stopPropagation();	//tell the event to stop bubbling
				event.preventDefault();	//prevent the default functionality from occurring
			}
		}
	}
}

/**Called when a tab is clicked.
A tab link is expected to have an href with parameters in the form "?tabbedPanelID=tabID".
@param event The object describing the event.
*/
function onTabClick(event)	//TODO maybe refactor to use new action click
{
	var element=event.currentTarget;	//get the element on which the event was registered
	var href=element.href;	//get the link href, which should be in the form "?tabbedPanelID=tabID"
	if(href)	//if there is an href
	{
		var uri=new URI(href);	//create a URI from the href
		if(AJAX_ENABLED)	//if AJAX is enabled
		{
			var ajaxRequest=new FormAJAXEvent(uri.parameters);	//create a new form request with the URI parameters
			guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request
		}
/*TODO fix
			else	//if AJAX is not enabled, do a POST
			{
				var actionInput=document.getElementById(actionInputID);	//get the action input
				if(actionInput)	//if there is an action input
				{
					actionInput.value=element.id;	//indicate which action was activated
				}
				form.submit();	//submit the form
				if(actionInput)	//if there is an action input
				{
					actionInput.value=null;	//remove the indication of which action was activated
				}
			}
*/
		event.stopPropagation();	//tell the event to stop bubbling
		event.preventDefault();	//prevent the default functionality from occurring
	}
}

/**Called when an element marked as "action" is clicked with the left button.
This method searches up the hierarchy to find the enclosing "component" element and sends an AJAX action event.
@param event The object describing the event.
*/
function onActionClick(event)
{
//TODO del alert("in action click");
	var target=event.currentTarget;	//get the element on which the event was registered
	var targetID=target.id;	//get the target ID
	if(targetID)	//if the element has an ID (otherwise, we couldn't report the action)
	{
		var component=DOMUtilities.getAncestorElementByClassName(target, STYLES.COMPONENT);	//get the component element TODO improve all this
		if(component)	//if there is a component
		{
			var componentID=component.id;	//get the component ID
			if(componentID)	//if there is a component ID
			{
				if(AJAX_ENABLED)	//if AJAX is enabled
				{
/*TODO fix					
					target.parentNode.style.cursor="inherit";	//TODO testing
					target.style.cursor="inherit";	//TODO testing
					document.body.style.cursor="wait";	//TODO testing
*/
/*TODO fix
					guise.oldElementIDCursors[targetID]=target.style.cursor;	//save the old cursor
					target.style.cursor="wait";	//TODO testing
*/
					guise.oldElementIDCursors[targetID]=target.style.cursor;	//save the old cursor
					target.style.cursor="wait";	//TODO testing


					var ajaxRequest=new ActionAJAXEvent(componentID, targetID, null, 0);	//create a new action request with no action ID and the default option
					guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request
					event.stopPropagation();	//tell the event to stop bubbling
					event.preventDefault();	//prevent the default functionality from occurring
				}
			}
		}
	}
}

/**Called when an element marked as "action" is clicked with the right mouse button.
This method searches up the hierarchy to find the enclosing "component" element and sends an AJAX action event.
@param event The object describing the event.
*/
function onContextMenu(event)
{
	var target=event.currentTarget;	//get the element on which the event was registered
	var targetID=target.id;	//get the target ID
	if(targetID)	//if the element has an ID (otherwise, we couldn't report the action)
	{
		var component=DOMUtilities.getAncestorElementByClassName(target, STYLES.COMPONENT);	//get the component element TODO improve all this
		if(component)	//if there is a component
		{
			var componentID=component.id;	//get the component ID
			if(componentID)	//if there is a component ID
			{
				if(AJAX_ENABLED)	//if AJAX is enabled
				{
					var ajaxRequest=new ActionAJAXEvent(componentID, targetID, null, 1);	//create a new action request with no action ID and the context option
					guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request
					event.stopPropagation();	//tell the event to stop bubbling
					event.preventDefault();	//prevent the default functionality from occurring
				}
			}
		}
	}
}

/**Called when a checkbox is activated.
@param event The object describing the event.
*/
function onCheckInputChange(event)
{
	var checkInput=event.currentTarget;	//get the control that was listening for events (the target could be the check input's label, as occurs in Mozilla)
	if(AJAX_ENABLED)	//if AJAX is enabled
	{
		guise.oldElementIDCursors[checkInput.id]=checkInput.style.cursor;	//save the old cursor
		checkInput.style.cursor="wait";	//TODO testing
		var ajaxRequest=new FormAJAXEvent(new Map(checkInput.name, checkInput.checked ? checkInput.value : ""));	//create a new form request with the control name and value
		guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request
		event.stopPropagation();	//tell the event to stop bubbling
	}
	else	//if AJAX is not enabled
	{
		if(DOMUtilities.getAncestorElementByClassName(checkInput, STYLES.MENU_BODY))	//if this check is inside a menu, submit the form so that menus will cause immediate reaction
		{
			var form=getForm(element);	//get the form
			if(form)	//if there is a form
			{
				form.submit();	//submit the form		
			}
		}
	}
}

/**Called when a select control changes.
@param event The object describing the event.
*/
function onSelectChange(event)
{
	if(AJAX_ENABLED)	//if AJAX is enabled
	{
		var select=event.currentTarget;	//get the control to which the listener was listening
		var selectName=select.name;	//get the name of the control
	//TODO del alert("a select changed! "+select.id);
		var options=select.options;	//get the select options
		var ajaxRequest=new FormAJAXEvent();	//create a new form request
		for(var i=0; i<options.length; ++i)	//for each option
		{
			var option=options[i];	//get this option
			if(option.selected)	//if this option is selected
			{
				ajaxRequest.parameters[selectName]=option.value;	//add the control name and value as a parameter
			}
		}
		guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request
		event.stopPropagation();	//tell the event to stop bubbling
	}
}

/**A class encapsulating menu state.*/
function MenuState()
{

	/**The menu currently open, or null if no menu is currently open.*/
	this._openedMenu=null;	//TODO this currently works with two-deep nested menus; verify that it works on multiple levels; if not, we may need to use an array---or maybe using stopPropagation on the event will obviate the problem

	this._closingMenus=new Array();	//create an array to keep track of closing menus

	this._closeTimeout=null;	//show that we have no timer in use

	if(!MenuState.prototype._initialized)
	{
		MenuState.prototype._initialized=true;

		/**Opens a menu.
		@param menu The menu to open.
		*/
		MenuState.prototype.openMenu=function(menu)
		{
			for(var i=this._closingMenus.length-1; i>=0; --i)	//look at all the menus set for closing, in reverse order
			{
				if(this._closingMenus[i]==menu)	//if this menu was scheduled for closing
				{
					this._closingMenus.remove(i);	//remove this menu from the closing list
				}
			}
			if(this._openedMenu!=menu)	//if this menu wasn't already open
			{
				this._closeMenus();	//close all menus, if any, that were queued to be closed
				menu.style.visibility="visible";	//show the menu
				this._openedMenu=menu;	//show that this menu is open
			}
		};

		/**Closes a menu.
		@param menu The menu to close.
		*/
		MenuState.prototype.closeMenu=function(menu)
		{
			this._closingMenus.enqueue(menu);	//add this menu to the list of menus needing closing
			var timeout=this._closeTimeout;	//get the current close timer
			if(timeout)	//if there is a close timer
			{
				clearTimeout(timeout);	//clear the timeout
			}
			var localThis=this;	//get a reference to this object to allow calling this via closure
			this._closeTimeout=window.setTimeout(function(){localThis._closeMenus()}, 500);	//close the menu a split second later
		};

		/**Closes all menus immediately.*/
		MenuState.prototype._closeMenus=function()
		{
			var timeout=this._closeTimeout;	//get the current close timer
			if(timeout)	//if there is a close timer
			{
				this._closeTimeout=null;	//remove the timeout
				clearTimeout(timeout);	//clear the timeout, but leave it 
			}
			while(this._closingMenus.length>0)	//while there are menus to close
			{
				var menu=this._closingMenus.dequeue();	//get the next menu to close
				menu.style.visibility="hidden";	//hide the menu
				if(this._openedMenu==menu)	//if this was the last-opened menu
				{
					this._openedMenu=null;	//show that the menu is no longer open
				}
			}
		};
	}
}

var menuState=new MenuState();	//create a new menu state object

/**Called when the mouse is over a menu.
@param event The object describing the event.
*/
function onMenuMouseOver(event)
{
	var menu=DOMUtilities.getDescendantElementByClassName(event.currentTarget, STYLES.MENU_CHILDREN);	//get the menu below us TODO use a constant
	if(menu)	//if there is a menu below us
	{
		menuState.openMenu(menu);	//open this menu
		//TODO stop bubbling, and see if this changes the currently-opened-menu code
	}
}

/**Called when the mouse is over a menu.
@param event The object describing the event.
*/
function onMenuMouseOut(event)
{
	var menu=DOMUtilities.getDescendantElementByClassName(event.currentTarget, STYLES.MENU_CHILDREN);	//get the menu below us
	if(menu)	//if there is a menu below us
	{
		menuState.closeMenu(menu);	//close this menu
		//TODO stop bubbling, and see if this changes the currently-opened-menu code
	}
}

/**Called when dragging begins on a drag handle.
@param event The object describing the event.
*/
function onDragBegin(event)	//TODO rename to onDragClick
{
try
{
	if(!dragState)	//if there's a drag state, stay with that one (e.g. the mouse button might have been released outside the document on Mozilla)
	{
		var dragHandle=event.target;	//get the target of the event
			//TODO make sure this isn't the context mouse button
//TODO del alert("checking to start drag");
		var dragSource=DOMUtilities.getAncestorElementByClassName(dragHandle, STYLES.DRAG_SOURCE);	//determine which element to drag
		if(dragSource)	//if there is a drag source
		{
//TODO del alert("found drag source: "+dragSource.nodeName);
			dragState=new DragState(dragSource, event.clientX, event.clientY);	//create a new drag state
			dragState.beginDrag(event.clientX, event.clientY);	//begin dragging
//TODO del alert("drag state element: "+dragState.element.nodeName);
			event.stopPropagation();	//tell the event to stop bubbling
			event.preventDefault();	//prevent the default functionality from occurring
		}
	}
}
catch(e)
{
	alert("drag error: "+e);
}
}

/**Called when dragging occurs.
@param event The object describing the event.
*/
function onDrag(event)
{
	if(dragState)	//if we are in the middle of a drag
	{
		dragState.drag(event.clientX, event.clientY);	//drag the object to the new mouse position
		event.stopPropagation();	//tell the event to stop bubbling
		event.preventDefault();	//prevent the default functionality from occurring
	}
	else
	{
		alert("Unexpectedly dragging without drag state.");	//TODO change to an assertion
	}
}

/**Called when dragging ends.
@param event The object describing the event.
*/
function onDragEnd(event)
{
	if(dragState)	//if we are in the middle of a drag
	{
		dragState.endDrag();	//end dragging
		var dropTarget=guise.getDropTarget(event.clientX, event.clientY);	//get the drop target under the mouse
		if(dropTarget)	//if the mouse was dropped over a drop target
		{
//TODO del when works alert("over drop target: "+dropTarget.nodeName);
			var dragSourceComponent=DOMUtilities.getAncestorElementByClassName(dragState.dragSource, STYLES.COMPONENT);	//get the component element TODO improve all this; decide if we want the dropTarget style on the component element or the drop target subcomponent, and how we want to relate that to the component ID
			var dropTargetComponent=DOMUtilities.getAncestorElementByClassName(dropTarget, STYLES.COMPONENT);	//get the component element TODO improve all this; decide if we want the dropTarget style on the component element or the drop target subcomponent, and how we want to relate that to the component ID
			if(dragSourceComponent && dropTargetComponent)	//if there source and target components
			{
				var ajaxRequest=new DropAJAXEvent(dragState, dragSourceComponent, dropTargetComponent, event);	//create a new AJAX drop event TODO probably remove the dragState parameter
				guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request
			}
		}
		dragState=null;	//release our drag state
		event.stopPropagation();	//tell the event to stop bubbling
		event.preventDefault();	//prevent the default functionality from occurring
	}
}

/**Called when dragging begins on a slider thumb.
@param event The object describing the event.
*/
function onSliderThumbDragBegin(event)
{
	//TODO perhaps just end the dragging if there's already a drag state
	if(!dragState)	//if there's a drag state, stay with that one (e.g. the mouse button might have been released outside the document on Mozilla)
	{
				//TODO make sure this isn't the context mouse button
		var thumb=event.currentTarget;	//get the target of the event
//TODO del alert("thumb offsetWidth: "+thumb.offsetWidth+" offsetHeight: "+thumb.offsetHeight);
		var slider=DOMUtilities.getAncestorElementByClassName(thumb, STYLES.SLIDER_CONTROL);	//find the slider
		var track=DOMUtilities.getAncestorElementByClassName(thumb, STYLES.SLIDER_CONTROL_TRACK);	//find the slider track
		
		
//TODO find out why the slider track gets constantly reloaded in IE6
//TODO we need to make sure the slider is fully loaded (which may not be as relevant once IE6 no longer constantly reloads images)		
		
		var positionID=slider.id+".position";	//TODO use constant
		var positionInput=document.getElementById(positionID);	//get the position element		
		if(slider && track && positionInput)	//if we found the slider and the slider track
		{
			var isHorizontal=DOMUtilities.hasClassName(track, STYLES.AXIS_X);	//see if this is a horizontal slider
			dragState=new DragState(thumb, event.clientX, event.clientY);	//create a new drag state
			dragState.dragCopy=false;	//drag the actual element, not a copy
			if(isHorizontal)	//if this is a horizontal slider
			{
				dragState.allowY=false;	//only allow horizontal dragging
				var min=0;	//calculate the minimum
				var max=track.offsetWidth-thumb[GUISE_STATE_WIDTH_ATTRIBUTE]+1;	//calculate the maximum
				dragState.minX=min;	//set the minimum
				dragState.maxX=max;	//set the maximum
			}
			else	//if this is a vertical slider
			{
				dragState.allowX=false;	//only allow vertical dragging
				var min=0;	//calculate the minimum
				var max=track.offsetHeight-thumb[GUISE_STATE_HEIGHT_ATTRIBUTE]+1;	//calculate the maximum
				dragState.minY=min;	//set the minimum
				dragState.maxY=max;	//set the maximum
			}
			var span=max-min;	//find the available range of the values
			dragState.onDragBegin=function(element)	//when dragging begins, send a slideBegin action event
					{
						updateSlider(slider);	//update the slider view
						var ajaxRequest=new ActionAJAXEvent(slider.id, thumb.id, "slideBegin", 0);	//create a new action request for sliding begin TODO use a constant TODO why are we sending an event back here?
						guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request
					}
			dragState.onDrag=function(element, x, y)	//when dragging occurs, update the slider value
					{
						var coordinate=isHorizontal ? x.toString() : y.toString();	//get the new slider position
						var position=(coordinate-min)/span;	//determine the position as a fraction of the total track available
						if(!isHorizontal)	//if this is a vertical slider
						{
							position=1.0-position;	//take into account that the vertical slider origin is the opposite of the graphics origin
						}
//TODO del alert("new position: "+position);
						positionInput.value=position.toString();	//put the position in the value
//TODO del						var test="coordinate: "+coordinate+" min: "+min+" coordinate-min: "+(coordinate-min)+" position: "+position;
/*TODO fix; this has been mostly fixed by preventing IE6 from reloading the images, but it has been seen once in Firefox after that
if(isNaN(position))	//TODO del; fixed; change to assertion
{
	alert("track.offsetWidth: "+track.offsetWidth+" thumb[GUISE_STATE_WIDTH_ATTRIBUTE]: "+thumb[GUISE_STATE_WIDTH_ATTRIBUTE]+" max: "+max+" coordinate: "+coordinate+" min: "+min+" coordinate-min: "+(coordinate-min)+" span: "+span+" position: "+position);
}
*/
						var ajaxRequest=new FormAJAXEvent(new Map(positionInput.name, position.toString()));	//create a new form request with the control name and value
						guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request
					};
			dragState.onDragEnd=function(element)	//when dragging ends, update the slider view to make sure it is synchronized with the updated value
					{
						var ajaxRequest=new ActionAJAXEvent(slider.id, thumb.id, "slideEnd", 0);	//create a new action request for sliding end TODO use a constant	//why are we sending back an action event here?
						guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request
						updateSlider(slider);	//update the slider view
					}
			dragState.beginDrag(event.clientX, event.clientY);	//begin dragging
	//TODO del alert("drag state element: "+dragState.element.nodeName);
			event.stopPropagation();	//tell the event to stop bubbling
			event.preventDefault();	//prevent the default functionality from occurring
		}
	}
}

/**Updates the representation of a slider based upon the slider's model value.
The slider is only updated if the slider is not in the sliding state (i.e. the user is not manually moving the slider).
This implementation also sets the thumb[GUISE_STATE_WIDTH_ATTRIBUTE] and thumb[GUISE_STATE_HEIGHT_ATTRIBUTE] to work around a Mozilla bug that doesn't properly calculate thumb.offsetWidth and thumb.offsetHeight if the thumb is partially outside the track.
@param slider The slider element.
*/
function updateSlider(slider)	//TODO maybe rename to updateSliderView
{
	if(DOMUtilities.hasClassName(slider, "sliding"))	//if the slider is not in a sliding state according to the server (i.e. the thumb is not being manually moved by the user) TODO use a constant
	{
		return;	//don't update the slider while the server still thinks the slider is sliding
	}
	var track=DOMUtilities.getDescendantElementByClassName(slider, STYLES.SLIDER_CONTROL_TRACK);	//find the slider track
	var thumb=DOMUtilities.getDescendantElementByClassName(slider, STYLES.SLIDER_CONTROL_THUMB);	//find the slider thumb
	if(dragState && dragState.dragging && dragState.dragSource==thumb)	//if the slider thumb is being dragged (i.e. the browser things the slider is being dragged)
	{
		return;	//don't update the slider while a drag is occurring
	}
/*TODO del when works

	else	//TODO del; debugging
	{
		if(!dragState)
		{
			alert("Staying in because no drag state");
		}
		else if(!dragState.dragging)
		{
			alert("Staying in because we aren't dragging");
		}
		else if(dragState.dragSource!=thumb)
		{
			alert("drag source is not thumb; thumb is "+thumb.id+" and drag source is "+dragState.dragSource.id);
		}
	}
*/
	var positionID=slider.id+".position";	//TODO use constant
	var positionInput=document.getElementById(positionID);	//get the position element
	if(track && thumb && positionInput)	//if we found the slider track and thumb
	{
		if(typeof thumb[GUISE_STATE_WIDTH_ATTRIBUTE]=="undefined" || thumb[GUISE_STATE_WIDTH_ATTRIBUTE]==0)	//if we haven't defined the thumb width, or we defined it when the control wasn't visible and therefore the thumb width was zero
		{
			thumb[GUISE_STATE_WIDTH_ATTRIBUTE]=thumb.offsetWidth;	//set the thumb width so that it won't change later with the Mozilla bug if the thumb is partially outside the track
		}
		if(typeof thumb[GUISE_STATE_HEIGHT_ATTRIBUTE]=="undefined" || thumb[GUISE_STATE_HEIGHT_ATTRIBUTE]==0)	//if we haven't defined the thumb height, or we defined it when the control wasn't visible and therefore the thumb height was zero
		{
			thumb[GUISE_STATE_HEIGHT_ATTRIBUTE]=thumb.offsetHeight;	//set the thumb height so that it won't change later with the Mozilla bug if the thumb is partially outside the track
		}
		var position=positionInput.value ? parseFloat(positionInput.value) : 0;	//get the position TODO make sure this logic is in synch with whether server code will always provide a value, even for null
		var isHorizontal=DOMUtilities.hasClassName(track, STYLES.AXIS_X);	//see if this is a horizontal slider
		if(isHorizontal)	//if this is a horizontal slider
		{
			var min=0;	//calculate the minimum
//TODO del alert("track width: "+track.offsetWidth);
//TODO del alert("thumb width: "+thumb.offsetWidth);
			var max=track.offsetWidth-thumb[GUISE_STATE_WIDTH_ATTRIBUTE]+1;	//calculate the maximum
		}
		else	//if this is a vertical slider
		{
			var min=0;	//calculate the minimum
			var max=track.offsetHeight-thumb[GUISE_STATE_HEIGHT_ATTRIBUTE]+1;	//calculate the maximum
			position=1.0-position;	//take into account that the vertical slider origin is the opposite of the graphics origin
		}
		var span=max-min;	//find the available range of the values
		var newCoordinate=Math.round(position*span+min);	//determine the new coordinate
//TODO del when works alert("new coordinate for "+slider.id+" is "+newCoordinate);
		if(isHorizontal)	//if this is a horizontal slider
		{	//TODO fix; the compiled version on IE once gave an "invalid argument" error here
		
/*TODO del; this was probably being caused from the IE6 image reloading problem combined with removing the thumb spans when synchronizing earlier
		
if(isNaN(newCoordinate))
{
	alert("track width: "+track.offsetWidth+"thumb width: "+thumb[GUISE_STATE_WIDTH_ATTRIBUTE]);
	alert("position: "+position+" span: "+span+" min: "+min);

}
*/		
			thumb.style.left=newCoordinate+"px";	//update the horizontal position of the slider
		}
		else	//if this is a vertical slider
		{
			thumb.style.top=newCoordinate+"px";	//update the vertical position of the slider
		}
//TODO del		alert("ready to update slider "+slider.id+" with value:"+positionInput.value);
	}
}

/**Called when the mouse enters or exits a mouse listener.
If the current target element has a "mouseListener" class, the event will be reported to the server.
@param event The object describing the event.
*/
function onMouse(event)
{
	var relatedTarget=event.relatedTarget;	//get the related target
	if(relatedTarget)	//if there is a related target, see if we should ignore the event
	{
		var ignoreEvent=event.currentTarget==relatedTarget;	//ignore the event if is generated by leaving the same element, as Mozilla does (TODO probably see if relatedTarget is a child of current target)
		try
		{
			var dummy=relatedTarget.nodeName;	//try to get the event.relatedTarget.nodeName, which on Mozilla will fail on the events we should ignore
		}
		catch(e)	//if we can't access the related target node name, this is one of the Mozilla bug events
		{
			ignoreEvent=true;	//ignore the event
		}
		if(ignoreEvent)	//if we should ignore the event
		{
			return;	//ignore extraneous mouse events
		}
	}
	var target=event.currentTarget;	//get the target of the event
		//if the mouse is supposedly leaving the element, make sure it's not just moving to a child element, and vice versa (see http://www.quirksmode.org/js/events_mouse.html#mouseover)
	var otherTarget=event.relatedTarget;	//see which element the mouse is going to or from
	while(otherTarget && otherTarget!=document.documentElement)	//while we haven't reached the top of the DOM TODO find out why otherTarget can be null in IE
	{
		if(otherTarget==target)	//if the mouse is still over the original element
		{
			return;	//ignore the mouse exit event while it is still over this element
		}
		otherTarget=otherTarget.parentNode;	//check up the hierarchy TODO fix; IE gave an error here once, so maybe somehow otherTarget was null
	}
	if(target.nodeName.toLowerCase()=="img")	//if this is an image, perform rollovers if needed
	{
		var rolloverSrc=target.getAttribute("guise:rolloverSrc");	//get the image rollover, if there is one
		if(rolloverSrc!=null)	//if the image has a rollover TODO use a constant
		{
			switch(event.type)	//see which type of mouse event this is
			{
				case "mouseover":	//if we are rolling over the element TODO use a constant
					target.src=rolloverSrc;	//switch to the rollover image
					break;
				case "mouseout":	//if we are rolling off the image TODO use a constant
					target.src=target.getAttribute("guise:originalSrc");	//switch back to the original source TODO use a constant
					break;
			}
		}
	}
	var component=DOMUtilities.getAncestorElementByClassName(target, STYLES.COMPONENT);	//get the component element
	if(component)	//if we know the component
	{
		var eventType;	//we'll determine the type of AJAX mouse event to send
		switch(event.type)	//see which type of mouse event this is
		{
			case "mouseover":	//TODO use a constant
				eventType=MouseAJAXEvent.EventType.ENTER;
				addComponentClassName(component, STYLES.ROLLOVER, component.id);	//add the "rollover" style to all component elements
				break;
			case "mouseout":	//TODO use a constant
				eventType=MouseAJAXEvent.EventType.EXIT;
				removeComponentClassName(component, STYLES.ROLLOVER, component.id);	//remove the "rollover" style from all component elements
				break;
			default:	//TODO assert an error or warning
				return;				
		}
		if(DOMUtilities.hasClassName(target, STYLES.MOUSE_LISTENER))	//if this is a mouse listener, report the event
		{
			var ajaxRequest=new MouseAJAXEvent(eventType, component, target, event);	//create a new AJAX mouse event
			guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request
			event.stopPropagation();	//tell the event to stop bubbling
			event.preventDefault();	//prevent the default functionality from occurring
		}
	}	
}

/**Retrieves the ancestor form of the given node, starting at the node itself.
@param node The node the form of which to find, or null if the search should not take place.
@return The form in which the node lies, or null if the node is not within a form.
*/
function getForm(node)	//TODO improve; currently this two-direction search is needed because some components, such as frames, can live outside forms; change this so that frames get added inside the form
{
	var form=DOMUtilities.getAncestorElementByName(node, "form");	//get the form ancestor
	if(form==null)	//if there is no form ancestor (e.g. the node is a frame outside the form)
	{
		form=DOMUtilities.getDescendantElementByName(document.documentElement, "form");	//search the whole document for the form
	}
	return form;	//return the form we found, if any
}

/**Adds the given class name to all a component's elements.
The class name is added to the element and all child elements that have the given ID or an ID that begins with the ID and a hyphen.
@param element The element that should be given a class.
@param className The name of the class to add.
@param componentID The ID of the component that owns relevant elements.
*/
function addComponentClassName(element, className, componentID)
{
	var id=element.id;	//get the element ID
	if(id==componentID || (id && id.startsWith(componentID+"-")))	//if the element ID is the component ID or starts with the component ID TODO use a constant
	{
		DOMUtilities.addClassName(element, className);	//add the class to the element
	}
	var childNodeList=element.childNodes;	//get all the child nodes
	var childNodeCount=childNodeList.length;	//find out how many children there are
	for(var i=0; i<childNodeCount; ++i)	//for each child node
	{
		var childNode=childNodeList[i];	//get this child node
		if(childNode.nodeType==Node.ELEMENT_NODE)	//if this is an element node
		{
			addComponentClassName(childNode, className, componentID);	//add this class to the child component
		}
	}
}

/**Removes the given class name from all a component's elements.
The class name is removed from the element and all child elements that have the given ID or an ID that begins with the ID and a hyphen.
@param element The element that should have a class removed.
@param className The name of the class to remove.
@param componentID The ID of the component that owns relevant elements.
*/
function removeComponentClassName(element, className, componentID)
{
	var id=element.id;	//get the element ID
	if(id==componentID || (id && id.startsWith(componentID+"-")))	//if the element ID is the component ID or starts with the component ID TODO use a constant
	{
		DOMUtilities.removeClassName(element, className);	//add the class to the element
	}
	var childNodeList=element.childNodes;	//get all the child nodes
	var childNodeCount=childNodeList.length;	//find out how many children there are
	for(var i=0; i<childNodeCount; ++i)	//for each child node
	{
		var childNode=childNodeList[i];	//get this child node
		if(childNode.nodeType==Node.ELEMENT_NODE)	//if this is an element node
		{
			removeComponentClassName(childNode, className, componentID);	//add this class to the child component
		}
	}
}

/**The elements that can receive focus.*/
var FOCUSABLE_ELEMENT_NAMES=new Array("button", "input", "select", "textarea");

/**Retrieves the first descendant node, including the node, that can be focused.
This method recognizes the following focusable elements:
<ul>
	<li>button</li>
	<li>input</li>
	<li>select</li>
	<li>textarea</li>
</ul>
@param node The root of the node tree to check.
@return The first descendant node that can be focused, or null if there is no focusable descendant.
*/
function getFocusableDescendant(node)
{
//TODO del	alert("node has focus type of: "+(typeof node.focus));
//TODO del	if(node.focus)	//if we can focus this node
	if(node.currentStyle)	//if this node has current style information (IE only) TODO add code for Mozilla
	{
//TODO del alert("node "+node.id+" has current style visibility "+node.currentStyle.visibility+" display "+node.currentStyle.display);
		if(node.currentStyle.visibility=="hidden" || node.currentStyle.display=="none")	//if the stylesheet set this node to hidden or not displayed; at least this keeps elements from being found that are not focusable by IE (Mozilla allows focusing of hidden and non-displayed elements)
		{
			return null;	//neither this node nor any of its descendants should be focused
		}
	}
	var nodeName=node.nodeName.toLowerCase();
	if(FOCUSABLE_ELEMENT_NAMES.contains(nodeName))	//if this is a focusable node
	{
			//TODO add a check for computed styles
		if((!node.style || (node.style.visibility!="hidden" && node.style.display!="none")) && !node.disabled)	//make sure the node is not hidden or disabled
		{
			if(nodeName!="input" || node.type!="hidden")	//make this isn't a hidden input
			{
				return node;	//indicate that this node can be focused
			}
		}
	}
	var childNodeList=node.childNodes;	//get all the child nodes
	var childNodeCount=childNodeList.length;	//find out how many children there are
	for(var i=0; i<childNodeCount; ++i)	//for each child node
	{
		var childNode=childNodeList[i];	//get this child node
		var focusable=getFocusableDescendant(childNode);	//see if this subtree has a focusable node
		if(focusable)	//if we found a focusable node
		{
			return focusable;	//return it
		}
	}
	return null;	//indicate that no focusable node could be found
}

/**Retrieves the absolute bounds of the given element.
@param The element the bounds of which to find.
@return A Rectangle containing the coordinates and size of the element.
*/
function getElementBounds(element)
{
	return new Rectangle(getElementCoordinates(element), new Size(element.offsetWidth, element.offsetHeight));	//create a rectangle containing the coordinates and size of the element
}

/**Retrieves the absolute X and Y coordinates of the given element.
@param The element the coordinates of which to find.
@return A Point containing the coordinates of the element.
@see http://www.oreillynet.com/pub/a/javascript/excerpt/JSDHTMLCkbk_chap13/index6.html
@see http://www.quirksmode.org/js/findpos.html
*/
function getElementCoordinates(element)	//TODO make sure this method correctly calculates margins and padding, as Mozilla and IE both show slight variations for text, but not for images
{
var originalElement=element;	//TODO del; testing
	var x=0, y=0;
	if(element.offsetParent)	//if element.offsetParent is supported
	{
//TODO del alert("using calculated element position");
		while(element)	//while we have an element
		{
			x+=element.offsetLeft;	//add this element's offsets
			y+=element.offsetTop;
/*TODO fix or del; apparently this code is not as good as the version below, although it doesn't seem to compensate for scrollLeft
			if(element.scrollLeft)
			{
				x-=element.scrollLeft;
			}
			if(element.scrollTop)
			{
//TODO del alert("element "+element.nodeName+" scroll top "+element.scrollTop);
				y-=element.scrollTop;
			}
*/
			element=element.offsetParent;	//go to the element's parent offset
		}
/*TODO fix for Mac
    if (navigator.userAgent.indexOf("Mac") != -1 && 
        typeof document.body.leftMargin != "undefined") {
        offsetLeft += document.body.leftMargin;
        offsetTop += document.body.topMargin;
    }
*/
/*TODO fix; this inappropriately adds in the viewport scroll position on IE but not on Mozilla---but this is needed for internal scrolled divs on IE; try to distinguish the two when internal scrolled divs are used
		var parent=originalElement.parentNode;
		while(parent!=document.documentElement)
		{
			if(parent.scrollTop)
			{
//TODO fix alert("element "+parent.nodeName+" scroll top "+parent.scrollTop);
				y-=parent.scrollTop;
			}
			parent=parent.parentNode;		
		}
*/
	}
	else if(element.x && element.y)	//if element.offsetParent is not supported by but element.x and element.y are supported (e.g. Navigator 4)
	{
		x=element.x;	//get the element's coordinates directly
		y=element.y;
	}
	return new Point(x, y);	//return the point we calculated
}

/**Retrieves the X and Y coordinates of the given element relative to the viewport.
@param The element the coordinates of which to find.
@return A Point containing the coordinates of the element relative to the viewport.
*/
function getElementFixedCoordinates(element)
{
	var absoluteCoordinates=getElementCoordinates(element);	//get the element's absolute coordinates
	var scrollCoordinates=GUIUtilities.getScrollCoordinates();	//get the viewport's scroll coordinates
	return new Point(absoluteCoordinates.x-scrollCoordinates.x, absoluteCoordinates.y-scrollCoordinates.y);	//compensate for viewport scrolling
}

/**@return The size of the document, even if it is outside the viewport.
@see http://www.quirksmode.org/viewport/compatibility.html
*/
function getPageSize()
{
	var width=Math.max(document.documentElement.scrollWidth, document.body.scrollWidth);
	var height=Math.max(document.documentElement.scrollHeight, document.body.scrollHeight);
//	alert("width: "+width+" height: "+height);
/*TODO this is presented at http://www.quirksmode.org/viewport/compatibility.html, but doesn't correctly give the scroll width for Firefox
	var width=0, height=0;	//we'll determine the width and height
	var scrollHeight=document.body.scrollHeight;	//get the scroll height
	var offsetHeight=document.body.offsetHeight;	//get the offset height
	if(scrollHeight>offsetHeight)	//if the scroll height is larger
	{
		width=document.body.scrollWidth;	//use the scroll dimensions
		height=document.body.scrollHeight;
	}
	else	//if the body offsets are larger (e.g. Explorer Mac and IE 6 strict)
	{
		width=document.body.offsetWidth;	//use the body offsets
		height=document.body.offsetHeight;
	}
	return new Size(width, height);	//return the page size
	
alert("document.body.scrollWidth: "+document.body.scrollWidth+"\n"+
			"document.body.offsetWidth: "+document.body.offsetWidth+"\n"+
			"document.body.clientWidth: "+document.body.clientWidth+"\n"+
			"document.documentElement.scrollWidth: "+document.documentElement.scrollWidth+"\n"+
			"document.documentElement.offsetWidth: "+document.documentElement.offsetWidth+"\n"+
			"document.documentElement.clientWidth: "+document.documentElement.clientWidth+"\n")
*/	
	return new Size(width, height);	//return the page size
}

/**An abstract effect base class.
Child classes should implement _doEffect() to perform the effect.
@param element The element on which the effect will be performed.
@param delay The delay before the effect should begin.
var effectBegin The function indicating what should occur at the beginning of the effect, after the delay, or null if there is no effect begin function.
var effectEnd The function indicating what should occur at the end of the effect, or null if there is no effect end function.
*/
function AbstractEffect(element, delay)
{
	this._element=element;	//save the element
	this.effectBegin=null;
	this.effectEnd=null;
	this._delay=delay;
//TODO del when works	this._delay=delay || 0;	//get the delay, compensating for no delay specified

	/**The ID of the timeout currently in progress, or null if there is no timeout in progress.*/
	this._timeoutID=null;

	if(!AbstractEffect.prototype._initialized)
	{
		AbstractEffect.prototype._initialized=true;

		/**Starts the effect process.*/
		AbstractEffect.prototype.start=function()
		{
			var effect=this;	//save this effect to use in closure
//TODO del alert("ready to start abstract effect with effect: "+(typeof effect));
			var delayFunction=function()	//create a function to call this._beginEffect() after the delay
					{
						effect._timeoutID=null;	//show that there is no timeout in effect
						effect._beginEffect();	//begin the effect
					};
			this._timeoutID=setTimeout(delayFunction, this._delay);	//call the delay function after the delay
		};

		/**Starts the actual effect.*/
		AbstractEffect.prototype._beginEffect=function()
		{
			if(this.effectBegin instanceof Function)	//if there is an effect begin function
			{
				this.effectBegin();	//begin the effect
			}
			if(this._doEffect())	//if there is a _doEffect() method
			{
				this._doEffect();	//perform the effect
			}
		};

		/**Ends the actual effect.*/
		AbstractEffect.prototype._endEffect=function()
		{
			if(this.effectEnd instanceof Function)	//if there is an effect end function
			{
				this.effectEnd();	//end the effect
			}
		};

	}
}

/**A delay effect.
@param element The element on which the delay will be performed.
@param delay The delay before the effect should begin.
var effectBegin The function indicating what should occur at the beginning of the effect, after the delay, or null if there is no effect begin function.
var effectEnd The function indicating what should occur at the end of the effect, or null if there is no effect end function.
*/
function DelayEffect(element, delay)	//extends AbstractEffect
{
	AbstractEffect.call(this, element, delay);	//call the parent class
	this._opacity=0;	//we'll start at zero opacity

	if(!DelayEffect.prototype._initialized)
	{
		DelayEffect.prototype._initialized=true;
		DelayEffect.prototype.start=AbstractEffect.prototype.start;
		DelayEffect.prototype._beginEffect=AbstractEffect.prototype._beginEffect;
		DelayEffect.prototype._updateEffect=AbstractEffect.prototype._updateEffect;
		DelayEffect.prototype._endEffect=AbstractEffect.prototype._endEffect;
	}
}

/**An effect for fading an element using opacity.
@param element The element on which the delay will be performed.
@param delay The delay before the effect should begin.
var effectBegin The function indicating what should occur at the beginning of the effect, after the delay, or null if there is no effect begin function.
var effectEnd The function indicating what should occur at the end of the effect, or null if there is no effect end function.
*/
function OpacityFadeEffect(element, delay)	//extends AbstractEffect
{
	AbstractEffect.call(this, element, delay);	//call the parent class
	this._opacity=0;	//we'll start at zero opacity

	if(!OpacityFadeEffect.prototype._initialized)
	{
		OpacityFadeEffect.prototype._initialized=true;

		OpacityFadeEffect.prototype.start=AbstractEffect.prototype.start;

		/**Starts the actual effect.*/
		OpacityFadeEffect.prototype._beginEffect=function()
		{
//TODO del alert("ready to begin opacity effect");
			this._updateEffect();	//update the opacity
			AbstractEffect.prototype._beginEffect.call(this);	//call the super version
//TODO del alert("finished beginning opacity effect");
		};

		/**Performs the main effect procedure.*/
		OpacityFadeEffect.prototype._doEffect=function()
		{
//			alert("ready to do effect");
			if(this._opacity<=100)	//if we haven't reached full opacity
			{
				this._updateEffect();	//update the opacity
				var effect=this;	//save this effect to use in closure
				var timeoutFunction=function()	//create a function to call this._doEffect() after each timeout
						{
							effect._timeoutID=null;	//show that there is no timeout in effect
							effect._opacity+=10;	//increase the opacity
							effect._doEffect();	//perform the effect
						};
				this._timeoutID=setTimeout(timeoutFunction, 50);	//call the timeout function after the given interval TODO allow this to be configured
			}
			else	//if we've reached full opacity
			{
//TODO del alert("ready to end effect");
				this._endEffect();	//finish the effect
			}
		};

		/**Updates the effect.*/
		OpacityFadeEffect.prototype._updateEffect=function()
		{
			this._element.style.opacity=this._opacity/100;	//update the opacity
			this._element.style.filter="alpha(opacity="+this._opacity+")";	//update the opacity for IE			
		};		

		/**Ends the actual effect.*/
		OpacityFadeEffect.prototype._endEffect=function()
		{
//TODO del alert("inside the correct endEffect");
			this._element.style.filter="";	//remove the IE-specific filter, because it will cause some elements (such as flyover tethers) not to appear, even if opacity is set to 100
			AbstractEffect.prototype._endEffect.call(this);	//call the super version
		};

	}
}

//TODO testing OpacityFadeEffect.prototype=new AbstractEffect();	//OpacityFadeEffect extends AbstractEffect


function debug(text)
{
	var dymamicContent="javascript:document.write('<html><body>"+DOMUtilities.encodeXML(text)+"</body></html>');"	//create JavaScript for writing the dynamic content
	window.open(dymamicContent, "debug", "status=no,menubar=no,scrollbars=yes,resizable=no,width=800,height=600");
}

eventManager.addEvent(window, "load", onWindowLoad, false);	//do the appropriate initialization when the window loads
