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
package jmidi.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jmidi.gui.widget.PageSelector;

/**
 * @author oliver
 *
 */
@SuppressWarnings("serial")
public class TestFrame extends JFrame {
	final PageSelector panel = new PageSelector();

	public TestFrame() {
		getContentPane().setBackground(Color.BLUE);
		getContentPane().setLayout(null);
		getContentPane().add(panel);

		panel.setBounds(50, 50, 600, 65);
	}

	private static final Logger logger = LogManager.getLogger(TestFrame.class);

	public static void main(final String[] args) {
		final TestFrame frame = new TestFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {

					frame.setSize(new Dimension(800, 600));
					frame.setVisible(true);
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		});
	}
}
