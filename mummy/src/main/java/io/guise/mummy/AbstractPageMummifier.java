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

import static com.globalmentor.io.Paths.*;
import static java.nio.file.Files.*;

import java.io.*;
import java.nio.file.Path;

import javax.annotation.*;

import org.w3c.dom.Document;

import com.globalmentor.html.HtmlSerializer;

/**
 * Abstract base mummifier for generating HTML pages.
 * @author Garret Wilson
 */
public abstract class AbstractPageMummifier extends AbstractSourcePathMummifier {

	/**
	 * {@inheritDoc}
	 * @implSpec This version changes the output file extension to <code>html</code>.
	 */
	@Override
	protected Path getArtifactTargetPath(final MummyContext context, final Path sourceFile) {
		return changeExtension(super.getArtifactTargetPath(context, sourceFile), "html"); //TODO use constant
	}

	@Override
	public Artifact plan(final MummyContext context, final Path sourcePath) throws IOException {
		return new PageArtifact(this, sourcePath, getArtifactTargetPath(context, sourcePath));
	}

	@Override
	public void mummify(final MummyContext context, final Artifact artifact) throws IOException {

		final Document document = loadSourceDocument(artifact.getSourcePath());

		System.out.println("parsed document: " + artifact.getSourcePath());
		try (final OutputStream outputStream = new BufferedOutputStream(newOutputStream(artifact.getTargetPath()))) {
			final HtmlSerializer htmlSerializer = new HtmlSerializer(true);
			htmlSerializer.serialize(document, outputStream);
		}
		System.out.println("generated document: " + artifact.getTargetPath());

	}

	/**
	 * Loads the source file and returns it as a document to be further refined before being used to generate the artifact.
	 * <p>
	 * The returned document will not yet have been processed. For example, no expressions will have been evaluated and links will still reference source paths.
	 * </p>
	 * <p>
	 * The document must be in XHTML using the HTML namespace.
	 * </p>
	 * @param sourceFile The file from which to load the document.
	 * @return A document describing the source content of the artifact to generate.
	 * @throws IOException if there is an error loading and/or converting the source file contents.
	 */
	protected abstract Document loadSourceDocument(@Nonnull final Path sourceFile) throws IOException;

}
