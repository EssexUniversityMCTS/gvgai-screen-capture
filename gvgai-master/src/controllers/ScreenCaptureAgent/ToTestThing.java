package controllers.ScreenCaptureAgent;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import javax.swing.JFrame;

import org.tc33.jheatchart.HeatChart;

public class ToTestThing {

	public static void main(String[] args) throws Exception{
		
		String lab1 = "I:\\MasterProj\\Code\\output\\labyrinth_lvl0\\400batch\\5by5\\3by3\\dropout_0.0\\0\\heatMap.txt";
		String lab2 = "I:\\MasterProj\\Code\\output\\labyrinth2_lvl0\\400batch\\5by5\\3by3\\dropout_0.0\\1\\heatMap.txt";
		
		BufferedReader b = new BufferedReader(new FileReader(new File(lab2)));
		
		ArrayList<double[]> op = new ArrayList();
		String st = "";
		
		int max = 0;
		
		while((st = b.readLine())!=null)
		{
			String[] tmp = st.split("\t");
			double[] line = new double[tmp.length];
			
			for(int i=0;i<line.length;i++)
			{
				int parse = Integer.parseInt(tmp[i]);
				line[i] = parse;
				
				if(parse>max)
					max = parse;
			}
			op.add(line);
		}
		
		double[][] real = new double[op.size()][op.get(0).length];
		
		for(int i=0;i<real.length;i++)
		{
			double[] norm = new double[real[i].length];
			
			for(int j=0;j<norm.length;j++)
			{
				if(op.get(i)[j]!=0)
				norm[j] = Math.log(op.get(i)[j]);
			}
			real[i] = norm;
		}
		
		for(int i=0;i<real.length;i++)
		{
			for(int j=0;j<real[0].length;j++)
				System.out.print(real[i][j]+"\t");
			System.out.println();
		}
		
		HeatChart map = new HeatChart(real);

		// Step 2: Customise the chart.
		map.setTitle("Modified Labyrinth heat map");
		
		map.setLowValueColour(Color.white);
		map.setHighValueColour(Color.BLUE);
	
		
		
		
	
		// Step 3: Output the chart to a file.
		map.saveToFile(new File("labyrinth_heat2.png"));
	}

}
