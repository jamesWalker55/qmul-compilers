[CmdletBinding()]
param (
    [Parameter(Mandatory=$true)]
    [String]
    $inputfilepath
)
# $inputfilepath = ".\testsuite\parser\addedlet.test"

$ErrorActionPreference = "Stop"

# define paths
$testsdir = "testsuite\parser"
$testoutputdir = "tests\parser"
$parserresultpath = "temp.txt"
$run = "run.ps1"
$build = "build.ps1"

# make paths relative to this script
$testsdir = Join-Path $PSScriptRoot $testsdir
$testoutputdir = Join-Path $PSScriptRoot $testoutputdir
$parserresultpath = Join-Path $PSScriptRoot $parserresultpath
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
[IO.File]::WriteAllLines($parserresultpath, $parserresult)

delta $testoutput $parserresultpath
