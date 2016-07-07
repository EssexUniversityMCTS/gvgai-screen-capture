package controllers.ScreenCaptureAgent;

import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
//import org.deeplearning4j.nn.conf.LearningRatePolicy;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import core.ArcadeMachine;

public class ModelOrganizer {

	public MultiLayerNetwork model;
	
	int nChannels = 1;
    int outputNum;// = numAct;
    //int batchSize = 100;//1000;
    int nEpochs = 1;
    int iterations = 1;
    int seed = 123;
    
    
	public ModelOrganizer(Dimension d, int outputNum)
	{
		this.outputNum = outputNum;
		
		int w = (int)d.getWidth();
		int h = (int)d.getHeight();
		
		
        
        if(!ArcadeMachine.continueLearning)
		{
        //	System.out.println(ArcadeMachine.currentSubsampling);
			if (!ArcadeMachine.currentSubsampling) 
			{
//				System.out.println("(int)ArcadeMachine.currentKernel1.getWidth() "+(int)ArcadeMachine.currentKernel1.getWidth());
//				System.out.println("(int)ArcadeMachine.currentKernel1.getHeight() "+(int)ArcadeMachine.currentKernel1.getHeight());
//				System.out.println("ArcadeMachine.strideSize_1 "+ArcadeMachine.strideSize_1);
//				System.out.println("(int)ArcadeMachine.currentKernel2.getWidth() "+(int)ArcadeMachine.currentKernel2.getWidth());
//				System.out.println("(int)ArcadeMachine.currentKernel2.getHeight() "+(int)ArcadeMachine.currentKernel2.getHeight());
//				System.out.println("ArcadeMachine.strideSize_2 "+ArcadeMachine.strideSize_2);
//				System.out.println("w "+w);
//				System.out.println("h "+h);
				MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder().seed(seed)
						.iterations(iterations).regularization(true).l2(0.00001)
						.learningRate(ArcadeMachine.initialLearningRate)
					//	.learningRateDecayPolicy(LearningRatePolicy.Sigmoid)
						.dropOut(ArcadeMachine.currentDropOut)
						.weightInit(WeightInit.RELU)
						.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
						.updater(Updater.RMSPROP)
						.momentum(0.9)
						.list(4)
						.layer(0,new ConvolutionLayer
								.Builder((int)ArcadeMachine.currentKernel1.getWidth(),(int)ArcadeMachine.currentKernel1.getHeight())
								.nIn(nChannels)
								.stride(ArcadeMachine.strideSize_1, ArcadeMachine.strideSize_1)
								.nOut(32)
								.activation("relu")
								.build())
						.layer(1,new ConvolutionLayer
								.Builder((int)ArcadeMachine.currentKernel2.getWidth(), (int)ArcadeMachine.currentKernel2.getHeight())
								.nIn(nChannels)
								.stride(ArcadeMachine.strideSize_2, ArcadeMachine.strideSize_2)
								.nOut(64)
								.activation("relu").build())
						.layer(2, new DenseLayer
								.Builder()
								.activation("relu")
								.nOut(512)
								.build())
						.layer(3, new OutputLayer
								.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
								.nOut(outputNum)
								.activation("softmax")
								.build())
						.backprop(true)
						.pretrain(false);
				new ConvolutionLayerSetup(builder, w, h, 1);

				MultiLayerConfiguration conf = builder.build();
				model = new MultiLayerNetwork(conf);
				model.init();
			}
			else
			{
				MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder().seed(seed)
						.iterations(iterations).regularization(true).l2(0.00001).learningRate(ArcadeMachine.initialLearningRate)
						.dropOut(ArcadeMachine.currentDropOut)
						.weightInit(WeightInit.RELU).optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
						.updater(Updater.RMSPROP).momentum(0.9).list(6)
						.layer(0,new ConvolutionLayer
								.Builder((int)ArcadeMachine.currentKernel1.getWidth(),(int)ArcadeMachine.currentKernel1.getHeight())
								.nIn(nChannels)
								.stride(ArcadeMachine.strideSize_1, ArcadeMachine.strideSize_1)
								.nOut(32)
								.activation("relu")
								.build())
						 .layer(1,new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX) 
								 .kernelSize(2,2)
								 .stride(1,1)
								 .build())
						 .layer(2,new ConvolutionLayer
								 .Builder((int)ArcadeMachine.currentKernel2.getWidth(), (int)ArcadeMachine.currentKernel2.getHeight())
								 .nIn(nChannels)
								 .stride(ArcadeMachine.strideSize_2, ArcadeMachine.strideSize_2)
								 .nOut(64)
								 .activation("relu").build())
						 .layer(3,new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX) 
								 .kernelSize(2,2) 
								 .stride(1,1) 
								 .build())
						.layer(4, new DenseLayer.Builder()
								.activation("relu")
								.nOut(512)
								.build())
						.layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
								.nOut(outputNum)
								.activation("softmax")
								.build())
						.backprop(true).
						pretrain(false);
				new ConvolutionLayerSetup(builder, w, h, 1);

				MultiLayerConfiguration conf = builder.build();
				model = new MultiLayerNetwork(conf);
				model.init();
			}
        }  
        else
        {
        	String path = ArcadeMachine.filePath;
        	//Load network configuration from disk:
            MultiLayerConfiguration confFromJson = null;
			try 
			{
				confFromJson = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(path+"/conf.json")));
			} 
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}

            //Load parameters from disk:
            INDArray newParams = null;
            try(DataInputStream dis = new DataInputStream(new FileInputStream(path+"/coefficients.bin")))
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
            try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path+"/updater.bin")))
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
        }
		
	}
	
}
