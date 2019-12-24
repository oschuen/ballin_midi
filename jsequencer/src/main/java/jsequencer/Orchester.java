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
 * @since 01.11.2019
 * @version 1.0
 * @author oliver
 */
package jsequencer;

import jsequencer.ui.model.SongModel;
import midi.chord.ChordRecognizer;
import midi.instrument.Guitar;
import midi.instrument.Percussion;
import midi.instrument.Sequencer;
import midi.loop.beat.Beat;
import midi.loop.config.InputChannelConfig;
import midi.loop.config.OutputChannelConfig;
import midi.pad.ui.event.Runtime;

/**
 * @author oliver
 *
 */
public class Orchester {
	private final SongModel model;
	private final Percussion percussion;
	private final Guitar guitar;
	private final ChordRecognizer recognizer;
	private final OutputChannelConfig percussionChannelConfig;
	private final OutputChannelConfig guitarChannelConfig;
	private final InputChannelConfig guitarInputConfig;
	private final Sequencer[] sequencer;

	public Orchester(final SongModel model, final Beat beat) {
		super();
		this.model = model;
		percussionChannelConfig = model.getPercussionChannelConfig();
		guitarChannelConfig = model.getGuitarChannelConfig();
		guitarInputConfig = model.getGuitarInputConfig();

		// Create instruments
		percussion = new Percussion(Runtime.getRuntime().getOutput(percussionChannelConfig),
				percussionChannelConfig);
		guitar = new Guitar(Runtime.getRuntime().getOutput(guitarChannelConfig),
				guitarChannelConfig);
		recognizer = new ChordRecognizer();
		recognizer.setListener(model.getGuitarModel(0));
		recognizer.setSplit(false);
		recognizer.setMidiChannel(guitarInputConfig.getChannel());

		sequencer = new Sequencer[model.getNumberOfSequencer()];
		for (int i = 0; i < sequencer.length; i++) {
			sequencer[i] = new Sequencer(
					Runtime.getRuntime().getOutput(model.getSequencerChannelConfig(i)),
					model.getSequencerChannelConfig(i), model.getSequencerInputChannelConfig(i));
			final Sequencer seq = sequencer[i];
			seq.setModel(model.getSequencerModel(i, 0));
			beat.addBeatListener(seq);
		}

		// Apply Models to instruments
		percussion.setModel(model.getPercussionModel(0));
		guitar.setModel(model.getGuitarModel(0));
		beat.addBeatListener(percussion);
		beat.addBeatListener(guitar);
	}

	public void setLayer(final int layer) {
		percussion.setModel(model.getPercussionModel(layer));
		guitar.setModel(model.getGuitarModel(layer));
		recognizer.setListener(model.getGuitarModel(layer));
		for (int i = 0; i < sequencer.length; i++) {
			final Sequencer seq = sequencer[i];
			seq.setModel(model.getSequencerModel(i, layer));
		}
	}

	/**
	 * @return the percussion
	 */
	public Percussion getPercussion() {
		return percussion;
	}

	/**
	 * @return the guitar
	 */
	public Guitar getGuitar() {
		return guitar;
	}

	public Sequencer getSequencer(final int seq) {
		return sequencer[seq];
	}

	/**
	 * @return the percussionChannelConfig
	 */
	public OutputChannelConfig getPercussionChannelConfig() {
		return percussionChannelConfig;
	}

	/**
	 * @return the guitarChannelConfig
	 */
	public OutputChannelConfig getGuitarChannelConfig() {
		return guitarChannelConfig;
	}

	public OutputChannelConfig getSequencerChannelConfig(final int sequencer) {
		return model.getSequencerChannelConfig(sequencer);
	}

	/**
	 * @return the guitarInputConfig
	 */
	public InputChannelConfig getGuitarInputConfig() {
		return guitarInputConfig;
	}

	public InputChannelConfig getSequencerInputConfig(final int sequencer) {
		return model.getSequencerInputChannelConfig(sequencer);
	}

	private static boolean isUsed(final boolean used[][], final OutputChannelConfig config) {
		return used[config.getMidiOut()][config.getChannel()];
	}

	private static void setUsed(final boolean used[][], final OutputChannelConfig config) {
		used[config.getMidiOut()][config.getChannel()] = true;
	}

	public void applyConfigs() {
		final boolean used[][] = new boolean[4][16];
		if (percussionChannelConfig.isChanged() && !isUsed(used, percussionChannelConfig)) {
			Runtime.getRuntime().applyChannelConfig(percussionChannelConfig);
			percussionChannelConfig.applied();
		}
		setUsed(used, percussionChannelConfig);
		if (guitarChannelConfig.isChanged() && !isUsed(used, guitarChannelConfig)) {
			Runtime.getRuntime().applyChannelConfig(guitarChannelConfig);
			guitarChannelConfig.applied();
		}
		setUsed(used, guitarChannelConfig);
		if (guitarInputConfig.isChanged()) {
			Runtime.getRuntime().removeInput(recognizer);
			switch (guitarInputConfig.getMode()) {
			case BELOW:
				Runtime.getRuntime().addBelowSplitInput(recognizer, guitarInputConfig.getMidiIn());
				break;
			case ABOVE:
				Runtime.getRuntime().addAboveSplitInput(recognizer, guitarInputConfig.getMidiIn());
				break;
			case ALL:
				Runtime.getRuntime().addInput(recognizer, guitarInputConfig.getMidiIn());
				break;
			case OFF:
			default:
				break;
			}
			recognizer.setMidiChannel(guitarInputConfig.getChannel());
			guitarInputConfig.applied();
		}

		for (int i = 0; i < sequencer.length; i++) {
			final Sequencer seq = sequencer[i];
			final OutputChannelConfig config = model.getSequencerChannelConfig(i);
			if (config.isChanged() && !isUsed(used, config)) {
				Runtime.getRuntime().applyChannelConfig(config);
				config.applied();
			}
			setUsed(used, config);
			final InputChannelConfig inConfig = model.getSequencerInputChannelConfig(i);
			if (inConfig.isChanged()) {
				Runtime.getRuntime().removeInput(seq);
				switch (inConfig.getMode()) {
				case BELOW:
					Runtime.getRuntime().addBelowSplitInput(seq, inConfig.getMidiIn());
					break;
				case ABOVE:
					Runtime.getRuntime().addAboveSplitInput(seq, inConfig.getMidiIn());
					break;
				case ALL:
					Runtime.getRuntime().addInput(seq, inConfig.getMidiIn());
					break;
				case OFF:
				default:
					break;
				}
				seq.setMidiInChannel(inConfig.getChannel());
				inConfig.applied();
			}
		}
	}
}
