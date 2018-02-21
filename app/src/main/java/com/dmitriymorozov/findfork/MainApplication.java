package com.dmitriymorozov.findfork;

import android.app.Application;
import com.facebook.stetho.Stetho;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MainApplication extends Application {
		@Override public void onCreate() {
				super.onCreate();

				if(BuildConfig.DEBUG) {
						// Create an InitializerBuilder
						Stetho.InitializerBuilder initializerBuilder = Stetho.newInitializerBuilder(this);
						// Enable Chrome DevTools
						initializerBuilder.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this));
						// Enable command line interface
						initializerBuilder.enableDumpapp(Stetho.defaultDumperPluginsProvider(this));
						// Use the InitializerBuilder to generate an Initializer
						Stetho.Initializer initializer = initializerBuilder.build();
						// Initialize Stetho with the Initializer
						Stetho.initialize(initializer);
						Stetho.initializeWithDefaults(this);
				}
		}
}
