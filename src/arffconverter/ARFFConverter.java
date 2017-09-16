package arffconverter;
import java.util.*;
import java.io.*;
/**
 *
 * @author Lizzie Herman
 */
public class ARFFConverter {
    static ArrayList<String[]> dataset = new ArrayList();
    static Hashtable<String,ArrayList<String>> descrip = new Hashtable(); 
    /*
    key is String att name , value is either arraylist of nominal values or 
    string NUMERIC
    */
    static int n = 13; // the tunable variable for nominal detection
    
    static Comparator<String> comp = new Comparator<String>(){
        public int compare(String s1, String s2){
            if(s1.matches("[0-9]+") && s2.matches("[0-9]+")){
                int i1 = Integer.parseInt(s1);
                int i2 = Integer.parseInt(s2);
                return i1 - i2;
            }
            return s1.compareTo(s2);
        }
    };
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.print("Data Set File Name: ");
            String filename = scanner.next();
            System.out.print("Is class attribute first? (y or n) ");
            boolean first = (scanner.next().equalsIgnoreCase("y"));
            readFile(filename, first);
            System.out.print("Data Set Description File Name: ");
            String desfilename = scanner.next();
            createARFFFile(filename, desfilename);
            System.out.print("\nConvert another file? (y or n) ");
            if(scanner.next().equalsIgnoreCase("n")) break;
            System.out.println();
        }
    }
    
    static void readFile(String filename, boolean first){
        BufferedReader br = null;
        String line = "";
        String csvSplitBy = ",";
        String[] split;
        //Pattern pattern = new Pattern();
        
        dataset.clear(); // make sure dataset is empty
        
        try {
            br = new BufferedReader(new FileReader(filename));
            line = br.readLine();
            while (line != null) {
                split = line.split(csvSplitBy);
                if(first){ // the class att has to be the last att
                    String c = split[0];
                    for(int i = 0; i < split.length-1; i++){
                        split[i] = split[i+1];
                    }
                    split[split.length-1] = c;
                }
                dataset.add(split);
                line = br.readLine();
            }
            br.close();
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
        
        String[] bob = dataset.get(0);
        int numVal = dataset.size();
        descrip.clear(); // make sure description is empty
        for(int c = 0; c < bob.length; c++){
            String att = bob[c];
            String attname = c + "attribute";
            if(c == bob.length - 1) attname = "class";
            ArrayList<String> noms = new ArrayList();
            if(att.matches("[a-zA-Z]+")){ // check to see if att contains letters
                for(int r = 0; r < numVal; r++){
                    String a = dataset.get(r)[c];
                    if(! noms.contains(a)){
                        noms.add(a);
                    }
                }
            } else if(att.contains(".")){ // check to see if att is double or int
                noms.clear(); // make sure noms is empty
                noms.add("NUMERIC");   
            } else { // att is an int
                for(int r = 0; r < numVal; r++){
                    String a = dataset.get(r)[c];
                    if((c == bob.length - 1 || noms.size() <= n) && ! noms.contains(a)){
                        noms.add(a);
                    }
                }
                if(!(c == bob.length - 1) && noms.size() > n){
                    noms.clear(); // make sure noms is empty
                    noms.add("NUMERIC");
                }
            }
            descrip.put(attname, noms);
        }
    }
    
    static void createARFFFile(String filename, String desfilename){
        int a = filename.indexOf('.');
        String name = filename.substring(0, a);
        try {
            BufferedReader br = new BufferedReader(new FileReader(desfilename));
            PrintWriter result = new PrintWriter(new BufferedWriter(new FileWriter(name + ".arff", false)));
            String line = br.readLine();
            result.println(line);
            result.println("%");
            result.println("% 2. Sources:");
            result.println("%\t(a) Creator: ");
            result.println("%\t(b) Donor: ");
            result.println("%\t(c) Date: ");
            result.println("%");
            result.println("@RELATION " + name);
            result.println(" ");
            for(int i = 0; i < descrip.size()-1; i++){
                ArrayList noms = descrip.get(i + "attribute");
                if(noms.size() == 1) result.println("@ATTRIBUTE " + i + "attribute\tNUMERIC");
                else{
                    noms.sort(comp);
                    result.println("@ATTRIBUTE " + i + "attribute\t" + noms.toString());
                }
            }
            ArrayList noms = descrip.get("class");
            noms.sort(comp);
            result.println("@ATTRIBUTE class\t" + noms.toString() + "\n");
            result.println(" ");
            result.println("\n\n@DATA");
            int numAtt = dataset.get(0).length;
            int numVal = dataset.size();
            for(int r = 0; r < numVal; r++){
                for(int c = 0; c < numAtt; c++){
                    result.print(dataset.get(r)[c]);
                    if(c < numAtt-1) result.print(",");
                }
                result.println("\n");
            }
            result.close();
            br.close();
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
