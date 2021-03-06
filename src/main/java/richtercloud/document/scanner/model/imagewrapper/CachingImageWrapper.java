/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package richtercloud.document.scanner.model.imagewrapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javafx.scene.image.WritableImage;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.ifaces.ImageWrapperException;
import richtercloud.message.handler.IssueHandler;

/**
 * Improvement of {@link DefaultImageWrapper} which caches image previews
 * because of the high need for resources for transformation and always loads
 * original image from disk in order to save memory/freeing of image data after
 * they've been used in memeory.
 *
 * Currently this class manages its cache in static constants which is not too
 * elegant and should be changed to a MVC architecture one day.
 *
 * @author richter
 */
public class CachingImageWrapper extends DefaultImageWrapper {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(CachingImageWrapper.class);
    private final static String SHUTDOWN_TEMPLATE = "returning null because shutdown has been requested";
    private final static CacheManager MANAGER =
                 Caching.getCachingProvider().getCacheManager();
    private final static Cache<Long, Map<Integer, BufferedImage>> CACHE;
    private final static Cache<Long, Map<Integer, WritableImage>> JAVAFX_CACHE;
    private final static Cache<Long, File> STREAM_CACHE;
    static {
        MutableConfiguration<Long, Map<Integer, BufferedImage>> config = new MutableConfiguration<>();
        MutableConfiguration<Long, Map<Integer, WritableImage>> javaFXConfig = new MutableConfiguration<>();
        MutableConfiguration<Long, File> streamConfig = new MutableConfiguration<>();
        for(MutableConfiguration config0 : new MutableConfiguration[] {config, javaFXConfig, streamConfig}) {
            config0.setStoreByValue(false)
                    .setStatisticsEnabled(true)
                    .setExpiryPolicyFactory(FactoryBuilder.factoryOf(
                            new AccessedExpiryPolicy(new Duration(TimeUnit.HOURS, 1))));
        }
        CACHE = MANAGER.createCache("cache",
                config);
        JAVAFX_CACHE = MANAGER.createCache("javafx-cache",
                javaFXConfig);
        STREAM_CACHE = MANAGER.createCache("stream-cache",
                streamConfig);
    }
    private static boolean shutdown = false;

    /**
     * Avoids <pre>java.lang.IllegalStateException: null
     *     at org.ehcache.jcache.JCache.checkNotClosed(JCache.java:763) ~[jcache-1.0.1.jar:na]</pre>
     */
    /*
    internal implementation notes:
    - this might close JCaches in other classes as well which might be
    troublesome, so if other classes use JCache as well, introduce a shared
    shutdown lock
    - the possibility that shutdown doesn't get this lock before the next thread
    invoking getOriginalImageStream0 exists and can only be avoid with a
    prioritizable lock like it's used in reflection-form-builder-jpa
    */
    public static void shutdown() {
        STREAM_CACHE_LOCK.lock();
        try {
            Caching.getCachingProvider().close();
            shutdown = true;
        }finally {
            STREAM_CACHE_LOCK.unlock();
        }
    }
    private final long cacheId;
    private final static AtomicInteger CACHE_ID_INTEGER = new AtomicInteger();
    /**
     * The lock for writing to and reading from the stream cache.
     */
    /*
    internal implementation notes:
    - a static STREAM_CACHE needs a static lock; this also works around the
    problem that during (de-)serialization the locks needs to be checked for
    initialization
    */
    private final static Lock STREAM_CACHE_LOCK = new ReentrantLock();
    private final static Lock PREVIEW_CACHE_LOCK = new ReentrantLock();
    private final static Lock PREVIEW_FX_CACHE_LOCK = new ReentrantLock();

    public CachingImageWrapper(File storageDir,
            BufferedImage image,
            IssueHandler issueHandler) throws IOException {
        super(storageDir,
                image,
                issueHandler);
        this.cacheId = CACHE_ID_INTEGER.incrementAndGet();
        LOGGER.trace(String.format("assigned cache id %d",
                this.cacheId));
    }

