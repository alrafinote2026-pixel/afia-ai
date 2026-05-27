package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" or "afia"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_shortcuts")
data class CustomShortcut(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phrase: String, // e.g. "deploy shields"
    val actionType: String, // "FLASHLIGHT", "VOLUME_MAX", "SYSTEM_INFO", "OPEN_APP", "SAY_TEXT"
    val actionArg: String = "", // e.g. "ON", "100" or custom spoken response
    val isEnabled: Boolean = true
)

@Entity(tableName = "workspace_notes")
data class WorkspaceNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "automation_tasks")
data class AutomationTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "Study Mode", "Sleep Routine"
    val description: String,
    val isEnabled: Boolean = false,
    val taskType: String, // "STUDY_MODE", "SLEEP_MODE", "BATTERY_SAVER", "MORNING"
    val scheduledTime: String = "08:00" // Formatted HH:MM
)
