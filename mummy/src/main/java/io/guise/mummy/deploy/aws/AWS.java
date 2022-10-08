/*
 * Copyright © 2019 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.guise.mummy.deploy.aws;

/**
 * Settings and definitions for AWS in general for Guise.
 * @author Garret Wilson
 */
public class AWS {

	/**
	 * The global configuration for the AWS profile to use in deployment.
	 * @see <a href="https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-profiles.html">Named Profiles</a>
	 * @see <a href="https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/credentials.html">Supplying and Retrieving AWS Credentials</a>
	 */
	public static final String CONFIG_KEY_DEPLOY_AWS_PROFILE = "deploy.aws.profile";

}