    /**
     * {@inheritDoc }
     *
     * @param width
     * @return the generated or cached preview or {@code null} if the generation
     * has been abort if the cache was shut down
     * @throws ImageWrapperException
     */
    @Override
    public BufferedImage getImagePreview(int width) throws ImageWrapperException {
        PREVIEW_CACHE_LOCK.lock();
        try {
            if(shutdown) {
                LOGGER.trace(SHUTDOWN_TEMPLATE);
                return null;
            }
            Map<Integer, BufferedImage> wrapperCacheEntry = CACHE.get(cacheId);
            if(wrapperCacheEntry == null) {
                wrapperCacheEntry = new HashMap<>();
                CACHE.put(cacheId, wrapperCacheEntry);
                LOGGER.trace(String.format("creating empty preview cache cache entry for id %d",
                        cacheId));
            }else {
                LOGGER.trace(String.format("using preview cache entry for id %d",
                        cacheId));
            }
            BufferedImage imagePreview = wrapperCacheEntry.get(width);
            if(imagePreview == null) {
                imagePreview = super.getImagePreview(width);
                if(imagePreview == null) {
                    //cache has been shut down
                    LOGGER.trace(SHUTDOWN_TEMPLATE);
                    return null;
                }
                LOGGER.trace(String.format("storing Java FX preview cache entry for width %d id %d",
                        width,
                        cacheId));
                wrapperCacheEntry.put(width, imagePreview);
            }else {
                LOGGER.trace(String.format("using preview cache entry for width %d id %d",
                        width,
                        cacheId));
            }
            return imagePreview;
        }finally {
            PREVIEW_CACHE_LOCK.unlock();
        }
    }

    @Override
    public WritableImage getImagePreviewFX(int width) throws ImageWrapperException {
        PREVIEW_FX_CACHE_LOCK.lock();
        try {
            if(shutdown) {
                LOGGER.trace(SHUTDOWN_TEMPLATE);
                return null;
            }
            Map<Integer, WritableImage> wrapperCacheEntry = JAVAFX_CACHE.get(cacheId);
            if(wrapperCacheEntry == null) {
                wrapperCacheEntry = new HashMap<>();
                JAVAFX_CACHE.put(cacheId, wrapperCacheEntry);
                LOGGER.trace(String.format("creating empty Java FX preview cache cache entry for id %d",
                        cacheId));
            }else {
                LOGGER.trace(String.format("using Java FX preview cache entry for id %d",
                        cacheId));
            }
            WritableImage imagePreview = wrapperCacheEntry.get(width);
            if(imagePreview == null) {
                imagePreview = super.getImagePreviewFX(width);
                if(imagePreview == null) {
                    LOGGER.trace(SHUTDOWN_TEMPLATE);
                    return null;
                }
                LOGGER.trace(String.format("storing Java FX preview cache entry for width %d id %d",
                        width,
                        cacheId));
                wrapperCacheEntry.put(width, imagePreview);
            }else {
                LOGGER.trace(String.format("using Java FX preview cache entry for width %d id %d",
                        width,
                        cacheId));
            }
            return imagePreview;
        }finally {
            PREVIEW_FX_CACHE_LOCK.unlock();
        }
    }

    @Override
    public File getOriginalImageStream0(String formatName) throws ImageWrapperException {
        STREAM_CACHE_LOCK.lock();
        try {
            if(shutdown) {
                LOGGER.trace(SHUTDOWN_TEMPLATE);
                return null;
            }
            File streamSource = STREAM_CACHE.get(cacheId);
            if(streamSource == null) {
                streamSource = super.getOriginalImageStream0(formatName);
                STREAM_CACHE.put(cacheId, streamSource);
                LOGGER.trace(String.format("storing orginal stream cache entry for id %d",
                        cacheId));
            }else {
                LOGGER.trace(String.format("using orginal stream cache entry for id %d",
                        cacheId));
            }
            return streamSource;
        }finally {
            STREAM_CACHE_LOCK.unlock();
        }
    }

    @Override
    public void setRotationDegrees(double rotationDegrees) throws ImageWrapperException{
        STREAM_CACHE_LOCK.lock();
        try {
            if(shutdown) {
                LOGGER.trace("returning immediately because shutdown has been requested");
                return;
            }
            super.setRotationDegrees(rotationDegrees);
            CACHE.remove(cacheId);
            JAVAFX_CACHE.remove(cacheId);
            STREAM_CACHE.remove(cacheId);
            LOGGER.trace(String.format("clearing preview and stream caches after change of rotation for id %d",
                    cacheId));
        }finally {
            STREAM_CACHE_LOCK.unlock();
        }
    }
}
