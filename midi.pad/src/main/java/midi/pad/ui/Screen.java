/**
 * Copyright (C) 2016 Oliver Schünemann
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, 
 * Boston, MA 02110, USA 
 * 
 * @since 11.12.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import midi.pad.ui.event.PadEvent;

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

	public void padEventOccured(final PadEvent event) {
		for (int i = layers.length - 1; i >= 0; --i) {
			if (layers[i] != null && layers[i].padEventOccured(event)) {
				break;
			}
		}
	}
}
