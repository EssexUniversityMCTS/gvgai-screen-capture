package myStuffs;
import java.util.ArrayList;
import java.util.HashMap;

import org.jblas.util.Random;

import core.ArcadeMachine;

public class gvgai_ui {

	public static String gameNameAndLevel = "";
	public static void main(String[] args) {

		GameSelectionFrame frame = new GameSelectionFrame();
		frame.setVisible(true);
		
		ArrayList<String> gameName = GameSelectionFrame.gameName;
		HashMap<String,Integer> levelMap = GameSelectionFrame.levelMap;
		
		while(gameName.size()==0);
		String gamesPath = "examples/gridphysics/";
		String recordActionsFile = null;
		for(String game : gameName)
		{
			int level = levelMap.get(game);
			System.out.println(game+".txt");
			for(int i=0;i<level;i++)
			{
				
				System.out.println(game+"_lvl"+i+".txt");
				gameNameAndLevel = game+"_lvl"+i+".txt";
				ArcadeMachine.playOneGame(gamesPath+game+".txt", gamesPath+game+"_lvl"+i+".txt",recordActionsFile, Random.nextInt(Integer.MAX_VALUE));
			}
		}
	
	}

}
