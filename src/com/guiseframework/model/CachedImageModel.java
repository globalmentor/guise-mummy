package com.guiseframework.model;

import java.io.IOException;
import java.net.URI;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;

import com.garretwilson.util.*;
import com.globalmentor.java.Objects;

/**An image model that can initiate retrieval of an image from a cache and update the image when fetching succeeds.
@param <K> The type of key used to lookup data in the cache.
@param <V> The type of value stored in the cache.
Cache checking and possible fetching is initiated when both the image URI is set using {@link #setImageURI(URI)} and the cached image key is set using {@link #setCachedImageKey(Object)}.
The image URI is set automatically when the image is determined to have been fetched into the cache.
@author Garret Wilson
*/
public class CachedImageModel<K, V> extends DefaultImageModel implements PendingImageModel
{

	/**The cached image key bound property.*/
	public final static String CACHED_IMAGE_KEY_PROPERTY=getPropertyName(CachedImageModel.class, "cachedImageKey");
	/**The cached image URI bound property.*/
	public final static String CACHED_IMAGE_URI_PROPERTY=getPropertyName(CachedImageModel.class, "cachedImageURI");

	/**The cache from which images will be retrieved.*/
	private final Cache<K, V> cache;

		/**The cache from which images will be retrieved.*/
		public Cache<K, V> getCache() {return cache;}

	/**The listener that changes the image URI when the an image is fetched into the cache.*/
	private final CachedImageListener cachedImageListener=new CachedImageListener();
		
	/**The cached image URI, which may be a resource URI, or <code>null</code> if there is no cached image URI.*/
	private URI cachedImageURI;

		/**@return The cached image URI, which may be a resource URI, or <code>null</code> if there is no cached image URI.*/
		public URI getCachedImageURI() {return cachedImageURI;}

		/**Sets the URI of the image.
		This is a bound property.
		@param newCachedImageURI The new URI of the image, which may be a resource URI.
		@exception IllegalStateException if the cached image URI is changed while the current image is pending.
		@see #CACHED_IMAGE_URI_PROPERTY
		*/
		public void setCachedImageURI(final URI newCachedImageURI)
		{
			if(!Objects.equals(cachedImageURI, newCachedImageURI))	//if the value is really changing
			{
				if(isImagePending())	//if the image is pending
				{
					throw new IllegalStateException("Cached image URI cannot be changed while the image is pending.");
				}
				final URI oldCachedImageURI=cachedImageURI;	//get the old value
				cachedImageURI=newCachedImageURI;	//actually change the value
				firePropertyChange(CACHED_IMAGE_URI_PROPERTY, oldCachedImageURI, newCachedImageURI);	//indicate that the value changed
				updatePending();	//initiate image retrieval if we can
			}
		}

	/**The key to the image in the cache, which or <code>null</code> if the image should not be looked up from the cache.*/
	private K cachedImageKey;

		/**@return The key to the image in the cache, which or <code>null</code> if the image should not be looked up from the cache.*/
		public K getCachedImageKey() {return cachedImageKey;}

		/**The key to the image in the cache, which or <code>null</code> if the image should not be looked up from the cache.
		Chaging the cached image key initiates a deferred retrieval of the image from the cache.
		This is a bound property.
		@param newCachedImageKey The new key to the image in the cache, which or <code>null</code> if the image should not be looked up from the cache.
		@exception IllegalStateException if the cached image key is changed while the current image is pending.
		@see #CACHED_IMAGE_KEY_PROPERTY
		*/
		public void setCachedImageKey(final K newCachedImageKey)
		{
			if(!Objects.equals(cachedImageKey, newCachedImageKey))	//if the value is really changing
			{
				if(isImagePending())	//if the image is pending
				{
					throw new IllegalStateException("Cached image key cannot be changed while the image is pending.");
				}
Debug.trace("for cached image", getCachedImageURI(), "changing from cached image key", cachedImageKey, "to", newCachedImageKey);
				final K oldCachedImageKey=cachedImageKey;	//get the old value
				cachedImageKey=newCachedImageKey;	//actually change the value
				firePropertyChange(CACHED_IMAGE_URI_PROPERTY, oldCachedImageKey, newCachedImageKey);	//indicate that the value changed
Debug.trace("initiating pending");
				updatePending();	//initiate image retrieval if we can
			}
		}

