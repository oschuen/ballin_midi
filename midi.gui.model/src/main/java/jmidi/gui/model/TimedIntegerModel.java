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
 * @since 17.12.2019
 * @version 1.0
 * @author oliver
 */
package jmidi.gui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author oliver
 *
 */
public class TimedIntegerModel<T extends Enum<?>> {

	private int currentIndex;
	private int lastBeforeOff = 0;
	private final List<T> values = new ArrayList<>();
	private long lastModeChange;
	private static final long offTime = 2000;
	private final Lock lock = new ReentrantLock();
	private Future<?> stopIncrement = null;
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private static final int REPEAT_TIME = 150;
	private final List<ValueObserver<T>> observers = new ArrayList<>();

	public static interface ValueObserver<T> {
		void valueChanged(T newValue);
	}

	@SafeVarargs
	public TimedIntegerModel(final T stableValue, final T... alternatives) {
		super();
		currentIndex = 0;
		lastBeforeOff = currentIndex;
		this.values.add(stableValue);
		this.values.addAll(Arrays.asList(alternatives));
	}

	public T increment() {
		lock.lock();
		try {
			if (System.currentTimeMillis() > lastModeChange + offTime) {
				if (currentIndex == 0) {
					if (lastBeforeOff == 0) {
						currentIndex = (currentIndex + 1) % values.size();
						lastBeforeOff = currentIndex;
					} else {
						currentIndex = lastBeforeOff;
					}
				} else {
					lastBeforeOff = currentIndex;
					currentIndex = 0;
				}
			} else {
				currentIndex = (currentIndex + 1) % values.size();
				lastBeforeOff = currentIndex;
			}
			lastModeChange = System.currentTimeMillis();
		} finally {
			lock.unlock();
		}
		fireNewValue();
		return values.get(currentIndex);
	}

	public void setValue(final T value) {
		lock.lock();
		try {
			currentIndex = values.indexOf(value) >= 0 ? values.indexOf(value) : 0;
			lastBeforeOff = currentIndex;
			fireNewValue();
		} finally {
			lock.unlock();
		}
	}

	public T getValue() {
		return values.get(currentIndex);
	}

	public void startIncrementing() {
		lock.lock();
		try {
			if (stopIncrement != null) {
				stopIncrementing();
			}
			stopIncrement = executor.scheduleWithFixedDelay(() -> {
				increment();
			}, 2 * REPEAT_TIME, REPEAT_TIME, TimeUnit.MILLISECONDS);
		} finally {
			lock.unlock();
		}
	}

	public void stopIncrementing() {
		lock.lock();
		try {
			if (stopIncrement != null) {
				stopIncrement.cancel(false);
				stopIncrement = null;
			}
		} finally {
			lock.unlock();
		}
	}

	public void fireNewValue() {
		lock.lock();
		try {
			final T newValue = getValue();
			for (final ValueObserver<T> observer : observers) {
				observer.valueChanged(newValue);
			}
		} finally {
			lock.unlock();
		}
	}

	public void addValueObserver(final ValueObserver<T> observer) {
		lock.lock();
		try {
			observers.add(observer);
		} finally {
			lock.unlock();
		}
	}

	public void removeValueObserver(final ValueObserver<T> observer) {
		lock.lock();
		try {
			observers.remove(observer);
		} finally {
			lock.unlock();
		}
	}
}
