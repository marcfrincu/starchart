package camera;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;



/**
 * Simple class for handling camera movement.
 * @author Marc Frincu
 * @since 2009
 */
final public class Camera {
	// Define camera variables
	public float cameraAzimuth = 1.0f, cameraSpeed = 0.0f, cameraElevation = 0.0f;
	 
	// Set camera at (0, 0, -20)
	private float cameraCoordsPosx = 0.0f, cameraCoordsPosy = 0.0f, cameraCoordsPosz = -20.0f;
	 
	// Set camera orientation
	private float cameraUpx = 0.0f, cameraUpy = 1.0f, cameraUpz = 0.0f;
	 
	public void moveCamera()
	{
		float[] tmp = polarToCartesian(cameraAzimuth, cameraSpeed, cameraElevation);
	 
		// Replace old x, y, z coords for camera
		cameraCoordsPosx += tmp[0];
		cameraCoordsPosy += tmp[1];
		cameraCoordsPosz += tmp[2];
	}
	 
	public void aimCamera(GL2 gl, GLU glu)
	{
		gl.glLoadIdentity();
	 
		// Calculate new eye vector
		float[] tmp = polarToCartesian(cameraAzimuth, 100.0f, cameraElevation);
	 
		// Calculate new up vector
		float[] camUp = polarToCartesian(cameraAzimuth, 100.0f, cameraElevation + 90);
	 
		cameraUpx = camUp[0];
		cameraUpy = camUp[1];
		cameraUpz = camUp[2];
	 
		glu.gluLookAt(cameraCoordsPosx, cameraCoordsPosy, cameraCoordsPosz,
				cameraCoordsPosx + tmp[0], cameraCoordsPosy + tmp[1],
				cameraCoordsPosz + tmp[2], cameraUpx, cameraUpy, cameraUpz);
	}
	 
	private float[] polarToCartesian (float azimuth, float length, float altitude)
	{
		float radian_correction = 3.14159265358979323846f / 180.0f;
		float[] result = new float[3];
		float x, y, z;
	 
		// Do x-z calculation
		float theta = (90 - azimuth) * radian_correction;
		float tantheta = (float) Math.tan(theta);
		float radian_alt = altitude * radian_correction;
		float cospsi = (float) Math.cos(radian_alt);
	 
		x = (float) Math.sqrt((length * length) / (tantheta * tantheta + 1));
		z = tantheta * x;
	 
		x = -x;
	 
		if (((azimuth >= 180.0) && (azimuth <= 360.0)) || (azimuth == 0.0f)) {
			x = -x;
			z = -z;
		}
	 
		// Calculate y, and adjust x and z
		y = (float) (Math.sqrt(z * z + x * x) * Math.sin(radian_alt));
	 
		if (length < 0) {
			x = -x;
			z = -z;
			y = -y;
		}
	 
		x = x * cospsi;
		z = z * cospsi;
	 
		result[0] = x;
		result[1] = y;
		result[2] = z;
	 
		return result;
	}
}
