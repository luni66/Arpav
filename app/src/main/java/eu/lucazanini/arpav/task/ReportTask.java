package eu.lucazanini.arpav.task;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.lucazanini.arpav.xml.Previsione;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class ReportTask {


    public final static int MODE_OVERWRITE = 0;
    public final static int MODE_SKIP_IF_EXISTS = 1;
    public static final String IMAGES_FOLDER = "images";
    private Context context;

    public ReportTask(Context context) {
        this.context = context;
    }

    @DebugLog
    private void sendBroadcast(Previsione previsione) {
        Timber.d("SENDING BROADCAST");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("eu.lucazanini.arpav.TEST");
        broadcastIntent.setPackage(context.getPackageName());
        broadcastIntent.putExtra("Previsione", previsione);
        context.sendBroadcast(broadcastIntent);
    }

    @DebugLog
    /**
     * Loads the bulletin as BufferedInputStream depending on the format of argument {@code text}
     * if the argument is or not is an url belonging to the Arpav site
     *
     * @param text
     * @return the BufferedInputStream associated to the {@code text}
     */
    public BufferedInputStream getBulletin(String text) {
        Timber.d("GETTING BULLETIN");
        if (isFromArpav(text)) {
            return download(text);
        } else {
            return loadFromAssets(text);
        }
    }


    /**
     * Loads the bulletin saved in the device storage
     *
     * @param file
     * @return the BufferedInputStream associated to the argument
     */
    public BufferedInputStream load(String file) {
        BufferedInputStream bis = null;
        try {
            FileInputStream fis = context.openFileInput(file);
            bis = new BufferedInputStream(fis);
        } catch (FileNotFoundException e) {
            Timber.e("error loading file %s", e.toString());
        } finally {
            return bis;
        }
    }

    @DebugLog
    /**
     * Load the bulletin saved in Assets test folder; use only for test
     *
     * @param file
     * @return
     */
    public BufferedInputStream loadFromAssets(String file) {
        Timber.d("LOADING FROM ASSETS");

        AssetManager assManager = context.getAssets();

        InputStream is = null;
        BufferedInputStream bis = null;

        try {
            is = assManager.open(file);
        } catch (IOException e) {
            Timber.e(e.toString());
        }
        bis = new BufferedInputStream(is);

        return bis;
    }

    @DebugLog
    /**
     * Download the resource located at the url passed as argument and returns it as the BufferedInputStream
     *
     * @param uri
     * @return
     */
    public BufferedInputStream download(String uri) {
        InputStream is = null;
        BufferedInputStream bis = null;

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(uri).openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();

            int response = conn.getResponseCode();

            if (response == 200) {
                is = conn.getInputStream();
                bis = new BufferedInputStream(is);
            } else {
                Timber.i("Not connected to %s", uri);
            }
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

    /**
     * Saves the file in the device storage using {@code folder} and {@code file} for the path file
     *
     * @param stream the file to be saved
     * @param folder the folder where to put the file
     * @param file   the name of the file
     */
    public void save(BufferedInputStream stream, String folder, String file) {
        FileOutputStream outputStream;

        try {
            File rootDir = new File(context.getFilesDir(), folder);
            outputStream = new FileOutputStream(new File(rootDir, file));

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
    /**
     * Saves the file in the device storage using the argument as the path file
     *
     * @param stream the file to be saved
     * @param file   the name of the file
     */
    public void save(BufferedInputStream stream, String file) {
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

    /**
     * Saves image files in their specific folder eventually overwriting
     *
     * @param previsione
     */
    public void saveImages(Previsione previsione) {
        Previsione.Bollettino meteoVeneto = previsione.getMeteoVeneto();
        if (meteoVeneto != null) {
            prepareImageFolder();
            for (int i = 0; i < Previsione.Bollettino.DAYS; i++) {
                Previsione.Bollettino.Giorno giorno = previsione.getMeteoVeneto().getGiorni()[i];
                for (int j = 0; j < giorno.getImgIndex(); j++) {
                    Timber.d("SAVING IMAGE %s", j);
                    String imageUrl = giorno.getImageUrl(j);
                    BufferedInputStream imageBuf = download(imageUrl);
                    save(imageBuf, IMAGES_FOLDER, giorno.getImageFile(j));
                    try {
                        imageBuf.close();
                    } catch (IOException e) {
                        Timber.e(e.toString());
                    }
                }
            }
        }
    }

    /**
     * Saves image files in their specific folder; if mode is equal to MODE_OVERWRITE overwrites the file else skips if it exists
     *
     * @param previsione
     * @param mode       Specify {@code MODE_OVERWRITE} or {@code MODE_SKIP_IF_EXISTS}
     */
    public void saveImages(Previsione previsione, int mode) {
        prepareImageFolder(mode);
        for (int i = 0; i < Previsione.Bollettino.DAYS; i++) {
            Previsione.Bollettino.Giorno giorno = previsione.getMeteoVeneto().getGiorni()[i];
            for (int j = 0; j < giorno.getImgIndex(); j++) {
                String imageUrl = giorno.getImageUrl(j);
                switch (mode) {
                    case MODE_SKIP_IF_EXISTS:
                        if (checkFile(IMAGES_FOLDER + File.separator, getUrlFile(giorno.getImageUrl(j)))) {
                            break;
                        }
                    case MODE_OVERWRITE:
                    default:
                        BufferedInputStream imageBuf = download(imageUrl);
                        save(imageBuf, IMAGES_FOLDER, giorno.getImageFile(j));
                        try {
                            imageBuf.close();
                        } catch (IOException e) {
                            Timber.e(e.toString());
                        }
                }
            }
        }
    }

    /**
     * Checks if the file passed as argument exists
     *
     * @param file
     * @return
     */
    public boolean checkFile(String file) {
        String[] fileList = context.fileList();
        for (String s : fileList) {
            if (s.equals(file)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the file passed as argument exists in the spcified folder
     *
     * @param folder
     * @param file
     * @return
     */
    public boolean checkFile(String folder, String file) {
        File rootDir = new File(context.getFilesDir(), folder);
        File[] fileList = rootDir.listFiles();
        for (File f : fileList) {
            if (f.getName().equals(file)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the images folder if not exists and delete all existing image files
     */
    public void prepareImageFolder() {
        String folder = context.getFilesDir().getAbsolutePath() + File.separator + IMAGES_FOLDER;
        File imageFolder = new File(folder);
        if (imageFolder.exists()) {
            for (File file : imageFolder.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
        } else {
            imageFolder.mkdirs();
        }
    }

    /**
     * Creates the folder images if not exists and if the argument is not equal to {@code MODE_SKIP_IF_EXISTS} deletes all existing image files
     *
     * @param mode
     */
    public void prepareImageFolder(int mode) {
        String folder = context.getFilesDir().getAbsolutePath() + File.separator + IMAGES_FOLDER;
        File imageFolder = new File(folder);
        if (imageFolder.exists()) {
            if (mode != MODE_SKIP_IF_EXISTS) {
                for (File file : imageFolder.listFiles()) {
                    if (!file.isDirectory()) {
                        file.delete();
                    }
                }
            }
        } else {
            imageFolder.mkdirs();
        }
    }

    /**
     * Returns the name of the file from the full path
     *
     * @param url
     * @return
     */
    public String getUrlFile(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
        return fileName;
    }

    /**
     * Checks if the argument is an url belonging to the Arpav site
     *
     * @param text
     * @return
     */
    public boolean isFromArpav(String text) {
        Pattern p = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        Matcher m;
        m = p.matcher(text);
        boolean matches = m.matches();
        return matches;
    }

    @DebugLog
    public void doTask(Previsione.Language language, String asset) {
        Timber.d("DO TASK TEST");

        BufferedInputStream bis = null;

        Previsione previsione = new Previsione(language, asset);

        String url = previsione.getUrl();
        String file = previsione.getUri();

        bis = getBulletin(url);
        if (bis != null) {
            if (bis.markSupported()) {
                bis.mark(150000);
            }
            previsione.parse(bis);
            if (previsione != null) {
                try {
                    bis.reset();
                } catch (IOException e) {
                    bis = getBulletin(url);
                } finally {
                    save(bis, file);
                    saveImages(previsione);
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

        Timber.d("CALLING BROADCAST TEST");
        sendBroadcast(previsione);

    }

    /**
     * Does all the job, download the bulletin and images and saves them in the device storage
     */
    public void doTask(Previsione.Language language) {

        Timber.d("DO TASK");

        BufferedInputStream bis = null;

//        Previsione previsione = new Previsione();

        Previsione previsione = new Previsione(language);

        String url = previsione.getUrl();
        String file = previsione.getUri();

        if (checkFile(file)) {
            Timber.i("loading bulletin from the internal storage");
            bis = load(file);
            previsione.parse(bis);

            if (!previsione.isUpdate()) {
                Timber.i("the bulletin is not update, searching online");
                bis = getBulletin(url);
                if (bis != null) {
                    if (bis.markSupported()) {
                        bis.mark(150000);
                    }
                    previsione.parse(bis);
                    if (previsione != null) {
                        try {
                            bis.reset();
                        } catch (IOException e) {
                            bis = getBulletin(url);
                        } finally {
                            save(bis, file);
                            saveImages(previsione);
                        }
                    }
                }
            } else {
                Timber.i("the bulletin is update");
                saveImages(previsione, MODE_SKIP_IF_EXISTS);
            }
        } else {
            Timber.i("Not found bulletin in the device, searching online");
            bis = getBulletin(url);
            if (bis != null) {
                if (bis.markSupported()) {
                    bis.mark(150000);
                }
                previsione.parse(bis);
                if (previsione != null) {
                    try {
                        bis.reset();
                    } catch (IOException e) {
                        bis = getBulletin(url);
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

        Timber.d("CALLING BROADCAST");
        sendBroadcast(previsione);
    }

    /**
     * Executes the inner class AsyncTask
     */
    public void execute() {
        ReportAsyncTask reportAsyncTask = new ReportAsyncTask();
        reportAsyncTask.execute(Previsione.Language.IT);
    }

    /**
     * Executes the inner class AsyncTask for test
     */
    public void test(String asset) {
        ReportAsyncTask reportAsyncTask = new ReportAsyncTask(asset);
        reportAsyncTask.execute(Previsione.Language.IT);
    }

    /**
     * The AsyncTask class that executes the {@link #doTask(Previsione.Language language)} method
     */
    public class ReportAsyncTask extends AsyncTask<Previsione.Language, Void, Void> {

        private boolean isTest;
        private String asset;

        public ReportAsyncTask() {
            isTest = false;
        }

        // only for test
        public ReportAsyncTask(String asset) {
            isTest = true;
            this.asset = asset;
        }


        @Override
        protected Void doInBackground(Previsione.Language... params) {

            Previsione.Language language = params[0];

            if (isTest) {
                doTask(language, asset);
            } else {
                doTask(language);
            }

            return null;
        }
    }

}