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

import javax.annotation.*;

import com.globalmentor.application.*;

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

	/** Strategy for providing version and other information from the configuration. */
	static class MetadataProvider extends AbstractMetadataProvider {
		public MetadataProvider() {
			super(GuiseCli.class);
		}
	}
}
