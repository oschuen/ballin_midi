/**
 * Copyright (C) 2019 Oliver Schünemann
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
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

	private final SongModel model;
	private final SongModel defaultModel;
	private final String defaultJsonString;
	private static final Logger logger = LoggerFactory.getLogger(Persistence.class);
	private final Path persistencePath;
	private final Lock lock = new ReentrantLock();
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	public Persistence(final SongModel model, final int currentSong) {
		this.model = model;
		this.currentSong = currentSong;
		final Map<String, Object> properties = new HashMap<>();
		properties.put(JsonGenerator.PRETTY_PRINTING, true);
		writerFactory = Json.createWriterFactory(properties);
		defaultModel = new SongModel(8, 4);
		defaultJsonString = modelToString(defaultModel);
		persistencePath = Paths.get(System.getProperty("user.home"), ".jsequencer");
		if (!Files.exists(persistencePath)) {
			try {
				Files.createDirectories(persistencePath);
			} catch (final IOException e) {
				logger.error("Can't create Persistence Path {}",
						persistencePath.toAbsolutePath().toString());
			}
		}
		loadSong(currentSong);
		executor.scheduleAtFixedRate(() -> writeSong(), 10, 10, TimeUnit.SECONDS);
	}

	private String modelToString(final SongModel model) {
		try (final ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			final JsonWriter writer = writerFactory.createWriter(out);
			writer.write(model.toJson());
			return new String(out.toByteArray());
		} catch (final IOException e) {
			logger.error("Model To String failed", e);
		}
		return defaultJsonString;
	}

	private Path getSongPath(final int song, final int revision) {
		return persistencePath.resolve("SONG_" + song + "_" + revision + ".json");
	}

	private void moveRevisions(final int song) {
		Path lastRevision = getSongPath(song, MAX_COPIES);
		try {
			if (Files.exists(lastRevision)) {
				Files.delete(lastRevision);
			}
			for (int i = MAX_COPIES; i > 0; --i) {
				final Path nextRevision = getSongPath(song, i - 1);
				if (Files.exists(nextRevision)) {
					Files.move(nextRevision, lastRevision, StandardCopyOption.REPLACE_EXISTING);
				}
				lastRevision = nextRevision;
			}
		} catch (final IOException e) {
			logger.error("Moving old revisions failed");
		}
	}

	public int getCurrentSong() {
		return currentSong;
	}

	public boolean[] getSongMatrix() {
		final boolean[] matrix = new boolean[64];
		for (int i = 0; i < matrix.length; i++) {
			final Path songPath = getSongPath(i, 0);
			matrix[i] = Files.exists(songPath);
		}
		return matrix;
	}

	public void writeSong() {
		String songJson = null;
		boolean identical = true;
		lock.lock();
		try {
			songJson = modelToString(model);
			identical = Objects.equals(songJson, currentJson);
			currentJson = songJson;
			if (!(identical || songJson == null || songJson == null)) {
				moveRevisions(currentSong);
				try (FileWriter fw = new FileWriter(getSongPath(currentSong, 0).toFile())) {
					fw.write(songJson);
				} catch (final IOException e) {
					logger.error("Write Song failed", e);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public void loadSong(final int song) {
		final Path songPath = getSongPath(song, 0);
		lock.lock();
		try {
			if (Files.exists(songPath)) {
				try {
					final String newJson = FileUtils.readFileToString(songPath.toFile(),
							StandardCharsets.UTF_8);
					final JsonReader reader = Json.createReader(new StringReader(newJson));
					final JsonObject json = reader.readObject();
					model.fromJson(json);
					reader.close();
					currentJson = newJson;
				} catch (final IOException e) {
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

	public void deleteSong(final int song) {
		lock.lock();
		try {
			if (song == currentSong) {
				writeSong();
			}
			moveRevisions(song);
			try (FileWriter fw = new FileWriter(getSongPath(song, 0).toFile())) {
				fw.write(defaultJsonString);
			} catch (final IOException e) {
				logger.error("Write Song failed", e);
			}
			if (song == currentSong) {
				loadSong(song);
			}
		} finally {
			lock.unlock();
		}
	}

	public void copySong(final int fromSong, final int toSong) {
		lock.lock();
		try {
			if (toSong == currentSong) {
				writeSong();
			}
			moveRevisions(toSong);
			final Path songPath = getSongPath(fromSong, 0);
			try {
				final String newJson;
				if (Files.exists(songPath)) {
					newJson = FileUtils.readFileToString(songPath.toFile(), StandardCharsets.UTF_8);
				} else {
					newJson = defaultJsonString;
				}
				try (FileWriter fw = new FileWriter(getSongPath(toSong, 0).toFile())) {
					fw.write(newJson);
				} catch (final IOException e) {
					logger.error("Write Song failed", e);
				}
			} catch (final IOException e) {
				logger.error("Failed to copy song");
			}
			if (toSong == currentSong) {
				loadSong(toSong);
			}
		} finally {
			lock.unlock();
		}

	}
}
