package controllers.ScreenCapReplayer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import core.ArcadeMachine;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

import controllers.ScreenCaptureAgent.*;

public class Agent extends AbstractPlayer{

	public MultiLayerNetwork model;
	HashMap<Integer, Color> mapper;
	Random random = new Random();
	
	/**
	 * initialize all variables for the agent
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 */
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		
		mapper = new HashMap<Integer, Color>();
		String path = "I:\\MasterProj\\Code\\output\\escape_lvl0\\400batch\\5by5\\3by3\\dropout_0.0\\3\\";
		
		String folder = "9_7_17_19_0";
		//ArcadeMachine.filePath;
    	//Load network configuration from disk:
        MultiLayerConfiguration confFromJson = null;
		try 
		{
			confFromJson = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(path+folder+"/conf.json")));
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}

        //Load parameters from disk:
        INDArray newParams = null;
        try(DataInputStream dis = new DataInputStream(new FileInputStream(path+folder+"/coefficients.bin")))
        {
            newParams = Nd4j.read(dis);
        } 
        catch (FileNotFoundException e) 
        {
			e.printStackTrace();
		}
        catch (IOException e) 
        {
			e.printStackTrace();
		}

        //Create a MultiLayerNetwork from the saved configuration and parameters
        model = new MultiLayerNetwork(confFromJson);
        model.init();
        model.setParameters(newParams);
        
      //Load the updater:
        org.deeplearning4j.nn.api.Updater updater = null;
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path+folder+"/updater.bin")))
        {
            try 
            {
				updater = (org.deeplearning4j.nn.api.Updater) ois.readObject();
			} 
            catch (ClassNotFoundException e) 
            {
				e.printStackTrace();
			}
        } 
        catch (FileNotFoundException e) 
        {
			e.printStackTrace();
		} 
        catch (IOException e) 
        {
			e.printStackTrace();
		}
        
        //Set the updater in the network
        model.setUpdater(updater);
        model.setListeners(new ScoreIterationListener(1));
        
        InputStream b = null;
		try 
		{
			b = new FileInputStream(path+"/mapper.properties");
		
		String st = "";
		Properties prop = new Properties();
		prop.load(b);
		Object[] keys = prop.keySet().toArray();
		for(int i=0;i<prop.size();i++)
		{
			String[] keyColor = prop.getProperty(keys[i].toString()).split(",");
			Color color = new Color(Integer.parseInt(keyColor[0]),Integer.parseInt(keyColor[1]),Integer.parseInt(keyColor[2]));
			System.out.println(keys[i]+" "+color);
			mapper.put(Integer.parseInt((keys[i].toString())),color );	
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * return ACTION_NIL on every call to simulate doNothing player
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 * @return 	ACTION_NIL all the time
	 */
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
		ArrayList<Observation>[][] obs = stateObs.getObservationGrid();
		double[][] pixs = new double[obs.length][obs[0].length];
		
		for(int i=0;i<obs.length;i++)
		{
			for(int j=0;j<obs[i].length;j++)
			{
				if(obs[i][j].size()>0)
				{
					try{
						
					pixs[i][j] = mapper.get(obs[i][j].get(0).itype).getRGB();
					}catch(Exception e)
					{
//						if(!mapper.containsKey(obs[i][j].get(0).itype))
//						{
//							Color white = Color.WHITE;
//							int red_white = white.getRed();
//							int green_white = white.getGreen();
//							int blue_white = white.getBlue();
//
//							Color genColor = white;
//							do {
//								int red_gen = random.nextInt(red_white);
//								int green_gen = random.nextInt(green_white);
//								int blue_gen = random.nextInt(blue_white);
//
//								genColor = new Color(red_gen, green_gen, blue_gen);
//
//							} while (mapper.containsValue(genColor));
//
//							mapper.put(obs[i][j].get(0).itype, genColor);
//							pixs[i][j] = mapper.get(obs[i][j].get(0).itype).getRGB();
//						}
					}
					
				}
			//	else
			//		System.out.print(" ");
			}
		//	System.out.println();
		}

	//	System.out.println();
		pixs = controllers.ScreenCaptureAgent.Agent.extendedImage(pixs, 6);
		
		BufferedImage temp = new BufferedImage(pixs.length,pixs[0].length,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<pixs.length;i++)
			for(int j=0;j<pixs[0].length;j++)
				temp.setRGB(i,j,(int) pixs[i][j]);
		
		File op = new File("screenshots/after.png");
	    try {
			ImageIO.write(temp, "png", op);
			System.out.println("save");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		double[][] tmpTest = new double[1][pixs.length*pixs[0].length];
        tmpTest[0] = controllers.ScreenCaptureAgent.Agent.flattenImage(pixs);
		
        
        INDArray test = Nd4j.create(tmpTest);//new NDArray(1,pixs.length*pixs[0].length);
        // test.putRow(0, Nd4j.create(flattenImage(pixs)));
    //     INDArray pd = model.output(test);
        int index = model.predict(test)[0];
        
        try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println(stateObs.getAvailableActions().get(index));
		return stateObs.getAvailableActions().get(index);
	}
}
