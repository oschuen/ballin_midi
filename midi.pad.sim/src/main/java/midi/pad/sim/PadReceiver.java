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
 * @since 20.06.2019
 * @version 1.0
 * @author oliver
 */
package midi.pad.sim;

import java.awt.EventQueue;
import java.awt.Graphics;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author oliver
 *
 */
public class PadReceiver implements Receiver {
	private Screen updateScreen = new Screen();
	private Screen displayScreen = new Screen();
	private final Screen screens[] = { displayScreen, updateScreen };
	private int counter;
	private final Runnable updateRunnable;
	private final Receiver receiver;
	private static final Logger logger = LoggerFactory.getLogger(PadReceiver.class);

	public PadReceiver(final Receiver receiver, final Runnable updateRunnable) {
		super();
		this.updateRunnable = updateRunnable;
		this.receiver = receiver;
	}

	public void draw(final Graphics g, final int width, final int height) {
		displayScreen.draw(g, width, height);
	}

	public void press(final int x, final int y, final int width, final int height) {
		final int button = displayScreen.getButton(x, y, width, height);
		if (button < 128 && button >= 0) {
			final ShortMessage message = new ShortMessage();
			try {
				message.setMessage(ShortMessage.NOTE_ON, 0, button, 127);
			} catch (final InvalidMidiDataException e) {
				logger.error("ShortMessage wrong configured");
			}
			receiver.send(message, 0);
		} else if (button >= 128 && button < 136) {
			final ShortMessage message = new ShortMessage();
			try {
				message.setMessage(ShortMessage.CONTROL_CHANGE, 0x68 + button - 128, 127);
			} catch (final InvalidMidiDataException e) {
				logger.error("ShortMessage wrong configured");
			}
			receiver.send(message, 0);
		}
	}

	public void release(final int x, final int y, final int width, final int height) {
		final int button = displayScreen.getButton(x, y, width, height);
		if (button < 128 && button >= 0) {
			final ShortMessage message = new ShortMessage();
			try {
				message.setMessage(ShortMessage.NOTE_ON, 0, button, 0);
			} catch (final InvalidMidiDataException e) {
				logger.error("ShortMessage wrong configured");
			}
			receiver.send(message, 0);
		} else if (button >= 128 && button < 136) {
			final ShortMessage message = new ShortMessage();
			try {
				message.setMessage(ShortMessage.CONTROL_CHANGE, 0x68 + button - 128, 0);
			} catch (final InvalidMidiDataException e) {
				logger.error("ShortMessage wrong configured");
			}
			receiver.send(message, 0);
		}
	}

	private void flipScreens(final int order) {
		if ((order & 0x20) == 0x20) {
			if ((order & 0x01) == 0x01) {
				displayScreen = screens[1];
			} else {
				displayScreen = screens[0];
			}
			if ((order & 0x04) == 0x04) {
				updateScreen = screens[1];
			} else {
				updateScreen = screens[0];
			}
			if ((order & 0x10) == 0x10) {
				updateScreen.copy(displayScreen);
			}
		}
		EventQueue.invokeLater(updateRunnable);
	}

	private void setScreenColor(final int button, final int data) {
		final int r = data & 0x03;
		final int g = (data >> 4) & 0x03;
		final boolean clear = (data & 0x04) == 0x04;
		final boolean copy = (data & 0x02) == 0x02;
		updateScreen.setColor(button, r, g);
		final Screen otherScreen = screens[0] == updateScreen ? screens[1] : screens[0];
		if (copy) {
			otherScreen.setColor(button, r, g);
		} else if (clear) {
			otherScreen.setColor(button, 0, 0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Receiver#send(javax.sound.midi.MidiMessage, long)
	 */
	@Override
	public void send(final MidiMessage message, final long timeStamp) {
		if (message instanceof ShortMessage) {
			final ShortMessage shortMessage = (ShortMessage) message;
			if (shortMessage.getCommand() == ShortMessage.CONTROL_CHANGE) {
				if (shortMessage.getData1() == 0) {
					flipScreens(shortMessage.getData2());
				}
				counter = 0;
			}
			if (shortMessage.getCommand() == ShortMessage.NOTE_ON
					&& shortMessage.getChannel() == 2) {
				setScreenColor(counter++, ((ShortMessage) message).getData1());
				setScreenColor(counter++, ((ShortMessage) message).getData2());
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Receiver#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
