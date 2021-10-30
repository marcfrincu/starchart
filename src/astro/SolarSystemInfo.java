package astro;

import java.util.ArrayList;

import astro.util.Date;

/**
 * Class holding information about the Solar System.
 * @author Marc Frincu
 * @since 2009
 */
public class SolarSystemInfo {
	private ArrayList<ObjectData> objects = null;
	private Date date = null;
	private double lat, longitude, timeDiff;

	public SolarSystemInfo(final double lat, final double longitude,
			final double timeDiff, Date date) {
		this.initAll(lat, longitude, timeDiff, date);
	}
	
	public SolarSystemInfo(final double lat, final double longitude,
			final double timeDiff) {
		this.initAll(lat, longitude, timeDiff, new Date(longitude, timeDiff));
	}
	
	private void initAll(final double lat, final double longitude,
			final double timeDiff, Date date){
		this.objects = new ArrayList<ObjectData>();

		this.timeDiff = timeDiff;

		this.date = date;
		this.lat = lat;
		this.longitude = longitude;

		final double dayNumber = date.getDayNumber();
		final SunData sun = new SunData("Soare", 0, 0, Date
				.rev(282.9404 + 0.0000470935 * dayNumber)
				* Math.PI / 180, 1, 0.016709 - 0.000000001151 * date
				.getDayNumber(), Date.rev(356.047 + 0.9856002585 * date
				.getDayNumber())
				* Math.PI / 180, dayNumber, lat, longitude, this.timeDiff);

		objects.add(sun);
		objects
				.add(new PlanetData("Mercur", Date
						.rev(48.3313 + 0.0000324587 * dayNumber)
						* Math.PI / 180, Date.rev(7.0047 + 0.00000005 * date
						.getDayNumber())
						* Math.PI / 180, Date.rev(29.1241 + 0.0000101444 * date
						.getDayNumber())
						* Math.PI / 180, 0.387098 + 0.0000000000180698 * date
						.getDayNumber(), 0.205635 + 0.000000000559 * date
						.getDayNumber(), Date
						.rev(168.6562 + 4.0923344368 * date.getDayNumber())
						* Math.PI / 180, dayNumber, lat, longitude, sun,
						this.timeDiff));
		objects
				.add(new PlanetData("Venus", Date
						.rev(76.6799 + 0.000024659 * dayNumber)
						* Math.PI / 180, Date.rev(3.3946 + 0.0000000275 * date
						.getDayNumber())
						* Math.PI / 180, Date.rev(54.891 + 0.0000138374 * date
						.getDayNumber())
						* Math.PI / 180, 0.72333 + 0.0000000000251882 * date
						.getDayNumber(), 0.006773 - 0.000000001302 * date
						.getDayNumber(), Date
						.rev(48.0052 + 1.60213022448 * date.getDayNumber())
						* Math.PI / 180, dayNumber, lat, longitude, sun,
						this.timeDiff));
		objects.add(new MoonData("Luna", Date
				.rev(125.1228 - 0.0529538083 * dayNumber)
				* Math.PI / 180, 5.1454 * Math.PI / 180, Date
				.rev(318.0634 + 0.1643573223 * date.getDayNumber())
				* Math.PI / 180, 60.2666, 0.054900, Date
				.rev(115.3654 + 13.06499295098 * date.getDayNumber())
				* Math.PI / 180, dayNumber, sun, lat, longitude, date.getLST(),
				this.timeDiff));
		objects
				.add(new PlanetData("Marte", Date
						.rev(49.5574 + 0.0000211081 * dayNumber)
						* Math.PI / 180, Date.rev(1.8497 - 0.0000000178 * date
						.getDayNumber())
						* Math.PI / 180, Date
						.rev(286.5016 + 0.0000292961 * date.getDayNumber())
						* Math.PI / 180, 1.523688 - 0.000000001977 * date
						.getDayNumber(), 0.093405 + 0.000000002516 * date
						.getDayNumber(), Date.rev(18.6021 + 0.5240207766 * date
						.getDayNumber())
						* Math.PI / 180, dayNumber, lat, longitude, sun,
						this.timeDiff));
		objects
				.add(new PlanetData("Jupiter", Date
						.rev(100.4542 + 0.0000276854 * dayNumber)
						* Math.PI / 180, Date.rev(1.303 - 0.0000001557 * date
						.getDayNumber())
						* Math.PI / 180, Date
						.rev(273.8777 + 0.0000164505 * date.getDayNumber())
						* Math.PI / 180, 5.20256 + 0.0000000166289 * date
						.getDayNumber(), 0.048498 + 0.000000004469 * date
						.getDayNumber(), Date.rev(19.895 + 0.0830853001 * date
						.getDayNumber())
						* Math.PI / 180, dayNumber, lat, longitude, sun,
						this.timeDiff));
		objects
				.add(new PlanetData("Saturn", Date
						.rev(113.6634 + 0.000023898 * dayNumber)
						* Math.PI / 180, Date.rev(2.4886 - 0.0000001081 * date
						.getDayNumber())
						* Math.PI / 180, Date
						.rev(339.3939 + 0.0000297661 * date.getDayNumber())
						* Math.PI / 180,
						9.55475 - 0.00000008255439999999999 * date
								.getDayNumber(),
						0.055546 - 0.000000009499 * date.getDayNumber(), Date
								.rev(316.967 + 0.0334442282 * date
										.getDayNumber())
								* Math.PI / 180, dayNumber, lat, longitude,
						sun, this.timeDiff));
		objects
				.add(new PlanetData("Uranus", Date
						.rev(74.0005 + 0.000013978 * dayNumber)
						* Math.PI / 180, Date.rev(0.7733 + 0.000000019 * date
						.getDayNumber())
						* Math.PI / 180, Date.rev(96.6612 + 0.000030565 * date
						.getDayNumber())
						* Math.PI / 180, 19.18171 + 0.0000000416222 * date
						.getDayNumber(), 0.047318 + 0.00000000745 * dayNumber,
						Date.rev(142.5905 + 0.011725806 * dayNumber) * Math.PI
								/ 180, dayNumber, lat, longitude, sun,
						this.timeDiff));
		objects
				.add(new PlanetData("Neptun", Date
						.rev(131.7806 + 0.000030173 * dayNumber)
						* Math.PI / 180, Date.rev(1.77 - 0.000000255 * date
						.getDayNumber())
						* Math.PI / 180, Date.rev(272.8461 - 0.000006027 * date
						.getDayNumber())
						* Math.PI / 180, 30.05826 - 0.0000000342768 * date
						.getDayNumber(), 0.008606 + 0.00000000215 * dayNumber,
						Date.rev(260.2471 + 0.005995147 * dayNumber) * Math.PI
								/ 180, dayNumber, lat, longitude, sun,
						this.timeDiff));

		
	}
	
