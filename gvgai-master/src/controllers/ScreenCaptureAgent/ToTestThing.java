package controllers.ScreenCaptureAgent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class ToTestThing {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BufferedReader b;
		
		String[] games = new String[]{"aliens", "bait", "blacksmoke", "boloadventures", "boulderchase",              //0-4
                "boulderdash", "brainman", "butterflies", "cakybaky", "camelRace",     //5-9
                "catapults", "chase", "chipschallenge", "chopper", "cookmepasta",        //10-14
                "crossfire", "defem", "defender", "digdug", "eggomania",           //15-19
                "enemycitadel", "escape", "factorymanager", "firecaster",  "firestorms",   //20-24
                "frogs", "gymkhana", "hungrybirds", "iceandfire", "infection",    //25-29
                "intersection", "jaws", "labyrinth", "lasers", "lasers2",        //30-34
                "lemmings", "missilecommand", "modality", "overload", "pacman",             //35-39
                "painter", "plants", "plaqueattack", "portals", "raceBet2",         //40-44
                "realportals", "realsokoban", "roguelike", "seaquest", "sheriff",      //45-49
                "sokoban", "solarfox" ,"superman", "surround", "survivezombies", //50-54
                "tercio", "thecitadel", "waitforbreakfast", "watergame", "whackamole", //55-59
                "zelda", "zenpuzzle" };
		
		int minW = Integer.MAX_VALUE;
		int maxW = Integer.MIN_VALUE;
		int minH = Integer.MAX_VALUE;
		int maxH = Integer.MIN_VALUE;
		
		for(int i=0;i<games.length;i++)
		{
			try
			{
			for(int j=0;j<5;j++)
			{
				int w = 0,h = 0;
				b = new BufferedReader(new FileReader(new File("examples/gridphysics/"+games[i]+"_lvl"+j+".txt")));
				
				String st = "";
				//String string = "";
				while((st=b.readLine())!=null)
				{
					//string = st;
					if(st.length()>h)
						h = st.length();
				//	System.out.println(games[i]+" "+string);
					w++;
				}
				//h = string.length();
				
				if(w>maxW)
				{
					maxW = w;
				}
				if(w<minW)
				{
					minW = w;
				}
				if(h>maxH)
				{
					maxH = h;
				}
				if(h<minH)
				{
					minH = h;
				}
				
			} 
		}
		catch (Exception e) 
		{
	//			e.printStackTrace();
		}
		
		}
		System.out.println("w: -- "+minW+" to "+maxW);
		System.out.println("h: -- "+minH+" to "+maxH);
	
	}

}
