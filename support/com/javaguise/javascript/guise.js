//Guise(TM) JavaScript support routines
//Copyright (c) 2005 GlobalMentor, Inc.
//Modal window support referenced code by Danny Goodman, http://www.dannyg.com/ .

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
		document.getElementById(menu.closeID).style.display="none";	//close the menu
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
	document.getElementById(id).style.display="block";	//show thismenu
}

function onMenuMouseOut(id)
{
	menu.closeID=id;	//show that this menu should be closed
	menu.timeout=window.setTimeout('closeMenu()', 1000);	//close the menu after a pause
}