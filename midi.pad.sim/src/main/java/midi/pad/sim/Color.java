package midi.pad.sim;

public class Color {
	private int r;
	private int g;

	public int getR() {
		return r;
	}

	public void setR(int r) {
		this.r = Math.max(0, Math.min(3, r));
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
		this.g = Math.max(0, Math.min(3, g));
	}

	public java.awt.Color getAwtColor() {
		return new java.awt.Color(r * 80, g * 80, 0);
	}
}
