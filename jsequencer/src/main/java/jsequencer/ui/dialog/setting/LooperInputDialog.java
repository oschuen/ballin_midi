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

import midi.loop.config.InputChannelConfig;
import midi.pad.ui.dialogs.HintDialog;
import midi.pad.ui.event.Runtime;
import midi.pad.ui.widgets.ChannelSelection;
import midi.pad.ui.widgets.MidiDeviceSelection;

/**
 * @author oliver
 *
 */
public class LooperInputDialog extends HintDialog {
	private final ChannelSelection channelSelection;
	private final MidiDeviceSelection midiSelection;

	private final InputChannelConfig config;

	/**
	 * @param hint
	 */
	public LooperInputDialog(final String hint, final InputChannelConfig config) {
		super(hint);
		this.config = config;
		midiSelection = new MidiDeviceSelection(0, 1, config.getMidiIn(),
				new SetMidiDeviceRunnable());
		channelSelection = new ChannelSelection(0, 3, config.getChannel(), new GetUsedProvider(),
				new SetChannelRunnable());

		setWidgets(channelSelection, midiSelection);
		start();
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
			config.setMidiIn(midiSelection.getCurrentMidi());
			extraHint("Device " + midiSelection.getCurrentMidi());
			Runtime.getRuntime().invalidate();
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
			return new boolean[16];
		}
	}

}
