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

package com.guiseframework.platform.web;

import java.util.*;
import static java.util.Collections.*;

import static com.globalmentor.java.Maths.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.java.Conditions.*;

import com.globalmentor.model.NameValuePair;

import com.guiseframework.GuiseApplication;
import com.guiseframework.platform.*;

/**An abstract implementation of a web platform for Guise.
This class registers no depictors.
@author Garret Wilson
*/
public abstract class AbstractWebPlatform extends AbstractPlatform implements WebPlatform
{

	/**The prefix used for creating depict ID strings on this platform.*/
	protected final static String DEPICT_ID_STRING_PREFIX="id";

	/**The user local environment.*/
	private final Environment environment;

		/**@return The user local environment.*/
		public Environment getEnvironment() {return environment;}

	/**@return The thread-safe queue of messages to be delivered to the platform.*/
	@SuppressWarnings("unchecked")
	public Queue<WebPlatformMessage> getSendMessageQueue() {return (Queue<WebPlatformMessage>)super.getSendMessageQueue();}

	/**The map of poll intervals requested for depicted objects.*/
	private final Map<DepictedObject, Integer> requestedPollIntervalMap=synchronizedMap(new HashMap<DepictedObject, Integer>());

	/**The default polling interval in milliseconds.*/
	public final static int DEFAULT_POLL_INTERVAL=5*60*1000;

	/**The current polling interval in milliseconds.*/
	private int pollInterval=DEFAULT_POLL_INTERVAL;

		/**@return The current polling interval in milliseconds.*/
		public int getPollInterval() {return pollInterval;}

		/**Sets the polling interval in milliseconds.
		@param newPollInterval The polling interval in milliseconds.
		@exception IllegalArgumentException if the given polling interval is less than zero.
		*/
		@SuppressWarnings("unchecked")
		public void setPollInterval(final int newPollInterval)
		{
			if(pollInterval!=checkArgumentNotNegative(newPollInterval))	//if the value is really changing
			{
				pollInterval=newPollInterval;	//actually change the value
				getSendMessageQueue().add(new WebCommandMessage<PollCommand>(PollCommand.POLL_INTERVAL,
						new NameValuePair<String, Object>(PollCommand.INTERVAL_PROPERTY, Integer.valueOf(pollInterval))));	//send a poll command to the platform with the new interval
			}			
		}

		/**Requests a polling interval for a given depicted object.
		The actual polling interval will be updated if the given polling interval is smaller than the current actual polling interval.
		@param depictedObject The depicted object requesting a polling interval.
		@param pollInterval The polling interval in milliseconds.
		@return <code>true</code> if the polling interval changed as a result of this request.
		@exception NullPointerException if the given depicted object is <code>null</code>.
		@exception IllegalArgumentException if the value is less than zero.
		@see #discontinuePollInterval(DepictedObject)
		@see #getPollInterval()
		@see #setPollInterval()
		*/ 
		public boolean requestPollInterval(final DepictedObject depictedObject, final int pollInterval)
		{
			checkArgumentNotNegative(pollInterval);
			synchronized(requestedPollIntervalMap)	//synchronize to ensure the that race conditions don't cause the actual poll interval to be out of synch with the requested poll intervals
			{
				final int oldPollInterval=getPollInterval();	//get the current polling interval
				requestedPollIntervalMap.put(checkInstance(depictedObject, "Depicted object cannot be null."), Integer.valueOf(pollInterval));	//indicate that this depicted object requests a certain poll interval
				if(pollInterval<oldPollInterval)	//if this poll interval is lower than the current poll interval
				{
					setPollInterval(pollInterval);	//switch to the lower polling interval
					return true;	//indicate that we changed the polling interval
				}
			}
			return false;	//indicate that the polling interval did not change
		}

		/**Indicates that a depicted object no longer requests a particular polling interval.
		The actual polling interval will be updated if the relinquished poll interval is less than or equal to the current poll interval.
		@param depictedObject The depicted object that is relinquishing a polling interval.
		@return <code>true</code> if the polling interval changed as a result of this relinquishment.
		@exception NullPointerException if the given depicted object is <code>null</code>.
		@see #requestPollInterval(DepictedObject, int)
		@see #getPollInterval()
		@see #setPollInterval()
		*/ 
		public boolean discontinuePollInterval(final DepictedObject depictedObject)
		{
//Log.trace("ready to discontinue poll interval for", depictedObject);
			synchronized(requestedPollIntervalMap)	//synchronize to ensure the that race conditions don't cause the actual poll interval to be out of synch with the requested poll intervals
			{
				final int oldPollInterval=getPollInterval();	//get the current polling interval
				final Integer relinquishedPollInterval=requestedPollIntervalMap.remove(checkInstance(depictedObject, "Depicted object cannot be null."));
//Log.trace("got relinquished poll interval", relinquishedPollInterval, "old poll interval", oldPollInterval);
				if(relinquishedPollInterval!=null && relinquishedPollInterval.intValue()<=oldPollInterval)	//if a poll interval was relinquished that was less than or equal to our current poll interval
				{
//Log.trace("now there are remaining poll intervals:", requestedPollIntervalMap.size());
					final int newPollInterval=min(requestedPollIntervalMap.values(), DEFAULT_POLL_INTERVAL);	//determine the new poll interval
//Log.trace("we want to change to new poll interval", newPollInterval);
					if(oldPollInterval!=newPollInterval)	//if the poll interval is different
					{
						setPollInterval(newPollInterval);	//update the polling interval
						return true;	//indicate that the polling interval changed
					}
				}
			}
			return false;	//indicate that the polling interval did not change
		}
	
	/**Application.
	This version copies the current application environment to a new environment for this platform.
	@param application The Guise application running on this platform.
	@exception NullPointerException if the given application is <code>null</code>.
	*/
	public AbstractWebPlatform(final GuiseApplication application)
	{
		super(application);	//construct the parent class
		this.environment=new DefaultEnvironment();	//create a new environment
//TODO del; don't copy application environment properties; currently this exposes application-level passwords; this information needs to be removed from the application environment, though		environment.setProperties(application.getEnvironment().getProperties());	//copy the application environment to the platform environment
		//TODO create some sort of configuration that gets loaded on this platform
	}

	/**Generates an ID for the given depicted object appropriate for using on the platform.
	@param depictID The depict ID to be converted to a platform ID.
	@return The form of the depict ID appropriate for using on the platform.
	*/
	public String getDepictIDString(final long depictID)	//TODO change to Base64 with safe encoding
	{
		return "id"+Long.toHexString(depictID);	//create an ID string from the depict ID
	}

	/**Returns the depicted object ID represented by the given platform-specific ID string.
	@param depictIDString The platform-specific form of the depict ID.
	@param depictID The depict ID to be converted to a platform ID.
	@return The depict ID the platform-specific form represents.
	@exception NullPointerException if the given string is <code>null</code>.
	@exception IllegalArgumentException if the given string does not represent the correct string form of a depict ID on this platform.
	*/
	public long getDepictID(final String depictIDString)
	{
		if(!checkInstance(depictIDString, "Depict ID string cannot be null.").startsWith(DEPICT_ID_STRING_PREFIX))	//if the string does not start with the correct prefix
		{
			throw new IllegalArgumentException("Depict ID string "+depictIDString+" is not in the correct format for this platform.");
		}
		return Long.parseLong(depictIDString.substring(DEPICT_ID_STRING_PREFIX.length()), 16);	//parse out the actual ID, throwing a NumberFormatException if the ID is not in the correct lexical format
	}

}
