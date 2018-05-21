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

import jsequencer.ui.screen.DrumLoopLayer;
import midi.loop.LoopModel;
import midi.loop.beat.Beat;
import midi.loop.config.ChannelConfig;
import midi.pad.ui.Screen;
import midi.pad.ui.event.Runtime;

/**
 * @author oliver
 *
 */
public class Sequencer {
	public static void main(final String[] args) {
		final Screen screen = Runtime.getRuntime().getScreen();
		final ChannelConfig config = new ChannelConfig();
		final Runnable finishRunnable = new Runnable() {

			@Override
			public void run() {
				System.exit(0);
			}
		};
		final Beat beat = new Beat();
		beat.start();
		Runtime.getRuntime().schedule(new Runnable() {
			@Override
			public void run() {
				final DrumLoopLayer layer = new DrumLoopLayer(new LoopModel());
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
}
