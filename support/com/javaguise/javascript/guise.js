//Guise(TM) JavaScript support routines
//Copyright (c) 2005 GlobalMentor, Inc.
//Modal window support referenced code by Danny Goodman, http://www.dannyg.com/ .

//TODO add node types for IE from Professional JavaScript page 169

//Array

/**An enqueue() method for arrays, equivalent to Array.push().*/
Array.prototype.enqueue=Array.prototype.push;

/**A dequeue() method for arrays, equivalent to Array.shift().*/
Array.prototype.dequeue=Array.prototype.shift;

//Node

if(typeof Node=="undefined")	//if no Node type is defined (e.g. IE), create one to give us constant node types
{
	var Node=
			{
				ELEMENT_NODE: 1,
				ATTRIBUTE_NODE: 2,
				TEXT_NODE: 3,
				CDATA_SECTION_NODE: 4,
				ENTITY_REFERENCE_NODE: 5,
				ENTITY_NODE: 6,
				PROCESSING_INSTRUCTION_NODE: 7,
				COMMENT_NODE: 8,
				DOCUMENT_NODE: 9,
				DOCUMENT_TYPE_NODE: 10,
				DOCUMENT_FRAGMENT_NODE: 11,
				NOTATION_NODE: 12
			}
}


function HTTPCommunicator()
{
//	this.XXX=xxx;
}

/**The versions of the Microsoft XML HTTP ActiveX objects, in increasing order of preference.*/
HTTPCommunicator.prototype.MSXMLHTTP_VERSIONS=["Microsoft.XMLHTTP", "MSXML2.XMLHTTP", "MSXML2.XMLHTTP.3.0", "MSXML2.XMLHTTP.4.0", "MSXML2.XMLHTTP.5.0", "MSXML2.XMLHTTP.6.0", "MSXML2.XMLHTTP.7.0"];

HTTPCommunicator.prototype.createXMLHTTP=function()
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
}

HTTPCommunicator.prototype.addParameter=function(name, value)
{
	if(!this.parameters)	//if there are no parameters
	{
		this.parameters=new Array();	//create a new parameters array	
	}
	this.parameters.push(encodeURIComponent(name)+"="+encodeURIComponent(value));	//add another parameter to the array
}

HTTPCommunicator.prototype.get=function(uri)
{
	return this._performRequest("GET", uri);	//perform a GET request
}

HTTPCommunicator.prototype.post=function(uri)
{
	return this._performRequest("POST", uri);	//perform a POST request
}

/**
@return The text of the response or, if the response provides an XML DOM tree, the XML document object.
*/
HTTPCommunicator.prototype._performRequest=function(method, uri)
{
	var xmlHTTP=this.createXMLHTTP();	//create an XML HTTP object
	if("GET"==method && this.parameters && this.parameters.length>0)	//if there are parameters for the GET method
	{
		uri=uri+"?"+this.parameters.join("&");	//add the parameters to the URI
	}
	xmlHTTP.open(method, uri, false);
	var content=null;	//we'll create content if we need to
	if("POST"==method)	//if this is the POST method
	{
		xmlHTTP.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");	//set the post content type
		if(this.parameters && this.parameters.length>0)	//if there are parameters for the POST method
		{
			content=this.parameters.join("&");	//create the content from the parameters
		}	
	}	
	xmlHTTP.send(content);	//send the request
	this.parameters=null;	//remove any parameters
//TODO del alert("got status: "+xmlHTTP.status);
//TODO del alert("response text: "+xmlHTTP.responseText);
	return xmlHTTP.responseXML ? xmlHTTP.responseXML : xmlHTTP.responseText;
}

/**Adds an event listener to an object.
@param object The object for which a listener should be added.
@param eventType The type of event.
@param fn The function to listen for the event.
@param useCapture Whether event capture should be used.
@see http://www.scottandrew.com/weblog/articles/cbs-events
*/
function addEvent(object, eventType, fn, useCapture)
{
	if(object.addEventListener)	//if the W3C DOM method is supported
	{
		object.addEventListener(eventType, fn, useCapture);	//add the event normally
		return true;
	}
	else	//if the W3C version isn't available
	{
		var eventName="on"+eventType;	//create the event name
		if(object.attachEvent)	//if we can use the IE version
		{
			return object.attachEvent(eventName, fn);	//attach the function
		}
		else	//if we can't use the IE version
		{
			object[eventName]=fn;	//use the object.onEvent property
		}
	}
}

