
@echo off
echo  "Limpiando el proyecto..."
call mvn clean -q

echo 🌳 Generando árbol de dependencias (deps.txt)...
call mvn dependency:tree -DoutputFile=deps.txt -DoutputType=text -q
echo ✅ Árbol guardado en deps.txt

echo 📦 Verificando actualizaciones de dependencias...
call mvn versions:display-dependency-updates -q > updates.txt
echo ✅ Resultado guardado en updates.txt

echo 🚀 Recompilando proyecto...
call mvn install -DskipTests -q

echo 🎉 Proceso terminado. Revisa:
echo    - deps.txt   → posibles conflictos de versiones
echo    - updates.txt → dependencias con versiones más nuevas
pause
