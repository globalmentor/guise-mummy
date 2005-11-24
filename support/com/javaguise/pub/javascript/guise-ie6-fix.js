/**Guise(TM) IE6 bug-fix JavaScript routines
Copyright (c) 2005 GlobalMentor, Inc.
This file should be included after the main guise.js file and after all stylesheet references.
*/

/**A class for representing a selector for a single element (which can be a component in a nested element selector).
@param selectorText The string form of the element selector.
var selectorText The string form of the element selector.
var ie6FixSelectorText The selector text to use in IE6.
var ie6FixClassName The selector class to use in an element on IE6, or null if no special selector class is needed on IE6.
var elementName The name of the element selected, or null if no element name was selected.
var classes The classes selected by the selector.
*/
function CSSElementSelector(selectorText)
{
	this.selectorText=selectorText;	//save the selector text
	this.classes=selectorText.split(".");	//get the name and classes of this selector
//TODO del	var elementName=this.classes.remove(0).toLowerCase();	//the first "selector" was an element name, so remove it (changing it to lowercase in case some browser put it in uppercase)
	var elementName=this.classes.remove(0);	//the first "selector" was an element name, so remove it
	this.elementName=elementName ? elementName : null;	//if there was no element name (i.e. the string started with "."), the element name will be empty
/*TODO del
	if(!selectorText.startsWith(".") && this.classes.length>0)	//if the selector didn't start with a class selector
	{
		this.elementName=this.classes.remove(0).toLowerCase();	//the first "selector" was an element name, so remove it and save it (changing it to lowercase in case some browser put it in uppercase)
	}
	else	//if there was no element name
	{
		this.elementName=null;	//show that there was no element name
	}
*/
	if(this.classes.length>1)	//if there are multiple classes being selected
	{
		this.ie6FixClassName=GuiseIE6Fix.GUISE_IE6_CLASS_PREFIX+"-"+this.classes.join("-");	//create a new class in the form guiseIE6-class1-class2-class3
		this.ie6FixSelectorText=elementName+"."+this.ie6FixClassName;	//create new selector text in the form elementName.guiseIE6-class1-class2-class3 (use the removed element name, which is never null)
	}
	else	//if multiple classes are not being selected
	{
		this.ie6FixClassName=null;	//don't create a special IE 6 class
		this.ie6FixSelectorText=this.selectorText;	//use the standard selector text
	}
	if(!CSSElementSelector.prototype._initialized)
	{
		CSSElementSelector.prototype._initialized=true;

		/**Determines if the given element is selected by this element selector.
		@param element The element to check.
		@param elementClassNames The array of element class names, or null if they have not yet been calculated.
		@return true if this selector selects the given element.
		*/
		CSSElementSelector.prototype.selects=function(element, elementClassNames)
		{
			if(this.elementName!=null && this.elementName!=element.nodeName.toLowerCase())	//if this selector doesn't match the element name
				return false;	//the element name doesn't match
			if(!elementClassNames)	//if no element class names have been calculated
			{
				elementClassNames=element.className ? element.className.split(/\s/) : EMPTY_ARRAY;	//split out the class names
			}
			for(var i=this.classes.length-1; i>=0; --i)	//for each class
			{
				if(!elementClassNames.contains(this.classes[i]))	//if this class name isn't included
				{
					return false;	//this element is missing a class name
				}
			}
			return true;	//the element passed all the tests
		};

	}
}

/**A class for representing a selector.
@param selectorText The string form of the selector.
var selectorText The string form of the selector.
var elementSelectors The CSSElementSelector objects representing each element selector in the selector.
var isMultipleClassSelector Whether one of the element selectors selects on multiple classes.
*/
function CSSSelector(selectorText)
{
	this.selectorText=selectorText;	//save the selector text
	var elementSelectors=selectorText.split(/\s/);
	this.elementSelectors=new Array(elementSelectors.length);	//create a new array in which to hold each element selector object
	var hasMultipleClassSelector=false;	//we'll determine if one of the element selectors selects on multiple classes
	var ie6FixSelectorTexts=new Array();	//create an array of IE6 selector text
	for(var elementSelectorIndex=0; elementSelectorIndex<elementSelectors.length; ++elementSelectorIndex)
	{
		var elementSelector=new CSSElementSelector(elementSelectors[elementSelectorIndex]);	//create an element selector object from this element selector string
		this.elementSelectors[elementSelectorIndex]=elementSelector;	//save the element selector
		ie6FixSelectorTexts.add(elementSelector.ie6FixSelectorText);	//save the IE6 selector text for this selector
		if(elementSelector.classes.length>1)	//if this element selector selects on multiple classes
		{
			this.isMultipleClassSelector=true;	//show that this is a multiple-class selector
		}
	}
	this.ie6FixSelectorText=ie6FixSelectorTexts.join(" ");	//join the element IE6 selector texts with spaces to get the overall IE6 selector text

	if(!CSSSelector.prototype._initialized)
	{
		CSSSelector.prototype._initialized=true;

		/**Determines whether the given element is included by the selector (i.e. it is part of the selection, if not the ultimate selection).
		@param The element to check.
		@return true if the given element is included in the selection path.
		*/
		CSSSelector.prototype.includes=function(element)
		{
			for(var i=this.elementSelectors.length-1; i>=0; --i)	//for each element selector
			{
				if(this.elementSelectors[i].selects(element))	//if the element is selected by this element selector
				{
					return true;	//the element is included in the selector
				}
			}
			return false;	//indicate that no element selector matched the given element
		};
	}
}

