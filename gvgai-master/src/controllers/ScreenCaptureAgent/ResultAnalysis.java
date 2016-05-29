package controllers.ScreenCaptureAgent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ResultAnalysis {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BufferedReader b;
		int round = 5;
		
		int kSize = 10;
		String gameName = "escape_lvl5";
		String path = "output/"+kSize+"by"+kSize+"/"+round+"/"+gameName+"/";
		
		for(int i=0;i<199;i++)
		{
			try 
			{
				String st = "";
				String result = "";
				b = new BufferedReader(new FileReader(new File(path+"run"+i+".txt")));
				while((st=b.readLine())!=null)
				{
					result = st;
				}
				
				b = new BufferedReader(new FileReader(new File(path+"run"+i+"action.txt")));
				
				int countAction = 0;
				while((st=b.readLine())!=null)
				{
					countAction++;
				}
				
				if(result.trim().equals("PLAYER_WINS"))
				{
					System.out.println(countAction*1);
				}
				
				else
				{
					System.out.println(countAction*-1);
				}
				
			//System.out.println(result);
			
			} catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}

}
