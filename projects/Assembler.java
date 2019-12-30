import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class Assembler{
	//fill symbol hashmap
	public static void fillSymbols(HashMap<String, String> symbMap) {
		symbMap.put("SP", "0"); symbMap.put("LCL", "1"); symbMap.put("ARG", "2"); symbMap.put("THIS", "3");
		symbMap.put("THAT", "4"); symbMap.put("R0", "0"); symbMap.put("R1", "1"); symbMap.put("R2", "2");
		symbMap.put("R3", "3"); symbMap.put("R4", "4"); symbMap.put("R5", "5"); symbMap.put("R6", "6");
		symbMap.put("R7", "7"); symbMap.put("R8", "8"); symbMap.put("R9", "9"); symbMap.put("R10", "10");
		symbMap.put("R11", "11"); symbMap.put("R12", "12"); symbMap.put("R13", "13"); symbMap.put("R14", "14");
		symbMap.put("R15", "15"); symbMap.put("SCREEN", "16384"); symbMap.put("KBD", "24576");
	}
	
	//fill dest hashmap
	public static void fillDest(HashMap<String, String> destMap) {
		destMap.put("", "000"); destMap.put("M", "001"); destMap.put("D", "010"); destMap.put("MD", "011"); 
		destMap.put("A", "100"); destMap.put("AM", "101"); destMap.put("AD", "110"); destMap.put("AMD", "111");
	}
	
	//fill comp hashmap
	public static void fillComp(HashMap<String, String> compAMap, HashMap<String, String> compMMap) {
		compMMap.put("M", "110000"); compMMap.put("!M", "110001"); compMMap.put("-M", "110011");
		compMMap.put("M+1", "110111"); compMMap.put("M-1", "110010"); compMMap.put("D+M", "000010");
		compMMap.put("D-M", "010011"); compMMap.put("M-D", "000111"); compMMap.put("D&M", "000000");
		compMMap.put("D|M", "010101"); compAMap.put("0", "101010"); compAMap.put("1", "111111");
		compAMap.put("-1", "111010"); compAMap.put("D", "001100"); compAMap.put("A", "110000");
		compAMap.put("!D", "001101"); compAMap.put("!A", "110001"); compAMap.put("-D", "001111");
		compAMap.put("-A", "110011"); compAMap.put("D+1", "011111"); compAMap.put("A+1", "110111");
		compAMap.put("D-1", "001110"); compAMap.put("A-1", "110010"); compAMap.put("D+A", "000010");
		compAMap.put("D-A", "010011"); compAMap.put("A-D", "000111"); compAMap.put("D&A", "000000");
		compAMap.put("D|A", "010101"); compAMap.put("", "000000"); compMMap.put("", "000000");
	}
	
	//fill jump hashmap
	public static void fillJump(HashMap<String, String> jumpMap) {
		jumpMap.put("", "000"); jumpMap.put("JGT", "001"); jumpMap.put("JEQ", "010");
		jumpMap.put("JGE", "011"); jumpMap.put("JLT", "100"); jumpMap.put("JNE", "101");
		jumpMap.put("JLE", "110"); jumpMap.put("JMP", "111");
	}
	
	private static String convertToBinary(String sym) {
		// Convert a base-10 number (string) to 16 bit binary
		Integer x = Integer.parseInt(sym);
		String binString = Integer.toBinaryString(x);
		String formatted = String.format("%16s", binString).replace(' ', '0');
		return formatted;
	}
	
	public static void main(String[] args) {
		try {
			//initialize fill symb, dest, comp, and jump tables
			HashMap<String, String> symbMap = new HashMap<String, String>();
			HashMap<String, String> destMap = new HashMap<String, String>();
			HashMap<String, String> compAMap = new HashMap<String, String>();
			HashMap<String, String> compMMap = new HashMap<String, String>();
			HashMap<String, String> jumpMap = new HashMap<String, String>();
			
			fillSymbols(symbMap);
			fillDest(destMap);
			fillComp(compAMap, compMMap);
			fillJump(jumpMap);
			
			String fileName = JOptionPane.showInputDialog("File Name");
			
			//PASS 1: Add labels to symbol table
			Scanner reader = new Scanner(new File(fileName+".asm"));
			String line;
			
			int rom=0;
			while(reader.hasNext()) {
				line=reader.nextLine().trim(); //cut whitespace
				
				//cut comments
				if(line.indexOf("//") > -1) line=line.substring(0, line.indexOf("//"));
				//skip blanks
				if(line.equals("")) continue;
				
				//add labels to symbol table
				if(Character.toString(line.charAt(0)).equals("(")) {
					symbMap.put(line, Integer.toString(rom));	
				}
				else { //increment on A and C instructions
					rom++;
				}
			}
			reader.close();
		
			//PASS 2: Handle variables and translate
			Scanner reader2 = new Scanner(new File(fileName+".asm"));
			
			rom = 16;
			String message = "";
			while(reader2.hasNext()) {
				line=reader2.nextLine().trim();

				//FINISH PARSING
				//cut comments
				if(line.indexOf("//") > -1) line=line.substring(0, line.indexOf("//"));
				//skip blanks
				if(line.equals("")) continue;
				
				String firstChar = Character.toString(line.charAt(0));
				//skip labels
				if(firstChar.equals("(")) continue;
				
				//handle variables
				if(firstChar.equals("@")) {
					//if variable isn't in symbol table, add to symbol table
					String k = line.substring(1); //cut @
					if(!symbMap.containsKey(k)) {
						symbMap.put(k, Integer.toString(rom));
						rom++;
					}
				}

				//TRANSLATE
				String eval = line;
				if(firstChar.equals("(") || firstChar.equals("@")) {
					eval = line.substring(1);
				}
				
				//handle a instructions
				if(firstChar.equals("@")) {
					String binNum = convertToBinary(symbMap.get(eval));
					message+=binNum+"\n";
				}
				
				//handle c instructions
				else {
					String dest = "";
					String comp = "";
					String jump = "";
					
					int index1 = eval.indexOf("=");
					int index2 = eval.indexOf(";");
					
					//dest = comp;jump
					if(index1 != -1 && index2 != -1) {
						dest = eval.substring(0, index1);
						comp = eval.substring(index1+1, index2);
						jump = eval.substring(index2+1).trim();
					}
					//dest = comp
					else if(index1 != -1) {
						dest = eval.substring(0, index1);
						comp = eval.substring(index1+1).trim();
					}
					//comp;jump
					else if(index2 != -1) {
						comp = eval.substring(0, index2);
						jump = eval.substring(index2+1).trim();
					}
					//dest
					else {
						dest = eval;
					}
					
					String add = "";
					add+="111";
					
					//a or m
					if(compAMap.containsKey(comp)) {
						add+="0";
						comp=compAMap.get(comp);
					}
					else {
						add+="1";
						comp=compMMap.get(comp);
					}
					add+=comp + destMap.get(dest) + jumpMap.get(jump) + "\n";
					
					message+=add;
				}
			}
			reader2.close();
			
			//Write to .hack file
			PrintWriter writer = new PrintWriter(fileName+".hack", "UTF-8");
			writer.print(message);
			writer.close();
		}
		catch(IOException ex) {
			System.out.println(ex);
		}
	}
}