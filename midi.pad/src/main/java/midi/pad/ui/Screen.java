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

import static midi.pad.ui.event.Runtime.getRuntime;

import java.util.Objects;
import java.util.Optional;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import midi.pad.ui.event.AbcButtonEvent;
import midi.pad.ui.event.Event;
import midi.pad.ui.event.NumButtonEvent;
import midi.pad.ui.event.PadEvent;
import midi.pad.ui.widgets.ControlButton;

/**
 * @author oliver
 *
 */
public class Screen {
	private static final Logger logger = LoggerFactory.getLogger(Screen.class);
	private Layer[] layers = new Layer[0];
	private final Graphic graphics = new Graphic();
	private Runnable[] finishRunnable = new Runnable[0];

	private boolean firstpage = true;
	private boolean blink = true;
	private final Color[] numberButton = new Color[8];
	private final char[] currentText = new char[80];

	/**
	 * 
	 */
	public Screen() {
		for (int i = 0; i < numberButton.length; i++) {
			numberButton[i] = Color.BLACK;
		}
	}

	private final int pages[][] = new int[2][80];

	public void draw(final Receiver receiver) {
		try {
			final int currentPage = firstpage ? 0 : 1;
			graphics.fill(Color.TRANSPARENT);
			final Optional<Layer> topLayer = getTopLayer();
			if (topLayer.isPresent()) {
				topLayer.get().paint(graphics);
			}
			for (int y = 0; y < 8; ++y) {
				for (int x = 0; x < 8; x++) {
					pages[currentPage][y * 8 + x] = getColor(x, y).getMidiValue();
				}
			}

			final byte blackMidiValue = Color.BLACK.getMidiValue();
			if (topLayer.isPresent()) {
				final Layer tLayer = topLayer.get();
				for (int i = 0; i < 8; i++) {
					pages[currentPage][64 + i] = tLayer.getAbcControlButton(i)
							.map(b -> getBlinkColor(b.getColor()).getMidiValue())
							.orElse(blackMidiValue);
				}
			} else {
				for (int i = 0; i < 8; i++) {
					pages[currentPage][64 + i] = blackMidiValue;
				}
			}
			final Optional<Layer> layer = getBottomLayer();
			for (int i = 0; i < 8; i++) {
				final int loop = i;
				final Color c1 = layer.map(l -> {
					return l.getNumControlButton(loop).orElse(null);
				}).map(ControlButton::getColor).orElse(Color.BLACK);
				pages[currentPage][72 + i] = getBlinkColor(c1).getMidiValue();
			}
			final int otherPage = (currentPage + 1) % 2;
			for (int y = 0; y < 8; ++y) {
				for (int x = 0; x < 8; ++x) {
					if (pages[currentPage][y * 8 + x] != pages[otherPage][y * 8 + x]) {
						receiver.send(new ShortMessage(ShortMessage.NOTE_ON, y * 16 + x,
								pages[currentPage][y * 8 + x] + 4), 0);
					}
				}
			}
			for (int y = 0; y < 8; ++y) {
				if (pages[currentPage][64 + y] != pages[otherPage][64 + y]) {
					receiver.send(new ShortMessage(ShortMessage.NOTE_ON, y * 16 + 8,
							pages[currentPage][64 + y] + 4), 0);
				}
			}
			for (int y = 0; y < 8; ++y) {
				if (pages[currentPage][72 + y] != pages[otherPage][72 + y]) {
					receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 104 + y,
							pages[currentPage][72 + y] + 4), 0);
				}
			}

