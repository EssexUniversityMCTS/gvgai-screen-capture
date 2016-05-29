package controllers.ScreenCaptureAgent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class ExperienceCount {
/*
	Comparator<Experience> comparator = new ExperienceComparator();
	
	public static PriorityQueue<Experience> priorityQueue;
	public static HashMap<Experience, Integer> mapToIndex;
	public ExperienceCount()
	{
		mapToIndex = new HashMap<>();
		priorityQueue = new PriorityQueue<Experience>(Agent.poolSize,comparator);
	}
	
	public Experience equiExp(Experience exp)
	{
		if(priorityQueue.isEmpty())
			return null;
		PriorityQueue<Experience> pqCopy = new PriorityQueue<Experience>(priorityQueue.size(),comparator);
		pqCopy.addAll(priorityQueue);
		while(!pqCopy.isEmpty())
		{
			Experience exp2 = pqCopy.poll();
			if(QLearning.ImageEquals(exp.getPrevious(), exp2.getPrevious()));
				return exp2;
		}
		
		return null;
	}
	
	public void increase(int i)
	{
		//System.out.println("before: "+Agent.experiencePool[i].accessCount);
		Agent.experiencePool[i].accessCount +=1;
		
		Experience equiExp = equiExp(Agent.experiencePool[i]);
		
		System.out.println(equiExp+" "+priorityQueue.contains(equiExp));
		
		if(equiExp!=null)
		{
			priorityQueue.remove(equiExp);
			System.out.println("trueeee");
		}
		else
		{
			mapToIndex.put(Agent.experiencePool[i], i);
		}
		
		//Agent.experiencePool[i].accessCount = Agent.experiencePool[i].accessCount + 1;
		//System.out.println("after: "+Agent.experiencePool[i].accessCount);
		priorityQueue.add(Agent.experiencePool[i]);
		//System.out.println("add "+i+" "+Agent.experiencePool[i].accessCount);
	
	}
	
	public static int getMinAccessNotRemove()
	{
		
		return mapToIndex.get(priorityQueue.peek());
	}
	
	public int getMinAccessAndRemove()
	{
		mapToIndex.remove(priorityQueue.peek());
		return mapToIndex.get(priorityQueue.poll());
	}
	public void print()
	{
		if(priorityQueue.isEmpty())
			return;
		PriorityQueue<Experience> pqCopy = new PriorityQueue<Experience>(priorityQueue.size(),comparator);
		pqCopy.addAll(priorityQueue);
		while(!pqCopy.isEmpty())
		{
			Experience exp = pqCopy.poll();
			System.out.println(priorityQueue.size()+" "+QLearning.pool.size()+" "+mapToIndex.get(exp)+" "+exp.accessCount);//+" "
								
		}
	}
}

class ExperienceComparator implements Comparator<Experience>
{

	@Override
	public int compare(Experience exp1, Experience exp2) {
		// TODO Auto-generated method stub
		//System.out.println(exp1.accessCount+" "+exp2.accessCount);
		
		if(exp1.accessCount > exp2.accessCount)
			return 1;
		else return -1;
		
		
	}

	
	
	*/
	
}
