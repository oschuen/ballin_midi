package midi.pad.sim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Toolkit;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class PadFrame extends JFrame {
	private static final Logger logger = LoggerFactory.getLogger(PadFrame.class);
	Screen screen = new Screen();
	
	public PadFrame()  {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int dim = Math.min(800, Math.min(dimension.width, dimension.height));
		setPreferredSize(new Dimension(dim, dim));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Pannel Simulation");
		pack();
		setLocationRelativeTo(null);
	}

	private void draw() {
		Graphics g = getContentPane().getGraphics();
		int width = getContentPane().getWidth();
		int height = getContentPane().getHeight();
		g.setColor(Color.lightGray);
		g.fillRect(0, 0, width - 1, height -1);
		screen.draw(g, width, height);
		g.dispose();
	}
	
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
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
					} catch (InterruptedException e) {
						
					}
				}
			}
		}).start();
	}

}