	public void update(Date date){
		this.date = date;
		PlanetData planet;
		MoonData moon;
		SunData sun = (SunData)objects.get(0);
		sun.update(this.date.getDayNumber(), this.lat, this.longitude);
		for (int i = 1, size = objects.size(); i < size; i++) {
			if (i != 3) {
				planet = ((PlanetData)objects.get(i));
					planet.update(this.date.getDayNumber(), this.lat, this.longitude, sun);
			}
			else{
				moon = ((MoonData)objects.get(i));
				moon.update(this.date.getDayNumber(), this.lat, this.longitude, sun);
			}
		}		
	}

	public void update() {
		date = new Date(this.longitude, this.timeDiff);
		PlanetData planet;
		MoonData moon;
		SunData sun = (SunData)objects.get(0);
		sun.update(this.date.getDayNumber(), this.lat, this.longitude);
		for (int i = 1, size = objects.size(); i < size; i++) {
			if (i != 3) {
				planet = ((PlanetData)objects.get(i));
					planet.update(this.date.getDayNumber(), this.lat, this.longitude, sun);
			}
			else{
				moon = ((MoonData)objects.get(i));
				moon.update(this.date.getDayNumber(), this.lat, this.longitude, sun);
			}
		}	
		}

	public ArrayList<ObjectData> getObjects() {
		return objects;
	}
}
