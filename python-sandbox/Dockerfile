FROM ubuntu:22.04

# Create non-root user
RUN useradd -m -U sandbox

WORKDIR /app

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    build-essential \
    python3-dev \
    && rm -rf /var/lib/apt/lists/*

# Install base requirements
RUN pip3 install --no-cache-dir python-dotenv flask[development] flask-cors

COPY .env .
RUN pip3 install --no-cache-dir $(grep PREINSTALL_LIBS .env | cut -d '"' -f2)

COPY app.py .

# Set ownership
RUN chown -R sandbox:sandbox /app

# Switch to non-root user
USER sandbox

CMD ["python3", "app.py"]
