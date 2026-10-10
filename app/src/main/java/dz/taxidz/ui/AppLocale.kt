package dz.taxidz.ui

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/** In-app language switch between French and Arabic (RTL handled by the system). */
object AppLocale {
    const val FRENCH = "fr"
    const val ARABIC = "ar"
    private const val PREFS = "taxidz_prefs"
    private const val KEY_LANG = "lang"

    fun current(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANG, null)
            ?: Locale.getDefault().language.takeIf { it == ARABIC } ?: FRENCH

    fun isRtl(lang: String): Boolean = lang == ARABIC

    /** Applies the language app-wide and persists it. Caller should recreate the activity. */
    fun apply(lang: String, context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANG, lang).apply()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
    }

    fun applySaved(context: Context) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(current(context)))
    }

    @Suppress("DEPRECATION")
    fun wrapContext(base: Context): Context {
        val lang = current(base)
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }
}
