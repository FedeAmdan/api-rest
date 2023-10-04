FROM openjdk:17-jdk as build
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN ./gradlew clean build

FROM openjdk:17-jdk
COPY --from=build /usr/src/myapp/build/libs/*.jar /usr/app/myapp.jar
ENTRYPOINT ["java", "-jar", "/usr/app/myapp.jar"]