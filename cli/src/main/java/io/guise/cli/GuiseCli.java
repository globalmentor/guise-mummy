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

import static com.globalmentor.java.OperatingSystem.*;

import java.io.IOException;
import java.nio.file.*;

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

	/** The default relative path of the source directory. */
	private final static Path DEFAULT_SOURCE_RELATIVE_DIR = Paths.get("src", "site"); //TODO define in GuiseMummy

	/** The default relative path of the target directory. */
	private final static Path DEFAULT_TARGET_RELATIVE_DIR = Paths.get("target", "site"); //TODO define in GuiseMummy

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
			@Option(names = "--site-source", description = "The source root directory of the site to mummify.%nDefaults to @|bold src/site/|@ relative to the project base directory.") @Nullable Path sourceDirectory,
			@Option(names = "--site-target", description = "The target root directory into which the site will be generated; will be created if needed.%nDefaults to @|bold target/site/|@ relative to the project base directory.") @Nullable Path targetDirectory,
			@Parameters(paramLabel = "<project>", description = "The base directory of the project to mummify.%nDefaults to the current working directory.", arity = "0..1") @Nullable Path projectDirectory,

			@Option(names = {"--debug", "-d"}, description = "Turns on debug level logging.") final boolean debug) {

		setDebug(debug); //TODO inherit from base class; see https://github.com/remkop/picocli/issues/649

		if(projectDirectory == null) {
			projectDirectory = getWorkingDirectory();
		}

		if(sourceDirectory == null) {
			sourceDirectory = projectDirectory.resolve(DEFAULT_SOURCE_RELATIVE_DIR);
		}

		if(targetDirectory == null) {
			targetDirectory = projectDirectory.resolve(DEFAULT_TARGET_RELATIVE_DIR);
		}

		getLogger().info("Mummify...");
		getLogger().info("Project: {}", projectDirectory);
		getLogger().info("Source: {}", sourceDirectory);
		getLogger().info("Target: {}", targetDirectory);

		final GuiseMummy mummifier = new GuiseMummy();
		try {
			mummifier.mummify(sourceDirectory.toAbsolutePath().normalize(), targetDirectory.toAbsolutePath().normalize());
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
