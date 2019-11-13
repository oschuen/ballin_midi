/**
 * Copyright (C) 2016 Oliver Schünemann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @since 13.12.2019
 * @version 1.0
 * @author oliver
 */
package jsequencer.persistence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jsequencer.ui.model.SongModel;

public class Persistence {

	private final JsonWriterFactory writerFactory;
	private int currentSong = 0;
	private final int MAX_COPIES = 10;
	private String currentJson = "";
	
	private final SongModel defaultModel;
	private final String defaultJsonString; 
	private static final Logger logger = LoggerFactory.getLogger(Persistence.class);
	private final Path persistencePath;
	public Persistence() {
		final Map<String, Object> properties = new HashMap<>();
		properties.put(JsonGenerator.PRETTY_PRINTING, true);
		writerFactory = Json.createWriterFactory(properties);
		defaultModel = new SongModel(8, 4);
		defaultJsonString = modelToString(defaultModel);
		persistencePath = Paths.get(System.getProperty("user.home", ".jsequencer"));
		if (!Files.exists(persistencePath)) {
			try {
				Files.createDirectories(persistencePath);
			} catch (IOException e) {
				logger.error("Can't create Persistence Path {}", persistencePath.toAbsolutePath().toString());
			}
		}
		
	}
	
	private String modelToString(SongModel model) {
		try (final ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			final JsonWriter writer = writerFactory.createWriter(out);
			writer.write(model.toJson());
			return new String(out.toByteArray());
		} catch (IOException e) {
			logger.error("Model To String failed", e);
		}
		return defaultJsonString;
	}
	

	
	
	public void loadSong(SongModel model, int song) {
		Path songPath = persistencePath.resolve("SONG_" + song);
		if (Files.exists(songPath)) {
			
		} else {
			model.fromJson(defaultModel.toJson());
		}
		currentJson = modelToString(model);
		currentSong = song;
	}
}
