/*Guise(TM) JavaScript support routines
Copyright (c) 2005-2008 GlobalMentor, Inc.

Dependencies:
	javascript.js
	dom.js
	ajax.js
	guise.swf

This script expects the following variables to be defined:
navigator.userAgentName The name of the user agent, such as "Firefox", "Mozilla", "MSIE", or "Opera".
navigator.userAgentVersionNumber The version of the user agent stored as a number.
GUISE_ASSETS_BASE_PATH The absolute base path of Guise assets.
GUISE_VERSION The build ID of the current Guise version.
*/

/*Guise AJAX Request Format, content type application/x-guise-ajax-request+xml
<request>
	<events>	<!--the list of events (zero or more)-->
		<action	<!--an action on a component-->
			objectID=""	<!--the ID of the depicted object-->
			targetID=""	<!--the ID of the target element on which the action occurred-->
			actionID=""	<!--the action identifier-->
		/>
		<change	<!--a property change on a depicted object; each value is presented in JSON syntax-->
			objectID="">	<!--the ID of the depicted object-->
			<property name="">	<!--the name of a property changing-->
				value	<!--the value of the property, encoded in JSON--> 
			</property>
		</change>
		<drop	<!--the end of a drag-and-drop operation-->
			objectID=""	<!--the ID of the depicted object that serves as the drop target-->
			dragSourceID=""	<!--the ID of the depicted object that serves as the drag source-->
			<mouse x="" y=""/>	<!--the mouse information at the time of the drop-->
		</drop>
		<focus	<!--a focus change-->
			objectID="">	<!--the ID of the depicted object-->
		/>
		<init	<!--initializes the page, requesting all frames to be resent-->
			jsVersion=""	<!--version of JavaScript supported by the browser-->
			utcOffset=""	<!--the current UTC offset in milliseconds-->
			utcOffset01=""	<!--the UTC offset of January in milliseconds-->
			utcOffset06=""	<!--the UTC offset of June in milliseconds-->
//TODO del			timezone="" <!--will vary according to DST-->
			hour=""
			language=""
			colorDepth=""
			screenWidth=""
			screenHeight=""
			javaEnabled=""
			browserWidth=""
			browserHeight=""
			referrer=""
		/>
		<keypress|keyrelease	<!--a key pressed or released anywhere; currently only certain control keys are reported-->
			code=""	<!--the code of the key pressed or released-->
			altKey="true|false"	<!--whether the Alt key was pressed-->
			controlKey="true|false"	<!--whether the Control key was pressed-->
			shiftKey="true|false"	<!--whether the Shift key was pressed-->
		/>
		<log> <!--sends debug information to the server-->
			level=""	<!--the level of debug reporting-->
			text	<!--the text to trace
		</log>
		<mouseenter|mouseexit|mouseclick	<!--a mouse event related to a component-->
			altKey="true|false"	<!--whether the Alt key was pressed-->
			controlKey="true|false"	<!--whether the Control key was pressed-->
			shiftKey="true|false"	<!--whether the Shift key was pressed-->
			button="buttonCode"	<!--the code of the button that was clicked, or -1 if no button was clicked-->
			clickCount="clickCount"	<!--the number of clicks, such as 1 for single click or 2 for double click, or 0 if no button was clicked-->
		>
			<viewport x="" y="" width="" height=""/>	<!--information on the viewport (scroll position and size)-->
			<component id="" x="" y="" width="" height=""/>	<!--information on the component that was the target of the mouse event (in absolute terms)-->
			<target id="" x="" y="" width="" height=""/>	<!--information on the element that was the target of the mouse event (in absolute terms)-->
			<mouse x="" y=""/>	<!--the mouse information at the time of the event (in fixed terms)-->
		</mouseEnter|mouseExit|mouseClick>
		<poll/> <!--polls the server to check for updates-->
	</events>
</request>
*/

/*Guise AJAX Response Format, content type application/x-guise-ajax-response+xml
<response>
	<patch></patch>	<!--XML element trees to be patched into the existing DOM tree.-->
	<attribute id="" name="" value=""></attribute>	<!--the new name and value of an attribute of an element with the given ID to be set (or removed if the value is null)-->
	<remove id=""/>	<!--ID of the XML element to be removed from the existing DOM tree-->
	<navigate>uri</navigate>	<!--URI of another page to which to navigate-->
	<frame></frame>	<!--definition of a frame to show-->
	<command objectID="" command="">parameters</command>	<!--a command with parameters encoded as JSON-->
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
Guise will also automatically add and remove a "jsRollover" class to the component and every subelement that is part of the component
(that is, every element that has a component ID-derived ID (i.e. "componentID-XXX") before sending the mouse event.
This "jsRollover" class indicates that the rollover state is completely controlled by JavaScript, and will be set and unset
independent of any server-side Guise rollover state.
(When a mouse leaves an element, for example, the "jsRollover" class will usually be removed well before the associated Guise component
is notified of the change in state.)
*/

//TODO before sending a drop event, send a component update for the drop target so that its value will be updated; or otherwise make sure the value is synchronized

/**See if the browser is Firefox.*/
var isUserAgentFirefox=navigator.userAgentName=="Firefox";
/**See if the browser is Firefox less than 3.*/
var isUserAgentFirefoxLessThan3=isUserAgentFirefox && navigator.userAgentVersionNumber<3;
/**See if the browser is IE.*/
var isUserAgentIE=navigator.userAgentName=="MSIE";
/**See if the browser is IE6.*/
var isUserAgentIE6=isUserAgentIE && navigator.userAgentVersionNumber<7;

/**Whether Guise AJAX communication is initially enabled.*/
var GUISE_AJAX_ENABLED=true;
/**The interval, in milliseconds, for polling the server under normal conditions, or -1 if no polling should occur.*/
var GUISE_AJAX_POLL_INTERVAL=-1;	//TODO del when new server-directed poll interval works
/**The interval, in milliseconds, for polling the server during file uploads, or -1 if no polling should occur.*/
var GUISE_AJAX_UPLOAD_POLL_INTERVAL=3000;

/**See if the browser is Safari.*/
var isSafari=navigator.userAgent.indexOf("Safari")>=0;	//TODO use a better variable; do better checks; update Guise server routines to check for Safari

/**This will later be updated to indicate if there is a resource import control on the form.*/
var hasResourceImportControl=false;

/**The URI of the XHTML namespace.*/
var XHTML_NAMESPACE_URI="http://www.w3.org/1999/xhtml";

/**The URI of the GuiseML namespace.*/
var GUISE_ML_NAMESPACE_URI="http://guiseframework.com/id/ml#";

/**The prefix for Guise state-related attributes, which shouldn't be removed when elements are synchronized.
When more states are added, the proto.NON_REMOVABLE_ATTRIBUTE_SET should be updated.
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
	JS_ROLLOVER: "jsRollover",
	SLIDER_CONTROL: "sliderControl",
	SLIDER_CONTROL_THUMB: "sliderControl-thumb",
	SLIDER_CONTROL_TRACK: "sliderControl-track",
	FRAME_TETHER: "frame-tether"
};

//Initialization AJAX Event

/**A class indicating an initialization AJAX request.
var jsVersion The version of JavaScript.
var utcOffset The current UTC offset in milliseconds.
var utcOffset01 The UTC offset of January in milliseconds.
var utcOffset06 The UTC offset of June in milliseconds.
//TODO del var timezone The time zone offset.
var hour The current hours.
var language The user language.
var colorDepth The color depth.
var screenWidth The screen width.
var screenHeight The screen height.
var javaEnabled Whether java is enabled.
var browserWidth The browser width.
var browserHeight The browser height
var referrer=document.referrer;	//get the document referrer
*/
function InitAJAXEvent()
{
	this.javascriptVersion=javascriptVersion;	//save the JavaScript version
	var date=new Date();	//create a new date
	
	//TODO fix; see http://www.breakingpar.com/bkp/home.nsf/0/87256B280015193F87256CFB006C45F7 and http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference:Global_Objects:Date:getTimezoneOffset and http://devblog.redfin.com/2007/08/getting_the_time_zone_from_a_web_browser.html
	
		//calculate the time zone, accounting for daylight saving time (modified from http://onlineaspect.com/2007/06/08/auto-detect-a-time-zone-with-javascript/)
/*TODO del if not needed; this seems to give the absolute time zone without adjusting for DST
	var dateString=date.toUTCString();	//create a string of the current date in UTC
	var dateLocal=new Date(dateString.substring(0, dateString.lastIndexOf(" ")-1));	//get the date as if it were local to GMT
	this.utcOffset=(date-dateLocal);	//get the current UTC offset in milliseconds
*/
	
	this.utcOffset=-date.getTimezoneOffset()*60*1000;	//get the current UTC offset in milliseconds
	var januaryUTC=new Date(date.getFullYear(), 0, 1, 0, 0, 0, 0);  //get January of this year in UTC
	var januaryUTCString=januaryUTC.toUTCString();	//get the string form of the January UTC date
	var januaryLocal=new Date(januaryUTCString.substring(0, januaryUTCString.lastIndexOf(" ")-1));	//get January of this year in local time
	var juneUTC=new Date(date.getFullYear(), 6, 1, 0, 0, 0, 0); //get June of this year in UTC
	var juneUTCString=juneUTC.toUTCString();	//get the string form of the June UTC date
	var juneLocal=new Date(juneUTCString.substring(0, juneUTCString.lastIndexOf(" ")-1));	//get January of this year in local time
	this.utcOffset01=(januaryUTC-januaryLocal);	//get the UTC offset of January in milliseconds
	this.utcOffset06=(juneUTC-juneLocal);	//get the UTC offset of June in milliseconds
/*TODO del if not needed
	var offsetDelta=januaryUTCOffset-juneUTCOffset;	//determine the difference in offset
	if(offsetDelta>0)
	if(januaryUTCOffset!=juneUTCOffset)	//if the offsets are different
	{
	   		// positive is southern, negative is northern hemisphere
			if (hemisphere >= 0)
				std_time_offset = daylight_time_offset;
			dst = "1"; // daylight savings time is observed
	   }
	   var i;
	   for (i = 0; i < document.getElementById('timezone').options.length; i++) {
			if (document.getElementById('timezone').options[i].value == convert(std_time_offset)+","+dst) {
				document.getElementById('timezone').selectedIndex = i;
				break;
			}
	   }
	}	
	else	//if both offsets are the same
	{
		dst=false; //daylight savings time is *not* observed
	}
*/
//TODO del	this.timezone=date.getTimezoneOffset()/-60;	//get the time zone offset
	this.hour=date.getHours();	//get the current hours
	this.language=navigator.language || navigator.userLanguage;	//get the user language
	this.colorDepth=screen.colorDepth;	//get the color depth
	this.screenWidth=screen.width;	//get the screen width
	this.screenHeight=screen.height;	//get the screen height
	this.javaEnabled=navigator.javaEnabled();	//see if java is enabled
	this.browserWidth=document.body.offsetWidth;	//get the browser width
	this.browserHeight=document.body.offsetHeight;	//get the browser height
	this.referrer=document.referrer;	//get the document referrer
}

//Poll AJAX Event

/**A class indicating a poll AJAX request.*/
function PollAJAXEvent()
{
}

//Action AJAX Event

/**A class encapsulating action information for an AJAX request.
@param objectID The ID of the depicted object.
@param targetID: The ID of the target element.
@param actionID: The action identifier, or null if no particular action is indicated.
@param option: The zero-based option indicating mouse buttons left, right, or middle in that order.
var objectID The ID of the depicted object.
var targetID: The ID of the target element.
var actionID: The action identifier, or null if no particular action is indicated.
var option: The zero-based option indicating mouse buttons left, right, or middle in that order.
*/
function ActionAJAXEvent(objectID, targetID, actionID, option)
{
	this.objectID=objectID;
	this.targetID=targetID;
	this.actionID=actionID;
	this.option=option;
}

//Change AJAX Event

