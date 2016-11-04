package eu.lucazanini.arpav.task;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import eu.lucazanini.arpav.xml.Previsione;
import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Created by luke on 02/11/16.
 */

public class ReportAsyncTask extends AsyncTask<String, Void, Void> {

    private Context context;

    public ReportAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        BufferedInputStream bis = null;

        String url = params[0];
        String file = params[1];

        Previsione previsione = new Previsione();

        if (checkFile(file)) {
            Timber.d("loading from the internal storage");
            bis = load(file);
            previsione.parse(bis);

            if (!previsione.isUpdate()) {
                Timber.d("loading from the net");
                bis = download(url);
                if (bis != null) {
                    if (bis.markSupported()) {
                        bis.mark(150000);
                    }
                    previsione.parse(bis);
                    if (previsione != null) {
                        try {
                            bis.reset();
                        } catch (IOException e) {
                            bis = download(url);
                        } finally {
                            save(bis, file);
                            saveImages(previsione);
                        }
                    }
                }
            } else {
                Timber.d("previsione is still update");
            }
        } else {
            Timber.d("loading from the net");
            bis = download(url);
            if (bis != null) {
                if (bis.markSupported()) {
                    bis.mark(150000);
                }
                previsione.parse(bis);
                if (previsione != null) {
                    try {
                        bis.reset();
                    } catch (IOException e) {
                        bis = download(params[0]);
                    } finally {
                        save(bis, file);
                        saveImages(previsione);
                    }
                }
            }
        }
        if (bis != null) {
            try {
                bis.close();
            } catch (IOException e) {
                Timber.e("error closing buffer %s", e.toString());
            }
        }
        return null;
    }

    private boolean checkFile(String myFile) {
        String[] fileList = context.fileList();
        for (String s : fileList) {
            if (s.equals(myFile)) {
                return true;
            }
        }
        return false;
    }

    @DebugLog
    private BufferedInputStream load(String myFile) {
        BufferedInputStream bis = null;
        try {
            FileInputStream fis = context.openFileInput(myFile);
            bis = new BufferedInputStream(fis);
        } catch (FileNotFoundException e) {
            Timber.e("error loading file %s", e.toString());
        } finally {
            return bis;
        }
    }

    @DebugLog
    private BufferedInputStream download(String myurl) {
        InputStream is = null;
        BufferedInputStream bis = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            int response = conn.getResponseCode();

            is = conn.getInputStream();

            bis = new BufferedInputStream(is);

        } catch (ProtocolException e) {
            Timber.e("error loading url %s", e.toString());
            bis = null;
        } catch (MalformedURLException e) {
            Timber.e("error loading url %s", e.toString());
            bis = null;
        } catch (IOException e) {
            Timber.e("error loading url %s", e.toString());
        } finally {
            return bis;
        }
    }

    @DebugLog
    private void prepareImageFolder() {
        String folder = context.getFilesDir().getAbsolutePath() + File.separator + "images";
        Timber.d("image folder %s", folder);
        File imageFolder = new File(folder);
        if (imageFolder.exists()) {
            Timber.d("image folder exists");
            for (File file : imageFolder.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
        } else {
            Timber.d("image folder doesn't exist");
            imageFolder.mkdirs();
        }
    }

    @DebugLog
    private void saveImages(Previsione previsione) {
        prepareImageFolder();
        String imageDir = "images";
        for (int i = 0; i < Previsione.Bollettino.DAYS; i++) {
            Previsione.Bollettino.Giorno giorno = previsione.getMeteoVeneto().getGiorni()[i];
            for (int j = 0; j < giorno.getImgIndex(); j++) {
                String imageUrl = giorno.getImageUrl(j);
                BufferedInputStream imageBuf = download(imageUrl);
                save(imageBuf, imageDir, previsione.getMeteoVeneto().getGiorni()[i].getImageFile(j));
                try {
                    imageBuf.close();
                } catch (IOException e) {
                    Timber.e(e.toString());
                }
            }
        }
    }

    @DebugLog
    private void save(BufferedInputStream stream, String folder, String file) {
        FileOutputStream outputStream;

        try {
            File rootDir = new File(context.getFilesDir(), folder);
            outputStream = new FileOutputStream(new File(rootDir, file));
//            outputStream = context.openFileOutput(file, Context.MODE_PRIVATE);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = stream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Timber.e("error saving file %s", e.toString());
        }
    }

    @DebugLog
    private void save(BufferedInputStream stream, String file) {
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(file, Context.MODE_PRIVATE);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = stream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Timber.e("error saving file %s", e.toString());
        }
    }

}