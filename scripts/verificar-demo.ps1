# Verifica que todo esté listo para la demo en el celular (depuración USB).
# Uso:  powershell -ExecutionPolicy Bypass -File scripts\verificar-demo.ps1 [-Puerto 8080]
# No modifica nada salvo crear el túnel `adb reverse` si hay un teléfono autorizado.
param([int]$Puerto = 8080)

$ErrorActionPreference = 'Continue'
$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
$fallos = 0

function Ok($m)   { Write-Host "  [OK]    $m" -ForegroundColor Green }
function Mal($m, $arreglo) { Write-Host "  [FALTA] $m" -ForegroundColor Red; Write-Host "          -> $arreglo" -ForegroundColor Yellow; $script:fallos++ }

Write-Host "`nVerificando la demo de SelahFinance`n"

# 1. adb + teléfono
if (-not (Test-Path $adb)) {
    Mal "No encuentro adb en $adb" "Instala Android SDK Platform-Tools (Android Studio > SDK Manager)."
} else {
    $lineas = & $adb devices | Select-Object -Skip 1 | Where-Object { $_.Trim() }
    $listos = @($lineas | Where-Object { $_ -match "`tdevice$" })
    $sinAutorizar = @($lineas | Where-Object { $_ -match "`tunauthorized$" })
    if ($listos.Count -ge 1) {
        Ok "Teléfono conectado y autorizado: $(($listos[0] -split "`t")[0])"
        if ($listos.Count -gt 1) { Write-Host "          (hay $($listos.Count) dispositivos; flutter run te pedirá elegir con -d <id>)" -ForegroundColor DarkYellow }
        # 2. túnel
        & $adb reverse "tcp:$Puerto" "tcp:$Puerto" | Out-Null
        if ((& $adb reverse --list) -match "tcp:$Puerto") { Ok "Túnel USB activo: teléfono 127.0.0.1:$Puerto -> PC :$Puerto" }
        else { Mal "No se pudo crear el túnel adb reverse" "Ejecuta: adb reverse tcp:$Puerto tcp:$Puerto" }
    } elseif ($sinAutorizar.Count -ge 1) {
        Mal "El teléfono está conectado pero SIN autorizar" "Desbloquéalo y acepta «¿Permitir depuración USB?» (o revoca autorizaciones y reconecta)."
    } else {
        Mal "No hay ningún teléfono conectado" "Cable de datos + Depuración USB activada + modo MTP (guía, paso 1)."
    }
}

# 3. backend
try {
    $salud = Invoke-RestMethod "http://localhost:$Puerto/actuator/health" -TimeoutSec 4
    if ($salud.status -eq 'UP') { Ok "Backend respondiendo en http://localhost:$Puerto (UP)" } else { Mal "Backend responde pero estado = $($salud.status)" "Revisa el log del backend y la base de datos." }
} catch {
    Mal "El backend no responde en el puerto $Puerto" "PostgreSQL encendido y luego: cd backend; `$env:SPRING_PROFILES_ACTIVE='dev,demo'; mvn spring-boot:run"
}

# 4. login de demo (comprueba que los datos de demo estén sembrados)
try {
    $cuerpo = @{ email = 'ana@selah.demo'; password = 'Demo1234!' } | ConvertTo-Json
    $r = Invoke-RestMethod "http://localhost:$Puerto/api/v1/auth/login" -Method Post -ContentType 'application/json' -Body $cuerpo -TimeoutSec 6
    if ($r.accessToken) { Ok "Login de demo correcto (ana@selah.demo, rol $($r.rol))" }
} catch {
    Mal "No se pudo iniciar sesión con ana@selah.demo" "Arranca el backend con SPRING_PROFILES_ACTIVE=dev,demo sobre una base vacía (guía, secciones 4 y 8)."
}

Write-Host ""
if ($fallos -eq 0) {
    Write-Host "Todo listo. Ahora:  cd mobile ; flutter run --dart-define=API_BASE_URL=http://127.0.0.1:$Puerto" -ForegroundColor Cyan
} else {
    Write-Host "$fallos punto(s) por resolver antes de la demo." -ForegroundColor Red
}
exit $fallos
