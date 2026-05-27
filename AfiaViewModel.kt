package com.example.ui.viewmodel

import android.app.Application
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.service.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AfiaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AfiaRepository(db.afiaDao())
    val deviceController = DeviceController(application)

    // --- State Streams ---
    val chatHistory: StateFlow<List<ChatMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shortcuts: StateFlow<List<CustomShortcut>> = repository.allShortcuts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<WorkspaceNote>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val automationTasks: StateFlow<List<AutomationTask>> = repository.allAutomationTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Local Interactive UI States ---
    private val _listeningState = MutableStateFlow(ListeningState.IDLE)
    val listeningState = _listeningState.asStateFlow()

    private val _continuousListening = MutableStateFlow(false)
    val continuousListening = _continuousListening.asStateFlow()

    private val _textToSpeechEnabled = MutableStateFlow(true)
    val textToSpeechEnabled = _textToSpeechEnabled.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(listOf("Afia OS kernel initialized.", "Security protocols active."))
    val logs = _logs.asStateFlow()

    // Ocular system analysis results
    private val _ocularAnalysisResult = MutableStateFlow("")
    val ocularAnalysisResult = _ocularAnalysisResult.asStateFlow()

    private val _isAnalyzingOcular = MutableStateFlow(false)
    val isAnalyzingOcular = _isAnalyzingOcular.asStateFlow()

    // Sound synth state
    private val _isSynthRunning = MutableStateFlow(false)
    val isSynthRunning = _isSynthRunning.asStateFlow()

    // Modern multi-panel visual routing
    private val _selectedTab = MutableStateFlow("dashboard")
    val selectedTab = _selectedTab.asStateFlow()

    private var voiceSystem: VoiceSystem? = null

    init {
        // Build initial automation states if empty
        viewModelScope.launch {
            repository.allAutomationTasks.collect { list ->
                if (list.isEmpty()) {
                    repoSetupDefaultAutomations()
                }
            }
        }

        // Initialize voice system
        voiceSystem = VoiceSystem(
            context = application,
            onTextRecognized = { text ->
                addLog("Voice Stream recognized: \"$text\"")
                sendMessageToAfia(text)
            },
            onListeningStateChanged = { state ->
                _listeningState.value = state
            },
            onError = { error ->
                addLog("Microphone Anomaly: $error")
            }
        )
    }

    private suspend fun repoSetupDefaultAutomations() {
        val defaultTasks = listOf(
            AutomationTask(name = "Morning Routine Protocol", description = "Speaks battery diagnostic and opens settings at sunrise.", isEnabled = false, taskType = "MORNING", scheduledTime = "07:30"),
            AutomationTask(name = "Study Mode Core", description = "Enables battery saving subroutines and silences music.", isEnabled = false, taskType = "STUDY_MODE"),
            AutomationTask(name = "Sleep Mode Subsurface", description = "Suspends synth frequencies and sets low volume.", isEnabled = false, taskType = "SLEEP_MODE"),
            AutomationTask(name = "Overdrive Battery Saver", description = "Deactivates flashlight, terminates synthetic beats.", isEnabled = false, taskType = "BATTERY_SAVER")
        )
        for (task in defaultTasks) {
            repository.insertAutomationTask(task)
        }
    }

    fun setSelectedTab(tab: String) {
        _selectedTab.value = tab
    }

    fun addLog(msg: String) {
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(0, "[${System.currentTimeMillis() % 100000}] $msg")
        _logs.value = currentLogs.take(50) // limit logs size to 50
    }

    // --- Voice control actions ---
    fun startVoiceListening() {
        voiceSystem?.startListening()
    }

    fun stopVoiceListening() {
        voiceSystem?.stopListening()
    }

    fun toggleContinuousListening() {
        val current = voiceSystem?.toggleContinuousMode() ?: false
        _continuousListening.value = current
        addLog("Continuous Listening: ${if (current) "ACTIVE (Hey Afia wake-word simulated)" else "SUSPENDED"}")
    }

    fun toggleTTS() {
        val current = _textToSpeechEnabled.value
        _textToSpeechEnabled.value = !current
        voiceSystem?.isTtsEnabled = !current
        addLog("Voice Synthesis Module: ${if (!current) "ONLINE" else "MUTED"}")
    }

    fun speak(text: String) {
        if (_textToSpeechEnabled.value) {
            voiceSystem?.speak(text)
        }
    }

    fun toggleCyberSynth() {
        val isRunning = _isSynthRunning.value
        if (isRunning) {
            val response = deviceController.stopCyberSynth()
            _isSynthRunning.value = false
            addLog(response)
            speak(response)
        } else {
            val response = deviceController.playCyberSynth()
            _isSynthRunning.value = true
            addLog(response)
            speak(response)
        }
    }

    // --- Message Processing & Intelligent Orchestrator ---
    fun sendMessageToAfia(prompt: String) {
        if (prompt.isBlank()) return

        addLog("User command input: \"$prompt\"")
        viewModelScope.launch {
            // Save user message to database
            repository.insertMessage(ChatMessage(sender = "user", content = prompt))

            // Attempt local command execution first (instant offline response!)
            val executedLocally = executeCommandLocally(prompt)
            if (executedLocally) return@launch

            // If not executed locally, proceed to ask Gemini
            addLog("No local physical routine found. Routing command to Cloud Gemini Engine...")
            
            // Build temporary conversation turns for history context
            val historyList = chatHistory.value.takeLast(10).map {
                GeminiContent(parts = listOf(GeminiPart(text = it.content)))
            }

            val response = withContext(Dispatchers.IO) {
                GeminiClient.generateResponse(prompt, chatHistory = historyList)
            }

            // Save Response
            repository.insertMessage(ChatMessage(sender = "afia", content = response))
            addLog("Afia output processed. Activating acoustic vocalizer.")
            speak(response)
        }
    }

    // --- Offline Local Command Execution Engine ---
    private suspend fun executeCommandLocally(rawText: String): Boolean {
        val text = rawText.lowercase().trim()

        // 1. Check Custom DB Shortcuts first
        val currentShortcuts = shortcuts.value
        val matchedShortcut = currentShortcuts.firstOrNull { text.contains(it.phrase.lowercase()) }
        if (matchedShortcut != null && matchedShortcut.isEnabled) {
            addLog("Intercepted Custom Shortcut mapping: \"${matchedShortcut.phrase}\"")
            executeShortcutAction(matchedShortcut)
            return true
        }

        // 2. Hardware: Flashlight ON
        if (text.contains("flashlight on") || text.contains("light on") || text.contains("torch on") || text.contains("photon on")) {
            val res = deviceController.setFlashlight(true)
            repository.insertMessage(ChatMessage(sender = "afia", content = res))
            addLog("Core Command Executed: Flashlight ON")
            speak(res)
            return true
        }

        // 3. Hardware: Flashlight OFF
        if (text.contains("flashlight off") || text.contains("light off") || text.contains("torch off") || text.contains("photon off")) {
            val res = deviceController.setFlashlight(false)
            repository.insertMessage(ChatMessage(sender = "afia", content = res))
            addLog("Core Command Executed: Flashlight OFF")
            speak(res)
            return true
        }

        // 4. Hardware: Battery Check
        if (text.contains("check battery") || text.contains("battery level") || text.contains("battery status") || text.contains("power status")) {
            val res = deviceController.getBatteryStatus()
            val fullRep = "Diagnostic scanner return data: $res"
            repository.insertMessage(ChatMessage(sender = "afia", content = fullRep))
            addLog("Core Command Executed: Battery Scan")
            speak(fullRep)
            return true
        }

        // 5. Hardware Diagnostics: Memory/RAM
        if (text.contains("check ram") || text.contains("ram info") || text.contains("memory diagnostic") || text.contains("ram status")) {
            val ram = deviceController.getRamDiagnostics()
            val reply = "RAM telemetry scanner: Total capacity ${ram.totalGB}GB. Currently utilizing ${ram.usedGB}GB. Status: ${if (ram.isLowMem) "CRITICAL OVERFLOW RISK" else "NOMINAL ENERGETICS"}."
            repository.insertMessage(ChatMessage(sender = "afia", content = reply))
            addLog("Core Command Executed: RAM Check")
            speak(reply)
            return true
        }

        // 6. Hardware Diagnostics: Storage
        if (text.contains("check storage") || text.contains("storage space") || text.contains("storage info") || text.contains("disk check")) {
            val store = deviceController.getStorageDiagnostics()
            val reply = "Disk Sector Diagnostic: Total files volume size ${store.totalGB}GB. Stored data volume ${store.usedGB}GB. Available block indices size ${store.availableGB}GB."
            repository.insertMessage(ChatMessage(sender = "afia", content = reply))
            addLog("Core Command Executed: Disk Space Check")
            speak(reply)
            return true
        }

        // 7. System Launches: Settings
        if (text == "open settings" || text.contains("launch configuration settings")) {
            val reply = deviceController.openSettings()
            repository.insertMessage(ChatMessage(sender = "afia", content = reply))
            speak(reply)
            return true
        }

        // 8. System Launches: Camera
        if (text == "open camera" || text == "launch camera") {
            val reply = deviceController.launchCamera()
            repository.insertMessage(ChatMessage(sender = "afia", content = reply))
            speak(reply)
            return true
        }

        // 9. System Launches: Gallery
        if (text == "open gallery" || text == "launch gallery") {
            val reply = deviceController.openGallery()
            repository.insertMessage(ChatMessage(sender = "afia", content = reply))
            speak(reply)
            return true
        }

        // 10. Cyber Music / Play Beats
        if (text.contains("play music") || text.contains("synth music") || text.contains("start music") || text.contains("ambient synth")) {
            val response = if (!_isSynthRunning.value) {
                _isSynthRunning.value = true
                deviceController.playCyberSynth()
            } else {
                "Synth engine already discharging frequencies."
            }
            repository.insertMessage(ChatMessage(sender = "afia", content = response))
            addLog(response)
            speak(response)
            return true
        }

        if (text.contains("pause music") || text.contains("stop music") || text.contains("stop synth") || text.contains("pause synth")) {
            val response = if (_isSynthRunning.value) {
                _isSynthRunning.value = false
                deviceController.stopCyberSynth()
            } else {
                "Synth engine already idle."
            }
            repository.insertMessage(ChatMessage(sender = "afia", content = response))
            addLog(response)
            speak(response)
            return true
        }

        // 11. Custom System launches: Google/YouTube with queries
        if (text.startsWith("search google for ") || text.startsWith("google ")) {
            val q = rawText.substring(if (text.startsWith("google ")) 7 else 18)
            val reply = deviceController.searchGoogle(q)
            repository.insertMessage(ChatMessage(sender = "afia", content = "Searching Google databases for \"$q\"."))
            speak("Searching Google database parameters.")
            return true
        }

        if (text.startsWith("search youtube for ") || text.startsWith("youtube ")) {
            val q = rawText.substring(if (text.startsWith("youtube ")) 8 else 19)
            val reply = deviceController.launchYouTube(q)
            repository.insertMessage(ChatMessage(sender = "afia", content = "Opening YouTube feed buffer for \"$q\"."))
            speak("Buffering YouTube link parameters.")
            return true
        }

        if (text == "open youtube") {
            val reply = deviceController.launchYouTube()
            repository.insertMessage(ChatMessage(sender = "afia", content = reply))
            speak(reply)
            return true
        }

        // 12. Phone Calls & Messages
        if (text.startsWith("call ")) {
            val rawNum = text.substring(5).trim()
            val reply = deviceController.makeCall(rawNum)
            repository.insertMessage(ChatMessage(sender = "afia", content = reply))
            speak(reply)
            return true
        }

        if (text.startsWith("sms ") || text.startsWith("text ")) {
            // e.g. "sms 12345 message Hello Afia"
            val bodyIndex = text.indexOf("message")
            val targetNum = if (bodyIndex != -1) text.substring(4, bodyIndex).trim() else text.substring(4).trim()
            val msgBody = if (bodyIndex != -1) rawText.substring(bodyIndex + 7).trim() else "Secure cyber transmission transmission successful."
            val reply = deviceController.sendSms(targetNum, msgBody)
            repository.insertMessage(ChatMessage(sender = "afia", content = reply))
            speak(reply)
            return true
        }

        // 13. System Automations Triggers
        if (text.contains("initiate study mode") || text.contains("enable study mode")) {
            initiateStudyMode()
            return true
        }

        if (text.contains("initiate sleep mode") || text.contains("enable sleep mode")) {
            initiateSleepMode()
            return true
        }

        if (text.contains("initiate battery saving") || text.contains("enable battery saver")) {
            initiateBatterySaver()
            return true
        }

        if (text.contains("initiate morning routine") || text.contains("good morning")) {
            initiateMorningRoutine()
            return true
        }

        return false // If nothing matches, return false to dispatch to Gemini API
    }

    private suspend fun executeShortcutAction(shortcut: CustomShortcut) {
        val type = shortcut.actionType
        val arg = shortcut.actionArg

        when (type) {
            "FLASHLIGHT" -> {
                val state = arg.trim().lowercase() != "off"
                val reply = deviceController.setFlashlight(state)
                repository.insertMessage(ChatMessage(sender = "afia", content = reply))
                speak(reply)
            }
            "VOLUME_MAX" -> {
                val reply = deviceController.setSystemVolume(100)
                repository.insertMessage(ChatMessage(sender = "afia", content = "Volume set to maximum levels!"))
                speak("Acoustics maximized.")
            }
            "SYSTEM_INFO" -> {
                val charge = deviceController.getBatteryStatus()
                val ramValue = deviceController.getRamDiagnostics()
                val replyText = "Diagnosing physical grid: $charge. RAM at ${ramValue.usedGB} of ${ramValue.totalGB}GB capacity."
                repository.insertMessage(ChatMessage(sender = "afia", content = replyText))
                speak(replyText)
            }
            "OPEN_APP" -> {
                val intentName = arg.trim().lowercase()
                val replyText = when (intentName) {
                    "youtube" -> deviceController.launchYouTube()
                    "settings" -> deviceController.openSettings()
                    "gallery" -> deviceController.openGallery()
                    "camera" -> deviceController.launchCamera()
                    else -> "Deploying cyber link: www.$arg.com"
                }
                if (intentName != "youtube" && intentName != "settings" && intentName != "gallery" && intentName != "camera") {
                    deviceController.openBrowser(arg)
                }
                repository.insertMessage(ChatMessage(sender = "afia", content = replyText))
                speak(replyText)
            }
            "SAY_TEXT" -> {
                repository.insertMessage(ChatMessage(sender = "afia", content = arg))
                speak(arg)
            }
        }
    }

    // --- Automation Triggers ---
    fun initiateStudyMode() {
        viewModelScope.launch {
            addLog("Executing Auto Routine: study_mode_protocol")
            // Turn off music/synth
            if (_isSynthRunning.value) {
                deviceController.stopCyberSynth()
                _isSynthRunning.value = false
            }
            // Mute volume/adjust volume
            deviceController.setSystemVolume(15)
            
            // Set study task enabled
            val task = automationTasks.value.firstOrNull { it.taskType == "STUDY_MODE" }
            if (task != null) {
                repository.updateAutomationTask(task.copy(isEnabled = true))
            }

            val reply = "Study Protocol Activated. Neural soundwaves suspended. Main notifications set to low priority mode. Concentrating on work parameters."
            repository.insertMessage(ChatMessage(sender = "afia", content = reply))
            speak(reply)
        }
    }

    fun initiateSleepMode() {
        viewModelScope.launch {
            addLog("Executing Auto Routine: sleep_mode_subsurface")
            // Flashlight OFF
            deviceController.setFlashlight(false)
            // Synth off
            if (_isSynthRunning.value) {
                deviceController.stopCyberSynth()
                _isSynthRunning.value = false
            }
            // Sound min
            deviceController.setSystemVolume(5)

            val task = automationTasks.value.firstOrNull { it.taskType == "SLEEP_MODE" }
            if (task != null) {
                repository.updateAutomationTask(task.copy(isEnabled = true))
            }

            val reply = "Sleep routine deployed. Shutting down photon emitters, muting synth streams, and lowering system decibels. Goodnight, commander."
            repository.insertMessage(ChatMessage(sender = "afia", content = reply))
            speak(reply)
        }
    }

    fun initiateBatterySaver() {
        viewModelScope.launch {
            addLog("Executing Auto Routine: battery_power_conservation")
            deviceController.setFlashlight(false)
            if (_isSynthRunning.value) {
                deviceController.stopCyberSynth()
                _isSynthRunning.value = false
            }

            val task = automationTasks.value.firstOrNull { it.taskType == "BATTERY_SAVER" }
            if (task != null) {
                repository.updateAutomationTask(task.copy(isEnabled = true))
            }

            val battery = deviceController.getBatteryPercent()
            val reply = "Overdrive battery saver protocol operational. Current load: $battery%. Hardware optimization initialized."
            repository.insertMessage(ChatMessage(sender = "afia", content = reply))
            speak(reply)
        }
    }

    fun initiateMorningRoutine() {
        viewModelScope.launch {
            addLog("Executing Auto Routine: morning_routine_awakening")
            val charge = deviceController.getBatteryPercent()
            
            val task = automationTasks.value.firstOrNull { it.taskType == "MORNING" }
            if (task != null) {
                repository.updateAutomationTask(task.copy(isEnabled = true))
            }

            val reply = "Good morning, commander. Host system is online. Secondary grid battery level is at $charge%. All sectors nominal. Shall we begin the daily objectives?"
            repository.insertMessage(ChatMessage(sender = "afia", content = reply))
            speak(reply)
        }
    }

    fun resetAutomationTask(task: AutomationTask) {
        viewModelScope.launch {
            repository.updateAutomationTask(task.copy(isEnabled = !task.isEnabled))
            addLog("Modified routine status: ${task.name} to ${if (!task.isEnabled) "ONLINE" else "DISABLED"}")
        }
    }

    // --- Shortcuts Management ---
    fun addShortcut(phrase: String, actionType: String, actionArg: String) {
        viewModelScope.launch {
            repository.insertShortcut(CustomShortcut(phrase = phrase, actionType = actionType, actionArg = actionArg))
            addLog("New command phrase mapped: \"$phrase\" -> $actionType")
        }
    }

    fun deleteShortcutItem(shortcut: CustomShortcut) {
        viewModelScope.launch {
            repository.deleteShortcut(shortcut)
            addLog("Removed voice command mapping: \"${shortcut.phrase}\"")
        }
    }

    // --- Notes Board Management ---
    fun saveNote(title: String, content: String) {
        viewModelScope.launch {
            repository.insertNote(WorkspaceNote(title = title, content = content))
            addLog("Saved local workspace file: \"$title\"")
        }
    }

    fun deleteNoteItem(note: WorkspaceNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
            addLog("Cleared workspace note: \"${note.title}\"")
        }
    }

    // --- Ocular Visual Analysis Core ---
    fun performOcularAnalysis(prompt: String, bitmap: android.graphics.Bitmap) {
        _isAnalyzingOcular.value = true
        _ocularAnalysisResult.value = ""
        addLog("Optical feed received. Converting to strategic binary stream...")

        viewModelScope.launch {
            // Encode bitmap to base64
            val base64 = withContext(Dispatchers.IO) {
                val stream = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, stream)
                Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            }

            addLog("Ocular payload built. Querying OCR/Visual model matrix...")
            val result = withContext(Dispatchers.IO) {
                GeminiClient.generateImageAnalysis(prompt, base64)
            }

            _ocularAnalysisResult.value = result
            _isAnalyzingOcular.value = false
            addLog("Optical core analysis resolved.")
            speak("Optical telemetry parsed successfully.")
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
            addLog("Telemetry chat logs purged completely.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceSystem?.shutdown()
        deviceController.stopCyberSynth()
    }
}
