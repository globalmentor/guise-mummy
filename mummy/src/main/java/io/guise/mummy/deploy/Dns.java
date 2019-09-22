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

package io.guise.mummy.deploy;

import java.io.IOException;

import javax.annotation.*;

import io.guise.mummy.*;

/**
 * Access to a Domain Name System (DNS).
 * @author Garret Wilson
 */
public interface Dns {

	/**
	 * Prepares the DNS for deploying. This may include creating any accounts or record zones, for example.
	 * @param context The context of static site generation.
	 * @throws IOException if there is an I/O error during site deployment preparation.
	 */
	public void prepare(@Nonnull final MummyContext context) throws IOException;

}
