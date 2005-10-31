/**Guise(TM) JavaScript support routines
Copyright (c) 2005 GlobalMentor, Inc.
var AJAX_URI: The URI to use for AJAX communication, or null/undefined if AJAX communication should not occur.
*/

/**Guise AJAX Request Format, content type application/x-guise-ajax-request+xml
<request>
	<init/>	<!--initializes the page, requesting all frames to be resent-->
	<events>	<!--the list of events (zero or more)-->
		<form exhaustive="true|false">	<!--information resulting from form changes, analogous to that in an HTTP POST; exhaustive indicates whether the event contains values for all form controls (defaults to false)-->
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
	</events>
</request>
*/

/**Guise AJAX Response Format, content type application/x-guise-ajax-response+xml
<response>
	<patch></patch>	<!--XML elements to be patched into the existing DOM tree.-->
	<remove id=""/>	<!--ID of the XML element to be removed from the existing DOM tree.-->
	<navigate>uri</navigate>	<!--URI of another page to which to navigate.-->
	<frame></frame>	<!--definition of a frame to show-->
</response>
*/

//TODO before sending a drop event, send a component update for the drop target so that its value will be updated; or otherwise make sure the value is synchronized

/**The URI of the XHTML namespace.*/
var XHTML_NAMESPACE_URI="http://www.w3.org/1999/xhtml";

/**The class prefix of a menu.*/
//TODO del when works var MENU_CLASS_PREFIX="menu-";

/**The class prefix of a tree node.*/
var TREE_NODE_CLASS_PREFIX="treeNode-";
/**The class suffix of a collapsed tree node.*/
var TREE_NODE_COLLAPSED_CLASS_SUFFIX="-collapsed";
/**The class suffix of an expanded tree node.*/
var TREE_NODE_EXPANDED_CLASS_SUFFIX="-expanded";
/**The class suffix of a leaf tree node.*/
//TODO del var TREE_NODE_LEAF_CLASS_SUFFIX="-leaf";
/**The class suffix for a tab.*/
var TAB_CLASS_SUFFIX="-tab";
/**The class suffix for a selected tab.*/
var TAB_SELECTED_CLASS_SUFFIX="-tab-selected";

/**The class suffix of a decorator.*/
//TODO del var DECORATOR_CLASS_PREFIX="-decorator";

/**The enumeration of recognized styles.*/
var STYLES=
{
	/**A component element that can be clicked as an action.*/
	ACTION: "action",
	/**A general component.*/
	COMPONENT_REGEXP: /^component(-[xy]-(ltr|rtl))?$/,
	DRAG_SOURCE: "dragSource",
	DRAG_HANDLE: "dragHandle",
	DROP_TARGET: "dropTarget",
	SLIDER_CONTROL_X_LTR_THUMB: "sliderControl-x-ltr-thumb",
	SLIDER_CONTROL_X_RTL_THUMB: "sliderControl-x-rtl-thumb",
	SLIDER_CONTROL_Y_LTR_THUMB: "sliderControl-y-ltr-thumb",
	SLIDER_CONTROL_Y_RTL_THUMB: "sliderControl-y-rtl-thumb",
	MENU_REGEXP: /^dropMenu-[xy]-(ltr|rtl)$/,
	MENU_BODY_REGEXP: /^dropMenu-[xy]-(ltr|rtl)-body$/,
	MENU_CHILDREN_REGEXP: /^dropMenu-[xy]-(ltr|rtl)-children$/
};

/**The array of drop targets, determined when the document is loaded. The drop targets are stored in increasing order of hierarchical depth.*/
var dropTargets=new Array();

/**The array of frame elements.*/
var frames=new Array();

/**Adds a frame to the array.
This version adds the frame to the document, initializes the frame, and updates the modal state.
@param frame The frame to add.
*/
frames.add=function(frame)
{
//TODO fix so that it works both on IE and Firefox							oldElement.style.position="fixed";	//TODO testing
	frame.style.position="absolute";	//change the element's position to absolute; it should already be set like this, but set it specifically so that dragging will know not to drag a copy TODO update the element's initial position
	document.body.appendChild(frame);	//add the frame element to the document; do this first, because IE doesn't allow the style to be accessed directly with imported nodes until they are added to the document
	var viewportSize=getViewportSize();	//get the size of the viewport
	frame.style.left=((viewportSize.width-frame.offsetWidth)/2)+"px";	//center the frame horizontally
	frame.style.top=((viewportSize.height-frame.offsetHeight)/2)+"px";	//center the frame vertically

	frame.style.zIndex=256;	//give the element an arbitrarily high z-index value so that it will appear in front of other components TODO fix
	initializeNode(frame);	//initialize the new imported frame, installing the correct event handlers
	updateComponents(frame);	//update all the components within the frame
	Array.prototype.add.call(this, frame);	//do the default adding to the array
	this.updateModal();	//update the modal state
	var focusable=getFocusableDescendant(frame);	//see if this frame has a node that can be focused
	if(focusable)	//if we found a focusable node
	{
		focusable.focus();	//focus on the node
	}
};

/**Removes a frame from the array.
This version removes the frame to the document, uninitializes the frame, and updates the modal state.
@param frame The frame to remove.
*/
frames.remove=function(frame)
{
	var index=this.indexOf(frame);	//get the frame index
	if(index>=0)	//if we know the index of the frame
	{
		Array.prototype.remove.call(this, index);	//do the default removal from the array
		uninitializeNode(frame);	//uninitialize the frame tree
		document.body.removeChild(frame);	//remove the frame element to the document
		this.updateModal();	//update the modal state
	}
};

/**The current modal frame, or null if there is no modal frame.*/
frames.modalFrame=null;

/**Updates the modal layer and current modal frame.*/
frames.updateModal=function()
{
	this.modalFrame=null;	//start out presuming there is no modal frame
	for(var i=this.length-1; i>=0 && this.modalFrame==null; --i)	//for each frame, find the last modal frame
	{
		var frame=this[i];	//get a reference to this frame
		if(hasClassName(frame, "frameModal"))	//if this is a modal frame
		{
			this.modalFrame=frame;	//indicate our modal frame TODO allow for multiple modal frames
		}
	}
	if(this.modalFrame!=null)	//if there is a modal frame
	{
		modalLayer.style.zIndex=this.modalFrame.style.zIndex-1;	//place the modal layer directly behind the modal frame
		modalLayer.style.display="block";	//make the modal layer visible
	}
	else
	{
		modalLayer.style.display="none";	//hide the modal layer
	}
}

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

var EMPTY_ARRAY=new Array();	//a shared empty array

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
    return document.createElement(qname);	//create the element, ignoring the namespace
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
    return document.createAttribute(qname);	//create the attribute, ignoring the namespace
	};
}

