package astro;

import astro.util.Date;

/**
 * Class holding information about the Moon
 * 
 * @author Marc Frincu
 * @since 2009
 */
public class MoonData extends ObjectData {

	SunData sun = null;
	double lat, longitude, lst, lonMoon, latMoon;

	MoonData(String name, double N, double i, double w, double a, double e,
			double M, final double dayNumber, SunData sun,
			final double latitude, final double longitude, final double lst,
			final double timeDiff) {

		super(name, N, i, w, a, e, M, dayNumber, timeDiff);

		this.sun = sun;
		this.lat = latitude;
		this.longitude = longitude;
		this.lst = lst;

		this.computePosition(dayNumber);
		this.computeEphemeride(dayNumber);
		this.computeRiseSetTime(latitude, longitude);
	}

	protected void computePosition(final double dayNumber) {

		final double LS = sun.getM() + sun.getW();

		// Mean Longitude
		final double LM = this.M + this.w + this.N;
		// Mean Elongation
		final double D = LM - LS;
		// Latitude argument
		final double F = LM - sun.getN();

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
		double r = Math.sqrt(xv * xv + yv * yv);

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

		lonecl = lonecl
				* 180
				/ Math.PI
				+ (-1.274 * Math.sin(this.M - 2 * D) + 0.658 * Math.sin(2 * D)
						- 0.186 * Math.sin(sun.getM()) - 0.059
						* Math.sin(2 * this.M - 2 * D) - 0.057
						* Math.sin(this.M - 2 * D + sun.getM()) + 0.053
						* Math.sin(this.M + 2 * D) + 0.046
						* Math.sin(2 * D - sun.getM()) + 0.041
						* Math.sin(this.M - sun.getM()) - 0.035 * Math.sin(D)
						- 0.031 * Math.sin(this.M + sun.getM()) - 0.015
						* Math.sin(2 * F - 2 * D) + 0.011 * Math.sin(this.M - 4
						* D));
		latecl = latecl
				* 180
				/ Math.PI
				+ (-0.173 * Math.sin(F - 2 * D) - 0.055
						* Math.sin(this.M - F - 2 * D) - 0.046
						* Math.sin(this.M + F - 2 * D) + 0.033
						* Math.sin(F + 2 * D) + 0.017 * Math
						.sin(2 * this.M + F));

		lonecl = lonecl * Math.PI / 180;
		latecl = latecl * Math.PI / 180;

		this.latMoon = latecl;
		this.lonMoon = lonecl;

		r = r + (-0.58 * Math.cos(this.M - 2 * D) - 0.46 * Math.cos(2 * D));

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

		// The previous computed position was the geocentric one.
		// For the topocentric we need to take into account the flattening of
		// Earth:
		final double mpar = 1 / this.a;
		final double gclat = (this.lat * 180 / Math.PI - 0.1924 * Math
				.sin(2 * this.lat))
				* Math.PI / 180;
		final double rho = 0.99833 + 0.00167 * Math.cos(2 * this.lat);
		final double ha = this.lst - this.RA;
		final double g = Math.atan(Math.tan(gclat) / Math.cos(ha));
		this.RA = this.RA - mpar * rho * Math.cos(gclat) * Math.sin(ha)
				/ Math.cos(this.Dec);
		this.Dec = this.Dec - mpar * rho * Math.sin(gclat)
				* Math.sin(g - this.Dec) / Math.sin(g);

		this.rg = r;
		this.rh = 1;
	}

	@Override
	protected void computeEphemeride(final double dayNumber) {
		final double LS = sun.getM() + sun.getW();

		this.appDiameter = 1873.7 / rg;
		this.elongation = Math.acos(Math.cos(LS - lonMoon) * Math.cos(latMoon));
		final double fv = 180 - this.elongation * 180 / Math.PI;
		this.phase = (1 + Math.cos(fv * Math.PI / 180)) / 2;
		this.magnitude = -1
				* (+0.23 + 5 * Math.log10(rh * rg) + 0.026 * fv + 0.000000004 * Math
						.pow(fv, 4));
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
		final double h = Date.rev(-0.883) * Math.PI / 180;

		final double LHA = ((Math.sin(h) - Math.sin(latitude)
				* Math.sin(this.Dec)) / (Math.cos(latitude) * Math
				.cos(this.Dec)));
		final double GMSTO = Date.rev(this.sun.getLonSun() * 180 / Math.PI
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
