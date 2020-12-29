/*
 * Copyright © 2020 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mesh;

import javax.annotation.*;

/**
 * An meshing exception indicating a problem parsing a Mesh Expression Language (MEXL) expression.
 * @author Garret Wilson
 */
public class MexlException extends MeshException {

	private static final long serialVersionUID = 1L;

	/** No-argument constructor. */
	public MexlException() {
		this((String)null);
	}

	/**
	 * Message constructor.
	 * @param message An explanation of the error, or <code>null</code> if no message should be used.
	 */
	public MexlException(@Nullable final String message) {
		this(message, null);
	}

	/**
	 * Cause constructor. The message of the cause will be used if available.
	 * @param cause The cause of the error or <code>null</code> if the cause is nonexistent or unknown.
	 */
	public MexlException(@Nullable final Throwable cause) {
		this(cause == null ? null : cause.toString(), cause);
	}

	/**
	 * Message and cause constructor.
	 * @param message An explanation of the error, or <code>null</code> if a no message should be used.
	 * @param cause The cause of the error or <code>null</code> if the cause is nonexistent or unknown.
	 */
	public MexlException(@Nullable final String message, @Nullable final Throwable cause) {
		super(message, cause);
	}

}
