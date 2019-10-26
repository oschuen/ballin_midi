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
 * @since 06.11.2016
 * @version 1.0
 * @author oliver
 */
package midi.pad.ui.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import midi.device.resource.InputDevice;
import midi.device.resource.NullReceiver;
import midi.device.resource.OutputDevice;
import midi.loop.config.ChannelConfig;
import midi.pad.ui.Screen;

/**
 * @author oliver
 *
 */
public class Runtime {

	private final Screen screen = new Screen();
	private static final Logger logger = LoggerFactory.getLogger(Runtime.class);

	private Receiver padOutput = null;
	private final Receiver padInput = new PadReceiver(screen);
	private static boolean checkConfiguration = false;
	private static String defaultDevice = "null";
	// private static String padInDevice = "Mini \\[hw:\\d,0,0\\]";
	// private static String padOutDevice = "Mini \\[hw:\\d,0,0\\]";
	private static String padInDevice = "VirMIDI \\[hw:\\d,2,0\\]";
	private static String padOutDevice = "VirMIDI \\[hw:\\d,2,0\\]";
	private static String midi1InDevice = "M2X2 \\[hw:\\d,0,0\\]";
	private static String midi1OutDevice = "M2X2 \\[hw:\\d,0,0\\]";
	private static String midi2InDevice = "M2X2 \\[hw:\\d,0,1\\]";
	private static String midi2OutDevice = "M2X2 \\[hw:\\d,0,1\\]";
	private static String midi3InDevice = "VirMIDI \\[hw:\\d,0,0\\]";
	private static String midi3OutDevice = "VirMIDI \\[hw:\\d,0,0\\]";
	private static String midi4InDevice = "VirMIDI \\[hw:\\d,1,0\\]";
	private static String midi4OutDevice = "VirMIDI \\[hw:\\d,1,0\\]";
	private static long flashPeriod = 500;
	private boolean inRuntimeThread = false;

	private final OutputDevice padOutputDevice = new OutputDevice();
	private final InputDevice padInputDevice = new InputDevice();
	private final OutputDevice midi1OutputDevice = new OutputDevice();
	private final InputDevice midi1InputDevice = new InputDevice();
	private final OutputDevice midi2OutputDevice = new OutputDevice();
	private final InputDevice midi2InputDevice = new InputDevice();
	private final OutputDevice midi3OutputDevice = new OutputDevice();
	private final InputDevice midi3InputDevice = new InputDevice();
	private final OutputDevice midi4OutputDevice = new OutputDevice();
	private final InputDevice midi4InputDevice = new InputDevice();
	private final OutputDevice[] outputChannels = { midi1OutputDevice, midi2OutputDevice,
			midi3OutputDevice, midi4OutputDevice };
	private final InputDevice[] inputChannels = { midi1InputDevice, midi2InputDevice,
			midi3InputDevice, midi4InputDevice };
	private static final NullReceiver nullReceiver = new NullReceiver();

	public static String CFG_NUMBER_OF_LAYERS = "NUMBER_OF_LAYERS";
	public static String CFG_PAD_INPUT_DEVICE = "PAD_INPUT_DEVICE";
	public static String CFG_PAD_OUTPUT_DEVICE = "PAD_OUTPUT_DEVICE";
	public static String CFG_MIDI_1_INPUT_DEVICE = "MIDI_1_INPUT_DEVICE";
	public static String CFG_MIDI_1_OUTPUT_DEVICE = "MIDI_1_OUTPUT_DEVICE";
	public static String CFG_MIDI_2_INPUT_DEVICE = "MIDI_2_INPUT_DEVICE";
	public static String CFG_MIDI_2_OUTPUT_DEVICE = "MIDI_2_OUTPUT_DEVICE";
	public static String CFG_MIDI_3_INPUT_DEVICE = "MIDI_3_INPUT_DEVICE";
	public static String CFG_MIDI_3_OUTPUT_DEVICE = "MIDI_3_OUTPUT_DEVICE";
	public static String CFG_MIDI_4_INPUT_DEVICE = "MIDI_4_INPUT_DEVICE";
	public static String CFG_MIDI_4_OUTPUT_DEVICE = "MIDI_4_OUTPUT_DEVICE";

