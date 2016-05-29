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
package jaccompaniment.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import jaccompaniment.resource.Beat.BeatListener;
import jaccompaniment.ui.ValuePanel.ValueObserver;

/**
 * Panel for editing loop Pattern and velocities
 * 
 * @author oliver
 */
@SuppressWarnings({ "serial", "PMD.AvoidInstantiatingObjectsInLoops" })
public class LoopPanel extends JPanel implements BeatListener {
	public static interface Instrument {
		@Override
		public String toString();

		public String name();
	}

	private final Map<Instrument, PatternPanel> patternMap = new HashMap<>();
	private final Map<Instrument, ValuePanel> velocityMap = new HashMap<>();
	private final List<ActionListener> listeners = new ArrayList<>();
	private final Lock lock = new ReentrantLock();
	private final Executor executor = Executors.newSingleThreadExecutor();
	private PatternPanel beatPanel = null;

	public LoopPanel(final Instrument set[]) {
		super();
		final GridBagLayout gbl_loopPanel = new GridBagLayout();
		super.setLayout(gbl_loopPanel);
		super.setBackground(Color.DARK_GRAY);
		for (int i = 0; i < set.length; i++) {
			final ValuePanel velocityPanel = new ValuePanel();
			final GridBagConstraints gbc_velocityPanel = new GridBagConstraints();
			gbc_velocityPanel.insets = new Insets(0, 0, 0, 5);
			gbc_velocityPanel.fill = GridBagConstraints.HORIZONTAL;
			gbc_velocityPanel.gridx = 0;
			gbc_velocityPanel.gridy = i;
			gbc_velocityPanel.weighty = 1.0;
			gbc_velocityPanel.weightx = 1.0;
			gbc_velocityPanel.insets = new Insets(5, 0, 5, 5);
			add(velocityPanel, gbc_velocityPanel);
			velocityPanel.setLabel(set[i].toString());
			velocityPanel.setMaxValue(127);
			velocityPanel.setMinValue(1);
			velocityMap.put(set[i], velocityPanel);
			velocityPanel.addValueObserver(new ValueObserver() {
				@Override
				public void valueChanged(final int newValue) {
					fireActionEvent(
							new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "VOLUME CHANGED"));
				}
			});

			final PatternPanel patternPanel = new PatternPanel();
			final GridBagConstraints gbc_patternPanel = new GridBagConstraints();
			gbc_patternPanel.fill = GridBagConstraints.HORIZONTAL;
			gbc_patternPanel.gridx = 1;
			gbc_patternPanel.gridy = i;
			gbc_patternPanel.weighty = 1.0;
			gbc_patternPanel.weightx = 100.0;
			gbc_patternPanel.insets = new Insets(5, 0, 5, 5);
			add(patternPanel, gbc_patternPanel);
			patternMap.put(set[i], patternPanel);
			patternPanel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					fireActionEvent(
							new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "PATTERN CHANGED"));
				}
			});
			if (i == 0) {
				beatPanel = patternPanel;
			}
		}
	}

	public void setNumberOfQuarter(final int quarters) {
		for (final Instrument instrument : patternMap.keySet()) {
			final PatternPanel panel = patternMap.get(instrument);
			final String currentPattern = panel.getPattern();
			String newPattern = currentPattern;

			if (currentPattern.length() < quarters * 4) {
				final char newPatternChar[] = new char[quarters * 4];
				System.arraycopy(currentPattern.toCharArray(), 0, newPatternChar, 0,
						currentPattern.length());
				Arrays.fill(newPatternChar, currentPattern.length(), newPatternChar.length, ' ');
				newPattern = new String(newPatternChar);
			} else if (currentPattern.length() > quarters * 4) {
				newPattern = currentPattern.substring(0, quarters * 4);
			}
			panel.setPattern(newPattern);
		}
		fireActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "PATTERN CHANGED"));
	}

	public int getNumberOfQuarter() {
		int quarterCount = 1;
		boolean resetQuarterCount = false;
		boolean first = true;

		for (final Instrument instrument : patternMap.keySet()) {
			final PatternPanel panel = patternMap.get(instrument);
			final int panelQuarterCount = panel.getPattern().length() / 4;
			if (first) {
				first = false;
			} else if (quarterCount != panelQuarterCount) {
				resetQuarterCount = true;
			}
			quarterCount = Math.max(panelQuarterCount, quarterCount);
		}
		if (resetQuarterCount) {
			setNumberOfQuarter(quarterCount);
		}
		return quarterCount;
	}

	public String getPattern(final Instrument instrument) {
		final PatternPanel panel = patternMap.get(instrument);
		if (panel == null) {
			return null;
		} else {
			return panel.getPattern();
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
		setVelocity(instrument, velocity, true);
	}

	public void setVelocity(final Instrument instrument, final int velocity,
			final boolean fireUpdate) {
		final ValuePanel panel = velocityMap.get(instrument);
		if (panel != null) {
			panel.setValue(velocity, fireUpdate);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see jaccompaniment.resource.Beat.BeatListener#nextBeat(int)
	 */
	@Override
	public void nextBeat(final int beat) {
		lock.lock();
		try {
			if (beatPanel != null) {
				beatPanel.nextBeat(beat);
			}
		} finally {
			lock.unlock();
		}

	}
}
