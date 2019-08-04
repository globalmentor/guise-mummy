/*
 * Copyright © 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mummy;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javax.annotation.*;

import com.globalmentor.text.StringTemplate;

import io.urf.model.UrfResourceDescription;

/**
 * A phantom artifact that implemented by a basic, default XHTML document.
 * @author Garret Wilson
 */
public class DefaultXhtmlPhantomArtifact extends AbstractSourceFileArtifact {

	/**
	 * The template for an XHTML default document. The template has a single parameter:
	 * <ol>
	 * <li>page title</li>
	 * </ul>
	 * @implNote Once an expression language is implemented that replaces expressions in the document, the implementation might switch back to storing the default
	 *           XHTML file in resources with an expression in the title to be replaced in a later phase with the title from the description.
	 */
	private static final StringTemplate TEMPLATE = StringTemplate.builder().text( //@formatter:off
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +	//no newlines are needed; this will be parsed immediately 
			"<!DOCTYPE html>" + 
			"<html xmlns=\"http://www.w3.org/1999/xhtml\">" + 
			"<head>" + 
			"<meta charset=\"UTF-8\" />" + 
			"<title>").parameter(StringTemplate.STRING_PARAMETER).text("</title>" + 
			"</head>" + 
			"<body>" + 
			"</body>" + 
			"</html>").build();	//@formatter:on

	/**
	 * Constructor. The description should include an {@value Artifact#PROPERTY_HANDLE_TITLE} property.
	 * @param mummifier The mummifier responsible for generating this artifact; must be a mummifier that supports XHTML source artifacts.
	 * @param sourcePath The file containing the source of this artifact.
	 * @param outputPath The file where the artifact will be generated.
	 * @param description The description of the artifact; will be used to update the content with the title, if present.
	 * @see Artifact#PROPERTY_HANDLE_TITLE
	 */
	public DefaultXhtmlPhantomArtifact(@Nonnull final Mummifier mummifier, @Nonnull final Path sourcePath, @Nonnull final Path outputPath,
			@Nonnull final UrfResourceDescription description) {
		super(mummifier, sourcePath, outputPath, description);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version returns the bytes of a default XHTML artifact with the appropriate title, if any is provided in the description.
	 * @see #getResourceDescription()
	 * @see Artifact#PROPERTY_HANDLE_TITLE
	 */
	@Override
	public InputStream openSource(final MummyContext context) throws IOException {
		//get the title, if any, from the description
		final String title = getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_TITLE).map(Object::toString).orElse("");
		final String source = TEMPLATE.apply(title);
		return new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
	}

}
