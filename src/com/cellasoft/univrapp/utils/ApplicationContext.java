package com.cellasoft.univrapp.utils;

import android.content.Context;

public class ApplicationContext extends android.app.Application {
	private static ApplicationContext instance;

	public ApplicationContext() {
		instance = this;
	}

	public static Context getInstance() {
		if (null == instance) 
			instance = new ApplicationContext();		

		return instance;
	}
}