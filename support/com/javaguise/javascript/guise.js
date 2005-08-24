//Guise(TM) JavaScript support routines
//Copyright (c) 2005 GlobalMentor, Inc.
//Modal window support referenced code by Danny Goodman, http://www.dannyg.com/ .
//
//var AJAX_URI: The URI to use for AJAX communication, or null/undefined if AJAX communication should not occur.

//TODO turn off AJAX when unloading

//Array

/**An enqueue() method for arrays, equivalent to Array.push().*/
Array.prototype.enqueue=Array.prototype.push;

/**A dequeue() method for arrays, equivalent to Array.shift().*/
Array.prototype.dequeue=Array.prototype.shift;

//Node

if(typeof Node=="undefined")	//if no Node type is defined (e.g. IE), create one to give us constant node types
{
	var Node={ELEMENT_NODE: 1, ATTRIBUTE_NODE: 2, TEXT_NODE: 3, CDATA_SECTION_NODE: 4, ENTITY_REFERENCE_NODE: 5, ENTITY_NODE: 6, PROCESSING_INSTRUCTION_NODE: 7, COMMENT_NODE: 8, DOCUMENT_NODE: 9, DOCUMENT_TYPE_NODE: 10, DOCUMENT_FRAGMENT_NODE: 11, NOTATION_NODE: 12}
}

//HTTPRequestInfo

/**A class encapsulating HTTP request information.
var parameters: An array of encoded name=value parameters.
*/
function HTTPRequestInfo()
{
}

/**Adds a parameter to the HTTP request.
@param name The name of the parameter.
@param value The value of the parameter.
*/
HTTPRequestInfo.prototype.addParameter=function(name, value)
{
	if(!this.parameters)	//if there are no parameters
	{
		this.parameters=new Array();	//create a new parameters array	
	}
	this.parameters.push(encodeURIComponent(name)+"="+encodeURIComponent(value));	//add another parameter to the array
}

//HTTP Communicator

