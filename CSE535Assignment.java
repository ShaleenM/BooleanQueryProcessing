import java.text.Collator;
import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import java.lang.*;

public class CSE535Assignment {
	
	static LinkedList<PostingNode> posting_list,posting_list1 ;
	static Map<String, LinkedList> dictionary = new HashMap<>();
	static Map<String, LinkedList> dictionary1 = new HashMap<>();
	static PrintWriter writer;
	
	public static void main(String[] args) throws IOException {

		writer = new PrintWriter(args[1]);
		File temp = new File(args[0]);
		Scanner file = new Scanner(temp);
		
		while(file.hasNextLine())
		{
			posting_list = new LinkedList<PostingNode>();
			posting_list1 = new LinkedList<PostingNode>();
			String[] entry = file.nextLine().split("\\\\",2);
			String term = entry[0];
			String rest = entry[1].substring(1);
			entry = rest.split("\\\\",2);
			int 	 = Integer.parseInt(entry[0]);
			rest = entry[1].substring(2,entry[1].length()-1).trim();
			String[] posting = rest.split(",");
			
			for(String q : posting)
			{
				PostingNode tempNode = new PostingNode();
				tempNode.docID = Long.parseLong(q.split("/")[0].trim());
				tempNode.docFreq = Integer.parseInt(q.split("/")[1].trim());
				posting_list.add(tempNode);
			}
			for(String q : posting)
			{
				PostingNode tempNode = new PostingNode();
				tempNode.docID = Long.parseLong(q.split("/")[0].trim());
				tempNode.docFreq = Integer.parseInt(q.split("/")[1].trim());
				posting_list1.add(tempNode);
			}
			
			dictionary.put(term, posting_list);
			dictionary1.put(term, posting_list1);
		}
		file.close();
		//Sort Posting List by Doc ID
				for(String key : dictionary.keySet())
				{
					LinkedList<PostingNode> posting_list = dictionary.get(key);
					Collections.sort(posting_list,new Comparator<PostingNode>()
							{
								@Override
								public int compare(PostingNode o1, PostingNode o2) {
									return o1.docID < o2.docID ? -1 : o1.docFreq == o2.docFreq ? 0 : 1;
								}
								
							});
					dictionary.put(key, posting_list);
				}
				
		//Sort Posting List by Doc frequency
		for(String key : dictionary1.keySet())
		{
			LinkedList<PostingNode> posting_list1 = dictionary1.get(key);
			Collections.sort(posting_list1,new Comparator<PostingNode>()
					{
						@Override
						public int compare(PostingNode o1, PostingNode o2) {
							return o1.docFreq > o2.docFreq ? -1 : o1.docFreq == o2.docFreq ? 0 : 1;
						}
						
					});
			dictionary1.put(key, posting_list1);
		}
		getTopK(Integer.parseInt(args[2]));
		writer.println();
		File temp1 = new File(args[3]);
		Scanner Input_file = new Scanner(temp1);
		while(Input_file.hasNext()){
			String[] terms = Input_file.nextLine().trim().split("\\s+");
			for(int f=0;f<terms.length;f++)
				getPostings(terms[f]);
			termAtATimeQueryAnd(terms);
			termAtATimeQueryOr(terms);
			docAtATimeQueryAnd(terms);
			docAtATimeQueryOr(terms);
		}
		Input_file.close();
		writer.close();
	}
	
