import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Graphics2D;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import javax.swing.JFrame;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.gl2.GLUT;

import astro.MessierData;
import astro.ObjectData;
import astro.PolarProjectionMap;
import astro.PolarProjectionMap.MilkyWayPoint;
import astro.util.Date;
import texture.TextureHandler;

/**
 * Simple JOGL class for displaying a Polar Projection Map. All the JOGL code is in here.
 * 
 * @author Marc Frincu
 * @since 2009
 *  Modified Nov 2011: switched to JOGL 2.0, created 3D planet info and centered the Messier info around the object
 *  Modified Oct 2017: added MilkyWay and possibility to run in both in web and stand alone mode (file loading). Fixed bug making objects to be off by few degrees. Added star sizes based on magnitude. Added option to hide constellation lines and names.
 *  Modified Jul 2021: fixed bug making canvas smaller than window size and causing issues with mouse positioning
 */

//"c:\Program Files\Java\jdk1.8.0_101\bin\keytool.exe" -selfcert -alias starchart -keystore starchart -validity 365
//"c:\Program Files\Java\jdk1.8.0_101\bin\jarsigner.exe" -keystore starchart starchart_20171027.jar starchart
public class MainFrame extends JFrame implements GLEventListener, KeyListener, MouseListener {
	private static final long serialVersionUID = 1L;
	private GLCanvas canvas;
	private Animator animator;

	private GLUT glut = null;

	// Hold the windows size and position
	private int wX = 0, wY = 0, wW = 512, wH = 512;
	
	// Holds a reference to an object responsible for handling the IO and
	// computations required for displaying the Polar Projection Map.
	private PolarProjectionMap ppm = null;
	// Use this in case display lists are preferred. The code is commented in the init method.
	private double v_size = 1;

	// The id of the selected object.
	private int idToShow;
	// The choice whether to show or not the info on the selected object.
	private boolean showInfo = false;
	
	// The choice whether to show or not the constellation lines.
	private boolean showConLines = false;
	
	// The choice whether to shoe the info text or not (constellation and Messier names)
	private boolean showAllNames = true;

	// Variables for moving the scene. Simple scaling and translation, no camera
	// movement.
	private float scaleX = 1, scaleY = 1, scaleZ = 1, posX = 0, posY = 0, posZ = 0;
	private boolean reset = true;

	// Variables for storing the mouse coordinates when a click event occurs.
	private int mouseX, mouseY;
	// Default mode is GL_RENDER;
	private int mode = GL2.GL_RENDER;

	// Lists for storing the textures.
	ArrayList<TextureHandler> textures = null, texturesSS = null,texturesSSMap = null;

	public MainFrame() throws Exception {
		super("Harta Astronomica v1.2 (c) 2009 - 2021, Marc E. Frincu - marc.frincu@e-uvt.ro (Societatea Romana pentru Astronomie Culturala)");

		TimeZone tz1 = TimeZone.getTimeZone("GMT");
	    TimeZone tz2 = TimeZone.getTimeZone("Europe/Bucharest");
	    long timeDifference = Math.abs(tz1.getRawOffset() - tz2.getRawOffset() + tz1.getDSTSavings() - tz2.getDSTSavings());
		
	    //System.out.println(timeDifference/3600000);//3600 secs * 1000 ms
	    
	   // ppm = new PolarProjectionMap(2020, 7, 14, 22+58/60.+48/3600., 21.42, 45.7);
		ppm = new PolarProjectionMap(21.42, 45.7, timeDifference/3600000);
		ppm.setFileSep(",");
		ppm.initializeConstellationLines("data/conlines.dat");
		ppm.initializeConstellationStars("data/constellation-lines-2.csv");
		ppm.initializeConstellationNames("data/cnames.dat");
		ppm.initializeConstellationBoundaries("data/cbounds.dat");
		ppm.initializeMessierObjects("data/messier.dat");
		ppm.initializeMilkyWayCoutour("data/milkyway.csv");

		ppm.initializeSolarSystemObjects();

		this.initializeJogl();
	}

	public void run() {		
		this.setPreferredSize(new Dimension(this.wW, this.wH));
		this.pack();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		this.setVisible(true);
		//this.setExtendedState(Frame.MAXIMIZED_BOTH);  
		this.canvas.requestFocusInWindow();
	}

	private void initializeJogl() {
		GLProfile glprofile = GLProfile.get(GLProfile.GL2);
		// Creating an object to manipulate OpenGL parameters.
		GLCapabilities capabilities = new GLCapabilities(glprofile);

		// Setting some OpenGL parameters.
		capabilities.setHardwareAccelerated(true);
		capabilities.setDoubleBuffered(true);
		capabilities.setStereo(true);
		
		// Creating an OpenGL display widget -- canvas.
		this.canvas = new GLCanvas();
		this.canvas.setSize(new Dimension (this.wW, this.wH));

		// Adding the canvas in the center of the frame.
		this.getContentPane().add(this.canvas);

		// Adding an OpenGL event listener to the canvas.
		this.canvas.addGLEventListener(this);
		this.canvas.addKeyListener(this);
		this.canvas.addMouseListener(this);

		// Creating an animator that will redraw the scene 40 times per second.
		this.animator = new Animator(this.canvas);
					
		// Starting the animator.
		this.animator.start();
	}
	
