package controllers.ScreenCaptureAgent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

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
import org.nd4j.linalg.cpu.NDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import core.ArcadeMachine;
import core.game.Game;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer{

	/**
	 * initialize all variables for the agent
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 */
	static int poolSize = 2000;
	static int batchSize = 100;
	static int maxW = 31;
	static int maxH = 48;
	static int currentIndex;
	boolean isFull;
	
	int minBlockSize = 5;
	int firstLayerStride = 1;
	int learningFrequency = 1;
	int w,h;
	
	int saveModelFreq = 10;
	
	public static Experience[] experiencePool = new Experience[poolSize];
	
	
	Random random = new Random();
	MyFrame frame;
	int numAct;
	ArrayList<Types.ACTIONS> actions; 
	public MultiLayerNetwork model;
	
	DefaultTableModel tableModel;
	
	private QLearning learning;
	Experience experience;
	int countRound = 0;
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		learning = new QLearning(experiencePool.length,stateObs.getAvailableActions().size());
		frame = new MyFrame(experiencePool,stateObs.getAvailableActions(),learning.qValues);
		tableModel = (DefaultTableModel) frame.table.getModel();
		frame.setVisible(true);
		currentIndex = 0;
		actions = stateObs.getAvailableActions();
	    numAct = actions.size();
	    experience = null;
		 int nChannels = 1;
	     int outputNum = numAct;
	     //int batchSize = 100;//1000;
	     int nEpochs = 1;
	     int iterations = 1;
	     int seed = 123;
	       
	     
	     for(int i=0;i<poolSize;i++)
	    	 tableModel.addRow(new String[]{});
	     /*   BufferedImage bufferedImage = Game.im;
			
	        while(bufferedImage == null) {
				//	bufferedImage = ImageIO.read(imgPath);
		//			System.out.println("null");
	        	bufferedImage = Game.im;
				//	return null;
			}
	       */ 
	        int blockW = stateObs.getBlockSize();//25;
	       // float[] pixs = preProcess(bufferedImage,blockW);
	        
	    //    System.out.println(bufferedImage.getWidth()+"x"+bufferedImage.getHeight()+"="+pixels.length);
	  
	      //  System.out.println(pixs[2]);
	        Dimension d = stateObs.getWorldDimension();
	        w = d.width/blockW;//bufferedImage.getWidth()/blockW;
	        h = d.height/blockW;//bufferedImage.getHeight()/blockW;
	        
	        if(w<minBlockSize)
	        	w = minBlockSize;
	        if(h<minBlockSize)
	        	h = minBlockSize;
	        
	        System.out.println(w+" "+h);
	        
	       // INDArray nd = Nd4j.create(pixs);
   //     log.info("Build model....");
	        
	        if(!ArcadeMachine.continueLearning)
	        {
	        MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
                    .seed(seed)
                    .iterations(iterations)
                    .regularization(true).l2(0.00001)
                    .learningRate(0.001)
                    //.dropOut(0.2)
                    .weightInit(WeightInit.RELU)
                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                    .updater(Updater.RMSPROP).momentum(0.9)
                    .list(4)
                    .layer(0, new ConvolutionLayer.Builder(minBlockSize-firstLayerStride, minBlockSize-firstLayerStride)
                            .nIn(nChannels)
                            .stride(firstLayerStride,firstLayerStride)
                            .nOut(32)
                            .activation("relu")
                            .build())
                    /*.layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                            .kernelSize(2,2)
                            .stride(2,2)
                            .build())
                    */.layer(1, new ConvolutionLayer.Builder(2, 2)
                            .nIn(nChannels)
                            .stride(1, 1)
                            .nOut(64)
                            .activation("relu")
                            .build())
                    /*.layer(2, new ConvolutionLayer.Builder(3, 3)
                            .nIn(nChannels)
                            .stride(1, 1)
                            .nOut(64)
                            .activation("relu")
                            .build())*/
                   /* .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                            .kernelSize(2,2)
                            .stride(2,2)
                            .build())*/
                    .layer(2, new DenseLayer.Builder().activation("relu")
                            .nOut(512).build())
                    .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                            .nOut(outputNum)
                            .activation("softmax")
                            .build())
                    .backprop(true).pretrain(false);
        new ConvolutionLayerSetup(builder,w,h,1);

        MultiLayerConfiguration conf = builder.build();
        model = new MultiLayerNetwork(conf);
        model.init();
	    }
	        
	        else
	        {
	        	String path = ArcadeMachine.filePath;
	        	//Load network configuration from disk:
	            MultiLayerConfiguration confFromJson = null;
				try {
					confFromJson = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(path+"/conf.json")));
				} catch (IOException e1) {
					e1.printStackTrace();
				}

	            //Load parameters from disk:
	            INDArray newParams = null;
	            try(DataInputStream dis = new DataInputStream(new FileInputStream(path+"/coefficients.bin"))){
	                newParams = Nd4j.read(dis);
	            } catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

	            //Create a MultiLayerNetwork from the saved configuration and parameters
	            model = new MultiLayerNetwork(confFromJson);
	            model.init();
	            model.setParameters(newParams);
	            
	          //Load the updater:
	            org.deeplearning4j.nn.api.Updater updater = null;
	            try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path+"/updater.bin"))){
	                try {
						updater = (org.deeplearning4j.nn.api.Updater) ois.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
	            } catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	            
	            //Set the updater in the network
	            model.setUpdater(updater);
	            
	            
	            model.setListeners(new ScoreIterationListener(1));
	        }
	        
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
		System.out.println("finished create Agent");
	}
	
	/**
	 * return ACTION_NIL on every call to simulate doNothing player
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 * @return 	ACTION_NIL all the time
	 */
	
	
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
		
       
       // log.info("Load data....");
      //  DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize,true,12345);
        
       // DataSetIterator mnistTest = new MnistDataSetIterator(batchSize,false,12345);

      //  File imgPath = new File("screenshots/2_2.0.png");//_shrink.png");
        BufferedImage bufferedImage = Game.im;
		
        //System.out.println(currentIndex);
        
        if(bufferedImage == null) {
			//	bufferedImage = ImageIO.read(imgPath);
	//			System.out.println("null");
				return Types.ACTIONS.ACTION_NIL;
		}
        else Game.im = null;
        
        int blockW = stateObs.getBlockSize();//25;
        double[][] pixs = extendedImage(preProcess(bufferedImage,blockW),minBlockSize);
        
        
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
        	if(learning.ImageEquals(experience.getPrevious(), pixs))
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
        				prevIndex = currentIndex;
        				QLearning.pool.set(currentIndex, experience.getPrevious());
        				learning.qValues[currentIndex] = new double[numAct];
        			}
        			
        			else
        			{
        				prevIndex = QLearning.pool.size();
        				QLearning.pool.add(experience.getPrevious());//.set(currentIndex,experience.getPrevious().clone());
        			}
        			experiencePool[prevIndex] = experience.copy();
        			tableModel.setValueAt("experience "+prevIndex,prevIndex,0);
                	
        			currentIndex++;
        			
        		}
        		else //update?
        		{
      //  			System.out.println("not add new");
        			experiencePool[prevIndex] = experience.copy();
        			tableModel.setValueAt("experience "+prevIndex,prevIndex,0);
                	
        		}
        	//	System.out.println(experience.getAction());
        	//	System.out.println(actions.indexOf(experience.getAction()));
        		learning.qUpdate(prevIndex, pixIndex,actions.indexOf(experience.getAction()),experience.getReward());
            	
        	}
        	catch(Exception e)
        	{
        		//learning.pool.add(pixs);
        	}
        	
        	if(currentIndex == poolSize)
    		{
    			currentIndex = 0;
    			isFull = true;
    		}
        	
        	experience = new Experience();
        	experience.setPrevious(pixs);
        }
        
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
        int limit = batchSize;
        if(QLearning.pool.size()<batchSize)
       	limit = QLearning.pool.size()/4;
    //    {
        INDArray training = new NDArray(limit,pixs.length*pixs[0].length);
        INDArray labels = new NDArray(limit,numAct);
        
        
        for(int i=0;i<limit;i++)
        {
        	int rand = 0;
        	do
        	{
        		//System.out.println(learning.pool.size()+" "+rand);
        		rand = random.nextInt(QLearning.pool.size());//*numAct-1);
        		
        	}while(experiencePool[rand]==null);
        	
        	Experience toUpdateExp = experiencePool[rand];
        	int startIndex = QLearning.findIndexFromImage(toUpdateExp.getPrevious());
        	int nextIndex = QLearning.findIndexFromImage(toUpdateExp.getResult());
   
        	//if(startIndex == -1)
        	//	continue;
        	learning.qUpdate(startIndex, nextIndex,actions.indexOf(toUpdateExp.getAction()),toUpdateExp.getReward());
        	if(countRound++%learningFrequency == 0){
        	training.putRow(i,Nd4j.create(flattenImage(toUpdateExp.getPrevious())));
        	labels.putRow(i,  Nd4j.create(learning.normalize(startIndex)));
        	}
        	
     //   }}catch(Exception e){}
        //     System.out.println(index);
        }
        
        if(countRound++%learningFrequency == 0)
        if(limit>0)
        { 
    //    	System.out.println(training);
    //    	System.out.println(labels);
        	
        	model.fit(training, labels);
        
        //learning.getMaxActionIndex(pixIndex);
        }
        //else index = learning.getMaxActionIndex(pixIndex);
        INDArray test = new NDArray(1,pixs.length*pixs[0].length);
        test.putRow(0, Nd4j.create(flattenImage(pixs)));
        INDArray pd = model.output(test);
        System.out.println(pd);
      //  for(int i=0;i<pd.length;i++)
      //  	System.out.print(actions.get(i)+":"+pd[i]+", ");
     //   System.out.println();
        
        if(pixIndex==-1)
    	{
        	if(isFull)
			{
				pixIndex = currentIndex;
				QLearning.pool.set(currentIndex, pixs);
				learning.qValues[currentIndex] = new double[numAct];
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
        
        int index;
        
     //   index = learning.getMaxActionIndex(pixIndex);
        if(random.nextDouble()<learning.epsilon)
        	index = random.nextInt(numAct);
        else
        	index = model.predict(test)[0];
        
        for(int i=0;i<numAct;i++)
        	System.out.print(learning.qValues[pixIndex][i]+" ");
        System.out.println();
        
        double[] d = learning.normalize(pixIndex);
        for(int i=0;i<d.length;i++)
        	System.out.print(d[i]+" ");
        System.out.println();
        
        experience.setAction(actions.get(index));
        
        System.out.println(pixIndex+" "+actions.get(index)+"\n");
        
        
        
        frame.update();
		return actions.get(index);
	}
	
	@Override
	public void result(StateObservation stateObservation, ElapsedCpuTimer elapsedCpuTimer)
    {
		System.out.println(stateObservation.isAvatarAlive());
		
		System.out.println("WIN = "+stateObservation.getGameWinner().equals(Types.WINNER.PLAYER_WINS));
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
				pixIndex = currentIndex;
				QLearning.pool.set(currentIndex, experience.getPrevious());
				learning.qValues[currentIndex] = new double[numAct];
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
			
			learning.qUpdate(pixIndex,-1,actions.indexOf(experience.getAction()), experience.getReward());
		//}
		System.out.println(pixIndex);
		experiencePool[pixIndex] = experience.copy();
		tableModel.setValueAt("experience "+pixIndex,pixIndex,0);
    	
		//System.out.println(learning.qValues[pixIndex][0]);
		
		if(currentIndex == poolSize)
		{
			currentIndex = 0;
			isFull = true;
		}
    	
    	if(learning.epsilon>0.01)
    		learning.epsilon -= 0.01;
    
    	experience = null;
    	
    	if(ArcadeMachine.i%saveModelFreq == 0)
    	{
    		String path = ArcadeMachine.filePath;
    		DateTime dateTime = new DateTime();
    		String dt = dateTime.getDayOfMonth()+"_"+dateTime.getMonthOfYear()+"_"+dateTime.getHourOfDay()+"_"+dateTime.getMinuteOfHour();
    		
    		File theDir = new File(path+"/"+dt);
    		theDir.mkdir();
    		
    		path+="/"+dt+"/";
    		
    		try(DataOutputStream dos = new DataOutputStream(Files.newOutputStream(Paths.get(path+"coefficients.bin")))){
                Nd4j.write(model.params(),dos);
            } catch (IOException e) {
				e.printStackTrace();
			}

            //Write the network configuration:
            try {
				FileUtils.write(new File(path+"conf.json"), model.getLayerWiseConfigurations().toJson());
			} catch (IOException e) {
				e.printStackTrace();
			}
        
          //Save the updater:
            try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path+"updater.bin"))){
                oos.writeObject(model.getUpdater());
            } catch (IOException e) {
				e.printStackTrace();
			}
            
            System.out.println("finished save model");
    	}
    }
	
