$port = 8080
$path = "D:\study\activity\EnglishPal"

$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add("http://+:$port/")
$listener.Start()

Write-Host "========================================"
Write-Host "EnglishPal 服务器已启动"
Write-Host "========================================"
Write-Host ""
Write-Host "在电脑浏览器打开: http://localhost:$port/englishpal.html"
Write-Host ""

# 获取本机 IP
$ips = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -notmatch "127.0.0.1|169.254" }).IPAddress
foreach ($ip in $ips) {
    Write-Host "在 iPhone Safari 打开: http://$ip`:$port/englishpal.html"
}
Write-Host ""
Write-Host "按 Ctrl+C 停止服务器"
Write-Host "========================================"

while ($listener.IsListening) {
    $context = $listener.GetContext()
    $request = $context.Request
    $response = $context.Response

    $reqPath = $request.Url.LocalPath.TrimStart('/')
    if ($reqPath -eq '') { $reqPath = 'index.html' }

    $fullPath = Join-Path $path $reqPath
    if (Test-Path $fullPath) {
        $content = [System.IO.File]::ReadAllBytes($fullPath)
        $ext = [System.IO.Path]::GetExtension($fullPath)
        $contentType = switch ($ext) {
            '.html' { 'text/html; charset=utf-8' }
            '.css'  { 'text/css' }
            '.js'   { 'application/javascript' }
            default { 'application/octet-stream' }
        }
        $response.ContentType = $contentType
        $response.OutputStream.Write($content, 0, $content.Length)
        Write-Host "200 - $reqPath"
    } else {
        $response.StatusCode = 404
        Write-Host "404 - $reqPath"
    }
    $response.Close()
}