	private GLU glu;

	public void init(GLAutoDrawable canvas) {
		// Obtaining the GL2 instance associated with the canvas.
		GL2 gl = canvas.getGL().getGL2();


		this.glu = GLU.createGLU();
		this.glut = new GLUT();

		// Setting the clear color -- the color which will be used to erase the canvas.
		gl.glClearColor(0, 0, 0, 0);

		// Selecting the projection matrix.
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);

		// Initializing the projection matrix with the identity matrix.
		gl.glLoadIdentity();

		// Setting the projection to be orthographic.
		// Selecting the view volume to be x from 0 to 1, y from 0 to 1, z from -1 to 1.
		gl.glOrtho(-this.v_size, this.v_size, -this.v_size, this.v_size, -1, 1);

		// Selecting the modelview matrix.
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

		// Activate the GL_LINE_SMOOTH state variable. Other options include
		// GL_POINT_SMOOTH and GL_POLYGON_SMOOTH.
		gl.glEnable(GL2.GL_LINE_SMOOTH);

		// Activate the GL_BLEND state variable. Means activating blending.
		gl.glEnable(GL2.GL_BLEND);

		// Set the blend function. For anti-aliasing it is set to GL_SRC_ALPHA
		// for the source and GL_ONE_MINUS_SRC_ALPHA for the destination pixel.
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