/**A class encapsulating HTTP communication functionality.
This class creates a shared HTTPCommunicator.prototype.xmlHTTP variable, necessary for the onreadystate() callback function.
function processHTTPResponse: A reference to a function to call for asynchronous HTTP requests, or null if HTTP communication should be synchronous.
*/
function HTTPCommunicator()
{
//TODO fix flag with another variable	if(typeof HTTPCommunicator.prototype.xmlHTTP=="undefined")	//if there isn't yet a global XML HTTP request object

	/**@return true if the commmunicator is in the process of communicating.*/
	HTTPCommunicator.prototype.isCommunicating=function()
	{
		return typeof HTTPCommunicator.prototype.xmlHTTP!="undefined" && HTTPCommunicator.prototype.xmlHTTP!=null;	//we're communicating if we have an XML HTTP request object
	}	

	/**The enumeration of ready states for asynchronous XMLHTTP requests.*/
	//TODO del if not needed HTTPCommunicator.prototype.READY_STATE={UNINITIALIZED: 0, LOADING: 1, LOADED: 2, INTERACTIVE: 3, COMPLETED: 4}
	
	/**The versions of the Microsoft XML HTTP ActiveX objects, in increasing order of preference.*/
	HTTPCommunicator.prototype.MSXMLHTTP_VERSIONS=["Microsoft.XMLHTTP", "MSXML2.XMLHTTP", "MSXML2.XMLHTTP.3.0", "MSXML2.XMLHTTP.4.0", "MSXML2.XMLHTTP.5.0", "MSXML2.XMLHTTP.6.0", "MSXML2.XMLHTTP.7.0"];
	
	/**Sets the callback method to use for processing an HTTP response.
	The callback function is set for the HTTP communicator prototype, shared among all instances of the HTTP communicator.
	@param fn The function to call when processing HTTP responses, or null if requests should be synchronous.
	*/
	HTTPCommunicator.prototype.setProcessHTTPResponse=function(fn)
	{
//TODO del	alert("setting up a callback using function: "+typeof fn);
		HTTPCommunicator.prototype.processHTTPResponse=fn;	//save the function for processing HTTP responses
	}
	
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
	}
	
	/**Performs an HTTP GET request.
	@param uri The request URI.
	@param httpRequestInfo The HTTP request information, or null if there is no related information.
	*/
	HTTPCommunicator.prototype.get=function(uri, httpRequestInfo)
	{
		return this._performRequest("GET", uri, httpRequestInfo);	//perform a GET request
	}
	
	/**Performs an HTTP POST request.
	@param uri The request URI.
	@param httpRequestInfo The HTTP request information, or null if there is no related information.
	*/
	HTTPCommunicator.prototype.post=function(uri, httpRequestInfo)
	{
		return this._performRequest("POST", uri, httpRequestInfo);	//perform a POST request
	}
	
	/**Performs an HTTP request and returns the result.
	@param The HTTP request method.
	@param uri The request URI.
	@param httpRequestInfo The HTTP request information, or null if there is no related information.
	@return The text of the response or, if the response provides an XML DOM tree, the XML document object; or null if the request is asynchronous.
	@throws Exception if an error occurs performing the request.
	@throws Number if the HTTP response code was not 200 (OK).
	*/
	HTTPCommunicator.prototype._performRequest=function(method, uri, httpRequestInfo)
	{
			//TODO assert HTTPCommunicator.prototype.xmlHTTP does not exist
		HTTPCommunicator.prototype.xmlHTTP=HTTPCommunicator.prototype._createXMLHTTP();	//create an XML HTTP object
	
		var xmlHTTP=HTTPCommunicator.prototype.xmlHTTP;	//put the XML HTTP object in a local variable
		if("GET"==method && httpRequestInfo && httpRequestInfo.parameters && httpRequestInfo.parameters.length>0)	//if there are parameters for the GET method
		{
			uri=uri+"?"+httpRequestInfo.parameters.join("&");	//add the parameters to the URI
		}
//TODO del	alert("typeof HTTPCommunicator.prototype.processHTTPResponse: "+HTTPCommunicator.prototype.processHTTPResponse);
		var asynchronous=typeof HTTPCommunicator.prototype.processHTTPResponse!="undefined" && typeof HTTPCommunicator.prototype.processHTTPResponse!=null;	//see if we should make an asynchronous request
		
		HTTPCommunicator.prototype.xmlHTTP.onreadystatechange=function()
		{
			if(HTTPCommunicator.prototype.xmlHTTP.readyState==4)	//if a transfer is completed
			{
//TODO del	alert("new state: "+HTTPCommunicator.prototype.xmlHTTP.readyState);
				HTTPCommunicator.prototype.status=HTTPCommunicator.prototype.xmlHTTP.status;	//store the status in the communicator
				HTTPCommunicator.prototype.responseText=HTTPCommunicator.prototype.xmlHTTP.responseText;	//store the response text in the communicator
				HTTPCommunicator.prototype.responseXML=HTTPCommunicator.prototype.xmlHTTP.responseXML;	//store the response XML in the communicator
				HTTPCommunicator.prototype.xmlHTTP=null;	//remove the XML HTTP request object (Firefox only allows one asynchronous communication per object)
				HTTPCommunicator.prototype.processHTTPResponse();	//process the response
			}
		}
		xmlHTTP.open(method, uri, asynchronous);
		var content=null;	//we'll create content if we need to
		if("POST"==method)	//if this is the POST method
		{
			xmlHTTP.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");	//set the post content type
			if(httpRequestInfo && httpRequestInfo.parameters && httpRequestInfo.parameters.length>0)	//if there are parameters for the POST method
			{
				content=httpRequestInfo.parameters.join("&");	//create the content from the parameters
			}	
		}
		xmlHTTP.send(content);	//send the request
		if(asynchronous)	//if we're communicating asynchronously
		{
//TODO del	alert("sending back null for asynchronous mode");
			return null;	//don't return anything to process
		}
		else	//if we're processing synchronously, process the return value
		{
	//TODO alert("got status: "+xmlHTTP.status);
	//TODO del alert("response text: "+xmlHTTP.responseText);
			HTTPCommunicator.prototype.status=HTTPCommunicator.prototype.xmlHTTP.status;	//store the status in the communicator
			HTTPCommunicator.prototype.responseText=HTTPCommunicator.prototype.xmlHTTP.responseText;	//store the response text in the communicator
			HTTPCommunicator.prototype.responseXML=HTTPCommunicator.prototype.xmlHTTP.responseXML;	//store the response XML in the communicator
			HTTPCommunicator.prototype.xmlHTTP=null;	//remove the XML HTTP request object (Firefox only allows one asynchronous communication per object)
			if(HTTPCommunicator.prototype.status==200)	//if everything went OK
			{
				return HTTPCommunicator.prototype.responseXML ? HTTPCommunicator.prototype.responseXML : HTTPCommunicator.prototype.responseText;
			}
			else	//if there was an HTTP error TODO check for redirects
			{
				throw HTTPCommunicator.prototype.status;	//throw the status code
			}
		}
	}
}


