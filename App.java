package app;
import java.net.*;
import java.io.*;

public class App {
    public static void main(String[] args) throws Exception {
        
        URL url = new URL("https://ru.wikipedia.org/wiki/%D0%93%D0%B8%D1%82%D0%BB%D0%B5%D1%80,_%D0%90%D0%B4%D0%BE%D0%BB%D1%8C%D1%84");
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

        String inputLine;
        boolean flag = false;
        int counter = 0;
        while ((inputLine = in.readLine()) != null) {
            if (inputLine.contains("id=\"content\""))
                flag = true;
            if (flag) {
                if (inputLine.contains("mw-data-after-content")) 
                    flag = false;
                int first = -1;
                String nurl = "";
                while ((first = inputLine.indexOf("<a href=", first + 1)) != -1) { //новая ссылка с first + 9
                    char c;
                    nurl = "";
                    first += 9;
                    while ((c = inputLine.charAt(first)) != '"') {
                        nurl += c;
                        first++;
                    }
                    if (nurl.indexOf("/wiki/") == 0) {
                        System.out.println("https://ru.wikipedia.org" + nurl + "\n");
                        counter++;
                    }
                }
            }  
        }
        System.out.println("Ссылок найдено: " + counter);
        in.close();
    }
}