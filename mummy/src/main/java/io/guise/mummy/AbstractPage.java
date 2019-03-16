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

import java.nio.file.Path;

import javax.annotation.*;

import com.globalmentor.net.URIPath;

/**
 * Abstract implementation of an artifact.
 * @author Garret Wilson
 */
public abstract class AbstractPage extends AbstractArtifact implements Page {
	//TODO fix 
	//	private final boolean isCollection;
	//
	//	@Override
	//	public boolean isCollection() {
	//		return isCollection;
	//	}

	/**
	 * Source resource context path constructor.
	 * @param resourceContextPath The absolute path of the resource, relative to the site context.
	 * @param sourceFile The file containing the source of this artifact.
	 * @param outputFile The file where the artifact will be generated.
	 * @throws IllegalArgumentException if the given context path is not absolute.
	 */
	public AbstractPage(/*TODO fix @Nonnull final URIPath resourceContextPath, final boolean isCollection, */ @Nonnull final Path sourceFile,
			@Nonnull final Path outputFile) {
		super(/*TODO fix resourceContextPath, */sourceFile, outputFile);
		//TODO fix this.isCollection = isCollection;
	}

}
