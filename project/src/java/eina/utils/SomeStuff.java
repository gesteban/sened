
package eina.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 *
 * @author Guillermo Esteban
 */
public class SomeStuff {
    
    private static class ServiceSingletonHolder {

        public static final SomeStuff instance = new SomeStuff();
    }

    public static SomeStuff getInstance() {
        return ServiceSingletonHolder.instance;
    }

    private SomeStuff() {
    }
    
    ////////////////////////////////////////
    
    /*
    private static String readFile(String fileName) throws IOException {
        String path = System.getProperty("user.dir") + System.getProperty("file.separator") + fileName;
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }
    */
    
    public String readFile(String fileName) throws IOException {
        InputStream in = getClass().getResourceAsStream(fileName);
        Reader fr = new InputStreamReader(in, "utf-8");
        
        StringBuilder fileData = new StringBuilder(1000);
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=fr.read(buf)) != -1){
            fileData.append(buf, 0, numRead);
        }
        return fileData.toString();
    }
    
}
