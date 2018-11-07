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

package io.guise.framework.event;

import static java.util.Objects.*;

import io.guise.framework.model.Notification;

/**
 * An event indicating there should be a notification message of some event or state. The event target indicates the object that originally fired the event.
 * @author Garret Wilson
 * @see NotificationListener
 */
public class NotificationEvent extends AbstractTargetedGuiseEvent {

	/** The notification information. */
	private final Notification notification;

	/** @return The notification information. */
	private final Notification getNotification() {
		return notification;
	}

	/**
	 * Source and notification constructor. The target will be set to be the same as the given source.
	 * @param source The object on which the event initially occurred.
	 * @param notification The notification information.
	 * @throws NullPointerException if the given source and/or notification is <code>null</code>.
	 */
	public NotificationEvent(final Object source, final Notification notification) {
		this(source, source, notification); //construct the class with the same target as the source
	}

	/**
	 * Source, target, and notification constructor.
	 * @param source The object on which the event initially occurred.
	 * @param target The target of the event.
	 * @param notification The notification information.
	 * @throws NullPointerException if the given source, target, and/or notification is <code>null</code>.
	 */
	public NotificationEvent(final Object source, final Object target, final Notification notification) {
		super(source, target); //construct the parent class
		this.notification = requireNonNull(notification, "Notification must be provided.");
	}

	/**
	 * Copy constructor that specifies a different source.
	 * @param source The object on which the event initially occurred.
	 * @param notificationEvent The event the properties of which will be copied.
	 * @throws NullPointerException if the given source and/or event is <code>null</code>.
	 */
	public NotificationEvent(final Object source, final NotificationEvent notificationEvent) {
		this(source, notificationEvent.getTarget(), notificationEvent.getNotification()); //construct the class with the same target		
	}

}