//	private static final Logger log = LoggerFactory.getLogger(Agent.class);
	private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

		if(image==null)
			return null;
	/*	  int [] pix =  ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		  
		  ByteBuffer byteBuffer = ByteBuffer.allocate(pix.length * 4);        
	      IntBuffer intBuffer = byteBuffer.asIntBuffer();
	      intBuffer.put(pix);

	      final byte[] pixels = byteBuffer.array();
	     */
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
	      
	      
	/*      final boolean hasAlphaChannel = image.getAlphaRaster() != null;

	      int[][] result = new int[height][width];
	      if (hasAlphaChannel) {
	         final int pixelLength = 4;
	         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
	            int argb = 0;
	            argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
	            argb += ((int) pixels[pixel + 1] & 0xff); // blue
	            argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
	            argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
	            result[row][col] = argb;
	            col++;
	            if (col == width) {
	               col = 0;
	               row++;
	            }
	         }
	      } else {
	         final int pixelLength = 3;
	         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
	            int argb = 0;
	            argb += -16777216; // 255 alpha
	            argb += ((int) pixels[pixel] & 0xff); // blue
	            argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
	            argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
	            result[row][col] = argb;
	            col++;
	            if (col == width) {
	               col = 0;
	               row++;
	            }
	         }
	      }
*/
	      return result;
	   }

	public static double[][] preProcess(BufferedImage image, int block)
	{
		int[][] im = convertTo2DWithoutUsingGetRGB(image);
		if(im == null)
			return null;
		//System.out.println(im.length+" "+im[0].length);
	/*	BufferedImage nm = new BufferedImage(im.length,im[0].length,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<im.length;i++)
			for(int j=0;j<im[0].length;j++)
			{
				nm.setRGB(i,j,im[i][j]);
			}
		BufferedImage temp = new BufferedImage(im.length,im[0].length,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<im.length;i++)
			for(int j=0;j<im[0].length;j++)
				temp.setRGB(i,j,im[i][j]);
		File op = new File("pre.png");
	    try {
			ImageIO.write(temp, "png", op);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		
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
		for(int i=0;i<shrinkedIm.length;i++)
		{
			for(int j=0;j<shrinkedIm[0].length;j++)
				System.out.print(shrinkedIm[i][j]+",");
			System.out.println();
		}
		*/
		/*
		BufferedImage newImage = new BufferedImage(shrinkedIm.length,shrinkedIm[0].length,BufferedImage.TYPE_INT_RGB);
		
		for(int i=0;i<shrinkedIm.length;i++)
			for(int j=0;j<shrinkedIm[0].length;j++)
			{
				newImage.setRGB(i,j,shrinkedIm[i][j]);
			}
	
		File outputfile = new File("nm_"+block+".png");
	    try {
			ImageIO.write(newImage, "png", outputfile);
		} catch (IOException e) { 
			e.printStackTrace();
		}
	    
        // get DataBufferBytes from Raster
        WritableRaster raster = newImage .getRaster();
        DataBufferInt data   = (DataBufferInt) raster.getDataBuffer();
        
        int[] pixels = data.getData();
        float[] pixs = new float[pixels.length];
        
        for(int i=0;i<pixs.length;i++)
        	pixs[i] = pixels[i];
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

}
