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

import jaccompaniment.resource.Beat.BeatListener;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JPanel;

/**
 * Panel for editing a single loop Pattern
 * 
 * @author oliver
 */
@SuppressWarnings("serial")
public class PatternPanel extends JPanel implements BeatListener {
	private String pattern = "                ";
	public static final int patHeight = 30;
	public static final int patWidth = 25;
	public static int taktDistance = 15;
	public static int taktWidth = 4 * patWidth + taktDistance;
	private final List<ActionListener> listeners = new ArrayList<>();
	private final Lock lock = new ReentrantLock();
	private final Executor executor = Executors.newSingleThreadExecutor();
	private int beat = -1;

	public PatternPanel() {
		super();
		super.setBackground(Color.DARK_GRAY);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				final int x = e.getPoint().x;
				final int takt = x / taktWidth;

				final int pos = takt * 4 + (e.getPoint().x - takt * taktWidth) / patWidth;
				String insert = " ";
				if (pos >= 0 && pos < pattern.length()) {
					if (pattern.charAt(pos) == ' ') {
						insert = "X";
					}
					pattern = pattern.substring(0, pos) + insert + pattern.substring(pos + 1);
					repaint();
				}
				fireActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, pattern));
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				// Nothing happens when mouse is released
			}
		});

	}

	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	public void setPattern(final String pattern) {
		if (!this.pattern.equals(pattern)) {
			this.pattern = pattern;
			revalidate();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		for (int i = 0; i < pattern.length(); i++) {
			final int offset = taktDistance * (i / 4);
			final boolean beatBox = beat >= 0 && beat % pattern.length() == i;
			final Color inactiveColor = beatBox ? Color.LIGHT_GRAY : Color.GRAY;
			final Color activeColor = beatBox ? Color.WHITE : Color.GREEN;
			if (pattern.charAt(i) == ' ') {
				g.setColor(inactiveColor);
				g.fill3DRect(offset + i * patWidth, 0, patWidth, getHeight(), false);
			} else {
				g.setColor(activeColor);
				g.fill3DRect(offset + i * patWidth, 0, patWidth, getHeight(), true);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(patWidth * pattern.length() + taktDistance * (pattern.length() / 4),
				patHeight);
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

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	private void fireActionEvent(final ActionEvent event) {
		lock.lock();
		try {
			for (final ActionListener listener : listeners) {
				executor.execute(new Runnable() {

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
	 * @see jaccompaniment.resource.Beat.BeatListener#nextBeat(int)
	 */
	@Override
	public void nextBeat(final int beat) {
		this.beat = beat;
		repaint();
	}
}
