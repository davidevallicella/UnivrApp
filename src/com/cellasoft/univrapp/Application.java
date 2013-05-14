package com.cellasoft.univrapp;

import java.util.Stack;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import com.github.droidfu.DroidFuApplication;

@ReportsCrashes(formKey = "dFFyRWpQWXV4blpmazN3MFo4VllKTUE6MQ")
public class Application extends DroidFuApplication {
	private static Application instance;
	public static Stack<Class<?>> parents = new Stack<Class<?>>();

	public Application() {
		instance = this;
	}

	public static Application getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
	}
}