	public static void getTopK(int k)
	{
		writer.println("FUNCTION: getTopK  "+k);
		Map<String, Integer> m = new TreeMap<>();
		for(String key : dictionary.keySet())
		{
			m.put(key, dictionary.get(key).size());
		}
		List<Map.Entry<String,Integer>> list = new ArrayList<Map.Entry<String,Integer>>(m.entrySet());
		
		//comparator compre = new comparator();
		//Collections.sort(list, compre);
		Collections.sort(list,new Comparator<Map.Entry<String, Integer>>(){
			public int compare(Map.Entry<String, Integer> o1,Map.Entry<String, Integer> o2) 
			{
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
		writer.print("Result : ");
		Iterator<Map.Entry<String, Integer>> it = list.iterator();
		for (int p=0; p<k && it.hasNext();p++) {
			Map.Entry<String, Integer> entry = it.next();
			writer.print(entry.getKey()+", ");
		}
	}
	
	public static void getPostings(String query_term)
	{
		writer.println("FUNCTION: getPostings "+query_term);
			if (dictionary.containsKey(query_term))
			{
				writer.print("Ordered by doc IDs: ");
				LinkedList<PostingNode> temp = dictionary.get(query_term);
				for (int i = 0; i < temp.size(); i++) {
		            writer.print(temp.get(i).docID+", ");
		        }
			}
			else
				writer.print("Term not found");
			writer.println();
			if (dictionary1.containsKey(query_term))
			{
				writer.print("Ordered by TF: ");
				LinkedList<PostingNode> temp2 = dictionary1.get(query_term);
				for (int j = 0; j < temp2.size(); j++) {
		            writer.print(temp2.get(j).docID+", ");
		        }
			}
			else
				writer.print("Term not found");
			
		writer.println();
	}

	public static void termAtATimeQueryAnd(String[] query)							//DONE
	{
		writer.print("FUNCTION: termAtATimeQueryAnd: ");
		for(int i=0;i<query.length;i++){writer.print(query[i]+ ", ");}
		//Checking for terms not in index.If any term not in index, the function returns ;
		for(int i=0;i<query.length;i++){
			if (!dictionary.containsKey(query[i])){
				writer.println();writer.println("Term not found");return;}}
		writer.println();	
		//Sorting Query for optimal number of comparisons.
		Map<String, Integer> m = new TreeMap<>();
		for(int i=0;i<query.length;i++)
			m.put(query[i], dictionary.get(query[i]).size());
		List<Map.Entry<String,Integer>> list = new ArrayList<Map.Entry<String,Integer>>(m.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>()
				{
					@Override
					public int compare(Entry<String, Integer> o1,Entry<String, Integer> o2) {
						return o1.getValue() < o2.getValue() ? -1 : o1.getValue() == o2.getValue() ? 0 : 1;
					}
				});
		//Created two arrays. One with Query terms as Input. Other with query term in increasing order of size of postings 
		int p= 0;
		String[] query1 = new String[query.length];
		Iterator<Map.Entry<String, Integer>> it = list.iterator();
		while(it.hasNext()) {
			Map.Entry<String, Integer> entry = it.next();
			query1[p] = entry.getKey();
			p=p+1;
		}
		
		//Query Processing in Input sequence
		int comparisions = 0;
		PostingNode temp = new PostingNode();
		LinkedList<PostingNode> result = new LinkedList<>();
		LinkedList<PostingNode> postingk = new LinkedList<>();
		LinkedList<PostingNode> postingk1 = new LinkedList<>();
		//Copy first posting list in a LinkedLIst.
		postingk1 = dictionary1.get(query[0]);
		
		float startTime = System.currentTimeMillis();
		//For Unsorted query
		for(int i=1;i<query.length;i++)
		{
			result = new LinkedList<PostingNode>();
			postingk = dictionary1.get(query[i]);
			//Compare First posting list with second in first iteration.
			//In every subsequent iteration compares, next posting list with result of previous comparision.
			for(int a= 0; a<postingk1.size();a++)
			{
				for(int b= 0; b<postingk.size();b++)
				{
					comparisions= comparisions+1;
					if(postingk1.get(a).docID == postingk.get(b).docID)
					{
						//Save the DocID in result list if it's present in both postings.
						temp = new PostingNode();
						temp.docID = postingk.get(b).docID;
	            		temp.docFreq = 1;
	            		result.add(temp);
	        		}
				}
			}
			//Storing result to postingk1 for next iteration
			postingk1 = new LinkedList<PostingNode>();
			postingk1 = result;
		}
		float stopTime = System.currentTimeMillis();
	    float elapsedTime = stopTime - startTime;
		
		//Comparison in optimum manner(Same as previous case)
	    //But with the sorted array of terms based on posting size.
		int comparisions1 = 0;
		PostingNode temp1 = new PostingNode();
		LinkedList<PostingNode> result1 = new LinkedList<>();
		LinkedList<PostingNode> postingk2 = new LinkedList<>();
		LinkedList<PostingNode> postingk3 = new LinkedList<>();
		postingk3 = dictionary1.get(query1[0]);
		for(int i=1;i<query1.length;i++)
		{
			result1 = new LinkedList<PostingNode>();
			postingk2 = dictionary1.get(query1[i]);
			for(int a= 0; a<postingk3.size();a++)
			{
				for(int b= 0; b<postingk2.size();b++)
				{
					comparisions1= comparisions1+1;
					if(postingk3.get(a).docID == postingk2.get(b).docID)
					{
						temp1 = new PostingNode();
						temp1.docID = postingk2.get(b).docID;
	            		temp1.docFreq = 1;
	            		result1.add(temp);
	        		}
				}
			}
			postingk3 = new LinkedList<PostingNode>();
			postingk3 = result1;
		}
		
		writer.println(postingk1.size()+" documents are found");
		writer.println(comparisions+" comparisions are made");
		writer.println(comparisions1+" comparisons are made with optimization");
		writer.println(elapsedTime/1000+" Seconds are used");
		writer.print("Result: ");
		//Display DocIDs from result of input order comparision.
		for(int c =0; c<postingk1.size();c++){
			writer.print(postingk1.get(c).docID+", ");}
	}

	public static void termAtATimeQueryOr(String[] queryi)							//DONE
	{
		/*
		Algorithm: 
		1. Take 1st posting list and add it to result.
		2. take next list and compare each elemetn of second list with each element of result. 
		3. If the docID in second list is laready in result do not add it, elase add it.
		4. Repeat the same for third posing lis and so on.
		*/
		writer.println(); 
		writer.print("FUNCTION: termAtATimeQueryOr: ");
		//Copy the input array of terms in another array for processing in this function
		String[] query2 = new String[queryi.length];
		for(int i=0;i<queryi.length;i++)
			query2[i]=queryi[i];
		
		//If any term in query string does not exist in the dictionary. Change it to Null
		int notFound=0;
		for(int i=0;i<query2.length;i++){
			writer.print(query2[i]+ ", ");
			if (!dictionary.containsKey(query2[i])){
				query2[i]=null;notFound=notFound+1;}}
		if (notFound==query2.length){
			writer.print("Term Not Found");
			return;}
			
		//Store all the not null terms(terms that exist in dictionary) in different array
		//We will use this array for rest of the processing in this function.
		String query[] = new String[query2.length-notFound];
		int j=0,k=0;
		while(!(j==query2.length)){
			if (!(query2[j]==null)){
				query[k]=query2[j];k=k+1;}
				j=j+1;}
		
		//Sorting Query for optimal number of comparisons.
			Map<String, Integer> m = new TreeMap<>();
			for(int i=0;i<query.length;i++)
				m.put(query[i], dictionary.get(query[i]).size());
			List<Map.Entry<String,Integer>> list = new ArrayList<Map.Entry<String,Integer>>(m.entrySet());
			Collections.sort(list, new Comparator<Map.Entry<String, Integer>>()
					{@Override
						public int compare(Entry<String, Integer> o1,Entry<String, Integer> o2) {
							return o1.getValue() < o2.getValue() ? -1 : o1.getValue() == o2.getValue() ? 0 : 1;
						}
					});
			int p= 0;
			String[] query1 = new String[query.length];
			Iterator<Map.Entry<String, Integer>> it = list.iterator();
			while(it.hasNext()) {
				Map.Entry<String, Integer> entry = it.next();
				query1[p] = entry.getKey();
				p=p+1;}
			
		
		float startTime = System.currentTimeMillis();
		int flag =1;
		int comparisions=0;
		LinkedList<PostingNode> postingk = new LinkedList<>();
		LinkedList<PostingNode> result = new LinkedList<>();
  		
		//Using query term in given order, processing AND operation.
		result.addAll(dictionary1.get(query[0]));
		for(int i=1;i<query.length;i++)
		{
			postingk.addAll(dictionary1.get(query[i]));
			//For every iteration I am checking in the result and next posting list.
			//Adding the element to result only if the element is not already present in the result.
			for(int a=0; a<postingk.size();a++)
			{
				flag =1;
				for(int b =0; b<result.size(); b++)
				{
					comparisions = comparisions +1;
					if(postingk.get(a).docID==result.get(b).docID)
						flag = 0;
				}
				if(flag==1)
					result.add(postingk.get(a));
			}
			
		}
		float stopTime = System.currentTimeMillis();
	    float elapsedTime = stopTime - startTime;
	   
	    //Optimal Order AND (same as input order AND )
	    int flag1 =1;
		int comparisions1=0;
		LinkedList<PostingNode> postingk1 = new LinkedList<>();
		LinkedList<PostingNode> result1 = new LinkedList<>();
		result1.addAll(dictionary1.get(query[0]));
		for(int i=1;i<query1.length;i++)
		{
			postingk1.addAll(dictionary1.get(query1[i]));
			
			for(int a=0; a<postingk1.size();a++)
			{
				flag1 =1;
				for(int b =0; b<result1.size(); b++)
				{
					comparisions1 = comparisions1 +1;
					if(postingk1.get(a).docID==result1.get(b).docID)
						flag = 0;
				}
				if(flag==1)
					result1.add(postingk1.get(a));
			}
			
		}
		writer.println();
		writer.println(result.size()+ " documents are found");
		writer.println(comparisions+ " comparisions are made");
		writer.println(elapsedTime/1000+" Seconds are used");
		writer.println(comparisions1+ " comparisons are made with optimization");
		writer.print("Result: ");
		//Printing DocIds from result.
		for(int c=0;c< result.size(); c++)
			writer.print(result.get(c).docID+", ");
	}

	public static void docAtATimeQueryAnd(String query[])
	{
		/*
		Algorithm: 
		1. Put a pointer at First DocId of every list. In every iteration we will be comparing the DocId's with pointers only.
		2. Check if all the DOC IDs are same.
		3. If yes than add the DOC ID to the result. and increment all the pointers.
		4. Else : Find Max of the DocId in every iteration.
		3. Increment all pointers by 1 with DOC ID less that the Max.
		4. Repeate the same any list gets exhausted. Leave the function as soon as a list gets exhausted.
		*/
		writer.println();
		writer.print("FUNCTION: docAtATimeQueryAnd: ");
		//Checking if there is any term which does not exist in index.
		//If yes , Display Term not found and return from function 
		for(int i=0;i<query.length;i++){
			writer.print(query[i]+ ", ");
			if (!dictionary.containsKey(query[i])){
				writer.println();writer.println("Term not found");return;}}
		
		int comparisions=0;
		long tempMax = 0;
		boolean shouldEnd = false;
		PostingNode tempNode = new PostingNode();
		LinkedList<PostingNode> temp = new LinkedList<>();
		LinkedList<PostingNode> result = new LinkedList<>();
		//created an integer array for maintaining pointers.
		int[] pointer = new int[query.length];
		
		for(int i=0;i<query.length;i++)
			pointer[i] = 0;
		float startTime = System.currentTimeMillis();
		//Will be comparing one docID from each posting list at a time.
		while(shouldEnd == false)
		{
			//created an array of docIDs which are in current comparison.
			long[] docs1 = new long[query.length];
			//fetching DocIDs from index hash map.
			for(int i=0;i<docs1.length;i++){
				temp = dictionary.get(query[i]);
				docs1[i] = temp.get(pointer[i]).docID;
			}
			boolean flag = true;
			//Check if all docIDs in current comparision are same.
			for (int i=1;i<docs1.length;i++)
			{
				if(docs1[i]!=docs1[0])
					flag= false;
			}
			//if flage true, implies all docIDs in current comparision are same
			//Add DocID to result and increment all pointers.
			if(flag==true)
			{
				tempNode = new PostingNode();
				tempNode.docID = docs1[0];
				tempNode.docFreq = 0;
        		result.add(tempNode);
        		for(int i=0;i<pointer.length;i++)
        			pointer[i]=pointer[i]+1;
			}
			else//Increment only the pointers which are less than the max of DocId in comparison list
			{
				for(int i=0;i<docs1.length;i++)
					tempMax = Math.max(docs1[i], tempMax);
				
				for(int i=0;i<docs1.length;i++)
				{
					comparisions = comparisions +1;
					if(docs1[i]<tempMax)
						pointer[i] =pointer[i] +1;
				}
			}
			//After ever iteration check if we have exhausted any list. IF yes. finish iterations
			//Doing same using shouldEnd flag.
			for(int i=0;i<query.length;i++)
			{
				if(dictionary.get(query[i]).size()==pointer[i])
					shouldEnd = true;
			}
		}
		float stopTime = System.currentTimeMillis();
	    float elapsedTime = stopTime - startTime;
	    
		writer.println();
		writer.println(result.size()+ " documents are found");
		writer.println(comparisions+ " comparisions are made");
		writer.println(elapsedTime/1000+" Seconds are used");
		writer.print("Result: ");
		for(int c=0;c< result.size(); c++)
			writer.print(result.get(c).docID+", ");
	}

	public static void docAtATimeQueryOr(String query2[])
	{
		/*
		Algorithm: 
		1. Put a pointer at First DocId of every list. In every iteration we will be comparing the DocId's with pointers only.
		2. FInd Min of the DocId in every iteration.
		3. Add the Min docID to the result and increment the pointer in the list with min DOC ID by 1.
		4. Repeate the same till every list gets exhausted.
		*/
		writer.println();
		writer.print("FUNCTION: docAtATimeQueryOr: ");
		//Checking if there is any term that does not exist in the index.
		//If so making the term = null in the copy of query.
		int notFound=0;
		for(int i=0;i<query2.length;i++){
			writer.print(query2[i]+ ", ");
			if (!dictionary.containsKey(query2[i])){
				query2[i]=null;notFound=notFound+1;}}
		if (notFound==query2.length){
			writer.print("Term Not Found");
			return;}
		//Created a new array with only the not null terms(that exist in the index)	
		String query[] = new String[query2.length-notFound];
		int j=0,k=0;
		while(!(j==query2.length)){
			if (!(query2[j]==null)){
				query[k]=query2[j];k=k+1;}
				j=j+1;}
		//As all the DOCIDs are 7 digit number I am taking a number 9999999 which will be greater than all the docIDs
		int comparisions =0;
		long tempMin = 9999999;
		//This flag will track if every posting list has reached end of their size or not
		boolean[] atEnd = new boolean[query.length];
		for(int i=0;i<query.length;i++)
			atEnd[i] = false;
		boolean shouldEnd =false;
		PostingNode tempNode = new PostingNode();
		LinkedList<PostingNode> temp = new LinkedList<>();
		LinkedList<PostingNode> result = new LinkedList<>();
		
		//Defined array of pointers.
		int[] pointer = new int[query.length];
		for(int i=0;i<query.length;i++)
			pointer[i] = 0;
		long startTime = System.currentTimeMillis();
		while(shouldEnd == false)
		{
			
			//created an array of docIDs which are in current comparison.
			//If any list gets exhausted, then the corresponding docID for that posting list in subsequent iterations will be a high value 9999999
			long[] docs1 = new long[query.length];
			for(int i=0;i<docs1.length;i++){
				if(dictionary.get(query[i]).size()<=pointer[i]){
					docs1[i]=9999999;
					atEnd[i]=true;
				}	
				else{
					temp = dictionary.get(query[i]);
					docs1[i] = temp.get(pointer[i]).docID;
				}	
			}
			
			shouldEnd=true;
			for(int i=0;i<query.length;i++)
			{
				if(atEnd[i]==false)
					shouldEnd = false;
			}
			
			boolean flag = true;
			for (int i=1;i<docs1.length;i++)
			{
				comparisions = comparisions+1;
				if(docs1[i]!=docs1[0])
					flag= false;
			}
			//if flag true implies all docIDs are same 
			//So add to result and increment all pointers.
			//This step is redundant but kept this to keep it symmetric to DAAT AND
			if(flag==true  && shouldEnd==false)
			{
				tempNode = new PostingNode();
				tempNode.docID = docs1[0];
				tempNode.docFreq = 0;
        		result.add(tempNode);
        		for(int i=0;i<pointer.length;i++)
        			pointer[i]=pointer[i]+1;
			}
			else//In every iteration I am adding only the min docID from the docIDs in current comparision
			{
				tempMin = 9999999;
				comparisions = comparisions +docs1.length;
				for(int i=0;i<docs1.length;i++)
					tempMin = Math.min(docs1[i], tempMin);
				if(tempMin!=9999999)
				{
					tempNode = new PostingNode();
					tempNode.docID = tempMin;
					tempNode.docFreq = 0;
	        		result.add(tempNode);
	        		//Increasing pointer of only the term with Min DocID in this iteration.
					for(int i=0;i<docs1.length;i++)
					{
						if(docs1[i]==tempMin)
							pointer[i] =pointer[i] +1;
					}
				}
			}
			
		}
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    writer.println();
		writer.println(result.size()+ " documents are found");
		writer.println(comparisions+ " comparisions are made");
		writer.println(elapsedTime/1000+" Seconds are used");
		writer.print("Result: ");
		for(int c=0;c< result.size(); c++)
			writer.print(result.get(c).docID+", ");
		writer.println();
	}
}

