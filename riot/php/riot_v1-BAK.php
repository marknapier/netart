<?php

require_once("file_funcs.php");
require_once("html_guts.php");

$RIOT_TITLE = "Riot 1.2";
$RIOT_URL_ROOT = "/riot/php/riot_v1.php";
$RIOT_URL = $RIOT_URL_ROOT . "?url=";
$RIOT_WIDTH_PIXELS = "1000";   // Riot 1.0 (1998) was designed to fit an 800x600 pixel window (see setDisplayScale())

$mergedWords = array();  // holds words for random sentence
$pageNum = 0;		// number of html page being output
$z = 10;		// z-index

initRandomGenerator();
$URLstring = sanitizeUrl(getUserURL());

startHTMLpage();
startFrame($URLstring);
riot($URLstring);
endFrame();
endHTMLpage();

//--------------------------------------------------------------------------------------------------

// Riot starts here
function riot($url) {
	if (isset($url) && $url !== '') {
		// parse and cache the given url and any further nested urls
		$urlParts = testURL(sanitizeURL($url));
		if ($urlParts === FALSE) {
			errorExit('Not a valid URL');
		}
		else {
			$safeURL = buildURL($urlParts);
			processURL($safeURL);
		}
	}
	// riotously render the three latest urls
	showTheRiotPage();		
}

// Parse the html from this url and store results into a cache file
function processURL($riotURL) {
	// limit how many urls we'll process
	$maxUrls = 7;
	$urlCount = 0;
	$elements = null;

	// push URL onto stack
	$URLstack = array($riotURL);

	// riot() may push more URLs onto stack (if processing a frameset) so loop until all are urls are processed
	while (($url = array_shift($URLstack)) && $urlCount++ < $maxUrls) {
		$elementsForOnePage = parseHTML($url, $URLstack);
		if ($elements == null) {
			// html elements from first url
			$elements = $elementsForOnePage;
		}
		else if ($elementsForOnePage != null) {
			// subsequent html elements (if any) are merged into first
			$elements = mergeElements($elements, $elementsForOnePage);
		}
	}

	if ($elements != null) {
		addToCache($elements, $riotURL);
		logURL($riotURL);
	}
}

// Load the last three web pages from cache and display them
function showTheRiotPage() {
	$threeUrls = latestUrls();
	$threeUrlsStr = "1) " . $threeUrls[0] . " &nbsp;&nbsp; 2) " . $threeUrls[1] . " &nbsp;&nbsp; 3) " . $threeUrls[2] ;

	// start the shredded output div and set the scale to imitate an 800 pixel width screen
	print "<div id='riot-output'>\n";
	print "<script>	setDisplayScale(); </script>\n";
	print "<span style='font-family:Arial; font-size:10px; color:#008000;'>$threeUrlsStr</span>\n";

	// Load and render the cache files for last three urls
	while (($url = array_pop($threeUrls))) {  // go from oldest url to newest
		$fileContents = getFromCache($url);
		$elements = makeElementsFromFileContents($fileContents);
		outputRiotHTML($elements, $url, $threeUrls);
	}

	// Set background color once, not for each url
	$bodyBGC = isset($elements['bodyBGColor']) ? $elements['bodyBGColor'] : '#fff';
	// Set body BG color
	print "<SCRIPT>document.body.style.backgroundColor = '" . $bodyBGC . "';</SCRIPT>\n";
	// Set page title and location from most recent url
	print "<SCRIPT>setPageTitle('" . $elements['title'] . "'); setPageLocation('" . $url . "');</SCRIPT>\n";
	// close the output container div
	print "</div><!-- end of riot-output -->\n";
}