/**Removes an event listener from an object.
@param object The object for which a listener should be removed.
@param eventType The type of event.
@param fn The function listening for the event.
@param useCapture Whether event capture should be used.
@see http://www.scottandrew.com/weblog/articles/cbs-events
*/
function removeEvent(object, eventType, fn, useCapture)
{
	if(object.removeEventListener)	//if the W3C DOM method is supported
	{
		object.removeEventListener(eventType, fn, useCapture);	//remove the event normally
		return true;
	}
	else	//if the W3C version isn't available
	{
		var eventName="on"+eventType;	//create the event name
		if(object.detachEvent)	//if we can use the IE version
		{
			return object.detachEvent(eventName, fn);
		}
		else	//if we can't use the IE version
		{
			object[eventName]=null;	//use the object.onEvent property
		}
  }
}

/**Adds a function to be called when a window loads.
@param func The function to listen for window loading.
@see http://simon.incutio.com/archive/2004/05/26/addLoadEvent
*/
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
		}
	}
}

/**Retrieves W3C event information in a cross-browser manner.
@param event The event information, or null if no event information is available (e.g. on IE).
@return A W3C-compliant event object.
*/
function getEvent(event)
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
	if(!event.data)	//if there is no data
	{
		//TODO assert event.keyCode
		event.data=event.keyCode;	//assign a W3C data property
	}
	return event;	//return our event
}

//Guise functionality

var httpCommunicator=new HTTPCommunicator();

/**The queue of AJAX response XML DOM trees.*/
var ajaxResponses=new Array();

/**Called when the window loads.
This implementation installs listeners.
*/
function onWindowLoad()
{
	installListeners();
}

/**Installs listeners on the appropriate controls.*/
function installListeners()
{
//TODO del	alert("installing listeners");
	var inputElementList=document.getElementsByTagName("input");	//get all input elements
	for(var i=0; i<inputElementList.length; ++i)	//for each input element
	{
		var inputElement=inputElementList[i];	//get this input element
		var inputType=inputElement.type;	//get the type of input
		if("text"==inputType || "password"==inputType)	//if this is a text control
		{
//TODO del alert("found text input element "+inputElement.id);
			addEvent(inputElement, "change", onTextInputChange, false);
		}
		else if("checkbox"==inputType || "radio"==inputType)	//if this is a checkbox or a radio button
		{
//TODO del alert("found text input element "+inputElement.id);
			addEvent(inputElement, "click", onCheckInputChange, false);
		}
	}
}

function onTextInputChange(event)
{
	var w3cEvent=getEvent(event);	//get the W3C event object
	var target=w3cEvent.target;	//get the target of the event
//TODO del alert("an input changed! "+target.id);
	httpCommunicator.addParameter("navigationPath", "test");	//TODO fix; testing
	httpCommunicator.addParameter(target.name, target.value);
	var ajaxXML=httpCommunicator.post("_ajax");
	ajaxResponses.enqueue(ajaxXML);	//enqueue the response XML
	setTimeout("processAJAXResponses();", 1);	//process the AJAX responses later
}

function onCheckInputChange(event)
{
	var w3cEvent=getEvent(event);	//get the W3C event object
	var target=w3cEvent.target;	//get the target of the event
//TODO del alert("checkbox "+target.id+" changed to "+target.checked);
	httpCommunicator.addParameter("navigationPath", "test");	//TODO fix; testing
	httpCommunicator.addParameter(target.name, target.checked ? target.id : "");
	var ajaxXML=httpCommunicator.post("_ajax");
	ajaxResponses.enqueue(ajaxXML);	//enqueue the response XML
	setTimeout("processAJAXResponses();", 1);	//process the AJAX responses later
}

/**Processes responses from AJAX requests.
This routine should be called asynchronously from an event so that the DOM tree can be successfully updated.
@see ajaxResponses
*/
function processAJAXResponses()
{
	while(ajaxResponses.length>0)	//while there are more AJAX responses
	{
		weaveAjaxElement(ajaxResponses.dequeue().documentElement);
	}
}

