/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cellasoft.univrapp.utils;

import static com.cellasoft.univrapp.utils.LogUtils.makeLogTag;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.cellasoft.univrapp.model.Lecturer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Handler that parses room JSON data into a list of content provider
 * operations.
 */
public class LecturersHandler extends JSONHandler {

	private static final String TAG = makeLogTag(LecturersHandler.class);

	public LecturersHandler(Context context) {
		super(context);
	}

	public ArrayList<Lecturer> parse(String json) throws IOException {
		Type type = new TypeToken<List<Lecturer>>() {
		}.getType();

		return (ArrayList<Lecturer>) new Gson().fromJson(json, type);
	}
}