/**
 * Copyright (C) 2018 Oliver Schünemann
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
 * @since 05.05.2018
 * @version 1.0
 * @author oliver
 */
package jsequencer.ui.screen;

import midi.loop.LoopModel;
import midi.loop.beat.Beat.BeatListener;
import midi.pad.ui.dialogs.HintDialog;
import midi.pad.ui.widgets.LoopConfig;

/**
 * @author oliver
 *
 */
public class DrumLoopLayer extends HintDialog implements BeatListener {

	private final LoopConfig config;

	public DrumLoopLayer(final LoopModel... models) {
		super("Drums");

		config = new LoopConfig(0, 0, new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

			}
		}, new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

			}
		}, models);
		setWidgets(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.loop.beat.Beat.BeatListener#accept(long)
	 */
	@Override
	public void accept(final long beat) {
		config.accept(beat);
	}

}
