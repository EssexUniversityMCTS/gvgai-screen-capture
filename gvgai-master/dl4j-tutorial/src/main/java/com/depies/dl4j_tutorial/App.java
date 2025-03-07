package com.depies.dl4j_tutorial;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.canova.api.records.reader.RecordReader;
import org.canova.api.records.reader.impl.CSVRecordReader;
import org.canova.api.split.FileSplit;
import org.canova.api.util.ClassPathResource;
import org.deeplearning4j.datasets.canova.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import java.util.Random;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.NDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App 
{
	private static final Logger log = LoggerFactory.getLogger(App.class);
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		int res = width*height;
		
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
			// TODO Auto-generated catch block
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
		return shrinkedIm;
		
	}
	
	static Random random = new Random();
	
    public static void main( String[] args )throws Exception 
    {
    	
    	/*
    	INDArray nd = Nd4j.create(new float[]{1,2,3,4},new int[]{2,2});
    	System.out.println(nd);
    	nd = nd.mul(5);
        System.out.println(nd);
        
        RecordReader recordReader = new CSVRecordReader(0,",");
        recordReader.initialize(new FileSplit(new File("test.txt")));
        //reader,label index,number of possible labels
        DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader,6,3);
  
        
        while(iterator.hasNext())
        {
        	System.out.println("ttt\n"+iterator.next());	
        }
        */
            int nChannels = 1;
            int outputNum = 2;
            int batchSize = 100;//1000;
            int nEpochs = 10;
            int iterations = 10;
            int seed = 123;

            log.info("Load data....");
          //  DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize,true,12345);
            
           // DataSetIterator mnistTest = new MnistDataSetIterator(batchSize,false,12345);
            
            File imgPath = new File("pic/black.png");//../screenshots/2_2.0.png");//_shrink.png");
            BufferedImage bufferedImage = ImageIO.read(imgPath);
            
            int blockW = 25;
            double[][] pixs = preProcess(bufferedImage,blockW);
            
        //    System.out.println(bufferedImage.getWidth()+"x"+bufferedImage.getHeight()+"="+pixels.length);
      
          //  System.out.println(pixs[2]);
            int w = bufferedImage.getWidth()/blockW;
            int h = bufferedImage.getHeight()/blockW;
            /*
            INDArray blackND = readImageFromFile("pic/black.png",blockW);
            INDArray redND = readImageFromFile("pic/red.png",blockW);
            INDArray pinkND = readImageFromFile("pic/pink.png",blockW);
            INDArray greyND = readImageFromFile("pic/grey.png",blockW);
            INDArray blackBoxND = readImageFromFile("pic/box.png",blockW);
            INDArray greyBoxND = readImageFromFile("pic/box2.png",blockW);
            INDArray redBoxND = readImageFromFile("pic/redBox.png",blockW);
            
            
            //System.out.println(blackND+"\n"+redND+"\n"+pinkND+"\n"+greyND);
            
            INDArray nd = Nd4j.create(6,redND.length());
            
     //       System.out.println(blackND);
            nd.putRow(0, blackND);
            nd.putRow(1, pinkND);
            nd.putRow(2, greyBoxND);
            nd.putRow(3, redND);
            nd.putRow(4, blackBoxND);
            nd.putRow(5, redBoxND);
            
            INDArray test = greyND;
            */
            
            int genSize = 50;
    		double[] greyND = App.readToDouble("pic/grey.png",blockW);
            double[] blackBoxND = App.readToDouble("pic/box.png",blockW);
           
            INDArray training0 = App.generate(greyND, genSize);
            INDArray training1 = App.generate(blackBoxND, genSize);
            
            INDArray shuffle = new NDArray(genSize*2,greyND.length);
            int[] label = new int[genSize*2];
            int index0 = 0;
            int index1 = 0;
            
            for(int i=0;i<genSize*2;i++)
            {
            	boolean nextB = random.nextBoolean();
            	if((nextB&&index0<genSize)||index1==genSize)
            	{
            		shuffle.putRow(i, training0.getRow(index0++).dup());
            		label[i] = 0;
            	}
            	else
            	{
            		shuffle.putRow(i, training1.getRow(index1++).dup());
            		label[i] = 1;
            	}
            	
            }
            
            INDArray trainingSet = new NDArray(genSize*4/5,greyND.length);
            int[] trainingLabel = new int[trainingSet.rows()];
            INDArray testSet = new NDArray(genSize/5,greyND.length);
            int[] testLabel = new int[testSet.rows()];
            
            for(int i=0;i<trainingSet.rows();i++)
            {
            	
            	trainingSet.putRow(i, shuffle.getRow(i));
            	trainingLabel[i] = label[i];
            }
            
            for(int i=0;i<testSet.rows();i++)
            {
            	testSet.putRow(i, shuffle.getRow(i+trainingSet.rows()));
            	testLabel[i] = label[i+trainingSet.rows()];
            }
            
            System.out.println("Train:");
            for(int i=0;i<trainingLabel.length;i++)
            {
            	System.out.print(trainingLabel[i]+" ");
            }
            System.out.println("\nTest:");
            for(int i=0;i<testLabel.length;i++)
            {
            	System.out.print(testLabel[i]+" ");
            }
            System.out.println();
            
          
          //  System.out.println("0:\n"+training0);
            
          //  System.out.println("1:\n"+training1);
            
           /* double[][] test = new double[][]{new double[]{0,1},new double[]{2,3}};
            
            double[] test2 = flattenImage(test);
            
            for(int i=0;i<test2.length;i++)
            	System.out.print(test2[i]+" ");
            */
            /*
            for(int i=0;i<10;i++)
            {
            	nd.putRow(i, redND);
            }
            */
            
           // System.out.println(nd);
            
            //Color color = Color.WHITE;
            //System.out.println("wwww "+color);
            	
            int[] labels = new int[]{0,0,1,0,1,1};//,0,1};
            
          //Load network configuration from disk:
            MultiLayerConfiguration confFromJson = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File("conf.json")));

            //Load parameters from disk:
            INDArray newParams;
            try(DataInputStream dis = new DataInputStream(new FileInputStream("coefficients.bin"))){
                newParams = Nd4j.read(dis);
            }

            //Create a MultiLayerNetwork from the saved configuration and parameters
            MultiLayerNetwork model = new MultiLayerNetwork(confFromJson);
            model.init();
            model.setParameters(newParams);
          
