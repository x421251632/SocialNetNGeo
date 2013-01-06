package org.zju;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import processing.core.PApplet;
import processing.core.PFont;
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
	int sizeX = 1200,sizeY = 700;
	final static int TARGET_TO_HANDLER_SELECTED = 2000;
	final static int HANDLER_TO_MIDDLEMAN_SELECTED = 2001;
	final static int MIDDLEMAN_TO_LEADER_SELECTED = 2002;
	final static int TARGET_TO_HANDLER_HOVERED = 2003;
	final static int HANDLER_TO_MIDDLEMAN_HOVERED = 2004;
	final static int MIDDLEMAN_TO_LEADER_HOVERED = 2005;
	final static int TO_INNOCENT = 2006;
	Map<Integer,Integer> colorMap = new HashMap<Integer,Integer>();
	{
		colorMap.put(City.FOREIGNSMALL, color(85,104,48));
		colorMap.put(City.SMALL, color(102,153,51));
		colorMap.put(City.MID, color(20,130,180));
		colorMap.put(City.LARGE, color(150,150,50));
		colorMap.put(City.LARGER, color(150,50,50));
		colorMap.put(Person.HANDLER, color(200,200,130));
		colorMap.put(Person.TARGET, color(150,200,150));
		colorMap.put(Person.INNOCENT, color(255,255,255));
		colorMap.put(Person.LEADER, color(250,150,150));
		colorMap.put(Person.MIDDLEMAN, color(150,150,250));
		colorMap.put(TARGET_TO_HANDLER_SELECTED, color(130,130,50));
		colorMap.put(TARGET_TO_HANDLER_HOVERED, color(180,180,100));
		colorMap.put(HANDLER_TO_MIDDLEMAN_SELECTED, color(50,50,160));
		colorMap.put(HANDLER_TO_MIDDLEMAN_HOVERED, color(130,130,200));
		colorMap.put(MIDDLEMAN_TO_LEADER_SELECTED, color(160,50,50));
		colorMap.put(MIDDLEMAN_TO_LEADER_HOVERED, color(200,130,130));
		colorMap.put(TO_INNOCENT, color(130,130,130));
	}
	List<Person> middleManSuspect = new LinkedList<Person>();
	List<Person> leaderSuspect = new LinkedList<Person>();
	List<Person> handlerSuspect = new LinkedList<Person>();
	List<Person> initPerson = new LinkedList<Person>();
	Set<Person> drawnPerson = new HashSet<Person>();
	Set<Person> nextDrawnPerson = new HashSet<Person>();
	float arcGraphCenterX;
	float arcGraphCenterY;
	String toast = "";
	float alpha = 0;
	String instructions = "Strips denote the cities. Red Color is for \n" +
			"Koul, Yellow Color is for Prounov and blue \n" +
			"and green color are for other middle and \n" +
			"small cities. The length of the strip \n" +
			"depends on the number of the users belong \n" +
			"to the city. The green nodes on the strips \n" +
			"are target employees who has 30-50 links \n" +
			"and 3 contact(handler-yellow nodes) who \n" +
			"has 29-41 links and don't know each other. \n" +
			"Red nodes are big user(leader) with 100+ \n" +
			"links and a international contact. Purple \n" +
			"nodes are small user(middle man) who has \n" +
			"less than 7 contacts";
	boolean structure = true;					//true for a, false for b
	boolean graph = true;						//true for arcGraph, false for geoGraph
	
	@Override
	public void setup(){
		System.out.print(PFont.list());
		readFile();
		processInfos();
		size(sizeX,sizeY);
	}

	@Override
	public void draw(){
		background(170);
		rectMode(CORNERS);
		drawTabs();
		fill(170);
		noStroke();
		rect(0,28,width,height);
		drawCurrentTabShadow();
		fill(255);
		rect(0,30,width,height);
		drawCurrentTab();
		if(graph)
			drawCityUserArc();
		drawToast();
	}

	@Override
	public void mousePressed(){
		synchronized(drawnPerson){
			for(Person p : drawnPerson){
				if(dist(mouseX,mouseY,p.xInArcGraph,p.yInArcGraph)<5){
					p.isSelected = !p.isSelected;
				}
			}
			drawnPerson.clear();
			drawnPerson.addAll(initPerson);
		}
		if(mouseX>900&&mouseX<1020&&mouseY>5&&mouseY<35)
			graph = true;
		else if(mouseX>1010&&mouseX<1130&&mouseY>5&&mouseY<35)
			graph = false;
	}

	@Override
	public void keyPressed(){
		if(key == ' '){
			structure = !structure;
			toast = "SPACE";
			alpha = 255;
			synchronized(drawnPerson){
				drawnPerson.clear();
				drawnPerson.addAll(initPerson);
			}
		}
		else if(key == '\t'){
			graph = !graph;
			toast = "TAB";
			alpha = 255;
		}
	}
	
	void drawTabs(){
		fill(255);
		stroke(170);
		strokeWeight(2);
		quad(930,5,900,35,1050,35,1020,5);
		quad(1040,5,1010,35,1160,35,1130,5);
	}
	
	void drawCurrentTabShadow(){
		fill(170);
		noStroke();
		if(graph)
			quad(927,5,897,35,1053,35,1023,5);
		else
			quad(1037,5,1007,35,1163,35,1133,5);
	}
	
	void drawCurrentTab(){
		fill(255);
		noStroke();
		if(graph)
			quad(930,5,900,35,1050,35,1020,5);
		else
			quad(1040,5,1010,35,1160,35,1130,5);
	}
	
	void drawCityUserArc(){
		if(arcGraph==null){
			float diameter = min(width,(height-100));
			float startDegree = 0,nextDegree;
			float border = 0.01f;
			arcGraphCenterX = width/2;
			arcGraphCenterY = (height+100)/2;
			arcGraph = createGraphics(width,height,JAVA2D);
			arcGraph.beginDraw();
			arcGraph.strokeWeight(1);
			arcGraph.stroke(255);
			for(Integer id:cityOrder){
				City c = cities.get(id);
				arcGraph.fill(colorMap.get(c.citySize));
				nextDegree = startDegree + (float)(c.people.size())/totalUser * 2 *PI;
				arcGraph.arc(arcGraphCenterX, arcGraphCenterY, diameter ,diameter, startDegree+border,nextDegree-border);
				List<Person> personToDraw = new LinkedList<Person>();
				personToDraw.addAll(c.targetSuspect);
//				personToDraw.addAll(c.leaderSuspect);
				initPerson.addAll(personToDraw);
				drawnPerson.addAll(personToDraw);
				float angleStep = (nextDegree - startDegree - 3*border)/(personToDraw.size()+1);
				float tmpDegree = (float) (startDegree +  1.5* border);
				for(Person person:personToDraw){
					tmpDegree += angleStep;
					float x = (diameter-15) / 2 * cos(tmpDegree) + arcGraphCenterX;
					float y = (diameter-15) / 2 * sin(tmpDegree) + arcGraphCenterY;
					person.xInArcGraph = x;
					person.yInArcGraph = y;
//					System.out.println(c.name + " " + person.type + "x: " + x + " y:" + y);
				}
				List<Person> personNotDraw = new LinkedList<Person>();
				personNotDraw.addAll(c.people);
				personNotDraw.removeAll(personToDraw);
				angleStep = (nextDegree - startDegree - 3* border)/(personNotDraw.size()+1);
				tmpDegree = (float) (startDegree + 1.5* border);
				for(Person person:personNotDraw){
					tmpDegree += angleStep;
					float x = (diameter-15) / 2 * cos(tmpDegree) + arcGraphCenterX;
					float y = (diameter-15) / 2 * sin(tmpDegree) + arcGraphCenterY;
					person.xInArcGraph = x;
					person.yInArcGraph = y;
				}
				startDegree = nextDegree;
			}
			arcGraph.fill(255);
			arcGraph.arc(arcGraphCenterX, arcGraphCenterY, diameter-30, diameter-30, 0, 2*PI);
			arcGraph.endDraw();
		}
		else{
			image(arcGraph,0,0);
			strokeWeight(0);
			for(Person p : drawnPerson){
				p.draw(this);
			}
			drawSelectedPerson();
			drawHoveredPerson();
			drawStructrueType();
			drawArcGraphInfo();
			drawHoverdPersonInfo();
		}
	}
	
	void drawToast(){
		textSize(200);
		textAlign(CENTER,CENTER);
		alpha = (float) (alpha *0.8);
		fill(40,40,40,alpha);
		text(toast,width/2,height/2);
	}
	
	void drawArcGraphInfo(){
		textSize(13);
		textAlign(LEFT,TOP);
		fill(0);
		text(instructions,20,200);
	}
	
	void drawStructrueType(){
		textSize(200);
		textAlign(CENTER,BOTTOM);
		fill(180,120,120);
		if(structure){
			text("A",70,220);
		}
		else{
			text("B",70,220);
		}
		textSize(30);
		text("structrue",210,190);
	}
	
	void drawHoverdPersonInfo(){
		fill(0);
		textSize(20);
		textAlign(LEFT,CENTER);
		text("name",930,200);
		text("id",930,250);
		text("links",930,300);
		text("city",930,350);
		text("country",930,400);
		synchronized(drawnPerson){
			for(Person p : drawnPerson){
				if(dist(mouseX,mouseY,p.xInArcGraph,p.yInArcGraph)<10){
					String name = p.name;
					int id = p.id;
					int links = p.linkTable.size();
					String cityName = p.city.name;
					String countryName = p.country.name;
					text(name,1080,200);
					text(id,1080,250);
					text(links,1080,300);
					text(cityName,1080,350);
					text(countryName,1080,400);
					break;
				}
			}
		}
	}

	void drawHoveredPerson(){
		strokeWeight(1);
		stroke(255);
		synchronized(drawnPerson){
			for(Person p : drawnPerson){
				if(dist(mouseX,mouseY,p.xInArcGraph,p.yInArcGraph)<10){
					if(p.type == Person.TARGET){
						drawTargetRelationship(p,true);
					}
					else if(p.type == Person.LEADER){
						for(Person contact:p.linkTable){
							if(p.country!=contact.country){
								stroke(colorMap.get(MIDDLEMAN_TO_LEADER_HOVERED));
								p.drawConnection(this, contact);
								contact.draw(this);
							}
						}
					}
					else if(p.type == Person.MIDDLEMAN){
						for(Person contact:p.linkTable){
							if(contact.type == Person.INNOCENT){
								stroke(colorMap.get(TO_INNOCENT));
							}
							else if(contact.type == Person.LEADER){
								stroke(colorMap.get(MIDDLEMAN_TO_LEADER_HOVERED));
							}
							else if(contact.type == Person.HANDLER){
								stroke(colorMap.get(HANDLER_TO_MIDDLEMAN_HOVERED));
							}
							else if(contact.type == Person.TARGET){
								stroke(colorMap.get(HANDLER_TO_MIDDLEMAN_HOVERED));
							}
							p.drawConnection(this, contact);
							contact.draw(this);
						}
					}
				}
			}
		}
	}
	
	void drawSelectedPerson(){
		strokeWeight(1);
		stroke(0);
		synchronized(drawnPerson){
			nextDrawnPerson.clear();
			nextDrawnPerson.addAll(drawnPerson);
			for(Person p : drawnPerson){
				if(p.isSelected){
					if(p.type == Person.TARGET){
						drawTargetRelationship(p,false);
					}
					else if(p.type == Person.LEADER){
						for(Person contact:p.linkTable){
							if(p.country!=contact.country){
								stroke(colorMap.get(MIDDLEMAN_TO_LEADER_SELECTED));
								p.drawConnection(this, contact);
								nextDrawnPerson.add(contact);
								contact.draw(this);
							}
						}
					}
					else if(p.type == Person.MIDDLEMAN){
						for(Person contact:p.linkTable){
							if(contact.type == Person.INNOCENT){
								stroke(colorMap.get(TO_INNOCENT));
							}
							else if(contact.type == Person.LEADER){
								stroke(colorMap.get(MIDDLEMAN_TO_LEADER_SELECTED));
							}
							else if(contact.type == Person.HANDLER){
								stroke(colorMap.get(HANDLER_TO_MIDDLEMAN_SELECTED));
							}
							else if(contact.type == Person.TARGET){
								stroke(colorMap.get(HANDLER_TO_MIDDLEMAN_SELECTED));
							}
							p.drawConnection(this, contact);
							contact.draw(this);
						}
					}
				}
			}
			drawnPerson.clear();
			drawnPerson.addAll(nextDrawnPerson);
		}
	}
	
	void drawTargetRelationship(Person p,boolean isHover){
		strokeWeight(1);
		List<Person> handlersFound = new LinkedList<Person>();
		for(Person contact:p.linkTable){
//			if(contact.type == Person.HANDLER||contact.type == Person.TARGET ){
			if(isSuspectHandler(contact)){
				if(isHover)
					stroke(colorMap.get(TARGET_TO_HANDLER_HOVERED));
				else
					stroke(colorMap.get(TARGET_TO_HANDLER_SELECTED));
				p.drawConnection(this, contact);
				contact.draw(this);
				nextDrawnPerson.add(contact);
				handlersFound.add(contact);
			}
		}
		if(structure){				//A
			for(Person middle:middleManSuspect){
				List<Person> handlerOfMiddel = new LinkedList<Person>();
				for(Person handler : handlersFound){
					if(handler.linkTable.contains(middle)){
						handlerOfMiddel.add(handler);
					}
				}
				if(handlerOfMiddel.size() >= 3){
					middle.draw(this);
					nextDrawnPerson.add(middle);
					for(Person handler: handlerOfMiddel){
						if(isHover)
							stroke(colorMap.get(HANDLER_TO_MIDDLEMAN_HOVERED));
						else
							stroke(colorMap.get(HANDLER_TO_MIDDLEMAN_SELECTED));
						middle.drawConnection(this, handler);
					}
					for(Person leader: middle.linkTable){
						if(leader.type == Person.LEADER){
							leader.draw(this);
							nextDrawnPerson.add(leader);
							if(isHover)
								stroke(colorMap.get(MIDDLEMAN_TO_LEADER_HOVERED));
							else
								stroke(colorMap.get(MIDDLEMAN_TO_LEADER_SELECTED));
							middle.drawConnection(this, leader);
						}
					}
				}
			}
		}
		else{				//B
			for(Person leader:leaderSuspect){
				List<Person> middleManOfLeader = new LinkedList<Person>();
				for(Person handler:handlersFound){
					for(Person middle:middleManSuspect){
						if(handler.linkTable.contains(middle)&&leader.linkTable.contains(middle)&&!middleManOfLeader.contains(middle)){
							middleManOfLeader.add(middle);
							break;
						}
					}
				}
				if(middleManOfLeader.size()>=3){
					nextDrawnPerson.add(leader);
					leader.draw(this);
					for(Person middle:middleManOfLeader){
						middle.draw(this);
						nextDrawnPerson.add(middle);
						if(isHover)
							stroke(colorMap.get(MIDDLEMAN_TO_LEADER_HOVERED));
						else
							stroke(colorMap.get(MIDDLEMAN_TO_LEADER_SELECTED));
						middle.drawConnection(this, leader);
						for(Person handler:handlersFound){
							if(handler.linkTable.contains(middle)){
								if(isHover)
									stroke(colorMap.get(HANDLER_TO_MIDDLEMAN_HOVERED));
								else
									stroke(colorMap.get(HANDLER_TO_MIDDLEMAN_SELECTED));
								handler.drawConnection(this, middle);
							}
						}
					}
				}
			}
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

			
			if(isSuspectHandler(person)){
				person.type = Person.HANDLER;
				person.city.handlerSuspect.add(person);
				handlerSuspect.add(person);
			}
			else if(isSuspectLeader(person)){
				person.type = Person.LEADER;
				person.city.leaderSuspect.add(person);
				leaderSuspect.add(person);
			}
			else if(isSuspectMiddleMan(person)){
				person.type = Person.MIDDLEMAN;
				person.city.middleManSuspect.add(person);
				middleManSuspect.add(person);
			}
		}	
		for(Person person:people.values()){

			if(isTarget(person)){
				person.type = Person.TARGET;
				person.city.targetSuspect.add(person);
			}
			
		}
	}
	
	boolean isSuspectHandler(Person person){
		if(person.linkTable.size()<=41&&person.linkTable.size()>=29&&person.country==countries.get(6013))
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
		if(isSuspectMiddleMan(person)){
			int numOfHandler = 0;
			boolean hasLeader = false; 
			for(Person contact: person.linkTable){
				if(contact.type == Person.HANDLER)
					numOfHandler++;
				if(contact.type == Person.LEADER)
					hasLeader = true;
			}
			if(numOfHandler >= 3 && hasLeader)
				return true;
		}
		return false;
	}
	
	boolean isTarget(Person person){
		if(person.linkTable.size()>=30&&person.linkTable.size()<=50){
			List<Person> handlersOfHim = new LinkedList<Person>();
			for(Person contact:person.linkTable){
				if(isSuspectHandler(contact)){
					handlersOfHim.add(contact);
				}
			}
			if(handlersOfHim.size()<3)
				return false;
			for(Person a:handlersOfHim){
				for(Person b:handlersOfHim){
					for(Person c:handlersOfHim){
						if(a!=b&&b!=c&&c!=a){
							if(a.linkTable.contains(b) || b.linkTable.contains(c) || c.linkTable.contains(a))
								return false;
							else
								return true;
						}
					}
				}
			}
		}
		return false;
	}
}
