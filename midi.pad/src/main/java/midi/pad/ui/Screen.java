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
 * @since 11.12.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import midi.pad.ui.event.Event;

/**
 * @author oliver
 *
 */
public class Screen {
	private Layer[] layers = new Layer[0];
	private Graphic[] graphics = new Graphic[0];
	private boolean firstpage = true;
	private boolean blink = true;

	public void draw(final Receiver receiver) {
		try {
			for (int i = layers.length - 1; i >= 0; --i) {
				if (!(layers[i] == null || graphics[i] == null)) {
					graphics[i].fill(Color.TRANSPARENT);
					layers[i].paint(graphics[i]);
				}
			}
			for (int y = 0; y < 8; ++y) {
				for (int x = 0; x < 4; x++) {
					final int vel1 = getColor(x * 2, y).getMidiValue();
					final int vel2 = getColor(x * 2 + 1, y).getMidiValue();
					final ShortMessage msg = new ShortMessage();
					msg.setMessage(0x92, 2, vel1, vel2);
					receiver.send(msg, 0);
				}
			}

			final ShortMessage toggleMsg = new ShortMessage();

			toggleMsg.setMessage(ShortMessage.CONTROL_CHANGE, 0, 0, 32 + (firstpage ? 1 : 4));
			receiver.send(toggleMsg, 0);
		} catch (final InvalidMidiDataException e) {
		}

		firstpage = !firstpage;
	}

	private Color getColor(final int x, final int y) {

		for (int i = graphics.length - 1; i >= 0; --i) {
			if (graphics[i] != null) {
				final Color c = graphics[i].getPixel(x, y);
				if (c != null && c.isOpaque()) {
					return c.isFlashing() ? (blink ? c : Color.BLACK) : c;
				}
			}
		}
		return Color.TRANSPARENT;
	}

	public void setNumberOfLayers(final int numberOfLayers) {
		if (layers.length != numberOfLayers) {
			final Layer[] newlayers = new Layer[numberOfLayers];
			final Graphic[] newGraphics = new Graphic[numberOfLayers];
			for (int i = 0; i < newlayers.length && i < layers.length; ++i) {
				newlayers[i] = layers[i];
				newGraphics[i] = graphics[i];
			}
			layers = newlayers;
			graphics = newGraphics;
		}
	}

	public void toggleFlash() {
		blink = !blink;
	}

	public void putLayer(final int level, final Layer layer) {
		if (level >= 0 && level < layers.length) {
			layers[level] = layer;
			graphics[level] = new Graphic();
		}
	}

	public void removeLayer(final int level) {
		if (level >= 0 && level < layers.length) {
			layers[level] = null;
			graphics[level] = null;
		}
	}

	public void eventOccured(final Event event) {
		for (int i = layers.length - 1; i >= 0; --i) {
			if (layers[i] != null && layers[i].eventOccured(event)) {
				break;
			}
		}
	}
}
