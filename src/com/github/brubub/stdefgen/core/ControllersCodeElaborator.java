package com.github.brubub.stdefgen.core;

import com.github.brubub.stdefgen.data.ControllerFile;
import com.github.brubub.stdefgen.exceptions.MethodNonAcceptableReturnException;

public class ControllersCodeElaborator {
	
	public ControllerFile annotate(ControllerFile file) throws MethodNonAcceptableReturnException{
		try{
			file = addClassLevelAnnotations(file);
		}
		catch(MethodNonAcceptableReturnException e) {
			throw e;
		}
		file = addMethodLevelAnnotations(file);
		file = addImports(file);
		return file;
	}
	
 	private ControllerFile addMethodLevelAnnotations(ControllerFile file){
 		int i = 0;
 		while(!file.get(i).contains("public class")) { // skippiamo fino al corpo della classe. E' necessario per evitare @RequestMapping al livello della classe.
 			i++;
 		}
 		for(; i<file.size(); i++) {
			try {
				if(file.get(i).contains("@ApiOperation")) {	//significa che il metodo e' gia' annotato quindi skippiamo fino al prossimo metodo
					while(!file.get(i).contains("@RequestMapping")){
						i++;
					}
					continue;
				}
				if(file.get(i).contains("@RequestMapping")) {
					String methodname = getMethodName(file, i);
					if(methodname == null) {
						throw new MethodNonAcceptableReturnException("stdefgen.core.addMethodLevelAnnotations(): non e' possibile trovare il nome del metodo e quindi annotarlo. Le coordinate: "+file.get(i)+" alla riga "+i);
					}
					file.set(i,modifyRequestMapValue(file.get(i)));
					file.add(i, "	@ApiOperation(value =\""+eliminateCamelCase(methodname)+"\")"); //si aggiunge una breve descrizione del metodo
					i+=2; // cosi vengono annotati tutti gli endpoint 
				}
			}
			catch(MethodNonAcceptableReturnException e) {
				System.err.println(e);
				continue;
			}
		}
		return file;
	}
	
	private ControllerFile addImports(ControllerFile file){
		String[] imports = importsToAdd();
		for(int i=0; i<file.size(); i++){
			if(file.get(i).contains("package")) {
				for(String line: imports) {
					file.add(i+1, line);
				}
				break;
			}
		}
		return file;
	}
	
	//opzionale: aggiungere il controllo quando la dichiarazione della classe si trova sulla stessa riga della annotazione @Controller	
	private ControllerFile addClassLevelAnnotations(ControllerFile file) throws MethodNonAcceptableReturnException{
		for(int i=0; i<=file.size(); i++) {
			if(file.get(i).contains("@Controller")) {	
				if(!file.get(i).contains("class")) {
					String classname = getControllerClassName(file);
					file.add(i,"@RequestMapping(\"/"+classname+"\")"); //specifichiamo il base path per i handler method di questa classe
					file.add(i,"@Api(value = \""+classname.toLowerCase()+"\")"); //aggiungiamo il tag del controller
					break;
				}
			}
			else if(i == file.size()) {
				throw new MethodNonAcceptableReturnException("stdefgen.core.addClassLevelAnnotations(): non e' stata trovata l'annotazione @Controller per il file: "+file.toString());
			}
		}
		return file;
	}
	
	//come parametro riceve la stringa contenente la annotazione @RequestMapping
	//e modifica il valore del campo 'value' sostituendo l'intero path con il path dell'endpoint
	private String modifyRequestMapValue(String mapping) {
		int[] position = getRequestMapValue(mapping);
		int index = mapping.lastIndexOf("/", position[1]);
		String value = mapping.substring(index,position[1]);
		String temp = mapping.substring(0,position[0]+1) +value+ mapping.substring(position[1]);
		return temp;
	}
	
	//trasforma il nome di un metodo. Separa il camel case, fa lowerCase di tutta la stringa e mette in upperCase la prima lettera
	private String eliminateCamelCase(String method) {
		String separated = ""+Character.toUpperCase(method.charAt(0));
		for(int pos = 1; pos<method.length(); pos++) {
			if(Character.isUpperCase(method.charAt(pos))) {
				separated = separated+" "+Character.toLowerCase(method.charAt(pos)); 
			}
			else {
				separated = separated+method.charAt(pos);
			}
		}
		return separated;
	}
	
	//restituisce il nome della classe a meno del suffisso 'Controller'
	public String getControllerClassName(ControllerFile file) {
		String name = null;
		for(String line: file) {
			if(line.contains("class")) {
				int index = line.indexOf("Controller", line.indexOf("class"));
				if(index!=-1) {
					int k = index-1;
					name = "";
					while(line.charAt(k)!=' ' && k>=0) {
						name = line.charAt(k) + name;
						--k;
					}
					break;
				}
			}
		}
		return name;
	}
	
	//la riga di partenza e' quella dove c'e' scritto @RequestMapping
	private String getMethodName(ControllerFile file, int startingLine) {
		int classnameline = -1; //il numero di riga che contiene il nome del metodo
		if(file.get(startingLine).contains("@RequestMapping")) {
			for(int i=startingLine; i<file.size(); i++) {//si cerca il numero della riga dove si trova il nome del metodo
				if(file.get(i).contains("public")) {
					String line = file.get(i);
					if(line.contains("HttpEntity<")) { //l'endpoint restituisce un oggetto di tipo HttpEntity quindi il nome del metodo dovrebbe stare sulla riga successiva
						if(file.get(i+1).startsWith(">")) { //inizio riga con '>' e' una particolarita' d ST
							classnameline = i+1; 
						}
					}
					else if(line.contains("void")) {
						if(line.contains("()") || line.contains("( )") || line.contains("(")) {
							classnameline = i;
						}
						else if (file.get(i+1).contains("(")) {
							classnameline = i+1;
						}
					}
					break;
				}
			}
			if(classnameline != -1) { //trovata la riga con il nome del metodo, dobbiamo estrarrlo dalla stringa
				String temp = file.get(classnameline);
				int index = temp.indexOf("(");
				String classname = new String();
				int t = index -1;
				if(temp.charAt(t) == ' ') {
					--t;
				}
				while(temp.charAt(t)!=' ') {
					classname = temp.charAt(t) + classname; //fondamentale il fatto che nella concatenazione la stringa classname e' il secondo addendo
					--t;
				}
				return classname;
			}
		}
		return null;
	}

	//come parametro riceve la stringa contenente la annotazione @RequestMapping
	// e restituisce l'indice di inizio e fine del contenuto del campo "value"	
	private int[] getRequestMapValue(String mapping) {
		if(mapping.contains("@RequestMapping")) {
			int value = mapping.indexOf("value");
			if(value != -1) {
				int equal = mapping.indexOf("=",value);
				if(equal != -1) {
					int virgolette = mapping.indexOf("\"",equal);
					if( virgolette != -1) {
						int virgolette2 = mapping.indexOf("\"",virgolette+1);
						if(virgolette2 != -1) {
							return new int[] {virgolette, virgolette2};
						}
					}
				}
			}
		}
		return null;
	}
	
	private String[] importsToAdd() {
		return new String[] {"import io.swagger.annotations.Api;", 
				"import io.swagger.annotations.ApiOperation;"
		};
	}
	
}
