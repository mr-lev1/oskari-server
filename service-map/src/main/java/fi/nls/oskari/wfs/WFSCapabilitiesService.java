package fi.nls.oskari.wfs;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;

import fi.nls.oskari.service.capabilities.OskariLayerCapabilitiesHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.internal.WFSGetCapabilities;
import org.oskari.service.wfs3.WFS3Service;
import org.oskari.service.wfs3.model.WFS3CollectionInfo;
import org.oskari.service.wfs3.model.WFS3Exception;
import org.oskari.utils.common.Sets;

import java.io.IOException;
import java.util.*;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.*;

public class WFSCapabilitiesService {
    private static Logger log = LogFactory.getLogger(WFSCapabilitiesService.class);
    protected static Set<String> SUPPORTED_FEATURE_FORMATS = new HashSet<>
            (Arrays.asList("text/xml; subtype=gml/3.1.1",
                    "text/xml; subtype=gml/3.2",
                    "application/gml+xml; version=3.2",
                    "application/json",
                    "text/xml"));

    public static List<String> getLayerNames (String url, String version,
                                                String user, String pw) throws ServiceException {
        WFSDataStore data = getDataStore(url, version, user, pw);
        try {
            return Arrays.asList(data.getTypeNames());
        } catch (IOException e) {
            throw new ServiceException("Failed to retrieve layer names from: " + url, e);
        }
    }
    public static WFSDataStore getDataStore (OskariLayer ml) throws ServiceException {
        String version = ml.getVersion();
        if (version == null || version.isEmpty()) {
            version = WFS_DEFAULT_VERSION;
        }
        return getDataStore(ml.getUrl(), version, ml.getUsername(), ml.getPassword());
    }

    private static WFSDataStore getDataStore (String url, String version,
                                                String user, String pw) throws ServiceException {
        try {
            Map connectionParameters = new HashMap();
            connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getUrl(url, version));
            connectionParameters.put("WFSDataStoreFactory:TIMEOUT", 30000);
            if (user != null && !user.isEmpty()) {
                connectionParameters.put("WFSDataStoreFactory:USERNAME", user);
                connectionParameters.put("WFSDataStoreFactory:PASSWORD", pw);
            }
            //  connection
            return (WFSDataStore) DataStoreFinder.getDataStore(connectionParameters);
            //TODO: try to catch wrong version or unauthorized
            //IllegalStateException: Unable to parse GetCapabilities document
            //IOException: Server returned HTTP response code: 401 for URL: xxx
        } catch (Exception ex) {
            throw new ServiceException("Couldn't read/get wfs capabilities response from url: " + url + " Message: " + ex.getMessage());
        }
    }

    private static String getUrl(String url, String version) throws ServiceException {
        if (url.isEmpty()) throw new ServiceException ("Empty url");
        String baseUrl = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
        return baseUrl + "?service=WFS&request=GetCapabilities&version="+ version;
    }
    public static Map<String, Object> getCapabilitiesResults (String url, String version, String user, String pw,
                                                              Set<String> systemCRSs)
                                                                throws ServiceException {
        try {
            if (WFS3_VERSION.equals(version)) {
                return getCapabilitiesWFS3( url, user, pw, systemCRSs);
            }
            return getCapabilitiesWFS( url, version, user, pw, systemCRSs);
        } catch (Exception e) {
            throw new ServiceException ("Failed to get capabilities version: " + version + " from url: " + url, e);
        }
    }

    public static Map<String, Object> getCapabilitiesWFS (String url, String version,
                                           String user, String pw, Set<String> systemCRSs) throws ServiceException, IOException {
        WFSDataStore data = getDataStore(url, version, user, pw);
        String title = data.getInfo().getTitle();
        String responseVersion = data.getInfo().getVersion();
        String layerNames[] = data.getTypeNames();
        List<OskariLayer> layers = new ArrayList<>();
        List<String> layersWithErrors = new ArrayList<>();
        WFSGetCapabilities capa = data.getWfsClient().getCapabilities();
        for (String layerName : layerNames) {
            try {
                SimpleFeatureSource source = data.getFeatureSource(layerName);
                String layerTitle = source.getInfo().getTitle();
                OskariLayer ml = toOskariLayer(layerName, layerTitle, version, url, user, pw);
                try {
                    OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWFS(capa, source, ml, systemCRSs);
                }catch (ServiceException e) {} //list layer even capabilities fails
                layers.add(ml);
            } catch (Exception e) {
                String error = e.getMessage();
                log.warn("Failed to parse layer:", layerName, "from url:", url);
                layersWithErrors.add(layerName); // TODO should we use Map (layerName, error) and pass error also to frontend
            }
        }
        Map<String, Object> results = new HashMap<>();
        results.put(KEY_VERSION, responseVersion);
        results.put(KEY_TITLE, title);
        results.put(KEY_LAYERS, layers);
        results.put(KEY_ERROR_LAYERS, layersWithErrors);

        return results;
    }

    public static Map<String, Object> getCapabilitiesWFS3 (String url, String user, String pw, Set<String> systemCRSs) throws WFS3Exception, IOException {
        WFS3Service service = WFS3Service.fromURL(url, user, pw);
        List<OskariLayer> layers = new ArrayList<>();

        for (WFS3CollectionInfo collection : service.getCollections()) {
            String name = collection.getId();
            String title = collection.getTitle();
            OskariLayer ml = toOskariLayer(name, title, WFS3_VERSION, url, user, pw);
            OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWFS(service, ml, systemCRSs);
            layers.add(ml);
        }
        // Do we need to parse title from WFS3Content.links
        return Collections.singletonMap( KEY_LAYERS, layers);
    }
    private static OskariLayer toOskariLayer(String layerName, String title, String version,
                                             String url, String user, String pw) {
        final OskariLayer ml = new OskariLayer();
        ml.setType(OskariLayer.TYPE_WFS);
        ml.setUrl(url);
        ml.setMaxScale(1d);
        ml.setMinScale(1500000d);
        ml.setName(layerName);
        ml.setVersion(version);
        ml.setPassword(pw);
        ml.setUsername(user);
        title = title == null || title.isEmpty() ? layerName : title;
        for (String lang : PropertyUtil.getSupportedLanguages()) {
            ml.setName(lang, title);
        }
        return ml;
    }
    protected static Set<String> getFormatsToStore (Set<String> formats) {
        return Sets.intersection(SUPPORTED_FEATURE_FORMATS, formats);
    }


}