//            log.info("Build model....");
//            MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
//                    .seed(seed)
//                    .iterations(iterations)
//                    .regularization(true).l2(0.0005)
//                    .learningRate(0.01)
//                    .weightInit(WeightInit.XAVIER)
//                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
//                    .updater(Updater.NESTEROVS).momentum(0.9)
//                    .list(4)
//                    .layer(0, new ConvolutionLayer.Builder(5, 5)
//                            .nIn(nChannels)
//                            .stride(1,1)
//                            .nOut(20)
//                            .activation("relu")
//                            .build())
//                   /* .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
//                            .kernelSize(2,2)
//                            .stride(2,2)
//                            .build())*/
//                    .layer(1, new ConvolutionLayer.Builder(3, 3)
//                            .nIn(nChannels)
//                            .stride(1, 1)
//                            .nOut(50)
//                            .activation("relu")
//                            .build())
//                   /* .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
//                            .kernelSize(2,2)
//                            .stride(2,2)
//                            .build())*/
//                    .layer(2, new DenseLayer.Builder().activation("relu")
//                            .nOut(500).build())
//                    .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
//                            .nOut(outputNum)
//                            .activation("softmax")
//                            .build())
//                    .backprop(true).pretrain(false);
//            new ConvolutionLayerSetup(builder,w,h,nChannels);
//
//            MultiLayerConfiguration conf = builder.build();
//            MultiLayerNetwork model = new MultiLayerNetwork(conf);
//            model.init();

          //Load the updater:
            org.deeplearning4j.nn.api.Updater updater;
            try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream("updater.bin"))){
                updater = (org.deeplearning4j.nn.api.Updater) ois.readObject();
            }
            
            //Set the updater in the network
            model.setUpdater(updater);
            
            
            model.setListeners(new ScoreIterationListener(1));
            
            log.info("Train model....");
            for( int i=0; i<nEpochs; i++ ) {
            //	System.out.println("ep "+nEpochs);
            	//System.out.println(nd+""+nd.length());
                model.fit(trainingSet,trainingLabel);// .fit(nd);//iterator);//mnistTrain);
                
        //        System.out.println(model.params());
                
                log.info("*** Completed epoch {} ***", i);

                log.info("Evaluate model....");
                
                Evaluation eval = new Evaluation(outputNum);
           //     while(.hasNext()){
                 //   DataSet ds = iterator.next();
                    int[] predict = model.predict(testSet);//.output(redND);//ds.getFeatureMatrix());//model.output(ds.getFeatureMatrix());
                    INDArray output = model.output(testSet);
                   // System.out.println("xxx"+ output.toString());
                    for(int p=0;p<predict.length;p++)
                    {
                    	System.out.println("p"+p+" = "+predict[p]+", r"+p+" = "+testLabel[p]);
                    }
                    
                    System.out.println(output);
                   // eval.eval(trainingLabel,output);//ds.getLabels(), output);
               
                    // }
           //     log.info(eval.stats());
           //     iterator.reset();//mnistTest.reset();
                
            
            }
            
            log.info("****************Example finished********************");
            
            try(DataOutputStream dos = new DataOutputStream(Files.newOutputStream(Paths.get("coefficients.bin")))){
                Nd4j.write(model.params(),dos);
            }

            //Write the network configuration:
            FileUtils.write(new File("conf.json"), model.getLayerWiseConfigurations().toJson());
        
          //Save the updater:
            try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("updater.bin"))){
                oos.writeObject(model.getUpdater());
            }
            
            System.out.println("finished save model");
    }
    
    public static INDArray readImageFromFile(String imgPath, int blockW)
    {
    	
        return Nd4j.create(readToDouble(imgPath,blockW));
    
    }
    
    public static double[] readToDouble(String imgPath, int blockW)
    {
    	BufferedImage bufferedImage = null;
		try {
			bufferedImage = ImageIO.read(new File(imgPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        double[][] pixs = preProcess(bufferedImage,blockW);
        return flattenImage(pixs);
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
    
    public static INDArray generate(double[] prototype, int num)
    {
    	
    	INDArray set = new NDArray(num,prototype.length);
    	
    	for(int i=0;i<num;i++)
    	{
    		HashMap<Double, Double> mapper = new HashMap();
    		double[] result = new double[prototype.length];
    		for(int j=0;j<prototype.length;j++)
    		{
    			
    				if(mapper.containsKey(prototype[j]))
    					result[j] = mapper.get(prototype[j]);
    				else
    				{
    					double nextDouble = random.nextDouble();
    					mapper.put(prototype[j], nextDouble);
    					result[j] = nextDouble;
    				}
    			
    		}
    		set.putRow(i, Nd4j.create(result));
    	}
    	
    	return set;
    }
}
