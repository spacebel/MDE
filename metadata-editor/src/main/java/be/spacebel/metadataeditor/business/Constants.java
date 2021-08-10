package be.spacebel.metadataeditor.business;

import java.io.Serializable;

/**
 * This class contains the constants are using in the application
 *
 * @author mng
 */
public class Constants implements Serializable {

    public static final String OS_NAMESPACE = "http://a9.com/-/spec/opensearch/1.1/";
    public static final String OS_PREFIX = "os";
    public static final String OS_PARAM_NAMESPACE = "http://a9.com/-/spec/opensearch/extensions/parameters/1.0/";
    public static final String TIME_NAMESPACE = "http://a9.com/-/opensearch/extensions/time/1.0/";

    public static final String DATE_TYPE = "date";
    public static final String TIME_START = "start";
    public static final String TIME_END = "end";

    public static final String OS_PARAMETERS_XML_FILE = "os-parameters.xml";
    public static final String HTTP_GET_DETAILS_ERROR_CODE = "errorCode";
    public static final String HTTP_GET_DETAILS_ERROR_MSG = "errorMsg";

    public static final String OS_RESPONSE = "osResponse";
    public static final String OS_TOTAL_RESULTS = "totalResults";

    public static String DATEFORMAT = "yyyy-MM-dd";
    //public static String DATETIMEFORMAT = "yyyy-MM-dd'T'hh:mm:ss'Z'";
    public static String DATETIMEFORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static String DATETIMEZONEFORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static String DATETIMEZONEFULLFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String SEPARATOR = "#####";
    public static final String AUTO_COMPLETE_LIST = "open-list";

    public static final String GMI_NS = "http://www.isotc211.org/2005/gmi";
    public static final String GCO_NS = "http://www.isotc211.org/2005/gco";
    public static final String GMD_NS = "http://www.isotc211.org/2005/gmd";
    public static final String GML_NS = "http://www.opengis.net/gml/3.2";
    public static final String GMX_NS = "http://www.isotc211.org/2005/gmx";
    public static final String XLINK_NS = "http://www.w3.org/1999/xlink";
    public static final String XML_NS = "http://www.w3.org/2000/xmlns/";
    public static final String DCT_NS = "http://purl.org/dc/terms/";

    public static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
    public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
    public static final String SKOS_NS = "http://www.w3.org/2004/02/skos/core#";
    public static final String SOSA_NS = "http://www.w3.org/ns/sosa/";
    public static final String VOID_NS = "http://rdfs.org/ns/void#";

    public static final String GCMD_OLD_NS = "http://gcmd.gsfc.nasa.gov/kms#";
    public static final String GCMD_NS = "https://gcmd.earthdata.nasa.gov/kms#";    
    public static final String RDF_FORMAT_NS = "http://www.w3.org/ns/formats/RDF_XML";
    public static final String CSV_FORMAT_NS = "http://www.w3.org/ns/formats/CSV";

    public static final String OWS_NS = "http://www.opengis.net/ows/2.0";
    public static final String ATOM_NS = "http://www.w3.org/2005/Atom";      

    public static final String GMI_PREFIX = "gmi";
    public static final String GCO_PREFIX = "gco";
    public static final String GMD_PREFIX = "gmd";
    public static final String GML_PREFIX = "gml";
    public static final String GMX_PREFIX = "gmx";
    public static final String XLINK_PREFIX = "xlink";
    public static final String DCT_PREFIX = "dcterms";

    public static final String OWL_PREFIX = "owl";
    public static final String RDF_PREFIX = "rdf";
    public static final String RDFS_PREFIX = "rdfs";
    public static final String SKOS_PREFIX = "skos";
    public static final String SOSA_PREFIX = "sosa";
    public static final String VOID_PREFIX = "void";

    public static final String GCMD_OLD_PREFIX = "gcmd_old";
    public static final String GCMD_PREFIX = "gcmd";
    public static final String CSV_FORMAT_PREFIX = "csv";
    public static final String OWS_PREFIX = "ows";
    public static final String ATOM_PREFIX = "atom";
    
