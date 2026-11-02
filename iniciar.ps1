# Compila y arranca todo el sistema distribuido del Corte 2 en este equipo.
#   .\iniciar.ps1              compila (sin pruebas) y arranca
#   .\iniciar.ps1 -ConPruebas  compila ejecutando todas las pruebas
#   .\iniciar.ps1 -SinCompilar arranca con los .jar ya compilados
#   .\iniciar.ps1 -SinNavegador no abre el navegador al terminar
# Los registros quedan en .\logs y los procesos se detienen con .\detener.ps1
param(
    [switch]$ConPruebas,
    [switch]$SinCompilar,
    [switch]$SinNavegador
)

$ErrorActionPreference = 'Stop'
$raiz = $PSScriptRoot
$logs = Join-Path $raiz 'logs'
New-Item -ItemType Directory -Force $logs | Out-Null

# Orden de arranque: primero el bus, luego los servicios, el gateway y el frontend.
$componentes = @(
    @{ Nombre = 'event-bus';              Puerto = 8070 },
    @{ Nombre = 'usuarios-service';       Puerto = 8081 },
    @{ Nombre = 'preguntas-service';      Puerto = 8082 },
    @{ Nombre = 'revision-service';       Puerto = 8083 },
    @{ Nombre = 'notificaciones-service'; Puerto = 8085 },
    @{ Nombre = 'api-gateway';            Puerto = 8080 },
    @{ Nombre = 'frontend-web';           Puerto = 3000 }
)

# Si quedo una ejecucion anterior, se detiene (ademas libera los .jar para poder recompilar).
& (Join-Path $raiz 'detener.ps1') | Out-Null
Start-Sleep -Seconds 1

if (-not $SinCompilar) {
    Write-Host 'Compilando los microservicios (la primera vez descarga dependencias y tarda unos minutos)...'
    $argumentos = @('-q', '-f', (Join-Path $raiz 'pom-microservicios.xml'), 'package')
    if (-not $ConPruebas) { $argumentos += '-DskipTests' }
    & (Join-Path $raiz 'mvnw.cmd') @argumentos
    if ($LASTEXITCODE -ne 0) { throw 'La compilacion fallo. Revise los mensajes de Maven.' }
}

# Avisa antes de arrancar si otro programa ya ocupa alguno de los puertos.
$ocupados = foreach ($c in $componentes + @(@{ Nombre = 'event-bus (broker)'; Puerto = 61616 })) {
    $conexion = Get-NetTCPConnection -State Listen -LocalPort $c.Puerto -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conexion) {
        $programa = (Get-Process -Id $conexion.OwningProcess -ErrorAction SilentlyContinue).ProcessName
        "  puerto $($c.Puerto) ($($c.Nombre)) lo usa el programa '$programa'"
    }
}
if ($ocupados) {
    Write-Host 'Hay puertos ocupados por otros programas:' -ForegroundColor Red
    $ocupados | ForEach-Object { Write-Host $_ }
    throw 'Cierre esos programas o cambie el puerto en el application.yml del servicio.'
}

# Espera a que el componente responda de verdad (no solo que el puerto este abierto).
function Esperar-Componente($c, [int]$segundos = 120) {
    $url = if ($c.Nombre -eq 'frontend-web') { "http://localhost:$($c.Puerto)/" } else { "http://localhost:$($c.Puerto)/actuator/health" }
    $limite = (Get-Date).AddSeconds($segundos)
    while ((Get-Date) -lt $limite) {
        try {
            $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 3
            if ($r.StatusCode -eq 200) { return }
        } catch { Start-Sleep -Milliseconds 800 }
    }
    throw "$($c.Nombre) no arranco. Revise logs\$($c.Nombre).log"
}

$pids = @()
foreach ($c in $componentes) {
    $carpeta = Join-Path $raiz $c.Nombre
    $jar = Get-ChildItem (Join-Path $carpeta 'target') -Filter '*.jar' |
            Where-Object { $_.Name -notlike '*.original' } | Select-Object -First 1
    if (-not $jar) { throw "No se encontro el .jar de $($c.Nombre). Ejecute sin -SinCompilar." }

    Write-Host ("Arrancando {0,-24} puerto {1}" -f $c.Nombre, $c.Puerto)
    $proceso = Start-Process java -ArgumentList '-jar', "`"$($jar.FullName)`"" `
        -WorkingDirectory $carpeta -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput (Join-Path $logs "$($c.Nombre).log") `
        -RedirectStandardError (Join-Path $logs "$($c.Nombre).err.log")
    $pids += $proceso.Id
    Set-Content -Path (Join-Path $logs 'pids.txt') -Value $pids

    # El bus debe estar listo antes que los servicios que publican y consumen eventos.
    if ($c.Nombre -eq 'event-bus') { Esperar-Componente $c }
}

foreach ($c in $componentes) { Esperar-Componente $c }

Write-Host ''
Write-Host 'Sistema listo:' -ForegroundColor Green
Write-Host '  Frontend web ........ http://localhost:3000'
Write-Host '  API Gateway ......... http://localhost:8080/api'
Write-Host '  Swagger preguntas ... http://localhost:8082/swagger-ui.html'
Write-Host '  Swagger revision .... http://localhost:8083/swagger-ui.html'
Write-Host '  Usuarios demo: admin, autor, revisor1, revisor2 (contrasena Demo#2026)'
if (-not $SinNavegador) { Start-Process 'http://localhost:3000' }
