FROM amazoncorretto:17-alpine-jdk
COPY ApplicationModule/target/*.jar ApplicationModule/app.jar
COPY audio/audios audio/audios
RUN apk add --no-cache ca-certificates && apk update && apk add ffmpeg
RUN apk update
RUN apk add ffmpeg
ENTRYPOINT ["java","-jar","ApplicationModule/app.jar"]