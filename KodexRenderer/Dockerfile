# syntax=docker/dockerfile:1.7
#
# KodexRenderer: renders Kodices screens to e-ink PNGs over HTTP.
#
# Build from the repository root (the renderer depends on :Kodices and :Piktographs):
#   docker build -f KodexRenderer/Dockerfile -t kodex-renderer .
#   docker buildx build -f KodexRenderer/Dockerfile --platform linux/amd64,linux/arm64 -t kodex-renderer .
#
# Run:
#   docker run -p 8090:8090 -v "$PWD/KodexRenderer/screens:/app/screens:ro" kodex-renderer

# The fat jar is plain bytecode plus the Skiko natives for both linux-x64 and linux-arm64, so it is
# built once on the build machine's own architecture, not under emulation for each target.
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :KodexRenderer:buildFatJar --no-daemon --console=plain \
    && cp KodexRenderer/build/libs/kodex-renderer.jar /kodex-renderer.jar

# glibc-based JRE: Skiko's natives do not run on musl (Alpine).
FROM eclipse-temurin:21-jre
# Skiko's Linux native links against fontconfig, libGL and libEGL even when only the CPU raster backend is
# used. The base image's system fonts are removed on purpose: the renderer ships its own, and any
# text that slipped through to a system font shows up as missing instead of silently differing.
RUN apt-get update \
    && apt-get install -y --no-install-recommends libfontconfig1 libgl1 libegl1 \
    && rm -rf /var/lib/apt/lists/* \
    # Delete the files rather than purging fonts-*: fontconfig depends on a font package.
    && find /usr/share/fonts -type f -delete

# Skiko unpacks its native library into $HOME/.skiko on first use, so the user needs a writable home.
RUN useradd --uid 10001 --create-home --home-dir /home/kodex kodex
WORKDIR /app
COPY --from=build /kodex-renderer.jar /app/kodex-renderer.jar
COPY --chown=kodex KodexRenderer/screens /app/screens

ENV KODEX_SCREENS_DIR=/app/screens \
    KODEX_PORT=8090 \
    JAVA_TOOL_OPTIONS="-Djava.awt.headless=true -XX:MaxRAMPercentage=75"

USER kodex
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/kodex-renderer.jar"]
