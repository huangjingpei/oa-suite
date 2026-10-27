$results = @()
$files = Get-ChildItem -Path "E:\OA\oa-suite\oa-front-service\src\api" -Filter "*.js"
foreach ($f in $files) {
    $content = Get-Content $f.FullName -Raw
    $pattern = '(?:getAction|postAction|putAction|deleteAction|httpAction)\s*\(\s*[`''"]([^`''"]+)[`''"]'
    $regex = [regex]$pattern
    $matches = $regex.Matches($content)
    foreach ($m in $matches) {
        $u = $m.Groups[1].Value
        $results += [PSCustomObject]@{
            File = $f.Name
            URL = $u
        }
    }
}

# Also scan views in oa-front-service for direct URL calls
$views = Get-ChildItem -Path "E:\OA\oa-suite\oa-front-service\src\views" -Recurse -Filter "*.vue"
foreach ($v in $views) {
    $content = Get-Content $v.FullName -Raw
    $pattern = '(?:getAction|postAction|putAction|deleteAction|httpAction)\s*\(\s*[`''"]([^`''"]+)[`''"]'
    $regex = [regex]$pattern
    $matches = $regex.Matches($content)
    foreach ($m in $matches) {
        $u = $m.Groups[1].Value
        $results += [PSCustomObject]@{
            File = $v.Name
            URL = $u
        }
    }
}

$results | Select-Object -Unique URL, File | Export-Csv -Path "E:\OA\oa-suite\oa-dev\docs\legacy_frontend_calls.csv" -NoTypeInformation -Encoding utf8
Write-Output "Total frontend API calls found: $(($results | Select-Object -Unique URL).Count)"