		// Control GL_LINE_SMOOTH_HINT by applying the GL_DONT_CARE behavior.
		// Other behaviors include GL_FASTEST or GL_NICEST.
		gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);

		/*
		 * this.ppm_list = gl.glGenLists(1); gl.glNewList(this.ppm_list,
		 * GL.GL_COMPILE); this.makePPM(gl); gl.glEndList();
		 */

		// Choose the shading model.
		gl.glShadeModel(GLLightingFunc.GL_SMOOTH);

		// Load the Messier textures.
		textures = new ArrayList<TextureHandler>();
		TextureHandler tex = null;
		for (int i = 1; i <= 110; i++) {
			tex = new TextureHandler(gl, glu, "images/m" + i + ".jpg", false);
			textures.add(tex);
		}

		// Load the Solar System textures.
		texturesSS = new ArrayList<TextureHandler>();
		tex = new TextureHandler(gl, glu, "images/sun.jpg", false);
		texturesSS.add(tex);
		tex = new TextureHandler(gl, glu, "images/mercury.jpg", false);
		texturesSS.add(tex);
		tex = new TextureHandler(gl, glu, "images/venus.jpg", false);
		texturesSS.add(tex);
		tex = new TextureHandler(gl, glu, "images/moon.jpg", false);
		texturesSS.add(tex);
		tex = new TextureHandler(gl, glu, "images/mars.jpg", false);
		texturesSS.add(tex);
		tex = new TextureHandler(gl, glu, "images/jupiter.jpg", false);
		texturesSS.add(tex);
		tex = new TextureHandler(gl, glu, "images/saturn.jpg", false);
		texturesSS.add(tex);
		tex = new TextureHandler(gl, glu, "images/uranus.jpg", false);
		texturesSS.add(tex);
		tex = new TextureHandler(gl, glu, "images/neptune.jpg", false);
		texturesSS.add(tex);

		texturesSSMap = new ArrayList<TextureHandler>();
		tex = new TextureHandler(gl, glu, "images/sun_map.jpg", false);
		texturesSSMap.add(tex);
		tex = new TextureHandler(gl, glu, "images/mercury_map.jpg", false);
		texturesSSMap.add(tex);
		tex = new TextureHandler(gl, glu, "images/venus_map.jpg", false);
		texturesSSMap.add(tex);
		tex = new TextureHandler(gl, glu, "images/moon_map.jpg", false);
		texturesSSMap.add(tex);
		tex = new TextureHandler(gl, glu, "images/mars_map.jpg", false);
		texturesSSMap.add(tex);
		tex = new TextureHandler(gl, glu, "images/jupiter_map.jpg", false);
		texturesSSMap.add(tex);
		tex = new TextureHandler(gl, glu, "images/saturn_map.jpg", false);
		texturesSSMap.add(tex);
		tex = new TextureHandler(gl, glu, "images/uranus_map.jpg", false);
		texturesSSMap.add(tex);
		tex = new TextureHandler(gl, glu, "images/neptune_map.jpg", false);
		texturesSSMap.add(tex);
	}

	public void display(GLAutoDrawable canvas) {
		GL2 gl = canvas.getGL().getGL2();
		
		if (this.mode == GL2.GL_RENDER) {
			// only clear the buffers when in GL_RENDER mode. Avoids flickering
			gl.glClear(GL.GL_COLOR_BUFFER_BIT);
			this.drawScene(gl);
		} else {
			this.pickHandle(gl, this.mouseX, this.mouseY);
		}
	}

	double dpiScalingFactor = 1;
	public void reshape(GLAutoDrawable canvas, int left, int top, int width,
			int height) {
		GL2 gl = canvas.getGL().getGL2();

		/*this.wX = left;
		this.wY = top;*/
		
		//System.out.println(width +" " +height);
		
		// Hack to scale canvas to full window size. See http://forum.jogamp.org/canvas-not-filling-frame-td4040092.html 
		dpiScalingFactor = ((Graphics2D) getGraphics()).getTransform().getScaleX();
		width = (int) (width * dpiScalingFactor);
		height = (int) (height * dpiScalingFactor);

		// Selecting the viewport -- the display area -- to be the entire widget.
		gl.glViewport(0, 0, width, height); 
		
		this.wH = height;
		this.wW = width;

		// Determining the width to height ratio of the widget.
		double ratio = (double) width / (double) height;

		// Selecting the projection matrix.
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);

		gl.glLoadIdentity();
		if (ratio < 1)
			gl.glOrtho(-v_size, v_size, -v_size, v_size / ratio, -1, 1);
		else
			gl.glOrtho(-v_size, v_size * ratio, -v_size, v_size, -1, 1);

		// Selecting the modelview matrix.
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
	}

	public void displayChanged(GLAutoDrawable canvas, boolean modeChanged,
			boolean deviceChanged) {
		return;
	}

	private void drawScene(GL2 gl) {
		gl.glLoadIdentity();
		
		//gl.glViewport(0, 0, this.wW, this.wH);
		
		gl.glTranslatef(posX, posY, posZ);
		gl.glScalef(scaleX, scaleY, scaleZ);
		// gl.glCallList(this.ppm_list);
		this.makePPM(gl);

		if (this.showInfo)
			this.showInfo(gl, this.idToShow);

		this.showTime(gl);		
		
		// Force the scene to be rendered.
		gl.glFlush();
	}

	/**
	 * This method is responsible for drawing the Polar Projection Map and
	 * contents
	 * 
	 * @param gl
	 */
	private void makePPM(GL2 gl) {
		float size = 0.005f;
		int i = 0;
		TextureHandler tex = null;
		final ArrayList<PolarProjectionMap.ConstellationLine> clLines = ppm
				.getConLines();
		final ArrayList<PolarProjectionMap.ConstellationName> clNames = ppm
				.getConNames();
		final ArrayList<PolarProjectionMap.ConstellationStar> clStars = ppm
				.getConStars();
		final ArrayList<PolarProjectionMap.ConstellationBoundaryLine> clBoundaries = ppm
				.getConBoundaryLines();
		final ArrayList<ObjectData> ssObjects = ppm.getPInfo().getObjects();
		final ArrayList<MessierData> messObjects = ppm.getMessData();
		final ArrayList<MilkyWayPoint> mwPoints = ppm.getMilkyWayPoints();

		
		ArrayList<ObjectData> ecliptic= this.ppm.updateEcliptic();		
		
		gl.glPushMatrix();
			gl.glColor3f(0.0f, 0.2f, 0.2f);
			gl.glBegin(GL2.GL_LINES);
				for (int j=0; j<ecliptic.size()-1;j++) {
					if (ecliptic.get(j).isVisiblePP() && ecliptic.get(j+1).isVisiblePP()) {
						gl.glVertex2d(ecliptic.get(j).getXpp(), ecliptic.get(j).getYpp());
						gl.glVertex2d(ecliptic.get(j+1).getXpp(), ecliptic.get(j+1).getYpp());
					}
				}
			gl.glEnd();
		gl.glPopMatrix();

		
		if (this.showConLines) { 
			gl.glPushMatrix();
				// Draw the constellation lines.
				gl.glColor3f(.9f, 0.0f, 0.0f);
				gl.glLineStipple(1, (short) 0x07);
				gl.glBegin(GL2.GL_LINES);
					for (PolarProjectionMap.ConstellationLine cl : clLines) {
						if (cl.isVisible()) {
							gl.glVertex2d(cl.getPosX1(), cl.getPosY1());
							gl.glVertex2d(cl.getPosX2(), cl.getPosY2());
			
						}
					}
				gl.glEnd();
			gl.glPopMatrix();
		}
		
		gl.glEnable(GL2.GL_LINE_STIPPLE);
		gl.glPushMatrix();
			// Draw the constellation boundaries.        
			gl.glColor3f(.8f, .8f, 0.0f);
			gl.glLineStipple(1, (short) 0x3F07);
			gl.glBegin(GL2.GL_LINES);
				for (PolarProjectionMap.ConstellationBoundaryLine cb : clBoundaries) {
					if (cb.isVisible()) {
						gl.glVertex2d(cb.getPosX1(), cb.getPosY1());
						gl.glVertex2d(cb.getPosX2(), cb.getPosY2());
		
					}
				}
			gl.glEnd();
		gl.glPopMatrix();
		gl.glDisable(GL2.GL_LINE_STIPPLE);
		
		gl.glPushMatrix();
		// Draw the stars.
			gl.glColor3f(1.0f, 0.0f, 0.0f);

			gl.glEnable(GL2.GL_POINT_SMOOTH);
			int x=0;
			for (PolarProjectionMap.ConstellationStar cs : clStars) {
				if (cs.isVisible()) {
					x++;
					gl.glPointSize((float)(2*(7-cs.getMag())));
					gl.glColor3f(1.0f, 0.0f, 0.0f);
					gl.glBegin(GL.GL_POINTS);
						gl.glVertex2d(cs.getPosX(), cs.getPosY());
					gl.glEnd();
					
					if (this.showAllNames && (cs.getName().compareTo("alfa") == 0 || (cs.getName().compareTo("beta") == 0))) {
						gl.glColor3f(1.0f, 1.0f, 1.0f);
						gl.glRasterPos2d(cs.getPosX(), cs.getPosY());
						glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_10, cs.getName());
					}
				}
			}
			gl.glDisable(GL2.GL_POINT_SMOOTH);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		// Draw the Milky Way.
			gl.glColor3f(0.0f, 1.0f, 0.0f);
			gl.glBegin(GL.GL_LINES);
			PolarProjectionMap.MilkyWayPoint mwp = null;
				for (int index = 0; index<mwPoints.size(); index++) {
					mwp = mwPoints.get(index);
					if (mwp.isVisible()) {
						if (mwp.getRa() == Double.MIN_VALUE && mwp.getDec() == Double.MIN_VALUE) {
							gl.glEnd();
							gl.glBegin(GL.GL_LINES);
							//System.out.println("NEW LINE LOOP");
						}
						else {
							if (index > 1 && mwPoints.get(index-1).isVisible()) {
								gl.glVertex2d(mwPoints.get(index-1).getX(), mwPoints.get(index-1).getY());
							//	System.out.println("PREV " +(index-1) + " " + mwPoints.get(index-1).getX() + " " + mwPoints.get(index-1).getY() + " " + mwPoints.get(index-1).isVisible()) ;

								gl.glVertex2d(mwp.getX(), mwp.getY());
							//	System.out.println(index +" " + mwp.getX() + " " + mwp.getY() + " " + mwp.isVisible()) ;
							}
						}
					}
					
				}
			gl.glEnd();
		gl.glPopMatrix();
		

		if (this.showAllNames) {
			gl.glPushMatrix();
			// Draw the constellation names.
			gl.glColor3f(0.0f, 0.0f, 1.0f);
				for (PolarProjectionMap.ConstellationName cn : clNames) {
					if (cn.isVisible()) {
						gl.glRasterPos2d(cn.getPosX(), cn.getPosY());
						glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_10, cn.getName());
					}
				}
			gl.glPopMatrix();
		}

		gl.glPushMatrix();
			// Draw the Messier objects.
			gl.glColor3f(0.0f, 1.0f, 1.0f);
			for (MessierData mo : messObjects) {
				if (mo.isVisible()) {
	
					if (mode == GL2.GL_SELECT) {
						// Push on the name stack the name (id) of the object.
						gl.glPushName(Integer.parseInt(mo.getName().substring(1)) - 1 + 10);
					}
	
					gl.glDisable(GL.GL_BLEND);
					
					tex = textures.get(Integer.parseInt(mo.getName().substring(1)) - 1);										
					tex.bind();
					tex.enable();
	
					gl.glBegin(GL2.GL_QUADS);
						gl.glTexCoord2d(0, 0);
						gl.glVertex2d(mo.getX() - size, mo.getY() - size);
						gl.glTexCoord2d(1, 0);
						gl.glVertex2d(mo.getX() + size, mo.getY() - size);
						gl.glTexCoord2d(1, 1);
						gl.glVertex2d(mo.getX() + size, mo.getY() + size);
						gl.glTexCoord2d(0, 1);
						gl.glVertex2d(mo.getX() - size, mo.getY() + size);	
					gl.glEnd();
					
					tex.disable();
					gl.glEnable(GL.GL_BLEND);
	
					if (this.mode == GL2.GL_SELECT) {
						// Pop from the name stack the name (id) of the object.
						gl.glPopName();
					}
	
					if (this.showAllNames) {
						gl.glRasterPos2d(mo.getX(), mo.getY());
						glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_10, mo.getName());
					}
					i++;
				}
			}
		gl.glPopMatrix();

		gl.glPushMatrix();
			// Draw the Solar System objects.
			size = 0.01f;
			i = 0;
			gl.glColor3f(1.0f, 1.0f, 1.0f);
			for (ObjectData o : ssObjects) {
				if (o.isVisiblePP()) {
	
					if (this.mode == GL2.GL_SELECT) {
						// Push on the name stack the name (id) of the sphere.
						gl.glPushName(i);
					}

					gl.glDisable(GL.GL_BLEND);
					
					tex = texturesSS.get(i);
					tex.bind();
					tex.enable();
	
					gl.glBegin(GL2.GL_QUADS);
						gl.glTexCoord2d(0, 0);
						gl.glVertex2d(o.getXpp() - size, o.getYpp() - size);
						gl.glTexCoord2d(1, 0);
						gl.glVertex2d(o.getXpp() + size, o.getYpp() - size);
						gl.glTexCoord2d(1, 1);
						gl.glVertex2d(o.getXpp() + size, o.getYpp() + size);
						gl.glTexCoord2d(0, 1);
						gl.glVertex2d(o.getXpp() - size, o.getYpp() + size);	
					gl.glEnd();
					
					tex.disable();
					gl.glEnable(GL.GL_BLEND);
	
					if (mode == GL2.GL_SELECT) {
						// Pop from the name stack the name (id) of the sphere.
						gl.glPopName();
					}
	
					if (this.showAllNames) {
						gl.glRasterPos2d(o.getXpp(), o.getYpp());
						glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_10, o.getName());
					}
				}
				i++;
			}
		gl.glPopMatrix();

		// Draw the cardinal points.
		final PolarProjectionMap.NorthPoint np = ppm.getNorthP();
		final PolarProjectionMap.SouthPoint sp = ppm.getSouthP();
		final PolarProjectionMap.EastPoint ep = ppm.getEastP();
		final PolarProjectionMap.WestPoint wp = ppm.getWestP();

		gl.glPushMatrix();
			// Draw map contour
			double rad = Math.sqrt(Math.pow(np.getPosX(),2) + Math.pow(np.getPosY(),2));		
			gl.glColor3f(0.4f,0.4f,0.1f);
			gl.glBegin(GL2.GL_LINES);
				for (int j=0;j<360;j++) {
					gl.glVertex2d(rad * Math.sin(j * Math.PI / 180), rad * Math.cos(j * Math.PI / 180));
				}
			gl.glEnd();
		gl.glPopMatrix();
		
		gl.glPushMatrix();		
			gl.glColor3f(1.0f, 1.0f, 1.0f);
			gl.glRasterPos2d(np.getPosX(), np.getPosY());
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "N");
	
			gl.glRasterPos2d(wp.getPosX(), wp.getPosY()-0.06);
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "V");
	
			gl.glRasterPos2d(ep.getPosX(), ep.getPosY());
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "E");
	
			gl.glRasterPos2d(sp.getPosX()-0.05, sp.getPosY());
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "S");
		gl.glPopMatrix();
		
		/*
		// Draw the ecliptic
		double coseps = Math.cos(23.439 * Math.PI / 180.0);
		double sineps = Math.sin(23.439 * Math.PI / 180.0);
		// Vvalues at lambda = 0
		double x = -90, y = 0, alpha, delta, x1, y1;
		// Each value of ecliptic longitude is treated as a point with zero
		// ecliptic latitude and converted to the appropriate RA and DEC and
		// then plotted in the usual way. The J2000 value for obliquity of
		// the ecliptic is used....
		gl.glPushMatrix();
		//gl.glRotatef(90,0,0,1);
		gl.glColor3f(0f,1f,0f);
		gl.glBegin(GL.GL_LINES);
		for (int lambda=1; lambda<=360; lambda++) {
			alpha = Math.atan2(Math.sin(lambda * Math.PI / 180.0) * coseps, Math.cos(lambda * Math.PI / 180.0));
			delta = Math.asin(sineps * Math.sin(lambda * Math.PI / 180.0)) * 180.0 / Math.PI;
			// Now find the polar coords and plot the line
			x1 = -(90 - delta) * Math.cos(alpha);
			y1 = (90 - delta) * Math.sin(alpha);
			gl.glVertex2d(x * Math.PI / 180.0,y * Math.PI / 180.0);
			gl.glVertex2d(x1 * Math.PI / 180.0,y1 * Math.PI / 180.0);
			x = x1;
			y = y1;
		}
		gl.glEnd();
		gl.glPopMatrix();
 		*/
		// plot another small cross marking the zenith for the
		// observer. The zenith will always be at 0 RA and a polar
		// angle equal to 90 - latitude.
		/*gl.glColor3f(1f,1f,1f);
		double x = -(90 - this.ppm.getLat() * 180 / Math.PI) * 1 * Math.PI / 180.0;
		double y = (90 - this.ppm.getLat() * 180 / Math.PI) * 0 * Math.PI / 180.0;
		gl.glBegin(GL.GL_LINES);
			gl.glVertex2d(x-0.05,y);
			gl.glVertex2d(x+0.05,y);
			gl.glVertex2d(x,y-0.05);
			gl.glVertex2d(x,y+0.05);

		gl.glEnd();*/

		// Update the coordinates for the next display.
		final double hour = this.ppm.getDate().getHour();
		final int day = this.ppm.getDate().getDay();
		final int month = this.ppm.getDate().getMonth();
		final int year = this.ppm.getDate().getYear();

		if (this.reset == true)
			this.ppm.update();
		else
			this.ppm.update(hour, day, month, year);		
	}

	/**
	 * This method is responsible for displaying information related with date
	 * and time
	 * 
	 * @param gl
	 */
	private void showTime(GL2 gl) {

		final Date date;
		
		if (this.reset == false)
			date = this.ppm.getDate();//new Date(this.ppm.getLongitude(), this.ppm.getTimeDiff());
		else
			date = new Date(this.ppm.getLongitude(), this.ppm.getTimeDiff());
		
		gl.glPushMatrix();
			gl.glLoadIdentity();
			gl.glColor3f(1, 1, 1);	
			if (this.reset == false) {			
				gl.glRasterPos2d(-v_size + 0.05, 1.0);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " Data: "
						+ date.getDay() + "-" + date.getMonth() + "-" + date.getYear());
				gl.glRasterPos2d(-v_size + 0.05, 0.95);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " Ora: "
					+ date.getHour() + ":" + date.getMinute() + ":" + date.getSecond() + " (UT "
					+ ((this.ppm.getTimeDiff() > 0) ? "+" : "")
					+ this.ppm.getTimeDiff() + ")");
			}
			else {
				gl.glRasterPos2d(-v_size + 0.05, 1.0);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " Data: "
						+ Date.getCurrentDate());
				gl.glRasterPos2d(-v_size + 0.05, 0.95);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " Ora: "+
						Date.getCurrentTime() + " (TU "
						+ ((this.ppm.getTimeDiff() > 0) ? "+" : "")
						+ this.ppm.getTimeDiff() + ")");
			}
			//gl.glRasterPos2d(-v_size + 0.05, 0.90);
			//glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " Day no.: "
					//+ date.getDayNumber());
			// gl.glRasterPos2d(-v_size +0.05, 0.85);
			// glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " LST: " +
			// date.getLST() * 180 / Math.PI / 15.04107);
		gl.glPopMatrix();
	}

	double angle = 0;
	
	/**
	 * This method is responsible for displaying information related with the
	 * selected object
	 * 
	 * @param gl
	 * @param id
	 */
	private void showInfo(GL2 gl, int id) {
		DecimalFormat df = new DecimalFormat("###.##");
		
		gl.glPushMatrix();
			gl.glLoadIdentity();
		
			// We need to show the information near the point we clicked on:			
			// Get the viewport.
			int[] viewport = new int[4];
			gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
			// Get the projection matrix.
			double[] projection = new double[16];
			gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projection, 0);
			// Get the modelview matrix.
			double[] modelview = new double[16];
			gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, modelview, 0);
			
			double x, y;
			// this vector will hold the world Cartesian coordinates of the point we clicked on
			double[] uprjC = new double[3];
			
			if (id < 10) {
				ObjectData o = this.ppm.getPInfo().getObjects().get(id);
				x = o.getXpp();
				y = o.getYpp();
			}
			else {
				MessierData o = this.ppm.getMessData().get(id - 10);
				x = o.getX();
				y = o.getY();
			}
			
			glu.gluUnProject(this.mouseX, viewport[3] - this.mouseY - 1, 0., modelview, 0, projection, 0, viewport, 0, uprjC, 0);			
			
			gl.glColor3f(1, 1, 1);
			
			double pos = uprjC[1] + 0.30;
	
			if (id >= 10) {
				this.textures.get(id - 10).bind();
				this.textures.get(id - 10).enable();
				
				gl.glBegin(GL2.GL_QUADS);
					gl.glTexCoord2d(0, 0);
					gl.glVertex2d(uprjC[0] - 0.35f, uprjC[1] - 0.35f);
					gl.glTexCoord2d(1, 0);
					gl.glVertex2d(uprjC[0] - 0.35f, uprjC[1] + 0.35f);
					gl.glTexCoord2d(1, 1);
					gl.glVertex2d(uprjC[0] + .35f, uprjC[1] + .35f);
					gl.glTexCoord2d(0, 1);
					gl.glVertex2d(uprjC[0] + 0.35f, uprjC[1] - 0.35f);
				gl.glEnd();
		
				this.textures.get(id - 10).disable();
				
				gl.glColor3f(0, 0.8f, 0);
				gl.glBegin(GL2.GL_LINE_LOOP);
					gl.glVertex2d(uprjC[0] - 0.355f, uprjC[1] - 0.355f);
					gl.glVertex2d(uprjC[0] - 0.355f, uprjC[1] + 0.355f);
					gl.glVertex2d(uprjC[0] + 0.355f, uprjC[1] + 0.355f);
					gl.glVertex2d(uprjC[0] + 0.355f, uprjC[1] - 0.355f);
				gl.glEnd();
			}
						
			if (id < 10) {
				ObjectData o = this.ppm.getPInfo().getObjects().get(id);
						
				// Set a small viewport in the upper right corner of the window to display the rotating solar system object and info
				gl.glViewport(this.wW/2, this.wH/2, this.wW/2, this.wH/2);
				gl.glLoadIdentity();

				// Set the projection to be perspective instead of orthographic
				gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
				// Store the old matrix. We will need it after we're done drawing
				gl.glPushMatrix();
				gl.glLoadIdentity();
				
				glu.gluPerspective (38, (double)this.wW / this.wH, 0.1, 100);
				
				gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
				// Only clear the depth buffer. We stiil want the projection map to be there when drawing
				gl.glClear(GL.GL_DEPTH_BUFFER_BIT );
				
				// We want to illuminate the planets but not the Sun
				if (id != 0) {			
					gl.glEnable(GL2.GL_LIGHTING);
		            gl.glEnable(GL2.GL_LIGHT0);
		            gl.glEnable(GL2.GL_LIGHT1);
		            
		            // The vector arguments represent the R, G, B, A values.
		            gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, new float [] {0.2f, 0.0f, 0.0f, 1f}, 0);
		            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, new float [] {0.9f, 0.9f, 0.9f, 1f}, 0);
		            // The vector arguments represent the x, y, z, w values of the position.
		            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float [] {-10, 0, 0, 1f}, 0);
				}
				
	            gl.glEnable(GL.GL_DEPTH_TEST);
	            gl.glDepthFunc(GL.GL_LESS);
					           
				glu.gluLookAt (5.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
				
				GLUquadric sphere = glu.gluNewQuadric();			
				GLUquadric disk = glu.gluNewQuadric();				
		        glu.gluQuadricTexture(sphere, true);
	
		        gl.glPushMatrix();
			        gl.glRotated(angle++, 0,1,0);
			        gl.glRotated(90, 1,0,0);
			        if (id == 6)
			        	gl.glRotated(20, 1,0,0);
		            	        
			        TextureHandler tex = texturesSSMap.get(id);
					tex.bind();
					tex.enable();
		
					glu.gluSphere(sphere, 0.5, 64, 64);			
					
					tex.disable();
					
					if (id == 6) {
						gl.glRotated(20, 1,0,0);
						glu.gluDisk(disk ,0.7f, 1.1f, 32, 32);
					}
					
					gl.glDisable(GL.GL_DEPTH_TEST);				
				gl.glPopMatrix();
				
				if (id != 0){			
					gl.glDisable(GL2.GL_LIGHTING);
		            gl.glDisable(GL2.GL_LIGHT0);
		            gl.glDisable(GL2.GL_LIGHT1);
				}
				
				if (id == 0)
					gl.glColor3f(0, 0, 1);				
				
				gl.glRasterPos2d(1., 0.1);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10,	"Obiect: " + o.getName());
				gl.glRasterPos2d(1.5, 0.2);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " Magnitudine ap.: "	+ df.format(o.getMagnitude()));
				gl.glRasterPos2d(1., 0.3);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " Faza: " + df.format(o.getPhase()));
				gl.glRasterPos2d(1., 0.4);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " Diam. ap.: " + df.format(o.getAppDiameter()));
				gl.glRasterPos2d(1., 0.5);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " Elongatie: " + df.format(o.getElongation()));
				gl.glRasterPos2d(1., 0.6);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " Rasarit: " + df.format(o.getRiseTime()) + " h ");
				gl.glRasterPos2d(1., 0.7);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " Tranzit: " + df.format(o.getTransitTime()) + " h ");
				gl.glRasterPos2d(1., 0.8);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " Apus: " + df.format(o.getSetTime()) + " h");	
				
				// Restore the old projection
				gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
				gl.glPopMatrix();
	
				// Restore the viewport
				gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
				gl.glViewport(this.wX, this.wY, this.wW, this.wH);
				
			} else {
				MessierData o = this.ppm.getMessData().get(id - 10);
	
				gl.glRasterPos2d(uprjC[0] - 0.30f, pos);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, "Obiect: " + o.getName());
				pos -= 0.05;
				gl.glRasterPos2d(uprjC[0] - 0.30f, pos);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " Magnitudine ap.: " + df.format(o.getMagnitude()));
				pos -= 0.05;
				gl.glRasterPos2d(uprjC[0] - 0.30f, pos);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " Ascensie Dr.: " + df.format(o.getRA()) + " h ");
				pos -= 0.05;
				gl.glRasterPos2d(uprjC[0] - 0.30f, pos);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, " Declinatie: " + df.format(o.getDec()) + " * ");
			}
		gl.glPopMatrix();
	}
	
	/**
	 * This method is responsible for handling the picking
	 * 
	 * @param gl
	 * @param x
	 *            the screen X coordinate
	 * @param y
	 *            the screen Y coordinate
	 */
	private void pickHandle(GL2 gl, int x, int y) {
		// Calculate the select buffer capacity and allocate data if necessary
		final int bufferSize = 10;
		final int capacity = Buffers.SIZEOF_INT * bufferSize;
		IntBuffer selectBuffer = Buffers.newDirectIntBuffer(capacity);

		// Send the select buffer to (J)OGL and use select mode to track object hits.
		gl.glSelectBuffer(selectBuffer.capacity(), selectBuffer);

		gl.glRenderMode(GL2.GL_SELECT);

		// Initialize the name stack.
		gl.glInitNames();

		// Get the viewport.
		int[] viewport = new int[4];
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		// Get the projection matrix.
		float[] projection = new float[16];
		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projection, 0);

		// Switch to the projection matrix mode.
		gl.glMatrixMode(GL2.GL_PROJECTION);
		// Save the current projection matrix.
		gl.glPushMatrix();
		// Reset the projection matrix.
		gl.glLoadIdentity();

		// Restrict region to pick object only in this region.
		glu.gluPickMatrix(x, viewport[3] - y, 1, 1, viewport, 0);

		// Load the projection matrix
		gl.glMultMatrixf(projection, 0);

		// Or redefine the perspective again.
		// glu.gluPerspective ( 38, (float) screenWidth / (float) screenHeight,
		// 0.1, 100 );

		// Go back to modelview matrix for rendering.
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		// Render the scene. Note we are in GL_SELECT mode now.
		this.drawScene(gl);

		gl.glMatrixMode(GL2.GL_PROJECTION);
		// Restore the saved projection matrix.
		gl.glPopMatrix();

		// Select the modelview matrix.
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		// Return to GL_RENDER mode.
		final int hits = gl.glRenderMode(GL2.GL_RENDER);
		mode = GL2.GL_RENDER;

		// Process the hits.
		processHits(gl, hits, selectBuffer);
	}

	/**
	 * This method is responsible for processing the hits. Retrieved from:
	 * http://user.cs.tu-berlin.de/~schabby/PickingExample.java
	 * 
	 * @param gl
	 * @param hits
	 * @param buffer
	 */
	private void processHits(GL gl, int hits, IntBuffer buffer) {
		int offset = 0;
		int names;
		float z1, z2;

		for (int i = 0; i < hits; i++) {
			names = buffer.get(offset);
			offset++;
			z1 = (float) buffer.get(offset) / 0x7fffffff;
			offset++;
			z2 = (float) buffer.get(offset) / 0x7fffffff;
			offset++;
			
			for (int j = 0; j < names; j++) {
				this.idToShow = buffer.get(offset);
				this.showInfo = true;
				offset++;
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent event) {

		if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
			System.exit(0);
		}
		
		if (event.getKeyCode() == KeyEvent.VK_UP) {
			this.posY += 0.05;
		}

		if (event.getKeyCode() == KeyEvent.VK_DOWN) {
			this.posY -= 0.05;
		}

		if (event.getKeyCode() == KeyEvent.VK_L) {
			this.showConLines = !this.showConLines;
		}
		
		if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
			this.posX -= 0.05;
		}

		if (event.getKeyCode() == KeyEvent.VK_LEFT) {
			this.posX += 0.05;
		}

		if (event.getKeyCode() == KeyEvent.VK_I) {
			if (this.scaleX < 5) {
				this.scaleX += 0.05;
				this.scaleY += 0.05;
				this.scaleZ += 0.05;
			}
		}
		
		if (event.getKeyCode() == KeyEvent.VK_H) {
			this.showAllNames = !this.showAllNames;
		}		

		if (event.getKeyCode() == KeyEvent.VK_O) {
			if (this.scaleX > 0.5) {
				this.scaleX -= 0.05;
				this.scaleY -= 0.05;
				this.scaleZ -= 0.05;
			}
		}

		if (event.getKeyCode() == KeyEvent.VK_R) {
			this.scaleX = 1;
			this.scaleY = 1;
			this.scaleZ = 1;
			this.posX = 0;
			this.posY = 0;
			this.posZ = 0;
			this.reset = true;
		}
		
		if (event.getKeyCode() == 46){ // >
			this.reset = false;
			double hour = this.ppm.getDate().getHour();
			int day = this.ppm.getDate().getDay();
			int month = this.ppm.getDate().getMonth();
			int year = this.ppm.getDate().getYear();
			hour += 1;
			if (hour > 24 ) {
				day += 1;
				hour = 0;
				switch (month){
				case 1:
				case 3:
				case 5:
				case 7:
				case 8:
				case 10:
					if (day > 31) {
						month +=1;
						day = 1;
					}
					break;
				case 12:
					if (day > 31) {
						month = 1;
						day = 1;
						year += 1;
					}
					break;
				case 2:
					if (day > 28) {
						month +=1;
						day = 1;
					}
					break;
				case 4:
				case 6:
				case 9:
				case 11:
					if (day > 30) { 
						month += 1;
						day = 1;
					}
					break;
				}
			}
			this.ppm.update(hour, day, month, year);
		}

		if (event.getKeyCode() == 44){ // <
			this.reset = false;
			double hour = this.ppm.getDate().getHour();
			int day = this.ppm.getDate().getDay();
			int month = this.ppm.getDate().getMonth();
			int year = this.ppm.getDate().getYear();
			hour -= 1;
			if (hour < 0 ) {
				day -= 1;
				hour = 24;
				switch (month){
				case 3:
					if (day < 1) {
						month -= 1;
						day = 28;
					}
					break;
				case 1:
					if (day < 1) {
						month = 12;
						day = 31;
						year -= 1;
					}
					break;
				case 5:
				case 7:
				case 8:
				case 10:
				case 12:
					if (day < 1) {
						month -= 1;
						day = 30;
					}
					break;
				case 2:
				case 4:
				case 6:
				case 9:
				case 11:
					if (day < 1) { 
						month -= 1;
						day = 31;
					}
					break;
				}
			}
			this.ppm.update(hour, day, month, year);		
		}

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent me) {
		mouseX = (int)(me.getX() * dpiScalingFactor);
		mouseY = (int)(me.getY() * dpiScalingFactor);
		mode = GL2.GL_SELECT;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		this.showInfo = false;
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}
}
