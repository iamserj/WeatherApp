package ru.iamserj.weatherapp;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * @author iamserj
 * 11.09.2019 1:55
 */

final class AppCache {
	
	private static final String CITY_KEY = "city";
	private static final String DEFAULT_CITY = "Moscow";
	private SharedPreferences userPreferences;
	
	AppCache(Activity activity) {
		userPreferences = activity.getPreferences(Activity.MODE_PRIVATE);
	}
	
	String getSavedCity() {
		return userPreferences.getString(CITY_KEY, DEFAULT_CITY);   // get last used city, or return default
	}
	
	void saveCity(String city) {
		userPreferences.edit().putString(CITY_KEY, city).apply();
	}
	
}
