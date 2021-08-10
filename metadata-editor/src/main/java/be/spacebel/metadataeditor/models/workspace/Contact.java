/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace;

import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.utils.parser.XmlUtils;
import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A representation of gmd:contact element of the Internal Metadata Model
 * 
 * @author mng
 */
public class Contact implements Serializable {

    private Node responsibleParty;
    private String individualName;
    private Node nIndividualName;
    private String orgName;
    private Node nOrgName;
    private String positionName;
    private Node nPositionName;
    private Node ciContact;
    private Node ciTelephone;
    private Node ciAddress;
    private Node ciOnlineRs;
    private String phone;
    private Node nPhone;
    private String fax;
    private Node nFax;
    private String address;
    private Node nAdd;
    private String city;
    private Node nCity;
    private String postal;
    private Node nPostal;
    private String country;
    private Node nCountry;
    private String email;
    private Node nEmail;
    private String onlineRs;
    private Node nOnlineRs;
    private String role;
    private Node nRole;

    public Contact() {
    }

    public Node getResponsibleParty() {
        return responsibleParty;
    }

    public void setResponsibleParty(Node responsibleParty) {
        this.responsibleParty = responsibleParty;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostal() {
        return postal;
    }

    public void setPostal(String postal) {
        this.postal = postal;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOnlineRs() {
        return onlineRs;
    }

    public void setOnlineRs(String onlineRs) {
        this.onlineRs = onlineRs;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Node getnOrgName() {
        return nOrgName;
    }

    public void setnOrgName(Node nOrgName) {
        this.nOrgName = nOrgName;
    }

    public Node getnPositionName() {
        return nPositionName;
    }

    public void setnPositionName(Node nPositionName) {
        this.nPositionName = nPositionName;
    }

    public Node getnPhone() {
        return nPhone;
    }

    public void setnPhone(Node nPhone) {
        this.nPhone = nPhone;
    }

    public Node getnFax() {
        return nFax;
    }

    public void setnFax(Node nFax) {
        this.nFax = nFax;
    }

    public Node getnAdd() {
        return nAdd;
    }

    public void setnAdd(Node nAdd) {
        this.nAdd = nAdd;
    }

    public Node getnCity() {
        return nCity;
    }

    public void setnCity(Node nCity) {
        this.nCity = nCity;
    }

    public Node getnPostal() {
        return nPostal;
    }

    public void setnPostal(Node nPostal) {
        this.nPostal = nPostal;
    }

    public Node getnCountry() {
        return nCountry;
    }

    public void setnCountry(Node nCountry) {
        this.nCountry = nCountry;
    }

    public Node getnEmail() {
        return nEmail;
    }

    public void setnEmail(Node nEmail) {
        this.nEmail = nEmail;
    }

    public Node getnOnlineRs() {
        return nOnlineRs;
    }

    public void setnOnlineRs(Node nOnlineRs) {
        this.nOnlineRs = nOnlineRs;
    }

    public Node getnRole() {
        return nRole;
    }

    public void setnRole(Node nRole) {
        this.nRole = nRole;
    }

    public String getIndividualName() {
        return individualName;
    }

    public void setIndividualName(String individualName) {
        this.individualName = individualName;
    }

    public Node getnIndividualName() {
        return nIndividualName;
    }

    public void setnIndividualName(Node nIndividualName) {
        this.nIndividualName = nIndividualName;
    }

    public Node getCiContact() {
        return ciContact;
    }

    public void setCiContact(Node ciContact) {
        this.ciContact = ciContact;
    }

    public Node getCiTelephone() {
        return ciTelephone;
    }

    public void setCiTelephone(Node ciTelephone) {
        this.ciTelephone = ciTelephone;
    }

    public Node getCiAddress() {
        return ciAddress;
    }

    public void setCiAddress(Node ciAddress) {
        this.ciAddress = ciAddress;
    }

    public Node getCiOnlineRs() {
        return ciOnlineRs;
    }

    public void setCiOnlineRs(Node ciOnlineRs) {
        this.ciOnlineRs = ciOnlineRs;
    }

    public void update() {
        Document ownerDoc = responsibleParty.getOwnerDocument();

        if (nIndividualName != null) {
            // update node            
            XmlUtils.setTextContent(nIndividualName, individualName);
        } else {
            if (StringUtils.isNotEmpty(individualName)) {
                // insert node
                Node parent = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:individualName");
                nIndividualName = ownerDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                Node value = ownerDoc.createTextNode(individualName);
                nIndividualName.appendChild(value);
                parent.appendChild(nIndividualName);

                Node node = findNode();
                if (node != null) {
                    responsibleParty.insertBefore(parent, node.getParentNode());
                } else {
                    if (nRole != null) {
                        responsibleParty.insertBefore(parent, nRole.getParentNode().getParentNode());
                    } else {
                        responsibleParty.appendChild(parent);
                    }

                }
            }
        }

        if (nOrgName != null) {
            // update node            
            XmlUtils.setTextContent(nOrgName, orgName);
        } else {
            if (StringUtils.isNotEmpty(orgName)) {
                // insert node

                Node parent = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:organisationName");
                nOrgName = ownerDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                Node value = ownerDoc.createTextNode(orgName);
                nOrgName.appendChild(value);
                parent.appendChild(nOrgName);

                if (nPositionName != null) {
                    responsibleParty.insertBefore(parent, nPositionName.getParentNode());
                } else {
                    if (ciContact != null) {
                        responsibleParty.insertBefore(parent, ciContact.getParentNode());
                    } else {
                        if (nRole != null) {
                            responsibleParty.insertBefore(parent, nRole.getParentNode().getParentNode());
                        } else {
                            responsibleParty.appendChild(parent);
                        }
                    }
                }
            }
        }

        if (nPositionName != null) {
            // update            
            XmlUtils.setTextContent(nPositionName, positionName);
        } else {
            if (StringUtils.isNotEmpty(positionName)) {
                // insert node

                Node parent = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:positionName");
                nPositionName = ownerDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                Node value = ownerDoc.createTextNode(positionName);
                nPositionName.appendChild(value);
                parent.appendChild(nPositionName);

                if (ciContact != null) {
                    responsibleParty.insertBefore(parent, ciContact.getParentNode());
                } else {
                    if (nRole != null) {
                        responsibleParty.insertBefore(parent, nRole.getParentNode().getParentNode());
                    } else {
                        responsibleParty.appendChild(parent);
                    }
                }
            }

        }

        if (nPhone != null) {
            // update            
            XmlUtils.setTextContent(nPhone, phone);
        } else {
            if (StringUtils.isNotEmpty(phone)) {
                // insert node

                Node parent = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:voice");
                nPhone = ownerDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                Node value = ownerDoc.createTextNode(phone);
                nPhone.appendChild(value);
                parent.appendChild(nPhone);

                if (ciTelephone == null) {
                    createTelephoneNode();
                }

                if (nFax != null) {
                    ciTelephone.insertBefore(parent, nFax.getParentNode());
                } else {
                    ciTelephone.appendChild(parent);
                }

            }
        }

        if (nFax != null) {            
            XmlUtils.setTextContent(nFax, fax);
        } else {
            if (StringUtils.isNotEmpty(fax)) {
                // insert node

                Node parent = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:facsimile");
                nFax = ownerDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                Node value = ownerDoc.createTextNode(fax);
                nFax.appendChild(value);
                parent.appendChild(nFax);

                if (ciTelephone == null) {
                    createTelephoneNode();
                }

                ciTelephone.appendChild(parent);

            }
        }

        if (nAdd != null) {
            // update            
            XmlUtils.setTextContent(nAdd, address);
        } else {
            if (StringUtils.isNotEmpty(address)) {
                // insert node

                Node parent = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:deliveryPoint");
                nAdd = ownerDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                Node value = ownerDoc.createTextNode(address);
                nAdd.appendChild(value);
                parent.appendChild(nAdd);

                if (ciAddress == null) {
                    createAddressNode();
                }

                Node node = findAddNode();
                if (node != null) {
                    ciAddress.insertBefore(parent, node.getParentNode());
                } else {
                    ciAddress.appendChild(parent);
                }
            }
        }

        if (nCity != null) {            
            XmlUtils.setTextContent(nCity, city);
        } else {
            if (StringUtils.isNotEmpty(city)) {
                // insert node

                Node parent = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:city");
                nCity = ownerDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                Node value = ownerDoc.createTextNode(city);
                nCity.appendChild(value);
                parent.appendChild(nCity);

                if (ciAddress == null) {
                    createAddressNode();
                }

                Node node = findCityNode();
                if (node != null) {
                    ciAddress.insertBefore(parent, node.getParentNode());
                } else {
                    ciAddress.appendChild(parent);
                }
            }
        }

        if (nPostal != null) {            
            XmlUtils.setTextContent(nPostal, postal);
        } else {
            if (StringUtils.isNotEmpty(postal)) {
                // insert node

                Node parent = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:postalCode");
                nPostal = ownerDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                Node value = ownerDoc.createTextNode(postal);
                nPostal.appendChild(value);
                parent.appendChild(nPostal);

                if (ciAddress == null) {
                    createAddressNode();
                }

                Node node = findPostalNode();
                if (node != null) {
                    ciAddress.insertBefore(parent, node.getParentNode());
                } else {
                    ciAddress.appendChild(parent);
                }
            }
        }

        if (nCountry != null) {            
            XmlUtils.setTextContent(nCountry, country);
        } else {
            if (StringUtils.isNotEmpty(country)) {
                // insert node

                Node parent = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:country");
                nCountry = ownerDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                Node value = ownerDoc.createTextNode(country);
                nCountry.appendChild(value);
                parent.appendChild(nCountry);

                if (ciAddress == null) {
                    createAddressNode();
                }

                if (nEmail != null) {
                    ciAddress.insertBefore(parent, nEmail.getParentNode());
                } else {
                    ciAddress.appendChild(parent);
                }
            }
        }

        if (nEmail != null) {            
            XmlUtils.setTextContent(nEmail, email);
        } else {
            if (StringUtils.isNotEmpty(email)) {
                // insert node

                Node parent = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:electronicMailAddress");
                nEmail = ownerDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                Node value = ownerDoc.createTextNode(email);
                nEmail.appendChild(value);
                parent.appendChild(nEmail);

                if (ciAddress == null) {
                    createAddressNode();
                }

                ciAddress.appendChild(parent);
            }
        }

        if (nOnlineRs != null) {            
            XmlUtils.setTextContent(nOnlineRs, onlineRs);
        }else{
            if (StringUtils.isNotEmpty(onlineRs)) {
                // insert node

                Node parent = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:linkage");
                nOnlineRs = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:URL");
                Node value = ownerDoc.createTextNode(onlineRs);
                nOnlineRs.appendChild(value);
                parent.appendChild(nOnlineRs);

                if (ciOnlineRs == null) {
                    createOnlineRsNode();
                }

                ciOnlineRs.appendChild(parent);
            }
        }
        
        if (nRole != null) {
            nRole.setNodeValue(role);
        }
    }

    private Node findNode() {
        if (nOrgName != null) {
            return nOrgName;
        }

        if (nPositionName != null) {
            return nPositionName;
        }

        if (ciContact != null) {
            return ciContact;
        }
        return null;
    }

    private Node findAddNode() {
        if (nCity != null) {
            return nCity;
        }

        if (nPostal != null) {
            return nPostal;
        }

        if (nCountry != null) {
            return nCountry;
        }

        if (nEmail != null) {
            return nEmail;
        }
        return null;
    }

    private Node findCityNode() {
        if (nPostal != null) {
            return nPostal;
        }

        if (nCountry != null) {
            return nCountry;
        }

        if (nEmail != null) {
            return nEmail;
        }
        return null;
    }

    private Node findPostalNode() {
        if (nCountry != null) {
            return nCountry;
        }

        if (nEmail != null) {
            return nEmail;
        }
        return null;
    }

    private void createContactInfoNode() {
        Document ownerDoc = responsibleParty.getOwnerDocument();
        ciContact = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:CI_Contact");
        Node contactInfo = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:contactInfo");
        contactInfo.appendChild(ciContact);

        if (nRole != null) {
            responsibleParty.insertBefore(contactInfo, nRole.getParentNode().getParentNode());
        } else {
            responsibleParty.appendChild(contactInfo);
        }
    }

    private void createTelephoneNode() {
        Document ownerDoc = responsibleParty.getOwnerDocument();
        ciTelephone = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:CI_Telephone");
        Node phoneNode = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:phone");
        phoneNode.appendChild(ciTelephone);

        if (ciContact == null) {
            createContactInfoNode();
        }

        if (ciAddress != null) {
            responsibleParty.insertBefore(phoneNode, ciAddress.getParentNode());
        } else {
            if (ciOnlineRs != null) {
                responsibleParty.insertBefore(phoneNode, ciOnlineRs.getParentNode());
            } else {
                ciContact.appendChild(phoneNode);
            }
        }

    }

    private void createAddressNode() {
        Document ownerDoc = responsibleParty.getOwnerDocument();
        ciAddress = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:CI_Address");
        Node addressNode = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:address");
        addressNode.appendChild(ciAddress);

        if (ciContact == null) {
            createContactInfoNode();
        }

        if (ciOnlineRs != null) {
            responsibleParty.insertBefore(addressNode, ciOnlineRs.getParentNode());
        } else {
            ciContact.appendChild(addressNode);
        }

    }

    private void createOnlineRsNode() {
        Document ownerDoc = responsibleParty.getOwnerDocument();
        ciOnlineRs = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:CI_OnlineResource");
        Node onlineRsNode = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:onlineResource");
        onlineRsNode.appendChild(ciOnlineRs);

        if (ciContact == null) {
            createContactInfoNode();
        }
        ciContact.appendChild(onlineRsNode);
    }
}
