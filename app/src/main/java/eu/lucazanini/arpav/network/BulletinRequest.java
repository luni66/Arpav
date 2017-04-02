package eu.lucazanini.arpav.network;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import eu.lucazanini.arpav.model.Previsione;

/**
 * Created by luke on 21/12/16.
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

//    public BulletinRequest(Previsione.Language language, Response.Listener<Previsione> listener, Response.ErrorListener errorListener) {
//        this(Method.GET, Previsione.getUrl(language), listener, errorListener);
//    }

    @Override
    protected Response<Previsione> parseNetworkResponse(NetworkResponse response) {
        Previsione previsione;
        String parsed;

        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }

//        previsione=new Previsione(Previsione.Language.IT);
//        previsione.parse(parsed);
        previsione = new Previsione(url, parsed);

        //TODO http://stackoverflow.com/questions/28523435/what-s-the-different-of-entry-softttl-and-entry-ttl-in-volley

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
        final long softExpire = now + cacheHitButRefreshed;
        final long ttl = now + cacheExpired;

        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = ttl;
        entry.serverDate = serverDate;
        entry.responseHeaders = headers;


/*        // force response to be cached
        Map<String, String> headers = response.headers;
        long cacheExpiration = 15 * 60 * 1000;
        long now = System.currentTimeMillis();
        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = headers.get("ETag");
        entry.ttl = now + cacheExpiration;
        entry.serverDate = HttpHeaderParser.parseDateAsEpoch(headers.get("Date"));
        entry.responseHeaders = headers;*/

/*        // force response to be cached
        Map<String, String> headers = response.headers;
//        long cacheExpiration = 5 * 60 * 1000;
//        long now = System.currentTimeMillis();
        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = headers.get("ETag");
//        entry.ttl = now + cacheExpiration;
        if (previsione.isUpdate()) {
            entry.ttl = previsione.getCacheExpiration();
        } else {
            long cacheExpiration = 15 * 60 * 1000;
            long now = System.currentTimeMillis();
            entry.ttl = now + cacheExpiration;
        }
        entry.serverDate = HttpHeaderParser.parseDateAsEpoch(headers.get("Date"));
        entry.responseHeaders = headers;*/

//        return Response.success(previsione, HttpHeaderParser.parseCacheHeaders(response));
        return Response.success(previsione, entry);
    }

    @Override
    protected void deliverResponse(Previsione response) {
        mListener.onResponse(response);
    }
}
