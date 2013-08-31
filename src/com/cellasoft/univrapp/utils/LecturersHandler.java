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

import java.io.IOException;
import java.util.List;

import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.widget.ContactItemInterface;
import com.google.gson.Gson;

/**
 * Handler that parses room JSON data into a list of content provider
 * operations.
 */
public class LecturersHandler extends JSONHandler {

	public List<ContactItemInterface> parse(String jsonString)
			throws IOException {
		List<ContactItemInterface> result = Lists.newArrayList();

		LecturerList list = null;
		list = getLecturerList(jsonString);

		if (list != null) {

			List<LecturerContainer> lecturers = list
					.getLecturerContainterList();

			for (LecturerContainer lc : lecturers) {
				result.add(lc.getLecturer());
			}
		}

		return result;
	}

	protected LecturerList getLecturerList(String jsonString) {
		LecturerList ll = null;
		ll = new Gson().fromJson(jsonString, LecturerList.class);
		return ll;
	}

	class LecturerList {

		private List<LecturerContainer> lecturers = Lists.newArrayList();

		public List<LecturerContainer> getLecturerContainterList() {
			return lecturers;
		}
	}

	class LecturerContainer {
		Lecturer lecturer;

		public Lecturer getLecturer() {
			return lecturer;
		}
	}
}
