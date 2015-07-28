/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eina.ws;

import eina.ontology.beans.SKOSCategory;
import eina.utils.XMLutil;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author peonza
 */
@WebService(serviceName = "DBService")
public class DBService {

    @WebMethod(operationName = "searchKeywords")
    public String searchKeywords(@WebParam(name = "keywords") String kws
            , @WebParam(name = "categoryLabel") String catName) {
        System.out.println("WEBSERVICE: Llamada a searchKeywords");
        try {
            return ServiceSingleton.getInstance().searchKeywords(kws, SKOSCategory.PREFIX+catName);
        } catch (Exception ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
            return "error";
        }
    }
    
    @WebMethod(operationName = "getRootCategories")
    public String getRootCategories() {
        return XMLutil.categorySetToXml(ServiceSingleton.getInstance().getRootCategories());
    }
    
    @WebMethod(operationName = "getRelated")
    public String searchRelated(@WebParam(name = "URI") String uri) {
        return ServiceSingleton.getInstance().searchRelated(uri);
    }
    
    @WebMethod(operationName = "searchRelatedWithSource")
    public String searchRelatedWithSource(@WebParam(name = "URI") String uri
            , @WebParam(name = "source") String source) {
        return ServiceSingleton.getInstance().searchRelatedWithSource(uri,source);
    }
    
    @WebMethod(operationName = "searchRelatedWithTarget")
    public String searchRelatedWithTarget(@WebParam(name = "URI") String uri
            , @WebParam(name = "target") String target) {
        return ServiceSingleton.getInstance().searchRelatedWithTarget(uri,target);
    }
    
    @WebMethod(operationName = "getSubCategories")
    public String getSubCategories(@WebParam(name = "categoryName") String categoryName) {
        return XMLutil.categorySetToXml(ServiceSingleton.getInstance().getSubCategories(categoryName));
    }
    
}
