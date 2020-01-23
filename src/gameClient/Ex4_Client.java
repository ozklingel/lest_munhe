package gameClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.JSONException;
import org.json.JSONObject;

import GameElements.Fruit;
import GameElements.Game_Algo;
import Server.Game_Server;
import Server.game_service;
import algorithms.Graph_Algo;
import dataStructure.DGraph;
import dataStructure.edge_data;
import dataStructure.graph;
import dataStructure.node_data;
import oop_dataStructure.OOP_DGraph;
import oop_dataStructure.oop_edge_data;
import oop_dataStructure.oop_graph;
import oop_utils.OOP_Point3D;
/**
* This class represents a simple example for using the GameServer API:
* the main file performs the following tasks:
* 0. login as a user ("999") for testing - do use your ID.
* 1. Creates a game_service [0,23] (user "999" has stage 9, can play in scenarios [0,9] not above
*    Note: you can also choose -1 for debug (allowing a 600 second game).
* 2. Constructs the graph from JSON String
* 3. Gets the scenario JSON String 
* 5. Add a set of robots  // note: in general a list of robots should be added
* 6. Starts game 
* 7. Main loop (vary simple thread)
* 8. move the robot along the current edge 
* 9. direct to the next edge (if on a node) 
* 10. prints the game results (after "game over"), and write a KML: 
*     Note: will NOT work on case -1 (debug).
*  
* @author boaz.benmoshe
*
*/
public class Ex4_Client implements Runnable{
	public static void main(String[] a) {
		Thread client = new Thread(new Ex4_Client());
		client.start();
	}

	private Object game;
	private Game_Algo ga;
	private DGraph level_graph;
	
