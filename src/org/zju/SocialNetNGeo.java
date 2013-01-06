package org.zju;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PGraphics;

@SuppressWarnings("serial")
public class SocialNetNGeo extends PApplet{

//6001	Ryzkland	city	s
//6002	Kouvnic	city	m
//6003	Solvenz	city	m
//6004	Kannvic	city	m
//6005	Prounov	city	l
//6006	Koul	city	ll
//6007	Solank	city	s
//6008	Pasko	city	s
//6009	Sresk	city	s
//6010	Otello	city	s
//6011	Transpasko	city	s
//6012	Tulamuk	city	s
//6013	Flovania	country
//6014	Trium	country
//6015	Posana	country
//6016	Transak	country
	
	int totalUser;
	Map<Integer,Person> people = new HashMap<Integer,Person>();
	Map<Integer,City> cities = new HashMap<Integer,City>();				//index is id
	Map<String, City> nameCities = new HashMap<String,City>();			//index is name
	Map<Integer,Country> countries = new HashMap<Integer,Country>();
	List<Integer> userCtnLevels = new ArrayList<Integer>();							//the user whose link counts over the level;
	{
		userCtnLevels.add(0);
		userCtnLevels.add(6);
		userCtnLevels.add(7);
		userCtnLevels.add(30);
		userCtnLevels.add(40);
		userCtnLevels.add(100);
		userCtnLevels.add(6001);
	}
	List<Integer> cityOrder = new LinkedList<Integer>();
	{
		cityOrder.add(6006);
		cityOrder.add(6005);
		cityOrder.add(6002);
		cityOrder.add(6003);
		cityOrder.add(6004);
		cityOrder.add(6001);
		cityOrder.add(6007);
		cityOrder.add(6008);
		cityOrder.add(6009);
		cityOrder.add(6010);
		cityOrder.add(6011);
		cityOrder.add(6012);
	}
	PGraphics arcGraph = null;
	int sizeX = 800,sizeY = 600;
	Map<Integer,Integer> colorMap = new HashMap<Integer,Integer>();
	{
		colorMap.put(City.FOREIGNSMALL, color(85,104,48));
		colorMap.put(City.SMALL, color(102,153,51));
		colorMap.put(City.MID, color(20,130,180));
		colorMap.put(City.LARGE, color(150,150,50));
		colorMap.put(City.LARGER, color(150,50,50));
		colorMap.put(Person.HANDLER, color(250,250,150));
		colorMap.put(Person.INNOCENT, color(255,255,255));
		colorMap.put(Person.LEADER, color(250,150,150));
		colorMap.put(Person.MIDDLEMAN, color(150,150,250));
	}
	List<Person> middleManSuspect = new LinkedList<Person>();
	List<Person> leaderSuspect = new LinkedList<Person>();
	List<Person> handlerSuspect = new LinkedList<Person>();
	
	@Override
	public void setup(){
		System.out.print("setup");
		readFile();
		processInfos();
		size(sizeX,sizeY);
	}

	@Override
	public void draw(){
		background(255);
		drawCityUserTable();
		drawCityUserArc();
	}

	Person pa;
	Person pb;
	void drawCityUserArc(){
		float diameter = 600;
		float startDegree = 0,nextDegree;
		float border = 0.01f;
		float centerX = width/2;
		float centerY = height/2;
//		boolean flag = false;
		if(arcGraph==null){
			arcGraph = createGraphics(width,height,JAVA2D);
			arcGraph.beginDraw();
			arcGraph.strokeWeight(1);
			arcGraph.stroke(255);
			for(Integer id:cityOrder){
				City c = cities.get(id);
				arcGraph.fill(colorMap.get(c.citySize));
				nextDegree = startDegree + (float)(c.people.size())/totalUser * 2 *PI;
				arcGraph.arc(centerX, centerY, diameter ,diameter, startDegree+border,nextDegree-border);
				List<Person> personToDraw = new LinkedList<Person>();
				personToDraw.addAll(c.handlerSuspect);
				personToDraw.addAll(c.leaderSuspect);
				float angleStep = (nextDegree - startDegree)/(personToDraw.size()+1);
				float tmpDegree = startDegree;
				for(Person person:personToDraw){
					tmpDegree += angleStep;
					float x = (diameter-10) / 2 * cos(tmpDegree) + centerX;
					float y = (diameter-10) / 2 * sin(tmpDegree) + centerY;
					person.xInArcGraph = x;
					person.yInArcGraph = y;
					person.draw(this);
					System.out.println(c.name + " " + person.type + "x: " + x + " y:" + y);
				}
				List<Person> personNotDraw = new LinkedList<Person>();
				personNotDraw.addAll(c.people);
				personNotDraw.removeAll(personToDraw);
				tmpDegree = (nextDegree - startDegree)/(personNotDraw.size()+1);
				for(Person person:personNotDraw){
					tmpDegree += angleStep;
					float x = (diameter-10) / 2 * cos(tmpDegree) + centerX;
					float y = (diameter-10) / 2 * sin(tmpDegree) + centerY;
					person.xInArcGraph = x;
					person.yInArcGraph = y;
				}
				startDegree = nextDegree;
			}
			arcGraph.fill(255);
			arcGraph.arc(width/2, height/2, diameter-30, diameter-30, 0, 2*PI);
			arcGraph.endDraw();
		}
		else{
			image(arcGraph,0,0);
//			strokeWeight(2);
//			noFill();
//			bezier(pa.xInArcGraph, pa.yInArcGraph, centerX, centerY, centerX, centerY, pb.xInArcGraph, pb.yInArcGraph);
		}
	}
	
	
	void drawCityUserTable(){
		fill(0);
		textAlign(CENTER, CENTER);
		textSize(14);
		int i = 0;
		for(City c:cities.values()){
			text(c.name,100,i*20+50);
			for(int j = 0;j<c.userCtn.length;j++){
				text(c.userCtn[j],150+j*50,i*20+50);
			}
			i++;
		}
	}
	
