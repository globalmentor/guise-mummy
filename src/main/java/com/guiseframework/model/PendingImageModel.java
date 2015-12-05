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

package com.guiseframework.model;

import static com.globalmentor.java.Classes.*;

/**
 * A model for an image that is pending.
 * @author Garret Wilson
 */
public interface PendingImageModel extends ImageModel {

	/** The image pending bound property. */
	public static final String IMAGE_PENDING_PROPERTY = getPropertyName(PendingImageModel.class, "imagePending");

	/** @return Whether the current image is in the process of transitioning to some other value. */
	public boolean isImagePending();

}
