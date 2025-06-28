FROM openjdk:11
ADD Question-Bank.jar Question-Bank.jar
ENTRYPOINT ["java", "-jar","Question-Bank.jar"]
EXPOSE 8080


## Use a base image that has Maven and Java pre-installed
#FROM maven:3.8.4-openjdk-11
#
## Set the working directory inside the container
#WORKDIR /app
#
## Copy the contents of your Maven project to the container
#COPY . .
#
## Build your Maven project inside the container
#RUN mvn clean install
#
## Copy the JAR file built by Maven to the desired location
#RUN cp target/Question-Bank.jar ./Question-Bank.jar
#
## Expose the port your application will listen on (assuming it's 8080)
#EXPOSE 8080
#
## Define the command to run your application
#CMD ["java", "-jar", "Question-Bank.jar"]