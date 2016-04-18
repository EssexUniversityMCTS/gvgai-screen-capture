package com.depies.dl4j_tutorial;

import java.util.Random;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.NDArray;

public class toTestStuff {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Random random = new Random();
		int blockW = 25;
		int genSize = 10;
		double[] greyND = App.readToDouble("pic/grey.png",blockW);
        double[] blackBoxND = App.readToDouble("pic/box.png",blockW);
       
        INDArray training0 = App.generate(greyND, genSize);
        INDArray training1 = App.generate(blackBoxND, genSize);
        
        INDArray shuffle = new NDArray(genSize*2,greyND.length);
        int[] label = new int[genSize*2];
        
      
        System.out.println("0:\n"+training0);
        
        System.out.println("1:\n"+training1);
		
	}

}
