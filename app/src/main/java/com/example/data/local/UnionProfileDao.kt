package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.UnionProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UnionProfileDao {
    @Query("SELECT * FROM union_profile WHERE id = 1 LIMIT 1")
    fun getUnionProfile(): Flow<UnionProfile?>

    @Query("SELECT * FROM union_profile WHERE id = 1 LIMIT 1")
    suspend fun getUnionProfileSync(): UnionProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUnionProfile(profile: UnionProfile)
}