/**A class encapsulating property change information for an AJAX request.
@param objectID The ID of the depicted object.
@param propertyMap An associative array of names of properties changing, keyed to a value or an array of values; null values are allowed.
var objectID The ID of the depicted object.
var properties An associative array of names of properties changing, keyed to a value or an array of values; null values are allowed.
*/
function ChangeAJAXEvent(objectID, properties)
{
	this.objectID=objectID;
	this.properties=properties;
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

//Focus AJAX Event

/**A class encapsulating focus information for an AJAX request.
@param objectID The ID of the depicted object.
var objectID The ID of the depicted object.
*/
function FocusAJAXEvent(objectID)
{
	this.objectID=objectID;
}

//Key Down AJAX Event

/**A class encapsulating key press or release information for an AJAX request.
@param eventType: The type of key event; one of KeyAJAXEvent.EventType.
@param code: The key code.
@param altKey: Whether the Alt key was pressed.
@param controlKey: Whether the Control key was pressed.
@param shiftKey: Whether the Shift key was pressed.
var code: The key code
var eventType: The type of key event; one of KeyAJAXEvent.EventType.
var altKey: Whether the Alt key was pressed.
var controlKey: Whether the Control key was pressed.
var shiftKey: Whether the Shift key was pressed.
*/
function KeyAJAXEvent(eventType, code, altKey, controlKey, shiftKey)
{
	this.eventType=eventType;	//save the event type
	this.code=code;	//save the key code
	this.altKey=altKey;	//save the key modifiers
	this.controlKey=controlKey;
	this.shiftKey=shiftKey;
}

/**The available types of key events.*/
KeyAJAXEvent.EventType={PRESS: "keypress", RELEASE: "keyrelease"};

//Log AJAX Event

/**A class encapsulating debug information to send to the server.
@param level The level of debug reporting.
@param text The text to log.
var level: The level of debug reporting.
var text: The text to log.
*/
function LogAJAXEvent(level, text)
{
	this.level=level;
	this.text=text;
}

/**A class encapsulating mouse information for an AJAX request.
@param eventType: The type of mouse event; one of MouseAJAXEvent.EventType.
@param component: The target component.
@param target: The element indicating the target of the event.
@param x: The horizontal position of the mouse, in absolute terms.
@param y: The vertical position of the mouse, in absolute terms.
@param altKey: Whether the Alt key was pressed.
@param controlKey: Whether the Control key was pressed.
@param shiftKey: Whether the Shift key was pressed.
@param button: The optional W3C code for the button that was clicked (defaults to -1).
@param clickCount: The optional number of clicks, such as 1 for single click or 2 for double-click (defaults to 0).
var eventType: The type of mouse event; one of MouseAJAXEvent.EventType.
var componentID: The ID of the target component.
var componentBounds: The rectangle of the component.
var targetID: The ID of the target element.
var targetBounds: The rectangle of the target element.
var viewportBounds: the absolute bounds of the viewport.
var mousePosition: The position of the mouse relative to the viewport.
var altKey: Whether the Alt key was pressed.
var controlKey: Whether the Control key was pressed.
var shiftKey: Whether the Shift key was pressed.
@param button: The W3C code for the button that was clicked, or -1 if no button was clicked.
@param clickCount: The number of clicks, such as 1 for single click or 2 for double-click, or 0 if no button was clicked.
*/
function MouseAJAXEvent(eventType, component, target, x, y, altKey, controlKey, shiftKey, button, clickCount)
{
	this.eventType=eventType;	//save the event type
	this.componentID=component.id;	//save the component ID
	this.componentBounds=GUIUtilities.getElementBounds(component);	//get the component bounds
	this.targetID=target.id;	//save the target ID
	this.targetBounds=GUIUtilities.getElementBounds(component);	//get the target bounds
	this.viewportBounds=GUIUtilities.getViewportBounds();	//get the viewport bounds
	this.mousePosition=new Point(x, y);	//save the mouse position
	this.altKey=altKey;	//save the key modifiers
	this.controlKey=controlKey;
	this.shiftKey=shiftKey;
	this.button=typeof button!="undefined" ? button : -1;	//save the button, or -1 if no button was given
	this.clickCount=clickCount||0;	//save the click count, or 0 if no button wasl clicked
}

/**The available types of mouse events.*/
MouseAJAXEvent.EventType={CLICK: "mouseclick", ENTER: "mouseenter", EXIT: "mouseexit"};

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

var com=com||{}; com.guiseframework=com.guiseframework||{}; com.guiseframework.js=com.guiseframework.js||{};	//create the com.guiseframework.js package

//Guise

/**A class encapsulating JavaScript and AJAX client functionality for Guise.
When the document is loaded the onLoad() method should be called to create the appropriate modal layers and append them to the document.
*/
com.guiseframework.js.Guise=function()
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

	/**The Guise support Flash, or null if the support Flash has not yet been embedded; the executeFlash() method should be used to lazily create the Flash object if needed.*/
	this._flash=null;

	/**Whether Guise support Flash has been initialized.*/
	this._flashInitialized=false;

	/**The Flash functions queued for execution before Flash is initialized.*/
	this._flashFunctions=new Array();

	/**The hidden IFrame target that receives the results of file uploads, or null if the upload IFrame hasn't yet been created.*/
	this._uploadIFrame=null;

	/**Whether Guise AJAX communication is enabled.*/
	this._enabled=GUISE_AJAX_ENABLED;

	/**The poll interval timer ID, or null if polling is not occuring.*/
	this._pollIntervalID=null;

	/**The current poll interval, in milleseconds, or -1 if polling is not enabled.*/
	this._pollInterval=-1;

	/**The array of drop targets, determined when the document is loaded. The drop targets are stored in increasing order of hierarchical depth.*/
	this._dropTargets=new Array();

	/**TODO del The array of original source images, keyed to 
	this.originalImageSrcs=new Array();*/

	/**The array of frame elements.*/
	this._frames=new Array();

	/**The current topmost modal frame, or null if there is no modal frame.*/
	this._modalFrame=null;

	/**The current flyover frame, or null if there is no flyover frame.*/
	this.flyoverFrame=null;

	/**The layer that allows modality by blocking user interaction to elements below.*/
	this._modalLayer=null;
	
	/**The IFrame that hides select elements from modal frames in IE6; positioned right below the modal layer.*/
	this._modalIFrame=null;

	/**The IFrame that hides select elements from flyover frames in IE6; positioned right below the flyover.*/
	this._flyoverIFrame=null;

	/**The current busy element, or null if there is no busy element.*/
	this._busyElement=null;

	/**Whether the busy indicator is visible.*/
	this._isBusyVisible=false;

	/**The last known focused node, or null if no node was ever known to have been focused.*/
	this._lastFocusedNode=null;

	/**The map of cursors that have been temporarily changed, keyed to the ID of the element the cursor of which has been changed.
	This is a tentative implementation, as blindly resetting the cursor after AJAX processing will prevent new cursors to be changed via AJAX.
	*/
	this.oldElementIDCursors=new Object();

	var proto=com.guiseframework.js.Guise.prototype;	//get the prototype

	if(!proto._initialized)
	{
		proto._initialized=true;

		/**The state of a task.
		@see com.guiseframework.model.TaskState
		*/
		proto.TaskState={INITIALIZE:"initialize", INCOMPLETE:"incomplete", ERROR:"error", PAUSED:"paused", STOPPED:"stopped", CANCELED:"canceled", COMPLETE:"complete"};

		/**The content type of a Guise AJAX request.*/
		proto.REQUEST_CONTENT_TYPE="application/x-guise-ajax-request+xml";

		/**The enumeration of the names of the request elements.*/
		proto.RequestElement=
				{
					REQUEST: "request", EVENTS: "events", OBJECT_ID: "objectID",
					FORM: "form", PROVISIONAL: "provisional", CONTROL: "control", NAME: "name", VALUE: "value",
					CHANGE: "change", PROPERTY: "property",
					ACTION: "action", COMPONENT: "component", TARGET_ID: "targetID", ACTION_ID: "actionID", OPTION: "option",
					DROP: "drop", SOURCE: "source", TARGET: "target", DRAG_SOURCE_ID:"dragSourceID", VIEWPORT: "viewport",
					FOCUS: "focus",
					CODE: "code", ALT_KEY: "altKey", CONTROL_KEY: "controlKey", SHIFT_KEY: "shiftKey",
					LOG: "log", LEVEL: "level",
					MOUSE: "mouse", ID: "id", X: "x", Y: "y", WIDTH: "width", HEIGHT: "height", BUTTON: "button", CLICK_COUNT: "clickCount",
					INIT: "init",
					POLL: "poll"
				};

		/**The content type of a Guise AJAX response.*/
		proto.RESPONSE_CONTENT_TYPE="application/x-guise-ajax-response+xml";

		/**The enumeration of the names of the response elements.*/
		proto.ResponseElement=
				{
					ATTRIBUTE: "attribute",
					COMMAND: "command",
					NAME: "name",
					NAVIGATE: "navigate",
					OBJECT_ID: "objectID",
					PATCH: "patch",
					RELOAD: "reload",
					REMOVE: "remove",
					RESPONSE: "response",
					VALUE: "value",
					VIEWPORT_ID: "viewportID"
				};

		/**Creates a new IFrame for receiving the contents of a file upload.
		If a file upload IFrame already exists, it will first be removed.
		@see #removeUploadIFrame()
		@see http://blog.caboo.se/articles/2007/4/2/ajax-file-upload
		@see http://www.openjs.com/articles/ajax/ajax_file_upload/
		@see http://sean.treadway.info/articles/2006/05/29/iframe-remoting-made-easy
		@see http://www.oreillynet.com/pub/a/javascript/2002/02/08/iframe.html
		@see http://www.quirksmode.org/dom/inputfile.html
		*/
		proto.createUploadIFrame=function()
		{
			this.removeUploadIFrame();	//first remove the current upload IFrame, if any
			this._uploadIFrame=document.createElementNS("http://www.w3.org/1999/xhtml", "iframe");	//create an IFrame
			this._uploadIFrame.id="uploadIFrame";	//set the ID of the frame so that we can do the IE fix later
			this._uploadIFrame.name="uploadIFrame";
			this._uploadIFrame.src=GUISE_ASSETS_BASE_PATH+"documents/empty.html";	//set the source to be an empty HTML document that won't create SSL messages of insecurity
			this._uploadIFrame.width="100px";	//Safari will ignore the IFrame if it has a size of 0, according to http://blog.caboo.se/articles/2007/4/2/ajax-file-upload
			this._uploadIFrame.height="100px";
			this._uploadIFrame.frameBorder="0";	//remove the border; see http://msdn2.microsoft.com/en-us/library/ms533770.aspx
			this._uploadIFrame.style.position="absolute";	//take the IFrame out of normal flow
			this._uploadIFrame.style.left="-9999px";	//completely remove the IFrame from sight
			this._uploadIFrame.style.top="-9999px";
			document.body.appendChild(this._uploadIFrame);	//add the upload IFrame to the document
			if(isUserAgentIE)	//if we're in IE6/7
			{
				window.frames["uploadIFrame"].name="uploadIFrame";	//because IE hasn't yet registered the name with the DOM, look up the frame by its ID and set its name again; see http://forums.digitalpoint.com/showthread.php?t=107314 and http://verens.com/archives/2005/07/06/ie-bugs-dynamically-creating-form-elements/
			}
		};

		/**Removes the IFrame for receiving the contents of a file upload.
		If no such IFrame exists, no action occurs.
		*/
		proto.removeUploadIFrame=function()
		{
			if(this._uploadIFrame!=null)	//if there is an IFrame
			{
				document.body.removeChild(this._uploadIFrame);	//remove the upload IFrame from the document
				this._uploadIFrame=null;	//indicate that we no longer have an upload IFrame
			}
		};

		/**Indicates whether AJAX communication is enabled.
		@return Whether AJAX communication is enabled.
		*/
		proto.isEnabled=function()
		{
			return this._enabled;	//return whether AJAX functionality is enabled
		};

		/**Enables or disables AJAX communication.
		If AJAX is disabled, all AJAX communication will immediately stop and polling will be disabled.
		@param enabled Whether AJAX communication should be enabled.
		*/
		proto.setEnabled=function(enabled)
		{
			if(this._enabled!=enabled)	//if the value is really changing
			{ 
				this._enabled=enabled;	//update the enabled status
				if(!enabled)	//if AJAX has been disabled
				{
					this.setPollInterval(-1);	//turn off polling
				}				
			}
		};

		/**Sets the interval for polling the server.
		If polling is already occuring at the given interval, no action occurs.
		@param pollInterval The new poll interval, in milleseconds, or -1 if polling should not be enabled.
		*/
		proto.setPollInterval=function(pollInterval)
		{
			if(this._pollInterval!=pollInterval)	//if the poll interval is really changing
			{ 
				this._pollInterval=pollInterval;	//update the poll interval immediately; in case another thread tries to set the same value (a little assistance for this race condition)
				if(this._pollIntervalID!=null)	//if there is a timer running already, turn it off
				{
					window.clearInterval(this._pollIntervalID);	//clear the timer
					this._pollIntervalID=null;	//remove the timer ID 
				}
				if(pollInterval>=0)	//if poll should be enabled
				{
					this._pollIntervalID=window.setInterval(this.poll.bind(this), pollInterval);	//send a poll event at the correct interval
				}
			}
		};

		/**Sends a poll request to the server.*/
		proto.poll=function()
		{
			this.sendAJAXRequest(new PollAJAXEvent());	//create and queue a new poll event
		};

		/**Sends a trace request to the server.
		@param objects The objects to trace; the string versions of these objects will be combined into a single string separated by whitespace.
		*/
		proto.trace=function(objects)
		{
			this.sendAJAXRequest(new LogAJAXEvent("trace", Array.from(arguments).join(" ")));	//create and queue a new log event from the arguments
		};

		/**Immediately sends or queues an AJAX request.
		@param ajaxRequest The AJAX request to send.
		*/
		proto.sendAJAXRequest=function(ajaxRequest)
		{
			if(this.isEnabled())	//if AJAX is enabled
			{
				this.ajaxRequests.enqueue(ajaxRequest);	//enqueue the request info
				this.processAJAXRequests();	//process any waiting requests now if we can
			}
		};
	
		/**Processes AJAX requests.
		@see #ajaxRequests
		*/
		proto.processAJAXRequests=function()
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
						if(ajaxRequest instanceof ActionAJAXEvent)	//if this is an action event
						{
							this._appendActionAJAXEvent(requestStringBuilder, ajaxRequest);	//append the action event
						}
						else if(ajaxRequest instanceof ChangeAJAXEvent)	//if this is an change event
						{
							this._appendChangeAJAXEvent(requestStringBuilder, ajaxRequest);	//append the change event
						}
						else if(ajaxRequest instanceof DropAJAXEvent)	//if this is a drop event
						{
							this._appendDropAJAXEvent(requestStringBuilder, ajaxRequest);	//append the drop event
						}
						else if(ajaxRequest instanceof FocusAJAXEvent)	//if this is a focus event
						{
							this._appendFocusAJAXEvent(requestStringBuilder, ajaxRequest);	//append the focus event
						}
						else if(ajaxRequest instanceof KeyAJAXEvent)	//if this is a key event
						{
							this._appendKeyAJAXEvent(requestStringBuilder, ajaxRequest);	//append the key event
						}
						else if(ajaxRequest instanceof LogAJAXEvent)	//if this is a log event
						{
							this._appendLogAJAXEvent(requestStringBuilder, ajaxRequest);	//append the log event
						}
						else if(ajaxRequest instanceof MouseAJAXEvent)	//if this is a mouse event
						{
							this._appendMouseAJAXEvent(requestStringBuilder, ajaxRequest);	//append the mouse event
						}
						else if(ajaxRequest instanceof InitAJAXEvent)	//if this is an initialization event
						{
							this._appendInitAJAXEvent(requestStringBuilder, ajaxRequest);	//append the init event
						}
						else if(ajaxRequest instanceof PollAJAXEvent)	//if this is a poll event
						{
							this._appendPollAJAXEvent(requestStringBuilder, ajaxRequest);	//append the poll event
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
						this.setEnabled(false);	//stop further AJAX communication
					}						
				}
				finally
				{
					this.processingAJAXRequests=false;	//we are no longer processing AJAX requests
				}
			}
		};

		/**Appends an AJAX action event to a string builder.
		@param stringBuilder The string builder collecting the request data.
		@param ajaxActionEvent The action event information to append.
		@return The string builder.
		*/
		proto._appendActionAJAXEvent=function(stringBuilder, ajaxActionEvent)
		{
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.ACTION,	//<action>
					new Map(this.RequestElement.OBJECT_ID, ajaxActionEvent.objectID,	//objectID="objectID"
							this.RequestElement.TARGET_ID, ajaxActionEvent.targetID,	//targetID="targetID"
							this.RequestElement.ACTION_ID, ajaxActionEvent.actionID,	//actionID="actionID"
							this.RequestElement.OPTION, ajaxActionEvent.option));	//option="option"
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.ACTION);	//</action>
			return stringBuilder;	//return the string builder
		};

		/**Appends an AJAX change event to a string builder.
		@param stringBuilder The string builder collecting the request data.
		@param ajaxChangeEvent The change event information to append.
		@return The string builder.
		*/
		proto._appendChangeAJAXEvent=function(stringBuilder, ajaxChangeEvent)
		{
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.CHANGE,	//<change>
					new Map(this.RequestElement.OBJECT_ID, ajaxChangeEvent.objectID));	//objectID="objectID"
			var properties=ajaxChangeEvent.properties;	//get the properties
			for(var propertyName in properties)	//for each property
			{
				DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.PROPERTY,	//<property>
						new Map(this.RequestElement.NAME, propertyName));	//name="name"
				DOMUtilities.appendXMLText(stringBuilder, JSON.serialize(properties[propertyName]));	//value
				DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.PROPERTY);	//</property>
			}
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.CHANGE);	//</change>
			return stringBuilder;	//return the string builder
		};

		/**Appends an AJAX drop event to a string builder.
		@param stringBuilder The string builder collecting the request data.
		@param ajaxDropEvent The drop event information to append.
		@return The string builder.
		*/
		proto._appendDropAJAXEvent=function(stringBuilder, ajaxDropEvent)
		{
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.DROP,	//<drop>
					new Map(this.RequestElement.OBJECT_ID, ajaxDropEvent.dropTarget.id,	//objectID="dropTargetID"
							this.RequestElement.DRAG_SOURCE_ID, ajaxDropEvent.dragSource.id));	//objectID="dragSourceID"
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.MOUSE, new Map(this.RequestElement.X, ajaxDropEvent.mousePosition.x, this.RequestElement.Y, ajaxDropEvent.mousePosition.y));	//<mouse x="x" y="y">
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.MOUSE);	//</mouse>
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.DROP);	//</drop>
			return stringBuilder;	//return the string builder
		};

		/**Appends an AJAX focus event to a string builder.
		@param stringBuilder The string builder collecting the request data.
		@param ajaxFocusEvent The focus event information to append.
		@return The string builder.
		*/
		proto._appendFocusAJAXEvent=function(stringBuilder, ajaxFocusEvent)
		{
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.FOCUS,	//<focus>
					new Map(this.RequestElement.OBJECT_ID, ajaxFocusEvent.objectID));	//objectID="objectID"
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.FOCUS);	//</focus>
			return stringBuilder;	//return the string builder
		};

		/**Appends an AJAX key event to a string builder.
		@param stringBuilder The string builder collecting the request data.
		@param ajaxKeyEvent The key event information to append.
		@return The string builder.
		*/
		proto._appendKeyAJAXEvent=function(stringBuilder, ajaxKeyEvent)
		{
			DOMUtilities.appendXMLStartTag(stringBuilder, ajaxKeyEvent.eventType,	//<keyXXX
					new Map(this.RequestElement.CODE, ajaxKeyEvent.code,	//code="code"
							this.RequestElement.ALT_KEY, ajaxKeyEvent.altKey,	//altKey="altKey"
							this.RequestElement.CONTROL_KEY, ajaxKeyEvent.controlKey,	//controlKey="controlKey"
							this.RequestElement.SHIFT_KEY, ajaxKeyEvent.shiftKey));	//shiftKey="shiftKey"
			DOMUtilities.appendXMLEndTag(stringBuilder, ajaxKeyEvent.eventType);	//</keyXXX>
			return stringBuilder;	//return the string builder
		};

		/**Appends an AJAX log event to a string builder.
		@param stringBuilder The string builder collecting the request data.
		@param ajaxLogEvent The log event information to append.
		@return The string builder.
		*/
		proto._appendLogAJAXEvent=function(stringBuilder, ajaxLogEvent)
		{
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.LOG,	//<log>
					new Map(this.RequestElement.LEVEL, ajaxLogEvent.level));	//level="level"
			DOMUtilities.appendXMLText(stringBuilder, ajaxLogEvent.text);	//text
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.LOG);	//</log>
			return stringBuilder;	//return the string builder
		};

		/**Appends an AJAX mouse event to a string builder.
		@param stringBuilder The string builder collecting the request data.
		@param ajaxMouseEvent The mouse event information to append.
		@return The string builder.
		*/
		proto._appendMouseAJAXEvent=function(stringBuilder, ajaxMouseEvent)
		{
			DOMUtilities.appendXMLStartTag(stringBuilder, ajaxMouseEvent.eventType,	//<mouseXXX>
//TODO del alert("ready to append viewport info: "+this.RequestElement.VIEWPORT+" x: "+ajaxMouseEvent.viewportBounds.x+" y: "+ajaxMouseEvent.viewportBounds.y+" width: "+ajaxMouseEvent.viewportBounds.width+" height: "+ajaxMouseEvent.viewportBounds.height);
					new Map(this.RequestElement.CODE, ajaxMouseEvent.code,	//code="code"
							this.RequestElement.ALT_KEY, ajaxMouseEvent.altKey,	//altKey="altKey"
							this.RequestElement.CONTROL_KEY, ajaxMouseEvent.controlKey,	//controlKey="controlKey"
							this.RequestElement.SHIFT_KEY, ajaxMouseEvent.shiftKey,	//shiftKey="shiftKey"
							this.RequestElement.BUTTON, ajaxMouseEvent.button,	//button="button"
							this.RequestElement.CLICK_COUNT, ajaxMouseEvent.clickCount));	//clickCount="clickCount"
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
		proto._appendInitAJAXEvent=function(stringBuilder, ajaxInitEvent)
		{
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.INIT,	//<init
					new Map("javascriptVersion", ajaxInitEvent.javascriptVersion,
						"utcOffset", ajaxInitEvent.utcOffset,
						"utcOffset01", ajaxInitEvent.utcOffset01,
						"utcOffset06", ajaxInitEvent.utcOffset06,
//TODO del						"timezone", ajaxInitEvent.timezone,
						"hour", ajaxInitEvent.hour,
						"language", ajaxInitEvent.language,
						"colorDepth", ajaxInitEvent.colorDepth,
						"screenWidth", ajaxInitEvent.screenWidth,
						"screenHeight", ajaxInitEvent.screenHeight,
						"javaEnabled", Boolean(ajaxInitEvent.javaEnabled).toString(),
						"browserWidth", ajaxInitEvent.browserWidth,
						"browserHeight", ajaxInitEvent.browserHeight,
						"referrer", ajaxInitEvent.referrer));
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.INIT);	//</init>
			return stringBuilder;	//return the string builder
		};

		/**Appends an AJAX poll event to a string builder.
		@param stringBuilder The string builder collecting the request data.
		@param ajaxPollEvent The poll event information to append.
		@return The string builder.
		*/
		proto._appendPollAJAXEvent=function(stringBuilder, ajaxPollEvent)
		{
			DOMUtilities.appendXMLStartTag(stringBuilder, this.RequestElement.POLL);	//<poll>
			DOMUtilities.appendXMLEndTag(stringBuilder, this.RequestElement.POLL);	//</poll>
			return stringBuilder;	//return the string builder
		};
	
		/**The callback method for processing HTTP communication.
		@param xmlHTTP The XML HTTP object.
		*/
		proto._processHTTPResponse=function(xmlHTTP)
		{
			try
			{
				var status=0;
				try
				{
					status=xmlHTTP.status;	//get the status
				}
				catch(e)	//if there is a problem getting the status, don't do anything; on Firefox, either the server went down (this would also happen if the form were to be submitted right before an AJAX request occurs or if an AJAX request were to be made during key event processing)
				{
					this.setEnabled(false);	//stop further AJAX communication
					return;	//don't do further processing; the page is probably reloading, anyway
				}
				if(status==200)	//if everything went OK
				{
//TODO del if not needed						if(this.isEnabled())	//if AJAX is enabled (if a user browsers to a page in Mozilla and the old page sent a request, GUISE_AJAX_ENABLED will be undefined by now; check it so that Mozilla won't throw an exception accessing AJAXResponse, which doesn't exist either)
					if((typeof AJAXResponse)!="undefined"	//if the page scope hasn't disappeared (if a user browsers to a page in Mozilla and the old page sent a request, AJAXResponse will be undefined here)
						&& xmlHTTP.responseText && xmlHTTP.responseXML && xmlHTTP.responseXML.documentElement)	//if we have XML (if there is no content or there is an error, IE sends back a document has a null xmlHTTP.responseXML.documentElement)
					{
						this.ajaxResponses.enqueue(new AJAXResponse(xmlHTTP.responseXML, xmlHTTP.responseText.length));	//enqueue the response
						this.processAJAXResponses();	//process enqueued AJAX responses
//TODO del						setTimeout("proto.processAJAXResponses();", 1);	//process the AJAX responses later		
//TODO del						this.processAJAXRequests();	//make sure there are no waiting AJAX requests
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
alert("text: "+xmlHTTP.responseText+" AJAX enabled? "+(this.isEnabled()));
				this.setEnabled(false);	//stop further AJAX communication
				throw exception;	//TODO testing
			}
		};

		/**Processes responses from AJAX requests.
		This routine should be called asynchronously from an event so that the DOM tree can be successfully updated.
		Whether the busy indicator is shown depends on the the browser type and the size of the response.
		This implementation shows the busy indicator for medium communication size on IE6, and on large communication sizes on all other browsers.
		Typical response sizes include:
		500-2000: Normal communication size; acceptable delay on IE6.
		5000-10000: Medium communication size; perceptible delay on IE6.
		20000-30000: Large communication size; unacceptable delay on IE6 without indicator. 
		@see #ajaxResponses
		*/
		proto.processAJAXResponses=function()
		{
			if(!this.processingAJAXResponses)	//if we aren't processing AJAX responses TODO fix small race condition in determining whether processing is occurring
			{
				this.processingAJAXResponses=true;	//we are processing AJAX responses now
				var newHRef=null;	//we'll see if a new URI was requested at any point
				try
				{
					while(this.ajaxResponses.length>0 && newHRef==null)	//while there are more AJAX responses and no redirect has been requested TODO fix small race condition on adding responses
					{
						var ajaxResponse=this.ajaxResponses.dequeue();	//get this response
						var responseDocument=ajaxResponse.document;	//get this response document
						var showBusy=isUserAgentIE6 ? ajaxResponse.size>5000 : ajaxResponse.size>20000;	//see if we should show a busy indicator
//TODO del; testing						var showBusy=ajaxResponse.size>100;	//TODO del; testing
/*TODO salvage if needed
						if(showBusy)	//if we should show a busy indicator
						{
							this.setBusyVisible(true);	//show a busy indicator
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
										case this.ResponseElement.COMMAND:	//command
//TODO del alert("this is a remove");
											this._processCommand(childNode);
											break;
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
											window.location.reload(true);	//reload the page, forcing a GET
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
								this.setBusyVisible(false);	//hide the busy indicator
							}
*/
						}
						this.processAJAXRequests();	//make sure there are no waiting AJAX requests
					}
					if(newHRef!=null)	//if navigation was requested
					{
						this.setEnabled(false);	//turn off AJAX processing
						window.location.href=newHRef;	//go to the new location
					}
				}
				finally
				{
					this.processingAJAXResponses=false;	//we are no longer processing AJAX responses
					if(newHRef==null)	//if we're not going to a new page
					{
						this.restoreTempElementCursors();	//restore the element cursors that were temporarily set just for this AJAX call
					}
				}
			}
		};

		/**Checks to see if Google Gears is installed.
		If Google Gears is installed, this method returns <code>true</code>.
		Otherwise, the user is asked if Google Gears should be installed.
		If so, the user is redirected to the Google Gears installation page.
		If the user elects not to install Google Gears, this method returns <code>false</code>.
		@param askInstall If the user should be asked to install Google Gears if it isn't already installed.
		@return <code>true</code> if Google Gears is installed, else <code>false</code> if Google Gears is not installed and the user has not been asked to install or the user declines.
		*/ 
		proto._verifyGoogleGears=function(askInstall)
		{
			if(window.google && google.gears)	//if Google Gears is installed
			{
				return true;
			}
			else	//if Google Gears is not installed
			{
				if(askInstall && confirm("To perform the requested functionality, you should have Google Gears installed.\nPlease install Google Gears and restart your browser.\n\nWould you like to install Google Gears now?\n\n(If you choose not to install Google Gears, the requested action may not perform correctly.)"))	//TODO i18n
				{
					location.href="http://gears.google.com/?action=install&message=Install%20Google%20Gears%20for%20the%20Marmox%E2%84%A2%20Network.&return="+location.href;
				}
				else	//if we shouldn't ask to install, or the user declined to install
				{
					return false;
				}
			}
		};
		
		/**The map of arrays of files ready for uploading, or <code>null</code> if no files have been selected.
		The arrays are mapped to the ID of the file list. 
		Each file also has a <var>httpRequest</var> variable with the current request, if any.
		*/
		proto._gearsFilesMap={};	//TODO provide a way for lists to be removed from the map

		/**Called when Google Gears selects files to open.
		@param files The files being selected.
		*/ 
		proto._onGearsOpenFiles=function(id, files)
		{
			var fileCount=files.length;
			if(fileCount>0)	//if the user selected files
			{
				var fileReferences=new Array(fileCount);	//create a new array of file infos to send back
				for(var fileIndex=0; fileIndex<fileCount; ++fileIndex)	//give each file an ID
				{
					var file=files[fileIndex];
					fileReferences[fileIndex]={id:fileIndex.toString(), name:file.name, size:file.blob.length};	//create our own information about this file reference; convert the index to a string for the ID, because server expects a string ID
				}
				this._gearsFilesMap[id]=files;	//store the files in the map, keyed to the ID
				this._onFilesSelected(id, fileReferences);	//send information on the selected files to the server
			}
		};

		/**Called when Google Gears has progress uploading files.
		@param filesID The ID of the files list.
		@param fileID The string ID of the file within the list.
		@param progressEvent The event containing information on the upload progress.
		*/ 
		proto._onGearsUploadProgress=function(filesID, fileID, progressEvent)
		{
			this._onFileProgress(filesID, fileID, this.TaskState.INCOMPLETE, progressEvent.loaded, progressEvent.lengthComputable ? progressEvent.total : -1);	//update the progress
		};

		/**Uploads a file using Google Gears.
		If no such files list exists, or no files match that indicated by the given file ID, no action occurs.
		@param filesID The ID of the files list.
		@param fileID The string ID of the file within the list.
		@param fileURI The URI to which the file should be uploaded.
		*/
		proto._gearsUploadFile=function(filesID, fileID, fileURI)
		{
			var files=this._gearsFilesMap[filesID];	//get the existing file list, if any
			if(files)	//if there is such a file list
			{
				var file=files[parseInt(fileID)];	//get the requested file; we should have been passed the string form of the index as teh ID
				if(file)	//if we found a file
				{
					this._onFileProgress(filesID, fileID, this.TaskState.INCOMPLETE, 0, file.blob.length);	//send an initial progress of zero bytes
					var httpUploadRequest=google.gears.factory.create("beta.httprequest");
					httpUploadRequest.open("PUT", fileURI);
					httpUploadRequest.upload.onprogress = this._onGearsUploadProgress.bind(this, filesID, fileID);
					var closureThis=this;
					httpUploadRequest.onreadystatechange = function()
					{
						switch(httpUploadRequest.readyState)
						{
							case 4:	//complete
								delete file.httpRequest;	//remove the HTTP request from the file
								closureThis._onFileProgress(filesID, fileID, closureThis.TaskState.COMPLETE, 0, file.blob.length);	//indicate that the transfer is complete TODO check for errors and cancellation, and verify that all the file was actually uploaded
								break;
						}
					};
					file.httpRequest=httpUploadRequest;	//save the request in case we need to cancel it
					httpUploadRequest.send(file.blob);
				} 
			}
		};

		/**Cancels a file upload using Google Gears.
		If no such files list exists, or no files match that indicated by the given file ID, no action occurs.
		@param filesID The ID of the files list.
		@param fileID The string ID of the file within the list.
		*/
		proto._gearsCancelUploadFile=function(filesID, fileID)
		{
			var files=this._gearsFilesMap[filesID];	//get the existing file list, if any
			if(files)	//if there is such a file list
			{
				var file=files[parseInt(fileID)];	//get the requested file; we should have been passed the string form of the index as teh ID
				if(file)	//if we found a file
				{
					var httpRequest=file.httpRequest;	//get the in-progress request, if any
					if(httpRequest)	//if there is an in-progress request
					{
						httpRequest.abort();	//abort the request
						delete file.httpRequest;	//remove the HTTP request from the file
					}
				} 
			}
		};
		
		/**Processes the AJAX command response.
		@param element The element representing the command response.
		*/ 
		proto._processCommand=function(element)
		{
			var objectID=element.getAttribute(this.ResponseElement.OBJECT_ID);	//get the object ID, if there is one
			var command=element.getAttribute(this.ResponseElement.COMMAND);	//get the command
			var parameters=JSON.evaluate(DOMUtilities.getNodeText(element));	//parse the parameters
//			alert("received command "+command+" for object "+objectID+" with audioURI "+parameters["audioURI"]);
			switch(command)	//see which command this is
			{
				case "audio-pause":
					this.executeFlash(function(flash){flash.pauseSound(objectID);});	//pause the sound
					break;
				case "audio-play":
					this.executeFlash(function(flash){flash.playSound(objectID, parameters["audioURI"]);});	//play the sound
					break;
				case "audio-position":
					this.executeFlash(function(flash){flash.setSoundPosition(objectID, parameters["position"]);});	//set the sound position
					break;
				case "audio-stop":
					this.executeFlash(function(flash){flash.stopSound(objectID);});	//stop the sound
					break;
				case "file-browse":
					if(this._verifyGoogleGears(true))	//use Google Gears if we can, asking the user if they want to install
					{
						var desktop = google.gears.factory.create("beta.desktop");	//access the desktop using Gears
						desktop.openFiles(this._onGearsOpenFiles.bind(this, objectID), {singleFile:!parameters["multiple"]});	//ask the user for files
					}
					else	//if Google Gears isn't installed, try to use Flash
					{
						this.executeFlash(function(flash){flash.browseFiles(objectID, parameters["multiple"]);});	//browse files, specifying whether multiple files should be selected
					}
					break;
				case "file-cancel":
					if(this._verifyGoogleGears(false))	//if we have Google Gears
					{
						this._gearsCancelUploadFile(objectID, parameters["id"]);	//cancel the identified file
					}
					else	//if Google Gears isn't installed, we must be using Flash
					{
						this.executeFlash(function(flash){flash.cancelFile(objectID, parameters["id"]);});	//cancel the identified file
					}
					this._onFileProgress(objectID, parameters["id"], this.TaskState.CANCELED, -1, -1);	//send a file progress canceled event, as neither Flash nor Google sends a cancel event for us
					break;
				case "file-upload":
					if(this._verifyGoogleGears(false))	//if we have Google Gears
					{
						this._gearsUploadFile(objectID, parameters["id"], parameters["destinationURI"]);	//upload the identified file to the specified destination URI
					}
					else	//if Google Gears isn't installed, we must be using Flash
					{
						this.executeFlash(function(flash){flash.uploadFile(objectID, parameters["id"], parameters["destinationURI"]);});	//upload the identified file to the specified destination URI
					}
					break;
				case "poll-interval":
//console.log("received poll interval request:", parameters["interval"]);
					this.setPollInterval(parameters["interval"]);	//poll at the requested interval
					break;
				case "resource-collect-receive":
					var element=document.getElementById(objectID);	//get the component element
					if(element)	//if the component element currently exists in the document
					{
						var childNodeList=element.childNodes;	//get all the child nodes
						for(var i=childNodeList.length-1; i>=0; --i)	//for each child node, going backwards
						{
							var childNode=childNodeList[i];	//get a reference to this child node
							if(childNode.nodeType==Node.ELEMENT_NODE && childNode.nodeName.toLowerCase()=="input" && childNode.type=="file")	//if this is a file input element
							{
								childNode.disabled=true;	//don't allow the file input to be modifed during transfer
								break;	//only disable the last file input
							}
						}
						var destinationURI=parameters["destionationURI"];	//get the destination URI
						var form=Node.getAncestorElementByName(element, "form");	//get the form ancestor
						this.createUploadIFrame();	//create the upload IFrame
						form.enctype="multipart/form-data";
						if(isUserAgentIE)	//if we're in IE6/7
						{
							form.encoding="multipart/form-data";	//IE requires the "encoding" property to be used; see http://verens.com/archives/2005/07/06/ie-bugs-dynamically-creating-form-elements/					
						}					
						form.action=destinationURI;	//indicate where the data should go
						form.target="uploadIFrame";	//indicate that the output should be re-routed to our hidden IFrame
						form.submit();	//submit the form
						this.setPollInterval(GUISE_AJAX_UPLOAD_POLL_INTERVAL);	//switch to polling at the upload interval
					}
					break;
				case "resource-collect-cancel":
					var element=document.getElementById(objectID);	//get the component element
					if(element)	//if the component element currently exists in the document
					{
						this.setPollInterval(GUISE_AJAX_POLL_INTERVAL);	//go back to polling at the normal interval
//TODO fix						this._uploadIFrame.src=GUISE_ASSETS_BASE_PATH+"documents/empty.html";	//set the source of the upload IFrame to an empty HTML document to cancel the upload; see http://www.missiondata.com/blog/java/28/file-upload-progress-with-ajax-and-java-and-prototype/feed/
//TODO fix; this doesn't work						this._uploadIFrame.src="";	//TODO testing
						this.removeUploadIFrame();	//remove the upload IFrame
						this._resetUploadControl(element);	//reset the upload control, which seems to make Firefox stop the upload TODO test on production to see if it cancels
						window.location.reload(true);	//reload the page, which seems to make IE stop the upload TODO test on production to see if it cancels
					}
					break;
				case "resource-collect-complete":
					var element=document.getElementById(objectID);	//get the component element
					if(element)	//if the component element currently exists in the document
					{
						this.setPollInterval(GUISE_AJAX_POLL_INTERVAL);	//go back to polling at the normal interval
						this.removeUploadIFrame();	//remove the upload IFrame
						this._resetUploadControl(element);	//reset the upload control
					}
					break;
			}
		};

		/**Processes the AJAX navigate response.
		If navigation is requested in a new viewport, navigation occurs; otherwise, the new navigation URI is returned.
		@param element The element representing the navigate response.
		@return The URI of the new requested navigation, or null if there is no new navigation or if the navigation occurs in a separate viewport
		*/ 
		proto._processNavigate=function(element)
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
		proto._processPatch=function(element)
		{
			var mozInlineBoxParentIDSet=null;	//we'll need to do special reflowing if Mozilla inline box children were updated; we'll lazily create and store a set of inline box parents if needed
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
							this._synchronizeElement(oldElement, childNode, true);	//synchronize this element tree, indicating that this is the root of a synchronization subtree
							this._updateComponents(oldElement, true);	//now that we've patched the old element, update any components that rely on the old element
							if(isUserAgentFirefoxLessThan3)	//if we're running Firefox <3 and we just patched an element inside a Mozilla inline box, Firefox won't correctly update the flow so we'll have to do that manually
							{
//console.debug("patched element", oldElement, "on Moz<3");
									//TODO at some point we may need to check the patched descendants, too---this only works if the root of the patched tree or one of its ancestors was a Mozilla inline box
								var mozInlineBoxAncestor=Node.getAncestorElementByStyle(oldElement, "display", "-moz-inline-box");	//see if there is a Mozilla inline box element ancestor (including this element)
								if(mozInlineBoxAncestor)	//if there is a Mozilla inline box element, we'll have to do special reflowing after all patching is done
								{
									var mozInlineBoxAncestorParent=mozInlineBoxAncestor.parentNode;	//get the inline box parent so we can just replace all the children
									if(mozInlineBoxAncestorParent && mozInlineBoxAncestorParent.id)	//if we find its parent as we expect, and it has an ID
									{
										if(!mozInlineBoxParentIDSet)	//if we haven't yet created the set of inline box parents
										{
											mozInlineBoxParentIDSet=new Object();	//create a new set
										}
										mozInlineBoxParentIDSet[mozInlineBoxAncestorParent.id]=true;	//indicate that we have another parent node of an inline box
									}
								}
							}
						}
						else if(DOMUtilities.hasClass(childNode, "frame"))	//if the element doesn't currently exist, but the patch is for a frame, create a new frame
						{
//TODO fix alert("ready to import frame node");
							oldElement=document.importNode(childNode, true);	//create an import clone of the node
//TODO del alert("ready to add frame: "+typeof oldElement);
							this.addFrame(oldElement);	//add this frame
//TODO fix alert("frame added");
						}
					}
				}
			}
			if(mozInlineBoxParentIDSet)	//if we have a set of inline box parent IDs to fix for Mozilla
			{
//console.debug("need to do some parent moz inline box updates");
				for(var mozInlineBoxParentID in mozInlineBoxParentIDSet)	//for each parent of a Mozilla inline box
				{
//console.debug("ready to find ID", mozInlineBoxParentID);
					var mozInlineBoxParent=document.getElementById(mozInlineBoxParentID);	//get the Mozilla inline box parent element
//console.debug("got inline box parent", mozInlineBoxParent, "ready to refresh");
					if(mozInlineBoxParent)	//if we have a parent to a Mozilla inline box that was updated
					{
						Node.refresh(mozInlineBoxParent);	//refresh the Mozilla inline box container by removing it from the tree and putting it back
						window.setTimeout(Node.refresh.bind(this, mozInlineBoxParent), 100);	//refresh the inline box container again in another thread; this is a horribly inefficient hack, but it is needed by Moxilla 2.x in some cases when the image source is updated
					}
				}
			}
		};

		/**Processes the AJAX attribute response.
		@param element The element representing attribute response.
		*/
