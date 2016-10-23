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
import java.awt.Graphics;

import jmidi.gui.Component;

/**
 * Panel for editing a single loop Pattern
 * 
 * @author oliver
 */
@SuppressWarnings("serial")
public class PatternPanel extends Component {
	private String pattern = "                ";
	public static final int patHeight = 26;
	public int patWidth = 25;
	public int taktDistance = patWidth;
	public int taktWidth = 4 * patWidth + taktDistance;
	private int beat = -1;

	private int quarterPerPage = 4;
	private int numberOfPages = 1;
	private int currentPage = 0;
	private int quarterDivision = 4;

	public PatternPanel() {
		super();
		super.setBackground(Color.DARK_GRAY);
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
			checkPatternLength();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(final Graphics g) {
		final int width = getWidth();
		final int numberOfFields = quarterPerPage * quarterDivision + quarterPerPage - 1;
		patWidth = width / numberOfFields;
		taktWidth = patWidth * (quarterDivision + 1);

		super.paintComponent(g);
		for (int i = 0; i < quarterPerPage * quarterDivision; i++) {
			final int offset = patWidth * (i / quarterDivision);
			final int step = i + currentPage * quarterPerPage * quarterDivision;
			final boolean beatBox = beat >= 0
					&& beat % (quarterPerPage * quarterDivision * numberOfPages) == step;

			final Color inactiveColor = beatBox ? Color.LIGHT_GRAY : Color.GRAY;
			final Color activeColor = beatBox ? Color.WHITE : Color.GREEN;
			if (pattern.charAt(step) == ' ') {
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
		return new Dimension(taktWidth * quarterPerPage, patHeight);
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
		final int subTakt = (x % taktWidth) / patWidth;

		final int pos = takt * quarterDivision + subTakt
				+ currentPage * quarterPerPage * quarterDivision;
		String insert = " ";
		if (pos >= 0 && pos < pattern.length() && 0 <= subTakt && subTakt < quarterDivision) {
			if (pattern.charAt(pos) == ' ') {
				insert = "X";
			}
			pattern = pattern.substring(0, pos) + insert + pattern.substring(pos + 1);
			repaint();
		}
		repaint();
		firePropertyChange("pattern", null, pattern);
	}

	private void checkPatternLength() {
		while (pattern.length() < numberOfPages * quarterPerPage * quarterDivision) {
			pattern = pattern + " ";
		}
	}

	/**
	 * @return the quaterPerPage
	 */
	public int getQuarterPerPage() {
		return quarterPerPage;
	}

	/**
	 * @param quaterPerPage
	 *            the quaterPerPage to set
	 */
	public void setQuaterPerPage(final int quaterPerPage) {
		quarterPerPage = quaterPerPage;
		checkPatternLength();
	}

	/**
	 * @return the numberOfPages
	 */
	public int getNumberOfPages() {
		return numberOfPages;
	}

	/**
	 * @param numberOfPages
	 *            the numberOfPages to set
	 */
	public void setNumberOfPages(final int numberOfPages) {
		this.numberOfPages = numberOfPages;
		checkPatternLength();
	}

	/**
	 * @return the currentPage
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * @param currentPage
	 *            the currentPage to set
	 */
	public void setCurrentPage(final int currentPage) {
		this.currentPage = currentPage;
	}

	/**
	 * @return the quarterDivision
	 */
	public int getQuarterDivision() {
		return quarterDivision;
	}

	/**
	 * @param quarterDivision
	 *            the quarterDivision to set
	 */
	public void setQuarterDivision(final int quarterDivision) {
		this.quarterDivision = quarterDivision;
		checkPatternLength();
	}
}
