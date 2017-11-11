import java.util.Scanner;

/**
 * Created by cemil on 12.09.2017.
 */
public class Main {
    public static void main(String[] args){
        Scanner in = new Scanner(System.in);
        if(args.length<2){
            System.out.println("You must give the settings file location as parameter");
        }
        else{
            try {
                String type = args[0];
                String fileLocation = args[1];

                if(type.equals("abox")) {
                    CsvCreator filter = new CsvCreator();
                    filter.filterDbpedia(fileLocation);
                }
                else{
                    TboxCsvCreator filter = new TboxCsvCreator();
                    filter.filterDbpedia(fileLocation);
                }
            }
            catch(Exception ex){
                ex.printStackTrace(System.out);
            }

        }
        System.out.println("Press any key to continue...");
        in.next();
    }
}
