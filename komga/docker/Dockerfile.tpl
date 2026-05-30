FROM eclipse-temurin:17-jre AS builder
ARG JAR={{distributionArtifactFile}}
WORKDIR /builder
COPY assembly/${JAR} application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM debian:bookworm-slim AS build-linux
ARG TARGETARCH
ENV JAVA_HOME=/opt/java/openjdk
COPY --from=eclipse-temurin:25-jre-jammy $JAVA_HOME $JAVA_HOME
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ARG GALLERY_DL_REV=local
RUN --mount=type=cache,target=/var/cache/apt,id=apt-cache-${TARGETARCH},sharing=locked \
    --mount=type=cache,target=/var/lib/apt,id=apt-lib-${TARGETARCH},sharing=locked \
    --mount=type=cache,target=/root/.cache/pip,id=pip-${TARGETARCH} \
    echo "gallery-dl-komga rev: ${GALLERY_DL_REV}" && \
    apt-get -y update && \
    apt-get -y install --no-install-recommends \
      ca-certificates libheif1 libwebp7 libarchive13 \
      curl python3 python3-pip && \
    pip3 install --break-system-packages --no-cache-dir --force-reinstall \
      https://github.com/08shiro80/gallery-dl-komga/archive/refs/heads/master.tar.gz && \
    KEPUBIFY_ARCH=$([ "$TARGETARCH" = "amd64" ] && echo "64bit" || echo "$TARGETARCH") && \
    curl -sL --retry 3 \
      "https://github.com/pgaskin/kepubify/releases/latest/download/kepubify-linux-${KEPUBIFY_ARCH}" \
      -o /usr/bin/kepubify && chmod +x /usr/bin/kepubify && \
    apt-get purge -y --auto-remove curl python3-pip && \
    rm -rf /var/lib/apt/lists/*
ENV LD_LIBRARY_PATH="/usr/lib"

# amd64
FROM build-linux AS build-amd64
ENV LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:/usr/lib/x86_64-linux-gnu"

# arm64
FROM build-linux AS build-arm64
ENV LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:/usr/lib/aarch64-linux-gnu"

FROM build-${TARGETARCH} AS runner
VOLUME /config
WORKDIR /app
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./
ENV KOMGA_CONFIGDIR="/config"
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
ENTRYPOINT ["java", "-Dspring.profiles.include=docker", "--enable-native-access=ALL-UNNAMED", "-jar", "application.jar", "--spring.config.additional-location=file:/config/"]
EXPOSE 25600
LABEL org.opencontainers.image.source="https://github.com/08shiro80/komga-enhanced"