	@SuppressWarnings("serial")
	private static Map<String, String> runtimeConfig = new HashMap<String, String>() {
		{
			put(CFG_NUMBER_OF_LAYERS, "5");
			put(CFG_PAD_INPUT_DEVICE, padInDevice);
			put(CFG_PAD_OUTPUT_DEVICE, padOutDevice);
			put(CFG_MIDI_1_INPUT_DEVICE, midi1InDevice);
			put(CFG_MIDI_1_OUTPUT_DEVICE, midi1OutDevice);
			put(CFG_MIDI_2_INPUT_DEVICE, midi2InDevice);
			put(CFG_MIDI_2_OUTPUT_DEVICE, midi2OutDevice);
			put(CFG_MIDI_3_INPUT_DEVICE, midi3InDevice);
			put(CFG_MIDI_3_OUTPUT_DEVICE, midi3OutDevice);
			put(CFG_MIDI_4_INPUT_DEVICE, midi4InDevice);
			put(CFG_MIDI_4_OUTPUT_DEVICE, midi4OutDevice);

		}
	};

	private static Lock lock = new ReentrantLock();

	private static Runtime singleton = null;

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private final Semaphore redrawSem = new Semaphore(0);

	private final Map<Runnable, DrawRunnable> runnerMap = new ConcurrentHashMap<>();

	private final Runnable flasher = new Runnable() {
		@Override
		public void run() {
			screen.toggleFlash();
			invalidate();
		};
	};

	private Runtime() {
		super();
		padOutput = padOutputDevice.getOutput();
		padInputDevice.addInput(padInput);
		scheduleAtFixedRate(flasher, flashPeriod, flashPeriod, TimeUnit.MILLISECONDS);
	}

