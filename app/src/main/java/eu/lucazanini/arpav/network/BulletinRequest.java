package eu.lucazanini.arpav.network;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import eu.lucazanini.arpav.model.Previsione;
import timber.log.Timber;

/**
 * Volley request for Bulettin
 */
public class BulletinRequest extends Request<Previsione> {
    private final Response.Listener<Previsione> mListener;
    private String url;

    public BulletinRequest(int method, String url, Response.Listener<Previsione> listener,
                           Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        this.url = url;
    }

    public BulletinRequest(String url, Response.Listener<Previsione> listener, Response.ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }

    public BulletinRequest(String url, Response.Listener<Previsione> listener, Response.ErrorListener errorListener, String tag) {
        this(url, listener, errorListener);
        setTag(tag);
    }

    @Override
    protected Response<Previsione> parseNetworkResponse(NetworkResponse response) {
        Previsione previsione;
        String parsed;

        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }

        previsione = new Previsione(url, parsed);

        long now = System.currentTimeMillis();

        Map<String, String> headers = response.headers;
        long serverDate = 0;
        String serverEtag = null;
        String headerValue;

        headerValue = headers.get("Date");
        if (headerValue != null) {
            serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
        }

        serverEtag = headers.get("ETag");

        final long cacheHitButRefreshed = 15 * 60 * 1000; // in 15 minutes cache will be hit, but also refreshed on background
        final long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache entry expires completely
//        final long cacheHitButRefreshed = 0;
//        final long cacheExpired = 0;
        final long softExpire = now + cacheHitButRefreshed;
        final long ttl = now + cacheExpired;

        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = ttl;
        entry.serverDate = serverDate;
        entry.responseHeaders = headers;

        return Response.success(previsione, entry);
    }

    @Override
    protected void deliverResponse(Previsione response) {
        mListener.onResponse(response);
    }
}