//GuiseAJAX

/**A class encapsulating AJAX functionality for Guise.*/
function GuiseAJAX()
{
	/**The object for communicating with Guise via AJAX.*/
	GuiseAJAX.prototype.httpCommunicator=new HTTPCommunicator();
	
	/**The queue of AJAX HTTP request information objects.*/
	GuiseAJAX.prototype.ajaxRequests=new Array();

	/**The queue of AJAX response XML DOM trees.*/
	GuiseAJAX.prototype.ajaxResponses=new Array();

	/**Immediately sends or queues an AJAX request.
	@param The HTTP request method.
	@param uri The request URI.
	@param httpRequestInfo The HTTP request information, or null if there is no related information.
	@return The text of the response or, if the response provides an XML DOM tree, the XML document object.
	@throws Exception if an error occurs performing the request.
	@throws Number if the HTTP response code was not 200 (OK).
	*/
	GuiseAJAX.prototype.sendAJAX=function(httpRequestInfo)
	{
		if(AJAX_URI)	//if AJAX is enabled
		{
			GuiseAJAX.prototype.ajaxRequests.enqueue(httpRequestInfo);	//enqueue the request info
			GuiseAJAX.prototype.processAJAXRequests();	//process any waiting requests now if we can
		}
	}
	
	/**Called in response to asynchronous HTTP communication.*/
	GuiseAJAX.prototype._processHTTPResponse=function()
	{
		try
		{
//TODO del alert("processing asynch result");
//TODO del alert("got status: "+HTTPCommunicator.prototype.status);
//TODO del alert("response text: "+HTTPCommunicator.prototype.responseText);
			if(HTTPCommunicator.prototype.status==200)	//if everything went OK
			{
				if(HTTPCommunicator.prototype.responseXML)
				{
					GuiseAJAX.prototype.ajaxResponses.enqueue(HTTPCommunicator.prototype.responseXML);	//enqueue the response XML
					setTimeout("GuiseAJAX.prototype.processAJAXResponses();", 1);	//process the AJAX responses later		
					GuiseAJAX.prototype.processAJAXRequests();	//make sure there are no waiting AJAX requests
				}
			}
			else	//if there was an HTTP error TODO check for redirects
			{
		//TODO fix		throw xmlHTTP.status;	//throw the status code
			}
		}
		catch(exception)	//if a problem occurred
		{
			//TODO log a warning
//TODO log alert(exception);
			AJAX_URI=null;	//stop further AJAX communication
		}
	}

	if(true)	//TODO add asynchronous configuration
	{
		this.httpCommunicator.setProcessHTTPResponse(GuiseAJAX.prototype._processHTTPResponse);	//set up our callback function for process HTTP responses
	}

	/**Processes AJAX requests.
	@see GuiseAJAX#ajaxRequests
	*/
	GuiseAJAX.prototype.processAJAXRequests=function()
	{
		//see if the communicator is not busy (if it is busy, we're in asychronous mode and the end of the processing will call this method again to check for new requests)
		while(!GuiseAJAX.prototype.httpCommunicator.isCommunicating() && GuiseAJAX.prototype.ajaxRequests.length>0)	//while the communicator is not busy and there are more AJAX requests
		{
			try
			{			
				var httpRequestInfo=GuiseAJAX.prototype.ajaxRequests.dequeue();	//post the HTTP request information
				var ajaxXML=GuiseAJAX.prototype.httpCommunicator.post(AJAX_URI, httpRequestInfo);	//post the HTTP request information
				if(ajaxXML)	//if we receive an AJAX response
				{
//TODO del	alert("processing synch result");
					GuiseAJAX.prototype.ajaxResponses.enqueue(ajaxXML);	//enqueue the response XML
					setTimeout("GuiseAJAX.prototype.processAJAXResponses();", 1);	//process the AJAX responses later
				}
			}
			catch(exception)	//if a problem occurred
			{
				//TODO log a warning
//TODO log alert(exception);
				AJAX_URI=null;	//stop further AJAX communication
			}
		}
	}

	/**Processes responses from AJAX requests.
	This routine should be called asynchronously from an event so that the DOM tree can be successfully updated.
	@see GuiseAJAX#ajaxResponses
	*/
	GuiseAJAX.prototype.processAJAXResponses=function()
	{
		while(GuiseAJAX.prototype.ajaxResponses.length>0)	//while there are more AJAX responses
		{
			patchAjaxElement(GuiseAJAX.prototype.ajaxResponses.dequeue().documentElement);
		}
		GuiseAJAX.prototype.processAJAXRequests();	//make sure there are no waiting AJAX requests
	}

}

