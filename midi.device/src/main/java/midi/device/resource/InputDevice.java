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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author oliver
 *
 */
public class InputDevice {
	public static final int splitTone = 54;
	private static final Logger logger = LoggerFactory.getLogger(OutputDevice.class);
	private String deviceName;
	private Transmitter transmitter = null;
	private final ReceiverDispatcher receiver = new ReceiverDispatcher();

	private final ReceiverDispatcher aboveSplitReceiver = new ReceiverDispatcher((m) -> {
		if (m instanceof ShortMessage) {
			final ShortMessage shortMessage = (ShortMessage) m;
			if (shortMessage.getCommand() == ShortMessage.NOTE_ON
					|| shortMessage.getCommand() == ShortMessage.NOTE_OFF) {
				final int onKey = shortMessage.getData1();
				return onKey > splitTone;
			}
		}
		return false;
	});
	private final ReceiverDispatcher belowSplitReceiver = new ReceiverDispatcher((m) -> {
		if (m instanceof ShortMessage) {
			final ShortMessage shortMessage = (ShortMessage) m;
			if (shortMessage.getCommand() == ShortMessage.NOTE_ON
					|| shortMessage.getCommand() == ShortMessage.NOTE_OFF) {
				final int onKey = shortMessage.getData1();
				return onKey <= splitTone;
			}
		}
		return false;
	});

	public InputDevice() {
		super();
		receiver.addReceiver(aboveSplitReceiver);
		receiver.addReceiver(belowSplitReceiver);
	}

	public InputDevice(final String deviceName) {
		this();
		setDeviceName(deviceName);
	}

	private void closeTransmitter() {
		if (transmitter != null) {
			transmitter.setReceiver(null);
			transmitter.close();
			transmitter = null;
		}
	}

	public void addInput(final Receiver receiver) {
		this.receiver.addReceiver(receiver);
	}

	public void removeInput(final Receiver receiver) {
		this.receiver.removeReceiver(receiver);
		aboveSplitReceiver.removeReceiver(receiver);
		belowSplitReceiver.removeReceiver(receiver);
	}

	public void addBelowSplitInput(final Receiver receiver) {
		belowSplitReceiver.addReceiver(receiver);
	}

	public void addAboveSplitInput(final Receiver receiver) {
		aboveSplitReceiver.addReceiver(receiver);
	}

	public void setDeviceName(final String deviceName) {
		try {
			if (deviceName == null && this.deviceName == null) {
				return;
			} else if (deviceName == null) {
				closeTransmitter();
			} else if (!deviceName.equals(this.deviceName)) {
				closeTransmitter();
				final MidiDevice transmitterDevice = MidiDevices
						.getTransmitterDeviceRegex(deviceName);
				if (transmitterDevice != null) {
					if (!transmitterDevice.isOpen()) {
						transmitterDevice.open();
					}
					transmitter = transmitterDevice.getTransmitter();
					transmitter.setReceiver(receiver);
				}
			}
		} catch (final MidiUnavailableException e) {
			logger.error("Couldn't set device {}", deviceName);
		}
	}

	public static class ReceiverDispatcher implements Receiver {
		private final List<Receiver> receivers = new CopyOnWriteArrayList<>();
		private final Predicate<MidiMessage> filter;

		public ReceiverDispatcher() {
			super();
			filter = (t) -> {
				return true;
			};
		}

		public ReceiverDispatcher(final Predicate<MidiMessage> filter) {
			this.filter = filter;
		}

		public void addReceiver(final Receiver receiver) {
			receivers.add(receiver);
		}

		public void removeReceiver(final Receiver receiver) {
			receivers.remove(receiver);
		}

		@Override
		public void send(final MidiMessage message, final long timeStamp) {
			if (filter.test(message)) {
				for (final Receiver receiver : receivers) {
					receiver.send(message, timeStamp);
				}
			}
		}

		@Override
		public void close() {
			for (final Receiver receiver : receivers) {
				receiver.close();
			}
		}
	}
}
