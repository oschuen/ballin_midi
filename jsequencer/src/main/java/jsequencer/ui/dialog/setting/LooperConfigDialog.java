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
import jsequencer.ui.model.SongModel;
import midi.loop.config.OutputChannelConfig;
import midi.pad.ui.Color;
import midi.pad.ui.Screen;
import midi.pad.ui.dialogs.HintDialog;
import midi.pad.ui.dialogs.NumberDialog;
import midi.pad.ui.event.Runtime;
import midi.pad.ui.widgets.ChannelSelection;
import midi.pad.ui.widgets.MidiDeviceSelection;
import midi.pad.ui.widgets.SinglePixelButton;

/**
 * @author oliver
 *
 */
public class LooperConfigDialog extends HintDialog {
	private final static Logger logger = LoggerFactory.getLogger(LooperConfigDialog.class);

	private enum MODE {

		BANK("Bank", 16383), PROGRAM("Program", 127), REVERB("Reverb", 127), CHOIR("Choir",
				127), VOLUME("Volume", 127);

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

	private final OutputChannelConfig config;
	private final ChannelSelection channelSelection;
	private final MidiDeviceSelection midiSelection;
	private final SongModel songModel;

	/**
	 * @param hint
	 */
	public LooperConfigDialog(final String hint, final SongModel songModel,
			final OutputChannelConfig config) {
		super(hint);
		this.config = config;
		this.songModel = songModel;
		channelSelection = new ChannelSelection(0, 5, config.getChannel(), new GetUsedProvider(),
				new SetChannelRunnable());
		midiSelection = new MidiDeviceSelection(0, 3, config.getMidiOut(),
				new SetMidiDeviceRunnable());
		setWidgets(channelSelection, midiSelection, getButton(0, 1, MODE.BANK),
				getButton(1, 1, Color.RED, MODE.PROGRAM), getButton(3, 1, MODE.VOLUME),
				getButton(4, 1, MODE.REVERB), getButton(5, 1, MODE.CHOIR));
		start();
	}

	private SinglePixelButton getButton(final int x, final int y, final MODE mode) {
		return new SinglePixelButton(x, y, Color.FULL_GREEN, new ConfigureRunnable(mode));
	}

	private SinglePixelButton getButton(final int x, final int y, final Color color,
			final MODE mode) {
		return new SinglePixelButton(x, y, color, new ConfigureRunnable(mode));
	}

	private int getValue(final OutputChannelConfig config, final MODE mode) {
		switch (mode) {
		case BANK:
			return config.getBank();
		case CHOIR:
			return config.getChoir();
		case PROGRAM:
			return config.getProgram();
		case REVERB:
			return config.getReverb();
		case VOLUME:
			return config.getVolume();
		default:
			logger.error("Unkown mode: " + mode.name());
			return 0;
		}
	}

	private void setValue(final OutputChannelConfig config, final MODE mode, final int value) {
		switch (mode) {
		case BANK:
			config.setBank(Math.min(mode.getMax(), value));
			break;
		case CHOIR:
			config.setChoir(Math.min(mode.getMax(), value));
			break;
		case PROGRAM:
			config.setProgram(Math.min(mode.getMax(), value));
			break;
		case REVERB:
			config.setReverb(Math.min(mode.getMax(), value));
			break;
		case VOLUME:
			config.setVolume(Math.min(mode.getMax(), value));
			break;
		default:
			logger.error("Unkown mode: " + mode.name());
		}
	}

	private final class GetUsedProvider implements ChannelSelection.UsedProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see midi.pad.ui.widgets.ChannelSelection.UsedProvider#getUsed()
		 */
		@Override
		public boolean[] getUsed() {
			return songModel.getUsedOutChannels(config.getMidiOut());
		}
	}

	private final class SetChannelRunnable implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			config.setChannel(channelSelection.getCurrentChannel());
			extraHint("Channel " + channelSelection.getCurrentChannel());
			Runtime.getRuntime().invalidate();
		}
	}

	private final class SetMidiDeviceRunnable implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			config.setMidiOut(midiSelection.getCurrentMidi());
			extraHint("Device " + midiSelection.getCurrentMidi());
			Runtime.getRuntime().invalidate();
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
