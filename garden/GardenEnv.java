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

import java.util.logging.*;
import javax.swing.*;

public class GardenEnv extends Environment {	
	public static final int GHeight = 4; //grid height
	public static final int GWidth = 6; //grid width
	
	public static final int PLANT = 8; //plant code in grid model
	public static final int WEED = 16; //weed code in grid model
	public static final int FIRE = 4; //fire code in grid model	
	
	public static final int OBSERVER = 0; // observer agent id
	public static final int PLANTER = 1; // planter agent id
	
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
				/*try {
				getEnvironmentInfraTier().getRuntimeServices().createAgent(
					"planter",
					"planter.asl",
					null,null,null,null,null);
				} catch (Exception x) {x.printStackTrace();}*/
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
		//"send" plants & weeds to observer --> belief
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
	}
	
	class GardenModel extends GridWorldModel{
		private GardenModel(){
			super(GWidth, GHeight, 2); //2??	
			
			setAgPos(0, 0, 0);
			setAgPos(1, 2, 1);
			
			//location of plants & weeds & fire --> fire covers plant that's under it
			add(PLANT, 0, 1);			
			add(PLANT, 2, 1);
			add(PLANT, GWidth - 1, GHeight - 1);
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
				//drawGardenObject(g, x, y, "WEED");
				break;
				/*
			case GardenEnv.FIRE:
				g.setColor(Color.red);
				drawString(g, x, y, defaultFont, "FIRE");
				//drawGardenObject(g, x, y, "FIRE");
				break;
				*/
			}
		}				
		/*
		public void drawGardenObject(Graphics g, int x, int y, String text){
			super.drawObstacle(g, x, y);
			if(text.equals("FIRE")){
				return;
			}
			else{
				g.setColor(Color.white);			
				drawString(g, x, y, defaultFont, text);
			}				
		}
		*/
		@Override
		public void drawAgent(Graphics g, int x, int y, Color c, int id) {
			c = Color.blue;
			boolean draw = true;
			switch(id) {
				case PLANTER: 
					c = new Color(0,200,0);
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
			model.isFree(WEED,x,y))
			addPercept(Literal.parseLiteral("free(" + x + "," + y + ")"));
		addPercept(Literal.parseLiteral("discovered(" + x + "," + y + ")"));
	}
	
    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        frame.dispose();
		super.stop();
    }
}


