package controllers.ScreenCaptureAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import ontology.Types;

public class QLearning {
	
	public double[][] qValues;
	//public HashMap<Experience, Integer> mapper;
	public double alpha = 0.1;
	public double gamma = 0.9;
	public double epsilon = 0.1;
	
	static ArrayList<double[][]> pool;
	static ArrayList<double[]> experienceReward;
	//Experience[] experiencePool;
	private Random random = new Random();
	
	public QLearning(int maxKept, int actionSize)
	{
		pool = new ArrayList();
		//experiencePool = exp;
		qValues = new double[maxKept][actionSize];
		experienceReward = new ArrayList();
	//	mapper = new HashMap<Experience, Integer>();
	}
	
	
	
	public double[] normalize(int index)
	{
		double[] norm = new double[qValues[index].length];
		double[] src = qValues[index];
		//System.arraycopy(qValues[index], 0, norm, 0, qValues[index].length);
		double max = -Double.MAX_VALUE;
		double min = Double.MAX_VALUE;
		int cMax = 1;
		for(int i=0;i<src.length;i++)
		{
			if(src[i]>max)
			{
				max = src[i];
				cMax = 1;
			}
			else if(src[i]==max)
			{
				cMax++;
			}
			
			if(src[i]<min)
			{
				min = src[i];
			}
			
			
	//		System.out.println(min+" "+max+" "+src[i]);
		}
		
	//	System.out.println(min+" "+max);
		if(max==min)
		{
			for(int i=0;i<norm.length;i++)
			{
				norm[i] = 1.0/(double)src.length;
			}
		}
		else
		for(int i=0;i<src.length;i++)
		{
			if(src[i]==max)
				norm[i] = 1.0/(double)cMax;
			
			else
				norm[i] = (src[i]-min)/(max-min);
			//System.out.println(norm[i]);
			//norm[i] = 1-norm[i];
		}
		
		
		return norm;
	}
	
	public void qUpdate(int curIndex, int nextIndex, int actionIndex,double reward)
	{
		//int index = mapper.get(experience);
		double max = 0;
		
		try
		{
			max = getMaxQValue(nextIndex);
		}
		catch(Exception e)
		{
			max = reward;
		}
		
		if(curIndex!=-1&&nextIndex!=-1&&ImageEquals(pool.get(curIndex),pool.get(nextIndex)))
		{
			qValues[curIndex][actionIndex] = -5;
			return;
		}
		
		
		double prevQ = qValues[curIndex][actionIndex];
		qValues[curIndex][actionIndex] = prevQ+alpha*(reward+gamma*max-prevQ);
		
		//if(reward == 100 || reward == -100)
			//System.out.println(qValues[curIndex][actionIndex]);
		//if(epsilon>0.01)
			//epsilon-=0.1;
	}
	
	public double getMaxQValue(int index)
	{
		int maxIndex = getMaxActionIndex(index);
		return qValues[index][maxIndex];
	}
	
	public int getMaxActionIndex(int index)
	{
		ArrayList<Integer> mIndex = new ArrayList();
		double r;
		int chosen;
		if((r = random.nextDouble())<epsilon)
		{
	//		System.out.println("random = "+(r<epsilon));
			return random.nextInt(qValues[0].length);
		}
		
		else
		{
			double[] actionV = qValues[index];
		//	System.out.println(actionV.length);
			double max = -Double.MAX_VALUE;
			
			for(int i=0;i<actionV.length;i++)
			{
				if(actionV[i]>max)
				{
					max = actionV[i];
					mIndex = new ArrayList();
					mIndex.add(i);
		//			System.out.println("add");
				}
				else if(actionV[i]==max)
				{
					mIndex.add(i);
				}
			}
			
						
		//	System.out.println(mIndex.size());
			chosen = mIndex.get(random.nextInt(mIndex.size()));
		//	System.out.println(index+" "+" "+chosen+" "+qValues[index][chosen]);
		//	System.out.println();
		//	for(int i=0;i<actionV.length;i++)
		//		System.out.println(actionV[i]+""+(chosen==i));
		//	System.out.println();
		}
	//	System.out.println("random = "+(r<epsilon));
		
		return chosen;
	}
	
	
	
	public int getMaxActionIndexFromScreenCap(double[][] image)
	{
		int i= findIndexFromImage(image);
		
		if(i==-1)
			return -1;
		else
		{
		//	System.out.println("xxx "+i);
			int index = getMaxActionIndex(i);
			
		//	System.out.println(index+" "+qValues[i][index]);
			return index;//getMaxActionIndex(i);
		}
	}
	
	public static int findIndexFromImage(double[][] image)
	{
		{
			boolean found = false;
			int i=0;
			try{
				
			
			for(i=0;i<pool.size();i++)//experiencePool.length;i++)
			{
				double[][] im = pool.get(i);//experiencePool[i].getPrevious();
				
				boolean pass = false;
				for(int j=0;j<im.length;j++)
					for(int k=0;k<im[0].length;k++)
					{
						
						if(pass)
							break;
						if(im[j][k]!=image[j][k])
							pass = true;
					}
				if(!pass)
					found = true;
				
				if(found)
					break;
			}
			}catch(Exception e){}
			if(!found)
				return -1;
			
			else {
		//		System.out.println("found "+i);
				return i;
			}
		}
	}
	
	public boolean ImageEquals(double[][] pix1, double[][] pix2)
	{
		
		boolean pass = false;
		for(int j=0;j<pix1.length&&!pass;j++)
			for(int k=0;k<pix1[0].length&&!pass;k++)
			{
				if(pix1[j][k]!=pix2[j][k])
					pass = true;
			}
		return !pass;	
	}

}
