package org.zju;

import java.util.LinkedList;
import java.util.List;

public class City {
	public static int SMALL = 1;
	public static int MID = 2;
	public static int LARGE = 3;
	public static int LARGER = 4;
	public static int FOREIGNSMALL = 5;
	
	
	int id;
	String name;
	Country country;
	List<Person> people = new LinkedList<Person>();
	List<Person> handlerSuspect = new LinkedList<Person>();
	List<Person> middleManSuspect = new LinkedList<Person>();
	List<Person> leaderSuspect = new LinkedList<Person>();
	List<Person> targetSuspect = new LinkedList<Person>();
	int citySize;
	int[] userCtn;
}
