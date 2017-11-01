/**
 * Copyright (C) 2017 Oliver Schünemann
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
 * @since 19.03.2017
 * @version 1.0
 * @author oliver
 */
package midi.loop.config;

import java.util.Optional;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import midi.device.resource.MidiDevices;

/**
 * @author oliver
 *
 */
public class MidiSystem {
	public static final String MIDI_OUT_DEVICE_1 = "midi_out_device_1";
	public static final String MIDI_OUT_DEVICE_2 = "midi_out_device_2";
	public static final String MIDI_OUT_DEVICE_3 = "midi_out_device_3";
	public static final String MIDI_OUT_DEVICE_4 = "midi_out_device_4";

	public static final String MIDI_IN_DEVICE_1 = "midi_in_device_1";
	public static final String MIDI_IN_DEVICE_2 = "midi_in_device_2";
	public static final String MIDI_IN_DEVICE_3 = "midi_in_device_3";
	public static final String MIDI_IN_DEVICE_4 = "midi_in_device_4";

	private static final Logger logger = LoggerFactory.getLogger(MidiSystem.class);

	private static String[] outDeviceKeys = { MIDI_OUT_DEVICE_1, MIDI_OUT_DEVICE_2,
			MIDI_OUT_DEVICE_3, MIDI_OUT_DEVICE_4 };
	private static String[] inDeviceKeys = { MIDI_IN_DEVICE_1, MIDI_IN_DEVICE_2, MIDI_IN_DEVICE_3,
			MIDI_IN_DEVICE_4 };

	private MidiSystem() {
		// No instance of this class
	}

	public static Optional<Receiver> getOutReceiver(final int midiDeviceNr) {
		try {
			final String deviceName = System.getProperty(outDeviceKeys[midiDeviceNr], "");
			final MidiDevice reveiverDevice = MidiDevices.secureGetReceiverDevice(deviceName);
			if (reveiverDevice == null) {
				logger.error("Device {} not found", deviceName);
			} else {
				if (!reveiverDevice.isOpen()) {
					reveiverDevice.open();
				}
				return Optional.of(reveiverDevice.getReceiver());
			}
		} catch (final Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return Optional.empty();
	}

	public static void setInReceiver(final int midiDeviceNr, final Receiver receiver) {
		try {
			final String deviceName = System.getProperty(inDeviceKeys[midiDeviceNr], "");
			final MidiDevice recognizerDevice = MidiDevices.secureGetTransmitterDevice(deviceName);
			if (recognizerDevice == null) {
				logger.error("Recognizer Device not found");
			} else {
				if (!recognizerDevice.isOpen()) {
					try {
						recognizerDevice.open();
					} catch (final MidiUnavailableException e1) {
						logger.error("Midi Exception occured In Receiver NR.:{}", midiDeviceNr, e1);
					}
				}
				final Transmitter recognizerTransmitter = recognizerDevice.getTransmitter();
				recognizerTransmitter.setReceiver(receiver);
			}
		} catch (final Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
}