/*TODO del or salvage
		proto._processAttribute=function(element)
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
		proto._processRemove=function(element)
		{
			var id=element.getAttribute("id");	//get the element ID, if there is one
//TODO del alert("processing remove with ID: "+id);
			if(id)	//if the element has an ID
			{
				var oldElement=document.getElementById(id);	//get the old element
				if(oldElement!=null)	//if we found the old element
				{
//TODO del alert("we found the old element");
					if(this._frames.contains(oldElement))	//if we're removing a frame
					{
//TODO fix alert("removing frame "+id);
						this.removeFrame(oldElement);	//remove the frame
					}
					else	//if we're removing any other node
					{
						this._uninitializeNode(oldElement, true);	//uninitialize the element
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
		proto.NON_REMOVABLE_ATTRIBUTE_SET=
		{
			"style":true,	//don't remove local styles, because they may be used by Guise (with frames, for instance)
			"onclick":true,	//don't remove the onclick attribute, because we may be using it for Safari to prevent a default action
			"hideFocus":true,	//don't remove the IE hideFocus attribute, because we're using it to fix the IE6 lack of CSS outline: none support
			"guiseStateWidth":true,	//don't remove Guise state attributes TODO change to jsXXX
			"guiseStateHeight":true
		};

		/**The set of attribute names that should not be copied literally when synchronizing.*/
		proto.UNCOPIED_ATTRIBUTE_SET=
		{
			"style":true,	//if this is a style attribute, we have to treat it differently, because neither Mozilla nor IE provide normal DOM access to the literal style attribute value
			"guise:patchType":true	//the guise:patchType attribute is used for patching information
		};

		/**The set of class names that should not be removed when synchronizing.*/
		proto.NON_REMOVABLE_CLASS_SET=
		{
			"jsRollover":true	//don't remove JavaScript-controlled classes
		};

		var nonRemovableClassArray=new Array();	//create a new array to hold the non-removable classes
		for(var nonRemovableClass in proto.NON_REMOVABLE_CLASS_SET)	//for each non-removable class
		{
			nonRemovableClassArray.add(nonRemovableClass);	//add this non-removable class to the array
		}

		/**The regular expression matching any non-removable class.*/
		proto.NON_REMOVABLE_CLASSES_REGEX=new RegExp(nonRemovableClassArray.join("|"));	//create a regular expression of all non-removable classes, separated by a regular expression union symbol
		
		/**Invalidates the content of all ancestor elements by removing the "guise:contentHash" attribute up the hierarchy.
		@param element The element the ancestors of which will have their ancestors invalidated.
		*/
		proto.invalidateAncestorContent=function(element)
		{
			var parentNode=element.parentNode;	//get the element's parent
			if(parentNode!=null && parentNode.nodeType==Node.ELEMENT_NODE && parentNode.nodeName.toLowerCase()!="table")	//if there is a parent element (IE6 crashes if we even check an attribute of TABLE)
			{
				parentNode.removeAttribute("guise:contentHash");	//indicate that the children have changed TODO use a constant
				this.invalidateAncestorContent(parentNode);	//invalidate the rest of the ancestors
			}
		};

		/**Removes all children from the given node.
		This implementation also unregistered any events for the node and all its children.
		@param node The node the children of which to remove.
		*/
		proto._removeChildren=function(node)	//TODO change this to use innerHTML=""
		{
			while(node.childNodes.length>0)	//while there are child nodes left (remove the last node, one at a time, because because IE can sometimes add an element back in after the last one was removed)
			{
				var childNode=node.childNodes[node.childNodes.length-1];	//get a reference to the last node
				this._uninitializeNode(childNode, true);	//uninitialize the node tree
				node.removeChild(childNode);	//remove the last node
			}
		};

		/**Synchronizes an element hierarchy with its patch element.
		@param oldElement The old version of the element.
		@param element The element hierarchy to patch into the existing document.
		@param isRoot Whether this is the top level element of a synchronization (optional); defaults to false.
		*/
		proto._synchronizeElement=function(oldElement, element, isRoot)
		{
			var elementName=element.nodeName;	//save the element name
			var patchType=Element.getAttributeNS(element, GUISE_ML_NAMESPACE_URI, "patchType");	//get the patch type TODO use a constant
			if(patchType=="none" || patchType=="temp")	//if we should not do any patching
			{
				return;	//stop synchronization
			}

				//get the content hash attributes before we update the attributes
			var oldElementContentHash=oldElement.getAttribute("guise:contentHash");	//get the old element's content hash, if any TODO use a constant
			var newElementContentHash=Element.getAttributeNS(element, GUISE_ML_NAMESPACE_URI, "contentHash");	//get the new element's content hash, if any TODO use a constant
/*TODO del
			if(oldElementContentHash==newElementContentHash)	//TODO del; testing
			{
				alert("we think: "+DOMUtilities.getNodeString(oldElement));
				alert("is the same as: "+DOMUtilities.getNodeString(element));
			}
*/

			var oldElementAttributeHash=oldElement.getAttribute("guise:attributeHash");	//get the old element's attribute hash, if any TODO use a constant
			var newElementAttributeHash=Element.getAttributeNS(element, GUISE_ML_NAMESPACE_URI, "attributeHash");	//get the new element's attribute hash, if any TODO use a constant
			var isAttributesChanged=oldElementAttributeHash!=newElementAttributeHash;	//see if the attributes have changed (this doesn't count for the content hash attribute, which we'll check separately)
			if(isAttributesChanged)	//if the attribute hash values are different
			{
				if(isRoot)	//if this is the root of the synchronization
				{
					this.invalidateAncestorContent(oldElement);	//indicate that the ancestors now have different content
				}
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
//TODO del; not needed						if(attributeName!="value" || elementName!="textarea")	//if this is the value attribute of a text area, don't remove the value, because the value is really specified as the text content
							//TODO see if there is a way to keep from removing all the non-null but empty default IE6 attributes
		//TODO del alert("ready to remove "+oldElement.nodeName+" attribute "+oldAttributeName+" with current value "+oldAttributeValue);
						oldElement.removeAttribute(oldAttributeName);	//remove the attribute normally (apparently no action will take place if performed on IE-specific attributes such as element.start)
		//TODO fix					i=0;	//TODO fix; temporary to get out of looking at all IE's attributes
					}

				}
				if((elementName!="button" && elementName!="textarea") && oldElement.value && element.getAttribute("value")==null)	//if there is an old value but no value attribute present in the new element (IE 6 and Mozilla do not show "value" in the list of enumerated values) (IE6 thinks that the value of a button is content, so ignore button values) (don't clear the value for text areas, which are stored as content) TODO fix button values for non-IE6 browsers, maybe, but current button values are unused anyway because of the IE6 bug
				{
//TODO del alert("clearing value; old value was: "+oldElement.value);
					if(patchType!="novalue")	//if we shouldn't ignore the value attribute
					{
						oldElement.value="";	//set the value to the empty string (setting the value to null will result in "null" being displayed in the input control on IE)
					}
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
//alert("patching value attribute");
							if(patchType=="novalue")	//if we should ignore the value attribute
							{
//alert("found novalue for ID: "+oldElement.id);
								continue;	//go to the next attribute
							}
						}
						var oldAttributeValue=oldElement[attributeName];	//get the old attribute value
						var valueChanged=oldAttributeValue!=attributeValue;	//see if the value is really changing
						if(valueChanged && attributeName=="className")	//if the class name value is changing, add back any non-removable classes as needed
						{
							if(oldAttributeValue.match(this.NON_REMOVABLE_CLASSES_REGEX))	//if the original class name had one of the non-removable classes (this is only to eliminate most cases in which there are no non-removable classes; because the regular expression has word boundary checking, this test may give some false positives because of substring matching)
							{
								var newAttributeValues=attributeValue.split(/\s/);	//we'll add back any of the missing non-removable attributes to this array; start with the attributes we already have
								var existingNonRemovableClasses=new Object();	//create a set of the already-existing non-removable classes
								for(var newAttributeValueIndex=newAttributeValues.length-1; newAttributeValueIndex>=0; --newAttributeValueIndex)	//for each new class
								{
									var newClass=newAttributeValues[newAttributeValueIndex];	//get the new class
									if(this.NON_REMOVABLE_CLASS_SET[newClass])	//if this is a non-removable class
									{
										existingNonRemovableClasses[newClass]=true;	//show that we already have this non-removable class
									}
								}
								var oldAttributeValues=oldAttributeValue.split(/\s/);	//split out all the old classes
								for(var oldAttributeValueIndex=oldAttributeValues.length-1; oldAttributeValueIndex>=0; --oldAttributeValueIndex)	//for each old class
								{
									var oldClass=oldAttributeValues[oldAttributeValueIndex];	//get the old class
									if(this.NON_REMOVABLE_CLASS_SET[oldClass] && !existingNonRemovableClasses[oldClass])	//if this is a non-removable class that was removed in the new value
									{
										newAttributeValues.add(oldClass);	//add the old class back to the array that will form the new class name
									}
								}
								attributeValue=newAttributeValues.join(" ");	//join the attributes back together to create the new class name
								valueChanged=oldAttributeValue!=attributeValue;	//check again to see if the value is really changing
							}
						}							
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
					if(isRoot)	//if this is the root of the synchronization
					{
						this.invalidateAncestorContent(oldElement);	//indicate that the ancestors now have different content
					}
				}
					//patch in the new child element hierarchy
				if(elementName=="textarea")	//if this is a text area, do special-case value changing (restructuring won't work in IE and Mozilla) TODO check for other similar types TODO use a constant
				{
					if(patchType!="novalue")	//if we shouldn't ignore the value attribute
					{
						oldElement.value=DOMUtilities.getNodeText(element);	//set the new value to be the text of the new element
					}
				}
				else	//for other elements, restructure the DOM tree normally
				{
					var oldChildNodeList=oldElement.childNodes;	//get all the child nodes of the old element
					var oldChildNodeCount=oldChildNodeList.length;	//find out how many old children there are
					var childNodeList=element.childNodes;	//get all the child nodes of the element
					var childNodeCount=childNodeList.length;	//find out how many children there are

/*TODO del when works
if(elementName=="select")
{

	alert("just assigned select child node count "+childNodeCount+" with old element ID "+oldElement.id);
	alert("old node structure of select is: "+DOMUtilities.getNodeString(oldElement));
	alert("new node structure of select is: "+DOMUtilities.getNodeString(element));
}
*/

					var isChildrenCompatible=true;	//start by assuming children are compatible; children will be compatible as long as the exiting children are of the same types and, if they are elements, of the same name
					for(var i=0; i<oldChildNodeCount && i<childNodeCount && isChildrenCompatible; ++i)	//for each child node (as long as children are compatible)
					{
						var oldChildNode=oldChildNodeList[i];	//get the old child node
						var childNode=childNodeList[i];	//get the new child node
						if(oldChildNode.nodeType==childNode.nodeType)	//if these are the same type of nodes
						{
							if(childNode.nodeType==Node.ELEMENT_NODE)	//if this is an element, check the name and ID
							{
									//if the IDs are different, assume that the entire child should be replaced rather than synchronized---the event listeners would probably be different anyway
								var oldChildID=oldChildNode.getAttribute("id");	//get the old child ID
								var oldChildID=oldChildNode.id ? oldChildNode.id : null;	//normalize the ID because some browsers such as IE in HTML mode might return "" for a missing attribute rather than null
								var childID=childNode.getAttribute("id");	//get the new child's ID
	//TODO del alert("comparing "+oldChildNode.nodeName+" IDs "+oldChildID+" "+typeof oldChildID+" and "+childID+" "+typeof childID);
								if(oldChildID!=childID)	//if the IDs are different
								{
//alert("child IDs don't match: "+oldChildNode.nodeName+" IDs "+oldChildID+" "+typeof oldChildID+" and "+childID+" "+typeof childID);
									isChildrenCompatible=false;	//these child elements aren't compatible because they have different IDs
								}
								else if(oldChildNode.nodeName.toLowerCase()!=childNode.nodeName.toLowerCase())	//if the IDs are the same, check the node names; if they are different
								{
									var patchType=Element.getAttributeNS(childNode, GUISE_ML_NAMESPACE_URI, "patchType");	//ignore different element types if this is a temp or a non-patch node TODO use a constant
									if(patchType!="none" && patchType!="temp")	//all regular patching nodes are incompatible if the node names are different (those that are not non-patching or temporary nodes)
									{
										isChildrenCompatible=false;	//these child elements aren't compatible because they have different node name
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
							this._uninitializeNode(oldChildNode, true);	//uninitialize the node tree
	//TODO del alert("removing old node: "+oldChildNodeList[i].nodeName);
							oldElement.removeChild(oldChildNode);	//remove this old child
							
						}
	//TODO del alert("children are still compatible, old child node count: "+oldElement.childNodes.length+" new child node count "+childNodeCount);
					}
					else	//if children are not compatible
					{
	//TODO del alert("children are not compatible, old "+oldElement.nodeName+" with ID "+oldElement.id+" child node count: "+oldChildNodeCount+" new "+element.nodeName+" "+"with ID "+element.getAttribute("id")+" child node count "+childNodeCount+" (verify) "+element.childNodes.length);
						this._removeChildren(oldElement);	//remove all the children from the old element and start from scratch
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
//alert("we're out of new children at index: "+i+" out of node count "+childNodeCount);
//alert("old node structure of parent is: "+DOMUtilities.getNodeString(oldElement));
	try
	{
	//TODO del alert("ready to clone node: "+DOMUtilities.getNodeString(childNode));
	//TODO del alert("ready to clone node");
							var importedNode=document.importNode(childNode, true);	//create an import clone of the node
//alert("imported node: "+i+" out of node count "+childNodeCount);
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
//alert("ready to append node: "+i+" out of node count "+childNodeCount);
							oldElement.appendChild(importedNode);	//append the imported node to the old element
//alert("ready to initialize node: "+i+" out of node count "+childNodeCount);
	//TODO del alert("ready to initialize node");
							this._initializeNode(importedNode, true);	//initialize the new imported node, installing the correct event handlers
//alert("initialized node: "+i+" out of node count "+childNodeCount);
	}
	catch(e)
	{
		alert("error creating new child node: "+DOMUtilities.getNodeString(childNode));
	}
						}
					}
					
					if(elementName=="select")	//if we just patched a select element, we must go back and make sure the correct options are selected, as IE6 will automatically select any newly added option, even if we didn't specify with the DOM that it should be selected
					{

//TODO del alert("selected index before: "+oldElement.selectedIndex);
					
						for(var i=childNodeList.length-1; i>=0; --i)	//for each select child node (use the list we already have, which has been dynamically updated throughout our changes)
						{
							var childNode=childNodeList[i];	//get the new child node
							if(childNode.nodeType==Node.ELEMENT_NODE && childNode.nodeName=="option")	//if this is an option child
							{
								var oldChildNode=oldChildNodeList[i];	//get the old child node
//TODO del alert("setting index "+i+" selected: "+(childNode.getAttribute("selected")=="selected"));
//TODO del; we need an even more elaborate IE6 workaround								oldChildNode.selected=childNode.getAttribute("selected")=="selected";	//update the selected state of this option
										//TODO maybe see if setting the new selected index could obviate this IE6 workaround workaround
								var oldSelected=oldChildNode.selected;	//see whether this option is selected
								var newSelected=childNode.getAttribute("selected")=="selected";	//see whether the option should be selected
								if(oldSelected!=newSelected)	//if the option doesn't have the correct selected state
								{
									try
									{
										oldChildNode.selected=newSelected;	//update the selected state
									}
									catch(e)	//for some unknown reason, IE6 will sometimes change the selected state (e.g. from true to false) but then throw an exception: "Could not set the specified property. Unspecified error."; perhaps this is caused by hidden controls
									{
										if(oldChildNode.selected!=newSelected)	//they change probably succeeded, regardless of the error; if not, rethrow the error
										{
											throw e;	//rethrow the error if the change didn't work
										}
									}
								}
							}
						}
//TODO del alert("selected index after: "+oldElement.selectedIndex);
						
					}
				}				
			}
		};

		/**Synchronizes the literal style of an element.
		@param oldElement The old version of the element.
		@param attributeValue The new literal value of the style attribute, which may be null or the empty string.
		*/ 
		proto._synchronizeElementStyle=function(oldElement, attributeValue)	//TODO del comment: do extra checks for attributeValue; sometimes when the server sends all border widths of 0px, the browser will change this to a single shortcut border width of 0px
		{
			if(dragState && dragState.dragging && dragState.dragSource==oldElement)	//if this element is being dragged
			{
				return;	//don't update the style of the element while it is being dragged TODO improve to update all the styles bug position-related styles
			}
			if(attributeValue==null)	//if there is no attribute value
			{
				attributeValue="";	//use the empty string
			}
			if(isUserAgentIE)	//if this is IE
			{
				oldElement.style.cssText=attributeValue;	//use a special form of accessing the style text; see http://ajax.stealthsettings.com/developing-cross-browser-javascript/setting-an-elements-style-via-javascript/
			}
			else	//for all other browsers
			{
				oldElement.setAttribute("style", attributeValue);	//set the style attribute
			}
		
//TODO fix			var removableStyles={"backgroundColor":true, "borderWidth":true, "color":true, "display":true, "visibility":true};	//create a new map of styles to remove if not assigned, with the style name as the key

//TODO del if works			var removableStyles=new Set("backgroundColor", "borderBottomWidth", "borderLeftWidth", "borderRightWidth", "borderTopWidth", "borderWidth", "color", "display", "visibility");	//create a new map of styles to remove if not assigned, with the style name as the key

/*TODO del if not needed
			var newStyles=null;	//we'll create a new map of styles if there are new styles
			if(attributeValue)	//if there is a new style
			{
				newStyles={};	//create a new map of styles
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
						newStyles[styleProperty]=
						
						
						if(oldElement.style[styleProperty]!=styleValue)	//if the style is different	TODO check about removing a style
						{
alert("ready to set element style "+styleProperty+" to value "+styleValue);
							oldElement.style[styleProperty]=styleValue;	//update this style
						}
					}
				}
			}
*/
/*TODO del if not needed
			if(attributeValue)	//if there is a new style
			{
//TODO fix with something else to give IE layout					oldElement["contentEditable"]=false;	//for IE 6, give the component "layout" so that things like opacity will work
				var compositeStyleMap={"borderBottomWidth":"borderWidth", "borderLeftWidth":"borderWidth", "borderRightWidth":"borderWidth", "borderTopWidth":"borderWidth", "borderWidth":"borderWidth"};	//the composite styles keyed to the componentized versions of them so that if we set a component we'll know not to remove the composite style TODO create a preconfigured version of this 
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
						var compositeStyle=compositeStyleMap[styleProperty];	//see if this is part of a composite style
						if(compositeStyle)	//if this style was part of a composite style
						{
							delete removableStyles[compositeStyle];	//remove the composite style style from the map of removable styles, indicating that we shouldn't remove the composite style							
						}
						if(oldElement.style[styleProperty]!=styleValue)	//if the style is different	TODO check about removing a style
						{
alert("ready to set element style "+styleProperty+" to value "+styleValue);
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
alert("trying to remove style "+removableStyleName+" with old value "+oldElement.style[removableStyleName]);
					oldElement.style[removableStyleName]="";	//remove the style from the old element
				}
			}
*/
		};

		/**The private method for asynchronously initializing.*/
		proto._initialize=function()
		{
		//TODO display a wait cursor until we initialize everything
		
		/*TODO del unless we want to fix external-toGuise stylesheets
			if(typeof guiseIE6Fix!="undefined")	//if we have IE6 fix routines loaded
			{
				guiseIE6Fix.fixStylesheets();	//fix all IE6 stylesheets
			}
		*/
			com.garretwilson.js.EventManager.addEvent(window, "resize", this._onWindowResize.bind(this), false);	//add a resize listener
		//TODO del	com.garretwilson.js.EventManager.addEvent(window, "scroll", onWindowScroll, false);	//add a scroll listener
			com.garretwilson.js.EventManager.addEvent(window, "unload", this.onUnload.bind(this), false);	//do the appropriate uninitialization when the window unloads

			/**Support for insert for TinyMCE.
			Modified from XHTMLxtras TinyMCE Plugin 3.2.1, Copyright © 2004-2008, Moxiecode Systems AB.
			@param elementName The name of the element to add.
			*/
			tinymce.Editor.prototype.insertElement=function(elementName)
			{
				var element=this.dom.getParent(this.focusElement, elementName.toUpperCase());
				tinyMCE.execCommand('mceBeginUndoLevel');
				if(element==null)
				{
					var selectedText=this.selection.getContent();
					if(selectedText.length>0)
					{
						if (tinymce.isIE && elementName.indexOf('html:') == 0)
						{
							elementName = elementName.substring(5).toLowerCase();
						}
						var dom = this.dom;
						this.getDoc().execCommand('FontName', false, 'mceinline');
						tinymce.each(dom.select(tinymce.isWebKit ? 'span' : 'font'), function(n) {
							if (n.style.fontFamily == 'mceinline' || n.face == 'mceinline')
								dom.replace(dom.create(elementName), n, 1);
						});
						var elementArray = tinymce.grep(dom.select(elementName));						
						for (var i=0; i<elementArray.length; i++)
						{
							var element = elementArray[i];
							element.id = '';
							element.setAttribute('id', '');
							element.removeAttribute('id');
						}
					}
				}
				else
				{
					setElementAttribs(element);
				}
				this.nodeChanged();
				tinyMCE.execCommand('mceEndUndoLevel');
			};

			tinyMCE.init(
				{
			    "mode": "none",
			    "theme": "advanced",
			    "skin": "guise",
			    "plugins": "autoresize,directionality,paste,table,xhtmlxtras,xhtmlphrases",
			    "theme_advanced_blockformats": "p,h1,h2,h3,h4,h5,h6,address,blockquote,pre,div,dt,dd",
			    "theme_advanced_toolbar_align": "left",
			    "theme_advanced_toolbar_location": "top",
			    "theme_advanced_statusbar_location": "bottom",
			    "theme_advanced_disable": "image,cleanup,help,code,fontselect,fontsizeselect,styleselect,forecolor,backcolor,forecolorpicker,backcolorpicker,newdocument",
			    "theme_advanced_buttons3_add": ",|,tablecontrols,|,ltr,rtl,|,cite,abbr,acronym,del,ins,attribs,dfn,computercode,var,samp,kbd",
			    "entity_encoding": "raw"	//don't entity encoding except for necessary XML characters; XHTML by default doesn't understand HTML entities, and numeric encoding would encur an unnecessary slowdown
				});
			
			this._initializeNode(document.documentElement, true, true);	//initialize the document tree, indicating that this is the first initialization
			this._updateComponents(document.documentElement, true);	//update all components represented by elements within the document
		//TODO del when works	dropTargets.sort(function(element1, element2) {return getElementDepth(element1)-getElementDepth(element2);});	//sort the drop targets in increasing order of document depth
			com.garretwilson.js.EventManager.addEvent(document, "mouseup", onDragEnd, false);	//listen for mouse down anywhere in the document (IE doesn't allow listening on the window), as dragging may end somewhere else besides a drop target
			com.garretwilson.js.EventManager.addEvent(document.documentElement, "keydown", onKey, false);	//listen for key down anywhere in the document so that we can send key events back to the server (IE doesn't work correctly with key events registered on the window or document)
			com.garretwilson.js.EventManager.addEvent(document.documentElement, "keyup", onKey, false);	//listen for key up anywhere in the document so that we can send key events back to the server (IE doesn't work correctly with key events registered on the window or document)
			com.garretwilson.js.EventManager.addEvent(document.documentElement, "click", onClick, false);	//listen for mouse clicks bubbling up from anywhere (that we haven't dealt with specifically and canceled) in the document so that we can report clicks back to the server
			this.sendAJAXRequest(new InitAJAXEvent());	//send an initialization AJAX request	
		//TODO del	alert("compatibility mode: "+document.compatMode);
			this.setBusyVisible(false);	//turn off the busy indicator	
				//remove the init IFrame shield
			var initIFrame=document.getElementById("initIFrame");	//get the init IFrame
			if(initIFrame)	//if there is an initialization IFrame
			{
				initIFrame.parentNode.removeChild(initIFrame);	//remove the init IFrame from the document; we don't need it anymore
			}
			var focusable=getFocusableDescendant(document.documentElement);	//see if the document has a node that can be focused
			if(focusable)	//if we found a focusable node
			{
				focusable.focus();	//focus on the node
			}
			this.setPollInterval(GUISE_AJAX_POLL_INTERVAL);	//turn on polling at the normal level
			
//TODO del			jQuery(".wymeditor-body").wymeditor();

			
		};

		/**Creates the appropriate modal layers, initializes document-related variables, and installs event handlers.
		This method should be called when the document is loaded.
		*/
		proto.onLoad=function()
		{
				//create the modal layer
			this._modalLayer=document.createElementNS("http://www.w3.org/1999/xhtml", "div");	//create a div TODO use a constant for the namespace
			this._modalLayer.className="modalLayer";	//load the modal layer style
	//TODO fix				oldModalLayerDisplayDisplay="none";
			this._modalLayer.style.display="none";
			this._modalLayer.style.position="absolute";
			this._modalLayer.style.top="0px";
			this._modalLayer.style.left="0px";
			document.body.appendChild(this._modalLayer);	//add the modal layer to the document
			if(isUserAgentIE6)	//if we're in IE6
			{
				this._modalIFrame=document.getElementById("modalIFrame");	//get the modal IFrame
				this._flyoverIFrame=document.getElementById("flyoverIFrame");	//get the flyover IFrame
			}
			this._busyElement=document.getElementById("busy");	//get the busy element
			if(document.bodyLength && document.bodyLength>60000)	//if the body length is over 60,000 (as indicated by the custom Guise variable), show a busy indicator
			{
				this.setBusyVisible(true);	//turn on the busy indicator
					//TODO fix; doesn't seem to work on IE6 or Firefox
				this.setElementTempCursor(document.body, "wait");	//change the document body cursor to "wait" until the AJAX initialization is finished
			}
			window.setTimeout(this._initialize.bind(this), 1);	//run the initialization function in a separate thread
		};

		/**Executes a function using the Guise support Flash object.
		This method is necessary to lazily create the Guise support Flash object, after which the command must be executed some time later on some platforms.
		We must embed Flash dynamically at runtime because of an EOLAS patent that forced Microsoft and Opera to disable automatic activation of Flash and other plugins.
		In addition, it makes page loading faster only to load Flash when needed.
		Example: this.executeFlash(function(flash){flash.pauseSound(objectID);});
		@param flashFunction A function to execute, taking a single parameter of the Flash support object.
		*/
		proto.executeFlash=function(flashFunction)
		{
			if(this._flash==null)	//if the Flash support object has not yet been created, create it
			{
				//see http://msdn2.microsoft.com/en-us/library/ms537508.aspx
				//see http://blog.deconcept.com/2005/12/15/internet-explorer-eolas-changes-and-the-flash-plugin/
				//see http://www.jeroenwijering.com/?item=embedding_flash
				var guiseFlashDiv=document.createElementNS("http://www.w3.org/1999/xhtml", "div");	//create an outer div; we must use innerHTML, as the DOM methods don't result in a fully working Flash object
				guiseFlashDiv.style.position="absolute";	//take the div out of normal flow
				guiseFlashDiv.style.left="-9999px";	//completely remove the div from sight
				guiseFlashDiv.style.top="-9999px";
				document.body.appendChild(guiseFlashDiv);	//add the outer div to the body before adding the content, or the SWF won't register its exposed methods
				var flashGuiseInnerHTMLStringBuilder=new StringBuilder();	//create a new string builder for creating the Flash object
				if(isUserAgentFirefox && navigator.userAgentVersionNumber>=3)	//Firefox 3 doesn't seem to like using <object> to dynamically embed Flash
				{
					var embedAttributes={"id":"guiseFlash", style:"width:1px;height:1px;", quality:"high", type:"application/x-shockwave-flash"};	//create a map of attributes for serialization
					embedAttributes.src=GUISE_ASSETS_BASE_PATH+"flash/guise.swf?guiseVersion="+GUISE_VERSION;	//add the Guise version so an out-of-date cached version won't be used
					DOMUtilities.appendXMLStartTag(flashGuiseInnerHTMLStringBuilder, "embed", embedAttributes);	//<embed ...>
					DOMUtilities.appendXMLEndTag(flashGuiseInnerHTMLStringBuilder, "embed");	//</embed>
				}
				else	//for all other browsers, including Firefox <3 TODO replace with SWFObject when it supports XHTML
				{
					var objectAttributes={"id":"guiseFlash", style:"width:1px;height:1px;"};	//create a map of attributes for serialization
					if(isUserAgentIE)	//if this is IE
					{
						objectAttributes.classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000";
						var httpMethod=window.location.protocol=="https:" ? "https" : "http";	//use HTTPS if this is a secure page
						objectAttributes.codebase=httpMethod+"://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,0,0,0";
					}
					else	//if this is any other browser
					{
						objectAttributes.type="application/x-shockwave-flash";
						objectAttributes.data=GUISE_ASSETS_BASE_PATH+"flash/guise.swf?guiseVersion="+GUISE_VERSION;	//add the Guise version so an out-of-date cached version won't be used
					}
					DOMUtilities.appendXMLStartTag(flashGuiseInnerHTMLStringBuilder, "object", objectAttributes);	//<object ...>
					DOMUtilities.appendXMLStartTag(flashGuiseInnerHTMLStringBuilder, "param", {"name":"movie", "value":GUISE_ASSETS_BASE_PATH+"flash/guise.swf?guiseVersion="+GUISE_VERSION}, true);	//<param name="movie" value="...guise.swf"/>
					DOMUtilities.appendXMLStartTag(flashGuiseInnerHTMLStringBuilder, "param", {"name":"quality", "value":"high"}, true);	//<param name="movie" value="...guise.swf"/>
					DOMUtilities.appendXMLEndTag(flashGuiseInnerHTMLStringBuilder, "object");	//</object>
				}
				guiseFlashDiv.innerHTML=flashGuiseInnerHTMLStringBuilder.toString();	//add the Flash content
				this._flash=guiseFlashDiv.childNodes[0];	//the first child node is the Flash component; save a reference to it for later
//console.debug("enqueueing flash function");
				this._flashFunctions.enqueue(flashFunction);	//enqueue the Flash function until after initialization
				return;	//don't execute the Flash function now; it will be executed after initialization of Flash support
			}
			if(this._flashInitialized)	//if Flash is initialized
			{
				flashFunction(this._flash);	//process the Flash functionality immediately
			}
			else	//if Flash has not yet been initialized (even though it has been created)
			{
				this._flashFunctions.enqueue(flashFunction);	//enqueue the Flash function until after initialization
			}
		};

		/**Uninstalls event handlers.
		This method should be called when the document is unloaded.
		*/
		proto.onUnload=function()
		{
			this.setEnabled(false);	//immediately turn off AJAX communication
				//TODO fix or del	this.setBusyVisible(true);	//turn on the busy indicator
			com.garretwilson.js.EventManager.clearEvents();	//unload all events
				//TODO fix or del	this.setBusyVisible(false);	//turn off the busy indicator
		};

		/**Called when the window resizes.
		This implementation updates the modal layer.
		@param event The object containing event information.
		*/
		proto._onWindowResize=function(event)
		{
			//TODO work around IE bug that stops calling onWindowResize after a couple of maximization/minimization cycles
			window.setTimeout(this.updateModalLayer.bind(this), 1);	//update the modal layer later, because during resize IE won't allow us to hide the modal layer and have the correct size update instantaneously
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

		/**Temporarily changes the cursor of an element, which will be reset after the next AJAX call.
		The element must have an ID.
		@param element The element the cursor of which to update.
		@param cursor The name of the cursor (such as "wait") to use.
		*/
		proto.setElementTempCursor=function(element, cursor)
		{
			var elementID=element.id;	//get the element ID
			if(elementID)	//if the element has an ID
			{
				var oldElementIDCursors=this.oldElementIDCursors;	//get the map of old element cursors
				var oldElementCursor=element.style.cursor;	//get the old element cursor
				if(!(elementID in oldElementIDCursors))	//if we haven't already saved the previous cursor (note that there is a small race condition here with asynchronous events and AJAX responses)
				{
					element.style.cursor=cursor;	//change the element's cursor
					oldElementIDCursors[elementID]=oldElementCursor;	//save the original cursor so that it can be reset after the next AJAX communication is finished
				}
			}
		};

		/**Restores all cursors that have been temporarily set during an AJAX call.
		@see #setElementTempCursor()
		*/
		proto.restoreTempElementCursors=function()
		{
			var oldElementIDCursors=this.oldElementIDCursors;	//get the map of old cursors
			for(var oldElementID in oldElementIDCursors)	//for each old element ID
			{
//TODO del alert("looking at old element ID: "+oldElementID);
				var oldCursor=oldElementIDCursors[oldElementID];	//get the old cursor
				delete oldElementIDCursors[oldElementID];	//immediately remove this old cursor from the map to reduce the race condition in which another thread could see that there is an existing cursor and not save it again
//TODO del alert("old cursor: "+oldCursor);
				var element=document.getElementById(oldElementID);	//get the old element in the document
				if(element!=null)	//if this element is still in the document
				{
//TODO del alert("restoring old cursor for element: "+oldElementID+" which has cursor "+element.style.cursor);
					element.style.cursor=oldCursor;	//set the cursor back to what it was
//TODO del alert("element now has cursor: "+element.style.cursor);
				}
			}
		};

		/**Adds a frame to the array of frames.
		This implementation adds the frame to the document, initializes the frame, and updates the modal state.
		@param frame The frame to add.
		*/
		proto.addFrame=function(frame)
		{
		//TODO fix so that it works both on IE and Firefox							oldElement.style.position="fixed";	//TODO testing
			frame.style.position="absolute";	//change the element's position to absolute; it should already be set like this, but set it specifically so that dragging will know not to drag a copy TODO update the element's initial position
		
			frame.style.visibility="hidden";	//TODO testing
			frame.style.left="-9999px";	//TODO testing; this works; maybe even remove the visibility changing
			frame.style.top="-9999px";	//TODO testing
		
/*TODO del; doesn't fix IE auto frame size problem
			var form=getForm(document.documentElement);	//get the form
			document.body.appendChild(frame);	//add the frame element to the document; do this first, because IE doesn't allow the style to be accessed directly with imported nodes until they are added to the document
*/
			var form=document.forms[0];	//get the form
			form.appendChild(frame);	//add the frame element to the form, so that it can submit input type="file" correctly; do this first, because IE doesn't allow the style to be accessed directly with imported nodes until they are added to the document			
			this._initializeNode(frame, true);	//initialize the new imported frame, installing the correct event handlers; do this before the frame is positioned, because initialization also fixes IE6 classes, which can affect position
			this._initializeFramePosition(frame);	//initialize the frame's position
		
			var openEffectClassName=Element.getClassName(frame, STYLES.OPEN_EFFECT_REGEXP);	//get the open effect specified for this frame
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
		
			this._updateComponents(frame, true);	//update all the components within the frame
			this._frames.add(frame);	//add the frame to the array
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
		proto.removeFrame=function(frame)
		{
			var index=this._frames.indexOf(frame);	//get the frame index
			if(index>=0)	//if we know the index of the frame
			{
				this._frames.remove(index);	//remove the frame from the array
				this._uninitializeNode(frame, true);	//uninitialize the frame tree
				document.forms[0].removeChild(frame);	//remove the frame element to the document
				this._updateModal();	//update the modal state
			}
		};

		/**Initializes the position of a frame.
		@param frame The frame to position.
		*/
		proto._initializeFramePosition=function(frame)
		{
		//TODO del var debugString="";
		//TODO del	var framePosition=new Point();	//we'll calculate the frame position; create an object rather than using primitives so that the internal function can access its variables via closure
		
//TODO del alert("initializing frame position, with width: "+DOMUtilities.getComputedStyle(frame, "width"));
//TODO del			alert("initializing frame position, with explicit width: "+frame.width);
//TODO del; fix alert("initializing frame position, with width: "+frame.currentStyle.width);
		
			var frameX, frameY;	//we'll calculate the frame position
			var relatedComponentInput=Node.getDescendantElementByName(frame, "input", new Map("name", "relatedComponentID"));	//get the input holding the related component ID
			var relatedComponent=relatedComponentInput ? document.getElementById(relatedComponentInput.value) : null;	//get the related component, if there is one
			if(relatedComponent)	//if there is a related component
			{
		//TODO del alert("found related component: "+relatedComponentID);
				var frameBounds=GUIUtilities.getElementBounds(frame);	//get the bounds of the frame
		//TODO del debugString+="frameBounds: "+frameBounds.x+","+frameBounds.y+","+frameBounds.width+","+frameBounds.height+"\n";
				var relatedComponentBounds=GUIUtilities.getElementBounds(relatedComponent);	//get the bounds of the related component
		//TODO del debugString+="relatedComponentBounds: "+relatedComponentBounds.x+","+relatedComponentBounds.y+","+relatedComponentBounds.width+","+relatedComponentBounds.height+"\n";
				var tether=Node.getDescendantElementByClassName(frame, STYLES.FRAME_TETHER);	//get the frame tether, if there is one
				if(tether)	//if there is a frame tether
				{
					var positionTether=function()	//create a function to position relative to the tether
							{
					//TODO del alert("found tether: "+tether.id);
								var tetherBounds=GUIUtilities.getElementBounds(tether);	//get the bounds of the tether
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
					var tetherIMG=Node.getDescendantElementByName(tether, "img");	//see if the tether has an image TODO use a constant
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
		proto._updateModal=function()
		{
			var frameCount=this._frames.length;	//find out how many frames there are
			this._modalFrame=null;	//start out presuming there is no modal frame
			this.flyoverFrame=null;	//start out presuming there is no flyover frame
			for(var i=0; i<frameCount; ++i)	//update the z-orders
			{
				var frame=this._frames[i];	//get a reference to this frame
				frame.style.zIndex=(i+1)*100;	//give the element the appropriate z-order
				if(Element.hasClassName(frame, "frameModal"))	//if this is a modal frame TODO use a constant
				{
					this._modalFrame=frame;	//indicate our last modal frame
				}
				if(Element.hasClassName(frame, "flyoverFrame"))	//if this is a flyover frame TODO use a constant
				{
					this.flyoverFrame=frame;	//indicate our last flyover frame
				}
			}
			if(this._modalFrame!=null)	//if there is a modal frame
			{
				if(this._modalLayer.style.display=="none")	//if the modal layer is not shown, update it (don't update it if it's already shown, as this will cause flickering by the modal layer being turned on and off; this tactic will not compensate for a new frame making the entire size larger, however)
				{
					this.updateModalLayer();	//always update the modal layer before it is shown, as IE may not always call resize to keep the modal layer updated
				}
				this._modalLayer.style.zIndex=this._modalFrame.style.zIndex-1;	//place the modal layer directly behind the modal frame
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
			if(isUserAgentIE6)	//if we're in IE6
			{
				var flyoverIFrame=this._flyoverIFrame;	//get the flyover IFrame
				if(flyoverIFrame)	//if we know the flyover IFrame
				{
					if(this.flyoverFrame!=null)	//if there is a flyover frame
					{
					
						var flyoverFrameBounds=GUIUtilities.getElementExternalBounds(this.flyoverFrame);	//get the flyover frame bounds
						flyoverIFrame.style.left=flyoverFrameBounds.x;	//update the bounds of the IFrame to match that of the flyover frame
						flyoverIFrame.style.top=flyoverFrameBounds.y;
						flyoverIFrame.style.width=flyoverFrameBounds.width;
						flyoverIFrame.style.height=flyoverFrameBounds.height;
						flyoverIFrame.style.zIndex=this.flyoverFrame.style.zIndex-1;	//place the flyover iframe directly behind the flyover frame (a frame shouldn't be modal and a flyover both at the same time)
						flyoverIFrame.style.display="block";	//make the flyover IFrame visible
					}
					else	//if there is no flyover frame
					{
						flyoverIFrame.style.display="none";	//hide the flyover iframe
					}
				}
			}
		};

		/**Updates the size of the modal layer, creating it if necessary.*/
		proto.updateModalLayer=function()
		{
			var oldModalLayerDisplay=this._modalLayer.style.display;	//get the current display status of the modal layer
			this._modalLayer.style.display="none";	//make sure the modal layer is hidden, because having it visible will interfere with the page/viewport size calculations (setting the size to 0px will not give us immediate feedback in IE during resize)
			var oldModalIFrameDisplay=null;	//get the old modal IFrame display if we need to
			if(this._modalIFrame)	//if we have a modal IFrame
			{
				oldModalIFrameDisplay=this._modalIFrame.style.display;	//get the current display status of the modal IFrame
				this._modalIFrame.style.display="none";	//make sure the modal IFrame is hidden, because having it visible will interfere with the page/viewport size calculations
			}
/*TODO del
			var oldFlyoverIFrameDisplay=null;	//get the old flyover IFrame display if we need to
			if(this._flyoverIFrame)	//if we have a flyover IFrame
			{
				oldFlyoverIFrameDisplay=this._flyoverIFrame.style.display;	//get the current display status of the flyover IFrame
				this._flyoverIFrame.style.display="none";	//make sure the flyover IFrame is hidden, because having it visible will interfere with the page/viewport size calculations
			}
*/
		
		/*TODO del; doesn't work instantanously with IE
			modalLayer.style.width="0px";	//don't let the size of the modal layer get in the way of the size calculations
			modalLayer.style.height="0px";
			if(modalIFrame)	//if we have a modal IFrame
			{
				modalIFrame.style.width="0px";	//don't let the size of the modal layer get in the way of the size calculations
				modalIFrame.style.height="0px";
			}
		*/
		
			var pageSize=GUIUtilities.getPageSize();	//get the size of the page
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
				this._modalIFrame.style.width=this._modalLayer.style.width;	//update the size of the IFrame to match that of the modal layer
				this._modalIFrame.style.height=this._modalLayer.style.height;
				this._modalIFrame.style.display=oldModalIFrameDisplay;	//show the modal IFrame, if it was visible before
			}
/*TODO del
			if(this._flyoverIFrame)	//if we have a flyover IFrame
			{
				this._flyoverIFrame.style.width=this._modalLayer.style.width;	//update the size of the IFrame to match that of the modal layer
				this._flyoverIFrame.style.height=this._modalLayer.style.height;
				this._flyoverIFrame.style.display=oldFlyoverIFrameDisplay;	//show the flyover IFrame, if it was visible before
			}
*/
		};

		/*Sets the busy indicator visible or hidden.
		@param busyVisible A boolean indication of whether the busy indicator should be visible.
		*/
		proto.setBusyVisible=function(busyVisible)
		{
			if(busyVisible!=this._isBusyVisible)	//if the busy visibility is changing
			{
				this._isBusyVisible=busyVisible;	//update the busy indicator flag
				var busyElement=this._busyElement;	//get the busy element
				if(busyElement)	//if there is a busy element
				{
					if(busyVisible)	//if we're going to show the busy element
					{
						busyElement.style.zIndex=9001;	//give the element an arbitrarily high z-index value so that it will appear in front of other components TODO calculate the highest z-order
						busyElement.style.left="-9999px";	//place the busy element off the screen before we display and center it; it will need to be displayed before we can determine its size
						busyElement.style.top="-9999px";
					}
					var newBusyDisplay=busyVisible ? "block" : "none";	//get the new show or hide status for the busy information
					busyElement.style.display=newBusyDisplay;	//show or hide the busy information
					if(busyVisible)	//TODO testing
					{
						GUIUtilities.centerNode(busyElement);
					}
					if(isUserAgentIE6)	//if we're in IE6, prepare the flyover frame TODO consolidate duplicate code
					{
						var flyoverIFrame=this._flyoverIFrame;	//get the flyover IFrame
						if(flyoverIFrame)	//if we know the flyover IFrame
						{
							if(busyVisible)	//if we are now showing the busy information
							{							
								var flyoverFrameBounds=GUIUtilities.getElementExternalBounds(busyElement);	//get the flyover frame bounds
								flyoverIFrame.style.left=flyoverFrameBounds.x;	//update the bounds of the IFrame to match that of the flyover frame
								flyoverIFrame.style.top=flyoverFrameBounds.y;
								flyoverIFrame.style.width=flyoverFrameBounds.width;
								flyoverIFrame.style.height=flyoverFrameBounds.height;
								flyoverIFrame.style.zIndex=busyElement.style.zIndex-1;	//place the flyover iframe directly behind the flyover frame (a frame shouldn't be modal and a flyover both at the same time)
							}
							flyoverIFrame.style.display=newBusyDisplay;	//show or hide the flyover IFrame
						}
					}
				}
			}
		};

		/**Adds an element to the list of drop targets.
		@param element The element to add to the list of drop targets.
		*/
		proto.addDropTarget=function(element)
		{
			this._dropTargets.add(element);	//add this element to the list of drop targets
			this._dropTargets.sort(function(element1, element2) {return Node.getDepth(element1)-Node.getDepth(element2);});	//sort the drop targets in increasing order of document depth
		};

		/**Determines the drop target at the given coordinates.
		@param x The horizontal test position.
		@param y The vertical test position.
		@return The drop target at the given coordinates, or null if there is no drop target at the given coordinates.
		*/
		proto.getDropTarget=function(x, y)
		{
			for(var i=this._dropTargets.length-1; i>=0; --i)	//for each drop target (which have been sorted by increasing element depth)
			{
				var dropTarget=this._dropTargets[i];	//get this drop target
				var dropTargetCoordinates=GUIUtilities.getElementFixedCoordinates(dropTarget);	//get the coordinates of the drop target
				if(x>=dropTargetCoordinates.x && y>=dropTargetCoordinates.y && x<dropTargetCoordinates.x+dropTarget.offsetWidth && y<dropTargetCoordinates.y+dropTarget.offsetHeight)	//if the coordinates are within the drop target area
				{
					return dropTarget;	//we've found the deepest drop target
				}
			}
		};

		/**Loads an image so that it will be present when needed.
		@param src The URL of the image to load.
		*/
		proto.loadImage=function(src)
		{
			var image=new Image();	//create a new image
			image.src=src;	//set the src of the image so that it will load
		};

		/**Initializes a node and optionally all its children, adding the correct listeners.
		@param node The node to initialize.
		@param deep true if the entire hierarchy should be initialized.
		@param initialInitialization true if this is the first initialization of the entire page.
		@return true if initialization was successful, else false if the node should be deleted.
		*/
		proto._initializeNode=function(node, deep, initialInitialization)
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
								if(isUserAgentIE6 && Element.hasClassName(node, "imageSelectActionControl"))	//if this is IE6, which doesn't support the CSS outline: none property, create a workaround TODO use a constant; create something more general than just the image select action control
								{
									node.hideFocus="true";	//hide the focus on this element
								}
								if(elementClassNames.contains("actionControl") || elementClassNames.contains("actionControl-link"))	//if this is a Guise action, or a link in an action control TODO later look at *all* link clicks and do popups for certain ones
								{
									if(!node.getAttribute("target"))	//if the link has no target (the target wouldn't work if we tried to take over the events; we can't just check for null because IE will always send back at least "")
									{
										com.garretwilson.js.EventManager.addEvent(node, "click", onLinkClick, false);	//listen for anchor clicks
										if(isSafari)	//if this is Safari TODO fix better; this may have been fixed in Safari 2.0.4; see http://developer.yahoo.com/yui/docs/YAHOO.util.Event.html
										{
											node.onclick=function(){return false;};	//cancel the default action, because Safari 1.3.2 ignores event.preventDefault(); http://www.sitepoint.com/article/dhtml-utopia-modern-web-design/3
										}
									}
								}
								break;
							case "button":
								if(elementClassNames.contains("buttonControl"))	//if this is a Guise button TODO use constant
								{
									com.garretwilson.js.EventManager.addEvent(node, "click", onButtonClick, false);	//listen for button clicks
									if(isSafari)	//if this is Safari TODO fix better
									{
										node.onclick=function(){return false;};	//cancel the default action, because Safari 1.3.2 ignores event.preventDefault(); http://www.sitepoint.com/article/dhtml-utopia-modern-web-design/3
									}
								}
								break;
							case "div":
								if(node.getAttribute("guise:patchType")=="temp")	//if this is just a temporary element that should be removed (in anticipation of a later replacement, such as the FCKeditor, for example) (IE doesn't let us check this attribute for all elements)
								{
									return false;	//stop initializing and indicate that the element should be deleted
								}
								break;
/*TODO del
							case "div":	//TODO maybe use a custom element
								if(elementClassNames.contains("textControl-body"))	//if this is a Guise text control TODO use constant
								{
										//TODO check content type, maybe
									jQuery(node).wymeditor();	//TODO testing
									return;
								}
								break;
*/
							case "img":
								var rolloverSrc=node.getAttribute("guise:rolloverSrc");	//get the image rollover, if there is one TODO use a constant
								if(rolloverSrc)	//if the image has a rollover TODO use a constant; maybe use hasAttributeNS()
								{
									this.loadImage(rolloverSrc);	//preload the image
									if(!Element.hasClassName(node, STYLES.MOUSE_LISTENER))	//if this is not a mouse listener (which would get a onMouse listener registered, anyway)
									{
										com.garretwilson.js.EventManager.addEvent(node, "mouseover", onMouse, false);	//listen for mouse over on a mouse listener
										com.garretwilson.js.EventManager.addEvent(node, "mouseout", onMouse, false);	//listen for mouse out on a mouse listener							
									}
		//TODO del							alert("rollover source: "+node.getAttribute("guise:rolloverSrc"));
								}
								if(isUserAgentFirefoxLessThan3)	//if we're running Firefox <3, check for the inline box layout bug that doesn't show images that have not yet been cached
								{
									if(node.offsetWidth==0 && node.offsetHeight==0)	//if this image has no dimensions
									{
										var mozInlineBoxAncestor=Node.getAncestorElementByStyle(node, "display", "-moz-inline-box");	//see if there is a Mozilla inline box element ancestor
										if(mozInlineBoxAncestor)	//if there is a Mozilla inline box element causing our problems
										{
											var mozInlineBoxAncestorParent=mozInlineBoxAncestor.parentNode;	//get *its* parent so we can just replace all the children
											if(mozInlineBoxAncestorParent)	//if we find its parent like we expect
											{
												Node.refresh(mozInlineBoxAncestorParent);	//refresh the container element by removing it from the tree and putting it back
											}
										}
									}
								}
								break;	//TODO del if not needed
							case "iframe":	//TODO improve to only ignore fckEditor iframes if needed
								deep=false;	//don't look at the iframe children
								break;
							case "input":
								switch(node.type)	//get the type of input
								{
									case "text":
									case "password":
										com.garretwilson.js.EventManager.addEvent(node, "change", onTextInputChange, false);
		//TODO del; doesn't work across browsers								com.garretwilson.js.EventManager.addEvent(node, "keypress", onTextInputKeyPress, false);
										com.garretwilson.js.EventManager.addEvent(node, "keydown", onTextInputKeyDown, false);
										com.garretwilson.js.EventManager.addEvent(node, "keyup", onTextInputKeyUp, false);
										break;
									case "checkbox":
									case "radio":
										com.garretwilson.js.EventManager.addEvent(node, "click", onCheckInputChange, false);
										break;
									case "file":
										if(elementClassNames.contains("resourceCollectControl-body"))	//if this is a Guise resource collect control TODO maybe change to the reverse logic (i.e. not ResourceCollectControl)
										{
											com.garretwilson.js.EventManager.addEvent(node, "change", onFileInputChange, false);
										}
										else if(elementClassNames.contains("resourceImportControl-body"))	//if this is a Guise resource import control, we'll later need to submit the form differently
										{
											hasResourceImportControl=true;	//we found a resource import control
										}
										break;
								}
								break;
							case "select":
								com.garretwilson.js.EventManager.addEvent(node, "change", onSelectChange, false);
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
							case "span":
								if(node.getAttribute("guise:patchType")=="temp")	//if this is just a temporary element that should be removed (in anticipation of a later replacement, such as the TinyMCE editor, for example) (IE doesn't let us check this attribute for all elements)
								{
									return false;	//stop initializing and indicate that the element should be deleted
								}
								break;
							case "textarea":
								var contentType=node.getAttribute("guise:contentType");	//get the content type TODO use a constant
								if(contentType=="application/xhtml+xml-external-parsed-entity")	//if this is an XHTML fragment
								{
									var component=Node.getAncestorElementByClassName(node, STYLES.COMPONENT);	//get the component element
									if(component)	//if there is a component
									{
										var componentID=component.id;	//get the component ID
										if(componentID)	//if there is a component ID
										{
/*TODO FCKeditor
											var oFCKeditor = new FCKeditor(componentID) ;
											oFCKeditor.BasePath = GUISE_ASSETS_BASE_PATH+"javascript/fckeditor/";
											oFCKeditor.OnAfterLinkedFieldUpdate=function(){alert("updated!");};
											oFCKeditor.ReplaceTextarea() ;
*/
											
												//if ever dynamic loading is fixed, see using global scope for eval() at http://josephsmarr.com/2007/01/31/fixing-eval-to-use-global-scope-in-ie/
												//see also http://ajaxian.com/archives/evaling-with-ies-windowexecscript#comments
												//dynamic loading TinyMCE via eval() gets an error and sometimes crashes Firefox
											var editor=new tinymce.Editor(node.id, tinyMCE.settings);	//create a new TinyMCE editor
											editor.componentID=componentID;	//indicate the Guise component ID of the editor
											editor.onChange.add(this._onTinyMCEChange.bind(this));
											editor.render();
										}
									}
								}
								else	//if this is a normal text area TODO maybe require text/plain
								{
									com.garretwilson.js.EventManager.addEvent(node, "change", onTextInputChange, false);
									com.garretwilson.js.EventManager.addEvent(node, "keydown", onTextInputKeyDown, false);	//commit the text area on Enter TODO decide whether we want real-time checking with onTextInpuKeyUp, which would be very expensive for text areas
								}
								break;
						}
/*TODO del
						if(elementName.endsWith("object"))
						{
								alert("object element name: "+elementName);
								alert("object: "+node.innerHTML);
						}
*/
						for(var i=elementClassNames.length-1; i>=0; --i)	//for each class name
						{
							switch(elementClassNames[i])	//check out this class name
							{
		/*TODO del
								case "button":	//TODO testing; del
									com.garretwilson.js.EventManager.addEvent(node, "click", onButtonClick, false);	//listen for button clicks
									break;
		*/
								case STYLES.ACTION:
									com.garretwilson.js.EventManager.addEvent(node, "click", onActionClick, false);	//listen for a click on an action element
									com.garretwilson.js.EventManager.addEvent(node, "contextmenu", onContextMenu, false);	//listen for a right click on an action element
									if(isSafari)	//if this is Safari TODO fix better
									{
										node.onclick=function(){return false;};	//cancel the default action, because Safari 1.3.2 ignores event.preventDefault(); http://www.sitepoint.com/article/dhtml-utopia-modern-web-design/3
									}
									break;
								case STYLES.DRAG_HANDLE:
									com.garretwilson.js.EventManager.addEvent(node, "mousedown", onDragBegin, false);	//listen for mouse down on a drag handle
									break;
								case STYLES.MOUSE_LISTENER:
									if(!Node.getAncestorElementByClassName(node.parentNode, STYLES.MOUSE_LISTENER))	//make sure this is the root mouse listener, as we'll allow events to bubble
									{
										com.garretwilson.js.EventManager.addEvent(node, "mouseover", onMouse, false);	//listen for mouse over on a mouse listener
										com.garretwilson.js.EventManager.addEvent(node, "mouseout", onMouse, false);	//listen for mouse out on a mouse listener
									}
									break;
								case STYLES.DROP_TARGET:
									this.addDropTarget(node);	//add this node to the list of drop targets
									break;
								case STYLES.SLIDER_CONTROL_THUMB:
									com.garretwilson.js.EventManager.addEvent(node, "mousedown", onSliderThumbDragBegin, false);	//listen for mouse down on a slider thumb
									break;
							}
						}
						if(node.focus)	//if this element can receive the focus
						{
							com.garretwilson.js.EventManager.addEvent(node, "focus", this._onFocus.bind(this), false);	//listen for focus events; we must do this specifically for each node, because focus events don't focus correctly
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
						var childNode=all[i];	//get this child node
						if(!this._initializeNode(childNode, false, initialInitialization))	//initialize this child node, but not its children; if the node should be removed
						{
							childNode.parentNode.removeChild(childNode);	//remove the child from the tree
							--i;	//account for the child node being removed, because the child node list is live
							--allCount;	//account for the child node being removed, because the child node list is live
						}
					}
				}
				else	//otherwise, walk the tree using the standard W3C DOM routines
				{
						//initialize child nodes
					var childNodeList=node.childNodes;	//get all the child nodes
					var childNodeCount=childNodeList.length;	//find out how many children there are
					for(var i=0; i<childNodeCount; ++i)	//for each child node
					{
						var childNode=childNodeList[i];	//get this child node
						if(!this._initializeNode(childNode, deep, initialInitialization))	//initialize this child subtree; if the node should be removed
						{
							node.removeChild(childNode);	//remove the child from the tree
							--i;	//account for the child node being removed, because the child node list is live
							--childNodeCount;	//account for the child node being removed, because the child node list is live
						}
					}
				}
			}
			return true;	//indicate that initialization was successful
		};
		
		/**Updates the representation of any dynamic components based upon the state of the underlying element.
		Components for the given node and any descendant nodes are updated.
		@param node The node for which components should be updated.
		@param deep true if the entire hierarchy should be initialized.
		*/
		proto._updateComponents=function(node, deep)
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
						this._updateComponents(all[i], false);	//update this component, but not its children
					}
				}
				else	//otherwise, walk the tree using the standard W3C DOM routines
				{
					var childNodeList=node.childNodes;	//get all the child nodes
					var childNodeCount=childNodeList.length;	//find out how many children there are
					for(var i=0; i<childNodeCount; ++i)	//for each child node
					{
						this._updateComponents(childNodeList[i], deep);	//update the components for this child subtree
					}
				}
			}
		};
		
		/**Uninitializes a node and optionally all its children, removing all added listeners.
		@param node The node to uninitialize.
		@param deep true if the entire hierarchy should be uninitialized.
		*/
		proto._uninitializeNode=function(node, deep)	//TODO remove the node from the sorted list of drop targets
		{
			com.garretwilson.js.EventManager.clearEvents(node);	//clear events for this node
			switch(node.nodeType)	//see which type of child node this is
			{
				case Node.ELEMENT_NODE:	//element
					var elementName=node.nodeName.toLowerCase();	//get the element name
					switch(elementName)	//see which element this is
					{
						case "textarea":
							var contentType=node.getAttribute("guise:contentType");	//get the content type TODO use a constant
							if(contentType=="application/xhtml+xml-external-parsed-entity")	//if this is an XHTML fragment
							{
								tinyMCE.execCommand('mceRemoveControl', false, node.id);	//remove TinyMCE
							}
							break;
					}
					break;
			}
			if(deep)	//if we should uninitialize child nodes
			{
				var all=node.all;	//see if the node has an all[] array, because that will be much faster
				if(all)	//if there is an all[] array
				{
					for(var i=all.length-1; i>=0; --i)	//for each descendant node
					{
						this._uninitializeNode(all[i], false);	//uninitialize this child node, but not its children
					}
				}
				else	//otherwise, walk the tree using the standard W3C DOM routines
				{
						//uninitialize child nodes
					var childNodeList=node.childNodes;	//get all the child nodes
					for(var i=childNodeList.length-1; i>=0; --i)	//uninitialize the child nodes in reverse order, because uninitialization can cause elements to be removed (e.g. TinyMCE)
					{
						this._uninitializeNode(childNodeList[i], deep);	//initialize this child subtree
					}
				}
			}
		};

		/**Resets an upload control after completion or cancellation of a resource transfer.
		All the file upload elements except the last will be removed, and the last one will be re-enabled.
		@param element The element representing the resource collect control.
		*/
		proto._resetUploadControl=function(element)
		{
			var childNodeList=element.childNodes;	//get all the child nodes
			var lastFileInput=null;	//we'll keep track of the last file input we find
			for(var i=childNodeList.length-1; i>=0; --i)	//for each child node, going backwards (especially important as we well be removing child nodes)
			{
				var childNode=childNodeList[i];	//get a reference to this child node
				if(childNode.nodeType==Node.ELEMENT_NODE && childNode.nodeName.toLowerCase()=="input" && childNode.type=="file")	//if this is a file input element
				{
					if(lastFileInput==null)	//if we haven't yet found the last file input
					{
						lastFileInput=childNode;	//keep track of the last file input
						childNode.disabled=false;	//re-enable the last file input
					}
					else	//if we've already found the last file input
					{
						this._uninitializeNode(childNode, true);	//uninitialize this file input
						element.removeChild(childNode);	//remove this file element from the document
					}
				}
			}
		};

		/**Called when any element receives the focus.
		@param event The object containing event information.
		*/
		proto._onFocus=function(event)
		{
			var target=event.target;	//get the element receiving the focus
			if(target!=this._lastFocusedNode)	//if the focus is really changing (Firefox seems to send multiple focus events for some elements, such as buttons)
			{
				if(this._modalFrame!=null)	//if there is a modal frame
				{
					if(!Node.hasAncestor(target, this._modalFrame))	//if focus is trying to go to something outside the modal frame
					{
						if(Node.hasAncestor(this._lastFocusedNode, this._modalFrame))	//if we know the last focused node, and it was in the modal frame
						{
							this._lastFocusedNode.focus();	//focus back on the last focused node
						}
						else	//if we don't know the last focused node, or it wasn't in the modal frame
						{
							var focusable=getFocusableDescendant(this._modalFrame);	//see if the modal frame has a node that can be focused TODO this will go away when Guise has better focus support in its component model
							if(focusable)	//if we found a focusable node
							{
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
						return;	//don't process the focus event any further
					}
				}
				this._lastFocusedNode=target;	//this is an allowed focus that isn't outside of a modal frame; keep track of what was last focused
				var component=Node.getAncestorElementByClassName(target, STYLES.COMPONENT);	//get the component element
				if(component)	//if there is a component
				{
					var componentID=component.id;	//get the component ID
					if(componentID)	//if there is a component ID
					{
						if(this.isEnabled())	//if AJAX is enabled
						{
							var ajaxRequest=new FocusAJAXEvent(componentID);	//create a new focus request
							this.sendAJAXRequest(ajaxRequest);	//send the AJAX request
						}
					}
				}
			}
		};

		/**Called when the TinyMCE editor content changes.
		@see http://wiki.moxiecode.com/index.php/TinyMCE:API/tinymce.Editor/onChange
		*/
		proto._onTinyMCEChange=function(editor, undoLevel, undoManager)
		{
			if(guise.isEnabled())	//if AJAX is enabled
			{
//TODO fix				textInput.removeAttribute("guise:attributeHash");	//the text is represented in the DOM by an element attribute, and this has changed, but the attribute hash still indicates the old value, so remove the attribute hash to indicate that the attributes have changed TODO use a constant
//TODO fix				guise.invalidateAncestorContent(editor);	//indicate that the ancestors now have different content TODO make sure this works with the editor
				var ajaxRequest=new ChangeAJAXEvent(editor.componentID, new Map("value", editor.getContent()));	//create a new property change event with the Guise component ID and the new value
				this.sendAJAXRequest(ajaxRequest);	//send the AJAX request
			}
		};

		/**Called when the FCKeditor is blurred.
		@see http://docs.fckeditor.net/FCKeditor_2.x/Developers_Guide/JavaScript_API#Events
		*/
/*TODO FCKeditor
		proto._onFCKeditorBlur=function(editor)
		{
			if(guise.isEnabled())	//if AJAX is enabled
			{
				if(editor.IsDirty())	//only report new content if the content changed
				{
//TODO fix				textInput.removeAttribute("guise:attributeHash");	//the text is represented in the DOM by an element attribute, and this has changed, but the attribute hash still indicates the old value, so remove the attribute hash to indicate that the attributes have changed TODO use a constant
//TODO fix				guise.invalidateAncestorContent(editor);	//indicate that the ancestors now have different content TODO make sure this works with the editor
					editor.ResetIsDirty();	//TODO testing
					var ajaxRequest=new ChangeAJAXEvent(editor.Name, new Map("value", editor.GetData(true)));	//create a new property change event with the Guise component ID and the new value
					this.sendAJAXRequest(ajaxRequest);	//send the AJAX request
				}
			}
		};
*/

		/**Called when the Flash component initializes.*/
		proto._onFlashInitialize=function()
		{
//console.debug("flash initialized");
			setTimeout(this._finishFlashInitialization.bind(this), 100);	//wait a little longer to make sure the Flash component has finished construction and then wrap up the initialization process 
		};

		/**Finishes Flash component initialization by setting the Flash initialization state and calling executing flash functions.*/
		proto._finishFlashInitialization=function()
		{
//console.debug("finishing flash initialization");
			while(this._flashFunctions.length>0)	//while there are more waiting Flash functions (execute waiting flash functions before setting the initialized flag, even though no more flash requests should come along while we're doing this)
			{
				var flashFunction=this._flashFunctions.dequeue();	//get the next Flash function to execute
				flashFunction(this._flash);	//call the Flash function
			}			
			this._flashInitialized=true;	//indicate that Flash support is now initialized			
//console.debug("flash support is now initialized");
		};

		/**Called when the state of a sound changes.
		@param soundID The ID of the sound the state of which is changing.
		@param oldState The old sound state.
		@param newState The new sound state.
		*/
		proto._onSoundStateChange=function(soundID, oldState, newState)
		{
//alert("sound state changed for sound "+soundID+" new state "+newState);
			this.sendAJAXRequest(new ChangeAJAXEvent(soundID, new Map("state", newState)));	//send an AJAX request with the new sound state
		};

		/**Called while a sound is playing.
		@param soundID The ID of the sound that is playing.
		@param position The position in milliseconds.
		@param duration The duration in milliseconds.
		*/
		proto._onSoundPositionChange=function(soundID, position, duration)
		{
			this.sendAJAXRequest(new ChangeAJAXEvent(soundID, new Map("position", position, "duration", duration)));	//send an AJAX request with the new sound position and duration
		};

		/**Called when files are selected by the user.
		@param fileReferenceListID The ID of the file reference list.
		@param fileReferences The array of file references representing selected files; these are not the actual Flash file references, but objects containing copies of relevant file reference data.
		*/
		proto._onFilesSelected=function(fileReferenceListID, fileReferences)
		{
			this.sendAJAXRequest(new ChangeAJAXEvent(fileReferenceListID, {"fileReferences":fileReferences}));	//send an AJAX request with the file references
		}; 

		/**Called while a file is uploading or downloading.
		@param fileReferenceListID The ID of the file reference list.
		@param fileReferenceID The ID of file reference being transferred.
		@param taskState The current state of the transfer.
		@param transferred The current number of bytes transferred, or <code>-1</code> if not known.
		@param total The total or estimated total bytes to transfer, or <code>-1</code> if not known.
		*/
		proto._onFileProgress=function(fileReferenceListID, fileReferenceID, taskState, transferred, total)
		{
			this.sendAJAXRequest(new ChangeAJAXEvent(fileReferenceListID, {"id":fileReferenceID, "taskState":taskState, "transferred":transferred, "total":total}));	//send an AJAX request with the new file progress
		};

	}

	this.httpCommunicator.setProcessHTTPResponse(this._processHTTPResponse.bind(this));	//set up our callback function for processing HTTP responses

};

