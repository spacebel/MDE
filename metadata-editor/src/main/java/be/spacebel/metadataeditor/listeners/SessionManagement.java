/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.listeners;

import be.spacebel.metadataeditor.business.Constants;
import java.io.File;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * This class implements the HttpSessionListener interface to clean workspace of
 * anonymous user when his session is expired
 *
 * @author mng
 */
@WebListener
public class SessionManagement implements HttpSessionListener {

    private final Logger log = Logger.getLogger(getClass());

    @Override
    public void sessionCreated(HttpSessionEvent hse) {
        HttpSession session = hse.getSession();
        log.debug("Created session id: " + session.getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent hse) {
        String anonymousWorkingDir = (String) hse.getSession().getAttribute(Constants.ME_ANONYMOUS_WSP_DIR);
        if (StringUtils.isNotEmpty(anonymousWorkingDir)) {
            log.debug("Deleting anonymous working dir: " + anonymousWorkingDir);
            if (FileUtils.deleteQuietly(new File(anonymousWorkingDir))) {
                log.debug("Deleted");
            }
        }
    }
}
