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
package jmidi.gui.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import jmidi.gui.Component;
import jmidi.gui.model.IntegerModel;
import jmidi.gui.model.IntegerModel.ValueObserver;

/**
 * Panel for editing an integer having an integrated label
 * 
 * @author oliver
 */
@SuppressWarnings("serial")
public class ValuePanel extends Component {
	private String label = "Velocity";
	public static final int labelWidth = 200;
	public static final int valueWidth = 80;
	public static final int width = labelWidth + valueWidth;
	public static final int height = 26;
	private final IntegerModel model;
	private int pressValue;

	public ValuePanel() {
		super();
		super.setBackground(Color.DARK_GRAY);
		model = new IntegerModel(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
		model.addValueObserver(new ValueObserver() {

			@Override
			public void valueChanged(final int newValue) {
				repaint();
			}
		});
	}

	public ValuePanel(final IntegerModel model) {
		super();
		super.setBackground(Color.DARK_GRAY);
		this.model = model;
		model.addValueObserver(new ValueObserver() {

			@Override
			public void valueChanged(final int newValue) {
				repaint();
			}
		});
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public int getValue() {
		return model.getValue();
	}

	public void setValue(final int value) {
		model.setValue(value);
	}

	public void setMinValue(final int minValue) {
		model.setMinValue(minValue);
	}

	public int getMinValue() {
		return model.getMinValue();
	}

	public void setMaxValue(final int maxValue) {
		model.setMaxValue(maxValue);
	}

	public int getMaxValue() {
		return model.getMaxValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(final Graphics g) {
		final String value = Integer.toString(getValue());
		super.paintComponent(g);
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, labelWidth, height);
		g.setFont(sf);
		g.setColor(Color.GREEN);
		final FontMetrics fm = g.getFontMetrics();
		Rectangle2D r = fm.getStringBounds(label, g);
		final int y = (height - (int) r.getHeight()) / 2 + fm.getAscent();
		g.drawString(label, 5, y);
		g.setColor(Color.DARK_GRAY);
		g.fill3DRect(labelWidth, 0, valueWidth, height, false);
		g.setColor(Color.YELLOW);
		r = fm.getStringBounds(value, g);
		final int x = labelWidth + valueWidth - 5 - (int) r.getWidth();
		g.drawString(value, x, y);
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

	public void addValueObserver(final ValueObserver observer) {
		model.addValueObserver(observer);
	}

	public void removeValueObserver(final ValueObserver observer) {
		model.removeValueObserver(observer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jmidi.gui.Component#mousePressed(int, int)
	 */
	@Override
	public void mousePressed(final int x, final int y) {
		pressValue = getValue();
		if (x > labelWidth && x < width) {
			if (x - labelWidth > valueWidth / 2) {
				model.increment();
				model.startIncrementing();
			} else {
				model.decrement();
				model.startDecrementing();
			}
			repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jmidi.gui.Component#mouseReleased(int, int)
	 */
	@Override
	public void mouseReleased(final int x, final int y) {
		model.stopIncrementing();
		model.stopDecrementing();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jmidi.gui.Component#mouseDragged(int, int, int, int)
	 */
	@Override
	public void mouseDragged(final int origin_x, final int origin_y, final int x, final int y) {
		if (x < labelWidth + valueWidth || x > width) {
			setValue(pressValue - origin_x + x - origin_y + y);
			model.fireNewValue();
		}
		model.stopIncrementing();
		model.stopDecrementing();
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jmidi.gui.Component#mouseWheelEvent(int)
	 */
	@Override
	public void mouseWheelEvent(final int steps) {
		if (steps < 0) {
			model.increment();
		} else {
			model.decrement();
		}
		repaint();
	}
}
