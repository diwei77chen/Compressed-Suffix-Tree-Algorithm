import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

// Author: Diwei Chen
// Date Revised: 15 Jul 2015

public class CompressedSuffixTrie {	
	private char[] C;
	private Node root;
	
	/**	compressed suffix trie node implements the compact representation */
	class Node{
		public int firstCharIndex;
		public int lastCharIndex;
		public int originFirstIndex;		//marks the origin first char index
		public boolean isLeaf;
		public Node parent;
		ArrayList<Node> children;
		
		public Node(){
			isLeaf = true;
			children = new ArrayList<Node>();
		}
	}
	
	/**
	 * This class override the compare method of its Comparator parent class
	 * to compare the value of C by given index
	 */
	class indexCom implements Comparator<Node>{
		public int compare(Node n1, Node n2){
			if(C[n1.firstCharIndex] > C[n2.firstCharIndex]){
				return 1;
			}
			else if(C[n1.firstCharIndex] < C[n2.firstCharIndex]){
				return -1;
			}
			else{
				return 0;
			}
		}
	}
	
	/** Constructor 
	 * 	Creates a compressed suffix trie from file f*/
	public CompressedSuffixTrie( String f ) {
		root = new Node();
		C = readFile(f).toCharArray();
		for(int i = 0; i < C.length; ++i){
			addChildren(root, i, C.length - 1);
		}
	}
	
	/**
	 * This class methods builds the compact representation of the compressed suffix trie
	 */
	public void addChildren(Node parent, int firstCharIndex, int lastCharIndex){
		//adds the first suffix, e.g. index from first to last, to the children array list of root
		if(firstCharIndex == 0){
			Node child = new Node();
			child.firstCharIndex = firstCharIndex;
			child.originFirstIndex = child.firstCharIndex;
			child.lastCharIndex = lastCharIndex;
			child.parent = parent;
			parent.children.add(child);
			parent.isLeaf = false;
		}
		else if(firstCharIndex != 0){
			if(parent.children.size() != 0){
				int flag = 0;
			//searches common characters between the input suffix and the suffixes of the children array list
			for(int i = 0; i < parent.children.size(); ++i){
				Node child = parent.children.get(i);
				int lenChild = child.lastCharIndex - child.firstCharIndex + 1;
				int lenNew = lastCharIndex - firstCharIndex + 1;
				int len = Math.min(lenChild, lenNew);
				
				int commonChar = 0;
				int tmpFirst = firstCharIndex;
				int tmpChildFirst = child.firstCharIndex;
				
				//computes the number of common characters 
				for(; tmpFirst <= lastCharIndex; ++tmpFirst){
					if(tmpChildFirst <= child.lastCharIndex){
						if(C[tmpFirst] == C[tmpChildFirst]){
							++commonChar;
							++tmpChildFirst;
							flag = 1;		//flag = 1 indicates there are common characters
						}
						else{
							break;
						}
					}
					else{
						break;
					}
				}
				
				if(commonChar != 0){
					if(commonChar < len){
						//splits the external child node by reducing the index range
						if(child.isLeaf){		
							Node child1 = new Node();
							Node child2 = new Node();
							
							//node child is split as node child and node child1
							child1.firstCharIndex = child.firstCharIndex + commonChar;
							child1.originFirstIndex = child1.firstCharIndex;
							child1.lastCharIndex = child.lastCharIndex;
							child1.parent = child;
				
							//the input suffix is split as node child2
							child2.firstCharIndex = firstCharIndex + commonChar;
							child2.originFirstIndex = child2.firstCharIndex;
							child2.lastCharIndex = lastCharIndex;
							child2.parent = child;
						
							child.lastCharIndex = child.firstCharIndex + commonChar - 1;
				
							//node child is the parent of node child1 & node child2
							child.children.add(child1);
							child.children.add(child2);
							Collections.sort(child1.children, new indexCom());
							child.isLeaf = false;
						}
						else if(!child.isLeaf){		//splits internal child node
							Node child1 = new Node();
							Node child2 = new Node();
							
							//node child1 is inserted to the position of parent of node child
							child1.firstCharIndex = child.firstCharIndex;
							child1.originFirstIndex = child1.firstCharIndex;
							child1.lastCharIndex = child.firstCharIndex + commonChar - 1;
							child1.children.add(child);
							child1.parent = child.parent;
							child1.isLeaf = false;
							child1.children.add(child2);
							Collections.sort(child.children, new indexCom());
							
							//node child the son node of child1
							child.parent.children.remove(child);
							child.parent.children.add(child1);
							child.parent = child1;
							Collections.sort(child.parent.children, new indexCom());	
							child.firstCharIndex = child.firstCharIndex + commonChar;
							
							//the input suffix is split as node child2
							child2.firstCharIndex = firstCharIndex + commonChar;
							child2.originFirstIndex = child2.firstCharIndex;
							child2.lastCharIndex = lastCharIndex;
							child2.parent = child1;
						}
					}
					if(commonChar == len){
						if(child.isLeaf){
							if(lenNew < lenChild){
								//splits the leaf node into two parts
								//the first part is not the common suffix index stored in newNode as a child
								Node newNode = new Node();
								newNode.lastCharIndex = child.lastCharIndex;
								newNode.firstCharIndex = child.firstCharIndex + commonChar;
								newNode.originFirstIndex = newNode.firstCharIndex;
								newNode.parent = child;
								
								//the second part is the common suffix index stored in node child as a parent
								child.lastCharIndex = child.firstCharIndex + commonChar - 1;
								child.children.add(newNode);
								Collections.sort(child.children, new indexCom());
								child.isLeaf = false;
							}
							else if(lenChild < lenNew){
								//splits the leaf node into two parts
								//the first part is not the common suffix index stored in newNode as a child
								Node newNode = new Node();
								newNode.lastCharIndex = lastCharIndex;	//the last char index of new node is different
								newNode.firstCharIndex = child.firstCharIndex + commonChar;
								newNode.originFirstIndex = newNode.firstCharIndex;
								newNode.parent = child;
								
								//the second part is the common suffix index stored in node child as a parent
								child.lastCharIndex = child.firstCharIndex + commonChar - 1;
								child.children.add(newNode);
								Collections.sort(child.children, new indexCom());
								child.isLeaf = false;
							}
							
						}
						else if(!child.isLeaf){			//recursively creates new node until meets the leaf node
							firstCharIndex = firstCharIndex + commonChar;
							if(firstCharIndex <= lastCharIndex){
								addChildren(child, firstCharIndex, lastCharIndex);
							}
						}
					}
				break;
				}
			}
			
			//if there is no common character between the input suffix and the child
			//creates a new node stored the suffix as a child for the parent node
			if(flag == 0){
				Node newNode = new Node();
				newNode.firstCharIndex = firstCharIndex;
				newNode.originFirstIndex = newNode.firstCharIndex;
				newNode.lastCharIndex = lastCharIndex;
				newNode.parent = parent;
				parent.children.add(newNode);
				Collections.sort(parent.children, new indexCom());
			}
			}
			if(parent.children.size() == 0)
				return;
		}
		
	}

