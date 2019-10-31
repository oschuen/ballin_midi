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

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jsequencer.ui.model.SongModel;
import jsequencer.ui.screen.DrumLoopLayer;
import jsequencer.ui.screen.GuitarLoopLayer;
import jsequencer.ui.screen.SongLayer;
import midi.chord.ChordRecognizer;
import midi.device.resource.MidiDevices;
import midi.instrument.Guitar;
import midi.instrument.Percussion;
import midi.instrument.model.GuitarModel;
import midi.instrument.model.PercussionModel;
import midi.loop.beat.Beat;
import midi.loop.beat.Beat.BeatListener;
import midi.loop.config.ChannelConfig;
import midi.pad.ui.Screen;
import midi.pad.ui.event.KeyBoardReceiver;
import midi.pad.ui.event.Runtime;

/**
 * @author oliver
 *
 */
public class Sequencer {
	private static final Logger logger = LoggerFactory.getLogger(Sequencer.class);

	public static void mainPercussion(final String[] args) throws MidiUnavailableException {
		final Screen screen = Runtime.getRuntime().getScreen();
		final Percussion percussion;
		final PercussionModel model = new PercussionModel();
		final Transmitter transmitter;
		final int midiChannel = 4;

		final ChannelConfig config = new ChannelConfig();
		config.setBank(0);
		config.setProgram(24);
		config.setChannel(midiChannel);
		config.setChoir(0);
		config.setReverb(127);
		config.setMidiOut(0);
		Runtime.getRuntime().applyChannelConfig(config);

		percussion = new Percussion(Runtime.getRuntime().getOutput(config), config);

		final MidiDevice transmitterDevice = MidiDevices
				.secureGetTransmitterDevice("VirMIDI [hw:4,2,0]");
		if (!transmitterDevice.isOpen()) {
			transmitterDevice.open();
		}
		transmitter = transmitterDevice.getTransmitter();
		transmitter.setReceiver(new KeyBoardReceiver());

		final Runnable finishRunnable = new Runnable() {

			@Override
			public void run() {
				System.exit(0);
			}
		};
		final Beat beat = new Beat();
		beat.start();
		percussion.setModel(model);
		beat.addBeatListener(new BeatListener() {

			@Override
			public void accept(final long beat) {
				percussion.accept(beat);
			}
		});
		Runtime.getRuntime().schedule(new Runnable() {
			@Override
			public void run() {
				final DrumLoopLayer layer = new DrumLoopLayer(model);
				beat.addBeatListener(layer);
				screen.putLayer(3,
						// new ConfirmDialog("Zufrieden ?", finishRunnable,
						// finishRunnable));
						// new NumberDialog("Program", 127, 0, finishRunnable));
						// new LooperConfigDialog("Track 1", config));
						layer);

			}
		});

	}

	/**
	 * @param args
	 */
	private static void mainGuitar(final File file) throws MidiUnavailableException {
		final Screen screen = Runtime.getRuntime().getScreen();
		final Properties props = new Properties();
		try {
			try (InputStream stream = new FileInputStream(file)) {
				props.load(stream);
			}
		} catch (final IOException e) {
		}
		Runtime.setRuntimeConfig(props);
		final Guitar guitar;
		final GuitarModel model = new GuitarModel();
		final ChordRecognizer recognizer = new ChordRecognizer(model);
		final int midiChannel = 4;
		final int midiDevice = 0;

		Runtime.getRuntime().addInput(recognizer, midiDevice);
		final ChannelConfig config = new ChannelConfig();
		config.setBank(128);
		config.setProgram(9);
		config.setChannel(midiChannel);
		config.setChoir(0);
		config.setReverb(127);
		config.setMidiOut(0);
		Runtime.getRuntime().applyChannelConfig(config);
		guitar = new Guitar(Runtime.getRuntime().getOutput(config), config);

		final Beat beat = new Beat();
		beat.start();
		guitar.setModel(model);
		beat.addBeatListener(new BeatListener() {

			@Override
			public void accept(final long beat) {
				guitar.accept(beat);
			}
		});
		Runtime.getRuntime().schedule(new Runnable() {
			@Override
			public void run() {
				final GuitarLoopLayer layer = new GuitarLoopLayer(model);
				beat.addBeatListener(layer);
				// 3, new ConfirmDialog("Zufrieden ?", () -> {}, () -> {}));
				// new NumberDialog("Program", 127, 0, finishRunnable));
				// new LooperConfigDialog("Track 1", config));
				screen.putLayer(3, layer);

			}
		});
		Runtime.getRuntime().addInput(new KeyBoardReceiver(), midiDevice);
	}

	private static void mainSong(final File file) throws MidiUnavailableException {
		final Screen screen = Runtime.getRuntime().getScreen();
		final SongModel model = new SongModel(8, 4);
		final Properties props = new Properties();
		try {
			try (InputStream stream = new FileInputStream(file)) {
				props.load(stream);
			}
		} catch (final IOException e) {
		}
		Runtime.setRuntimeConfig(props);

		final ChannelConfig percussionChannelConfig = model.getPercussionChannelConfig();
		Runtime.getRuntime().applyChannelConfig(model.getGuitarChannelConfig());

		Runtime.getRuntime().applyChannelConfig(percussionChannelConfig);
		final Percussion percussion = new Percussion(
				Runtime.getRuntime().getOutput(percussionChannelConfig), percussionChannelConfig);
		final Beat beat = new Beat();
		beat.start();
		percussion.setModel(model.getPercussionModel(0));
		beat.addBeatListener(new BeatListener() {

			@Override
			public void accept(final long beat) {
				percussion.accept(beat);
			}
		});

		Runtime.getRuntime().schedule(() -> {
			final SongLayer layer = new SongLayer(model, beat);
			screen.putLayer(1, layer);
			beat.addBarListener(layer);
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
			// mainGuitar(file);
		}
	}
}
