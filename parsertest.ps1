[CmdletBinding()]
param (
    [Parameter(Mandatory=$true)]
    [String]
    $inputfilepath
)
# $inputfilepath = ".\testsuite\parser\addedlet.test"

$ErrorActionPreference = "Stop"

$testsdir = "./testsuite/parser/"
$testoutputdir = "./tests/parser/"
$parserresultpath = "./temp.txt"

$inputfilename = Split-Path $inputfilepath -Leaf

$testfile = Join-Path $testsdir $inputfilename
$testoutput = Join-Path $testoutputdir "$inputfilename.out"

Write-Output "Testing this file: $testfile"
Write-Output "Matching with expected output at: $testoutput"

./build.ps1
$parserresult = & ./run.ps1 $testfile 2>&1
[IO.File]::WriteAllLines($parserresultpath, $parserresult)

delta $testoutput $parserresultpath
