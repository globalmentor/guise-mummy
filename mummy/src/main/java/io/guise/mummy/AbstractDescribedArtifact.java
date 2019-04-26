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

import static java.util.Objects.*;

import java.nio.file.Path;

import javax.annotation.*;

import io.urf.model.UrfResourceDescription;

/**
 * Abstract implementation with a known description.
 * @author Garret Wilson
 */
public abstract class AbstractDescribedArtifact extends AbstractArtifact {

	private final UrfResourceDescription description;

	@Override
	public UrfResourceDescription getResourceDescription() {
		return description;
	}

	/**
	 * Constructor
	 * @param mummifier The mummifier responsible for generating this artifact.
	 * @param sourcePath The file containing the source of this artifact.
	 * @param outputPath The file where the artifact will be generated.
	 * @param description The description of the artifact.
	 */
	public AbstractDescribedArtifact(@Nonnull final Mummifier mummifier, @Nonnull final Path sourcePath, @Nonnull final Path outputPath,
			@Nonnull final UrfResourceDescription description) {
		super(mummifier, sourcePath, outputPath);
		this.description = requireNonNull(description);
	}

}