/**Weaves an element and its children into the existing element hierarchy.
Any element in the hierarchy without an ID attribute will be ignored, although its children will be processed.
@param element The element hierarchy to weave into the existing document.
*/ 
function weaveAjaxElement(element)
{
	var id=element.getAttribute("id");	//get the element's ID, if there is one
	if(id)	//if the element has an ID
	{
		var oldElement=document.getElementById(id);	//get the old element
		if(oldElement)	//if the element currently exists in the document
		{
				//remove any attributes the old element has that are not in the new element
			var oldAttributes=oldElement.attributes;	//get the old element's attributes
			for(var i=oldAttributes.length-1; i>=0; --i)	//for each old attribute
			{
				var attribute=oldAttributes[i];	//get this attribute
				var attributeName=attribute.nodeName;	//get the attribute name
				var attributeValue=attribute.nodeValue;	//get the attribute value
				if(/*TODO fix or del attributeValue!=null && attributeValue.length>0 && */!element.getAttribute(attributeName))	//if there is really an attribute value (IE provides all possible attributes, even with those with no value) and the new element doesn't have this attribute
				{
//TODO del alert("ready to remove "+id+" attribute "+attributeName+" with current value "+attributeValue);
					oldElement.removeAttribute(attributeName);	//remove the attribute normally TODO make sure this works in IE
//TODO fix					i=0;	//TODO fix; temporary to get out of looking at all IE's attributes
				}
			}
			var attributes=element.attributes;	//get the new element's attributes
			for(var i=attributes.length-1; i>=0; --i)	//for each attribute
			{
				var attribute=attributes[i];	//get this attribute
//TODO del alert("looking at attribute: "+attribute.nodeName);
				var attributeName=attribute.nodeName;	//get the attribute name
				var attributeValue=attribute.nodeValue;	//get the attribute value
					//TODO fix for oldElement.class/oldElement.className on IE
				if(oldElement.getAttribute(attributeName)!=attributeValue)	//if the old element has a different (or no) value for this attribute
				{
//TODO del alert("updating "+id+" attribute "+attributeName+" to new value "+attributeValue);
//TODO del when works setTimeout("setElementAttribute('"+id+"', '"+attributeName+"', '"+attributeValue+"');", 1);	//TODO del; testing
					oldElement[attributeName]=attributeValue;	//update the old element's attribute (this format works for Firefox where oldElement.setAttribute("value", attributeValue) does not)
/*TODO del
if("disabled"==attributeName)
{
alert("added disabled value: "+attributeValue+" which registered as "+oldElement.getAttribute(attributeName));
}
*/
/*TODO fix when works
					if(attributeName=="value")	//TODO find out why this works on IE but not on Firefox
					{
						oldElement.value=attributeValue;	//update the old element's attribute
					}
					else
					{
							//TODO figure out why IE wants to use a different name for class attributes
						oldElement.setAttribute(attributeName, attributeValue);	//update the old element's attribute
					}
*/
//TODO fix						oldElement.setAttribute(attributeName, id);	//update the old element's attribute
//TODO fix					oldElement.setAttribute(attributeName, attributeValue);	//update the old element's attribute


//TODO: fix the Firefox problem of sending an onchange event for any elements that get updated from an Ajax request, but only later when the focus blurs
//TODO fix the focus problem if the user has focus on an element that gets changed in response to the event

				}
			}
			//TODO remove all attributes from the old element that have been deleted; maybe doing this first will result in efficiencies in some cases
		}
	}
	var childNodeList=element.childNodes;	//get all the child nodes of the element
	for(var i=0; i<childNodeList.length; ++i)	//for each child node
	{
		var childNode=childNodeList[i];	//get this child node
		if(childNode.nodeType==Node.ELEMENT_NODE)	//if this is a child node
		{
			weaveAjaxElement(childNode);	//weave this child element
		}
	}
}

addEvent(window, "load", onWindowLoad, false);	//do the appropriate initialization when the window loads

var Nav4 = ((navigator.appName == "Netscape") && (parseInt(navigator.appVersion) >= 4));
var modalState=new Object();	//the state of modality