var guise=new com.guiseframework.js.Guise();	//create a new global variable for the Guise client


//editor supplements

/**The global function that recognizes that an instance of the FCKeditor has been initialized.
@param editor The editor instance that has just completed initializing.
*/
/*TODO FCKeditor
function FCKeditor_OnComplete(editor)
{
//	alert("on complete");
	editor.Events.AttachEvent("OnBlur", guise._onFCKeditorBlur.bind(guise, editor));	//when the editor os blurred, notify Guise
} 
*/

/**The global drag state variable.*/
var dragState;

/**The key codes that are recognized globally and sent to the server as key events.*/
var REPORTED_KEY_CODES=[KEY_CODE.ENTER, KEY_CODE.ESCAPE];

/**The key codes that are canceled globally, whether or not they are reported.*/
var CANCELED_KEY_CODES=[KEY_CODE.ENTER];

/**Called when a key is pressed or released generally.
If any control character is pressed, it is sent to the server as a key event and its default action is canceled.
@param event The object describing the event.
@see REPORTED_KEY_CODES
@see http://www.quirksmode.org/js/keys.html
*/
function onKey(event)
{
	var keyCode=event.keyCode;	//get the code of the pressed key
	if(keyCode==KEY_CODE.ENTER)	//if Enter was pressed
	{
		var target=event.target;	//get the node on which the event occurred
		if(target.nodeType==Node.ELEMENT_NODE)	//if the event occurred on an element
		{
			var targetNodeName=target.nodeName.toLowerCase();	//get the name of the target
			if(targetNodeName=="button" || (targetNodeName=="input" && (target.type=="button" || target.type=="file")))	//if Enter was pressed on a button
			{
				return;	//let the browser handle Enter on a button TODO add checks for other things			
			}
		}
	}
	if(guise.isEnabled())	//if AJAX is enabled
	{
		var keyCode=event.keyCode;	//get the code of the pressed key
		if(REPORTED_KEY_CODES.contains(keyCode))	//if this is a code to report
		{
			var eventType;	//we'll determine the type of AJAX key event to send
			switch(event.type)	//see which type of mouse event this is
			{
				case "keydown":
					eventType=KeyAJAXEvent.EventType.PRESS;
					break;
				case "keydown":
					eventType=KeyAJAXEvent.EventType.RELEASE;
					break;
				default:	//TODO assert an error or warning
					return;				
			}
			var ajaxRequest=new KeyAJAXEvent(eventType, keyCode, Boolean(event.altKey), Boolean(event.ctrlKey), Boolean(event.shiftKey));	//create a new AJAX key event
			window.setTimeout(guise.sendAJAXRequest.bind(guise, ajaxRequest), 1);	//send the AJAX request later; in Firefox 2.0.0.3, sending the request from within the key event will cause the AJAX response to disappear 
		}
		if(CANCELED_KEY_CODES.contains(keyCode))	//if this is a code to canceled
		{
			event.stopPropagation();	//tell the event to stop bubbling
			event.preventDefault();	//prevent the default functionality from occurring
		}
	}
}

