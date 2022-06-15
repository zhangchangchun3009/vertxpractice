FROM openjdk:8-jre-alpine
ENV VERTICLE_FILE vertxpractice-1.0.0-SNAPSHOT-fat.jar
# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles
EXPOSE 8880
EXPOSE 8888
# Copy your fat jar to the container
COPY target/$VERTICLE_FILE $VERTICLE_HOME/
# Launch the verticle
WORKDIR $VERTICLE_HOME
ENV VERTX_ID=vertxpractice
ENV INSTANCES=10
ENV DB_MYSQL_URI=mysql://verxp-mysql:3306/vertxp?useSSL=false&allowMultiQueries=true
ENV DB_MYSQL_USERNAME=develop
ENV DB_MYSQL_PASSWORD=admin
ENV DB_MONGO_URI=mongodb://root:admin@vertxp-mongodb:27017/?connectTimeoutMS=30000&maxPoolSize=500

CMD java -jar $VERTICLE_FILE -Dvertx-id=$VERTX_ID -instances $INSTANCES -Ddb.mongodb.uri=$DB_MONGO_URI -Ddb.mysql.uri=$DB_MYSQL_URI -Ddb.mysql.username=$DB_MYSQL_USERNAME -Ddb.mysql.password=$DB_MYSQL_PASSWORD