/*
 * Copyright © 2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

/**
 * XHTML Phrases TinyMCE plugin.
 * 
 * This plugin relies on a method <code>tinymce.Editor.prototype.insertElement(elementName)</code> being defined elsewhere.
 * 
 * Modified from XHTMLxtras TinyMCE Plugin, Copyright © 2004-2008, Moxiecode Systems AB.
 *
 * @author Garret Wilson
 */
(function() {
	tinymce.PluginManager.requireLangPack('xhtmlphrases');
	tinymce.create('tinymce.plugins.XHTMLPhrasesPlugin',
	{
		init:function(ed, url)
		{
			ed.addCommand('mceComputerCode', function(ui, v)
					{
						ed.insertElement("code");
					});
			ed.addCommand('mceDfn', function(ui, v)
					{
						ed.insertElement("dfn");
					});
			ed.addCommand('mceKbd', function(ui, v)
					{
						ed.insertElement("kbd");
					});
			ed.addCommand('mceSamp', function(ui, v)
					{
						ed.insertElement("samp");
					});
			ed.addCommand('mceVar', function(ui, v)
					{
						ed.insertElement("var");
					});
			ed.addButton('computercode', {title : 'xhtmlphrases.computercode_desc', cmd : 'mceComputerCode', image : url + '/img/code.gif'});
			ed.addButton('dfn', {title : 'xhtmlphrases.dfn_desc', cmd : 'mceDfn', image : url + '/img/dfn.gif'});
			ed.addButton('kbd', {title : 'xhtmlphrases.kbd_desc', cmd : 'mceKbd', image : url + '/img/kbd.gif'});
			ed.addButton('samp', {title : 'xhtmlphrases.samp_desc', cmd : 'mceSamp', image : url + '/img/samp.gif'});
			ed.addButton('var', {title : 'xhtmlphrases.var_desc', cmd : 'mceVar', image : url + '/img/var.gif'});
			ed.onNodeChange.add(function(ed, cm, n, co)
					{
						n = ed.dom.getParent(n, 'CODE,DFN,KBD,SAMP,VAR');
						cm.setDisabled('computercode', co);
						cm.setDisabled('dfn', co);
						cm.setDisabled('kbd', co);
						cm.setDisabled('samp', co);
						cm.setDisabled('var', co);
						if (n) {
							var name=n.nodeName.toLowerCase();
							if(name=="code")
							{
								name="computercode";
							}
							cm.setDisabled(name, 0);
							cm.setActive(name, 1);
						} else {
							cm.setActive('computercode', 0);
							cm.setActive('dfn', 0);
							cm.setActive('kbd', 0);
							cm.setActive('samp', 0);
							cm.setActive('var', 0);
						}
					});
		},
		getInfo : function()
		{
			return {
				longname : "XHTML Phrases Plugin",
				author : "GlobalMentor, Inc.",
				authorurl : "http://www.globalmentor.com/",
				version : "1.0"
			};
		}
	});
	tinymce.PluginManager.add('xhtmlphrases', tinymce.plugins.XHTMLPhrasesPlugin);
})();