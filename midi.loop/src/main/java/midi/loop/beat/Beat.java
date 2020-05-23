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
 * @since 05.03.2017
 * @version 1.0
 * @author oliver
 */
package midi.loop.beat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author oliver
 *
 */
public class Beat {
	private static final Logger logger = LoggerFactory.getLogger(Beat.class);
	public static final int BEAT_DIVISION = 24;
	private int beat = 0;
	private long beatStart = 0;
	private int currentBpm = 120;
	private final long minute = 60000000000l;
	private int bpm = 120;
	private boolean running = false;
	private int step = 0;
	private final ScheduledExecutorService service = Executors.newScheduledThreadPool(16);
	private final List<BeatListener> listeners = new ArrayList<>();
	private final List<BarListener> barListeners = new ArrayList<>();
	private final Lock lock = new ReentrantLock();

	private final NextStepRunnable[] fractionSteps = { new NextBeat(), new IntermediateBeat(8, 1),
			new IntermediateBeat(6, 1), new IntermediateBeat(8, 2), new IntermediateBeat(6, 2),
			new IntermediateBeat(8, 3), new IntermediateBeat(8, 4), new IntermediateBeat(8, 5),
			new IntermediateBeat(6, 4), new IntermediateBeat(8, 6), new IntermediateBeat(6, 5),
			new IntermediateBeat(8, 7) };

	private void publishBeat(final long beat) {
		step++;
		final NextStepRunnable nextStep = fractionSteps[step % fractionSteps.length];
		final long delay = nextStep.occuranceTime() - System.nanoTime();
		if (running) {
			lock.lock();
			try {
				for (final BeatListener listener : listeners) {
					service.execute(() -> {
						listener.accept(beat);
					});
				}
			} finally {
				lock.unlock();
			}
			service.schedule(nextStep, delay, TimeUnit.NANOSECONDS);
		}
	}

	private void publishBar(final long beat) {
		if (running) {
			lock.lock();
			try {
				barListeners.stream().filter(it -> {
					return beat % it.getNumberOfQuarterPerBar() == 0;
				}).forEach(it -> {
					service.execute(() -> {
						it.accept(beat / it.getNumberOfQuarterPerBar());
					});
				});
			} finally {
				lock.unlock();
			}
		}
	}

	public void start() {
		step = 0;
		currentBpm = bpm;
		beatStart = System.nanoTime();
		running = true;
		beat = -1;
		final long delay = fractionSteps[0].occuranceTime() - beatStart;
		service.schedule(fractionSteps[0], delay, TimeUnit.NANOSECONDS);
	}

	public void stop() {
		running = false;
	}

	private class IntermediateBeat implements NextStepRunnable {

		private final long fraction;
		private final long step;

		public IntermediateBeat(final long fraction, final long step) {
			super();
			this.fraction = fraction;
			this.step = step;
		}

		@Override
		public long occuranceTime() {
			return beatStart + minute * beat / currentBpm + minute * step / currentBpm / fraction;
		};

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if (bpm != currentBpm) {
				currentBpm = bpm;
				beatStart = System.nanoTime()
						- (minute * beat / currentBpm + minute * step / currentBpm / fraction);
			}
			publishBeat(beat * BEAT_DIVISION + BEAT_DIVISION * step / fraction);
		}
	}

	private class NextBeat implements NextStepRunnable {

		@Override
		public long occuranceTime() {
			return beatStart + minute * (beat + 1) / currentBpm;
		};

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			beat++;
			if (bpm != currentBpm) {
				currentBpm = bpm;
				beatStart = System.nanoTime() - (minute * beat / currentBpm);
			}
			logger.debug("Next beat {}", beat * BEAT_DIVISION);
			publishBar(beat);
			publishBeat(beat * BEAT_DIVISION);
		}
	}

	private interface NextStepRunnable extends Runnable {

		long occuranceTime();
	}

	/**
	 * @param listener
	 *            to add
	 */
	public void addBeatListener(final BeatListener listener) {
		lock.lock();
		try {
			listeners.add(listener);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 
	 * @param listener
	 *            to remove
	 */
	public void removeBeatListener(final BeatListener listener) {
		lock.lock();
		try {
			listeners.remove(listener);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @param listener
	 *            to add
	 */
	public void addBarListener(final BarListener listener) {
		lock.lock();
		try {
			barListeners.add(listener);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 
	 * @param listener
	 *            to remove
	 */
	public void removeBarListener(final BarListener listener) {
		lock.lock();
		try {
			barListeners.remove(listener);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Observer interface for all clients that want to react to the bar
	 */
	public interface BarListener extends LongConsumer {
		/**
		 * @return the number of Quarters the listener thinks, a bar has
		 */
		long getNumberOfQuarterPerBar();

		@Override
		void accept(long bar);
	}

	/**
	 * Observer interface for all clients that want to react to the beat
	 * 
	 * @author oliver
	 */
	public interface BeatListener extends LongConsumer {
		@Override
		void accept(long beat);
	}

	/**
	 * @return the bpm
	 */
	public int getBpm() {
		return bpm;
	}

	/**
	 * @param bpm
	 *            the bpm to set
	 */
	public void setBpm(final int bpm) {
		this.bpm = bpm;
	}

	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}
}
