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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.annotation.*;

import io.urf.model.UrfResourceDescription;

/**
 * A phantom artifact that implemented by a basic, default XHTML document.
 * @author Garret Wilson
 */
public class DefaultPhantomArtifact extends AbstractSourceFileArtifact {

	/** The name of the Java resource containing the default XHTML file. */
	private static final String DEFAULT_RESOURCE_NAME = "default.xhtml";

	/**
	 * Constructor
	 * @param mummifier The mummifier responsible for generating this artifact.
	 * @param sourcePath The file containing the source of this artifact.
	 * @param outputPath The file where the artifact will be generated.
	 * @param description The description of the artifact.
	 */
	public DefaultPhantomArtifact(@Nonnull final Mummifier mummifier, @Nonnull final Path sourcePath, @Nonnull final Path outputPath,
			@Nonnull final UrfResourceDescription description) {
		super(mummifier, sourcePath, outputPath, description);
	}

	@Override
	public InputStream openSource(final MummyContext context) throws IOException {
		return getClass().getResourceAsStream(DEFAULT_RESOURCE_NAME);
	}

}
