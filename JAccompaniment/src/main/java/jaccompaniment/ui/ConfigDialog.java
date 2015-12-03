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
 * @since 29.11.2015
 * @version 1.0
 * @author oliver
 */
package jaccompaniment.ui;

import jaccompaniment.resource.MidiDevices;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configures Midi devices and channels
 * 
 * @author oliver
 */
@SuppressWarnings("serial")
public class ConfigDialog extends JDialog {

	private static String GUITAR_OUTPUT_KEY = "Guitar Output";
	private static String PERCUSSION_OUTPUT_KEY = "Percussion Output";
	private static String MIDI_THROUGH_OUTPUT_KEY = "Midi Through Output";
	private static String MIDI_THROUGH_INPUT_KEY = "Midi Through Input";
	private static String RECOGINZER_INPUT_KEY = "Recognizer Input";
	private static String GUITAR_CHANNEL_KEY = "Guitar Channel";
	private static String PERCUSSION_CHANNEL_KEY = "Percussion Channel";
	private static String MIDI_THROUGH_KEY = "Midi Through";
	private static String FILTER_CHORD_KEY = "Filter Chord";

	private static final Preferences prefs = Preferences.userNodeForPackage(ConfigDialog.class);

	private final JPanel contentPanel = new JPanel();

	private final JComboBox<String> chordRecComboBox;
	private final JComboBox<String> midiThroughInputComboBox;
	private final JComboBox<String> midiThroughOutputComboBox;
	private final JComboBox<String> guitarComboBox;
	private final JComboBox<String> percussionComboBox;
	private final JTextField guitarTextField;
	private final JTextField percussionTextField;
	private final JCheckBox midiThroughCheckBox;
	private final JCheckBox filterChordCheckBox;

	private boolean result = false;

	private static final Logger logger = LogManager.getLogger(ConfigDialog.class);

