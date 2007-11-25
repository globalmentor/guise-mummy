package com.guiseframework.model;

import java.io.IOException;
import java.net.URI;

import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.util.*;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;

import static com.guiseframework.theme.Theme.*;

/**An image model that can initiate retrieval of an image from a cache and update the image when fetching succeeds.
@param <K> The type of key used to lookup data in the cache.
@param <V> The type of value stored in the cache.
The cached image URI should be set using {@link #setCachedImageURI(URI)} before calling {@link #setCachedImageKey(Object)} to initiate cache checking and possible fetching.
The image URI is set automatically when the image is determined to have been fetched into the cache.
@author Garret Wilson
*/
public class CachedImageModel<K, V> extends DefaultImageModel
{

	/**The cached image key bound property.*/
	public final static String CACHED_IMAGE_KEY_PROPERTY=getPropertyName(CachedImageModel.class, "cachedImageKey");
	/**The cached image URI bound property.*/
	public final static String CACHED_IMAGE_URI_PROPERTY=getPropertyName(CachedImageModel.class, "cachedImageURI");

	/**The cache from which images will be retrieved.*/
	private final Cache<K, V> cache;

		/**The cache from which images will be retrieved.*/
		public Cache<K, V> getCache() {return cache;}

	/**The listener that changes the image URI when the an image is fetched into the cache.
	@see #getCachedImageURI()
	*/
	private final CacheFetchListener<K, V> cacheFetchListener=new CacheFetchListener<K, V>()
			{
				public void fetched(final CacheFetchEvent<K, V> cacheFetchEvent)	//when the image is fetched
				{
					cacheFetchEvent.getSource().removeCacheFetchListener(cacheFetchEvent.getKey(), this);	//stop listening for fetches
					setImageURI(getCachedImageURI());	//switch to the cached image URI
				}
			};
		
	/**The cached image URI, which may be a resource URI, or <code>null</code> if there is no cached image URI.*/
	private URI cachedImageURI;

		/**@return The cached image URI, which may be a resource URI, or <code>null</code> if there is no cached image URI.*/
		public URI getCachedImageURI() {return cachedImageURI;}

		/**Sets the URI of the image.
		This is a bound property.
		@param newCachedImageURI The new URI of the image, which may be a resource URI.
		@see #CACHED_IMAGE_URI_PROPERTY
		*/
		public void setCachedImageURI(final URI newCachedImageURI)
		{
			if(!ObjectUtilities.equals(cachedImageURI, newCachedImageURI))	//if the value is really changing
			{
				final URI oldCachedImageURI=cachedImageURI;	//get the old value
				cachedImageURI=newCachedImageURI;	//actually change the value
				firePropertyChange(CACHED_IMAGE_URI_PROPERTY, oldCachedImageURI, newCachedImageURI);	//indicate that the value changed
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
		@see #CACHED_IMAGE_KEY_PROPERTY
		*/
		public void setCachedImageKey(final K newCachedImageKey)
		{
			if(!ObjectUtilities.equals(cachedImageKey, newCachedImageKey))	//if the value is really changing
			{
				final Cache<K, V> cache=getCache();	//get the cache				
				if(cachedImageKey!=null)	//if we had a cache key before
				{
					cache.removeCacheFetchListener(cachedImageKey, cacheFetchListener);	//make sure we aren't listening for cache fetches
				}
				final K oldCachedImageKey=cachedImageKey;	//get the old value
				cachedImageKey=newCachedImageKey;	//actually change the value
				if(cachedImageKey!=null)	//if we have a new cache key
				{
					cache.addCacheFetchListener(cachedImageKey, cacheFetchListener);	//listen for cache fetches before requesting the image in case the fetch occurs before we can check if the image exists
					try
					{
						cache.get(cachedImageKey, true);	//initiate a get from the cache with deferred fetching
						if(cache.isCached(cachedImageKey))	//if the value is cached, we don't have to listen for changes
						{
							cache.removeCacheFetchListener(cachedImageKey, cacheFetchListener);	//stop listening for fetches (we don't know whether the image was already cached or, less likely, the image was cached after we initiated the retrieval but before we could check it)
							setImageURI(getCachedImageURI());	//switch to the cached image URI in case the listener didn't have a chance because the image was already cached
						}
					}
					catch(final IOException ioException)
					{
						throw new AssertionError(ioException);	//TODO importan: fix; do something with the error, which can occur when we care checking to see if the information is cached
					}
				}
				firePropertyChange(CACHED_IMAGE_URI_PROPERTY, oldCachedImageKey, newCachedImageKey);	//indicate that the value changed
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
		super(GLYPH_BUSY);	//default to a busy icon
		this.cache=checkInstance(cache, "Cache cannot be null.");
		this.cachedImageURI=cachedImageURI;	//save the cached image URI
	}

}