			final ShortMessage toggleMsg = new ShortMessage();
			toggleMsg.setMessage(ShortMessage.CONTROL_CHANGE, 0, 0, 32 + (firstpage ? 1 : 4));
			receiver.send(toggleMsg, 0);
		} catch (final InvalidMidiDataException e) {
		}

		firstpage = !firstpage;
	}

	private void updateText(final Receiver receiver, final char[] text, final int pos,
			final int length) {
		for (int i = 0; i < length; ++i) {
			final char c = i < text.length ? text[i] : ' ';
			if (currentText[pos + i] != c) {
				currentText[pos + i] = c;
				try {
					final ShortMessage msg = new ShortMessage();
					msg.setMessage(ShortMessage.NOTE_ON, 4, pos + i, c);
					receiver.send(msg, 0);
				} catch (final InvalidMidiDataException e) {
				}
			}
		}
	}

	public void drawText(final Receiver receiver) {
		getTopLayer().ifPresent(it -> {
			updateText(receiver, it.getTitle().toCharArray(), 0, 20);
			updateText(receiver, it.getHint().toCharArray(), 20, 20);
			updateText(receiver, it.getExtraHint().toCharArray(), 40, 20);
			if (it.getExtraHint().length() > 20) {
				updateText(receiver, it.getExtraHint().substring(20).toCharArray(), 60, 20);
			} else {
				updateText(receiver, "".toCharArray(), 60, 20);
			}
			try {
				final ShortMessage msg = new ShortMessage();
				msg.setMessage(ShortMessage.NOTE_OFF, 4, 0, 0);
				receiver.send(msg, 0);
			} catch (final InvalidMidiDataException e) {
			}
		});
	}

	private Color getBlinkColor(final Color c) {
		return c.isFlashing() ? (blink ? c : Color.BLACK) : c;
	}

	private Color getColor(final int x, final int y) {
		final Color c = graphics.getPixel(x, y);
		if (c != null && c.isOpaque()) {
			return getBlinkColor(c);
		}
		return Color.TRANSPARENT;
	}

	public void setNumberOfLayers(final int numberOfLayers) {
		if (layers.length != numberOfLayers) {
			final Layer[] newlayers = new Layer[numberOfLayers];
			final Runnable[] newRunnables = new Runnable[numberOfLayers];
			for (int i = 0; i < newlayers.length && i < layers.length; ++i) {
				newlayers[i] = layers[i];
				newRunnables[i] = finishRunnable[i];
			}
			for (int i = newlayers.length; i < layers.length; ++i) {
				if (finishRunnable[i] != null) {
					getRuntime().schedule(finishRunnable[i]);
				}
			}
			layers = newlayers;
			finishRunnable = newRunnables;
		}
	}

	public void toggleFlash() {
		blink = !blink;
	}

	public boolean isTopLayer(final Layer layer) {
		return Objects.equals(layer, getTopLayer().orElse(null));
	}

	public boolean isBottomLayer(final Layer layer) {
		return Objects.equals(layer, getBottomLayer().orElse(null));
	}

	public void putLayer(final int level, final Layer layer) {
		if (level >= 0 && level < layers.length) {
			layers[level] = layer;
			finishRunnable[level] = null;
		}
	}

	public void putLayer(final int level, final Layer layer, final Runnable runnable) {
		if (level >= 0 && level < layers.length) {
			layers[level] = layer;
			finishRunnable[level] = runnable;
			getRuntime().invalidate();
		}

	}

	public void removeLayer(final int level) {
		if (level >= 0 && level < layers.length) {
			if (layers[level] != null) {
				layers[level].stopLayer();
			}
			layers[level] = null;
			if (finishRunnable[level] != null) {
				getRuntime().schedule(finishRunnable[level]);
				finishRunnable[level] = null;
			}
			getRuntime().invalidate();
		}
	}

	public void showBottomLayer() {
		for (int i = layers.length - 1; i >= 0; --i) {
			if (!(layers[i] == null || isBottomLayer(layers[i]))) {
				removeLayer(i);
			}
		}
	}

	public void removeLayer(final Layer layer) {
		for (int i = 0; i < layers.length; ++i) {
			if (layers[i] == layer) {
				removeLayer(i);
			}
		}
	}

	private Optional<Layer> getTopLayer() {
		for (int i = layers.length - 1; i >= 0; --i) {
			if (layers[i] != null) {
				return Optional.of(layers[i]);
			}
		}
		return Optional.empty();
	}

	private Optional<Layer> getBottomLayer() {
		for (int i = 0; i < layers.length; ++i) {
			if (layers[i] != null) {
				return Optional.of(layers[i]);
			}
		}
		return Optional.empty();
	}

	public void eventOccured(final Event event) {
		if (logger.isDebugEnabled()) {
			logger.debug("Event occured ({})", event);
		}
		if (PadEvent.isEventOfThisType(event)) {
			for (int i = layers.length - 1; i >= 0; --i) {
				if (layers[i] != null && layers[i].eventOccured(event)) {
					break;
				}
			}
		} else if (AbcButtonEvent.isEventOfThisType(event)) {
			final AbcButtonEvent abcEvent = AbcButtonEvent.getEvent(event);
			getTopLayer().ifPresent(l -> l.getAbcControlButton(abcEvent.getY())
					.ifPresent(b -> b.eventOccured(event)));
		} else if (NumButtonEvent.isEventOfThisType(event)) {
			final NumButtonEvent numEvent = NumButtonEvent.getEvent(event);
			getBottomLayer().ifPresent(l -> l.getNumControlButton(numEvent.getX())
					.ifPresent(b -> b.eventOccured(event)));
		}
	}
}
