package com.example.service

import com.example.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun generateResponse(
        prompt: String,
        chatHistory: List<GeminiContent> = emptyList(),
        model: String = "gemini-3.5-flash"
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isEmpty()) {
            return "Please configure your GEMINI_API_KEY in the Secrets panel in AI Studio to communicate with Afia."
        }

        // Build request contents combining conversation history + current prompt
        val contents = mutableListOf<GeminiContent>()
        contents.addAll(chatHistory)
        contents.add(GeminiContent(parts = listOf(GeminiPart(text = prompt))))

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = GeminiContent(
                parts = listOf(
                    GeminiPart(
                        text = "You are Afia AI, an advanced, highly intelligent futuristic cyber-assistant from the year 2099. " +
                                "Keep your answers highly efficient, futuristic (with terms like cyber core, visual buffer, diagnostic sector, quantum protocols) yet approachable. " +
                                "If the user commands system tasks (e.g. Turn flashlight on, open YouTube, set Study mode), respond confirming that Afia is executing those specific instructions!"
                    )
                )
            )
        )

        return try {
            val response = service.generateContent(model, apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Connection lost. No response sector detected."
        } catch (e: Exception) {
            "Diagnostic Error: ${e.message ?: "Unknown anomaly in transmission link."}"
        }
    }

    suspend fun generateImageAnalysis(
        prompt: String,
        base64Image: String,
        model: String = "gemini-3.5-flash"
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isEmpty()) {
            return "OCR interface offline. Please configure your GEMINI_API_KEY in the Secrets panel to resume visual telemetry."
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = prompt),
                        GeminiPart(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            ),
            systemInstruction = GeminiContent(
                parts = listOf(
                    GeminiPart(
                        text = "You are Afia AI's Optical Telemetry Core. Perform advanced visual analysis, " +
                                "OCR text extraction, writing enhancement, or mathematical parsing as requested. " +
                                "Keep your answers structured, precise, and aesthetically futuristic. If the user uploads a math equation, solve it step-by-step."
                    )
                )
            )
        )

        return try {
            val response = service.generateContent(model, apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Ocular buffer empty. No analysis generated."
        } catch (e: Exception) {
            "Ocular Sensor Error: ${e.message}"
        }
    }
}
