package de.hdm.faceCapture;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class fileReader {
	
	// Reader
	FileReader fr;
	BufferedReader br;
	
	// Writer
	BufferedWriter output;
	
	// Line Variables
	String line;
	String last = null;
	
	
	// Get the Id of the last image
	public int getImageId() {
		int imageId = 0;
		
		try {
			fr = new FileReader("media/test/test.txt");
			br = new BufferedReader(fr);

			while ((line = br.readLine()) != null) {
				// System.out.println(zeile);
				last = line;
			}
		
			imageId = Integer.parseInt(last.split("\\-")[0]);
			// System.out.println(imageId);
		
			fr.close();
		}
		catch (IOException e) {
			System.out.println("Fehler beim Lesen der Datei ");
			System.out.println(e.toString());
		}	
		return imageId;
	}
	

	// Get Name 
	public String getName(int id) {
		int tempId = 0;
		try {
			fr = new FileReader("media/test/test.txt");
			br = new BufferedReader(fr);
			
			while (((line = br.readLine()) != null) && (tempId < id)) {
				last = line;
				tempId++;
			}
		}
		catch (IOException e) {
			System.out.println("Fehler bei Suche");
			System.out.println(e.toString());
		}
		String[] tokens1 = last.split("-");
		String name = tokens1[1];	
		return name;
	}
	
	// Add new Entry
	public void addEntry(String name) {
		try {
			output = new BufferedWriter(new FileWriter("media/test/test.txt", true));
			output.newLine();
			output.append(name);
			output.close();
		} 
		catch (IOException e) {
			System.out.println("Fehler beim Schreiben in Datei");
			System.out.println(e.toString());
		}
	}
	
}
