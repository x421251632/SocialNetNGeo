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
	int type = INNOCENT;
	float xInArcGraph;
	float yInArcGraph;
	void draw(SocialNetNGeo pa){
		pa.fill(pa.colorMap.get(type));
		pa.ellipse(xInArcGraph, yInArcGraph, 5, 5);
	}
	
	void drawConnection(PGraphics pg,Person p){
		
	}
}
