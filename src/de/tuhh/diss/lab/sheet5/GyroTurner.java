package de.tuhh.diss.lab.sheet5;

import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.hardware.sensor.EV3GyroSensor;

public class GyroTurner implements Turner{
	
	private static final double E = -5; //epsilon
	private static final double END = -2; 
	private final int ANGULAR_VELOCITY = 1000;
	private RegulatedMotor leftMotor;
	private RegulatedMotor rightMotor;
	private EV3GyroSensor gyrSens;
	

	public GyroTurner(RegulatedMotor leftMotor, RegulatedMotor rightMotor, EV3GyroSensor gyrSens) {
		
		this.leftMotor= leftMotor;
		this.rightMotor = rightMotor;
		this.gyrSens = gyrSens;
		Delay.msDelay(750);
		this.gyrSens.reset();
		Delay.msDelay(750);		
	}

	
	private float getAngle () {
		
		float angleValue[] = new float[1];
		SampleProvider angle = gyrSens.getAngleMode();
		angle.fetchSample(angleValue, 0);
		return Math.abs(angleValue[0]);	 
	}
	
	
	private double calcDelta (int deg) {
		
		double delta;
		
		if (deg>0) {
			delta = getAngle() - deg;
			return delta;
		}
		else {
			delta = getAngle() + deg;
			return delta;
		}
	}
	
	
	private void turnCW(int deg) {
		
		rightMotor.backward();
		leftMotor.forward();
		Delay.msDelay(50);
		controlTurn(deg);
	}
	
	
	private void turnCCW(int deg) {
		
		leftMotor.backward();
		rightMotor.forward();
		Delay.msDelay(50);
		controlTurn(deg);
	}
	
	
	private void controlTurn(int deg) {

		while (calcDelta(deg) < END) { 
			Delay.msDelay(20);
			if(calcDelta(deg) > 5*E ) {                  //5*epsilon interval set to decrease speed when reached
				setSpeed((int)0.999*ANGULAR_VELOCITY);
			}
			System.out.println("In turner 1 " + calcDelta(deg));
			System.out.println("Motor Speed (left, right): " + leftMotor.getSpeed() + " r: " + rightMotor.getSpeed());
			// if (calcDelta(deg) == 0)break;              //the loop is not breaking without this statement
		}

		rightMotor.stop();
		leftMotor.stop();
		gyrSens.reset();
	}
	
	
	private void setSpeed(int angluarVelocity) {
		
		rightMotor.setSpeed(angluarVelocity);
		leftMotor.setSpeed(angluarVelocity);
	}
	
	
	public void turn(int degrees) {

		setSpeed(ANGULAR_VELOCITY);		
		if (degrees>0) {
			turnCCW(degrees);
		}
		else if(degrees<0){
			turnCW(degrees);
		}
	}
}
