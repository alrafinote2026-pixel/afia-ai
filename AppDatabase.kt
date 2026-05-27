package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AfiaDao {

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()

    // --- Custom Shortcuts ---
    @Query("SELECT * FROM custom_shortcuts")
    fun getAllShortcuts(): Flow<List<CustomShortcut>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: CustomShortcut)

    @Update
    suspend fun updateShortcut(shortcut: CustomShortcut)

    @Delete
    suspend fun deleteShortcut(shortcut: CustomShortcut)

    // --- Workspace Notes ---
    @Query("SELECT * FROM workspace_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<WorkspaceNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: WorkspaceNote)

    @Delete
    suspend fun deleteNote(note: WorkspaceNote)

    @Update
    suspend fun updateNote(note: WorkspaceNote)

    // --- Automation Tasks ---
    @Query("SELECT * FROM automation_tasks")
    fun getAllAutomationTasks(): Flow<List<AutomationTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutomationTask(task: AutomationTask)

    @Update
    suspend fun updateAutomationTask(task: AutomationTask)
}

@Database(
    entities = [
        ChatMessage::class,
        CustomShortcut::class,
        WorkspaceNote::class,
        AutomationTask::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun afiaDao(): AfiaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "afia_ai_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
