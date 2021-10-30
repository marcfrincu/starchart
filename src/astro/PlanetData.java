package astro;

import astro.util.Date;

/**
 * Class holding information about the a Planet
 * @author Marc Frincu
 * @since 2009
 *
 */
public class PlanetData extends ObjectData {

	SunData sun = null;

	PlanetData(String name, double N, double i, double w, double a, double e,
			double M, final double dayNumber, final double latitude,
			final double longitude, SunData sun, final double timeDiff) {

		super(name, N, i, w, a, e, M, dayNumber, timeDiff);

		this.sun = sun;

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

		final double xv = this.a * (Math.cos(Ecl) - this.e);
		final double yv = this.a
				* (Math.sqrt(1.0 - this.e * this.e) * Math.sin(Ecl));
		final double v = Math.atan2(yv, xv);
		final double r = Math.sqrt(xv * xv + yv * yv);

		this.x = r
				* (Math.cos(this.N) * Math.cos(v + this.w) - Math.sin(this.N)
						* Math.sin(v + this.w) * Math.cos(this.i));
		this.y = r
				* (Math.sin(this.N) * Math.cos(v + this.w) + Math.cos(this.N)
						* Math.sin(v + this.w) * Math.cos(this.i));
		this.z = r * (Math.sin(v + this.w) * Math.sin(this.i));

		double lonecl = Math.atan2(this.y, this.x);
		double latecl = Math.atan2(this.z, Math.sqrt(this.x * this.x + this.y
				* this.y));

		if (this.name.compareTo("Jupiter") == 0) {
			final double Ms = Date.rev(316.967 + 0.0334442282 * dayNumber)
					* Math.PI / 180;

			lonecl = lonecl - 0.332
					* Math.sin(2 * this.M - 5 * Ms - 67.6 * Math.PI / 180);
			lonecl = lonecl - 0.056
					* Math.sin(2 * this.M - 2 * Ms + 21 * Math.PI / 180);
			lonecl = lonecl + 0.042
					* Math.sin(3 * this.M - 5 * Ms + 21 * Math.PI / 180);
			lonecl = lonecl - 0.036 * Math.sin(this.M - 2 * Ms);
			lonecl = lonecl + 0.022 * Math.cos(this.M - Ms);
			lonecl = lonecl + 0.023
					* Math.sin(2 * this.M - 3 * Ms + 52 * Math.PI / 180);
			lonecl = lonecl - 0.016
					* Math.sin(this.M - 5 * Ms - 69 * Math.PI / 180);

		}

		if (this.name.compareTo("Saturn") == 0) {
			final double Mj = Date.rev(19.895 + 0.0830853001 * dayNumber)
					* Math.PI / 180;
			final double Ms = Date.rev(316.967 + 0.0334442282 * dayNumber)
					* Math.PI / 180;

			lonecl = lonecl + 0.812
					* Math.sin(2 * Mj - 5 * this.M - 67.6 * Math.PI / 180);
			lonecl = lonecl - 0.229
					* Math.cos(2 * Mj - 4 * this.M - 2 * Math.PI / 180);
			lonecl = lonecl + 0.119
					* Math.sin(Mj - 2 * this.M - 3 * Math.PI / 180);
			lonecl = lonecl + 0.046
					* Math.sin(2 * Mj - 6 * this.M - 69 * Math.PI / 180);
			lonecl = lonecl + 0.014
					* Math.sin(Mj - 3 * this.M + 32 * Math.PI / 180);

			latecl = latecl - 0.020
					* Math.cos(2 * Mj - 4 * Ms - 2 * Math.PI / 180);
			latecl = latecl + 0.018
					* Math.sin(2 * Mj - 6 * Ms - 49 * Math.PI / 180);
		}

		if (this.name.compareTo("Uranus") == 0) {
			final double Mj = Date.rev(19.895 + 0.0830853001 * dayNumber)
					* Math.PI / 180;
			final double Ms = Date.rev(316.967 + 0.0334442282 * dayNumber)
					* Math.PI / 180;

			lonecl = lonecl + 0.040
					* Math.sin(Ms - 2 * this.M + 6 * Math.PI / 180);
			lonecl = lonecl + 0.035
					* Math.sin(Ms - 3 * this.M + 33 * Math.PI / 180);
			lonecl = lonecl - 0.015
					* Math.sin(Mj - this.M + 20 * Math.PI / 180);
		}

		this.x = r * Math.cos(lonecl) * Math.cos(latecl);
		this.y = r * Math.sin(lonecl) * Math.cos(latecl);
		this.z = r * Math.sin(latecl);
		
		final double xg = this.x + sun.getXs();
		final double yg = this.y + sun.getYs();
		final double zg = this.z;

		final double xe = xg;
		final double ye = yg * Math.cos(this.computeOblEcl(dayNumber)) - zg
				* Math.sin(this.computeOblEcl(dayNumber));
		final double ze = yg * Math.sin(this.computeOblEcl(dayNumber)) + zg
				* Math.cos(this.computeOblEcl(dayNumber));

		this.RA = Math.atan2(ye, xe);
		this.Dec = Math.atan2(ze, Math.sqrt(xe * xe + ye * ye));
		
		this.rh = r;
		this.rg = Math.sqrt(xg * xg + yg * yg + zg * zg);

	}