	void readFile(){
		//load names and ids
		String lines[] = loadStrings("Entities_Table.txt");
		for(int i = 2;i < lines.length;i++){
			String words[] = lines[i].split("\t");
			int id = Integer.parseInt(words[0]);
			String name = words[1];
			String type = words[2];
			if(type.equals("person")){
				Person p = new Person();
				p.name = name;
				p.id = id;
				people.put(id, p);
			}
			else if(type.equals("city")){
				City c = new City();
				c.name = name;
				c.id = id;
				cities.put(id, c);
				nameCities.put(name, c);
			}
			else if(type.equals("country")){
				Country c = new Country();
				c.name = name;
				c.id = id;
				countries.put(id, c);
			}
		}
		
		//load people's links and city's country
		lines = loadStrings("Links_Table.txt");
		for(int i = 2;i < lines.length;i++){
			if(i>= lines.length-12){			//city -> country
				String words[] = lines[i].split("\t");
				int cityId = Integer.parseInt(words[0]);
				int countryId = Integer.parseInt(words[1]);
				City city = cities.get(cityId);
				Country country = countries.get(countryId);
				city.country = country;
			}
			else{				//people's links
				String words[] = lines[i].split("\t");
				int sPersonId = Integer.parseInt(words[0]);
				int tPersonId = Integer.parseInt(words[1]);
				Person sPerson = people.get(sPersonId);
				Person tPerson = people.get(tPersonId);
				sPerson.linkTable.add(tPerson);
				tPerson.linkTable.add(sPerson);
			}
		}
		
		//load person's city
		lines = loadStrings("People-Cities.txt");
		for(int i = 2;i < lines.length;i++){
			String words[] = lines[i].split("\t");
			int personId = Integer.parseInt(words[0]);
			Person person = people.get(personId);
			City city = nameCities.get(words[1]);
			person.city = city;
			person.country = city.country;
			city.people.add(person);
		}
	}

	//6001	Ryzkland	city	s
	//6002	Kouvnic	city	m
	//6003	Solvenz	city	m
	//6004	Kannvic	city	m
	//6005	Prounov	city	l
	//6006	Koul	city	ll
	//6007	Solank	city	s
	//6008	Pasko	city	s
	//6009	Sresk	city	s
	//6010	Otello	city	fs
	//6011	Transpasko	city	fs
	//6012	Tulamuk	city	fs
	void processInfos(){
		totalUser = 0;
		//generate person hasInternationalContact
		for(Person p: people.values()){
			totalUser ++;
			if(p.hasInternationalContact) continue;
			for(Person contact:p.linkTable){
				if(p.country!=contact.country){
					p.hasInternationalContact = true;
					contact.hasInternationalContact = true;
					break;
				}
			}
		}
		
		//generate city size
		cities.get(6001).citySize = City.SMALL;
		cities.get(6002).citySize = City.MID;
		cities.get(6003).citySize = City.MID;
		cities.get(6004).citySize = City.MID;
		cities.get(6005).citySize = City.LARGE;
		cities.get(6006).citySize = City.LARGER;
		cities.get(6007).citySize = City.SMALL;
		cities.get(6008).citySize = City.SMALL;
		cities.get(6009).citySize = City.SMALL;
		cities.get(6010).citySize = City.FOREIGNSMALL;
		cities.get(6011).citySize = City.FOREIGNSMALL;
		cities.get(6012).citySize = City.FOREIGNSMALL;
		//generate city userCtn
		for(City city:cities.values()){
			city.userCtn =  new int[userCtnLevels.size()-1];
			for(Person person:people.values()){
				if(person.city == city){
					for(int i = 0;i < userCtnLevels.size()-1;i++){
						if(person.linkTable.size()>=userCtnLevels.get(i)&&person.linkTable.size()<userCtnLevels.get(i+1)){
							city.userCtn[i]++;
							break;
						}
					}
				}
			}
		}
		
		//get the suspect
		for(Person person:people.values()){
			if(isSuspectMiddleMan(person)){
				person.type = Person.MIDDLEMAN;
				person.city.middleManSuspect.add(person);
				middleManSuspect.add(person);
			}
			else if(isSuspectHandler(person)){
				person.type = Person.HANDLER;
				person.city.handlerSuspect.add(person);
				handlerSuspect.add(person);
			}
			else if(isSuspectLeader(person)){
				person.type = Person.LEADER;
				person.city.leaderSuspect.add(person);
				leaderSuspect.add(person);
			}
		}	
	}
	
	boolean isSuspectHandler(Person person){
		if(person.linkTable.size()<=40&&person.linkTable.size()>=30&&person.country==countries.get(6013))
			return true;
		return false;
	}
	
	boolean isSuspectMiddleMan(Person person){
		if(person.linkTable.size()<6)
			return true;
		return false;
	}
	
	boolean isSuspectLeader(Person person){
		if(person.linkTable.size()>=100&&person.hasInternationalContact&&person.country==countries.get(6013)){
			return true;
		}
		return false;
	}
	
	boolean isMiddleMan(Person person){
		for(Person contact: person.linkTable){
			
		}
		return false;
	}
}
