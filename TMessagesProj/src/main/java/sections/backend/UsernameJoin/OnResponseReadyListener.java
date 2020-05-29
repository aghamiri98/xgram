package sections.backend.UsernameJoin;

import org.json.JSONObject;




public interface OnResponseReadyListener {

    void OnResponseReady(boolean error, JSONObject data, String message);
}
