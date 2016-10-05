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
package jmidi.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * Panel for editing a single loop Pattern
 * 
 * @author oliver
 */
@SuppressWarnings("serial")
public class PatternPanel extends Component {
	private String pattern = "                ";
	public static final int patHeight = 30;
	public static final int patWidth = 25;
	public static int taktDistance = 15;
	public static int taktWidth = 4 * patWidth + taktDistance;
	private int beat = -1;

	public PatternPanel() {
		super();
		super.setBackground(Color.DARK_GRAY);
		setDoubleBuffered(true);
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
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
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

	public void setBeat(final int beat) {
		this.beat = beat;
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jmidi.gui.Component#mousePressed(int, int)
	 */
	@Override
	public void mousePressed(final int x, final int y) {
		final int takt = x / taktWidth;

		final int pos = takt * 4 + (x - takt * taktWidth) / patWidth;
		String insert = " ";
		if (pos >= 0 && pos < pattern.length()) {
			if (pattern.charAt(pos) == ' ') {
				insert = "X";
			}
			pattern = pattern.substring(0, pos) + insert + pattern.substring(pos + 1);
			repaint();
		}
		repaint();
		firePropertyChange("pattern", null, pattern);
	}
}
