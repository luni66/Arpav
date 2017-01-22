package eu.lucazanini.arpav;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import eu.lucazanini.arpav.model.Bollettino;
import eu.lucazanini.arpav.model.Meteogramma;
import eu.lucazanini.arpav.model.Previsione;
import eu.lucazanini.arpav.network.BulletinRequest;
import eu.lucazanini.arpav.network.VolleySingleton;
import eu.lucazanini.arpav.task.ReportTask;
import timber.log.Timber;

import static eu.lucazanini.arpav.model.Meteogramma.SCADENZA_IDX;
import static eu.lucazanini.arpav.model.Previsione.MG_IDX;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by luke on 26/12/16.
 */

@RunWith(AndroidJUnit4.class)
public class BulletinRequestTest {

    @Test
    public void volleyDownload() {
        final Context appContext = InstrumentationRegistry.getTargetContext();

        ReportTask reportTask = new ReportTask(appContext);

//        reportTask.doRequest();

        final VolleySingleton volleyApp = VolleySingleton.getInstance(appContext);

        // Get the ImageLoader through your singleton class.
        final ImageLoader mImageLoader = volleyApp.getImageLoader();

//        RequestFuture<Previsione> future = RequestFuture.newFuture();

        final CountDownLatch latch = new CountDownLatch(1+MG_IDX*SCADENZA_IDX);

        BulletinRequest bulletinRequest = new BulletinRequest(Request.Method.GET, Previsione.URL_IT,
                new Response.Listener<Previsione>() {
                    @Override
                    public void onResponse(Previsione response) {

                        Timber.d("found previsione for %s", response.getDataAggiornamento());

                        assertThat(response.getDataEmissione(), containsString("alle"));
                        assertThat(response.getDataAggiornamento(), anyOf(is(nullValue()), containsString("alle")));

                        Meteogramma[] meteogrammi = response.getMeteogramma();
                        for (int i = 0; i < MG_IDX; i++) {
                            Meteogramma meteogramma = meteogrammi[i];
                            assertThat(meteogramma, is(notNullValue()));

                            Meteogramma.Scadenza[] scadenze = meteogramma.getScadenza();
                            for (int j = 0; j < SCADENZA_IDX; j++) {
                                Meteogramma.Scadenza scadenza = scadenze[j];
                                assertThat(scadenza, is(notNullValue()));
                                assertThat(scadenza.getData(), not(isEmptyOrNullString()));
                            }
                        }

                        if (response.getLanguage() == Previsione.Language.IT) {
                            Bollettino bollettino = response.getMeteoVeneto();
                            assertThat(bollettino, is(notNullValue()));
                            Bollettino.Giorno[] giorni = bollettino.getGiorni();
                            for (int i = 0; i < Bollettino.DAYS; i++) {
                                Bollettino.Giorno giorno = giorni[i];
                                assertThat(giorno, is(notNullValue()));
                            }
                        }

//                        assertThat(response.getDataEmissione(), containsString("alle2"));

   /*                     // Retrieves an image specified by the URL, displays it in the UI.
                        ImageRequest request = new ImageRequest(response.getMeteogramma()[0].getScadenza()[0].getSimbolo(),
                                new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap bitmap) {
                                        assertThat(bitmap, is(notNullValue()));
//                                        mImageView.setImageBitmap(bitmap);
                                    }
                                }, 0, 0, null,
                                new Response.ErrorListener() {
                                    public void onErrorResponse(VolleyError error) {
//                                        mImageView.setImageResource(R.drawable.image_load_error);
                                    }
                                });*/


                        for(int i = 0; i<MG_IDX; i++){
                            for(int j=0; j<SCADENZA_IDX; j++){
                                String imgUrl = response.getMeteogramma()[i].getScadenza()[j].getSimbolo();
                                mImageLoader.get(imgUrl, new ImageLoader.ImageListener() {
                                    @Override
                                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                        Timber.d("Image URL: " + response.getRequestUrl());
                                        assertThat(response, is(notNullValue()));
//                                Bitmap bitmap = response.getBitmap();
//                                assertThat(bitmap, is(notNullValue()));
                                        Timber.d("Image Load completed: "+ response.getRequestUrl());
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Timber.e("Image Load Error: " + error.getMessage());
                                    }
                                });
                            }
                        }


/*                        mImageLoader.get(response.getMeteogramma()[0].getScadenza()[0].getSimbolo(), new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                Timber.d("Image URL: " + response.getRequestUrl());
                                assertThat(response, is(notNullValue()));
//                                Bitmap bitmap = response.getBitmap();
//                                assertThat(bitmap, is(notNullValue()));
                                Timber.d("Image Load completed");
                                latch.countDown();
                            }

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Timber.e("Image Load Error: " + error.getMessage());
                            }
                        });*/


                        latch.countDown();

  /*
   // Access the RequestQueue through your singleton class.
                        VolleySingleton.getInstance(this).addToRequestQueue(request);

                        ImageLoader imageLoader = VolleySingleton.getInstance(appContext).getImageLoader();

                        Timber.d("getSimbolo %s", response.getMeteogramma()[0].getScadenza()[0].getSimbolo());

                        imageLoader.get(response.getMeteogramma()[0].getScadenza()[0].getSimbolo(), new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                Bitmap bitmap = response.getBitmap();
                                assertThat(bitmap, is(notNullValue()));
                            }

                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });*/

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Timber.e(error);
            }
        });

//        queue.add(bulletinRequest);

        volleyApp.addToRequestQueue(bulletinRequest);

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


/*        try {
//            Previsione response = future.get(10, TimeUnit.SECONDS); // Blocks for at most 10 seconds.
            Previsione response = future.get();
        } catch (InterruptedException e) {
            // Exception handling
        } catch (ExecutionException e) {
            // Exception handling
//        } catch (TimeoutException e) {
//            e.printStackTrace();
        }*/

//        try {
//            Thread.sleep(30000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

}
