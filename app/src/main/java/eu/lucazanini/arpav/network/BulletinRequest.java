package eu.lucazanini.arpav.network;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;

import eu.lucazanini.arpav.model.Previsione;
import rx.Observable;

/**
 * Created by luke on 21/12/16.
 */

public class BulletinRequest extends Request<Previsione> {
    private final Response.Listener<Previsione> mListener;

    public BulletinRequest(int method, String url, Response.Listener<Previsione> listener,
                         Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    public BulletinRequest(String url, Response.Listener<Previsione> listener, Response.ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
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

        previsione=new Previsione(Previsione.Language.IT);
        previsione.parse(parsed);

        return Response.success(previsione, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(Previsione response) {
        mListener.onResponse(response);
    }
}
