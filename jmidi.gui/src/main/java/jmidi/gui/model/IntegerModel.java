/**
 * Copyright (C) 2016 Oliver Schünemann
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, 
 * Boston, MA 02110, USA 
 * 
 * @since 03.10.2016
 * @version 1.0
 * @author oliver
 */
package jmidi.gui.model;

import java.awt.EventQueue;
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
			this.value = Math.min(maxValue, Math.max(minValue, value));
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
			fireNewValue();
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
			fireNewValue();
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
					fireNewValue();
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
					fireNewValue();
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

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public void fireNewValue() {
		lock.lock();
		try {
			final int newValue = getValue();
			for (final ValueObserver observer : observers) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						observer.valueChanged(newValue);
					}
				});
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
