package Experiments;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import PreProcessing.WordType;
import ReadFile.ReadFromFile;
import ReadFile.fileIO;
import Solver.CBA_DP;
import Solver.JavaShellUtil;

public class Evaluation {	
	public static List<String> evaluateDP_Multiple(String ruleName, String testSet) {
		List<String> outList = new LinkedList<String>();
		String rootPath = "data/test/";
		
		String rulePath = rootPath + "rules/" + ruleName + ".lp";//DPGeneratedRules, DPID
		List<String> ruleList = new LinkedList<String>();
		ReadFromFile.readFileByLines(rulePath);
		ruleList.addAll(ReadFromFile.list);		
		
		String programName = ruleName;//DPID, DPGeneratedRules
		
		String gringoline = "gringo rules/" + programName + ".lp ";
		gringoline += "facts/" + testSet + "_ID.facts ";
		gringoline += "facts/stopword.facts > ASP/" + testSet + ".asp";
		
		List<String> shellList = new LinkedList<String>();
		shellList.add("#!/bin/bash");
		shellList.add("cd " + rootPath);
		shellList.add(gringoline);
		String shellPath = rootPath + programName + ".sh";
		fileIO.printSaveList(shellList, shellPath);	
		
		System.out.println("Starting JavaShellUtil ...");							
		int success = 0;
		try {
			success = JavaShellUtil.executeShell("sh " + rootPath + programName + ".sh");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<String> testLabelList = getTestLabelList(rootPath, testSet);
		System.out.println("labeled aspect number = " + testLabelList.size());		
		
		if(success == 1) {// shell is successfully done			
			List<String> extList = getExtractedList(rootPath, testSet);			
			extList = getFrequentExtractedAspects(extList, testSet.toLowerCase());
			System.out.println("extracted aspect number = " + extList.size());
			
			String evaluation = AspectEvaluation(testLabelList, extList);
			//System.out.println("evaluation result = " + evaluation);
			//outList.add("DP: Classifier testing results on the dataset: " + testSet);
			//outList.add("#trueExtNum,#falseExtNum,precision,recall,f-score");
			outList.add(evaluation);
		}
		return outList;
	}
	
	
	
	public static List<String> evaluateDP_Distinct(String ruleName, String testSet) {
		List<String> outList = new LinkedList<String>();
		String rootPath = "data/test/";
		
		String rulePath = rootPath + "rules/" + ruleName + ".lp";//DPGeneratedRules, DPID
		List<String> ruleList = new LinkedList<String>();
		ReadFromFile.readFileByLines(rulePath);
		ruleList.addAll(ReadFromFile.list);		
		
		String programName = ruleName;//DPID, DPGeneratedRules
		
		String gringoline = "gringo rules/" + programName + ".lp ";
		gringoline += "facts/" + testSet + "_ID.facts ";
		gringoline += "facts/stopword.facts > ASP/" + testSet + ".asp";			
		
		List<String> shellList = new LinkedList<String>();
		shellList.add("#!/bin/bash");
		shellList.add("cd " + rootPath);
		shellList.add(gringoline);
		String shellPath = rootPath + programName + ".sh";
		fileIO.printSaveList(shellList, shellPath);	
		
		System.out.println("Starting JavaShellUtil ...");							
		int success = 0;
		try {
			success = JavaShellUtil.executeShell("sh " + rootPath + programName + ".sh");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<String> testLabelList = getTestLabelList(rootPath, testSet);
		
		if(success == 1) {// shell is successfully done			
			List<String> extList = getExtractedList(rootPath, testSet);
			extList = getFrequentExtractedAspects(extList, testSet.toLowerCase());
			
			String evaluation = AspectEvaluationByOccurence(testLabelList, extList);
			//outList.add("DP: Classifier testing results on the dataset: " + testSet);
			//outList.add("#trueExtNum,#falseExtNum,precision,recall,f-score");
			outList.add(evaluation);
		}
		return outList;
	}
	
	
	public static List<String> evaluateCRF(String dataPath, String testPath, String dataset) {
		List<String> outList = new LinkedList<String>();
		
		List<String> labList = getTestLabelList("data/test/", dataset);//getLabeledAspects(labPath);
		List<String> extList = getExtractedCRFAspects(dataPath, testPath);
		fileIO.printList(extList);
		String score = AspectEvaluation_CRF(labList, extList);
		outList.add(score);
		score = AspectEvaluationByOccurence_CRF(labList, extList);
		outList.add(score);
		return outList;
	}
	
	
	public static List<String> getExtractedCRFAspects(String dataPath, String testPath) {
		List<String> outList = new LinkedList<String>();
		List<String> dataList = new LinkedList<String>();
		List<String> testList = new LinkedList<String>();
		
		ReadFromFile.readFileByLines(dataPath);
		dataList.addAll(ReadFromFile.list);
		
		ReadFromFile.readFileByLines(testPath);
		testList.addAll(ReadFromFile.list);
		
		for(int li = 0; li < testList.size(); li++) {
			String tline = testList.get(li).trim();
			//System.out.println(tline);
			if(tline.length() > 0 && !tline.equals("O")) {
				String outStr = "";
				if(tline.equals("B-Target")) {
					String dline = dataList.get(li);
					//System.out.println(dline);
					if(dline.trim().length() > 0) {
						dline = dline.substring(0, dline.indexOf(" ")).trim();
						outStr += dline;
					}
					if(li < testList.size() - 1) {
						int dli = li + 1;
						String next = testList.get(dli).trim();
						boolean flag = false;
						if(next.equals("I-Target"))
							flag = true;
						while(flag == true) {
							testList.set(dli, "O");
							dline = dataList.get(dli);
							dline = dline.substring(0, dline.indexOf(" ")).trim();
							outStr += "_" + dline;
							dli++;
							if(dli < testList.size()) {
								next = testList.get(dli).trim();
								if(next.isEmpty() || !next.equals("I-Target"))//
									flag = false;
							}
						}
					}
					outList.add(outStr);
					outStr = "";
				}
			}
		}
		outList = DoublePropagation.ExtractedAspects_ID.getFrequent(outList);
		outList = PreProcessing.WordType.bubble_Sort(outList);
		return outList;
	}
	
	
	public static List<String> getTestLabelList(String rootPath, String testSet) {
		List<String> testLabelList = new LinkedList<String>();
		List<String> listcorpusStr = new LinkedList<String>();
		String sourceFileName = "";
		
		if(testSet.contains("Laptops") || testSet.contains("Restaurants")) {
			sourceFileName = rootPath + "facts/" + testSet + "_lab.facts";
			ReadFromFile.readFileByLines(sourceFileName);
			listcorpusStr.addAll(ReadFromFile.list);				
			testLabelList = PreProcessing.LabeledFactsExtractor_SemEval.getAspect(listcorpusStr);
		}
		else if(testSet.equals("Canon") || testSet.equals("Nikon") || testSet.equals("Nokia")
				|| testSet.equals("Creative") || testSet.equals("Apex")){
			if(testSet.equals("Canon"))
				sourceFileName = "data/CustomerReviewDatasets/processed datasets/" + testSet + "G3Sentences.txt";
			else if(testSet.equals("Creative"))
				sourceFileName = "data/CustomerReviewDatasets/processed datasets/" + testSet + "LabsSentences.txt";
			else
				sourceFileName = "data/CustomerReviewDatasets/processed datasets/" + testSet + "Sentences.txt";	
			ReadFromFile.readFileByLines(sourceFileName);
			listcorpusStr.addAll(ReadFromFile.list);					
			testLabelList = PreProcessing.LabeledFactsExtractor.getAspect(listcorpusStr, testSet.toLowerCase());
			
		} else {//if(testSet.equals("Diaper") || testSet.equals("Hitachi") || testSet.equals("Linksys") || testSet.equals("Norton")) 
			sourceFileName = "data/CustomerReviewDatasets/NewData/" + testSet + "Sentences.txt";
			ReadFromFile.readFileByLines(sourceFileName);
			listcorpusStr.addAll(ReadFromFile.list);
			System.out.println("corpus size = " + listcorpusStr.size());
			testLabelList = PreProcessing.LabeledFactsExtractor.getAspect(listcorpusStr, testSet.toLowerCase());			
		}		
		
		testLabelList = PreProcessing.LabeledFactsExtractor.getFrequent(testLabelList);	// compute the frequencies of the labeled aspects
		testLabelList = WordType.bubble_Sort(testLabelList); // sort the aspects in descending order
		
		return testLabelList;
	}	
	
		
	
	public static List<String> getExtractedList(String rootPath, String aspFileName) {
		List<String> extList = new LinkedList<String>();
		String aspPath = rootPath + "ASP/" + aspFileName + ".asp";
		List<String> aspList = new LinkedList<String>();
		ReadFromFile.readFileByLines(aspPath);
		aspList.addAll(ReadFromFile.list);
		
		System.out.println("getExtractedList.getAspect_ID() ...");
		extList = DoublePropagation.ExtractedAspects_ID.getAspect_ID(aspList); // extract aspects from the answer set
		System.out.println("getExtractedList.getFrequent() ...");
		extList = DoublePropagation.ExtractedAspects_ID.getFrequent(extList);// compute the frequencies of the aspect candidates
		System.out.println("getExtractedList.bubble_sort() ...");
		extList = PreProcessing.WordType.bubble_Sort(extList);// sort the candidates in descending order			
				
		return extList;
	}
	
	
	/**
	 * get frequent aspect candidates
	 * @param list
	 * @return
	 */
	public static List<String> getFrequentExtractedAspects(List<String> list, String dataset)
	{
		List<String> freAspectList = new LinkedList<String>();
		int wordlength1 = 0;
		int frequent1 = 0;
		int wordlength2 = 0;
		int frequent2 = 0;
		int wordlength3 = 0;
		int frequent3 = 0;
		if(dataset.equals("canon"))
		{//canon:3,0; nikon:2,0; nokia:2,0; create:2,0; apex:2,0		 
			wordlength1 = 3;
			frequent1 = 0;
			//canon:3,0; nikon:2,0; nokia:2,0; create:2,0; apex:1,0
			wordlength2 = 3;
			frequent2 = 0;
			//canon:3,1; nikon:7,1; nokia:7,1; create:12,1; apex:3,1
			wordlength3 = 3;
			frequent3 = 1;
		}
		else if(dataset.equals("nokia"))
		{//canon:3,0; nikon:2,0; nokia:2,0; create:2,0; apex:2,0		 
			wordlength1 = 2;
			frequent1 = 0;
			//canon:3,0; nikon:2,0; nokia:2,0; create:2,0; apex:1,0
			wordlength2 = 2;
			frequent2 = 0;
			//canon:3,1; nikon:7,1; nokia:7,1; create:12,1; apex:3,1
			wordlength3 = 7;
			frequent3 = 1;
		}
		else if(dataset.equals("nikon"))
		{//canon:3,0; nikon:2,0; nokia:2,0; create:2,0; apex:2,0		 
			wordlength1 = 2;
			frequent1 = 0;
			//canon:3,0; nikon:2,0; nokia:2,0; create:2,0; apex:1,0
			wordlength2 = 2;
			frequent2 = 0;
			//canon:3,1; nikon:7,1; nokia:7,1; create:12,1; apex:3,1
			wordlength3 = 7;
			frequent3 = 1;
		}
		else if(dataset.equals("creative"))
		{//canon:3,0; nikon:2,0; nokia:2,0; create:2,0; apex:2,0		 
			wordlength1 = 2;
			frequent1 = 0;
			//canon:3,0; nikon:2,0; nokia:2,0; create:2,0; apex:1,0
			wordlength2 = 2;
			frequent2 = 0;
			//canon:3,1; nikon:7,1; nokia:7,1; create:12,1; apex:3,1
			wordlength3 = 12;
			frequent3 = 1;
		}
		else if(dataset.equals("apex"))
		{//canon:3,0; nikon:2,0; nokia:2,0; create:2,0; apex:2,0		 
			wordlength1 = 2;
			frequent1 = 0;
			//canon:3,0; nikon:2,0; nokia:2,0; create:2,0; apex:1,0
			wordlength2 = 1;
			frequent2 = 0;
			//canon:3,1; nikon:7,1; nokia:7,1; create:12,1; apex:3,1
			wordlength3 = 3;
			frequent3 = 1;
		}
		if(list.size() > 0)
		{
			for(int li = 0; li < list.size(); li++)
			{
				String line = list.get(li).trim();				
				//System.out.println(line);
				if(!line.equals(""))
				{
					int frequency = Integer.parseInt(line.substring(0, line.indexOf("	")).trim());
					String word = line.substring(line.indexOf("	") + 1).trim();				
					word  = normalize(word);
					
					if(word.length() > wordlength1 && frequency > frequent1)//canon:3,0; nikon:2,0; nokia:2,0; create:2,0; apex:2,0
					{
						if(WordType.isNegWord(word) == 0 && WordType.isPosWord(word) == 0)
						{
							freAspectList.add(frequency + "	" + word);
						}						
						else if(word.length() > wordlength2 && frequency > frequent2)//canon:3,0; nikon:2,0; nokia:2,0; create:2,0; apex:1,0
						{
							if(word.length() > wordlength3 && frequency > frequent3)//canon:3,1; nikon:7,1; nokia:7,1; create:12,1; apex:3,1
							{							
								freAspectList.add(frequency + "	" + word);
								//System.out.println(word);
							}
							//else
								//System.out.println(frequency + "	" + word + " A1");
						}
						//else
							//System.out.println(frequency + "	" + word + " A2");
					}
					//else
						//System.out.println(frequency + "	" + word + " A3");
				}
				
			}
		}
		return freAspectList;
	}
	
	
	/**
	 * 功能：计算抽取到的explicit aspect的准确率，召回率和F值
	 * @param labeledFileName
	 * @param extractedFileName
	 */
	public static String AspectEvaluation(List<String> labeledList, List<String> extractedList)
	{
		//List<String> outList = new LinkedList<String>();
		double precision = 0;
		double recall = 0;
		double f_measure = 0;
		List<String> extUnmatchedlist = new LinkedList<String>();
		List<String> extMatchedlist = new LinkedList<String>();
		List<String> labUnmatchedlist = new LinkedList<String>();
		List<String> labMatchedlist = new LinkedList<String>();
		
		List<String> lablistFlag = new LinkedList<String>();
		for(int list = 0; list < labeledList.size(); list++)
			lablistFlag.add("unmatched");
		
		List<String> extlistFlag = new LinkedList<String>();
		for(int list = 0; list < extractedList.size(); list++)
			extlistFlag.add("unmatched");
		
		List<String> extAspectList = new LinkedList<String>();
		List<Integer> extFreqList = new LinkedList<Integer>();
		for(int list = 0; list < extractedList.size(); list++)
		{
			String extLine = extractedList.get(list).trim();
			int fre = Integer.parseInt(extLine.substring(0, extLine.indexOf("	")));
			extLine = extLine.substring(extLine.indexOf("	") + 1);
			extAspectList.add(extLine);
			extFreqList.add(fre);
		}
		//fileIO.printList(extractedList);
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		//fileIO.printList(labeledList);
		
		//String extPercentage = getPhrasePercentage(extractedList);
		//String labPercentage = getPhrasePercentage(labeledList);		
		
		List<String> labAspectList = new LinkedList<String>();
		List<Integer> labFreqList = new LinkedList<Integer>();
		for(int list = 0; list < labeledList.size(); list++)
		{
			String labLine = labeledList.get(list).trim();
			if(!labLine.trim().equals(""))
			{
				//System.out.println("labLine: " + labLine);
				int fre = Integer.parseInt(labLine.substring(0, labLine.indexOf("	")));
				labLine = labLine.substring(labLine.indexOf("	") + 1);
				labLine = normalize(labLine);
				labAspectList.add(labLine);
				labFreqList.add(fre);
			}			
		}
		
		for(int ext = 0; ext < extAspectList.size(); ext++)
		{//for1
			String extaspect = extAspectList.get(ext).trim();
			//String tmp = extaspect;
			extaspect = normalize(extaspect);
			int flag = 0;			
			for(int list = 0; list < labAspectList.size(); list++)
			{//for2
				String labeledLine = labAspectList.get(list);
				
				if(extaspect.equals(labeledLine) || labeledLine.contains(extaspect))//
				{//if2						
					flag = 1;
					lablistFlag.set(list, "matched");
					//System.out.println(labeledLine + " a--> " + tmp);
				}//if2
				else if(!extaspect.contains("_"))// && !labeledLine.contains("_")
				{//if5					
					if(WordType.isSimilar(extaspect, labeledLine) == 1 || WordType.isSimilar(labeledLine, extaspect) == 1)// 
					{//if6								
						flag = 1;
						lablistFlag.set(list, "matched");
						//System.out.println(labeledLine + " b--> " + tmp);
					}//if6
				}//if5
				else if(extaspect.contains("_"))
				{//if else5
					String[] extExpArray = extaspect.split("_");
					for(int arr = 0; arr < extExpArray.length; arr++)
					{//for4
						if(WordType.isSimilar(extExpArray[arr], labeledLine) == 1 || WordType.isSimilar(labeledLine, extExpArray[arr]) == 1)//
						{//if8										
							flag = 1;
							lablistFlag.set(list, "matched");
							//System.out.println(labeledLine + " c--> " + tmp);
						}//if8						
					}//for4								
				}//if else5
			}//for2					
			if(flag == 1)
				extlistFlag.set(ext, "matched");
		}	
		
		int allExtNum = 0;
		int trueExtNum = 0;
		int allLabNum = 0;
		int trueLabNum = 0;
		
		for(int list = 0; list < labFreqList.size(); list++)
		{
			int num = labFreqList.get(list);
			allLabNum += num;
			if(lablistFlag.get(list).equals("matched"))
			{
				trueLabNum += num;
				labMatchedlist.add(labAspectList.get(list));
				//System.out.println("labeled matched: " + labAspectList.get(list) + " " + labFreqList.get(list));
			}
			else
			{
				labUnmatchedlist.add(labAspectList.get(list));
				System.out.println("labeled unmatched: " + labAspectList.get(list) + " " + labFreqList.get(list));
			}
		}
		
		for(int list = 0; list < extFreqList.size(); list++)
		{
			int num = extFreqList.get(list);
			allExtNum += num;
			if(extlistFlag.get(list).equals("matched"))
			{
				trueExtNum += num;
				extMatchedlist.add(extAspectList.get(list));
				//System.out.println("extracted matched: " + extAspectList.get(list) + " " + extFreqList.get(list));
			}
			else
			{
				extUnmatchedlist.add(extAspectList.get(list));
				System.out.println("extracted unmatched: " + extAspectList.get(list) + " " + extFreqList.get(list));
			}
		}
		
		precision = ((trueExtNum) * 1.0 / allExtNum) * 100;
		recall = (trueLabNum * 1.0 / allLabNum) * 100;
		f_measure = 2.0 * precision * recall / (precision + recall);	
		
		String outStr = "";
		//outStr += trueExtNum + "," + allExtNum + "," + trueLabNum + "," + allLabNumEachRule + "," + support + ",";
		//outStr += String.format("%.4f", confidence) + "," + String.format("%.6f", lift) + ",";
		outStr += String.format("%.2f", precision) + "," + String.format("%.2f", recall) + "," 
		+ String.format("%.2f", f_measure);
		
		//outStr += ",*," + extPercentage + ",*," + labPercentage;
		//System.out.println("Extracted phrase percentage is: " + extPercentage);
		//System.out.println("Labeled phrase percentage is: " + labPercentage);
		
		//System.out.println(outStr);	
		return outStr;		
	}
	
	/**
	 * 功能：计算抽取到的explicit aspect的准确率，召回率和F值
	 * @param labeledFileName
	 * @param extractedFileName
	 */
	public static String AspectEvaluationByOccurence(List<String> labeledList, List<String> extractedList)
	{
		//List<String> outList = new LinkedList<String>();
		double precision = 0;
		double recall = 0;
		double f_measure = 0;
		List<String> extUnmatchedlist = new LinkedList<String>();
		List<String> extMatchedlist = new LinkedList<String>();
		List<String> labUnmatchedlist = new LinkedList<String>();
		List<String> labMatchedlist = new LinkedList<String>();
		
		List<String> lablistFlag = new LinkedList<String>();
		for(int list = 0; list < labeledList.size(); list++)
			lablistFlag.add("unmatched");
		
		List<String> extlistFlag = new LinkedList<String>();
		for(int list = 0; list < extractedList.size(); list++)
			extlistFlag.add("unmatched");
		
		List<String> extAspectList = new LinkedList<String>();
		List<Integer> extFreqList = new LinkedList<Integer>();
		for(int list = 0; list < extractedList.size(); list++)
		{
			String extLine = extractedList.get(list).trim();
			int fre = Integer.parseInt(extLine.substring(0, extLine.indexOf("	")));
			extLine = extLine.substring(extLine.indexOf("	") + 1);
			extAspectList.add(extLine);
			extFreqList.add(fre);
		}	
		
		List<String> labAspectList = new LinkedList<String>();
		List<Integer> labFreqList = new LinkedList<Integer>();
		for(int list = 0; list < labeledList.size(); list++)
		{
			String labLine = labeledList.get(list).trim();
			if(!labLine.trim().equals(""))
			{
				//System.out.println("labLine: " + labLine);
				int fre = Integer.parseInt(labLine.substring(0, labLine.indexOf("	")));
				labLine = labLine.substring(labLine.indexOf("	") + 1);
				labLine = normalize(labLine);
				labAspectList.add(labLine);
				labFreqList.add(fre);
			}			
		}
		
		for(int ext = 0; ext < extAspectList.size(); ext++)
		{//for1
			String extaspect = extAspectList.get(ext).trim();
			String tmp = extaspect;
			extaspect = normalize(extaspect);
			int flag = 0;			
			for(int list = 0; list < labAspectList.size(); list++)
			{//for2
				String labeledLine = labAspectList.get(list);
				
				if(extaspect.equals(labeledLine) || labeledLine.contains(extaspect))//
				{//if2						
					flag = 1;
					lablistFlag.set(list, "matched");
					//System.out.println(labeledLine + " a--> " + tmp);
				}//if2
				else if(!extaspect.contains("_"))
				{//if5					
					if(WordType.isSimilar(extaspect, labeledLine) == 1 || WordType.isSimilar(labeledLine, extaspect) == 1)
					{//if6								
						flag = 1;
						lablistFlag.set(list, "matched");
						//System.out.println(labeledLine + " b--> " + tmp);
					}//if6
				}//if5
				else 
				{//if else5
					String[] extExpArray = extaspect.split("_");
					for(int arr = 0; arr < extExpArray.length; arr++)
					{//for4
						if(WordType.isSimilar(extExpArray[arr], labeledLine) == 1 || WordType.isSimilar(labeledLine, extExpArray[arr]) == 1)
						{//if8										
							flag = 1;
							lablistFlag.set(list, "matched");
							//System.out.println(labeledLine + " c--> " + tmp);
						}//if8						
					}//for4								
				}//if else5
			}//for2					
			if(flag == 1)
				extlistFlag.set(ext, "matched");
		}	
		
		int allExtNum = 0;
		int trueExtNum = 0;
		int allLabNum = 0;
		int trueLabNum = 0;
		
		for(int list = 0; list < labFreqList.size(); list++)
		{
			//int num = labFreqList.get(list);
			allLabNum += 1;
			if(lablistFlag.get(list).equals("matched"))
			{
				trueLabNum += 1;
				labMatchedlist.add(labAspectList.get(list));
				//System.out.println("labeled matched: " + labAspectList.get(list) + " " + labFreqList.get(list));
			}
			else
			{
				labUnmatchedlist.add(labAspectList.get(list));
				//System.out.println("labeled unmatched: " + labAspectList.get(list) + " " + labFreqList.get(list));
			}
		}
		
		for(int list = 0; list < extFreqList.size(); list++)
		{
			//int num = extFreqList.get(list);
			allExtNum += 1;
			if(extlistFlag.get(list).equals("matched"))
			{
				trueExtNum += 1;
				extMatchedlist.add(extAspectList.get(list));
				//System.out.println("extracted matched: " + extAspectList.get(list) + " " + extFreqList.get(list));
			}
			else
			{
				extUnmatchedlist.add(extAspectList.get(list));
				//System.out.println("extracted unmatched: " + extAspectList.get(list) + " " + extFreqList.get(list));
			}
		}
		
		precision = ((trueExtNum) * 1.0 / allExtNum) * 100;
		recall = (trueLabNum * 1.0 / allLabNum) * 100;
		f_measure = 2.0 * precision * recall / (precision + recall);	
		
		String outStr = "";
		//outStr += trueExtNum + "," + allExtNum + "," + trueLabNum + "," + allLabNumEachRule + ","
		//+ support + "," + String.format("%.4f", confidence) + "," + String.format("%.6f", lift) + ",";
		outStr += String.format("%.2f", precision) + "," + String.format("%.2f", recall) + "," 
		+ String.format("%.2f", f_measure);
		
		//outStr += ",*," + extPercentage + ",*," + labPercentage;		
		//System.out.println("Extracted phrase percentage is: " + extPercentage);
		//System.out.println("Labeled phrase percentage is: " + labPercentage);
		//System.out.println(outStr);
		return outStr;		
	}
	
	
	
	/**
	 * 功能：计算抽取到的explicit aspect的准确率，召回率和F值
	 * @param labeledFileName
	 * @param extractedFileName
	 */
	public static String AspectEvaluation_CRF(List<String> labeledList, List<String> extractedList)
	{
		//List<String> outList = new LinkedList<String>();
		double precision = 0;
		double recall = 0;
		double f_measure = 0;
		List<String> extUnmatchedlist = new LinkedList<String>();
		List<String> extMatchedlist = new LinkedList<String>();
		List<String> labUnmatchedlist = new LinkedList<String>();
		List<String> labMatchedlist = new LinkedList<String>();
		
		List<String> lablistFlag = new LinkedList<String>();
		for(int list = 0; list < labeledList.size(); list++)
			lablistFlag.add("unmatched");
		
		List<String> extlistFlag = new LinkedList<String>();
		for(int list = 0; list < extractedList.size(); list++)
			extlistFlag.add("unmatched");
		
		List<String> extAspectList = new LinkedList<String>();
		List<Integer> extFreqList = new LinkedList<Integer>();
		for(int list = 0; list < extractedList.size(); list++)
		{
			String extLine = extractedList.get(list).trim();
			int fre = Integer.parseInt(extLine.substring(0, extLine.indexOf("	")));
			extLine = extLine.substring(extLine.indexOf("	") + 1);
			extAspectList.add(extLine);
			extFreqList.add(fre);
		}
		//fileIO.printList(extractedList);
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		//fileIO.printList(labeledList);
		
		//String extPercentage = getPhrasePercentage(extractedList);
		//String labPercentage = getPhrasePercentage(labeledList);		
		
		List<String> labAspectList = new LinkedList<String>();
		List<Integer> labFreqList = new LinkedList<Integer>();
		for(int list = 0; list < labeledList.size(); list++)
		{
			String labLine = labeledList.get(list).trim();
			if(!labLine.trim().equals(""))
			{
				//System.out.println("labLine: " + labLine);
				int fre = Integer.parseInt(labLine.substring(0, labLine.indexOf("	")));
				labLine = labLine.substring(labLine.indexOf("	") + 1);
				labLine = normalize(labLine);
				labAspectList.add(labLine);
				labFreqList.add(fre);
			}			
		}
		
		for(int ext = 0; ext < extAspectList.size(); ext++)
		{//for1
			String extaspect = extAspectList.get(ext).trim();
			//String tmp = extaspect;
			extaspect = normalize(extaspect);
			int flag = 0;			
			for(int list = 0; list < labAspectList.size(); list++)
			{//for2
				String labeledLine = labAspectList.get(list);
				
				if(extaspect.equals(labeledLine))// || labeledLine.startsWith(extaspect)
				{//if2						
					flag = 1;
					lablistFlag.set(list, "matched");
					//System.out.println(labeledLine + " a--> " + tmp);
				}//if2
				else if(!extaspect.contains("_"))// && !labeledLine.contains("_")
				{//if5					
					if(WordType.isSimilar(extaspect, labeledLine) == 1)// || WordType.isSimilar(labeledLine, extaspect) == 1
					{//if6								
						flag = 1;
						lablistFlag.set(list, "matched");
						//System.out.println(labeledLine + " b--> " + tmp);
					}//if6
				}//if5
				else if(extaspect.contains("_"))
				{//if else5
					String[] extExpArray = extaspect.split("_");
					for(int arr = 0; arr < extExpArray.length; arr++)
					{//for4
						if(WordType.isSimilar(extExpArray[arr], labeledLine) == 1)// || WordType.isSimilar(labeledLine, extExpArray[arr]) == 1
						{//if8										
							flag = 1;
							lablistFlag.set(list, "matched");
							//System.out.println(labeledLine + " c--> " + tmp);
						}//if8						
					}//for4								
				}//if else5
			}//for2					
			if(flag == 1)
				extlistFlag.set(ext, "matched");
		}	
		
		int allExtNum = 0;
		int trueExtNum = 0;
		int allLabNum = 0;
		int trueLabNum = 0;
		
		for(int list = 0; list < labFreqList.size(); list++)
		{
			int num = labFreqList.get(list);
			allLabNum += num;
			if(lablistFlag.get(list).equals("matched"))
			{
				trueLabNum += num;
				labMatchedlist.add(labAspectList.get(list));
				//System.out.println("labeled matched: " + labAspectList.get(list) + " " + labFreqList.get(list));
			}
			else
			{
				labUnmatchedlist.add(labAspectList.get(list));
				System.out.println("labeled unmatched: " + labAspectList.get(list) + " " + labFreqList.get(list));
			}
		}
		
		for(int list = 0; list < extFreqList.size(); list++)
		{
			int num = extFreqList.get(list);
			allExtNum += num;
			if(extlistFlag.get(list).equals("matched"))
			{
				trueExtNum += num;
				extMatchedlist.add(extAspectList.get(list));
				//System.out.println("extracted matched: " + extAspectList.get(list) + " " + extFreqList.get(list));
			}
			else
			{
				extUnmatchedlist.add(extAspectList.get(list));
				System.out.println("extracted unmatched: " + extAspectList.get(list) + " " + extFreqList.get(list));
			}
		}
		
		precision = ((trueExtNum) * 1.0 / allExtNum) * 100;
		recall = (trueLabNum * 1.0 / allLabNum) * 100;
		f_measure = 2.0 * precision * recall / (precision + recall);	
		
		String outStr = "";
		//outStr += trueExtNum + "," + allExtNum + "," + trueLabNum + "," + allLabNumEachRule + "," + support + ",";
		//outStr += String.format("%.4f", confidence) + "," + String.format("%.6f", lift) + ",";
		outStr += String.format("%.2f", precision) + "," + String.format("%.2f", recall) + "," 
		+ String.format("%.2f", f_measure);
		
		//outStr += ",*," + extPercentage + ",*," + labPercentage;
		//System.out.println("Extracted phrase percentage is: " + extPercentage);
		//System.out.println("Labeled phrase percentage is: " + labPercentage);
		
		//System.out.println(outStr);	
		return outStr;		
	}
	
	/**
	 * 功能：计算抽取到的explicit aspect的准确率，召回率和F值
	 * @param labeledFileName
	 * @param extractedFileName
	 */
	public static String AspectEvaluationByOccurence_CRF(List<String> labeledList, List<String> extractedList)
	{
		//List<String> outList = new LinkedList<String>();
		double precision = 0;
		double recall = 0;
		double f_measure = 0;
		List<String> extUnmatchedlist = new LinkedList<String>();
		List<String> extMatchedlist = new LinkedList<String>();
		List<String> labUnmatchedlist = new LinkedList<String>();
		List<String> labMatchedlist = new LinkedList<String>();
		
		List<String> lablistFlag = new LinkedList<String>();
		for(int list = 0; list < labeledList.size(); list++)
			lablistFlag.add("unmatched");
		
		List<String> extlistFlag = new LinkedList<String>();
		for(int list = 0; list < extractedList.size(); list++)
			extlistFlag.add("unmatched");
		
		List<String> extAspectList = new LinkedList<String>();
		List<Integer> extFreqList = new LinkedList<Integer>();
		for(int list = 0; list < extractedList.size(); list++)
		{
			String extLine = extractedList.get(list).trim();
			int fre = Integer.parseInt(extLine.substring(0, extLine.indexOf("	")));
			extLine = extLine.substring(extLine.indexOf("	") + 1);
			extAspectList.add(extLine);
			extFreqList.add(fre);
		}	
		
		List<String> labAspectList = new LinkedList<String>();
		List<Integer> labFreqList = new LinkedList<Integer>();
		for(int list = 0; list < labeledList.size(); list++)
		{
			String labLine = labeledList.get(list).trim();
			if(!labLine.trim().equals(""))
			{
				//System.out.println("labLine: " + labLine);
				int fre = Integer.parseInt(labLine.substring(0, labLine.indexOf("	")));
				labLine = labLine.substring(labLine.indexOf("	") + 1);
				labLine = normalize(labLine);
				labAspectList.add(labLine);
				labFreqList.add(fre);
			}			
		}
		
		for(int ext = 0; ext < extAspectList.size(); ext++)
		{//for1
			String extaspect = extAspectList.get(ext).trim();
			String tmp = extaspect;
			extaspect = normalize(extaspect);
			int flag = 0;			
			for(int list = 0; list < labAspectList.size(); list++)
			{//for2
				String labeledLine = labAspectList.get(list);
				
				if(extaspect.equals(labeledLine))// || labeledLine.startsWith(extaspect)
				{//if2						
					flag = 1;
					lablistFlag.set(list, "matched");
					//System.out.println(labeledLine + " a--> " + tmp);
				}//if2
				else if(!extaspect.contains("_"))// && !labeledLine.contains("_")
				{//if5					
					if(WordType.isSimilar(extaspect, labeledLine) == 1)// || WordType.isSimilar(labeledLine, extaspect) == 1 
					{//if6								
						flag = 1;
						lablistFlag.set(list, "matched");
						//System.out.println(labeledLine + " b--> " + tmp);
					}//if6
				}//if5
				else 
				{//if else5
					String[] extExpArray = extaspect.split("_");
					for(int arr = 0; arr < extExpArray.length; arr++)
					{//for4
						if(WordType.isSimilar(extExpArray[arr], labeledLine) == 1)// || WordType.isSimilar(labeledLine, extExpArray[arr]) == 1
						{//if8										
							flag = 1;
							lablistFlag.set(list, "matched");
							//System.out.println(labeledLine + " c--> " + tmp);
						}//if8						
					}//for4								
				}//if else5
			}//for2					
			if(flag == 1)
				extlistFlag.set(ext, "matched");
		}	
		
		int allExtNum = 0;
		int trueExtNum = 0;
		int allLabNum = 0;
		int trueLabNum = 0;
		
		for(int list = 0; list < labFreqList.size(); list++)
		{
			//int num = labFreqList.get(list);
			allLabNum += 1;
			if(lablistFlag.get(list).equals("matched"))
			{
				trueLabNum += 1;
				labMatchedlist.add(labAspectList.get(list));
				//System.out.println("labeled matched: " + labAspectList.get(list) + " " + labFreqList.get(list));
			}
			else
			{
				labUnmatchedlist.add(labAspectList.get(list));
				//System.out.println("labeled unmatched: " + labAspectList.get(list) + " " + labFreqList.get(list));
			}
		}
		
		for(int list = 0; list < extFreqList.size(); list++)
		{
			//int num = extFreqList.get(list);
			allExtNum += 1;
			if(extlistFlag.get(list).equals("matched"))
			{
				trueExtNum += 1;
				extMatchedlist.add(extAspectList.get(list));
				//System.out.println("extracted matched: " + extAspectList.get(list) + " " + extFreqList.get(list));
			}
			else
			{
				extUnmatchedlist.add(extAspectList.get(list));
				//System.out.println("extracted unmatched: " + extAspectList.get(list) + " " + extFreqList.get(list));
			}
		}
		
		precision = ((trueExtNum) * 1.0 / allExtNum) * 100;
		recall = (trueLabNum * 1.0 / allLabNum) * 100;
		f_measure = 2.0 * precision * recall / (precision + recall);	
		
		String outStr = "";
		//outStr += trueExtNum + "," + allExtNum + "," + trueLabNum + "," + allLabNumEachRule + ","
		//+ support + "," + String.format("%.4f", confidence) + "," + String.format("%.6f", lift) + ",";
		outStr += String.format("%.2f", precision) + "," + String.format("%.2f", recall) + "," 
		+ String.format("%.2f", f_measure);
		
		//outStr += ",*," + extPercentage + ",*," + labPercentage;		
		//System.out.println("Extracted phrase percentage is: " + extPercentage);
		//System.out.println("Labeled phrase percentage is: " + labPercentage);
		//System.out.println(outStr);
		return outStr;		
	}
	
	
	
	public static String normalize(String word)
	{
		if(word.equals("lense"))
			word = "lens";
		if(word.equals("pix") || word.equals("pic"))
			word = "picture";
		if(word.equals("digicam"))
			word = "digital_camera";
		if(word.equals("extension_cord"))
			word = "extension_card";
		if(word.startsWith("auto") && !word.contains("_") && word.length() > 4)		
			word = "auto_" + word.substring(4);
		/*if(word.startsWith("color") && !word.contains("_") && word.length() > 5)					
			word = "color_" + word.substring(5);
		if(word.startsWith("photo") && !word.contains("_") && word.length() > 5)					
			word = "photo_" + word.substring(5);*/
		/*if(word.equals("g3"))// || word.equals("g2")
			word = "canon_g3";*/
		/*if(word.equals("made"))
			word = "make";*/
		if(word.equals("photograph"))
			word = "photo";
		if(word.equals("setup"))
			word = "set_up";
		if(word.equals("menue"))
			word = "menu";
		if(word.equals("canera"))
			word = "camera";
		if(word.contains("megapixel"))
			word = "az_4mp";
		if(word.equals("dvd"))
			word = "dvd_disc";
		if(word.equals("bookmakr"))
			word = "bookmark";
		if(word.equals("equilizer"))
			word = "equalizer";
		if(word.equals("transfter"))
			word = "transfer";
		if(word.equals("notmad"))
			word = "nomad";
		if(word.equals("griep"))
			word = "grip";
		/*if(word.equals("nokium"))
			word = "nokia";*/
		if(word.equals("mediaplayer"))
			word = "media_player";
		/*if(word.equals("user"))
			word = "use";*/
		if(word.equals("ad2600"))
			word = "ad_2600";
		if(word.equals("vcd"))
			word = "vcd_cd";
		if(word.equals("soom"))
			word = "zoom";
		if(word.equals("zennx"))
			word = "zx";		
		return word;
	}
	
	public static List<String> changeResultFormat(List<String> list) {
		//System.out.println(list.size());
		List<String> outList = new LinkedList<String>();
		List<String> firstList = new LinkedList<String>();
		List<String> secondList = new LinkedList<String>();
		for(int li = 0; li < list.size() - 1; li = li + 2) {
			String firstline = list.get(li).trim();
			String secondline = list.get(li + 1).trim();
			//System.out.println("first: " + firstline);
			//System.out.println("second: " + secondline);
			firstList.add(firstline);
			secondList.add(secondline);
		}
		
		firstList.add(average(firstList));
		secondList.add(average(secondList));
		for(int li = 0; li < firstList.size(); li++) {
			String first = firstList.get(li);
			String second = secondList.get(li);
			String line = first + "," + second;
			outList.add(line);
			//System.out.println(line);
		}	
		return outList;
	}
	
	public static String average(List<String> list) {
		String outStr = "";
		double precision = 0;
		double recall = 0; 
		double fscore = 0;
		for(int li = 0; li < list.size(); li++) {
			String line = list.get(li).trim();
			//System.out.println("average line: " + line);
			if(line.contains(",")) {
				String[] array = line.split(",");
				double p = Double.parseDouble(array[0].trim());
				double r = Double.parseDouble(array[1].trim());
				double f = Double.parseDouble(array[2].trim());
				precision += p;
				recall += r;
				fscore += f;
			}			
		}
		precision = precision / list.size();
		recall = recall / list.size();
		fscore = fscore / list.size();
		outStr = String.format("%.2f", precision) + "," + String.format("%.2f", recall) + "," + String.format("%.2f", fscore);
		return outStr;
	}

}
