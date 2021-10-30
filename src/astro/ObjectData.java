package astro;

/**
 * Abstract class offering basic functionality to compute data on Solar System objects.
 * @author Marc Frincu
 * @since 2009
 */
public abstract class ObjectData {
	protected double N, i, w, a, e, M;
	protected String name;
	protected double x, y, z, RA, Dec, phase, magnitude, elongation,
			appDiameter, rh, rg, xpp, ypp, dayNumber, riseTime, setTime,
			transitTime;
	protected boolean visiblePP = false;
	protected double timeDiff;

	ObjectData(String name, double N, double i, double w, double a, double e,
			double M, final double dayNumber, final double timeDiff) {
		this.N = N;
		this.i = i;
		this.w = w;
		this.a = a;
		this.e = e;
		this.M = M;
		this.name = name;
		this.dayNumber = dayNumber;
		this.timeDiff = timeDiff;
	}

	/**
	 * Computes the heliocentric position of an object.
	 * @param dayNumber the day number
	 */
	protected abstract void computePosition(final double dayNumber);

	/**
	 * Computes the ephemerides of an object.
	 * @param dayNumber the day number
	 */
	protected abstract void computeEphemeride(final double dayNumber);

	/**
	 * Updates the position and ephemerides of an object.
	 * @param dayNumber the day number
	 * @param latitude the latitude of the place
	 * @param longitude the longitude of the place
	 */
	public abstract void update(final double dayNumber, final double latitude,
			final double longitude);

	/**
	 * Computes the rise, set and transit times of an object.
	 * @param latitude the latitude of the place
	 * @param longitude the longitude of the place
	 */
	protected abstract void computeRiseSetTime(final double latitude,
			final double longitude);

	public double getN() {
		return N;
	}

	public void setN(double n) {
		N = n;
	}

	public double getI() {
		return i;
	}

	public void setI(double i) {
		this.i = i;
	}

	public double getW() {
		return w;
	}

	public void setW(double w) {
		this.w = w;
	}

	public double getA() {
		return a;
	}

	public void setA(double a) {
		this.a = a;
	}

	public double getE() {
		return e;
	}

	public void setE(double e) {
		this.e = e;
	}

	public double getM() {
		return M;
	}

	public void setM(double m) {
		M = m;
	}

	public String getName() {
		return name;
	}

	protected double computeOblEcl(final double d) {
		return (23.4393 - 0.0000003563 * d) * Math.PI / 180;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public double getRA() {
		return RA;
	}

	public double getDec() {
		return Dec;
	}

	public double getRh() {
		return rh;
	}

	public double getRg() {
		return rg;
	}

	public double getXpp() {
		return xpp;
	}

	public void setXpp(double xpp) {
		this.xpp = xpp;
	}

	public boolean isVisiblePP() {
		return visiblePP;
	}

	public void setVisiblePP(boolean visiblePP) {
		this.visiblePP = visiblePP;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public double getYpp() {
		return ypp;
	}

	public void setYpp(double ypp) {
		this.ypp = ypp;
	}

	public double getAppDiameter() {
		return appDiameter;
	}

	public double getPhase() {
		return phase;
	}

	public double getMagnitude() {
		return magnitude;
	}

	public double getElongation() {
		return elongation;
	}

	public double getRiseTime() {
		return riseTime;
	}

	public double getSetTime() {
		return setTime;
	}

	public double getTransitTime() {
		return transitTime;
	}
}
