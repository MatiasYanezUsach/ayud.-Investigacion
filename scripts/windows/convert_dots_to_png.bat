@echo off
REM Ejecutar siempre desde la raiz del repositorio (dos niveles arriba de scripts\windows)
cd /d "%~dp0..\.."
echo ========================================
echo Convirtiendo archivos .dot a .png
echo ========================================
echo.

REM Convertir todos los archivos job.*.BestIndividual.dot (ya reparados por el programa)
for /r "out\results" %%f in (job.*.BestIndividual.dot) do (
    echo Convirtiendo: %%~nxf
    tools\graphviz-2.38\bin\dot.exe -Tpng "%%f" -o "%%~dpnf.png" 2>nul
    if exist "%%~dpnf.png" (
        echo   [OK] PNG generado: %%~nxf.png
    ) else (
        echo   [ERROR] No se pudo generar el PNG para %%~nxf
    )
    echo.
)

echo.
echo ========================================
echo Conversion completada
echo ========================================
echo.
echo Las imagenes PNG estan en: out\results\
echo.
pause
