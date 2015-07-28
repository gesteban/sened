
package eina.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Clase simple tipo Singleton para el manejo de logs, existen 4 tipos de 
 * comportamientos ({@link LogBehaviour}):
 * <ul>
 *  <li> DEBUG: Permite el paso de todos los mensajes
 *  <li> INFO: Filtra los mensajes de debug menos importantes
 *  <li> WARNINGS: Solo pasan los mensajes de warning o error
 *  <li> FILE: Como DEBUG, pero los mensajes son mandados a un fichero
 * </ul>
 * @author Guillermo Esteban Pérez
 */
public final class Log {
    
    public enum LogBehaviour {
        DEBUG, INFO, WARNINGS, FILE;
    }
    
    private LogBehaviour behaviour;
    private FileWriter fstream;
    private BufferedWriter out;
    
    private static Log instance = new Log();

    private Log () {
        try {
            this.behaviour = LogBehaviour.DEBUG;
            this.fstream = new FileWriter("Log.txt");
            this.out = new BufferedWriter(fstream);
        } catch (IOException ex) {
            debug("<Log:Log> IOException, no se pudo abrir fichero");
            ex.printStackTrace();
        }
    }
    
    public static Log getLog() {
        return instance;
    }
    
    public void setLogBehaviour(LogBehaviour thisBehaviour) {
        behaviour = thisBehaviour;
    }
    
    public void closeStream () {
        try {
            fstream.close();
        } catch (IOException ex) {
            error("<Log:closeStream> IOException, no se pudo cerrar el fichero");
            ex.printStackTrace();
        }
    }
    
    public void debug (String str) {
        try {
            switch(behaviour) {
                case DEBUG:
                    System.out.println("DEBUG "+str);
                    break;
                case FILE:
                    out.write("DEBUG "+str+"\n");
            }
        } catch (IOException ex) {
            error("<Log:debug> IOException, no se pudo escribir en fichero");
            this.behaviour = LogBehaviour.DEBUG;
            ex.printStackTrace();
        }
    }
    
    public void info (String str) {
        try {
            switch(behaviour) {
                case DEBUG:
                case INFO:
                    System.out.println("INFO "+str);
                    break;
                case FILE:
                    out.write("DEBUG "+str+"\n");
            }
        } catch (IOException ex) {
            error("<Log:info> IOException, no se pudo escribir en fichero");
            this.behaviour = LogBehaviour.DEBUG;
            ex.printStackTrace();
        }
    }
    
    public void warning (String str) {
        try {
            switch(behaviour) {
                case DEBUG:
                case INFO:
                case WARNINGS:
                    System.out.println("INFO "+str);
                    break;
                case FILE:
                    out.write("DEBUG "+str+"\n");
            }
        } catch (IOException ex) {
            error("<Log:warning> IOException, no se pudo escribir en fichero");
            this.behaviour = LogBehaviour.DEBUG;
            ex.printStackTrace();
        }
    }
    
    
    public void error (String str) {
        try {
            switch(behaviour) {
                case FILE:
                    out.write("ERROR "+str+"\n");
                default:
                    System.err.println("ERROR "+str);
            }
        } catch (IOException ex) {
            error("<Log:error> IOException, no se pudo escribir en fichero");
            this.behaviour = LogBehaviour.DEBUG;
            ex.printStackTrace();
        }
    }
    
}
