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

import java.util.Optional;

import jsequencer.ui.model.SongModel;
import midi.loop.beat.Beat;
import midi.loop.beat.Beat.BarListener;
import midi.pad.ui.Color;
import midi.pad.ui.Screen;
import midi.pad.ui.dialogs.HintDialog;
import midi.pad.ui.event.Runtime;
import midi.pad.ui.widgets.ControlButton;
import midi.pad.ui.widgets.TrackConfig;

/**
 * @author oliver
 *
 */
public class SongLayer extends HintDialog implements BarListener {

	private final TrackConfig[] config = new TrackConfig[8];
	private final SongModel model;
	private final Beat beat;
	private int currentLayer = 0;
	private int nextLayer = 0;

	/**
	 * @param hint
	 */
	public SongLayer(final SongModel model, final Beat beat) {
		super("J 1.0");
		this.model = model;
		this.beat = beat;
		config[0] = new TrackConfig(0, () -> {
			final DrumLoopLayer layer = new DrumLoopLayer(
					this.model.getPercussionModel(currentLayer));
			final Screen screen = Runtime.getRuntime().getScreen();
			this.beat.addBeatListener(layer);
			screen.putLayer(3, layer, () -> {
				beat.removeBeatListener(layer);
				screen.removeLayer(layer);
			});
			layer.start();
		}, () -> {
			getRuntime().invalidate();
		}, () -> {
			getRuntime().invalidate();
		}, () -> {
			getRuntime().invalidate();
		}, model.getLayerModel(0));

		for (int i = 1; i < config.length; i++) {
			config[i] = new TrackConfig(i, () -> {
				getRuntime().invalidate();
			}, () -> {
				getRuntime().invalidate();
			}, () -> {
				getRuntime().invalidate();
			}, () -> {
				getRuntime().invalidate();
			}, model.getLayerModel(i));
		}
		setWidgets(config);
		for (int i = 1; i < 8; i++) {
			config[i].setIn(true);
		}
		start();
	}

	@Override
	public Optional<ControlButton> getNumControlButton(final int x) {
		final Screen screen = Runtime.getRuntime().getScreen();
		if (screen.isTopLayer(this)) {
			if (x >= 4) {
				final Color color;
				if (x - 4 == nextLayer) {
					color = new Color(Color.LOW_GREEN, true, false);
				} else if (x - 4 == currentLayer) {
					color = Color.GREEN;
				} else {
					color = Color.BLACK;
				}
				return Optional.of(new ControlButton(color, () -> {
					nextLayer = x - 4;
				}));
			}
		} else {
			if (x == 0) {
				return Optional.of(new ControlButton(Color.GREEN, () -> {
					screen.showBottomLayer();
					getRuntime().invalidate();
				}));
			}
		}
		return super.getNumControlButton(x);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.loop.beat.Beat.BarListener#getNumberOfQuarterPerBar()
	 */
	@Override
	public long getNumberOfQuarterPerBar() {
		return model.getPercussionModel(currentLayer).getQuarterPerPage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see midi.loop.beat.Beat.BarListener#accept(long)
	 */
	@Override
	public void accept(final long bar) {
		if (nextLayer >= 0) {
			currentLayer = nextLayer;
		}
		nextLayer = -1;
	}
}
