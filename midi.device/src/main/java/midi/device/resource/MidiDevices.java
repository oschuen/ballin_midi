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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single resource for all Receiver and Transmission devices
 * 
 * @author oliver
 */
public final class MidiDevices {
	private static MidiDevice[] devRecv;
	private static MidiDevice[] devTrans;
	private static MidiDevice.Info[] infos;
	private static final Logger logger = LoggerFactory.getLogger(MidiDevices.class);

	static {
		infos = MidiSystem.getMidiDeviceInfo();
		devRecv = new MidiDevice[infos.length];
		devTrans = new MidiDevice[infos.length];
		for (int i = 0; i < infos.length; i++) {
			if (logger.isDebugEnabled()) {
				logger.debug(i + " name='" + infos[i].getName() + "' (" + infos[i].getVendor() + ")");
			}
		}
	}

	private MidiDevices() {
		super();
	}

	private static void readDevicesByName(String name) {
		try {
			for (int i = 0; i < infos.length; i++) {
				if (name.equals(infos[i].getName()) && devTrans[i] == null && devRecv[i] == null) {
					final MidiDevice device = MidiSystem.getMidiDevice(infos[i]);
					if (Math.abs(device.getMaxTransmitters()) > 0) {
						devTrans[i] = device;
					}
					if (Math.abs(device.getMaxReceivers()) > 0) {
						devRecv[i] = device;
					}
				}
			}
		} catch (MidiUnavailableException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private static void readDevicesByRegex(String regex) {
		try {
			for (int i = 0; i < infos.length; i++) {
				if (infos[i].getName().matches(regex) && devTrans[i] == null && devRecv[i] == null) {
					final MidiDevice device = MidiSystem.getMidiDevice(infos[i]);
					if (Math.abs(device.getMaxTransmitters()) > 0) {
						devTrans[i] = device;
					}
					if (Math.abs(device.getMaxReceivers()) > 0) {
						devRecv[i] = device;
					}
				}
			}
		} catch (MidiUnavailableException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private static void readAllDevices() {
		try {
			for (int i = 0; i < infos.length; i++) {
				if (devTrans[i] == null && devRecv[i] == null) {
					final MidiDevice device = MidiSystem.getMidiDevice(infos[i]);
					if (Math.abs(device.getMaxTransmitters()) > 0) {
						devTrans[i] = device;
					}
					if (Math.abs(device.getMaxReceivers()) > 0) {
						devRecv[i] = device;
					}
				}
			}
		} catch (MidiUnavailableException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static MidiDevice getTransmitterDevice(final String name) {
		readDevicesByName(name);
		for (final MidiDevice transmitter : devTrans) {
			if (transmitter != null && name.equals(transmitter.getDeviceInfo().getName())) {
				return transmitter;
			}
		}
		return null;
	}

	public static MidiDevice getReceiverDevice(final String name) {
		readDevicesByName(name);
		for (final MidiDevice receiver : devRecv) {
			if (receiver != null && name.equals(receiver.getDeviceInfo().getName())) {
				return receiver;
			}
		}
		return null;
	}

	public static MidiDevice getTransmitterDeviceRegex(final String regex) {
		if (regex == null) {
			return null;
		}
		readDevicesByRegex(regex);
		for (final MidiDevice transmitter : devTrans) {
			if (transmitter != null && transmitter.getDeviceInfo().getName().matches(regex)) {
				return transmitter;
			}
		}
		return null;
	}

	public static MidiDevice getReceiverDeviceRegex(final String regex) {
		if (regex == null) {
			return null;
		}
		readDevicesByRegex(regex);
		for (final MidiDevice receiver : devRecv) {
			if (receiver != null && receiver.getDeviceInfo().getName().matches(regex)) {
				return receiver;
			}
		}
		return null;
	}

	public static List<String> getTransmitterNames() {
		readAllDevices();
		final List<String> transmitters = new ArrayList<>();
		for (final MidiDevice transmitter : devTrans) {
			transmitters.add(transmitter.getDeviceInfo().getName());
		}
		return transmitters;
	}

	public static List<String> getReceiverNames() {
		readAllDevices();
		final List<String> receivers = new ArrayList<>();
		for (final MidiDevice reveiver : devRecv) {
			receivers.add(reveiver.getDeviceInfo().getName());
		}
		return receivers;
	}

	public static MidiDevice secureGetReceiverDevice(final String name) {
		readDevicesByName(name);
		MidiDevice device = MidiDevices.getReceiverDevice(name);
		if (device == null) {
			device = MidiDevices.getReceiverDevice(MidiDevices.getReceiverNames().get(0));
		}
		return device;
	}

	public static MidiDevice secureGetTransmitterDevice(final String name) {
		readDevicesByName(name);
		MidiDevice device = MidiDevices.getTransmitterDevice(name);
		if (device == null) {
			device = MidiDevices.getTransmitterDevice(MidiDevices.getTransmitterNames().get(0));
		}
		return device;
	}
}
