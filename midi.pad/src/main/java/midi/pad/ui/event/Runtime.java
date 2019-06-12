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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.midi.Receiver;

import midi.device.resource.InputDevice;
import midi.device.resource.OutputDevice;
import midi.pad.ui.Screen;

/**
 * @author oliver
 *
 */
public class Runtime {

	private final Screen screen = new Screen();

	private Receiver padOutput = null;
	private final Receiver padInput = new PadReceiver(screen);
	private static boolean checkConfiguration = true;
	private static String defaultDevice = "Mini [hw:3,0,0]";
	private static long flashPeriod = 500;
	private boolean inRuntimeThread = false;
	
	private final OutputDevice padOutputDevice = new OutputDevice();
	private final InputDevice padInputDevice = new InputDevice();

	@SuppressWarnings("serial")
	private static Map<String, Object> runtimeConfig = new HashMap<String, Object>() {
		{
			put(CFG_NUMBER_OF_LAYERS, 5);
			put(CFG_INPUT_DEVICE, defaultDevice);
			put(CFG_OUTPUT_DEVICE, defaultDevice);
		}
	};

	private final Map<String, Object> runningConfig = new HashMap<>();

	private static Lock lock = new ReentrantLock();

	private static Runtime singleton = null;

	public static String CFG_NUMBER_OF_LAYERS = "NUMBER_OF_LAYERS";
	public static String CFG_INPUT_DEVICE = "INPUT_DEVICE";
	public static String CFG_OUTPUT_DEVICE = "OUTPUT_DEVICE";

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
	public static synchronized void setRuntimeConfig(final Map<String, Object> runtimeConfig) {
		lock.lock();
		try {
			Runtime.runtimeConfig = runtimeConfig;
			checkConfiguration = true;
			getRuntime().innerSetConfig(runtimeConfig);
		} finally {
			lock.unlock();
		}
	}

	protected static Integer getIntConfig(final String key, final Integer defValue) {
		final Object raw = Runtime.runtimeConfig.get(key);
		return raw == null ? defValue : ((Integer) raw).intValue();
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
					singleton.innerSetConfig(runtimeConfig);
				}
			}
			return singleton;
		} finally {
			lock.unlock();
		}
	}

	private void innerSetConfig(final Map<String, Object> runtimeConfig) {
		try {
			lock.lock();
			final Integer numberOfLayers = getIntConfig(CFG_NUMBER_OF_LAYERS, Integer.valueOf(5));
			if (!numberOfLayers.equals(runningConfig.get(CFG_NUMBER_OF_LAYERS))) {
				screen.setNumberOfLayers(numberOfLayers);
				runningConfig.put(CFG_NUMBER_OF_LAYERS, numberOfLayers);
			}
			padInputDevice.setDeviceName(getStringConfig(CFG_INPUT_DEVICE, defaultDevice));
			padOutputDevice.setDeviceName(getStringConfig(CFG_OUTPUT_DEVICE, defaultDevice));
		} finally {
			checkConfiguration = false;
			lock.unlock();
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
	public void scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period,
			final TimeUnit unit) {
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
	public void scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay,
			final TimeUnit unit) {
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
}
