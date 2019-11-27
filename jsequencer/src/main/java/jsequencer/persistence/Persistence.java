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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jsequencer.ui.model.SongModel;

public class Persistence {

	private final JsonWriterFactory writerFactory;
	private int currentSong = 0;
	private final int MAX_COPIES = 10;
	private String currentJson = "";
	
	private SongModel model;
	private final SongModel defaultModel;
	private final String defaultJsonString; 
	private static final Logger logger = LoggerFactory.getLogger(Persistence.class);
	private final Path persistencePath;
	private Lock lock = new ReentrantLock();
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	public Persistence(SongModel model, int currentSong) {
		this.model = model;
		final Map<String, Object> properties = new HashMap<>();
		properties.put(JsonGenerator.PRETTY_PRINTING, true);
		writerFactory = Json.createWriterFactory(properties);
		defaultModel = new SongModel(8, 4);
		defaultJsonString = modelToString(defaultModel);
		persistencePath = Paths.get(System.getProperty("user.home"), ".jsequencer");
		if (!Files.exists(persistencePath)) {
			try {
				Files.createDirectories(persistencePath);
			} catch (IOException e) {
				logger.error("Can't create Persistence Path {}", persistencePath.toAbsolutePath().toString());
			}
		}
		loadSong(currentSong);
		executor.scheduleAtFixedRate(() -> writeSong(), 10, 10, TimeUnit.SECONDS); 
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
	
	private Path getSongPath(int song, int revision) {
		return persistencePath.resolve("SONG_" + song + "_" + revision + ".json");
	}

	private void moveRevisions(int song) {
		Path lastRevision = getSongPath(song, MAX_COPIES);
		try {
			if (Files.exists(lastRevision)) {
				Files.delete(lastRevision);
			}
			for (int i = MAX_COPIES; i > 0; --i) {
				Path nextRevision = getSongPath(song, i-1);
				if (Files.exists(nextRevision)) {
					Files.move(nextRevision, lastRevision, StandardCopyOption.REPLACE_EXISTING);
				}
				lastRevision = nextRevision;
			}
		} catch (IOException e) {
			logger.error("Moving old revisions failed");
		}
	}
	
	public void writeSong() {
		String songJson = null;
		boolean identical = true;
		int song = -1;
		try {
			song = currentSong;
			songJson = modelToString(model);
			identical = Objects.equals(songJson, currentJson);
		} finally {
			lock.unlock();
		}
		if (! (identical || songJson == null || songJson == null)) {
			try (FileWriter fw = new FileWriter(getSongPath(song, 0).toFile())) {
				fw.write(songJson);
				moveRevisions(song);
			} catch (IOException e) {
				logger.error("Write Song failed", e);
			}
		}
	}
	
	public void loadSong(int song) {
		Path songPath = getSongPath(song, 0);
		lock.lock();
		try {
			if (Files.exists(songPath)) {
				try {
					String newJson = FileUtils.readFileToString(songPath.toFile(), StandardCharsets.UTF_8);	
					JsonReader reader = Json.createReader(new StringReader(currentJson));
			        JsonObject json = reader.readObject();
			        model.fromJson(json);
			        reader.close();
			        currentJson = newJson;
				} catch (IOException e) {
					logger.error("Loading song failed", e);
				}
			} else {
				model.fromJson(defaultModel.toJson());
				currentJson = defaultJsonString;
			}
			currentSong = song;
		} finally {
			lock.unlock();
		}
	}
}
