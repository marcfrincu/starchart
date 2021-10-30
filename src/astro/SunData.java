package astro;

import astro.util.Date;

/**
 * Class holding information about the Sun.
 * @author Marc Frincu
 * @since 2009
 */
public class SunData extends ObjectData {
	
	double xs, ys, rs, lonSun;
	
	SunData(String name, double N, double i, double w, double a,
			double e, double M, final double dayNumber, final double latitude, final double longitude, final double timeDiff) {
		
		super(name, N, i, w, a, e, M, dayNumber, timeDiff);
		
		this.computePosition(dayNumber);
		this.computeRiseSetTime(latitude, longitude);
		
		this.magnitude = -23;
		this.phase = 100;
	}
	
	public void update(final double dayNumber, final double latitude, final double longitude){
		this.computePosition(dayNumber);
		this.computeEphemeride(dayNumber);
		this.computeRiseSetTime(latitude, longitude);
		
	}

	protected void computePosition(final double dayNumber) {
		double Ecl = this.M + this.e * Math.sin(this.M)
				* (1.0 + this.e * Math.cos(this.M));
		double EclOld = 0;
		
		while (Math.abs(Ecl - EclOld) > 0.0005) {
			EclOld = Ecl;
			Ecl = Ecl - (Ecl - this.e * Math.sin(Ecl) - this.M)
					/ (1 - this.e * Math.cos(Ecl));
		}

		final double xv = Math.cos(Ecl) - this.e;
		final double yv = Math.sin(Ecl)
				* Math.sqrt(1.0 - (this.e * this.e));

		final double v = Math.atan2(yv, xv);
		final double r = Math.sqrt(xv * xv + yv * yv);
		
		rs = r;
		this.lonSun = v + this.w;

		this.xs = rs * Math.cos(this.lonSun);
		this.ys = rs * Math.sin(this.lonSun);

		this.x = xs;
		this.y = ys * Math.cos(this.computeOblEcl(dayNumber));
		this.z = ys * Math.sin(this.computeOblEcl(dayNumber));

		this.RA = Math.atan2(this.y, this.x);
		this.Dec = Math.atan2(this.z, Math.sqrt(this.x * this.x + this.y
				* this.y));
	}

	public double getXs() {
		return xs;
	}

	public double getYs() {
		return ys;
	}

	@Override
	protected void computeEphemeride(final double dayNumber) {
		return;
	}

	public double getRs() {
		return rs;
	}

	public double getLonSun() {
		return lonSun;
	}

	@Override
	protected void computeRiseSetTime(final double latitude, final double longitude) {
		final double LS = this.M + this.w;
		
		final double h=Date.rev(-0.883)*Math.PI/180;
		final double LHA = ((Math.sin(h) - Math.sin(latitude)
				* Math.sin(this.Dec)) / (Math.cos(latitude) * Math
				.cos(this.Dec)));
		final double GMSTO = Date.rev(LS * 180 / Math.PI
				+ 180);
		final double UTSunInSouth = Date.rev((RA * 180) / Math.PI - GMSTO
				- longitude) / 15.04107;
		final double arcCos = Math.acos(LHA);

		this.riseTime = UTSunInSouth
				- (Date.rev(arcCos * 180 / Math.PI) / 15.04107);
		this.riseTime += this.timeDiff;

		if (this.riseTime < 0)
			this.riseTime += 24;

		if (this.riseTime > 24)
			this.riseTime -= 24;

		this.setTime = UTSunInSouth
				+ (Date.rev(arcCos * 180 / Math.PI) / 15.04107);
		this.setTime += this.timeDiff;

		if (this.setTime < 0)
			this.setTime += 24;
		if (this.setTime > 24)
			this.setTime -= 24;

		this.transitTime = UTSunInSouth + this.timeDiff;
		if (this.transitTime < 0)
			this.transitTime += 24;
		if (this.transitTime > 24)
			this.transitTime -= 24;
		
	}
}

