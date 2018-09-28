package com.luismiguelcisneros.cronoescalada;

import com.hs.gpxparser.modal.Track;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lcisneros on 7/10/15.
 */
public class CronoescaladaRestClient {

    private static final String HOST = "http://www.cronoescalada.com";
    //private static final String HOST = "http://localhost:8080";
    private static String UPLOAD_URL = HOST + "/index.php/tracks/do_upload";

    private static CookieStore cookies;

    private String bikeId = null;

    public CronoescaladaRestClient(String userName, String password, String bikeID) throws IOException, URISyntaxException {

        this.bikeId = bikeID;
        URI loginUri = new URI(HOST + "/index.php/auth/login");

        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
        CookieStore cookieStore = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);

        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore).build();
        HttpPost post = new HttpPost(loginUri);


        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("login", userName));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("remember", "1"));
        post.setEntity(new UrlEncodedFormEntity(params));

        CloseableHttpResponse loginResponse = httpClient.execute(post, context);

        System.out.println("Login:" + loginResponse.getStatusLine().toString());
        loginResponse.getEntity().writeTo(System.out);
        cookies = context.getCookieStore();

    }

    private CloseableHttpClient prepareClient() throws DateParseException {

        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BEST_MATCH).build();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookies);

        return HttpClients.custom().setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookies).build();
    }

    private  HttpPost createPostObject() {

        HttpPost post = new HttpPost(UPLOAD_URL);
        post.setHeader("Connection", "keep-alive");
        post.setHeader("Cache-Control", "max-age=0");
        post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        post.setHeader("Content-Type", "multipart/form-data; boundary=------WebKitFormBoundaryCEI");
        post.setHeader("Referer", "http://www.cronoescalada.com/index.php/tracks/upload");
        post.setHeader("Accept-Encoding", "gzip, deflate");
        post.setHeader("Accept-Language", "en-US,en;q=0.8,es;q=0.6");

        return post;
    }

    public void uploadTrack(Track track, File gpxFile  ) throws DateParseException, IOException {
        System.out.println("Uploading " + track.getName());

        HttpPost post = createPostObject();

        MultipartEntityBuilder builder = buildEntity(track, gpxFile);
        post.setEntity(builder.build());

        CloseableHttpClient client = prepareClient();
        HttpResponse response = client.execute(post);
        System.out.println(String.format("Upladed %s response: %s", track.getName(), response.getStatusLine()));
        client.close();
    }

    private MultipartEntityBuilder buildEntity(Track track, File gpxFile) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("localizacion", "", ContentType.TEXT_PLAIN);

        builder.addBinaryBody("userfile", gpxFile, ContentType.APPLICATION_OCTET_STREAM, gpxFile.getName());
        builder.addTextBody("ruta", "C:\\fakepath\\" + gpxFile.getName(), ContentType.TEXT_PLAIN);
        builder.addTextBody("realizado", "on", ContentType.TEXT_PLAIN);
        builder.addTextBody("descripcion", track.getDescription() != null ? track.getDescription() : "", ContentType.TEXT_PLAIN);
        builder.addTextBody("titulo", track.getName(), ContentType.TEXT_PLAIN);
        builder.addTextBody("bici_id", bikeId, ContentType.TEXT_PLAIN);

        builder.setBoundary("------WebKitFormBoundaryCEI");
        return builder;
    }
}