    public static final String SERVICE_PREFIX = "srv";
    public static final String SERVICE_NS = "http://www.isotc211.org/2005/srv";    

    public static final String EARTH_TOPOLOGIES_KEY = "earthtypologies";
    public static final String EARTH_MISSIONS_KEY = "earthmissions";
    public static final String EARTH_INSTRUMENTS_KEY = "earthinstruments";
    public static final String EARTH_TOPIC_KEY = "earthtopics";
    public static final String GCMD_KEY = "gcmd";

    public static final String EOP21_KEY = "eop21";
    public static final String EOP_EXT_KEY = "eopext";

    public static final String ORBITTYPE_KEY = "orbitType";
    public static final String WAVELENGTH_KEY = "wavelengthInformation";
    public static final String PROCESSORVER_KEY = "processorVersion";
    public static final String RESOLUTION_KEY = "resolution";
    public static final String PRODUCTTYPE_KEY = "productType";

    public static final String ORBITHEIGHT_KEY = "orbitHeight";
    public static final String SWATHWIDTH_KEY = "swathWidth";

    public static final String SKOS_INSCHEME = "skos:inScheme";
    public static final String RDF_TYPE = "rdf:type";

    public static final String SKOS_PREFLABEL = "skos:prefLabel";
    public static final String SKOS_ALTLABEL = "skos:altLabel";
    public static final String GCMD_ALTLABEL = "gcmd:altLabel";

    public static final String SKOS_DEFINITION = "skos:definition ";

    public static final String SKOS_BROADER = "skos:broader";
    public static final String SKOS_NARROWER = "skos:narrower";

    public static final String SKOS_EXACTMATCH = "skos:exactMatch";
    public static final String SKOS_RELATED = "skos:related";
    public static final String SKOS_RELATEDMATCH = "skos:relatedMatch";

    public static final String RDFS_SEEALSO = "rdfs:seeAlso";

    public static final String SOSA_ISHOSTEDBY = "sosa:isHostedBy";
    public static final String SOSA_HOSTS = "sosa:hosts";

    public static final String CSV_CATEGORY = "csv:Category";
    public static final String CSV_TOPIC = "csv:Topic";
    public static final String CSV_TERM = "csv:Term";
    public static final String CSV_VAR_LEVEL1 = "csv:Variable_Level_1";
    public static final String CSV_VAR_LEVEL2 = "csv:Variable_Level_2";
    public static final String CSV_VAR_LEVEL3 = "csv:Variable_Level_3";
    public static final String CSV_DETAILED_VAR = "csv:Detailed_Variable";

    public static final String ME_CONFIG_ATTR = "metadataEditorConfigAttr";
    public static final String ME_WSP_DIR_ATTR = "metadataEditorWorkspaceDirAttr";

    public static final String ME_ANONYMOUS_WSP_DIR = "meAnonymousWorkspaceDir";
    public static final String ME_ANONYMOUS_WSP_DIR_PREFIX = "me-anonymous-";

    public static final String STANDARDS_ISO_ORG_CODELIST_BASE = "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#";
    public static final String ISO_TC211_ORG_CODELIST_BASE = "http://www.isotc211.org/2005/resources/codeList.xml#";
    public static String GEO_JSON_SERIES_TYPE = "http://purl.org/dc/dcmitype/Collection";
    public static String GEO_JSON_SERVICE_TYPE = "http://purl.org/dc/dcmitype/Service";
    
    /* image mime type */
    public static final String JPEG_MIME_TYPE = "image/jpeg";
    public static final String PNG_MIME_TYPE = "image/png";
    public static final String TIFF_MIME_TYPE = "image/tiff";
    public static final String GIF_MIME_TYPE = "image/gif";
    
    /* document mime type */
    public static final String APPLICATION_PDF_MIME_TYPE = "application/pdf";
    public static final String APPLICATION_WORD_MIME_TYPE = "application/msword";    

    /* XML realted mimetype */    
    public static final String TEXT_HTML_MIME_TYPE = "text/html";    
    
    /* compressed mime type*/
    public static final String APPLICATION_ZIP_MIME_TYPE = "application/zip";
}