if(typeof document.importNode=="undefined")	//if the document does not support document.importNode(), create a substitute
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
				var attributes=node.attributes;	//get the element's attributes
				var attributeCount=attributes.length;	//find out how many attributes there are
				for(var i=0; i<attributeCount; ++i)	//for each attribute
				{
					var attribute=attributes[i];	//get this attribute
					var attributeName=attribute.nodeName;	//get the attribute name
					if(attributeName=="style")	//if this is the style attribute, it must be copied differently or it will throw an error on IE
					{
						DOMUtilities.copyStyleAttribute(importedNode, node);	//copy the style attribute
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
						importedNode.innerHTML=innerHTMLStringBuilder.toString();	//set the element's inner HTML to the string we constructed
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
*/
function StringBuilder()
{
	this._strings=new Array();	//create an array of strings
	if(!this._initialized)
	{
		this._initialized=true;

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
}

//Parameter

/**A class encapsulating a name and value.
@param name: The name of the parameter, stored under this.name;
@param value: The value of the parameter, stored under this.value;
*/
function Parameter(name, value) {this.name=name; this.value=value;}

//Point

/**A class encapsulating a point.
@param x: The X coordinate, stored under this.x;
@param y: The Y coordinate, stored under this.y;
*/
function Point(x, y) {this.x=x; this.y=y;}

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
var parameters An array of parameters (which may be empty) each of type Parameter.
*/
function URI(uriString)
{
	if(!this._initialized)
	{
		this._initialized=true;

		URI.prototype.URI_REG_EXP=/^(([^:\/?#]+):)?(\/\/([^\/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?/;	//the regular expression for parsing URIs, from http://www.ietf.org/rfc/rfc2396.txt

		/**Determines first occurrence of a given parameter.
		@param name The name of the parameter.
		@return The first matching parameter (of type Parameter), or null if there is no such parameter.
		*/
		URI.prototype.getParameter=function(name)
		{
			var parameterCount=this.parameters.length;	//find out how many parameters there are
			for(var i=0; i<parameterCount; ++i)	//for each parameter
			{
				var parameter=this.parameters[i];	//get a reference to this parameter
				if(parameter.name==name)	//if this parameter name matches
				{
					return parameter;	//return this parameter
				}
			}
			return null;	//indicate that no parameter matched
		};

		/**Determines the value of the first occurrence of a given parameter.
		@param name The name of the parameter.
		@return The value of the given parameter, or null if the parameter value is null or there is no such parameter.
		*/
		URI.prototype.getParameterValue=function(name)
		{
			var parameter=getParameter(name);	//get the requested parameter
			return parameter!=null ? parameter.value : null;	//return the parameter value, or null if there is no such parameter
		};
	}
	this.URI_REG_EXP.test(uriString);	//split out the components of the URI using a regular expression
	this.scheme=RegExp.$2;	//save the URI components
	this.authority=RegExp.$4;
	this.path=RegExp.$5;
	this.query=RegExp.$7;
	this.parameters=new Array();	//create a new array to hold parameters
	if(this.query)	//if a query is given
	{
		var queryComponents=this.query.split("&");	//split up the query components
		var parameterCount=queryComponents.length;	//find out how many parameters there are
		for(var i=0; i<parameterCount; ++i)	//for each parameter
		{
			var parameterComponents=queryComponents[i].split("=");	//split out the parameter components
			var parameterName=decodeURIComponent(parameterComponents[0]);	//get and decode the parameter name
			var parameterValue=parameterComponents.length>1 ? decodeURIComponent(parameterComponents[1]) : null;	//get and decode the parameter value
			this.parameters.add(new Parameter(parameterName, parameterValue));	//create and add a new parameter to the parameter array
		}
	}
	this.fragment=RegExp.$9;
}


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

	/**Removes all children from the given node.
	This implementation also unregistered any events for the node and all its children.
	@param node The node the children of which to remove.
	*/
	removeChildren:function(node)
	{
		while(node.childNodes.length>0)	//while there are child nodes left (remove the last node, one at a time, because because IE can sometimes add an element back in after the last one was removed)
		{
			var childNode=node.childNodes[node.childNodes.length-1];	//get a reference to the last node
			uninitializeNode(childNode);	//uninitialize the node tree
			node.removeChild(childNode);	//remove the last node
		}
	},

	/**Retrieves the HTML attribute name from the DOM attribute name.
	This method converts "class" to "className", for example.
	@param domAttributeName The literal DOM attribute name.
	@return The attribute name expected in HTML.
	*/
	getHTMLAttributeName:function(domAttributeName)
	{
		switch(domAttributeName)	//see which DOM attribute name was given
		{
			case "class":
				return "className";
			case "readonly":
				return "readOnly";
			default:	//for all other class names, return the DOM attribute
				return domAttributeName;
		}
	},

	/**Sets the attribute value of an element, using namespaces if the DOM supports them.
	@param element The element for which an attribute should be set.
	@param namespaceURI The URI of the namespace.
	@param qname The qualified name of the attribute.
	@param value The value of the attribute.
	*/
	setAttributeNS:function(element, namespaceURI, qname, value)
	{
		var attribute=document.createAttributeNS(namesapceURI, qname);	//create the attribute
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

	/**Copies the style attribute by parsing out the individual style declarations and applying them to the destination style.
	This method is needed because the IE DOM does not allow the style attribute to be copied directly.
	@param destinationElement The destination of the style information.
	@param sourceElement The source of the style information.
	*/
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
					destinationElement.style[styleProperty]=styleValue;	//copy this style
				}
			}				
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
					this.appendXMLAttribute(stringBuilder, attribute.nodeName, attribute.nodeValue);	//append this attribute
				}
				var childNodes=node.childNodes;	//get a list of child nodes
				var childNodeCount=childNodes.length;	//find out how many child nodes there are
				if(childNodeCount>0 || nodeName=="div" || nodeName=="span")	//if there are children (special-case "div" and "span" for IE, which will drop a <div/> from the DOM
				{				
					stringBuilder.append(">");	//>
					for(var i=0; i<childNodeCount; ++i)	//for all of the child nodes
					{
						var childNode=childNodes[i];	//get a reference to this child node
						this.appendNodeString(stringBuilder, childNode);	//append this child node
					}
					this.appendXMLEndTag(stringBuilder, nodeName);	//append the end tag
				}
				else	//if there are no children, create an empty element (otherwise, for elements like <input></input>, IE6 will see two elements)
				{
					stringBuilder.append("/>");	///>
				}
				break;
			case Node.COMMENT_NODE:	//comment
				stringBuilder.append(node.nodeValue);	//append the node's value with no changes TODO encode the sequence "--"
				break;
			case Node.TEXT_NODE:	//text
				this.appendXMLText(stringBuilder, node.nodeValue);	//append the node's text value
				break;
			//TODO add checks for other elements, such as CDATA
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
		string=string.replace(/"/g, "&quot;");	//encode '\"'
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
	@param parameters (...) Zero or more parameters of type Parameter.
	@return A reference to the string builder.
	*/ 
	appendXMLStartTag:function(stringBuilder, tagName, parameters)
	{
		stringBuilder.append("<").append(tagName);	//<tagName
		var argumentCount=arguments.length;	//find out how many arguments there are
		for(var i=2; i<argumentCount; ++i)	//for each argument (not counting the first two)
		{
			var parameter=arguments[i];	//get this argument
			if(parameter.value!=null)	//if a parameter value was given
			{
				this.appendXMLAttribute(stringBuilder, parameter.name, parameter.value);	//append the attribute
			}
		}
		return stringBuilder.append(">");	//>
	},

	/**Appends an XML attribute with the given name and value to the given string builder.
	If the value of a parameter is not a string, it will be converted to one.
	The attribute-value combination will be preceded by a space.
	@param stringBuilder The string builder to hold the data.
	@param attributeName The name of the XML attribute.
	@param attributeValue The value of the XML attribute.
	@return A reference to the string builder.
	*/ 
	appendXMLAttribute:function(stringBuilder, attributeName, attributeValue)
	{
		return stringBuilder.append(" ").append(attributeName).append("=\"").append(this.encodeXML(attributeValue.toString())).append("\"");	//name="value"
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
@param parameter: An optional parameter with which to initialize the request.
var parameters: The list of parameters.
@see Parameter
*/
function FormAJAXEvent(parameter)
{
	this.parameters=new Array();	//create the parameter array
	if(!FormAJAXEvent.prototype._initialized)
	{
		FormAJAXEvent.prototype._initialized=true;
		
		/**Adds a parameter to the form AJAX request.
		@param parameter: The parameter to add.
		@see Parameter
		*/
		FormAJAXEvent.prototype.addParameter=function(parameter)
		{
			this.parameters.add(parameter);	//add another parameter to the array
		};
	}
	if(parameter)	//if a parameter was passed
	{
		this.addParameter(parameter);	//add this parameter to the request
	}
}

//Action AJAX Event

/**A class encapsulating action information for an AJAX request.
@param componentID: The ID of the source component.
@param targetID: The ID of the target element.
@param actionID: The action identifier, or null if no particular action is indicated.
var componentID: The ID of the source component.
var targetID: The ID of the target element.
var actionID: The action identifier, or null if no particular action is indicated.
*/
function ActionAJAXEvent(componentID, targetID, actionID)
{
	this.componentID=componentID;
	this.targetID=targetID;
	this.actionID=actionID;
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

	if(!this._initialized)
	{
		this._initialized=true;

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
		
		/**@return A newly created XML HTTP request object.*/
		HTTPCommunicator.prototype._createXMLHTTP=function()
		{
			if(window.XMLHttpRequest)	//if we can create an XML HTTP request (e.g. Mozilla)
			{
				return new XMLHttpRequest();
			}
			else if(window.ActiveXObject)	//if we can create ActiveX objects
			{
				for(var i=this.MSXMLHTTP_VERSIONS.length-1; i>=0; --i)	//for each available version
				{
					try
					{
						return new ActiveXObject(this.MSXMLHTTP_VERSIONS[i]);	//try to create a new ActiveX object
					}
					catch(exception)	//ignore the errors
					{
					}
				}
			}
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
			this.xmlHTTP=HTTPCommunicator.prototype._createXMLHTTP();	//create an XML HTTP object
			if(method=="GET" && query)	//if there is a query for the GET method
			{
				uri=uri+"?"+query;	//add the query to the URI
			}
			var asynchronous=Boolean(this.processHTTPResponse);	//see if we should make an asynchronous request
			if(asynchronous)	//if we're making asynchronous requests
			{
				this.xmlHTTP.onreadystatechange=this._createOnReadyStateChangeCallback();	//create and assign a callback function for processing the response
			}
			this.xmlHTTP.open(method, uri, asynchronous);
			var content=null;	//we'll create content if we need to
			if(method=="POST")	//if this is the POST method
			{
//TODO del alert("posting with query: "+query);
	//TODO del alert("posting with query: "+query);
//TODO del				this.xmlHTTP.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");	//set the post content type
				if(contentType)	//if a content type was given
				{
					this.xmlHTTP.setRequestHeader("Content-Type", contentType);	//set the post content type
				}
				if(query)	//if there is a post query
				{
					content=query;	//use the query as the content
				}	
			}
			this.xmlHTTP.send(content);	//send the request
			if(!asynchronous)	//if we're communicating synchronously
			{
				thisHTTPCommunicator._reportReportResponse();	//report the response immediately TODO maybe put this into an asynchrous call using setTimeout()
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

/**A class encapsulating AJAX functionality for Guise.*/
function GuiseAJAX()
{

	/**The object for communicating with Guise via AJAX.*/
	this.httpCommunicator=new HTTPCommunicator();

	/**The queue of AJAX HTTP request information objects.*/
	this.ajaxRequests=new Array();

	/**The queue of AJAX response XML DOM trees.*/
	this.ajaxResponses=new Array();

	/**Whether we are currently processing AJAX requests.*/
	this.processingAJAXRequests=false;

	/**Whether we are currently processing AJAX responses.*/
	this.processingAJAXResponses=false;

	if(!this._initialized)
	{
		this._initialized=true;

		/**The content type of a Guise AJAX request.*/
		GuiseAJAX.prototype.REQUEST_CONTENT_TYPE="application/x-guise-ajax-request+xml";

		/**The enumeration of the names of the request elements.*/
		GuiseAJAX.prototype.RequestElement=
			{
				REQUEST: "request", EVENTS: "events",
				FORM: "form", CONTROL: "control", NAME: "name", VALUE: "value",
				ACTION: "action", COMPONENT_ID: "componentID", TARGET_ID: "targetID", ACTION_ID: "actionID",
				DROP: "drop", SOURCE: "source", TARGET: "target", MOUSE: "mouse", ID: "id", X: "x", Y: "y",
				INIT: "init"
			};

		/**The content type of a Guise AJAX response.*/
		GuiseAJAX.prototype.RESPONSE_CONTENT_TYPE="application/x-guise-ajax-response+xml";

		/**The enumeration of the names of the response elements.*/
		GuiseAJAX.prototype.ResponseElement={RESPONSE: "response", PATCH: "patch", REMOVE: "remove", NAVIGATE: "navigate", RELOAD: "reload"};

		/**Immediately sends or queues an AJAX request.
		@param ajaxRequest The AJAX request to send.
		*/
		GuiseAJAX.prototype.sendAJAXRequest=function(ajaxRequest)
		{
			if(AJAX_URI)	//if AJAX is enabled
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
						this.httpCommunicator.post(AJAX_URI, requestStringBuilder.toString(), this.REQUEST_CONTENT_TYPE);	//post the HTTP request information
					}
					catch(exception)	//if a problem occurred
					{
						//TODO log a warning
//TODO fix alert(exception);
						AJAX_URI=null;	//stop further AJAX communication
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
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.FORM);	//<form>
			var parameters=ajaxFormRequest.parameters;	//get the parameters
			if(parameters.length>0)	//if there are parameters
			{
				var parameterStrings=new Array(parameters.length);	//create an array of parameter strings
				for(var i=parameterStrings.length-1; i>=0; --i)	//for each parameter string
				{
					var parameter=parameters[i];	//get this parameter
					DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.CONTROL, new Parameter(this.RequestElement.NAME, parameter.name));	//<control name="name">
					DOMUtilities.appendXMLText(stringBuilder, parameter.value);	//append the parameter value
					DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.CONTROL);	//</control>
				}
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
					new Parameter(this.RequestElement.COMPONENT_ID, ajaxActionEvent.componentID),	//componentID="componentID"
					new Parameter(this.RequestElement.TARGET_ID, ajaxActionEvent.targetID),	//targetID="targetID"
					new Parameter(this.RequestElement.ACTION_ID, ajaxActionEvent.actionID));	//actionID="actionID"
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
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.SOURCE, new Parameter(this.RequestElement.ID, ajaxDropEvent.dragSource.id));	//<source id="id">
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.SOURCE);	//</source>
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.TARGET, new Parameter(this.RequestElement.ID, ajaxDropEvent.dropTarget.id));	//<source id="id">
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.TARGET);	//</target>
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.MOUSE, new Parameter(this.RequestElement.X, ajaxDropEvent.mousePosition.x), new Parameter(this.RequestElement.Y, ajaxDropEvent.mousePosition.y));	//<mouse x="x" y="y">
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.MOUSE);	//</mouse>
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.DROP);	//</drop>
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
						if(xmlHTTP.responseXML && xmlHTTP.responseXML.documentElement)	//if we have XML (if there is no content or there is an error, IE sends back a document has a null xmlHTTP.responseXML.documentElement)
						{
							thisGuiseAJAX.ajaxResponses.enqueue(xmlHTTP.responseXML);	//enqueue the response XML
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
					AJAX_URI=null;	//stop further AJAX communication
					throw exception;	//TODO testing
				}
			};
		};

		/**Processes responses from AJAX requests.
		This routine should be called asynchronously from an event so that the DOM tree can be successfully updated.
		@see GuiseAJAX#ajaxResponses
		*/
		GuiseAJAX.prototype.processAJAXResponses=function()
		{
			if(!this.processingAJAXResponses)	//if we aren't processing AJAX responses TODO fix small race condition in determining whether processing is occurring
			{
				this.processingAJAXResponses=true;	//we are processing AJAX responses now
				try
				{
					while(this.ajaxResponses.length>0)	//while there are more AJAX responses TODO fix small race condition on adding responses
					{
						var responseDocument=this.ajaxResponses.dequeue();	//get this response
						//TODO assert document element name is "response"
						var childNodeList=responseDocument.documentElement.childNodes;	//get all the child nodes of the document element
						var childNodeCount=childNodeList.length;	//find out how many children there are
						for(var i=0; i<childNodeCount; ++i)	//for each child node
						{
							var childNode=childNodeList[i];	//get this child node
							if(childNode.nodeType==Node.ELEMENT_NODE)	//if this is an element
							{
								var elementName=childNode.nodeName;	//get this element name
								switch(elementName)	//see which type of response this is
								{
									case this.ResponseElement.PATCH:	//patch
										this._processPatch(childNode);	//patch the document with this patch information
										break;
									case this.ResponseElement.REMOVE:	//remove
										this._processRemove(childNode);	//remove the elements from the document with this removal element
										break;
									case this.ResponseElement.NAVIGATE:	//navigate
										window.location.href=getText(childNode);	//go to the new location
										return;	//stop processing events
//TODO del when works										break;	//TODO decide whether we should continue processing events or not
									case this.ResponseElement.RELOAD:	//reload
										window.location.reload();	//reload the page
										return;	//stop processing events
								}
							}
						}
						this.processAJAXRequests();	//make sure there are no waiting AJAX requests
					}
				}
				finally
				{
					this.processingAJAXResponses=false;	//we are no longer processing AJAX responses
				}
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
							updateComponents(oldElement);	//now that we've patched the old element, update any components that rely on the old element
						}
						else if(hasClass(childNode, "frame"))	//if the element doesn't currently exist, but the patch is for a frame, create a new frame
						{
//TODO fix alert("ready to import node");
							oldElement=document.importNode(childNode, true);	//create an import clone of the node
//TODO del alert("ready to add frame: "+typeof oldElement);
							frames.add(oldElement);	//add this frame
//TODO fix alert("frame added");
						}
					}
				}
			}
		}

		/**Processes the AJAX remove response.
		@param element The element representing removal response.
		*/ 
		GuiseAJAX.prototype._processRemove=function(element)
		{
			var id=element.getAttribute("id");	//get the element ID, if there is one
			if(id)	//if the element has an ID
			{
				var oldElement=document.getElementById(id);	//get the old element
				if(oldElement!=null)	//if we found the old element
				{
					if(frames.contains(oldElement))	//if we're removing a frame
					{
						frames.remove(oldElement);	//remove the frame
					}
					else	//if we're removing any other node
					{
						uninitializeNode(oldElement);	//uninitialize the element
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
		}

		/**Synchronizes an element hierarchy with its patch element.
		@param oldElement The old version of the element.
		@param element The element hierarchy to patch into the existing document.
		*/ 
		GuiseAJAX.prototype._synchronizeElement=function(oldElement, element)
		{
//TODO del alert("ready to synchronize element "+oldElement.nodeName+" with ID: "+oldElement.id+" against element "+element.nodeName+" with ID: "+element.getAttribute("id"));
				//remove any attributes the old element has that are not in the new element
			var oldAttributes=oldElement.attributes;	//get the old element's attributes
			for(var i=oldAttributes.length-1; i>=0; --i)	//for each old attribute
			{
				var oldAttribute=oldAttributes[i];	//get this attribute
				var oldAttributeName=oldAttribute.nodeName;	//get the attribute name
				var oldAttributeValue=oldAttribute.nodeValue;	//get the attribute value
				var attributeName=oldAttributeName;
				if(oldAttributeName=="readOnly")	//TODO fix for other misspelled attributes, such as className
				{
					attributeName="readonly";
				}
//TODO fix or del				if(attributeValue!=null && attributeValue.length>0 && !element.getAttribute(attributeName))	//if there is really an attribute value (IE provides all possible attributes, even with those with no value) and the new element doesn't have this attribute
				if(element.getAttribute(attributeName)==null)	//if the new element doesn't have this attribute
				{
					if(attributeName!="style")	//don't remove local styles, because they may be used by Guise (with frames, for instance)
					{
//TODO del alert("ready to remove "+oldElement.nodeName+" attribute "+oldAttributeName+" with current value "+oldAttributeValue);
						oldElement.removeAttribute(oldAttributeName);	//remove the attribute normally (apparently no action will take place if performed on IE-specific attributes such as element.start)
					}
//TODO fix					i=0;	//TODO fix; temporary to get out of looking at all IE's attributes
				}
			}
			if(oldElement.value && element.getAttribute("value")==null)	//if there is an old value but no value attribute present in the new element (IE 6 and Mozilla do not show "value" in the list of enumerated values)
			{
				oldElement.value="";	//set the value to the empty string (setting the value to null will result in "null" being displayed in the input control on IE)
			}
				//patch in the new and changed attributes
			var attributes=element.attributes;	//get the new element's attributes
			for(var i=attributes.length-1; i>=0; --i)	//for each attribute
			{
				var attribute=attributes[i];	//get this attribute
				var attributeName=DOMUtilities.getHTMLAttributeName(attribute.nodeName);	//get the attribute name, compensating for special HTML attributes such as "className"
				var attributeValue=attribute.nodeValue;	//get the attribute value
				if(attributeName=="style")	//if this is a style attribute, we have to treat it differently, because neither Mozilla nor IE provide normal DOM access to the literal style attribute value
				{
//TODO fix with something else to give IE layout					oldElement["contentEditable"]=false;	//for IE 6, give the component "layout" so that things like opacity will work
					var styles=attributeValue.split(";");	//split out the individual styles
					for(var styleIndex=styles.length-1; styleIndex>=0; --styleIndex)	//for each style
					{
						var styleComponents=styles[styleIndex].split(":");	//get a reference to this style and split out the property and value
						if(styleComponents.length==2)	//we expect there to be a property and a value
						{
							var styleProperty=styleComponents[0].trim();	//get the trimmed style property
							var styleValue=styleComponents[1].trim();	//get the trimmed style value
							if(oldElement.style[styleProperty]!=styleValue)	//if the style is different	TODO check about removing a style
							{
//TODO del alert("ready to set element style "+styleProperty+" to value "+styleValue);
								oldElement.style[styleProperty]=styleValue;	//update this style
							}
						}
					}				
				}
				else	//for any other attribute
				{
					if(oldElement[attributeName]!=attributeValue)	//if the old element has a different (or no) value for this attribute (Firefox maintains different values for element.getAttribute(attributeName) and element[attributeName]) (note also that using setAttribute() IE will sometimes throw an error if button.style is changed, for instance)
					{
	//TODO del alert("updating "+element.nodeName+" attribute "+attributeName+" from value "+oldElement[attributeName]+" to new value "+attributeValue);
						oldElement[attributeName]=attributeValue;	//update the old element's attribute (this format works for Firefox where oldElement.setAttribute("value", attributeValue) does not)
	//TODO: fix the Firefox problem of sending an onchange event for any elements that get updated from an Ajax request, but only later when the focus blurs
	//TODO fix the focus problem if the user has focus on an element that gets changed in response to the event
					}
				}
			}
			var elementName=element.nodeName;	//save the element name
			
				//patch in the new child element hierarchy
			if(elementName=="textarea")	//if this is a text area, do special-case value changing (restructuring won't work in IE and Mozilla) TODO check for other similar types TODO use a constant
			{
				oldElement.value=getText(element);	//set the new value to be the text of the new element
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
//TODO del alert("node types are different; old: "+oldChildNode.nodeType+" new: "+childNode.nodeType);
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
//TODO del alert("children are compatible, old "+oldElement.nodeName+" with ID "+oldElement.id+" child node count: "+oldChildNodeCount+" new "+element.nodeName+" "+"with ID "+element.getAttribute("id")+" child node count "+childNodeCount+" (verify) "+element.childNodes.length);

/*TODO del
if(oldChildNodeCount!=childNodeCount)	//TODO del
{
	for(var i=0; i<oldChildNodeCount; ++i)
	{
		var childNode=oldElement.childNodes[i];
//TODO fix alert("child node "+i+" is of type "+childNode.nodeType+" with name "+childNode.nodeName);
alert("child node "+i+" is of type "+childNode.nodeType+" with name "+childNode.nodeName+(childNode.nodeType==Node.ELEMENT_NODE ? " with ID "+childNode.id : ""));
	}
}
*/
						//remove superfluous old nodes
					for(var i=oldChildNodeCount-1; i>=childNodeCount; --i)	//for each old child node that is not in the new node
					{
						var oldChildNode=oldChildNodeList[i];	//get this child node
						uninitializeNode(oldChildNode);	//uninitialize the node tree
//TODO del alert("removing old node: "+oldChildNodeList[i].nodeName);
						oldElement.removeChild(oldChildNode);	//remove this old child
						
					}
//TODO del alert("children are still compatible, old child node count: "+oldElement.childNodes.length+" new child node count "+childNodeCount);
				}
				else	//if children are not compatible
				{
//TODO del alert("children are not compatible, old "+oldElement.nodeName+" with ID "+oldElement.id+" child node count: "+oldChildNodeCount+" new "+element.nodeName+" "+"with ID "+element.getAttribute("id")+" child node count "+childNodeCount+" (verify) "+element.childNodes.length);
					DOMUtilities.removeChildren(oldElement);	//remove all the children from the old element and start from scratch
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
//TODO del alert("ready to clone node: "+DOMUtilities.getNodeString(childNode));
						var importedNode=document.importNode(childNode, true);	//create an import clone of the node
						oldElement.appendChild(importedNode);	//append the imported node to the old element
						initializeNode(importedNode);	//initialize the new imported node, installing the correct event handlers
					}
				}
			}
		};

	}

	this.httpCommunicator.setProcessHTTPResponse(this._createHTTPResponseCallback());	//set up our callback function for processing HTTP responses

}

