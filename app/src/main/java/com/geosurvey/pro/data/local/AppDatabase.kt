package com.geosurvey.pro.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SurveyDao {
    @Query("SELECT * FROM surveys")
    fun getAllSurveys(): Flow<List<SurveyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurveys(surveys: List<SurveyEntity>)
}

@Database(entities = [SurveyEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun surveyDao(): SurveyDao
}
