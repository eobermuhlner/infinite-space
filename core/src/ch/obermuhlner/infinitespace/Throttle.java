package ch.obermuhlner.infinitespace;

import ch.obermuhlner.infinitespace.util.MathUtil;

public class Throttle {

	private boolean started;
	
	private boolean valid;
	
	public float throttle;
	
	public float throttleFactor = 0.01f;
	
	public float value;
	
	public float minThrottle = -1.0f;
	public float maxThrottle = 1.0f;
	
	private final float throttleThreshold = 0.7f;
	
	public void start() {
		if (!started) {
			valid = true;
			started = true;
		}
	}
	
	public void stop() {
		started = false;
		valid = false;
	}
	
	public void setThrottle(float throttle) {
		this.throttle = throttle;
	}
	
	public void update(float deltaTime) {
		if (valid) {
			float oldSign = Math.signum(value);
			
			value += throttle * throttleFactor * deltaTime;
			value = MathUtil.clamp(value, minThrottle, maxThrottle);
			
			if (Math.abs(throttle) > throttleThreshold) {
				float newSign = Math.signum(value);
				if (oldSign != 0 && oldSign != newSign) {
					valid = false;
					value = 0;
				}
			}
		} else {
			value = MathUtil.clamp(value, minThrottle, maxThrottle);
		}
	}
}