// Convert text data from cache file into $elements (array of html elements)
function makeElementsFromFileContents($lines) {
	$elements = array(
		'frames' => array(),
		'links' => array(),
		'linkTexts' => array(),
		'images' => array(),
		'bgimages' => array(),
		'colors' => array(),
		'videos' => array(),
		'mp4s' => array(),
		'title' => '',
		'base' => '',
		'text' => ''
	);
	foreach($lines as $line) {
		// remove newline at end
		$line = substr($line, 0, -1);

		// break line into two parts on '=='
		$lineParts = preg_split("/==/", $line);
		$left = $lineParts[0];
		if (sizeof($lineParts) <= 1) {
			guts("ERROR LINEPARTS", $lineParts);
		}
		else {
			$right = $lineParts[1];
		}

		// process each type of line
		if ($left === "f") {
			array_push($elements['frames'], $right); 
			print "<!Got FRAME from file: $right>\n"; 
		}   
		else if ($left === "l") {
			// example:  "http://marknapier.com/press||Press"
			$lineParts = preg_split("/\|\|/", $right);
			array_push($elements['links'], $lineParts[0]);
			array_push($elements['linkTexts'], (sizeof($lineParts) > 1 ? $lineParts[1] : ""));
		}
		else if ($left === "bgimg" && $right !== "") {
			array_push($elements['bgimages'], $right);
			array_push($elements['images'], $right);
		}
		else if ($left === "i") {
		    array_push($elements['images'], $right);
		}
		else if ($left === "c") {
		    array_push($elements['colors'], $right);
		}
		else if ($left === "title") {
			$elements['title'] = $right;
		}
		else if ($left === "base") {
			$elements['base'] = $right;
		}
		else if ($left === "t") {
		    $elements['text'] = $right;
		}
	}
	return $elements;
}

// Parse a single html page.
// Return an array of parsed html elements or null if the url is malformed
function parseHTML($url, &$URLstack) {
	$elements = null;
	// clean and parse the URL
	$urlParts = testURL(sanitizeURL($url));
	if ($urlParts) {
		$elements = array();
		// get the html headers
		$safeURL = buildURL($urlParts);
		$headers = getHeaders($safeURL);

		// get the actual page url (might change if $url redirected to another page)
		$safeURL = makeFullHref($headers['location'], $urlParts);

		// get the html page 
		$html = getHTML($safeURL);

		if ($html !== FALSE) {
			// parse images, links, etc. from the html
			$elements = getHTMLElements($html, $safeURL);

			// add frameset urls onto stack (this will shred pages nested in frames)
			foreach($elements['frames'] as $frameURL) {
				array_push($URLstack, $frameURL);
			}			
		}
	}
	return $elements;
}

// remove newlines and double spaces
function cleanString($str) {
	$str = trim(preg_replace('/\s+/', ' ', $str));
	$str = trim(preg_replace('/[\n\r]/', '', $str));
	return $str;
}

// save the parsed html values to a file
function addToCache($elements, $safeURL) {
	if ($elements != null) {
		$date = date("Y.m.d-H:i:s");
		$someText = isset($elements['text']) ? $elements['text'] : "";

		$cachedir = "../cache";
		$dataFileName = $cachedir . "/" . makeFname($safeURL);
		$outText = "url==$safeURL\n" .
			"base==" . $elements['base'] . "\n" .
			"date==" . $date . "\n" .
			"title==" . cleanString($elements['title']) . "\n" .
			"bgimg==" . (isset($elements['bodyBGImage']) ? $elements['bodyBGImage'] : "") . "\n" ;
		foreach($elements['frames'] as $frame) {
			$outText .= "f==$frame\n"; 
		}
		foreach($elements['images'] as $image) {
			$outText .= "i==$image\n"; 
		}
		foreach($elements['links'] as $link) {
			$link = cleanString($link);   // sometimes links end in a newline, so clean it first
			$linkText = cleanString(array_shift($elements['linkTexts']));
			$outText .= "l==$link||$linkText\n";    // !!!!! add linktext here $link||$linkText
		}
		foreach($elements['colors'] as $color) {
			$outText .= "c==$color\n"; 
		}
		$outText .= "t==$someText\n";
		writeFile($outText, $dataFileName);		
	}
}

