FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy pre-built jars from the build context
COPY app/build/libs ./build/libs

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx1024m -Dliquibase.secureParsing=false"

ENTRYPOINT ["sh", "-c", "\
  set -e; \
  set -- /app/build/libs/*.jar; \
  [ -e \"$1\" ] || { echo 'No jar found in /app/build/libs'; exit 1; }; \
  exec java $JAVA_OPTS -jar \"$1\" \
"]
