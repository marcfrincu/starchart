package astro.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Handles some basic dates such as LST, Day Number, Current Date/Time
 * 
 * @author Marc Frincu
 * @since March 15th 2009
 * 
 */
public class Date {

	private static final String DATE_FORMAT_NOW = "yyyy-MM-dd";
	private static final String TIME_FORMAT_NOW = "HH:mm:ss";

	private double lst;
	private double d;

	private int day, month, year, minute, second;
	private double hour;

	public Date(int year, int month, int day, double hour, double longitude,
			double timeDiff) {

		this.computeDayNumberAndLST(year, month, day, hour - timeDiff,
				longitude);

		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.minute = 0;
		this.second = 0;
		
	//	System.out.println("d set"+ this.d + " " + this.lst + " " + this.hour + " " + (this.lst * 180 / Math.PI) / 15.04107 );

	}

	public Date(double longitude, double timeDiff) {
		final String[] date = Date.getCurrentDate().split("-");
		final String[] time = Date.getCurrentTime().split(":");

		this.year = Integer.parseInt(date[0]);
		this.month = Integer.parseInt(date[1]);
		this.day = Integer.parseInt(date[2]);
		this.hour = Double.parseDouble(time[0])
				+ Double.parseDouble(time[1]) / 60
				+ Double.parseDouble(time[2]) / 3600;
		this.minute = Integer.parseInt(time[1]);
		this.second = Integer.parseInt(time[2]);

		this.computeDayNumberAndLST(Integer.parseInt(date[0]), Integer
				.parseInt(date[1]), Integer.parseInt(date[2]), Double
				.parseDouble(time[0])
				- timeDiff
				+ Double.parseDouble(time[1])
				/ 60
				+ Double.parseDouble(time[2]) / 3600, longitude);
		
		//System.out.println("d current "+ this.d + " " + this.lst);
	}

	private void computeDayNumberAndLST(int year, int month, int day,
			double hour, double longitude) {
		this.d = 367 * year - (int)(7 * (year + (month + 9) / 12) / 4.)
				+ (275 * month) / 9 + day - 730531.5;
		this.d = this.d + hour / 24;
		
		this.lst = Date.rev(100.46 + 0.985647 * this.d + longitude + hour * 15.04107);
		this.lst = this.lst * Math.PI / 180;
		
	
	}
	

	public static double rev(double x) {
		double rv;
		rv = x - ((int) x / 360) * 360;
		if (rv < 0) {
			rv = rv + 360;
		}
		return rv;
	}

	public double getDayNumber() {
		return this.d;
	}

	public double getLST() {
		return this.lst;
	}

	static public String getCurrentDate() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

	static public String getCurrentTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

	public int getDay() {
		return day;
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}

	public double getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}

	public int getSecond() {
		return second;
	}
}
