FROM mcr.microsoft.com/dotnet/sdk:8.0

WORKDIR /app
COPY . .

RUN useradd -m -U sandbox
RUN chown -R sandbox:sandbox /app

USER sandbox

ENV DOTNET_ENVIRONMENT=Development
EXPOSE 8080
CMD ["dotnet", "watch", "run", "--project", "DotnetSandbox"]
