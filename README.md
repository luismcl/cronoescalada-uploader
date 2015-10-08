# Cronoescalada.es Tracks Importer
Java application to upload gpx files to the incredible site Cronoescalada.es

Allow you to massive upload tour gpx files to the web, for example, you can request a zip with all you tracks from strava.com 
(using the Download your data button on the https://www.strava.com/settings/profile page), then unzip it and use this application 
to upload all this files to Cronoescalada. 


##Ussage

###Prerequisites
    Apache Maven > 3
    JDK 7
    
    Install GPX-Parser lib in maven:  Source in  (https://github.com/himanshu-soni/gpx-parser)
    ```
    mvn install:install-file -Dfile=GPS-library.jar -DgroupId=com.hs -DartifactId=gpx-parser -Dversion=1.0-SNAPSHOT -Dpackaging=jar    
    ```
    
### Build the project
    ```
    mvn clean install
    ```
### Import your tracks:
    
    ```
    mvn exec:java -Dexec.mainClass="com.luismiguelcisneros.cronoescalada.TracksImporter" -Dexec.args="<OPTIONS>"
    ```
    
    Where options are:
    -r A file with the name of the tracks that you want to upload. optional
    -d directory with the .gpx files
    -u cronoescalada username
    -p cronoescalada password 
    -b bike ID 
    -l number of tracks to upload
    
    example: Upload All the tracks on activities directory. 
    ```
    mvn exec:java -Dexec.mainClass="com.luismiguelcisneros.cronoescalada.TracksImporter" -Dexec.args="-d /home/user/activities -u CoolName -p CoolPassword -b 000"
    ```
    
Enjoy!
