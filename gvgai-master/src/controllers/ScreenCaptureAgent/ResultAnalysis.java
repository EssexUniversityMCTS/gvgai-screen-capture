package controllers.ScreenCaptureAgent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ResultAnalysis {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BufferedReader b;
		
		int r = 0;
		//for(int r=0;r<5;r++)
		{
		String path = "I:\\MasterProj\\Code\\output\\butterflies_lvl0\\400batch\\5by5\\3by3\\dropout_0.0\\"+r+"\\";//"../../output/"+kSize+"by"+kSize+"/"+round+"/"+gameName+"/";
		System.out.println("round "+r);
		for(int i=0;i<500;i++)
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
				
				System.out.println(result.split(" ")[0]);
				
//				if(result.trim().contains("PLAYER_WINS"))
//				{
//					System.out.println(countAction*1);
//				}
//				
//				else
//				{
//					System.out.println(countAction*-1);
//				}
//				
			//System.out.println(result);
			
			} catch (Exception e) 
			{
				//e.printStackTrace();
			}
		}
	}
	}
}
