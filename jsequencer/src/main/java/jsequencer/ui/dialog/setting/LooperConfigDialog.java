/**
 * Copyright (C) 2017 Oliver Schünemann
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
 * @since 15.01.2017
 * @version 1.0
 * @author oliver
 */
package jsequencer.ui.dialog.setting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmidi.gui.model.IntegerModel;
import midi.loop.config.ChannelConfig;
import midi.pad.ui.Color;
import midi.pad.ui.Screen;
import midi.pad.ui.dialogs.HintDialog;
import midi.pad.ui.dialogs.NumberDialog;
import midi.pad.ui.event.Runtime;
import midi.pad.ui.widgets.SinglePixelButton;

/**
 * @author oliver
 *
 */
public class LooperConfigDialog extends HintDialog {
	private final static Logger logger = LoggerFactory.getLogger(LooperConfigDialog.class);

	private enum MODE {

		BANK("Bank", 255), PROGRAM("Program", 127), MIDIOUT("Midi Out", 3), CHANNEL("Channel",
				15), REVERB("Reverb", 120), CHOIR("Choir", 100);

		private MODE(final String hint, final int max) {
			this.hint = hint;
			this.max = max;
		}

		private final String hint;
		private final int max;

		/**
		 * return the hint;
		 */
		public String getHint() {
			return hint;
		}

		/**
		 * @return the max
		 */
		public int getMax() {
			return max;
		}
	}

	private final ChannelConfig config;

	/**
	 * @param hint
	 */
	public LooperConfigDialog(final String hint, final ChannelConfig config) {
		super(hint);
		this.config = config;
		setWidgets(getButton(0, 2, MODE.MIDIOUT), getButton(1, 2, Color.FULL_AMBER, MODE.CHANNEL),
				getButton(2, 2, MODE.BANK), getButton(3, 2, Color.RED, MODE.PROGRAM),
				getButton(4, 2, MODE.REVERB), getButton(5, 2, MODE.CHOIR));
		start();
	}

	private SinglePixelButton getButton(final int x, final int y, final MODE mode) {
		return new SinglePixelButton(x, y, Color.FULL_GREEN, new ConfigureRunnable(mode));
	}

	private SinglePixelButton getButton(final int x, final int y, final Color color,
			final MODE mode) {
		return new SinglePixelButton(x, y, color, new ConfigureRunnable(mode));
	}

	private int getValue(final ChannelConfig config, final MODE mode) {
		switch (mode) {
		case BANK:
			return config.getBank();
		case CHANNEL:
			return config.getChannel();
		case CHOIR:
			return config.getChoir();
		case MIDIOUT:
			return config.getMidiOut();
		case PROGRAM:
			return config.getProgram();
		case REVERB:
			return config.getReverb();
		default:
			logger.error("Unkown mode: " + mode.name());
			return 0;
		}
	}

	private void setValue(final ChannelConfig config, final MODE mode, final int value) {
		switch (mode) {
		case BANK:
			config.setBank(Math.max(mode.getMax(), value));
			break;
		case CHANNEL:
			config.setChannel(Math.max(mode.getMax(), value));
			break;
		case CHOIR:
			config.setChoir(Math.max(mode.getMax(), value));
			break;
		case MIDIOUT:
			config.setMidiOut(Math.max(mode.getMax(), value));
			break;
		case PROGRAM:
			config.setProgram(Math.max(mode.getMax(), value));
			break;
		case REVERB:
			config.setReverb(Math.max(mode.getMax(), value));
			break;
		default:
			logger.error("Unkown mode: " + mode.name());
		}
	}

	private final class ConfigureRunnable implements Runnable {
		private final MODE mode;

		public ConfigureRunnable(final MODE mode) {
			super();
			this.mode = mode;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			final Screen screen = Runtime.getRuntime().getScreen();
			final IntegerModel model = new IntegerModel(0, mode.getMax(), getValue(config, mode));
			screen.putLayer(4, new NumberDialog(mode.getHint(), model, () -> {
				setValue(config, mode, model.getValue());
				screen.removeLayer(4);
			}));
		}
	}
}