/**Called when a key is pressed in a text input.
This implementation checks to see if the Enter key was pressed, and if so commits the input by sending it to the server and canceling the default action.
The Enter keypress is allowed to bubble so that it may be reported to the server.
If the Enter key was pressed for a textinput with guise:multiline="true", the Enter key is stopped from propagating so that it won't be sent back to the server,
but the browser is allowed to process the key normally multiple lines can be entered.
@param event The object describing the event.
@see http://www.quirksmode.org/js/keys.html
@see http://support.microsoft.com/kb/298498
@see #onKey()
*/
function onTextInputKeyDown(event)
{
	var keyCode=event.keyCode;	//get the code of the pressed key
	if(keyCode==KEY_CODE.ENTER)	//if Enter/Return was pressed
	{
		var textInput=event.currentTarget;	//get the control in which text changed
		if(textInput.nodeName.toLowerCase()=="textarea" && textInput.getAttribute("guise:multiline")=="true")	//if this is a multiline text area
		{
			event.stopPropagation();	//tell the event to stop bubbling, so that it won't be sent back to the server, but allow the browser to process the key normally, so that multiple lines can be created in a text area
			return;	//don't do anything; allow the Enter key to function normally
		}
		if(guise.isEnabled())	//if AJAX is enabled
		{
			textInput.removeAttribute("guise:attributeHash");	//the text is represented in the DOM by an element attribute, and this has changed, but the attribute hash still indicates the old value, so remove the attribute hash to indicate that the attributes have changed TODO use a constant
			guise.invalidateAncestorContent(textInput);	//indicate that the ancestors now have different content
			var ajaxRequest=new ChangeAJAXEvent(textInput.name, new Map("value", textInput.value));	//create a new property change event with the control ID and the new value
			guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
			event.preventDefault();	//prevent the default functionality from occurring, but allow it to keep bubbling so that it can be reported back to the server
		}
		else	//TODO submit the form
		{
		}
	}
}

