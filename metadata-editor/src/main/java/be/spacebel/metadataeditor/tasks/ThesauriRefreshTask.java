/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.tasks;

import be.spacebel.metadataeditor.models.configuration.Configuration;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.parser.ConceptUtils;
import java.io.IOException;
import java.util.Date;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * Define a task to synchronize frequently remote thesauri files to local
 *
 * @author mng
 */
public class ThesauriRefreshTask extends TimerTask {

    private final Logger LOG = Logger.getLogger(getClass());
    private final Configuration config;

    public ThesauriRefreshTask(Configuration config) {
        this.config = config;
    }

    @Override
    public void run() {
        try {
            LOG.debug("Start loading new thesauri at: " + CommonUtils.dateTimeToStr(new Date()));
            ConceptUtils.loadConcepts(config, false);
            LOG.debug("End loading new thesauri at: " + CommonUtils.dateTimeToStr(new Date()));
        } catch (IOException | SAXException e) {
            LOG.error("Error while refreshing thesauri: " + e);
        }
    }

}
