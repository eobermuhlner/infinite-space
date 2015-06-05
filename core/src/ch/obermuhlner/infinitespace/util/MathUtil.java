package ch.obermuhlner.infinitespace.util;

public class MathUtil {

	public static float clamp(float value, float min, float max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	public static double clamp(double value, double min, double max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	public static float smoothstep (float edge0, float edge1, float x) {
		float clamped = clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
		return clamped * clamped * (3 - 2 * clamped);
	}

	
	public static float transform(float fromMin, float fromMax, float toMin, float toMax, float value) {
		if (value < fromMin) {
			return toMin;
		}
		if (value > fromMax) {
			return toMax;
		}
		return (value - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin;
	}
}
