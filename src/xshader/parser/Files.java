package xshader.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;


public class Files {

	public static String readFile(File file){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			String source = "";
			while((line=reader.readLine())!=null){
				source += line+"\n";
			}
			reader.close();
			return source;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void saveFile(File file, String content){
		try {
			PrintStream ps = new PrintStream(file);
			ps.print(content);
			ps.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
