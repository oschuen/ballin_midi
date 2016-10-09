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
 * @since 09.10.2016
 * @version 1.0
 * @author oliver
 */
package jmidi.gui.widget;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import jmidi.gui.Component;

/**
 * @author oliver
 *
 */
@SuppressWarnings("serial")
public class PageSelector extends Component {

	public PageSelector() {
		super();
		setBackground(Color.DARK_GRAY);
	}

	private int numberOfPages = 5;
	private int currentPage = 0;
	private String prefix = "F";
	private static final int tabWidth = 50;

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
		this.numberOfPages = Math.max(1, numberOfPages);
		setCurrentPage(currentPage);
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
		this.currentPage = Math.max(0, Math.min(currentPage, numberOfPages - 1));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		g.setFont(sf);
		final int height = getHeight();
		final FontMetrics fm = g.getFontMetrics();
		g.setColor(Color.BLACK);
		g.fillPolygon(new int[] { 6 + currentPage * tabWidth, (currentPage + 1) * tabWidth,
				(currentPage + 1) * tabWidth, currentPage * tabWidth, currentPage * tabWidth },
				new int[] { 0, 0, height - 1, height - 1, 6 }, 5);
		for (int i = 0; i < numberOfPages; ++i) {
			final String label = prefix + (i + 1);
			final Rectangle2D r = fm.getStringBounds(label, g);
			final int x = i * tabWidth + (int) (tabWidth - r.getWidth()) / 2;
			final int y = (height - (int) r.getHeight()) / 2 + fm.getAscent();
			g.setColor(Color.WHITE);
			g.drawString(label, x, y);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jmidi.gui.Component#mousePressed(int, int)
	 */
	@Override
	public void mousePressed(final int x, final int y) {
		final int oldPage = getCurrentPage();
		setCurrentPage(x / tabWidth);

		if (oldPage != getCurrentPage()) {
			repaint();
			firePropertyChange("currentPage", oldPage, getCurrentPage());
		}

	}

	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * @param prefix
	 *            the prefix to set
	 */
	public void setPrefix(final String prefix) {
		this.prefix = prefix;
	}

}
