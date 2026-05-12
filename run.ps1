$port = 8080
$root = "D:\study\activity\EnglishPal"
$ip = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -notmatch "127.0.0.1|169.254" }).IPAddress | Select-Object -First 1

$tcp = New-Object System.Net.Sockets.TcpListener([System.Net.IPAddress]::Any, $port)
$tcp.Start()

Write-Host "=== EnglishPal 服务器已启动 ==="
Write-Host "iPhone Safari 打开: http://${ip}:${port}/englishpal.html"
Write-Host "按 Ctrl+C 停止`n"

while ($true) {
    $client = $tcp.AcceptTcpClient()
    $stream = $client.GetStream()
    $reader = New-Object System.IO.StreamReader($stream)
    $line = $reader.ReadLine()

    $path = ""
    if ($line -match 'GET\s+/(\S*)') {
        $path = $Matches[1]
        if ($path -eq "") { $path = "index.html" }
    }

    $fullPath = Join-Path $root $path
    if (Test-Path $fullPath) {
        $content = [System.IO.File]::ReadAllBytes($fullPath)
        $header = "HTTP/1.1 200 OK`r`nContent-Type: text/html; charset=utf-8`r`nContent-Length: $($content.Length)`r`nAccess-Control-Allow-Origin: *`r`nConnection: close`r`n`r`n"
        $headerBytes = [System.Text.Encoding]::UTF8.GetBytes($header)
        $stream.Write($headerBytes, 0, $headerBytes.Length)
        $stream.Write($content, 0, $content.Length)
    } else {
        $errorMsg = "HTTP/1.1 404 Not Found`r`nContent-Length: 0`r`nConnection: close`r`n`r`n"
        $errorBytes = [System.Text.Encoding]::UTF8.GetBytes($errorMsg)
        $stream.Write($errorBytes, 0, $errorBytes.Length)
    }

    $stream.Close()
    $client.Close()
}
