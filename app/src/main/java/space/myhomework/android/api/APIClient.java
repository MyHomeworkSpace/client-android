package space.myhomework.android.api;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIClient {
    private final String baseURL = "https://api-v2.myhomework.space/";

    private static APIClient _instance;

    public static synchronized APIClient getInstance(Context ctx, Runnable initCallback) {
        if (_instance == null) {
            _instance = new APIClient(ctx, initCallback);
        } else if (initCallback != null) {
            initCallback.run();
        }
        return _instance;
    }

    public static synchronized void clearInstance() {
        _instance = null;
    }

    private URI baseURLObj = null;
    private CookieManager _cookieManager;
    private RequestQueue _requestQueue;

    private String _csrfToken = "";

    public APIAccount account;
    public ArrayList<APIClass> classes;

    private static Context _ctx;

    private APIClient(Context ctx, final Runnable initCallback) {
        _ctx = ctx;
        try {
            baseURLObj = new URI(baseURL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        _csrfToken = "";
        _requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        _cookieManager = new CookieManager();
        CookieHandler.setDefault(_cookieManager);

        // check if we have stored a cookie value
        try {
            FileInputStream fis = _ctx.openFileInput("session_id");
            byte[] sessionData = new byte[fis.available()];
            fis.read(sessionData);
            String sessionId = new String(sessionData);

            // what is this garbage api why google why
            HashMap<String, List<String>> cookies = new HashMap<String, List<String>>();
            ArrayList<String> cookieList = new ArrayList<String>();
            cookieList.add("session=" + sessionId + "; Path=/; Expires=Tue, 19 Jan 2038 03:14:07 UTC");
            cookies.put("Set-Cookie", cookieList);
            _cookieManager.put(baseURLObj, cookies);
        } catch (FileNotFoundException e) {
            // no stored session id, just carry on
        } catch (IOException e) {
            e.printStackTrace();
        }

        makeRequest(Request.Method.GET, "auth/csrf", new HashMap<String, String>(), null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                _csrfToken = getCookieValue("csrfToken");
                if (initCallback != null) {
                    initCallback.run();
                }
            }
        });
    }

    public String getCookieValue(String name) {
        List<HttpCookie> cookies = _cookieManager.getCookieStore().get(baseURLObj);
        for (HttpCookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void makeRequest(int method, String url, HashMap<String, String> params, Response.Listener<JSONObject> successHandler, Response.ErrorListener errorHandler) {
        String requestUrl = baseURL + url + (_csrfToken.equals("") ? "" : "?csrfToken=" + _csrfToken);
        if (method == Request.Method.GET) {
            if (params.size() > 0) {
                int i = 0;
                for (Map.Entry<String, String> param : params.entrySet()) {
                    if (i == 0 && _csrfToken.equals("")) {
                        requestUrl += "?";
                    } else {
                        requestUrl += "&";
                    }

                    requestUrl += param.getKey();
                    requestUrl += "=";
                    requestUrl += param.getValue(); // TODO: url encoding

                    i++;
                }
            }
        }
        APIRequest request = new APIRequest(method, requestUrl, successHandler, errorHandler, params);
        request.setRetryPolicy(new APIRetryPolicy());
        _requestQueue.add(request);
    }
}

class APIRequest extends Request<JSONObject> {
    private Response.Listener<JSONObject> listener;
    public Map<String, String> params;

    public APIRequest(int method, String url, Response.Listener<JSONObject> reponseListener, Response.ErrorListener errorListener, Map<String, String> requestParams) {
        super(method, url, errorListener);
        listener = reponseListener;
        params = requestParams;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        listener.onResponse(response);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}

class APIRetryPolicy extends DefaultRetryPolicy {
    @Override
    public void retry(VolleyError error) throws VolleyError {
        if (error.networkResponse != null && error.networkResponse.statusCode == 401) { // unauthorized
            throw error;
        }
        super.retry(error);
    }
}