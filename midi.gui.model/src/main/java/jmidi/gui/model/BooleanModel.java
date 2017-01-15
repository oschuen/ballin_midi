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
 * @since 08.10.2016
 * @version 1.0
 * @author oliver
 */
package jmidi.gui.model;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author oliver
 *
 */
public class BooleanModel {
	boolean value = false;
	private final Lock lock = new ReentrantLock();
	private final List<ValueObserver> observers = new ArrayList<>();

	public BooleanModel() {
		super();
	}

	public BooleanModel(final boolean value) {
		super();
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public boolean isValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(final boolean value) {
		this.value = value;
	}

	public void toggle() {
		lock.lock();
		try {
			value = !value;
			fireNewValue();
		} finally {
			lock.unlock();
		}
	}

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public void fireNewValue() {
		lock.lock();
		try {
			final boolean newValue = isValue();
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

	public static interface ValueObserver {
		void valueChanged(boolean newValue);
	}
}
