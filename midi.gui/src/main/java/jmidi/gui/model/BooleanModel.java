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