// Retrieve the parsed html contents for on cached web page
// as an array of strings.
function getFromCache($url) {
	$urlParts = testURL(sanitizeURL($url));
	$saveURL = buildURL($urlParts);
	$cachedir = "../cache";
	$dataFileName = $cachedir . "/" . makeFname($saveURL);
	$lines = loadFile($dataFileName);
	return $lines;
}

// Add one url to the urls log.  Most recent url is first.
// Keep only ten urls in the log.
// The urls log is a javascript file that is also read by the urls dropdown in the toolbar.
function logURL($safeURL) {
	$logFileContents = file_get_contents("../riot_urls.js");
	// strip off the var declaration so we have just the array of urls
	$logFileContents = preg_replace('/var riotUrlHistory = /', '', $logFileContents);
	$urls = $logFileContents ? json_decode($logFileContents) : array();		
	array_unshift($urls, $safeURL);
	$tenUrls = array_slice($urls, 0, 10);
	file_put_contents("../riot_urls.js", 'var riotUrlHistory = ' . json_encode($tenUrls));
}

// Return array containing three most recent urls from log.
function latestUrls() {
	$logFileContents = file_get_contents("../riot_urls.js");
	// strip off the var declaration so we have just the array of urls
	$logFileContents = preg_replace('/var riotUrlHistory = /', '', $logFileContents);
	$urls = $logFileContents ? json_decode($logFileContents) : array();
	return array_slice($urls, 0, 3);
}

