/**
 * Copyright (C) 2015 Oliver Schünemann
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
 * @since 21.11.2015
 * @version 1.0
 * @author oliver
 */
package midi.device.resource;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Single resource for all Receiver and Transmission devices
 * 
 * @author oliver
 */
public final class MidiDevices {
	private static MidiDevice devRecv[];
	private static MidiDevice devTrans[];
	private static final Logger logger = LogManager.getLogger(MidiDevices.class);

	static {
		try {
			final MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
			final List<MidiDevice> transmitter = new ArrayList<>();
			final List<MidiDevice> receiver = new ArrayList<>();

			for (int i = 0; i < infos.length; i++) {
				if (logger.isDebugEnabled()) {
					logger.debug(i + " name='" + infos[i].getName() + "' (" + infos[i].getVendor()
							+ "):    " + infos[i].getDescription());
				}
				final MidiDevice device = MidiSystem.getMidiDevice(infos[i]);
				if (Math.abs(device.getMaxTransmitters()) > 0) {
					transmitter.add(device);
				} else if (Math.abs(device.getMaxReceivers()) > 0) {
					receiver.add(device);
				}
			}
			devRecv = receiver.toArray(new MidiDevice[receiver.size()]);
			devTrans = transmitter.toArray(new MidiDevice[transmitter.size()]);
		} catch (final MidiUnavailableException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private MidiDevices() {
		super();
	}

	public static MidiDevice getTransmitterDevice(final String name) {
		for (final MidiDevice transmitter : devTrans) {
			if (name.equals(transmitter.getDeviceInfo().getName())) {
				return transmitter;
			}
		}
		return null;
	}

	public static MidiDevice getReceiverDevice(final String name) {
		for (final MidiDevice receiver : devRecv) {
			if (name.equals(receiver.getDeviceInfo().getName())) {
				return receiver;
			}
		}
		return null;
	}

	public static List<String> getTransmitterNames() {
		final List<String> transmitters = new ArrayList<>();
		for (final MidiDevice transmitter : devTrans) {
			transmitters.add(transmitter.getDeviceInfo().getName());
		}
		return transmitters;
	}

	public static List<String> getReceiverNames() {
		final List<String> receivers = new ArrayList<>();
		for (final MidiDevice reveiver : devRecv) {
			receivers.add(reveiver.getDeviceInfo().getName());
		}
		return receivers;
	}
}
