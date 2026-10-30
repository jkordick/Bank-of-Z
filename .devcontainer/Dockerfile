FROM mcr.microsoft.com/devcontainers/base:ubuntu-24.04

# Ubuntu 24.04 ships Java 21 and Python 3.12 as packages: no SDK downloads, no compiling.
RUN apt-get update \
    && apt-get install -y --no-install-recommends openjdk-21-jre-headless python3 \
    && rm -rf /var/lib/apt/lists/*