// Output the Riotous HTML file
function outputRiotHTML($elements, $url) {
	global $pageNum;
	global $z;
	$urlParts = testURL($url);
	$text = $elements['text'];
	$colors = $elements['colors'];
	$images = $elements['images'];
	$videos = $elements['videos'];
	$mp4s = $elements['mp4s'];
	$links = $elements['links'];
	$linkTexts = $elements['linkTexts'];
	$title = $elements['title'];

	$scale = (4 - $pageNum);
	$scale = ($scale < 1) ? 1 : $scale;
	$ran100 = randint(0, 100);

	// body background image
	if ( sizeof($images) > 0 ) {
		$image = $images[randint(0, sizeof($images)-1)];
		print "<SCRIPT>document.body.style.backgroundImage = \"url('" . $image . "')\";</SCRIPT>\n";
	}

	// Pick an image and tile in a div behind other content
	print "<!-------- Background -------->\n";
	if ( sizeof($images) > 0 ) {
		$bgw = ((200 * $scale) + ($ran100 * 2)) . "px";
		$bgh = ((200 * $scale) + $ran100) . "px";
		$bgL = (((4 - $scale) * 150) + ($ran100 * 2)) . "px";
		$image = $images[randint(0, sizeof($images)-1)];
		print "<div id='BGLyr' style='position:absolute; left:$bgL; top:20px; width:$bgw; height:$bgh; background-image:url(" . makeFullHref($image, $urlParts) . "); clip:rect(10px $bgw $bgh 10px); z-index:$pageNum'>\n" ;
		print "</div>\n\n";
	}

	// Pick an image and position towards the right in foreground
	print "<!-------- Fg Img -------->\n";
	if ( sizeof($images) > 0 ) {
		$image = $images[randint(0, sizeof($images)-1)];
		$fgL = ((650 + $ran100) + ($pageNum * 100)) . "px";
		$fgT = (150 + $ran100) . "px";
		print "<div id=\"FlkLyr$z\" style=\"position:absolute; left:$fgL; top:$fgT; height:420px; z-index:$z\">\n" ;
		print "<img src='" . makeFullHref($image, $urlParts) . "' style='width:200px;'>\n";
		print "</div>\n\n";
		$z++;
	}

	// Scatter all images, scale some up
	print "<!-------- Images -------->\n";
	while ($image = array_shift($images)) {
		$imageURL = makeFullHref($image, $urlParts);
		$rX = "" . (randint(0, 600) + ($pageNum * 100)) . "px";
		$rY = "" . randint(10, 600) . "px";
		$rW = "" . randint(80, 280) . "px";
		$larger = randint(1, 3) > 1;   // split about 66/33
		print "<div id=\"ImgLyr$z\" style=\"position:absolute; left:$rX; top:$rY; z-index:$z\">\n" ;
		print "<img src='$imageURL' " . ($larger? "style='width:$rW;'" : "") . ">\n";
		print "</div>\n\n";
		$z++;
	}

	// Show a chunk of random text from the page
	print "<!-------- Text -------->\n";
	if ($text != "") {
		randomText($text, $colors);
		$pageNum++;   // Increment page number only if we displayed some text
	}

	// Show the page title near the top left
	print "<!-------- Title -------->\n";
	if ($title == "" ) {
		$title = $url;
	}
	$rX = ($z/2) . 'px';
	$rY = (($pageNum) * 30) . 'px';
	print "<div id=\"Lyr$z\" style=\"position:absolute; left:$rX; top:$rY; width:100px; height:20px; z-index:$z\">\n" ;
	print "<nobr><span class='largetext'>$title</span></nobr>\n";
	print "</div>\n\n";
	$z++;

	// Scatter colors randomly
	print "<!-------- colors -------->\n";
	$i = 0;
	while ($color = array_shift($colors)) {
		if ($i < 20) {
			# scatter colors randomly
			$rX = (randint(0, 600) + ($pageNum * 100)) . 'px';
			$rY = (randint(0, 600)) . 'px';
			$rW = (randint(0, 50) + 10) . 'px';
			print "<div id=\"ClrLyr$z\" style=\"position:absolute; left:$rX; top:$rY; width:$rW; height:$rW; background-color:$color; z-index:$z\">\n" ;
			print "&nbsp;&nbsp;&nbsp;\n";
			print "</div>\n\n";
			$i++;
			$z++;
	 	}
	}

	// Scatter links towards the right
	print "<!-------- Links -------->\n";
	while ($link = array_shift($links)) {
		$linkText = array_shift($linkTexts);
		$lpos = (randint(50, 650) + ($pageNum * 100)) . "px";
		$tpos = randint(50,650) . "px";
		$link = makeFullHref($link, $urlParts);
		print "<div id=\"LnkLyr$z\" style=\"position:absolute; left:$lpos; top:$tpos; width:100px; height:10px; z-index:$z\">\n" ;
		print "<A HREF='$link' onclick='riot(\"$link\");return false;'>$linkText</A>\n";
		print "</div>\n\n";
		$z++;
	}

	// print "<!-------- mp4s -------->\n";
	// while ($mp4url = array_shift($mp4s)) {
	// 	$rX = "" . randint(150, 500) . "px";
	// 	$rY = "" . randint(10, 50) . "px";
	// 	$clipW = "" . randint(50, 350) . "px";
	// 	$clipL = "" . randint(0, $clipW-20) . "px";
	// 	print "<div id='VidLyr$i' style='position:absolute; left:$rX; top:$rY; width:700px; height:700px; overflow:hidden; clip:rect(0px $clipW 700px $clipL); z-index:$i;'>\n";
	// 	print "  <video id='video1' controls='' autoplay loop style='height:100%'>\n";
	// 	print "    <source src='$mp4url' type='video/mp4'>\n";
	// 	print "  </video>\n";
	// 	print "</div>\n\n";
	// 	$i++;
	// }

	randomSentence();

	$z++;

	return 0;
}

// show Random text
function randomSentence() {
	global $mergedWords;
	global $z;

	print "<!-------- Random Sentence -------->\n";
	if (sizeof($mergedWords) > 0) {
		$t = "";
		for ($i=0; $i < 200; $i++) {
			if ($w = array_shift($mergedWords)) {
				$t = $t . " " . $w;
			}
		}
		$rY = randint(600, 800) . 'px';
		$rX = randint(200, 400) . 'px';
		print "<div id=\"Lyr$z\" style=\"position:absolute; left:$rX; top:$rY; width:650px; height:50px; z-index:$z\">\n" ;
		print "<span class='mediumtext'>$t</span>\n";
		print "</div>\n\n";
		$z++;
	}	
}

