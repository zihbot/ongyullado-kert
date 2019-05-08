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
import javax.swing.*;

public class GardenEnv extends Environment {	
	public static final int GHeight = 4; //grid height
	public static final int GWidth = 6; //grid width
	
	public static final int PLANT = 8; //plant code in grid model
	public static final int WEED = 16; //weed code in grid model
	public static final int FIRE = 4; //fire code in grid model	
	
	public static final int OBSERVER = 0; //observer agent id
	public static final int PLANTER = 1; //planter agent id
	public static final int WEEDERS = 2; //weeders agent id
	
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
		//every minute the observer searches for weed
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run(){
				addPercept(Literal.parseLiteral("needWeedSearch"));	
			}
		}, 10 * 1000, 10 * 1000);
        updatePercepts();
    }
	private void setup(){		
		frame = new JFrame("Garden controller");
		frame.setPreferredSize(new Dimension(400, 200));
		frame.setLayout(new GridLayout(2, 2, 10, 10));//rows, cols, hgap, vgap --> col ignored 
		//TODO: button listeners
		JButton fire = new JButton("Fire");
		fire.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.out.println("FIRE");
			}
		});
		JButton plant = new JButton("Plant");
		plant.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.out.println("PLANT");
				addPercept(Literal.parseLiteral("needPlant"));				
			}
		});
		JButton sprinkle = new JButton("Sprinkle");
		sprinkle.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.out.println("SPRINKLE");					
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
	}
	
	class GardenModel extends GridWorldModel{
		private GardenModel(){			
			super(GWidth, GHeight, 3);			
			//setAgPos(0, 0, 0);
			setAgPos(PLANTER, 2, 1);
			setAgPos(WEEDERS, 3, 0);
			//initial location of plants & weeds & fire --> fire covers plant that's under it
			add(PLANT, 0, 1);			
			add(PLANT, 2, 1);
			add(PLANT, GWidth - 1, GHeight - 1);
			add(WEED, 0, 0);
			add(WEED, 4, 2);
			add(FIRE, 1, 1);
			add(FIRE, 1, 2);			
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
				//drawAgent(g, x, y, Color.green, -1);
				break;			
			case GardenEnv.WEED:
				g.setColor(new Color(102,51,0));
				drawString(g, x, y, defaultFont, "WEED");				
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
		//model.isFree(x,y) to skip field on which an other agent stands
		if(model.isFreeOfObstacle(x,y) && 
			model.isFree(PLANT,x,y) &&
			model.isFree(WEED,x,y)	&&
			model.isFree(x,y))			
			addPercept(Literal.parseLiteral("free(" + x + "," + y + ")"));
		addPercept(Literal.parseLiteral("discovered(" + x + "," + y + ")"));
	}		
	
	void searchWeed(){					
		for(int i = 0; i < GWidth; i++){
			for(int j = 0; j < GHeight; j++){
				if(model.hasObject(WEED, i, j)){					
					addPercept(Literal.parseLiteral("weedDiscovered(" + i + "," + j + ")"));	
					System.out.println("x: " + i + " y: " + j);
					return;					
				}
			}
		}	
	}
	
	
    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        frame.dispose();
		super.stop();
    }
}


