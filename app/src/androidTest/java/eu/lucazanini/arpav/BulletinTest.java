package eu.lucazanini.arpav;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import eu.lucazanini.arpav.model.Previsione;
import timber.log.Timber;

import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BulletinTest {

//    @Test
//    public void hasBulletinAssets() {
//        String[] filePaths = getBulletinFiles();
//        assertTrue("NOT FOUND FILES IN ASSETS", filePaths != null && filePaths.length > 0);
//        for (String filePath : filePaths) {
//            Timber.d(filePath);
//        }
//    }

    private String dir;
    private Context appContext;

//    @Before
//    public void init(){
//        appContext = InstrumentationRegistry.getTargetContext();
//        dir = "test";
//    }

    @Before
    public void setUp() throws Exception {
        appContext = InstrumentationRegistry.getTargetContext();
        dir = "test";
    }

    @Test
    public void loadBulletin() {
//        final String dir = "test";
        String[] filePaths = getBulletinFiles();
        for (String filePath : filePaths) {
            assertTrue("NOT FOUND FILES IN ASSETS", filePaths != null && filePaths.length > 0);
            Timber.d(filePath);
            try {
                String text = readFile(dir+"/"+filePath);
                String url = getMockUrl(filePath);
                Previsione previsione = new Previsione(url, text);
                assertThat(previsione.getDataEmissione(), containsString("alle"));
                assertThat(previsione.getDataAggiornamento(), anyOf(is(nullValue()), containsString("alle")));
            } catch (IOException e) {
                Timber.d(e.getLocalizedMessage());
                fail("FILE NOT READ: "+filePath);
            }
        }
    }

    private String getMockUrl(String filePath){
        String mockUrl;
        if(filePath.contains("_en")){
            mockUrl=Previsione.URL_EN;
        } else if(filePath.contains("_fr")){
            mockUrl=Previsione.URL_FR;
        } else if(filePath.contains("_de")){
            mockUrl=Previsione.URL_DE;
        } else {
            mockUrl=Previsione.URL_IT;
        }
        return mockUrl;
    }

    private String readFile(String fileName) throws IOException {
//        final Context appContext = InstrumentationRegistry.getTargetContext();
        StringBuilder buf=new StringBuilder();
        InputStream inputStream=appContext.getAssets().open(fileName);
        BufferedReader in= new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String str;
        while ((str=in.readLine()) != null) {
            buf.append(str);
        }
        in.close();
        return buf.toString();
    }

    private String[] getBulletinFiles() {
//        final Context appContext = InstrumentationRegistry.getTargetContext();
//        final String dir = "test";
        AssetManager assetManager = appContext.getAssets();
        try {
            String[] filePaths = assetManager.list(dir);
            return filePaths;
        } catch (IOException e) {
            Timber.e(e.getLocalizedMessage());
            return null;
        }
    }

}