	public ConfigDialog(final Frame owner, final boolean modal) {
		super(owner, modal);

		setTitle("Configuration");
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		final GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 1.0, 10.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		final List<String> transDevicenames = MidiDevices.getTransmitterNames();
		final String transmitters[] = transDevicenames.toArray(new String[transDevicenames.size()]);
		final List<String> recvDevicenames = MidiDevices.getReceiverNames();
		final String receivers[] = recvDevicenames.toArray(new String[recvDevicenames.size()]);

		contentPanel.setLayout(gbl_contentPanel);
		{
			final JLabel lblChordRecognition = new JLabel("Chord Recognition (Input)");
			final GridBagConstraints gbc_lblChordRecognition = new GridBagConstraints();
			gbc_lblChordRecognition.anchor = GridBagConstraints.EAST;
			gbc_lblChordRecognition.insets = new Insets(0, 0, 5, 5);
			gbc_lblChordRecognition.gridx = 0;
			gbc_lblChordRecognition.gridy = 0;
			contentPanel.add(lblChordRecognition, gbc_lblChordRecognition);
		}
		{
			chordRecComboBox = new JComboBox<String>();
			chordRecComboBox.setModel(new DefaultComboBoxModel<String>(transmitters));
			chordRecComboBox.setSelectedItem(getRecognizerInputDevice());
			final GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 5, 0);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 1;
			gbc_comboBox.gridy = 0;
			contentPanel.add(chordRecComboBox, gbc_comboBox);
		}
		{
			final JLabel lblNewLabel = new JLabel("Midi Through (Input)");
			final GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 1;
			contentPanel.add(lblNewLabel, gbc_lblNewLabel);
		}
		{
			midiThroughInputComboBox = new JComboBox<>();
			midiThroughInputComboBox.setModel(new DefaultComboBoxModel<String>(transmitters));
			midiThroughInputComboBox.setSelectedItem(getMidiThroughInputDevice());
			final GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 5, 0);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 1;
			gbc_comboBox.gridy = 1;
			contentPanel.add(midiThroughInputComboBox, gbc_comboBox);
		}
		{
			final JLabel lblMelodytransmit = new JLabel("Midi Through (Output)");
			final GridBagConstraints gbc_lblMelodytransmit = new GridBagConstraints();
			gbc_lblMelodytransmit.anchor = GridBagConstraints.EAST;
			gbc_lblMelodytransmit.insets = new Insets(0, 0, 5, 5);
			gbc_lblMelodytransmit.gridx = 0;
			gbc_lblMelodytransmit.gridy = 2;
			contentPanel.add(lblMelodytransmit, gbc_lblMelodytransmit);
		}
		{
			midiThroughOutputComboBox = new JComboBox<>();
			midiThroughOutputComboBox.setModel(new DefaultComboBoxModel<String>(receivers));
			midiThroughOutputComboBox.setSelectedItem(getMidiThroughOutputDevice());
			final GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 5, 0);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 1;
			gbc_comboBox.gridy = 2;
			contentPanel.add(midiThroughOutputComboBox, gbc_comboBox);
		}
		{
			final JLabel lblGuitar = new JLabel("Guitar (Output)");
			final GridBagConstraints gbc_lblGuitar = new GridBagConstraints();
			gbc_lblGuitar.insets = new Insets(0, 0, 5, 5);
			gbc_lblGuitar.anchor = GridBagConstraints.EAST;
			gbc_lblGuitar.gridx = 0;
			gbc_lblGuitar.gridy = 3;
			contentPanel.add(lblGuitar, gbc_lblGuitar);
		}
		{
			guitarComboBox = new JComboBox<>();
			guitarComboBox.setModel(new DefaultComboBoxModel<String>(receivers));
			guitarComboBox.setSelectedItem(getGuitarOutputDevice());
			final GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 5, 0);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 1;
			gbc_comboBox.gridy = 3;
			contentPanel.add(guitarComboBox, gbc_comboBox);
		}
		{
			final JLabel lblGuitarChannel = new JLabel("Guitar (Channel)");
			final GridBagConstraints gbc_lblGuitar = new GridBagConstraints();
			gbc_lblGuitar.insets = new Insets(0, 0, 5, 5);
			gbc_lblGuitar.anchor = GridBagConstraints.EAST;
			gbc_lblGuitar.gridx = 0;
			gbc_lblGuitar.gridy = 4;
			contentPanel.add(lblGuitarChannel, gbc_lblGuitar);
		}
		{
			guitarTextField = new JTextField();
			final GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 5, 0);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 1;
			gbc_comboBox.gridy = 4;
			guitarTextField.setText(Integer.toString(getGuitarChannel()));
			contentPanel.add(guitarTextField, gbc_comboBox);
		}
		{
			final JLabel lblPercussion = new JLabel("Percussion (Output)");
			final GridBagConstraints gbc_lblPercussion = new GridBagConstraints();
			gbc_lblPercussion.anchor = GridBagConstraints.EAST;
			gbc_lblPercussion.insets = new Insets(0, 0, 5, 5);
			gbc_lblPercussion.gridx = 0;
			gbc_lblPercussion.gridy = 5;
			contentPanel.add(lblPercussion, gbc_lblPercussion);
		}
		{
			percussionComboBox = new JComboBox<>();
			percussionComboBox.setModel(new DefaultComboBoxModel<String>(receivers));
			percussionComboBox.setSelectedItem(getPercussionOutputDevice());
			final GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 5, 0);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 1;
			gbc_comboBox.gridy = 5;
			contentPanel.add(percussionComboBox, gbc_comboBox);
		}
		{
			final JLabel lblPercussionChannel = new JLabel("Percussion (Channel)");
			final GridBagConstraints gbc_lblPercussion = new GridBagConstraints();
			gbc_lblPercussion.insets = new Insets(0, 0, 5, 5);
			gbc_lblPercussion.anchor = GridBagConstraints.EAST;
			gbc_lblPercussion.gridx = 0;
			gbc_lblPercussion.gridy = 6;
			contentPanel.add(lblPercussionChannel, gbc_lblPercussion);
		}
		{
			percussionTextField = new JTextField();
			final GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 5, 0);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 1;
			gbc_comboBox.gridy = 6;
			percussionTextField.setText(Integer.toString(getPercussionChannel()));
			contentPanel.add(percussionTextField, gbc_comboBox);
		}
		{
			final JLabel lblMidiThrough = new JLabel("Midi Through");
			final GridBagConstraints gbc_lblMidiThrough = new GridBagConstraints();
			gbc_lblMidiThrough.anchor = GridBagConstraints.EAST;
			gbc_lblMidiThrough.insets = new Insets(0, 0, 5, 5);
			gbc_lblMidiThrough.gridx = 0;
			gbc_lblMidiThrough.gridy = 7;
			contentPanel.add(lblMidiThrough, gbc_lblMidiThrough);
		}
		{
			midiThroughCheckBox = new JCheckBox("");
			midiThroughCheckBox.setSelected(isMidiThrough());
			final GridBagConstraints gbc_checkBox = new GridBagConstraints();
			gbc_checkBox.anchor = GridBagConstraints.WEST;
			gbc_checkBox.insets = new Insets(0, 0, 5, 0);
			gbc_checkBox.gridx = 1;
			gbc_checkBox.gridy = 7;
			contentPanel.add(midiThroughCheckBox, gbc_checkBox);
		}
		{
			final JLabel lblFilterChord = new JLabel("Filter Chord");
			final GridBagConstraints gbc_lblFilterChord = new GridBagConstraints();
			gbc_lblFilterChord.anchor = GridBagConstraints.EAST;
			gbc_lblFilterChord.insets = new Insets(0, 0, 5, 5);
			gbc_lblFilterChord.gridx = 0;
			gbc_lblFilterChord.gridy = 8;
			contentPanel.add(lblFilterChord, gbc_lblFilterChord);
		}
		{
			filterChordCheckBox = new JCheckBox("");
			filterChordCheckBox.setSelected(isFilterChord());
			final GridBagConstraints gbc_checkBox = new GridBagConstraints();
			gbc_checkBox.insets = new Insets(0, 0, 5, 0);
			gbc_checkBox.anchor = GridBagConstraints.WEST;
			gbc_checkBox.gridx = 1;
			gbc_checkBox.gridy = 8;
			contentPanel.add(filterChordCheckBox, gbc_checkBox);
		}
		{
			final JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				final JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				okButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(final ActionEvent e) {
						result = true;
						storeSetting();
						setVisible(false);
					}
				});
			}
			{
				final JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				cancelButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(final ActionEvent e) {
						result = false;
						setVisible(false);
					}
				});
			}
		}
		pack();
	}

	private void storeSetting() {
		prefs.put(GUITAR_OUTPUT_KEY, (String) guitarComboBox.getSelectedItem());
		prefs.put(PERCUSSION_OUTPUT_KEY, (String) percussionComboBox.getSelectedItem());
		prefs.put(MIDI_THROUGH_OUTPUT_KEY, (String) midiThroughOutputComboBox.getSelectedItem());
		prefs.put(MIDI_THROUGH_INPUT_KEY, (String) midiThroughInputComboBox.getSelectedItem());
		prefs.put(RECOGINZER_INPUT_KEY, (String) chordRecComboBox.getSelectedItem());
		prefs.putBoolean(MIDI_THROUGH_KEY, midiThroughCheckBox.isSelected());
		prefs.putBoolean(FILTER_CHORD_KEY, filterChordCheckBox.isSelected());
		final String rawGuitarChannel = guitarTextField.getText();
		int guitarChannel = getGuitarChannel();
		try {
			guitarChannel = Integer.parseInt(rawGuitarChannel.trim());
		} catch (final NumberFormatException e) {
			logger.warn(e.getMessage(), e);
		}
		prefs.putInt(GUITAR_CHANNEL_KEY, guitarChannel);
		final String rawPercussionChannel = percussionTextField.getText();
		int percussionChannel = getPercussionChannel();
		try {
			percussionChannel = Integer.parseInt(rawPercussionChannel.trim());
		} catch (final NumberFormatException e) {
			logger.warn(e.getMessage(), e);
		}
		prefs.putInt(PERCUSSION_CHANNEL_KEY, percussionChannel);
		try {
			prefs.sync();
		} catch (final BackingStoreException e) {
			logger.error("Failed storing settings", e);
		}
	}

	public static String getGuitarOutputDevice() {
		return prefs.get(GUITAR_OUTPUT_KEY, MidiDevices.getReceiverNames().get(0));
	}

	public static String getPercussionOutputDevice() {
		return prefs.get(PERCUSSION_OUTPUT_KEY, MidiDevices.getReceiverNames().get(0));
	}

	public static String getMidiThroughOutputDevice() {
		return prefs.get(MIDI_THROUGH_OUTPUT_KEY, MidiDevices.getReceiverNames().get(0));
	}

	public static String getMidiThroughInputDevice() {
		return prefs.get(MIDI_THROUGH_INPUT_KEY, MidiDevices.getTransmitterNames().get(0));
	}

	public static String getRecognizerInputDevice() {
		return prefs.get(RECOGINZER_INPUT_KEY, MidiDevices.getTransmitterNames().get(0));
	}

	public static int getGuitarChannel() {
		return prefs.getInt(GUITAR_CHANNEL_KEY, 1);
	}

	public static int getPercussionChannel() {
		return prefs.getInt(PERCUSSION_CHANNEL_KEY, 9);
	}

	public static boolean isMidiThrough() {
		return prefs.getBoolean(MIDI_THROUGH_KEY, true);
	}

	public static boolean isFilterChord() {
		return prefs.getBoolean(FILTER_CHORD_KEY, true);
	}

	/**
	 * @return the result
	 */
	public boolean isResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(final boolean result) {
		this.result = result;
	}

}