/**The global object for AJAX communication with Guise.*/
var guiseAJAX=new GuiseAJAX();

/**A class maintaining event function information and optionally adapting an event to a W3C compliant version.
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
			if(!event.target)	//if there is no target information
			{
				//TODO assert event.srcElement
				event.target=event.srcElement;	//assign a W3C target property
			}
			if(!event.currentTarget && currentTarget)	//if there is no current target information, but one was passed to us
			{
				event.currentTarget=currentTarget;	//assign a W3C current target property
			}
			if(!event.data)	//if there is no data
			{
				//TODO assert event.keyCode
				event.data=event.keyCode;	//assign a W3C data property
			}
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
		@see http://www.scottandrew.com/weblog/articles/cbs-events
		*/
		EventManager.prototype.removeEvent=function(object, eventType, fn, useCapture)
		{
			var eventListener=this._removeEventListener(object, eventType, fn);	//remove the event listener keeping information about this event
			var result=true;	//we'll store the result here
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
		@param object The object for which events should be cleared, including all child objects, or null if events should be cleared on all objects.
		*/
		EventManager.prototype.clearEvents=function(object)
		{
			for(var i=this._eventListeners.length-1; i>=0; --i)	//for each event listener, going backwards so that removing an event listener will not disturb iteration
			{
				var eventListener=this._eventListeners[i];	//get the last event listener
				if(!object || eventListener.currentTarget==object)	//if this event listener was registered on this object, or if all event listeners should be removed)
				{
					this.removeEvent(eventListener.currentTarget, eventListener.eventType, eventListener.fn, eventListener.useCapture);	//remove this event, which will also remove the event listener from the array
				}
			}
			if(object && object.nodeType==Node.ELEMENT_NODE)	//if a DOM element was given
			{
				var childNodes=object.childNodes;	//get a reference to all child nodes
				for(var i=childNodes.length-1; i>=0; --i)	//for each child node
				{
					this.clearEvents(childNodes[i]);	//clear events for this child
				}
			}		
/*TODO del when works
			while(this._eventListeners.length)	//while there are event listeners
			{
				var eventListener=this._eventListeners[this._eventListeners.length-1];	//get the last event listener
				this.removeEvent(eventListener.currentTarget, eventListener.eventType, eventListener.fn, eventListener.useCapture);	//remove this event, which will also remove the event listener from the array
			}
*/
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
var onDrag(element, x, y): The method called when dragging occurs, or null if no additional action should be taken. The coordinates are in terms of the element's position type.
var onEnd(element): The method called when dragging ends, or null if no additional action should be taken. The coordinates are in terms of the element's position type.
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
				element.style.zIndex=256;	//give the element an arbitrarily high z-index value so that it will appear in front of other components
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

/**The layer that allows modality by blocking user interaction to elements below.*/
var modalLayer=null;

/**Updates the size of the modal layer, creating it if necessary.*/
function updateModalLayer()
{
	if(modalLayer==null)	//if the modal layer has not yet been created
	{
		modalLayer=document.createElementNS("http://www.w3.org/1999/xhtml", "div");	//create a div
		modalLayer.className="modalLayer";	//load the modal layer style
		document.body.appendChild(modalLayer);	//add the modal layer to the document
	}
	var pageSize=getPageSize();	//get the size of the page
	var viewportSize=getViewportSize();	//get the size of the viewport
	modalLayer.style.width=Math.max(viewportSize.width, pageSize.width)+"px";	//update the size of the modal layer to the larger of the page and the viewport
	modalLayer.style.height=Math.max(viewportSize.height, pageSize.height)+"px";
}

//Guise functionality

/**Called when the window loads.
This implementation installs listeners.
*/
function onWindowLoad()
{
	eventManager.addEvent(window, "resize", onWindowResize, false);	//add a resize listener
	eventManager.addEvent(window, "scroll", onWindowResize, false);	//add a scroll listener
	eventManager.addEvent(window, "unload", onWindowUnload, false);	//do the appropriate uninitialization when the window unloads
	initializeNode(document.documentElement);	//initialize the document tree
	updateComponents(document.documentElement);	//update all components represented by elements within the document
	dropTargets.sort(function(element1, element2) {return getElementDepth(element1)-getElementDepth(element2);});	//sort the drop targets in increasing order of document depth
	eventManager.addEvent(document, "mouseup", onDragEnd, false);	//listen for mouse down anywhere in the document (IE doesn't allow listening on the window), as dragging may end somewhere else besides a drop target
	updateModalLayer();	//create and update the modal layer
	var focusable=getFocusableDescendant(document.documentElement);	//see if the document has a node that can be focused
	if(focusable)	//if we found a focusable node
	{
		focusable.focus();	//focus on the node
	}
	guiseAJAX.sendAJAXRequest(new InitAJAXEvent());	//send an initialization AJAX request
}

/**Called when the window unloads.
This implementation uninstalls all listeners.
@param event The object containing event information.
*/
function onWindowUnload(event)
{
	AJAX_URI=null;	//turn off AJAX
	eventManager.clearEvents();	//unload all events
}

/**Called when the window resizes.
This implementation updates the modal layer.
@param event The object containing event information.
*/
function onWindowResize(event)
{
	updateModalLayer();	//update the modal layer
}

/**Called when the window scrolls.
@param event The object containing event information.
*/
function onWindowScroll(event)
{
}

/**Initializes a node and all its children, adding the correct listeners.
@param node The node to initialize.
*/
function initializeNode(node)
{
	switch(node.nodeType)	//see which type of child node this is
	{
		case Node.ELEMENT_NODE:	//element
//TODO fix with something else to give IE layout			node["contentEditable"]=false;	//for IE 6, give the component "layout" so that things like opacity will work
//TODO bring back after giving all relevant nodes IDs			if(node.id)	//only look at element swith IDs
//TODO this may allow "layout" for IE, but only do it when we need it (otherwise it will screw up buttons and such)			node.style.zoom=1;	//TODO testing
			{
				var elementName=node.nodeName.toLowerCase();	//get the element name
				var elementClassName=node.className;	//get the element class name
				var elementClassNames=elementClassName ? elementClassName.split(/\s/) : EMPTY_ARRAY;	//split out the class names
				switch(elementName)	//see which element this is
				{
					case "a":
						if(elementClassNames.contains("link"))	//if this is a Guise link TODO later look at *all* link clicks and do popups for certain ones
						{
							eventManager.addEvent(node, "click", onLinkClick, false);	//listen for anchor clicks
						}
						else if(elementClassNames.containsMatch(/-tab(-selected)?$/))	//if this is a tab TODO use a constant
						{
							eventManager.addEvent(node, "click", onTabClick, false);	//listen for tab clicks
						}
						break;
					case "button":
						if(elementClassNames.contains("button"))	//if this is a Guise button
						{
							eventManager.addEvent(node, "click", onButtonClick, false);	//listen for button clicks
						}
						break;
					case "div":
								//check for menu
						if(elementClassNames.containsMatch(STYLES.MENU_REGEXP))	//if this is a menu
						{
							var menu=getMenu(node);	//get the menu ancestor
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
					case "input":
						switch(node.type)	//get the type of input
						{
							case "text":
							case "password":
								eventManager.addEvent(node, "change", onTextInputChange, false);
								break;
							case "checkbox":
							case "radio":
								eventManager.addEvent(node, "click", onCheckInputChange, false);
								break;
						}
						break;
					case "li":
						if(elementClassName && elementClassName.indexOf(TREE_NODE_CLASS_PREFIX)==0)	//if this is a tree node
						{
							eventManager.addEvent(node, "click", onTreeNodeClick, false);	//listen for clicks
						}
						break;
					case "select":
						eventManager.addEvent(node, "change", onSelectChange, false);
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
							break;
						case STYLES.DRAG_HANDLE:
							eventManager.addEvent(node, "mousedown", onDragBegin, false);	//listen for mouse down on a drag handle
							break;
						case STYLES.DROP_TARGET:
							dropTargets.add(node);	//add this node to the list of drop targets
							break;
						case STYLES.SLIDER_CONTROL_X_LTR_THUMB:
						case STYLES.SLIDER_CONTROL_X_RTL_THUMB:
						case STYLES.SLIDER_CONTROL_Y_LTR_THUMB:
						case STYLES.SLIDER_CONTROL_Y_RTL_THUMB:
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
		//initialize child nodes
	var childNodeList=node.childNodes;	//get all the child nodes
	var childNodeCount=childNodeList.length;	//find out how many children there are
	for(var i=0; i<childNodeCount; ++i)	//for each child node
	{
		initializeNode(childNodeList[i]);	//initialize this child node
	}
}

/**Updates the representation of any dynamic components based upon the state of the underlying element.
Components for the given node and any descendant nodes are updated.
@param node The node for which components should be updated.
*/
function updateComponents(node)
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
						if(elementClassNames.containsMatch(/^sliderControl-[xy]-(ltr|rtl)$/))	//if this is a slider control TODO use a constant
						{
							updateSlider(node);	//update the slider
						}
						break;
				}
			}
			break;
	}
		//update child node components
	var childNodeList=node.childNodes;	//get all the child nodes
	var childNodeCount=childNodeList.length;	//find out how many children there are
	for(var i=0; i<childNodeCount; ++i)	//for each child node
	{
		updateComponents(childNodeList[i]);	//initialize the components for this child node
	}
}

/**Uninitializes a node and all its children, removing all added listeners.
@param node The node to uninitialize.
*/
function uninitializeNode(node)
{
	eventManager.clearEvents(node);	//clear events for this node and descendants
}

//TODO del var test=new Array();

var lastFocusedNode=null;

/**Called when an element receives a focus event.
@param event The object containing event information.
*/
function onFocus(event)
{
	var currentTarget=event.currentTarget;	//get the control receiving the focus
	if(frames.modalFrame!=null)	//if there is a modal frame
	{
	
//TODO del var dummy=currentTarget.nodeName+" "+currentTarget.id;

		if(!hasAncestor(currentTarget, frames.modalFrame))	//if focus is trying to go to something outside the modal frame
		{
//TODO fix alert("focus outside of frame");
			if(hasAncestor(lastFocusedNode, frames.modalFrame))	//if we know the last focused node, and it was in the modal frame
			{

//TODO del dummy+=" changing to last focused "+lastFocusedNode.nodeName+" id "+lastFocusedNode.id;

				lastFocusedNode.focus();	//focus back on the last focused node


			}
			else	//if we don't know the last focused node
			{
				var focusable=getFocusableDescendant(frames.modalFrame);	//see if the modal frame has a node that can be focused
				if(focusable)	//if we found a focusable node
				{
//TODO del dummy+=" changing to first focusable "+focusable.nodeName+" id "+focusable.id;
					focusable.focus();	//focus on the node
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

/**Called when the contents of a text input changes.
@param event The object describing the event.
*/
function onTextInputChange(event)
{
	if(AJAX_URI)	//if AJAX is enabled
	{
		var textInput=event.currentTarget;	//get the control in which text changed
	//TODO del alert("an input changed! "+textInput.id);
		var ajaxRequest=new FormAJAXEvent(new Parameter(textInput.name, textInput.value));	//create a new form request with the control name and value
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
	if(form && getDescendantElementByName(form, "input", new Parameter("type", "file")))	//if there is a file input element, we'll have to submit the entire page rather than using AJAX
	{
		if(element.id)	//if the button has an ID
		{
				//ask confirmations if needed
			var childNodeList=element.childNodes;	//get all the child nodes of the element
			var childNodeCount=childNodeList.length;	//find out how many children there are
			for(var i=0; i<childNodeCount; ++i)	//for each child node
			{
				var childNode=childNodeList[i];	//get this child node
				if(childNode.nodeType==Node.COMMENT_NODE && childNode.nodeValue)	//if this is a comment node
				{
					var commentValue=childNode.nodeValue;	//get the comment value
					var delimiterIndex=commentValue.indexOf(':');	//get the delimiter index
					if(delimiterIndex>=0)	//if there is a delimiter
					{
						var paramName=commentValue.substring(0, delimiterIndex);	//get the parameter name
						var paramValue=commentValue.substring(delimiterIndex+1);	//get the parameter value
						if(paramName="confirm")	//if this is a confirmation
						{
							if(!confirm(paramValue))	//ask for confirmation; if the user does not confirm
							{
								return;	//don't process the event further
							}
						}
					}
				}
			}
//TODO del			var form=getForm(element);	//get the form
//TODO del			if(form && form.id)	//if there is a form with an ID
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
	var element=event.currentTarget;	//get the element on which the event was registered
//TODO del alert("action on: "+element.nodeName);
	if(element.id)	//if the button has an ID
	{
			//ask confirmations if needed
		var childNodeList=element.childNodes;	//get all the child nodes of the element
		var childNodeCount=childNodeList.length;	//find out how many children there are
		for(var i=0; i<childNodeCount; ++i)	//for each child node
		{
			var childNode=childNodeList[i];	//get this child node
			if(childNode.nodeType==Node.COMMENT_NODE && childNode.nodeValue)	//if this is a comment node
			{
				var commentValue=childNode.nodeValue;	//get the comment value
				var delimiterIndex=commentValue.indexOf(':');	//get the delimiter index
				if(delimiterIndex>=0)	//if there is a delimiter
				{
					var paramName=commentValue.substring(0, delimiterIndex);	//get the parameter name
					var paramValue=commentValue.substring(delimiterIndex+1);	//get the parameter value
					if(paramName="confirm")	//if this is a confirmation
					{
						if(!confirm(paramValue))	//ask for confirmation; if the user does not confirm
						{
							event.stopPropagation();	//tell the event to stop bubbling
							event.preventDefault();	//prevent the default functionality from occurring
							return;	//don't process the event further
						}
					}
				}
			}
		}
		var form=getForm(element);	//get the form
		if(form && form.id)	//if there is a form with an ID
		{
			var actionInputID=form.id.replace(".form", ".input");	//determine the ID of the hidden action input TODO use a constant, or get these values using a better method
			if(AJAX_URI)	//if AJAX is enabled
			{
				var ajaxRequest=new FormAJAXEvent(new Parameter(actionInputID, element.id));	//create a new form request with form's hidden action control and the action element ID
				guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request			
			}
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
			event.stopPropagation();	//tell the event to stop bubbling
			event.preventDefault();	//prevent the default functionality from occurring
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
		if(uri.parameters.length>0)	//if there are parameters given
		{
			var parameter=uri.parameters[0];	//get the first parameter
			if(AJAX_URI)	//if AJAX is enabled
			{
				var ajaxRequest=new FormAJAXEvent(parameter);	//create a new form request with the parameter
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
}

/**Called when an element marked as "action" is clicked.
This method searches up the hierarchy to find the enclosing "component" element and sends an AJAX action event.
@param event The object describing the event.
*/
function onActionClick(event)
{
	var target=event.currentTarget;	//get the element on which the event was registered
	var targetID=target.id;	//get the target ID
	if(targetID)	//if the element has an ID (otherwise, we couldn't report the action)
	{
		var component=getAncestorElementByClassName(target, STYLES.COMPONENT_REGEXP);	//get the component element TODO improve all this
		if(component)	//if there is a component
		{
			var componentID=component.id;	//get the component ID
			if(componentID)	//if there is a component ID
			{
				if(AJAX_URI)	//if AJAX is enabled
				{
					var ajaxRequest=new ActionAJAXEvent(componentID, targetID, null);	//create a new action request with no action ID
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
	if(AJAX_URI)	//if AJAX is enabled
	{
		var ajaxRequest=new FormAJAXEvent(new Parameter(checkInput.name, checkInput.checked ? checkInput.value : ""));	//create a new form request with the control name and value
		guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request
		event.stopPropagation();	//tell the event to stop bubbling
	}
	else	//if AJAX is not enabled
	{
		if(getMenu(checkInput))	//if this check is inside a menu, submit the form so that menus will cause immediate reaction
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
	if(AJAX_URI)	//if AJAX is enabled
	{
		var select=event.currentTarget;	//get the control to which the listener was listening
	//TODO del alert("a select changed! "+select.id);
		var options=select.options;	//get the select options
		var ajaxRequest=new FormAJAXEvent();	//create a new form request
		for(var i=0; i<options.length; ++i)	//for each option
		{
			var option=options[i];	//get this option
			if(option.selected)	//if this option is selected
			{
				ajaxRequest.addParameter(new Parameter(select.name, option.value));	//add the control name and value as a parameter
			}
		}
		guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request
		event.stopPropagation();	//tell the event to stop bubbling
	}
}

/**Called when a checkbox is activated.
@param event The object describing the event.
*/
function onTreeNodeClick(event)
{
	if(AJAX_URI)	//if AJAX is enabled
	{
		if(event.target.nodeName.toLowerCase()=="a")	//TODO fix; temporary hack for allowing links inside trees
		{
			return;
		}
		if(event.target.nodeName.toLowerCase()=="ul" && event.target.className==oldClassName && event.target.parentNode==treeNode)	//if the user clicked on the tree node's list of child nodes
		{
			return;	//don't toggle the list if the user clicked on the children
		}
		var treeNode=event.currentTarget;	//get tree node that owns the clicked element
//TODO del alert("target of tree click: "+treeNode.nodeName);
		event.stopPropagation();	//tell the event to stop bubbling
		event.preventDefault();	//prevent the default functionality from occurring
//TODO del	alert("ID of tree node: "+treeNode.lastChild.id);
		var oldClassName=treeNode.className;	//get the class name of the tree node
/*TODO del
alert("target node name: "+event.target.nodeName);
alert("target class name: "+event.target.className);
alert("target parent is the tree node?: "+(event.target.parentNode==treeNode));
alert("target parent node name: "+event.target.parentNode.nodeName);
*/
		var isCollapsed=oldClassName.indexOf(TREE_NODE_COLLAPSED_CLASS_SUFFIX)>=0;	//see if this tree node has a class name representing the collapsed state
		var isExpanded=oldClassName.indexOf(TREE_NODE_EXPANDED_CLASS_SUFFIX)>=0;	//see if this tree node has a class name representing the expanded state
		if(isCollapsed || isExpanded)	//if the tree node is collapsed or expanded (ignore leaf nodes)
		{
			var childNodeList=treeNode.childNodes;	//get all the child nodes of the element
			for(var i=childNodeList.length-1; i>=0; --i)	//for each child node (the sub-list is probably the last one)
			{
				var childNode=childNodeList[i];	//get this child node
				if(childNode.nodeType==Node.ELEMENT_NODE && childNode.nodeName.toLowerCase()=="ul" && childNode.className==oldClassName)	//if there is a child ul element with the same class name
				{
					if(isExpanded)	//if the tree node is expanded
					{
						var newClassName=oldClassName.replace(TREE_NODE_EXPANDED_CLASS_SUFFIX, TREE_NODE_COLLAPSED_CLASS_SUFFIX);	//convert from expanded to collapsed
					}
					else	//if the tree node is collapsed
					{
						var newClassName=oldClassName.replace(TREE_NODE_COLLAPSED_CLASS_SUFFIX, TREE_NODE_EXPANDED_CLASS_SUFFIX);	//convert from expanded to collapsed				
					}
					childNode.className=newClassName;	//update the child list class
					treeNode.className=newClassName;	//update class of the the tree node itself to match
					break;	//we've switched states, so stop looking for a tree child
				}
			}
		}
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
			this._closeTimeout=window.setTimeout("menuState._closeMenus();", 500);	//TODO fix; use local function with closure; place variables in W3Compiler build script so they won't get mangled
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
	var menu=getDescendantElementByClassName(event.currentTarget, STYLES.MENU_CHILDREN_REGEXP);	//get the menu below us TODO use a constant
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
	var menu=getDescendantElementByClassName(event.currentTarget, STYLES.MENU_CHILDREN_REGEXP);	//get the menu below us
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
	if(!dragState)	//if there's a drag state, stay with that one (e.g. the mouse button might have been released outside the document on Mozilla)
	{
		var dragHandle=event.target;	//get the target of the event
			//TODO make sure this isn't the context mouse button
//TODO del alert("checking to start drag");
		var dragSource=getAncestorElementByClassName(dragHandle, STYLES.DRAG_SOURCE);	//determine which element to drag
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
		var dropTarget=getDropTarget(event.clientX, event.clientY);	//get the drop target under the mouse
		if(dropTarget)	//if the mouse was dropped over a drop target
		{
//TODO del when works alert("over drop target: "+dropTarget.nodeName);
			var dragSourceComponent=getAncestorElementByClassName(dragState.dragSource, "component");	//get the component element TODO improve all this; decide if we want the dropTarget style on the component element or the drop target subcomponent, and how we want to relate that to the component ID
			var dropTargetComponent=getAncestorElementByClassName(dropTarget, "component");	//get the component element TODO improve all this; decide if we want the dropTarget style on the component element or the drop target subcomponent, and how we want to relate that to the component ID
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
		var slider=getAncestorElementByClassName(thumb, /^sliderControl-[xy]-(ltr|rtl)$/);	//find the slider TODO use a constant
		var track=getAncestorElementByClassName(thumb, /^sliderControl-[xy]-(ltr|rtl)-track$/);	//find the slider track TODO use a constant
		var positionID=slider.id+".position";	//TODO use constant
		var positionInput=document.getElementById(positionID);	//get the position element		
		if(slider && track && positionInput)	//if we found the slider and the slider track
		{
			var isHorizontal=hasClassName(track, /^sliderControl-x-(ltr|rtl)-track$/);	//see if this is a horizontal slider
			dragState=new DragState(thumb, event.clientX, event.clientY);	//create a new drag state
			dragState.dragCopy=false;	//drag the actual element, not a copy
			if(isHorizontal)	//if this is a horizontal slider
			{
				dragState.allowY=false;	//only allow horizontal dragging
				var min=0;	//calculate the minimum
				var max=track.offsetWidth-thumb.width+1;	//calculate the maximum
				dragState.minX=min;	//set the minimum
				dragState.maxX=max;	//set the maximum
			}
			else	//if this is a vertical slider
			{
				dragState.allowX=false;	//only allow vertical dragging
				var min=0;	//calculate the minimum
				var max=track.offsetHeight-thumb.height+1;	//calculate the maximum
				dragState.minY=min;	//set the minimum
				dragState.maxY=max;	//set the maximum
			}
			var span=max-min;	//find the available range of the values
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
						var ajaxRequest=new FormAJAXEvent(new Parameter(positionInput.name, position.toString()));	//create a new form request with the control name and value
						guiseAJAX.sendAJAXRequest(ajaxRequest);	//send the AJAX request
					};
			dragState.onDragEnd=function(element)	//when dragging ends, update the slider view to make sure it is synchronized with the updated value
					{
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
This implementation also sets the thumb.width and thumb.height to work around a Mozilla bug that doesn't properly calculate thumb.offsetWidth and thumb.offsetHeight if the thumb is partially outside the track.
@param slider The slider element.
*/
function updateSlider(slider)	//TODO maybe rename to updateSliderView
{
	var track=getDescendantElementByClassName(slider, /^sliderControl-[xy]-(ltr|rtl)-track$/);	//find the slider track TODO use a constant
	var thumb=getDescendantElementByClassName(slider, /^sliderControl-[xy]-(ltr|rtl)-thumb$/);	//find the slider thumb TODO use a constant
	if(dragState && dragState.dragging && dragState.dragSource==thumb)	//if the slider thumb is being dragged
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
		if(typeof thumb.width=="undefined")	//if we haven't defined the thumb width
		{
			thumb.width=thumb.offsetWidth;	//set the thumb width so that it won't change later with the Mozilla bug if the thumb is partially outside the track
		}
		if(typeof thumb.height=="undefined")	//if we haven't defined the thumb height
		{
			thumb.height=thumb.offsetHeight;	//set the thumb height so that it won't change later with the Mozilla bug if the thumb is partially outside the track
		}
		var position=positionInput.value ? parseFloat(positionInput.value) : 0;	//get the position TODO make sure this logic is in synch with whether server code will always provide a value, even for null
		var isHorizontal=hasClassName(track, /^sliderControl-x-(ltr|rtl)-track$/);	//see if this is a horizontal slider
		if(isHorizontal)	//if this is a horizontal slider
		{
			var min=0;	//calculate the minimum
//TODO del alert("track width: "+track.offsetWidth);
//TODO del alert("thumb width: "+thumb.offsetWidth);
			var max=track.offsetWidth-thumb.width+1;	//calculate the maximum
		}
		else	//if this is a vertical slider
		{
			var min=0;	//calculate the minimum
			var max=track.offsetHeight-thumb.height+1;	//calculate the maximum
			position=1.0-position;	//take into account that the vertical slider origin is the opposite of the graphics origin
		}
		var span=max-min;	//find the available range of the values
		var newCoordinate=Math.round(position*span+min);	//determine the new coordinate
//TODO del when works alert("new coordinate for "+slider.id+" is "+newCoordinate);
		if(isHorizontal)	//if this is a horizontal slider
		{
			thumb.style.left=newCoordinate+"px";	//update the horizontal position of the slider
		}
		else	//if this is a vertical slider
		{
			thumb.style.top=newCoordinate+"px";	//update the vertical position of the slider
		}
//TODO del		alert("ready to update slider "+slider.id+" with value:"+positionInput.value);
	}
}

/**Determines the drop target at the given coordinates.
@param x The horizontal test position.
@param y The vertical test position.
@return The drop target at the given coordinates, or null if there is no drop target at the given coordinates.
*/
function getDropTarget(x, y)
{
	for(var i=dropTargets.length-1; i>=0; --i)	//for each drop target (which have been sorted by increasing element depth)
	{
		var dropTarget=dropTargets[i];	//get this drop target
		var dropTargetCoordinates=getElementFixedCoordinates(dropTarget);	//get the coordinates of the drop target
		if(x>=dropTargetCoordinates.x && y>=dropTargetCoordinates.y && x<dropTargetCoordinates.x+dropTarget.offsetWidth && y<dropTargetCoordinates.y+dropTarget.offsetHeight)	//if the coordinates are within the drop target area
		{
			return dropTarget;	//we've found the deepest drop target
		}
	}
}

/**Retrieves the ancestor form of the given node, starting at the node itself.
@param node The node the form of which to find, or null if the search should not take place.
@return The form in which the node lies, or null if the node is not within a form.
*/
function getForm(node)
{
	var form=getAncestorElementByName(node, "form");	//get the form ancestor
	if(form==null)	//if there is no form ancestor (e.g. the node is a frame outside the form)
	{
		form=getDescendantElementByName(document.documentElement, "form");	//search the whole document for the form
	}
	return form;	//return the form we found, if any
}

/**Retrieves the ancestor menu element of the node, starting at the node itself.
@param node The node the ancestor of which to find, or null if the search should not take place.
@return The menu ancestor, or null if there is no menu ancestor.
*/
function getMenu(node)	//TODO rename method when works
{
	return getAncestorElementByClassName(node, STYLES.MENU_BODY_REGEXP);	//TODO comment; use a constant
}

/**Retrieves the named ancestor element of the given node, starting at the node itself.
@param node The node the ancestor of which to find, or null if the search should not take place.
@param elementName The name of the element to find.
@return The named element in which the node lies, or null if the node is not within such a named element.
*/
function getAncestorElementByName(node, elementName)
{
	while(node && (node.nodeType!=Node.ELEMENT_NODE || node.nodeName.toLowerCase()!=elementName))	//while we haven't found the named element
	{
		node=node.parentNode;	//get the parent node
	}
	return node;	//return the element we found
}

/**Retrieves the ancestor element with the given class of the given node, starting at the node itself. Multiple class names are supported.
@param node The node the ancestor of which to find, or null if the search should not take place.
@param className The name of the class for which to check, or a regular expression if a match should be found.
@return The element with the given class in which the node lies, or null if the node is not within such an element.
*/
function getAncestorElementByClassName(node, className)
{
	while(node)	//while we haven't reached the top of the hierarchy
	{
		if(node.nodeType==Node.ELEMENT_NODE && hasClassName(node, className))	//if this is an element and this class name is one of the class names
		{
			return node;	//this node has a matching class name; we'll use it
		}
		node=node.parentNode;	//try the parent node
	}
	return node;	//return whatever node we found
}

/**Retrieves the descendant element with the given name and attributes, starting at the node itself.
Currently this function only accepts a single parameter specification.
@param node The node the descendant of which to find, or null if the search should not take place.
@param elementName The name of the element to find.
@param parameters (...) Zero or more parameters of type Parameter, each representing an attribute name and value that should be present (or, if the parameter value is null, an attribute that must not be present).
@return The element with the given name, or null if there is no such element descendant.
*/
function getDescendantElementByName(node, elementName, parameters)
{
	if(node)	//if we have a node
	{
		var argumentCount=arguments.length;	//find out how many arguments there are
		if(node.nodeType==Node.ELEMENT_NODE && node.nodeName.toLowerCase()==elementName)	//if this is an element with the given name
		{
			var parametersMatch=true;	//start out assuming that the parameters match		
			for(var i=2; i<argumentCount && parametersMatch; ++i)	//for each argument (not counting the first two), as long as all parameters match so far
			{
				var parameter=arguments[i];	//get this argument
				if(node.getAttribute(parameter.name)!=parameter.value)	//if this attribute doesn't exist or doesn't have the correct value
				{
					parametersMatch=false;	//indicate that the parameters do not match
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
			if(argumentCount==2)	//if there are only two arguments
			{
				var match=getDescendantElementByName(childNode, elementName);	//see if we can find the node in this branch
			}
			else	//if there are more arguments
			{
				var match=getDescendantElementByName(childNode, elementName, parameters);	//see if we can find the node in this branch TODO fix passing more than one parameter
			}
			if(match)	//if we found a match
			{
				return match;	//return it
			}
		}
	}
	return null;	//show that we didn't find a matching element
}

/**Retrieves the descendant element with the given class of the given node, starting at the node itself. Multiple class names are supported.
@param node The node the descendant of which to find, or null if the search should not take place.
@param className The name of the class for which to check, or a regular expression if a match should be found.
@return The element with the given class for which the given node is a parent or itself, or null if there is no such element descendant.
*/
function getDescendantElementByClassName(node, className)
{
	if(node)	//if we have a node
	{
		if(node.nodeType==Node.ELEMENT_NODE && hasClassName(node, className))	//if this is an element with the given class name
		{
			return node;	//show that we found a matching element class name
		}
		var childNodeList=node.childNodes;	//get all the child nodes
		var childNodeCount=childNodeList.length;	//find out how many children there are
		for(var i=0; i<childNodeCount; ++i)	//for each child node
		{
			var childNode=childNodeList[i];	//get this child node
			var match=getDescendantElementByClassName(childNode, className);	//see if we can find the node in this branch
			if(match)	//if we found a match
			{
				return match;	//return it
			}
		}
	}
	return null;	//show that we didn't find a matching element
}

/**Determines whether the given element has the given class. Multiple class names are supported.
@param element The element that should be checked for class.
@param className The name of the class for which to check, or a regular expression if a match should be found.
@return true if one of the element's class names equals the given class name.
*/
function hasClassName(element, className)
{
	var classNamesString=element.className;	//get the element's class names
	var classNames=classNamesString ? classNamesString.split(/\s/) : EMPTY_ARRAY;	//split out the class names
	return className instanceof RegExp ? classNames.containsMatch(className) : classNames.contains(className);	//return whether this class name is one of the class names
}

/**Determines whether the given element has the given class, using DOM methods. Multiple class names are supported.
@param element The element that should be checked for class.
@param className The name of the class for which to check, or a regular expression if a match should be found.
@return true if one of the element's class names equals the given class name.
*/
function hasClass(element, className)
{
	var classNamesString=element.getAttribute("class");	//get the element's class names
	var classNames=classNamesString ? classNamesString.split(/\s/) : EMPTY_ARRAY;	//split out the class names
	return className instanceof RegExp ? classNames.containsMatch(className) : classNames.contains(className);	//return whether this class name is one of the class names
}

/**Determines whether the given node has the indicated ancestor, including the node itself in the search.
@param node The node the ancestor of which to find, or null if the search should not take place.
@param ancestor The ancestor to find.
@return true if the node or one of its ancestors is the given ancestor.
*/
function hasAncestor(node, ancestor)
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
}

/**Determines the document tree depth of the given element, returning a zero-level depth for the document node.
@param element The element for which a depth should be found.
@return The zero-based depth of the given element in the document, with a zero-level depth for the document node.
*/
function getElementDepth(element)
{
	var depth=-1;	//this element will be at least depth zero
	do
	{
		element=element.parentNode;	//get the parent node
		++depth;	//increase the depth
	}
	while(element);	//keep getting the parent node while there are ancestors left
	return depth;	//return the depth we calculated
}

/**Retrieves the immediate text nodes of the given element as a string.
@param element The element from which to retrieve text.
@return The text of the given element.
*/ 
function getText(element)
{
	var childNodeList=element.childNodes;	//get all the child nodes of the element
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
	var nodeName=node.nodeName.toLowerCase();
	if(FOCUSABLE_ELEMENT_NAMES.contains(nodeName))	//if this is a focusable node
	{
		if((!node.style || node.style.visibility!="hidden") && !node.disabled)	//make sure the node is not hidden or disabled
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

/**Retrieves the absolute X and Y coordinates of the given element.
@param The element the coordinates of which to find.
@return A Point containing the coordinates of the element.
@see http://www.oreillynet.com/pub/a/javascript/excerpt/JSDHTMLCkbk_chap13/index6.html
@see http://www.quirksmode.org/js/findpos.html
*/
function getElementCoordinates(element)	//TODO make sure this method correctly calculates margins and padding, as Mozilla and IE both show slight variations for text, but not for images
{
	var x=0, y=0;
	if(element.offsetParent)	//if element.offsetParent is supported
	{
		while(element)	//while we have an element
		{
			x+=element.offsetLeft;	//add this element's offsets
			y+=element.offsetTop;
			element=element.offsetParent;	//go to the element's parent offset
		}
/*TODO fix for Mac
    if (navigator.userAgent.indexOf("Mac") != -1 && 
        typeof document.body.leftMargin != "undefined") {
        offsetLeft += document.body.leftMargin;
        offsetTop += document.body.topMargin;
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
	var scrollCoordinates=getScrollCoordinates();	//get the viewport's scroll coordinates
	return new Point(absoluteCoordinates.x-scrollCoordinates.x, absoluteCoordinates.y-scrollCoordinates.y);	//compensate for viewport scrolling
}

/**@return The size of the document, even if it is outside the viewport.
@see http://www.quirksmode.org/viewport/compatibility.html 
*/
function getPageSize()
{
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
}

/**@return The coordinates that the page has scrolled.
@see http://www.quirksmode.org/viewport/compatibility.html 
*/
function getScrollCoordinates()
{
	var x, y;
	if(window.pageYOffset) //if we know the page vertical offset (all browsers except IE)
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
}

/**@return The size of the viewport.
@see http://www.quirksmode.org/viewport/compatibility.html 
*/
function getViewportSize()
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

eventManager.addEvent(window, "load", onWindowLoad, false);	//do the appropriate initialization when the window loads
