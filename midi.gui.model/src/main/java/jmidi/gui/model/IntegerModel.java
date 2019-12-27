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
 * @since 03.10.2016
 * @version 1.0
 * @author oliver
 */
package jmidi.gui.model;

import java.util.ArrayList;
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
public class IntegerModel {

	private int minValue = Integer.MIN_VALUE;
	private int maxValue = Integer.MAX_VALUE;
	private int value = 0;
	private final Lock lock = new ReentrantLock();

	private Future<?> stopIncrement = null;
	private Future<?> stopDecrement = null;
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private static final int REPEAT_TIME = 150;
	private final List<ValueObserver> observers = new ArrayList<>();

	public IntegerModel(final int minValue, final int maxValue, final int value) {
		super();
		setMinValue(minValue);
		setMaxValue(maxValue);
		setValue(value);
	}

	public void setValue(final int value) {
		lock.lock();
		try {
			final int oldValue = this.value;
			this.value = Math.min(maxValue, Math.max(minValue, value));
			if (oldValue != this.value) {
				fireNewValue();
			}
		} finally {
			lock.unlock();
		}
	}

	public int getValue() {
		return value;
	}

	public void increment() {
		increment(1);
	}

	public void increment(final int steps) {
		lock.lock();
		try {
			setValue(value + Math.abs(steps));
		} finally {
			lock.unlock();
		}
	}

	public void decrement() {
		decrement(1);
	}

	public void decrement(final int steps) {
		lock.lock();
		try {
			setValue(value - Math.abs(steps));
		} finally {
			lock.unlock();
		}
	}

	public void startIncrementing() {
		lock.lock();
		try {
			if (stopIncrement != null) {
				stopIncrementing();
			}
			stopIncrement = executor.scheduleWithFixedDelay(new Runnable() {
				int addNumber = 0;

				@Override
				public void run() {
					if (addNumber < 4) {
						increment();
					} else {
						increment(5);
					}
					addNumber++;
				}
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

	public void startDecrementing() {
		lock.lock();
		try {
			if (stopDecrement != null) {
				stopDecrementing();
			}
			stopDecrement = executor.scheduleWithFixedDelay(new Runnable() {
				int addNumber = 0;

				@Override
				public void run() {
					if (addNumber < 4) {
						decrement();
					} else {
						decrement(5);
					}
					addNumber++;
				}
			}, 2 * REPEAT_TIME, REPEAT_TIME, TimeUnit.MILLISECONDS);
		} finally {
			lock.unlock();
		}
	}

	public void stopDecrementing() {
		lock.lock();
		try {
			if (stopDecrement != null) {
				stopDecrement.cancel(false);
				stopDecrement = null;
			}
		} finally {
			lock.unlock();
		}
	}

	public static interface ValueObserver {
		void valueChanged(int newValue);
	}

	public void fireNewValue() {
		lock.lock();
		try {
			final int newValue = getValue();
			for (final ValueObserver observer : observers) {
				observer.valueChanged(newValue);
			}
		} finally {
			lock.unlock();
		}
	}

	public void addValueObserver(final ValueObserver observer) {
		lock.lock();
		try {
			observers.add(observer);
		} finally {
			lock.unlock();
		}
	}

	public void removeValueObserver(final ValueObserver observer) {
		lock.lock();
		try {
			observers.remove(observer);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @return the minValue
	 */
	public int getMinValue() {
		return minValue;
	}

	/**
	 * @return the maxValue
	 */
	public int getMaxValue() {
		return maxValue;
	}

	/**
	 * @param minValue
	 *            the minValue to set
	 */
	public void setMinValue(final int minValue) {
		lock.lock();
		try {
			this.minValue = minValue;
			maxValue = Math.max(minValue, maxValue);
			setValue(value);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @param maxValue
	 *            the maxValue to set
	 */
	public void setMaxValue(final int maxValue) {
		lock.lock();
		try {
			this.maxValue = maxValue;
			minValue = Math.min(maxValue, minValue);
			setValue(value);
		} finally {
			lock.unlock();
		}
	}
}
