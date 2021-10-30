package astro;

/**
 * Class holding information about a Messier object
 * @author Marc Frincu
 * @since 2009
 *
 */
public class MessierData {

	String name;
	double RA, dec, magnitude, x, y;
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getRA() {
		return RA;
	}

	public void setRA(double rA) {
		RA = rA;
	}

	public double getDec() {
		return dec;
	}

	public void setDec(double dec) {
		this.dec = dec;
	}

	public double getMagnitude() {
		return magnitude;
	}

	public void setMagnitude(double mag) {
		this.magnitude = mag;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	boolean isVisible;
	
	public MessierData(String name, double ra, double dec, double mag) {
		this.name = name;
		this.RA = ra;
		this.dec = dec;
		this.magnitude = mag;
	}
}
