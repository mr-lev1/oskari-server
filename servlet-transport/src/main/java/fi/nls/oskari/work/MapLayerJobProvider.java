package fi.nls.oskari.work;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.worker.Job;

/**
 * Wrapping for WFSMapLayerJob so it can be found with Oskari annotation
 */

@Oskari("default")
public class MapLayerJobProvider extends OskariComponent {

    public Job createJob(ResultProcessor service, JobType type, SessionStore store, WFSLayerStore layer,
                                    boolean reqSendFeatures, boolean reqSendImage, boolean reqSendHighlight) {
        return new WFSMapLayerJob(service, type, store, layer,
                reqSendFeatures, reqSendImage, reqSendHighlight);
    }
}
