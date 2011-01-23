/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework.model;

import com.globalmentor.net.ContentType;
import com.globalmentor.beans.*;
import com.globalmentor.text.Text;
import com.globalmentor.text.xml.xhtml.XHTML;

/**Base interface for all component models.
@author Garret Wilson
*/
public interface Model extends PropertyBindable, PropertyConstrainable
{

	/**A content type of <code>text/plain</code>.*/
	public final static ContentType PLAIN_TEXT_CONTENT_TYPE=Text.TEXT_PLAIN_CONTENT_TYPE;

	/**A content type of <code>application/xhtml+xml</code>.*/
	public final static ContentType XHTML_CONTENT_TYPE=XHTML.XHTML_CONTENT_TYPE;
	
	/**A content type of <code>application/xhtml+xml-external-parsed-entity</code>.*/
	public final static ContentType XHTML_FRAGMENT_CONTENT_TYPE=XHTML.XHTML_FRAGMENT_CONTENT_TYPE;

}
