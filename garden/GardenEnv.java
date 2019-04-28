// Environment code for project garden.mas2j
import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import java.util.logging.*;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class GardenEnv extends Environment {	
	public static final int GHeight = 4; //grid height
	public static final int GWidth = 6; //grid width
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
		//GridWorldView in a different window
		//User controls & messages in other window 		
		setup();
   //     updatePercepts();
    }
	private void setup(){
		frame = new JFrame("Kert vez�rl�s");
		frame.setPreferredSize(new Dimension(400, 200));
		frame.setLayout(new GridLayout(2, 2, 10, 10));//rows, cols, hgap, vgap --> col ignored 
		//TODO: button listeners
		JButton fire = new JButton("T�z");
		fire.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.out.println("T�Z");
			}
		});
		JButton plant = new JButton("�ltet�s");
		plant.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.out.println("�LTET�S");
			}
		});
		JButton sprinkle = new JButton("Locsol�s");
		sprinkle.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.out.println("LOCSOL�S");
			}
		});
		//TODO: temperature = getTemperature(); --> other method to periodically refresh the variable & text in the temperatureLabel
		temperature = 0;
		temperatureLabel = new JLabel("M�rt h�m�rs�klet: " + temperature);	
		
		frame.add(fire);
		frame.add(plant);
		frame.add(sprinkle);
		frame.add(temperatureLabel);
		frame.pack();
		frame.setVisible(true);
	}
	class GardenModel extends GridWorldModel{
		private GardenModel(){
			super(GWidth, GHeight, 2); //2??
		}
	}
	class GardenView extends GridWorldView{
		public GardenView(GardenModel model){
			super(model, "Kert", 400); //model, title, window size
			setVisible(true);
			repaint();
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


