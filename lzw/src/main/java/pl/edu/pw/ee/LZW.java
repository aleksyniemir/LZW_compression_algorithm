package pl.edu.pw.ee;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class LZW {

    String pathToDir;
    HashMap<DictKey, Integer> dictionary;
    Integer[] text;
    int freePosition = 256;

    public LZW(String pathToDir) {
        if (pathToDir == null || pathToDir.isEmpty()) {
            throw new IllegalArgumentException("Path to directory cannot be null!");
        }
        this.pathToDir = pathToDir;
    }

    public void compress() {
        this.text = readDecompressedFile();
        initializeDictionary();
        dictionaryBytes();
        ArrayList<Integer> output = LZWCompressingAlgorithm();
        writeOutputIntoBytes(output);
    }

    public void decompress() {
            initializeDictionary();
            int [] input = readFile();   
            String finalString = LZWDecompressingAlgorithm(input);
            writeFinalString(finalString);
    }

    private Integer[] readDecompressedFile() {
        try {
            String path = pathToDir + "decompressedFile.txt";
            File file = new File(path);
            if (!file.exists() || file.isDirectory()) {
                throw new IllegalArgumentException("The file must exists/path cannot lead to a directory!");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            int n = 0, c;
            while((c = reader.read()) != -1) {
                n++;
            }
            reader.close();
            this.text = new Integer[n];
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            int i = 0;
            while((c = reader.read()) != -1) {
                text[i] = c;;
                i++;
            }
            reader.close();
            return text;
        } catch (IOException e) {
            System.err.println("Problem with the decompressed file!");
            return null;
        }
    }

    private void initializeDictionary() {
        this.dictionary = new HashMap<DictKey, Integer>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(new DictKey(-1, (byte) i), i);
        }
        
    }

    private void dictionaryBytes() {
        byte[] buffer = new byte[3];
        boolean onLeft = true;
        File file = new File(pathToDir + "dictionary.txt");
        try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            for (int i = 0; i < dictionary.size(); i++) {
                int value = dictionary.get(new DictKey(i, (byte) 0));
                String valueInBites = new String();
                while (value > 0) {
                    if (value % 2 == 1) {
                        valueInBites = "1" + valueInBites;
                    } else {
                        valueInBites = "0" + valueInBites;
                    }
                    value = value / 2;
                }
                while (valueInBites.length() < 12) {
                    valueInBites = "0" + valueInBites;
                }
                if (onLeft) {
                    buffer[0] = (byte) Integer.parseInt(valueInBites.substring(0, 8), 2);
                    buffer[1] = (byte) Integer.parseInt(valueInBites.substring(8, 12) + "0000", 2);
                } else {
                    buffer[1] += (byte) Integer.parseInt(valueInBites.substring(0, 4), 2);
                    buffer[2] = (byte) Integer.parseInt(valueInBites.substring(4, 12), 2);
                }
                if (!onLeft || (dictionary.size() % 2 == 1 && i == dictionary.size() - 1)) {
                    for (int j = 0; j < buffer.length; j++) {
                        os.writeByte(buffer[j]);
                        buffer[j] = 0;
                    }
                }
                onLeft = !onLeft;
            }
        } catch (Exception e) {
            System.out.println("Exception " + e );
        }
    }

    private ArrayList<Integer> LZWCompressingAlgorithm() {
        ArrayList<Integer> output = new ArrayList<>();
        DictKey combined, prev;
        int value = text[0];
        byte current,  by = (byte) value;
        int prevId = dictionary.get(new DictKey(-1, by)); 
        for (int i = 1; i < text.length; i++) {
            value = text[i];
            current = (byte) value;
            combined = new DictKey(prevId, current);
            if (dictionary.containsKey(combined)) {
                prevId = dictionary.get(combined);
            } else {
                dictionary.put(combined, freePosition);
                freePosition++;
                output.add(prevId);
                prev = new DictKey(-1, current);
                prevId = dictionary.get(prev);
            }
        }
        output.add(prevId);
        return output;
    }
    
    private void writeOutputIntoBytes(ArrayList<Integer> output) {
        File file = new File(pathToDir + "compressedFile.txt");
        try (BufferedWriter os = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < output.size(); i++) {
                int value = output.get(i);
                String valueInBites = new String();
                while (value > 0) {
                    if (value % 2 == 1) {
                        valueInBites = "1" + valueInBites;
                    } else {
                        valueInBites = "0" + valueInBites;7
                    }
                    value = value / 2;
                }
                while (valueInBites.length() < 12) {
                    valueInBites = "0" + valueInBites;
                }
                os.write(valueInBites);
            }
        } catch (Exception e) {
            
        }
    }
    
    private int[] readFile() {
        try { 
            Path path = Paths.get(pathToDir + "compressedFile.txt");
            String str = Files.readString(path);
            int[] values = new int[str.length() / 12];
            for (int i = 0; i < values.length; i++) {
                String temp;
                int value  = 0;
                temp = str.substring(0 + 12 * i , 12 + 12 * i);
                for (int j = 0; j < 12; j++) {
                    value += temp.charAt(j) == '0' ? 0 : Math.pow(2, 11 - j);
                }
                values[i] = value;
            }
            return values;
        } catch (IOException e) {
            System.out.println("Exception: " + e);
            return null;
        }
    }

    private String LZWDecompressingAlgorithm(int[] input) {
        String finalString = "";
        int prevWordId = input[0];
        int currentWordId;
        char prevWord = (char) prevWordId;
        finalString += (char) prevWord;
        for (int i = 1; i < input.length; i++) {
            currentWordId = input[i];
            if (dictionary.containsValue(currentWordId)) {
                finalString += getWord(currentWordId);
                dictionary.put(new DictKey(prevWordId, (byte) getFirstByte(currentWordId)), freePosition);
                freePosition++;
                prevWordId = currentWordId;
            } else {
                finalString += getWord(prevWordId) + getFirstByte(prevWordId);
                dictionary.put(new DictKey(prevWordId,  (byte) getFirstByte(currentWordId)), freePosition);
                freePosition++;
                prevWordId = currentWordId;
            }
        }
        return finalString;
    }

    private String getWord(int currentWordId) {
        String string = new String();
        for (HashMap.Entry<DictKey, Integer> entry : dictionary.entrySet()) {
            if (Objects.equals(entry.getValue(), currentWordId)) {
                while (entry.getKey().key != -1) {
                    string = (char) entry.getKey().by + string ;
                    if (dictionary.containsValue(entry.getKey().key)) {
                        for (HashMap.Entry<DictKey, Integer> entryTwo : dictionary.entrySet()) {
                            if (Objects.equals(entryTwo.getValue(), entry.getKey().key)) {
                                entry = entryTwo;
                            }
                        }
                    }
                }
                string = (char) entry.getKey().by + string ;
            }
        }
        return string;
    }

    private int getFirstByte(int currentWordId) {
        byte by = 0;
        for (HashMap.Entry<DictKey, Integer> entry : dictionary.entrySet()) {
            if (Objects.equals(entry.getValue(), currentWordId)) {
                while (entry.getKey().key != -1) {
                    if (dictionary.containsValue(entry.getKey().key)) {
                        for (HashMap.Entry<DictKey, Integer> entryTwo : dictionary.entrySet()) {
                            if (Objects.equals(entryTwo.getValue(), entry.getKey().key)) {
                                entry = entryTwo;
                            }
                        }
                    }
                }
                by =  entry.getKey().by;
            }
        }

        return by;
    }

    private void writeFinalString(String finalString) {
        try (PrintWriter out = new PrintWriter(pathToDir + "decompressedFile.txt")) {
            out.print(finalString);
        } catch (IOException e) {
            System.err.println("Problem with the decompressed file!");
        }
    }
}

