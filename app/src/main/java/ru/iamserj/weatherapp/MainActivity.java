package ru.iamserj.weatherapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author iamserj
 * 11.09.2019 1:50
 */

public class MainActivity extends AppCompatActivity {
	
	private static final String FONT_FILENAME = "fonts/weathericons.ttf";
	private AppCache appCache;
	private final Handler handler = new Handler();
	
	private Typeface weatherFont;
	private TextView cityTextView;
	private TextView updatedTextView;
	private TextView detailsTextView;
	private TextView currentTemperatureTextView;
	private TextView weatherIcon;
	
	protected LocationManager locationManager;
	protected LocationListener locationListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// TODO: update for Pie version
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		appCache = new AppCache(this);
		weatherFont = Typeface.createFromAsset(getAssets(), FONT_FILENAME);
		
		setViews();
		
		updateWeatherData(appCache.getSavedCity());
	}
	
	private void setViews() {
		cityTextView = findViewById(R.id.city_field);
		updatedTextView = findViewById(R.id.updated_field);
		detailsTextView = findViewById(R.id.details_field);
		currentTemperatureTextView = findViewById(R.id.current_temperature_field);
		weatherIcon = findViewById(R.id.weather_icon);
		weatherIcon.setTypeface(weatherFont);
	}
	
	// inflate menu (res/menu/weather.xml)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.weather, menu);
		return true;
	}
	
	// menu item select handler
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.current_location) {
			getCurrentLocation();
			return true;
		}
		if (item.getItemId() == R.id.change_city) {
			showInputDialog();
			return true;
		}
		if (item.getItemId() == R.id.refresh_city) {
			updateWeatherData(appCache.getSavedCity());
			return true;
		}
		return false;
	}
	
	
	private void getCurrentLocation() {
		// TODO: check permissions granted
		// TODO: get location
		// TODO: call WeatherLoader.getJsonByCityLocation
		Toast.makeText(getApplicationContext(), "not implemented yet",
				Toast.LENGTH_LONG).show();
	}
	
	// show city select dialog
	private void showInputDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.change_city_dialog));
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);
		builder.setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				changeCity(input.getText().toString());
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}
	
	// Обновляем вид, сохраняем выбранный город
	public void changeCity(String city) {
		updateWeatherData(city);
		appCache.saveCity(city);
	}
	
	// udpate/download weather data
	private void updateWeatherData(final String city) {
		// create new thread for URL request
		new Thread() {
			public void run() {
				final JSONObject json = WeatherLoader.getJsonByCityName(city);
				// running in main thread may cause runtime error
				if (json == null) {
					handler.post(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(), getString(R.string.place_not_found),
									Toast.LENGTH_LONG).show();
						}
					});
				} else {
					handler.post(new Runnable() {
						public void run() {
							renderWeather(json);
						}
					});
				}
			}
		}.start();
	}
	
	//Обработка загруженных данных и обновление UI
	private void renderWeather(JSONObject json) {
		//Log.d("Log", "json " + json.toString());
		Toast.makeText(getApplicationContext(), getString(R.string.weather_updated),
				Toast.LENGTH_LONG).show();
		try {
			cityTextView.setText(json.getString("name").toUpperCase(Locale.US) + ", " + json.getJSONObject("sys").getString("country"));
			
			JSONObject details = json.getJSONArray("weather").getJSONObject(0);
			JSONObject main = json.getJSONObject("main");
			
			detailsTextView.setText(details.getString("description").toUpperCase(Locale.US) + "\n" +
					"Humidity: " + main.getString("humidity") + "%" + "\n" +
					"Pressure: " + main.getString("pressure") + " hPa");
			
			currentTemperatureTextView.setText(String.format("%.2f", main.getDouble("temp")) + " ℃");
			
			DateFormat df = DateFormat.getDateTimeInstance();
			String updatedOn = df.format(new Date(json.getLong("dt") * 1000));
			updatedTextView.setText("Last update: " + updatedOn);
			
			setWeatherIcon(details.getInt("id"), json.getJSONObject("sys").getLong("sunrise") * 1000, json.getJSONObject("sys").getLong("sunset") * 1000);
			
		} catch (Exception e) {
			Log.d("Log", "One or more fields not found in the JSON data");//FIXME Обработка ошибки
		}
	}
	
	// Подстановка нужной иконки
	// Парсим коды http://openweathermap.org/weather-conditions
	private void setWeatherIcon(int actualId, long sunrise, long sunset) {
		int id = actualId / 100; // Упрощение кодов (int оставляет только целочисленное значение)
		String icon = "";
		if (actualId == 800) {
			long currentTime = new Date().getTime();
			if (currentTime >= sunrise && currentTime < sunset) {
				icon = getString(R.string.weather_sunny);
			} else {
				icon = getString(R.string.weather_clear_night);
			}
		} else {
			switch (id) {
				case 2:
					icon = getString(R.string.weather_thunder);
					break;
				case 3:
					icon = getString(R.string.weather_drizzle);
					break;
				case 5:
					icon = getString(R.string.weather_rainy);
					break;
				case 6:
					icon = getString(R.string.weather_snowy);
					break;
				case 7:
					icon = getString(R.string.weather_foggy);
					break;
				case 8:
					icon = getString(R.string.weather_cloudy);
					break;
				// Можете доработать приложение, найдя все иконки и распарсив все значения
				default:
					break;
			}
		}
		weatherIcon.setText(icon);
	}
}