function randomText ($someText, $colors) {
	global $mergedWords;
	global $pageNum;
	global $z;
	$formInputs = array();

	if ($someText != "") {		# if text is not blank - show it
		$rTop = randint(100, 200);
		$rW = randint(200, 400) . 'px';
		$leftPos = ($rTop * ($pageNum + 1)) . 'px';
		$topPos = (200 + ($rTop * $pageNum)) . 'px';
		if (sizeof($colors) > 1) {
			$tmpColor = $colors[ randint(0, sizeof($colors)-1) ];
			$tmpbgColor = $colors[ randint(0, sizeof($colors)-1) ];
		} else {
			$tmpColor = "#333333";
			$tmpbgColor = "#ffffff";
		}
		if ($tmpColor == $tmpbgColor) {	# insure font color != bgcolor
			$tmpColor = "#3333c0";
		}
		if (sizeof($formInputs) > 0) {
			$inputTag = "<form>" . array_shift($formInputs) . "<\/form>\n";
		} else {
			$inputTag = "";
		}
		print "<div id=\"Lyr$z\" style=\"position:absolute; left:$leftPos; top:$topPos; width:$rW; height:10px; z-index:$z\">\n";
		print "<TABLE border=1 width=$rW><TR><TD style='background-color:$tmpbgColor;'><span style='color:$tmpColor;'><B>$someText</B></span>\n$inputTag</TD></TR></TABLE>";
		print "</div>\n\n";

		# Merge texts
		$mergedWordsIsEmpty = (sizeof($mergedWords) == 0);
		$tmpWords = array("");
		print "<!add someText=$someText>\n";
		$someWords = preg_split('/\s+/', $someText);   # array of words
		if (sizeof($someWords) >= 5) {
			print "<!add someWords=((($someWords[0] $someWords[1] $someWords[2] $someWords[3] $someWords[4] $someWords[5])))>\n";
		}
		// randomly mix words from someWords into mergedWords
		// The first time through, mergedWords will be empty, so we'll just copy someWords into mergedWords on pass 1
		for ($i=0; $i < 200; $i++) {
			$w = (randint(1,100) > 50) ? array_shift($someWords) : array_shift($mergedWords);
			if ($w) {
				array_push($tmpWords, $w);
			}
		}
		// mergedWords contains the mashed up words 
		$mergedWords = $tmpWords;
		if (sizeof($someWords) >= 5) {
			print "<!mergedWords=((($mergedWords[0] $mergedWords[1] $mergedWords[2] $mergedWords[3] $mergedWords[4] $mergedWords[5])))>\n";
		}
	}
}

// seed the random number generator
function initRandomGenerator() {
	$randomSeed = getParam('srand');  
	if (!isset($randomSeed)) {
		$randomSeed = time();
	}
	mt_srand($randomSeed);
}

function makePermalink($url, $randomVal) {
	global $RIOT_URL;
	global $RIOT_TITLE;
	$atag = "<div class='permalink'><a href='" . $RIOT_URL . $url . "&srand=" .$randomVal.  "&frame=n" . "' title='Permalink Shredder $url'>o-o $RIOT_TITLE</a></div>";
	return $atag;
}

function makeShreddableLink($href, $urlParts) {
	global $RIOT_URL;
	$fullHref = makeFullHref($href, $urlParts);
	$atag = '';
	if (strlen($fullHref) > 0) {
		$atag = "<a href='" . $RIOT_URL . $fullHref . "'>Shred it</a>";
	}
	return $atag;		
}

function makeFname($URL) {
	$URL = strtolower($URL);
	$URL = preg_replace('/[ \.\~\&\?\/\=\,\:\n\r]/', '_', $URL);  // kill special chars
	$fname = "$URL.hdata";
	return $fname;
}

// mt_rand returns an int between min and max inclusive
// mt_rand(0,1) will return a 50/50 distribution of 0 and 1
// intval() returns the floor, ie. intval(2.8) will return 2
function randint($min, $max) {
	return mt_rand($min, $max);
}

