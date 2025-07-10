package com.nhom11.innowave;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {
    private static final String LANGUAGE_KEY = "language_key";
    private static final String VIETNAMESE = "vi";
    private static final String ENGLISH = "en";

    public static Context onAttach(Context context) {
        String language = getPersistedLanguage(context);
        android.util.Log.d("LocaleHelper", "onAttach called with language: " + language);
        return setLocale(context, language);
    }

    public static Context onAttach(Context context, String defaultLanguage) {
        String language = getPersistedLanguage(context);
        android.util.Log.d("LocaleHelper", "onAttach called with default language: " + language);
        return setLocale(context, language);
    }

    public static String getLanguage(Context context) {
        return getPersistedLanguage(context);
    }

    public static Context setLocale(Context context, String language) {
        persistLanguage(context, language);
        android.util.Log.d("LocaleHelper", "Setting locale to: " + language);

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            Context newContext = context.createConfigurationContext(configuration);
            android.util.Log.d("LocaleHelper", "Created new context for API " + Build.VERSION.SDK_INT);
            return newContext;
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            android.util.Log.d("LocaleHelper", "Updated configuration for API " + Build.VERSION.SDK_INT);
            return context;
        }
    }

    private static String getPersistedLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String language = preferences.getString(LANGUAGE_KEY, VIETNAMESE);
        android.util.Log.d("LocaleHelper", "Getting persisted language: " + language);
        return language;
    }

    private static void persistLanguage(Context context, String language) {
        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LANGUAGE_KEY, language);
        editor.apply();
        android.util.Log.d("LocaleHelper", "Persisted language: " + language);
    }

    public static String getVietnamese() {
        return VIETNAMESE;
    }

    public static String getEnglish() {
        return ENGLISH;
    }
    
    /**
     * Kiểm tra ngôn ngữ hiện tại của context
     */
    public static String getCurrentLanguage(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        Locale currentLocale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            currentLocale = configuration.getLocales().get(0);
        } else {
            currentLocale = configuration.locale;
        }
        String currentLang = currentLocale.getLanguage();
        android.util.Log.d("LocaleHelper", "Current context language: " + currentLang);
        return currentLang;
    }
    
    /**
     * Restart activity để áp dụng ngôn ngữ mới
     */
    public static void restartActivity(android.app.Activity activity) {
        android.content.Intent intent = new android.content.Intent(activity, activity.getClass());
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.finish();
        activity.startActivity(intent);
    }
    
    /**
     * Force restart activity với ngôn ngữ mới
     */
    public static void forceRestartActivity(android.app.Activity activity) {
        android.util.Log.d("LocaleHelper", "Force restarting activity...");
        activity.recreate();
    }
    
    /**
     * Áp dụng ngôn ngữ cho activity
     */
    public static void applyLocale(android.app.Activity activity) {
        String language = getPersistedLanguage(activity);
        android.util.Log.d("LocaleHelper", "Applying locale: " + language);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        
        Resources resources = activity.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        android.util.Log.d("LocaleHelper", "Locale applied successfully");
    }

    /**
     * Force update tất cả resources với ngôn ngữ mới
     */
    public static void forceUpdateResources(android.app.Activity activity) {
        String language = getPersistedLanguage(activity);
        android.util.Log.d("LocaleHelper", "Force updating resources with language: " + language);
        
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        
        Resources resources = activity.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        android.util.Log.d("LocaleHelper", "Resources updated successfully");
    }
} 