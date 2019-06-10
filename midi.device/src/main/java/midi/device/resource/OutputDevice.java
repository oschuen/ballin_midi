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
 * @since 09.06.2019
 * @version 1.0
 * @author oliver
 */
package midi.device.resource;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author oliver
 *
 */
public class OutputDevice {
	private static final Logger logger = LoggerFactory.getLogger(OutputDevice.class);
	private final Receiver nullReceiver = new NullReceiver();
	private Receiver currentReceiver = nullReceiver;
	private String deviceName;

	public OutputDevice() {
		super();
	}

	public OutputDevice(final String deviceName) {
		setDeviceName(deviceName);
	}

	public Receiver getOutput() {
		return new FassadeReceiver();
	}

	public void setDeviceName(final String deviceName) {
		if (deviceName == null) {
			currentReceiver = nullReceiver;
			currentReceiver = null;
		} else if (!deviceName.equals(this.deviceName)) {
			final MidiDevice receiverDevice = MidiDevices.secureGetReceiverDevice(deviceName);
			try {
				if (!receiverDevice.isOpen()) {
					receiverDevice.open();
				}
				currentReceiver = receiverDevice.getReceiver();
			} catch (final MidiUnavailableException e) {
				logger.error("Unable to get Device {}", deviceName);
			}
		}
	}

	private class FassadeReceiver implements Receiver {
		private boolean closed;

		/*
		 * {@inheritdoc}
		 */
		@Override
		public void send(final MidiMessage message, final long timeStamp) {
			if (!closed) {
				currentReceiver.send(message, timeStamp);
			}

		}

		/*
		 * {@inheritdoc}
		 */
		@Override
		public void close() {
			closed = true;
		}
	}

	public static class NullReceiver implements Receiver {

		@Override
		public void send(final MidiMessage message, final long timeStamp) {
		}

		@Override
		public void close() {
		}
	}
}
