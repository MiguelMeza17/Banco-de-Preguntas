# Detiene los componentes que arranco iniciar.ps1.
# Busca los procesos java que ejecutan un .jar de este proyecto (asi funciona aunque el comando
# 'java' del PATH sea un lanzador que crea otro proceso).
$raiz = $PSScriptRoot
$procesos = Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" |
        Where-Object { $_.CommandLine -and $_.CommandLine.Contains($raiz) -and $_.CommandLine -like '*.jar*' }

if (-not $procesos) {
    Write-Host 'No hay componentes del sistema en ejecucion.'
} else {
    foreach ($p in $procesos) {
        $jar = [regex]::Match($p.CommandLine, '[\w-]+-\d+\.\d+\.\d+\.jar').Value
        try {
            Stop-Process -Id $p.ProcessId -Force -ErrorAction Stop
            Write-Host "Detenido $jar (proceso $($p.ProcessId))"
        } catch {
            Write-Host "No se pudo detener el proceso $($p.ProcessId): $($_.Exception.Message)"
        }
    }
}
$archivo = Join-Path $raiz 'logs\pids.txt'
if (Test-Path $archivo) { Remove-Item $archivo -Force }
