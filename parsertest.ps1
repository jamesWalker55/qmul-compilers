[CmdletBinding()]
param (
    [Parameter(Mandatory=$true)]
    [String]
    $inputfilepath
)
# $inputfilepath = ".\testsuite\parser\addedlet.test"

# define paths
$testsdir = "testsuite\parser"
$testoutputdir = "tests\parser"
$parserresultpath = "delta_a.txt"
$realresultpath = "delta_b.txt"
$run = "run.ps1"
$build = "build.ps1"

# make paths relative to this script
$testsdir = Join-Path $PSScriptRoot $testsdir
$testoutputdir = Join-Path $PSScriptRoot $testoutputdir
$parserresultpath = Join-Path $PSScriptRoot $parserresultpath
$realresultpath = Join-Path $PSScriptRoot $realresultpath
$run = Join-Path $PSScriptRoot $run
$build = Join-Path $PSScriptRoot $build

# main script
$inputfilename = Split-Path $inputfilepath -Leaf

$testfile = Join-Path $testsdir $inputfilename
$testoutput = Join-Path $testoutputdir "$inputfilename.out"

Write-Output "Testing this file: $testfile"
Write-Output "Matching with expected output at: $testoutput"

./build.ps1
$parserresult = & ./run.ps1 $testfile 2>&1
$parserresult = $($parserresult | Select-String -Pattern "^ *#" -NotMatch)
[IO.File]::WriteAllLines($parserresultpath, $parserresult)

$realoutput = Get-Content $testoutput | Select-String -Pattern "^ *#" -NotMatch
[IO.File]::WriteAllLines($realresultpath, $realoutput)

delta $realresultpath $parserresultpath