// Get the input (url to display)
function getUserUrl() {
	$url = getParam('url');
	if (isset($url)) {
		return $url;
	}
	return '';
}

function showFrame() {
	return strtolower(getParam('frame')) == 'y';
}

//--------------------------------------------------------------------------------------------------
// HTML, scripts and CSS are below
//--------------------------------------------------------------------------------------------------

function startHTMLpage() {
global $RIOT_TITLE;
global $RIOT_URL;
global $RIOT_WIDTH_PIXELS;
$top_display = showFrame() ? 'table-row' : 'none';
$frame = showFrame() ? 'y' : 'n';

$html = <<<EOT
	<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
	<html>
	<head>
	<title>$RIOT_TITLE</title>
	<script>
		function riot(URL) {
			if (parent.riotToolbar) {
				parent.riotToolbar.riotURL(URL);
			}
		}
		function setPageTitle(title) {
			document.title='$RIOT_TITLE: ' + title;
		}
		function setDisplayScale() {
			var width = window.innerWidth || document.body.clientWidth;
			var scale = width/$RIOT_WIDTH_PIXELS;
			var el = document.getElementById('riot-output');
			el.style.transformOrigin = 'left top'; 
			el.style.transform = 'scale(' + scale + ')';
		}
		function setPageLocation(url) {
			if (url && (input=document.getElementById('user-url-input'))) {
				input.value = url;
			}
		}
	</script>
	<style>
		html, body {
			height: 100%;
			width: 100%;
		}
		body {
			margin: 0;
			padding: 0;
			background-color: #fefefe;
		}
		.mediumtext {
			font-size: 26px;
			line-height: 26px;
			letter-spacing: 6px;
			color: #40c0b0;  /*#334033;*/
			background-color: #ffffff;
		}
		.largetext {
			font-family: Arial,sans-serif; 
			font-size: 19px; 
			line-height: 24px; 
			letter-spacing: 6px; 
			color: #ffffff; 
			background-color: #0000c0;
		}
		.verylargetext {
			letter-spacing: 6px;
			font-size: 94px;
			line-height: 27px;
		}
		.monospacetext {
			font-size: 18px;
			font-family: "Courier New", Courier, monospace;
		}
		.smalltext, .permalink {
			/* the original shredder default font size (-2) is equivalent to 10px (x-small) */
			font-family: 'arial';
			font-size: 10px;
			line-height: 1.2;
			margin-top: 0;
		}
		.permalink {
			text-decoration: none;
			position: absolute;
			left: 700px;
			top: 0px;
			width: 100px;
		}
		.permalink:hover {
			text-decoration: underline;
		}
		#riot-output {
			position: relative;
			margin: 2px 0px 0px 2px;
		}
	</style>
	</head>
	<body>
EOT;
print $html;
}

function endHTMLpage() {
$html = <<<EOT
	<script>
		if (parent.riotToolbar) { parent.riotToolbar.pageLoaded(); }
	</script>
	</body>
	</html>
EOT;
print $html;
}


function startFrame($url) {
	global $RIOT_URL_ROOT;
$html = <<<EOT
	<div id="outer-container">
		<div id="top">
			<iframe src="../toolbar.html" name="riotToolbar" scrolling="no" width="100%" height="38px"></iframe>
		</div>
		<div id="bottom">
			<div id="scroller">
EOT;
print showFrame() ? $html : '';
}

function endFrame() {
$html = <<<EOT
			</div> <!-- scroller -->
		</div> <!-- bottom -->
	</div> <!-- outer-container -->

EOT;
print showFrame() ? $html : '';
}

// Show message and exit
function errorExit($message)
{
	print "<div>\n";
	print "<h3>Error: $message</h3>\n";
	print "<p><a href='javascript:history.back()'>&lt; BACK</a></p>\n";
	print "</div>\n";
	print "<script>if(parent.riotToolbar){parent.riotToolbar.pageLoaded();}</script>\n";
	print "</body></html>\n";
	exit(-1);
}

?>