/**Called when a key is raised in a text input.
This implementation sends the current text input value as a provisional value if the pressed key was not the Enter key.
@param event The object describing the event.
*/
function onTextInputKeyUp(event)
{
	var keyCode=event.keyCode;	//get the code of the pressed key
	if(keyCode!=KEY_CODE.ENTER)	//if Enter was pressed, don't do anything for raising the Enter key; we already committed the input in onTextInputKeyDown().
	{
		if(guise.isEnabled())	//if AJAX is enabled
		{
		//TODO del alert("an input changed! "+textInput.id);
			var textInput=event.currentTarget;	//get the control in which text changed
				//TODO decide if we need to remove the attribute hash attribute
			var ajaxRequest=new ChangeAJAXEvent(textInput.name, new Map("provisionalValue", textInput.value));	//create a new property change event with the control ID and the new value
			guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request, but allow this event to be processed normally
		}
	}
}

/**Called when the contents of a text input or a text area changes.
@param event The object describing the event.
*/
function onTextInputChange(event)
{
	if(guise.isEnabled())	//if AJAX is enabled
	{
		var textInput=event.currentTarget;	//get the control in which text changed
		textInput.removeAttribute("guise:attributeHash");	//the text is represented in the DOM by an element attribute, and this has changed, but the attribute hash still indicates the old value, so remove the attribute hash to indicate that the attributes have changed TODO use a constant
		guise.invalidateAncestorContent(textInput);	//indicate that the ancestors now have different content
//TODO del alert("an input changed! "+textInput.id+" value "+textInput.value);
		var ajaxRequest=new ChangeAJAXEvent(textInput.name, new Map("value", textInput.value));	//create a new property change event with the control ID and the new value
		guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
		event.stopPropagation();	//tell the event to stop bubbling
	}
}

