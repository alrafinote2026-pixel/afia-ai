package com.example.data

import kotlinx.coroutines.flow.Flow

class AfiaRepository(private val dao: AfiaDao) {

    // Chat History
    val allMessages: Flow<List<ChatMessage>> = dao.getAllMessages()

    suspend fun insertMessage(message: ChatMessage) {
        dao.insertMessage(message)
    }

    suspend fun clearChatHistory() {
        dao.clearChatHistory()
    }

    // Custom Shortcuts
    val allShortcuts: Flow<List<CustomShortcut>> = dao.getAllShortcuts()

    suspend fun insertShortcut(shortcut: CustomShortcut) {
        dao.insertShortcut(shortcut)
    }

    suspend fun updateShortcut(shortcut: CustomShortcut) {
        dao.updateShortcut(shortcut)
    }

    suspend fun deleteShortcut(shortcut: CustomShortcut) {
        dao.deleteShortcut(shortcut)
    }

    // Workspace Notes
    val allNotes: Flow<List<WorkspaceNote>> = dao.getAllNotes()

    suspend fun insertNote(note: WorkspaceNote) {
        dao.insertNote(note)
    }

    suspend fun updateNote(note: WorkspaceNote) {
        dao.updateNote(note)
    }

    suspend fun deleteNote(note: WorkspaceNote) {
        dao.deleteNote(note)
    }

    // Automation Tasks
    val allAutomationTasks: Flow<List<AutomationTask>> = dao.getAllAutomationTasks()

    suspend fun insertAutomationTask(task: AutomationTask) {
        dao.insertAutomationTask(task)
    }

    suspend fun updateAutomationTask(task: AutomationTask) {
        dao.updateAutomationTask(task)
    }
}