/**The global object for AJAX communication with Guise.*/
var guiseAJAX=new GuiseAJAX();

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

/**Called when the window loads.
This implementation installs listeners if AJAX is enabled.
@see #AJAX_URI
*/
function onWindowLoad()
{
	if(AJAX_URI)	//if AJAX is enabled
	{
		installListeners();	//install listeners for appropriate elements
	}
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
		if(inputType=="text" || inputType=="password")	//if this is a text control
		{
//TODO del alert("found text input element "+inputElement.id);
			addEvent(inputElement, "change", onTextInputChange, false);
		}
		else if(inputType=="checkbox" || inputType=="radio")	//if this is a checkbox or a radio button
		{
//TODO del alert("found text input element "+inputElement.id);
			addEvent(inputElement, "click", onCheckInputChange, false);
		}
	}
	var selectElementList=document.getElementsByTagName("select");	//get all select elements
	for(var i=0; i<selectElementList.length; ++i)	//for each select element
	{
		var selectElement=selectElementList[i];	//get this select element
		addEvent(selectElement, "change", onSelectChange, false);
	}
}

function onTextInputChange(event)
{
	if(AJAX_URI)	//if AJAX is enabled
	{
		var w3cEvent=getEvent(event);	//get the W3C event object
		var textInput=w3cEvent.target;	//get the target of the event
	//TODO del alert("an input changed! "+textInput.id);
		var httpRequestInfo=new HTTPRequestInfo();	//create new HTTP request information
		httpRequestInfo.addParameter(textInput.name, textInput.value);
		guiseAJAX.sendAJAX(httpRequestInfo);	//send the AJAX request
	}
}

function onCheckInputChange(event)
{
	//TODO fix Mozilla bug that doesn't see an onclick of a checkbox label changes the checkbox
	if(AJAX_URI)	//if AJAX is enabled
	{
		var w3cEvent=getEvent(event);	//get the W3C event object
		var checkInput=w3cEvent.target;	//get the target of the event
		if(checkInput.nodeName=="label" && checkInput.htmlFor)	//if the check input's label was passed as the target (as occurs in Mozilla)
		{
			checkInput=document.getElementById(checkInput.htmlFor);	//the real target is the check input with which this label is associated; the htmlFor attribute is the ID of the element, not the actual element as Danny Goodman says in JavaScript Bible 5th Edition (649)
		}
//TODO del alert("checkbox "+checkInput.id+" changed to "+checkInput.checked);
		var httpRequestInfo=new HTTPRequestInfo();	//create new HTTP request information
		httpRequestInfo.addParameter(checkInput.name, checkInput.checked ? checkInput.id : "");
		guiseAJAX.sendAJAX(httpRequestInfo);	//send the AJAX request
	}
}

function onSelectChange(event)
{
	if(AJAX_URI)	//if AJAX is enabled
	{
		var w3cEvent=getEvent(event);	//get the W3C event object
		var select=w3cEvent.target;	//get the target of the event
	//TODO del alert("a select changed! "+select.id);
		var options=select.options;	//get the select options
		var httpRequestInfo=new HTTPRequestInfo();	//create new HTTP request information
		for(var i=0; i<options.length; ++i)	//for each option
		{
			var option=options[i];	//get this option
			if(option.selected)	//if this option is selected
			{
				httpRequestInfo.addParameter(select.name, option.value);
			}
		}
		guiseAJAX.sendAJAX(httpRequestInfo);	//send the AJAX request
	}
}

