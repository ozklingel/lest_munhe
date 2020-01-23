package gameClient;

import GameElements.Fruit;
import GameElements.Robot;
import Server.*;
import dataStructure.*;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Point3D;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

import GameElements.*;

/**
 * This class create gui for the game
 
 * @author oz klingel
 */
public class MyGameGUI extends JFrame implements ActionListener, MouseListener,Runnable {
   
    private JButton ototstart;
    private JButton yadstart;
    private JButton exit;

    private static graph level_graph;
    private Boolean PaintRobots;
    private double max_node_x;
    private double max_node_y;
    private double min_node_x;
    private double min_node_y;
    private game_service game;
    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private Thread clientThread;
    private boolean ManuelMode;
    private boolean firstpress=false;
    private Robot choosenrobot;
    private boolean AutoMode=false;
    private Game_Algo ga;
    private int level;
    private static KML_Logger log;

    /**
     * function main
     * Initialize new MyGameGUI() and set it to be visible
     */
    public static void main(String[] args) {
        MyGameGUI g = new MyGameGUI();
        g.setVisible(true);
    }
    /**
     * This function calls private function "INITGUI"
     */
    public MyGameGUI()
    {
        INITGUI();
    }
    /**
     * This function make the first init for the gui
     * player must choose whether to play it manually or automatically
     */
    private void INITGUI() {
        PaintRobots = false;
        this.setSize(1300, 700);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addMouseListener(this);
        ototstart = new JButton("play  Game by your self");
        yadstart = new JButton("let me give you my algorithm");
        exit=new JButton("exit");
        ototstart.addActionListener(this);
        yadstart.addActionListener(this);
        exit.addActionListener(this);

        this.getContentPane().setLayout(new GridLayout());///set grid
        this.getContentPane().add(ototstart);///set buttuns
        this.getContentPane().add(yadstart);
        this.getContentPane().add(exit);

        clientThread = new Thread(this);
       
    }
    /**
     * This function analyze data (later to be paint in the gui)
     * graph level,and robots.
     * if player choose to play automatically he will choose the level number.
     * robots will be added by algorithm (calls a private function AutoSetRobot)
     * if player choose to play manually he will choose the level number
     * and the first location for each robot.(calls a private function ManuelsetRobots)
     * @param actionEvent
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String str = actionEvent.getActionCommand();
        /////////////////////////////////////////////////////
        if (str.equals("exit")) {
            JFrame start = new JFrame();
            JOptionPane.showMessageDialog(start, "byy");

        }

        if (str.equals("play  Game by your self")) {
            ManuelMode=true;
            JFrame start = new JFrame();
            try {
                level = Integer.parseInt(JOptionPane.showInputDialog(start, "Enter senario between 0-23"));////pop awindow tom enter senario
                if (level < 0 || level > 23) {
                    JOptionPane.showMessageDialog(start, "Invalid level");
                } else {
                    log =new KML_Logger(level);
                    game = Game_Server.getServer(level);
                    String Graph_str = game.getGraph();
                    level_graph = new DGraph();
                    ((DGraph) level_graph).init(Graph_str);
                    this.remove(this.ototstart);
                    this.remove(this.yadstart);
                    this.remove(this.exit);
                    repaint();
                    ///////the defent-choose loacton for robots
                    ManuelsetRobots();
                    game.startGame();
                    ga=new Game_Algo();

                    AutoMode=true;
                    clientThread.start();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(start, "Invalid Pattern/Not entered any Number");
                e.printStackTrace();
            }
        }
        /////////////////////////////////////////////////////
        if (str.equals("let me give you my algorithm")) {
            ManuelMode=false;
            JFrame start = new JFrame();
            try {
                 level = Integer.parseInt(JOptionPane.showInputDialog(start, "Enter level between 0-23"));
                if (level < 0 || level > 23) {
                    JOptionPane.showMessageDialog(start, "Invalid level");
                } else {
                    log =new KML_Logger(level);
                    game = Game_Server.getServer(level);
                    String Graph_str = game.getGraph();
                    level_graph = new DGraph();
                    ((DGraph) level_graph).init(Graph_str);
                    this.remove(ototstart);
                    this.remove(this.yadstart);
                    this.remove(this.exit);

                    ga=new Game_Algo();
                    ga.AutoSetRobot(game,level_graph);
                    PaintRobots=true;
                    game.startGame();
                    AutoMode=true;
                    clientThread.start();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(start, "Invalid Pattern/Not entered any Number");
                e.printStackTrace();
            }
        }
        //if (str.equals("New Game")) { }
    }
    /**
     * This private function called by "actionPerformed"
     * player enter the location he chooses for each robot.
     * The location must be a valid vertex in the level map.
     */
    private void ManuelsetRobots() {
        try {
            String info = game.toString();
            JSONObject line;
            line = new JSONObject(info);
            JSONObject ttt = line.getJSONObject("GameServer");
            int rs = ttt.getInt("robots");
            int robot_src = -1;
            int i = 0;
            while (i < rs) {
                while (!level_graph.getV().contains(level_graph.getNode(robot_src))) {
                    try {
                        robot_src = Integer.parseInt(JOptionPane.showInputDialog(ototstart, "Enter intersection (in the map) number to place the Robot. you have: " + (rs - i) + " robots left to place"));
                        if (!level_graph.getV().contains(level_graph.getNode(robot_src))) {
                            JOptionPane.showMessageDialog(ototstart, "intersection not in the map!");
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(ototstart, "Invalid Pattern/Not entered any Number");
                    }
                }
                game.addRobot(robot_src);
                robot_src = -1;
                i++;
                PaintRobots = true;
            }
            repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ototstart, "Invalid input");
            e.printStackTrace();
        }
    }
    /**
     * This function paint the following data to the gui.
     * graph
     *background
     * fruit
     * robot
     * the data is also read by kml_logger
     * called by "paint"
     * @param g
     */
    @Override
    public void paintComponents(Graphics g){
        super.paint(g);
        if (level_graph != null) {
            max_node_x = getMaxMinNode(level_graph.getV(), true, true);
            max_node_y = getMaxMinNode(level_graph.getV(), true, false);
            min_node_x = getMaxMinNode(level_graph.getV(), false, true);
            min_node_y = getMaxMinNode(level_graph.getV(), false, false);
            try {
                String[] splitData = game.toString().split("[:\\}]");
                BufferedImage graph_image = ImageIO.read(new File(splitData[9].substring(1, 8) + ".png"));
                g.drawImage(graph_image, 30, 60, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
           ////////////////draw nodes
            for (node_data n : level_graph.getV()) {
                Point3D nodes_src = n.getLocation();
                g.setColor(Color.orange);
                double nodes_src_x = scale(nodes_src.x(), min_node_x, max_node_x, 50, 1250);
                double nodes_src_y = 700 - scale(nodes_src.y(), min_node_y, max_node_y, 50, 650);
                g.fillOval((int) nodes_src_x - 7, (int) nodes_src_y - 7, 15, 15);
                g.drawString("" + n.getKey(), (int) nodes_src_x, (int) (nodes_src_y + 20));
                try {
                	///////////////////draw edges
                    for (edge_data edges : level_graph.getE(n.getKey())) {
                        g.setColor(Color.BLUE);
                        Point3D nodes_dest = level_graph.getNode(edges.getDest()).getLocation();
                        double nodes_dest_x = scale(nodes_dest.x(), min_node_x, max_node_x, 50, 1250);
                        double nodes_dest_y = 700 - scale(nodes_dest.y(), min_node_y, max_node_y, 50, 650);
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setStroke(new BasicStroke(2));
                        g2.drawLine((int) nodes_src_x, (int) nodes_src_y, (int) nodes_dest_x, (int) nodes_dest_y);

                        g.setColor(Color.green);
                        int directed_x = (int) (nodes_src_x * 0.15 + nodes_dest_x * 0.85);
                        int directed_y = (int) (nodes_src_y * 0.15 + nodes_dest_y * 0.85);
                       g.fillOval(directed_x - 4, directed_y - 2, 7, 7);
                        g.setColor(Color.DARK_GRAY);
                        g.drawString("" + df2.format(edges.getWeight()), directed_x, directed_y);
                    }
                } catch (Exception e) {
                    //this node has no edges, the graph is still being initialized...
                }
            }
            ////////draw fruits
            for (int i = 0; i < game.getFruits().size(); i++) {
                try {
                    Fruit f = new Fruit(game.getFruits(), i);
                    double fruit_src_x = scale(f.getLocation().x(), min_node_x, max_node_x, 50, 1250);
                    double fruit_src_y = 700 - scale(f.getLocation().y(), min_node_y, max_node_y, 50, 650);
                    g.drawRect((int) fruit_src_x - 15, (int) fruit_src_y - 10, 30,20);
                    g.setColor(Color.black);
                    if (f.getType() == 1) {
                        MyGameGUI.log.Place_Mark("fruit_1", f.getLocation().toString());
                        g.drawString(f.getValue() + " ^", (int) fruit_src_x - 9, (int) fruit_src_y + 11);
                    }
                    if (f.getType() == -1) {
                        MyGameGUI.log.Place_Mark("fruit_-1", f.getLocation().toString());
                        g.drawString(f.getValue() + " v", (int) fruit_src_x - 9, (int) fruit_src_y + 11);
                    }
                } catch (Exception e) {
                    System.out.println("404-file not found!");
                    e.printStackTrace();
                }
            }
            if (PaintRobots) {
                for (int i = 0; i < game.getRobots().size(); i++) {
                    try {
                        Robot Robot_src = new Robot(game.getRobots(), i);
                        double robot_src_x = scale(Robot_src.getLocation().x(), min_node_x, max_node_x, 50, 1250);
                        double robot_src_y = 700 - scale(Robot_src.getLocation().y(), min_node_y, max_node_y, 50, 650);
                        MyGameGUI.log.Place_Mark("data/robot3.png", Robot_src.getLocation().toString());
                        g.setColor(Color.RED);

                        g.drawOval((int) robot_src_x - 15, (int) robot_src_y - 10, 30,20);

                    } catch (Exception e) {
                        System.out.println("404-file not found!");
                        e.printStackTrace();
                    }
                }
            }
            Robot r=new Robot(game.toString());
            g.drawString("Level: " + level + "    Time 00:" + (game.timeToEnd() / 1000) + "    Total Score: " + r.TotalScore()+"    Total Moves: "+r.TotalMoves(), 10, 50);
            if (game.getRobots().size() == 1) {
                g.drawString(game.getRobots().get(0).substring(0, 60), 100, 610);
            }
            if (game.getRobots().size() == 2) {
                g.drawString(game.getRobots().get(0).substring(0, 60), 100, 610);
                g.drawString(game.getRobots().get(1).substring(0, 60), 100, 630);
            }
            if (game.getRobots().size() == 3) {
                g.drawString(game.getRobots().get(0).substring(0, 60), 100, 610);
                g.drawString(game.getRobots().get(1).substring(0, 60), 100, 630);
                g.drawString(game.getRobots().get(2).substring(0, 60), 100, 650);
            }

        }
    }

    /**
     * this function calls "paintComponents"
     * and paint the gui
     * @param g
     */
    @Override
    public void paint(Graphics g) {
        Image IBI = createImage(1300, 650);
        Graphics gdb = IBI.getGraphics();
        paintComponents(gdb);
        g.drawImage(IBI,0,0,this);
    }

    /**
     * this function is a math Calculating for scale the items we paint
     * @param data  denote some data to be scaled
     * @param r_min the minimum of the range of your data
     * @param r_max the maximum of the range of your data
     * @param t_min the minimum of the range of your desired target scaling
     * @param t_max the maximum of the range of your desired target scaling
     * @return
     */
    private double scale(double data, double r_min, double r_max, double t_min, double t_max) {
        double res = ((data - r_min) / (r_max - r_min)) * (t_max - t_min) + t_min;
        return res;
    }

    /**
     * the function Calculating Max or Min Node
     * @param col Collection of node_data in level_graph
     * @param b   true to get MaxNode false to get MinNode
     * @param c   true to get x false to get y
     * @return
     */
    private double getMaxMinNode(Collection<node_data> col, boolean b, boolean c) {
        double MaxNode = 0, MinNode = 0;
        if (b) {
            MaxNode = Integer.MIN_VALUE;
            for (node_data nd : col) {
                if (c) {
                    if (MaxNode < nd.getLocation().x()) {
                        MaxNode = nd.getLocation().x();
                    }
                } else {
                    if (MaxNode < nd.getLocation().y()) {
                        MaxNode = nd.getLocation().y();
                    }
                }
            }
        } else {
            MinNode = Integer.MAX_VALUE;
            for (node_data nd : col) {
                if (c) {
                    if (MinNode > nd.getLocation().x()) {
                        MinNode = nd.getLocation().x();
                    }
                } else {
                    if (MinNode > nd.getLocation().y()) {
                        MinNode = nd.getLocation().y();
                    }
                }
            }
        }
        if (b) return MaxNode;
        else return MinNode;
    }

    /**
     * this function responsible to make the movement of the robots in the Manuel game
     * if only one robot the player can constantly choose the next dest for the robot
     * because the server will save the robot id
     * if more then one robot the player need to choose a robot and immediately a node and again robot and dest.
     * because the server will not save the robot id after giving him a dest
     * @param e1
     */
    @Override
    public void mouseClicked(MouseEvent e1) {
        if(ManuelMode) {
            if(game.getRobots().size()==1) {
                Robot r = new Robot(game.getRobots(), 0);
                double e1_get_y = scale(e1.getY(), 0, 700, 0, 700);
                double e1_get_x = scale(e1.getX(), 0, 1300, 0, 1300);
                for (edge_data ed : level_graph.getE(r.getSrc())) {
                    double ndlocationX = scale(level_graph.getNode(ed.getDest()).getLocation().x(), min_node_x, max_node_x, 50, 1250);
                    double ndlocationY = scale(level_graph.getNode(ed.getDest()).getLocation().y(), min_node_y, max_node_y, 50, 650);
                    if (Math.abs(e1_get_x - ndlocationX) < 25 && Math.abs(e1_get_y - ndlocationY) < 25) {
                        game.chooseNextEdge(0, ed.getDest());
                    }
                }
            }
            else {
                if (!firstpress) {
                    for (int i = 0; i < game.getRobots().size(); i++) {
                        choosenrobot = new Robot(game.getRobots(), i);
                        double RobotlocationX = scale(level_graph.getNode(choosenrobot.getSrc()).getLocation().x(), min_node_x, max_node_x, 50, 1250);
                        double RobotlocationY = scale(level_graph.getNode(choosenrobot.getSrc()).getLocation().y(), min_node_y, max_node_y, 50, 650);
                        double e1_get_y = scale(e1.getY(), 0, 700, 0, 700);
                        double e1_get_x = scale(e1.getX(), 0, 1300, 0, 1300);
                        if (Math.abs(RobotlocationX - e1_get_x) < 15 && Math.abs(RobotlocationY - e1_get_y) < 15) {
                            firstpress = true;
                            break;
                        }
                    }
                }
                if (firstpress) {
                    double e1_get_y = scale(e1.getY(), 0, 700, 0, 700);
                    double e1_get_x = scale(e1.getX(), 0, 1300, 0, 1300);
                    for (edge_data ed : level_graph.getE(choosenrobot.getSrc())) {
                        double ndlocationX = scale(level_graph.getNode(ed.getDest()).getLocation().x(), min_node_x, max_node_x, 50, 1250);
                        double ndlocationY = scale(level_graph.getNode(ed.getDest()).getLocation().y(), min_node_y, max_node_y, 50, 650);
                        if (Math.abs(e1_get_x - ndlocationX) < 15 && Math.abs(e1_get_y - ndlocationY) < 15) {
                            game.chooseNextEdge(choosenrobot.getId(), ed.getDest());
                            firstpress = false;
                        }
                    }
                }
            }
        }
    }

   
    /**
     * this function used because we have a thread.
     * in Auto mode will call "MoveRobots" and "repaint"
     * in manual mode will call "repaint"
     */
    @Override
    public void run() {
        int dt = 50;
        while (game.isRunning()) {
            if(AutoMode){
                try {
                    ga.MoveRobots(game, level_graph);///sent to algo class
                    game.move();///move the robots one step
                    repaint();//////draw again the gui
                    Thread.sleep(dt);
                }catch (InterruptedException | JSONException e) {
                    e.printStackTrace();
                }
            }
            try {
                game.move();
                repaint();
                Thread.sleep(dt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.KML_Stop();
        JOptionPane.showMessageDialog(null, "GameOver, Final Score is: "+new Robot(game.toString()).TotalScore());
    }
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}