package com.example.ai

import com.example.BuildConfig
import com.example.data.model.AiSoundscapeResponse
import com.example.data.model.AppLanguage
import com.example.data.model.SoundId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiRelaxationService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun generateSoundscape(
        userPrompt: String,
        language: AppLanguage
    ): Result<AiSoundscapeResponse> = withContext(Dispatchers.IO) {
        val apiKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            (field.get(null) as? String) ?: ""
        } catch (e: Exception) {
            ""
        }

        // If no API key configured or offline, use intelligent procedural fallback
        if (apiKey.isNullOrBlank() || apiKey.contains("MY_GEMINI_API_KEY")) {
            return@withContext Result.success(getSmartLocalFallback(userPrompt, language))
        }

        try {
            val langInstruction = when (language) {
                AppLanguage.UZBEK -> "Javobni toza va chiroyli o'zbek tilida yozing."
                AppLanguage.RUSSIAN -> "Напишите ответ на красивом и спокойном русском языке."
                else -> "Write the response in calm, soothing English."
            }

            val systemInstruction = """
                You are Nature Calm's expert soundscape alchemist and meditation guide.
                Analyze the user's emotional state, activity, or requested atmosphere: "$userPrompt".
                Recommend a custom soundscape mix by choosing 1 to 4 sounds from these exact IDs:
                - gentle_rain
                - heavy_rain
                - forest
                - ocean
                - river
                - wind
                - birds
                - fireplace
                - night_nature
                - rainy_cafe
                - rain_window
                - peaceful_garden

                Assign volume levels between 0.15 and 0.85 for each chosen sound.
                Choose a recommended sleep timer in minutes (10, 20, 30, 45, or 60).
                Provide a short 1-2 sentence comforting mindfulness reflection note.
                $langInstruction

                Respond strictly with valid JSON conforming to this schema:
                {
                   "title": "Short poetic atmosphere title",
                   "description": "One sentence explaining why this mix brings calm",
                   "recommendedTimerMinutes": 30,
                   "meditationNote": "Soothing mindfulness advice",
                   "volumes": {
                      "gentle_rain": 0.7,
                      "fireplace": 0.4
                   }
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", systemInstruction)
                            })
                        })
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.7)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.success(getSmartLocalFallback(userPrompt, language))
            }

            val responseBody = response.body?.string() ?: ""
            val jsonRoot = JSONObject(responseBody)
            val candidates = jsonRoot.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val contentObj = candidates.getJSONObject(0).optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val rawText = parts.getJSONObject(0).optString("text")
                    val parsed = parseJsonResponse(rawText)
                    if (parsed != null) {
                        return@withContext Result.success(parsed)
                    }
                }
            }
            Result.success(getSmartLocalFallback(userPrompt, language))
        } catch (e: Exception) {
            Result.success(getSmartLocalFallback(userPrompt, language))
        }
    }

    private fun parseJsonResponse(rawText: String): AiSoundscapeResponse? {
        return try {
            val json = JSONObject(rawText)
            val title = json.getString("title")
            val desc = json.getString("description")
            val timer = json.optInt("recommendedTimerMinutes", 30)
            val note = json.optString("meditationNote", "")
            val volsObj = json.getJSONObject("volumes")
            val volumes = mutableMapOf<SoundId, Float>()
            val keys = volsObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val soundId = SoundId.fromKey(key)
                val vol = volsObj.optDouble(key, 0.6).toFloat()
                volumes[soundId] = vol.coerceIn(0.1f, 1.0f)
            }
            if (volumes.isEmpty()) {
                volumes[SoundId.GENTLE_RAIN] = 0.75f
            }
            AiSoundscapeResponse(
                title = title,
                description = desc,
                volumes = volumes,
                recommendedTimerMinutes = timer,
                meditationNote = note
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getSmartLocalFallback(prompt: String, language: AppLanguage): AiSoundscapeResponse {
        val lower = prompt.lowercase()
        return when {
            lower.contains("sleep") || lower.contains("uxla") || lower.contains("сон") || lower.contains("night") -> {
                when (language) {
                    AppLanguage.UZBEK -> AiSoundscapeResponse(
                        title = "Tungi sokin orom",
                        description = "Chuqur va osoyishta uyquga eltuvchi mayin yomg'ir va tun nafasi.",
                        volumes = mapOf(SoundId.GENTLE_RAIN to 0.75f, SoundId.NIGHT_NATURE to 0.35f, SoundId.WIND to 0.20f),
                        recommendedTimerMinutes = 45,
                        meditationNote = "Ko'zlaringizni yuming, yelkangizni bo'sh qo'ying va har bir nafasda vujudingizdagi charchoq tarqalayotganini his eting."
                    )
                    AppLanguage.RUSSIAN -> AiSoundscapeResponse(
                        title = "Ночное умиротворение",
                        description = "Мягкий дождь и тихий шелест ночи для быстрого и глубокого засыпания.",
                        volumes = mapOf(SoundId.GENTLE_RAIN to 0.75f, SoundId.NIGHT_NATURE to 0.35f, SoundId.WIND to 0.20f),
                        recommendedTimerMinutes = 45,
                        meditationNote = "Закройте глаза, отпустите напряжение дня и позвольте мягкому ритму дождя убаюкать вас."
                    )
                    else -> AiSoundscapeResponse(
                        title = "Nocturnal Sanctuary",
                        description = "Gentle rain and night nature interwoven for a seamless transition into restorative deep sleep.",
                        volumes = mapOf(SoundId.GENTLE_RAIN to 0.75f, SoundId.NIGHT_NATURE to 0.35f, SoundId.WIND to 0.20f),
                        recommendedTimerMinutes = 45,
                        meditationNote = "Close your eyes, drop your shoulders, and let the rhythmic patter of rain wash away the day's fatigue."
                    )
                }
            }
            lower.contains("study") || lower.contains("dars") || lower.contains("fokus") || lower.contains("учеб") || lower.contains("focus") -> {
                when (language) {
                    AppLanguage.UZBEK -> AiSoundscapeResponse(
                        title = "Chuqur diqqat maskani",
                        description = "Diqqatni bir joyga jamlash va samarali o'qish uchun shinam qahvaxona va yomg'ir sadolari.",
                        volumes = mapOf(SoundId.RAINY_CAFE to 0.65f, SoundId.RAIN_WINDOW to 0.40f, SoundId.WIND to 0.20f),
                        recommendedTimerMinutes = 45,
                        meditationNote = "Barcha chalg'ituvchi fikrlarni ortda qoldiring va bor e'tiboringizni hozirgi vazifangizga qarating."
                    )
                    AppLanguage.RUSSIAN -> AiSoundscapeResponse(
                        title = "Пространство фокуса",
                        description = "Атмосфера уютной дождливой кофейни, настраивающая на продуктивную работу и учебу.",
                        volumes = mapOf(SoundId.RAINY_CAFE to 0.65f, SoundId.RAIN_WINDOW to 0.40f, SoundId.WIND to 0.20f),
                        recommendedTimerMinutes = 45,
                        meditationNote = "Отпустите суету, сосредоточьтесь на одной задаче и позвольте ритму звуков поддерживать ваш фокус."
                    )
                    else -> AiSoundscapeResponse(
                        title = "Deep Focus Chamber",
                        description = "Warm cafe ambience and window rain creating an insulated cocoon for deep cognitive immersion.",
                        volumes = mapOf(SoundId.RAINY_CAFE to 0.65f, SoundId.RAIN_WINDOW to 0.40f, SoundId.WIND to 0.20f),
                        recommendedTimerMinutes = 45,
                        meditationNote = "Release distractions and allow the steady acoustic cocoon to center your thoughts."
                    )
                }
            }
            lower.contains("anxiety") || lower.contains("stress") || lower.contains("xavotir") || lower.contains("тревог") -> {
                when (language) {
                    AppLanguage.UZBEK -> AiSoundscapeResponse(
                        title = "Sokin bog' va dengiz nafasi",
                        description = "Ichki hayajon va stressni bartaraf etuvchi tinchlantiruvchi okean to'lqinlari.",
                        volumes = mapOf(SoundId.OCEAN to 0.70f, SoundId.PEACEFUL_GARDEN to 0.45f, SoundId.WIND to 0.25f),
                        recommendedTimerMinutes = 30,
                        meditationNote = "Chuqur 4 soniya nafas oling, 4 soniya ushlab turing va sekin 6 soniyada nafas chiqaring. Siz xavfsizsiz."
                    )
                    AppLanguage.RUSSIAN -> AiSoundscapeResponse(
                        title = "Океанический покой",
                        description = "Ритмичные волны океана и дыхание сада для мгновенного снятия напряжения.",
                        volumes = mapOf(SoundId.OCEAN to 0.70f, SoundId.PEACEFUL_GARDEN to 0.45f, SoundId.WIND to 0.25f),
                        recommendedTimerMinutes = 30,
                        meditationNote = "Сделайте глубокий вдох на 4 счета, задержите дыхание и медленно выдохните на 6 счетов. Все хорошо."
                    )
                    else -> AiSoundscapeResponse(
                        title = "Tidal Serenity",
                        description = "Rhythmic ocean surges and zen garden wind chimes designed to calm a racing pulse.",
                        volumes = mapOf(SoundId.OCEAN to 0.70f, SoundId.PEACEFUL_GARDEN to 0.45f, SoundId.WIND to 0.25f),
                        recommendedTimerMinutes = 30,
                        meditationNote = "Inhale for 4 seconds, pause for 4, and exhale slowly for 6. You are safe and grounded."
                    )
                }
            }
            else -> {
                when (language) {
                    AppLanguage.UZBEK -> AiSoundscapeResponse(
                        title = "Tog' o'rmoni oromi",
                        description = "Daraxtlar nafasi, qushlar sayrashi va musaffo daryo oqimi bilan vujudingizni yangilang.",
                        volumes = mapOf(SoundId.FOREST to 0.70f, SoundId.BIRDS to 0.55f, SoundId.RIVER to 0.40f),
                        recommendedTimerMinutes = 30,
                        meditationNote = "Tabiatning toza energiyasi sizga kuch va xotirjamlik bag'ishlasin."
                    )
                    AppLanguage.RUSSIAN -> AiSoundscapeResponse(
                        title = "Лесное уединение",
                        description = "Шелест сосен, горный ручей и пение птиц для восстановления душевных сил.",
                        volumes = mapOf(SoundId.FOREST to 0.70f, SoundId.BIRDS to 0.55f, SoundId.RIVER to 0.40f),
                        recommendedTimerMinutes = 30,
                        meditationNote = "Почувствуйте свежесть горного воздуха и спокойствие первозданной природы."
                    )
                    else -> AiSoundscapeResponse(
                        title = "Highland Woodland Retreat",
                        description = "Pine canopy rustle, singing birds, and a cascading stream for complete restorative peace.",
                        volumes = mapOf(SoundId.FOREST to 0.70f, SoundId.BIRDS to 0.55f, SoundId.RIVER to 0.40f),
                        recommendedTimerMinutes = 30,
                        meditationNote = "Feel the crisp pine air recharge your spirit as nature carries your worries away."
                    )
                }
            }
        }
    }
}
