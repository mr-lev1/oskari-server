package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

/**
 *  Modifier for UserLayerPluginHandler config
 */
public class UserLayerPluginHandler implements PluginHandler {

    private static final Logger LOGGER = LogFactory.getLogger(UserLayerPluginHandler.class);
    public static final String PLUGIN_NAME = "Oskari.mapframework.bundle.myplacesimport.plugin.UserLayersLayerPlugin";
    public static final String KEY_ID = "id";
    public static final String KEY_CONFIG = "config";
    public static final String KEY_CLUSTERING_DISTANCE = "clusteringDistance";
    private static String CLUSTERING_DISTANCE = PropertyUtil.getOptional("userlayer.clustering.distance");

    @Override
    public boolean modifyPlugin(final JSONObject plugin,
                                final ModifierParams params,
                                final String mapSrs) {
        if(plugin == null) {
            LOGGER.debug("Tried to modify UserLayersLayerPlugin, but plugin didn't exist!");
            return false;
        }
        if(!PLUGIN_NAME.equals(plugin.optString(KEY_ID))) {
            LOGGER.debug("Tried to modify UserLayersLayerPlugin, but given JSON isn't UserLayersLayerPlugin!");
            return false;
        }
        if (CLUSTERING_DISTANCE == null) {
            // Clustering is not enabled in oskari-ext.properties
            return true;
        }
        JSONObject config = plugin.optJSONObject(KEY_CONFIG);
        if(config == null) {
            config = new JSONObject();
            JSONHelper.putValue(plugin, KEY_CONFIG, config);
        }
        if(config.has(KEY_CLUSTERING_DISTANCE)) {
            return true;
        }
        try {
            int distance = Integer.parseInt(CLUSTERING_DISTANCE);
            JSONHelper.putValue(config, KEY_CLUSTERING_DISTANCE, distance);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
