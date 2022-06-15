# vertxpractice
 vert.x learning. 
 has contained main necessary modules in a web application development.
 doesn't support cluster mode yet.

# RUN
    develop mode: run class 'pers.zcc.vertxprc.launch.Launcher'
    docker deploy:
        mvn clean install 
        docker build --tag vertxpractice .
        docker network create vertxp
        docker run --rm -d --network vertxp --network-alias vertxp-mysql --name vertxp-mysql -v verxp-mysql-data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=admin -e MYSQL_DATABASE=vertxp mysql:8.0.29
        docker run --rm -d --network vertxp --network-alias vertxp-mongodb -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=admin --name vertxp-mongodb mongo:latest
        docker run --rm --network vertxp --network-alias vertxprac -d -p 8888:8888 -p 8880:8880 --name vertxprac vertxpractice:latest