function openModalWindow(url)
{
	modalState.url=url;	//save the modal URL
	if(!modalState.window || (modalState.window && modalState.window.closed))
	{
		modalState.width = screen.availWidth*2/3;
    modalState.height = screen.availHeight*2/3;
		if(Nav4)
		{
         // Center on the main window.
         modalState.left = window.screenX + ((window.outerWidth - modalState.width) / 2);
         modalState.top = window.screenY + ((window.outerHeight - modalState.height) / 2);
         var modalAttributes = "screenX=" + modalState.left + ",screenY=" + modalState.top + ",resizable=no,width=" + modalState.width + ",height=" + modalState.height;
      }
      else
      {
         // The best we can do is center in screen.;
         modalState.left = (screen.width - modalState.width) / 2;
         modalState.top = (screen.height - modalState.height) / 2;
         var modalAttributes = "left=" + modalState.left + ",top=" + modalState.top + ",resizable=no,width=" + modalState.width + ",height=" + modalState.height;
      }
      modalState.name = (new Date()).getSeconds().toString()      
      // Generate the dialog and make sure it has focus.
		modalState.window=window.open(url, modalState.name, modalAttributes);
		modalState.window.onclose=endModal;
//TODO fix for IE		modalState.window=showModalDialog(url);
		modalState.window.focus();
	}
	window.onfocus=recoverModality;	//recover modality if the original window gets focus
	installNodeModalRecovery(window.document);	//install modal recovery for the entire document tree

	for(var formIndex=0; formIndex<window.document.forms.length; ++formIndex)	//for all the forms
	{
		for(var elementIndex=0; elementIndex<window.document.forms[formIndex].elements.length; elementIndex++)	//for all the elements on each form
		{
			window.document.forms[formIndex].elements[elementIndex].onfocus=recoverModality;	//recover modality for focus
			window.document.forms[formIndex].elements[elementIndex].onclick=recoverModality;	//recover modality for clicks
		}
	}
}

function installNodeModalRecovery(node)
{
	try
	{
		node.onfocus=recoverModality;
		node.onfocus=recoverModality;
	}
	catch(exception)
	{
	}	
	for(var i=node.childNodes.length-1; i>=0; --i)
	{
		installNodeModalRecovery(node.childNodes[i]);
	}
}

function recoverModality()
{
	if(modalState.window)	//if we've been modal before
	{
		if(modalState.window.closed)	//if the modal window is closed
		{
		}
		else
		{
			modalState.window.focus();
		}
	}
}

//Ends modality for a modal window. It is assumed that the window is being closed.
function endModal()
{
//	if(opener && !opener.closed)	//if the window has an open opener
	{
//		endModal(opener);	//end modality for the opener
	}
}

//Ends modality for the given opener
function endModal(opener)
{
//	alert("modality ended for opener");
}

function test()
{
	setTimeout("openModalWindow('test.html')", 5000);
	openModalWindow("test.html");
}

var menu=new Object();

function closeMenu()
{
	if(menu.closeTimeout)	//if there is a timer running
	{
		clearTimeout(menu.closeTimeout);	//clear the timer
		menu.closeTimeout=null;	//release the timer
	}
	if(menu.closeID)	//if a menu was being closed
	{
		document.getElementById(menu.closeID).style.visibility="hidden";	//close the menu
		menu.closeID=null;	//indicate that no menu is closing
	}
}

function onMenuMouseOver(id)
{
	if(menu.closeID)	//if a menu is closing
	{
		if(menu.closeID==id)	//if this menu is closing
		{
			menu.closeID=null;	//quickly stop our menu from closing
			if(menu.closeTimeout)	//if there's a timer running
			{
				clearTimeout(menu.closeTimeout);	//clear the timer
				menu.closeTimeout=null;	//release the timer
			}
		}
		else	//if another menu is closing
		{
			closeMenu();	//go ahead and close it now
		}
	}
	document.getElementById(id).style.visibility="visible";	//show this menu
}

function onMenuMouseOut(id)
{
	menu.closeID=id;	//show that this menu should be closed
	menu.timeout=window.setTimeout('closeMenu()', 1000);	//close the menu after a pause
}