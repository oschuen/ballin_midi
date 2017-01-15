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
package jmidi.gui.widget;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jmidi.gui.Component;

/**
 * @author oliver
 *
 */
@SuppressWarnings("serial")
public class TriggerButton extends Component {

	private String label = "Velocity";
	private boolean pressed = false;
	private final List<ActionListener> listeners = new ArrayList<>();
	private final Lock lock = new ReentrantLock();
	private Color textColor = Color.GREEN;

	/*
	 * (non-Javadoc)
	 * 
	 * @see jmidi.gui.Component#mousePressed(int, int)
	 */
	@Override
	public void mousePressed(final int x, final int y) {
		pressed = true;
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jmidi.gui.Component#mouseReleased(int, int)
	 */
	@Override
	public void mouseReleased(final int x, final int y) {
		pressed = false;
		fireActionEvent(new ActionEvent(this, 1, label));
		repaint();

	}

	public void addActionListener(final ActionListener listener) {
		lock.lock();
		try {
			listeners.add(listener);
		} finally {
			lock.unlock();
		}
	}

	public void removeActionListener(final ActionListener listener) {
		lock.lock();
		try {
			listeners.remove(listener);
		} finally {
			lock.unlock();
		}
	}

	public void fireActionEvent(final ActionEvent event) {
		lock.lock();
		try {
			for (final ActionListener listener : listeners) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						listener.actionPerformed(event);
					}
				});
			}
		} finally {
			lock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(final Graphics g) {
		final int height = getHeight();
		final int width = getWidth();

		if (pressed) {
			g.setColor(Color.BLACK);
		} else {
			g.setColor(Color.DARK_GRAY);
		}
		g.fillRect(0, 0, width - 1, height - 1);

		if (pressed) {
			g.setColor(Color.LIGHT_GRAY);
		} else {
			g.setColor(Color.BLACK);
		}
		g.drawRect(0, 0, width - 1, height - 1);
		g.drawRect(1, 1, width - 3, height - 3);
		g.setColor(Color.WHITE);
		if (pressed) {
			g.drawLine(width - 1, 0, width - 1, height - 1);
			g.drawLine(width - 2, 1, width - 2, height - 2);
			g.drawLine(0, height - 1, width - 1, height - 1);
			g.drawLine(1, height - 2, width - 2, height - 2);
		} else {
			g.drawLine(0, 0, width - 1, 0);
			g.drawLine(1, 1, width - 2, 1);
			g.drawLine(0, 0, 0, height - 1);
			g.drawLine(1, 1, 1, height - 2);
		}

		g.setFont(new Font("Arial Black", Font.PLAIN, 20));
		g.setColor(textColor);
		final FontMetrics fm = g.getFontMetrics();
		final Rectangle2D r = fm.getStringBounds(label, g);
		final int y = (height - (int) r.getHeight()) / 2 + fm.getAscent();
		g.drawString(label, 5, y);
	}

	/**
	 * @param textColor
	 *            the textColor to set
	 */
	public void setTextColor(final Color textColor) {
		this.textColor = textColor;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(final String label) {
		this.label = label;
	}
}
