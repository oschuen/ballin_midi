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
import midi.loop.beat.Beat;
import midi.loop.config.ChannelConfig;
import midi.loop.config.InputChannelConfig;
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
	private final ChannelConfig percussionChannelConfig;
	private final ChannelConfig guitarChannelConfig;
	private final InputChannelConfig guitarInputConfig;

	public Orchester(final SongModel model, final Beat beat) {
		super();
		this.model = model;
		percussionChannelConfig = model.getPercussionChannelConfig();
		guitarChannelConfig = model.getGuitarChannelConfig();
		guitarInputConfig = model.getGuitarInputConfig();

		// Configure Runtime
		Runtime.getRuntime().applyChannelConfig(guitarChannelConfig);
		Runtime.getRuntime().applyChannelConfig(percussionChannelConfig);

		// Create instruments
		percussion = new Percussion(Runtime.getRuntime().getOutput(percussionChannelConfig),
				percussionChannelConfig);
		guitar = new Guitar(Runtime.getRuntime().getOutput(guitarChannelConfig),
				guitarChannelConfig);
		recognizer = new ChordRecognizer();
		recognizer.setListener(model.getGuitarModel(0));
		recognizer.setSplit(false);
		recognizer.setMidiChannel(guitarInputConfig.getChannel());
		Runtime.getRuntime().addBelowSplitInput(recognizer, guitarInputConfig.getMidiIn());

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

	/**
	 * @return the percussionChannelConfig
	 */
	public ChannelConfig getPercussionChannelConfig() {
		return percussionChannelConfig;
	}

	/**
	 * @return the guitarChannelConfig
	 */
	public ChannelConfig getGuitarChannelConfig() {
		return guitarChannelConfig;
	}

	public void applyConfigs() {
		if (percussionChannelConfig.isChanged()) {
			Runtime.getRuntime().applyChannelConfig(percussionChannelConfig);
			percussionChannelConfig.applied();
		}
		if (guitarChannelConfig.isChanged()) {
			Runtime.getRuntime().applyChannelConfig(guitarChannelConfig);
			guitarChannelConfig.applied();
		}
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
	}

	/**
	 * @return the guitarInputConfig
	 */
	public InputChannelConfig getGuitarInputConfig() {
		return guitarInputConfig;
	}
}
