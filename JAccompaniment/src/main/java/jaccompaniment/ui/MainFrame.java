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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jaccompaniment.accompaniment.Guitar;
import jaccompaniment.accompaniment.Percussion;
import jaccompaniment.chord.ChordRecognizer;
import jaccompaniment.chord.ChordRecognizer.ChordListener;
import jaccompaniment.filter.MidiThroughFilter;
import jaccompaniment.resource.Beat;
import jaccompaniment.resource.Beat.BeatListener;
import jmidi.gui.group.LoopPanel;
import jmidi.gui.group.LoopPanel.Instrument;
import jmidi.gui.model.IntegerModel.ValueObserver;
import jmidi.gui.widget.TriggerButton;
import jmidi.gui.widget.ValuePanel;
import midi.device.resource.MidiDevices;

/**
 * Main application frame
 * 
 * @author oliver
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	private static final Preferences prefs = Preferences.userNodeForPackage(ConfigDialog.class);

	private static final int panelCount = 5;
	private Guitar guitar = null;
	private Percussion percussion = null;
	private ChordRecognizer recognizer = null;
	private MidiThroughFilter filter = null;
	private Transmitter recognizerTransmitter = null;
	private Transmitter filterTransmitter = null;
	private BeatListener beatListener = null;
	private final JTabbedPane tabbedPane;
	private final LoopPanel loopPanel[] = new LoopPanel[panelCount];
	private final ValuePanel masterVelocityPanel;
	private final ValuePanel bpmPanel;
	private final ValuePanel quarterPanel;
	private final ValuePanel pagePanel;
	private final ValuePanel divisionPanel;
	private final Beat beat = new Beat();

	private static final String LAST_FILE_KEY = "last file";

	private final static LoopPanel.Instrument percussionInstruments[] = Percussion.PercussionInstrument
			.values();

	private final static LoopPanel.Instrument guitarInstruments[] = Guitar.GuitarInstrument
			.values();

	private final static LoopPanel.Instrument allInstruments[];

	static {
		allInstruments = new LoopPanel.Instrument[percussionInstruments.length
				+ guitarInstruments.length];
		System.arraycopy(percussionInstruments, 0, allInstruments, 0, percussionInstruments.length);
		System.arraycopy(guitarInstruments, 0, allInstruments, percussionInstruments.length,
				guitarInstruments.length);
	}

	private static final Logger logger = LogManager.getLogger(MainFrame.class);

	/**
	 * Create the frame.
	 * 
	 * @throws MidiUnavailableException
	 *             when there is something wrong with the midi system
	 */
	public MainFrame() throws MidiUnavailableException {
		final JPanel contentPane;
		final Font mainFont = new Font("Arial Black", Font.PLAIN, 20);
		setTitle("JAccompaniment");
		UIManager.put("Button.select", Color.BLACK);
		UIManager.put("TabbedPane.foreground", Color.WHITE);
		UIManager.put("TabbedPane.selected", Color.BLACK);
		UIManager.put("TabbedPane.selectHighlight", Color.BLACK);
		UIManager.put("TabbedPane.font", mainFont);
		UIManager.put("TabbedPane.borderHightlightColor", Color.DARK_GRAY);
		UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
		UIManager.put("TabbedPane.darkShadow", Color.DARK_GRAY);
		UIManager.put("TabbedPane.focus", Color.DARK_GRAY);
		UIManager.put("TabbedPane.light", Color.DARK_GRAY);
		// UIManager.put("TabbedPane.highlight", Color.DARK_GRAY);

		UIManager.put("", Color.DARK_GRAY);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setLayout(new BorderLayout(5, 5));

		final JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setBackground(Color.DARK_GRAY);
		panel.setLayout(new GridLayout(0, 3, 5, 5));

		tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
		contentPane.add(tabbedPane);

		for (int i = 0; i < loopPanel.length; ++i) {
			loopPanel[i] = new LoopPanel(allInstruments);
			tabbedPane.add("F" + (1 + i), loopPanel[i]);
			tabbedPane.setBackground(Color.DARK_GRAY);
			tabbedPane.setBackgroundAt(0, Color.DARK_GRAY);

			loopPanel[i].addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					copyLoopPanelInfo();
				}
			});
		}

		tabbedPane.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(final ChangeEvent e) {
				copyLoopPanelInfo();
				repaint();
			}
		});

		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(new KeyEventDispatcher() {

					@Override
					public boolean dispatchKeyEvent(final KeyEvent e) {
						if (e.getID() == KeyEvent.KEY_PRESSED) {
							if (e.getKeyCode() >= KeyEvent.VK_F1
									&& e.getKeyCode() < KeyEvent.VK_F1 + panelCount) {
								tabbedPane.setSelectedIndex(e.getKeyCode() - KeyEvent.VK_F1);
							} else if (e.getKeyCode() == KeyEvent.VK_F9) {
								beat.start();
							} else if (e.getKeyCode() == KeyEvent.VK_F10) {
								beat.stop();
							} else if (e.getKeyCode() == KeyEvent.VK_F11) {
								beat.syncStart();
							} else if (e.getKeyCode() == KeyEvent.VK_F12) {
								guitar.panic();
								percussion.panic();
								filter.panic();
							}
						}
						return false;
					}
				});

		final JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.EAST);
		panel_1.setBackground(Color.DARK_GRAY);
		panel_1.setLayout(new GridLayout(0, 1, 0, 0));

		final TriggerButton btnStart = new TriggerButton();
		btnStart.setLabel("Start");
		btnStart.setPreferredSize(new Dimension(150, 120));
		panel_1.add(btnStart);
		btnStart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				beat.start();
			}
		});

		final TriggerButton btnStop = new TriggerButton();
		btnStop.setLabel("Stop");
		btnStop.setPreferredSize(new Dimension(150, 120));
		btnStop.setTextColor(Color.RED);
		panel_1.add(btnStop);

		btnStop.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				beat.stop();
			}
		});

		final TriggerButton btnSyncStart = new TriggerButton();
		btnSyncStart.setLabel("Sync Start");
		btnSyncStart.setPreferredSize(new Dimension(150, 120));
		panel_1.add(btnSyncStart);
		btnSyncStart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				beat.syncStart();
			}
		});

		final TriggerButton btnPanicStart = new TriggerButton();
		btnPanicStart.setLabel("Panic");
		btnPanicStart.setPreferredSize(new Dimension(150, 120));
		btnPanicStart.setTextColor(Color.RED);
		panel_1.add(btnPanicStart);
		btnPanicStart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				guitar.panic();
				percussion.panic();
				filter.panic();
			}
		});

		final JPanel menuPanel = new JPanel();
		contentPane.add(menuPanel, BorderLayout.NORTH);
		menuPanel.setBackground(Color.DARK_GRAY);
		menuPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		final JButton btnLoad = new JButton();
		btnLoad.setText("Load");
		btnLoad.setFont(new Font("Arial Black", Font.PLAIN, 20));
		btnLoad.setBackground(Color.DARK_GRAY);
		btnLoad.setForeground(Color.GRAY);
		menuPanel.add(btnLoad);
		btnLoad.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser fc = new JFileChooser(new File(prefs.get(LAST_FILE_KEY, "./")));
				final int returnVal = fc.showOpenDialog(MainFrame.this);

				if (returnVal == JFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null) {
					try {
						final FileInputStream fin = new FileInputStream(fc.getSelectedFile());
						final Properties props = new Properties();
						props.load(fin);
						setProperties(props);
						fin.close();
						prefs.put(LAST_FILE_KEY, fc.getSelectedFile().getCanonicalPath());
					} catch (final Exception e1) {
						logger.error(e1.getMessage(), e1);
					}
				}
			}
		});

		final JButton btnSave = new JButton();
		btnSave.setText("Save");
		btnSave.setFont(new Font("Arial Black", Font.PLAIN, 20));
		btnSave.setBackground(Color.DARK_GRAY);
		btnSave.setForeground(Color.GRAY);
		menuPanel.add(btnSave);
		btnSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser fc = new JFileChooser(new File(prefs.get(LAST_FILE_KEY, "./")));
				final int returnVal = fc.showSaveDialog(MainFrame.this);

				if (returnVal == JFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null) {
					try {
						final FileOutputStream fout = new FileOutputStream(fc.getSelectedFile());
						final Properties props = getProperties();
						props.store(fout, "JAccompaniment");
						fout.close();
						prefs.put(LAST_FILE_KEY, fc.getSelectedFile().getCanonicalPath());
					} catch (final Exception e1) {
						logger.error(e1.getMessage(), e1);
					}
				}
			}
		});

		final JButton btnConfig = new JButton();
		btnConfig.setText("Config");
		btnConfig.setFont(new Font("Arial Black", Font.PLAIN, 20));
		btnConfig.setBackground(Color.DARK_GRAY);
		btnConfig.setForeground(Color.GRAY);
		menuPanel.add(btnConfig);
		btnConfig.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					final ConfigDialog dialog = new ConfigDialog(MainFrame.this, true);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setBounds(100, 100, dialog.getPreferredSize().width,
							dialog.getPreferredSize().height);
					dialog.setVisible(true);
					if (dialog.isResult()) {
						setupMidi();
					}
				} catch (final Exception exp) {
					logger.error(exp.getMessage(), exp);
				}
			}
		});

		pagePanel = new ValuePanel();
		panel.add(pagePanel);
		pagePanel.setLabel("Nr. pages");
		pagePanel.setValue(1);
		pagePanel.setMinValue(1);
		pagePanel.setMaxValue(8);

		pagePanel.addValueObserver(new ValueObserver() {

			@Override
			public void valueChanged(final int newValue) {
				for (final LoopPanel loopPanel : MainFrame.this.loopPanel) {
					loopPanel.setNumberOfPages(newValue);
				}
				copyLoopPanelInfo();
			}
		});

		quarterPanel = new ValuePanel();
		panel.add(quarterPanel);
		quarterPanel.setLabel("Nr. quarter");
		quarterPanel.setValue(4);
		quarterPanel.setMinValue(1);
		quarterPanel.setMaxValue(6);

		quarterPanel.addValueObserver(new ValueObserver() {

			@Override
			public void valueChanged(final int newValue) {
				for (final LoopPanel loopPanel : MainFrame.this.loopPanel) {
					loopPanel.setQuarterPerPage(newValue);
				}
				copyLoopPanelInfo();
			}
		});

		divisionPanel = new ValuePanel();
		panel.add(divisionPanel);
		divisionPanel.setLabel("Division");
		divisionPanel.setValue(4);
		divisionPanel.setMinValue(2);
		divisionPanel.setMaxValue(6);

		divisionPanel.addValueObserver(new ValueObserver() {

			@Override
			public void valueChanged(final int newValue) {
				for (final LoopPanel loopPanel : MainFrame.this.loopPanel) {
					loopPanel.setQuarterDivision(newValue);
				}
				copyLoopPanelInfo();
				beat.setDivision(newValue);
			}
		});

		bpmPanel = new ValuePanel();
		panel.add(bpmPanel);
		bpmPanel.setLabel("BPM");
		bpmPanel.setValue(120);
		bpmPanel.setMaxValue(300);
		bpmPanel.setMinValue(10);
		bpmPanel.addValueObserver(new ValueObserver() {

			@Override
			public void valueChanged(final int newValue) {
				beat.setBpM(newValue);
				copyLoopPanelInfo();
			}
		});

		masterVelocityPanel = new ValuePanel();
		panel.add(masterVelocityPanel);
		masterVelocityPanel.setLabel("Master");
		masterVelocityPanel.setValue(100);
		masterVelocityPanel.setMinValue(1);
		masterVelocityPanel.setMaxValue(127);

		masterVelocityPanel.addValueObserver(new ValueObserver() {

			@Override
			public void valueChanged(final int newValue) {
				copyLoopPanelInfo();
			}
		});

		setupMidi();

		pack();
		setBounds(100, 100, getPreferredSize().width, getPreferredSize().height);
	}

	private void setupMidi() throws MidiUnavailableException {

		if (guitar != null) {
			guitar.close();
			guitar = null;
		}
		if (percussion != null) {
			percussion.close();
			percussion = null;
		}

		if (filter != null) {
			filter.close();
			filter = null;
		}

		if (recognizer != null) {
			recognizer.close();
			recognizer = null;
		}

		if (recognizerTransmitter != null) {
			recognizerTransmitter.close();
			recognizerTransmitter = null;
		}

		if (filterTransmitter != null) {
			filterTransmitter.close();
			filterTransmitter = null;
		}

		if (beatListener != null) {
			beat.removeBeatListener(beatListener);
			beatListener = null;
		}

		final MidiDevice guitarDevice = MidiDevices
				.secureGetReceiverDevice(ConfigDialog.getGuitarOutputDevice());
		if (guitarDevice == null) {
			logger.error("Guitar Device not found");
		} else {
			if (!guitarDevice.isOpen()) {
				guitarDevice.open();
			}
			guitar = new Guitar(guitarDevice.getReceiver(), ConfigDialog.getGuitarChannel());
		}
		final MidiDevice percussionDevice = MidiDevices
				.secureGetReceiverDevice(ConfigDialog.getPercussionOutputDevice());
		if (percussionDevice == null) {
			logger.error("Percussion Device not found");
		} else {
			if (!percussionDevice.isOpen()) {
				percussionDevice.open();
			}
			percussion = new Percussion(percussionDevice.getReceiver(),
					ConfigDialog.getPercussionChannel());
		}

		final MidiDevice recognizerDevice = MidiDevices
				.secureGetTransmitterDevice(ConfigDialog.getMidiThroughOutputDevice());
		if (recognizerDevice == null) {
			logger.error("Recognizer Device not found");
		} else {
			if (!recognizerDevice.isOpen()) {
				try {
					recognizerDevice.open();
				} catch (final MidiUnavailableException e1) {
					logger.error("Midi Exception occured for Recognizer", e1);
				}
			}
			recognizer = new ChordRecognizer(new ChordListener() {
				@Override
				public void newChord(final String chord) {
					beat.newChord(chord);
					guitar.newChord(chord);
				}

				@Override
				public void noChord() {
					beat.noChord();
					guitar.noChord();
				}

			});
			recognizerTransmitter = recognizerDevice.getTransmitter();
			recognizerTransmitter.setReceiver(recognizer);
		}

		final MidiDevice midiThroughInputDevice = MidiDevices
				.secureGetTransmitterDevice(ConfigDialog.getMidiThroughInputDevice());
		if (midiThroughInputDevice == null) {
			logger.error("Midi Through Input Device not found");
		} else {
			if (!midiThroughInputDevice.isOpen()) {
				try {
					midiThroughInputDevice.open();
				} catch (final MidiUnavailableException e1) {
					logger.error("Midi Exception occured for Guitar Output", e1);
				}
			}
		}

		final MidiDevice midiThroughOutputDevice = MidiDevices
				.secureGetReceiverDevice(ConfigDialog.getMidiThroughOutputDevice());
		if (midiThroughOutputDevice == null) {
			logger.error("Midi Through Output Device not found");
		} else {
			if (!midiThroughOutputDevice.isOpen()) {
				midiThroughOutputDevice.open();
			}
		}

		beatListener = new BeatListener() {
			@Override
			public void nextBeat(final int beat) {
				try {
					if (percussion != null) {
						percussion.beat(beat);
					}
					if (guitar != null) {
						guitar.beat(beat);
					}
					loopPanel[tabbedPane.getSelectedIndex()].nextBeat(beat);
					Toolkit.getDefaultToolkit().sync();
				} catch (final InvalidMidiDataException e) {
					logger.error(e.getMessage(), e);
				}
			}
		};
		beat.addBeatListener(beatListener);

		if (!(midiThroughInputDevice == null || midiThroughOutputDevice == null)) {
			filter = new MidiThroughFilter(midiThroughOutputDevice.getReceiver());
			filterTransmitter = midiThroughInputDevice.getTransmitter();
			filterTransmitter.setReceiver(filter);
			filter.setFilterChord(ConfigDialog.isFilterChord());
			filter.setMidiThrough(ConfigDialog.isMidiThrough());
		}
		copyLoopPanelInfo();
	}

	private void copyLoopPanelInfo() {
		final int masterVelocity = masterVelocityPanel.getValue();
		final LoopPanel selectedPanel = loopPanel[tabbedPane.getSelectedIndex()];
		final String newPercussionPattern[] = new String[percussionInstruments.length];
		final int newPercussionVelocity[] = new int[percussionInstruments.length];
		final String accent = selectedPanel.getPattern(LoopPanel.ACCENT);
		final int factor = selectedPanel.getVelocity(LoopPanel.ACCENT);

		for (int i = 0; i < newPercussionPattern.length; ++i) {
			newPercussionPattern[i] = selectedPanel.getPattern(percussionInstruments[i]);
			newPercussionVelocity[i] = masterVelocity
					* selectedPanel.getVelocity(percussionInstruments[i]) / 127;
		}
		if (percussion != null) {
			percussion.setPattern(accent, newPercussionPattern);
			percussion.setVelocity(factor, newPercussionVelocity);
		}

		final String newGuitarPattern[] = new String[guitarInstruments.length];
		final int newGuitarVelocity[] = new int[guitarInstruments.length];
		for (int i = 0; i < newGuitarPattern.length; ++i) {
			newGuitarPattern[i] = selectedPanel.getPattern(guitarInstruments[i]);
			newGuitarVelocity[i] = masterVelocity * selectedPanel.getVelocity(guitarInstruments[i])
					/ 127;
		}
		if (guitar != null) {
			guitar.setPattern(newGuitarPattern);
			guitar.setVelocity(newGuitarVelocity);
		}
		repaint();
	}

	private final static String BPM_KEY = "bpm";
	private final static String MASTER_KEY = "master";
	private final static String VELOCITY_KEY = ".velocity";
	private final static String PATTERN_KEY = ".pattern";
	private final static String PAGE_KEY = "page";
	private final static String QUARTER_KEY = "quarter";
	private final static String DIVISION_KEY = "division";

	private Properties getProperties() {
		final Properties props = new Properties();
		props.setProperty(BPM_KEY, Integer.toString(bpmPanel.getValue()));
		props.setProperty(MASTER_KEY, Integer.toString(masterVelocityPanel.getValue()));
		props.setProperty(PAGE_KEY, Integer.toString(pagePanel.getValue()));
		props.setProperty(QUARTER_KEY, Integer.toString(quarterPanel.getValue()));
		props.setProperty(DIVISION_KEY, Integer.toString(divisionPanel.getValue()));

		for (int i = 0; i < loopPanel.length; i++) {
			props.setProperty("P" + i + "_" + LoopPanel.ACCENT.name() + VELOCITY_KEY,
					Integer.toString(loopPanel[i].getVelocity(LoopPanel.ACCENT)));
			props.setProperty("P" + i + "_" + LoopPanel.ACCENT.name() + PATTERN_KEY,
					loopPanel[i].getPattern(LoopPanel.ACCENT));

			for (final Instrument instrument : percussionInstruments) {
				final int velocity = loopPanel[i].getVelocity(instrument);
				final String pattern = loopPanel[i].getPattern(instrument);
				props.setProperty("P" + i + "_" + instrument.name() + VELOCITY_KEY,
						Integer.toString(velocity));
				props.setProperty("P" + i + "_" + instrument.name() + PATTERN_KEY, pattern);
			}

			for (final Instrument instrument : guitarInstruments) {
				final int velocity = loopPanel[i].getVelocity(instrument);
				final String pattern = loopPanel[i].getPattern(instrument);
				props.setProperty("P" + i + "_" + instrument.name() + VELOCITY_KEY,
						Integer.toString(velocity));
				props.setProperty("P" + i + "_" + instrument.name() + PATTERN_KEY, pattern);
			}
		}

		return props;
	}

	private void setProperties(final Properties props) {

		pagePanel.setValue(Integer.parseInt(props.getProperty(PAGE_KEY, "1")));
		quarterPanel.setValue(Integer.parseInt(props.getProperty(QUARTER_KEY, "4")));
		divisionPanel.setValue(Integer.parseInt(props.getProperty(DIVISION_KEY, "4")));

		for (int i = 0; i < loopPanel.length; ++i) {
			loopPanel[i].setVelocity(LoopPanel.ACCENT, Integer.parseInt(props
					.getProperty("P" + i + "_" + LoopPanel.ACCENT.name() + VELOCITY_KEY, "127")));
			loopPanel[i].setPattern(LoopPanel.ACCENT, props
					.getProperty("P" + i + "_" + LoopPanel.ACCENT.name() + PATTERN_KEY, "    "));

			for (final Instrument instrument : percussionInstruments) {
				final int velocity = Integer.parseInt(
						props.getProperty("P" + i + "_" + instrument.name() + VELOCITY_KEY, "60"));
				final String pattern = props
						.getProperty("P" + i + "_" + instrument.name() + PATTERN_KEY, "    ");
				loopPanel[i].setPattern(instrument, pattern);
				loopPanel[i].setVelocity(instrument, velocity);
			}

			for (final Instrument instrument : guitarInstruments) {
				final int velocity = Integer.parseInt(
						props.getProperty("P" + i + "_" + instrument.name() + VELOCITY_KEY, "60"));
				final String pattern = props
						.getProperty("P" + i + "_" + instrument.name() + PATTERN_KEY, "    ");
				loopPanel[i].setPattern(instrument, pattern);
				loopPanel[i].setVelocity(instrument, velocity);
			}
			loopPanel[i].setNumberOfPages(pagePanel.getValue());
			loopPanel[i].setQuarterPerPage(quarterPanel.getValue());
			loopPanel[i].setQuarterDivision(divisionPanel.getValue());
		}
		bpmPanel.setValue(Integer.parseInt(props.getProperty(BPM_KEY, "120")));
		masterVelocityPanel.setValue(Integer.parseInt(props.getProperty(MASTER_KEY, "60")));

		beat.setBpM(bpmPanel.getValue());
		beat.setDivision(divisionPanel.getValue());
		copyLoopPanelInfo();
		repaint();
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 *            form command line
	 * 
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					final MainFrame frame = new MainFrame();
					frame.setResizable(false);
					frame.setVisible(true);
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		});
	}
}
