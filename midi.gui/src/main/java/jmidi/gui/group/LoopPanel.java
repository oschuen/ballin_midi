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
 * @since 18.11.2015
 * @version 1.0
 * @author oliver
 */
package jmidi.gui.group;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JPanel;

import jmidi.gui.model.IntegerModel.ValueObserver;
import jmidi.gui.widget.PageSelector;
import jmidi.gui.widget.PatternPanel;
import jmidi.gui.widget.ValuePanel;

/**
 * Panel for editing loop Pattern and velocities
 * 
 * @author oliver
 */
@SuppressWarnings({ "serial", "PMD.AvoidInstantiatingObjectsInLoops" })
public class LoopPanel extends JPanel {
	public static interface Instrument {
		@Override
		public String toString();

		public String name();
	}

	public static final Instrument ACCENT = new Instrument() {

		@Override
		public String name() {
			return "ACCENT";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Accent";
		}
	};

	private final Map<Instrument, PatternPanel> patternMap = new HashMap<>();
	private final Map<Instrument, ValuePanel> velocityMap = new HashMap<>();
	private final List<ActionListener> listeners = new ArrayList<>();
	private final Lock lock = new ReentrantLock();
	private final Executor executor = Executors.newSingleThreadExecutor();
	private PatternPanel beatPanel = null;
	private int numberOfPages = 1;
	private int quarterPerPage = 4;
	private int quarterDivision = 4;
	private final PageSelector pageSelector;
	private static final int width = 900;
	private final int height;
	private static final int pageSelHeight = 20;
	private static final int margin = 3;

