package controllers.ScreenCaptureAgent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

public class ToTestThing {

	public static void main(String[] args) {
		
		Random rng = new Random(); // Ideally just create one instance globally
		// Note: use LinkedHashSet to maintain insertion order
		Set<Integer> generated = new LinkedHashSet<Integer>();
		
		while (generated.size() < 100)
		{
		    Integer next = rng.nextInt(100) + 1;
		    // As we're adding to a set, this will automatically do a containment check
		    generated.add(next);
		}
		
		boolean[] check = new boolean[100];
		
		for (Iterator<Integer> it = generated.iterator(); it.hasNext(); ) {
	        int f = it.next();
	            check[f-1] = true;
	    }
		
		for(int i=0;i<check.length;i++)
			System.out.println(check[i]);
	}

}
