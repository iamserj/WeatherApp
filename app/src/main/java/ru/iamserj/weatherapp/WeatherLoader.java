package ru.iamserj.weatherapp;

import android.util.Log;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author iamserj
 * 11.09.2019 1:59
 */

public final class WeatherLoader {
	
	private static final String OPEN_WEATHER_MAP_API = "https://api.openweathermap.org/data/2.5/weather?q=%s&&apikey=%s&units=metric";
	private static final String API_KEY = "b4ac708d44b9d2bf58d4abdbd0bf03ee";
	private static final String RESPONSE = "cod";
	private static final String NEW_LINE = "\n";
	private static final int RESPONSE_SUCCESS = 200;
	private static final String LOG_TAG = "-------------------->";
	
	static JSONObject getJsonByCityName(String city) {
		
		try {
			URL url = new URL(String.format(OPEN_WEATHER_MAP_API, city, API_KEY));
			
			Log.d(LOG_TAG, url.toString());
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder rawData = new StringBuilder(1024);
			String tempVariable;
			while ((tempVariable = reader.readLine()) != null) {
				rawData.append(tempVariable).append(NEW_LINE);
			}
			reader.close();
			
			JSONObject jsonObject = new JSONObject(rawData.toString());
			if (jsonObject.getInt(RESPONSE) == RESPONSE_SUCCESS) {
				return jsonObject;
			} else {
				return null;//FIXME Обработка ошибки
			}
		} catch (Exception e) {
			System.out.println("\n-------exception-------");
			System.out.println(e);
			System.out.println("-------exception-------");
			Log.d(LOG_TAG, "Error requesting server");
			return null; //FIXME Обработка ошибки
		}
	}
	
	private WeatherLoader() {}
}
