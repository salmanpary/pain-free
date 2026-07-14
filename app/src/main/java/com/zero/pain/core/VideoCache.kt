package com.zero.pain.core

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@OptIn(UnstableApi::class)
object VideoCache {
    private var cache: SimpleCache? = null

    fun getCache(context: Context): SimpleCache {
        if (cache == null) {
            val cacheDir = File(context.cacheDir, "video_cache")
            val evictor = LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024) // 100MB
            val databaseProvider = StandaloneDatabaseProvider(context)
            cache = SimpleCache(cacheDir, evictor, databaseProvider)
        }
        return cache!!
    }

    fun getCacheDataSourceFactory(context: Context): CacheDataSource.Factory {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        val defaultDataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        
        return CacheDataSource.Factory()
            .setCache(getCache(context))
            .setUpstreamDataSourceFactory(defaultDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
}
