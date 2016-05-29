package controllers.ScreenCaptureAgent;

import ontology.Types;

public class Experience {
	
	private double[][] previous;
	private Types.ACTIONS action;
	private double[][] result;
	private double reward;
//	public int accessCount;
	
	public Experience()
	{
//		accessCount = 1;
	}
	
	public Experience(double[][] p, Types.ACTIONS a, double[][] r, double re)//,int count)
	{
		previous = p;
		action = a;
		result = r;
		reward = re;
	///	accessCount = 1;
	}
	
	
	public void setPrevious(double[][] previous)
	{
		this.previous = previous;
	}
	
	public void setAction(Types.ACTIONS action)
	{
		this.action = action;
	}
	
	public void setReward(double reward)
	{
		this.reward = reward;
	}
	
	public void setResult(double[][] result)
	{
		this.result = result;
	}
	
	public double[][] getPrevious()
	{
		return previous;
	}
	
	public Types.ACTIONS getAction()
	{
		return action;
	}
	
	public double getReward()
	{
		return reward;
	}
	
	public double[][] getResult()
	{
		return result;
	}
	
	public Experience copy()
	{
		Experience newExp = new Experience(previous,action,result,reward);//,accessCount);
		//newExp.accessCount = accessCount;
		
	//	System.out.println("count "+newExp.accessCount+" "+this.accessCount);
		
		return newExp;
	}
	

}
