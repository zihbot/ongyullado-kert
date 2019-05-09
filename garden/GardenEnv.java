// Environment code for project garden.mas2j
import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class GardenEnv extends Environment {	
	public static final int GHeight = 4; //grid height
	public static final int GWidth = 6; //grid width
	
	public static final int PLANT = 8; //plant code in grid model
	public static final int WEED = 16; //weed code in grid model
	public static final int FIRE = 4; //fire code in grid model
	public static final int BURNT = 32;
	
	public static final int OBSERVER = 0; //observer agent id
	public static final int PLANTER = 1; //planter agent id
	public static final int WEEDERS = 2; //weeders agent id
	public static final int SPRINKLER = 3; //sprinkler agent id
	
	public Timer timer;
	private GardenModel model;
	private GardenView view;
	private JFrame frame;	
	private JLabel temperatureLabel;
	private int temperature;	
	private Logger logger = Logger.getLogger("garden.mas2j."+GardenEnv.class.getName());

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
		super.init(args);				
		model = new GardenModel();
		view = new GardenView(model);
		model.setView(view);	
		setup();		
		timer = new Timer();
		//every 15 seconds the observer searches for weed
		//every 15 seconds there is a 1/5 probability of new weed
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run(){	
				//while model has BURNT object, weeders agent can't go back to its normal behaviour
				if(!hasBurnt()){
					addPercept(Literal.parseLiteral("needWeedSearch"));
				}										
				Random rand = new Random();
				boolean val = rand.nextInt(5) == 0;
				if(val){
					createWeed();
				}
			}
		}, 15 * 1000, 15 * 1000);	
		//every 10 seconds try to spread fire
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run(){								
				spreadFire();							
			}
		}, 10 * 1000, 10 * 1000);	
    }
	private void setup(){		
		frame = new JFrame("Garden controller");
		frame.setPreferredSize(new Dimension(400, 200));
		frame.setLayout(new GridLayout(2, 2, 10, 10));//rows, cols, hgap, vgap --> col ignored 		
		JButton fire = new JButton("Fire");
		fire.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.out.println("Fire button clicked");
				createFire();				
			}
		});
		JButton plant = new JButton("Plant");
		plant.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.out.println("Plant button clicked");
				addPercept(Literal.parseLiteral("needPlant"));				
			}
		});
		JButton sprinkle = new JButton("Water");
		sprinkle.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.out.println("Water button clicked");		
				addPercept(Literal.parseLiteral("needWatering"));
			}
		});				
		//TODO: temperature = getTemperature(); --> other method to periodically refresh the variable & text in the temperatureLabel
		temperature = 0;
		temperatureLabel = new JLabel("Temperature: " + temperature);	
		frame.add(fire);
		frame.add(plant);
		frame.add(sprinkle);
		frame.add(temperatureLabel);
		frame.pack();
		frame.setVisible(true);
	}
	
	void updatePercepts(){		
		clearPercepts();
		clearPercepts("observer");			
		for(int i = 0; i < GWidth; i++){
			for(int j = 0; j < GHeight; j++){
				Location loc = new Location(i, j);
				if (model.hasObject(PLANT, loc)) {
					Literal pos = Literal.parseLiteral("pos(plant," + loc.x + "," + loc.y + ")");
					addPercept(pos);
				}
				else if (model.hasObject(WEED, loc)) {
					Literal pos = Literal.parseLiteral("pos(weed," + loc.x + "," + loc.y + ")");
					addPercept(pos);										
				} 									
			}
		}
		
		Location pp = model.getAgPos(PLANTER);
		addPercept(Literal.parseLiteral("pos(planter," + pp.x + "," + pp.y + ")"));
		clearPercepts("planter");		
		clearPercepts("weeders");		
		clearPercepts("sprinkler");		
	}
	
	class GardenModel extends GridWorldModel{
		private GardenModel(){			
			super(GWidth, GHeight, 4);			
			//setAgPos(0, 0, 0);
			//initial locations of agents
			setAgPos(PLANTER, 3, 2);
			setAgPos(WEEDERS, 0, 0);
			//Sprinkler has to start from (0,0)!
			setAgPos(SPRINKLER, 0, 0);
			//initial locations of plants
			add(PLANT, 0, 1);			
			add(PLANT, 2, 1);
			add(PLANT, GWidth - 1, GHeight - 1);
		}
	}	
	class GardenView extends GridWorldView{
		public GardenView(GardenModel model){
			super(model, "Garden", 500); //model, title, window size
			defaultFont = new Font("Arial", Font.BOLD, 18);
			setVisible(true);
			repaint();			
		}
		@Override
		public void draw(Graphics g, int x, int y, int object){
			switch (object){
			case GardenEnv.PLANT:
				g.setColor(new Color(0,102,0));
				drawString(g, x, y, defaultFont, "PLANT");				
				break;			
			case GardenEnv.WEED:
				g.setColor(new Color(102,51,0));
				drawString(g, x, y, defaultFont, "WEED");				
				break;	
			case GardenEnv.BURNT:
				g.setColor(new Color(0,0,0));
				drawString(g, x, y, defaultFont, "BURNT");				
				break;	
			}
		}						
		@Override
		public void drawAgent(Graphics g, int x, int y, Color c, int id) {
			c = Color.blue;
			boolean draw = true;
			switch(id) {
				case PLANTER: 
					c = new Color(0,200,0);
					break;
				case WEEDERS:
					c = new Color(173,255,47);
					break;
				case SPRINKLER:
					c = new Color(0,191,255);
					break;
				default:
					draw = false;
					break;					
			}
			if(draw) {
				super.drawAgent(g, x, y, c, -1);
				g.setColor(Color.black);
				super.drawString(g, x, y, defaultFont, "");
			}
		}
		@Override
		public void	drawObstacle(Graphics g, int x, int y) {
			super.drawObstacle(g, x, y);
			g.setColor(new Color(200,0,0));
			drawString(g, x, y, defaultFont, "FIRE");
		}
	}
    @Override
    public boolean executeAction(String agName, Structure action) {
        logger.info(agName+" doing: "+ action);
		try {
			if (action.getFunctor().equals("moveTo")) {
				int x = (int)((NumberTerm)action.getTerm(0)).solve();
				int y = (int)((NumberTerm)action.getTerm(1)).solve();			
				model.setAgPos(PLANTER, x, y);             
			}        
			if (action.getFunctor().equals("plant")) {
				Location pp = model.getAgPos(PLANTER);
				model.add(PLANT, pp.x, pp.y);       
				updatePercepts();      
			}
			if (action.getFunctor().equals("discover")) {
				int x = (int)((NumberTerm)action.getTerm(0)).solve();
				int y = (int)((NumberTerm)action.getTerm(1)).solve();
				discover(x,y);
				discover(x-1,y);
				discover(x,y-1);
				discover(x+1,y);
				discover(x,y+1);
				addPercept(Literal.parseLiteral("fullyDiscovered(" + x + "," + y + ")"));
			}		
			if(action.getFunctor().equals("searchPlants")){
				searchPlants();			
			}			
			if(action.getFunctor().equals("watering")){	
				Location loc = model.getAgPos(SPRINKLER);
				if(loc.x == 0){
					for(int i = 0; i < GWidth; i++){
						for(int j = 0; j < GHeight; j++){
							model.setAgPos(SPRINKLER, i, j);
							try {
								Thread.sleep(300);
								} catch (Exception e) {}
							if(model.hasObject(PLANT, i, j)){							
								System.out.println("Watering plant at: (" + i + "," + j + ")");
							}							
						}
					}
					updatePercepts();
				}
				else{
					for(int i = loc.x; i >= 0; i--){
						for(int j = loc.y; j >= 0; j--){
							model.setAgPos(SPRINKLER, i, j);
							try {
								Thread.sleep(300);
								} catch (Exception e) {}
							if(model.hasObject(PLANT, i, j)){							
								System.out.println("Watering plant at: (" + i + "," + j + ")");
							}							
						}
					}
					updatePercepts();
				}
			}	
			if(action.getFunctor().equals("searchWeed")){
				searchWeed();
			}
			if (action.getFunctor().equals("goTo")) {				
				int goalX = (int)((NumberTerm)action.getTerm(0)).solve();
				int goalY = (int)((NumberTerm)action.getTerm(1)).solve();
				Location pp = model.getAgPos(WEEDERS);
				int startX = pp.x;
				int startY = pp.y;								
				//steps to the weed
				//without this, the weeders would teleport
				//the agent can step over other agents --> if planter steps down --> weeders will make it disappear!!
				boolean finished = false;
				while(!finished){	
					try {
							Thread.sleep(300);
        					} catch (Exception e) {}
					if(goalX == startX && goalY == startY){
						finished = true;
					}					
					else{
						if(goalY < startY){
							startY -= 1;								
							model.setAgPos(WEEDERS, startX, startY);
						}
						if(goalY > startY){
							startY += 1;
							model.setAgPos(WEEDERS, startX, startY);
						}
						if(goalX < startX){
							startX -= 1;
							model.setAgPos(WEEDERS, startX, startY);
						}
						if(goalX > startX){
							startX += 1;
							model.setAgPos(WEEDERS, startX, startY);
						}					
					}
				}									          
			}  
			if (action.getFunctor().equals("remove")) {
				Location pp = model.getAgPos(WEEDERS);
				model.remove(WEED, pp.x, pp.y);      
				System.out.println("Remove weed from: (" + pp.x + "," + pp.y + ")"); 				
				updatePercepts();      
			}
			if(action.getFunctor().equals("extinguish")){
				Location loc = model.getAgPos(SPRINKLER);
				if(loc.x == 0){
					for(int i = 0; i < GWidth; i++){
						for(int j = 0; j < GHeight; j++){
							model.setAgPos(SPRINKLER, i, j);
							try {
								Thread.sleep(300);
								} catch (Exception e) {}
							if(model.hasObject(FIRE, i, j)){		
								model.remove(FIRE, i, j);
								System.out.println("Extinguish fire at: (" + i + "," + j + ")");
							}							
						}
					}
					updatePercepts();
				}
				else{
					for(int i = loc.x; i >= 0; i--){
						for(int j = loc.y; j >= 0; j--){
							model.setAgPos(SPRINKLER, i, j);
							try {
								Thread.sleep(300);
								} catch (Exception e) {}
							if(model.hasObject(FIRE, i, j)){
								model.remove(FIRE, i, j);
								System.out.println("Extinguish fire at: (" + i + "," + j + ")");
							}							
						}
					}
					updatePercepts();
				}				
			}
			if (action.getFunctor().equals("checkForFire")) {				
				boolean hasFire = false;
				for(int i = 0; i < GWidth; i++){
					for(int j = 0; j < GHeight; j++){
						if(model.hasObject(FIRE, i, j)){							
							hasFire = true;
							System.out.println("Not extinguished fire at: " + i + ", " + j);
							addPercept(Literal.parseLiteral("needExtinguish"));							
						}
					}
				}
				if(!hasFire){
					addPercept(Literal.parseLiteral("cleanAfterFire"));								
				}
				updatePercepts();      
			}
			if(action.getFunctor().equals("cleaning")){
				//this comes after the sprinkler stepped on every field --> that results the weeders agent to disappear (hide)
				//so instead of route finding, weeders go back to the start position, and steps on every field				
				model.setAgPos(WEEDERS, 0, 0);
				for(int i = 0; i < GWidth; i++){
					for(int j = 0; j < GHeight; j++){
						model.setAgPos(WEEDERS, i, j);
						try {
							Thread.sleep(300);
							} catch (Exception e) {}
						if(model.hasObject(BURNT, i, j)){		
							model.remove(BURNT, i, j);
							System.out.println("Remove burnt plant/weed from: (" + i + "," + j + ")");
						}							
					}
				}
				updatePercepts();				
			}			
		} catch (Exception e) {}
        try {
            Thread.sleep(200);
        } catch (Exception e) {}
		
		informAgsEnvironmentChanged();
        return true;
    }
	
	void discover(int x, int y) {		
		if(x<0 || y<0 || x>=GWidth || y>GHeight) return;			
		if(model.isFreeOfObstacle(x,y) && 
			model.isFree(PLANT,x,y) &&
			model.isFree(WEED,x,y) && 
			model.isFree(BURNT, x, y))			
			addPercept(Literal.parseLiteral("free(" + x + "," + y + ")"));
		addPercept(Literal.parseLiteral("discovered(" + x + "," + y + ")"));
	}		
	
	void searchWeed(){					
		for(int i = 0; i < GWidth; i++){
			for(int j = 0; j < GHeight; j++){
				if(model.hasObject(WEED, i, j)){					
					addPercept(Literal.parseLiteral("weedDiscovered(" + i + "," + j + ")"));						
					return;					
				}
			}
		}	
	}
	
	void searchPlants(){				
		for(int i = 0; i < GWidth; i++){
			for(int j = 0; j < GHeight; j++){
				if(model.hasObject(PLANT, i, j)){									
					addPercept(Literal.parseLiteral("hasPlant(" + i + "," + j + ")"));	
					break;
				}
			}
		}		
	}	
	
	void createWeed(){
		Random rand = new Random();
		int x = rand.nextInt(GWidth);
		int y = rand.nextInt(GHeight);
		if(model.hasObject(PLANT, x, y) || model.hasObject(WEED, x, y) || model.hasObject(FIRE, x, y) || model.hasObject(BURNT, x, y)){
			createWeed();
		}
		else{
			model.add(WEED, x, y);
		}
	}
	
	void createFire(){		
		Random rand = new Random();
		int x = rand.nextInt(GWidth);
		int y = rand.nextInt(GHeight);
		boolean success = false; 		
		//extuinguished area can not burn again
		if(!model.hasObject(FIRE, x, y) && !model.hasObject(BURNT, x, y)){								
			if(model.hasObject(PLANT, x, y)){
				model.remove(PLANT, x, y);				
				success = true;				
			}
			else if(model.hasObject(WEED, x, y)){
				model.remove(WEED, x, y);				
				success = true;			
			}							
		}
		if(success){
			model.add(BURNT, x, y);
			model.add(FIRE, x, y);			
			System.out.println("Fire at: " + x + ", " + y);
			addPercept(Literal.parseLiteral("needExtinguish"));							
		}
		else{			
			createFire();			
		}		
	}
	
	void spreadFire(){
		int x = -1;
		int y = -1;	
		for(int i = 0; i < GWidth; i++){
			for(int j = 0; j < GHeight; j++){
				if(model.hasObject(FIRE, i, j)){
					x = i;
					y = j;					
					break;
				}
			}
		}		
		if(y == -1){
			return;
		}
		else{
			if(y != GHeight - 1){				
				if(model.hasObject(PLANT, x, y + 1) || model.hasObject(WEED, x, y + 1)){
					createFireWithProbability(x, y + 1);
					return;					
				}				
			}
			else if(y != 0){				
				if(model.hasObject(PLANT, x, y - 1) || model.hasObject(WEED, x, y - 1)){
					createFireWithProbability(x, y - 1);
					return;
				}				
			}
			else if(x != GWidth - 1){				
				if(model.hasObject(PLANT, x + 1, y) || model.hasObject(WEED, x + 1, y)){
					createFireWithProbability(x + 1, y);
					return;
				}				
			}
			else if(x != 0){				
				if(model.hasObject(PLANT, x - 1, y) || model.hasObject(WEED, x - 1, y)){
					createFireWithProbability(x - 1, y);
					return;
				}				
			}
		}							
	}
	
	void createFireWithProbability(int x, int y){
		Random rand = new Random();
		//probability of spreading is 1/5
		boolean val = rand.nextInt(5) == 0;
		if(val){		
			if(model.hasObject(PLANT, x, y)){
				model.remove(PLANT, x, y);
			}
			else if(model.hasObject(WEED, x, y)){
				model.remove(WEED, x, y);
			}
			model.add(BURNT, x, y);
			model.add(FIRE, x, y);			
			System.out.println("Fire spread to: " + x + ", " + y);
		}
	}
	
	boolean hasBurnt(){
		for(int i = 0; i < GWidth; i++){
			for(int j = 0; j < GHeight; j++){
				if(model.hasObject(BURNT, i, j)){
					return true;
				}
			}
		}
		return false;
	}
	
    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        frame.dispose();
		super.stop();
    }
}


