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

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jsequencer.ui.screen.DrumLoopLayer;
import jsequencer.ui.screen.GuitarLoopLayer;
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
		final ChannelConfig config = new ChannelConfig();
		final Percussion percussion;
		final PercussionModel model = new PercussionModel();
		final Transmitter transmitter;

		final MidiDevice percussionDevice = MidiDevices
				.secureGetReceiverDevice("VirMIDI [hw:4,0,2]");

		if (percussionDevice == null) {
			logger.error("Percussion Device not found");
			return;
		} else {
			if (!percussionDevice.isOpen()) {
				percussionDevice.open();
			}
			percussion = new Percussion(percussionDevice.getReceiver(), 9);
		}

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
	private static void mainGuitar(final String[] args) throws MidiUnavailableException {
		final Screen screen = Runtime.getRuntime().getScreen();
		final Guitar guitar;
		final GuitarModel model = new GuitarModel();
		final ChordRecognizer recognizer = new ChordRecognizer(model);
		final int midiChannel = 5;
		final int midiDevice = 0;

		guitar = new Guitar(Runtime.getRuntime().getOutput(midiDevice), midiChannel);
		Runtime.getRuntime().addInput(recognizer, midiDevice);
		final ChannelConfig config = new ChannelConfig();
		config.setBank(128);
		config.setProgram(24);
		config.setChannel(midiChannel);
		config.setChoir(0);
		config.setReverb(0);
		config.setMidiOut(0);
		Runtime.getRuntime().applyChannelConfig(config);

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
				screen.putLayer(3,
						// new ConfirmDialog("Zufrieden ?", finishRunnable,
						// finishRunnable));
						// new NumberDialog("Program", 127, 0, finishRunnable));
						// new LooperConfigDialog("Track 1", config));
						layer);

			}
		});

	}

	public static void main(final String[] args) throws MidiUnavailableException {
		mainGuitar(args);
	}

}
