

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


public class homework {
	static int queryNum;
	static int sentenceNum;
	static ArrayList<String> queryArray = new ArrayList<>();
	static HashMap<String,List<String[]>> sentenceMap = new HashMap<>();
	static HashMap<String,List<String[]>> newSentenceMap = new HashMap<>();
	static HashSet<String> sentenceSet = new HashSet<>();
	static HashSet<String> compareSet = new HashSet<>();
	static HashSet<String> newSentenceSet = new HashSet<>();
	static ArrayList<Boolean> result;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readFile();
	}
	public static void readFile(){
		BufferedReader br = null;
		FileReader fr = null;
		try {
			fr = new FileReader("input.txt");
			br = new BufferedReader(fr);
			// get query number and add query to hash set.
			queryNum = Integer.parseInt(br.readLine());
			result = new ArrayList<Boolean>();
			for(int i=0;i<queryNum;i++){
				String query = br.readLine();
				queryArray.add(query);
			}
			// get sentence number and add it to hash set.
			sentenceNum = Integer.parseInt(br.readLine());
			for(int i=0;i<sentenceNum;i++){
				String sentence = br.readLine();
				addToMap(sentence);	
			}
			//printMap(sentenceMap);
			while(!sentenceSet.isEmpty()) {
				Iterator<String> iterator = sentenceSet.iterator();
				while(iterator.hasNext()){
					String s = iterator.next();
					refreshKB(s);
					sentenceMap = new HashMap<>(newSentenceMap);
					//printMap(sentenceMap);
				}
				sentenceSet = new HashSet<>(newSentenceSet);
				newSentenceSet = new HashSet<>();

			}
			getResult();
			//printSet(compareSet);
			//printMap(sentenceMap);
			//System.out.println(result);
			printToFile(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void printSet(HashSet<String> set){
		Iterator<String> iter = set.iterator();
		while(iter.hasNext()){
			System.out.println(iter.next());
		}
	}
	public static void getResult(){
		for(int i=0;i<queryArray.size();i++){
			String s = queryArray.get(i);
//			if(compareSet.contains(s)){
//				result.add(true);
//			}
//			else{
//				result.add(false);
//			}
			// another check
			Iterator<String> iter = compareSet.iterator();
			boolean res =false;
			while(iter.hasNext()){
				String idle = iter.next();
				String newIdle = idle.split("\\(")[0];
				String newS = s.split("\\(")[0];
				if(newIdle.equals(newS)){
					res = anotherCheck(s,idle);
					if(res){
						result.add(true);
						break;
					}
				}
			}
			if(!res)
			result.add(false);
		}
	}
	public static boolean anotherCheck(String s, String idle){
		s = s.substring(s.indexOf("(")+1);
		s = s.substring(0,s.indexOf(")"));
		String[] a = s.split(",");
		//
		idle = idle.substring(idle.indexOf("(")+1);
		idle = idle.substring(0,idle.indexOf(")"));
		String[] b = idle.split(",");
		if(a.length!=b.length){return false;}
		for(int i=0;i<a.length;i++){
			if(a[i].equals(b[i])|| Character.isLowerCase(b[i].charAt(0))){
				continue;
			}
			return false;
		}
		return true;
	}
	public static void addToMap(String sentence){
		if(sentence.contains("|")){
			String[] array = sentence.split(" \\| ");
			for(int i=0;i<array.length;i++){
				List<String[]> parentList = new ArrayList<>();
				if(sentenceMap.containsKey(array[i])){
					parentList = sentenceMap.get(array[i]);
				}
				String[] list = new String[array.length-1];
				int index=0;
				for(int j=0;j<array.length;j++){
					if(j!=i){list[index++] = array[j];}
				}
				Arrays.sort(list);
				boolean recordBefore = false;
				for(int j=0;j<parentList.size();j++){
					if(isSame(parentList.get(j),list)){recordBefore = true;break;}
				}
				if(!recordBefore){
					parentList.add(list);
					sentenceMap.put(array[i],parentList);
				}
			}
		}
		else{
			// add the predicate to set directly
			sentenceSet.add(sentence);
			compareSet.add(sentence);
		}
	};
	//return true is two String array is the same.
	public static boolean isSame(String[] a,String[] b){
		if(a.length!=b.length){return false;}
		for(int i=0;i<a.length;i++){
			if(!a[i].equals(b[i])){return false;}
		}
		return true;
	}
	public static void refreshKB(String sentence){
		// get term
		newSentenceMap = new HashMap<>(sentenceMap);
		Iterator<String> it = sentenceMap.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			String newKey = key.split("\\(")[0];
			String check = sentence.split("\\(")[0];
			check = negate(check);
			if(newKey.equals(check)){
				refreshSentence(sentence,key);
			}
		}
	}
	public static void refreshSentence(String sentence,String key){
		String originalKey = key;
		HashMap<String,String> map = new HashMap<>();
		sentence = sentence.substring(sentence.indexOf("(")+1);
		sentence = sentence.substring(0,sentence.indexOf(")"));
		String[] a = sentence.split(",");
		
		key = key.substring(key.indexOf("(")+1);
		key = key.substring(0,key.indexOf(")"));
		String[] b = key.split(",");
		//change all b to a 
		for(int i=0;i<a.length;i++){
			if(Character.isUpperCase(b[i].charAt(0)) && Character.isLowerCase(a[i].charAt(0))){
				continue;
			}
			if(Character.isUpperCase(b[i].charAt(0)) && !b[i].equals(a[i])){
				return;
			}
			map.put(b[i],a[i]);
		}
		List<String[]> list = new ArrayList<String[]>(newSentenceMap.get(originalKey));
		for(int i=0;i<list.size();i++){
			String[] origin = list.get(i);
			String[] array = new String[origin.length];
			for(int j=0;j<origin.length;j++){
				array[j]=origin[j];
			}
			changeVariable(array,map);
		}
	}
	public static void changeVariable(String[] array, HashMap<String,String> map){
		for(int i=0;i<array.length;i++){
			String s = array[i];
			String functionName = s.substring(0,s.indexOf("("));
			int test = s.indexOf("(");
			String temp = s.substring(s.indexOf("(")+1);
			temp = temp.substring(0,temp.indexOf(")"));
			String[] beforeChange = temp.split(",");
			String result = functionName + "(";
			for(int j=0;j<beforeChange.length;j++){
				if(map.containsKey(beforeChange[j])){
					result = result + map.get(beforeChange[j]) + ",";
				}
				else{
					result = result + beforeChange[j] + ","; 
				}
			}
			result = result.substring(0,result.length()-1);
			result = result + ")";
			array[i] = result;
		}
		addNewSentence(array);
	}
	public static void addNewSentence(String[] array){
		if(array.length==1){
			if(compareSet.contains(array[0])){return;}
			newSentenceSet.add(array[0]);
			compareSet.add(array[0]);
			return;
		}
		for(int i = 0;i<array.length;i++){
			List<String[]> parentList = new ArrayList<>();
			if(newSentenceMap.containsKey(array[i])){
				parentList = newSentenceMap.get(array[i]);
			}
			String[] list = new String[array.length-1];
			int index = 0;
			for(int j=0;j<array.length;j++){
				if(j==i){continue;}
				list[index++] = array[j];
			}
			parentList.add(list);
			newSentenceMap.put(array[i],parentList);
		}
	}     
	// negate the term
	public static String negate(String s){
		if(s.charAt(0)=='~'){
			return s.substring(1);
		}
		return "~" + s;
	}
	public static void printMap(HashMap<String,List<String[]>> map){
		Iterator<String> iter = map.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			List<String[]> list = map.get(key);
			for(int i=0;i<list.size();i++){
				String[] array = list.get(i);
				System.out.print(key);
				for(int j=0;j<array.length;j++){
					System.out.print(array[j]);
				}
				System.out.println(";");
			}
		}
		System.out.println("***************");
	}
	public static void printToFile(ArrayList<Boolean> result) {
		int length = result.size();
		try {
			PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
			for(int i =0;i<length;i++){
				boolean flag = result.get(i);
				if(flag){
					writer.println("TRUE");
				}
				else{
					writer.println("FALSE");
				}
			}
			writer.close();
			//System.out.println("finish");
		} catch (IOException e) {
			System.out.println("fail when write into the file");
		}
	}
}