	protected void computeEphemeride(final double dayNumber) {

		this.elongation = Math.atan((-((this.sun.getRs() * this.sun.getRs()
				+ rg * rg - rh * rh) / (2 * this.sun.getRs() * rg)) / Math
				.sqrt(-((this.sun.getRs() * this.sun.getRs() + rg * rg - rh
						* rh) / (2 * this.sun.getRs() * rg))
						* ((this.sun.getRs() * this.sun.getRs() + rg * rg - rh
								* rh) / (2 * this.sun.getRs() * rg)) + 1)))
				+ 2 * Math.atan(1);
		final double fv = Math.atan((-((rh * rh + rg * rg - this.sun.getRs()
				* this.sun.getRs()) / (2 * rg * rh)) / Math
				.sqrt(-((rh * rh + rg * rg - this.sun.getRs()
						* this.sun.getRs()) / (2 * rg * rh))
						* ((rh * rh + rg * rg - this.sun.getRs()
								* this.sun.getRs()) / (2 * rg * rh)) + 1)))
				+ 2 * Math.atan(1);
		this.phase = (1 + Math.cos(fv)) / 2;

		if (this.name.compareTo("Mercur") == 0) {
			this.magnitude = -0.36 + 5 * Math.log(rh * rg) / Math.log(10)
					+ 0.027 * fv + (0.00000000000022 * Math.pow(fv, 6));
			this.appDiameter = 6.74 / this.rg;
		}
		if (this.name.compareTo("Venus") == 0) {
			this.magnitude = -4.34 + 5 * Math.log(rh * rg) / Math.log(10)
					+ 0.013 * fv + 0.00000042 * Math.pow(fv, 3);
			this.appDiameter = 16.92 / this.rg;
		}
		if (this.name.compareTo("Marte") == 0) {
			this.magnitude = -1.51 + 5 * Math.log(rh * rg) / Math.log(10)
					+ 0.016 * fv;
			this.appDiameter = 9.362 / this.rg;
		}
		if (this.name.compareTo("Jupiter") == 0) {
			this.magnitude = -9.25 + 5 * Math.log(rh * rg) / Math.log(10)
					+ 0.014 * fv;
			this.appDiameter = 196.9 / this.rg;
		}
		if (this.name.compareTo("Saturn") == 0) {
			final double las = this.Dec;
			final double los = this.RA;
			final double ir = 28.06 * Math.PI / 180;
			final double nr = (169.51 + 0.0000382 * dayNumber) * Math.PI / 180;
			final double temp1 = (Math.sin(las) * Math.cos(ir) - Math.cos(las)
					* Math.sin(ir) * Math.sin(los - nr));
			final double b = Math.atan(temp1 / Math.sqrt(-temp1 * temp1 + 1));
			final double ring_magnitude = -2.6 * Math.sin(Math.abs(b)) + 1.2
					* Math.pow(Math.sin(b), 2);

			this.magnitude = -9 + 5 * Math.log(rh * rg) / Math.log(10) + 0.044
					* fv + ring_magnitude;
			this.appDiameter = 165.6 / this.rg;
		}
		if (this.name.compareTo("Uranus") == 0) {
			this.magnitude = -7.15 + 5 * Math.log(rh * rg) / Math.log(10)
					+ 0.001 * fv;
			this.appDiameter = 65.8 / this.rg;
		}
		if (this.name.compareTo("Neptun") == 0) {
			this.magnitude = -6.9 + 5 * Math.log(rh * rg) / Math.log(10)
					+ 0.001 * fv;
			this.appDiameter = 62.2 / this.rg;
		}
	}

	@Override
	public void update(double dayNumber, final double latitude,
			final double longitude) {
		this.computePosition(dayNumber);
		this.computeEphemeride(dayNumber);
		this.computeRiseSetTime(latitude, longitude);
	}

	public void update(double dayNumber, final double latitude,
		final double longitude, SunData sun) {
		
		this.sun = sun;
		this.update(dayNumber, latitude, longitude);
	}
	
	@Override
	protected void computeRiseSetTime(final double latitude,
			final double longitude) {
		final double LS = sun.getM() + sun.getW();

		final double h = Date.rev(29.0 / 60.0 * Math.PI / 180
				- ((6378.15 / 149600000.0) / this.rg));

		final double LHA = ((Math.sin(h) - Math.sin(latitude)
				* Math.sin(this.Dec)) / (Math.cos(latitude) * Math
				.cos(this.Dec)));
		final double GMSTO = Date.rev(LS * 180 / Math.PI + 180);
		final double UTSunInSouth = Date.rev(this.RA * 180 / Math.PI - GMSTO
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

		this.transitTime = UTSunInSouth + timeDiff;
		if (this.transitTime < 0)
			this.transitTime += 24;
		if (this.transitTime > 24)
			this.transitTime -= 24;
	}

}