	/**Whether the current image is in the process of transitioning to some other value.*/
	private boolean imagePending=false;

		/**@return Whether the current image is in the process of transitioning to some other value.*/
		public boolean isImagePending() {return imagePending;}

		/**Sets whether the current image is in the process of transitioning to some other value.
		This is a bound property of type {@link Boolean}.
		@param newImagePending <code>true</code> if the current image is a transitional image that is expected to change.
		@see #IMAGE_PENDING_PROPERTY
		*/
		protected void setImagePending(final boolean newImagePending)
		{
			if(imagePending!=newImagePending)	//if the value is really changing
			{
				final boolean oldImagePending=imagePending;	//get the current value
				imagePending=newImagePending;	//update the value
Debug.trace("pending state changed to", newImagePending, "now adding or removing cache fetch listener");
				if(newImagePending)	//if the image is now pending
				{
					getCache().addCacheFetchListener(cachedImageKey, cachedImageListener);	//listen for cache fetches before requesting the image in case the fetch occurs before we can check if the image exists
				}
				else	//if the image is no longer pending
				{
					getCache().removeCacheFetchListener(getCachedImageKey(), cachedImageListener);	//stop listening for fetches				
				}
				firePropertyChange(IMAGE_PENDING_PROPERTY, oldImagePending, newImagePending);
			}
		}


	/**Initiates pending by both a cached image key and cached image URI are available.
	@see #getCachedImageURI()
	@see #getCachedImageURI()
	*/
	protected void updatePending()
	{
		final K cachedImageKey=getCachedImageKey();	//get the key of the cached image
		final URI cachedImageURI=getCachedImageURI();	//get the URI of the cached image
		if(cachedImageKey!=null && cachedImageURI!=null)	//if we know enough information to begin pending
		{
			setImagePending(true);	//show that the image is pending
			try
			{
				cache.get(cachedImageKey, true);	//initiate a get from the cache with deferred fetching
				if(cache.isCached(cachedImageKey))	//if the value is cached, we don't have to listen for changes
				{
					setImageURI(getCachedImageURI());	//switch to the cached image URI in case the listener didn't have a chance because the image was already cached
					setImagePending(false);	//the image is no longer pending
				}
			}
			catch(final IOException ioException)
			{
				throw new AssertionError(ioException);	//TODO important: fix; do something with the error, which can occur when we care checking to see if the information is cached
			}
			
		}
	}

	/**Cache constructor.
	@param cache The cache from which the image will be retrieved.
	@exception NullPointerException if the given cache is <code>null</code>.
	*/
	public CachedImageModel(final Cache<K, V> cache)
	{
		this(cache, null);	//construct the class with no image
	}

	/**Cached image URI constructor.
	@param cache The cache from which the image will be retrieved.
	@param cachedImageURI The cached image URI, which may be a resource URI, or <code>null</code> if there is no cached image URI.
	@exception NullPointerException if the given cache is <code>null</code>.
	*/
	public CachedImageModel(final Cache<K, V> cache, final URI cachedImageURI)
	{
		this.cache=checkInstance(cache, "Cache cannot be null.");
		this.cachedImageURI=cachedImageURI;	//save the cached image URI
	}

	/**A listener that changes the image URI when the an image is fetched into the cache.
	@see #getCachedImageURI()
	*/
	protected class CachedImageListener implements CacheFetchListener<K, V>
	{
		public void fetched(final CacheFetchEvent<K, V> cacheFetchEvent)	//when the image is fetched
		{
			setImageURI(getCachedImageURI());	//switch to the cached image URI
			setImagePending(false);	//indicate that the image is no longer pending
		}
	};

}
