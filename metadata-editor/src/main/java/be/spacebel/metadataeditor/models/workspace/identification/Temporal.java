/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace.identification;

import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.utils.parser.XmlUtils;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.parser.XPathUtils;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

/**
 * This class represents gmd:temporalElement element of ISO 19139-2 XML metadata
 *
 * @author mng
 */
public class Temporal {

    private Date startDate;
    private Node nStart;
    private Date endDate;
    private Node nEnd;
    private GmxAnchor description;
    private Node self;

    public Temporal() {
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Node getnStart() {
        return nStart;
    }

    public void setnStart(Node nStart) {
        this.nStart = nStart;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Node getnEnd() {
        return nEnd;
    }

    public void setnEnd(Node nEnd) {
        this.nEnd = nEnd;
    }

    public GmxAnchor getDescription() {
        if (description == null) {
            description = new GmxAnchor();
        }
        return description;
    }

    public void setDescription(GmxAnchor description) {
        this.description = description;
    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }

    public void validate() throws IOException {
        StringBuilder errors = new StringBuilder();

//        if (StringUtils.isNotEmpty(startDate)) {
//            System.out.println("Start date: " + startDate);
//            try {
//                CommonUtils.dateFormat.parse(startDate);
//            } catch (ParseException e) {
//                e.printStackTrace();
//                errors.append("Start date should not be in format ")
//                        .append(Constants.DATEFORMAT);
//            }
//        }
//
//        if (StringUtils.isNotEmpty(endDate)) {
//            System.out.println("End date: " + endDate);
//            try {
//                CommonUtils.dateFormat.parse(endDate);
//            } catch (ParseException e) {
//                e.printStackTrace();
//                errors.append("End date should not be in format ")
//                        .append(Constants.DATEFORMAT);
//            }
//        }
        if (errors.length() > 0) {
            throw new IOException(errors.toString());
        }
    }

    public void update() throws IOException {
        validate();

        if (nStart != null) {
            XmlUtils.setTextContent(nStart, (startDate == null ? "" : CommonUtils.dateToStr(startDate)));
        }
        if (nEnd != null) {
            XmlUtils.setTextContent(nEnd, (endDate == null ? "" : CommonUtils.dateToStr(endDate)));
        }
        if (description != null) {
            //System.out.println("Update description");
            if (StringUtils.isNotEmpty(description.getLink())
                    || StringUtils.isNotEmpty(description.getText())) {
//                if (description.getSelf() != null) {
//                    System.out.println("NNNNNNNNNNNNNNN:" + description.getSelf().getLocalName());
//                    description.getSelf().getParentNode().getParentNode()
//                            .removeChild(description.getSelf().getParentNode());
//                    //self.removeChild(description.getSelf().getParentNode());
//                }
                XPathUtils.removeNodes(self, "./gmd:description");
                Node newNode = XmlUtils.createSPNode(self, description, "description", Constants.GMD_NS);
                if (newNode != null) {
                    self.insertBefore(newNode, self.getFirstChild());
                }
            }
        }

    }

}
