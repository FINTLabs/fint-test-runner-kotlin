FROM gradle:9.9.1-jdk-25-and-27 as builder
USER root
COPY . .
RUN gradle --no-daemon build

FROM gcr.io/distroless/java21
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/build/libs/fint-test-runner-kotlin*.jar /data/app.jar
CMD ["/data/app.jar"]
