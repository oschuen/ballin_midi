package midi.pad.sim;

import java.awt.Graphics;

public class Screen {
	private Color[] buttons = new Color[80];
	public Screen() {
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new Color();
			buttons[i].setG(3);
			buttons[i].setR(3);
		}
	}
	
	public void draw(Graphics g, int width, int height) {
		final int padding = 8;
		final int space = 1;
		int xPad = width / (9 * padding + 10 * space);
		int yPad = height / (9 * padding + 10 * space);
		int xoff = space * xPad;
		int xstep = xoff + padding * xPad;
		int xsize = padding * xPad; 
		int yoff = space * yPad;
		int ystep = yoff + padding * yPad;
		int ysize = padding * yPad; 
		
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				g.setColor(buttons[x + y * 8].getAwtColor());
				g.fill3DRect(x * xstep + xoff, y * ystep + yoff + ystep, xsize, ysize, true);
			}
		}
		for (int x = 0; x < 8; x++) {
			g.setColor(buttons[x + 64].getAwtColor());
			g.fillOval(x * xstep + xoff , yoff, xsize, ysize);
		}
		for (int y = 0; y < 8; y++) {
			g.setColor(buttons[y + 72].getAwtColor());
			g.fillOval(8 * xstep + xoff, y * ystep + ystep + yoff,xsize, ysize);
		}
	}
}
