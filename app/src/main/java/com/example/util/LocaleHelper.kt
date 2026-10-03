package com.example.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.example.data.model.AppLanguage
import java.util.Locale

object LocaleHelper {

    fun setLocale(context: Context, appLanguage: AppLanguage): Context {
        val targetLocale = when (appLanguage) {
            AppLanguage.UZBEK -> Locale("uz")
            AppLanguage.RUSSIAN -> Locale("ru")
            AppLanguage.ENGLISH -> Locale("en")
            AppLanguage.SYSTEM -> {
                // If system language is uz or ru, keep it, else default
                val defaultLang = Locale.getDefault().language
                if (defaultLang.startsWith("uz")) Locale("uz")
                else if (defaultLang.startsWith("ru")) Locale("ru")
                else Locale("en")
            }
        }

        Locale.setDefault(targetLocale)

        val res = context.resources
        val config = Configuration(res.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(targetLocale))
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = targetLocale
            @Suppress("DEPRECATION")
            res.updateConfiguration(config, res.displayMetrics)
            return context
        }
    }
}
