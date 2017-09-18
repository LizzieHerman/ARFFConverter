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
            System.out.print("Do you also have a description file? (y or n) ");
            String desfilename = "";
            if((scanner.next().equalsIgnoreCase("y"))){
                System.out.print("Data Set Description File Name: ");
                desfilename = scanner.next();
            }
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
            while (line != null){
                if(line.equals("")){ // we don't care about any blank lines
                    line = br.readLine();
                    continue;
                }
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
            String attname = (c+1) + "attribute";
            if(c == bob.length - 1) attname = "class";
            ArrayList<String> noms = new ArrayList();
            if(att.contains("[a-zA-Z]+")){ // check to see if att contains letters
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
                    //System.out.println("(" + r + "," + c + ")");
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
        String[] sources = new String[4];
        if(desfilename.equals("")) sources[0] = name;
        else{
            sources = readDescriptionFile(desfilename);
            name = desfilename.substring(0, desfilename.indexOf('.'));
        }
        try {
            PrintWriter result = new PrintWriter(new BufferedWriter(new FileWriter(name + ".arff", false)));
            result.println("% 1. Title: " + sources[0]);
            result.println("%");
            result.println("% 2. Sources:");
            result.println("%\t(a) Creator: " + sources[1]);
            result.println("%\t(b) Donor: " + sources[2]);
            result.println("%\t(c) Date: " + sources[3]);
            result.println("%");
            result.println("@RELATION " + name);
            result.println(" ");
            for(int i = 1; i < descrip.size(); i++){
                ArrayList noms = descrip.get(i + "attribute");
                if(noms.size() == 1) result.println("@ATTRIBUTE " + i + "attribute\tNUMERIC");
                else{
                    noms.sort(comp);
                    result.println("@ATTRIBUTE " + i + "attribute\t");
                    result.print("{ " + noms.get(0));
                    for(int i = 1, i < noms.size(); i++){
                        result.print(", " + noms.get(i));
                    }
                    result.println("}");
                }
            }
            ArrayList noms = descrip.get("class");
            noms.sort(comp);
            result.println("@ATTRIBUTE class\t");
            result.print("{ " + noms.get(0));
            for(int i = 1, i < noms.size(); i++){
                result.print(", " + noms.get(i));
            }
            result.println("}\n");
            
            result.println("@DATA");
            int numAtt = dataset.get(0).length;
            int numVal = dataset.size();
            for(int r = 0; r < numVal; r++){
                for(int c = 0; c < numAtt; c++){
                    result.print(dataset.get(r)[c]);
                    if(c < numAtt-1) result.print(",");
                }
                result.println("");
            }
            result.close();
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    static String[] readDescriptionFile(String desfilename){
        String title = "";
        String creator = "";
        String donor = "";
        String date = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(desfilename));
            String line = br.readLine();
            title = line.substring(line.indexOf(':')+1);
            while (line != null) { // find the line where the sources start
                line = br.readLine();
                line = line.replaceFirst("^\\s*", "");
                if(line.startsWith("2.")) break;
            }
            char source = 'a';
            while (line != null) { // collect all lines in the sources
                line = br.readLine();
                line = line.replaceFirst("^\\s*", "");
                if(line.startsWith("3.")) break; // sources end when 3 starts (Past Usage)
                if(line.equals("")) continue; // we don't care about any blank lines
                if(line.startsWith("(a)") || line.startsWith("a)")){
                    source = 'a';
                    line = line.substring(line.indexOf(')')+1);
                }else if(line.startsWith("(b)") || line.startsWith("b)")){
                    source = 'b';
                    line = line.substring(line.indexOf(')')+1);
                }else if(line.startsWith("(c)") || line.startsWith("c)")){
                    source = 'c';
                    line = line.substring(line.indexOf(')')+1);
                }
                if(line.contains(":")) line = line.substring(line.indexOf(':')+1);
                switch(source){
                    case 'a':
                        if(! creator.equals("")) creator += "\n%\t\t";
                        creator += line;
                        break;
                    case 'b':
                        if(! donor.equals("")) donor += "\n%\t\t";
                        donor += line;
                        break;
                    case 'c':
                        if(! date.equals("")) date += "\n%\t\t";
                        date += line;
                        break;
                    default:
                        break;
                }
            }
            /**
             * would continue to parse description file to find attribute names
             * but there is no common way these values are presented throughout
             * the different description files to make this possible for a 
             * generalized algorithm
             */
            br.close();
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
        String[] sources = {title, creator, donor, date};
        return sources;
    }
}
