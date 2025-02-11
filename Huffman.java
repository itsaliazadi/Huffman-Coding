import java.util.List;
import java.util.HashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

class HuffmanNode {

    char character;
    int frequency;
    HuffmanNode left;
    HuffmanNode right;

    public HuffmanNode(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
    }

}

public class huffman {

    private static final char NEWLINE_SYMBOL = '\uFFFF';
    private static final char ESCAPE_SYMBOL = '\uFFFE';

    public static List<String> readFile(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }


    public static HashMap<Character, Integer> countCharacterFrequency(List<String> lines) {
        HashMap<Character, Integer> frequencyMap = new HashMap<>();
        for (String line : lines) {
            for (char c : line.toCharArray()) {
                frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
            }
            frequencyMap.put(NEWLINE_SYMBOL, frequencyMap.getOrDefault(NEWLINE_SYMBOL, 0) + 1);
        }
        return frequencyMap;
    }


    public static HuffmanNode buildHuffmanTree(HashMap<Character, Integer> frequencyMap) {
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(node -> node.frequency));
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            priorityQueue.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }
        while (priorityQueue.size() > 1) {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();
            HuffmanNode parent = new HuffmanNode('\0', left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;
            priorityQueue.add(parent);
        }
        return priorityQueue.poll();
    }


    public static Map<Character, String> generateHuffmanCodes(HuffmanNode root) {
        Map<Character, String> huffmanCodes = new HashMap<>();
        generateCodesHelper(root, "", huffmanCodes);
        return huffmanCodes;
    }


    public static void generateCodesHelper(HuffmanNode root, String code, Map<Character, String> huffmanCodes) {
        if (root.left == null && root.right == null) {
            huffmanCodes.put(root.character, code);
            return;
        }
        generateCodesHelper(root.left, code + "0", huffmanCodes);
        generateCodesHelper(root.right, code + "1", huffmanCodes);
    }

    public static String encode(List<String> lines, Map<Character, String> huffmanCodes) {
        StringBuilder encodedString = new StringBuilder();
        int numberOfLines = lines.size();

        for (int i = 0; i < numberOfLines; i++) {
            String line = lines.get(i);
            for (char c : line.toCharArray()) {
                String code = huffmanCodes.get(c);
                if (code != null) {
                    encodedString.append(code);
                }
            }

            if (i < numberOfLines - 1) {
                String newlineCode = huffmanCodes.get(NEWLINE_SYMBOL);
                if (newlineCode != null) {
                    encodedString.append(newlineCode);
                }
            }
        }

        return encodedString.toString();
    }

    public static void writeEncodedFile(String encodedString, Map<Character, String> huffmanCodes) {
        String encodedFilePath = "C:\\Users\\User\\Desktop\\Final Project\\encodedText.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(encodedFilePath))) {
            writer.write(encodedString);
            writer.newLine();
            writer.write("#");
            writer.newLine();
            for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
                char key = entry.getKey();
                String stringKey = String.valueOf(key);
                if (key == ':') {
                    stringKey = String.valueOf(ESCAPE_SYMBOL);
                } else if (key == NEWLINE_SYMBOL){
                    stringKey = ""; 

                }

                writer.write(stringKey + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }


    public static void writeByteEncodedFile(String encodedString, Map<Character, String> huffmanCodes) {
        String encodedFilePath = "C:\\Users\\User\\Desktop\\Final Project\\byteEncodedText.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(encodedFilePath))) {

            for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
                char key = entry.getKey();
                String keyStr;
                
                if (key == NEWLINE_SYMBOL) {
                    keyStr = "";  
                } else if (key == ':') {
                    keyStr = String.valueOf(ESCAPE_SYMBOL);  
                } else {
                    keyStr = String.valueOf(key); 
                }
                
                writer.write(keyStr + ":" + entry.getValue());
                writer.newLine();
            }

                byte[] bytes = convertEncodedStringToBytes(encodedString);
                int padding = (8 - (encodedString.length() % 8)) % 8;
                writer.write("###PAD=" + padding); 
                writer.newLine();
                writer.close(); 
                
                Files.write(Paths.get(encodedFilePath), bytes, StandardOpenOption.APPEND);

        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }
 
        private static byte[] convertEncodedStringToBytes(String encodedString) {
            int bitLength = encodedString.length();
            int padding = (8 - (bitLength % 8)) % 8;
            StringBuilder sb = new StringBuilder(encodedString);
            for (int i = 0; i < padding; i++) {
                sb.append('0');
            }
            String padded = sb.toString();
            
            byte[] bytes = new byte[padded.length() / 8];
            for (int i = 0; i < bytes.length; i++) {
                String byteStr = padded.substring(i * 8, (i + 1) * 8);
                bytes[i] = (byte) Integer.parseUnsignedInt(byteStr, 2);
            }
            return bytes;
    }


    public static String decode(String encodedString, Map<String, Character> huffmanCodes) {
        StringBuilder decodedString = new StringBuilder();
        StringBuilder currentCode = new StringBuilder();

        for (char bit : encodedString.toCharArray()) {
            currentCode.append(bit);
            if (huffmanCodes.containsKey(currentCode.toString())) {
                char decodedChar = huffmanCodes.get(currentCode.toString());
                if (decodedChar == NEWLINE_SYMBOL) {
                    decodedString.append(System.lineSeparator());
                } else {
                    decodedString.append(decodedChar);
                }
                currentCode.setLength(0); 
            }
        }

        return decodedString.toString();
    }



   public static void decodeFromFile(String filePath, String outputFilePath) throws IOException {
        List<String> lines = readFile(filePath);
        StringBuilder encodedString = new StringBuilder();
        Map<String, Character> huffmanCodes = new HashMap<>();

        boolean foundHash = false;
        for (String line : lines) {
            if (line.equals("#")) {
                foundHash = true;
                continue;
            }
            if (!foundHash) {
                encodedString.append(line);
            } 
            else {
                String[] parts = line.split(":");
                if (parts.length == 2) {

                    String keyString = parts[0];
                    char character;

                   if (keyString.equals(String.valueOf(ESCAPE_SYMBOL))) {
                        character = ':';
                    } 
                    else if(keyString.isEmpty()){
                        character = NEWLINE_SYMBOL;
                    }
                    else {
                        character = parts[0].charAt(0);
                    }
                    huffmanCodes.put(parts[1], character);
                }
            }
        }

        String decodedString = decode(encodedString.toString(), huffmanCodes);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write(decodedString);
        } catch (IOException e) {
            System.err.println("Error writing the file: " + e.getMessage());
        }
    }

   public static void decodeFromByteFile(String filePath, String outputFilePath) throws IOException {

        byte[] allBytes = Files.readAllBytes(Paths.get(filePath));

        String content = new String(allBytes, StandardCharsets.ISO_8859_1);
        String[] lines = content.split("\n");
        
        // Find the separator line with padding info
        int separatorIndex = -1;
        int padding = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith("###PAD=")) {
                separatorIndex = i;
                String[] parts = lines[i].split("=");
                padding = Integer.parseInt(parts[1].trim());
                break;
            }
        }
        
        if (separatorIndex == -1) {
            throw new IOException("Invalid encoded file: separator not found.");
        }
        
        // Parse Huffman codes
        Map<String, Character> huffmanCodes = new HashMap<>();
        for (int i = 0; i < separatorIndex; i++) {
            String line = lines[i];
            String[] parts = line.split(":", 2);
            if (parts.length != 2) continue;
            
            String keyStr = parts[0];
            String code = parts[1].trim();
            char key;
            if (keyStr.isEmpty()) {
                key = NEWLINE_SYMBOL;
            } else if (keyStr.charAt(0) == ESCAPE_SYMBOL) {
                key = ':';
            } else {
                key = keyStr.charAt(0);
            }
            huffmanCodes.put(code, key);
        }
        
        // Calculate binary data start position
        int binaryStart = 0;
        for (int i = 0; i <= separatorIndex; i++) {
            binaryStart += lines[i].length() + 1; // +1 for newline character
        }
        
        // Extract binary data
        byte[] binaryData = Arrays.copyOfRange(allBytes, binaryStart, allBytes.length);
        
        // Convert bytes to bit string
        StringBuilder bitString = new StringBuilder();
        for (byte b : binaryData) {
            bitString.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        
        // Remove padding bits
        if (padding > 0) {
            bitString.setLength(bitString.length() - padding);
        }
        
        // Decode and write result
        String decodedString = decode(bitString.toString(), huffmanCodes);
        Files.write(Paths.get(outputFilePath), decodedString.getBytes());
}

    public static void main(String[] args) {
        try {
            String filePath = "C:\\Users\\User\\Desktop\\Final Project\\textFile.txt";

            List<String> lines = readFile(filePath);
            if (lines.isEmpty()) {
                System.err.println("The input file is empty.");
                return;
            }

            HashMap<Character, Integer> frequencyMap = countCharacterFrequency(lines);

            HuffmanNode huffmanHead = buildHuffmanTree(frequencyMap);

            Map<Character, String> codes = generateHuffmanCodes(huffmanHead);

            String encodedString = encode(lines, codes);

            writeEncodedFile(encodedString, codes);
            writeByteEncodedFile(encodedString, codes);

            decodeFromFile("C:\\Users\\User\\Desktop\\Final Project\\encodedText.txt", "C:\\Users\\User\\Desktop\\Final Project\\decodedText.txt");
            // decodeFromByteFile("C:\\Users\\User\\Desktop\\Final Project\\byteEncodedText.txt", "C:\\Users\\User\\Desktop\\Final Project\\byteDecodedText.txt");

        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
}