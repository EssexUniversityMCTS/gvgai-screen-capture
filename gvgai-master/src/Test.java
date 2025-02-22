import java.lang.annotation.Repeatable;
import java.util.Properties;
import java.util.Random;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import core.ArcadeMachine;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:29
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Test
{
	public static boolean continueLearning = false;
	public static String filePath = "previousModel";///22_5_15_18";
	
    public static void main(String[] args)
    {
        //Available controllers:
    	String sampleRandomController = "controllers.sampleRandom.Agent";
    	String doNothingController = "controllers.doNothing.Agent";
        String sampleOneStepController = "controllers.sampleonesteplookahead.Agent";
        String sampleMCTSController = "controllers.sampleMCTS.Agent";
        String sampleFlatMCTSController = "controllers.sampleFlatMCTS.Agent";
        String sampleOLMCTSController = "controllers.sampleOLMCTS.Agent";
        String sampleGAController = "controllers.sampleGA.Agent";
        String tester = "controllers.Tester.Agent";
        String repeatOLETS = "controllers.repeatOLETS.Agent";
        String screenCap = "controllers.ScreenCaptureAgent.Agent";

        //Available Generators
        String randomLevelGenerator = "levelGenerators.randomLevelGenerator.LevelGenerator";
        String geneticGenerator = "levelGenerators.geneticLevelGenerator.LevelGenerator";
        String constructiveLevelGenerator = "levelGenerators.constructiveLevelGenerator.LevelGenerator";
        
        //Available games:
        String gamesPath = "examples/gridphysics/";
        String games[] = new String[]{};
        String generateLevelPath = "examples/";

        //All public games
        games = new String[]{"aliens", "bait", "blacksmoke", "boloadventures", "boulderchase",              //0-4
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
                             "zelda", "zenpuzzle" }; //60, 61 ...

        //Other settings
        boolean visuals = false;
        String recordActionsFile = null; //where to record the actions executed. null if not to save.
        int seed = new Random().nextInt();

        //Game and level to play
        int gameIdx = 21;//53;
        int levelIdx = 3; //level names from 0 to 4 (game_lvlN.txt).
        String game = gamesPath + games[gameIdx] + ".txt";
        String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx +".txt";

        String recordLevelFile = generateLevelPath + games[gameIdx] + ".txt";

        // 1. This starts a game, in a level, played by a human.
   //     ArcadeMachine.playOneGame(game, level1, recordActionsFile, seed);
        
        // 2. This plays a game in a level by the controller.
       // ArcadeMachine.runOneGame(game, level1, visuals, sampleOneStepController, recordActionsFile, seed, false);
        
        String propertieFile = "";
        if(args.length>0)
        {
        	propertieFile = args[0];
        }
        else
        {
        	propertieFile = "tmpConfig.properties";
        }
        	try 
        	{
				InputStream b = new FileInputStream(propertieFile);
				String st = "";
				Properties prop = new Properties();
				prop.load(b);
				
				//System.out.println(prop.getProperty("gameName"));
				ArcadeMachine.setUp(prop, visuals, screenCap, recordActionsFile, seed);
				
			} catch (Exception e) 
        	{
				e.printStackTrace();
			}
        
        
        //System.out.println(args.length);
       
        
        // 3. This replays a game from an action file previously recorded
      //  String readActionsFile = "actionsFile_aliens_lvl0.txt";  //This example is for
       // ArcadeMachine.replayGame(game, level1, visuals, readActionsFile);

        // 4. This plays a single game, in N levels, M times :
        //String level2 = gamesPath + games[gameIdx] + "_lvl" + 1 +".txt";
        //int M = 3;
        //for(int i=0; i<games.length; i++){
        //	game = gamesPath + games[i] + ".txt";
        //	level1 = gamesPath + games[i] + "_lvl" + levelIdx +".txt";
        //	ArcadeMachine.runGames(game, new String[]{level1}, 5, evolutionStrategies, null);
        //}
        
        //5. This starts a game, in a generated level created by a specific level generator
//        if(ArcadeMachine.generateOneLevel(game, geneticGenerator, recordLevelFile)){
//        	ArcadeMachine.playOneGeneratedLevel(game, recordActionsFile, recordLevelFile, seed);
//        }

        //6. This plays N games, in the first L levels, M times each. Actions to file optional (set saveActions to true).
        /*int N = 60, L = 5, M = 1;
        boolean saveActions = false;
        String[] levels = new String[L];
        String[] actionFiles = new String[L*M];
        for(int i = 0; i < N; ++i)
        {
            int actionIdx = 0;
            game = gamesPath + games[i] + ".txt";
            for(int j = 0; j < L; ++j){
                levels[j] = gamesPath + games[i] + "_lvl" + j +".txt";
                if(saveActions) for(int k = 0; k < M; ++k)
                    actionFiles[actionIdx++] = "actions_game_" + i + "_level_" + j + "_" + k + ".txt";
            }
            ArcadeMachine.runGames(game, levels, M, kNearestNeighbour, saveActions? actionFiles:null);
        }*/
    }
}
