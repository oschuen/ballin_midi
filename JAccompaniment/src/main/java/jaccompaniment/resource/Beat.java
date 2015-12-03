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
package jaccompaniment.resource;

import jaccompaniment.chord.ChordRecognizer.ChordListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Produces a beat of one-sixteenth steps that correspond to the configured bpm
 * amount
 * 
 * @author oliver
 */
public class Beat implements ChordListener {

	private final ScheduledExecutorService service;
	private final List<BeatListener> listeners = new ArrayList<>();
	private Future<?> stopFuture = null;
	private final Lock lock = new ReentrantLock();
	private long bpm = 120;
	private int currentBeat = 0;

	private boolean awaitSyncStart = false;

	/**
	 * Constructs an instance of beat and allocates all needed resources
	 */
	public Beat() {
		service = Executors.newScheduledThreadPool(16);
	}

	/**
	 * starts the beat immediately
	 */
	public void start() {
		lock.lock();
		try {
			awaitSyncStart = false;
			currentBeat = 0;
			startBeat();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * beat is started after a chord is recognized
	 */
	public void syncStart() {
		lock.lock();
		try {
			awaitSyncStart = true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * stops the beat
	 */
	public void stop() {
		lock.lock();
		try {
			awaitSyncStart = false;
			if (stopFuture != null) {
				stopFuture.cancel(false);
				stopFuture = null;
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * internal used start function that stops a currently active beat and start
	 * a new. For example used at bpm change
	 */
	private void startBeat() {
		lock.lock();
		try {
			if (stopFuture != null) {
				stopFuture.cancel(false);
			}
			stopFuture = service.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					fireNextBeat(currentBeat++);
				}
			}, 0, 60000000000l / bpm / 4, TimeUnit.NANOSECONDS);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Closes the beat and releases all resources
	 */
	public void close() {
		service.shutdown();
	}

	/**
	 * Observer interface for all clients that want to react to the beat
	 * 
	 * @author oliver
	 */
	public interface BeatListener {
		void nextBeat(int beat);
	}

	/**
	 * starts at syncstart the beat when chord is recognized
	 * 
	 * @see midi.chord.ChordRecognizer.ChordListener#newChord(java.lang.String)
	 */
	@Override
	public void newChord(final String chord) {
		lock.lock();
		try {
			if (awaitSyncStart) {
				awaitSyncStart = false;
				start();
			}
		} finally {
			lock.unlock();
		}
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
	 * internal used fire function that signals the next beat to the clients
	 * 
	 * @param beat
	 */
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	private void fireNextBeat(final int beat) {
		lock.lock();
		try {
			for (final BeatListener listener : listeners) {
				service.execute(new Runnable() {
					@Override
					public void run() {
						listener.nextBeat(beat);
					}
				});
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @return the beat
	 */
	public long getBpM() {
		return bpm;
	}

	/**
	 * @param bpm
	 *            the beat to set
	 */
	public void setBpM(final long bpm) {
		lock.lock();
		try {
			this.bpm = bpm;
			if (stopFuture != null) {
				startBeat();
			}
		} finally {
			lock.unlock();
		}
	}
}
