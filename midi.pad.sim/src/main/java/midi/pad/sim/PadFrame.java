package midi.pad.sim;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import midi.device.resource.InputDevice;
import midi.device.resource.OutputDevice;

@SuppressWarnings("serial")
public class PadFrame extends JFrame {
	private static final Logger logger = LoggerFactory.getLogger(PadFrame.class);

	private static String midiInDevice = "VirMIDI \\[hw:\\d,3,0\\]";
	private static String midiOutDevice = "VirMIDI \\[hw:\\d,3,0\\]";
	private final InputDevice midiInputDevice = new InputDevice();
	private final OutputDevice midiOutputDevice = new OutputDevice();
	public static String CFG_MIDI_INPUT_DEVICE = "MIDI_INPUT_DEVICE";
	public static String CFG_MIDI_OUTPUT_DEVICE = "MIDI_OUTPUT_DEVICE";
	private static String defaultDevice = "null";
	private final PadReceiver padReceiver = new PadReceiver(midiOutputDevice.getOutput(), () -> draw());

	private final MouseAdapter adapter = new MouseAdapter() {
		@Override
		public void mousePressed(final MouseEvent e) {
			padReceiver.press(e.getX(), e.getY(), getContentPane().getWidth(), getContentPane().getHeight());
		}

		@Override
		public void mouseReleased(final MouseEvent e) {
			padReceiver.release(e.getX(), e.getY(), getContentPane().getWidth(), getContentPane().getHeight());
		}

		@Override
		public void mouseDragged(final MouseEvent e) {
		}
	};

	private static Map<String, Object> runtimeConfig = new HashMap<String, Object>() {
		{
			put(CFG_MIDI_INPUT_DEVICE, midiInDevice);
			put(CFG_MIDI_OUTPUT_DEVICE, midiOutDevice);
		}
	};

	public PadFrame() {
		final Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		final int dim = Math.min(800, Math.min(dimension.width, dimension.height));
		setPreferredSize(new Dimension(dim, dim));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Pannel Simulation");
		pack();
		setLocationRelativeTo(null);
		innerSetConfig(runtimeConfig);
		midiInputDevice.addInput(padReceiver);
		this.getContentPane().addMouseListener(adapter);
	}

	protected static Integer getIntConfig(final String key, final Integer defValue) {
		final Object raw = runtimeConfig.get(key);
		return raw == null ? defValue : ((Integer) raw).intValue();
	}

	protected static String getStringConfig(final String key, final String defValue) {
		final Object raw = runtimeConfig.get(key);
		return raw == null ? defValue : (String) raw;
	}

	private void innerSetConfig(final Map<String, Object> runtimeConfig) {
		midiInputDevice.setDeviceName(getStringConfig(CFG_MIDI_INPUT_DEVICE, defaultDevice));
		midiOutputDevice.setDeviceName(getStringConfig(CFG_MIDI_OUTPUT_DEVICE, defaultDevice));
	}

	private void draw() {
		final Graphics g = getContentPane().getGraphics();
		final int width = getContentPane().getWidth();
		final int height = getContentPane().getHeight();
		padReceiver.draw(g, width, height);
		g.dispose();
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		draw();
	}

	private static void innerMain(File file) {
		Properties props = new Properties();
		try {
			try (InputStream stream = new FileInputStream(file)) {
				props.load(stream);
			}
		} catch (IOException e) {
		}
		runtimeConfig.clear();
		props.entrySet().stream().forEach(e -> runtimeConfig.put((String)e.getKey(), (String)e.getValue()));
		final PadFrame frame = new PadFrame();
		frame.innerSetConfig(runtimeConfig);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					frame.setVisible(true);
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		});
	}
	
	/**
	 * Launch the application.
	 * 
	 * @param args
	 *            form command line
	 * 
	 */
	public static void main(final String[] args) {
		Options options = new Options();
		options.addOption("c", "config", true, "Configuration File");
		CommandLineParser parser = new DefaultParser();
		boolean error = false;
		File file = null;
		try {
			CommandLine cmd = parser.parse(options, args);
			String configFile = cmd.getOptionValue("c");
			if (configFile == null) {
				error = true;
				file = null;
			} else {
				file = new File(configFile);
			}
		} catch (ParseException e) {
			error = true;
		}
		if (error || file == null) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Pad Simulation", options);
		} else {
			innerMain(file);
		}
	}
}
