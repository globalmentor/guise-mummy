/*Garret Wilson AJAX JavaScript library
Copyright (c) 2005-2007 Garret Wilson

Dependencies:
	javascript.js
	dom.js
*/

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
			/**The versions of the Microsoft XML HTTP ActiveX objects, in increasing order of preference.
			@see http://support.microsoft.com/?kbid=269238
			*/
			var MSXMLHTTP_VERSIONS=["Microsoft.XMLHTTP", "MSXML2.XMLHTTP", "MSXML2.XMLHTTP.3.0", "MSXML2.XMLHTTP.4.0", "MSXML2.XMLHTTP.5.0", "MSXML2.XMLHTTP.6.0", "MSXML2.XMLHTTP.7.0"];
			var msXMLHTTPVersion;	//we'll determine the correct version of the ActiveX to use
			for(var i=MSXMLHTTP_VERSIONS.length-1; i>=0; --i)	//for each available version
			{
				try
				{
					msXMLHTTPVersion=MSXMLHTTP_VERSIONS[i];	//get this version
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
	
//console.log("creating drag state mouse X", mouseX, "mouse Y", mouseY);
	this.dragging=false;	//initially we are not dragging
	this.dragSource=dragSource;

	this.initialMouseFixedPosition=new Point(mouseX, mouseY);
//TODO del alert("initial mouse fixed position X: "+mouseX+" Y: "+mouseY);
	this.initialFixedPosition=GUIUtilities.getElementFixedCoordinates(dragSource);	//get the initial position of the drag source in fixed terms of the viewport
	this.initialOffsetPosition=new Point(dragSource.offsetLeft, dragSource.offsetTop);	//get the offset position of the drag source

	this.initialPosition=null;	//these will be updated when dragging is started
	this.mouseDeltaX=0;
	this.mouseDeltaY=0;

	this.minX=null;
	this.maxX=null;	
//TODO fix	this.initialPosition=new Point(dragSource.offsetLeft, dragSource.offsetTop);	//get the position of the drag source
	
//TODO fix	this.initialPosition=GUIUtilities.getElementFixedCoordinates(dragSource);	//get the position of the drag source
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
//console.log("beginning drag; element has X", this.element.style.left, "Y", this.element.style.top);
/*TODO del when works
			this.width=this.element.offsetWidth;	//store the size of the element, because IE6 can sometimes reset the width and height to zero during AJAX calls for some unknown reason
			this.height=this.element.offsetHeight;
*/

			this.drag(mouseX, mouseY);	//drag the element to the current mouse position
//console.log("after initial drag; element has X", this.element.style.left, "Y", this.element.style.top);
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
			com.garretwilson.js.EventManager.addEvent(document, "mousemove", onDrag, false);	//listen for mouse move anywhere in document (IE doesn't allow us to listen on the window), as dragging may end somewhere else besides a drop target
			if(this.onDragBegin)	//if there is a function for beginning dragging
			{
				this.onDragBegin(this.element);	//call the dragging begin method
			}
		};

		/**Drags the component to the location indicated by the mouse coordinates.
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
//console.log("oldX:", oldX, "oldY:", oldY, "newX:", newX, "newY:", newY);
			if(newX!=oldX || newY!=oldY)	//if one of the coordinates has changed
			{
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
			com.garretwilson.js.EventManager.removeEvent(document, "mousemove", onDrag, false);	//stop listening for mouse moves
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
				this.initialPosition=GUIUtilities.getElementCoordinates(this.dragSource);	//get the absolute element coordinates, as we'll be positioning the element absolutely
//console.log("getting drag element; initial position:", this.initialPosition);

				element=this.dragSource.cloneNode(true);	//create a clone of the original element TODO be careful about this---probably use our own copy method, because IE will clone event handlers as well
				DOMUtilities.cleanNode(element);	//clean the clone
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
//console.log("got drag element; initial position:", this.initialPosition);

//TODO del alert("element: "+element.nodeName+" class: "+element.className);
			this.mouseDeltaX=this.initialMouseFixedPosition.x-this.initialPosition.x;	//calculate the mouse position relative to the drag source
			this.mouseDeltaY=this.initialMouseFixedPosition.y-this.initialPosition.y;
//TODO del alert("initialXY: "+this.initialPosition.x+", "+this.initialPosition.y+" mouseXY: "+this.initialMouseFixedPosition.x+", "+this.initialMouseFixedPosition.y+" deltaXY: "+this.mouseDeltaX+", "+this.mouseDeltaY);
			return element;	//return the cloned element
		};
	}
}
