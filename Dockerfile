FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/stand-lib.jar /stand-lib/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/stand-lib/app.jar"]