	/** Time complexity analysis:
	 * 	The time complexity of the findString(s) method is O(|s|)(|s| is the length of s).
	 *  Because this find string method is based on a built compact compressed suffix trie
	 *  and the characters of s are compared sequentially, at each time, only one character 
	 *  of s is compared and all the characters of it will be compared at most alphabet times
	 *  (here is only A, C, G, T, e.g. 4 times). Once a character is unmatched, the methods 
	 *  returns -1 immediately. Therefore, the time complexity is: O(4|s|), e.g, O(|s|).
	 */
	public int findString(String s){
		if (s == null || s.length() == 0)
			return -1;
		if (root == null) {
			return -1;
		} else {
			int i = findString(root, s);
			if(i == -1 || i == -2){
				return -1;
			}
			else{
				return i;
			}
		}
	}
	
	public int findString(Node parent, String s){
		if(s.length() != 1){
			for(int i = 0; i < parent.children.size(); ++i){
				Node child = parent.children.get(i);
				int lenChild = child.lastCharIndex - child.firstCharIndex + 1;
				int lenStr = s.length();

				int commonChar = 0;
				int tmpChildFirst = child.firstCharIndex;
				int breakFlag1 = 0;
				int breakFlag2 = 0;
				
				//if the length of string is less than or equals to that of the child
				//computes the common characters between s and the child
				if(lenStr <= lenChild){
					for(int j = 0; j < lenStr; j++){
						if(s.charAt(j) == C[tmpChildFirst]){
							++tmpChildFirst;
							++commonChar;
							continue;		//if a char is matched, compares the next one
						}
						else{
							breakFlag1 = 1;
							break;		//once finds unmatched, breaks and compares the next child
						}
					}
					if(commonChar == lenStr){
						return child.originFirstIndex;
					}
				}
				if(breakFlag1 == 1)
					continue;
				
				//if the length of s is greater than that of the child
				//computes the common characters between s and the child
				if(lenStr > lenChild){
					for(int j = 0; j < lenChild; j++){
						if(s.charAt(j) == C[tmpChildFirst]){
							++tmpChildFirst;
							++commonChar;
							continue;
						}
						else{
							breakFlag2 = 1;
							break;
						}
					}
					//if the number of common chars is equals to the length of the child
					//then compares to the children of the child
					if(commonChar == lenChild){
						//tags the original index of the first matched sequence
						int tag = findString(child, s.substring(commonChar)) - commonChar;
						if(tag >= child.originFirstIndex){
							return tag;
						}
					}
					if(breakFlag2 == 1)
						continue;
				}
			}
		}
		if(s.length() == 1){
			int flag = 0;
			if(parent.children.size() != 0){
				for(int i = 0; i < parent.children.size(); ++i){
					Node child = parent.children.get(i);
					if(s.charAt(0) == C[child.firstCharIndex]){
						flag = 1;
						return child.originFirstIndex;
					}
				}
			}
			if(flag == 0){		//there is no matched sequence between the child and s
				return -1;
			}	
		}
		return -2;
	}
	