	public LoopPanel(final Instrument set[]) {
		super();
		int i = 0;
		final List<Instrument> instruments = new ArrayList<>();
		instruments.add(ACCENT);
		instruments.addAll(Arrays.asList(set));
		super.setLayout(null);
		super.setBackground(Color.DARK_GRAY);
		height = 3 * margin + pageSelHeight + (margin + ValuePanel.height) * instruments.size();
		pageSelector = new PageSelector();
		pageSelector.setPrefix("P");
		pageSelector.setNumberOfPages(numberOfPages);
		pageSelector.setCurrentPage(0);
		pageSelector.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				setCurrentPage(pageSelector.getCurrentPage());
				repaint();
			}
		});
		pageSelector.setBounds(margin, margin, width - 2 * margin, pageSelHeight);
		add(pageSelector);

		for (final Instrument instrument : instruments) {
			final ValuePanel velocityPanel = new ValuePanel();
			velocityPanel.setBounds(margin,
					2 * margin + pageSelHeight + i * (ValuePanel.height + margin), ValuePanel.width,
					ValuePanel.height);
			velocityPanel.setLabel(instrument.toString());
			velocityPanel.setMaxValue(127);
			velocityPanel.setMinValue(1);
			velocityPanel.setValue(100);
			add(velocityPanel);
			velocityMap.put(instrument, velocityPanel);
			velocityPanel.addValueObserver(new ValueObserver() {
				@Override
				public void valueChanged(final int newValue) {
					fireActionEvent(
							new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "VOLUME CHANGED"));
				}
			});

			final PatternPanel patternPanel = new PatternPanel();
			patternPanel.setBounds(2 * margin + ValuePanel.width,
					2 * margin + pageSelHeight + i * (ValuePanel.height + margin),
					width - 3 * margin - ValuePanel.width, PatternPanel.patHeight);
			add(patternPanel);
			patternMap.put(instrument, patternPanel);
			patternPanel.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(final PropertyChangeEvent evt) {
					fireActionEvent(
							new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "PATTERN CHANGED"));
				}
			});
			if (i == 0) {
				beatPanel = patternPanel;
			}
			++i;
		}
	}

	public String getPattern(final Instrument instrument) {
		final PatternPanel panel = patternMap.get(instrument);
		if (panel == null) {
			return null;
		} else {
			return panel.getPattern().substring(0,
					numberOfPages * quarterPerPage * quarterDivision);
		}
	}

	public int getVelocity(final Instrument instrument) {
		final ValuePanel panel = velocityMap.get(instrument);
		if (panel == null) {
			return 0;
		} else {
			return panel.getValue();
		}
	}

	public void setPattern(final Instrument instrument, final String pattern) {
		final PatternPanel panel = patternMap.get(instrument);
		if (panel != null) {
			panel.setPattern(pattern);
		}
	}

	public void setVelocity(final Instrument instrument, final int velocity) {
		final ValuePanel panel = velocityMap.get(instrument);
		if (panel != null) {
			panel.setValue(velocity);
		}
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

	public void nextBeat(final int beat) {
		lock.lock();
		try {
			final int currentPage = pageSelector.getCurrentPage();

			final int patternPos = beat % (numberOfPages * quarterDivision * quarterPerPage);
			final int page = patternPos / quarterDivision / quarterPerPage;
			if (currentPage != page) {
				setCurrentPage(page);
				beatPanel.setBeat(beat);
				repaint();
			} else {
				beatPanel.setBeat(beat);
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @return the quarterPerPage
	 */
	public int getQuarterPerPage() {
		int quarterCount = 1;
		boolean resetQuarterCount = false;
		boolean first = true;

		for (final Instrument instrument : patternMap.keySet()) {
			final PatternPanel panel = patternMap.get(instrument);
			final int panelQuarterCount = panel.getQuarterPerPage();
			if (first) {
				first = false;
			} else if (quarterCount != panelQuarterCount) {
				resetQuarterCount = true;
			}
			quarterCount = Math.max(panelQuarterCount, quarterCount);
		}
		if (resetQuarterCount) {
			setQuarterPerPage(quarterCount);
		}
		return quarterCount;
	}

	/**
	 * @param quarterPerPage
	 *            the quaterPerPage to set
	 */
	public void setQuarterPerPage(final int quarterPerPage) {
		for (final Instrument instrument : patternMap.keySet()) {
			final PatternPanel panel = patternMap.get(instrument);
			panel.setQuaterPerPage(quarterPerPage);
		}
		this.quarterPerPage = quarterPerPage;
	}

	/**
	 * @return the numberOfPages
	 */
	public int getNumberOfPages() {
		int numberOfPages = 1;
		boolean resetNumberOfPages = false;
		boolean first = true;

		for (final Instrument instrument : patternMap.keySet()) {
			final PatternPanel panel = patternMap.get(instrument);
			final int panelNumberOfPages = panel.getNumberOfPages();
			if (first) {
				first = false;
			} else if (numberOfPages != panelNumberOfPages) {
				resetNumberOfPages = true;
			}
			numberOfPages = Math.max(panelNumberOfPages, numberOfPages);
		}
		if (resetNumberOfPages) {
			setNumberOfPages(numberOfPages);
		}
		return numberOfPages;
	}

	/**
	 * @param numberOfPages
	 *            the numberOfPages to set
	 */
	public void setNumberOfPages(final int numberOfPages) {
		for (final Instrument instrument : patternMap.keySet()) {
			final PatternPanel panel = patternMap.get(instrument);
			panel.setNumberOfPages(numberOfPages);
		}
		this.numberOfPages = numberOfPages;
		pageSelector.setNumberOfPages(numberOfPages);
	}

	/**
	 * @return the currentPage
	 */
	public int getCurrentPage() {
		int currentPage = 0;
		boolean resetCurrentPage = false;
		boolean first = true;

		for (final Instrument instrument : patternMap.keySet()) {
			final PatternPanel panel = patternMap.get(instrument);
			final int panelCurrentPage = panel.getCurrentPage();
			if (first) {
				first = false;
			} else if (currentPage != panelCurrentPage) {
				resetCurrentPage = true;
			}
			currentPage = Math.max(panelCurrentPage, currentPage);
		}
		if (resetCurrentPage) {
			setCurrentPage(currentPage);
		}
		return currentPage;
	}

	/**
	 * @param currentPage
	 *            the currentPage to set
	 */
	public void setCurrentPage(final int currentPage) {
		for (final Instrument instrument : patternMap.keySet()) {
			final PatternPanel panel = patternMap.get(instrument);
			panel.setCurrentPage(currentPage);
		}
		pageSelector.setCurrentPage(currentPage);
	}

	/**
	 * @return the quarterDivision
	 */
	public int getQuarterDivision() {
		int quarterDivision = 0;
		boolean resetQuarterDivision = false;
		boolean first = true;

		for (final Instrument instrument : patternMap.keySet()) {
			final PatternPanel panel = patternMap.get(instrument);
			final int panelQuarterDivision = panel.getQuarterDivision();
			if (first) {
				first = false;
			} else if (quarterDivision != panelQuarterDivision) {
				resetQuarterDivision = true;
			}
			quarterDivision = Math.max(panelQuarterDivision, quarterDivision);
		}
		if (resetQuarterDivision) {
			setQuarterDivision(quarterDivision);
		}
		return quarterDivision;
	}

	/**
	 * @param quarterDivision
	 *            the quarterDivision to set
	 */
	public void setQuarterDivision(final int quarterDivision) {
		for (final Instrument instrument : patternMap.keySet()) {
			final PatternPanel panel = patternMap.get(instrument);
			panel.setQuarterDivision(quarterDivision);
		}
		this.quarterDivision = quarterDivision;
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
}
