/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework.component;

import com.guiseframework.model.*;

/**
 * Control accepting a resource to be imported, such as a web file upload.
 * @author Garret Wilson
 */
public class ResourceImportControl extends AbstractEditValueControl<ResourceImport> {

	/** Default constructor with a default value model. */
	public ResourceImportControl() {
		this(new DefaultValueModel<ResourceImport>(ResourceImport.class)); //construct the class with a default model
	}

	/**
	 * Value model constructor.
	 * @param valueModel The component value model.
	 * @throws NullPointerException if the given value model is <code>null</code>.
	 */
	public ResourceImportControl(final ValueModel<ResourceImport> valueModel) {
		super(new DefaultInfoModel(), valueModel, new DefaultEnableable()); //construct the parent class
	}

}
