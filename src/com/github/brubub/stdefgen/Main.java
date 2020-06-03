package com.github.brubub.stdefgen;

import java.io.FileNotFoundException;

import java.io.IOException;
import java.util.Scanner;

import com.github.brubub.stdefgen.exceptions.ImpossibleToContinueExecutionException;

public class Main {
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ImpossibleToContinueExecutionException {
		System.out.println("ST-DEF-GEN");
		
		if(args.length == 0) {
			args = readParamFromConsole();
		}
		
		ExecutionPlanner planner = null;
		if(paramsValidation(args)) {
			planner = plannerInstantiation(args);
		}
		else {
			throw new ImpossibleToContinueExecutionException("Parametri non validi.");
		}
		planner.run();
	}
	
	private static String[] readParamFromConsole() {
		String[] args = null;
		
		System.out.println("Non e' stato specificato nessun parametro di input. Sono ammessi due parametri:");
		System.out.println("(OBBLIGATORIO)  - il path assoluto della directory con il codice sorgente di ST");
		System.out.println("(OPZIONALE) 	- il path assoluto della directory dove posizionare la definizione creata");
		System.out.println("Scrivere su un'unica riga i parametri separandoli con ?");
		
		Scanner scanner = new Scanner(System.in);
		
		if(scanner.hasNextLine()) {
			String arg0 = scanner.nextLine();
			if(!arg0.equals("") && !arg0.equals(" ")) {
				args = arg0.split("\\?");
			}
			//args rimane null
		}

		scanner.close();
		return args;
	}
	
	private static String paramNormalization(String param) {
		int i = 0;
		while(param.charAt(i) == ' ') {
			i++;
		}
		int k = param.length() - 1;
		while(param.charAt(k) == ' ') {
			k--;
		}
		param = param.substring(i,k+1);
		param = param.replace('\\', '/');
		return param;
	}
	
	private static boolean paramsValidation(String[] param) {
		if(param.length == 0 ) {
			return false;
		}
		
		for(int i = 0; i<param.length; i++) {
			param[i] = paramNormalization(param[i]);
		}
		
		boolean valid = false;
		
		if(param.length >= 1) {
			valid = Utils.fileExists(param[0]);
		}
		
		if(valid == true) {
			if(param.length == 2) {
				valid =  Utils.isValidPath(param[1]);
			}
		}
		return valid;
	}
	
	private static ExecutionPlanner plannerInstantiation(String[] param) {
		if(param.length == 1 ) {
			return new ExecutionPlanner(param[0]);
		}
		else if(param.length == 2 ) {
			return new ExecutionPlanner(param[0],param[1]);
		}
		return null;
	}
	
}
