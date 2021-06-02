# Metadata Editor (MDE)

## Introduction

The Metadata Editor provides a Web-based user interface allowing to create or update metadata records for Earth Observation (EO) collections and/or services/applications and interacting with one or more compatible OpenSearch or OGC API-Features based catalogues.  The Metadata Editor supports multiple metadata formats:

*	Collection metadata: ISO19139-2 and OGC 17-084r1 (GeoJSON).
*	Service metadata: ISO19139 and OGC 19-020r1 (GeoJSON).

It allows:

*	Retrieving metadata from a catalogue supporting OGC 13-026r9 OpenSearch interfaces with Atom response.
*	Storing metadata in a catalogue.
*	Retrieve metadata from the local file system
*	Save metadata to the local file system.
*	Creating, updating, validating and visualising metadata in the user’s private online workspace. 

It supports multiple user roles:

*	Anonymous (no persistent workspace, no catalogue write access)
*	Registered user (persistent workspace, preferences, …)
*	Administrator (add users, add catalogues, …)


## External interfaces

The Metadata Editor is tightly integrated with the ESA SKOS Thesauri and the GCMD SKOS vocabularies.  It supports users to find the appropriate platform, instrument and earth topics keywords from the ESA Thesauri and includes the corresponding GCMD concepts automatically.  The tool automatically synchronises with the latest version of the thesauri and indicates discrepancies with the metadata records in the users’ workspace.

The Metadata Editor interfaces with one or more catalogues to retrieve metadata records or store metadata records.

Finally, it interfaces with a NASA Web-service endpoint to validate the DIF-10 metadata encoding. 


## User interface

A user uploads metadata files from his local harddisk to his private workspace or retrieves them from a connected OpenSearch catalogue endpoint.

![Metadata editor workspace](/images/workspace.png)

OGC service endpoints available for a particular collection or service/application can be associated to the collection or service metadata via "offerings".  They are subsequently encoded in the corresponding GeoJSON and ISO metadata formats which can be previewed within the tool’s user interface.

![Metadata editor workspace](/images/offerings.png)

Users with administrator roles can enable/disable support for metadata formats, register multiple catalogue endpoints for read and/or write access and enable/disable automatic updates of ESA and GCMD thesauri. They also create user accounts and manage visibility of catalogue endpoints for registered users.

![Metadata editor workspace](/images/preferences.png)


## Resources

* References
  * [ESE-ERGO Project Page](https://wiki.services.eoportal.org/tiki-index.php?page=ESE-ERGO)
  * [OGC 13-026r9, OGC OpenSearch Extension for Earth Observation](https://docs.opengeospatial.org/is/13-026r9/13-026r9.html)
  * [CEOS OpenSearch Best Practice v1.3](https://ceos.org/document_management/Working_Groups/WGISS/Documents/WGISS%20Best%20Practices/CEOS%20OpenSearch%20Best%20Practice.pdf) 
  * [Technical Guidance for the implementation of INSPIRE dataset and service metadata based on ISO/TS 19139:2007](https://inspire.ec.europa.eu/id/document/tg/metadata-iso19139)
  * [ISO 19139:2007, Geographic information — Metadata — XML schema implementation](http://www.iso.org/iso/iso_catalogue/catalogue_tc/catalogue_detail.htm?csnumber=32557)
  * [ISO 19139-2:2012, Geographic information – Metadata – XML schema implementation – Part 2: Extensions for imagery and gridded data](http://www.iso.org/iso/iso_catalogue/catalogue_tc/catalogue_detail.htm?csnumber=57104)
  * [ISO 19119:2005, Geographic Information – Services](http://www.iso.org/iso/iso_catalogue/catalogue_tc/catalogue_detail.htm?csnumber=39890)
  * [OGC 17-084r1, EO Collection GeoJSON(-LD) Encoding](https://docs.ogc.org/bp/17-084r1/17-084r1.html)
  * [OGC 19-020r1, OGC Testbed-15: Catalogue and Discovery Engineering Report](https://docs.ogc.org/per/19-020r1.html)
  
* Presentations
  * Final Presentation, 28/05/2021  
  * [CEOS WGISS-50, fedEO Metadata Editor, 22/09/2020](http://ceos.org/document_management/Working_Groups/WGISS/Meetings/WGISS-50/1.%20Tuesday%20Sept%2022/2020.09.22_fedeo_metadata_editor.pptx)

## Credits

This project has received funding from the [European Space Agency](https://esa.int) under the [General Support Technology Programme](http://www.esa.int/Enabling_Support/Space_Engineering_Technology/Shaping_the_Future/About_the_General_Support_Technology_Programme_GSTP) and was supported by the [Belgian Science Policy Office](https://www.belspo.be/belspo/index_en.stm).
