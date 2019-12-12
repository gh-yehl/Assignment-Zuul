FROM java:8
VOLUME /tmp
ADD zuul-server-1.0-SNAPSHOT.jar zuul-server.jar
ENTRYPOINT ["java","-Xms200m","-Xmx300m","-jar","/zuul-server.jar"]