	/**Time complexity analysis:
	 * The time complexity of the similarityAnalyser is O(mn), where m and n is 
	 * the size of f1 and f2.
	 * This methods makes use of dynamic programming to find length of the longest
	 * common suffix for all substrings and stores these lengths in a table. It totally
	 * consumes two for loops which is size m and size n to build up this table. 
	 * Therefore, the time complexity is O(mn).
	 */
	public static double similarityAnalyser(String f1, String f2, String f3){
		String strA = readFile(f1);
		String strB = readFile(f2);
		int lenA = strA.length();
		int lenB = strB.length();
		//creates a table to store lengths of longest common suffixes of substrings
		//arr[x][y] contains length of longest common suffix of strA[0..x-1] and 
		//strB[0..y-1]. The first row and first column entries have no logical meaning.
		int arr[][] = new int[lenA + 1][lenB + 1];
		
		//builds arr[lenA + 1][lenB + 1] in top down fashion
		for (int i = lenA - 1; i >= 0; i--){
            for (int j = lenB - 1; j >= 0; j--){
                if (strA.charAt(i) == strB.charAt(j))
                    arr[i][j] = arr[i + 1][j + 1] + 1;
                else 
                    arr[i][j] = Math.max(arr[i + 1][j], arr[i][j + 1]);
            }
        }
 
        int i = 0, j = 0;
        StringBuffer stringBuffer = new StringBuffer();
        //builds up the longest common subsequence from arr[][]
        while (i < lenA && j < lenB) {
            if (strA.charAt(i) == strB.charAt(j)){
                stringBuffer.append(strA.charAt(i));
                i++;
                j++;
            }
            else if (arr[i + 1][j] >= arr[i][j + 1]) 
                i++;
            else
                j++;
        }
        
        //computes the degree of similarity 
        String lcs = stringBuffer.toString();
        int lenLcs = lcs.length();
        double degree = -1;
        double maxAB = Math.max(lenA, lenB);
        degree = lenLcs / maxAB;
        //writes the degree to file3
        writeFile(degree, f3);		
        
        return degree;

	}
	
	/**	This class method reads the text of a given file and writes it to a string,
	 * 	but only character A, C, G, T can be written into it.
	 *  returns a char array of that string **/
	public static String readFile(String file){
		file = file + ".txt";
		String fileStr = "";
		String lineStr = "";
		char readChar;
		try{
			FileReader fr = new FileReader(file);		
			BufferedReader br = new BufferedReader(fr);
			while((lineStr = br.readLine()) != null){
				for(int i = 0; i < lineStr.length(); i++){
					readChar = lineStr.charAt(i);
					if(readChar == 'A' || readChar == 'C'
							|| readChar == 'G' || readChar == 'T'){
						fileStr = fileStr.concat(String.valueOf(readChar));
					}
				}
			}
			br.close();
			fr.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		return fileStr;
	}

	/** This class method writes the computed degree to a file
	 */
	public static void writeFile(double degree, String f3){
		f3 = f3 + ".txt";
		try{
			PrintWriter writer = new PrintWriter(f3, "UTF-8");
			writer.print(degree);		//prints the value of degree to f3
			writer.close();
		}
		catch(IOException e){
			
		}
	}
	
	 public static void main(String args[]) throws Exception{
	        
		 /** Construct a trie named trie1
		  */       
		 CompressedSuffixTrie trie1 = new CompressedSuffixTrie("file1");
		         
		 System.out.println("ACTTCGTAAG is at: " + trie1.findString("ACTTCGTAAG"));

		 System.out.println("AAAACAACTTCG is at: " + trie1.findString("AAAACAACTTCG"));
		         
		 System.out.println("ACTTCGTAAGGTT : " + trie1.findString("ACTTCGTAAGGTT"));
		         
		 System.out.println(CompressedSuffixTrie.similarityAnalyser("file2", "file3", "file4"));
		  }

}
