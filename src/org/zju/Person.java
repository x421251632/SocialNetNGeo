package org.zju;

import java.util.LinkedList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PGraphics;

public class Person {
	public static int HANDLER = 1001;
	public static int TARGET = 1002;
	public static int MIDDLEMAN = 1003;
	public static int LEADER = 1004;
	public static int INNOCENT = 1000;
	int id;
	String name;
	City city;
	Country country;
	List<Person> linkTable = new LinkedList<Person>();
	boolean hasInternationalContact = false;
	boolean isSelected = false;
	int type = INNOCENT;
	float xInArcGraph;
	float yInArcGraph;
	float annotationSize;
	void draw(SocialNetNGeo pa){
		if(isSelected){
			annotationSize = 15;
			pa.stroke(0);
			pa.strokeWeight(1);
		}
		else{
			annotationSize = 8;
			pa.stroke(255);
			pa.strokeWeight(1);
		}
		pa.fill(pa.colorMap.get(type));
		pa.ellipse(xInArcGraph, yInArcGraph, annotationSize, annotationSize);
	}
	
	void drawConnection(SocialNetNGeo pa,Person p){
		pa.noFill();
		pa.bezier(xInArcGraph, yInArcGraph, pa.arcGraphCenterX, pa.arcGraphCenterY, pa.arcGraphCenterX, pa.arcGraphCenterY, p.xInArcGraph, p.yInArcGraph);
	}
	
}
