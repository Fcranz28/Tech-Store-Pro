param(
    [string]$Tag = 'localhost/techstore-backend:latest',
    [string]$RegistryImage = ''
)

$ErrorActionPreference = 'Stop'
$backendRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$podmanCommand = Get-Command podman -ErrorAction SilentlyContinue
$podmanExecutable = if ($podmanCommand) { $podmanCommand.Source } else { 'C:\Program Files\RedHat\Podman\podman.exe' }
if (!(Test-Path -LiteralPath $podmanExecutable)) { throw 'Podman no está instalado o no se encuentra en PATH.' }

& $podmanExecutable info --format '{{.Host.Arch}}'
if ($LASTEXITCODE -ne 0) { throw 'Inicia la máquina Podman. Si acabas de habilitar WSL2, reinicia Windows primero.' }

& $podmanExecutable build --platform linux/amd64 --format docker --file (Join-Path $backendRoot 'Dockerfile') --tag $Tag $backendRoot
if ($LASTEXITCODE -ne 0) { throw 'No se pudo construir la imagen del backend.' }

if ($RegistryImage) {
    & $podmanExecutable tag $Tag $RegistryImage
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo etiquetar la imagen para el registro.' }
    & $podmanExecutable push $RegistryImage
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo publicar la imagen. Comprueba el login y los permisos del registro.' }
}

Write-Output "Imagen construida: $Tag"