/**Called when the contents of a file input.
@param event The object describing the event.
*/
function onFileInputChange(event)
{
	if(guise.isEnabled())	//if AJAX is enabled
	{
		var fileInput=event.currentTarget;	//get the control in which the file changed
		fileInput.removeAttribute("guise:attributeHash");	//the file is represented in the DOM by an element attribute, and this has changed, but the attribute hash still indicates the old value, so remove the attribute hash to indicate that the attributes have changed TODO use a constant
		guise.invalidateAncestorContent(fileInput);	//indicate that the ancestors now have different content
//TODO fix alert("file input changed to value: "+fileInput.value);
		var fileInputValue=fileInput.value;	//get the new value
		if(fileInputValue)	//if the value is changing to something interesting
		{
			var component=Node.getAncestorElementByClassName(fileInput, STYLES.COMPONENT);	//get the component element
			if(component)	//if there is a component
			{
				var componentID=component.id;	//get the component ID
				if(componentID)	//if there is a component ID
				{
					var isDuplicate=false;	//we'll make sure this isn't a duplicate file input before we accept it
					var childNodeList=component.childNodes;	//get all the child nodes
					for(var i=childNodeList.length-1; i>=0 && !isDuplicate; --i)	//for each child node, going backwards, stopping if we find a duplicate
					{
						var childNode=childNodeList[i];	//get a reference to this child node
						if(childNode.nodeType==Node.ELEMENT_NODE && childNode.nodeName.toLowerCase()=="input" && childNode.type=="file")	//if this is a file input element
						{
							if(childNode!=fileInput && childNode.value==fileInput.value)	//if this is a different file input with the same file as our file input
							{
								isDuplicate=true;	//indicate that this is a duplicate value
							}
						}
					}
					if(isDuplicate)	//if this is a duplicate value
					{
						fileInput.value=null;	//remove the value
					}
					else	//if this is not a duplicate value
					{
						var ajaxRequest=new ChangeAJAXEvent(componentID, new Map("resourcePath", fileInput.value));	//create a new property change event with the control ID and the added resource path
						guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
						var fileInputCopy=document.createElementNS("http://www.w3.org/1999/xhtml", "input");	//create a new input element; do *not* clone the element, because IE will clone the event handlers along with it
						fileInputCopy.className=fileInput.className;
						fileInputCopy.name=fileInput.name;
						fileInputCopy.type=fileInput.type;
						fileInputCopy.disabled=fileInput.disabled;
						fileInput.style.display="none";	//hide the old file input
						fileInput.parentNode.appendChild(fileInputCopy);	//insert the new file input after the existing one (now hidden) 
						guise._initializeNode(fileInputCopy, true);	//initialize the new imported file input copy, installing the correct event handlers
					}
				}
			}
		}
		event.stopPropagation();	//tell the event to stop bubbling
	}
}

