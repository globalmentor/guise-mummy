/*
 * Copyright © 2020 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.mummy.mummify.page.widget;

import java.io.IOException;
import java.util.List;

import javax.annotation.*;

import org.w3c.dom.*;

import com.globalmentor.io.IllegalDataException;
import com.globalmentor.xml.spec.NsName;

import io.guise.mummy.*;
import io.guise.mummy.mummify.page.PageMummifier;

/**
 * A Guise Mummy widget that can be placed on a page.
 * @apiNote A widget expects to process an XHTML DOM tree, regardless of the original format of the page.
 * @author Garret Wilson
 */
public interface Widget {

	/** @return The identification of the XHTML element representing the widget in the source tree. */
	public NsName getWidgetElementName();

	/**
	 * Processes a source XHTML element to generate content for the widget.
	 * <p>
	 * The given widget element will be replaced in the document tree with the returned element(s). If only the same element is returned, no replacement is made.
	 * If no element is returned, the source element is removed.
	 * </p>
	 * @param mummifier The mummifier processing the page on which this widget appears.
	 * @param context The context of static site generation.
	 * @param artifact The artifact being generated.
	 * @param widgetElement The list element to regenerate.
	 * @return The processed element(s), if any, to replace the widget source element.
	 * @throws IOException if there is an I/O error processing the element.
	 * @throws IllegalDataException if the information in the widget element is not appropriate for the widget.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	public List<Element> processElement(@Nonnull PageMummifier mummifier, @Nonnull MummyContext context, @Nonnull Artifact artifact,
			@Nonnull Element widgetElement) throws IOException, IllegalDataException, DOMException;

}