	@Override
	public void run() {
		int scenario_num = 0; // current "stage is 9, can play[0,9], can NOT 10 or above
		int id = 313387359;
		int num_robots = 0;
		Game_Server.login(id);
		game_service game = Game_Server.getServer(scenario_num); // you have [0,23] games
		
		String g = game.getGraph();
		List<String> fruits = game.getFruits();
		DGraph level_graph = new DGraph();
		level_graph.init(g);
		init(game);
		
		String info = game.toString();
		System.out.println(info);
		try {
			JSONObject line;
			line = new JSONObject(info);

			JSONObject ttt = line.getJSONObject("GameServer");

			num_robots = ttt.getInt("robots");	//num of robot
            String Graph_str = game.getGraph();

			level_graph.init(Graph_str);
			  ga=new Game_Algo();
              ga.AutoSetRobot(game,level_graph);			


		} catch (JSONException e1) {e1.printStackTrace();}
		game.startGame();
		int ind=0;
		long dt=60;
		int jj = 0;
		while(game.isRunning()) {
			try {
				MoveRobots(game, level_graph);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				List<String> stat = game.getRobots();
				for(int i=0;i<stat.size();i++) {
					System.out.println(jj+") "+stat.get(i));
				}
				ind++;
				Thread.sleep(50);
				jj++;
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		String res = game.toString();
		String remark = "This string should be a KML file!!";
		game.sendKML(remark); // Should be your KML (will not work on case -1).
		System.out.println(res);
	}
	/** 
	 * Moves each of the robots along the edge, 
	 * in case the robot is on a node the next destination (next edge) is chosen (randomly).
	 * @param game
	 * @param gg
	 * @param //log
	 */
	 public void MoveRobots(game_service game,graph levelgraph) throws JSONException {
	        List<String> log = game.move();
	        if(log!=null) {
	            for (String robot_json : log) {
	                try {
	                    JSONObject line = new JSONObject(robot_json);
	                    JSONObject ttt = line.getJSONObject("Robot");
	                    int rid = ttt.getInt("id");
	                    int src = ttt.getInt("src");
	                    int dest = ttt.getInt("dest");
	                    if (dest == -1) {
	                        dest = nextNode(levelgraph, src, game);////set the direction of robot this
	                        game.chooseNextEdge(rid, dest);
	                    }
	                } catch (JSONException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	    }
	/**
	 * a very simple random walk implementation!
	 * @param g
	 * @param src
	 * @return
	 */
	  private int nextNode(graph levelgraph, int src,game_service game) {
	        Graph_Algo ga=new Graph_Algo();
	        int key =-1;
	        double nearest=Integer.MAX_VALUE;///holds the smalest dist to fruit
	        ga.init(levelgraph);
	        for (int j = 0; j <game.getFruits().size() ; j++){
	            edge_data ed=getFruitEdge(j, game, levelgraph);///////get the fruit location
	            if (ga.shortestPathDist(src, ed.getDest()) < nearest) {//////we found nearer fruit
	                try {
	                	nearest = ga.shortestPathDist(src, ed.getDest());
	                    key = ga.shortestPath(src, ed.getDest()).get(1).getKey();
	                }
	                catch (Exception e){
	                    key=ed.getSrc();
	                }
	            }
	        }
	        return key;
	    }
	
	
private void init(game_service game) {
		
		String g = game.getGraph();
		List<String> fruits = game.getFruits();
		OOP_DGraph gg = new OOP_DGraph();
		gg.init(g);

		String info = game.toString();
		JSONObject line;
		try {
			line = new JSONObject(info);
			JSONObject ttt = line.getJSONObject("GameServer");
			int rs = ttt.getInt("robots");
			System.out.println(info);
			// the list of fruits should be considered in your solution
			Iterator<String> f_iter = game.getFruits().iterator();
			while(f_iter.hasNext()) {System.out.println(f_iter.next());}	
			int src_node = 0;  // arbitrary node, you should start at one of the fruits
			for(int a = 0;a<rs;a++) {
				game.addRobot(a);
			}
		}
		catch (JSONException e) {e.printStackTrace();}
		
	}


public edge_data getFruitEdge(int i,game_service Game,graph level_graph) {
    Fruit f = new Fruit(Game.getFruits(), i);
    boolean b = false;
    edge_data temp = null;
    for (node_data nd : level_graph.getV()) {
        for (edge_data ed : level_graph.getE(nd.getKey())) {
            double distance = Math.sqrt(Math.pow(((level_graph.getNode(ed.getSrc()).getLocation().x()) - (level_graph.getNode(ed.getDest()).getLocation().x())), 2) + Math.pow(((level_graph.getNode(ed.getSrc()).getLocation().y()) - (level_graph.getNode(ed.getDest()).getLocation().y())), 2));
            double fruit_from_dest = Math.sqrt(Math.pow(((f.getLocation().x() - (level_graph.getNode(ed.getDest()).getLocation().x()))), 2) + Math.pow(((f.getLocation().y() - (level_graph.getNode(ed.getDest()).getLocation().y()))), 2));
            double fruit_to_src = Math.sqrt(Math.pow(((level_graph.getNode(ed.getSrc()).getLocation().x()) - (f.getLocation().x())), 2) + Math.pow(((level_graph.getNode(ed.getSrc()).getLocation().y()) - (f.getLocation().y())), 2));
            if (fruit_from_dest + fruit_to_src - distance <= 0.00001) {
                temp = ed;
                b = true;
            }
            if (b) break;
        }
        if (b) break;
    }
    if (f.getType() == 1) {
        if (Math.min(temp.getSrc(), temp.getDest()) == temp.getDest()) {

            return level_graph.getEdge(temp.getDest(), temp.getSrc());
        } else {
            return level_graph.getEdge(temp.getSrc(), temp.getDest());
        }
    } else {
        if (Math.max(temp.getSrc(), temp.getDest()) == temp.getDest()) {
            return level_graph.getEdge(temp.getDest(), temp.getSrc());
        } else {
            return level_graph.getEdge(temp.getSrc(), temp.getDest());
        }
    }
}
}
