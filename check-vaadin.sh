
#!/bin/bash

echo "🔄 Limpiando el proyecto..."
mvn clean -q

echo "🌳 Generando árbol de dependencias (deps.txt)..."
mvn dependency:tree -DoutputFile=deps.txt -DoutputType=text -q
echo "✅ Árbol guardado en deps.txt"

echo "📦 Verificando actualizaciones de dependencias..."
mvn versions:display-dependency-updates -q | tee updates.txt
echo "✅ Resultado guardado en updates.txt"

echo "🚀 Recompilando proyecto..."
mvn install -DskipTests -q

echo "🎉 Proceso terminado. Revisa:"
echo "   - deps.txt   → posibles conflictos de versiones"
echo "   - updates.txt → dependencias con versiones más nuevas"
