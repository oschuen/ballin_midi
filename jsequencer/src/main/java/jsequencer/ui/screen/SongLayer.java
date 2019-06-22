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
 * @since 22.06.2019
 * @version 1.0
 * @author oliver
 */
package jsequencer.ui.screen;

import static midi.pad.ui.event.Runtime.getRuntime;

import midi.pad.ui.dialogs.HintDialog;
import midi.pad.ui.widgets.TrackConfig;

/**
 * @author oliver
 *
 */
public class SongLayer extends HintDialog {

	private final TrackConfig[] config = new TrackConfig[8];

	/**
	 * @param hint
	 */
	public SongLayer() {
		super("J 1.0");
		for (int i = 0; i < config.length; i++) {
			config[i] = new TrackConfig(i, () -> {
				getRuntime().invalidate();
			}, () -> {
				getRuntime().invalidate();
			}, () -> {
				getRuntime().invalidate();
			}, () -> {
				getRuntime().invalidate();
			});
		}
		setWidgets(config);
		for (int i = 1; i < 8; i++) {
			config[i].setIn(true);
		}
		start();
	}

}
