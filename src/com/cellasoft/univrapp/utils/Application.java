package com.cellasoft.univrapp.utils;

import com.github.droidfu.DroidFuApplication;

public class Application extends DroidFuApplication {
	private static Application instance;

	public Application() {
		instance = this;
	}

	public static Application getInstance() {
		return instance;
	}
}