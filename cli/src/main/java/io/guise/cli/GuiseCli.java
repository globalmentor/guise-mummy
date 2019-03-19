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

package io.guise.cli;

import java.io.IOException;
import java.nio.file.Path;

import javax.annotation.*;

import com.globalmentor.application.*;

import io.guise.mummy.GuiseMummy;
import picocli.CommandLine.*;

/**
 * Command-line interface for Guise tasks.
 * @author Garret Wilson
 */
@Command(name = "guise", description = "Command-line interface for Guise tasks.", versionProvider = GuiseCli.MetadataProvider.class, mixinStandardHelpOptions = true)
public class GuiseCli extends BaseCliApplication {

	/**
	 * Constructor.
	 * @param args The command line arguments.
	 */
	public GuiseCli(@Nonnull final String[] args) {
		super(args);
	}

	/**
	 * Main program entry method.
	 * @param args Program arguments.
	 */
	public static void main(@Nonnull final String[] args) {
		Application.start(new GuiseCli(args));
	}

	@Command(description = "Mummifies a site by generating a static version.")
	public void mummify(
			@Option(names = "--target", description = "The target directory into which the site will be generated; will be created if needed.", required = true) final Path targetDirectory,
			@Parameters(paramLabel = "<source-directory>", description = "The root directory of the site to mummify.") @Nonnull final Path sourceDirectory) {
		final GuiseMummy mummifier = new GuiseMummy();
		try {
			mummifier.mummify(sourceDirectory, targetDirectory);
		} catch(final IOException ioException) {
			getLogger().error("Error mummifying site.", ioException);
			System.err.println(ioException.getMessage());
		}
	}

	/** Strategy for providing version and other information from the configuration. */
	static class MetadataProvider extends AbstractMetadataProvider {
		public MetadataProvider() {
			super(GuiseCli.class);
		}
	}
}
