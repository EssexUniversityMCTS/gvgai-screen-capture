package myStuffs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import core.ArcadeMachine;

public class MyThread implements Runnable{

	private String gameFile;
	private String gameLevelFile;
	Random random = new Random();
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public MyThread(String gameFile, String gameLevelFile)
	{
		this.gameFile = gameFile;
		this.gameLevelFile = gameLevelFile;
	}
	@Override
	public void run() {
		
		//String recordActionsFile = null;
		try {
			String line = "";
			Process p = Runtime.getRuntime().exec("java -jar RunGame.jar "+gameFile+" "+gameLevelFile);
			BufferedReader input =
			        new BufferedReader
			          (new InputStreamReader(p.getErrorStream()));
			      while ((line = input.readLine()) != null) {
			        System.out.println(line);
			      }
			      input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	//	Test2.main(new String[]{gameFile,gameLevelFile});
	}
	

}