	/**
	 * @param runtimeConfig
	 *            the runtimeConfig to set
	 */
	public static synchronized void setRuntimeConfig(final Map<String, String> runtimeConfig) {
		lock.lock();
		try {
			Runtime.runtimeConfig = runtimeConfig;
			checkConfiguration = true;
			getRuntime().innerSetConfig();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @param runtimeConfig
	 *            the runtimeConfig to set
	 */
	public static synchronized void setRuntimeConfig(final Properties runtimeConfig) {
		lock.lock();
		try {
			Runtime.runtimeConfig.clear();
			runtimeConfig.entrySet().stream().forEach(
					e -> Runtime.runtimeConfig.put((String) e.getKey(), (String) e.getValue()));
			checkConfiguration = true;
			getRuntime().innerSetConfig();
		} finally {
			lock.unlock();
		} 
	}

	protected static Integer getIntConfig(final String key, final Integer defValue) {
		final String raw = Runtime.runtimeConfig.get(key);
		return raw == null ? defValue : Integer.parseInt(raw);
	}

	protected static String getStringConfig(final String key, final String defValue) {
		final Object raw = Runtime.runtimeConfig.get(key);
		return raw == null ? defValue : (String) raw;
	}

	public static Runtime getRuntime() {
		lock.lock();
		try {
			if (singleton == null) {
				singleton = new Runtime();
				if (checkConfiguration) {
					singleton.innerSetConfig();
				}
			}
			return singleton;
		} finally {
			lock.unlock();
		}
	}

	private void innerSetConfig() {
		try {
			lock.lock();
			final Integer numberOfLayers = getIntConfig(CFG_NUMBER_OF_LAYERS, Integer.valueOf(5));
			screen.setNumberOfLayers(numberOfLayers);
			padInputDevice.setDeviceName(getStringConfig(CFG_PAD_INPUT_DEVICE, defaultDevice));
			padOutputDevice.setDeviceName(getStringConfig(CFG_PAD_OUTPUT_DEVICE, defaultDevice));
			midi1InputDevice.setDeviceName(getStringConfig(CFG_MIDI_1_INPUT_DEVICE, defaultDevice));
			midi1OutputDevice
					.setDeviceName(getStringConfig(CFG_MIDI_1_OUTPUT_DEVICE, defaultDevice));
			midi2InputDevice.setDeviceName(getStringConfig(CFG_MIDI_2_INPUT_DEVICE, defaultDevice));
			midi2OutputDevice
					.setDeviceName(getStringConfig(CFG_MIDI_2_OUTPUT_DEVICE, defaultDevice));
			midi3InputDevice.setDeviceName(getStringConfig(CFG_MIDI_3_INPUT_DEVICE, defaultDevice));
			midi3OutputDevice
					.setDeviceName(getStringConfig(CFG_MIDI_3_OUTPUT_DEVICE, defaultDevice));
			midi4InputDevice.setDeviceName(getStringConfig(CFG_MIDI_4_INPUT_DEVICE, defaultDevice));
			midi4OutputDevice
					.setDeviceName(getStringConfig(CFG_MIDI_4_OUTPUT_DEVICE, defaultDevice));
		} finally {
			checkConfiguration = false;
			lock.unlock();
		}
	}

	public void applyChannelConfig(final ChannelConfig config) {
		System.out.println(config);
		if (config.getMidiOut() >= 0 && config.getMidiOut() < outputChannels.length) {
			try {
				final Receiver receiver = outputChannels[config.getMidiOut()].getOutput();
				final ShortMessage bsmsb = new ShortMessage();
				System.out.println(
						"MSB : " + (config.getBank() / 128) + " LSB " + (config.getBank() % 128));
				bsmsb.setMessage(ShortMessage.CONTROL_CHANGE, config.getChannel(), 0x00,
						config.getBank() / 128);

				receiver.send(bsmsb, 0);
				final ShortMessage bslsb = new ShortMessage();
				bslsb.setMessage(ShortMessage.CONTROL_CHANGE, config.getChannel(), 0x20,
						(config.getBank() % 128));
				receiver.send(bslsb, 0);
				final ShortMessage pc = new ShortMessage();
				pc.setMessage(ShortMessage.PROGRAM_CHANGE, config.getChannel(), config.getProgram(),
						0);
				receiver.send(pc, 0);
				final ShortMessage chorus = new ShortMessage();
				chorus.setMessage(ShortMessage.CONTROL_CHANGE, config.getChannel(), 93,
						(config.getChoir() & 0x7f));
				receiver.send(chorus, 0);
				final ShortMessage reverb = new ShortMessage();
				reverb.setMessage(ShortMessage.CONTROL_CHANGE, config.getChannel(), 91,
						(config.getReverb() & 0x7f));
				receiver.send(reverb, 0);
			} catch (final InvalidMidiDataException e) {
				logger.error("Failed to configure output device");
			}
		}

	}

	private void redraw() {
		if (!(padOutput == null || screen == null)) {
			screen.draw(padOutput);
		}
	}

	public void invalidate() {
		lock.lock();
		try {
			if (inRuntimeThread) {
				redrawSem.release();
			} else {
				schedule(new Runnable() {
					@Override
					public void run() {
						redrawSem.release();
					}
				});
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @param task
	 * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable)
	 */
	public void schedule(final Runnable command) {
		executor.submit(new DrawRunnable(command));
	}

	/**
	 * @param command
	 * @param delay
	 * @param unit
	 * @see java.util.concurrent.ScheduledExecutorService#schedule(java.lang.Runnable,
	 *      long, java.util.concurrent.TimeUnit)
	 */
	public void schedule(final Runnable command, final long delay, final TimeUnit unit) {
		final DrawRunnable runner = new DrawRunnable(command);
		runnerMap.put(command, runner);
		runner.setFuture(executor.schedule(runner, delay, unit));
	}

	/**
	 * @param command
	 * @param initialDelay
	 * @param period
	 * @param unit
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(java.lang.Runnable,
	 *      long, long, java.util.concurrent.TimeUnit)
	 */
	public void scheduleAtFixedRate(final Runnable command, final long initialDelay,
			final long period, final TimeUnit unit) {
		final DrawRunnable runner = new DrawRunnable(command);
		runnerMap.put(command, runner);
		runner.setFuture(executor.scheduleAtFixedRate(runner, initialDelay, period, unit));
	}

	/**
	 * @param command
	 * @param initialDelay
	 * @param delay
	 * @param unit
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(java.lang.Runnable,
	 *      long, long, java.util.concurrent.TimeUnit)
	 */
	public void scheduleWithFixedDelay(final Runnable command, final long initialDelay,
			final long delay, final TimeUnit unit) {
		final DrawRunnable runner = new DrawRunnable(command);
		runnerMap.put(command, runner);
		runner.setFuture(executor.scheduleWithFixedDelay(runner, initialDelay, delay, unit));

	}

	public void stop(final Runnable command) {
		final DrawRunnable runner = runnerMap.remove(command);
		if (runner != null) {
			runner.getFuture().cancel(false);
		}
	}

	public void checkRuntimeContext() {
		if (!inRuntimeThread) {
			throw new RuntimeException("Function not called from RuntTimeContext");
		}
	}

	private class DrawRunnable implements Runnable {
		private Future<?> future;
		private final Runnable runner;

		public DrawRunnable(final Runnable runner) {
			super();
			this.runner = runner;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			lock.lock();
			try {
				inRuntimeThread = true;
				runner.run();
				inRuntimeThread = false;
				if (redrawSem.drainPermits() > 0) {
					redraw();
				}
			} finally {
				inRuntimeThread = false;
				lock.unlock();
			}
		}

		/**
		 * @return the future
		 */
		public Future<?> getFuture() {
			return future;
		}

		/**
		 * @param future
		 *            the future to set
		 */
		public void setFuture(final Future<?> future) {
			this.future = future;
		}
	}

	/**
	 * @return the screen
	 */
	public Screen getScreen() {
		return screen;
	}

	public Receiver getOutput(final int channel) {
		if (channel >= 0 && channel < outputChannels.length) {
			return outputChannels[channel].getOutput();
		}
		return nullReceiver;
	}

	public Receiver getOutput(final ChannelConfig config) {
		return new ConfiguredReceiver(config);
	}

	public void addBelowSplitInput(final Receiver receiver, final int device) {
		if (device >= 0 && device < outputChannels.length) {
			inputChannels[device].addBelowSplitInput(receiver);
		}
	}

	public void addAboveSplitInput(final Receiver receiver, final int device) {
		if (device >= 0 && device < outputChannels.length) {
			inputChannels[device].addAboveSplitInput(receiver);
		}
	}

	public void addInput(final Receiver receiver, final int device) {
		if (device >= 0 && device < outputChannels.length) {
			inputChannels[device].addInput(receiver);
		}
	}

	public void removeInput(final Receiver receiver, final int device) {
		if (device >= 0 && device < outputChannels.length) {
			inputChannels[device].removeInput(receiver);
		}
	}

	private class ConfiguredReceiver implements Receiver {
		private int channel;
		private Receiver receiver;
		private final ChannelConfig config;
		private boolean close = false;

		public ConfiguredReceiver(final ChannelConfig config) {
			this.config = config;
			channel = config.getMidiOut();
			receiver = getOutput(channel);
		}

		@Override
		public void send(final MidiMessage message, final long timeStamp) {
			if (channel != config.getMidiOut()) {
				channel = config.getMidiOut();
				receiver.close();
				receiver = getOutput(channel);
			}
			if (!close) {
				receiver.send(message, timeStamp);
			}
		}

		@Override
		public void close() {
			receiver.close();
			close = true;
		}
	}
}
