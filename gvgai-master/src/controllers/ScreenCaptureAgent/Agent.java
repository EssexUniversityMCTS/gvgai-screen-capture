package controllers.ScreenCaptureAgent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.joda.time.DateTime;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import core.ArcadeMachine;
import core.game.Game;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import it.unimi.dsi.fastutil.Arrays;
import ontology.Types;
import ontology.Types.ACTIONS;
import tachyon.network.protocol.databuffer.DataBuffer;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer{

	/**
	 * initialize all variables for the agent
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 */
	static int poolSize = 2000;
	static int batchSize = 1000;
	static int maxW = 31;
	static int maxH = 48;
	static int currentIndex;
	boolean isFull;
	
	int numPlay = 5;
	int minBlockSize = 10;
	int firstLayerStride = 1;
	int learningFrequency = 1;
	boolean subsamplinglayer = false;
	int w,h;
	
	int saveModelFreq;//10;
	PrintWriter writeOutput;
	String actionSequence;
	String dumpedOutput;
	String outputPath = "output";
	File theDir;
	
	
//	public static Experience[] experiencePool = new Experience[poolSize];
	
	
	Random random = new Random();

	int numAct;
	ArrayList<Types.ACTIONS> actions;
	public ModelOrganizer modelOrganizer;
	public MultiLayerNetwork model;
	
	//MyFrame frame;
	//DefaultTableModel tableModel;
	
	private QLearning learning;
	//private ExperienceCount expCount;
	public static int[] countAccess;
	
	Stack<Integer> toBeLearnt;
	
	int loopIndex = 0;
	boolean randomPick = true;
	
	Experience experience;
	int countRound = 0;
	
	HashMap<Integer, Color> mapper;
	
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		toBeLearnt = new Stack();
		poolSize = ArcadeMachine.poolSize;
		batchSize = ArcadeMachine.currentBatchSize;
		numPlay = ArcadeMachine.currentRepetition;//ArcadeMachine.repetition;
		saveModelFreq = ArcadeMachine.episodes/5;
		
		if(saveModelFreq == 0)
			saveModelFreq = 1;
		//System.out.println(poolSize);
		
		///// bear in mind the case of w!=h
		minBlockSize = ArcadeMachine.currentKernel1.width+ArcadeMachine.strideSize_1;
		
		int learningFrequency = 1;
		
		try 
		{
			String subsamp = ArcadeMachine.currentSubsampling? "subsampling/":"";
			
			String kernelSizeFolder = ArcadeMachine.currentKernel1.width+"";//(minBlockSize-firstLayerStride)+"";
			kernelSizeFolder +="by"+ArcadeMachine.currentKernel1.height+"";
			String kernelSizeFolder2 = ArcadeMachine.currentKernel2.width+"by"+ArcadeMachine.currentKernel2.height;
			String str = outputPath+"/"
						+ArcadeMachine.gameDescription+"/"
					+ArcadeMachine.currentBatchSize+"batch/"
					+subsamp
					+kernelSizeFolder+"/"
					+kernelSizeFolder2+"/"
					+"dropout_"+ArcadeMachine.currentDropOut+"/"
					+numPlay+"/";
			
			
			
			String[] directory = str.split("\\/");
			createNestedDir(directory);
			theDir = new File(str);
			System.out.println("\nsaving directory = "+theDir.getPath());
    		//theDir.mkdir();
			writeOutput = new PrintWriter(new FileWriter(new File(theDir.getPath()+"/run0"+".txt")));
			
		} catch (IOException e2) 
		{
			e2.printStackTrace();
		}
		
		actionSequence = "";
		
		//expCount = new ExperienceCount();
	//	toBeLearned = new ArrayList();
		
		countAccess = new int[poolSize];
		learning = new QLearning(poolSize,stateObs.getAvailableActions().size());
	//	frame = new MyFrame(experiencePool,stateObs.getAvailableActions(),learning.qValues);
	//	tableModel = (DefaultTableModel) frame.table.getModel();
	//	frame.setVisible(true);
		currentIndex = 0;
		actions = stateObs.getAvailableActions();
	    numAct = actions.size();
	    experience = null;

		// for(int i=0;i<poolSize;i++)
		// tableModel.addRow(new String[]{});
		//
		int blockW = stateObs.getBlockSize();// 25;
		// float[] pixs = preProcess(bufferedImage,blockW);

		// System.out.println(bufferedImage.getWidth()+"x"+bufferedImage.getHeight()+"="+pixels.length);

		// System.out.println(pixs[2]);
		Dimension d = stateObs.getWorldDimension();
		w = d.width / blockW;// bufferedImage.getWidth()/blockW;
		h = d.height / blockW;// bufferedImage.getHeight()/blockW;

		if (w < minBlockSize)
			w = minBlockSize;
		if (h < minBlockSize)
			h = minBlockSize;

		modelOrganizer = new ModelOrganizer(new Dimension(w, h), numAct);
		model = modelOrganizer.model;		
      //  System.out.println(w+" "+h);

    //    log.info("Train model....");
   //     model.setListeners(new ScoreIterationListener(1));
    //    for( int i=0; i<nEpochs; i++ ) {
        //	System.out.println("ep "+nEpochs);
        	//System.out.println(nd+""+nd.length());
      //      model.fit(nd);//iterator);//mnistTrain);
            
        //    System.out.println(model.params());
            
   //         log.info("*** Completed epoch {} ***", i);
/*
            log.info("Evaluate model....");
            
            Evaluation eval = new Evaluation(outputNum);
           // while(.hasNext()){
             //   DataSet ds = iterator.next();
                INDArray output = model.output(nd);//ds.getFeatureMatrix());//model.output(ds.getFeatureMatrix());
                eval.eval(nd,output);//ds.getLabels(), output);
           // }
            log.info(eval.stats());
       //     iterator.reset();//mnistTest.reset();
            
        
        }
        
        log.info("****************Example finished********************");
        
*/
	    if(!ArcadeMachine.continueLearning)
		{
			mapper = new HashMap();
			ArrayList<Observation>[][] obs = stateObs.getObservationGrid();

			for (int i = 0; i < obs.length; i++) {
				for (int j = 0; j < obs[i].length; j++) {
					if (obs[i][j].size() > 0) {
						// System.out.print(obs[i][j].size());
						if (!mapper.containsKey(obs[i][j].get(0).itype)) {
							Color white = Color.WHITE;
							int red_white = white.getRed();
							int green_white = white.getGreen();
							int blue_white = white.getBlue();

							Color genColor = white;
							do {
								int red_gen = random.nextInt(red_white);
								int green_gen = random.nextInt(green_white);
								int blue_gen = random.nextInt(blue_white);

								genColor = new Color(red_gen, green_gen, blue_gen);

							} while (mapper.containsValue(genColor));

							mapper.put(obs[i][j].get(0).itype, genColor);
						}
					}
					// else
					// System.out.print(" ");
				}
				// System.out.println();
			}

			
		}
	    else
	    {
	    	InputStream b = null;
			try 
			{
				b = new FileInputStream(theDir+"/mapper.properties");
			
			String st = "";
			Properties prop = new Properties();
			prop.load(b);
			Object[] keys = prop.keySet().toArray();
			for(int i=0;i<prop.size();i++)
			{
				String[] keyColor = prop.getProperty(keys[i].toString()).split(",");
				Color color = new Color(Integer.parseInt(keyColor[0]),Integer.parseInt(keyColor[1]),Integer.parseInt(keyColor[2]));
				mapper.put((int)keys[i],color );	
			}
			
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
		//	System.out.println();
		System.out.println("finished create Agent");
	}
	
	/**
	 * return ACTION_NIL on every call to simulate doNothing player
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 * @return 	ACTION_NIL all the time
	 */
	
	
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
	//	System.out.println("mapper size = "+mapper.size());
		
	//	expCount.print();
       
       // log.info("Load data....");
      //  DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize,true,12345);
        
       // DataSetIterator mnistTest = new MnistDataSetIterator(batchSize,false,12345);

      //  File imgPath = new File("screenshots/2_2.0.png");//_shrink.png");
		 double[][] pixs;
		if(ArcadeMachine.vis)
		{
			BufferedImage bufferedImage = Game.im;
			
//			BufferedImage temp = new BufferedImage(pixs.length,pixs[0].length,BufferedImage.TYPE_INT_RGB);
//			for(int i=0;i<pixs.length;i++)
//				for(int j=0;j<pixs[0].length;j++)
//					temp.setRGB(i,j,(int) pixs[i][j]);
//			
			

			// System.out.println(currentIndex);

			if (bufferedImage == null) {
				// bufferedImage = ImageIO.read(imgPath);
				// System.out.println("null");
				return Types.ACTIONS.ACTION_NIL;
			} else
				Game.im = null;

//			File op = new File("screenshots/"+stateObs.getGameTick()+"before.png");
//		    try {
//				ImageIO.write(bufferedImage, "png", op);
//		//		System.out.println("before");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
			int blockW = stateObs.getBlockSize();// 25;
			pixs = extendedImage(preProcess(bufferedImage, blockW), minBlockSize);
		}
		else
		{
			ArrayList<Observation>[][] obs = stateObs.getObservationGrid();
			pixs = new double[obs.length][obs[0].length];
			
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
							if(!mapper.containsKey(obs[i][j].get(0).itype))
							{
								Color white = Color.WHITE;
								int red_white = white.getRed();
								int green_white = white.getGreen();
								int blue_white = white.getBlue();

								Color genColor = white;
								do {
									int red_gen = random.nextInt(red_white);
									int green_gen = random.nextInt(green_white);
									int blue_gen = random.nextInt(blue_white);

									genColor = new Color(red_gen, green_gen, blue_gen);

								} while (mapper.containsValue(genColor));

								mapper.put(obs[i][j].get(0).itype, genColor);
								pixs[i][j] = mapper.get(obs[i][j].get(0).itype).getRGB();
							}
						}
						
					}
				//	else
				//		System.out.print(" ");
				}
			//	System.out.println();
			}

		//	System.out.println();
		
			
		}
        
		
		BufferedImage temp = new BufferedImage(pixs.length,pixs[0].length,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<pixs.length;i++)
			for(int j=0;j<pixs[0].length;j++)
				temp.setRGB(i,j,(int) pixs[i][j]);
		
		File op = new File("screenshots/"+stateObs.getGameTick()+"before.png");
	    try {
			ImageIO.write(temp, "png", op);
//			System.out.println("save");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		pixs = extendedImage(pixs, minBlockSize);
		
		temp = new BufferedImage(pixs.length,pixs[0].length,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<pixs.length;i++)
			for(int j=0;j<pixs[0].length;j++)
				temp.setRGB(i,j,(int) pixs[i][j]);
		
		op = new File("screenshots/"+stateObs.getGameTick()+"after.png");
	    try {
			ImageIO.write(temp, "png", op);
			System.out.println("save");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    //    System.out.println(pixs.length+" llll "+pixs[0].length);
        int pixIndex = QLearning.findIndexFromImage(pixs);
        if(experience == null)
        {
        	experience = new Experience();
        	experience.setPrevious(pixs);
        	
        }
        
        else
        {
        	experience.setResult(pixs);
        	
        	double penalty = 0;
        	if(QLearning.ImageEquals(experience.getPrevious(), pixs))
        		penalty = -5;
        	experience.setReward(stateObs.getGameScore()+penalty);
        		
        	//learning.mapper.put(experience.copy(), currentIndex);
        //	int pixIndex =  QLearning.findIndexFromImage(pixs);
        	//learning.qValues[currentIndex] = new double[stateObs.getAvailableActions().size()];
        	
        	
        	try
        	{
        		int prevIndex = QLearning.findIndexFromImage(experience.getPrevious());
        		if(prevIndex==-1)
        		{
        			if(isFull)
        			{
        				int lowestIndex = findLowestAccessedIndex();//expCount.getMinAccessAndRemove();
        				prevIndex = lowestIndex;
        				countAccess[lowestIndex] = 0;
        				QLearning.pool.set(lowestIndex, experience.getPrevious());
        				learning.qValues[lowestIndex] = new double[numAct];
        				
        			}
        			
        			else
        			{
        				prevIndex = QLearning.pool.size();
        				QLearning.pool.add(experience.getPrevious());//.set(currentIndex,experience.getPrevious().clone());
        				countAccess[prevIndex]++;
        			}
        			
        			writeExpToFile(experience.copy(),prevIndex);
        		//	experiencePool[prevIndex] = experience.copy();
        		//	toBeLearned.add(prevIndex);
        		//	System.out.println(experiencePool[prevIndex].accessCount);
        //			expCount.increase(prevIndex);
        		//	System.out.println(experiencePool[prevIndex].accessCount);
        			//System.out.println("happening "+experiencePool[prevIndex].accessCount);
        			
       // 			tableModel.setValueAt("experience "+prevIndex,prevIndex,0);
                	
        			currentIndex++;
        			
        		}
        		else //update?
        		{
        			
      //  			System.out.println("not add new");
        			//experiencePool[prevIndex] = experience.copy();
        			countAccess[prevIndex]++;
        			
        			writeExpToFile(experience.copy(),prevIndex);
        			
        			//System.out.println(experiencePool[prevIndex].accessCount);
        			
        	//		expCount.increase(prevIndex);
        			
        			
        	//		tableModel.setValueAt("experience "+prevIndex,prevIndex,0);
                	
        		}
        	//	System.out.println(experience.getAction());
        	//	System.out.println(actions.indexOf(experience.getAction()));
        		learning.qUpdate(prevIndex, pixIndex,actions.indexOf(experience.getAction()),experience.getReward());
            	
        	}
        	catch(Exception e)
        	{
        	//	e.printStackTrace();
        	}
        	
        	if(currentIndex == poolSize)
    		{
    			currentIndex = 0;
    			isFull = true;
    		}
        	
        	experience = new Experience();
        	experience.setPrevious(pixs);
        }
        
//		BufferedImage temp = new BufferedImage(pixs.length,pixs[0].length,BufferedImage.TYPE_INT_RGB);
//		for(int i=0;i<pixs.length;i++)
//			for(int j=0;j<pixs[0].length;j++)
//				temp.setRGB(i,j,(int) pixs[i][j]);
//		
//		File op = new File("screenshots/"+stateObs.getGameTick()+".png");
//	    try {
//			ImageIO.write(temp, "png", op);
//			System.out.println("save");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
        
        //double[][] p = new double[0][0];
    //    System.out.println(bufferedImage.getWidth()+"x"+bufferedImage.getHeight()+"="+pixels.length);
  
      //  System.out.println(pixs[2]);
       // INDArray nd = Nd4j.create(pixs);
       // model.fit(nd);
         //     for(int i=0;i<nd.length();i++)
//        	System.out.println(nd); 
		//System.out.println(model.params());
        
        //if(index==-1)
        //	index = random.nextInt(actions.size());
        //else
        	//int index = learning.getMaxActionIndex(pixIndex);
        int minLim = 1;
        int limit = batchSize;
        if(QLearning.pool.size()<batchSize)
       	limit = QLearning.pool.size()/4;
    //    {
        
        double[][] tmpTraining = new double[limit][pixs.length*pixs[0].length];
        double[][] tmpLabel = new double[limit][numAct];
        
        countRound++;
        
        if(limit>=minLim)
        for(int i=0;i<limit;i++)
        {
        	Experience toUpdateExp;
        	if(randomPick)
        	{
        		if(toBeLearnt.isEmpty())
        		{
        			Set<Integer> generated = new LinkedHashSet<Integer>();
        			
        			while (generated.size() < QLearning.pool.size())
        			{
        			    Integer next = random.nextInt(QLearning.pool.size());
        			    // As we're adding to a set, this will automatically do a containment check
        			    generated.add(next);
        			}
        			toBeLearnt.addAll(generated);
        		}
        		
        		int rand = toBeLearnt.pop();
				
				toUpdateExp = readExpFromFile(rand,w,h);
		//		System.out.println(rand+" "+toBeLearnt.size()+" "+learning.pool.size());
        	}
        	
        	else
        	{
        		toUpdateExp = readExpFromFile(loopIndex,w,h);
        		loopIndex++;
        		
        		if(loopIndex == QLearning.pool.size())
        			loopIndex = 0;
        		
        	}
			
			int startIndex = QLearning.findIndexFromImage(toUpdateExp.getPrevious());
			int nextIndex = QLearning.findIndexFromImage(toUpdateExp.getResult());

			// if(startIndex == -1)
			// continue;
			learning.qUpdate(startIndex, nextIndex, actions.indexOf(toUpdateExp.getAction()), toUpdateExp.getReward());
			if (countRound % learningFrequency == 0) {
				tmpTraining[i] = flattenImage(toUpdateExp.getPrevious());
				tmpLabel[i] = learning.normalize(startIndex);
			//	training.putRow(i, Nd4j.create(flattenImage(toUpdateExp.getPrevious())));
			//	labels.putRow(i, Nd4j.create(learning.normalize(startIndex)));
			}
     //   }}catch(Exception e){}
        //     System.out.println(index);
        }
        
        if(countRound%learningFrequency == 0)
        if(limit>=minLim)
        { 
        INDArray training = Nd4j.create(tmpTraining);//new NDArray(limit,pixs.length*pixs[0].length);
        INDArray labels = Nd4j.create(tmpLabel);//new NDArray(limit,numAct);
        
    //    	System.out.println(training);
    //    	System.out.println(labels);
        
    //    System.out.println("happening limit = "+limit);
      //  System.out.println("training\n[");
        
      //  System.out.println(training);
        
//        float[] b = training.data().asFloat();
//        try {
//			PrintWriter pr = new PrintWriter(new FileWriter(new File("input"+limit+".txt")));
//			pr.write("[");
//			 for(int in=0;in<b.length;in++)
//		        	pr.write(b[in]+" ");
//		        pr.write("]");
//		pr.close();
//        } catch (IOException e) {
//			// 
//			e.printStackTrace();
//		}
//       
        
  //      System.out.println("finish");
//        
//        System.out.println(labels.data().asFloat().toString());
        	model.fit(training, labels);
        
        //learning.getMaxActionIndex(pixIndex);
        }
        
        
        if(pixIndex==-1)
    	{
        	if(isFull)
			{
				pixIndex = currentIndex;//expCount.getMinAccessAndRemove();
				QLearning.pool.set(pixIndex, pixs);
				learning.qValues[pixIndex] = new double[numAct];
			}
        	
        	else
        	{
        		pixIndex = QLearning.pool.size();
        		QLearning.pool.add(pixs);
        	}
    		currentIndex++;
    		
    		if(currentIndex == poolSize)
    		{
    			currentIndex = 0;
    			isFull = true;
    		}
    	}
        
      //else index = learning.getMaxActionIndex(pixIndex);
        double[][] tmpTest = new double[1][pixs.length*pixs[0].length];
        tmpTest[0] = flattenImage(pixs);
        
        INDArray test = Nd4j.create(tmpTest);//new NDArray(1,pixs.length*pixs[0].length);
       // test.putRow(0, Nd4j.create(flattenImage(pixs)));
        INDArray pd = model.output(test);
    //    System.out.println(pd);
        
        
        writeOutput.write(pd.toString()+"\n");
        
        
      //  for(int i=0;i<pd.length;i++)
      //  	System.out.print(actions.get(i)+":"+pd[i]+", ");
     //   System.out.println();
        
        int index;
     //   System.out.println("eps "+learning.epsilon);
        
     //   index = learning.getMaxActionIndex(pixIndex);
        
        boolean ran = false;
        
        if(random.nextDouble()<learning.epsilon)
        {
        	index = random.nextInt(numAct);
        	ran = true;
        }
        else
        	index = model.predict(test)[0];
        
        
        
        for(int i=0;i<numAct;i++)
        {
    //    	System.out.print(learning.qValues[pixIndex][i]+" ");
        	writeOutput.write(learning.qValues[pixIndex][i]+" ");
        }
    //    System.out.println();
        writeOutput.write("\n");
        
        double[] d = learning.normalize(pixIndex);
        for(int i=0;i<d.length;i++)
        {
    //    	System.out.print(d[i]+" ");
        	writeOutput.write(d[i]+" ");
        }
    //    System.out.println();
        writeOutput.write("\n");
        
        experience.setAction(actions.get(index));
        
        
     //   System.out.println(stateObs.getGameTick()+" "+actions.get(index)+"\n");
        
        writeOutput.write(ran+" "+pixIndex+" "+actions.get(index)+"\n\n");
        
        actionSequence += actions.get(index)+"\n";
    //    frame.update();
		return actions.get(index);
	}
	
	@Override
	public void result(StateObservation stateObservation, ElapsedCpuTimer elapsedCpuTimer)
    {
	//	System.out.println(stateObservation.isAvatarAlive());
		
		//System.out.println("WIN = "+stateObservation.getGameWinner().equals(Types.WINNER.PLAYER_WINS));
		int pixIndex = QLearning.findIndexFromImage(experience.getPrevious());
		
		
		if(stateObservation.getGameWinner().equals(Types.WINNER.PLAYER_WINS))
		{
			experience.setReward(100);
		}
		else if (!stateObservation.isAvatarAlive())
		{
			experience.setReward(-100);
		}
		
	//	System.out.println(pixIndex);
		//experience.setResult(null);
		//System.out.println(experience.getResult());
		//learning.mapper.put(experience.copy(), currentIndex);
    	//learning.qValues[currentIndex] = new double[numAct];
		
		if(pixIndex == -1)
		{
			
			if(isFull)
			{
				pixIndex = findLowestAccessedIndex();//expCount.getMinAccessAndRemove();
				QLearning.pool.set(pixIndex, experience.getPrevious());
				learning.qValues[pixIndex] = new double[numAct];
				countAccess[pixIndex]++;
				
			}
			else
			{
				pixIndex = QLearning.pool.size();
				QLearning.pool.add(experience.getPrevious());
				
			}
			currentIndex++;
		}
	//	if(pixIndex!=-1)
	//	{
			learning.qUpdate(pixIndex,-1,actions.indexOf(experience.getAction()),experience.getReward());
			//experiencePool[pixIndex] = experience.copy();
	//	}
		//else
	//	{
			
//			learning.qUpdate(pixIndex,-1,actions.indexOf(experience.getAction()), experience.getReward());
		//}
	//	System.out.println(pixIndex);
	//	if(experiencePool[pixIndex]!=null)
	//	experience.accessCount = experiencePool[pixIndex].accessCount;
		countAccess[pixIndex]++;
		writeExpToFile(experience.copy(),pixIndex);
	//	experiencePool[pixIndex] = experience.copy();
	//	expCount.increase(pixIndex);
	//	tableModel.setValueAt("experience "+pixIndex,pixIndex,0);
    	
		//System.out.println(learning.qValues[pixIndex][0]);
		
		if(currentIndex == poolSize)
		{
			currentIndex = 0;
			isFull = true;
		}
    	
    	if(learning.epsilon>0.1)
    		learning.epsilon -= 0.02;
    	//	learning.epsilon = 1.0/(ArcadeMachine.i+1);
    
    		//DecimalFormat df = new DecimalFormat("##.####");
    //		System.out.println("EPS "+" "+learning.epsilon);
    	experience = null;
    	countRound = 0;
    	
    	
    	
    	//Save Model Here
    	if(ArcadeMachine.i!=0&&ArcadeMachine.i%saveModelFreq == 0)
    	{
    	//	System.out.println("save");
    		//String path = ArcadeMachine.filePath;
    		DateTime dateTime = new DateTime();
    		String d = dateTime.getDayOfMonth()+"_"+dateTime.getMonthOfYear()+"_"+dateTime.getHourOfDay()+"_"+dateTime.getMinuteOfHour()+"_"+dateTime.getSecondOfMinute();
    		//String dt = ArcadeMachine.gameDescription+"_"+d;
    		
    		 String[] directory = (theDir.getPath()+"/"+d+"/experience/").split("\\/");
             
             createNestedDir(directory);
    		
    	//	File theDir1 = new File(path+"/"+dt);
    	//	theDir1.mkdir();
    		
    	//	path+="/"+dt+"/";
    //		System.out.println(path);
    		
    		try(DataOutputStream dos = new DataOutputStream(Files.newOutputStream(Paths.get(theDir.getPath()+"/"+d+"/"+"coefficients.bin")))){
                Nd4j.write(model.params(),dos);
            } catch (IOException e) {
				e.printStackTrace();
			}

            //Write the network configuration:
            try {
				FileUtils.write(new File(theDir.getPath()+"/"+d+"/conf.json"), model.getLayerWiseConfigurations().toJson());
			} catch (IOException e) {
				e.printStackTrace();
			}
        
          //Save the updater:
            try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(theDir.getPath()+"/"+d+"/updater.bin"))){
                oos.writeObject(model.getUpdater());
            } catch (IOException e) {
				e.printStackTrace();
			}
            System.out.println("finished save model at "+d);
            
            File newDir = new File(theDir.getPath()+"/"+d+"/experience/");
            newDir.mkdir();
            
            for(int i=0;i<countAccess.length;i++)
            {
            	try 
            	{
					
					if(countAccess[i]>0)
	            	{
						PrintWriter p = new PrintWriter(new FileWriter(new File(newDir.getPath()+"/exp"+i+".txt")));
						
						Experience exp = readExpFromFile(i,w,h);
	            		double[][] prev = exp.getPrevious();
	            		double[][] res = exp.getResult();
	            		double reward = exp.getReward();
	            		String action = exp.getAction().toString();
	            		int access = countAccess[i];
	            		
	            		p.write("Previous=================================\n");
	            		for(int m=0;m<prev.length;m++)
	            		{
	            			for(int n=0;n<prev[0].length;n++)
	            				p.write(prev[m][n]+" ");
	            			p.write("\n");;
	            		}
	            		p.write("Result=================================\n");
	            		if(res!=null)
	            		for(int m=0;m<res.length;m++)
	            		{
	            			for(int n=0;n<res[0].length;n++)
	            				p.write(res[m][n]+" ");
	            			p.write("\n");;
	            		}
	            		else
	            			p.write("END OF EP\n");
	            		p.write("Action="+action+"\n");
	            		p.write("Reward="+reward+"\n");
	            		p.write("access="+access+"\n");
	            		
	            		p.close();
	            	}
					else break;
					
				} catch (IOException e) 
            	{
					e.printStackTrace();
				}
            	
            }
            System.out.println("finished exp");
            
            
    	}
    	writeOutput.write(stateObservation.getGameScore()+" ");
    	writeOutput.write((stateObservation.getGameWinner().toString()));
    	writeOutput.close();
    	
    	try 
    	{
			writeOutput = new PrintWriter(new FileWriter(new File(theDir.getPath()+"/run"+(ArcadeMachine.i+1)+".txt")));
			
		} catch (IOException e) 
    	{
		}
    	
    	try 
    	{
			PrintWriter outputAction = new PrintWriter(new File(theDir.getPath()+"/run"+(ArcadeMachine.i)+"action.txt"));
			outputAction.write(actionSequence);
			outputAction.close();
			actionSequence = "";
		} catch (FileNotFoundException e) 
    	{
			e.printStackTrace();
		}
    	
    	if(ArcadeMachine.i==0)
    	try {
			PrintWriter p = new PrintWriter(new FileWriter(new File(theDir.getPath() + "/mapper.properties")));
			Object[] key = mapper.keySet().toArray();
			for (int i = 0; i < mapper.size(); i++) {
				Color color = mapper.get((int) key[i]);
				p.write(key[i] + "=" + color.getRed() + "," + color.getGreen() + "," + color.getBlue());
				p.write("\n");
			}
			p.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
//	private static final Logger log = LoggerFactory.getLogger(Agent.class);
	
	private int findLowestAccessedIndex()
	{
		int min = 0;
		
		for(int i=0;i<countAccess.length;i++)
		{
			if(countAccess[i]>0)
			{
				if(countAccess[i]<countAccess[min])
					min = i;
			}
			else break;
		}
		return min;
	}
	
	private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

		if(image==null)
			return null;

	      final int width = image.getWidth();
	      final int height = image.getHeight();
	    //  System.out.println("2D "+width+" "+height);
	      int[][] result = new int[width][height];
	      
	      for(int i=0;i<width;i++)
	    	  for(int j=0;j<height;j++)
	    	  {
	    		 // System.out.println(width+" "+height+" "+i+" "+j);
	    		  result[i][j] = image.getRGB(i, j);
	    	  }
	
	      
	      
	      return result;
	   }

	public static double[][] preProcess(BufferedImage image, int block)
	{
		int[][] im = convertTo2DWithoutUsingGetRGB(image);
		if(im == null)
			return null;
		//System.out.println(im.length+" "+im[0].length);
	
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		//int res = width*height;
		
		double[][] shrinkedIm = new double[width/block][height/block];
		
	//	System.out.println(im.length+" "+im[0].length);
	//	System.out.println(shrinkedIm.length+" "+shrinkedIm[0].length);
		
		
		for(int i=0;i<width/block;i++)
		{
			for(int j=0;j<height/block;j++)
			{
				//System.out.println(i+"\t"+j);
				int startW = i*block;
				int endW = startW+(block-1);
				int startH = j*block;
				int endH = startH+(block-1);
				
	//			System.out.println(i+"\t"+j+"\t"+startW+"\t"+endW+"\t"+startH+"\t"+endH);
				
				int mean[] = new int[3];//Integer.MIN_VALUE;
				
				for(int k=startW;k<=endW;k++)
					for(int m=startH;m<=endH;m++)
					{
						Color c = new Color(im[k][m]);
						int red = c.getRed();
						int green = c.getGreen();
						int blue = c.getBlue();
							
						//if(im[k][m]>mean)
						//mean+=im[k][m];
						mean[0] += red;
						mean[1] += green;
						mean[2] += blue;
					}
				
				mean[0]/= block*block;
				mean[1]/= block*block;
				mean[2]/= block*block;
				
				shrinkedIm[i][j] = mean[0]*65536+mean[1]*256+mean[2];
			}
		}
		/*
		
		
	
		File outputfile = new File("nm_"+block+".png");
	    try {
			ImageIO.write(newImage, "png", outputfile);
		} catch (IOException e) { 
			e.printStackTrace();
		}
	    
       
		*/
		//System.out.println(shrinkedIm);
		return shrinkedIm;
		
	}
	
	public static double[][] extendedImage(double[][] image, int width)
	{
		int newW = image[0].length;
		int newH = image.length;
		
		int height = width;
		
		if(newW>=width && newH>=height)
			return image;
		
		if(newW<width)
			newW = width;
		
		if(newH<height)
			newH = height;
		
		double[][] newIm = new double[newH][newW];
		
	//	System.out.println(newW+" "+newH+" "+image[0].length+" "+image.length);
		for(int i=0;i<image.length;i++)
			for(int j=0;j<image[0].length;j++)
			{
	//			System.out.println(i+" "+j);
				newIm[i][j] = image[i][j];
			}
		
		return newIm;
	}
	
	public static double[] flattenImage(double[][] image)
    {
    	Color white = Color.WHITE;
    	int rw = white.getRed();
    	int gw = white.getGreen();
    	int bw = white.getBlue();
    	
    	int norm = rw*65536+gw*256+bw;
    	
    	double[] newImage = new double[image.length*image[0].length];
    	for(int i=0;i<image.length;i++)
    	{
    		for(int j=0;j<image[0].length;j++)
    		{
    			newImage[i*image[0].length+j] = image[i][j]/(double)norm;
    		}
    	}
    	return newImage;
    }
	
	public void createNestedDir(String[] str)
	{
		String concat = "";
		for(int i=0;i<str.length;i++)
		{
			concat +=str[i]+"/";
			File theDir = new File(concat);
			if(!theDir.exists())
				theDir.mkdirs();
		}
	}
	
	public void writeExpToFile(Experience exp, int index)
	{
		createNestedDir((theDir+"/experience/").split("\\/"));
		PrintWriter expWriter = null;
		try {
			expWriter = new PrintWriter(new FileWriter(new File(theDir+"/experience/"+index+".txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		double[][] prev = exp.getPrevious();
		double[][] res = exp.getResult();
		double reward = exp.getReward();
		String action = exp.getAction().toString();
		int access = countAccess[index];
		
		expWriter.write("Previous=================================\n");
		for(int m=0;m<prev.length;m++)
		{
			for(int n=0;n<prev[0].length;n++)
				expWriter.write(prev[m][n]+" ");
			expWriter.write("\n");;
		}
		expWriter.write("Result=================================\n");
		if(res!=null)
		for(int m=0;m<res.length;m++)
		{
			for(int n=0;n<res[0].length;n++)
				expWriter.write(res[m][n]+" ");
			expWriter.write("\n");;
		}
		else
			expWriter.write("END OF EP\n");
		expWriter.write("Action="+action+"\n");
		expWriter.write("Reward="+reward+"\n");
		expWriter.write("access="+access+"\n");
		
		expWriter.close();
	
	}
	
	public Experience readExpFromFile(int index, int width, int height)
	{
		Experience exp = new Experience();
	
		try 
		{
			String fileName = theDir+"/experience/"+index+".txt";
		//	System.out.println(fileName);
			BufferedReader b = new BufferedReader(new FileReader(new File(fileName)));
			
			String st = "";
		
			
			st = b.readLine();
			
			
			double[][] tmp = new double[width][height];
			
				for (int i = 0; i < width; i++) {
					st = b.readLine();
					String[] string = st.split(" ");
					for (int j = 0; j < height; j++) {
						tmp[i][j] = Double.parseDouble(string[j]);
					}
				}
			
			exp.setPrevious(tmp);
			
			st = b.readLine();
				
			tmp = new double[width][height];
			try
			{
				for (int i = 0; i < width; i++) {
					st = b.readLine();
					String[] string = st.split(" ");
					for (int j = 0; j < height; j++) {
						tmp[i][j] = Double.parseDouble(string[j]);
					}
				}
			}
			catch(NumberFormatException e)
			{
				tmp = null;
			//	b.readLine();
			}
			exp.setResult(tmp);
			
			st = b.readLine();

		//	System.out.println(st);
			exp.setAction(Types.ACTIONS.fromString(st.split("=")[1]));
			//System.out.println(exp.getAction());
			st = b.readLine();
			exp.setReward(Double.parseDouble(st.split("=")[1]));
				
			b.close();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		return exp;
		
	}

}
