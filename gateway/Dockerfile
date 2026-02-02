FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy pre-built jars from the build context
COPY build/libs ./build/libs

EXPOSE 1000

ENV JAVA_OPTS="-Xms256m -Xmx1024m"

ENTRYPOINT ["sh", "-c", "\
  set -e; \
  set -- /app/build/libs/*.jar; \
  [ -e \"$1\" ] || { echo 'No jar found in /app/build/libs'; exit 1; }; \
  exec java $JAVA_OPTS -jar \"$1\" \
"]