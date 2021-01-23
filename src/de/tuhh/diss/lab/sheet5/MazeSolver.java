package de.tuhh.diss.lab.sheet5;

import java.text.SimpleDateFormat;
import java.util.Date;

import MazebotSim.MazebotSimulation;
import MazebotSim.Visualization.GuiMazeVisualization;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

public class MazeSolver {


	private static RegulatedMotor leftMotor;
	private static RegulatedMotor rightMotor;
	private static EV3GyroSensor gyrSens;
	private static EV3UltrasonicSensor distSens;
	private static String wantedColor;
	private static String foundColor;
	private static String foundColorFront;
	private static boolean colorFound;
	private static boolean loopOfDeath;                  //is set to false initaly since we dont think that the goal wall will be left to the starting position
	private static RobotStarter robotStarter;
	private static SimpleBeeper simpleBeeper;
	private static ColorDetector colorDetector;
	private static GyroTurner turner;
	private static Drive driver;
	
	
	private static void initializeHardware() {
		rightMotor = new EV3LargeRegulatedMotor(MotorPort.C);
		leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);
		gyrSens = new EV3GyroSensor(SensorPort.S3);
		distSens = new EV3UltrasonicSensor(SensorPort.S4);
		foundColorFront = foundColor = "NONE";
	}
	
	private static String checkWallColor() {
		driver.approachTileEdge(true);
		foundColor = colorDetector.getColor();
		if (foundColor != "NONE") {
			driver.adjustTileEdgeDist();
		} else {
			driver.approachTileEdge(false);
		}
		return foundColor;
	}
	
	private static boolean solvingMaze() {
				
		
		foundColorFront = checkWallColor();             //check front wall
		colorFound = false;
	
		if (foundColor == wantedColor) {						
			simpleBeeper.playBeep();
			colorFound = true;
		} else if(loopOfDeath) {
			turner.turnCCW();
			foundColor = checkWallColor();                //check left wall	
			if (foundColor == wantedColor) {
				simpleBeeper.playBeep();
				colorFound = true;
			} else if (foundColor == "NONE") {				
				driver.driveTileForward();              // drives left if left Wall is not wanted Color but is None (no wall)
				loopOfDeath = true;
			} else {									
				turner.turnCW();                        // turns right relativ, front absolut
				if (foundColorFront == "NONE") {
					driver.driveTileForward();          // drives forward
					loopOfDeath = true;
				} else {
					turner.turnCW();
					loopOfDeath = false;
				}
			}
		} else {
			if (foundColorFront == "NONE") {
				driver.driveTileForward();
				loopOfDeath = true;
			} else {
				turner.turnCW();
				loopOfDeath = false;
			}
		}
		return colorFound;
	}

	public static void main(String[] args) {
		MazebotSimulation sim = new MazebotSimulation("Mazes/Mazes_4x4_1.png", 1.4 , 1.4);
		GuiMazeVisualization gui = new GuiMazeVisualization(1.4, sim.getStateAccessor());
		sim.scaleSpeed(1);
		sim.setRobotPosition(0.175, 0.175, 90);

		sim.startSimulation();
		gui.startVisualization();
		
		
		robotStarter = new RobotStarter();
		simpleBeeper = new SimpleBeeper();
		colorDetector = new ColorDetector();
		initializeHardware();
		turner = new GyroTurner(leftMotor, rightMotor, gyrSens);
		driver = new Drive(leftMotor, rightMotor, distSens);
		robotStarter.startRobot();
		simpleBeeper.playBeep();
		wantedColor = robotStarter.getColor();
		
		// adjust the maximum Motor Speed
		System.out.println(rightMotor.getMaxSpeed());
		
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
		Date date = new Date(System.currentTimeMillis());
		System.out.println("Start: " + formatter.format(date));

		while (colorFound == false) {
			colorFound = solvingMaze();
		}
		
		date = new Date(System.currentTimeMillis());
		System.out.println("End: " + formatter.format(date));

		Delay.msDelay(100);
		sim.stopSimulation();

	}

}
