# Imagem base com OpenJDK 21 (Ubuntu Jammy)
FROM eclipse-temurin:21-jdk-jammy

# Instalar Node.js 20 LTS
RUN apt-get update && apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copiar os arquivos necessários do backend Java e frontend Node
COPY backend-java ./backend-java
COPY frontend-node ./frontend-node

# Compilar todo o Core POO Java
RUN mkdir -p backend-java/bin && \
    javac -encoding UTF-8 -d backend-java/bin $(find backend-java/src -name "*.java")

# Criar script de inicialização para orquestrar Java e Node
RUN printf '#!/bin/sh\n\
echo "===================================================================="\n\
echo "  Iniciando Core POO Java na porta 8080..."\n\
echo "===================================================================="\n\
java -cp backend-java/bin br.com.cesmac.medcheck.MedCheckApplication &\n\
sleep 2\n\
echo "===================================================================="\n\
echo "  Iniciando Gateway Web Node.js na porta $PORT..."\n\
echo "===================================================================="\n\
cd /app/frontend-node && node server.js\n' > /app/start.sh && chmod +x /app/start.sh

# Variáveis de ambiente padrão
ENV PORT=3000
ENV JAVA_API_BASE=http://127.0.0.1:8080

EXPOSE 3000

CMD ["/app/start.sh"]
