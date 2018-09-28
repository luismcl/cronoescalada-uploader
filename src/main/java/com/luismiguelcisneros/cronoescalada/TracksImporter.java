package com.luismiguelcisneros.cronoescalada;

import com.hs.gpxparser.GPXParser;
import com.hs.gpxparser.modal.GPX;
import com.hs.gpxparser.modal.Track;
import org.apache.commons.cli.*;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lcisneros on 7/10/15.
 */
public class TracksImporter {


    private final GPXParser parser;
    private List<String> rides;
    private String tracksDirectory;

    private CronoescaladaRestClient client;

    public static void main(String[] args) throws Exception {

        String ridesFileName;
        String ridesDirectory;
        String user;
        String password;
        String bikeId;
        int limit = 100000;

        Options options = buildOptions();

        try {
            CommandLineParser parser = null;
            CommandLine cmdLine = null;

            parser = new BasicParser();
            cmdLine = parser.parse(options, args);
            if (cmdLine.hasOption("h")) {
                new HelpFormatter().printHelp(TracksImporter.class.getCanonicalName(), options);
                return;
            }

            ridesFileName = cmdLine.getOptionValue("r");

            ridesDirectory = cmdLine.getOptionValue("d");
            if (ridesDirectory == null) {
                throw new org.apache.commons.cli.ParseException("The directory is required");
            }

            user = cmdLine.getOptionValue("u");
            if (ridesDirectory == null) {
                throw new org.apache.commons.cli.ParseException("The user is required");
            }

            password = cmdLine.getOptionValue("p");
            if (ridesDirectory == null) {
                throw new org.apache.commons.cli.ParseException("The password is required");
            }

            bikeId = cmdLine.getOptionValue("b");
            if (bikeId == null) {
                throw new org.apache.commons.cli.ParseException("The bikeId is required");
            }

            if (cmdLine.hasOption(("l"))) {
                limit = Integer.parseInt(cmdLine.getOptionValue("l"));
            }


        } catch (org.apache.commons.cli.ParseException ex) {
            System.out.println(ex.getMessage());
            new HelpFormatter().printHelp(TracksImporter.class.getCanonicalName(), options);
            return;
        }

        List<String> rides = null;
        if (ridesFileName != null) {
            rides = getRides(ridesFileName);
        }

        TracksImporter tracksImporter = new TracksImporter(rides, ridesDirectory, user, password, bikeId);
        tracksImporter.importTracks(limit);
    }

    private static List<String> getRides(String ridesFileName) throws IOException {
        BufferedReader ridesFile = new BufferedReader(new FileReader(ridesFileName));

        List<String> rides = new ArrayList<String>();
        String line;
        while ((line = ridesFile.readLine()) != null) {
            rides.add(line);
        }
        return rides;
    }

    private static Options buildOptions() {
        Options options = new Options();
        options.addOption("u", "user", true, "Cronoescalada User Name.");
        options.addOption("p", "password", true, "Cronoescalada user Password.");
        options.addOption("b", "bike", true, "The bike ID in Cronoescalada");
        options.addOption("r", "rides-file-path", true, "The file with the name's rides that will be uploaded.");
        options.addOption("d", "rides-path", true, "The directory with the gpx files.");
        options.addOption("l", "limit", true, "limit of files to import");
        options.addOption("h", "help", false, "Print this help");
        return options;
    }

    public TracksImporter(List<String> rides, String ridesDirectory, String user, String password, String bikeId) throws IOException, URISyntaxException {

        this.rides = rides;
        this.tracksDirectory = ridesDirectory;
        client = new CronoescaladaRestClient(user, password, bikeId);
        this.parser = new GPXParser();
    }

    public void importTracks(int limit) throws Exception {

        File activitiesDir = new File(tracksDirectory);
        int i = 0;
        for (File gpxFile : activitiesDir.listFiles()) {
            GPX gpx = parser.parseGPX(new FileInputStream(gpxFile));
            if (gpx.getTracks().isEmpty()) {
                continue;
            }

            Track track = gpx.getTracks().iterator().next();
            if (rides == null || rides.contains(track.getName())) {
                client.uploadTrack(track, gpxFile);
                if (++i > limit)
                    break;
            }

        }

    }

}
