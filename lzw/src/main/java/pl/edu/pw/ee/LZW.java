package pl.edu.pw.ee;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class LZW {

    String pathToDir;
    ArrayList<String> dictionary;
    Integer[] text;

    public LZW(String pathToDir) {
        if (pathToDir == null || pathToDir.isEmpty()) {
            throw new IllegalArgumentException("Path to directory cannot be null!");
        }
        this.pathToDir = pathToDir;
    }

    public void compress() {
        String pathToFile = pathToDir + "decompressedFile.txt";
        this.text = readDecompressedFile(pathToFile);
        initializeDictionaryFromIntegers();
        byte[] dictionaryBytes = dictionaryIntoBytes();
        writeBytes(dictionaryBytes, "DICTIONARY");
        ArrayList<Integer> output = LZWCompressingAlgorithm();
        byte[] outputBytes = outputIntoBytes(output);
        writeBytes(outputBytes, "OUTPUT");
    }

    public void decompress() {
        byte[] dictionaryBytes = readBytes("DICTIONARY");
        initializeDictionaryFromBytes(dictionaryBytes);
        byte[] inputBytes = readBytes("INPUT");
        int[] input =  bytesIntoIntegers(inputBytes);
        String[] output = LZWDecompressingAlgorithm(input);
        String finalString = integerIntoString(output);
        writeFinalString(finalString);
    }

    private String[] LZWDecompressingAlgorithm(int[] input) {
        ArrayList<String> textList = new ArrayList<>();
        int currentWordId, prevWordId;
        String currentWord, prevWord, combined;
        prevWordId = input[0];
        prevWord = dictionary.get(prevWordId);
        textList.add(prevWord);
        for (int i = 1; i < input.length; i++) {
            currentWordId = input[i];
            if (dictionary.size() >= currentWordId) {
                currentWord = dictionary.get(currentWordId);
                prevWord = dictionary.get(prevWordId);
                combined = prevWord + " " + currentWord;
                dictionary.add(combined);
                textList.add(currentWord);
            } else {
                prevWord = dictionary.get(prevWordId);
                combined = prevWord;
                textList.add(combined);
            }
            prevWordId = currentWordId;
        }
        String[] text = new String[input.length];
        for (int i = 0; i < textList.size(); i++) {
            text[i] = textList.get(i);
        }
        return text;
    }

    private ArrayList<Integer> LZWCompressingAlgorithm() {
        String current, prev, combined;
        ArrayList<Integer> output = new ArrayList<>();
        current = Integer.toString(text[0]);
        prev = current;
        for (int i = 1; i < text.length; i++) {
            current = Integer.toString(text[i]);
            combined = prev + " " + current;
            if (dictionary.contains(combined)) {
                prev = prev + " " + current;
            } else {
                dictionary.add(combined);
                output.add(dictionary.indexOf(prev));
                prev = current;
            }
        }
        output.add(dictionary.indexOf(prev));
        return output;
    }

    private Integer[] readDecompressedFile(String pathToDecompressedFile) {
        try {
            File file = new File(pathToDecompressedFile);
            if (!file.exists() || file.isDirectory()) {
                throw new IllegalArgumentException("The file must exists/path cannot lead to a directory!");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
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

    private void writeBytes(byte[] bytes, String string) {
        File file;
        if (string.equals("DICTIONARY")) {
            file = new File(pathToDir + "dictionary.txt");
        } else if (string.equals("OUTPUT")) {
            file = new File(pathToDir + "compressedFile.txt");
        } else {
            throw new IllegalArgumentException("Method should be called with an argument DICTIONARY or INPUT");
        }
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(bytes);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    private byte[] readBytes(String string) {
        try { 
            Path path;
            if (string.equals("DICTIONARY")) {
                path = Paths.get(pathToDir + "dictionary.txt");
            } else if (string.equals("INPUT")) {
                path = Paths.get(pathToDir + "compressedFile.txt");
            } else {
                throw new IllegalArgumentException("Method should be called with an argument DICTIONARY or INPUT");
            }
            byte[] bytes = Files.readAllBytes(path);
            return bytes;
        } catch (IOException e) {
            System.out.println("Exception: " + e);
            return null;
        }
    }

    private void initializeDictionaryFromIntegers() {
        this.dictionary = new ArrayList<>();
        for (int i = 0; i < text.length; i++) {
            if (!dictionary.contains(Integer.toString(text[i]))) {
                dictionary.add(Integer.toString(text[i]));
            }
        }
    }

    private void initializeDictionaryFromBytes(byte[] bytes) {
        this.dictionary = new ArrayList<>();
        for (int i = 0; i < bytes.length; i++) {
            int value = (int) bytes[i];
            dictionary.add(Integer.toString(value));
        }
    }

    private byte[] dictionaryIntoBytes() {
        byte[] bytes = new byte[dictionary.size()];
        for (int i = 0; i < bytes.length; i++) {
            int value = Integer.parseInt(dictionary.get(i));
            bytes[i] = (byte) value;
        }
        return bytes;
    }

    private byte[] outputIntoBytes(ArrayList<Integer> output) {
        byte[] bytes = new byte[output.size()];
        for (int i = 0; i < output.size(); i++) {
            int value = output.get(i);
            bytes[i] = (byte) value;
        }
        return bytes;
    }

    private int[] bytesIntoIntegers(byte[] bytes)  {
        int[] table = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            int value = (int) bytes[i];
            while (value < 0) {
                value += 256;
            }
            table[i] = value;
        }
        return table;
    }

    private String integerIntoString(String[] strings) {
        String finalString = new String();
        for (int i = 0; i < strings.length; i++) {
            String[] string = strings[i].split("\\s+");
            for (int j = 0; j < string.length; j++) {
                int value = Integer.parseInt(string[j]);
                finalString = finalString + (char) value; 
            }
        }
        return finalString;
    }

    private void writeFinalString(String finalString) {
        try (PrintWriter out = new PrintWriter(pathToDir + "decompressedFile.txt")) {
            out.print(finalString);
        } catch (IOException e) {
            System.err.println("Problem with the decompressed file!");
        }
    }
}
