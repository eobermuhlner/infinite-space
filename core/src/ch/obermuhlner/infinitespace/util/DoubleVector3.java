package ch.obermuhlner.infinitespace.util;

import com.badlogic.gdx.math.Vector3;

public class DoubleVector3 {
	public double x;
	public double y;
	public double z;
	
	public DoubleVector3() {
		this(0, 0, 0);
	}
	
	public DoubleVector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(Vector3 vector3) {
		this.x = vector3.x;
		this.y = vector3.y;
		this.z = vector3.z;
	}

	public void add(Vector3 vector3) {
		this.x += vector3.x;
		this.y += vector3.y;
		this.z += vector3.z;
	}

	public void mul(double value) {
		this.x *= value;
		this.y *= value;
		this.z *= value;
	}

	@Override
	public String toString() {
		return "[" + x + ", " + y + ", " + z + "]";
	}

	public static void setToVector3(Vector3 vector3, DoubleVector3 doubleVector3) {
		vector3.set((float) doubleVector3.x, (float) doubleVector3.y, (float) doubleVector3.z);
	}
	
	public static void setToVector3(Vector3 vector3, DoubleVector3 doubleVector3, double factor) {
		vector3.set((float) (doubleVector3.x * factor), (float) (doubleVector3.y * factor), (float) (doubleVector3.z * factor));
	}
	
	public Vector3 toVector() {
		return new Vector3((float) x, (float) y, (float) z);
	}
	
	public Vector3 toVector(double factor) {
		return new Vector3((float) (x * factor), (float) (y * factor), (float) (z * factor));
	}
	
	public static DoubleVector3 fromVector(Vector3 vector3) {
		return new DoubleVector3(vector3.x, vector3.y, vector3.z);
	}
}
