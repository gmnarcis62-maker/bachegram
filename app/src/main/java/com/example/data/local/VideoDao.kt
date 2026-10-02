package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos WHERE videoStatus = 'VERIFIED' ORDER BY createdAt DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos ORDER BY createdAt DESC")
    fun getAllVideosForAdmin(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isFavorite = 1 AND videoStatus = 'VERIFIED' ORDER BY createdAt DESC")
    fun getFavoriteVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isSaved = 1 AND videoStatus = 'VERIFIED' ORDER BY createdAt DESC")
    fun getSavedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isDownloaded = 1 AND videoStatus = 'VERIFIED' ORDER BY createdAt DESC")
    fun getDownloadedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE watchCount > 0 AND videoStatus = 'VERIFIED' ORDER BY createdAt DESC")
    fun getHistoryVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE category = :category AND videoStatus = 'VERIFIED' ORDER BY createdAt DESC")
    fun getVideosByCategory(category: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE ageMin <= :age AND ageMax >= :age AND videoStatus = 'VERIFIED' ORDER BY createdAt DESC")
    fun getVideosByAge(age: Int): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE videoStatus = 'VERIFIED' AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')")
    fun searchVideos(query: String): Flow<List<VideoEntity>>

    @Query("SELECT COUNT(*) FROM videos")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM videos WHERE videoStatus = :status")
    suspend fun getCountByStatus(status: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Query("UPDATE videos SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE videos SET isSaved = :isSaved WHERE id = :id")
    suspend fun setSaved(id: String, isSaved: Boolean)

    @Query("UPDATE videos SET isDownloaded = :isDownloaded, localFilePath = :path, fileSizeBytes = :size WHERE id = :id")
    suspend fun setDownloaded(id: String, isDownloaded: Boolean, path: String?, size: Long)

    @Query("UPDATE videos SET videoStatus = :status WHERE id = :id")
    suspend fun updateVideoStatus(id: String, status: String)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideo(id: String)

    @Query("DELETE FROM videos WHERE videoStatus = 'FAILED'")
    suspend fun deleteFailedVideos(): Int

    @Query("UPDATE videos SET likesCount = likesCount + 1 WHERE id = :id")
    suspend fun incrementLike(id: String)

    @Query("UPDATE videos SET watchCount = watchCount + 1 WHERE id = :id")
    suspend fun incrementWatch(id: String)
}
