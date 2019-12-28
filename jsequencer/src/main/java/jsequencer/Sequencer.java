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
 * @since 27.12.2016
 * @version 1.0
 * @author oliver
 */
package jsequencer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sound.midi.MidiUnavailableException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import jsequencer.persistence.Persistence;
import jsequencer.ui.model.SongModel;
import jsequencer.ui.screen.SongLayer;
import midi.loop.beat.Beat;
import midi.pad.ui.Screen;
import midi.pad.ui.event.Runtime;

/**
 * @author oliver
 *
 */
public class Sequencer {

	private static void mainSong(final File file) throws MidiUnavailableException {
		final Screen screen = Runtime.getRuntime().getScreen();
		final SongModel model = new SongModel(8, 4);
		final Beat beat = new Beat();
		final Properties props = new Properties();

		final Persistence persistence = new Persistence(model, beat, 0);
		try {
			try (InputStream stream = new FileInputStream(file)) {
				props.load(stream);
			}
		} catch (final IOException e) {
		}
		Runtime.setRuntimeConfig(props);

		final Orchester orchester = new Orchester(model, beat);
		final Controller controller = new Controller(orchester);
		Runtime.getRuntime().addControlInput(controller);
		orchester.applyConfigs();
		beat.stop();
		Runtime.getRuntime().schedule(() -> {
			final SongLayer layer = new SongLayer(orchester, model, persistence, beat);
			screen.putLayer(1, layer);
			beat.addBarListener(layer);
		});
		java.lang.Runtime.getRuntime().addShutdownHook(new Thread() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				orchester.panic();
			}
		});
	}

	public static void main(final String[] args) throws MidiUnavailableException {
		final Options options = new Options();
		options.addOption("c", "config", true, "Configuration File");
		final CommandLineParser parser = new DefaultParser();
		boolean error = false;
		File file = null;
		try {
			final CommandLine cmd = parser.parse(options, args);
			final String configFile = cmd.getOptionValue("c");
			if (configFile == null) {
				error = true;
				file = null;
			} else {
				file = new File(configFile);
			}
		} catch (final ParseException e) {
			error = true;
		}
		if (error || file == null) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Sequencer", options);
		} else {
			mainSong(file);
		}
	}
}
