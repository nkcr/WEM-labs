package ch.mse.hesso.webmining.solrbridge;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.List;

public class Indexer {

    private SolrClient solr;

    public Indexer(SolrClient solr){
        this.solr = solr;
    }

    public synchronized void index(SolrInputDocument document){
        try {
            solr.add(document);
            solr.commit();
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void index(List<SolrInputDocument> documents){
        try {
            if(documents.size() > 0) {
                solr.add(documents);
                solr.commit();
            }
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
