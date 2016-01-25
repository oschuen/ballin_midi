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
 * @since 14.11.2015
 * @version 1.0
 * @author oliver
 */
package jaccompaniment.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JPanel;

/**
 * Panel for editing an integer having an integrated label
 * 
 * @author oliver
 */
@SuppressWarnings("serial")
public class ValuePanel extends JPanel {
	private int maxValue = Integer.MAX_VALUE;
	private int minValue = Integer.MIN_VALUE;
	private static final int REPEAT_TIME = 250;
	private String label = "Velocity";
	private int value = 127;
	private Future<?> stopIncrement = null;
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	public static final int labelWidth = 200;
	public static final int valueWidth = 80;
	public static final int buttonWidth = 20;
	public static final int width = labelWidth + valueWidth + buttonWidth;
	private static final int height = 30;
	private final Lock lock = new ReentrantLock();
	private final List<ValueObserver> observers = new ArrayList<>();

	public ValuePanel() {
		super();
		value = 127;
		super.setBackground(Color.DARK_GRAY);
		final MouseAdapter adapter = new MouseAdapter() {
			int pressX = 0;
			int pressY = 0;
			int pressValue = 0;

			@Override
			public void mousePressed(final MouseEvent e) {
				pressX = e.getX();
				pressY = e.getY();
				pressValue = getValue();
				if (e.getX() > labelWidth + valueWidth && e.getX() < width) {
					if (e.getY() < height / 2) {
						setValue(getValue() + 1);
						stopIncrement = executor.scheduleWithFixedDelay(new Runnable() {
							int addNumber = 0;

							@Override
							public void run() {
								if (addNumber < 5) {
									setValue(getValue() + 1);
								} else {
									setValue(getValue() + 5);
								}
								addNumber++;
								repaint();
							}
						}, 2 * REPEAT_TIME, REPEAT_TIME, TimeUnit.MILLISECONDS);
						repaint();
					} else {
						setValue(getValue() - 1);
						stopIncrement = executor.scheduleWithFixedDelay(new Runnable() {
							int addNumber = 0;

							@Override
							public void run() {
								if (addNumber < 5) {
									setValue(getValue() - 1);
								} else {
									setValue(getValue() - 5);
								}
								addNumber++;
								repaint();
							}
						}, 2 * REPEAT_TIME, REPEAT_TIME, TimeUnit.MILLISECONDS);
						repaint();
					}
				}
			};

			@Override
			public void mouseDragged(final MouseEvent e) {
				if (e.getX() < labelWidth + valueWidth || e.getX() > width) {
					setValue(pressValue - pressX + e.getX() - pressY + e.getY());
				}

			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if (stopIncrement != null) {
					stopIncrement.cancel(false);
				}
			}
		};
		this.addMouseListener(adapter);
		this.addMouseMotionListener(adapter);
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public int getValue() {
		return value;
	}

	public void setValue(final int value) {
		final int temp = Math.max(Math.min(maxValue, value), minValue);
		if (temp != this.value) {
			this.value = temp;
			fireNewValue(this.value);
			repaint();
		}
	}

	public void setMinValue(final int minValue) {
		this.minValue = minValue;
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMaxValue(final int maxValue) {
		this.maxValue = maxValue;
	}

	public int getMaxValue() {
		return maxValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(final Graphics g) {
		final String value = Integer.toString(getValue());
		super.paint(g);
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, labelWidth, height);
		g.setFont(new Font("Arial Black", Font.PLAIN, 20));
		g.setColor(Color.GREEN);
		final FontMetrics fm = g.getFontMetrics();
		Rectangle2D r = fm.getStringBounds(label, g);
		int y = (height - (int) r.getHeight()) / 2 + fm.getAscent();
		g.drawString(label, 5, y);
		g.setColor(Color.DARK_GRAY);
		g.fill3DRect(labelWidth, 0, valueWidth, height, false);
		g.setColor(Color.YELLOW);
		r = fm.getStringBounds(value, g);
		int x = labelWidth + valueWidth - 5 - (int) r.getWidth();
		g.drawString(value, x, y);
		g.setColor(Color.DARK_GRAY);
		g.fill3DRect(labelWidth + valueWidth, 0, buttonWidth, height / 2, false);
		g.fill3DRect(labelWidth + valueWidth, height / 2 + 1, buttonWidth, height, false);
		r = fm.getStringBounds("+", g);
		x = labelWidth + valueWidth + (buttonWidth - (int) r.getWidth()) / 2;
		y = (height / 2 - (int) r.getHeight()) / 2 + fm.getAscent();
		g.setColor(Color.WHITE);
		g.drawString("+", x, y);
		r = fm.getStringBounds("-", g);
		x = labelWidth + valueWidth + (buttonWidth - (int) r.getWidth()) / 2;
		y = height / 2 + (height / 2 - (int) r.getHeight()) / 2 + fm.getAscent();
		g.setColor(Color.WHITE);
		g.drawString("-", x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	private void fireNewValue(final int newValue) {
		lock.lock();
		try {
			for (final ValueObserver observer : observers) {
				executor.execute(new Runnable() {
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

	public interface ValueObserver {
		void valueChanged(int newValue);
	}
}
