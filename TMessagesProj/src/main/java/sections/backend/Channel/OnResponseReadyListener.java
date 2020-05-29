package sections.backend.Channel;

import org.json.JSONObject;


public interface OnResponseReadyListener {

    void OnResponseReady(boolean error, JSONObject data, String message);
}
