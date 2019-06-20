package midi.pad.sim;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

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
	private final PadReceiver padReceiver = new PadReceiver(midiOutputDevice.getOutput(),
			() -> draw());

	private final MouseAdapter adapter = new MouseAdapter() {
		@Override
		public void mousePressed(final MouseEvent e) {
			padReceiver.press(e.getX(), e.getY(), getContentPane().getWidth(),
					getContentPane().getHeight());
		}

		@Override
		public void mouseReleased(final MouseEvent e) {
			padReceiver.release(e.getX(), e.getY(), getContentPane().getWidth(),
					getContentPane().getHeight());
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
		// g.setColor(Color.lightGray);
		// g.fillRect(0, 0, width - 1, height - 1);
		draw();
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 *            form command line
	 * 
	 */
	public static void main(final String[] args) {
		final PadFrame frame = new PadFrame();
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					// frame.setResizable(true);
					frame.setVisible(true);
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		});
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							frame.draw();
						}
					});
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {

					}
				}
			}
		}).start();
	}
}