/**Patches an element and its children into the existing element hierarchy.
Any element in the hierarchy without an ID attribute will be ignored, although its children will be processed.
@param element The element hierarchy to patch into the existing document.
*/ 
function patchAjaxElement(element)
{
		//do depth-first patching, allowing us to precheck children at the same time for later patching at this level in the hierarchy
	var childNodeList=element.childNodes;	//get all the child nodes of the element
	var childNodeCount=childNodeList.length;	//find out how many children there are
	var childTextNodeCount=0;	//keep track of how many child text nodes there are
	for(var i=0; i<childNodeCount; ++i)	//for each child node
	{
		var childNode=childNodeList[i];	//get this child node
		switch(childNode.nodeType)	//see which type of child node this is
		{
			case Node.ELEMENT_NODE:	//element
				patchAjaxElement(childNode);	//patch this child element
				break;
			case Node.TEXT_NODE:	//text
				++childTextNodeCount;	//show that we found another text child
				break;
		}
	}
		//TODO make sure the Mozilla/IE attribute access functionality is robust, taking into account Mozilla's separate value structure
	var id=element.getAttribute("id");	//get the element's ID, if there is one
	if(id)	//if the element has an ID
	{
//TODO del alert("patching stuff for ID "+id);
		var oldElement=document.getElementById(id);	//get the old element
		if(oldElement)	//if the element currently exists in the document
		{
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
				if(/*TODO fix or del attributeValue!=null && attributeValue.length>0 && */!element.getAttribute(attributeName))	//if there is really an attribute value (IE provides all possible attributes, even with those with no value) and the new element doesn't have this attribute
				{
//TODO del alert("ready to remove "+id+" attribute "+attributeName+" with current value "+attributeValue);
					oldElement.removeAttribute(oldAttributeName);	//remove the attribute normally (apparently no action will take place if performed on IE-specific attributes such as element.start)
//TODO fix					i=0;	//TODO fix; temporary to get out of looking at all IE's attributes
				}
			}
				//patch in the new and changed attributes
			var attributes=element.attributes;	//get the new element's attributes
			for(var i=attributes.length-1; i>=0; --i)	//for each attribute
			{
				var attribute=attributes[i];	//get this attribute
				var attributeName=attribute.nodeName;	//get the attribute name
				var attributeValue=attribute.nodeValue;	//get the attribute value
//TODO del alert("looking at attribute "+attributeName+" with value "+attributeValue);
//TODO del alert("looking at old attribute "+attributeName+" with value "+oldElement.getAttribute(attributeName)+" other way "+oldElement[attributeName]);
					//TODO fix for oldElement.class/oldElement.className on IE
				if(oldElement[attributeName]!=attributeValue)	//if the old element has a different (or no) value for this attribute (Firefox maintains different values for element.getAttribute(attributeName) and element[attributeName])
				{
//TODO del alert("updating "+id+" attribute "+attributeName+" to new value "+attributeValue);
					oldElement[attributeName]=attributeValue;	//update the old element's attribute (this format works for Firefox where oldElement.setAttribute("value", attributeValue) does not)
//TODO: fix the Firefox problem of sending an onchange event for any elements that get updated from an Ajax request, but only later when the focus blurs
//TODO fix the focus problem if the user has focus on an element that gets changed in response to the event
				}
			}
				//patch in the new child element hierarchy
			var oldChildNodeList=oldElement.childNodes;	//get all the child nodes of the old element
			var oldChildNodeCount=oldChildNodeList.length;	//find out how many old children there are
			if(childNodeCount>0)	//if the new element has child nodes
			{
					//patch any changed text
				if(childTextNodeCount==childNodeCount)	//if all the child nodes are text nodes
				{
					var onlyChangeValues=false;	//see if we can get by with just changing text node values
					if(oldChildNodeCount==childNodeCount)	//if the old element has the same number of children as the new element
					{
						onlyChangeValues=true;	//we may get away with only changing values after all
						for(var i=0; i<oldChildNodeCount; ++i)	//look at each old child node
						{
							if(oldChildNodeList[i].nodeType!=childNodeList[i].nodeType)	//if the old child element is a different type than the new one
							{
								onlyChangeValues=false;	//we'll have to actually change around child nodes
								break;	//stop looking for difficulties---we just found one
							}
						}
					}
					if(onlyChangeValues)	//if we think we can simply change text node values
					{
						for(var i=0; i<childNodeCount; ++i)	//for each new child node
						{
								//TODO later check for text
							oldChildNodeList[i].nodeValue=childNodeList[i].nodeValue;	//copy the text over to the old node
						}
					}
					else	//if we have to rearrange the child nodes
					{
						for(var i=oldChildNodeCount-1; i>=0; --i)	//for all of the old nodes
						{
							oldElement.removeChild(oldChildNodeList[i]);	//remove this child
						}
						for(var i=0; i<childNodeCount; ++i)	//for each new child node
						{
							var childNode=childNodeList[i];	//get this child node
								//TODO later check for text
							oldElement.appendChild(document.createTextNode(childNode.nodeValue));	//create and append an equivalent text node
						}
					}
				}
			}
			else	//if the element has no child nodes, remove all the child nodes from the old element
			{
				for(var i=oldChildNodeCount-1; i>=0; --i)	//for all of the old nodes
				{
					oldElement.removeChild(oldChildNodeList[i]);	//remove this child
				}
			}
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