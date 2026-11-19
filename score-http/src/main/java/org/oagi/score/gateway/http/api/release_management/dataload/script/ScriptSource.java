package org.oagi.score.gateway.http.api.release_management.dataload.script;

import java.io.IOException;
import java.io.InputStream;

/**
 * Where the scripts named in an information file are read from.
 */
public interface ScriptSource {

    boolean exists(String script);

    InputStream open(String script) throws IOException;

}