/**

var cssMultiClassSelectors: An array of CSSSelector objects that include a multiple class selection.
*/
function GuiseIE6Fix(selectorText)
{
	this.cssMultipleClassSelectors=new Array();	//create a new array of selectors

	//TODO del var selectorText="a b.c d.e.f .g";

	var httpCommunicator=new HTTPCommunicator();	//create a communicator to connect back to the server

	for(var stylesheetIndex=0; stylesheetIndex<document.styleSheets.length; ++stylesheetIndex)	//for each stylesheet
	{
		var stylesheet=document.styleSheets[stylesheetIndex];	//get a reference to this stylesheet
//TODO del alert("stylesheet: "+stylesheet.href);

		var xmlHTTP=httpCommunicator.get(stylesheet.href);
		if(xmlHTTP.status==200)	//if we were able to load the stylesheet
		{
			var stylesheetText=xmlHTTP.responseText;	//get the text of the stylesheet
//TODO del alert("finished communicating; stylesheet text: "+stylesheetText);
			var noComments=stylesheetText.replace(/\/\*(.|[\r\n])*?\*\//gm, "");	//remove comments (see http://ostermiller.org/findcomment.html)
//TODO del alert("no comments: "+noComments);
			var noDefinitions=noComments.replace(/\{(.|[\r\n])*?\}/gm, ",");	//replace definitions with commas
//TODO del alert("no definitions: "+noDefinitions);
			var selectors=noDefinitions.split(/\s*,\s*/m);	//split out the selectors
			var rules=stylesheet.cssRules ? stylesheet.cssRules : stylesheet.rules;	//get a reference to the stylesheet rules, compensating for IE
			if(selectors.length!=rules.length)	//if we don't recognize as many selectors as IE recognizes, we're in trouble
			{
				alert("We found selector count "+selectors.length+", but IE has rule count: "+rules.length);	//TODO fix
				continue;
			}
			for(var ruleIndex=0; ruleIndex<rules.length; ++ruleIndex)	//for each rule in this stylesheet
			{
				var rule=rules[ruleIndex];	//get a reference to this rule
//TODO fix					var selectorText=rule.selectorText;	//get the selector text
				var selectorText=selectors[ruleIndex];	//get our own version of the selector text, because IE throws away some information for multiple-class selectors
//alert("looking at selector: "+selectorText);	
				var cssSelector=new CSSSelector(selectorText);	//create a selector from this selector text
				if(cssSelector.isMultipleClassSelector)	//if this is a multi-class selector
				{
					this.cssMultipleClassSelectors.add(cssSelector);	//add this selector to our list of multiple class selector
					var cssText=rule.style.cssText;	//get the text of the rule (even though Danny Goodman's _JavaScript Bible_, Fifth Edition says that IE doesn't support this property
//TODO del alert("before: cssText: "+cssText);
/*TODO fix					
					for(var property in rule.style)	//for each property in the original style
					{
if(style[property])
{
alert("property: "+property+" is string "+(property instanceof String));
alert("property to string: "+property.toString());
alert("test is uppercase : "+("test".isUpperCase()));
alert("property to string is uppercase: "+property.toString().isUpperCase());
//TODO fix	if(!property.toString().isUpperCase())
alert("property: "+property+": "+style[property]);
}
//TODO fix						newRule.style[property]=rule.style[property];	//copy over this style property
					}
*/
//					alert("ready to substitute "+rule.selectorText+" with "+cssSelector.ie6FixSelectorText);
					stylesheet.removeRule(ruleIndex);	//remove the rule at this index (using an IE6-specific method)
					stylesheet.addRule(cssSelector.ie6FixSelectorText, cssText, ruleIndex);	//add a new rule in its place with the new selector text (using an IE6-specific method)
//TODO del					var newRule=rules[ruleIndex];	//get a reference to the new rule
//TODO del alert("new rule selector text: "+newRule.selectorText);
//alert("new rule style: "+newRule.style);




//TODO fix					newRule.style=style;
/*TODO fix
					for(var property in rule.style)	//for each property in the original style
					{
alert("property: "+property+": "+rule.style[property]);
//TODO fix						newRule.style[property]=rule.style[property];	//copy over this style property
					}
*/
					
					
//TODO fix; doesn't work					rules[ruleIndex].style=rule.style;	//use the same style as the old rule had
//TODO del alert("new selector text: "+rules[ruleIndex].selectorText);
					
//					rule.selectorText=cssSelector.ie6FixSelectorText;	//change the selector to the special IE6 version
//alert("new selector text: "+rule.selectorText);
/*TODO del when works
						cssMultiClassSelectors
						alert("multi selector: "+cssSelector.selectorText);
						alert("element selector: "+elementSelector.selectorText+" consisting of "+elementSelector.classes[0]+" and "+elementSelector.classes[1]);
*/
				}
			}
		}
	}


	if(!GuiseIE6Fix.prototype._initialized)
	{
		GuiseIE6Fix.prototype._initialized=true;

		/**Check to see if this element is affected by IE6 selection bugs and, if so, updates the classes so that the class may be properly selected.
		@param element The element to fix.
		*/
		GuiseIE6Fix.prototype.fixElementClassName=function(element)
		{
			var elementClassName=element.className;	//get the element's class name
			var fixedElementClassName=this.getFixedElementClassName(element, elementClassName);	//get the fixed version of the class name
			if(fixedElementClassName!=elementClassName)	//if the class name changed
			{
				element.className=fixedElementClassName;	//update the element class name
//TODO fix alert("affected element "+element.nodeName+" id "+element.id+" with new class "+element.className);
			}
		};

		/**Retrieves the fixed form of the element class name.
		@param element The element to fix.
		@param elementClassName The proposed class name of the element.
		@return The fixed class name, which may or may not be different than the proposed class name.
		*/
		GuiseIE6Fix.prototype.getFixedElementClassName=function(element, elementClassName)
		{
			var classNameModified=false;	//we'll note whether we modifiy the class names
			var elementClassNames=elementClassName ? elementClassName.split(/\s/) : EMPTY_ARRAY;	//split out the class names
				//remove the IE6 fix classes, if any are already there
			for(var classNameIndex=elementClassNames.length-1; classNameIndex>=0; --classNameIndex)	//for each class (looking backwards, so that item removal will not corrupt iteration)
			{
				var className=elementClassNames[classNameIndex];	//get a reference to this class name
				if(className.startsWith(GuiseIE6Fix.GUISE_IE6_CLASS_PREFIX))	//if this is a special Guise IE6 fix class name
				{
					elementClassNames.remove(classNameIndex);	//remove this special Guise IE6 fix class name
					classNameModified=true;	//note the we modified the class name
				}
			}
			for(var selectorIndex=this.cssMultipleClassSelectors.length-1; selectorIndex>=0; --selectorIndex)	//for each multiple class selector
			{
				var selector=this.cssMultipleClassSelectors[selectorIndex];	//get a reference to this selector
				var elementSelectors=selector.elementSelectors;	//get the element selectors for this selector
				for(var elementSelectorIndex=elementSelectors.length-1; elementSelectorIndex>=0; --elementSelectorIndex)	//for each element selector
				{
					var elementSelector=elementSelectors[elementSelectorIndex];	//get a reference to this element selector
					if(elementSelector.selects(element, elementClassNames))	//if this element selector selects this element
					{
//TODO fix	alert("affected element "+element.nodeName+" id "+element.id+" with class "+element.className);
						elementClassNames.add(elementSelector.ie6FixClassName);	//add the IE6 fix class for this selector
						classNameModified=true;	//note the we modified the class name
					}
				}
			}
			if(classNameModified)	//if we touched up the class names
			{
				elementClassName=elementClassNames.join(" ");	//join the class names back together and assign them back to the class name
//TODO fix alert("affected element "+element.nodeName+" id "+element.id+" with new class "+element.className);
			}
			return elementClassName;	//return the fixed class name
		};

		/**Regular expression for matching individual element selector segments.*/
//TODO del		GuiseIE6Fix.prototype.ELEMENT_SELECTOR_REGEXP=/\s*(\S+)\s*/g;
	}

};

/**The prefix for Guise IE6 fixup classes.*/
GuiseIE6Fix.GUISE_IE6_CLASS_PREFIX="guiseIE6";

var guiseIE6Fix=new GuiseIE6Fix();	//create and initialize the IE 6 fix routines