/**Called when a button is clicked.
@param event The object describing the event.
*/
function onButtonClick(event)
{
	if(hasResourceImportControl)	//if there is a resource import control on the page TODO later change this to submit normal AJAX actions, and send back a message to actually submit the page
	{
		var element=event.currentTarget;	//get the element on which the event was registered
		if(element.id)	//if the button has an ID
		{
			var form=Node.getAncestorElementByName(element, "form");	//get the form ancestor
			//assert form
			if(form.id)	//if the form has an ID
			{
				var actionInputID=form.id.replace(".form", ".input");	//determine the ID of the hidden action input TODO update this to use a constant non-form-relative value
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
	else	//if there is no resource import element, we can submit the action via AJAX normally
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
	if(isUserAgentIE/*TODO find out why this is zero when TinyMCE is in a dialog: && tinyMCE.editors.length>0*/)	//on IE if there are TinyMCE editors in use, make sure their changes are saved before performing any action, because TinyMCE doesn't trigger a change when focus is lost on IE 
	{
		tinyMCE.triggerSave();
	}
/*TODO FCKeditor
	if(typeof FCKeditorAPI!="undefined")
	{
		var fckEditorInstances=FCKeditorAPI.Instances;	//TODO testing
		for(var editorName in fckEditorInstances)
		{
			var editor=fckEditorInstances[editorName];
			if(editor.IsDirty())	//only report new content if the content changed
			{
				//TODO fix				textInput.removeAttribute("guise:attributeHash");	//the text is represented in the DOM by an element attribute, and this has changed, but the attribute hash still indicates the old value, so remove the attribute hash to indicate that the attributes have changed TODO use a constant
				//TODO fix				guise.invalidateAncestorContent(editor);	//indicate that the ancestors now have different content TODO make sure this works with the editor
				editor.ResetIsDirty();	//TODO testing
				var ajaxRequest=new ChangeAJAXEvent(editor.Name, new Map("value", editor.GetData(true)));	//create a new property change event with the Guise component ID and the new value
				guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
			}
		}
	}
*/

	var target=event.currentTarget;	//get the element on which the event was registered
//TODO del alert("action on: "+element.nodeName);
	var component=Node.getAncestorElementByClassName(target, STYLES.COMPONENT);	//get the component element TODO improve all this
	if(component)	//if there is a component
	{
		var componentID=component.id;	//get the component ID
		if(componentID)	//if there is a component ID
		{
			if(guise.isEnabled())	//if AJAX is enabled
			{
				guise.setElementTempCursor(component, "wait");	//change the cursor to "wait" until the AJAX communication is finished
				var ajaxRequest=new ActionAJAXEvent(componentID, componentID, null, 0);	//create a new action request with no action ID and the default option
				guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
			}
/*TODO fix; distinguish between !guise.isEnabled() and AJAX_SUSPENDED; also fix bug on server where an exhaustive post may clear information on non-displayed cards
			else	//if AJAX is not enabled, do a POST
			{
				var form=getForm(component);	//get the form
				if(form && form.id)	//if there is a form with an ID
				{
					var actionInputID=form.id.replace(".form", ".input");	//determine the ID of the hidden action input TODO use a constant, or get these values using a better method
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
			}
*/
			event.stopPropagation();	//tell the event to stop bubbling
			event.preventDefault();	//prevent the default functionality from occurring
		}
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
		var component=Node.getAncestorElementByClassName(target, STYLES.COMPONENT);	//get the component element TODO improve all this
		if(component)	//if there is a component
		{
			var componentID=component.id;	//get the component ID
			if(componentID)	//if there is a component ID
			{
				if(guise.isEnabled())	//if AJAX is enabled
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
					guise.setElementTempCursor(target, "wait");	//change the cursor to "wait" until the AJAX communication is finished

					var ajaxRequest=new ActionAJAXEvent(componentID, targetID, null, 0);	//create a new action request with no action ID and the default option
					guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
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
		var component=Node.getAncestorElementByClassName(target, STYLES.COMPONENT);	//get the component element TODO improve all this
		if(component)	//if there is a component
		{
			var componentID=component.id;	//get the component ID
			if(componentID)	//if there is a component ID
			{
				if(guise.isEnabled())	//if AJAX is enabled
				{
					var ajaxRequest=new ActionAJAXEvent(componentID, targetID, null, 1);	//create a new action request with no action ID and the context option
					guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
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

	var invalidated=false;	//we'll keep track of whether we invalidate this checkInput	
	var name=checkInput.name;	//get the name of the check
	if(name)	//if we know the name of the check
	{
		var groupCheckInputs=document.forms[0][name];	//get all the checkboxes/radio buttons in the form, because being mutually exclusive they all have changed values in the browser
		if(groupCheckInputs && groupCheckInputs.length)	//if there is a group of checkboxes/radio buttons (independent checkboxes will not have groups, for examples)
		{
			invalidated=true;	//if we invalidate the group, we invalidate this checkbox, too
			for(var i=groupCheckInputs.length-1; i>=0; --i)	//for each check
			{
				var groupCheckInput=groupCheckInputs[i];	//get this group check
				groupCheckInput.removeAttribute("guise:attributeHash");	//the checked status is represented in the DOM by an element attribute, and this has changed, but the attribute hash still indicates the old value, so remove the attribute hash to indicate that the attributes have changed TODO use a constant
				guise.invalidateAncestorContent(groupCheckInput);	//indicate that the ancestors now have different content
			}
		}
	}
	if(!invalidated)	//if we didn't invalidate this checkbox
	{
		checkInput.removeAttribute("guise:attributeHash");	//the checked status is represented in the DOM by an element attribute, and this has changed, but the attribute hash still indicates the old value, so remove the attribute hash to indicate that the attributes have changed TODO use a constant
		guise.invalidateAncestorContent(checkInput);	//indicate that the ancestors now have different content
	}
	if(guise.isEnabled())	//if AJAX is enabled
	{
		guise.setElementTempCursor(checkInput, "wait");	//change the cursor to "wait" until the AJAX communication is finished
		var ajaxRequest=new ChangeAJAXEvent(checkInput.value, new Map("value", checkInput.checked));	//create a new property change event with the new value, indicating the control ID (which is specified in the value of the control)
		guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
		event.stopPropagation();	//tell the event to stop bubbling
	}
/*TODO fix; distinguish between !guise.isEnabled() and AJAX_SUSPENDED; also fix bug on server where an exhaustive post may clear information on non-displayed cards
	else	//if AJAX is not enabled
	{
		if(Node.getAncestorElementByClassName(checkInput, STYLES.MENU_BODY))	//if this check is inside a menu, submit the form so that menus will cause immediate reaction
		{
			var form=getForm(element);	//get the form
			if(form)	//if there is a form
			{
				form.submit();	//submit the form		
			}
		}
	}
*/
}

/**Called when the mouse clicks any element.
The event will be reported to the server using a MouseAJAXEvent of type MouseAJAXEvent.EventType.CLICK.
@param event The object describing the event.
*/
function onClick(event)
{
	var target=event.target;	//get the element being clicked 
	if(target.nodeType==Node.ELEMENT_NODE)	//if the event occurred on an element
	{
		var targetNodeName=target.nodeName.toLowerCase();	//get the name of the target
		if(targetNodeName=="input")	//if an input was clicked
		{
			var type=target.type;	//discover the type
			if(type=="checkbox" || type=="radio" || type=="file")	//if this was checkbox, radio button, or a file input
			{
				return;	//let the browser handle these mouse clicks TODO add checks for other things			
			}
		}
		if(targetNodeName=="a"	//if a link was clicked
			|| (targetNodeName=="label" && target.htmlFor))	//or if a label was clicked and the label was for a particular control TODO maybe process even these, once we completely implement server-push focusing
		{
			return;	//let the browser handle these mouse clicks TODO add checks for other things			
		}
	}
	var component=Node.getAncestorElementByClassName(target, STYLES.COMPONENT);	//get the component element
	if(component)	//if there is a component
	{
		var componentID=component.id;	//get the component ID
		if(componentID)	//if there is a component ID
		{
			if(guise.isEnabled())	//if AJAX is enabled
			{
				var button=event.button;	//get the button pressed
				if(isUserAgentIE)	//if this is an IE browser, change the event button to match the W3C's definition; see http://www.quirksmode.org/dom/w3c_events.html
				{
					if(button&1)	//if this was the left button
					{
						button=MOUSE_BUTTON.LEFT;
					}
					else if(button&2)	//if this was the right button
					{
						button=MOUSE_BUTTON.RIGHT;
					}
					else if(button&4)	//if this was the middle button
					{
						button=MOUSE_BUTTON.MIDDLE;
					}
				}
				var ajaxRequest=new MouseAJAXEvent(MouseAJAXEvent.EventType.CLICK, component, target, event.clientX, event.clientY, Boolean(event.altKey), Boolean(event.ctrlKey), Boolean(event.shiftKey), button, 1);	//create a new AJAX mouse event
				guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
				event.stopPropagation();	//tell the event to stop bubbling
				event.preventDefault();	//prevent the default functionality from occurring
			}
		}
	}
}

/**Called when a select control changes.
@param event The object describing the event.
*/
function onSelectChange(event)
{
	if(guise.isEnabled())	//if AJAX is enabled
	{
		var select=event.currentTarget;	//get the control to which the listener was listening
		var component=Node.getAncestorElementByClassName(select, STYLES.COMPONENT);	//get the component element
		if(component)	//if there is a component
		{
			var componentID=component.id;	//get the component ID
			if(componentID)	//if there is a component ID
			{
		//TODO del		select.removeAttribute("guise:contentHash");	//indicate that the select's children have changed TODO use a constant
		//TODO del when works		var selectName=select.name;	//get the name of the control
			//TODO del alert("a select changed! "+select.id);
				var selectedIDs=new Array();	//create an array to hold the selected values
				var options=select.options;	//get the select options
				for(var i=0; i<options.length; ++i)	//for each option
				{
					var option=options[i];	//get this option
					if(option.selected)	//if this option is selected
					{
						option.removeAttribute("guise:attributeHash");	//the option selected status is represented in the DOM by an element attribute, and this has changed, but the attribute hash still indicates the old value, so remove the attribute hash to indicate that the attributes have changed TODO use a constant
						guise.invalidateAncestorContent(option);	//indicate that the ancestors now have different content
							//TODO dirty the unselected option
						selectedIDs.add(option.value);	//add the selected value (which is an ID of the selected value)
					}
				}
				var ajaxRequest=new ChangeAJAXEvent(componentID, new Map("selectedIDs", selectedIDs));	//create a new property change event with select ID and the new selected values
				guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
				event.stopPropagation();	//tell the event to stop bubbling
			}
		}
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
		var dragSource=Node.getAncestorElementByClassName(dragHandle, STYLES.DRAG_SOURCE);	//determine which element to drag
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
			var dragSourceComponent=Node.getAncestorElementByClassName(dragState.dragSource, STYLES.COMPONENT);	//get the component element TODO improve all this; decide if we want the dropTarget style on the component element or the drop target subcomponent, and how we want to relate that to the component ID
			var dropTargetComponent=Node.getAncestorElementByClassName(dropTarget, STYLES.COMPONENT);	//get the component element TODO improve all this; decide if we want the dropTarget style on the component element or the drop target subcomponent, and how we want to relate that to the component ID
			if(dragSourceComponent && dropTargetComponent)	//if there source and target components
			{
				var ajaxRequest=new DropAJAXEvent(dragState, dragSourceComponent, dropTargetComponent, event);	//create a new AJAX drop event TODO probably remove the dragState parameter
				guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
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
		var slider=Node.getAncestorElementByClassName(thumb, STYLES.SLIDER_CONTROL);	//find the slider
		var track=Node.getAncestorElementByClassName(thumb, STYLES.SLIDER_CONTROL_TRACK);	//find the slider track
		
		
//TODO find out why the slider track gets constantly reloaded in IE6
//TODO we need to make sure the slider is fully loaded (which may not be as relevant once IE6 no longer constantly reloads images)		
		
		var positionID=slider.id+"-position";	//TODO use constant
		var positionInput=document.getElementById(positionID);	//get the position element		
		if(slider && track && positionInput)	//if we found the slider and the slider track
		{
			var isHorizontal=Element.hasClassName(track, STYLES.AXIS_X);	//see if this is a horizontal slider
			dragState=new DragState(thumb, event.clientX, event.clientY);	//create a new drag state
			dragState.dragCopy=false;	//drag the actual element, not a copy
			if(isHorizontal)	//if this is a horizontal slider
			{
//console.log("This is a horizontal slider.");
				dragState.allowY=false;	//only allow horizontal dragging
				var min=0;	//calculate the minimum
				var max=track.offsetWidth-thumb[GUISE_STATE_WIDTH_ATTRIBUTE]+1;	//calculate the maximum
//console.log("This is a horizontal slider", min, max);
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
//console.log("using drag span", span);
			dragState.onDragBegin=function(element)	//when dragging begins, send a slideBegin action event
					{
//console.log("drag begin");
						updateSlider(slider);	//update the slider view
						var ajaxRequest=new ActionAJAXEvent(slider.id, thumb.id, "slideBegin", 0);	//create a new action request for sliding begin TODO use a constant TODO why are we sending an event back here?
						guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
					}
			dragState.onDrag=function(element, x, y)	//when dragging occurs, update the slider value
					{
//console.log("on drag begin");
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
						var ajaxRequest=new ChangeAJAXEvent(slider.id, new Map("position", position));	//create a new property change event with the control ID and the new value
						guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
					};
			dragState.onDragEnd=function(element)	//when dragging ends, update the slider view to make sure it is synchronized with the updated value
					{
//console.log("drag end");
						var ajaxRequest=new ActionAJAXEvent(slider.id, thumb.id, "slideEnd", 0);	//create a new action request for sliding end TODO use a constant	//why are we sending back an action event here?
						guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
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
	if(Element.hasClassName(slider, "sliding"))	//if the slider is in a sliding state according to the server (i.e. the thumb is being manually moved by the user) TODO use a constant
	{
		return;	//don't update the slider while the server still thinks the slider is sliding
	}
	var track=Node.getDescendantElementByClassName(slider, STYLES.SLIDER_CONTROL_TRACK);	//find the slider track
	var thumb=Node.getDescendantElementByClassName(slider, STYLES.SLIDER_CONTROL_THUMB);	//find the slider thumb
	if(dragState && dragState.dragging && dragState.dragSource==thumb)	//if the slider thumb is being dragged (i.e. the browser thinks the slider is being dragged)
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
	var positionID=slider.id+"-position";	//TODO use constant
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
		var isHorizontal=Element.hasClassName(track, STYLES.AXIS_X);	//see if this is a horizontal slider
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
//alert("thumb.style.left: "+thumb.style.left);
		}
		else	//if this is a vertical slider
		{
			thumb.style.top=newCoordinate+"px";	//update the vertical position of the slider
		}
//TODO del		alert("ready to update slider "+slider.id+" with value:"+positionInput.value);
	}
}

/**The set of component IDs for which the mouse is recorded as being over.*/
var mouseOverComponentIDs=new Object();

/**Called when the mouse enters or exits a mouse listener.
If the current target element has a "mouseListener" class, the event will be reported to the server.
Currently mouse events are implemented to perform the following functionality:
mouseout: A MouseAJAXEvent.EventType.EXIT event is fired, for every element in target to root order, for every STYLES.MOUSE_LISTENER that the mouse is really no longer over. 
mouseover: A MouseAJAXEvent.EventType.ENTER event is fired, for every element in root to target order, for every STYLES.MOUSE_LISTENER that the mouse is over that it wasn't before. 
@param event The object describing the event.
*/
function onMouse(event)
{
	var target=event.target;	//get the target of the event
	var relatedTarget=event.relatedTarget;	//see which element the mouse is going to or from
//console.log("mouse target", target, "related target", relatedTarget);
	if(relatedTarget)	//if there is a related target, see if we should ignore the event
	{
		var ignoreEvent=target==relatedTarget;	//ignore the event if is generated by leaving the same element, as Mozilla does (TODO probably see if relatedTarget is a child of current target)
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
	if(target.nodeName.toLowerCase()=="img")	//if this is an image, perform rollovers if needed
	{
		var rolloverSrc=target.getAttribute("guise:rolloverSrc");	//get the image rollover, if there is one
		if(rolloverSrc)	//if the image has a rollover TODO use a constant; maybe use hasAttributeNS()
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
			event.stopPropagation();	//tell the event to stop bubbling
			event.preventDefault();	//prevent the default functionality from occurring
			return;	//don't send any AJAX event for the image rollover
		}
	}
	var currentTarget=event.currentTarget;	//get the element on which this event listener was registered
	var component=Node.getAncestorElementByClassName(Node.getAncestorElementByClassName(target, STYLES.MOUSE_LISTENER), STYLES.COMPONENT);	//we'll only report mouse in/out events to the top-most component that is the mouse listener
	var otherComponent=Node.getAncestorElementByClassName(Node.getAncestorElementByClassName(relatedTarget, STYLES.MOUSE_LISTENER), STYLES.COMPONENT);	//get the component element of the other mouse listener
	if(component && component!=otherComponent)	//if we know the component, and the mouse isn't simply moving around inside the same component TODO at some point remove the whole mouse listener class and record events for all components
	{
		var componentID=component.id;	//get the component ID
		switch(event.type)	//see which type of mouse event this is
		{
			case "mouseover":	//if we are entering a component
//console.log("got mouse over component ID ", componentID, " mouseOverComponentIDs: ", JSON.serialize(mouseOverComponentIDs));
				var ancestorComponents=Node.getAncestorElementsByClassName(component, STYLES.COMPONENT);	//get an array of all ancestor components, including this component, in current-to-root order
//guise.trace("got mouse over component ID: ", componentID, "with ancestor component count", ancestorComponents.length, "will look up to to target", currentTarget.id);
				for(var i=ancestorComponents.length-1; i>=0; --i)	//for each ancestor, from root to this one
				{
//guise.trace("index", i, "of", ancestorComponents.length);
					var ancestorComponent=ancestorComponents[i];	//get this ancestor
//guise.trace("looking at ancestor component", ancestorComponent.id);
					var ancestorComponentID=ancestorComponent.id;	//get the ID of this ancestor
//guise.trace("ancestor component", ancestorComponentID, "is not below target");
					if(!mouseOverComponentIDs[ancestorComponentID])	//if we haven't already recorded the mouse as being over this ancestor
					{
						mouseOverComponentIDs[ancestorComponentID]=true;	//record the mouse as being over this ancestor
						if(Element.hasClassName(ancestorComponent, STYLES.MOUSE_LISTENER))	//if this component wants to be notified of mouse events
						{
//console.log("sending mouse over for component: ", ancestorComponentID);
							var ajaxRequest=new MouseAJAXEvent(MouseAJAXEvent.EventType.ENTER, ancestorComponent, target, event.clientX, event.clientY, Boolean(event.altKey), Boolean(event.ctrlKey), Boolean(event.shiftKey));	//create a new AJAX mouse event for this ancestor
							guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
						}
					}
				}
				event.stopPropagation();	//tell the event to stop bubbling
				event.preventDefault();	//prevent the default functionality from occurring
//console.log("finished mouse over, mouseOverComponentIDs: ", JSON.serialize(mouseOverComponentIDs));
				break;
			case "mouseout":	//if we are leaving a component
//console.log("got mouse out of component ID ", componentID, " other component ID ", (otherComponent!=null ? otherComponent.id : "none"), " mouseOverComponentIDs: ", JSON.serialize(mouseOverComponentIDs));
				var ancestorComponents=Node.getAncestorElementsByClassName(component, STYLES.COMPONENT);	//get an array of all ancestor components, including this component, in current-to-root order
//console.log("mouse really leaving hierarchy, ancestor components number ", ancestorComponents.length);
				for(var i=0, length=ancestorComponents.length; i<length; ++i)	//for each ancestor, from this one to root
				{
					var ancestorComponent=ancestorComponents[i];	//get this ancestor
//console.log("ancestor component: ", ancestorComponent.id);
					if(!Node.hasAncestor(otherComponent, ancestorComponent))	//if the mouse goes to another subtree of a parent, make sure we don't invalidate the root of the subtree the mouse is still over (this also addresses the case where the mouse moves to a child element; see http://www.quirksmode.org/js/events_mouse.html#mouseover )
					{
//console.log("ancestor component: ", ancestorComponent.id, " is not an ancestor of the other component");
						if(Element.hasClassName(ancestorComponent, STYLES.MOUSE_LISTENER))	//if this component wants to be notified of mouse events
						{
							var ancestorComponentID=ancestorComponent.id;	//get the ID of this ancestor
							if(mouseOverComponentIDs[ancestorComponentID])	//if the mouse is marked as still being over this ancestor
							{
//console.log("-removing and sending back ancestor component: ", ancestorComponentID);
								delete mouseOverComponentIDs[ancestorComponentID];	//indicate that the mouse is no longer over this component					
								var ajaxRequest=new MouseAJAXEvent(MouseAJAXEvent.EventType.EXIT, ancestorComponent, target, event.clientX, event.clientY, Boolean(event.altKey), Boolean(event.ctrlKey), Boolean(event.shiftKey));	//create a new AJAX mouse event for this ancestor
								guise.sendAJAXRequest(ajaxRequest);	//send the AJAX request
							}
						}
					}
				}
//console.log("finished mouse out, mouseOverComponentIDs: ", JSON.serialize(mouseOverComponentIDs));
				event.stopPropagation();	//tell the event to stop bubbling
				event.preventDefault();	//prevent the default functionality from occurring
				break;
		}
	}	
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
var FOCUSABLE_ELEMENT_NAMES=["button", "input", "select", "textarea"];

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

com.garretwilson.js.EventManager.addEvent(window, "load", guise.onLoad.bind(guise), false);	//do the appropriate initialization when the window loads
