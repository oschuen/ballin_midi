/**
 * Copyright (C) 2020 Oliver Schünemann
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
 * @since 12.01.2020
 * @version 1.0
 * @author oliver
 */
package midi.message;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import midi.device.resource.InputDevice;

/**
 * @author oliver
 *
 */
public class MessageReceiver implements Receiver {
	private static final Logger logger = LoggerFactory.getLogger(MessageReceiver.class);

	private static String midiInDevice = "VirMIDI \\[hw:\\d,3,0\\]";
	private final InputDevice midiInputDevice = new InputDevice();
	public static String CFG_MIDI_INPUT_DEVICE = "MIDI_INPUT_DEVICE";
	private static String defaultDevice = "null";
	private final Semaphore closeSem = new Semaphore(0);
	private final char[] text = new char[80];
	private final Display display = new Display();

	@SuppressWarnings("serial")
	private static Map<String, Object> runtimeConfig = new HashMap<String, Object>() {
		{
			put(CFG_MIDI_INPUT_DEVICE, midiInDevice);
		}
	};

	public MessageReceiver() {
		innerSetConfig(runtimeConfig);
		midiInputDevice.addInput(this);
		display.display_init();
	}

	protected static Integer getIntConfig(final String key, final Integer defValue) {
		final Object raw = runtimeConfig.get(key);
		return raw == null ? defValue : ((Integer) raw).intValue();
	}

	protected static String getStringConfig(final String key, final String defValue) {
		final Object raw = runtimeConfig.get(key);
		return raw == null ? defValue : (String) raw;
	}

	private void innerSetConfig(final Map<String, Object> runtimeConfig) {
		midiInputDevice.setDeviceName(getStringConfig(CFG_MIDI_INPUT_DEVICE, defaultDevice));
	}

	public void setChar(final int pos, final char c) {
		final String allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890.-_!\"§$%&/()=?<>#*+- ";
		if (allowed.indexOf(c) >= 0 && pos >= 0 && pos < text.length) {
			text[pos] = c;
		}
	}

	public void clearText() {
		for (int i = 0; i < text.length; i++) {
			text[i] = ' ';
		}
	}

	private String line1 = "";
	private String line2 = "";
	private String line3 = "";
	private String line4 = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Receiver#send(javax.sound.midi.MidiMessage, long)
	 */
	@Override
	public void send(final MidiMessage message, final long timeStamp) {
		if (message instanceof ShortMessage) {
			final ShortMessage shortMessage = (ShortMessage) message;
			if (shortMessage.getCommand() == ShortMessage.NOTE_ON
					&& shortMessage.getChannel() == 4) {
				setChar(((ShortMessage) message).getData1(),
						(char) ((ShortMessage) message).getData2());
			} else if (shortMessage.getCommand() == ShortMessage.NOTE_OFF
					&& shortMessage.getChannel() == 4) {
				String temp = new String(text, 0, 20);
				if (!line1.equals(temp)) {
					line1 = temp;
					display.sendByte(Display.LCD_LINE_1, Display.LCD_CMD);
					display.sendString(line1);
					logger.info("Write line 1 \"{}\"", line1);
				}
				temp = new String(text, 20, 20);
				if (!line2.equals(temp)) {
					line2 = temp;
					display.sendByte(Display.LCD_LINE_2, Display.LCD_CMD);
					display.sendString(line2);
					logger.info("Write line 2 \"{}\"", line2);
				}
				temp = new String(text, 40, 20);
				if (!line3.equals(temp)) {
					line3 = temp;
					display.sendByte(Display.LCD_LINE_3, Display.LCD_CMD);
					display.sendString(line3);
					logger.info("Write line 3 \"{}\"", line3);
				}
				temp = new String(text, 60, 20);
				if (!line4.equals(temp)) {
					line4 = temp;
					display.sendByte(Display.LCD_LINE_4, Display.LCD_CMD);
					display.sendString(line4);
					logger.info("Write line 4 \"{}\"", line4);
				}
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
		closeSem.release();
	}

	public void waitForClose() {
		try {
			closeSem.acquire();
		} catch (final InterruptedException e) {
			logger.error("Failed to wait for close", e);
		}
	}

	private static void innerMain(final File file) {
		final Properties props = new Properties();
		try {
			try (InputStream stream = new FileInputStream(file)) {
				props.load(stream);
			}
		} catch (final IOException e) {
		}
		runtimeConfig.clear();
		props.entrySet().stream()
				.forEach(e -> runtimeConfig.put((String) e.getKey(), (String) e.getValue()));
		try (MessageReceiver receiver = new MessageReceiver()) {
			receiver.waitForClose();
		}
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 *            form command line
	 * 
	 */
	public static void main(final String[] args) {
		final Options options = new Options();
		options.addOption("c", "config", true, "Configuration File");
		final CommandLineParser parser = new DefaultParser();
		boolean error = false;
		File file = null;
		try {
			final CommandLine cmd = parser.parse(options, args);
			final String configFile = cmd.getOptionValue("c");
			if (configFile == null) {
				error = true;
				file = null;
			} else {
				file = new File(configFile);
			}
		} catch (final ParseException e) {
			error = true;
		}
		if (error || file == null) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Message Display", options);
		} else {
			innerMain(file);
		}
	}
}
