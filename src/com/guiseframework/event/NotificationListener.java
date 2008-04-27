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

package com.guiseframework.event;

/**An object that listens for notification events.
The notification system is used to pass messages that should be reported.
@author Garret Wilson
*/
public interface NotificationListener extends GuiseEventListener
{

	/**Called when a notification event occurs.
	@param notificationEvent The event containing notification information.
	*/
	public void notified(final NotificationEvent notificationEvent);

}
