package ch.mse.hesso.webmining;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MapSolrParams;

import java.util.HashMap;
import java.util.Map;

public class Querying {


    public static void main(String[] args) {


        //Instantiate solr client
        SolrClient solrClient = initSolorClient(CONNECTION.SORL_BASE_URL+CONNECTION.SORL_CORE);

        SolrDocumentList documents =  applyQuery(solrClient, "name:\"castella alexandre\"");
        displayResults(documents, new String[]{"name", "address", "phone"});

    }

    private static SolrDocumentList applyQuery(SolrClient solrClient, String query){
        SolrDocumentList documents = null;
        Map<String, String> queryParamMap = new HashMap<String, String>();
        queryParamMap.put("q", query);
        queryParamMap.put("fl", "*, score");
        MapSolrParams queryParams = new MapSolrParams(queryParamMap);
        try {
            final QueryResponse response = solrClient.query(queryParams);
            documents = response.getResults();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return documents;
    }


    private static void displayResults(SolrDocumentList documents, String[] fieldnames){
        for (SolrDocument document : documents) {
            for(String fieldname: fieldnames){
                System.out.printf("%s : %s - score: %f\n", fieldname, document.getFieldValue(fieldname), document.getFieldValue("score"));
            }
            System.out.println("");
        }
    }

    private static SolrClient initSolorClient(String urlString) {
        return new HttpSolrClient.Builder(urlString).build();
    }
}
