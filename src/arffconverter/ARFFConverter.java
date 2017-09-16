package arffconverter;
import java.util.*;
import java.io.*;
/**
 *
 * @author Lizzie Herman
 */
public class ARFFConverter {
    static ArrayList dataset = new ArrayList();
    /*
    The ArrayList dataset has flipped notation when calling dataset.get(i) you 
    are getting the ith col not row so it gives you an entire attribute column 
    you have to use dataset.get(i)[j] to get the value in the 
    table at (j,i) jth row ith col
    */
    static Hashtable<String,ArrayList<String>> descrip = new Hashtable(); 
    /*
    key is String att name , value is either arraylist of nominal values or 
    string NUMERIC
    */
    static int n = 10; // the tunable variable for nominal detection
                    

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Data Set File Name: ");
        String filename = scanner.next();
        System.out.print("Is class attribute first? (y or n) ");
        boolean first = (scanner.next().equalsIgnoreCase("y"));
        readFile(filename, first);
        //System.out.print("Data Set Description File Name: ");
        //String desfilename = scanner.next();
        //createARFFFile(filename);
        
    }
    
    static void readFile(String filename, boolean first){
        BufferedReader br = null;
        String line = "";
        String csvSplitBy = ",";
        String[] split;
        //Pattern pattern = new Pattern();
        
        ArrayList<String[]> olddataset = new ArrayList();
        
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
                olddataset.add(split);
                line = br.readLine();
            }
            br.close();
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
        
        String[] bob = olddataset.get(0);
        int numVal = olddataset.size();
        dataset.clear(); // make sure dataset is empty
        descrip.clear(); // make sure description is empty
        for(int i = 0; i < bob.length; i++){
            String att = bob[i];
            String attname = i + "attribute";
            if(i == bob.length - 1) attname = "class";
            ArrayList<String> noms = new ArrayList();
            if(att.matches("[a-zA-Z]+")){ // check to see if att contains letters
                String[] x = new String[numVal];
                for(int j = 0; j < x.length; j++){
                    String a = olddataset.get(j)[i];
                    x[j] = a;
                    if(! noms.contains(a)){
                        noms.add(a);
                    }
                }
                dataset.add(x);
            } else if(att.contains(".")){ // check to see if att is double or int
                double[] x = new double[numVal];
                for(int j = 0; j < x.length; j++){
                    String a = olddataset.get(j)[i];
                    x[j] = Double.parseDouble(a);
                }
                dataset.add(x);
                noms.clear(); // make sure noms is empty
                noms.add("NUMERIC");   
            } else { // att is an int
                int[] x = new int[numVal];
                for(int j = 0; j < x.length; j++){
                    String a = olddataset.get(j)[i];
                    x[j] = Integer.parseInt(a);
                    if(noms.size() <= n && ! noms.contains(a)){
                        noms.add(a);
                    }
                }
                dataset.add(x);
                if(noms.size() > n){
                    noms.clear(); // make sure noms is empty
                    noms.add("NUMERIC");
                }
            }
            descrip.put(attname, noms);
        }
    }
    
}
