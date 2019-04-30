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
	public static final int FIRE = 6; //fire code in grid model	
	
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
	}
	
	class GardenModel extends GridWorldModel{
		private GardenModel(){
			super(GWidth, GHeight, 2); //2??	
			
			//location of plants & weeds & fire --> fire covers plant that's under it
			add(PLANT, 1, 1);			
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
				drawGardenObject(g, x, y, "PLANT");
				break;			
			case GardenEnv.WEED:
				drawGardenObject(g, x, y, "WEED");
				break;
			case GardenEnv.FIRE:
				drawGardenObject(g, x, y, "FIRE");
				break;
			}
		}				
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
	}
    @Override
    public boolean executeAction(String agName, Structure action) {
        logger.info("executing: "+action+", but not implemented!");
        if (true) { // you may improve this condition
             informAgsEnvironmentChanged();
        }
        return true; // the action was executed with success
    }
    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        frame.dispose();
		super.stop();
    }
}


