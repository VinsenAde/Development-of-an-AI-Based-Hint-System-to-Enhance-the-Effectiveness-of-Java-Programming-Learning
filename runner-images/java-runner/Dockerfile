FROM openjdk:17-slim

# Create a secure sandboxed execution execution environment
RUN useradd -m -s /bin/bash sandboxuser
WORKDIR /sandbox

# Grant the non-privileged user access to the runtime directory
RUN chown -R sandboxuser:sandboxuser /sandbox
USER sandboxuser

CMD ["sh"]