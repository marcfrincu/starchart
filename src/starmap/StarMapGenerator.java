package starmap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.sourceforge.jlibeps.epsgraphics.EpsGraphics2D;

public class StarMapGenerator {
	
	private int offset = 1500;
	private int scale = 10;
	private double lat = 45.17;
	private int width = 3350;
	private int height = 3000;

	public static void main(String args[]) throws Exception {

		
		StarMapGenerator sm = new StarMapGenerator();
	//	sm.generateEPSStarMap("data/constellation-lines-2.csv", "harta.eps");
	}
	
	public void generateEPSStarMap(String constellationFile, String epsfile) throws IOException {
		FileOutputStream finalImage = new FileOutputStream(epsfile);
		EpsGraphics2D g = new EpsGraphics2D("Harta astronomica", finalImage, 0, 0, width, height);

		g.setColor(Color.BLACK);

		BufferedReader input = new BufferedReader(new FileReader(constellationFile));
		try {
			String line = null;
			String[] partsPrev = null;
			double ra1, dec1, ra2, dec2, magS;
			while ((line = input.readLine()) != null) {
				String[] parts = line.split(",");
					
				if (parts.length > 1) {
					g.setColor(Color.BLACK);
					ra1 = new Double (parts[6]);
					dec1 = new Double (parts[7]);
					magS = new Double(parts[4]);
					double radius = 2*(7-magS);
					if (new Double (parts[3]) > -45) {
						Ellipse2D.Double circle = new Ellipse2D.Double(offset-(int)((ra1)*scale), offset+(int)((dec1)*scale), radius, radius);
						g.fill(circle);
						if (parts[1].compareTo("alpha") == 0) {
							g.setColor(Color.RED);
							g.setFont(new Font("TimesRoman", Font.PLAIN, 24)); 
							//g.drawString("alfa", offset-(int)((ra1)*scale), offset+(int)((dec1)*scale));
						}
					}
				}
				
				
				if (partsPrev != null && partsPrev.length > 1 && partsPrev[0].compareTo(parts[0]) == 0 && parts.length > 1) { // if same name => same constellation
					try {
						ra1 = new Double (partsPrev[6]);
						dec1 = new Double (partsPrev[7]);
						ra2 = new Double (parts[6]);
						dec2 = new Double (parts[7]);
						magS = new Double(parts[4]);

						if (new Double (partsPrev[3]) > -45) {	
							g.setColor(Color.BLACK);
							g.drawLine(offset-(int)(ra1*scale), offset+(int)(dec1*scale), offset-(int)(ra2*scale), offset+(int)(dec2*scale));
						}
					}
					catch(NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}	
				
				if (partsPrev == null || (partsPrev.length == 1  && partsPrev[0].compareTo(parts[0]) != 0 && parts.length > 1)) { // we switched constellation
					g.setColor(Color.BLUE);
					ra1 = new Double (parts[6]);
					dec1 = new Double (parts[7]);
					if (new Double (parts[3]) >= -45) {
						g.setFont(new Font("TimesRoman", Font.PLAIN, 32));
						int rand = Math.random() < 0.5 ? 5 : 0;
						g.drawString(parts[0], offset-(int)((ra1 + rand)*scale), offset+(int)((dec1)*scale));
					}
				}
				
				partsPrev = parts;
				
			}
			
			g.setColor(Color.BLACK);

            g.setStroke(new BasicStroke(8));
			g.drawArc(70, 70, 290*scale, 290*scale, 0, 360);
			
			g.setColor(Color.BLACK);
			g.setFont(new Font("TimesRoman", Font.BOLD, 24)); 
			g.drawString("INSTRUCȚIUNI DE FOLOSIRE:", 20, 2900);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
			g.drawString("Se decupează fiecare constelație după conturul acesteia și se lipește peste figura indicată de grupul de litere corespondent.", 20, 2935);
			g.drawString("Exemplu: constelația Carul Mare se lipește peste figura indicată de grupul de litere UMA (Ursa Mare).", 20, 2970);
			
			g.setFont(new Font("TimesRoman", Font.BOLD, 28)); 

			g.drawString("	AND	-	Andromeda	",	3000	,	540	);
			g.drawString("	AQL	-	Vulturul	",	3000	,	580	);
			g.drawString("	AQR	-	Vărsătorul	",	3000	,	620	);
			g.drawString("	ARI	-	Berbecul	",	3000	,	660	);
			g.drawString("	AUR	-	Vizitiul	",	3000	,	700	);
			g.drawString("	BOO	-	Boarul	",	3000	,	740	);
			g.drawString("	CAE	-	Dalta	",	3000	,	780	);
			g.drawString("	CAM	-	Girafa	",	3000	,	820	);
			g.drawString("	CAP	-	Capricornul	",	3000	,	860	);
			g.drawString("	CAS	-	Casiopeea	",	3000	,	900	);
			g.drawString("	CEP	-	Cefeu	",	3000	,	940	);
			g.drawString("	CET	-	Balena	",	3000	,	980	);
			g.drawString("	CMA	-	Câinele Mare	",	3000	,	1020	);
			g.drawString("	CMI	-	Câinele Mic	",	3000	,	1060	);
			g.drawString("	CNC	-	Cancer	",	3000	,	1100	);
			g.drawString("	COL	-	Porumbelul	",	3000	,	1140	);
			g.drawString("	COM	-	Părul Berenicei	",	3000	,	1180	);
			g.drawString("	CRA	-	Coroana Australă	",	3000	,	1220	);
			g.drawString("	CRB	-	Coroana Boreală	",	3000	,	1260	);
			g.drawString("	CRT	-	Cupa	",	3000	,	1300	);
			g.drawString("	CRV	-	Corbul	",	3000	,	1340	);
			g.drawString("	CVN	-	Câinii de Vânătoare	",	3000	,	1380	);
			g.drawString("	CYG	-	Lebăda	",	3000	,	1420	);
			g.drawString("	DEL	-	Delfinul	",	3000	,	1460	);
			g.drawString("	DRA	-	Dragonul	",	3000	,	1500	);
			g.drawString("	EQU	-	Mânzul	",	3000	,	1540	);
			g.drawString("	FOR	-	Furnalul	",	3000	,	1580	);
			g.drawString("	GEM	-	Gemenii	",	3000	,	1620	);
			g.drawString("	HER	-	Hercule	",	3000	,	1660	);
			g.drawString("	HOR	-	Pendulul	",	3000	,	1700	);
			g.drawString("	HYA	-	Hidra	",	3000	,	1740	);
			g.drawString("	LAC	-	Șopârla	",	3000	,	1780	);
			g.drawString("	LEO	-	Leul	",	3000	,	1820	);
			g.drawString("	LEP	-	Iepurele	",	3000	,	1860	);
			g.drawString("	LIB	-	Balanța	",	3000	,	1900	);
			g.drawString("	LMI	-	Leul Mic	",	3000	,	1940	);
			g.drawString("	LYN	-	Linxul	",	3000	,	1980	);
			g.drawString("	LYR	-	Lira	",	3000	,	2020	);
			g.drawString("	MIC	-	Microscopul	",	3000	,	2060	);
			g.drawString("	MON	-	Unicornul	",	3000	,	2100	);
			g.drawString("	OPH	-	Omul cu Șarpele	",	3000	,	2140	);
			g.drawString("	ORI	-	Orion	",	3000	,	2180	);
			g.drawString("	PEG	-	Pegas	",	3000	,	2220	);
			g.drawString("	PER	-	Perseu	",	3000	,	2260	);
			g.drawString("	PHE	-	Phoenix	",	3000	,	2300	);
			g.drawString("	PSA	-	Peștii Australi	",	3000	,	2340	);
			g.drawString("	PSC	-	Peștii	",	3000	,	2380	);
			g.drawString("	PUP	-	Pupa	",	3000	,	2420	);
			g.drawString("	PYX	-	Compasul	",	3000	,	2460	);
			g.drawString("	SCL	-	Sculptorul	",	3000	,	2500	);
			g.drawString("	SCO	-	Scorpionul	",	3000	,	2540	);
			g.drawString("	SCT	-	Scutul	",	3000	,	2580	);
			g.drawString("	SER	-	Șarpele	",	3000	,	2620	);
			g.drawString("	SEX	-	Sextantul	",	3000	,	2660	);
			g.drawString("	SGE	-	Săgeata	",	3000	,	2700	);
			g.drawString("	SGR	-	Săgetătorul	",	3000	,	2740	);
			g.drawString("	TAU	-	Taurul	",	3000	,	2780	);
			g.drawString("	TRI	-	Triunghiul	",	3000	,	2820	);
			g.drawString("	UMA	-	Ursa Mare	",	3000	,	2860	);
			g.drawString("	UMI	-	Ursa Mică	",	3000	,	2900	);
			g.drawString("	VIR	-	Fecioara	",	3000	,	2940	);
			g.drawString("	VUL	-	Vulpea	",	3000	,	2980	);
			
			BufferedImage img = null;
			try {
			    img = ImageIO.read(new File("images/srpac-1-jpg.jpg"));
			} catch (IOException e) {
			}
			g.scale(0.20, 0.20);
			g.drawImage(img, 14500, 100, null);
			
			img.flush();
			
			
			
			try {
			    img = ImageIO.read(new File("images/qr-fb.jpg"));
			} catch (IOException e) {
			}
			g.scale(5, 5);

			g.drawImage(img, 50, 50, null);
			
			img.flush();
			
		/*	try {
			    img = ImageIO.read(new File("images/arcasul.png"));
			} catch (IOException e) {
			}
			g.scale(0.75, 0.75);

			g.drawImage(img, 0, 10, null);
			
			try {
			    img = ImageIO.read(new File("images/balaurul.png"));
			} catch (IOException e) {
			}

			g.drawImage(img, 500, 10, null);

			try {
			    img = ImageIO.read(new File("images/berbecul.png"));
			} catch (IOException e) {
			}

			g.drawImage(img, 1000, 10, null);
			*/
			g.setFont(new Font("TimesRoman", Font.PLAIN, 80));

			StarMapGenerator.drawCircleText(g, "Constelații Românești", new Point(1650,1260), 1260, 0.4, 0);
			
			
			
		}
		catch (IOException ex) {
		ex.printStackTrace();
		}
		finally {
			input.close();
			g.flush();
			g.close();
			finalImage.close();
		}
	}
	
	static void drawCircleText(Graphics2D g, String st, Point center,
            double r, double a1, double af)
	{
		double curangle = a1;
		double curangleSin;
		Point2D c = new Point2D.Double(center.x, center.y);
		char ch[] = st.toCharArray();
		FontMetrics fm = g.getFontMetrics();
		AffineTransform xform1, cxform;
		xform1 = AffineTransform.getTranslateInstance(c.getX(),c.getY());
		for(int i = 0; i < ch.length; i++) {
		double cwid = (double)getWidth(ch[i],fm);
		if (!(ch[i] == ' ' || Character.isSpaceChar(ch[i]))) {
			cwid = (double)(fm.charWidth(ch[i]));
			cxform = new AffineTransform(xform1);
			cxform.rotate(curangle, 0.0, 0.0);
			String chstr = new String(ch, i, 1);
			g.setTransform(cxform);
			g.drawString(chstr, (float)(-cwid/2), (float)(-r));
		}
	
		// compute advance of angle assuming cwid<<radius
		if (i < (ch.length - 1)) {
		double adv = cwid/2.0 + fm.getLeading() + getWidth(ch[i + 1],fm)/2.0;
		// Use of atan() suggested by Michael Moradzadeh
		curangle += Math.atan(adv / r);
		// Original code was:
		// curangle += Math.sin(adv / r);
	
		}
	}
		

	}
	
	  /**
     * Get the width of a given character under the
     * specified FontMetrics, interpreting all spaces as
     * en-spaces.
     */
    static int getWidth(char c, FontMetrics fm) {
        if (c == ' ' || Character.isSpaceChar(c)) {
            return fm.charWidth('n');
        }
        else {
            return fm.charWidth(c);
        }
    }
}
