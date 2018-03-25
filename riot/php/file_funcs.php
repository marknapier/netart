<?php


/*--------------------------- Functions --------------------------*/

function makeNextFilename($name) {
	$objDateTime = new DateTime('NOW');
	$datetime_string = $objDateTime->format('Ymd-His');
	return $datetime_string . "_" . ($name!=null && $name!=""? $name : "test");
}

function writeFile($stringData, $filename) {
	$myFile = $filename? $filename : makeNextFilename('riot_data');

	//
//Warning: fopen(../cache/http___www_yansite_jp_.hdata): failed to open stream: No such file or directory in C:\xampp2\htdocs\riot\php\file_funcs.php on line 14
//save.php writeToFile(): can't open file ../cache/http___www_yansite_jp_.hdata

	$fh = fopen($myFile, 'w') or die("save.php writeToFile(): can't open file " . $myFile);
	fwrite($fh, $stringData);
	fclose($fh);
}

/*
 * Return file contents as an array of strings, or null if file open fails
 */
function loadFile($filename) {
	try {
		$lines = file($filename);
	} catch (Exception $e) {
	    print 'loadFile() exception: ' . $e->getMessage() . "\n";
	}
	return $lines === FALSE ? null : $lines;
}

// use:
//    $files1 = scandir($dir);   // array
//
function listFolderFiles($folderName) {
	$first = 1;

	print("[\n");

	if ($handle = opendir($folderName)) {
	    while (false !== ($file = readdir($handle))) {
			if ($file != "." && $file != "..") {
				if (!$first) print(",");
				print('"' . $file . '"');
				print(" \n");
				$first = 0;
			}
	    }
	    closedir($handle);
	}

	print("]\n");
}

/*
function filterchars($str) {
	// strip whitespace, allow only legal chars
	$str = preg_replace('/\s\s+/', '', $str);
	$str = preg_replace('/[^0-9a-zA-Z\.\-\_]/', '', $str);
	return $str;
}


function filterdata($str) {  // allow newline, comma, colon
	// strip whitespace, allow only legal chars
	$str = preg_replace('/\s\s+/', '', $str);
	$str = preg_replace('/[^0-9a-zA-Z\.\-\_\n\,\:]/', '', $str);
	return $str;
}
*/

?>